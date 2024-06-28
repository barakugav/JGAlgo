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
import com.jgalgo.alg.common.AlgorithmBuilderBase;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Compute the minimum-cost (max) flow in a flow network.
 *
 * <p>
 * There are a few variations of the minimum-cost flow problem: (1) given source(s) and sink(s) terminal vertices, and
 * the objecting is to find the flow with the lowest cost out of all maximum flows. (2) given per-vertex finite supply,
 * and the objective is to find a minimum-cost flow satisfying the supply, namely that for each vertex the sum of flow
 * units going out of the vertex minus the sum of flow units going into it is equal to its supply.
 *
 * <p>
 * In addition to these variants, a lower bound for each edge flow can be specified, similar to the capacities which can
 * be viewed as upper bounds.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
 *
 * @see    MaximumFlow
 * @see    Flow
 * @see    <a href= "https://en.wikipedia.org/wiki/Minimum-cost_flow_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MinimumCostFlow {

	/**
	 * Compute the min-cost max-flow in a network between a source and a sink.
	 *
	 * <p>
	 * Some algorithm might run faster for integer networks, and {@link WeightFunctionInt} can be passed as argument as
	 * {@code capacity} and {@code cost}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code capacity} and
	 * {@code cost} to avoid boxing/unboxing. If {@code g} is an {@link IntGraph}, the returned object is {@link IFlow}.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  g        the graph
	 * @param  capacity a capacity edge weight function
	 * @param  cost     an edge weight function representing the cost of each unit of flow along the edge
	 * @param  source   a source vertex
	 * @param  sink     a sink vertex
	 * @return          the flows computed for each edge
	 */
	<V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost, V source,
			V sink);

	/**
	 * Compute the min-cost max-flow in a network between a source and a sink given a lower bound for the edges flows.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code capacity},
	 * {@code cost} and {@code lowerBound} to avoid boxing/unboxing. If {@code g} is an {@link IntGraph}, the returned
	 * object is {@link IFlow}.
	 *
	 * @param  <V>        the vertices type
	 * @param  <E>        the edges type
	 * @param  g          the graph
	 * @param  capacity   a capacity edge weight function
	 * @param  cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param  lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param  source     a source vertex
	 * @param  sink       a sink vertex
	 * @return            the flows computed for each edge
	 */
	<V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, V source, V sink);

	/**
	 * Compute the min-cost max-flow in a network between a set of sources and a set of sinks.
	 *
	 * <p>
	 * Some algorithm might run faster for integer networks, and {@link WeightFunctionInt} can be passed as argument as
	 * {@code capacity} and {@code cost}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code capacity} and
	 * {@code cost}, and {@link IntCollection} as {@code sources} and {@code sinks} to avoid boxing/unboxing. If
	 * {@code g} is an {@link IntGraph}, the returned object is {@link IFlow}.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  g        the graph
	 * @param  capacity a capacity edge weight function
	 * @param  cost     an edge weight function representing the cost of each unit of flow along the edge
	 * @param  sources  a set of source vertices
	 * @param  sinks    a set of sinks vertices
	 * @return          the flows computed for each edge
	 */
	<V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			Collection<V> sources, Collection<V> sinks);

	/**
	 * Compute the min-cost max-flow in a network between a set of sources and a set of sinks given a lower bound for
	 * the edges flows.
	 *
	 * <p>
	 * Some algorithm might run faster for integer networks, and {@link WeightFunctionInt} can be passed as argument as
	 * {@code capacity}, {@code cost} and {@code lowerBound}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code capacity},
	 * {@code cost} and {@code lowerBound}, and {@link IntCollection} as {@code sources} and {@code sinks} to avoid
	 * boxing/unboxing. If {@code g} is an {@link IntGraph}, the returned object is {@link IFlow}.
	 *
	 * @param  <V>        the vertices type
	 * @param  <E>        the edges type
	 * @param  g          the graph
	 * @param  capacity   a capacity edge weight function
	 * @param  cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param  lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param  sources    a set of source vertices
	 * @param  sinks      a set of sinks vertices
	 * @return            the flows computed for each edge
	 */
	<V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, Collection<V> sources, Collection<V> sinks);

	/**
	 * Compute the min-cost (not maximum!) flow in a network given a supply for each vertex.
	 *
	 * <p>
	 * The supply is a scalar for each vertex, and the objective is to find a minimum-cost flow satisfying the supply,
	 * namely that for each vertex the sum of flow units going out of the vertex minus the sum of flow units going into
	 * it is equal to its supply.
	 *
	 * <p>
	 * Some algorithm might run faster for integer networks, and {@link WeightFunctionInt} can be passed as argument as
	 * {@code capacity}, {@code cost} and {@code supply}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code capacity},
	 * {@code cost} and {@code supply} to avoid boxing/unboxing. If {@code g} is an {@link IntGraph}, the returned
	 * object is {@link IFlow}.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  g        the graph
	 * @param  capacity a capacity edge weight function
	 * @param  cost     an edge weight function representing the cost of each unit of flow along the edge
	 * @param  supply   a vertex weight function representing the supply for each vertex
	 * @return          the flows computed for each edge
	 */
	<V, E> Flow<V, E> computeMinCostFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			WeightFunction<V> supply);

	/**
	 * Compute the min-cost (not maximum!) flow in a network given a supply for each vertex and a lower bound for the
	 * edges flows.
	 *
	 * <p>
	 * The supply is a scalar for each vertex, and the objective is to find a minimum-cost flow satisfying the supply,
	 * namely that for each vertex the sum of flow units going out of the vertex minus the sum of flow units going into
	 * it is equal to its supply.
	 *
	 * <p>
	 * Some algorithm might run faster for integer networks, and {@link WeightFunctionInt} can be passed as argument as
	 * {@code capacity}, {@code cost}, {@code lowerBound} and {@code supply}.
	 *
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code capacity},
	 * {@code cost}, {@code lowerBound} and {@code supply} to avoid boxing/unboxing. If {@code g} is an
	 * {@link IntGraph}, the returned object is {@link IFlow}.
	 *
	 * @param  <V>        the vertices type
	 * @param  <E>        the edges type
	 * @param  g          the graph
	 * @param  capacity   a capacity edge weight function
	 * @param  cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param  lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param  supply     a vertex weight function representing the supply for each vertex
	 * @return            the flows computed for each edge
	 */
	<V, E> Flow<V, E> computeMinCostFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, WeightFunction<V> supply);

	/**
	 * Create a new min-cost-flow algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumCostFlow} object. The
	 * {@link MinimumCostFlow.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MinimumCostFlow}
	 */
	static MinimumCostFlow newInstance() {
		return builder().build();
	}

	/**
	 * Create a new minimum cost flow algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MinimumCostFlow} objects
	 */
	static MinimumCostFlow.Builder builder() {
		return new MinimumCostFlow.Builder() {
			boolean integerNetwork;
			boolean integerCosts;

			@Override
			public MinimumCostFlow build() {
				if (integerNetwork && integerCosts) {
					return new MinimumCostFlowCostScaling();
				} else {
					return new MinimumCostFlow() {

						private final MinimumCostFlow integerAlgo = new MinimumCostFlowCostScaling();
						private final MinimumCostFlow floatsAlgo = new MinimumCostFlowCycleCanceling();

						@Override
						public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity,
								WeightFunction<E> cost, V source, V sink) {
							if (WeightFunction.isInteger(capacity) && WeightFunction.isInteger(cost)) {
								return integerAlgo.computeMinCostMaxFlow(g, capacity, cost, source, sink);
							} else {
								return floatsAlgo.computeMinCostMaxFlow(g, capacity, cost, source, sink);
							}
						}

						@Override
						public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity,
								WeightFunction<E> cost, WeightFunction<E> lowerBound, V source, V sink) {
							if (WeightFunction.isInteger(capacity) && WeightFunction.isInteger(cost)
									&& WeightFunction.isInteger(lowerBound)) {
								return integerAlgo.computeMinCostMaxFlow(g, capacity, cost, lowerBound, source, sink);
							} else {
								return floatsAlgo.computeMinCostMaxFlow(g, capacity, cost, lowerBound, source, sink);
							}
						}

						@Override
						public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity,
								WeightFunction<E> cost, Collection<V> sources, Collection<V> sinks) {
							if (WeightFunction.isInteger(capacity) && WeightFunction.isInteger(cost)) {
								return integerAlgo.computeMinCostMaxFlow(g, capacity, cost, sources, sinks);
							} else {
								return floatsAlgo.computeMinCostMaxFlow(g, capacity, cost, sources, sinks);
							}
						}

						@Override
						public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity,
								WeightFunction<E> cost, WeightFunction<E> lowerBound, Collection<V> sources,
								Collection<V> sinks) {
							if (WeightFunction.isInteger(capacity) && WeightFunction.isInteger(cost)
									&& WeightFunction.isInteger(lowerBound)) {
								return integerAlgo.computeMinCostMaxFlow(g, capacity, cost, lowerBound, sources, sinks);
							} else {
								return floatsAlgo.computeMinCostMaxFlow(g, capacity, cost, lowerBound, sources, sinks);
							}
						}

						@Override
						public <V, E> Flow<V, E> computeMinCostFlow(Graph<V, E> g, WeightFunction<E> capacity,
								WeightFunction<E> cost, WeightFunction<V> supply) {
							if (WeightFunction.isInteger(capacity) && WeightFunction.isInteger(cost)
									&& WeightFunction.isInteger(supply)) {
								return integerAlgo.computeMinCostFlow(g, capacity, cost, supply);
							} else {
								return floatsAlgo.computeMinCostFlow(g, capacity, cost, supply);
							}
						}

						@Override
						public <V, E> Flow<V, E> computeMinCostFlow(Graph<V, E> g, WeightFunction<E> capacity,
								WeightFunction<E> cost, WeightFunction<E> lowerBound, WeightFunction<V> supply) {
							if (WeightFunction.isInteger(capacity) && WeightFunction.isInteger(cost)
									&& WeightFunction.isInteger(lowerBound) && WeightFunction.isInteger(supply)) {
								return integerAlgo.computeMinCostFlow(g, capacity, cost, lowerBound, supply);
							} else {
								return floatsAlgo.computeMinCostFlow(g, capacity, cost, lowerBound, supply);
							}
						}

					};
				}
			}

			@Override
			public MinimumCostFlow.Builder integerNetwork(boolean enable) {
				integerNetwork = enable;
				return this;
			}

			@Override
			public MinimumCostFlow.Builder integerCosts(boolean enable) {
				integerCosts = enable;
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MinimumCostFlow} objects.
	 *
	 * @see    MinimumCostFlow#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for minimum cost flow computation.
		 *
		 * @return a new minimum cost flow algorithm
		 */
		MinimumCostFlow build();

		/**
		 * Enable/disable integer network (capacities, flows, vertices supplies and edges flow lower bound).
		 *
		 * <p>
		 * More efficient and accurate implementations may be supported if the network is known to be integral. If the
		 * option is enabled, non-integer networks will not be supported by the built algorithms.
		 *
		 * <p>
		 * The default value of this option is {@code false}.
		 *
		 * @param  enable if {@code true}, algorithms built by this builder will support integer networks only
		 * @return        this builder
		 */
		MinimumCostFlow.Builder integerNetwork(boolean enable);

		/**
		 * Enable/disable integer costs.
		 *
		 * <p>
		 * More efficient and accurate implementations may be supported if the cost function is known to be integral. If
		 * the option is enabled, non-integer cost functions will not be supported by the built algorithms.
		 *
		 * <p>
		 * The default value of this option is {@code false}.
		 *
		 * @param  enable if {@code true}, algorithms built by this builder will support integer cost functions only
		 * @return        this builder
		 */
		MinimumCostFlow.Builder integerCosts(boolean enable);
	}

}
