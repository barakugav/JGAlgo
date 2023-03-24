package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

public class UnionFindArrayTest extends TestUtils {

	@Test
	public void testRandOps() {
		UnionFindTestUtils.randOps(UnionFindArray::new);
	}

}
