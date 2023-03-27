package com.ugav.jgalgo.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.DiGraph;
import com.ugav.jgalgo.EdgeIter;
import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.Graphs;
import com.ugav.jgalgo.SSSP;
import com.ugav.jgalgo.SSSPDijkstra;
import com.ugav.jgalgo.Weights;
import com.ugav.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntList;

public class GraphsTest extends TestUtils {

	@Test
	public void testBfsConnected() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph g = new RandomGraphBuilder().n(n).m(m).doubleEdges(false).doubleEdges(true).selfEdges(true)
					.cycles(true).connected(true).build();
			int source = rand.nextInt(n);

			boolean[] visited = new boolean[n];
			List<Integer> invalidVertices = new ArrayList<>();
			for (Graphs.BFSIter it = new Graphs.BFSIter(g, source); it.hasNext();) {
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
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(true).selfEdges(true).cycles(true)
					.connected(true).build();
			int source = rand.nextInt(n);

			boolean[] visited = new boolean[n];
			List<Integer> invalidVertices = new ArrayList<>();
			for (Graphs.DFSIter it = new Graphs.DFSIter(g, source); it.hasNext();) {
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
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();

			Assertions.assertTrue(Graphs.isTree(g));
		});
	}

	@Test
	public void testIsTreeUnrootedNegativeUnconnected() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
			int[] edges = g.edges().toIntArray();
			int e = edges[rand.nextInt(edges.length)];
			g.removeEdge(e);

			Assertions.assertFalse(Graphs.isTree(g));
		});
	}

	@Test
	public void testIsTreeUnrootedNegativeCycle() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
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
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
			int root = rand.nextInt(n);

			Assertions.assertTrue(Graphs.isTree(g, root));
		});
	}

	@Test
	public void testIsTreeRootedNegativeUnconnected() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
			int root = rand.nextInt(n);
			int[] edges = g.edges().toIntArray();
			int e = edges[rand.nextInt(edges.length)];
			g.removeEdge(e);

			Assertions.assertFalse(Graphs.isTree(g, root));
		});
	}

	@Test
	public void testIsTreeRootedNegativeCycle() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
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

//		public <E> Pair<Integer, int[]> findConnectivityComponents(Graph<E> g) {;

//		public <E> Pair<Integer, int[]> findStrongConnectivityComponents(Graph<E> g) {;

	@Test
	public void testTopologicalSortUnconnected() {
		topologicalSort(false);
	}

	@Test
	public void testTopologicalSortConnected() {
		topologicalSort(true);
	}

	private static void topologicalSort(boolean connected) {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(4, 1024, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			DiGraph g = (DiGraph) new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(true).selfEdges(false)
					.cycles(false).connected(connected).build();

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
		distancesDAG(false);
	}

	@Test
	public void testDistancesDAGConnected() {
		distancesDAG(true);
	}

	private static void distancesDAG(boolean connected) {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(16, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			DiGraph g = (DiGraph) new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(true).selfEdges(false)
					.cycles(false).connected(connected).build();
			GraphsTestUtils.assignRandWeightsIntPos(g);
			int source = 0;

			Weights.Int w = g.edgesWeight("weight");
			SSSP.Result result = Graphs.calcDistancesDAG(g, w, source);

			SSSPTestUtils.validateResult(g, w, source, result, new SSSPDijkstra());
		});
	}

}
