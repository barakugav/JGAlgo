package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MSTYao1976;

public class MSTYao1976Test extends TestUtils {

	@Test
	public void testRandGraph() {
		MSTTestUtils.testRandGraph(MSTYao1976::new);
	}

}
