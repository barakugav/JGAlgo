package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

public class MinimumMeanCycleHowardTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x6968128e5b6c70dfL;
		testMinimumMeanCycle(MinimumMeanCycleHoward::new, seed);
	}

	private static void testMinimumMeanCycle(Supplier<? extends MinimumMeanCycle> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 3, 2), phase(128, 16, 32), phase(64, 64, 128));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			// Graph g = new
			// RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
			// .selfEdges(true).cycles(true).connected(false).build();
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(false)
					.cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			MinimumMeanCycle algo = builder.get();
			Path cycle = algo.computeMinimumMeanCycle(g, w);
			verifyMinimumMeanCycle(g, w, cycle);
		});
	}

	private static void verifyMinimumMeanCycle(Graph g, EdgeWeightFunc w, Path cycle) {
		List<Path> cycles = new CyclesFinderTarjan().findAllCycles(g);
		if (cycle == null) {
			assertTrue(cycles.isEmpty(), "failed to find a cycle");
			return;
		}
		assertEquals(cycle.source(), cycle.target());
		int prevV = cycle.source();
		for (EdgeIter eit = cycle.edgeIter();;) {
			int e = eit.nextInt();
			assertEquals(prevV, g.edgeSource(e));
			prevV = g.edgeTarget(e);
			if (!eit.hasNext()) {
				assertEquals(cycle.target(), prevV);
				break;
			}
		}
		double cycleMeanWeight = getMeanWeight(cycle, w);

		for (Path c : cycles) {
			double cMeanWeight = getMeanWeight(c, w);
			assertTrue(cMeanWeight >= cycleMeanWeight, "found a cycle with smaller mean weight: " + c);
		}
	}

	private static double getMeanWeight(Path cycle, EdgeWeightFunc w) {
		return SSSPTestUtils.getPathWeight(cycle, w) / cycle.size();
	}

}
