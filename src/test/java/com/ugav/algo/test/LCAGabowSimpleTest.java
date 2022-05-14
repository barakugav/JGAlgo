package com.ugav.algo.test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.ugav.algo.LCADynamic;
import com.ugav.algo.LCAGabowSimple;

public class LCAGabowSimpleTest extends TestUtils {

	@Test
	public static boolean randTrees() {
		return randTrees(LCAGabowSimple::new);
	}

	private static boolean randTrees(Supplier<? extends LCADynamic> builder) {
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096), phase(1, 4096, 16384));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			int m = args[1];
			return randTree(builder, n, m);
		});
	}

	@SuppressWarnings("boxing")
	private static boolean randTree(Supplier<? extends LCADynamic> builder, int n, int m) {
		if (n < 2)
			throw new IllegalArgumentException();
		Random rand = new Random(nextRandSeed());

		final int addLeafOp = 0;
		final int lcaOp = 1;
		int[] ops = new int[n - 2 + m];
		Arrays.fill(ops, 0, n - 2, addLeafOp);
		Arrays.fill(ops, n - 2, n - 2 + m, lcaOp);
		ops = Utils.suffle(ops, nextRandSeed());

		int[] algLabels = new int[n];
		int[] parent = new int[n];
		int[] depth = new int[n];
		int nodesCount = 0;
		Arrays.fill(parent, -1);

		/* create LCA and insert first two elements */
		LCADynamic lca = builder.get();
		int root = nodesCount++;
		algLabels[root] = lca.initTree();
		parent[root] = -1;
		depth[root] = 0;
		int firstLeaf = nodesCount++;
		algLabels[firstLeaf] = lca.addLeaf(algLabels[root]);
		parent[firstLeaf] = root;
		depth[firstLeaf] = depth[root] + 1;

		for (int op : ops) {
			switch (op) {
			case addLeafOp: {
				int p = rand.nextInt(nodesCount);
				int node = nodesCount++;
				algLabels[node] = lca.addLeaf(algLabels[p]);
				parent[node] = p;
				depth[node] = depth[p] + 1;
				break;
			}
			case lcaOp: {
				int x = rand.nextInt(nodesCount);
				int y = rand.nextInt(nodesCount);

				int x0 = x, y0 = y;
				if (depth[x0] > depth[y0]) {
					x0 = y;
					y0 = x;
				}
				while (depth[x0] < depth[y0])
					y0 = parent[y0];
				while (x0 != y0) {
					x0 = parent[x0];
					y0 = parent[y0];
				}

				int lcaExpected = x0;
				int lcaActual = lca.calcLCA(x, y);
				if (lcaExpected != lcaActual) {
					printTestStr("LCA has an expected value: ", lcaExpected, " != ", lcaActual, "\n");
					return false;
				}
				break;
			}
			default:
				throw new InternalError();
			}
		}
		return true;
	}

}
