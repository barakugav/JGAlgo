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

import java.util.Iterator;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for computing all minimum vertex cuts between two vertices in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MinimumVertexCutAllStAbstract implements MinimumVertexCutAllSt {

	/**
	 * Default constructor.
	 */
	public MinimumVertexCutAllStAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Set<V>> minimumCutsIter(Graph<V, E> g, WeightFunction<V> w, V source, V sink) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			int source0 = ((Integer) source).intValue(), sink0 = ((Integer) sink).intValue();
			return (Iterator) minimumCutsIter((IndexGraph) g, w0, source0, sink0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, viMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);
			Iterator<IntSet> indexCuts = minimumCutsIter(iGraph, iw, iSource, iSink);
			return indexCuts == null ? null : IterTools.map(indexCuts, c -> IndexIdMaps.indexToIdSet(c, viMap));
		}
	}

	/**
	 * Iterate over all the minimum vertex-cuts in a graph between two terminal vertices.
	 *
	 * @see           #minimumCutsIter(Graph, WeightFunction, Object, Object)
	 * @param  g      a graph
	 * @param  w      a vertex weight function
	 * @param  source the source vertex
	 * @param  sink   the sink vertex
	 * @return        an iterator over the sets of vertices that form the minimum vertex-cuts, or {@code null} if an
	 *                edge exists between the source and the sink and therefore no vertex-cut exists
	 */
	protected abstract Iterator<IntSet> minimumCutsIter(IndexGraph g, IWeightFunction w, int source, int sink);

}
