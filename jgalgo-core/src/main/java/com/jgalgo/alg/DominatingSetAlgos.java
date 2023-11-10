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

import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import it.unimi.dsi.fastutil.ints.IntSet;

class DominatingSetAlgos {

	abstract static class AbstractImpl implements DominatingSetAlgo {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Set<V> computeMinimumDominationSet(Graph<V, E> g, WeightFunction<V> w,
				EdgeDirection dominanceDirection) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (Set<V>) computeMinimumDominationSet((IndexGraph) g, w0, dominanceDirection);
			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IWeightFunction w0 = IndexIdMaps.idToIndexWeightFunc(w, viMap);
				IntSet indexRes = computeMinimumDominationSet(iGraph, w0, dominanceDirection);
				return IndexIdMaps.indexToIdSet(indexRes, viMap);
			}
		}

		abstract IntSet computeMinimumDominationSet(IndexGraph g, IWeightFunction w, EdgeDirection dominanceDirection);

	}

}
