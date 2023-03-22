package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class RMQPowerOf2TableTest extends TestUtils {

	@Test
	public void testRegularNRange64to256() {
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQPowerOf2Table::new, n, 1024);
	}

	@Test
	public void testRegular65536() {
		RMQTestUtils.testRMQ65536(RMQPowerOf2Table::new);
	}

}
