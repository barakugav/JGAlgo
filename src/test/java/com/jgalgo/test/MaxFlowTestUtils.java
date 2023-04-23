package com.jgalgo.test;

import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

import com.jgalgo.DiGraph;
import com.jgalgo.FlowNetwork;
import com.jgalgo.Graph;
import com.jgalgo.MaxFlow;
import com.jgalgo.Path;
import com.jgalgo.test.GraphImplTestUtils.GraphImpl;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntIterator;

@SuppressWarnings("boxing")
public class MaxFlowTestUtils extends TestUtils {

	private MaxFlowTestUtils() {
	}

	private static DiGraph randGraph(int n, int m, GraphImpl graphImpl, long seed) {
		return (DiGraph) new RandomGraphBuilder(seed).n(n).m(m).directed(true).parallelEdges(false).selfEdges(false)
				.cycles(true).connected(false).graphImpl(graphImpl).build();
	}

	public static FlowNetwork randNetwork(DiGraph g, long seed) {
		final double minGap = 0.001;
		NavigableSet<Double> usedCaps = new TreeSet<>();

		Random rand = new Random(seed);
		FlowNetwork flow = FlowNetwork.createAsEdgeWeight(g);
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

			flow.setCapacity(e, cap);
		}

		return flow;
	}

	public static com.jgalgo.FlowNetwork.Int randNetworkInt(DiGraph g, long seed) {
		Random rand = new Random(seed);
		FlowNetwork.Int flow = FlowNetwork.Int.createAsEdgeWeight(g);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int cap = rand.nextInt(16384);
			flow.setCapacity(e, cap);
		}
		return flow;
	}

	static void testRandGraphs(Supplier<? extends MaxFlow> builder, long seed) {
		testRandGraphs(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT, seed);
	}

	static void testRandGraphsInt(Supplier<? extends MaxFlow> builder, long seed) {
		testRandGraphsInt(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT, seed);
	}

	static void testRandGraphs(Supplier<? extends MaxFlow> builder, GraphImpl graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 6, 6), phase(64, 16, 16), phase(64, 16, 32), phase(32, 64, 64),
				phase(32, 64, 128), phase(4, 512, 512), phase(2, 512, 1324), phase(1, 1025, 2016));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			DiGraph g = randGraph(n, m, graphImpl, seedGen.nextSeed());
			FlowNetwork net = randNetwork(g, seedGen.nextSeed());
			int source, sink;
			for (;;) {
				source = rand.nextInt(g.vertices().size());
				sink = rand.nextInt(g.vertices().size());
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			MaxFlow algo = builder.get();
			testNetwork(g, net, source, sink, algo);
		});
	}

	static void testRandGraphsInt(Supplier<? extends MaxFlow> builder, GraphImpl graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 6, 6), phase(64, 16, 16), phase(64, 16, 32), phase(32, 64, 64),
				phase(16, 64, 128), phase(2, 512, 512), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			DiGraph g = randGraph(n, m, graphImpl, seedGen.nextSeed());
			FlowNetwork.Int net = randNetworkInt(g, seedGen.nextSeed());
			int source, sink;
			for (;;) {
				source = rand.nextInt(g.vertices().size());
				sink = rand.nextInt(g.vertices().size());
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			MaxFlow algo = builder.get();
			testNetworkInt(g, net, source, sink, algo);
		});
	}

	private static void testNetwork(Graph g, FlowNetwork net, int source, int sink, MaxFlow algo) {
		double actualMaxFlow = algo.computeMaximumFlow(g, net, source, sink);

		int n = g.vertices().size();
		double[] vertexFlowOut = new double[n];
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut[u] += net.getFlow(e);
			vertexFlowOut[v] -= net.getFlow(e);
		}
		for (int v = 0; v < n; v++) {
			double expected = v == source ? actualMaxFlow : v == sink ? -actualMaxFlow : 0;
			assertEquals(expected, vertexFlowOut[v], 1E-3, "Invalid vertex(" + v + ") flow");
		}

		double expectedMaxFlow = calcExpectedFlow(g, net, source, sink);
		assertEquals(expectedMaxFlow, actualMaxFlow, 1E-3, "Unexpected max flow");
	}

	private static void testNetworkInt(Graph g, FlowNetwork.Int net, int source, int sink, MaxFlow algo) {
		// Clear net, for debug 'drop to frame'
		// for (IntIterator it = g.edges().iterator(); it.hasNext();)
		// net.setFlow(it.nextInt(), 0);

		double actualMaxFlow0 = algo.computeMaximumFlow(g, net, source, sink);
		int actualMaxFlow = (int) actualMaxFlow0;
		assertEquals(actualMaxFlow, actualMaxFlow0, "not integral max flow in integral network");

		int n = g.vertices().size();
		int[] vertexFlowOut = new int[n];
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut[u] += net.getFlowInt(e);
			vertexFlowOut[v] -= net.getFlowInt(e);
		}
		for (int v = 0; v < n; v++) {
			int expected = v == source ? actualMaxFlow : v == sink ? -actualMaxFlow : 0;
			assertEquals(expected, vertexFlowOut[v], "Invalid vertex(" + v + ") flow");
		}

		int expectedMaxFlow = (int) calcExpectedFlow(g, net, source, sink);
		assertEquals(expectedMaxFlow, actualMaxFlow, "Unexpected max flow");
	}

	/* implementation taken from the Internet */

	private static double calcExpectedFlow(Graph g, FlowNetwork net, int source, int sink) {
		int n = g.vertices().size();
		double[][] capacities = new double[n][n];
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			capacities[u][v] += net.getCapacity(e);
		}

		return fordFulkerson(capacities, source, sink);
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
