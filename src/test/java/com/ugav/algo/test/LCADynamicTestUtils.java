package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.ugav.algo.LCADynamic;

class LCADynamicTestUtils extends TestUtils {

	private LCADynamicTestUtils() {
		throw new InternalError();
	}

	static boolean fullBinaryTreesRandOps(Supplier<? extends LCADynamic> builder) {
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096), phase(1, 4096, 16384));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			Collection<Op> ops = generateRandOpsOnFullBinaryTree(n, m);
			return testLCA(builder, n, ops);
		});
	}

	static boolean randTrees(Supplier<? extends LCADynamic> builder) {
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096), phase(1, 4096, 16384));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			Collection<Op> ops = generateRandOps(n, m);
			return testLCA(builder, n, ops);
		});
	}

	static Collection<Op> generateRandOpsOnFullBinaryTree(int n, int m) {
		if (n < 2)
			throw new IllegalArgumentException();
		Random rand = new Random(nextRandSeed());

		final int addLeafOp = 0;
		final int lcaOp = 1;
		int[] opsOrder = new int[n - 2 + m];
		Arrays.fill(opsOrder, 0, n - 2, addLeafOp);
		Arrays.fill(opsOrder, n - 2, n - 2 + m, lcaOp);
		opsOrder = Utils.suffle(opsOrder, nextRandSeed());

		List<Op> ops = new ArrayList<>();
		int nodesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = nodesCount++;
		ops.add(new OpAddLeaf(root));
		nodesCount++;

		for (int op : opsOrder) {
			switch (op) {
			case addLeafOp: {
				int p = (nodesCount - 1) / 2;
				ops.add(new OpAddLeaf(p));
				nodesCount++;
				break;
			}
			case lcaOp: {
				int x = rand.nextInt(nodesCount);
				int y = rand.nextInt(nodesCount);
				ops.add(new OpLCAQuery(x, y));
				break;
			}
			default:
				throw new InternalError();
			}
		}
		return ops;
	}

	static Collection<Op> generateRandOps(int n, int m) {
		if (n < 2)
			throw new IllegalArgumentException();
		Random rand = new Random(nextRandSeed());

		final int addLeafOp = 0;
		final int lcaOp = 1;
		int[] opsOrder = new int[n - 2 + m];
		Arrays.fill(opsOrder, 0, n - 2, addLeafOp);
		Arrays.fill(opsOrder, n - 2, n - 2 + m, lcaOp);
		opsOrder = Utils.suffle(opsOrder, nextRandSeed());

		List<Op> ops = new ArrayList<>();
		int nodesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = nodesCount++;
		ops.add(new OpAddLeaf(root));
		nodesCount++;

		for (int op : opsOrder) {
			switch (op) {
			case addLeafOp: {
				int p = rand.nextInt(nodesCount);
				ops.add(new OpAddLeaf(p));
				nodesCount++;
				break;
			}
			case lcaOp: {
				int x = rand.nextInt(nodesCount);
				int y = rand.nextInt(nodesCount);
				ops.add(new OpLCAQuery(x, y));
				break;
			}
			default:
				throw new InternalError();
			}
		}
		return ops;
	}

	@SuppressWarnings("boxing")
	static boolean testLCA(Supplier<? extends LCADynamic> builder, int n, Collection<Op> ops) {
		int[] algLabels = new int[n];
		int[] parent = new int[n];
		int[] depth = new int[n];
		int nodesCount = 0;
		Arrays.fill(parent, -1);

		LCADynamic lca = builder.get();

		for (Op op0 : ops) {
			if (op0 instanceof OpInitTree) {
				int node = nodesCount++;
				algLabels[node] = lca.initTree();
				parent[node] = -1;
				depth[node] = 0;

			} else if (op0 instanceof OpAddLeaf) {
				OpAddLeaf op = (OpAddLeaf) op0;
				int node = nodesCount++;
				algLabels[node] = lca.addLeaf(algLabels[op.parent]);
				parent[node] = op.parent;
				depth[node] = depth[op.parent] + 1;

			} else if (op0 instanceof OpLCAQuery) {
				OpLCAQuery op = (OpLCAQuery) op0;

				int x0 = op.x, y0 = op.y;
				if (depth[x0] > depth[y0]) {
					x0 = op.y;
					y0 = op.x;
				}
				while (depth[x0] < depth[y0])
					y0 = parent[y0];
				while (x0 != y0) {
					x0 = parent[x0];
					y0 = parent[y0];
				}

				int lcaExpected = x0;
				int lcaActual = lca.calcLCA(op.x, op.y);
				if (lcaExpected != lcaActual) {
					printTestStr("LCA has an expected value: ", lcaExpected, " != ", lcaActual, "\n");
					return false;
				}

			} else
				throw new InternalError();
		}
		return true;
	}

	static class Op {
	}

	static class OpInitTree extends Op {
	}

	static class OpAddLeaf extends Op {
		final int parent;

		OpAddLeaf(int parent) {
			this.parent = parent;
		}
	}

	static class OpLCAQuery extends Op {
		final int x, y;

		OpLCAQuery(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}
