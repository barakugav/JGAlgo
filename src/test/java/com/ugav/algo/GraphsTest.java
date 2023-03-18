package com.ugav.algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphsTestUtils.RandomGraphBuilder;

public class GraphsTest extends TestUtils {

	@Test
	public static void bfsConnected() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph<Void> g = new RandomGraphBuilder().n(n).m(m).doubleEdges(false).doubleEdges(true).selfEdges(true)
					.cycles(true).connected(true).build();
			int source = rand.nextInt(n);

			boolean[] visited = new boolean[n];
			List<Integer> invalidVertices = new ArrayList<>();
			Graphs.runBFS(g, source, (v, e) -> {
				if (visited[v] || (v != source && e.v() != v))
					invalidVertices.add(Integer.valueOf(v));
				visited[v] = true;
				return true;
			});
			invalidVertices.isEmpty();
		});
	}

	@Test
	public static void dfsConnected() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph<Void> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(true).selfEdges(true)
					.cycles(true).connected(true).build();
			int source = rand.nextInt(n);

			boolean[] visited = new boolean[n];
			List<Integer> invalidVertices = new ArrayList<>();
			Graphs.runDFS(g, source, (v, pathFromSource) -> {
				if (visited[v] || (v != source && pathFromSource.get(pathFromSource.size() - 1).v() != v))
					invalidVertices.add(Integer.valueOf(v));
				visited[v] = true;
				return true;
			});
			assertTrue(invalidVertices.isEmpty());
		});
	}

	@Test
	public static void isTreeUnrootedPositive() {
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph<Void> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();

			assertTrue(Graphs.isTree(g));
		});
	}

	@Test
	public static void isTreeUnrootedNegativeUnconnected() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph<Void> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
			@SuppressWarnings("unchecked")
			Edge<Void> e = g.edges().toArray(new Edge[n])[rand.nextInt(m)];
			g.removeEdge(e);

			assertFalse(Graphs.isTree(g));
		});
	}

	@Test
	public static void isTreeUnrootedNegativeCycle() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph<Void> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
			int u, v;
			do {
				u = rand.nextInt(n);
				v = rand.nextInt(n);
			} while (u == v);
			g.addEdge(u, v);

			assertFalse(Graphs.isTree(g));
		});
	}

	@Test
	public static void isTreeRootedPositive() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph<Void> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
			int root = rand.nextInt(n);

			assertTrue(Graphs.isTree(g, root));
		});
	}

	@Test
	public static void isTreeRootedNegativeUnconnected() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph<Void> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
			int root = rand.nextInt(n);
			@SuppressWarnings("unchecked")
			Edge<Void> e = g.edges().toArray(new Edge[n])[rand.nextInt(m)];
			g.removeEdge(e);

			assertFalse(Graphs.isTree(g, root));
		});
	}

	@Test
	public static void isTreeRootedNegativeCycle() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n - 1;
			Graph<Void> g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(false).connected(true).build();
			int root = rand.nextInt(n);
			int u, v;
			do {
				u = rand.nextInt(n);
				v = rand.nextInt(n);
			} while (u == v);
			g.addEdge(u, v);

			assertFalse(Graphs.isTree(g, root));
		});
	}

//		public <E> Pair<Integer, int[]> findConnectivityComponents(Graph<E> g) {;

//		public <E> Pair<Integer, int[]> findStrongConnectivityComponents(Graph<E> g) {;

	@Test
	public static void topologicalSortUnconnected() {
		topologicalSort(false);
	}

	@Test
	public static void topologicalSortConnected() {
		topologicalSort(true);
	}

	private static void topologicalSort(boolean connected) {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(4, 1024, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph.Directed<Void> g = (Graph.Directed<Void>) new RandomGraphBuilder().n(n).m(m).directed(true)
					.doubleEdges(true).selfEdges(false).cycles(false).connected(connected).<Void>build();

			int[] topolSort = Graphs.calcTopologicalSortingDAG(g);

			Set<Integer> seenVertices = new HashSet<>(n);
			for (int i = 0; i < n; i++) {
				int u = topolSort[i];
				for (Edge<Void> e : Utils.iterable(g.edges(u)))
					assertFalse(seenVertices.contains(Integer.valueOf(e.v())));
				seenVertices.add(Integer.valueOf(u));
			}
		});
	}

	@Test
	public static void distancesDAGUnconnected() {
		distancesDAG(false);
	}

	@Test
	public static void distancesDAGConnected() {
		distancesDAG(true);
	}

	private static void distancesDAG(boolean connected) {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(16, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph.Directed<Integer> g = (Graph.Directed<Integer>) new RandomGraphBuilder().n(n).m(m).directed(true)
					.doubleEdges(true).selfEdges(false).cycles(false).connected(connected).<Integer>build();
			GraphsTestUtils.assignRandWeightsIntPos(g);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
			int source = 0;

			SSSP.Result<Integer> result = Graphs.calcDistancesDAG(g, w, source);

			SSSPTestUtils.validateResult(g, w, source, result, new SSSPDijkstra());
		});
	}

}
