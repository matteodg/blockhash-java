/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockhashjava;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Matteo Di Giovinazzo
 */
public class BlockHashTest {

    @Test
    public void testMedianOdd() {
        int[] src = new int[]{1, 2, 3};
        int expResult = 2;
        int result = BlockHash.median(src);
        assertEquals(expResult, result);
    }

    @Test
    public void testMedianEven() {
        int[] src = new int[]{1, 3};
        int expResult = 2;
        int result = BlockHash.median(src);
        assertEquals(expResult, result);
    }

    @Test
    public void testTotalValueRGB() {
        System.out.println("totalValueRGB");
        int[] data = new int[]{
            0x00112233
        };
        int width = 1;
        int height = 1;
        int x = 0;
        int y = 0;
        int expResult = 0x11 + 0x22 + 0x33;
        int result = BlockHash.totalValueRGB(data, width, height, x, y);
        assertEquals(expResult, result);
    }

    @Test
    public void testTotalValueRGBA1() {
        System.out.println("totalValueRGBA1");
        int[] data = new int[]{
            0x00112233
        };
        int width = 1;
        int height = 1;
        int x = 0;
        int y = 0;
        int expResult = 765;
        int result = BlockHash.totalValueRGBA(data, width, height, x, y);
        assertEquals(expResult, result);
    }

    @Test
    public void testTotalValueRGBA2() {
        System.out.println("totalValueRGBA2");
        int[] data = new int[]{
            0xCC112233
        };
        int width = 1;
        int height = 1;
        int x = 0;
        int y = 0;
        int expResult = 0x11 + 0x22 + 0x33;
        int result = BlockHash.totalValueRGBA(data, width, height, x, y);
        assertEquals(expResult, result);
    }

}
