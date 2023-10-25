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
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Flow on graph edges, with capacities and flows values.
 * <p>
 * A flow network on graph edges is defined as two functions: the capacity function \(C:E \rightarrow R\) and flow
 * function \( F:E \rightarrow R\). The capacity function define how many units of flow an edge can transfer from its
 * source to its target. The flow function define the number of units of flow that are currently transferred along each
 * edge. The capacity of any edge must be non negative, and the edge's flow must be smaller or equal to its capacity.
 * <p>
 * Problems formulated using flow networks involve a source and a sink vertices. The source is a vertex from which the
 * flow is originated, and every flow going along its edges must reach the sink vertex using the edges of the graphs
 * while not violating the capacities of the network. For each vertex except the source and sink the sum of flow units
 * going along {@link Graph#inEdges(int)} must be equal to the sum of flow units going along
 * {@link Graph#outEdges(int)}.
 * <p>
 * A flow is most intuitively defined on directed graphs, as the flow on an edge is transferred from one vertex to
 * another in some direction, but we can define and solve flow problem on undirected graphs as well. Technically, the
 * flows values returned by {@link #getFlow(int)} can either be positive or negative for undirected edges, with values
 * absolutely smaller than the capacity of the edge. A positive flow \(+f\) value assigned to edge {@code e} means a
 * flow directed from {@code edgeSource(e)} to {@code edgeTarget(e)} with \(f\) units of flow. A negative flow \(-f\)
 * value assigned to edge {@code e} means a flow directed from {@code edgeTarget(e)} to {@code edgeSource(e)} (opposite
 * direction) with \(|-f|\) units of flow (see {@link #getFlow(int)}).
 *
 * <pre> {@code
 * Graph g = ...;
 * FlowNetwork net = FlowNetwork.createAsEdgeWeight(g);
 * for (int e : g.edges())
 *  f.setCapacity(e, 1);
 *
 * int sourceVertex = ...;
 * int targetVertex = ...;
 * MaximumFlow maxFlowAlg = MaximumFlow.newInstance();
 *
 * double totalFlow = maxFlowAlg.computeMaximumFlow(g, net, sourceVertex, targetVertex);
 * System.out.println("The maximum flow that can be pushed in the network is " + totalFlow);
 * for (int e : g.edges()) {
 * 	double capacity = net.getCapacity(e);
 * 	double flow = net.getFlow(e);
 * 	System.out.println("flow on edge " + e + ": " + flow + "/" + capacity);
 * }
 * }</pre>
 *
 * @see    MaximumFlow
 * @author Barak Ugav
 */
public interface FlowNetwork {

	/**
	 * Get the capacity of an edge.
	 *
	 * @param  edge                      an edge identifier in the graph
	 * @return                           the capacity of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	double getCapacity(int edge);

	/**
	 * Set the capacity of an edge.
	 *
	 * @param  edge                      an edge identifier in the graph
	 * @param  capacity                  the new capacity of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 * @throws IllegalArgumentException  if {@code capacity} is negative
	 */
	void setCapacity(int edge, double capacity);

	/**
	 * Get the amount of flow units going along an edge.
	 * <p>
	 * If the graph is directed, a flow of \(f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of \(f\)
	 * units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}.
	 * <p>
	 * If the graph is undirected, a flow of \(+f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of
	 * \(f\) units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}, while a flow of \(-f\) units on
	 * {@code e}, for \(-cap(e) \leq -f &lt; 0\), means a flow of \(|-f|\) units of flow from {@code edgeTarget(e)} to
	 * {@code edgeSource(e)} (opposite direction).
	 *
	 * @param  edge                      an edge identifier in the graph
	 * @return                           the amount of flow units going along an edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	double getFlow(int edge);

	/**
	 * Set the amount of flow units going along an edge.
	 *
	 * @param  edge                      an edge identifier in the graph
	 * @param  flow                      the new flow of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	void setFlow(int edge, double flow);

	/**
	 * Get the sum of flow units going out of a source vertex.
	 *
	 * @param  g      a graph
	 * @param  source a source vertex
	 * @return        the sum of flow units going out of {@code source}
	 */
	default double getFlowSum(Graph g, int source) {
		return getFlowSum(g, IntList.of(source));
	}

	/**
	 * Get the sum of flow units going out of a set of source vertices.
	 *
	 * @param  g       a graph
	 * @param  sources a set of source vertices
	 * @return         the sum of flow units going out of {@code sources}
	 */
	default double getFlowSum(Graph g, IntCollection sources) {
		double sum = 0;
		if (g.isDirected()) {
			for (int source : sources) {
				for (int e : g.outEdges(source))
					sum += getFlow(e);
				for (int e : g.inEdges(source))
					sum -= getFlow(e);
			}
		} else {
			for (int source : sources) {
				for (int e : g.outEdges(source)) {
					if (source != g.edgeTarget(e)) {
						sum += getFlow(e);
					} else if (source != g.edgeSource(e)) {
						sum -= getFlow(e);
					}
				}
			}
		}
		return sum;
	}

	/**
	 * Get the cost of the flow along a set of edges.
	 * <p>
	 * The cost function define the cost per unit of flow on each edge of the network. The cost of an edge in the
	 * network is defined as the flow on the edge multiplied by the cost per unit of flow on the edge.
	 *
	 * @param  edges the set of edges to sum their cost
	 * @param  cost  a edge weight cost function
	 * @return       the sum of the cost of the flow along the edges
	 */
	default double getCostSum(IntCollection edges, WeightFunction cost) {
		double sum = 0;
		if (cost instanceof WeightFunctionInt) {
			WeightFunctionInt costInt = (WeightFunctionInt) cost;
			for (int e : edges)
				sum += getFlow(e) * costInt.weightInt(e);
		} else {
			for (int e : edges)
				sum += getFlow(e) * cost.weight(e);
		}
		return sum;
	}

	/**
	 * Create a flow network by adding edge weights using {@link Graph#addEdgesWeights}.
	 * <p>
	 * Unless {@link #setCapacity(int, double)} or {@link #setFlow(int, double)} are used, the capacity and flow of each
	 * edge will be zero.
	 * <p>
	 * By using {@link Graph#addEdgesWeights}, the weights containers (and the flow network) remains valid in case the
	 * graph is modified, as they are added to the graph. This is a key difference between this function and
	 * {@link #createFromEdgeWeights(WeightsDouble, WeightsDouble)}, which if provided with weights containers created
	 * with {@link Weights#createExternalEdgesWeights}. doesn't remain valid if the graph is modified, but may suite in
	 * scenarios in which we are not allowed to add weights to the graph.
	 *
	 * @param  g a graph
	 * @return   a flow network implemented as edge weights containers added to the graph
	 */
	static FlowNetwork createFromEdgeWeights(Graph g) {
		WeightsDouble capacities = g.addEdgesWeights("_capacity", double.class);
		WeightsDouble flows = g.addEdgesWeights("_flow", double.class);
		return createFromEdgeWeights(capacities, flows);
	}

	/**
	 * Create a flow network by using existing edge weights.
	 * <p>
	 * This method can be used together with {@link Weights#createExternalEdgesWeights}, creating a flow network for a
	 * graph without adding any new containers to it. This is useful in scenarios in which we are not allowed to modify
	 * the graph.
	 *
	 * @param  capacities a weight container containing the capacities of the edges
	 * @param  flows      a weight container that will contain the flow values of the edges
	 * @return            a flow network implemented as external edge weights containers
	 */
	static FlowNetwork createFromEdgeWeights(WeightsDouble capacities, WeightsDouble flows) {
		return new FlowNetworks.NetImplEdgeWeights(capacities, flows);
	}

}
