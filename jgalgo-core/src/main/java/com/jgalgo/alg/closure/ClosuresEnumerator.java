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
package com.jgalgo.alg.closure;

import java.util.Iterator;
import java.util.Set;
import com.jgalgo.alg.path.IPath;
import com.jgalgo.alg.path.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * An algorithm that enumerate all the closure subsets in a directed graph.
 *
 * <p>
 * Given a directed graph \(G = (V, E)\), a closure is a subset of vertices \(C \subseteq V\) such that there are no
 * edges from \(C\) to \(V \setminus C\).
 *
 * <p>
 * There may be exponentially many closures in a graph, therefore all implementations of this interface use some
 * heuristic to speed up the process but run in exponential time in the worst case. The algorithm returns an iterator
 * over the closures, so that the caller can iterate over them without storing them all in memory. Avoid using this
 * algorithm on very large graphs.
 *
 * <p>
 * For undirected graphs, the closure subsets are simply the weakly connected components, and algorithms implementing
 * this interface will throw an exception if the graph is not directed.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Closure_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface ClosuresEnumerator {

	/**
	 * Iterate over all closures in the given graph.
	 *
	 * <p>
	 * Given a directed graph \(G = (V, E)\), a closure is a subset of vertices \(C \subseteq V\) such that there are no
	 * edges from \(C\) to \(V \setminus C\). Although the empty set of vertices is considered a colure, it is not
	 * returned by this method.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned iterator will iterate over {@link IntSet} objects.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a directed graph
	 * @return                          an iterator that iteration over all closures in the graph
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	<V, E> Iterator<Set<V>> closuresIter(Graph<V, E> g);

	/**
	 * Create a new closure enumeration algorithm.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ClosuresEnumerator} object.
	 *
	 * @return a default implementation of {@link ClosuresEnumerator}
	 */
	static ClosuresEnumerator newInstance() {
		return new ClosuresEnumeratorSchrageBaker();
	}

	/**
	 * Check whether the given set of vertices is a closure in the given graph.
	 *
	 * <p>
	 * Given a directed graph \(G = (V, E)\), a closure is a subset of vertices \(C \subseteq V\) such that there are no
	 * edges from \(C\) to \(V \setminus C\). The empty set of vertices is considered a closure.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @param  c   a set of vertices
	 * @return     {@code true} if the set is a closure, {@code false} otherwise
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isClosure(Graph<V, E> g, Set<V> c) {
		if (g instanceof IntGraph) {
			IntSet c0 = IntAdapters.asIntSet((Set<Integer>) c);
			for (int w : IPath.reachableVertices((IntGraph) g, c0.iterator()))
				if (!(c0.contains(w)))
					return false;
		} else {
			for (V w : Path.reachableVertices(g, c.iterator()))
				if (!(c.contains(w)))
					return false;
		}
		return true;
	}

}
