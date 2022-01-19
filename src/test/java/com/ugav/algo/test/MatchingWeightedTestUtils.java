package com.ugav.algo.test;

import java.util.Collection;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphBipartite;
import com.ugav.algo.Graphs;
import com.ugav.algo.MatchingBipartiteHopcroftKarp1973;
import com.ugav.algo.MatchingWeighted;
import com.ugav.algo.MatchingWeightedBipartiteHungarianMethod;
import com.ugav.algo.MatchingWeightedBipartiteSSSP;

class MatchingWeightedTestUtils {

	private MatchingWeightedTestUtils() {
		throw new InternalError();
	}

	static boolean randBipartiteGraphsWeighted(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 }, { 2, 1024, 1024, 8192 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int sn = args[1];
			int tn = args[2];
			int m = args[3];

			return randBipartiteGraphsWeighted(algo, sn, tn, m);
		});
	}

	private static boolean randBipartiteGraphsWeighted(MatchingWeighted algo, int sn, int tn, int m) {
		GraphBipartite<Integer> g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m);
		GraphsTestUtils.assignRandWeightsIntNeg(g);
		WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

		MatchingWeighted validationAlgo = algo instanceof MatchingWeightedBipartiteSSSP
				? MatchingWeightedBipartiteHungarianMethod.getInstance()
				: MatchingWeightedBipartiteSSSP.getInstance();

		Collection<Edge<Integer>> actual = algo.calcMaxMatching(g, w);
		if (!MatchingTestUtils.validateMatching(actual))
			return false;
		double actualWeight = calcMatchingWeight(actual, w);

		Collection<Edge<Integer>> expected = validationAlgo.calcMaxMatching(g, w);
		double expectedWeight = calcMatchingWeight(expected, w);

		if (actualWeight < expectedWeight) {
			TestUtils.printTestStr("unexpected match weight: " + actualWeight + " < " + expectedWeight + "\n");
			System.out.println("expected " + expected);
			System.out.println("actual " + actual);
			return false;
		} else if (actualWeight > expectedWeight) {
			TestUtils.printTestStr(
					"matching is better than validation algo found: " + actualWeight + " > " + expectedWeight + "\n");
			throw new InternalError();
		}

		return true;
	}

	static boolean randBipartiteGraphsWeightedPerfect(MatchingWeighted algo) {
		int[][] phases = { { 256, 8, 8, 8 }, { 128, 16, 16, 64 }, { 12, 128, 128, 128 }, { 8, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 }, { 2, 1024, 1024, 8192 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int sn = args[1];
			int tn = args[2];
			int m = args[3];

			return randBipartiteGraphsWeightedPerfect(algo, sn, tn, m);
		});
	}

	private static boolean randBipartiteGraphsWeightedPerfect(MatchingWeighted algo, int sn, int tn, int m) {
		GraphBipartite<Integer> g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m);
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
		WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

		Collection<Edge<Integer>> actual = algo.calcPerfectMaxMatching(g, w);
		if (!MatchingTestUtils.validateMatching(actual))
			return false;

		Collection<Edge<Integer>> expected = MatchingBipartiteHopcroftKarp1973.getInstance().calcMaxMatching(g);

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
