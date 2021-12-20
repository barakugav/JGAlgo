package com.ugav.algo.test;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphArray;
import com.ugav.algo.Graphs;
import com.ugav.algo.MST;
import com.ugav.algo.MSTKruskal1956;
import com.ugav.algo.TPM;
import com.ugav.algo.test.GraphsTestUtils.RandomGraphBuilder;

class TPMTestUtils {

	private TPMTestUtils() {
		throw new InternalError();
	}

	static <E> Edge<E>[] calcExpectedTPM(Graph<E> t, WeightFunction<E> w, int[] queries) {
		int queriesNum = queries.length / 2;
		@SuppressWarnings("unchecked")
		Edge<E>[] res = new Edge[queriesNum];
		for (int q = 0; q < queriesNum; q++) {
			int u = queries[q * 2], v = queries[q * 2 + 1];

			List<Edge<E>> path = Graphs.findPath(t, u, v);

			Edge<E> maxEdge = null;
			double maxEdgeWeight = 0;
			for (Edge<E> e : path) {
				if (maxEdge == null || w.weight(e) > maxEdgeWeight) {
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

	static <E> boolean compareActualToExpectedResults(int[] queries, Edge<E>[] actual, Edge<E>[] expected,
			WeightFunction<E> w) {
		if (actual.length != expected.length) {
			TestUtils.printTestStr("Unexpected result size: " + actual.length + " != " + expected.length + "\n");
			return false;
		}
		for (int i = 0; i < actual.length; i++) {
			double aw = actual[i] != null ? w.weight(actual[i]) : Double.MIN_VALUE;
			double ew = expected[i] != null ? w.weight(expected[i]) : Double.MIN_VALUE;
			if (aw != ew) {
				int u = queries[i * 2];
				int v = queries[i * 2 + 1];
				TestUtils.printTestStr("Unexpected result for query (" + u + ", " + v + "): " + actual[i] + " != "
						+ expected[i] + "\n");
				return false;
			}
		}
		return true;
	}

	static boolean testTPM(TPM algo) {
		int[][] phases = new int[][] { { 64, 16 }, { 32, 32 }, { 16, 64 }, { 8, 128 }, { 4, 256 } };
		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];

			for (int r = 0; r < repeat; r++)
				if (!testTPM(algo, n))
					return false;
		}
		return true;
	}

	static boolean testTPM(TPM algo, int n) {
//		int[][] matrix = { { 0, 83, 0, 57, 0, 18, 0, 4 }, { 83, 0, 0, 32, 0, 79, 26, 0 }, { 0, 0, 0, 28, 53, 0, 90, 0 },
//				{ 57, 32, 28, 0, 0, 8, 14, 65 }, { 0, 0, 53, 0, 0, 68, 76, 0 }, { 18, 79, 0, 8, 68, 0, 89, 0 },
//				{ 0, 26, 90, 14, 76, 89, 0, 0 }, { 4, 0, 0, 65, 0, 0, 0, 0 } };
//		Graph<Integer> t = GraphsTestUtils.createGraphFromAdjacencyMatrixWeightedInt(matrix, DirectedType.Undirected);
//		WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
//		System.out.println("T:\n " + Graphs.formatAdjacencyMatrixWeightedInt(t, w));

		Graph<Integer> t = GraphsTestUtils.randTree(n);
		GraphsTestUtils.assignRandWeightsInt(t);
		WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
		return testTPM(t, w, algo);
	}

	static <E> boolean testTPM(Graph<E> t, WeightFunction<E> w, TPM algo) {
		RuntimeException e = null;
		try {
			if (testTPM0(t, w, algo))
				return true;
		} catch (RuntimeException e1) {
			e = e1;
		}
		TestUtils.printTestStr(Graphs.formatAdjacencyMatrixWeighted(t, w) + "\n");
		if (e != null)
			throw e;
		return false;
	}

	static <E> boolean testTPM0(Graph<E> t, WeightFunction<E> w, TPM algo) {
		int[] queries = generateAllPossibleQueries(t.vertices());
		Edge<E>[] actual = algo.calcTPM(t, w, queries, queries.length / 2);
		Edge<E>[] expected = calcExpectedTPM(t, w, queries);
		return compareActualToExpectedResults(queries, actual, expected, w);
	}

	static boolean verifyMSTPositive(TPM algo) {
		int[][] phases = new int[][] { { 64, 8, 16 }, { 32, 16, 32 }, { 16, 32, 64 }, { 8, 64, 128 }, { 4, 128, 256 } };
		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];
			int m = phases[phase][2];

			for (int r = 0; r < repeat; r++) {
				Graph<Integer> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false)
						.selfEdges(false).cycles(true).connected(true).build();
				GraphsTestUtils.assignRandWeightsInt(g);
				WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

				if (!verifyMSTPositive(algo, g, w))
					return false;
			}
		}
		return true;
	}

	static <E> boolean verifyMSTPositive(TPM algo, Graph<E> g, WeightFunction<E> w) {
		Collection<Edge<E>> mstEdges = MSTKruskal1956.getInstance().calcMST(g, w);

		return MST.verifyMST(g, w, mstEdges, algo);
	}

	static boolean verifyMSTNegative(TPM algo) {
		int[][] phases = new int[][] { { 64, 8, 16 }, { 32, 16, 32 }, { 16, 32, 64 }, { 8, 64, 128 }, { 4, 128, 256 } };
		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];
			int m = phases[phase][2];

			for (int r = 0; r < repeat; r++) {
				Graph<Integer> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false)
						.selfEdges(false).cycles(true).connected(true).build();
				GraphsTestUtils.assignRandWeightsInt(g);
				WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

				if (!verifyMSTNegative(algo, g, w))
					return false;
			}
		}
		return true;
	}

	static <E> boolean verifyMSTNegative(TPM algo, Graph<E> g, WeightFunction<E> w) {
		Collection<Edge<E>> mstEdges = MSTKruskal1956.getInstance().calcMST(g, w);
		Graph<E> mst = GraphArray.valueOf(g.vertices(), mstEdges, DirectedType.Undirected);

		@SuppressWarnings("unchecked")
		Edge<E>[] edges = g.edges().toArray(new Edge[g.edges().size()]);

		Random rand = new Random();
		Edge<E> e;
		do {
			e = edges[rand.nextInt(edges.length)];
		} while (mstEdges.contains(e));

		List<Edge<E>> mstPath = Graphs.findPath(mst, e.u(), e.v());
		mst.removeEdge(mstPath.get(rand.nextInt(mstPath.size())));
		mst.addEdge(e);

		return !MST.verifyMST(g, w, mst, algo);
	}

}
