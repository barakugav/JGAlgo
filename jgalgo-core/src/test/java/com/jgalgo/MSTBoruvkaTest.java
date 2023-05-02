package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MSTBoruvkaTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x9bb8032ff5628f22L;
		MSTTestUtils.testRandGraph(new MSTBoruvka(), seed);
	}

}
