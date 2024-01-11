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
import java.util.Optional;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntStack;

class EulerianTourImpl implements EulerianTourAlgo {

	Optional<IPath> computeEulerianTour(IndexGraph g) {
		return g.isDirected() ? computeTourDirected(g) : computeTourUndirected(g);
	}

	private static Optional<IPath> computeTourUndirected(IndexGraph g) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		int start = -1, end = -1;
		for (int u : range(n)) {
			if (degreeWithoutSelfLoops(g, u) % 2 == 0)
				continue;
			if (start == -1) {
				start = u;
			} else if (end == -1) {
				end = u;
			} else {
				/* More than two vertices have an odd degree */
				return Optional.empty();
			}
		}
		if (start == -1) {
			assert end == -1;
			start = end = 0;
		}

		Bitmap usedEdges = new Bitmap(m);
		IEdgeIter[] iters = new IEdgeIter[n];
		for (int u : range(n))
			iters[u] = g.outEdges(u).iterator();

		IntArrayList tour = new IntArrayList(g.edges().size());
		IntStack queue = new IntArrayList();

		for (int u = end;;) {
			findCycle: for (;;) {
				int e, v;
				for (IEdgeIter iter = iters[u];;) {
					if (!iter.hasNext())
						break findCycle;
					e = iter.nextInt();
					if (!usedEdges.get(e)) {
						v = iter.targetInt();
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

		if (usedEdges.cardinality() != m)
			/* Graph is not connected */
			return Optional.empty();

		return Optional.of(new PathImpl(g, start, end, tour));
	}

	private static int degreeWithoutSelfLoops(IndexGraph g, int u) {
		int d = 0;
		for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
			eit.nextInt();
			if (eit.targetInt() != u)
				d++;
		}
		return d;
	}

	private static Optional<IPath> computeTourDirected(IndexGraph g) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		int start = -1, end = -1;
		for (int u : range(n)) {
			int outD = g.outEdges(u).size();
			int inD = g.inEdges(u).size();
			if (outD == inD)
				continue;
			if (outD == inD + 1) {
				if (start == -1) {
					start = u;
				} else {
					/* More than one vertex have an extra out edge */
					return Optional.empty();
				}
			} else if (outD + 1 == inD) {
				if (end == -1) {
					end = u;
				} else {
					/* More than one vertex have an extra in edge */
					return Optional.empty();
				}
			} else {
				/* Can't compute Eulerian tour with vertex degrees */
				return Optional.empty();
			}
		}
		if (start == -1) {
			assert end == -1;
			start = end = 0;
		}

		Bitmap usedEdges = new Bitmap(m);
		IEdgeIter[] iters = new IEdgeIter[n];
		for (int u : range(n))
			iters[u] = g.outEdges(u).iterator();

		IntArrayList tour = new IntArrayList(g.edges().size());
		IntStack queue = new IntArrayList();

		for (int u = start;;) {
			findCycle: for (;;) {
				int e, v;
				for (IEdgeIter iter = iters[u];;) {
					if (!iter.hasNext())
						break findCycle;
					e = iter.nextInt();
					if (!usedEdges.get(e)) {
						v = iter.targetInt();
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

		if (usedEdges.cardinality() != m)
			/* Graph is not connected */
			return Optional.empty();

		IntArrays.reverse(tour.elements(), 0, tour.size());
		return Optional.of(new PathImpl(g, start, end, tour));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Optional<Path<V, E>> computeEulerianTourIfExist(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (Optional) computeEulerianTour((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			Optional<IPath> indexPath = computeEulerianTour(iGraph);
			return indexPath.map(path -> PathImpl.pathFromIndexPath(g, path));
		}
	}

}
