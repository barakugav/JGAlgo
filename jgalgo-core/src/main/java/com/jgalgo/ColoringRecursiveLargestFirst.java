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

/**
 * The Recursive Largest First coloring algorithm.
 * <p>
 * The Recursive Largest First (RLF) coloring algorithm assign colors to vertices in the following way: identify a
 * maximal independent set \(S\), assign to \(S\) a new color, repeat as long as there are uncolored vertices. The
 * maximal independent set is chosen in a greedy fashion; the vertex with the maximum number of uncolored vertices is
 * first added to \(S\), than vertices are added one after another by choosing the vertex with maximum number of
 * neighbors adjacent to vertices in \(S\), until no more vertices can be added to \(S\) (all uncolored vertices are
 * adjacent to vertices in \(S\)).
 * <p>
 * The algorithm runs in time \(O(n m)\).
 * <p>
 * Note that the result is an approximate for the minimum number of colors, as finding an optimal coloring is an NP-hard
 * problem.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Recursive_largest_first_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class ColoringRecursiveLargestFirst extends ColoringUtils.AbstractImpl {

	/**
	 * Create a new coloring algorithm object.
	 */
	ColoringRecursiveLargestFirst() {}

	@Override
	Coloring.Result computeColoring(IndexGraph g) {
		ArgumentCheck.onlyUndirected(g);
		ArgumentCheck.noSelfLoops(g, "no valid coloring in graphs with self loops");

		ColoringUtils.ResultImpl res = new ColoringUtils.ResultImpl(g);
		int n = g.vertices().size();
		int[] degree = new int[n];
		for (int u = 0; u < n; u++)
			degree[u] = g.edgesOut(u).size();

		BitSet S = new BitSet(n);
		BitSet isAdjacentToS = new BitSet(n);

		for (int color = 0;; color++) {
			S.clear();
			isAdjacentToS.clear();

			int bestDegree = -1, firstU = -1;
			for (int u = 0; u < n; u++) {
				if (res.colorOf(u) != -1)
					continue;
				int d = degree[u];
				if (bestDegree < d) {
					bestDegree = d;
					firstU = u;
				}
			}
			if (firstU == -1) {
				res.colorsNum = color;
				break;
			}

			for (int u = firstU; u != -1;) {
				// add u to S
				S.set(u);

				// update info
				for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.target();
					if (res.colorOf(v) != -1)
						continue;
					isAdjacentToS.set(v);
				}

				int nextU = -1, bestNumOfNeighborsAdjacentToS = -1;
				bestDegree = -1;
				for (int v = 0; v < n; v++) {
					if (res.colorOf(v) != -1 || S.get(v) || isAdjacentToS.get(v))
						continue;
					int numOfNeighborsAdjacentToS = 0;
					for (EdgeIter eit = g.edgesOut(v).iterator(); eit.hasNext();) {
						eit.nextInt();
						int w = eit.target();
						if (isAdjacentToS.get(w))
							numOfNeighborsAdjacentToS++;
					}
					if (bestNumOfNeighborsAdjacentToS < numOfNeighborsAdjacentToS
							|| (bestNumOfNeighborsAdjacentToS == numOfNeighborsAdjacentToS
									&& degree[nextU] > degree[v])) {
						nextU = v;
						bestNumOfNeighborsAdjacentToS = numOfNeighborsAdjacentToS;
					}
				}
				u = nextU;
			}

			for (int u : Utils.iterable(S)) {
				res.colors[u] = color;

				// update degree to include only vertices without color
				for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					degree[eit.target()]--;
				}
			}
		}

		return res;
	}

}
