package com.ugav.algo.test;

import com.ugav.algo.RMQLookupTable;

public class RMQLookupTableTest {

	@Test
	public static boolean regularNRange64to256() {
		for (int n = 64; n <= 256; n++)
			if (RMQTestUtils.testRMQ(RMQLookupTable.getInstace(), n, 1024) != true)
				return false;
		return true;
	}

	@Test
	public static boolean regular16384() {
		return RMQTestUtils.testRMQ(RMQLookupTable.getInstace(), 16384, 4096);
	}
}
