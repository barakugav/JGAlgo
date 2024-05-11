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
import com.jgalgo.alg.IVertexBiPartition;
import com.jgalgo.alg.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IterTools;

/**
 * Abstract class for computing all minimum edge cuts between two terminal nodes.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MinimumEdgeCutAllStAbstract implements MinimumEdgeCutAllSt {

	/**
	 * Default constructor.
	 */
	public MinimumEdgeCutAllStAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<VertexBiPartition<V, E>> minimumCutsIter(Graph<V, E> g, WeightFunction<E> w, V source,
			V sink) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			int source0 = ((Integer) source).intValue();
			int sink0 = ((Integer) sink).intValue();
			return (Iterator) minimumCutsIter((IndexGraph) g, w0, source0, sink0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);
			Iterator<IVertexBiPartition> indexIter = minimumCutsIter(iGraph, iw, iSource, iSink);
			return IterTools.map(indexIter, iPartition -> VertexBiPartition.partitionFromIndexPartition(g, iPartition));
		}
	}

	/**
	 * Iterate over all the minimum edge-cuts in a graph between two terminal vertices.
	 *
	 * @see           #minimumCutsIter(Graph, WeightFunction, Object, Object)
	 * @param  g      a graph
	 * @param  w      an edge weight function
	 * @param  source the source vertex
	 * @param  sink   the sink vertex
	 * @return        an iterator over all the minimum edge-cuts
	 */
	protected abstract Iterator<IVertexBiPartition> minimumCutsIter(IndexGraph g, IWeightFunction w, int source,
			int sink);

}
