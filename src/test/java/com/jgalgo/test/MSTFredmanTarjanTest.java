package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTFredmanTarjan;

public class MSTFredmanTarjanTest extends TestUtils {

	@Test
	public void testRandGraph() {
		final long seed = 0xaa99618f74cc983aL;
		MSTTestUtils.testRandGraph(MSTFredmanTarjan::new, seed);
	}

}
