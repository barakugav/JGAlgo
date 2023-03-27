package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.RMQPowerOf2Table;

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
