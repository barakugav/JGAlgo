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
 * Algorithm for enumerating over all maximal independent sets in a graph.
 *
 * <p>
 * An independent set is a subset of vertices of an graph such that there are no edges between any pair of vertices in
 * the set. Self edges are allowed within an independent set, namely they are ignored. A maximal independent set is an
 * independent set that cannot be extended by including one more vertex.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * MaximalIndependentSetsEnumerator independentSetsAlgo = MaximalIndependentSetsEnumerator.newInstance();
 *
 * for (Iterator<Set<String>> it = independentSetsAlgo.maximalIndependentSetsIter(g); it.hasNext();) {
 *	Set<String> independentSet = it.next();
 *	System.out.println("Independent set in the graph:");
 *	for (String v : independentSet)
 *		System.out.println("\t" + v);
 * }
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface MaximalIndependentSetsEnumerator {

	/**
	 * Iterate over all maximal independent sets in a graph.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned iterator will be iterate over {@link IntSet}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     an iterator that iterates over all maximal independent sets in the graph
	 */
	<V, E> Iterator<Set<V>> maximalIndependentSetsIter(Graph<V, E> g);

	/**
	 * Create a new maximal independent sets algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MaximalIndependentSetsEnumerator} object.
	 *
	 * @return a default implementation of {@link MaximalIndependentSetsEnumerator}
	 */
	static MaximalIndependentSetsEnumerator newInstance() {
		return new MaximalIndependentSetsEnumeratorComplementCliques();
	}

}
