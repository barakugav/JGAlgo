package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.UnionFindArray;

public class UnionFindArrayTest extends TestUtils {

	@Test
	public void testRandOps() {
		UnionFindTestUtils.randOps(UnionFindArray::new);
	}

}
