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
package com.jgalgo.alg.connect;

import static java.util.stream.Collectors.toList;
import java.util.List;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for k-vertex connected components algorithms.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class KVertexConnectedComponentsAlgoAbstract implements KVertexConnectedComponentsAlgo {

	/**
	 * Default constructor.
	 */
	public KVertexConnectedComponentsAlgoAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> List<Set<V>> findKVertexConnectedComponents(Graph<V, E> g, int k) {
		if (g instanceof IndexGraph) {
			return (List) findKVertexConnectedComponents((IndexGraph) g, k);
		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			List<IntSet> indexRes = findKVertexConnectedComponents(ig, k);
			return indexRes.stream().map(cc -> IndexIdMaps.indexToIdSet(cc, viMap)).collect(toList());
		}
	}

	/**
	 * Find all k-vertex connected components in a graph.
	 *
	 * @see      #findKVertexConnectedComponents(Graph, int)
	 * @param  g a graph
	 * @param  k the k value, non negative
	 * @return   a list of the k-connected components
	 */
	protected abstract List<IntSet> findKVertexConnectedComponents(IndexGraph g, int k);

}
