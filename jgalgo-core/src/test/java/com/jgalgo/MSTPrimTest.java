package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MSTPrimTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x9861dea860859a83L;
		MSTTestUtils.testRandGraph(MSTPrim::new, seed);
	}

}
