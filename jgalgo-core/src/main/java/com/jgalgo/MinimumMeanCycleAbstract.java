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

package com.jgalgo;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;

abstract class MinimumMeanCycleAbstract implements MinimumMeanCycle {

	@Override
	public Path computeMinimumMeanCycle(Graph g, WeightFunction w) {
		if (g instanceof IndexGraph)
			return computeMinimumMeanCycle((IndexGraph) g, w);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();
		w = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

		Path indexPath = computeMinimumMeanCycle(iGraph, w);
		return PathImpl.pathFromIndexPath(indexPath, viMap, eiMap);
	}

	abstract Path computeMinimumMeanCycle(IndexGraph g, WeightFunction w);

}
