package com.ugav.algo.test;

import java.util.Collection;
import java.util.stream.Collectors;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphArray;
import com.ugav.algo.GraphBipartite;
import com.ugav.algo.Graphs;
import com.ugav.algo.Matching;
import com.ugav.algo.MatchingBipartiteHopcroftKarp1973;
import com.ugav.algo.MatchingGabow1976;
import com.ugav.algo.MatchingWeighted;
import com.ugav.algo.MatchingWeightedBipartiteHungarianMethod;
import com.ugav.algo.MatchingWeightedBipartiteSSSP;
import com.ugav.algo.MatchingWeightedGabow2018;

class MatchingWeightedTestUtils {

	private MatchingWeightedTestUtils() {
		throw new InternalError();
	}

	static boolean randGraphsBipartiteWeighted(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 }, { 2, 1024, 1024, 8192 } };
		return TestUtils.runTestMultiple(phases, args -> {
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

	static boolean randGraphsWeighted(MatchingWeighted algo) {
//		int[][] phases = { /* { 256000, 4, 6 }, { 256000, 5, 10 }, */ { 100000, 6, 15 }, { 100000, 7, 21 },
//				{ 100000, 8, 28 }, { 12800, 16, 64 }, { 12800, 16, 120 }, { 12, 128, 128 }, { 8, 128, 512 } };
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 }, { 2, 1024, 1024, 8192 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];

			Graph<Integer> g = GraphsTestUtils.randGraph(n, m);
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			MatchingWeighted validationAlgo = new MatchingWeighted() {

				@Override
				public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g, WeightFunction<E> w) {
					return calcMaxMatchingSuffled(g, w, false);
				}

				@Override
				public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g, WeightFunction<E> w) {
					return calcMaxMatchingSuffled(g, w, true);
				}

				private static <E> Collection<Edge<E>> calcMaxMatchingSuffled(Graph<E> g, WeightFunction<E> w,
						boolean perfect) {
					int n = g.vertices();
					int[] suffle = Utils.randPermutation(n, TestUtils.nextRandSeed());
					Graph<Edge<E>> suffledG = new GraphArray<>(DirectedType.Undirected, n);
					g.edges().forEach(e -> suffledG.addEdge(suffle[e.u()], suffle[e.v()]).val(e));
					WeightFunction<Edge<E>> suffledW = e -> w.weight(e.val());
					Collection<Edge<Edge<E>>> res = perfect // have nothing else other than MatchingWeightedGabow2018
							? MatchingWeightedGabow2018.getInstance().calcPerfectMaxMatching(suffledG, suffledW)
							: MatchingWeightedGabow2018.getInstance().calcMaxMatching(suffledG, suffledW);
					return res.stream().map(e -> e.val()).collect(Collectors.toList());
				}

			};

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
			TestUtils.printTestStr("unexpected match weight: " + actualWeight + " < " + expectedWeight + "\n");
			System.out.println("expected " + expected);
			System.out.println("actual " + actual);
			return false;
		} else if (actualWeight > expectedWeight) {
			TestUtils.printTestStr(
					"matching is better than validation algo found: " + actualWeight + " > " + expectedWeight + "\n");
			System.out.println("expected " + expected);
			System.out.println("actual " + actual);
			throw new InternalError();
		}

		return true;
	}

	static boolean randBipartiteGraphsWeightedPerfect(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int sn = args[1];
			int tn = args[2];
			int m = args[3];

			GraphBipartite<Integer> g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m);
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			Matching validationAlgo = MatchingBipartiteHopcroftKarp1973.getInstance();
			return testGraphWeightedPerfect(algo, g, w, validationAlgo);
		});
	}

	static boolean randGraphsWeightedPerfect(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];

			Graph<Integer> g = GraphsTestUtils.randGraph(n, m);
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			Matching validationAlgo = MatchingGabow1976.getInstance();
			return testGraphWeightedPerfect(algo, g, w, validationAlgo);
		});
	}

	private static <E> boolean testGraphWeightedPerfect(MatchingWeighted algo, Graph<E> g, WeightFunctionInt<E> w,
			Matching validationAlgo) {
		Collection<Edge<E>> actual = algo.calcPerfectMaxMatching(g, w);
		if (!MatchingUnweightedTestUtils.validateMatching(actual))
			return false;

		Collection<Edge<E>> expected = validationAlgo.calcMaxMatching(g);

		if (actual.size() < expected.size()) {
			TestUtils.printTestStr("unexpected match size: " + actual.size() + " < " + expected.size() + "\n");
			System.out.println("expected " + expected);
			System.out.println("actual " + actual);
			return false;
		} else if (actual.size() > expected.size()) {
			TestUtils.printTestStr(
					"matching is better than validation algo found: " + actual.size() + " > " + expected.size() + "\n");
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

}
