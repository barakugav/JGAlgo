package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

public class UnionFindPtrTest extends TestUtils {

	@Test
	public void testRandOps() {
		UnionFindTestUtils.randOps(UnionFindPtr::new);
	}

}
