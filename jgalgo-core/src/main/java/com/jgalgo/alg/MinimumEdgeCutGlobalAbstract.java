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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

abstract class MinimumEdgeCutGlobalAbstract implements MinimumEdgeCutGlobal {

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
			return VertexBiPartitions.partitionFromIndexPartition(g, indexCut);
		}
	}

	abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w);

}