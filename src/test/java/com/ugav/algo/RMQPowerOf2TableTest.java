package com.ugav.algo;

public class RMQPowerOf2TableTest extends TestUtils {

	@Test
	public static void regularNRange64to256() {
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQPowerOf2Table::new, n, 1024);
	}

	@Test
	public static void regular65536() {
		RMQTestUtils.testRMQ65536(RMQPowerOf2Table::new);
	}

}
