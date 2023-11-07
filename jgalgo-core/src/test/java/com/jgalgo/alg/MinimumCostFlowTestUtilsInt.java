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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class MinimumCostFlowTestUtilsInt extends TestUtils {

	static void testMinCostMaxFlowWithSourceSink(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(false).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			FlowNetworkInt<Integer, Integer> net = randNetwork(g, rand);
			WeightFunctionInt<Integer> cost = randCost(g, rand);
			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);

			testMinCostMaxFlowWithSourceSink(g, net, cost, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	static void testMinCostMaxFlowWithSourceSinkLowerBound(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(false).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			FlowNetworkInt<Integer, Integer> net = randNetwork(g, rand);
			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);
			Integer source = sourceSink.first();
			Integer sink = sourceSink.second();
			WeightFunctionInt<Integer> cost = randCost(g, rand);
			WeightFunctionInt<Integer> lowerBound = randLowerBound(g, net, source, sink, rand);

			testMinCostMaxFlowWithSourceSinkLowerBound(g, net, cost, lowerBound, source, sink, algo);
		});
	}

	static void testMinCostMaxFlowWithSourcesSinks(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(false).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			FlowNetworkInt<Integer, Integer> net = randNetwork(g, rand);
			WeightFunctionInt<Integer> cost = randCost(g, rand);
			Pair<Collection<Integer>, Collection<Integer>> sourcesSinks =
					MaximumFlowTestUtils.chooseMultiSourceMultiSink(g, rand);

			testMinCostMaxFlowWithSourcesSinks(g, net, cost, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	static void testMinCostMaxFlowWithSourcesSinksLowerBound(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(false).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			FlowNetworkInt<Integer, Integer> net = randNetwork(g, rand);
			Pair<Collection<Integer>, Collection<Integer>> sourcesSinks =
					MaximumFlowTestUtils.chooseMultiSourceMultiSink(g, rand);
			Collection<Integer> sources = sourcesSinks.first();
			Collection<Integer> sinks = sourcesSinks.second();
			WeightFunctionInt<Integer> cost = randCost(g, rand);
			WeightFunctionInt<Integer> lowerBound = randLowerBound(g, net, sources, sinks, rand);

			testMinCostMaxFlowWithSourcesSinksLowerBound(g, net, cost, lowerBound, sources, sinks, algo);
		});
	}

	static void testMinCostFlowWithSupply(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(false).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			FlowNetworkInt<Integer, Integer> net = randNetwork(g, rand);
			WeightFunctionInt<Integer> cost = randCost(g, rand);
			WeightFunctionInt<Integer> supply = FlowCirculationTestUtils.randSupplyInt(g, net, rand);

			testMinCostFlowWithSupply(g, net, cost, supply, algo);
		});
	}

	static void testMinCostFlowWithSupplyLowerBound(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(false).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			FlowNetworkInt<Integer, Integer> net = randNetwork(g, rand);
			WeightFunctionInt<Integer> supply = FlowCirculationTestUtils.randSupplyInt(g, net, rand);

			/* build a 'random' lower bound by solving min-cost flow with a different cost function and use the flows */
			WeightFunctionInt<Integer> cost1 = randCost(g, rand);
			MinimumCostFlow.newInstance().computeMinCostFlow(g, net, cost1, supply);
			WeightsInt<Integer> lowerBound = Weights.createExternalEdgesWeights(g, int.class);
			for (Integer e : g.edges()) {
				lowerBound.set(e, (int) (net.getFlowInt(e) * 0.4 * rand.nextDouble()));
				net.setFlow(e, 0);
			}

			WeightFunctionInt<Integer> cost = randCost(g, rand);

			testMinCostFlowWithSupplyLowerBound(g, net, cost, lowerBound, supply, algo);
		});
	}

	private static <V, E> FlowNetworkInt<V, E> randNetwork(Graph<V, E> g, Random rand) {
		FlowNetworkInt<V, E> net = FlowNetworkInt.createFromEdgeWeights(g);
		for (E e : g.edges())
			net.setCapacity(e, 400 + rand.nextInt(1024));
		return net;
	}

	private static <V, E> WeightFunctionInt<E> randCost(Graph<V, E> g, Random rand) {
		WeightsInt<E> cost = Weights.createExternalEdgesWeights(g, int.class);
		for (E e : g.edges())
			cost.set(e, rand.nextInt(2424) - 600);
		return cost;
	}

	private static <V, E> WeightFunctionInt<E> randLowerBound(Graph<V, E> g, FlowNetworkInt<V, E> net, V source, V sink,
			Random rand) {
		return randLowerBound(g, net, Set.of(source), Set.of(sink), rand);
	}

	private static <V, E> WeightFunctionInt<E> randLowerBound(Graph<V, E> g, FlowNetworkInt<V, E> net,
			Collection<V> sources, Collection<V> sinks, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		Object2IntMap<E> capacity = new Object2IntOpenHashMap<>();
		for (E e : g.edges())
			capacity.put(e, net.getCapacityInt(e));

		Set<V> sinksSet = new ObjectOpenHashSet<>(sinks);
		List<V> sourcesList = new ArrayList<>(sources);

		WeightsInt<E> lowerBound = Weights.createExternalEdgesWeights(g, int.class);
		ObjectArrayList<E> path = new ObjectArrayList<>();
		Set<V> visited = new ObjectOpenHashSet<>();
		sourcesLoop: for (;;) {
			if (sourcesList.isEmpty())
				return lowerBound;

			path.clear();
			visited.clear();

			int sourceIdx = rand.nextInt(sourcesList.size());
			V source = sourcesList.get(sourceIdx);
			visited.add(source);
			dfs: for (V u = source;;) {

				/* Find a random edge to deepen the DFS */
				List<E> es = new ArrayList<>(g.outEdges(u));
				Collections.shuffle(es, rand);
				for (E e : es) {
					if (capacity.getInt(e) == 0)
						continue;
					assert u.equals(g.edgeSource(e));
					V v = g.edgeTarget(e);
					if (visited.contains(v))
						continue;
					path.add(e);

					if (sinksSet.contains(v))
						/* found an residual path from source to sink */
						break dfs;

					/* Continue down in the DFS */
					visited.add(v);
					u = v;
					continue dfs;
				}

				/* No more edges to explore */
				if (path.isEmpty()) {
					/* No more residual paths from source to any sink, remove source from sources list */
					sourcesList.set(sourceIdx, sourcesList.get(sourcesList.size() - 1));
					sourcesList.remove(sourcesList.size() - 1);
					continue sourcesLoop;
				}

				/* Back up in the DFS path one vertex */
				E lastEdge = path.pop();
				assert u.equals(g.edgeTarget(lastEdge));
				u = g.edgeSource(lastEdge);
			}

			/* Found a residual path from source to sink */
			int delta = path.stream().mapToInt(capacity::getInt).min().getAsInt();
			assert delta > 0;
			for (E e : path)
				capacity.put(e, capacity.getInt(e) - delta);

			/* Add lower bounds to some of the edges */
			ObjectList<E> lowerBoundEdges = new ObjectArrayList<>(path);
			Collections.shuffle(lowerBoundEdges, rand);
			if (lowerBoundEdges.size() == 2) {
				lowerBoundEdges.remove(1);
			} else if (lowerBoundEdges.size() > 2) {
				lowerBoundEdges.removeElements(2, lowerBoundEdges.size());
			}
			for (E e : lowerBoundEdges) {
				int boundDelta = delta / 2 + rand.nextInt((delta + 1) / 2);
				lowerBound.set(e, lowerBound.weightInt(e) + boundDelta);
			}
		}
	}

	private static <V, E> void testMinCostMaxFlowWithSourceSink(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> cost, V source, V sink, MinimumCostFlow algo) {
		for (E e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, source, sink);
		double totalFlow = net.getFlowSum(g, source);
		MaximumFlowTestUtils.assertValidFlow(g, net, source, sink, totalFlow);

		assertMaximumFlow(g, net, null, List.of(source), List.of(sink));
		assertOptimalCirculation(g, net, cost, null);
	}

	private static <V, E> void testMinCostMaxFlowWithSourceSinkLowerBound(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> cost, WeightFunctionInt<E> lowerBound, V source, V sink, MinimumCostFlow algo) {
		for (E e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, lowerBound, source, sink);
		double totalFlow = net.getFlowSum(g, source);
		MaximumFlowTestUtils.assertValidFlow(g, net, source, sink, totalFlow);
		assertLowerBound(g, net, lowerBound);

		assertMaximumFlow(g, net, lowerBound, List.of(source), List.of(sink));
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static <V, E> void testMinCostMaxFlowWithSourcesSinks(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> cost, Collection<V> sources, Collection<V> sinks, MinimumCostFlow algo) {
		for (E e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, sources, sinks);
		double totalFlow = net.getFlowSum(g, sources);
		MaximumFlowTestUtils.assertValidFlow(g, net, sources, sinks, totalFlow);

		assertMaximumFlow(g, net, null, sources, sinks);
		assertOptimalCirculation(g, net, cost, null);
	}

	private static <V, E> void testMinCostMaxFlowWithSourcesSinksLowerBound(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> cost, WeightFunctionInt<E> lowerBound, Collection<V> sources, Collection<V> sinks,
			MinimumCostFlow algo) {
		for (E e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, lowerBound, sources, sinks);
		double totalFlow = net.getFlowSum(g, sources);
		MaximumFlowTestUtils.assertValidFlow(g, net, sources, sinks, totalFlow);
		assertLowerBound(g, net, lowerBound);

		assertMaximumFlow(g, net, lowerBound, sources, sinks);
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static <V, E> void testMinCostFlowWithSupply(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> cost, WeightFunctionInt<V> supply, MinimumCostFlow algo) {
		for (E e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostFlow(g, net, cost, supply);
		MaximumFlowTestUtils.assertValidFlow(g, net,
				FlowCirculationTestUtils.verticesWithPositiveSupply(g.vertices(), supply),
				FlowCirculationTestUtils.verticesWithNegativeSupply(g.vertices(), supply));

		FlowCirculationTestUtils.assertSupplySatisfied(g, net, supply);
		assertOptimalCirculation(g, net, cost, null);
	}

	private static <V, E> void testMinCostFlowWithSupplyLowerBound(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> cost, WeightFunctionInt<E> lowerBound, WeightFunctionInt<V> supply,
			MinimumCostFlow algo) {
		for (E e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostFlow(g, net, cost, lowerBound, supply);
		MaximumFlowTestUtils.assertValidFlow(g, net,
				FlowCirculationTestUtils.verticesWithPositiveSupply(g.vertices(), supply),
				FlowCirculationTestUtils.verticesWithNegativeSupply(g.vertices(), supply));

		FlowCirculationTestUtils.assertSupplySatisfied(g, net, supply);
		assertLowerBound(g, net, lowerBound);
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static <V, E> void assertLowerBound(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> lowerBound) {
		for (E e : g.edges())
			assertTrue(net.getFlowInt(e) >= lowerBound.weightInt(e));
	}

	private static <V, E> void assertMaximumFlow(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> lowerBound, Collection<V> sources, Collection<V> sinks) {
		Assertions.Graphs.onlyDirected(g);

		/*
		 * a flow is a maximum flow if no augmenting path can be found in the residual network. Search for one to verify
		 * the given flow is maximum.
		 */

		Object2IntMap<E> capacity = new Object2IntOpenHashMap<>(g.edges().size());
		Object2IntMap<E> flow = new Object2IntOpenHashMap<>(g.edges().size());
		for (E e : g.edges())
			capacity.put(e, net.getCapacityInt(e));
		for (E e : g.edges())
			flow.put(e, net.getFlowInt(e));
		if (lowerBound != null) {
			for (E e : g.edges()) {
				int l = lowerBound.weightInt(e);
				assertTrue(flow.getInt(e) >= l);
				capacity.put(e, capacity.getInt(e) - l);
				flow.put(e, flow.getInt(e) - l);
			}
		}

		// perform BFS and find a path of non saturated edges from the sources to the sinks
		Set<V> visited = new ObjectOpenHashSet<>();
		PriorityQueue<V> queue = new ObjectArrayFIFOQueue<>();
		for (V source : sources) {
			queue.enqueue(source);
			visited.add(source);
		}
		Set<V> sinksSet = new ObjectOpenHashSet<>(sinks);
		for (;;) {
			if (queue.isEmpty())
				return; /* no path to sink, flow is maximum */
			V u = queue.dequeue();
			for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
				E e = eit.next();
				if (flow.getInt(e) == capacity.getInt(e))
					continue; /* saturated */
				V v = eit.target();
				if (visited.contains(v))
					continue;

				/* if we found an augmenting path, we can push on it, flow is not maximum */
				assertFalse(sinksSet.contains(v));

				visited.add(v);
				queue.enqueue(v);
			}
			for (EdgeIter<V, E> eit = g.inEdges(u).iterator(); eit.hasNext();) {
				E e = eit.next();
				if (flow.getInt(e) == 0)
					continue; /* saturated */
				V v = eit.source();
				if (visited.contains(v))
					continue;

				/* if we found an augmenting path, we can push on it, flow is not maximum */
				assertFalse(sinksSet.contains(v));

				visited.add(v);
				queue.enqueue(v);
			}
		}
	}

	private static <V, E> void assertOptimalCirculation(Graph<V, E> g, FlowNetworkInt<V, E> net,
			WeightFunctionInt<E> cost, WeightFunctionInt<E> lowerBound) {
		Assertions.Graphs.onlyDirected(g);

		/*
		 * a circulation is optimal with respect to a cost function if no circle with negative mean cost exists in the
		 * graph residual network. Search for one to verify the circulation is optimal.
		 */

		Object2IntMap<E> capacity = new Object2IntOpenHashMap<>(g.edges().size());
		Object2IntMap<E> flow = new Object2IntOpenHashMap<>(g.edges().size());
		for (E e : g.edges())
			capacity.put(e, net.getCapacityInt(e));
		for (E e : g.edges())
			flow.put(e, net.getFlowInt(e));
		if (lowerBound != null) {
			for (E e : g.edges()) {
				int l = lowerBound.weightInt(e);
				assertTrue(flow.getInt(e) >= l);
				capacity.put(e, capacity.getInt(e) - l);
				flow.put(e, flow.getInt(e) - l);
			}
		}

		/* build the residual graph */
		GraphBuilder<V, Integer> b = GraphBuilder.newDirected();
		Supplier<Integer> edgeSupplier = () -> Integer.valueOf(b.edges().size());
		for (V v : g.vertices())
			b.addVertex(v);
		WeightsInt<Integer> residualWeights = b.addEdgesWeights("cost", int.class);
		for (E e : g.edges()) {
			V u = g.edgeSource(e);
			V v = g.edgeTarget(e);
			int cap = capacity.getInt(e);
			int f = flow.getInt(e);
			boolean isResidual = f < cap;
			boolean isRevResidual = f > 0;
			int c = cost.weightInt(e);
			if (isResidual) {
				Integer residualE = edgeSupplier.get();
				b.addEdge(u, v, residualE);
				residualWeights.set(residualE, c);
			}
			if (isRevResidual) {
				Integer residualE = edgeSupplier.get();
				b.addEdge(v, u, residualE);
				residualWeights.set(residualE, -c);
			}
		}
		Graph<V, Integer> residualGraph = b.build();
		residualWeights = residualGraph.getEdgesWeights("cost");

		/* the circulation is optimal if no circle with negative mean cost exists */
		Path<V, Integer> cycle = MinimumMeanCycle.newInstance().computeMinimumMeanCycle(residualGraph, residualWeights);
		assertTrue(cycle == null || residualWeights.weightSum(cycle.edges()) >= 0,
				"Negative cycle found in residual graph, the circulation is not optimal");
	}

}
