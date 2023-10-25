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
import com.jgalgo.graph.WeightsInt;
import it.unimi.dsi.fastutil.ints.IntIterable;

/**
 * Flow on graph edges, with integer capacities and flows values.
 * <p>
 * Similar to the regular {@link FlowNetwork} interface, but with integer capacities and flows. Some algorithms that
 * work on flow networks are specifically for integers networks, or may performed faster if the capacities and flows are
 * integers.
 *
 * @author Barak Ugav
 */
public interface FlowNetworkInt extends FlowNetwork {

	/**
	 * Get the integer capacity of an edge.
	 *
	 * @param  edge                      an edge identifier in the graph
	 * @return                           the capacity of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	public int getCapacityInt(int edge);

	@Deprecated
	@Override
	default double getCapacity(int edge) {
		return getCapacityInt(edge);
	}

	/**
	 * Set the integer capacity of an edge.
	 *
	 * @param  edge                      an edge identifier in the graph
	 * @param  capacity                  the new capacity of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 * @throws IllegalArgumentException  if {@code capacity} is negative
	 */
	public void setCapacity(int edge, int capacity);

	@Deprecated
	@Override
	default void setCapacity(int edge, double capacity) {
		setCapacity(edge, (int) capacity);
	}

	/**
	 * Get the integer amount of flow units going along an edge.
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
	public int getFlowInt(int edge);

	@Deprecated
	@Override
	default double getFlow(int edge) {
		return getFlowInt(edge);
	}

	/**
	 * Set the integer amount of flow units going along an edge.
	 *
	 * @param  edge                      an edge identifier in the graph
	 * @param  flow                      the new flow of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	public void setFlow(int edge, int flow);

	@Deprecated
	@Override
	default void setFlow(int edge, double flow) {
		setFlow(edge, (int) flow);
	}

	@Override
	default double getCostSum(IntIterable edges, WeightFunction cost) {
		if (cost instanceof WeightFunctionInt) {
			WeightFunctionInt costInt = (WeightFunctionInt) cost;
			int sum = 0;
			for (int e : edges)
				sum += getFlowInt(e) * costInt.weightInt(e);
			return sum;
		} else {
			double sum = 0;
			for (int e : edges)
				sum += getFlowInt(e) * cost.weight(e);
			return sum;
		}
	}

	/**
	 * Create an integer flow network by adding edge weights using {@link Graph#addEdgesWeights}.
	 * <p>
	 * Unless {@link #setCapacity(int, int)} or {@link #setFlow(int, int)} are used, the capacity and flow of each edge
	 * will be zero.
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
	static FlowNetworkInt createFromEdgeWeights(Graph g) {
		WeightsInt capacities = g.addEdgesWeights("_capacity", int.class);
		WeightsInt flows = g.addEdgesWeights("_flow", int.class);
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
	static FlowNetworkInt createFromEdgeWeights(WeightsInt capacities, WeightsInt flows) {
		return new FlowNetworks.NetImplEdgeWeightsInt(capacities, flows);
	}

}
