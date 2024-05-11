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
 * Abstract class for computing minimum cost flow in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MinimumCostFlowAbstract implements MinimumCostFlow {

	/**
	 * Default constructor.
	 */
	public MinimumCostFlowAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			V source, V sink) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
			int source0 = ((Integer) source).intValue(), sink0 = ((Integer) sink).intValue();
			return (Flow<V, E>) computeMinCostMaxFlow((IndexGraph) g, capacity0, cost0, source0, sink0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);
			IFlow indexFlow = computeMinCostMaxFlow(iGraph, iCapacity, iCost, iSource, iSink);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, V source, V sink) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
			IWeightFunction lowerBound0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) lowerBound);
			int source0 = ((Integer) source).intValue(), sink0 = ((Integer) sink).intValue();
			return (Flow<V, E>) computeMinCostMaxFlow((IndexGraph) g, capacity0, cost0, lowerBound0, source0, sink0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			IWeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);
			IFlow indexFlow = computeMinCostMaxFlow(iGraph, iCapacity, iCost, iLowerBound, iSource, iSink);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			Collection<V> sources, Collection<V> sinks) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
			IntCollection sources0 = IntAdapters.asIntCollection((Collection<Integer>) sources);
			IntCollection sinks0 = IntAdapters.asIntCollection((Collection<Integer>) sinks);
			return (Flow<V, E>) computeMinCostMaxFlow((IndexGraph) g, capacity0, cost0, sources0, sinks0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);
			IFlow indexFlow = computeMinCostMaxFlow(iGraph, iCapacity, iCost, iSources, iSinks);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, Collection<V> sources, Collection<V> sinks) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
			IWeightFunction lowerBound0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) lowerBound);
			IntCollection sources0 = IntAdapters.asIntCollection((Collection<Integer>) sources);
			IntCollection sinks0 = IntAdapters.asIntCollection((Collection<Integer>) sinks);
			return (Flow<V, E>) computeMinCostMaxFlow((IndexGraph) g, capacity0, cost0, lowerBound0, sources0, sinks0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			IWeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);
			IFlow indexFlow = computeMinCostMaxFlow(iGraph, iCapacity, iCost, iLowerBound, iSources, iSinks);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMinCostFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			WeightFunction<V> supply) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
			IWeightFunction supply0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) supply);
			return (Flow<V, E>) computeMinCostFlow((IndexGraph) g, capacity0, cost0, supply0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			IWeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);
			IFlow indexFlow = computeMinCostFlow(iGraph, iCapacity, iCost, iSupply);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMinCostFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, WeightFunction<V> supply) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
			IWeightFunction lowerBound0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) lowerBound);
			IWeightFunction supply0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) supply);
			return (Flow<V, E>) computeMinCostFlow((IndexGraph) g, capacity0, cost0, lowerBound0, supply0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			IWeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			IWeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);
			IFlow indexFlow = computeMinCostFlow(iGraph, iCapacity, iCost, iLowerBound, iSupply);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	protected abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			int source, int sink);

	protected abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			IWeightFunction lowerBound, int source, int sink);

	protected abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			IntCollection sources, IntCollection sinks);

	protected abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			IWeightFunction lowerBound, IntCollection sources, IntCollection sinks);

	protected abstract IFlow computeMinCostFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			IWeightFunction supply);

	protected abstract IFlow computeMinCostFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			IWeightFunction lowerBound, IWeightFunction supply);

	protected static IFlow newFlow(IndexGraph g, double[] flow) {
		return new Flows.IndexFlow(g, flow);
	}

}
