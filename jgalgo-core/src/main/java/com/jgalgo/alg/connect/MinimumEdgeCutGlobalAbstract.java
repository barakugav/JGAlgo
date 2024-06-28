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

import com.jgalgo.alg.common.IVertexBiPartition;
import com.jgalgo.alg.common.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

/**
 * Abstract class for computing the global minimum edge cut in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MinimumEdgeCutGlobalAbstract implements MinimumEdgeCutGlobal {

	/**
	 * Default constructor.
	 */
	public MinimumEdgeCutGlobalAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> VertexBiPartition<V, E> computeMinimumCut(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			return (VertexBiPartition<V, E>) computeMinimumCut((IndexGraph) g,
					WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

			IVertexBiPartition indexCut = computeMinimumCut(iGraph, iw);
			return VertexBiPartition.partitionFromIndexPartition(g, indexCut);
		}
	}

	/**
	 * Compute the global minimum edge-cut in a graph.
	 *
	 * @see      #computeMinimumCut(Graph, WeightFunction)
	 * @param  g a graph
	 * @param  w an edge weight function
	 * @return   the cut that was computed
	 */
	protected abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w);

}
