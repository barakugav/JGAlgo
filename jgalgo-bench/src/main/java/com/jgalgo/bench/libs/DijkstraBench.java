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
package com.jgalgo.bench.libs;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.opt.graph.sparse.SparseIntUndirectedWeightedGraph;
import org.jgrapht.util.SupplierUtil;
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
import com.jgalgo.ShortestPathSingleSource;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class DijkstraBench {

	private static abstract class Base {

		List<JGAlgoArgs> jgalgoGraphs;
		List<JGAlgoCSRArgs> jgalgoCSRGraphs;
		List<JGraphTArgs> jgraphtGraphs;
		List<JGraphTSparseArgs> jgraphtSparseGraphs;

		private final int graphsNum = 10;
		private final AtomicInteger graphIdx = new AtomicInteger();

		void generateGraphs() {
			final SeedGenerator seedGen = new SeedGenerator(0x88da246e71ef3dacL);
			Random rand = new Random(seedGen.nextSeed());
			jgalgoGraphs = new ObjectArrayList<>(graphsNum);
			jgalgoCSRGraphs = new ObjectArrayList<>(graphsNum);
			jgraphtGraphs = new ObjectArrayList<>(graphsNum);
			jgraphtSparseGraphs = new ObjectArrayList<>(graphsNum);

			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = generateGraph(seedGen.nextSeed());
				WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
				int source = g.vertices().toIntArray()[rand.nextInt(g.vertices().size())];

				JGAlgoArgs gArgs = new JGAlgoArgs(g, w, source);
				jgalgoGraphs.add(gArgs);
				jgalgoCSRGraphs.add(JGAlgoCSRArgs.fromJGAlgoArgs(gArgs));
				jgraphtGraphs.add(JGraphTArgs.fromJGAlgoArgs(gArgs));
				jgraphtSparseGraphs.add(JGraphTSparseArgs.fromJGAlgoArgs(gArgs));
			}
		}

		abstract Graph generateGraph(long seed);

		@Benchmark
		public void jgalgo(Blackhole blackhole) {
			JGAlgoArgs args = jgalgoGraphs.get(nextGraphIdx());
			ShortestPathSingleSource algo = ShortestPathSingleSource.newBuilder().build();
			ShortestPathSingleSource.Result result = algo.computeShortestPaths(args.graph, args.w, args.source);
			blackhole.consume(result);
		}

		@Benchmark
		public void jgalgoCSR(Blackhole blackhole) {
			JGAlgoCSRArgs args = jgalgoCSRGraphs.get(nextGraphIdx());
			ShortestPathSingleSource algo = ShortestPathSingleSource.newBuilder().build();
			ShortestPathSingleSource.Result result = algo.computeShortestPaths(args.graph, args.w, args.source);
			blackhole.consume(result);
		}

		@Benchmark
		public void jgrapht(Blackhole blackhole) {
			JGraphTArgs args = jgraphtGraphs.get(nextGraphIdx());
			DijkstraShortestPath<Integer, DefaultWeightedEdge> algo = new DijkstraShortestPath<>(args.graph);
			@SuppressWarnings("boxing")
			SingleSourcePaths<Integer, DefaultWeightedEdge> result = algo.getPaths(args.source);
			blackhole.consume(result);
		}

		@SuppressWarnings("boxing")
		@Benchmark
		public void jgraphtSparse(Blackhole blackhole) {
			JGraphTSparseArgs args = jgraphtSparseGraphs.get(nextGraphIdx());
			DijkstraShortestPath<Integer, Integer> algo = new DijkstraShortestPath<>(args.graph);
			SingleSourcePaths<Integer, Integer> result = algo.getPaths(args.source);
			blackhole.consume(result);
		}

		private int nextGraphIdx() {
			return graphIdx.getAndUpdate(i -> (i + 1) % graphsNum);
		}

		static class JGAlgoArgs {
			final Graph graph;
			final WeightFunction.Int w;
			final int source;

			JGAlgoArgs(Graph graph, WeightFunction.Int w, int source) {
				this.graph = graph;
				this.w = w;
				this.source = source;
			}
		}

		static class JGAlgoCSRArgs {
			final IndexGraph graph;
			final WeightFunction.Int w;
			final int source;

			JGAlgoCSRArgs(IndexGraph graph, WeightFunction.Int w, int source) {
				this.graph = graph;
				this.w = w;
				this.source = source;
			}

			static JGAlgoCSRArgs fromJGAlgoArgs(JGAlgoArgs args) {
				IndexGraph ig = args.graph.indexGraph();
				int source = args.graph.indexGraphVerticesMap().idToIndex(args.source);
				IndexIdMap eiMap = args.graph.indexGraphEdgesMap();
				WeightFunction.Int w = IndexIdMaps.idToIndexWeightFunc(args.w, eiMap);

				IndexGraphBuilder.ReIndexedGraph gReindexed = IndexGraphBuilder.newFrom(ig).reIndexAndBuild(true, true);
				IndexGraph g = gReindexed.graph();
				Optional<IndexGraphBuilder.ReIndexingMap> vsMap = gReindexed.verticesReIndexing();
				source = vsMap.isPresent() ? vsMap.get().origToReIndexed(source) : source;
				return new JGAlgoCSRArgs(g, w, source);
			}

		}

		static class JGraphTArgs {
			final org.jgrapht.Graph<Integer, DefaultWeightedEdge> graph;
			final int source;

			JGraphTArgs(org.jgrapht.Graph<Integer, DefaultWeightedEdge> graph, int source) {
				this.graph = graph;
				this.source = source;
			}

			static JGraphTArgs fromJGAlgoArgs(JGAlgoArgs args) {
				org.jgrapht.Graph<Integer, DefaultWeightedEdge> jg = graph2jgrapht(args.graph, args.w);
				return new JGraphTArgs(jg, args.source);
			}
		}

		static class JGraphTSparseArgs {
			final org.jgrapht.Graph<Integer, Integer> graph;
			final int source;

			JGraphTSparseArgs(org.jgrapht.Graph<Integer, Integer> graph, int source) {
				this.graph = graph;
				this.source = source;
			}

			static JGraphTSparseArgs fromJGAlgoArgs(JGAlgoArgs args) {
				IndexGraph ig = args.graph.indexGraph();
				int source = args.graph.indexGraphVerticesMap().idToIndex(args.source);
				IndexIdMap eiMap = args.graph.indexGraphEdgesMap();
				WeightFunction.Int w = IndexIdMaps.idToIndexWeightFunc(args.w, eiMap);

				org.jgrapht.Graph<Integer, Integer> jg = graph2jgraphtSparse(ig, w);
				return new JGraphTSparseArgs(jg, source);
			}
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class BarabasiAlbert extends Base {

		@Param({ "|V|=64", "|V|=300", "|V|=700", "|V|=1600", "|V|=2500", "|V|=4000", "|V|=6000", "|V|=9000",
				"|V|=13500", "|V|=21000", "|V|=32000", "|V|=48000", "|V|=75000",
				// "|V|=115000", "|V|=175000", "|V|=250000"
		})
		public String args;
		private int n;

		@Override
		@Setup(Level.Iteration)
		public void generateGraphs() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("|V|"));
			super.generateGraphs();
		}

		@Override
		Graph generateGraph(long seed) {
			Graph g = GraphsTestUtils.randomGraphBarabasiAlbert(n, false, seed);
			return GraphFactory.newUndirected().newCopyOf(g);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class Gnp extends Base {

		@Param({ "|V|=64", "|V|=300", "|V|=700", "|V|=1200", "|V|=1600", "|V|=2000", "|V|=2500", "|V|=3000", "|V|=3500",
				"|V|=4000",
				// "|V|=4500", "|V|=5000", "|V|=5500", "|V|=6000"
		})
		public String args;
		private int n;

		@Override
		@Setup(Level.Iteration)
		public void generateGraphs() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("|V|"));
			super.generateGraphs();
		}

		@Override
		Graph generateGraph(long seed) {
			Graph g = GraphsTestUtils.randomGraphGnp(n, false, seed);
			return GraphFactory.newUndirected().newCopyOf(g);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class RecursiveMatrix extends Base {

		@Param({ "|V|=64", "|V|=128", "|V|=256", "|V|=512", "|V|=1024", "|V|=2048", "|V|=4096", "|V|=8192", "|V|=16384",
				"|V|=32768",
				// "|V|=65536", "|V|=131072", "|V|=262144"
		})
		public String args;
		private int n;

		@Override
		@Setup(Level.Iteration)
		public void generateGraphs() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("|V|"));
			super.generateGraphs();
		}

		@Override
		Graph generateGraph(long seed) {
			int m = n * 16;
			Graph g = GraphsTestUtils.randomGraphRecursiveMatrix(n, m, true, seed);
			return GraphFactory.newUndirected().newCopyOf(g);
		}

	}

	@SuppressWarnings("boxing")
	private static org.jgrapht.Graph<Integer, DefaultWeightedEdge> graph2jgrapht(Graph g,
			WeightFunction edgeWeightFunc) {
		org.jgrapht.Graph<Integer, DefaultWeightedEdge> jg = GraphTypeBuilder.undirected().weighted(true)
				.edgeClass(DefaultWeightedEdge.class).vertexSupplier(SupplierUtil.createIntegerSupplier())
				.allowingMultipleEdges(true).allowingSelfLoops(true).buildGraph();
		for (int v : g.vertices())
			jg.addVertex(v);
		for (int e : g.edges()) {
			var je = jg.addEdge(g.edgeSource(e), g.edgeTarget(e));
			jg.setEdgeWeight(je, edgeWeightFunc.weight(e));
		}
		return jg;
	}

	@SuppressWarnings("boxing")
	private static org.jgrapht.Graph<Integer, Integer> graph2jgraphtSparse(IndexGraph g,
			WeightFunction edgeWeightFunc) {
		return new SparseIntUndirectedWeightedGraph(g.vertices().size(), g.edges().size(),
				() -> g.edges().intStream().mapToObj(e -> new org.jgrapht.alg.util.Triple<>(g.edgeSource(e),
						g.edgeTarget(e), edgeWeightFunc.weight(e))));
	}

}
