package com.ugav.algo.test;

import java.util.List;
import java.util.Random;

import com.ugav.algo.Graph;
import com.ugav.algo.Graphs;
import com.ugav.algo.LCA;
import com.ugav.algo.LCARMQBenderFarachColton2000;

public class LCARMQBenderFarachColton2000Test extends TestUtils {

	private static int[][] randLCAQueries(Graph<Void> g, int r, int queriesNum) {
		Random rand = new Random(nextRandSeed());
		int[][] queries = new int[queriesNum][3];

		int n = g.vertices();
		int[] parent = new int[n];
		int[] depth = new int[n];

		Graphs.runBFS(g, r, (v, e) -> {
			if (e == null) {
				parent[v] = -1;
				depth[v] = 0;
			} else {
				int p = e.u();
				parent[v] = p;
				depth[v] = depth[p] + 1;
			}
			return true;
		});

		for (int query = 0; query < queriesNum; query++) {
			int u = rand.nextInt(n);
			int v = rand.nextInt(n);

			int uDepth = depth[u], vDepth = depth[v];
			/* assume v is deeper */
			int up = u, vp = v;
			if (uDepth > vDepth) {
				up = v;
				vp = u;
				int temp = uDepth;
				uDepth = vDepth;
				vDepth = temp;
			}
			/* iterate up to equal level */
			for (; uDepth < vDepth; vDepth--)
				vp = parent[vp];
			while (up != vp) {
				up = parent[up];
				vp = parent[vp];
			}
			int lca = up;

			queries[query][0] = u;
			queries[query][1] = v;
			queries[query][2] = lca;
		}
		return queries;
	}

	private static boolean testLCA(Graph<Void> g, LCA lca, int[][] queries) {
		LCA.Result result = lca.preprocessLCA(g, 0);

		for (int[] query : queries) {
			int u = query[0];
			int v = query[1];
			int expected = query[2];
			int actual = result.query(u, v);
			if (expected != actual) {
				printTestStr(" [" + u + "," + v + "] -> " + expected + "" + actual + "\n");
				return false;
			}
		}
		return true;
	}

	@Test
	public static boolean randTrees() {
		List<Phase> phases = List.of(phase(128, 16, 16), phase(64, 64, 64), phase(16, 512, 512), phase(4, 4096, 4096),
				phase(1, 16384, 16384));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			int m = args[1];
			Graph<Void> g = GraphsTestUtils.randTree(n);
			int[][] queries = randLCAQueries(g, 0, m);

			return testLCA(g, LCARMQBenderFarachColton2000.getInstace(), queries);
		});
	}

}
