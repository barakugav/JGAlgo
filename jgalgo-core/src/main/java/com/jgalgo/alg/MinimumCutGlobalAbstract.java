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

import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IWeightFunction;

abstract class MinimumCutGlobalAbstract implements MinimumCutGlobal {

	@Override
	public VertexBiPartition computeMinimumCut(IntGraph g, IWeightFunction w) {
		if (g instanceof IndexGraph)
			return computeMinimumCut((IndexGraph) g, w);

		IndexGraph iGraph = g.indexGraph();
		IndexIntIdMap eiMap = g.indexGraphEdgesMap();
		IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

		VertexBiPartition indexCut = computeMinimumCut(iGraph, iw);
		return new VertexBiPartitions.BiPartitionFromIndexBiPartition(g, indexCut);
	}

	abstract VertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w);

}
