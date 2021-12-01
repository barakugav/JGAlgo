package com.ugav.algo.test;

import com.ugav.algo.HeapFibonacci;

public class HeapFibonacciTest {

	@Test
	public static boolean regular() {
		return HeapTestUtils.testHeap(HeapFibonacci::new);
	}

}
