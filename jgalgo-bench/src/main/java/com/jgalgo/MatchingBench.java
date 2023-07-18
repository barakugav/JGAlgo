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
import com.jgalgo.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MatchingBench {

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MaximumCardinality {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Graph> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
				graphs.add(g);
			}
		}

		private void benchAlgo(MatchingAlgorithm algo, Blackhole blackhole) {
			Graph g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			Matching matching = algo.computeMaximumCardinalityMatching(g);
			blackhole.consume(matching);
		}

		@Benchmark
		public void CardinalityGabow1976(Blackhole blackhole) {
			benchAlgo(getAlgo("CardinalityGabow1976"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MaximumCardinalityBipartite {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Graph> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randGraphBipartite(n / 2, n / 2, m, seedGen.nextSeed());
				graphs.add(g);
			}
		}

		private void benchAlgo(MatchingAlgorithm algo, Blackhole blackhole) {
			Graph g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			Matching matching = algo.computeMaximumCardinalityMatching(g);
			blackhole.consume(matching);
		}

		@Benchmark
		public void CardinalityBipartiteHopcroftKarp(Blackhole blackhole) {
			benchAlgo(getAlgo("CardinalityBipartiteHopcroftKarp"), blackhole);
		}

		@Benchmark
		public void CardinalityGabow1976(Blackhole blackhole) {
			benchAlgo(getAlgo("CardinalityGabow1976"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MaximumWeighted {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Pair<Graph, WeightFunction>> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
				WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
				graphs.add(Pair.of(g, w));
			}
		}

		private void benchAlgo(MatchingAlgorithm algo, Blackhole blackhole) {
			Pair<Graph, WeightFunction> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			Graph g = gw.first();
			WeightFunction w = gw.second();
			Matching matching = algo.computeMaximumWeightedMatching(g, w);
			blackhole.consume(matching);
		}

		@Benchmark
		public void Gabow1990Simpler(Blackhole blackhole) {
			benchAlgo(getAlgo("Gabow1990Simpler"), blackhole);
		}

		@Benchmark
		public void Gabow1990(Blackhole blackhole) {
			benchAlgo(getAlgo("Gabow1990"), blackhole);
		}

		@Benchmark
		public void BlossomV(Blackhole blackhole) {
			benchAlgo(getAlgo("BlossomV"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MaximumWeightedBipartite {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Pair<Graph, WeightFunction>> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randGraphBipartite(n / 2, n / 2, m, seedGen.nextSeed());
				WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
				graphs.add(Pair.of(g, w));
			}
		}

		private void benchAlgo(MatchingAlgorithm algo, Blackhole blackhole) {
			Pair<Graph, WeightFunction> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			Graph g = gw.first();
			WeightFunction w = gw.second();
			Matching matching = algo.computeMaximumWeightedMatching(g, w);
			blackhole.consume(matching);
		}

		@Benchmark
		public void BipartiteHungarianMethod(Blackhole blackhole) {
			benchAlgo(getAlgo("BipartiteHungarianMethod"), blackhole);
		}

		@Benchmark
		public void BipartiteSSSP(Blackhole blackhole) {
			benchAlgo(getAlgo("BipartiteSSSP"), blackhole);
		}

		@Benchmark
		public void BlossomV(Blackhole blackhole) {
			benchAlgo(getAlgo("BlossomV"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MinimumPerfect {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Pair<Graph, WeightFunction>> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());

				if (g.vertices().size() % 2 != 0)
					throw new IllegalArgumentException("there is no perfect matching");

				MatchingAlgorithm cardinalityAlgo = new MatchingCardinalityGabow1976();
				Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
				IntList unmatchedVertices = new IntArrayList(cardinalityMatch.unmatchedVertices());
				assert unmatchedVertices.size() % 2 == 0;
				IntLists.shuffle(unmatchedVertices, new Random(seedGen.nextSeed()));
				for (int i = 0; i < unmatchedVertices.size() / 2; i++) {
					int u = unmatchedVertices.getInt(i * 2 + 0);
					int v = unmatchedVertices.getInt(i * 2 + 1);
					g.addEdge(u, v);
				}
				assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();

				WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
				graphs.add(Pair.of(g, w));
			}
		}

		private void benchAlgo(MatchingAlgorithm algo, Blackhole blackhole) {
			Pair<Graph, WeightFunction> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			Graph g = gw.first();
			WeightFunction w = gw.second();
			Matching matching = algo.computeMinimumWeightedPerfectMatching(g, w);
			blackhole.consume(matching);
		}

		@Benchmark
		public void Gabow1990Simpler(Blackhole blackhole) {
			benchAlgo(getAlgo("Gabow1990Simpler"), blackhole);
		}

		@Benchmark
		public void Gabow1990(Blackhole blackhole) {
			benchAlgo(getAlgo("Gabow1990"), blackhole);
		}

		@Benchmark
		public void BlossomV(Blackhole blackhole) {
			benchAlgo(getAlgo("BlossomV"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MinimumPerfectBipartite {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Pair<Graph, WeightFunction>> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randGraphBipartite(n / 2, n / 2, m, seedGen.nextSeed());
				Weights.Bool partition = g.getVerticesWeights(Weights.DefaultBipartiteWeightKey);

				MatchingAlgorithm cardinalityAlgo = new MatchingCardinalityBipartiteHopcroftKarp();
				Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
				IntList unmatchedVerticesS = new IntArrayList(cardinalityMatch.unmatchedVertices());
				IntList unmatchedVerticesT = new IntArrayList(cardinalityMatch.unmatchedVertices());
				unmatchedVerticesS.removeIf(v -> partition.getBool(v));
				unmatchedVerticesT.removeIf(v -> !partition.getBool(v));
				assert unmatchedVerticesS.size() == unmatchedVerticesT.size();
				IntLists.shuffle(unmatchedVerticesS, new Random(seedGen.nextSeed()));
				IntLists.shuffle(unmatchedVerticesT, new Random(seedGen.nextSeed()));
				for (int i = 0; i < unmatchedVerticesS.size(); i++) {
					int u = unmatchedVerticesS.getInt(i);
					int v = unmatchedVerticesT.getInt(i);
					g.addEdge(u, v);
				}
				assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();

				WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
				graphs.add(Pair.of(g, w));
			}
		}

		private void benchAlgo(MatchingAlgorithm algo, Blackhole blackhole) {
			Pair<Graph, WeightFunction> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			Graph g = gw.first();
			WeightFunction w = gw.second();
			Matching matching = algo.computeMinimumWeightedPerfectMatching(g, w);
			blackhole.consume(matching);
		}

		@Benchmark
		public void BipartiteHungarianMethod(Blackhole blackhole) {
			benchAlgo(getAlgo("BipartiteHungarianMethod"), blackhole);
		}

		@Benchmark
		public void Gabow1990Simpler(Blackhole blackhole) {
			benchAlgo(getAlgo("Gabow1990Simpler"), blackhole);
		}

		@Benchmark
		public void BlossomV(Blackhole blackhole) {
			benchAlgo(getAlgo("BlossomV"), blackhole);
		}

	}

	private static MatchingAlgorithm getAlgo(String name) {
		return MatchingAlgorithm.newBuilder().setOption("impl", name).build();
	}

}
