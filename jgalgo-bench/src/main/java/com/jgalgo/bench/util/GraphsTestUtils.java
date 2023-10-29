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

import java.util.BitSet;
import java.util.Random;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsInt;
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
		final double p = 0.1;
		return randomGraphGnp(n, p, directed, seed);
	}

	public static IntGraph randomGraphGnp(int n, double p, boolean directed, long seed) {
		if (n < 0)
			throw new IllegalArgumentException();
		if (!(0 <= p && p <= 1))
			throw new IllegalArgumentException();

		IndexGraphBuilder builder = directed ? IndexGraphBuilder.newDirected() : IndexGraphBuilder.newUndirected();
		builder.expectedVerticesNum(n);
		for (int i = 0; i < n; i++)
			builder.addVertex();
		if (p > 0) {
			Random rand = new Random(seed);
			if (directed) {
				for (int u = 0; u < n; u++)
					for (int v = 0; v < n; v++)
						if (u != v && rand.nextDouble() <= p)
							builder.addEdge(u, v);
			} else {
				for (int u = 0; u < n; u++)
					for (int v = u + 1; v < n; v++)
						if (rand.nextDouble() <= p)
							builder.addEdge(u, v);
			}
		}

		return builder.reIndexAndBuild(true, true).graph();
	}

	public static IntGraph randomGraphBarabasiAlbert(int n, boolean directed, long seed) {
		final int nInit = 20;
		final int m = 10;
		return randomGraphBarabasiAlbert(n, nInit, m, directed, seed);
	}

	public static IntGraph randomGraphBarabasiAlbert(int n, int nInit, int m, boolean directed, long seed) {
		if (nInit <= 0 || nInit > n)
			throw new IllegalArgumentException();
		if (m > nInit)
			throw new IllegalArgumentException();

		int[] endpoints = new int[(nInit * (nInit - 1) / 2 + (n - nInit) * m) * 2];
		int edgeNum = 0;

		/* start with a complete graph of size nInit */
		for (int u = 0; u < nInit; u++) {
			for (int v = u + 1; v < nInit; v++) {
				int e = edgeNum++;
				endpoints[e * 2 + 0] = u;
				endpoints[e * 2 + 1] = v;
			}
		}

		/* add n-nInit vertices, each with m edges */
		Random rand = new Random(seed);
		for (int vNum = nInit; vNum < n; vNum++) {
			final int edgeNumAtStart = edgeNum;
			final int u = vNum;
			for (int i = 0; i < m; i++) {
				/* by sampling from the current endpoints, we sample a vertex with prob of its degree */
				int v = endpoints[rand.nextInt(edgeNumAtStart * 2)];
				int e = edgeNum++;
				endpoints[e * 2 + 0] = u;
				endpoints[e * 2 + 1] = v;
			}
		}

		IndexGraphBuilder builder = directed ? IndexGraphBuilder.newDirected() : IndexGraphBuilder.newUndirected();
		builder.expectedVerticesNum(n);
		for (int i = 0; i < n; i++)
			builder.addVertex();
		for (int e = 0; e < edgeNum; e++) {
			int u = endpoints[e * 2 + 0];
			int v = endpoints[e * 2 + 1];
			if (rand.nextBoolean()) {
				int tmp = u;
				u = v;
				v = tmp;
			}
			builder.addEdge(u, v);
		}
		return builder.reIndexAndBuild(true, true).graph();
	}

	public static IntGraph randomGraphRecursiveMatrix(int n, int m, boolean directed, long seed) {
		if (directed) {
			final double a = 0.57;
			final double b = 0.21;
			final double c = 0.17;
			final double d = 0.05;
			return randomGraphRecursiveMatrix(n, m, a, b, c, d, true, seed);
		} else {
			final double a = 0.57;
			final double b = 0.19;
			final double c = 0.19;
			final double d = 0.05;
			return randomGraphRecursiveMatrix(n, m, a, b, c, d, false, seed);
		}
	}

	public static IntGraph randomGraphRecursiveMatrix(int n, int m, double a, double b, double c, double d,
			boolean directed, long seed) {
		if (n <= 0 || m < 0)
			throw new IllegalArgumentException();
		if (m > 0.75 * n * (n - 1))
			throw new IllegalArgumentException(
					"too many edges for random sampling (max=" + (int) (0.75 * n * (n - 1)) + ")");
		if (a < 0 || b < 0 || c < 0 || d < 0)
			throw new IllegalArgumentException();
		if (a + b + c + d != 1)
			throw new IllegalArgumentException();

		final double p1 = a;
		final double p2 = a + b;
		final double p3 = a + b + c;
		final double p4 = a + b + c + d;

		final int depth = nextPowerOf2(n);
		final int N = 1 << depth;
		BitSet edges = new BitSet(N * N);

		Random rand = new Random(seed);
		for (int edgeNum = 0; edgeNum < m;) {
			int u = 0, v = 0;
			for (int s = depth; s > 0; s--) {
				double p = rand.nextDouble();
				if (p < p1) {
				} else if (p < p2) {
					v += 1 << (s - 1);
				} else if (p < p3) {
					u += 1 << (s - 1);
				} else if (p < p4) {
					u += 1 << (s - 1);
					v += 1 << (s - 1);
				} else {
					throw new AssertionError();
				}
			}
			if (edges.get(u * N + v))
				continue;
			edges.set(u * N + v);
			edgeNum++;
		}

		IndexGraphBuilder builder = directed ? IndexGraphBuilder.newDirected() : IndexGraphBuilder.newUndirected();
		builder.expectedVerticesNum(n);
		for (int i = 0; i < n; i++)
			builder.addVertex();
		if (directed) {
			for (int u = 0; u < n; u++)
				for (int v = 0; v < n; v++)
					if (edges.get(u * N + v))
						builder.addEdge(u, v);
		} else {
			for (int u = 0; u < n; u++)
				for (int v = u; v < n; v++)
					if (edges.get(u * N + v))
						builder.addEdge(u, v);
		}
		return builder.reIndexAndBuild(true, true).graph();
	}

	private static int nextPowerOf2(int x) {
		return x == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(x - 1);
	}

}
