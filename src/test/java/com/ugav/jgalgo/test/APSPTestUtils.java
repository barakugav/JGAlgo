package com.ugav.jgalgo.test;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.ugav.jgalgo.APSP;
import com.ugav.jgalgo.EdgeWeightFunc;
import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.SSSP;
import com.ugav.jgalgo.SSSPDijkstra;
import com.ugav.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntList;

class APSPTestUtils extends TestUtils {

	private APSPTestUtils() {
	}

	public static void testAPSPDirectedPositiveInt(Supplier<? extends APSP> builder, long seed) {
		testAPSPPositiveInt(builder, true, seed);
	}

	public static void testAPSPUndirectedPositiveInt(Supplier<? extends APSP> builder, long seed) {
		testAPSPPositiveInt(builder, false, seed);
	}

	private static void testAPSPPositiveInt(Supplier<? extends APSP> builder, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 6, 20), phase(128, 16, 32), phase(64, 64, 256));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).doubleEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			EdgeWeightFunc.Int w = g.edgesWeight("weight");
			testAPSP(g, w, builder);
		});
	}

	static void testAPSP(Graph g, EdgeWeightFunc w, Supplier<? extends APSP> builder) {
		APSP algo = builder.get();
		APSP.Result result = algo.calcDistances(g, w);
		SSSP validationAlgo = new SSSPDijkstra();

		int n = g.vertices().size();
		for (int source = 0; source < n; source++) {
			SSSP.Result expectedRes = validationAlgo.calcDistances(g, w, source);

			for (int target = 0; target < n; target++) {
				double expectedDistance = expectedRes.distance(target);
				double actualDistance = result.distance(source, target);
				if (expectedDistance != actualDistance)
					System.out.println();
				Assertions.assertEquals(expectedDistance, actualDistance, "Distance to vertex " + target + " is wrong");
				IntList path = result.getPath(source, target);
				if (path != null) {
					double pathWeight = SSSPTestUtils.getPathWeight(g, path, w);
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
