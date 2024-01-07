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

import java.util.Collection;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntCollection;

interface MaximumFlowBase extends MaximumFlow, MinimumEdgeCutSTBase {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> Flow<V, E> computeMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity, V source, V sink) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			int source0 = ((Integer) source).intValue();
			int sink0 = ((Integer) sink).intValue();
			return (Flow<V, E>) computeMaximumFlow((IndexGraph) g, capacity0, source0, sink0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);
			IFlow indexFlow = computeMaximumFlow(iGraph, iCapacity, iSource, iSink);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> Flow<V, E> computeMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity, Collection<V> sources,
			Collection<V> sinks) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IntCollection sources0 = IntAdapters.asIntCollection((Collection<Integer>) sources);
			IntCollection sinks0 = IntAdapters.asIntCollection((Collection<Integer>) sinks);
			return (Flow<V, E>) computeMaximumFlow((IndexGraph) g, capacity0, sources0, sinks0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);
			IFlow indexFlow = computeMaximumFlow(iGraph, iCapacity, iSources, iSinks);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	abstract IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, int source, int sink);

	abstract IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, IntCollection sources,
			IntCollection sinks);

	@Override
	default IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, int source, int sink) {
		return MinimumEdgeCutUtils.computeMinimumCutUsingMaxFlow(g, w, source, sink, this);
	}

	@Override
	default IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, IntCollection sources,
			IntCollection sinks) {
		return MinimumEdgeCutUtils.computeMinimumCutUsingMaxFlow(g, w, sources, sinks, this);
	}

}
