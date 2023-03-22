package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class MSTFredmanTarjan1987Test extends TestUtils {

	@Test
	public void testRandGraph() {
		MSTTestUtils.testRandGraph(MSTFredmanTarjan1987::new);
	}

}
