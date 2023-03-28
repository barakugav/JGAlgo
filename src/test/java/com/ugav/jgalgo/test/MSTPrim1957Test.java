package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MSTPrim1957;

public class MSTPrim1957Test extends TestUtils {

	@Test
	public void testRandGraph() {
		final long seed = 0x9861dea860859a83L;
		MSTTestUtils.testRandGraph(MSTPrim1957::new, seed);
	}

}
