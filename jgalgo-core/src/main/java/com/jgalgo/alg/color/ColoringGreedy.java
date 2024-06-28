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

package com.jgalgo.alg.color;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.common.RandomizedAlgorithm;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * A greedy coloring algorithm with random vertices order.
 *
 * <p>
 * The algorithm examine the vertices in a random order and assign for each vertex the minimum (integer) color which is
 * not used by its neighbors.
 *
 * <p>
 * The algorithm runs in linear time, assuming the number of colors is constant.
 *
 * <p>
 * For deterministic behavior, set the seed using {@link #setSeed(long)}.
 *
 * <p>
 * Note that the result is an approximation for the minimum number of colors, as finding an optimal coloring is an
 * NP-hard problem.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Greedy_coloring">Wikipedia</a>
 * @author Barak Ugav
 */
public class ColoringGreedy extends ColoringAlgoAbstract implements RandomizedAlgorithm {

	private final Random rand = new Random();

	/**
	 * Create a new coloring algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link ColoringAlgo#newInstance()} to get a default implementation for the
	 * {@link ColoringAlgo} interface.
	 */
	public ColoringGreedy() {}

	@Override
	public void setSeed(long seed) {
		rand.setSeed(seed);
	}

	@Override
	protected IVertexPartition computeColoring(IndexGraph g) {
		Assertions.onlyUndirected(g);
		Assertions.noSelfEdges(g, "no valid coloring in graphs with self edges");

		final int n = g.vertices().size();
		int[] colors = new int[n];
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
				if (c >= 0)
					usedColors.set(c);
			}
			colors[u] = usedColors.nextClearBit(0);
		}
		int colorsNum = Arrays.stream(colors).max().orElse(-1) + 1;
		return IVertexPartition.fromArray(g, colors, colorsNum);
	}

}
