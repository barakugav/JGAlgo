package com.jgalgo.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.jgalgo.DiGraph;
import com.jgalgo.EdgeIter;
import com.jgalgo.TopologicalOrder;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

public class TopologicalOrderTest extends TestBase {

	@Test
	public void testTopologicalSortUnconnected() {
		final long seed = 0x858cb81cf8e5b5c7L;
		topologicalSort(false, seed);
	}

	@Test
	public void testTopologicalSortConnected() {
		final long seed = 0xef5ef391b897c354L;
		topologicalSort(true, seed);
	}

	private static void topologicalSort(boolean connected, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(2, 1024, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(true).selfEdges(false).cycles(false).connected(connected).build();

			int[] topolSort = TopologicalOrder.computeTopologicalSortingDAG(g);

			Set<Integer> seenVertices = new HashSet<>(n);
			for (int i = 0; i < n; i++) {
				int u = topolSort[i];
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					assertFalse(seenVertices.contains(Integer.valueOf(v)));
				}
				seenVertices.add(Integer.valueOf(u));
			}
		});
	}

}
