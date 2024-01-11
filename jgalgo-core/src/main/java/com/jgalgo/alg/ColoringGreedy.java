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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * A greedy coloring algorithm with random vertices order.
 *
 * <p>
 * The algorithm examine the vertices in random order and assign for each vertex the minimum (integer) color which is
 * not used by its neighbors.
 *
 * <p>
 * The algorithm runs in linear time, assuming the number of colors is constant.
 *
 * <p>
 * Note that the result is an approximate for the minimum number of colors, as finding an optimal coloring is an NP-hard
 * problem.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Greedy_coloring">Wikipedia</a>
 * @author Barak Ugav
 */
class ColoringGreedy implements ColoringAlgoBase, RandomizedAlgorithm {

	private final Random rand = new Random();

	@Override
	public void setSeed(long seed) {
		rand.setSeed(seed);
	}

	@Override
	public IVertexPartition computeColoring(IndexGraph g) {
		Assertions.onlyUndirected(g);
		Assertions.noSelfEdges(g, "no valid coloring in graphs with self edges");

		final int n = g.vertices().size();
		int[] colors = new int[n];
		int colorsNum = 0;
		Arrays.fill(colors, -1);
		int[] order = range(n).toIntArray();
		IntArrays.shuffle(order, rand);

		BitSet usedColors = new BitSet();
		for (int u : order) {
			usedColors.clear();
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				int c = colors[v];
				if (c != -1)
					usedColors.set(c);
			}
			int color = colors[u] = usedColors.nextClearBit(0);
			colorsNum = Math.max(colorsNum, color + 1);
		}
		return new VertexPartitions.Impl(g, colorsNum, colors);
	}

}
