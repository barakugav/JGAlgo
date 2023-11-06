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

class FlowCirculations {

	abstract static class AbstractImpl implements FlowCirculation {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> void computeCirculation(Graph<V, E> g, FlowNetwork<V, E> net, WeightFunction<V> supply) {
			if (g instanceof IndexGraph && net instanceof IFlowNetwork) {
				IWeightFunction supply0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) supply);
				computeCirculation((IndexGraph) g, (IFlowNetwork) net, supply0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IFlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
				IWeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);

				computeCirculation(iGraph, iNet, iSupply);
			}
		}

		abstract void computeCirculation(IndexGraph g, IFlowNetwork net, IWeightFunction supply);

	}

}
