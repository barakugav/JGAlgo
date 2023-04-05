package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTFredmanTarjan1987;

public class MSTFredmanTarjan1987Test extends TestUtils {

	@Test
	public void testRandGraph() {
		final long seed = 0xaa99618f74cc983aL;
		MSTTestUtils.testRandGraph(MSTFredmanTarjan1987::new, seed);
	}

}
