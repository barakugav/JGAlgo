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

package com.jgalgo.bench.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import com.jgalgo.gen.BarabasiAlbertGraphGenerator;
import com.jgalgo.gen.GnpGraphGenerator;
import com.jgalgo.gen.RecursiveMatrixGraphGenerator;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.Range;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;

public class GraphsTestUtils extends TestUtils {

	private GraphsTestUtils() {}

	public static Boolean2ObjectFunction<IntGraph> defaultGraphImpl() {
		return direct -> IntGraphFactory.newUndirected().setDirected(direct).newGraph();
	}

	public static IntGraph randTree(int n, long seed) {
		return new RandomGraphBuilder(seed).n(n).m(n - 1).directed(false).selfEdges(false).cycles(false).connected(true)
				.build();
	}

	public static IntGraph randForest(int n, int m, long seed) {
		return new RandomGraphBuilder(seed).n(n).m(m).directed(false).selfEdges(false).cycles(false).connected(false)
				.build();
	}

	public static IWeightsDouble assignRandWeights(IntGraph g, long seed) {
		return assignRandWeights(g, 1.0, 100.0, seed);
	}

	public static IWeightsDouble assignRandWeights(IntGraph g, double minWeight, double maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(seed);
		IWeightsDouble weight = g.addEdgesWeights("weight", double.class);
		for (int e : g.edges())
			weight.set(e, nextDouble(rand, minWeight, maxWeight));
		return weight;
	}

	public static IWeightsInt assignRandWeightsIntPos(IntGraph g, long seed) {
		int m = g.edges().size();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, minWeight, maxWeight, seed);
	}

	public static IWeightsInt assignRandWeightsIntNeg(IntGraph g, long seed) {
		int m = g.edges().size();
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seed);
	}

	public static IWeightsInt assignRandWeightsInt(IntGraph g, int minWeight, int maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(seed);
		IWeightsInt weight = IWeights.createExternalEdgesWeights(g, int.class);
		for (int e : g.edges())
			weight.set(e, rand.nextInt(maxWeight - minWeight) + minWeight);
		return weight;
	}

	public static IntGraph randGraph(int n, int m, long seed) {
		return new RandomGraphBuilder(seed).n(n).m(m).directed(false).parallelEdges(false).selfEdges(false).cycles(true)
				.connected(false).build();
	}

	public static IntGraph randGraphBipartite(int sn, int tn, int m, long seed) {
		return new RandomGraphBuilder(seed).sn(sn).tn(tn).m(m).directed(false).bipartite(true).parallelEdges(false)
				.selfEdges(false).cycles(true).connected(false).build();
	}

	public static IntGraph randomGraphGnp(int n, boolean directed, long seed) {
		GnpGraphGenerator<Integer, Integer> gen = GnpGraphGenerator.newIntInstance();
		gen.setSeed(seed);
		gen.setDirected(directed);
		gen.setVertices(Range.of(n));
		gen.setEdges(new AtomicInteger()::getAndIncrement);
		return gen.generate().indexGraph();
	}

	public static IntGraph randomGraphBarabasiAlbert(int n, boolean directed, long seed) {
		BarabasiAlbertGraphGenerator<Integer, Integer> gen = BarabasiAlbertGraphGenerator.newIntInstance();
		gen.setSeed(seed);
		gen.setDirected(directed);
		gen.setVertices(Range.of(n));
		gen.setEdges(new AtomicInteger()::getAndIncrement);
		return gen.generate().indexGraph();
	}

	public static IntGraph randomGraphRecursiveMatrix(int n, int m, boolean directed, long seed) {
		RecursiveMatrixGraphGenerator<Integer, Integer> gen = RecursiveMatrixGraphGenerator.newIntInstance();
		gen.setSeed(seed);
		gen.setDirected(directed);
		gen.setVertices(Range.of(n));
		gen.setEdges(m, new AtomicInteger()::getAndIncrement);
		return gen.generate().indexGraph();
	}

}
