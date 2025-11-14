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

package com.jgalgo.alg.shortestpath;

import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.euler.EulerianTourAlgo;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

class TspMetricUtils {

	private TspMetricUtils() {}

	static IPath calcEulerianTourAndConvertToHamiltonianCycle(IndexGraph g, IndexGraph g1, int[] edgeRef) {
		int n = g.vertices().size();

		/* Assert degree is actually even in the new graph */
		assert g.vertices().intStream().allMatch(v -> g1.outEdges(v).size() % 2 == 0);

		/* Calculate Eulerian tour in the new graph */
		IPath tour = (IPath) EulerianTourAlgo.newInstance().computeEulerianTour(g1);
		assert tour.sourceInt() == tour.targetInt();
		assert IPath.isPath(g1, tour.sourceInt(), tour.targetInt(), tour.edges());
		assert g1.vertices().equals(new IntOpenHashSet(tour.vertices()));

		/* Use shortcuts to convert to a Hamiltonian cycle */
		IntList cycle = new IntArrayList(n);
		int firstVertex = -1, lastVertex = -1;
		Bitmap visited = new Bitmap(n);
		for (IEdgeIter it = tour.edgeIter(); it.hasNext();) {
			int e0 = it.nextInt();
			int e = edgeRef[e0];
			final int u = it.sourceInt();
			if (firstVertex < 0)
				firstVertex = u;
			visited.set(u);
			while (visited.get(it.targetInt()) && it.hasNext()) {
				it.nextInt();
				e = g.getEdge(u, it.targetInt());
			}
			cycle.add(e);
			lastVertex = it.targetInt();
		}

		assert firstVertex == lastVertex;
		assert IPath.isPath(g, firstVertex, lastVertex, cycle);
		IPath cycle0 = IPath.valueOf(g, firstVertex, lastVertex, cycle);
		assert g.vertices().equals(new IntOpenHashSet(cycle0.vertices()));
		return cycle0;
	}

}
