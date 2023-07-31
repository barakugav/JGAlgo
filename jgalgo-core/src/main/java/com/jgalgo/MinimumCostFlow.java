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
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.BuilderAbstract;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Compute the minimum-cost (max) flow in a flow network.
 * <p>
 * There are a few variations of the minimum-cost flow problem: (1) given source(s) and sink(s) terminal nodes, and the
 * objecting is to find the flow with the lowest cost out of all maximum flows. (2) given per-vertex finite supply, and
 * the objective is to find a minimum-cost flow satisfying the supply, namely that for each vertex the sum of flow units
 * going out of the vertex minus the sum of flow units going into it is equal to its supply.
 * <p>
 * In addition to these variants, a lower bound for each edge flow can be specified, similar to the capacities which can
 * be viewed as upper bounds.
 *
 * @see    MaximumFlow
 * @see    FlowNetwork
 * @see    <a href= "https://en.wikipedia.org/wiki/Minimum-cost_flow_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MinimumCostFlow {

	/**
	 * Compute the min-cost max-flow in a network between a source and a sink.
	 *
	 * @param g      the graph
	 * @param net    the flow network. The result flow values will be set using this network
	 * @param cost   an edge weight function representing the cost of each unit of flow along the edge
	 * @param source a source vertex
	 * @param sink   a sink vertex
	 */
	void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, int source, int sink);

	/**
	 * Compute the min-cost max-flow in a network between a source and a sink given a lower bound for the edges flows.
	 *
	 * @param g          the graph
	 * @param net        the flow network. The result flow values will be set using this network
	 * @param cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param source     a source vertex
	 * @param sink       a sink vertex
	 */
	void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound, int source,
			int sink);

	/**
	 * Compute the min-cost max-flow in a network between a set of sources and a set of sinks.
	 *
	 * @param g       the graph
	 * @param net     the flow network. The result flow values will be set using this network
	 * @param cost    an edge weight function representing the cost of each unit of flow along the edge
	 * @param sources a set of source vertices
	 * @param sinks   a set of sinks vertices
	 */
	void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, IntCollection sources,
			IntCollection sinks);

	/**
	 * Compute the min-cost max-flow in a network between a set of sources and a set of sinks given a lower bound for
	 * the edges flows.
	 *
	 * @param g          the graph
	 * @param net        the flow network. The result flow values will be set using this network
	 * @param cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param sources    a set of source vertices
	 * @param sinks      a set of sinks vertices
	 */
	void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
			IntCollection sources, IntCollection sinks);

	/**
	 * Compute the min-cost (not maximum!) flow in a network given a supply for each vertex.
	 * <p>
	 * The supply is a scalar for each vertex, and the objective is to find a minimum-cost flow satisfying the supply,
	 * namely that for each vertex the sum of flow units going out of the vertex minus the sum of flow units going into
	 * it is equal to its supply.
	 *
	 * @param g      the graph
	 * @param net    the flow network. The result flow values will be set using this network
	 * @param cost   an edge weight function representing the cost of each unit of flow along the edge
	 * @param supply a vertex weight function representing the supply for each vertex
	 */
	void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction supply);

	/**
	 * Compute the min-cost (not maximum!) flow in a network given a supply for each vertex and a lower bound for the
	 * edges flows.
	 * <p>
	 * The supply is a scalar for each vertex, and the objective is to find a minimum-cost flow satisfying the supply,
	 * namely that for each vertex the sum of flow units going out of the vertex minus the sum of flow units going into
	 * it is equal to its supply.
	 *
	 * @param g          the graph
	 * @param net        the flow network. The result flow values will be set using this network
	 * @param cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param supply     a vertex weight function representing the supply for each vertex
	 */
	void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
			WeightFunction supply);

	/**
	 * Create a new minimum cost flow algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumCostFlow} object.
	 *
	 * @return a new builder that can build {@link MinimumCostFlow} objects
	 */
	static MinimumCostFlow.Builder newBuilder() {
		return new MinimumCostFlow.Builder() {
			String impl;
			boolean integerNetwork;
			boolean integerCosts;

			@Override
			public MinimumCostFlow build() {
				if (impl != null) {
					switch (impl) {
						case "cycle-canceling":
							return new MinimumCostFlowCycleCanceling();
						case "cost-scaling":
							return new MinimumCostFlowCostScaling();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				if (integerNetwork && integerCosts) {
					return new MinimumCostFlowCostScaling();
				} else {
					return new MinimumCostFlow() {

						private final MinimumCostFlow integerAlgo = new MinimumCostFlowCostScaling();
						private final MinimumCostFlow floatsAlgo = new MinimumCostFlowCycleCanceling();

						@Override
						public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, int source,
								int sink) {
							if (net instanceof FlowNetwork.Int && cost instanceof WeightFunction.Int) {
								integerAlgo.computeMinCostMaxFlow(g, net, cost, source, sink);
							} else {
								floatsAlgo.computeMinCostMaxFlow(g, net, cost, source, sink);
							}
						}

						@Override
						public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost,
								WeightFunction lowerBound, int source, int sink) {
							if (net instanceof FlowNetwork.Int && cost instanceof WeightFunction.Int
									&& lowerBound instanceof WeightFunction.Int) {
								integerAlgo.computeMinCostMaxFlow(g, net, cost, lowerBound, source, sink);
							} else {
								floatsAlgo.computeMinCostMaxFlow(g, net, cost, lowerBound, source, sink);
							}
						}

						@Override
						public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost,
								IntCollection sources, IntCollection sinks) {
							if (net instanceof FlowNetwork.Int && cost instanceof WeightFunction.Int) {
								integerAlgo.computeMinCostMaxFlow(g, net, cost, sources, sinks);
							} else {
								floatsAlgo.computeMinCostMaxFlow(g, net, cost, sources, sinks);
							}
						}

						@Override
						public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost,
								WeightFunction lowerBound, IntCollection sources, IntCollection sinks) {
							if (net instanceof FlowNetwork.Int && cost instanceof WeightFunction.Int
									&& lowerBound instanceof WeightFunction.Int) {
								integerAlgo.computeMinCostMaxFlow(g, net, cost, lowerBound, sources, sinks);
							} else {
								floatsAlgo.computeMinCostMaxFlow(g, net, cost, lowerBound, sources, sinks);
							}
						}

						@Override
						public void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost,
								WeightFunction supply) {
							if (net instanceof FlowNetwork.Int && cost instanceof WeightFunction.Int
									&& supply instanceof WeightFunction.Int) {
								integerAlgo.computeMinCostFlow(g, net, cost, supply);
							} else {
								floatsAlgo.computeMinCostFlow(g, net, cost, supply);
							}
						}

						@Override
						public void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost,
								WeightFunction lowerBound, WeightFunction supply) {
							if (net instanceof FlowNetwork.Int && cost instanceof WeightFunction.Int
									&& lowerBound instanceof WeightFunction.Int
									&& supply instanceof WeightFunction.Int) {
								integerAlgo.computeMinCostFlow(g, net, cost, lowerBound, supply);
							} else {
								floatsAlgo.computeMinCostFlow(g, net, cost, lowerBound, supply);
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

			@Override
			public MinimumCostFlow.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MinimumCostFlow} objects.
	 *
	 * @see    MinimumCostFlow#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<MinimumCostFlow.Builder> {

		/**
		 * Create a new algorithm object for minimum cost flow computation.
		 *
		 * @return a new minimum cost flow algorithm
		 */
		MinimumCostFlow build();

		/**
		 * Enable/disable integer network (capacities, flows, vertices supplies and edges flow lower bound).
		 * <p>
		 * More efficient and accurate implementations may be supported if the network is known to be integral. If the
		 * option is enabled, non-integer networks will not be supported by the built algorithms.
		 * <p>
		 * The default value of this option is {@code false}.
		 *
		 * @param  enable if {@code true}, algorithms built by this builder will support integer networks only
		 * @return        this builder
		 */
		MinimumCostFlow.Builder integerNetwork(boolean enable);

		/**
		 * Enable/disable integer costs.
		 * <p>
		 * More efficient and accurate implementations may be supported if the cost function is known to be integral. If
		 * the option is enabled, non-integer cost functions will not be supported by the built algorithms.
		 * <p>
		 * The default value of this option is {@code false}.
		 *
		 * @param  enable if {@code true}, algorithms built by this builder will support integer cost functions only
		 * @return        this builder
		 */
		MinimumCostFlow.Builder integerCosts(boolean enable);
	}

}
