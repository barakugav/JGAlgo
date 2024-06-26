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
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;

/**
 * The Recursive Largest First coloring algorithm.
 *
 * <p>
 * The Recursive Largest First (RLF) coloring algorithm assign colors to vertices in the following way: identify a
 * maximal independent set \(S\), assign to \(S\) a new color, repeat as long as there are uncolored vertices. The
 * maximal independent set is chosen in a greedy fashion; the vertex with the maximum number of uncolored vertices is
 * first added to \(S\), than vertices are added one after another by choosing the vertex with maximum number of
 * neighbors adjacent to vertices in \(S\), until no more vertices can be added to \(S\) (all uncolored vertices are
 * adjacent to vertices in \(S\)).
 *
 * <p>
 * The algorithm runs in time \(O(n m)\).
 *
 * <p>
 * Note that the result is an approximation for the minimum number of colors, as finding an optimal coloring is an
 * NP-hard problem.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Recursive_largest_first_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class ColoringRecursiveLargestFirst extends ColoringAlgoAbstract {

	/**
	 * Create a new coloring algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link ColoringAlgo#newInstance()} to get a default implementation for the
	 * {@link ColoringAlgo} interface.
	 */
	public ColoringRecursiveLargestFirst() {}

	@Override
	protected IVertexPartition computeColoring(IndexGraph g) {
		Assertions.onlyUndirected(g);
		Assertions.noSelfEdges(g, "no valid coloring in graphs with self edges");

		final int n = g.vertices().size();
		int[] colors = new int[n];
		int colorsNum = 0;
		Arrays.fill(colors, -1);
		int[] degree = new int[n];
		for (int u : range(n))
			degree[u] = g.outEdges(u).size();

		Bitmap S = new Bitmap(n);
		Bitmap isAdjacentToS = new Bitmap(n);

		for (int color = 0;; color++) {
			S.clear();
			isAdjacentToS.clear();

			int bestDegree = -1, firstU = -1;
			for (int u : range(n)) {
				if (colors[u] >= 0)
					continue;
				int d = degree[u];
				if (bestDegree < d) {
					bestDegree = d;
					firstU = u;
				}
			}
			if (firstU < 0) {
				colorsNum = color;
				break;
			}

			for (int u = firstU; u >= 0;) {
				// add u to S
				S.set(u);

				// update info
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (colors[v] >= 0)
						continue;
					isAdjacentToS.set(v);
				}

				int nextU = -1, bestNumOfNeighborsAdjacentToS = -1;
				bestDegree = -1;
				for (int v : range(n)) {
					if (colors[v] >= 0 || S.get(v) || isAdjacentToS.get(v))
						continue;
					int numOfNeighborsAdjacentToS = 0;
					for (IEdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
						eit.nextInt();
						int w = eit.targetInt();
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

			for (int u : S) {
				colors[u] = color;

				// update degree to include only vertices without color
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					degree[eit.targetInt()]--;
				}
			}
		}

		return IVertexPartition.fromArray(g, colors, colorsNum);
	}

}
