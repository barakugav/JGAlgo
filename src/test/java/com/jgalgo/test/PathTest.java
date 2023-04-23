package com.jgalgo.test;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.jgalgo.Graph;
import com.jgalgo.Path;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPDijkstra;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

public class PathTest extends TestBase {

	@Test
	public void testFindPath() {
		final long seed = 0x03afc698ec4c71ccL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		SSSP validationAlgo = new SSSPDijkstra();
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			int source = rand.nextInt(n);
			int target = rand.nextInt(n);

			Path actual = Path.findPath(g, source, target);
			Path expected = validationAlgo.computeShortestPaths(g, w -> 1, source).getPath(target);
			if (expected == null) {
				assertNull(actual, "found non existing path");
			} else {
				assertNotNull(actual, "failed to found a path");
				assertEquals(expected.size(), actual.size(), "failed to find shortest path");
			}
		});
	}

}
