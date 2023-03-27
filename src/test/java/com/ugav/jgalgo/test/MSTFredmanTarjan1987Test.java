package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MSTFredmanTarjan1987;

public class MSTFredmanTarjan1987Test extends TestUtils {

	@Test
	public void testRandGraph() {
		MSTTestUtils.testRandGraph(MSTFredmanTarjan1987::new);
	}

}
