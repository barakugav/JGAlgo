package com.ugav.algo.test;

import com.ugav.algo.BinomialHeap;

public class BinomialHeapTest {

	@Test
	public static boolean regular() {
		return HeapTestUtils.testHeap(BinomialHeap::new);
	}

}
