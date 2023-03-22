package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class RMQLookupTableTest extends TestUtils {

	@Test
	public void testRegularNRange64to256() {
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQLookupTable::new, n, 1024);
	}

	@Test
	public void testRegular16384() {
		RMQTestUtils.testRMQ(RMQLookupTable::new, 16384, 4096);
	}
}
