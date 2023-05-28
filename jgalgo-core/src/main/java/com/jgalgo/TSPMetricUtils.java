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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

class TSPMetricUtils {

	private TSPMetricUtils() {}

	static Path calcEulerianTourAndConvertToHamiltonianCycle(Graph g, Graph g1, Weights.Int edgeRef) {
		int n = g.vertices().size();

		/* Assert degree is actually even in the new graph */
		for (int u = 0; u < n; u++)
			assert g1.degreeOut(u) % 2 == 0;

		/* Calculate Eulerian tour in the new graph */
		Path tour = EulerianTourAlgorithm.newBuilder().build().computeEulerianTour(g1);
		assert isValidCycle(g1, tour);
		assert isPathVisitEvery(g1, tour);

		/* Use shortcuts to convert to a Hamiltonian cycle */
		IntList cycle = new IntArrayList(n);
		int firstVertex = -1, lastVertex = -1;
		BitSet visited = new BitSet(n);
		for (EdgeIter it = tour.edgeIter(); it.hasNext();) {
			int e0 = it.nextInt();
			int e = edgeRef.getInt(e0);
			final int u = it.source();
			if (firstVertex == -1)
				firstVertex = u;
			visited.set(u);
			while (visited.get(it.target()) && it.hasNext()) {
				it.nextInt();
				e = g.getEdge(u, it.target());
			}
			cycle.add(e);
			lastVertex = it.target();
		}

		assert firstVertex == lastVertex;
		Path cycle0 = new PathImpl(g, firstVertex, lastVertex, cycle);
		assert isValidCycle(g, cycle0);
		assert isPathVisitEvery(g, cycle0);
		return cycle0;
	}

	private static boolean isValidCycle(Graph g, Path path) {
		EdgeIter it = path.edgeIter();
		it.nextInt();
		final int begin = it.source();
		for (;;) {
			if (!it.hasNext())
				return it.target() == begin;
			int lastV = it.target();
			it.nextInt();
			if (lastV != it.source())
				return false;
		}
	}

	private static boolean isPathVisitEvery(Graph g, Path path) {
		final int n = g.vertices().size();
		BitSet visited = new BitSet(n);
		for (IntIterator it = path.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			visited.set(u);
			visited.set(v);
		}
		for (int u = 0; u < n; u++)
			if (!visited.get(u))
				return false;
		return true;
	}

	static void checkNoParallelEdges(Graph g) {
		if (GraphsUtils.containsParallelEdges(g))
			throw new IllegalArgumentException("Graph contains parallel edges");
	}

	// static void checkArgDistanceTableIsMetric(double[][] distances) {
	// final double eps = 0.001;
	// int n = distances.length;
	// for (int u = 0; u < n; u++)
	// if (distances[u].length != n)
	// throw new IllegalArgumentException("Distances table is not full");
	// for (int u = 0; u < n; u++)
	// for (int v = u + 1; v < n; v++)
	// for (int w = v + 1; w < n; w++)
	// if (distances[u][v] + distances[v][w] + eps < distances[u][w])
	// throw new IllegalArgumentException("Distance table is not metric: (" + u + ", " + v + ", " + w
	// + ") " + distances[u][v] + " + " + distances[v][w] + " < " + distances[u][w]);
	// }

}
