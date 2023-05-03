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

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * The DSatur coloring algorithm.
 * <p>
 * The Saturation Degree (DSatur) coloring algorithm is a greedy algorithm, namely it examine the vertices in some order
 * and assign for each vertex the minimum (integer) color which is not used by its neighbors. It differ from other
 * greedy coloring algorithm by the order of the vertices: the next vertex to be colored is the vertex with the highest
 * number of colors in its neighborhood (called saturation degree).
 * <p>
 * The algorithm runs in time \(O(n m)\), and it could be implemented faster using a heap with {@code decreaseKey}
 * operation, see {@link ColoringDSaturHeap}.
 * <p>
 * Note that the result is an approximate for the minimum number of colors, as finding an optimal coloring is an NP-hard
 * problem.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/DSatur">Wikipedia</a>
 * @see    ColoringDSaturHeap
 * @author Barak Ugav
 */
public class ColoringDSatur implements Coloring {

	/**
	 * Create a new coloring algorithm object.
	 */
	public ColoringDSatur() {}

	@Override
	public Coloring.Result computeColoring(UGraph g) {
		if (GraphsUtils.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();

		IntSet uncolored = new IntOpenHashSet(n);
		BitSet[] usedColors = new BitSet[n];
		int[] usedColorsNum = new int[n];
		int[] degree = new int[n];
		for (int u = 0; u < n; u++) {
			uncolored.add(u);
			usedColors[u] = new BitSet();
			degree[u] = g.degreeOut(u);
		}

		while (!uncolored.isEmpty()) {
			int u = uncolored.iterator().nextInt();
			for (IntIterator it = uncolored.iterator(); it.hasNext();) {
				int u1 = it.nextInt();
				int s1 = usedColorsNum[u], s2 = usedColorsNum[u1];
				if (s1 > s2 || (s1 == s2 && degree[u] > degree[u1]))
					u = u1;
			}

			int color = 0;
			while (usedColors[u].get(color))
				color++;
			res.colors[u] = color;
			res.colorsNum = Math.max(res.colorsNum, color + 1);
			uncolored.remove(u);
			usedColors[u].clear();
			usedColorsNum[u] = 0;
			usedColors[u] = null;

			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (res.colorOf(v) == -1) {/* v is uncolored */
					if (!usedColors[v].get(color)) {
						usedColorsNum[v]++;
						usedColors[v].set(color);
					}
				}
			}
		}
		return res;
	}

}
