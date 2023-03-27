package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MSTBoruvka1926;

public class MSTBoruvka1926Test extends TestUtils {

	@Test
	public void testRandGraph() {
		MSTTestUtils.testRandGraph(MSTBoruvka1926::new);
	}

}
