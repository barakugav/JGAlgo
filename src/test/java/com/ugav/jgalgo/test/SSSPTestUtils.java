package com.ugav.jgalgo.test;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.ugav.jgalgo.EdgeWeightFunc;
import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.Graphs.PathIter;
import com.ugav.jgalgo.SSSP;
import com.ugav.jgalgo.SSSPBellmanFord;
import com.ugav.jgalgo.SSSPDial1969;
import com.ugav.jgalgo.SSSPDijkstra;
import com.ugav.jgalgo.SSSPGoldberg1995;
import com.ugav.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntList;

public class SSSPTestUtils extends TestUtils {

	private SSSPTestUtils() {
	}

	public static void testSSSPDirectedPositiveInt(Supplier<? extends SSSP> builder, long seed) {
		testSSSPPositiveInt(builder, true, seed);
	}

	public static void testSSSPUndirectedPositiveInt(Supplier<? extends SSSP> builder, long seed) {
		testSSSPPositiveInt(builder, false, seed);
	}

	private static void testSSSPPositiveInt(Supplier<? extends SSSP> builder, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).doubleEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			EdgeWeightFunc.Int w = g.edgesWeight("weight");
			int source = rand.nextInt(g.vertices().size());

			SSSP algo = builder.get();
			SSSP.Result actualRes = algo.calcDistances(g, w, source);

			SSSP validationAlgo = algo instanceof SSSPDijkstra ? new SSSPDial1969() : new SSSPDijkstra();
			validateResult(g, w, source, actualRes, validationAlgo);
		});
	}

	static void testSSSPDirectedNegativeInt(Supplier<? extends SSSP> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(512, 4, 4), phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(2, 1024, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).doubleEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			EdgeWeightFunc.Int w = g.edgesWeight("weight");
			int source = 0;

			SSSP algo = builder.get();
			SSSP.Result actualRes = algo.calcDistances(g, w, source);

			SSSP validationAlgo = algo instanceof SSSPBellmanFord ? new SSSPGoldberg1995() : new SSSPBellmanFord();
			validateResult(g, w, source, actualRes, validationAlgo);
		});
	}

	static void validateResult(Graph g, EdgeWeightFunc w, int source, SSSP.Result result, SSSP validationAlgo) {
		SSSP.Result expectedRes = validationAlgo.calcDistances(g, w, source);

		if (result.foundNegativeCycle()) {
			IntList cycle = null;
			try {
				cycle = result.getNegativeCycle();
			} catch (UnsupportedOperationException e) {
			}
			if (cycle != null) {
				double cycleWeight = getPathWeight(g, cycle, w);
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

		int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			double expectedDistance = expectedRes.distance(v);
			double actualDistance = result.distance(v);
			Assertions.assertEquals(expectedDistance, actualDistance, "Distance to vertex " + v + " is wrong");
			IntList path = result.getPathTo(v);
			if (path != null) {
				double pathWeight = getPathWeight(g, path, w);
				Assertions.assertEquals(pathWeight, actualDistance, "Path to vertex " + v + " doesn't match distance ("
						+ actualDistance + " != " + pathWeight + "): " + path);
			} else {
				Assertions.assertEquals(Double.POSITIVE_INFINITY, actualDistance,
						"Distance to vertex " + v + " is not infinity but path is null");
			}
		}
	}

	static double getPathWeight(Graph g, IntList path, EdgeWeightFunc w) {
		double totalWeight = 0;
		for (PathIter it = PathIter.of(g, path); it.hasNext();)
			totalWeight += w.weight(it.nextEdge());
		return totalWeight;
	}

}
