package com.jgalgo.test;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.GraphArrayUndirected;
import com.jgalgo.MSTKruskal;
import com.jgalgo.Path;
import com.jgalgo.TPM;
import com.jgalgo.UGraph;
import com.jgalgo.Weights;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class TPMTestUtils extends TestUtils {

	private TPMTestUtils() {
	}

	private static int[] calcExpectedTPM(Graph t, EdgeWeightFunc w, TPM.Queries queries) {
		int queriesNum = queries.size();
		int[] res = new int[queriesNum];
		for (int q = 0; q < queriesNum; q++) {
			IntIntPair query = queries.getQuery(q);
			int u = query.firstInt(), v = query.secondInt();

			Path path = Path.findPath(t, u, v);

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

	public static TPM.Queries generateAllPossibleQueries(int n) {
		TPM.Queries queries = new TPM.Queries();
		for (int i = 0; i < n; i++)
			for (int j = i; j < n; j++)
				queries.addQuery(i, j);
		return queries;
	}

	public static TPM.Queries generateRandQueries(int n, int m, long seed) {
		Random rand = new Random(seed);
		TPM.Queries queries = new TPM.Queries();
		for (int q = 0; q < m; q++)
			queries.addQuery(rand.nextInt(n), rand.nextInt(n));
		return queries;
	}

	static void compareActualToExpectedResults(TPM.Queries queries, int[] actual, int[] expected, EdgeWeightFunc w) {
		assertEquals(expected.length, actual.length, "Unexpected result size");
		for (int i = 0; i < actual.length; i++) {
			IntIntPair query = queries.getQuery(i);
			int u = query.firstInt(), v = query.secondInt();
			double aw = actual[i] != -1 ? w.weight(actual[i]) : Double.MIN_VALUE;
			double ew = expected[i] != -1 ? w.weight(expected[i]) : Double.MIN_VALUE;
			assertEquals(ew, aw, "Unexpected result for query (" + u + ", " + v
					+ "): " + actual[i] + " != " + expected[i]);
		}
	}

	static void testTPM(Supplier<? extends TPM> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 16), phase(32, 32), phase(16, 64), phase(8, 128), phase(4, 256),
				phase(2, 512), phase(1, 1234));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			TPM algo = builder.get();
			testTPM(algo, n, seedGen.nextSeed());
		});
	}

	private static void testTPM(TPM algo, int n, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Graph t = GraphsTestUtils.randTree(n, seedGen.nextSeed());
		EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(t, seedGen.nextSeed());

		TPM.Queries queries = n <= 32 ? generateAllPossibleQueries(n)
				: generateRandQueries(n, Math.min(n * 16, 1000), seedGen.nextSeed());
		int[] actual = algo.computeHeaviestEdgeInTreePaths(t, w, queries);
		int[] expected = calcExpectedTPM(t, w, queries);
		compareActualToExpectedResults(queries, actual, expected, w);
	}

	static void verifyMSTPositive(Supplier<? extends TPM> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 16), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 2048, 4096), phase(2, 8192, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(false).cycles(true).connected(true).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			IntCollection mstEdges = new MSTKruskal().computeMinimumSpanningTree(g, w);

			TPM algo = builder.get();
			boolean isMST = TPM.verifyMST(g, w, mstEdges, algo);
			assertTrue(isMST);
		});
	}

	static void verifyMSTNegative(Supplier<? extends TPM> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 16), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 2048, 4096), phase(2, 8192, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(false).cycles(true).connected(true).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			IntCollection mstEdges = new MSTKruskal().computeMinimumSpanningTree(g, w);
			Graph mst = new GraphArrayUndirected(g.vertices().size());
			Weights.Int edgeRef = mst.addEdgesWeights("edgeRef", int.class, Integer.valueOf(-1));
			for (IntIterator it = mstEdges.iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int e0 = mst.addEdge(u, v);
				edgeRef.set(e0, e);
			}

			Random rand = new Random(seedGen.nextSeed());
			int[] edges = g.edges().toIntArray();
			for (;;) {
				int badEdge;
				do {
					badEdge = edges[rand.nextInt(edges.length)];
				} while (mstEdges.contains(badEdge));

				Path mstPath = Path.findPath(mst, g.edgeSource(badEdge), g.edgeTarget(badEdge));
				int goodEdge = mstPath.getInt(rand.nextInt(mstPath.size()));

				if (w.weightInt(edgeRef.getInt(goodEdge)) < w.weightInt(badEdge)) {
					mstEdges.rem(goodEdge);
					mstEdges.add(badEdge);
					break;
				}
			}

			TPM algo = builder.get();
			boolean isMST = TPM.verifyMST(g, w, mstEdges, algo);
			assertFalse(isMST, "MST validation failed");
		});
	}

}
