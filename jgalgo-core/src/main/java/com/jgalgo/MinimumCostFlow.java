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
 * objecting is to find the flow with the lowest cost out of all maximum flows. (2) given per-vertex finite demand, and
 * the objective is to find a minimum-cost flow satisfying the demand, namely that for each vertex the sum of flow units
 * going into the vertex minus the sum of flow units going out of it is equal to its demand.
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
	 * Compute the min-cost (not maximum!) flow in a network given a demand for each vertex.
	 *
	 * @param g      the graph
	 * @param net    the flow network. The result flow values will be set using this network
	 * @param cost   an edge weight function representing the cost of each unit of flow along the edge
	 * @param demand a vertex weight function representing the demand for each vertex
	 */
	void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction demand);

	/**
	 * Compute the min-cost (not maximum!) flow in a network given a demand for each vertex and a lower bound for the
	 * edges flows.
	 *
	 * @param g          the graph
	 * @param net        the flow network. The result flow values will be set using this network
	 * @param cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param demand     a vertex weight function representing the demand for each vertex
	 */
	void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
			WeightFunction demand);

	/**
	 * Create a new minimum cost flow algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumCostFlow} object.
	 *
	 * @return a new builder that can build {@link MinimumCostFlow} objects
	 */
	static MinimumCostFlow.Builder newBuilder() {
		return MinimumCostFlowCycleCanceling::new;
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
	}

}
