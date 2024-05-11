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
package com.jgalgo.alg.clique;

import java.util.Iterator;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Algorithm for enumerating over all maximal cliques in a graph.
 *
 * <p>
 * A clique is a subset of vertices of an undirected graph such that every two distinct vertices in the clique are
 * adjacent (connected by an edge). A maximal clique is a clique that cannot be extended by including one more adjacent
 * vertex.
 *
 * <p>
 * There may be exponentially many maximal cliques in a graph, therefore all implementations of this interface use some
 * heuristic to speed up the process but run in exponential time in the worst case. The algorithm returns an iterator
 * over the cliques, so that the caller can iterate over them without storing them all in memory. Avoid using this
 * algorithm on very large graphs.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * MaximalCliquesEnumerator maxCliquesAlgo = MaximalCliquesEnumerator.newInstance();
 *
 * for (Iterator<Set<String>> it = maxCliquesAlgo.maximalCliquesIter(g); it.hasNext();) {
 *	Set<String> clique = it.next();
 *	System.out.println("Maximal clique in the graph:");
 *	for (String v : clique)
 *		System.out.println("\t" + v);
 * }
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface MaximalCliquesEnumerator {

	/**
	 * Iterate over all maximal cliques in a graph.
	 *
	 * <p>
	 * The input graph should not be changed during the iteration.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned iterator will be iterate over {@link IntSet}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     an iterator that iterates over all maximal cliques in the graph
	 */
	<V, E> Iterator<Set<V>> maximalCliquesIter(Graph<V, E> g);

	/**
	 * Create a new maximal cliques algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MaximalCliquesEnumerator} object.
	 *
	 * @return a default implementation of {@link MaximalCliquesEnumerator}
	 */
	static MaximalCliquesEnumerator newInstance() {
		return new MaximalCliquesEnumeratorBronKerbosch();
	}

}
