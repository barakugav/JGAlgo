package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTPrim;

public class MSTPrimTest extends TestUtils {

	@Test
	public void testRandGraph() {
		final long seed = 0x9861dea860859a83L;
		MSTTestUtils.testRandGraph(MSTPrim::new, seed);
	}

}
