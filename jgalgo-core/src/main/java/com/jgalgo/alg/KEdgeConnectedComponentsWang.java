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

import java.util.Arrays;
import java.util.Random;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Range;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Wang's algorithm for computing the k-edge connected components of a graph.
 *
 * <p>
 * Based on "A Simple Algorithm for Finding All k-EdgeConnected Components" by Tianhao Wang.
 *
 * @author Barak Ugav
 */
class KEdgeConnectedComponentsWang extends KEdgeConnectedComponentsUtils.AbstractImpl {

	private final Random rand;
	private final WeaklyConnectedComponentsAlgo wccAlgo = WeaklyConnectedComponentsAlgo.newInstance();
	private final MinimumCutST minCutAlgo = MinimumCutST.newInstance();

	/**
	 * Create a new algorithm object with a random seed.
	 */
	KEdgeConnectedComponentsWang() {
		rand = new Random();
	}

	/**
	 * Create a new algorithm object with a given seed.
	 *
	 * @param seed a seed for the random number generator
	 */
	KEdgeConnectedComponentsWang(long seed) {
		rand = new Random(seed);
	}

	@Override
	IVertexPartition computeKEdgeConnectedComponents(IndexGraph g, int k) {
		final int n = g.vertices().size();
		if (n == 0)
			return new VertexPartitions.Impl(g, 0, IntArrays.DEFAULT_EMPTY_ARRAY);

		IndexGraphBuilder auxGraph = IndexGraphBuilder.newUndirected();
		auxGraph.expectedVerticesNum(n);
		auxGraph.expectedEdgesNum(n - 1);
		int[] auxWeights = new int[n - 1];
		for (int v = 0; v < n; v++)
			auxGraph.addVertex();

		IntStack stack = new IntArrayList();
		IVertexPartition wccs = (IVertexPartition) wccAlgo.findWeaklyConnectedComponents(g);
		int[] vertices = new int[Range.of(wccs.numberOfBlocks()).intStream().map(b -> wccs.blockVertices(b).size())
				.max().orElse(0)];
		for (int wccNum = wccs.numberOfBlocks(), wccIdx = 0; wccIdx < wccNum; wccIdx++) {
			IntSet wccVertices = wccs.blockVertices(wccIdx);
			if (wccVertices.size() <= 1)
				continue;
			Object newArray = wccVertices.toArray(vertices);
			assert newArray == vertices;

			int from = 0, to = wccVertices.size();
			int source = vertices[rand.nextInt(to)];
			stack.push(source);
			stack.push(from);
			stack.push(to);

			while (!stack.isEmpty()) {
				to = stack.popInt();
				from = stack.popInt();
				source = stack.popInt();
				assert to - from > 1;

				int sink;
				do {
					sink = vertices[from + rand.nextInt(to - from)];
				} while (sink == source);
				if (rand.nextBoolean()) {
					int temp = source;
					source = sink;
					sink = temp;
				}

				IVertexBiPartition minCut = (IVertexBiPartition) minCutAlgo.computeMinimumCut(g,
						IWeightFunction.CardinalityWeightFunction, Integer.valueOf(source), Integer.valueOf(sink));
				int cutWeight = minCut.crossEdges().size();
				if (g.isDirected()) {
					IVertexBiPartition minCut2 = (IVertexBiPartition) minCutAlgo.computeMinimumCut(g,
							IWeightFunction.CardinalityWeightFunction, Integer.valueOf(sink), Integer.valueOf(source));
					int cutWeight2 = minCut2.crossEdges().size();
					if (cutWeight > cutWeight2) {
						cutWeight = cutWeight2;
						minCut = minCut2;
						int temp = source;
						source = sink;
						sink = temp;
					}
				}

				if (cutWeight > 0)
					auxWeights[auxGraph.addEdge(source, sink)] = cutWeight;

				int middle;
				sortLoop: for (int f = from, t = to - 1;;) {
					while (minCut.isLeft(vertices[f])) {
						if (++f == t) {
							middle = f + (minCut.isLeft(vertices[f]) ? 1 : 0);
							assert from < middle && middle < to && minCut.isRight(vertices[middle]);
							break sortLoop;
						}
					}
					while (minCut.isRight(vertices[t])) {
						if (f == --t) {
							middle = f + (minCut.isLeft(vertices[f]) ? 1 : 0);
							assert from < middle && middle < to && minCut.isRight(vertices[middle]);
							break sortLoop;
						}
					}
					int temp = vertices[f];
					vertices[f] = vertices[t];
					vertices[t] = temp;
				}

				int from1 = from, to1 = middle;
				int from2 = middle, to2 = to;
				if (to1 - from1 > 1) {
					stack.push(source);
					stack.push(from1);
					stack.push(to1);
				} else {
					assert to1 - from1 == 1;
					assert vertices[from1] == source;
				}
				if (to2 - from2 > 1) {
					stack.push(sink);
					stack.push(from2);
					stack.push(to2);
				} else {
					assert to2 - from2 == 1;
					assert vertices[from2] == sink;
				}
			}
		}
		return computeKEdgeConnectedComponentsFromAuxGraph(g, auxGraph.build(), auxWeights, k);
	}

	private static IVertexPartition computeKEdgeConnectedComponentsFromAuxGraph(IndexGraph origGraph,
			IndexGraph auxGraph, int[] auxWeights, int k) {
		assert !auxGraph.isDirected();
		final int n = auxGraph.vertices().size();
		assert n == origGraph.vertices().size();
		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		IntStack stack = new IntArrayList();
		for (int root = 0; root < n; root++) {
			if (comp[root] != -1)
				continue;
			final int compIdx = compNum++;
			stack.push(root);
			comp[root] = compIdx;

			while (!stack.isEmpty()) {
				int u = stack.popInt();

				for (IEdgeIter eit = auxGraph.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (auxWeights[e] < k)
						continue;
					int v = eit.targetInt();
					if (comp[v] != -1) {
						assert comp[v] == compIdx;
						continue;
					}
					comp[v] = compIdx;
					stack.push(v);
				}
			}
		}
		return new VertexPartitions.Impl(origGraph, compNum, comp);
	}

}