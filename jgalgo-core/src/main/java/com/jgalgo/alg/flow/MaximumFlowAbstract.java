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
package com.jgalgo.alg.flow;

import java.util.Collection;
import com.jgalgo.alg.IVertexBiPartition;
import com.jgalgo.alg.connect.MinimumEdgeCutSt2;
import com.jgalgo.alg.connect.MinimumEdgeCutSt2Abstract;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Abstract class for computing a maximum flow in a graph.
 *
 * <p>
 * This abstract class also implements the {@link MinimumEdgeCutSt2} interface, and thus can be used to compute the
 * minimum edge cut of a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MaximumFlowAbstract extends MinimumEdgeCutSt2Abstract implements MaximumFlow {

	/**
	 * Default constructor.
	 */
	public MaximumFlowAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity, V source, V sink) {
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
	public <V, E> Flow<V, E> computeMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity, Collection<V> sources,
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

	protected abstract IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, int source, int sink);

	protected abstract IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, IntCollection sources,
			IntCollection sinks);

	@Override
	protected IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, int source, int sink) {
		return (IVertexBiPartition) MinimumEdgeCutSt2
				.newFromMaximumFlow(this)
				.computeMinimumCut(g, w, Integer.valueOf(source), Integer.valueOf(sink));
	}

	@Override
	protected IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, IntCollection sources,
			IntCollection sinks) {
		return (IVertexBiPartition) MinimumEdgeCutSt2.newFromMaximumFlow(this).computeMinimumCut(g, w, sources, sinks);
	}

	protected static IFlow newFlow(IndexGraph g, double[] flow) {
		return new Flows.IndexFlow(g, flow);
	}

}
