package com.jgalgo.test;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jgalgo.DiGraph;
import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPDag;
import com.jgalgo.SSSPDijkstra;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

public class SSSPDagTest extends TestBase {

	@Test
	public void testDistancesDAGUnconnected() {
		final long seed = 0xbaa64a2aa57cb602L;
		distancesDAG(false, seed);
	}

	@Test
	public void testDistancesDAGConnected() {
		final long seed = 0x21ee13eb1bee6e46L;
		distancesDAG(true, seed);
	}

	private static void distancesDAG(boolean connected, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		SSSP ssspAlgo = new SSSPDag();
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(16, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(true).selfEdges(false).cycles(false).connected(connected).build();
			EdgeWeightFunc w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			int source = 0;

			SSSPTestUtils.testAlgo(g, w, source, ssspAlgo, new SSSPDijkstra());
		});
	}

}
