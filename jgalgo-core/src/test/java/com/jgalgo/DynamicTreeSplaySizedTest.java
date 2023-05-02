package com.jgalgo;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jgalgo.DynamicTreeSplayTest.Op;

public class DynamicTreeSplaySizedTest extends TestBase {

	@Test
	public void testRandOps() {
		final long seed = 0x5ec72b4b420cd8d4L;
		List<Op> ops = List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut, Op.Size);
		var holder = new Object() {
			DynamicTreeSplayExtension.TreeSize treeSizeExt;
		};
		DynamicTreeSplayTest.testRandOps(maxWeight -> {
			holder.treeSizeExt = new DynamicTreeSplayExtension.TreeSize();
			return new DynamicTreeSplayExtended(maxWeight, List.of(holder.treeSizeExt));
		}, ops, node -> holder.treeSizeExt.getTreeSize(node), seed);
	}

	@Test
	public void testRandOpsInt() {
		final long seed = 0x9efac04f7e9404cdL;
		List<Op> ops = List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut, Op.Size);
		var holder = new Object() {
			DynamicTreeSplayExtension.TreeSize treeSizeExt;
		};
		DynamicTreeSplayTest.testRandOps(maxWeight -> {
			holder.treeSizeExt = new DynamicTreeSplayExtension.TreeSize();
			return new DynamicTreeSplayIntExtended((int) maxWeight, List.of(holder.treeSizeExt));
		}, ops, node -> holder.treeSizeExt.getTreeSize(node), seed);
	}
}
