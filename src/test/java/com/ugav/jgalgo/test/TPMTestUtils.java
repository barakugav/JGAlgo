package com.ugav.jgalgo.test;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.ugav.jgalgo.EdgeWeightFunc;
import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.GraphArrayUndirected;
import com.ugav.jgalgo.Graphs;
import com.ugav.jgalgo.MST;
import com.ugav.jgalgo.MSTKruskal1956;
import com.ugav.jgalgo.Path;
import com.ugav.jgalgo.TPM;
import com.ugav.jgalgo.UGraph;
import com.ugav.jgalgo.Weights;
import com.ugav.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

class TPMTestUtils extends TestUtils {

	private TPMTestUtils() {
	}

	private static int[] calcExpectedTPM(Graph t, EdgeWeightFunc w, int[] queries) {
		int queriesNum = queries.length / 2;
		int[] res = new int[queriesNum];
		for (int q = 0; q < queriesNum; q++) {
			int u = queries[q * 2], v = queries[q * 2 + 1];

			Path path = Graphs.findPath(t, u, v);

			int maxEdge = -1;
			double maxEdgeWeight = 0;
			for (IntIterator it = path.iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (maxEdge == -1 || w.weight(e) > maxEdgeWeight) {
					maxEdge = e;
					maxEdgeWeight = w.weight(e);
				}
			}
			res[q] = maxEdge;
		}
		return res;
	}

	static int[] generateAllPossibleQueries(int n) {
		int[] queries = new int[(n + 1) * n];
		for (int q = 0, i = 0; i < n; i++) {
			for (int j = i; j < n; j++, q++) {
				queries[q * 2] = i;
				queries[q * 2 + 1] = j;
			}
		}
		return queries;
	}

	static int[] generateRandQueries(int n, int m, long seed) {
		Random rand = new Random(seed);
		int[] queries = new int[m * 2];
		for (int q = 0; q < m; q++) {
			queries[q * 2] = rand.nextInt(n);
			queries[q * 2 + 1] = rand.nextInt(n);
		}
		return queries;
	}

	static void compareActualToExpectedResults(int[] queries, int[] actual, int[] expected, EdgeWeightFunc w) {
		Assertions.assertEquals(expected.length, actual.length, "Unexpected result size");
		for (int i = 0; i < actual.length; i++) {
			double aw = actual[i] != -1 ? w.weight(actual[i]) : Double.MIN_VALUE;
			double ew = expected[i] != -1 ? w.weight(expected[i]) : Double.MIN_VALUE;
			Assertions.assertEquals(ew, aw, "Unexpected result for query (" + queries[i * 2] + ", " + queries[i * 2 + 1]
					+ "): " + actual[i] + " != " + expected[i]);
		}
	}

	static void testTPM(Supplier<? extends TPM> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 16), phase(32, 32), phase(16, 64), phase(8, 128), phase(4, 256),
				phase(2, 512), phase(1, 2485), phase(1, 3254));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			TPM algo = builder.get();
			testTPM(algo, n, seedGen.nextSeed());
		});
	}

	private static void testTPM(TPM algo, int n, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Graph t = GraphsTestUtils.randTree(n, seedGen.nextSeed());
		GraphsTestUtils.assignRandWeightsIntPos(t, seedGen.nextSeed());
		EdgeWeightFunc.Int w = t.edgesWeight("weight");

		int[] queries = n <= 64 ? generateAllPossibleQueries(n)
				: generateRandQueries(n, Math.min(n * 64, 8192), seedGen.nextSeed());
		int[] actual = algo.calcTPM(t, w, queries, queries.length / 2);
		int[] expected = calcExpectedTPM(t, w, queries);
		compareActualToExpectedResults(queries, actual, expected, w);
	}

	static void verifyMSTPositive(Supplier<? extends TPM> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 16), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 2048, 4096), phase(2, 8192, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(true)
					.selfEdges(false).cycles(true).connected(true).build();
			GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			EdgeWeightFunc.Int w = g.edgesWeight("weight");
			IntCollection mstEdges = new MSTKruskal1956().calcMST(g, w);

			TPM algo = builder.get();
			Assertions.assertTrue(MST.verifyMST(g, w, mstEdges, algo));
		});
	}

	static void verifyMSTNegative(Supplier<? extends TPM> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 16), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 2048, 4096), phase(2, 8192, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(true)
					.selfEdges(false).cycles(true).connected(true).build();
			GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			EdgeWeightFunc.Int w = g.edgesWeight("weight");

			IntCollection mstEdges = new MSTKruskal1956().calcMST(g, w);
			Graph mst = new GraphArrayUndirected(g.vertices().size());
			Weights.Int edgeRef = mst.addEdgesWeight("edgeRef").defVal(-1).ofInts();
			for (IntIterator it = mstEdges.iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int e0 = mst.addEdge(u, v);
				edgeRef.set(e0, e);
			}

			Random rand = new Random(seedGen.nextSeed());
			int[] edges = g.edges().toIntArray();
			int e;
			do {
				e = edges[rand.nextInt(edges.length)];
			} while (mstEdges.contains(e));

			Path mstPath = Graphs.findPath(mst, g.edgeSource(e), g.edgeTarget(e));
			int edgeToRemove = mstPath.getInt(rand.nextInt(mstPath.size()));
			mst.removeEdge(edgeToRemove);
			int en = mst.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(en, e);

			TPM algo = builder.get();

			Assertions.assertFalse(MST.verifyMST(g, w, mst, algo, edgeRef), "MST validation failed");
		});
	}

}
