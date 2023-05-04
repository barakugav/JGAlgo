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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A greedy coloring algorithm.
 * <p>
 * The algorithm examine the vertices in an arbitrary order and assign for each vertex the minimum (integer) color which
 * is not used by its neighbors.
 * <p>
 * The algorithm runs in linear time, assuming the number of colors is constant.
 * <p>
 * Note that the result is an approximate for the minimum number of colors, as finding an optimal coloring is an NP-hard
 * problem.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Greedy_coloring">Wikipedia</a>
 * @author Barak Ugav
 */
public class ColoringGreedy implements Coloring {

	/**
	 * Create a new coloring algorithm object.
	 */
	public ColoringGreedy() {}

	@Override
	public Coloring.Result computeColoring(Graph g) {
		ArgumentCheck.onlyUndirected(g);
		ArgumentCheck.noSelfLoops(g, "no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();
		IntSet usedColors = new IntOpenHashSet();
		for (int u = 0; u < n; u++) {
			usedColors.clear();
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				int c = res.colorOf(v);
				if (c != -1)
					usedColors.add(c);
			}
			int color = 0;
			while (usedColors.contains(color))
				color++;
			res.colors[u] = color;
			res.colorsNum = Math.max(res.colorsNum, color + 1);
		}
		return res;
	}

}
