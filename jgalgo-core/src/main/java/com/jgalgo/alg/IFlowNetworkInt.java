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
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntIterable;

/**
 * Flow on graph edges, with integer capacities and flows values.
 *
 * <p>
 * Similar to the regular {@link IFlowNetwork} interface, but with integer capacities and flows. Some algorithms that
 * work on flow networks are specifically for integers networks, or may performed faster if the capacities and flows are
 * integers.
 *
 * @author Barak Ugav
 */
public interface IFlowNetworkInt extends IFlowNetwork, FlowNetworkInt<Integer, Integer> {

	/**
	 * Get the integer capacity of an edge.
	 *
	 * @param  edge                an edge in the graph
	 * @return                     the capacity of the edge
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge
	 */
	public int getCapacityInt(int edge);

	@Deprecated
	@Override
	default double getCapacity(int edge) {
		return getCapacityInt(edge);
	}

	@Deprecated
	@Override
	default int getCapacityInt(Integer edge) {
		return getCapacityInt(edge.intValue());
	}

	@Deprecated
	@Override
	default double getCapacity(Integer edge) {
		return getCapacityInt(edge.intValue());
	}

	/**
	 * Set the integer capacity of an edge.
	 *
	 * @param  edge                     an edge in the graph
	 * @param  capacity                 the new capacity of the edge
	 * @throws NoSuchEdgeException      if {@code edge} is not a valid edge
	 * @throws IllegalArgumentException if {@code capacity} is negative
	 */
	public void setCapacity(int edge, int capacity);

	@Deprecated
	@Override
	default void setCapacity(int edge, double capacity) {
		setCapacity(edge, (int) capacity);
	}

	@Deprecated
	@Override
	default void setCapacity(Integer edge, int capacity) {
		setCapacity(edge.intValue(), capacity);
	}

	@Deprecated
	@Override
	default void setCapacity(Integer edge, double capacity) {
		setCapacity(edge.intValue(), (int) capacity);
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
	public int getFlowInt(int edge);

	@Deprecated
	@Override
	default double getFlow(int edge) {
		return getFlowInt(edge);
	}

	@Deprecated
	@Override
	default int getFlowInt(Integer edge) {
		return getFlowInt(edge.intValue());
	}

	@Deprecated
	@Override
	default double getFlow(Integer edge) {
		return getFlowInt(edge.intValue());
	}

	/**
	 * Set the integer amount of flow units going along an edge.
	 *
	 * @param  edge                an edge in the graph
	 * @param  flow                the new flow of the edge
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge
	 */
	public void setFlow(int edge, int flow);

	@Deprecated
	@Override
	default void setFlow(int edge, double flow) {
		setFlow(edge, (int) flow);
	}

	@Deprecated
	@Override
	default void setFlow(Integer edge, int flow) {
		setFlow(edge.intValue(), flow);
	}

	@Deprecated
	@Override
	default void setFlow(Integer edge, double flow) {
		setFlow(edge.intValue(), (int) flow);
	}

	@Override
	default double getFlowSum(Graph<Integer, Integer> g, Iterable<Integer> sources) {
		if (!(g instanceof IntGraph))
			return FlowNetworkInt.super.getFlowSum(g, sources);
		long sum = 0;
		IntGraph g0 = (IntGraph) g;
		if (g0.isDirected()) {
			for (int source : IntAdapters.asIntIterable(sources)) {
				for (int e : g0.outEdges(source))
					sum += getFlowInt(e);
				for (int e : g0.inEdges(source))
					sum -= getFlowInt(e);
			}
		} else {
			for (int source : IntAdapters.asIntIterable(sources)) {
				for (int e : g0.outEdges(source)) {
					if (source != g0.edgeTarget(e)) {
						sum += getFlowInt(e);
					} else if (source != g0.edgeSource(e)) {
						sum -= getFlowInt(e);
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
		if (cost0 instanceof IWeightFunctionInt) {
			IWeightFunctionInt costInt = (IWeightFunctionInt) cost0;
			long sum = 0;
			for (int e : edges0)
				sum += getFlowInt(e) * costInt.weightInt(e);
			return sum;
		} else {
			double sum = 0;
			for (int e : edges0)
				sum += getFlowInt(e) * cost0.weight(e);
			return sum;
		}
	}

	/**
	 * Create an integer flow network by adding edge weights using {@link IntGraph#addEdgesWeights}.
	 *
	 * <p>
	 * Unless {@link #setCapacity(int, int)} or {@link #setFlow(int, int)} are used, the capacity and flow of each edge
	 * will be zero.
	 *
	 * <p>
	 * By using {@link IntGraph#addEdgesWeights}, the weights containers (and the flow network) remains valid in case
	 * the graph is modified, as they are added to the graph. This is a key difference between this function and
	 * {@link #createFromEdgeWeights(IWeightsDouble, IWeightsDouble)}, which if provided with weights containers created
	 * with {@link IWeights#createExternalEdgesWeights}. doesn't remain valid if the graph is modified, but may suite in
	 * scenarios in which we are not allowed to add weights to the graph.
	 *
	 * @param  g a graph
	 * @return   a flow network implemented as edge weights containers added to the graph
	 */
	static IFlowNetworkInt createFromEdgeWeights(IntGraph g) {
		IWeightsInt capacities = g.addEdgesWeights("_capacity", int.class);
		IWeightsInt flows = g.addEdgesWeights("_flow", int.class);
		return createFromEdgeWeights(capacities, flows);
	}

	/**
	 * Create a flow network by using existing edge weights.
	 *
	 * <p>
	 * This method can be used together with {@link IWeights#createExternalEdgesWeights}, creating a flow network for a
	 * graph without adding any new containers to it. This is useful in scenarios in which we are not allowed to modify
	 * the graph.
	 *
	 * @param  capacities a weight container containing the capacities of the edges
	 * @param  flows      a weight container that will contain the flow values of the edges
	 * @return            a flow network implemented as external edge weights containers
	 */
	static IFlowNetworkInt createFromEdgeWeights(IWeightsInt capacities, IWeightsInt flows) {
		return new FlowNetworks.NetImplEdgeIWeightsInt(capacities, flows);
	}

}
