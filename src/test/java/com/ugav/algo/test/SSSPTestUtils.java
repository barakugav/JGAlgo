package com.ugav.algo.test;

import java.util.List;
import java.util.Random;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.Graphs;
import com.ugav.algo.SSSP;
import com.ugav.algo.SSSPBellmanFord;
import com.ugav.algo.SSSPDial1969;
import com.ugav.algo.SSSPDijkstra;
import com.ugav.algo.SSSPGoldberg1995;
import com.ugav.algo.test.GraphsTestUtils.RandomGraphBuilder;

class SSSPTestUtils {

	private SSSPTestUtils() {
		throw new InternalError();
	}

	static boolean testSSSPDirectedPositiveInt(SSSP algo) {
		return testSSSPPositiveInt(algo, true);
	}

	static boolean testSSSPUndirectedPositiveInt(SSSP algo) {
		return testSSSPPositiveInt(algo, false);
	}

	private static boolean testSSSPPositiveInt(SSSP algo, boolean directed) {
		Random rand = new Random(TestUtils.nextRandSeed());
		int[][] phases = { { 128, 16, 32 }, { 64, 64, 256 }, { 8, 512, 4096 }, { 1, 4096, 16384 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];
			Graph<Integer> g = new RandomGraphBuilder().n(n).m(m).directed(directed).doubleEdges(true).selfEdges(true)
					.cycles(true).connected(false).build();
			GraphsTestUtils.assignRandWeightsIntPos(g);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
			int source = rand.nextInt(g.vertices());

			SSSP.Result<Integer> actualRes = algo.calcDistances(g, w, source);

			SSSP validationAlgo = algo instanceof SSSPDijkstra ? SSSPDial1969.getInstace() : SSSPDijkstra.getInstace();
			return validateResult(g, w, source, actualRes, validationAlgo);
		});
	}

	static boolean testSSSPDirectedNegativeInt(SSSP algo) {
		int[][] phases = { { 512, 4, 4 }, { 128, 16, 32 }, { 64, 64, 256 }, { 8, 512, 4096 }, { 2, 1024, 4096 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];
			Graph<Integer> g = new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(true).selfEdges(true)
					.cycles(true).connected(true).build();
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
			int source = 0;

			SSSP.Result<Integer> actualRes = algo.calcDistances(g, w, source);

			SSSP validationAlgo = algo instanceof SSSPBellmanFord ? SSSPGoldberg1995.getInstace()
					: SSSPBellmanFord.getInstace();
			return validateResult(g, w, source, actualRes, validationAlgo);
		});
	}

	static <E> boolean validateResult(Graph<E> g, WeightFunction<E> w, int source, SSSP.Result<E> result,
			SSSP validationAlgo) {
		SSSP.Result<E> expectedRes = validationAlgo.calcDistances(g, w, source);

		if (result.foundNegativeCycle()) {
			List<Edge<E>> cycle = null;
			try {
				cycle = result.getNegativeCycle();
			} catch (UnsupportedOperationException e) {
			}
			if (cycle != null) {
				double cycleWeight = getPathWeight(cycle, w);
				if (cycleWeight == Double.NaN || cycle.get(0).u() != cycle.get(cycle.size() - 1).v()) {
					TestUtils.printTestStr("Invalid cycle: " + cycle + "\n");
					return false;
				}
				if (cycleWeight >= 0) {
					TestUtils.printTestStr("Cycle is not negative: " + cycle + "\n");
					return false;
				}
				if (!expectedRes.foundNegativeCycle())
					throw new InternalError("validation algorithm didn't find negative cycle: " + cycle);
			} else if (!expectedRes.foundNegativeCycle()) {
				TestUtils.printTestStr("found non existing negative cycle\n");
				return false;
			}
			return true;
		} else if (expectedRes.foundNegativeCycle()) {
			TestUtils.printTestStr("failed to found negative cycle\n");
			return false;
		}

		int n = g.vertices();
		for (int v = 0; v < n; v++) {
			double expectedDistance = expectedRes.distance(v);
			double actualDistance = result.distance(v);
			if (expectedDistance != actualDistance) {
				TestUtils.printTestStr(
						"Distance to vertex " + v + " is wrong: " + expectedDistance + " != " + actualDistance + "\n");
				return false;
			}
			List<Edge<E>> path = result.getPathTo(v);
			if (path != null) {
				double pathWeight = getPathWeight(path, w);
				if (pathWeight != actualDistance) {
					TestUtils.printTestStr("Path to vertex " + v + " doesn't match distance (" + actualDistance + " != "
							+ pathWeight + "): " + path + "\n");
					return false;
				}
			} else if (actualDistance != Double.POSITIVE_INFINITY) {
				TestUtils.printTestStr("Distance to vertex " + v + " is not infinity but path is null\n");
				return false;

			}
		}
		return true;
	}

	private static <E> double getPathWeight(List<Edge<E>> path, WeightFunction<E> w) {
		double totalWeight = 0;
		int prev = -1;
		for (Edge<E> e : path) {
			if (prev != -1 && prev != e.u())
				return Double.NaN;
			totalWeight += w.weight(e);
			prev = e.v();
		}
		return totalWeight;
	}

}
