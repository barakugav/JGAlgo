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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntStack;

class EulerianTourImpl implements EulerianTourAlgorithm {

	/**
	 * {@inheritDoc}
	 * <p>
	 * The running time and space of this function is \(O(n + m)\).
	 */
	@Override
	public Path computeEulerianTour(Graph g) {
		return g.getCapabilities().directed() ? computeTourDirected(g) : computeTourUndirected(g);
	}

	private static Path computeTourUndirected(Graph g) {
		int n = g.vertices().size();

		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			if (degreeWithoutSelfLoops(g, u) % 2 == 0)
				continue;
			if (start == -1)
				start = u;
			else if (end == -1)
				end = u;
			else
				throw new IllegalArgumentException(
						"More than two vertices have an odd degree (" + start + ", " + end + ", " + u + ")");
		}
		if (start != -1 ^ end != -1)
			throw new IllegalArgumentException(
					"Eulerian tour exists only if all vertices have even degree or only two vertices have odd degree");
		if (start == -1)
			start = 0;
		if (end == -1)
			end = 0;

		Weights.Bool usedEdges = Weights.createExternalEdgesWeights(g, boolean.class);
		EdgeIter[] iters = new EdgeIter[n];
		for (int u = 0; u < n; u++)
			iters[u] = g.edgesOut(u);

		IntArrayList tour = new IntArrayList(g.edges().size());
		IntStack queue = new IntArrayList();

		for (int u = end;;) {
			findCycle: for (;;) {
				int e, v;
				for (EdgeIter iter = iters[u];;) {
					if (!iter.hasNext())
						break findCycle;
					e = iter.nextInt();
					if (!usedEdges.getBool(e)) {
						v = iter.target();
						break;
					}
				}
				usedEdges.set(e, true);
				queue.push(e);
				u = v;
			}

			if (queue.isEmpty())
				break;

			int e = queue.popInt();
			tour.add(e);
			u = g.edgeEndpoint(e, u);
		}

		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (!usedEdges.getBool(e))
				throw new IllegalArgumentException("Graph is not connected");
		}
		return new PathImpl(g, start, end, tour);
	}

	private static int degreeWithoutSelfLoops(Graph g, int u) {
		int d = 0;
		for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			if (eit.target() != u)
				d++;
		}
		return d;
	}

	private static Path computeTourDirected(Graph g) {
		int n = g.vertices().size();

		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			int outD = g.degreeOut(u);
			int inD = g.degreeIn(u);
			if (outD == inD)
				continue;
			if (outD == inD + 1) {
				if (start == -1) {
					start = u;
				} else {
					throw new IllegalArgumentException(
							"More than one vertex have an extra out edge (" + start + ", " + u + ")");
				}
			} else if (outD + 1 == inD) {
				if (end == -1) {
					end = u;
				} else {
					throw new IllegalArgumentException(
							"More than one vertex have an extra in edge (" + end + ", " + u + ")");
				}
			} else {
				throw new IllegalArgumentException(
						"Can't compute Eulerian tour with vertex degrees (" + u + ": in=" + inD + " out=" + outD + ")");
			}
		}
		if (start != -1 ^ end != -1)
			throw new IllegalArgumentException("Eulerian tour exists in a directed graph only if all vertices have "
					+ "equal in and out degree or only one have an extra in edge and one have an extra out edge");
		if (start == -1)
			start = 0;
		if (end == -1)
			end = 0;

		Weights.Bool usedEdges = Weights.createExternalEdgesWeights(g, boolean.class);
		EdgeIter[] iters = new EdgeIter[n];
		for (int u = 0; u < n; u++)
			iters[u] = g.edgesOut(u);

		IntArrayList tour = new IntArrayList(g.edges().size());
		IntStack queue = new IntArrayList();

		for (int u = start;;) {
			findCycle: for (;;) {
				int e, v;
				for (EdgeIter iter = iters[u];;) {
					if (!iter.hasNext())
						break findCycle;
					e = iter.nextInt();
					if (!usedEdges.getBool(e)) {
						v = iter.target();
						break;
					}
				}
				usedEdges.set(e, true);
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

		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (!usedEdges.getBool(e))
				throw new IllegalArgumentException("Graph is not connected");
		}
		IntArrays.reverse(tour.elements(), 0, tour.size());
		return new PathImpl(g, start, end, tour);
	}

}
