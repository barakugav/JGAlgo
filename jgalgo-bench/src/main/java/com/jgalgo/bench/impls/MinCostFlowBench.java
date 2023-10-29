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
package com.jgalgo.bench.impls;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import com.jgalgo.alg.FlowNetworkInt;
import com.jgalgo.alg.MinimumCostFlow;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MinCostFlowBench {

	final int graphsNum = 31;
	final AtomicInteger graphIdx = new AtomicInteger();

	public static class Supply extends MinCostFlowBench {

		List<Supply.Task> graphs;

		static class Task {
			IntGraph g;
			FlowNetworkInt net;
			IWeightFunctionInt cost;
			IWeightFunctionInt lowerBound;
			IWeightFunctionInt supply;

			static Task newRand(IntGraph g, Random rand) {
				FlowNetworkInt net = randNetwork(g, rand);
				IWeightFunctionInt supply = randSupply(g, net, rand);

				/*
				 * build a 'random' lower bound by solving min-cost flow with a different cost function and use the
				 * flows
				 */
				IWeightFunctionInt cost1 = randCost(g, rand);
				MinimumCostFlow.newInstance().computeMinCostFlow(g, net, cost1, supply);
				IWeightsInt lowerBound = IWeights.createExternalEdgesWeights(g, int.class);
				for (int e : g.edges()) {
					lowerBound.set(e, (int) (net.getFlowInt(e) * 0.4 * rand.nextDouble()));
					net.setFlow(e, 0);
				}

				IWeightFunctionInt cost = randCost(g, rand);

				Task t = new Task();
				t.g = g;
				t.net = net;
				t.cost = cost;
				t.lowerBound = lowerBound;
				t.supply = supply;
				return t;
			}
		}

		public void resetFlow() {
			for (Task task : graphs)
				for (int e : task.g.edges())
					task.net.setFlow(e, 0);
		}

		void benchMinCostFlow(MinimumCostFlow.Builder builder, Blackhole blackhole) {
			Task t = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			MinimumCostFlow algo = builder.build();
			algo.computeMinCostFlow(t.g, t.net, t.cost, t.lowerBound, t.supply);
			blackhole.consume(t.net);
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class Gnp extends MinCostFlowBench.Supply {

			// @Param({ "|V|=1000", "|V|=2000", "|V|=2500" })
			@Param({ "|V|=100" })
			public String args;

			public String directed = "directed";

			@Override
			@Setup(Level.Invocation)
			public void resetFlow() {
				super.resetFlow();
			}

			@Setup(Level.Trial)
			public void setup() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				int n = Integer.parseInt(argsMap.get("|V|"));
				boolean directed = this.directed.equals("directed");

				final SeedGenerator seedGen = new SeedGenerator(0x94fc6ec413f60392L);
				Random rand = new Random(seedGen.nextSeed());
				graphs = new ObjectArrayList<>(graphsNum);
				for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
					IntGraph g = GraphsTestUtils.randomGraphGnp(n, directed, seedGen.nextSeed());
					graphs.add(Task.newRand(g, rand));
				}
			}

			@Benchmark
			public void CycleCanceling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cycle-canceling"), blackhole);
			}

			@Benchmark
			public void CostScaling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cost-scaling"), blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class BarabasiAlbert extends MinCostFlowBench.Supply {

			// @Param({ "|V|=3000", "|V|=4500", "|V|=6000" })
			@Param({ "|V|=100" })
			public String args;

			public String directed = "directed";

			@Override
			@Setup(Level.Invocation)
			public void resetFlow() {
				super.resetFlow();
			}

			@Setup(Level.Trial)
			public void setup() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				int n = Integer.parseInt(argsMap.get("|V|"));
				boolean directed = this.directed.equals("directed");

				final SeedGenerator seedGen = new SeedGenerator(0xdc6c4cf7f4d3843cL);
				Random rand = new Random(seedGen.nextSeed());
				graphs = new ObjectArrayList<>(graphsNum);
				for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
					IntGraph g = GraphsTestUtils.randomGraphBarabasiAlbert(n, directed, seedGen.nextSeed());
					graphs.add(Task.newRand(g, rand));
				}
			}

			@Benchmark
			public void CycleCanceling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cycle-canceling"), blackhole);
			}

			@Benchmark
			public void CostScaling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cost-scaling"), blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class RecursiveMatrix extends MinCostFlowBench.Supply {

			// @Param({ "|V|=1500 |E|=5000", "|V|=2500 |E|=8000", "|V|=4000 |E|=16000" })
			// @Param({ "|V|=15 |E|=50" })
			@Param({ "|V|=3 |E|=3" })
			public String args;

			public String directed = "directed";

			@Override
			@Setup(Level.Invocation)
			public void resetFlow() {
				super.resetFlow();
			}

			@Setup(Level.Trial)
			public void setup() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				int n = Integer.parseInt(argsMap.get("|V|"));
				int m = Integer.parseInt(argsMap.get("|E|"));
				boolean directed = this.directed.equals("directed");

				final SeedGenerator seedGen = new SeedGenerator(0x9716aede5cfa6eabL);
				Random rand = new Random(seedGen.nextSeed());
				graphs = new ObjectArrayList<>(graphsNum);
				for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
					IntGraph g = GraphsTestUtils.randomGraphRecursiveMatrix(n, m, directed, seedGen.nextSeed());
					graphs.add(Task.newRand(g, rand));
				}
			}

			@Benchmark
			public void CycleCanceling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cycle-canceling"), blackhole);
			}

			@Benchmark
			public void CostScaling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cost-scaling"), blackhole);
			}
		}
	}

	public static class MaxFlow extends MinCostFlowBench {

		List<MaxFlow.Task> graphs;

		static class Task {
			IntGraph g;
			FlowNetworkInt net;
			IWeightFunctionInt cost;
			IWeightFunctionInt lowerBound;
			IntCollection sources;
			IntCollection sinks;

			static Task newRand(IntGraph g, Random rand) {
				FlowNetworkInt net = randNetwork(g, rand);
				Pair<IntCollection, IntCollection> sourcesSinks = MaximumFlowBench.chooseMultiSourceMultiSink(g, rand);
				IntCollection sources = sourcesSinks.first();
				IntCollection sinks = sourcesSinks.second();
				IWeightFunctionInt cost = randCost(g, rand);
				IWeightFunctionInt lowerBound = randLowerBound(g, net, sources, sinks, rand);

				Task t = new Task();
				t.g = g;
				t.net = net;
				t.cost = cost;
				t.lowerBound = lowerBound;
				t.sources = sources;
				t.sinks = sinks;
				return t;
			}
		}

		public void resetFlow() {
			for (Task task : graphs)
				for (int e : task.g.edges())
					task.net.setFlow(e, 0);
		}

		void benchMinCostFlow(MinimumCostFlow.Builder builder, Blackhole blackhole) {
			Task t = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			MinimumCostFlow algo = builder.build();
			algo.computeMinCostMaxFlow(t.g, t.net, t.cost, t.lowerBound, t.sources, t.sinks);
			blackhole.consume(t.net);
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class Gnp extends MinCostFlowBench.MaxFlow {

			// @Param({ "|V|=1000", "|V|=2000", "|V|=2500" })
			@Param({ "|V|=100" })
			public String args;

			public String directed = "directed";

			@Override
			@Setup(Level.Invocation)
			public void resetFlow() {
				super.resetFlow();
			}

			@Setup(Level.Trial)
			public void setup() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				int n = Integer.parseInt(argsMap.get("|V|"));
				boolean directed = this.directed.equals("directed");

				final SeedGenerator seedGen = new SeedGenerator(0x94fc6ec413f60392L);
				Random rand = new Random(seedGen.nextSeed());
				graphs = new ObjectArrayList<>(graphsNum);
				for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
					IntGraph g = GraphsTestUtils.randomGraphGnp(n, directed, seedGen.nextSeed());
					graphs.add(Task.newRand(g, rand));
				}
			}

			@Benchmark
			public void CycleCanceling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cycle-canceling"), blackhole);
			}

			@Benchmark
			public void CostScaling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cost-scaling"), blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class BarabasiAlbert extends MinCostFlowBench.MaxFlow {

			// @Param({ "|V|=3000", "|V|=4500", "|V|=6000" })
			@Param({ "|V|=100" })
			public String args;

			public String directed = "directed";

			@Override
			@Setup(Level.Invocation)
			public void resetFlow() {
				super.resetFlow();
			}

			@Setup(Level.Trial)
			public void setup() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				int n = Integer.parseInt(argsMap.get("|V|"));
				boolean directed = this.directed.equals("directed");

				final SeedGenerator seedGen = new SeedGenerator(0xdc6c4cf7f4d3843cL);
				Random rand = new Random(seedGen.nextSeed());
				graphs = new ObjectArrayList<>(graphsNum);
				for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
					IntGraph g = GraphsTestUtils.randomGraphBarabasiAlbert(n, directed, seedGen.nextSeed());
					graphs.add(Task.newRand(g, rand));
				}
			}

			@Benchmark
			public void CycleCanceling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cycle-canceling"), blackhole);
			}

			@Benchmark
			public void CostScaling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cost-scaling"), blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class RecursiveMatrix extends MinCostFlowBench.MaxFlow {

			// @Param({ "|V|=1500 |E|=5000", "|V|=2500 |E|=8000", "|V|=4000 |E|=16000" })
			@Param({ "|V|=15 |E|=50" })
			public String args;

			public String directed = "directed";

			@Override
			@Setup(Level.Invocation)
			public void resetFlow() {
				super.resetFlow();
			}

			@Setup(Level.Trial)
			public void setup() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				int n = Integer.parseInt(argsMap.get("|V|"));
				int m = Integer.parseInt(argsMap.get("|E|"));
				boolean directed = this.directed.equals("directed");

				final SeedGenerator seedGen = new SeedGenerator(0x9716aede5cfa6eabL);
				Random rand = new Random(seedGen.nextSeed());
				graphs = new ObjectArrayList<>(graphsNum);
				for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
					IntGraph g = GraphsTestUtils.randomGraphRecursiveMatrix(n, m, directed, seedGen.nextSeed());
					graphs.add(Task.newRand(g, rand));
				}
			}

			@Benchmark
			public void CycleCanceling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cycle-canceling"), blackhole);
			}

			@Benchmark
			public void CostScaling(Blackhole blackhole) {
				benchMinCostFlow(MinimumCostFlow.newBuilder().setOption("impl", "cost-scaling"), blackhole);
			}
		}
	}

	private static FlowNetworkInt randNetwork(IntGraph g, Random rand) {
		IWeightsInt capacities = IWeights.createExternalEdgesWeights(g, int.class);
		IWeightsInt flows = IWeights.createExternalEdgesWeights(g, int.class);
		FlowNetworkInt net = FlowNetworkInt.createFromEdgeWeights(capacities, flows);
		for (int e : g.edges())
			net.setCapacity(e, 400 + rand.nextInt(1024));
		return net;
	}

	private static IWeightFunctionInt randCost(IntGraph g, Random rand) {
		IWeightsInt cost = IWeights.createExternalEdgesWeights(g, int.class);
		for (int e : g.edges())
			cost.set(e, rand.nextInt(2424) - 600);
		return cost;
	}

	private static IWeightFunctionInt randLowerBound(IntGraph g, FlowNetworkInt net, IntCollection sources,
			IntCollection sinks, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		Int2IntMap capacity = new Int2IntOpenHashMap();
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));

		IntSet sinksSet = new IntOpenHashSet(sinks);
		IntList sourcesList = new IntArrayList(sources);

		IWeightsInt lowerBound = IWeights.createExternalEdgesWeights(g, int.class);
		IntArrayList path = new IntArrayList();
		IntSet visited = new IntOpenHashSet();

		int lowerBoundCount = 0;
		sourcesLoop: for (;;) {
			if (sourcesList.isEmpty() || lowerBoundCount >= g.vertices().size() / 2)
				return lowerBound;

			class EdgeIterator {
				int[] es;
				int idx;
			}

			path.clear();
			visited.clear();
			Int2ObjectMap<EdgeIterator> edgeIters = new Int2ObjectOpenHashMap<>(g.vertices().size());
			for (int u : g.vertices()) {
				int[] es = g.outEdges(u).toIntArray();
				IntArrays.shuffle(es, rand);
				EdgeIterator iter = new EdgeIterator();
				iter.es = es;
				iter.idx = 0;
				edgeIters.put(u, iter);
			}

			int sourceIdx = rand.nextInt(sourcesList.size());
			int source = sourcesList.getInt(sourceIdx);
			visited.add(source);
			dfs: for (int u = source;;) {

				/* Find a random edge to deepen the DFS */
				for (EdgeIterator edgeIter = edgeIters.get(u); edgeIter.idx < edgeIter.es.length; edgeIter.idx++) {
					int e = edgeIter.es[edgeIter.idx];
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
			if (lowerBoundEdges.size() > 1)
				lowerBoundEdges.removeElements(Math.max(1, lowerBoundEdges.size() / 3), lowerBoundEdges.size());
			for (int e : lowerBoundEdges) {
				int boundDelta = delta / 2 + rand.nextInt((delta + 1) / 2);
				lowerBound.set(e, lowerBound.weightInt(e) + boundDelta);
				lowerBoundCount++;
			}
		}
	}

	private static IWeightFunctionInt randSupply(IntGraph g, FlowNetworkInt net, Random rand) {
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

		IWeightsInt supply = IWeights.createExternalVerticesWeights(g, int.class);
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

}
