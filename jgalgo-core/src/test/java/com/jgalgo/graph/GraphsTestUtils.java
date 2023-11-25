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

	public static Boolean2ObjectFunction<Graph<Integer, Integer>> defaultGraphImpl(long seed) {
		if (new Random(seed).nextBoolean()) {
			return direct -> IntGraphFactory.newUndirected().setDirected(direct).newGraph();
		} else {
			return direct -> GraphFactory.<Integer, Integer>newUndirected().setDirected(direct).newGraph();
		}
	}

	public static Graph<Integer, Integer> randTree(int n, long seed) {
		return new RandomGraphBuilder(seed).n(n).m(n - 1).directed(false).selfEdges(false).cycles(false).connected(true)
				.build();
	}

	public static Graph<Integer, Integer> randForest(int n, int m, long seed) {
		return new RandomGraphBuilder(seed).n(n).m(m).directed(false).selfEdges(false).cycles(false).connected(false)
				.build();
	}

	public static <V, E> WeightsDouble<E> assignRandWeights(Graph<V, E> g, long seed) {
		return assignRandWeights(g, 1.0, 100.0, seed);
	}

	public static <V, E> WeightsDouble<E> assignRandWeights(Graph<V, E> g, double minWeight, double maxWeight,
			long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(seed);
		WeightsDouble<E> weight = g.addEdgesWeights("weight", double.class);
		for (E e : g.edges())
			weight.set(e, nextDouble(rand, minWeight, maxWeight));
		return weight;
	}

	public static <V, E> WeightsInt<E> assignRandWeightsIntPos(Graph<V, E> g, long seed) {
		int m = g.edges().size();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, minWeight, maxWeight, seed);
	}

	public static <V, E> WeightsInt<E> assignRandWeightsIntNeg(Graph<V, E> g, long seed) {
		int m = g.edges().size();
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seed);
	}

	public static <V, E> WeightsInt<E> assignRandWeightsInt(Graph<V, E> g, int minWeight, int maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < g.edges().size() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomIntUnique rand = new RandomIntUnique(minWeight, maxWeight, seed);
		WeightsInt<E> weight = g.addEdgesWeights("weight", int.class);
		for (E e : g.edges())
			weight.set(e, rand.next());
		return weight;
	}

	public static <V, E> WeightFunction<E> assignRandWeightsMaybeInt(Graph<V, E> g, int minWeight, int maxWeight,
			long seed) {
		if (new Random(seed).nextBoolean())
			return assignRandWeightsInt(g, minWeight, maxWeight, seed);
		WeightsDouble<E> w = assignRandWeights(g, minWeight, maxWeight, seed);
		/* floor weights to avoid floating points errors */
		for (E e : g.edges())
			w.set(e, (int) w.get(e));
		return w;
	}

	public static Graph<Integer, Integer> randGraph(int n, int m, long seed) {
		return new RandomGraphBuilder(seed).n(n).m(m).directed(false).parallelEdges(false).selfEdges(true).cycles(true)
				.connected(false).build();
	}

	public static Graph<Integer, Integer> randGraph(int n, int m,
			Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		boolean selfEdges = graphImpl.get(false).isAllowSelfEdges();
		return new RandomGraphBuilder(seed).graphImpl(graphImpl).n(n).m(m).directed(false).parallelEdges(false)
				.selfEdges(selfEdges).cycles(true).connected(false).build();
	}

}
