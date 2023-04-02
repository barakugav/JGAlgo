package com.ugav.jgalgo.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.BFSIter;
import com.ugav.jgalgo.DFSIter;
import com.ugav.jgalgo.DiGraph;
import com.ugav.jgalgo.EdgeIter;
import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.Graphs;
import com.ugav.jgalgo.SSSP;
import com.ugav.jgalgo.SSSPDag;
import com.ugav.jgalgo.SSSPDijkstra;
import com.ugav.jgalgo.UGraph;
import com.ugav.jgalgo.Weights;
import com.ugav.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntList;

public class GraphsTest extends TestUtils {

	@Test
	public void testBfsConnected() {
		final long seed = 0xa782852da2497b7fL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).doubleEdges(false).doubleEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			int source = rand.nextInt(n);

			boolean[] visited = new boolean[n];
			List<Integer> invalidVertices = new ArrayList<>();
			for (BFSIter it = new BFSIter(g, source); it.hasNext();) {
				int v = it.nextInt();
				int e = it.inEdge();
				if (visited[v] || (v != source && g.edgeEndpoint(e, g.edgeEndpoint(e, v)) != v))
					invalidVertices.add(Integer.valueOf(v));
				visited[v] = true;
			}
			Assertions.assertTrue(invalidVertices.isEmpty());
		});
	}

	@Test
	public void testDfsConnected() {
		final long seed = 0x77678e2ce068199cL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			int source = rand.nextInt(n);

			boolean[] visited = new boolean[n];
			List<Integer> invalidVertices = new ArrayList<>();
			for (DFSIter it = new DFSIter(g, source); it.hasNext();) {
				int v = it.nextInt();
				IntList pathFromSource = it.edgePath();
				int e = v == source ? -1 : pathFromSource.getInt(pathFromSource.size() - 1);
				if (visited[v] || (v != source && g.edgeEndpoint(e, g.edgeEndpoint(e, v)) != v))
					invalidVertices.add(Integer.valueOf(v));
				visited[v] = true;
			}
			Assertions.assertTrue(invalidVertices.isEmpty());
		});
	}

	@Test
	public void testIsTreeUnrootedPositive() {
		final long seed = 0xb83f3ebfa35ba7a8L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();

			Assertions.assertTrue(Graphs.isTree(g));
		});
	}

	@Test
	public void testIsTreeUnrootedNegativeUnconnected() {
		final long seed = 0x77ec2f837d2f095bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int[] edges = g.edges().toIntArray();
			int e = edges[rand.nextInt(edges.length)];
			g.removeEdge(e);

			Assertions.assertFalse(Graphs.isTree(g));
		});
	}

	@Test
	public void testIsTreeUnrootedNegativeCycle() {
		final long seed = 0x2545a2e6fdbf259cL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int u, v;
			do {
				u = rand.nextInt(n);
				v = rand.nextInt(n);
			} while (u == v);
			g.addEdge(u, v);

			Assertions.assertFalse(Graphs.isTree(g));
		});
	}

	@Test
	public void testIsTreeRootedPositive() {
		final long seed = 0x15d7bb062a63d066L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int root = rand.nextInt(n);

			Assertions.assertTrue(Graphs.isTree(g, root));
		});
	}

	@Test
	public void testIsTreeRootedNegativeUnconnected() {
		final long seed = 0xa06f15857aeff09dL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int root = rand.nextInt(n);
			int[] edges = g.edges().toIntArray();
			int e = edges[rand.nextInt(edges.length)];
			g.removeEdge(e);

			Assertions.assertFalse(Graphs.isTree(g, root));
		});
	}

	@Test
	public void testIsTreeRootedNegativeCycle() {
		final long seed = 0xad27b6b0cb625eb3L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int root = rand.nextInt(n);
			int u, v;
			do {
				u = rand.nextInt(n);
				v = rand.nextInt(n);
			} while (u == v);
			g.addEdge(u, v);

			Assertions.assertFalse(Graphs.isTree(g, root));
		});
	}

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
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(4, 1024, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).doubleEdges(true)
					.selfEdges(false).cycles(false).connected(connected).build();

			int[] topolSort = Graphs.calcTopologicalSortingDAG(g);

			Set<Integer> seenVertices = new HashSet<>(n);
			for (int i = 0; i < n; i++) {
				int u = topolSort[i];
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					Assertions.assertFalse(seenVertices.contains(Integer.valueOf(v)));
				}
				seenVertices.add(Integer.valueOf(u));
			}
		});
	}

	@Test
	public void testDdistancesDAGUnconnected() {
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
			DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).doubleEdges(true)
					.selfEdges(false).cycles(false).connected(connected).build();
			GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			int source = 0;

			Weights.Int w = g.edgesWeight("weight");
			SSSP.Result result = ssspAlgo.calcDistances(g, w, source);

			SSSPTestUtils.validateResult(g, w, source, result, new SSSPDijkstra());
		});
	}

}
