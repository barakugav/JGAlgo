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

package com.jgalgo.alg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

@SuppressWarnings("boxing")
public class MaximumFlowTestUtils extends TestUtils {

	private MaximumFlowTestUtils() {}

	private static Graph<Integer, Integer> randGraph(int n, int m,
			Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed, boolean directed) {
		for (SeedGenerator seedGen = new SeedGenerator(seed);;) {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(false).selfEdges(true).cycles(true).connected(false).graphImpl(graphImpl).build();

			boolean allSelfEdges = true;
			for (int e : g.edges())
				if (g.edgeSource(e) != g.edgeTarget(e))
					allSelfEdges = false;
			if (!allSelfEdges)
				return g;
		}
	}

	static <V, E> FlowNetwork<V, E> randNetwork(Graph<V, E> g, long seed) {
		final double minGap = 0.001;
		NavigableSet<Double> usedCaps = new TreeSet<>();

		Random rand = new Random(seed);
		FlowNetwork<V, E> flow = FlowNetwork.createFromEdgeWeights(g);
		for (E e : g.edges()) {
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

		/* given unknown FlowNetwork implementation sometimes */
		if (rand.nextInt(4) == 0) {
			if (flow instanceof IFlowNetwork && rand.nextBoolean()) {
				IFlowNetwork flowOrig = (IFlowNetwork) flow;

				@SuppressWarnings("unchecked")
				FlowNetwork<V, E> flowTemp = (FlowNetwork<V, E>) new IFlowNetwork() {
					@Override
					public double getCapacity(int edge) {
						return flowOrig.getCapacity(edge);
					}

					@Override
					public void setCapacity(int edge, double capacity) {
						flowOrig.setCapacity(edge, capacity);
					}

					@Override
					public double getFlow(int edge) {
						return flowOrig.getFlow(edge);
					}

					@Override
					public void setFlow(int edge, double flow) {
						flowOrig.setFlow(edge, flow);
					}
				};
				flow = flowTemp;
			} else {
				FlowNetwork<V, E> flowOrig = flow;
				flow = new FlowNetwork<>() {
					@Override
					public double getCapacity(E edge) {
						return flowOrig.getCapacity(edge);
					}

					@Override
					public void setCapacity(E edge, double capacity) {
						flowOrig.setCapacity(edge, capacity);
					}

					@Override
					public double getFlow(E edge) {
						return flowOrig.getFlow(edge);
					}

					@Override
					public void setFlow(E edge, double flow) {
						flowOrig.setFlow(edge, flow);
					}
				};
			}
		}

		return flow;
	}

	static <V, E> FlowNetworkInt<V, E> randNetworkInt(Graph<V, E> g, long seed) {
		Random rand = new Random(seed);
		FlowNetworkInt<V, E> flow = FlowNetworkInt.createFromEdgeWeights(g);
		for (E e : g.edges())
			flow.setCapacity(e, rand.nextInt(16384));

		/* given unknown FlowNetwork implementation sometimes */
		if (rand.nextInt(4) == 0) {
			if (flow instanceof IFlowNetworkInt && rand.nextBoolean()) {
				IFlowNetworkInt flowOrig = (IFlowNetworkInt) flow;
				@SuppressWarnings("unchecked")
				FlowNetworkInt<V, E> flowTemp = (FlowNetworkInt<V, E>) new IFlowNetworkInt() {
					@Override
					public int getCapacityInt(int edge) {
						return flowOrig.getCapacityInt(edge);
					}

					@Override
					public void setCapacity(int edge, int capacity) {
						flowOrig.setCapacity(edge, capacity);
					}

					@Override
					public int getFlowInt(int edge) {
						return flowOrig.getFlowInt(edge);
					}

					@Override
					public void setFlow(int edge, int flow) {
						flowOrig.setFlow(edge, flow);
					}
				};
				flow = flowTemp;

			} else {
				FlowNetworkInt<V, E> flowOrig = flow;
				flow = new FlowNetworkInt<>() {
					@Override
					public int getCapacityInt(E edge) {
						return flowOrig.getCapacityInt(edge);
					}

					@Override
					public void setCapacity(E edge, int capacity) {
						flowOrig.setCapacity(edge, capacity);
					}

					@Override
					public int getFlowInt(E edge) {
						return flowOrig.getFlowInt(edge);
					}

					@Override
					public void setFlow(E edge, int flow) {
						flowOrig.setFlow(edge, flow);
					}
				};
			}
		}

		return flow;
	}

	static void testRandGraphs(MaximumFlow algo, long seed, boolean directed) {
		testRandGraphs(algo, GraphsTestUtils.defaultGraphImpl(), seed, directed);
	}

	static void testRandGraphsInt(MaximumFlow algo, long seed, boolean directed) {
		testRandGraphsInt(algo, GraphsTestUtils.defaultGraphImpl(), seed, directed);
	}

	public static void testRandGraphs(MaximumFlow algo, Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl,
			long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(256);
		tester.addPhase().withArgs(16, 16).repeat(64);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 64).repeat(32);
		tester.addPhase().withArgs(64, 128).repeat(32);
		tester.addPhase().withArgs(512, 512).repeat(4);
		tester.addPhase().withArgs(512, 1324).repeat(2);
		tester.addPhase().withArgs(1025, 2016).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = randGraph(n, m, graphImpl, seedGen.nextSeed(), directed);
			FlowNetwork<Integer, Integer> net = randNetwork(g, seedGen.nextSeed());

			Pair<Integer, Integer> sourceSink = chooseSourceSink(g, rand);
			testNetwork(g, net, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	public static void testRandGraphsMultiSourceMultiSink(MaximumFlow algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(256);
		tester.addPhase().withArgs(16, 16).repeat(64);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 64).repeat(32);
		tester.addPhase().withArgs(64, 128).repeat(32);
		tester.addPhase().withArgs(512, 512).repeat(4);
		tester.addPhase().withArgs(512, 1324).repeat(2);
		tester.addPhase().withArgs(1025, 2016).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g =
					randGraph(n, m, GraphsTestUtils.defaultGraphImpl(), seedGen.nextSeed(), directed);
			FlowNetwork<Integer, Integer> net = randNetwork(g, seedGen.nextSeed());

			final int sourcesNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
			final int sinksNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
			Pair<Collection<Integer>, Collection<Integer>> sourcesSinks =
					chooseMultiSourceMultiSink(g, sourcesNum, sinksNum, rand);

			testNetwork(g, net, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	static void testRandGraphsInt(MaximumFlow algo, Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl,
			long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(3, 3).repeat(256);
		tester.addPhase().withArgs(6, 6).repeat(256);
		tester.addPhase().withArgs(16, 16).repeat(64);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 64).repeat(32);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 512).repeat(2);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = randGraph(n, m, graphImpl, seedGen.nextSeed(), directed);
			FlowNetworkInt<Integer, Integer> net = randNetworkInt(g, seedGen.nextSeed());

			Pair<Integer, Integer> sourceSink = chooseSourceSink(g, rand);
			testNetwork(g, net, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	public static void testRandGraphsMultiSourceMultiSinkInt(MaximumFlow algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(256);
		tester.addPhase().withArgs(16, 16).repeat(64);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 64).repeat(32);
		tester.addPhase().withArgs(64, 128).repeat(32);
		tester.addPhase().withArgs(512, 512).repeat(4);
		tester.addPhase().withArgs(512, 1324).repeat(2);
		tester.addPhase().withArgs(1025, 2016).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g =
					randGraph(n, m, GraphsTestUtils.defaultGraphImpl(), seedGen.nextSeed(), directed);
			FlowNetworkInt<Integer, Integer> net = randNetworkInt(g, seedGen.nextSeed());

			final int sourcesNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
			final int sinksNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
			Pair<Collection<Integer>, Collection<Integer>> sourcesSinks =
					chooseMultiSourceMultiSink(g, sourcesNum, sinksNum, rand);
			testNetwork(g, net, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	static void testRandGraphsWithALotOfParallelEdges(MaximumFlow algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(3, 30).repeat(256);
		tester.addPhase().withArgs(6, 150).repeat(256);
		tester.addPhase().withArgs(10, 450).repeat(64);
		tester.addPhase().withArgs(18, 1530).repeat(64);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seed).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			FlowNetworkInt<Integer, Integer> net = randNetworkInt(g, seedGen.nextSeed());

			Pair<Integer, Integer> sourceSink = chooseSourceSink(g, rand);
			testNetwork(g, net, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	static <V, E> Pair<V, V> chooseSourceSink(Graph<V, E> g, Random rand) {
		for (int retry = 0;; retry++) {
			V source = Graphs.randVertex(g, rand);
			V sink = Graphs.randVertex(g, rand);
			if (!source.equals(sink) && Path.findPath(g, source, sink) != null)
				return Pair.of(source, sink);
			if (retry > 1000) {
				boolean allSelfEdges = true;
				for (E e : g.edges())
					if (g.edgeSource(e) != g.edgeTarget(e))
						allSelfEdges = false;
				if (allSelfEdges)
					throw new IllegalArgumentException(
							"all edges of the graph are self edges, no valid source and sink");
				throw new RuntimeException("failed to find source and sink after " + retry + " retries in graph: " + g);
			}
		}
	}

	static <V, E> Pair<Collection<V>, Collection<V>> chooseMultiSourceMultiSink(Graph<V, E> g, Random rand) {
		final int n = g.vertices().size();
		int sourcesNum, sinksNum;
		if (n < 2) {
			throw new IllegalArgumentException("too few vertices");
		} else if (n < 4) {
			sourcesNum = sinksNum = 1;
		} else if (n <= 6) {
			sourcesNum = sinksNum = 2;
		} else {
			sourcesNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
			sinksNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
		}
		return chooseMultiSourceMultiSink(g, sourcesNum, sinksNum, rand);
	}

	static <V, E> Pair<Collection<V>, Collection<V>> chooseMultiSourceMultiSink(Graph<V, E> g, int sourcesNum,
			int sinksNum, Random rand) {
		Collection<V> sources = new ObjectOpenHashSet<>(sourcesNum);
		Collection<V> sinks = new ObjectOpenHashSet<>(sinksNum);
		while (sources.size() < sourcesNum)
			sources.add(Graphs.randVertex(g, rand));
		while (sinks.size() < sinksNum) {
			V sink = Graphs.randVertex(g, rand);
			if (!sources.contains(sink))
				sinks.add(sink);
		}
		return Pair.of(sources, sinks);
	}

	private static <V, E> void testNetwork(Graph<V, E> g, FlowNetwork<V, E> net, V source, V sink, MaximumFlow algo) {
		double actualTotalFlow = algo.computeMaximumFlow(g, net, source, sink);

		assertValidFlow(g, net, source, sink, actualTotalFlow);

		double expectedTotalFlow = calcExpectedFlow(g, net, source, sink);
		assertEquals(expectedTotalFlow, actualTotalFlow, 1E-3, "Unexpected max flow");
	}

	private static <V, E> void testNetwork(Graph<V, E> g, FlowNetwork<V, E> net, Collection<V> sources,
			Collection<V> sinks, MaximumFlow algo) {
		double actualMaxFlow = algo.computeMaximumFlow(g, net, sources, sinks);

		int n = g.vertices().size();
		Object2DoubleMap<V> vertexFlowOut = new Object2DoubleOpenHashMap<>(n);
		for (E e : g.edges()) {
			V u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut.put(u, vertexFlowOut.getDouble(u) + net.getFlow(e));
			vertexFlowOut.put(v, vertexFlowOut.getDouble(v) - net.getFlow(e));
		}
		Set<V> sources0 = new ObjectOpenHashSet<>(sources);
		Set<V> sinks0 = new ObjectOpenHashSet<>(sinks);
		for (V v : g.vertices()) {
			double vFlow = vertexFlowOut.getDouble(v);
			if (sources0.contains(v)) {
				assertTrue(vFlow >= -1E-3, "negative flow for sink vertex: " + vFlow);
			} else if (sinks0.contains(v)) {
				assertTrue(vFlow <= 1E-3, "positive flow for sink vertex: " + vFlow);
			} else {
				assertEquals(0, vFlow, 1E-3, "Invalid vertex(" + v + ") flow");
			}
		}
		double sourcesFlowSum = 0;
		double sinksFlowSum = 0;
		for (V v : sources)
			sourcesFlowSum += vertexFlowOut.getDouble(v);
		for (V v : sinks)
			sinksFlowSum += vertexFlowOut.getDouble(v);
		assertEquals(actualMaxFlow, sourcesFlowSum, 1E-3);
		assertEquals(-actualMaxFlow, sinksFlowSum, 1E-3);

		double expectedMaxFlow = calcExpectedFlow(g, net, sources, sinks);
		assertEquals(expectedMaxFlow, actualMaxFlow, 1E-3, "Unexpected max flow");
	}

	static <V, E> void assertValidFlow(Graph<V, E> g, FlowNetwork<V, E> net, V source, V sink, double totalFlow) {
		int n = g.vertices().size();
		Object2DoubleMap<V> vertexFlowOut = new Object2DoubleOpenHashMap<>(n);
		for (E e : g.edges()) {
			V u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut.put(u, vertexFlowOut.getDouble(u) + net.getFlow(e));
			vertexFlowOut.put(v, vertexFlowOut.getDouble(v) - net.getFlow(e));
		}
		for (V v : g.vertices()) {
			double expected = v.equals(source) ? totalFlow : v.equals(sink) ? -totalFlow : 0;
			assertEquals(expected, vertexFlowOut.getDouble(v), 1E-3, "Invalid vertex(" + v + ") flow");
		}
	}

	static <V, E> void assertValidFlow(Graph<V, E> g, FlowNetwork<V, E> net, Collection<V> sources, Collection<V> sinks,
			double totalFlow) {
		int n = g.vertices().size();
		Object2DoubleMap<V> vertexFlowOut = new Object2DoubleOpenHashMap<>(n);
		for (E e : g.edges()) {
			V u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut.put(u, vertexFlowOut.getDouble(u) + net.getFlow(e));
			vertexFlowOut.put(v, vertexFlowOut.getDouble(v) - net.getFlow(e));
		}
		sources = new ObjectOpenHashSet<>(sources);
		sinks = new ObjectOpenHashSet<>(sinks);
		for (V v : g.vertices()) {
			if (sources.contains(v))
				assertTrue(vertexFlowOut.getDouble(v) >= -1e-9);
			if (sinks.contains(v))
				assertTrue(vertexFlowOut.getDouble(v) <= 1e-9);
		}
		double sourcesFlowSum = 0;
		double sinksFlowSum = 0;
		for (V v : sources)
			sourcesFlowSum += vertexFlowOut.getDouble(v);
		for (V v : sinks)
			sinksFlowSum += vertexFlowOut.getDouble(v);
		assertEquals(sourcesFlowSum, totalFlow, 1E-3);
		assertEquals(sinksFlowSum, -totalFlow, 1E-3);
	}

	static <V, E> void assertValidFlow(Graph<V, E> g, FlowNetwork<V, E> net, Collection<V> sources,
			Collection<V> sinks) {
		int n = g.vertices().size();
		Object2DoubleMap<V> vertexFlowOut = new Object2DoubleOpenHashMap<>(n);
		for (E e : g.edges()) {
			V u = g.edgeSource(e), v = g.edgeTarget(e);
			vertexFlowOut.put(u, vertexFlowOut.getDouble(u) + net.getFlow(e));
			vertexFlowOut.put(v, vertexFlowOut.getDouble(v) - net.getFlow(e));
		}
		sources = new ObjectOpenHashSet<>(sources);
		sinks = new ObjectOpenHashSet<>(sinks);
		for (V v : g.vertices()) {
			if (sources.contains(v))
				assertTrue(vertexFlowOut.getDouble(v) >= 0);
			if (sinks.contains(v))
				assertTrue(vertexFlowOut.getDouble(v) <= 0);
		}
		double sourcesFlowSum = 0;
		double sinksFlowSum = 0;
		for (V v : sources)
			sourcesFlowSum += vertexFlowOut.getDouble(v);
		for (V v : sinks)
			sinksFlowSum += vertexFlowOut.getDouble(v);
		assertEquals(sourcesFlowSum, -sinksFlowSum, 1E-3);
	}

	/* implementation taken from the Internet */

	private static <V, E> double calcExpectedFlow(Graph<V, E> g, FlowNetwork<V, E> net, V source, V sink) {
		int n = g.vertices().size();
		double[][] capacities = new double[n][n];

		IndexIdMap<V> vToIdx = g.indexGraphVerticesMap();

		for (V u : g.vertices()) {
			for (EdgeIter<V, E> it = g.outEdges(u).iterator(); it.hasNext();) {
				E e = it.next();
				V v = it.target();
				capacities[vToIdx.idToIndex(u)][vToIdx.idToIndex(v)] += net.getCapacity(e);
			}
		}

		return fordFulkerson(capacities, vToIdx.idToIndex(source), vToIdx.idToIndex(sink));
	}

	private static <V, E> double calcExpectedFlow(Graph<V, E> g, FlowNetwork<V, E> net, Collection<V> sources,
			Collection<V> sinks) {
		int n = g.vertices().size();
		double[][] capacities = new double[n + 2][n + 2];

		IndexIdMap<V> vToIdx = g.indexGraphVerticesMap();

		for (V u : g.vertices()) {
			for (EdgeIter<V, E> it = g.outEdges(u).iterator(); it.hasNext();) {
				E e = it.next();
				V v = it.target();
				capacities[vToIdx.idToIndex(u)][vToIdx.idToIndex(v)] += net.getCapacity(e);
			}
		}

		int source = n, sink = n + 1;
		double capacitiesSum = 0;
		for (E e : g.edges())
			capacitiesSum += net.getCapacity(e);
		for (V v : sources)
			capacities[source][vToIdx.idToIndex(v)] = capacitiesSum;
		for (V v : sinks)
			capacities[vToIdx.idToIndex(v)][sink] = capacitiesSum;

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
