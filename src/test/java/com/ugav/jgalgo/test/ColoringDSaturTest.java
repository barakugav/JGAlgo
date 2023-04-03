package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.Coloring;
import com.ugav.jgalgo.ColoringDSatur;

public class ColoringDSaturTest {

	@Test
	public void testRandGraphs() {
		final long seed = 0xaf95beb0ce86b8f2L;
		Coloring algo = new ColoringDSatur();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
