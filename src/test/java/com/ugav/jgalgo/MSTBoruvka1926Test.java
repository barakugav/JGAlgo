package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

public class MSTBoruvka1926Test extends TestUtils {

	@Test
	public void testRandGraph() {
		MSTTestUtils.testRandGraph(MSTBoruvka1926::new);
	}

}
