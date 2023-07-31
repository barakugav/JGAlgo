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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Random;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class MinimumCostFlowTestUtils extends TestUtils {

	static void testMinCostMaxFlowWithSourceSink(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			FlowNetwork.Int net = randNetwork(g, rand);
			WeightFunction.Int cost = randCost(g, rand);
			IntIntPair sourceSink = MinimumCutSTTestUtils.chooseSourceSink(g, rand);

			testMinCostMaxFlowWithSourceSink(g, net, cost, sourceSink.firstInt(), sourceSink.secondInt(), algo);
		});
	}

	static void testMinCostMaxFlowWithSourceSinkLowerBound(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			FlowNetwork.Int net = randNetwork(g, rand);
			IntIntPair sourceSink = MinimumCutSTTestUtils.chooseSourceSink(g, rand);
			int source = sourceSink.firstInt();
			int sink = sourceSink.secondInt();
			WeightFunction.Int cost = randCost(g, rand);
			WeightFunction.Int lowerBound = randLowerBound(g, net, source, sink, rand);

			testMinCostMaxFlowWithSourceSinkLowerBound(g, net, cost, lowerBound, source, sink, algo);
		});
	}

	static void testMinCostMaxFlowWithSourcesSinks(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			FlowNetwork.Int net = randNetwork(g, rand);
			WeightFunction.Int cost = randCost(g, rand);
			Pair<IntCollection, IntCollection> sourcesSinks = MinimumCutSTTestUtils.chooseMultiSourceMultiSink(g, rand);

			testMinCostMaxFlowWithSourcesSinks(g, net, cost, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	static void testMinCostMaxFlowWithSourcesSinksLowerBound(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			FlowNetwork.Int net = randNetwork(g, rand);
			Pair<IntCollection, IntCollection> sourcesSinks = MinimumCutSTTestUtils.chooseMultiSourceMultiSink(g, rand);
			IntCollection sources = sourcesSinks.first();
			IntCollection sinks = sourcesSinks.second();
			WeightFunction.Int cost = randCost(g, rand);
			WeightFunction.Int lowerBound = randLowerBound(g, net, sources, sinks, rand);

			testMinCostMaxFlowWithSourcesSinksLowerBound(g, net, cost, lowerBound, sources, sinks, algo);
		});
	}

	static void testMinCostFlowWithSupply(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			FlowNetwork.Int net = randNetwork(g, rand);
			WeightFunction.Int cost = randCost(g, rand);
			WeightFunction.Int supply = randSupply(g, net, rand);

			testMinCostFlowWithSupply(g, net, cost, supply, algo);
		});
	}

	static void testMinCostFlowWithSupplyLowerBound(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			FlowNetwork.Int net = randNetwork(g, rand);
			WeightFunction.Int supply = randSupply(g, net, rand);

			/* build a 'random' lower bound by solving min-cost flow with a different cost function and use the flows */
			WeightFunction.Int cost1 = randCost(g, rand);
			MinimumCostFlow.newBuilder().build().computeMinCostFlow(g, net, cost1, supply);
			Weights.Int lowerBound = Weights.createExternalEdgesWeights(g, int.class);
			for (int e : g.edges()) {
				lowerBound.set(e, (int) (net.getFlowInt(e) * 0.4 * rand.nextDouble()));
				net.setFlow(e, 0);
			}

			WeightFunction.Int cost = randCost(g, rand);

			testMinCostFlowWithSupplyLowerBound(g, net, cost, lowerBound, supply, algo);
		});
	}

	private static FlowNetwork.Int randNetwork(Graph g, Random rand) {
		FlowNetwork.Int net = FlowNetwork.Int.createFromEdgeWeights(g);
		for (int e : g.edges())
			net.setCapacity(e, 400 + rand.nextInt(1024));
		return net;
	}

	private static WeightFunction.Int randCost(Graph g, Random rand) {
		Weights.Int cost = Weights.createExternalEdgesWeights(g, int.class);
		for (int e : g.edges())
			cost.set(e, rand.nextInt(2424) - 600);
		return cost;
	}

	private static WeightFunction.Int randLowerBound(Graph g, FlowNetwork.Int net, int source, int sink, Random rand) {
		return randLowerBound(g, net, IntSets.singleton(source), IntSets.singleton(sink), rand);
	}

	private static WeightFunction.Int randLowerBound(Graph g, FlowNetwork.Int net, IntCollection sources,
			IntCollection sinks, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		Int2IntMap capacity = new Int2IntOpenHashMap();
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));

		IntSet sinksSet = new IntOpenHashSet(sinks);
		IntList sourcesList = new IntArrayList(sources);

		Weights.Int lowerBound = Weights.createExternalEdgesWeights(g, int.class);
		IntArrayList path = new IntArrayList();
		IntSet visited = new IntOpenHashSet();
		sourcesLoop: for (;;) {
			if (sourcesList.isEmpty())
				return lowerBound;

			path.clear();
			visited.clear();

			int sourceIdx = rand.nextInt(sourcesList.size());
			int source = sourcesList.getInt(sourceIdx);
			visited.add(source);
			dfs: for (int u = source;;) {

				/* Find a random edge to deepen the DFS */
				int[] es = g.outEdges(u).toIntArray();
				IntArrays.shuffle(es, rand);
				for (int e : es) {
					if (capacity.get(e) == 0)
						continue;
					assert u == g.edgeSource(e);
					int v = g.edgeTarget(e);
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
					sourcesList.set(sourceIdx, sourcesList.getInt(sourcesList.size() - 1));
					sourcesList.removeInt(sourcesList.size() - 1);
					continue sourcesLoop;
				}

				/* Back up in the DFS path one vertex */
				int lastEdge = path.popInt();
				assert u == g.edgeTarget(lastEdge);
				u = g.edgeSource(lastEdge);
			}

			/* Found a residual path from source to sink */
			int delta = path.intStream().map(capacity::get).min().getAsInt();
			assert delta > 0;
			for (int e : path)
				capacity.put(e, capacity.get(e) - delta);

			/* Add lower bounds to some of the edges */
			IntList lowerBoundEdges = new IntArrayList(path);
			IntLists.shuffle(lowerBoundEdges, rand);
			if (lowerBoundEdges.size() == 2) {
				lowerBoundEdges.removeInt(1);
			} else if (lowerBoundEdges.size() > 2) {
				lowerBoundEdges.removeElements(2, lowerBoundEdges.size());
			}
			for (int e : lowerBoundEdges) {
				int boundDelta = delta / 2 + rand.nextInt((delta + 1) / 2);
				lowerBound.set(e, lowerBound.weightInt(e) + boundDelta);
			}
		}
	}

	private static WeightFunction.Int randSupply(Graph g, FlowNetwork.Int net, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		IntList suppliers = new IntArrayList();
		IntList demanders = new IntArrayList();
		int[] vertices = g.vertices().toIntArray();
		IntArrays.shuffle(vertices, rand);
		assert vertices.length >= 2;
		suppliers.add(vertices[0]);
		demanders.add(vertices[1]);
		for (int i = 2; i < vertices.length; i++) {
			int r = rand.nextInt(3);
			if (r == 0) {
				suppliers.add(vertices[i]);
			} else if (r == 1) {
				demanders.add(vertices[i]);
			} else {
				/* do nothing */
			}
		}

		Int2IntMap capacity = new Int2IntOpenHashMap();
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));

		IntSet demandersSet = new IntOpenHashSet(demanders);
		IntList suppliersList = new IntArrayList(suppliers);

		Weights.Int supply = Weights.createExternalVerticesWeights(g, int.class);
		IntArrayList path = new IntArrayList();
		IntSet visited = new IntOpenHashSet();
		suppliersLoop: for (;;) {
			if (suppliersList.isEmpty()) {
				assert g.vertices().intStream().map(supply::weightInt).sum() == 0;
				return supply;
			}

			path.clear();
			visited.clear();

			int supplierIdx = rand.nextInt(suppliersList.size());
			int supplier = suppliersList.getInt(supplierIdx);
			visited.add(supplier);
			dfs: for (int u = supplier;;) {

				/* Find a random edge to deepen the DFS */
				int[] es = g.outEdges(u).toIntArray();
				IntArrays.shuffle(es, rand);
				for (int e : es) {
					if (capacity.get(e) == 0)
						continue;
					assert u == g.edgeSource(e);
					int v = g.edgeTarget(e);
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
					suppliersList.set(supplierIdx, suppliersList.getInt(suppliersList.size() - 1));
					suppliersList.removeInt(suppliersList.size() - 1);
					continue suppliersLoop;
				}

				/* Back up in the DFS path one vertex */
				int lastEdge = path.popInt();
				assert u == g.edgeTarget(lastEdge);
				u = g.edgeSource(lastEdge);
			}

			/* Found a residual path from a supplier to a demander */
			int delta = path.intStream().map(capacity::get).min().getAsInt();
			assert delta > 0;
			for (int e : path)
				capacity.put(e, capacity.get(e) - delta);

			/* Add lower bounds to some of the edges */
			int source = g.edgeSource(path.getInt(0));
			int sink = g.edgeTarget(path.getInt(path.size() - 1));
			if (rand.nextBoolean()) {
				int s = rand.nextInt((delta + 1) / 2);
				supply.set(source, supply.weightInt(source) + s);
				supply.set(sink, supply.weightInt(sink) - s);
			}
		}
	}

	private static void testMinCostMaxFlowWithSourceSink(Graph g, FlowNetwork.Int net, WeightFunction.Int cost,
			int source, int sink, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, source, sink);
		double totalFlow = net.getFlowSum(g, source);
		MaximumFlowTestUtils.assertValidFlow(g, net, source, sink, totalFlow);

		assertMaximumFlow(g, net, null, IntList.of(source), IntList.of(sink));
		assertOptimalCirculation(g, net, cost, null);
	}

	private static void testMinCostMaxFlowWithSourceSinkLowerBound(Graph g, FlowNetwork.Int net,
			WeightFunction.Int cost, WeightFunction.Int lowerBound, int source, int sink, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, lowerBound, source, sink);
		double totalFlow = net.getFlowSum(g, source);
		MaximumFlowTestUtils.assertValidFlow(g, net, source, sink, totalFlow);
		assertLowerBound(g, net, lowerBound);

		assertMaximumFlow(g, net, lowerBound, IntList.of(source), IntList.of(sink));
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static void testMinCostMaxFlowWithSourcesSinks(Graph g, FlowNetwork.Int net, WeightFunction.Int cost,
			IntCollection sources, IntCollection sinks, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, sources, sinks);
		double totalFlow = net.getFlowSum(g, sources);
		MaximumFlowTestUtils.assertValidFlow(g, net, sources, sinks, totalFlow);

		assertMaximumFlow(g, net, null, sources, sinks);
		assertOptimalCirculation(g, net, cost, null);
	}

	private static void testMinCostMaxFlowWithSourcesSinksLowerBound(Graph g, FlowNetwork.Int net,
			WeightFunction.Int cost, WeightFunction.Int lowerBound, IntCollection sources, IntCollection sinks,
			MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, lowerBound, sources, sinks);
		double totalFlow = net.getFlowSum(g, sources);
		MaximumFlowTestUtils.assertValidFlow(g, net, sources, sinks, totalFlow);
		assertLowerBound(g, net, lowerBound);

		assertMaximumFlow(g, net, lowerBound, sources, sinks);
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static void testMinCostFlowWithSupply(Graph g, FlowNetwork.Int net, WeightFunction.Int cost,
			WeightFunction.Int supply, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostFlow(g, net, cost, supply);
		MaximumFlowTestUtils.assertValidFlow(g, net, verticesWithPositiveSupply(g.vertices(), supply),
				verticesWithNegativeSupply(g.vertices(), supply));

		assertSupplySatisfied(g, net, supply);
		assertOptimalCirculation(g, net, cost, null);
	}

	private static void testMinCostFlowWithSupplyLowerBound(Graph g, FlowNetwork.Int net, WeightFunction.Int cost,
			WeightFunction.Int lowerBound, WeightFunction.Int supply, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostFlow(g, net, cost, lowerBound, supply);
		MaximumFlowTestUtils.assertValidFlow(g, net, verticesWithPositiveSupply(g.vertices(), supply),
				verticesWithNegativeSupply(g.vertices(), supply));

		assertSupplySatisfied(g, net, supply);
		assertLowerBound(g, net, lowerBound);
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static void assertLowerBound(Graph g, FlowNetwork net, WeightFunction lowerBound) {
		for (int e : g.edges())
			assertTrue(net.getFlow(e) >= lowerBound.weight(e));
	}

	private static IntSet verticesWithPositiveSupply(IntCollection vertices, WeightFunction supply) {
		IntSet sources = new IntOpenHashSet();
		for (int v : vertices)
			if (supply.weight(v) > 0)
				sources.add(v);
		return sources;
	}

	private static IntSet verticesWithNegativeSupply(IntCollection vertices, WeightFunction supply) {
		IntSet sources = new IntOpenHashSet();
		for (int v : vertices)
			if (supply.weight(v) < 0)
				sources.add(v);
		return sources;
	}

	private static void assertSupplySatisfied(Graph g, FlowNetwork net, WeightFunction supply) {
		for (int v : g.vertices()) {
			int supplySum = 0;
			for (int e : g.outEdges(v))
				supplySum += net.getFlow(e);
			for (int e : g.inEdges(v))
				supplySum -= net.getFlow(e);
			assertEquals(supply.weight(v), supplySum);
		}
	}

	private static void assertMaximumFlow(Graph g, FlowNetwork.Int net, WeightFunction.Int lowerBound,
			IntCollection sources, IntCollection sinks) {
		Assertions.Graphs.onlyDirected(g);

		/*
		 * a flow is a maximum flow if no augmenting path can be found in the residual network. Search for one to verify
		 * the given flow is maximum.
		 */

		Int2IntMap capacity = new Int2IntOpenHashMap(g.edges().size());
		Int2IntMap flow = new Int2IntOpenHashMap(g.edges().size());
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));
		for (int e : g.edges())
			flow.put(e, net.getFlowInt(e));
		if (lowerBound != null) {
			for (int e : g.edges()) {
				int l = lowerBound.weightInt(e);
				assertTrue(flow.get(e) >= l);
				capacity.put(e, capacity.get(e) - l);
				flow.put(e, flow.get(e) - l);
			}
		}

		// perform BFS and find a path of non saturated edges from the sources to the sinks
		IntSet visited = new IntOpenHashSet();
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		for (int source : sources) {
			queue.enqueue(source);
			visited.add(source);
		}
		IntSet sinksSet = new IntOpenHashSet(sinks);
		for (;;) {
			if (queue.isEmpty())
				return; /* no path to sink, flow is maximum */
			int u = queue.dequeueInt();
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (flow.get(e) == capacity.get(e))
					continue; /* saturated */
				int v = eit.target();
				if (visited.contains(v))
					continue;

				/* if we found an augmenting path, we can push on it, flow is not maximum */
				assertFalse(sinksSet.contains(v));

				visited.add(v);
				queue.enqueue(v);
			}
			for (EdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (flow.get(e) == 0)
					continue; /* saturated */
				int v = eit.source();
				if (visited.contains(v))
					continue;

				/* if we found an augmenting path, we can push on it, flow is not maximum */
				assertFalse(sinksSet.contains(v));

				visited.add(v);
				queue.enqueue(v);
			}
		}
	}

	private static void assertOptimalCirculation(Graph g, FlowNetwork.Int net, WeightFunction.Int cost,
			WeightFunction.Int lowerBound) {
		Assertions.Graphs.onlyDirected(g);

		/*
		 * a circulation is optimal with respect to a cost function if no circle with negative mean cost exists in the
		 * graph residual network. Search for one to verify the circulation is optimal.
		 */

		Int2IntMap capacity = new Int2IntOpenHashMap(g.edges().size());
		Int2IntMap flow = new Int2IntOpenHashMap(g.edges().size());
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));
		for (int e : g.edges())
			flow.put(e, net.getFlowInt(e));
		if (lowerBound != null) {
			for (int e : g.edges()) {
				int l = lowerBound.weightInt(e);
				assertTrue(flow.get(e) >= l);
				capacity.put(e, capacity.get(e) - l);
				flow.put(e, flow.get(e) - l);
			}
		}

		/* build the residual graph */
		GraphBuilder b = GraphBuilder.newDirected();
		for (int v : g.vertices())
			b.addVertex(v);
		Weights.Int residualWeights = b.addEdgesWeights("cost", int.class);
		for (int e : g.edges()) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			int cap = capacity.get(e);
			int f = flow.get(e);
			boolean isResidual = f < cap;
			boolean isRevResidual = f > 0;
			int c = cost.weightInt(e);
			if (isResidual) {
				int residualE = b.addEdge(u, v);
				residualWeights.set(residualE, c);
			}
			if (isRevResidual) {
				int residualE = b.addEdge(v, u);
				residualWeights.set(residualE, -c);
			}
		}
		Graph residualGraph = b.build();
		residualWeights = residualGraph.getEdgesWeights("cost");

		/* the circulation is optimal if no circle with negative mean cost exists */
		Path cycle = MinimumMeanCycle.newBuilder().build().computeMinimumMeanCycle(residualGraph, residualWeights);
		assertTrue(cycle == null || cycle.weight(residualWeights) >= 0,
				"Negative cycle found in residual graph, the circulation is not optimal");
	}

}
