/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockhashjava;

import com.google.common.primitives.ImmutableIntArray;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Matteo Di Giovinazzo
 */
public class BlockHash {

    public static void main(String[] args) throws IOException {
        List<String> arguments = Arrays.asList(args);

        if (arguments.contains("--help")) {

            System.out.println("Usage: BlockHash [options] filenames");
            System.out.println("Options:");
            System.out.println("\t--quick\tUse quick hashing method. Default: False");
            System.out.println("\t--bits\tCreate hash of size N^2 bits. Default: 16");
            System.out.println("\t--size\tResize image to specified size before hashing, e.g. 256x256");
            System.out.println("\t--interpolation\tInterpolation method: 1 - nearest neightbor, 2 - bilinear, 3 - bicubic, 4 - antialias. Default: 1");
            System.out.println("\t--debug\tPrint hashes as 2D maps (for debugging)");
            System.out.println("filenames");

            return;
        }

        boolean quick = false;
        int bits = 16;
        int width = -1;
        int height = -1;
        int interpolation = 1;
        boolean debug = false;
        List<File> files = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String current = args[i];

            if ("--quick".equals(current)) {
                if (i + 1 < args.length) {
                    String next = args[++i];
                    quick = Boolean.parseBoolean(next);
                }
            } else if ("--bits".equals(current)) {
                if (i + 1 < args.length) {
                    String next = args[++i];
                    bits = Integer.parseInt(next);
                }
            } else if ("--size".equals(current)) {
                if (i + 1 < args.length) {
                    String next = args[++i];
                    String[] tmp = next.split("x");
                    if (tmp.length == 2) {
                        width = Integer.parseInt(tmp[0]);
                        height = Integer.parseInt(tmp[1]);
                    }
                }
            } else if ("--interpolation".equals(current)) {
                if (i + 1 < args.length) {
                    String next = args[++i];
                    interpolation = Integer.parseInt(next);
                }
            } else if ("--debug".equals(current)) {
                if (i + 1 < args.length) {
                    String next = args[++i];
                    debug = Boolean.parseBoolean(next);
                }

            } else {
                File f = new File(current);
                if (f.isFile()) {
                    files.add(f);
                }
            }
        }

        for (File file : files) {
            BufferedImage im = ImageIO.read(file);

            // TODO: convert from indexed/grayscale to RGB/RGBA 
            {
            }

            // TODO: resize
            if (width != -1 && height != -1) {

            }

            String hash = quick
                    ? blockhashEven(im, bits)
                    : blockhash(im, bits);
            System.out.printf("%s %s%n", hash, file.getName());

            if (debug) {
                // TODO
            }
        }
    }

    public static String blockhashEven(BufferedImage im, int bits) {
        ColorModel cm = im.getColorModel();
        boolean hasAlpha = cm.hasAlpha();

        final int width = im.getWidth();
        final int height = im.getHeight();
        int[] data = im.getRGB(0, 0, width, height, null, 0, width);
        int blocksizeX = width / bits;
        int blocksizeY = height / bits;

        ImmutableIntArray.Builder builder = ImmutableIntArray.builder();
        for (int y = 0; y < bits; y++) {
            for (int x = 0; x < bits; x++) {
                int value = 0;

                for (int iy = 0; iy < blocksizeY; iy++) {
                    for (int ix = 0; ix < blocksizeX; ix++) {
                        int cx = x * blocksizeX + ix;
                        int cy = y * blocksizeY + iy;
                        if (hasAlpha) {
                            value = totalValueRGBA(data, width, height, cx, cy);
                        } else {
                            value = totalValueRGB(data, width, height, cx, cy);
                        }
                    }
                }

                builder.add(value);
            }
        }

        final int[] result = builder.build().toArray();
        translateBlocksToBits(result, blocksizeX * blocksizeY);
        return bitsToHexhash(result);
    }

    public static String blockhash(BufferedImage im, int bits) {
        throw new UnsupportedOperationException();
    }

    public static int[] sorted(int[] src) {
        final int length = src.length;
        int[] dest = new int[length];
        System.arraycopy(src, 0, dest, 0, src.length);
        Arrays.sort(dest);
        return dest;
    }

    public static int[] subarray(int[] array, int from, int to) {
        int[] result = new int[to - from];
        System.arraycopy(array, from, result, 0, to - from);
        return result;
    }

    public static int median(int[] data) {
        int length = data.length;
        data = sorted(data);

        if (length % 2 == 0) {
            return (int) Math.rint(((float) data[length / 2 - 1] + (float) data[length / 2]) / 2f);
        } else {
            return data[length / 2];
        }
    }

    public static int totalValueRGBA(int[] data, int width, int height, int x, int y) {
        int rgba = data[x + y * width];
        int r = getRed(rgba);
        int g = getGreen(rgba);
        int b = getBlue(rgba);
        int a = getAlpha(rgba);
        if (a == 0) {
            return 765;
        }
        return r + g + b;
    }

    public static int totalValueRGB(int[] data, int width, int height, int x, int y) {
        int rgba = data[x + y * width];
        int r = getRed(rgba);
        int g = getGreen(rgba);
        int b = getBlue(rgba);
        return r + g + b;
    }

    public static int getRed(int rgba) {
        return (rgba & 0x000000FF);
    }

    public static int getGreen(int rgba) {
        return (rgba & 0x0000FF00) >> 8;
    }

    public static int getBlue(int rgba) {
        return (rgba & 0x00FF0000) >> 16;
    }

    public static int getAlpha(int rgba) {
        return (rgba & 0xFF000000) >> 24;
    }

    public static void translateBlocksToBits(int[] blocks, int pixelsPerBlock) {
        int halfBlockValue = pixelsPerBlock * 256 * 3 / 2;

        // Compare medians across four horizontal bands
        int bandsize = blocks.length / 4;
        for (int i = 0; i < 4; i++) {
            int from = i * bandsize;
            int to = (i + 1) * bandsize;
            int[] tmp = subarray(blocks, from, to);
            int m = median(tmp);
            for (int j = from; j < to; j++) {
                float v = blocks[j];

                // Output a 1 if the block is brighter than the median.
                // With images dominated by black or white, the median may
                // end up being 0 or the max value, and thus having a lot
                // of blocks of value equal to the median.  To avoid
                // generating hashes of all zeros or ones, in that case output
                // 0 if the median is in the lower value space, 1 otherwise
                blocks[j] = ((v > m) || (Math.abs(v - m) < 1.0 && m > halfBlockValue)) ? 1 : 0;
            }
        }
    }

    public static String bitsToHexhash(int[] bits) {
        throw new UnsupportedOperationException();
    }
}
