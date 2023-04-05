package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTBoruvka1926;

public class MSTBoruvka1926Test extends TestUtils {

	@Test
	public void testRandGraph() {
		final long seed = 0x9bb8032ff5628f22L;
		MSTTestUtils.testRandGraph(MSTBoruvka1926::new, seed);
	}

}
