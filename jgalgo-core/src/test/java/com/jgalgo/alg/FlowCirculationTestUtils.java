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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class FlowCirculationTestUtils extends TestUtils {

	static void testRandCirculationInt(FlowCirculation algo, long seed) {
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

			WeightFunctionInt<Integer> capacity = randNetworkInt(g, rand);
			WeightFunctionInt<Integer> supply = randSupplyInt(g, capacity, rand);

			testRandCirculationInt(g, capacity, supply, algo);
		});
	}

	private static <V, E> void testRandCirculationInt(Graph<V, E> g, WeightFunctionInt<E> capacity,
			WeightFunctionInt<V> supply, FlowCirculation algo) {
		Flow<V, E> flow = algo.computeCirculation(g, capacity, supply);

		MaximumFlowTestUtils.assertValidFlow(g, flow, verticesWithPositiveSupply(g.vertices(), supply),
				verticesWithNegativeSupply(g.vertices(), supply));
		assertSupplySatisfied(g, supply, flow);
	}

	static void testRandCirculation(FlowCirculation algo, long seed) {
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

			WeightFunction<Integer> capacity = randNetwork(g, rand);
			WeightFunction<Integer> supply = randSupply(g, capacity, rand);

			testRandCirculation(g, capacity, supply, algo);
		});
	}

	private static <V, E> void testRandCirculation(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<V> supply,
			FlowCirculation algo) {
		Flow<V, E> flow = algo.computeCirculation(g, capacity, supply);

		MaximumFlowTestUtils.assertValidFlow(g, flow, verticesWithPositiveSupply(g.vertices(), supply),
				verticesWithNegativeSupply(g.vertices(), supply));
		assertSupplySatisfied(g, supply, flow);
	}

	private static <V, E> WeightFunctionInt<E> randNetworkInt(Graph<V, E> g, Random rand) {
		WeightsInt<E> capacity = g.addEdgesWeights("capacity", int.class);
		for (E e : g.edges())
			capacity.set(e, 400 + rand.nextInt(1024));
		return capacity;
	}

	private static <V, E> WeightFunction<E> randNetwork(Graph<V, E> g, Random rand) {
		WeightsDouble<E> capacity = g.addEdgesWeights("capacity", double.class);
		for (E e : g.edges())
			capacity.set(e, 400 + rand.nextDouble() * 1024);
		return capacity;
	}

	static <V, E> WeightFunctionInt<V> randSupplyInt(Graph<V, E> g, WeightFunctionInt<E> capacity, Random rand) {
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

		Object2IntMap<E> caps = new Object2IntOpenHashMap<>();
		for (E e : g.edges())
			caps.put(e, capacity.weightInt(e));

		Set<V> demandersSet = new ObjectOpenHashSet<>(demanders);
		List<V> suppliersList = new ArrayList<>(suppliers);

		WeightsInt<V> supply = Weights.createExternalVerticesWeights(g, int.class);
		ObjectArrayList<E> path = new ObjectArrayList<>();
		Set<V> visited = new ObjectOpenHashSet<>();
		suppliersLoop: for (;;) {
			if (suppliersList.isEmpty()) {
				assert g.vertices().stream().mapToInt(supply::weightInt).sum() == 0;
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
					if (caps.getInt(e) == 0)
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
			int delta = path.stream().mapToInt(caps::getInt).min().getAsInt();
			assert delta > 0;
			for (E e : path)
				caps.put(e, caps.getInt(e) - delta);

			/* Add lower bounds to some of the edges */
			V source = g.edgeSource(path.get(0));
			V sink = g.edgeTarget(path.get(path.size() - 1));
			if (rand.nextBoolean()) {
				int s = rand.nextInt((delta + 1) / 2);
				supply.set(source, supply.weightInt(source) + s);
				supply.set(sink, supply.weightInt(sink) - s);
			}
		}
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

	static <V, E> void assertSupplySatisfied(Graph<V, E> g, WeightFunction<V> supply, Flow<V, E> flow) {
		for (V v : g.vertices()) {
			double supplySum = 0;
			for (E e : g.outEdges(v))
				supplySum += flow.getFlow(e);
			for (E e : g.inEdges(v))
				supplySum -= flow.getFlow(e);
			assertEquals(supply.weight(v), supplySum, 1e-9);
		}
	}

	static <V> Set<V> verticesWithPositiveSupply(Collection<V> vertices, WeightFunction<V> supply) {
		Set<V> sources = new ObjectOpenHashSet<>();
		for (V v : vertices)
			if (supply.weight(v) > 0)
				sources.add(v);
		return sources;
	}

	static <V> Set<V> verticesWithNegativeSupply(Collection<V> vertices, WeightFunction<V> supply) {
		Set<V> sources = new ObjectOpenHashSet<>();
		for (V v : vertices)
			if (supply.weight(v) < 0)
				sources.add(v);
		return sources;
	}

}
