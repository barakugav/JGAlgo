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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * An algorithm that enumerate all the closure subsets in a directed graph.
 *
 * <p>
 * Given a directed graph \(G = (V, E)\), a closure is a subset of vertices \(C \subseteq V\) such that no edges leave
 * \(C\) to \(V \setminus C\). There might be exponentially many closures in a graph, and algorithms implementing this
 * interface enumerate over all of them (use an {@link Iterator} avoiding storing all of them in memory).
 *
 * <p>
 * For undirected graphs, the closure subsets are simply the weakly connected components.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Closure_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface ClosuresEnumerator {

	/**
	 * Iterate over all closures in the given graph.
	 *
	 * <p>
	 * Given a directed graph \(G = (V, E)\), a closure is a subset of vertices \(C \subseteq V\) such that no edges
	 * leave \(C\) to \(V \setminus C\). Although the empty set of vertices is consider a colure, it is not returned by
	 * this method.
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
	 * Find all closures in the given graph.
	 *
	 * <p>
	 * Given a directed graph \(G = (V, E)\), a closure is a subset of vertices \(C \subseteq V\) such that no edges
	 * leave \(C\) to \(V \setminus C\). Although the empty set of vertices is consider a colure, it is not returned by
	 * this method.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned list will contain {@link IntSet} objects.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a directed graph
	 * @return                          a list of all closures in the graph
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	default <V, E> List<Set<V>> allClosures(Graph<V, E> g) {
		return new ObjectArrayList<>(closuresIter(g));
	}

	/**
	 * Create a new closure enumeration algorithm.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ClosuresEnumerator} object. The
	 * {@link ClosuresEnumerator.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ClosuresEnumerator}
	 */
	static ClosuresEnumerator newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new closure enumeration algo builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link ClosuresEnumerator} objects
	 */
	static ClosuresEnumerator.Builder newBuilder() {
		return ClosuresEnumeratorSchrageBaker::new;
	}

	/**
	 * A builder for {@link ClosuresEnumerator} objects.
	 *
	 * @see    ClosuresEnumerator#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for closure enumeration.
		 *
		 * @return a new closure enumeration algorithm
		 */
		ClosuresEnumerator build();
	}

	/**
	 * Check whether the given set is a closure in the given graph.
	 *
	 * <p>
	 * Given a directed graph \(G = (V, E)\), a closure is a subset of vertices \(C \subseteq V\) such that no edges
	 * leave \(C\) to \(V \setminus C\). The empty set of vertices is considered a closure.
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
