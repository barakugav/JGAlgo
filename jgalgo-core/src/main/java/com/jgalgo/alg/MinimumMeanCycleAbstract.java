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
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

abstract class MinimumMeanCycleAbstract implements MinimumMeanCycle {

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Path<V, E> computeMinimumMeanCycle(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (Path<V, E>) computeMinimumMeanCycle((IndexGraph) g, w0);

		} else if (g instanceof IntGraph) {
			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
			IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
			IPath indexPath = computeMinimumMeanCycle(iGraph, iw);
			return (Path<V, E>) PathImpl.intPathFromIndexPath(indexPath, viMap, eiMap);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IPath indexPath = computeMinimumMeanCycle(iGraph, iw);
			return PathImpl.objPathFromIndexPath(indexPath, viMap, eiMap);
		}
	}

	abstract IPath computeMinimumMeanCycle(IndexGraph g, IWeightFunction w);

}
