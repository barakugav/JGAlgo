package com.jgalgo.test;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.jgalgo.APSP;
import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.Path;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPDijkstra;
import com.jgalgo.SSSPGoldberg1995;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

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
		List<Phase> phases = List.of(phase(128, 6, 20), phase(128, 16, 32), phase(64, 64, 256));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			testAPSP(g, w, builder, new SSSPGoldberg1995());
		});
	}

	static void testAPSP(Graph g, EdgeWeightFunc w, Supplier<? extends APSP> builder, SSSP validationAlgo) {
		APSP algo = builder.get();
		APSP.Result result = algo.calcDistances(g, w);

		int n = g.vertices().size();
		for (int source = 0; source < n; source++) {
			SSSP.Result expectedRes = validationAlgo.calcDistances(g, w, source);

			if (result.foundNegativeCycle()) {
				Path cycle = null;
				try {
					cycle = result.getNegativeCycle();
				} catch (UnsupportedOperationException e) {
				}
				if (cycle != null) {
					double cycleWeight = SSSPTestUtils.getPathWeight(cycle, w);
					Assertions.assertTrue(cycleWeight != Double.NaN, "Invalid cycle: " + cycle);
					Assertions.assertTrue(cycleWeight < 0, "Cycle is not negative: " + cycle);
					if (!expectedRes.foundNegativeCycle())
						throw new IllegalStateException("validation algorithm didn't find negative cycle: " + cycle);
				} else {
					Assertions.assertTrue(expectedRes.foundNegativeCycle(), "found non existing negative cycle");
				}
				return;
			}
			Assertions.assertFalse(expectedRes.foundNegativeCycle(), "failed to found negative cycle");

			for (int target = 0; target < n; target++) {
				double expectedDistance = expectedRes.distance(target);
				double actualDistance = result.distance(source, target);
				Assertions.assertEquals(expectedDistance, actualDistance, "Distance to vertex " + target + " is wrong");
				Path path = result.getPath(source, target);
				if (path != null) {
					double pathWeight = SSSPTestUtils.getPathWeight(path, w);
					Assertions.assertEquals(pathWeight, actualDistance, "Path to vertex " + target
							+ " doesn't match distance (" + actualDistance + " != " + pathWeight + "): " + path);
				} else {
					Assertions.assertEquals(Double.POSITIVE_INFINITY, actualDistance,
							"Distance to vertex " + target + " is not infinity but path is null");
				}
			}
		}
	}

}
