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

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Fastutil;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Abstract class for computing a minimum cost flow in a graph, based on a supply solution.
 *
 * <p>
 * The {@link MinimumCostFlow} interface expose a large number of methods of different variants of the minimum cost flow
 * problem. This abstract class implements some of these methods by reducing to a single supply problem,
 * {@link #computeMinCostFlow(IndexGraph, IWeightFunction, IWeightFunction, IWeightFunction)}, which is left to the
 * subclasses to implement.
 *
 * @author Barak Ugav
 */
public abstract class MinimumCostFlowAbstractBasedSupply extends MinimumCostFlows.AbstractImpl {

	/**
	 * Default constructor.
	 */
	public MinimumCostFlowAbstractBasedSupply() {}

	@Override
	protected IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost, int source,
			int sink) {
		return computeMinCostMaxFlow(g, capacity, cost, Fastutil.list(source), Fastutil.list(sink));
	}

	@Override
	protected IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			IntCollection sources, IntCollection sinks) {
		return computeMinCostMaxFlow(g, capacity, cost, null, sources, sinks);
	}

	@Override
	protected IFlow computeMinCostMaxFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IWeightFunction costOrig,
			IWeightFunction lowerBoundOrig, IntCollection sources, IntCollection sinks) {
		Assertions.onlyDirected(gOrig);
		Assertions.flowSourcesSinksNotTheSame(sources, sinks);

		final boolean integerFlow = WeightFunction.isInteger(capacityOrig) && WeightFunction.isInteger(lowerBoundOrig);
		final boolean integerCost = WeightFunction.isInteger(costOrig);

		IndexGraphBuilder builder = IndexGraphBuilder.directed();
		builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
		builder.ensureEdgeCapacity(gOrig.edges().size() + sources.size() + sinks.size() + 2);

		builder.addVertices(gOrig.vertices());
		builder.addEdges(EdgeSet.allOf(gOrig));
		/* any edge with index smaller than this threshold is an original edge of the graph */
		final int origEdgesThreshold = builder.edges().size();

		final int source = builder.addVertexInt();
		final int sink = builder.addVertexInt();
		for (int v : sources)
			builder.addEdge(source, v);
		for (int v : sinks)
			builder.addEdge(v, sink);

		/*
		 * Any edge with index smaller than this threshold and equal or greater than origEdgesThreshold is an edge
		 * connect source-sources or sinks-sink. Any edge with index greater or equal to this threshold is an edge
		 * connecting the super source and the super sink.
		 */
		final int sourcesSinksThreshold = builder.edges().size();

		builder.addEdge(source, sink);
		builder.addEdge(sink, source);

		IndexGraph g = builder.build();

		final double hugeCapacity = Flows.hugeCapacity(gOrig, capacityOrig, sources, sinks);
		IWeightFunction capacity;
		if (integerFlow) {
			IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
			int hugeCapacityInt = (int) hugeCapacity;
			if (hugeCapacityInt != hugeCapacity)
				throw new AssertionError("integer overflow");
			IWeightFunctionInt capacityInt =
					e -> e < origEdgesThreshold ? capacityOrigInt.weightInt(e) : hugeCapacityInt;
			capacity = capacityInt;
		} else {
			capacity = e -> e < origEdgesThreshold ? capacityOrig.weight(e) : hugeCapacity;
		}

		IWeightFunction supply;
		if (integerFlow) {
			int hugeCapacityInt = (int) hugeCapacity;
			if (hugeCapacityInt != hugeCapacity)
				throw new AssertionError("integer overflow");
			IWeightFunctionInt supplyInt = v -> {
				if (v == source)
					return hugeCapacityInt;
				if (v == sink)
					return -hugeCapacityInt;
				return 0;
			};
			supply = supplyInt;
		} else {
			supply = v -> {
				if (v == source)
					return hugeCapacity;
				if (v == sink)
					return -hugeCapacity;
				return 0;
			};
		}

		IWeightFunction cost;
		if (integerCost) {
			IWeightFunctionInt costOrigInt = (IWeightFunctionInt) costOrig;
			final int hugeCost = hugeCost(gOrig, costOrigInt);
			IWeightFunctionInt costInt = e -> {
				if (e < origEdgesThreshold)
					return costOrigInt.weightInt(e);
				if (e < sourcesSinksThreshold)
					return 0;
				return hugeCost;
			};
			cost = costInt;
		} else {
			final double hugeCost = hugeCost(gOrig, costOrig);
			cost = e -> {
				if (e < origEdgesThreshold)
					return costOrig.weight(e);
				if (e < sourcesSinksThreshold)
					return 0;
				return hugeCost;
			};
		}

		IFlow flow0;
		if (lowerBoundOrig == null) {
			flow0 = computeMinCostFlow(g, capacity, cost, supply);
		} else {
			IWeightFunction lowerBound;
			if (integerFlow) {
				IWeightFunctionInt lowerBoundOrigInt = (IWeightFunctionInt) lowerBoundOrig;
				IWeightFunctionInt lowerBoundInt = e -> e < origEdgesThreshold ? lowerBoundOrigInt.weightInt(e) : 0;
				lowerBound = lowerBoundInt;
			} else {
				lowerBound = e -> e < origEdgesThreshold ? lowerBoundOrig.weight(e) : 0;
			}
			flow0 = computeMinCostFlow(g, capacity, cost, lowerBound, supply);
		}

		double[] flow = new double[gOrig.edges().size()];
		for (int e : range(gOrig.edges().size()))
			flow[e] = flow0.getFlow(e);
		return newFlow(gOrig, flow);
	}

}
