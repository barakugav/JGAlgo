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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for enumerating over all maximal cliques in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MaximalCliquesEnumeratorAbstract implements MaximalCliquesEnumerator {

	/**
	 * Default constructor.
	 */
	public MaximalCliquesEnumeratorAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Set<V>> maximalCliquesIter(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (Iterator) maximalCliquesIter((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			Iterator<IntSet> indexResult = maximalCliquesIter(iGraph);
			return IterTools.map(indexResult, iSet -> IndexIdMaps.indexToIdSet(iSet, viMap));
		}
	}

	/**
	 * Iterate over all maximal cliques in a graph.
	 *
	 * @see      #maximalCliquesIter(Graph)
	 * @param  g a graph
	 * @return   an iterator that iterates over all maximal cliques in the graph
	 */
	protected abstract Iterator<IntSet> maximalCliquesIter(IndexGraph g);

}
