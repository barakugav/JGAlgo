package com.ugav.jgalgo.test;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.DynamicTreeSplaySized;
import com.ugav.jgalgo.test.DynamicTreeSplayTest.Op;

public class DynamicTreeSplaySizedTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0x5ec72b4b420cd8d4L;
		List<Op> ops = List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut, Op.Size);
		DynamicTreeSplayTest.testRandOps(DynamicTreeSplaySized::new, ops, seed);
	}
}
