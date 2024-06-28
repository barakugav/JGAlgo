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
package com.jgalgo.alg.hamilton;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Hamiltonian path/cycle algorithm.
 *
 * <p>
 * Given a graph \(G = (V, E)\), a path is a sequence of edges \(e_1, e_2, \ldots, e_k\) such that for each pair of
 * consecutive edges \(e_i, e_{i+1}\), the target of \(e_i\) is the source of \(e_{i+1}\). A cycle is a path where the
 * source of the first edge is the target of the last edge. We says that a path/cycle <i>visits</i> a vertex \(v\) if
 * \(v\) is the source or target of some edge in the path/cycle. A path/cycle is <i>Hamiltonian</i> if it visits each
 * vertex exactly once.
 *
 * <p>
 * There are few problems related to Hamiltonian paths/cycles:
 * <ul>
 * <li>Given a graph, find one/all Hamiltonian path/s.</li>
 * <li>Given a graph, find one/all Hamiltonian path/s that start and end at two given vertices.</li>
 * <li>Given a graph, find one/all Hamiltonian cycle/s.</li>
 * </ul>
 *
 * <p>
 * This interface provides algorithms for solving all the above problems. All of the above problems are NP-complete, so
 * the algorithms provided here are exponential in the worst case, and expect them to be useful on small graphs.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Hamiltonian_path">Wikipedia</a>
 * @author Barak Ugav
 */
public interface HamiltonianPathAlgo {

	/**
	 * Find a Hamiltonian path in the given graph.
	 *
	 * <p>
	 * An Hamiltonian path is a path that visits each vertex of the graph exactly once. This method returns an
	 * Hamiltonian path if one exists, or {@link Optional#empty()} otherwise. The returned path will start and end at
	 * two arbitrary vertices.
	 *
	 * <p>
	 * If the graph has no vertices, this methods return {@link Optional#empty()}. If the graph has only one vertex,
	 * this method returns an empty path (which is actually a cycle).
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned path will an instance of {@link IPath}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @return     an Hamiltonian path if one exists, or {@link Optional#empty()} otherwise
	 */
	default <V, E> Optional<Path<V, E>> hamiltonianPath(Graph<V, E> g) {
		Iterator<Path<V, E>> hamiltonianPaths = hamiltonianPathsIter(g);
		return hamiltonianPaths.hasNext() ? Optional.of(hamiltonianPaths.next()) : Optional.empty();
	}

	/**
	 * Find a Hamiltonian path in the given graph that start and end at two given vertices.
	 *
	 * <p>
	 * An Hamiltonian path is a path that visits each vertex of the graph exactly once. This method returns an
	 * Hamiltonian path that start at {@code source} and end at {@code target} if one exists, or
	 * {@link Optional#empty()} otherwise.
	 *
	 * <p>
	 * If the source and target are the same vertex, the return path will actually be an Hamiltonian cycle.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned path will an instance of {@link IPath}.
	 *
	 * @param  <V>                   the vertices type
	 * @param  <E>                   the edges type
	 * @param  g                     the graph
	 * @param  source                the source vertex
	 * @param  target                the target vertex
	 * @return                       an Hamiltonian path that start at {@code source} and end at {@code target} if one
	 *                               exists, or {@link Optional#empty()} otherwise
	 * @throws NoSuchVertexException if {@code source} or {@code target} are not vertices in {@code g}
	 */
	default <V, E> Optional<Path<V, E>> hamiltonianPath(Graph<V, E> g, V source, V target) {
		Iterator<Path<V, E>> hamiltonianPaths = hamiltonianPathsIter(g, source, target);
		return hamiltonianPaths.hasNext() ? Optional.of(hamiltonianPaths.next()) : Optional.empty();
	}

	/**
	 * Iterate over all Hamiltonian paths in the given graph.
	 *
	 * <p>
	 * An Hamiltonian path is a path that visits each vertex of the graph exactly once. This method returns an iterator
	 * that iterate over all Hamiltonian paths in the graph. Each returned path will start and end at two arbitrary
	 * vertices.
	 *
	 * <p>
	 * If the graph has no vertices, this methods returns an empty iterator. If the graph has only one vertex, this
	 * method returns an iterator that iterate over a single empty path (which is actually a cycle).
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned iterator will iterate over {@link IPath} objects.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @return     an iterator that iterate over all Hamiltonian paths in the graph
	 */
	<V, E> Iterator<Path<V, E>> hamiltonianPathsIter(Graph<V, E> g);

	/**
	 * Iterate over all Hamiltonian paths in the given graph that start and end at two given vertices.
	 *
	 * <p>
	 * An Hamiltonian path is a path that visits each vertex of the graph exactly once. This method returns an iterator
	 * that iterate over all Hamiltonian paths in the graph that start at {@code source} and end at {@code target}.
	 *
	 * <p>
	 * If the source and target are the same vertex, the returned iterator will iterate over all Hamiltonian cycles in
	 * the graph.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned iterator will iterate over {@link IPath} objects.
	 *
	 * @param  <V>                   the vertices type
	 * @param  <E>                   the edges type
	 * @param  g                     the graph
	 * @param  source                the source vertex
	 * @param  target                the target vertex
	 * @return                       an iterator that iterate over all Hamiltonian paths in the graph that start at
	 *                               {@code source} and end at {@code target}
	 * @throws NoSuchVertexException if {@code source} or {@code target} are not vertices in {@code g}
	 */
	<V, E> Iterator<Path<V, E>> hamiltonianPathsIter(Graph<V, E> g, V source, V target);

	/**
	 * Find a Hamiltonian cycle in the given graph.
	 *
	 * <p>
	 * An Hamiltonian cycle is a cycle that visits each vertex of the graph exactly once. This method returns an
	 * Hamiltonian cycle if one exists, or {@link Optional#empty()} otherwise.
	 *
	 * <p>
	 * If the graph has no vertices, this methods return {@link Optional#empty()}. If the graph has only one vertex,
	 * this method returns an empty cycle.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned cycle will an instance of {@link IPath}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @return     an Hamiltonian cycle if one exists, or {@link Optional#empty()} otherwise
	 */
	default <V, E> Optional<Path<V, E>> hamiltonianCycle(Graph<V, E> g) {
		Iterator<Path<V, E>> hamiltonianPaths = hamiltonianCyclesIter(g);
		return hamiltonianPaths.hasNext() ? Optional.of(hamiltonianPaths.next()) : Optional.empty();
	}

	/**
	 * Iterate over all Hamiltonian cycles in the given graph.
	 *
	 * <p>
	 * An Hamiltonian cycle is a cycle that visits each vertex of the graph exactly once. This method returns an
	 * iterator that iterate over all Hamiltonian cycles in the graph.
	 *
	 * <p>
	 * If the graph has no vertices, this methods returns an empty iterator. If the graph has only one vertex, this
	 * method returns an iterator that iterate over a single empty cycle.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned iterator will iterate over {@link IPath} objects.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @return     an iterator that iterate over all Hamiltonian cycles in the graph
	 */
	<V, E> Iterator<Path<V, E>> hamiltonianCyclesIter(Graph<V, E> g);

	/**
	 * Check whether the given path is a Hamiltonian path (or cycle) in the given graph.
	 *
	 * <p>
	 * Given a graph \(G = (V, E)\), a path is a sequence of edges \(e_1, e_2, \ldots, e_k\) such that for each pair of
	 * consecutive edges \(e_i, e_{i+1}\), the target of \(e_i\) is the source of \(e_{i+1}\). A path is
	 * <i>Hamiltonian</i> if it visits each vertex exactly once. An hamiltonian cycle is a path with \(|V|\) edges that
	 * visit every vertex exactly once, but return to the first vertex with the last edge to form a cycle.
	 *
	 * <p>
	 * This methods accept a graph and list of edges, and check whether the list of edges is a Hamiltonian path in the
	 * graph. It first validate that the list of edges is a valid path in the graph, and then check whether the path is
	 * Hamiltonian.
	 *
	 * @param  <V>  the vertices type
	 * @param  <E>  the edges type
	 * @param  g    the graph
	 * @param  path a list of edges in the graph
	 * @return      {@code true} if the list of edges is a Hamiltonian path (or cycle) in the graph, {@code false} if
	 *              the list of edges is not a valid path in the graph or if the path is not Hamiltonian
	 */
	static <V, E> boolean isHamiltonianPath(Graph<V, E> g, List<E> path) {
		IndexGraph ig;
		IntList ipath;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			@SuppressWarnings("unchecked")
			IntList ipath0 = IntAdapters.asIntList((List<Integer>) path);
			ipath = ipath0;
		} else {
			ig = g.indexGraph();
			ipath = IndexIdMaps.idToIndexList(path, g.indexGraphEdgesMap());
		}

		final int n = ig.vertices().size();
		if (n <= 1)
			return ipath.isEmpty();
		if (ipath.size() != n && ipath.size() != n - 1)
			return false;
		boolean cycle = ipath.size() == n;

		Bitmap visited = new Bitmap(n);
		if (ig.isDirected()) {
			int source = ig.edgeSource(ipath.getInt(0));
			visited.set(source);

			int v = source;
			for (IntIterator it = ipath.iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (v != ig.edgeSource(e))
					return false;
				v = ig.edgeTarget(e);
				if (!visited.set(v)) {
					if (it.hasNext() || !cycle || v != source)
						return false;
					break;
				}
			}
			assert visited.cardinality() == n;
			return true;

		} else {
			int firstEdge = ipath.getInt(0);
			srcLoop: for (int source : Fastutil.list(ig.edgeSource(firstEdge), ig.edgeTarget(firstEdge))) {
				visited.clear();
				visited.set(source);

				int v = source;
				for (IntIterator it = ipath.iterator(); it.hasNext();) {
					int e = it.nextInt();
					int eu = ig.edgeSource(e), ev = ig.edgeTarget(e);
					if (v == eu) {
						v = ev;
					} else if (v == ev) {
						v = eu;
					} else {
						continue srcLoop;
					}
					if (!visited.set(v)) {
						if (it.hasNext() || !cycle || v != source)
							continue srcLoop;
						break;
					}
				}
				assert visited.cardinality() == n;
				return true;
			}
			return false;
		}
	}

	/**
	 * Create a new Hamiltonian path algorithm.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link HamiltonianPathAlgo} object.
	 *
	 * @return a default implementation of {@link HamiltonianPathAlgo}
	 */
	static HamiltonianPathAlgo newInstance() {
		return new HamiltonianPathRubin();
	}

}
