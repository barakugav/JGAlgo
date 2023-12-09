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
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class MinimumCostFlowTestUtilsDouble extends TestUtils {

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
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());

			WeightFunction<Integer> capacity = randNetwork(g, rand);
			WeightFunction<Integer> cost = randCost(g, rand);
			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);

			testMinCostMaxFlowWithSourceSink(g, capacity, cost, sourceSink.first(), sourceSink.second(), algo);
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
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());

			WeightFunction<Integer> capacity = randNetwork(g, rand);
			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);
			Integer source = sourceSink.first();
			Integer sink = sourceSink.second();
			WeightFunction<Integer> cost = randCost(g, rand);
			WeightFunction<Integer> lowerBound = randLowerBound(g, capacity, source, sink, rand);

			testMinCostMaxFlowWithSourceSinkLowerBound(g, capacity, cost, lowerBound, source, sink, algo);
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
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());

			WeightFunction<Integer> capacity = randNetwork(g, rand);
			WeightFunction<Integer> cost = randCost(g, rand);
			Pair<Collection<Integer>, Collection<Integer>> sourcesSinks =
					MaximumFlowTestUtils.chooseMultiSourceMultiSink(g, rand);

			testMinCostMaxFlowWithSourcesSinks(g, capacity, cost, sourcesSinks.first(), sourcesSinks.second(), algo);
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
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());

			WeightFunction<Integer> capacity = randNetwork(g, rand);
			Pair<Collection<Integer>, Collection<Integer>> sourcesSinks =
					MaximumFlowTestUtils.chooseMultiSourceMultiSink(g, rand);
			Collection<Integer> sources = sourcesSinks.first();
			Collection<Integer> sinks = sourcesSinks.second();
			WeightFunction<Integer> cost = randCost(g, rand);
			WeightFunction<Integer> lowerBound = randLowerBound(g, capacity, sources, sinks, rand);

			testMinCostMaxFlowWithSourcesSinksLowerBound(g, capacity, cost, lowerBound, sources, sinks, algo);
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
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());
			WeightFunction<Integer> capacity = randNetwork(g, rand);
			WeightFunction<Integer> cost = randCost(g, rand);
			WeightFunction<Integer> supply = randSupply(g, capacity, rand);

			testMinCostFlowWithSupply(g, capacity, cost, supply, algo);
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
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());

			WeightFunction<Integer> capacity = randNetwork(g, rand);
			WeightFunction<Integer> supply = randSupply(g, capacity, rand);

			/* build a 'random' lower bound by solving min-cost flow with a different cost function and use the flows */
			WeightFunction<Integer> cost1 = randCost(g, rand);
			Flow<Integer, Integer> flow = MinimumCostFlow.newInstance().computeMinCostFlow(g, capacity, cost1, supply);
			WeightsDouble<Integer> lowerBound = Weights.createExternalEdgesWeights(g, double.class);
			for (Integer e : g.edges())
				lowerBound.set(e, (int) (flow.getFlow(e) * 0.4 * rand.nextDouble()));

			WeightFunction<Integer> cost = randCost(g, rand);

			testMinCostFlowWithSupplyLowerBound(g, capacity, cost, lowerBound, supply, algo);
		});
	}

	private static <V, E> WeightFunction<E> randNetwork(Graph<V, E> g, Random rand) {
		WeightsDouble<E> capacity = g.addEdgesWeights("capacity", double.class);
		for (E e : g.edges())
			capacity.set(e, 400 + rand.nextDouble() * 1024);
		return capacity;
	}

	private static <V, E> WeightFunction<E> randCost(Graph<V, E> g, Random rand) {
		WeightsDouble<E> cost = Weights.createExternalEdgesWeights(g, double.class);
		for (E e : g.edges())
			cost.set(e, rand.nextDouble() * 2424 - 600);
		return cost;
	}

	private static <V, E> WeightFunction<E> randLowerBound(Graph<V, E> g, WeightFunction<E> capacity, V source, V sink,
			Random rand) {
		return randLowerBound(g, capacity, Set.of(source), Set.of(sink), rand);
	}

	private static <V, E> WeightFunction<E> randLowerBound(Graph<V, E> g, WeightFunction<E> capacity,
			Collection<V> sources, Collection<V> sinks, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		final double eps = 1e-6;

		Object2DoubleMap<E> caps = new Object2DoubleOpenHashMap<>();
		for (E e : g.edges())
			caps.put(e, capacity.weight(e));

		Set<V> sinksSet = new ObjectOpenHashSet<>(sinks);
		List<V> sourcesList = new ObjectArrayList<>(sources);

		WeightsDouble<E> lowerBound = Weights.createExternalEdgesWeights(g, double.class);
		ObjectArrayList<E> path = new ObjectArrayList<>();
		Set<V> visited = new ObjectOpenHashSet<>();
		sourcesLoop: for (;;) {
			if (sourcesList.isEmpty())
				break;

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
					if (caps.getDouble(e) < eps)
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
			double delta = path.stream().mapToDouble(caps::getDouble).min().getAsDouble();
			assert delta >= eps;
			for (E e : path)
				caps.put(e, caps.getDouble(e) - delta);

			/* Add lower bounds to some of the edges */
			ObjectList<E> lowerBoundEdges = new ObjectArrayList<>(path);
			Collections.shuffle(lowerBoundEdges, rand);
			if (lowerBoundEdges.size() == 2) {
				lowerBoundEdges.remove(1);
			} else if (lowerBoundEdges.size() > 2) {
				lowerBoundEdges.removeElements(2, lowerBoundEdges.size());
			}
			for (E e : lowerBoundEdges) {
				double boundDelta = delta / 2 + rand.nextDouble() * (delta / 2);
				assert 0 <= boundDelta && boundDelta <= delta;
				lowerBound.set(e, lowerBound.weight(e) + boundDelta);
			}
		}

		for (E e : g.edges())
			lowerBound.set(e, Math.min(lowerBound.get(e), capacity.weight(e)));
		return lowerBound;
	}

	private static <V, E> WeightFunction<V> randSupply(Graph<V, E> g, WeightFunction<E> capacity, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		List<V> suppliers = new ArrayList<>();
		List<V> demanders = new ArrayList<>();
		List<V> vertices = new ArrayList<>(g.vertices());
		Collections.shuffle(vertices, rand);
		assert vertices.size() >= 2;
		suppliers.add(vertices.get(0));
		demanders.add(vertices.get(1));
		for (int i = 2; i < vertices.size(); i++) {
			int r = rand.nextInt(3);
			if (r == 0) {
				suppliers.add(vertices.get(i));
			} else if (r == 1) {
				demanders.add(vertices.get(i));
			} else {
				/* do nothing */
			}
		}

		Object2DoubleMap<E> caps = new Object2DoubleOpenHashMap<>();
		for (E e : g.edges())
			caps.put(e, capacity.weight(e));

		Set<V> demandersSet = new ObjectOpenHashSet<>(demanders);
		List<V> suppliersList = new ArrayList<>(suppliers);

		WeightsDouble<V> supply = Weights.createExternalVerticesWeights(g, double.class);
		ObjectArrayList<E> path = new ObjectArrayList<>();
		Set<V> visited = new ObjectOpenHashSet<>();
		suppliersLoop: for (;;) {
			if (suppliersList.isEmpty()) {
				assert Math.abs(g.vertices().stream().mapToDouble(supply::weight).sum()) < 1e-3;
				return supply;
			}

			path.clear();
			visited.clear();

			int supplierIdx = rand.nextInt(suppliersList.size());
			V supplier = suppliersList.get(supplierIdx);
			visited.add(supplier);
			dfs: for (V u = supplier;;) {

				/* Find a random edge to deepen the DFS */
				List<E> es = new ArrayList<>(g.outEdges(u));
				Collections.shuffle(es, rand);
				for (E e : es) {
					if (caps.getDouble(e) == 0)
						continue;
					assert u.equals(g.edgeSource(e));
					V v = g.edgeTarget(e);
					if (visited.contains(v))
						continue;
					path.add(e);

					if (demandersSet.contains(v))
						/* found an residual path from a supplier to a demander */
						break dfs;

					/* Continue down in the DFS */
					visited.add(v);
					u = v;
					continue dfs;
				}

				/* No more edges to explore */
				if (path.isEmpty()) {
					/* No more residual paths from supplier to any demander, remove supplier from suppliers list */
					suppliersList.set(supplierIdx, suppliersList.get(suppliersList.size() - 1));
					suppliersList.remove(suppliersList.size() - 1);
					continue suppliersLoop;
				}

				/* Back up in the DFS path one vertex */
				E lastEdge = path.pop();
				assert u.equals(g.edgeTarget(lastEdge));
				u = g.edgeSource(lastEdge);
			}

			/* Found a residual path from a supplier to a demander */
			double delta = path.stream().mapToDouble(caps::getDouble).min().getAsDouble();
			assert delta > 0;
			for (E e : path)
				caps.put(e, caps.getDouble(e) - delta);

			/* Add lower bounds to some of the edges */
			V source = g.edgeSource(path.get(0));
			V sink = g.edgeTarget(path.get(path.size() - 1));
			if (rand.nextBoolean()) {
				double s = rand.nextDouble() * ((delta + 1) / 2);
				supply.set(source, supply.weight(source) + s);
				supply.set(sink, supply.weight(sink) - s);
			}
		}
	}

	private static <V, E> void testMinCostMaxFlowWithSourceSink(Graph<V, E> g, WeightFunction<E> capacity,
			WeightFunction<E> cost, V source, V sink, MinimumCostFlow algo) {
		Flow<V, E> flow = algo.computeMinCostMaxFlow(g, capacity, cost, source, sink);
		double totalFlow = flow.getSupply(source);
		MaximumFlowTestUtils.assertValidFlow(g, flow, source, sink, totalFlow);

		assertMaximumFlow(g, capacity, null, List.of(source), List.of(sink), flow);
		assertOptimalCirculation(g, capacity, cost, null, flow);
	}

	private static <V, E> void testMinCostMaxFlowWithSourceSinkLowerBound(Graph<V, E> g, WeightFunction<E> capacity,
			WeightFunction<E> cost, WeightFunction<E> lowerBound, V source, V sink, MinimumCostFlow algo) {
		Flow<V, E> flow = algo.computeMinCostMaxFlow(g, capacity, cost, lowerBound, source, sink);
		double totalFlow = flow.getSupply(source);
		MaximumFlowTestUtils.assertValidFlow(g, flow, source, sink, totalFlow);
		assertLowerBound(g, lowerBound, flow);

		assertMaximumFlow(g, capacity, lowerBound, List.of(source), List.of(sink), flow);
		assertOptimalCirculation(g, capacity, cost, lowerBound, flow);
	}

	private static <V, E> void testMinCostMaxFlowWithSourcesSinks(Graph<V, E> g, WeightFunction<E> capacity,
			WeightFunction<E> cost, Collection<V> sources, Collection<V> sinks, MinimumCostFlow algo) {
		Flow<V, E> flow = algo.computeMinCostMaxFlow(g, capacity, cost, sources, sinks);
		double totalFlow = flow.getSupplySubset(sources);
		MaximumFlowTestUtils.assertValidFlow(g, flow, sources, sinks, totalFlow);

		assertMaximumFlow(g, capacity, null, sources, sinks, flow);
		assertOptimalCirculation(g, capacity, cost, null, flow);
	}

	private static <V, E> void testMinCostMaxFlowWithSourcesSinksLowerBound(Graph<V, E> g, WeightFunction<E> capacity,
			WeightFunction<E> cost, WeightFunction<E> lowerBound, Collection<V> sources, Collection<V> sinks,
			MinimumCostFlow algo) {
		Flow<V, E> flow = algo.computeMinCostMaxFlow(g, capacity, cost, lowerBound, sources, sinks);
		double totalFlow = flow.getSupplySubset(sources);
		MaximumFlowTestUtils.assertValidFlow(g, flow, sources, sinks, totalFlow);
		assertLowerBound(g, lowerBound, flow);

		assertMaximumFlow(g, capacity, lowerBound, sources, sinks, flow);
		assertOptimalCirculation(g, capacity, cost, lowerBound, flow);
	}

	private static <V, E> void testMinCostFlowWithSupply(Graph<V, E> g, WeightFunction<E> capacity,
			WeightFunction<E> cost, WeightFunction<V> supply, MinimumCostFlow algo) {
		Flow<V, E> flow = algo.computeMinCostFlow(g, capacity, cost, supply);
		MaximumFlowTestUtils.assertValidFlow(g, flow,
				FlowCirculationTestUtils.verticesWithPositiveSupply(g.vertices(), supply),
				FlowCirculationTestUtils.verticesWithNegativeSupply(g.vertices(), supply));

		FlowCirculationTestUtils.assertSupplySatisfied(g, supply, flow);
		assertOptimalCirculation(g, capacity, cost, null, flow);
	}

	private static <V, E> void testMinCostFlowWithSupplyLowerBound(Graph<V, E> g, WeightFunction<E> capacity,
			WeightFunction<E> cost, WeightFunction<E> lowerBound, WeightFunction<V> supply, MinimumCostFlow algo) {
		Flow<V, E> flow = algo.computeMinCostFlow(g, capacity, cost, lowerBound, supply);
		MaximumFlowTestUtils.assertValidFlow(g, flow,
				FlowCirculationTestUtils.verticesWithPositiveSupply(g.vertices(), supply),
				FlowCirculationTestUtils.verticesWithNegativeSupply(g.vertices(), supply));

		FlowCirculationTestUtils.assertSupplySatisfied(g, supply, flow);
		assertLowerBound(g, lowerBound, flow);
		assertOptimalCirculation(g, capacity, cost, lowerBound, flow);
	}

	private static <V, E> void assertLowerBound(Graph<V, E> g, WeightFunction<E> lowerBound, Flow<V, E> flow) {
		for (E e : g.edges())
			assertTrue(flow.getFlow(e) >= lowerBound.weight(e));
	}

	private static <V, E> void assertMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity,
			WeightFunction<E> lowerBound, Collection<V> sources, Collection<V> sinks, Flow<V, E> flow) {
		Assertions.Graphs.onlyDirected(g);

		/*
		 * a flow is a maximum flow if no augmenting path can be found in the residual network. Search for one to verify
		 * the given flow is maximum.
		 */

		Object2DoubleMap<E> caps = new Object2DoubleOpenHashMap<>(g.edges().size());
		Object2DoubleMap<E> flows = new Object2DoubleOpenHashMap<>(g.edges().size());
		for (E e : g.edges())
			caps.put(e, capacity.weight(e));
		for (E e : g.edges())
			flows.put(e, flow.getFlow(e));
		if (lowerBound != null) {
			for (E e : g.edges()) {
				double l = lowerBound.weight(e);
				assertTrue(flows.getDouble(e) >= l);
				caps.put(e, caps.getDouble(e) - l);
				flows.put(e, flows.getDouble(e) - l);
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
				if (flows.getDouble(e) >= caps.getDouble(e) - 1e-9)
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
				if (flows.getDouble(e) <= 1e-9)
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

	private static <V, E> void assertOptimalCirculation(Graph<V, E> g, WeightFunction<E> capacity,
			WeightFunction<E> cost, WeightFunction<E> lowerBound, Flow<V, E> flow) {
		Assertions.Graphs.onlyDirected(g);

		/*
		 * a circulation is optimal with respect to a cost function if no circle with negative mean cost exists in the
		 * graph residual network. Search for one to verify the circulation is optimal.
		 */

		Object2DoubleMap<E> caps = new Object2DoubleOpenHashMap<>(g.edges().size());
		Object2DoubleMap<E> flows = new Object2DoubleOpenHashMap<>(g.edges().size());
		for (E e : g.edges())
			caps.put(e, capacity.weight(e));
		for (E e : g.edges())
			flows.put(e, flow.getFlow(e));
		if (lowerBound != null) {
			for (E e : g.edges()) {
				double l = lowerBound.weight(e);
				assertTrue(flows.getDouble(e) >= l);
				caps.put(e, caps.getDouble(e) - l);
				flows.put(e, flows.getDouble(e) - l);
			}
		}

		/* build the residual graph */
		GraphBuilder<V, Integer> b = GraphBuilder.directed();
		Supplier<Integer> edgeSupplier = () -> Integer.valueOf(b.edges().size());
		for (V v : g.vertices())
			b.addVertex(v);
		WeightsDouble<Integer> residualWeights = b.addEdgesWeights("cost", double.class);
		for (E e : g.edges()) {
			V u = g.edgeSource(e);
			V v = g.edgeTarget(e);
			double cap = caps.getDouble(e);
			double f = flows.getDouble(e);
			boolean isResidual = f < cap - 1e-9;
			boolean isRevResidual = f > 1e-9;
			double c = cost.weight(e);
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
