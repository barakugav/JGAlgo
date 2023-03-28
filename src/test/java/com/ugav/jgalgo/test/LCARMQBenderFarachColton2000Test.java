package com.ugav.jgalgo.test;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.Graphs;
import com.ugav.jgalgo.LCARMQBenderFarachColton2000;
import com.ugav.jgalgo.LCAStatic;

public class LCARMQBenderFarachColton2000Test extends TestUtils {

	private static int[][] randLCAQueries(Graph g, int r, int queriesNum, long seed) {
		Random rand = new Random(seed);
		int[][] queries = new int[queriesNum][3];

		int n = g.vertices().size();
		int[] parent = new int[n];
		int[] depth = new int[n];

		for (Graphs.BFSIter it = new Graphs.BFSIter(g, r); it.hasNext();) {
			int v = it.nextInt();
			int e = it.inEdge();
			if (e == -1) {
				parent[v] = -1;
				depth[v] = 0;
			} else {
				int p = g.edgeEndpoint(e, v);
				parent[v] = p;
				depth[v] = depth[p] + 1;
			}
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

	private static void testLCA(Graph g, Supplier<? extends LCAStatic> builder, int[][] queries) {
		LCAStatic lca = builder.get();
		lca.preprocessLCA(g, 0);

		for (int[] query : queries) {
			int u = query[0];
			int v = query[1];
			int expected = query[2];
			int actual = lca.calcLCA(u, v);
			Assertions.assertEquals(expected, actual, "<- [" + u + "," + v + "]");
		}
	}

	@Test
	public void testRandTrees() {
		final long seed = 0x16f0491558fa62f8L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(64, 64, 64), phase(16, 512, 512), phase(4, 4096, 4096),
				phase(1, 16384, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			int[][] queries = randLCAQueries(g, 0, m, seedGen.nextSeed());
			testLCA(g, LCARMQBenderFarachColton2000::new, queries);
		});
	}

}
