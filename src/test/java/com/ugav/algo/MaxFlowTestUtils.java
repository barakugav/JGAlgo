package com.ugav.algo;

import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphImplTestUtils.GraphImpl;
import com.ugav.algo.GraphsTestUtils.RandomGraphBuilder;
import com.ugav.algo.MaxFlow.FlowEdgeDataDefault;
import com.ugav.algo.MaxFlow.FlowNetwork;
import com.ugav.algo.MaxFlow.FlowNetworkDefault;

@SuppressWarnings("boxing")
class MaxFlowTestUtils extends TestUtils {

	private MaxFlowTestUtils() {
		throw new InternalError();
	}

	private static Pair<Graph<FlowEdgeDataDefault>, FlowNetwork<FlowEdgeDataDefault>> randNetword(int n, int m,
			GraphImpl graphImpl) {
		Graph<FlowEdgeDataDefault> g = new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(false)
				.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();

		final double minGap = 0.001;
		NavigableSet<Double> usedCaps = new TreeSet<>();

		Random rand = new Random(nextRandSeed());
		for (Edge<FlowEdgeDataDefault> e : g.edges()) {
			double cap;
			for (;;) {
				cap = nextDouble(rand, 1, 100);
				Double lower = usedCaps.lower(cap);
				Double higher = usedCaps.higher(cap);
				if (lower != null && cap - lower < minGap)
					continue;
				if (higher != null && higher - cap < minGap)
					continue;
				break;
			}
			usedCaps.add(cap);

			e.setData(new FlowEdgeDataDefault(cap));
		}

		return Pair.of(g, new FlowNetworkDefault());
	}

	static void testRandGraphs(Supplier<? extends MaxFlow> builder) {
		testRandGraphs(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
	}

	static void testRandGraphs(Supplier<? extends MaxFlow> builder, GraphImpl graphImpl) {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(1024, 6, 6), phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64),
				phase(64, 64, 128), phase(8, 512, 512), phase(4, 512, 1324), phase(1, 1025, 2016),
				phase(1, 3246, 5612));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Pair<Graph<FlowEdgeDataDefault>, FlowNetwork<FlowEdgeDataDefault>> p = randNetword(n, m, graphImpl);
			Graph<FlowEdgeDataDefault> g = p.e1;
			FlowNetwork<FlowEdgeDataDefault> net = p.e2;
			int source, target;
			do {
				source = rand.nextInt(g.vertices());
				target = rand.nextInt(g.vertices());
			} while (source == target);

			MaxFlow algo = builder.get();
			testNetwork(g, net, source, target, algo);
		});
	}

	private static <E> void testNetwork(Graph<E> g, FlowNetwork<E> net, int source, int target, MaxFlow algo) {
		double actualMaxFlow = algo.calcMaxFlow(g, net, source, target);

		int n = g.vertices();
		double[] vertexFlowOut = new double[n];
		for (Edge<E> e : g.edges()) {
			vertexFlowOut[e.u()] += net.getFlow(e);
			vertexFlowOut[e.v()] -= net.getFlow(e);
		}
		for (int v = 0; v < n; v++) {
			double expected = v == source ? actualMaxFlow : v == target ? -actualMaxFlow : 0;
			assertEqFp(expected, vertexFlowOut[v], 1E-3, "Invalid vertex(", v, ") flow");
		}

		double expectedMaxFlow = calcExpectedFlow(g, net, source, target);
		assertEqFp(expectedMaxFlow, actualMaxFlow, 1E-3, "Unexpected max flow");
	}

	/* implementation taken from the Internet */

	private static <E> double calcExpectedFlow(Graph<E> g, FlowNetwork<E> net, int source, int target) {
		int n = g.vertices();
		double[][] capacities = new double[n][n];
		for (Edge<E> e : g.edges())
			capacities[e.u()][e.v()] += net.getCapacity(e);

		return fordFulkerson(capacities, source, target);
	}

	private static boolean bfs(double rGraph[][], int s, int t, int parent[]) {
		int n = rGraph.length;
		boolean[] visited = new boolean[n];
		LinkedList<Integer> queue = new LinkedList<>();
		queue.add(s);
		visited[s] = true;
		parent[s] = -1;
		while (queue.size() != 0) {
			int u = queue.poll();
			for (int v = 0; v < n; v++) {
				if (visited[v] == false && rGraph[u][v] > 0) {
					queue.add(v);
					parent[v] = u;
					visited[v] = true;
				}
			}
		}
		return (visited[t] == true);
	}

	private static double fordFulkerson(double graph[][], int s, int t) {
		int n = graph.length;
		int u, v;
		double[][] rGraph = new double[n][n];
		for (u = 0; u < n; u++)
			for (v = 0; v < n; v++)
				rGraph[u][v] = graph[u][v];
		int[] parent = new int[n];
		double max_flow = 0;
		while (bfs(rGraph, s, t, parent)) {
			double pathFlow = Double.MAX_VALUE;
			for (v = t; v != s; v = parent[v]) {
				u = parent[v];
				pathFlow = Math.min(pathFlow, rGraph[u][v]);
			}
			for (v = t; v != s; v = parent[v]) {
				u = parent[v];
				rGraph[u][v] -= pathFlow;
				rGraph[v][u] += pathFlow;
			}
			max_flow += pathFlow;
		}
		return max_flow;
	}

}
