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
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;

/**
 * Flow on graph edges.
 *
 * <p>
 * A flow network on graph edges is defined as two functions: the capacity function \(C:E \rightarrow R\) and flow
 * function \(F:E \rightarrow R\). The capacity function define how many units of flow an edge can transfer from its
 * source to its target. The flow function define the number of units of flow that are currently transferred along each
 * edge. The capacity of any edge must be non negative, and the edge's flow must be smaller or equal to its capacity.
 *
 * <p>
 * Problems formulated using flow networks involve a source and a sink vertices. The source is a vertex from which the
 * flow is originated, and every flow going along its edges must reach the sink vertex using the edges of the graphs
 * while not violating the capacities of the network. For each vertex except the source and sink the sum of flow units
 * going along {@link Graph#inEdges(Object)} must be equal to the sum of flow units going along
 * {@link Graph#outEdges(Object)}. This sum is also called the supply of the vertex. The supply of the source vertex
 * must be equal to the negative of the supply of the sink vertex. The negative of the supply is sometimes called the
 * demand.
 *
 * <p>
 * A flow is most intuitively defined on directed graphs, as the flow on an edge is transferred from one vertex to
 * another in some direction, but we can define and solve flow problem on undirected graphs as well. Technically, the
 * flows values returned by {@link #getFlow(Object)} can either be positive or negative for undirected edges, with
 * values absolutely smaller than the capacity of the edge. A positive flow \(+f\) value assigned to edge {@code e}
 * means a flow directed from {@code edgeSource(e)} to {@code edgeTarget(e)} with \(f\) units of flow. A negative flow
 * \(-f\) value assigned to edge {@code e} means a flow directed from {@code edgeTarget(e)} to {@code edgeSource(e)}
 * (opposite direction) with \(|-f|\) units of flow (see {@link #getFlow(Object)}).
 *
 * <p>
 * Some algorithm might be more efficient when the capacities, and usually accept {@link WeightFunction} as capacities
 * input. In that case, its better to pass {@link WeightFunctionInt} instead.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * WeightsDouble<Integer> capacities = g.addEdgesWeights("capacity", double.class);
 * for (Integer e : g.edges())
 *  capacities.set(e, 1);
 *
 * String sourceVertex = ...;
 * String targetVertex = ...;
 * MaximumFlow maxFlowAlg = MaximumFlow.newInstance();
 * Flow<String, Integer> flow = maxFlowAlg.computeMaximumFlow(g, capacities, sourceVertex, targetVertex);
 *
 * System.out.println("The maximum flow that can be pushed in the network is " + flow.getSupply(sourceVertex));
 * for (Integer e : g.edges()) {
 * 	double capacity = capacities.get(e);
 * 	double f = flow.getFlow(e);
 * 	System.out.println("flow on edge " + e + ": " + f + "/" + capacity);
 * }
 * }</pre>
 *
 * @see        MaximumFlow
 * @see        MinimumCostFlow
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface Flow<V, E> {

	/**
	 * Get the amount of flow units going along an edge.
	 *
	 * <p>
	 * If the graph is directed, a flow of \(f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of \(f\)
	 * units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}.
	 *
	 * <p>
	 * If the graph is undirected, a flow of \(+f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of
	 * \(f\) units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}, while a flow of \(-f\) units on
	 * {@code e}, for \(-cap(e) \leq -f \leq 0\), means a flow of \(|-f|\) units of flow from {@code edgeTarget(e)} to
	 * {@code edgeSource(e)} (opposite direction).
	 *
	 * @param  edge                an edge in the graph
	 * @return                     the amount of flow units going along an edge
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge
	 */
	double getFlow(E edge);

	/**
	 * Get the sum of flow units going out of a vertex, minus the sum of flow units going into a vertex.
	 *
	 * <p>
	 * In the classical {@linkplain MaximumFlow maximum flow problem} with two terminal nodes {@code s} and {@code t},
	 * the supply of {@code s} is the total amount of flow in the network, and it is equal to the negative of the supply
	 * of {@code t}. The negative of the supply is sometimes called the demand. For any other vertex, the supply is
	 * zero.
	 *
	 *
	 * @param  vertex a vertex in the graph
	 * @return        the sum of flow units going out of a vertex, minus the sum of flow units going into a vertex
	 */
	double getSupply(V vertex);

	/**
	 * Get the sum of flow units going out of a set of vertices, minus the sum of flow units going into the set.
	 *
	 * <p>
	 * Flow on edges between vertices in {@code vertices} is ignored.
	 *
	 * <p>
	 * In the {@linkplain MaximumFlow maximum flow problem} with multi sources set {@code S} and multi sinks set
	 * {@code T}, the supply of {@code S} is the total amount of flow in the network, and it is equal to the negative of
	 * the supply of {@code T}. For any other vertex, the supply is zero.
	 *
	 * @param  vertices a set of vertices in the graph
	 * @return          the sum of flow units going out of a set of vertices, minus the sum of flow units going into the
	 *                  set
	 */
	double getSupplySubset(Collection<V> vertices);

	/**
	 * Get the total cost of the flow.
	 *
	 * <p>
	 * The cost of an edge is defined as the amount of flow on the edge multiplied by the weight (cost) of the edge. The
	 * total cost of a network is the sum of all edges cost.
	 *
	 * @param  cost a weight function
	 * @return      the total cost of the flow
	 * @see         MinimumCostFlow
	 */
	double getTotalCost(WeightFunction<E> cost);

}
