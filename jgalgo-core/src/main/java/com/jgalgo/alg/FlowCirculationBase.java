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

interface FlowCirculationBase extends FlowCirculation {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> Flow<V, E> computeCirculation(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<V> supply) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IWeightFunction supply0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) supply);
			return (Flow<V, E>) computeCirculation((IndexGraph) g, capacity0, supply0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IWeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);
			IFlow indexFlow = computeCirculation(iGraph, iCapacity, iSupply);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	IFlow computeCirculation(IndexGraph g, IWeightFunction capacity, IWeightFunction supply);

}
