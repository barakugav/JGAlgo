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
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsInt;

/**
 * Flow on graph edges, with integer capacities and flows values.
 *
 * <p>
 * Similar to the regular {@link FlowNetwork} interface, but with integer capacities and flows. Some algorithms that
 * work on flow networks are specifically for integers networks, or may performed faster if the capacities and flows are
 * integers.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface FlowNetworkInt<V, E> extends FlowNetwork<V, E> {

	/**
	 * Get the integer capacity of an edge.
	 *
	 * @param  edge                an edge in the graph
	 * @return                     the capacity of the edge
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge
	 */
	public int getCapacityInt(E edge);

	@Deprecated
	@Override
	default double getCapacity(E edge) {
		return getCapacityInt(edge);
	}

	/**
	 * Set the integer capacity of an edge.
	 *
	 * @param  edge                     an edge in the graph
	 * @param  capacity                 the new capacity of the edge
	 * @throws NoSuchEdgeException      if {@code edge} is not a valid edge
	 * @throws IllegalArgumentException if {@code capacity} is negative
	 */
	public void setCapacity(E edge, int capacity);

	@Deprecated
	@Override
	default void setCapacity(E edge, double capacity) {
		setCapacity(edge, (int) capacity);
	}

	/**
	 * Get the integer amount of flow units going along an edge.
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
	public int getFlowInt(E edge);

	@Deprecated
	@Override
	default double getFlow(E edge) {
		return getFlowInt(edge);
	}

	/**
	 * Set the integer amount of flow units going along an edge.
	 *
	 * @param  edge                an edge in the graph
	 * @param  flow                the new flow of the edge
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge
	 */
	public void setFlow(E edge, int flow);

	@Deprecated
	@Override
	default void setFlow(E edge, double flow) {
		setFlow(edge, (int) flow);
	}

	@Override
	default double getFlowSum(Graph<V, E> g, Iterable<V> sources) {
		long sum = 0;
		if (g.isDirected()) {
			for (V source : sources) {
				for (E e : g.outEdges(source))
					sum += getFlowInt(e);
				for (E e : g.inEdges(source))
					sum -= getFlowInt(e);
			}
		} else {
			for (V source : sources) {
				for (E e : g.outEdges(source)) {
					if (!source.equals(g.edgeTarget(e))) {
						sum += getFlowInt(e);
					} else if (!source.equals(g.edgeSource(e))) {
						sum -= getFlowInt(e);
					}
				}
			}
		}
		return sum;
	}

	@Override
	default double getCostSum(Iterable<E> edges, WeightFunction<E> cost) {
		if (cost instanceof WeightFunctionInt) {
			WeightFunctionInt<E> costInt = (WeightFunctionInt<E>) cost;
			long sum = 0;
			for (E e : edges)
				sum += getFlowInt(e) * costInt.weightInt(e);
			return sum;
		} else {
			double sum = 0;
			for (E e : edges)
				sum += getFlowInt(e) * cost.weight(e);
			return sum;
		}
	}

	/**
	 * Create an integer flow network by adding edge weights using {@link Graph#addEdgesWeights}.
	 *
	 * <p>
	 * Unless {@link #setCapacity(Object, int)} or {@link #setFlow(Object, int)} are used, the capacity and flow of each
	 * edge will be zero.
	 *
	 * <p>
	 * By using {@link Graph#addEdgesWeights}, the weights containers (and the flow network) remains valid in case the
	 * graph is modified, as they are added to the graph. This is a key difference between this function and
	 * {@link #createFromEdgeWeights(WeightsInt, WeightsInt)}, which if provided with weights containers created with
	 * {@link Weights#createExternalEdgesWeights}. doesn't remain valid if the graph is modified, but may suite in
	 * scenarios in which we are not allowed to add weights to the graph.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     a flow network implemented as edge weights containers added to the graph
	 */
	static <V, E> FlowNetworkInt<V, E> createFromEdgeWeights(Graph<V, E> g) {
		WeightsInt<E> capacities = g.addEdgesWeights("_capacity", int.class);
		WeightsInt<E> flows = g.addEdgesWeights("_flow", int.class);
		return createFromEdgeWeights(capacities, flows);
	}

	/**
	 * Create a flow network by using existing edge weights.
	 *
	 * <p>
	 * This method can be used together with {@link Weights#createExternalEdgesWeights}, creating a flow network for a
	 * graph without adding any new containers to it. This is useful in scenarios in which we are not allowed to modify
	 * the graph.
	 *
	 * @param  <V>        the vertices type
	 * @param  <E>        the edges type
	 * @param  capacities a weight container containing the capacities of the edges
	 * @param  flows      a weight container that will contain the flow values of the edges
	 * @return            a flow network implemented as external edge weights containers
	 */
	@SuppressWarnings("unchecked")
	static <V, E> FlowNetworkInt<V, E> createFromEdgeWeights(WeightsInt<E> capacities, WeightsInt<E> flows) {
		if (capacities instanceof IWeightsInt && flows instanceof IWeightsInt) {
			return (FlowNetworkInt<V, E>) new FlowNetworks.NetImplEdgeIWeightsInt((IWeightsInt) capacities,
					(IWeightsInt) flows);
		} else {
			return new FlowNetworks.NetImplEdgeWeightsInt<>(capacities, flows);
		}
	}

}
