package com.ugav.algo.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.MaxFlow;
import com.ugav.algo.MaxFlow.FlowEdgeValueDefault;
import com.ugav.algo.MaxFlow.FlowNetwork;
import com.ugav.algo.MaxFlow.FlowNetworkDefault;
import com.ugav.algo.Pair;
import com.ugav.algo.test.GraphImplTestUtils.GraphImpl;
import com.ugav.algo.test.GraphsTestUtils.RandomGraphBuilder;

@SuppressWarnings("boxing")
class MaxFlowTestUtils extends TestUtils {

	private MaxFlowTestUtils() {
		throw new InternalError();
	}

	private static Pair<Graph<FlowEdgeValueDefault>, FlowNetwork<FlowEdgeValueDefault>> randNetword(int n, int m,
			GraphImpl graphImpl) {
		Graph<FlowEdgeValueDefault> g = new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(false)
				.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();

		Random rand = new Random(nextRandSeed());
		for (Edge<FlowEdgeValueDefault> e : g.edges()) {
			double cap;
			do {
				cap = rand.nextDouble() * 100;
			} while (Math.abs(cap) < 1E-10);
			e.val(new FlowEdgeValueDefault(cap));
		}

		return Pair.of(g, new FlowNetworkDefault());
	}

	static boolean testRandGraphs(Supplier<? extends MaxFlow> builder) {
		return testRandGraphs(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
	}

	static boolean testRandGraphs(Supplier<? extends MaxFlow> builder, GraphImpl graphImpl) {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(1024, 6, 6), phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64),
				phase(64, 64, 128), phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096),
				phase(1, 4096, 16384));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Pair<Graph<FlowEdgeValueDefault>, FlowNetwork<FlowEdgeValueDefault>> p = randNetword(n, m, graphImpl);
			Graph<FlowEdgeValueDefault> g = p.e1;
			FlowNetwork<FlowEdgeValueDefault> net = p.e2;
			int source, target;
			do {
				source = rand.nextInt(g.vertices());
				target = rand.nextInt(g.vertices());
			} while (source == target);

			MaxFlow algo = builder.get();
			return testNetwork(g, net, source, target, algo);
		});
	}

	private static <E> boolean testNetwork(Graph<E> g, FlowNetwork<E> net, int source, int target, MaxFlow algo) {
		double actualMaxFlow = algo.calcMaxFlow(g, net, source, target);

		int n = g.vertices();
		double[] vertexFlowOut = new double[n];
		for (Edge<E> e : g.edges()) {
			vertexFlowOut[e.u()] += net.getFlow(e);
			vertexFlowOut[e.v()] -= net.getFlow(e);
		}
		for (int v = 0; v < n; v++) {
			double expected = v == source ? actualMaxFlow : v == target ? -actualMaxFlow : 0;
			if (!doubleEql(vertexFlowOut[v], expected, 1E-3)) {
				printTestStr("Invalid vertex(", v, ") flow: ", vertexFlowOut[v], "\n");
				return false;
			}
		}

		double expectedMaxFlow = calcExpectedFlow(g, net, source, target);
		if (!doubleEql(expectedMaxFlow, actualMaxFlow, 1E-10)) {
			printTestStr("Unexpected max flow: ", expectedMaxFlow, " != ", actualMaxFlow, "\n");
			return false;
		}

		return true;
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
