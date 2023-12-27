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

import java.util.List;
import java.util.Optional;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Eulerian tour calculation algorithm.
 *
 * <p>
 * An Eulerian tour is a tour that visits every edge exactly once (allowing for revisiting vertices). For a connected
 * undirected graph, if all vertices have an even degree, an Eulerian cycle will be found. If exactly two vertices have
 * an odd degree, called \(s,t\), an Eulerian tour that start at \(s\) and ends at \(t\) exists. For any other vertices
 * degrees an Eulerian tour does not exists. For a strongly connected directed graph, the in-degree and out-degree of
 * each vertex must be equal for an Eulerian cycle to exists. If exactly one vertex \(s\) has one more out-edge than
 * in-edges, and one vertex \(t\) has one more in-edge than out-edges, an Eulerian tour that start at \(s\) and ends at
 * \(t\) exists.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Eulerian_path">Wikipedia</a>
 * @author Barak Ugav
 */
public interface EulerianTourAlgo {

	/**
	 * Compute an Eulerian tour in the graph that visit all edges exactly once.
	 *
	 * <p>
	 * The graph is assumed to be (strongly) connected. Either a cycle or tour will be found, depending on the vertices
	 * degrees.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IPath}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @return                          an Eulerian tour that visit all edges of the graph exactly once
	 * @throws IllegalArgumentException if there is no Eulerian tour in the graph
	 */
	default <V, E> Path<V, E> computeEulerianTour(Graph<V, E> g) {
		return computeEulerianTourIfExist(g).orElseThrow(() -> new IllegalArgumentException("No Eulerian tour exists"));
	}

	/**
	 * Compute an Eulerian tour in the graph that visit all edges exactly once if one exists.
	 *
	 * <p>
	 * The graph is assumed to be (strongly) connected. Either a cycle or tour will be found, depending on the vertices
	 * degrees.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned optional will contain {@link IPath}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     an Eulerian tour that visit all edges of the graph exactly once if one exists
	 */
	<V, E> Optional<Path<V, E>> computeEulerianTourIfExist(Graph<V, E> g);

	/**
	 * Check whether a graph is Eulerian.
	 *
	 * <p>
	 * A graph is Eulerian if it contains an Eulerian tour.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     {@code true} if the graph is Eulerian, {@code false} otherwise
	 */
	default <V, E> boolean isEulerian(Graph<V, E> g) {
		return computeEulerianTourIfExist(g).isPresent();
	}

	/**
	 * Check if the given tour is an Eulerian tour in the given graph.
	 *
	 * <p>
	 * A list of edges form an Eulerian tour in a graph if it firstly is a valid path in the graph, and it visit all
	 * edges of the graph exactly once.
	 *
	 * @param  <V>  the vertices type
	 * @param  <E>  the edges type
	 * @param  g    a graph
	 * @param  tour a list of edges that should form an Eulerian tour in the graph
	 * @return      {@code true} if the given tour is an Eulerian tour in the given graph, {@code false} otherwise
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isEulerianTour(Graph<V, E> g, List<E> tour) {
		IndexGraph ig;
		IntList tour0;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			tour0 = IntAdapters.asIntList((List<Integer>) tour);
		} else {
			ig = g.indexGraph();
			tour0 = IndexIdMaps.idToIndexList(tour, g.indexGraphEdgesMap());
		}

		final int m = ig.edges().size();
		if (tour0.size() != m)
			return false;
		if (m == 0)
			return true;
		Bitmap visited = new Bitmap(m);
		if (ig.isDirected()) {
			int u = ig.edgeSource(tour0.getInt(0));
			IntIterator it = tour0.iterator();
			for (int i = 0; i < m; i++) {
				int e = it.nextInt();
				if (visited.get(e))
					return false;
				visited.set(e);
				if (ig.edgeSource(e) != u)
					return false;
				u = ig.edgeTarget(e);
			}
			return true;

		} else {
			int firstEdge = tour0.getInt(0);
			boolean foundValidStartingEndpoint = false;
			startingEndpointLoop: for (boolean startingEndpoint : new boolean[] { true, false }) {
				visited.clear();
				int u = startingEndpoint ? ig.edgeSource(firstEdge) : ig.edgeTarget(firstEdge);
				IntIterator it = tour0.iterator();
				for (int i = 0; i < m; i++) {
					int e = it.nextInt();
					if (visited.get(e))
						return false;
					visited.set(e);
					int eu = ig.edgeSource(e), ev = ig.edgeTarget(e);
					if (u == eu) {
						u = ev;
					} else if (u == ev) {
						u = eu;
					} else {
						continue startingEndpointLoop;
					}
				}
				foundValidStartingEndpoint = true;
				break;
			}
			return foundValidStartingEndpoint;
		}
	}

	/**
	 * Create a new Eulerian tour computation algorithm.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link EulerianTourAlgo} object.
	 *
	 * @return a default implementation of {@link EulerianTourAlgo}
	 */
	static EulerianTourAlgo newInstance() {
		return new EulerianTourImpl();
	}

}
