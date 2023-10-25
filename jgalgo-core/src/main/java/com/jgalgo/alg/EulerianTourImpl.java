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

import java.util.BitSet;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntStack;

class EulerianTourImpl implements EulerianTourAlgo {

	/**
	 * {@inheritDoc}
	 * <p>
	 * The running time and space of this function is \(O(n + m)\).
	 */
	Path computeEulerianTour(IndexGraph g) {
		return g.isDirected() ? computeTourDirected(g) : computeTourUndirected(g);
	}

	private static Path computeTourUndirected(IndexGraph g) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			if (degreeWithoutSelfLoops(g, u) % 2 == 0)
				continue;
			if (start == -1)
				start = u;
			else if (end == -1)
				end = u;
			else
				throw new IllegalArgumentException("More than two vertices have an odd degree. Vertices indices: "
						+ start + ", " + end + ", " + u);
		}
		if (start != -1 ^ end != -1)
			throw new IllegalArgumentException(
					"Eulerian tour exists only if all vertices have even degree or only two vertices have odd degree");
		if (start == -1)
			start = 0;
		if (end == -1)
			end = 0;

		BitSet usedEdges = new BitSet(m);
		EdgeIter[] iters = new EdgeIter[n];
		for (int u = 0; u < n; u++)
			iters[u] = g.outEdges(u).iterator();

		IntArrayList tour = new IntArrayList(g.edges().size());
		IntStack queue = new IntArrayList();

		for (int u = end;;) {
			findCycle: for (;;) {
				int e, v;
				for (EdgeIter iter = iters[u];;) {
					if (!iter.hasNext())
						break findCycle;
					e = iter.nextInt();
					if (!usedEdges.get(e)) {
						v = iter.target();
						break;
					}
				}
				usedEdges.set(e);
				queue.push(e);
				u = v;
			}

			if (queue.isEmpty())
				break;

			int e = queue.popInt();
			tour.add(e);
			u = g.edgeEndpoint(e, u);
		}

		for (int e = 0; e < m; e++)
			if (!usedEdges.get(e))
				throw new IllegalArgumentException("Graph is not connected");
		return new PathImpl(g, start, end, tour);
	}

	private static int degreeWithoutSelfLoops(IndexGraph g, int u) {
		int d = 0;
		for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
			eit.nextInt();
			if (eit.target() != u)
				d++;
		}
		return d;
	}

	private static Path computeTourDirected(IndexGraph g) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			int outD = g.outEdges(u).size();
			int inD = g.inEdges(u).size();
			if (outD == inD)
				continue;
			if (outD == inD + 1) {
				if (start == -1) {
					start = u;
				} else {
					throw new IllegalArgumentException(
							"More than one vertex have an extra out edge. Vertices indices: " + start + ", " + u);
				}
			} else if (outD + 1 == inD) {
				if (end == -1) {
					end = u;
				} else {
					throw new IllegalArgumentException(
							"More than one vertex have an extra in edge. Vertices indices: " + end + ", " + u);
				}
			} else {
				throw new IllegalArgumentException(
						"Can't compute Eulerian tour with vertex degrees, index=" + u + ": in=" + inD + " out=" + outD);
			}
		}
		if (start != -1 ^ end != -1)
			throw new IllegalArgumentException("Eulerian tour exists in a directed graph only if all vertices have "
					+ "equal in and out degree or only one have an extra in edge and one have an extra out edge");
		if (start == -1)
			start = 0;
		if (end == -1)
			end = 0;

		BitSet usedEdges = new BitSet(m);
		EdgeIter[] iters = new EdgeIter[n];
		for (int u = 0; u < n; u++)
			iters[u] = g.outEdges(u).iterator();

		IntArrayList tour = new IntArrayList(g.edges().size());
		IntStack queue = new IntArrayList();

		for (int u = start;;) {
			findCycle: for (;;) {
				int e, v;
				for (EdgeIter iter = iters[u];;) {
					if (!iter.hasNext())
						break findCycle;
					e = iter.nextInt();
					if (!usedEdges.get(e)) {
						v = iter.target();
						break;
					}
				}
				usedEdges.set(e);
				queue.push(e);
				u = v;
			}

			if (queue.isEmpty())
				break;

			int e = queue.popInt();
			tour.add(e);
			assert g.edgeTarget(e) == u;
			u = g.edgeSource(e);
		}

		for (int e = 0; e < m; e++)
			if (!usedEdges.get(e))
				throw new IllegalArgumentException("Graph is not connected");
		IntArrays.reverse(tour.elements(), 0, tour.size());
		return new PathImpl(g, start, end, tour);
	}

	@Override
	public Path computeEulerianTour(Graph g) {
		if (g instanceof IndexGraph)
			return computeEulerianTour((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		Path indexPath = computeEulerianTour(iGraph);
		return PathImpl.pathFromIndexPath(indexPath, viMap, eiMap);
	}

}
