package com.jgalgo.test;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jgalgo.DynamicTreeSplaySized;
import com.jgalgo.DynamicTreeSplaySizedInt;
import com.jgalgo.test.DynamicTreeSplayTest.Op;

public class DynamicTreeSplaySizedTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0x5ec72b4b420cd8d4L;
		List<Op> ops = List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut, Op.Size);
		DynamicTreeSplayTest.testRandOps(DynamicTreeSplaySized::new, ops, seed);
	}

	@Test
	public void testRandOpsInt() {
		final long seed = 0x9efac04f7e9404cdL;
		List<Op> ops = List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut, Op.Size);
		DynamicTreeSplayTest.testRandOps(maxWeight -> new DynamicTreeSplaySizedInt<>((int) maxWeight), ops, seed);
	}
}
