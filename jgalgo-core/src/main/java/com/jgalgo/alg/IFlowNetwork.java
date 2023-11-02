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
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Flow on graph edges, with capacities and flows values.
 * <p>
 * This interface is a specific version of {@link FlowNetwork} for {@link IntGraph}. For the full documentation see
 * {@link FlowNetwork}.
 *
 * @see    MaximumFlow
 * @author Barak Ugav
 */
public interface IFlowNetwork extends FlowNetwork<Integer, Integer> {

	/**
	 * Get the capacity of an edge.
	 *
	 * @param  edge                      an edge in the graph
	 * @return                           the capacity of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge
	 */
	double getCapacity(int edge);

	@Deprecated
	@Override
	default double getCapacity(Integer edge) {
		return getCapacity(edge.intValue());
	}

	/**
	 * Set the capacity of an edge.
	 *
	 * @param  edge                      an edge in the graph
	 * @param  capacity                  the new capacity of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge
	 * @throws IllegalArgumentException  if {@code capacity} is negative
	 */
	void setCapacity(int edge, double capacity);

	@Deprecated
	@Override
	default void setCapacity(Integer edge, double capacity) {
		setCapacity(edge.intValue(), capacity);
	}

	/**
	 * Get the amount of flow units going along an edge.
	 * <p>
	 * If the graph is directed, a flow of \(f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of \(f\)
	 * units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}.
	 * <p>
	 * If the graph is undirected, a flow of \(+f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of
	 * \(f\) units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}, while a flow of \(-f\) units on
	 * {@code e}, for \(-cap(e) \leq -f \leq 0\), means a flow of \(|-f|\) units of flow from {@code edgeTarget(e)} to
	 * {@code edgeSource(e)} (opposite direction).
	 *
	 * @param  edge                      an edge in the graph
	 * @return                           the amount of flow units going along an edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge
	 */
	double getFlow(int edge);

	@Deprecated
	@Override
	default double getFlow(Integer edge) {
		return getFlow(edge.intValue());
	}

	/**
	 * Set the amount of flow units going along an edge.
	 *
	 * @param  edge                      an edge in the graph
	 * @param  flow                      the new flow of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge
	 */
	void setFlow(int edge, double flow);

	@Deprecated
	@Override
	default void setFlow(Integer edge, double flow) {
		setFlow(edge.intValue(), flow);
	}

	/**
	 * Get the sum of flow units going out of a source vertex.
	 *
	 * @param  g      a graph
	 * @param  source a source vertex
	 * @return        the sum of flow units going out of {@code source}
	 */
	default double getFlowSum(IntGraph g, int source) {
		return getFlowSum(g, IntList.of(source));
	}

	@Deprecated
	@Override
	default double getFlowSum(Graph<Integer, Integer> g, Integer source) {
		return getFlowSum(g, IntList.of(source.intValue()));
	}

	@Override
	default double getFlowSum(Graph<Integer, Integer> g, Iterable<Integer> sources) {
		if (!(g instanceof IntGraph))
			return FlowNetwork.super.getFlowSum(g, sources);
		double sum = 0;
		IntGraph g0 = (IntGraph) g;
		if (g0.isDirected()) {
			for (int source : IntAdapters.asIntIterable(sources)) {
				for (int e : g0.outEdges(source))
					sum += getFlow(e);
				for (int e : g0.inEdges(source))
					sum -= getFlow(e);
			}
		} else {
			for (int source : IntAdapters.asIntIterable(sources)) {
				for (int e : g0.outEdges(source)) {
					if (source != g0.edgeTarget(e)) {
						sum += getFlow(e);
					} else if (source != g0.edgeSource(e)) {
						sum -= getFlow(e);
					}
				}
			}
		}
		return sum;
	}

	@Override
	default double getCostSum(Iterable<Integer> edges, WeightFunction<Integer> cost) {
		IntIterable edges0 = IntAdapters.asIntIterable(edges);
		IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc(cost);
		double sum = 0;
		if (cost0 instanceof IWeightFunctionInt) {
			IWeightFunctionInt costInt = (IWeightFunctionInt) cost0;
			for (int e : edges0)
				sum += getFlow(e) * costInt.weightInt(e);
		} else {
			for (int e : edges0)
				sum += getFlow(e) * cost0.weight(e);
		}
		return sum;
	}

	/**
	 * Create a flow network by adding edge weights using {@link IntGraph#addEdgesWeights}.
	 * <p>
	 * Unless {@link #setCapacity(int, double)} or {@link #setFlow(int, double)} are used, the capacity and flow of each
	 * edge will be zero.
	 * <p>
	 * By using {@link IntGraph#addEdgesWeights}, the weights containers (and the flow network) remains valid in case
	 * the graph is modified, as they are added to the graph. This is a key difference between this function and
	 * {@link #createFromEdgeWeights(IWeightsDouble, IWeightsDouble)}, which if provided with weights containers created
	 * with {@link IWeights#createExternalEdgesWeights} doesn't remain valid if the graph is modified, but may suite in
	 * scenarios in which we are not allowed to add weights to the graph.
	 *
	 * @param  g a graph
	 * @return   a flow network implemented as edge weights containers added to the graph
	 */
	static IFlowNetwork createFromEdgeWeights(IntGraph g) {
		IWeightsDouble capacities = g.addEdgesWeights("_capacity", double.class);
		IWeightsDouble flows = g.addEdgesWeights("_flow", double.class);
		return createFromEdgeWeights(capacities, flows);
	}

	/**
	 * Create a flow network by using existing edge weights.
	 * <p>
	 * This method can be used together with {@link IWeights#createExternalEdgesWeights}, creating a flow network for a
	 * graph without adding any new containers to it. This is useful in scenarios in which we are not allowed to modify
	 * the graph.
	 *
	 * @param  capacities a weight container containing the capacities of the edges
	 * @param  flows      a weight container that will contain the flow values of the edges
	 * @return            a flow network implemented as external edge weights containers
	 */
	static IFlowNetwork createFromEdgeWeights(IWeightsDouble capacities, IWeightsDouble flows) {
		return new FlowNetworks.NetImplEdgeIWeights(capacities, flows);
	}

}
