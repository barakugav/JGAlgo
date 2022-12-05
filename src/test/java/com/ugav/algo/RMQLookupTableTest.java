package com.ugav.algo;

public class RMQLookupTableTest extends TestUtils {

	@Test
	public static void regularNRange64to256() {
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQLookupTable::new, n, 1024);
	}

	@Test
	public static void regular16384() {
		RMQTestUtils.testRMQ(RMQLookupTable::new, 16384, 4096);
	}
}
