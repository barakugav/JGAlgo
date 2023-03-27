package com.ugav.jgalgo.test;

import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.MaxFlow;
import com.ugav.jgalgo.Pair;
import com.ugav.jgalgo.Weights;
import com.ugav.jgalgo.MaxFlow.FlowEdgeDataDefault;
import com.ugav.jgalgo.MaxFlow.FlowNetwork;
import com.ugav.jgalgo.MaxFlow.FlowNetworkDefault;
import com.ugav.jgalgo.test.GraphImplTestUtils.GraphImpl;
import com.ugav.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntIterator;

@SuppressWarnings("boxing")
class MaxFlowTestUtils extends TestUtils {

	private MaxFlowTestUtils() {
	}

	private static Pair<Graph, FlowNetwork> randNetwork(int n, int m, GraphImpl graphImpl) {
		Graph g = new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(false).selfEdges(false).cycles(true)
				.connected(false).graphImpl(graphImpl).build();

		final double minGap = 0.001;
		NavigableSet<Double> usedCaps = new TreeSet<>();

		Random rand = new Random(nextRandSeed());
		Weights<FlowEdgeDataDefault> data = g.addEdgesWeight("flowData").ofObjs();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
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

			data.set(e, new FlowEdgeDataDefault(cap));
		}

		return Pair.of(g, new FlowNetworkDefault(data));
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
			Pair<Graph, FlowNetwork> p = randNetwork(n, m, graphImpl);
			Graph g = p.e1;
			FlowNetwork net = p.e2;
			int source, target;
			do {
				source = rand.nextInt(g.vertices().size());
				target = rand.nextInt(g.vertices().size());
			} while (source == target);

			MaxFlow algo = builder.get();
			testNetwork(g, net, source, target, algo);
		});
	}

	private static void testNetwork(Graph g, FlowNetwork net, int source, int target, MaxFlow algo) {
		double actualMaxFlow = algo.calcMaxFlow(g, net, source, target);

		int n = g.vertices().size();
		double[] vertexFlowOut = new double[n];
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut[u] += net.getFlow(e);
			vertexFlowOut[v] -= net.getFlow(e);
		}
		for (int v = 0; v < n; v++) {
			double expected = v == source ? actualMaxFlow : v == target ? -actualMaxFlow : 0;
			Assertions.assertEquals(expected, vertexFlowOut[v], 1E-3, "Invalid vertex(" + v + ") flow");
		}

		double expectedMaxFlow = calcExpectedFlow(g, net, source, target);
		Assertions.assertEquals(expectedMaxFlow, actualMaxFlow, 1E-3, "Unexpected max flow");
	}

	/* implementation taken from the Internet */

	private static double calcExpectedFlow(Graph g, FlowNetwork net, int source, int target) {
		int n = g.vertices().size();
		double[][] capacities = new double[n][n];
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			capacities[u][v] += net.getCapacity(e);
		}

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
