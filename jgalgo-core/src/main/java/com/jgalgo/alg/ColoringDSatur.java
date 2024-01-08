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
import java.util.BitSet;
import java.util.Objects;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.LongIntReferenceableHeap;
import com.jgalgo.internal.ds.ReferenceableHeap;
import com.jgalgo.internal.util.Assertions;

/**
 * The DSatur coloring algorithm.
 *
 * <p>
 * The Saturation Degree (DSatur) coloring algorithm is a greedy algorithm, namely it examine the vertices in some order
 * and assign for each vertex the minimum (integer) color which is not used by its neighbors. It differ from other
 * greedy coloring algorithms by the order of the vertices: the next vertex to be colored is the vertex with the highest
 * number of colors in its neighborhood (called saturation degree).
 *
 * <p>
 * The algorithm runs in \(O(m \log n)\) time assuming the number of colors is constant.
 *
 * <p>
 * Note that the result is an approximate for the minimum number of colors, as finding an optimal coloring is an NP-hard
 * problem.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/DSatur">Wikipedia</a>
 * @author Barak Ugav
 */
class ColoringDSatur implements ColoringAlgoBase {

	private ReferenceableHeap.Builder heapBuilder = ReferenceableHeap.builder();

	/**
	 * Create a new coloring algorithm object.
	 */
	ColoringDSatur() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(ReferenceableHeap.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	@Override
	public IVertexPartition computeColoring(IndexGraph g) {
		Assertions.onlyUndirected(g);
		Assertions.noSelfEdges(g, "no valid coloring in graphs with self edges");

		final int n = g.vertices().size();
		int[] colors = new int[n];
		int colorsNum = 0;
		BitSet[] neighborColors = new BitSet[n];
		Arrays.fill(colors, -1);

		LongIntReferenceableHeap heap = (LongIntReferenceableHeap) heapBuilder.build(long.class, int.class);
		LongIntReferenceableHeap.Ref[] refs = new LongIntReferenceableHeap.Ref[n];
		for (int u = 0; u < n; u++) {
			long key = heapKey(0, g.outEdges(u).size());
			refs[u] = heap.insert(key, u);
			neighborColors[u] = new BitSet();
		}

		while (heap.isNotEmpty()) {
			int u = heap.extractMin().value();

			int color = colors[u] = neighborColors[u].nextClearBit(0);
			colorsNum = Math.max(colorsNum, color + 1);

			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (colors[v] == -1) { /* v is uncolored */
					LongIntReferenceableHeap.Ref ref = refs[v];
					long key = ref.key();
					int saturationDegree = heapKeyToSaturationDegree(key);
					int uncoloredDegree = heapKeyToUncoloredDegree(key);

					/* we colored u, v has one less uncolored neighbor */
					uncoloredDegree--;

					if (!neighborColors[v].get(color)) {
						/* v has one more unique color in its neighborhood */
						neighborColors[v].set(color);
						saturationDegree++;

						key = heapKey(saturationDegree, uncoloredDegree);
						heap.decreaseKey(ref, key);
					} else {

						/*
						 * we would prefer to use decreaseKey, but we only decrease the uncolored degree, which is
						 * increaseKey with respect to the heap ordering. pay \(O(\log n)\) instead of \(O(1)\)
						 */
						key = heapKey(saturationDegree, uncoloredDegree);
						heap.increaseKey(ref, key);
					}

				}
			}
		}
		return new VertexPartitions.Impl(g, colorsNum, colors);
	}

	private static long heapKey(int saturationDegree, int uncoloredDegree) {
		/* We want to compose both the saturationDegree and uncoloredDegree in a single long key */
		/* negate saturationDegree, more neighbor colors should be extracted from the heap first */
		return -(((long) saturationDegree << 32) + uncoloredDegree);
	}

	private static int heapKeyToSaturationDegree(long key) {
		return (int) (-key >> 32);
	}

	private static int heapKeyToUncoloredDegree(long key) {
		return (int) ((-key) & ((1L << 32) - 1));
	}

}
