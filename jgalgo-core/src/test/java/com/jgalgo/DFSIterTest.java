package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntList;

public class DFSIterTest extends TestBase {

	@Test
	public void testDfsConnected() {
		final long seed = 0x77678e2ce068199cL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			int source = rand.nextInt(n);

			BitSet visited = new BitSet(n);
			for (DFSIter it = new DFSIter(g, source); it.hasNext();) {
				int v = it.nextInt();
				IntList pathFromSource = it.edgePath();
				int e = v == source ? -1 : pathFromSource.getInt(pathFromSource.size() - 1);
				assertFalse(visited.get(v), "already visited vertex " + v);
				if (v != source)
					assertTrue(g.edgeEndpoint(e, g.edgeEndpoint(e, v)) == v, "v is not an endpoint of inEdge");
				visited.set(v);
			}
		});
	}

}
