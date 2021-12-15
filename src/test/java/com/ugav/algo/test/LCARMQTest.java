package com.ugav.algo.test;

import java.util.Random;

import com.ugav.algo.Graph;
import com.ugav.algo.Graphs;
import com.ugav.algo.LCA;
import com.ugav.algo.LCARMQ;

public class LCARMQTest {

	private static int[][] randLCAQueries(Graph<Void> g, int r, int queriesNum) {
		Random rand = new Random();
		int[][] queries = new int[queriesNum][3];

		int n = g.vertices();

		int[] layer = new int[n];
		int[] layerNext = new int[n];
		int layerLen = 0, layerNextLen;
		int[] parent = new int[n];
		int[] edges = new int[n];
		int[] depth = new int[n];

		/* perform BFS to explore the tree and calculate depth of every vertex */
		layer[layerLen++] = r;
		parent[r] = -1;
		for (int d = 0; layerLen > 0; d++) {
			layerNextLen = 0;

			for (int i = 0; i < layerLen; i++) {
				int p = layer[i];
				depth[p] = d;

				int edgesCount = g.getEdgesArrVs(p, edges, 0);
				for (int e = 0; e < edgesCount; e++) {
					int q = edges[e];
					if (q == parent[p])
						continue;
					layerNext[layerNextLen++] = q;
					parent[q] = p;
				}
			}
			int[] temp = layer;
			layer = layerNext;
			layerNext = temp;
			layerLen = layerNextLen;
		}

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

	private static boolean testLCA0(Graph<Void> g, LCA lca, int[][] queries) {
		LCA.Result result = lca.preprocessLCA(g, 0);

		for (int[] query : queries) {
			int u = query[0];
			int v = query[1];
			int expected = query[2];
			int actual = result.query(u, v);
			if (expected != actual) {
				TestUtils.printTestStr(" [" + u + "," + v + "] -> " + expected + "" + actual + "\n");
				return false;
			}
		}
		return true;
	}

	private static boolean testLCA(Graph<Void> g, LCA lca, int[][] queries) {
		RuntimeException e = null;
		try {
			if (testLCA0(g, lca, queries))
				return true;
		} catch (RuntimeException e1) {
			e = e1;
		}
		TestUtils.printTestStr(Graphs.formatAdjacencyMatrix(g));
		if (e != null)
			throw e;
		return false;
	}

	@Test
	public static boolean randTrees() {
		int[][] phases = { { 128, 16, 16 }, { 64, 64, 64 }, { 8, 512, 512 }, { 1, 4096, 4096 } };

		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];
			int m = phases[phase][2];

			for (int i = 0; i < repeat; i++) {
				Graph<Void> g = GraphsTestUtils.randTree(n);
				int[][] queries = randLCAQueries(g, 0, m);

				if (!testLCA(g, LCARMQ.getInstace(), queries))
					return false;
			}
		}
		return true;
	}

}
