/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

@SuppressWarnings("boxing")
public class MaximumFlowTestUtils extends TestUtils {

	private MaximumFlowTestUtils() {}

	private static Graph randGraph(int n, int m, Boolean2ObjectFunction<Graph> graphImpl, long seed, boolean directed) {
		return new RandomGraphBuilder(seed).n(n).m(m).directed(directed).parallelEdges(false).selfEdges(false)
				.cycles(true).connected(false).graphImpl(graphImpl).build();
	}

	static FlowNetwork randNetwork(Graph g, long seed) {
		final double minGap = 0.001;
		NavigableSet<Double> usedCaps = new TreeSet<>();

		Random rand = new Random(seed);
		FlowNetwork flow = FlowNetwork.createAsEdgeWeight(g);
		for (int e : g.edges()) {
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

	static FlowNetwork.Int randNetworkInt(Graph g, long seed) {
		Random rand = new Random(seed);
		FlowNetwork.Int flow = FlowNetwork.Int.createAsEdgeWeight(g);
		for (int e : g.edges())
			flow.setCapacity(e, rand.nextInt(16384));
		return flow;
	}

	static void testRandGraphs(MaximumFlow algo, long seed, boolean directed) {
		testRandGraphs(algo, GraphsTestUtils.defaultGraphImpl(), seed, directed);
	}

	static void testRandGraphsInt(MaximumFlow algo, long seed, boolean directed) {
		testRandGraphsInt(algo, GraphsTestUtils.defaultGraphImpl(), seed, directed);
	}

	static void testRandGraphs(MaximumFlow algo, Boolean2ObjectFunction<Graph> graphImpl, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 6, 6), phase(64, 16, 16), phase(64, 16, 32), phase(32, 64, 64),
				phase(32, 64, 128), phase(4, 512, 512), phase(2, 512, 1324), phase(1, 1025, 2016));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = randGraph(n, m, graphImpl, seedGen.nextSeed(), directed);
			FlowNetwork net = randNetwork(g, seedGen.nextSeed());
			int source, sink;
			for (int[] vs = g.vertices().toIntArray();;) {
				source = vs[rand.nextInt(vs.length)];
				sink = vs[rand.nextInt(vs.length)];
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			testNetwork(g, net, source, sink, algo);
		});
	}

	static void testRandGraphsInt(MaximumFlow algo, Boolean2ObjectFunction<Graph> graphImpl, long seed,
			boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 3, 3), phase(256, 6, 6), phase(64, 16, 16), phase(64, 16, 32),
				phase(32, 64, 64), phase(16, 64, 128), phase(2, 512, 512), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = randGraph(n, m, graphImpl, seedGen.nextSeed(), directed);
			FlowNetwork.Int net = randNetworkInt(g, seedGen.nextSeed());
			int source, sink;
			for (int[] vs = g.vertices().toIntArray();;) {
				source = vs[rand.nextInt(vs.length)];
				sink = vs[rand.nextInt(vs.length)];
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			testNetworkInt(g, net, source, sink, algo);
		});
	}

	static void testRandGraphsWithALotOfParallelEdges(MaximumFlow algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 3, 30), phase(256, 6, 150), phase(64, 10, 450), phase(64, 18, 1530));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seed).n(n).m(m).directed(directed).parallelEdges(true).selfEdges(false)
					.cycles(true).connected(false).build();
			FlowNetwork.Int net = randNetworkInt(g, seedGen.nextSeed());
			int source, sink;
			for (int[] vs = g.vertices().toIntArray();;) {
				source = vs[rand.nextInt(vs.length)];
				sink = vs[rand.nextInt(vs.length)];
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			testNetworkInt(g, net, source, sink, algo);
		});
	}

	private static void testNetwork(Graph g, FlowNetwork net, int source, int sink, MaximumFlow algo) {
		double actualMaxFlow = algo.computeMaximumFlow(g, net, source, sink);

		int n = g.vertices().size();
		Int2DoubleMap vertexFlowOut = new Int2DoubleOpenHashMap(n);
		for (int e : g.edges()) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut.put(u, vertexFlowOut.get(u) + net.getFlow(e));
			vertexFlowOut.put(v, vertexFlowOut.get(v) - net.getFlow(e));
		}
		for (int v : g.vertices()) {
			double expected = v == source ? actualMaxFlow : v == sink ? -actualMaxFlow : 0;
			assertEquals(expected, vertexFlowOut.get(v), 1E-3, "Invalid vertex(" + v + ") flow");
		}

		double expectedMaxFlow = calcExpectedFlow(g, net, source, sink);
		assertEquals(expectedMaxFlow, actualMaxFlow, 1E-3, "Unexpected max flow");
	}

	private static void testNetworkInt(Graph g, FlowNetwork.Int net, int source, int sink, MaximumFlow algo) {
		// Clear net, for debug 'drop to frame'
		// for (int e : g.edges())
		// net.setFlow(e, 0);

		double actualMaxFlow0 = algo.computeMaximumFlow(g, net, source, sink);
		int actualMaxFlow = (int) actualMaxFlow0;
		assertEquals(actualMaxFlow, actualMaxFlow0, "not integral max flow in integral network");

		int n = g.vertices().size();
		Int2IntMap vertexFlowOut = new Int2IntOpenHashMap(n);
		for (int e : g.edges()) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut.put(u, vertexFlowOut.get(u) + net.getFlowInt(e));
			vertexFlowOut.put(v, vertexFlowOut.get(v) - net.getFlowInt(e));
		}
		for (int v : g.vertices()) {
			int expected = v == source ? actualMaxFlow : v == sink ? -actualMaxFlow : 0;
			assertEquals(expected, vertexFlowOut.get(v), "Invalid vertex(" + v + ") flow");
		}

		int expectedMaxFlow = (int) calcExpectedFlow(g, net, source, sink);
		assertEquals(expectedMaxFlow, actualMaxFlow, "Unexpected max flow");
	}

	/* implementation taken from the Internet */

	private static double calcExpectedFlow(Graph g, FlowNetwork net, int source, int sink) {
		int n = g.vertices().size();
		double[][] capacities = new double[n][n];

		IndexIdMap vToIdx = g.indexGraphVerticesMap();

		for (int u : g.vertices()) {
			for (EdgeIter it = g.outEdges(u).iterator(); it.hasNext();) {
				int e = it.nextInt();
				int v = it.target();
				capacities[vToIdx.idToIndex(u)][vToIdx.idToIndex(v)] += net.getCapacity(e);
			}
		}

		return fordFulkerson(capacities, vToIdx.idToIndex(source), vToIdx.idToIndex(sink));
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
