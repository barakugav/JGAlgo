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

package com.jgalgo.graph;

import java.util.Random;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.RandomIntUnique;
import com.jgalgo.internal.util.TestUtils;
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
		if (maxWeight - minWeight < g.edges().size() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomIntUnique rand = new RandomIntUnique(minWeight, maxWeight, seed);
		IWeightsInt weight = g.addEdgesWeights("weight", int.class);
		for (int e : g.edges())
			weight.set(e, rand.next());
		return weight;
	}

	public static IntGraph randGraph(int n, int m, long seed) {
		return randGraph(n, m, GraphsTestUtils.defaultGraphImpl(), seed);
	}

	public static IntGraph randGraph(int n, int m, Boolean2ObjectFunction<IntGraph> graphImpl, long seed) {
		return new RandomGraphBuilder(seed).graphImpl(graphImpl).n(n).m(m).directed(false).parallelEdges(false)
				.selfEdges(true).cycles(true).connected(false).build();
	}

}
