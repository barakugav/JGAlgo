package com.ugav.algo.test;

import com.ugav.algo.HeapBinary;

public class HeapBinaryTest {

    @Test
    public static boolean regular() {
	return HeapTestUtils.testHeap(HeapBinary::new);
    }

}
