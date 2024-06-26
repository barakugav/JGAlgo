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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for enumerating over all the closure subsets in a directed graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class ClosuresEnumeratorAbstract implements ClosuresEnumerator {

	/**
	 * Default constructor.
	 */
	public ClosuresEnumeratorAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Set<V>> closuresIter(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (Iterator) closuresIter((IndexGraph) g);
		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			Iterator<IntSet> indexIter = closuresIter(ig);
			return IterTools.map(indexIter, s -> IndexIdMaps.indexToIdSet(s, viMap));
		}
	}

	/**
	 * Iterate over all closures in the given graph.
	 *
	 * @see      #closuresIter(Graph)
	 * @param  g a directed graph
	 * @return   an iterator that iteration over all closures in the graph
	 */
	protected abstract Iterator<IntSet> closuresIter(IndexGraph g);

}
