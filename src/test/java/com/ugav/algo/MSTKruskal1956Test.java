package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class MSTKruskal1956Test extends TestUtils {

	@Test
	public void testRandGraph() {
		MSTTestUtils.testRandGraph(MSTKruskal1956::new);
	}

}
