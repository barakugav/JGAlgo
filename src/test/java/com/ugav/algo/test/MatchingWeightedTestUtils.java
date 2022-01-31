package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphArray;
import com.ugav.algo.GraphBipartite;
import com.ugav.algo.GraphBipartiteArray;
import com.ugav.algo.Graphs;
import com.ugav.algo.Matching;
import com.ugav.algo.MatchingBipartiteHopcroftKarp1973;
import com.ugav.algo.MatchingGabow1976;
import com.ugav.algo.MatchingWeighted;
import com.ugav.algo.MatchingWeightedBipartiteHungarianMethod;
import com.ugav.algo.MatchingWeightedBipartiteSSSP;
import com.ugav.algo.MatchingWeightedGabow2018;

class MatchingWeightedTestUtils extends TestUtils {

	private MatchingWeightedTestUtils() {
		throw new InternalError();
	}

	static boolean randGraphsBipartiteWeighted(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 }, { 2, 1024, 1024, 8192 } };
		return runTestMultiple(phases, args -> {
			int sn = args[1];
			int tn = args[2];
			int m = args[3];

			GraphBipartite<Integer> g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m);
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			MatchingWeighted validationAlgo = algo instanceof MatchingWeightedBipartiteSSSP
					? MatchingWeightedBipartiteHungarianMethod.getInstance()
					: MatchingWeightedBipartiteSSSP.getInstance();
			return testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	static boolean randBipartiteGraphsWeightedPerfect(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 } };
		return runTestMultiple(phases, args -> {
			int sn = args[1];
			int tn = args[2];
			int m = args[3];

			GraphBipartite<Integer> g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m);
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			Matching validationUnweightedAlgo = MatchingBipartiteHopcroftKarp1973.getInstance();
			MatchingWeighted validationWeightedAlgo = algo instanceof MatchingWeightedBipartiteHungarianMethodTest
					? MatchingWeightedGabow2018.getInstance()
					: MatchingWeightedBipartiteHungarianMethod.getInstance();
			return testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	static boolean randGraphsWeighted(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 }, { 2, 1024, 1024, 8192 } };
		return runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];

			Graph<Integer> g = GraphsTestUtils.randGraph(n, m);
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			// have nothing other than MatchingWeightedGabow2018, at least shuffle graph
			MatchingWeighted validationAlgo = new MatchingWeightedShuffled(MatchingWeightedGabow2018.getInstance());

			return testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	private static <E> boolean testGraphWeighted(MatchingWeighted algo, Graph<E> g, WeightFunctionInt<E> w,
			MatchingWeighted validationAlgo) {
		Collection<Edge<E>> actual = algo.calcMaxMatching(g, w);
		if (!MatchingUnweightedTestUtils.validateMatching(actual))
			return false;
		double actualWeight = calcMatchingWeight(actual, w);

		Collection<Edge<E>> expected = validationAlgo.calcMaxMatching(g, w);
		double expectedWeight = calcMatchingWeight(expected, w);

		if (actualWeight < expectedWeight) {
			printTestStr("unexpected match weight: " + actualWeight + " < " + expectedWeight + "\n");
			System.out.println("expected " + expected);
			System.out.println("actual " + actual);
			return false;
		} else if (actualWeight > expectedWeight) {
			printTestStr(
					"matching is better than validation algo found: " + actualWeight + " > " + expectedWeight + "\n");
			System.out.println("expected " + expected);
			System.out.println("actual " + actual);
			throw new InternalError();
		}

		return true;
	}

	static boolean randGraphsWeightedPerfect(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 } };
		return runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];

			Graph<Integer> g = GraphsTestUtils.randGraph(n, m);
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			Matching validationUnweightedAlgo = MatchingGabow1976.getInstance();
			MatchingWeighted validationWeightedAlgo = new MatchingWeightedShuffled(
					MatchingWeightedGabow2018.getInstance());
			return testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	private static <E> boolean testGraphWeightedPerfect(MatchingWeighted algo, Graph<E> g, WeightFunctionInt<E> w,
			Matching validationUnweightedAlgo, MatchingWeighted validationWeightedAlgo) {
		Collection<Edge<E>> actual = algo.calcPerfectMaxMatching(g, w);
		if (!MatchingUnweightedTestUtils.validateMatching(actual))
			return false;
		int actualSize = actual.size();
		double actualWeight = calcMatchingWeight(actual, w);

		int expectedSize = validationUnweightedAlgo.calcMaxMatching(g).size();
		if (actualSize < expectedSize) {
			printTestStr("unexpected match size: " + actualSize + " < " + expectedSize + "\n");
			System.out.println("expected " + expectedSize);
			System.out.println("actual " + actual);
			return false;
		} else if (actualSize > expectedSize) {
			printTestStr(
					"matching size is better than validation algo found: " + actualSize + " > " + expectedSize + "\n");
			throw new InternalError();
		}

		double expectedWeight = calcMatchingWeight(validationWeightedAlgo.calcPerfectMaxMatching(g, w), w);
		if (actualWeight < expectedWeight) {
			printTestStr("unexpected match weight: " + actualWeight + " < " + expectedWeight + "\n");
			System.out.println("expected " + expectedWeight);
			System.out.println("actual " + actual);
			return false;
		} else if (actualWeight > expectedWeight) {
			printTestStr("matching weight is better than validation algo found: " + actualWeight + " > "
					+ expectedWeight + "\n");
			throw new InternalError();
		}

		return true;
	}

	private static <E> double calcMatchingWeight(Collection<Edge<E>> matching, WeightFunction<E> w) {
		double sum = 0;
		for (Edge<E> e : matching)
			sum += w.weight(e);
		return sum;
	}

	private static class MatchingWeightedShuffled implements MatchingWeighted {

		private final MatchingWeighted algo;

		MatchingWeightedShuffled(MatchingWeighted algo) {
			this.algo = algo;
		}

		@Override
		public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g, WeightFunction<E> w) {
			return calcMaxMatchingshuffled(g, w, false);
		}

		@Override
		public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g, WeightFunction<E> w) {
			return calcMaxMatchingshuffled(g, w, true);
		}

		private <E> Collection<Edge<E>> calcMaxMatchingshuffled(Graph<E> g, WeightFunction<E> w, boolean perfect) {
			if (g.isDirected())
				throw new IllegalArgumentException("only undirected graphs are supported");
			int n = g.vertices();
			int[] shuffle = Utils.randPermutation(n, nextRandSeed());

			Graph<Edge<E>> shuffledG;
			if (g instanceof GraphBipartite) {
				GraphBipartite<E> gb = (GraphBipartite<E>) g;
				GraphBipartite<Edge<E>> shuffledGb = new GraphBipartiteArray<>(DirectedType.Undirected);

				int[] shuffleInv = new int[n];
				for (int v = 0; v < n; v++)
					shuffleInv[shuffle[v]] = v;

				for (int v = 0; v < n; v++) {
					int newv;
					if (gb.isVertexInS(shuffleInv[v]))
						newv = gb.newVertexS();
					else
						newv = gb.newVertexT();
					if (newv != v)
						throw new InternalError();
				}
				shuffledG = shuffledGb;
			} else {
				shuffledG = new GraphArray<>(DirectedType.Undirected, n);
			}

			g.edges().forEach(e -> shuffledG.addEdge(shuffle[e.u()], shuffle[e.v()]).val(e));
			WeightFunction<Edge<E>> shuffledW = e -> w.weight(e.val());

			Collection<Edge<Edge<E>>> shuffledEdges = perfect ? algo.calcPerfectMaxMatching(shuffledG, shuffledW)
					: algo.calcMaxMatching(shuffledG, shuffledW);

			List<Edge<E>> unshuffledEdges = new ArrayList<>(shuffledEdges.size());
			for (Edge<Edge<E>> e : shuffledEdges)
				unshuffledEdges.add(e.val());
			return unshuffledEdges;
		}

	}

}
