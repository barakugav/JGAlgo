package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

class APSPTestUtils extends TestUtils {

	private APSPTestUtils() {
	}

	static void testAPSPDirectedPositiveInt(Supplier<? extends APSP> builder, long seed) {
		testAPSPPositiveInt(builder, true, seed);
	}

	static void testAPSPUndirectedPositiveInt(Supplier<? extends APSP> builder, long seed) {
		testAPSPPositiveInt(builder, false, seed);
	}

	private static void testAPSPPositiveInt(Supplier<? extends APSP> builder, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 6, 20), phase(128, 16, 32), phase(64, 64, 256));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			testAPSP(g, w, builder, new SSSPDijkstra());
		});
	}

	static void testAPSPDirectedNegativeInt(Supplier<? extends APSP> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 6, 20), phase(64, 16, 32), phase(10, 64, 256));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			testAPSP(g, w, builder, new SSSPGoldberg());
		});
	}

	static void testAPSP(Graph g, EdgeWeightFunc w, Supplier<? extends APSP> builder, SSSP validationAlgo) {
		APSP algo = builder.get();
		APSP.Result result = algo.computeAllShortestPaths(g, w);

		int n = g.vertices().size();
		for (int source = 0; source < n; source++) {
			SSSP.Result expectedRes = validationAlgo.computeShortestPaths(g, w, source);

			if (result.foundNegativeCycle()) {
				Path cycle = null;
				try {
					cycle = result.getNegativeCycle();
				} catch (UnsupportedOperationException e) {
				}
				if (cycle != null) {
					double cycleWeight = SSSPTestUtils.getPathWeight(cycle, w);
					assertTrue(cycleWeight != Double.NaN, "Invalid cycle: " + cycle);
					assertTrue(cycleWeight < 0, "Cycle is not negative: " + cycle);
					if (!expectedRes.foundNegativeCycle())
						throw new IllegalStateException("validation algorithm didn't find negative cycle: " + cycle);
				} else {
					assertTrue(expectedRes.foundNegativeCycle(), "found non existing negative cycle");
				}
				return;
			}
			assertFalse(expectedRes.foundNegativeCycle(), "failed to found negative cycle");

			for (int target = 0; target < n; target++) {
				double expectedDistance = expectedRes.distance(target);
				double actualDistance = result.distance(source, target);
				assertEquals(expectedDistance, actualDistance, "Distance to vertex " + target + " is wrong");
				Path path = result.getPath(source, target);
				if (path != null) {
					double pathWeight = SSSPTestUtils.getPathWeight(path, w);
					assertEquals(pathWeight, actualDistance, "Path to vertex " + target
							+ " doesn't match distance (" + actualDistance + " != " + pathWeight + "): " + path);
				} else {
					assertEquals(Double.POSITIVE_INFINITY, actualDistance,
							"Distance to vertex " + target + " is not infinity but path is null");
				}
			}
		}
	}

}