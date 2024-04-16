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
import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Abstract class for computing a minimum cost flow in a graph, based on a source-sink solution.
 *
 * <p>
 * The {@link MinimumCostFlow} interface expose a large number of methods of different variants of the minimum cost flow
 * problem. This abstract class implements some of these methods by reducing to a single source-sink problem,
 * {@link #computeMinCostMaxFlow(IndexGraph, IWeightFunction, IWeightFunction, int, int)}, which is left to the
 * subclasses to implement.
 *
 * @author Barak Ugav
 */
public abstract class MinimumCostFlowAbstractBasedSourceSink extends MinimumCostFlows.AbstractImpl {

	/**
	 * Default constructor.
	 */
	public MinimumCostFlowAbstractBasedSourceSink() {}

	@Override
	protected IFlow computeMinCostMaxFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IWeightFunction costOrig,
			IntCollection sources, IntCollection sinks) {
		Assertions.onlyDirected(gOrig);

		final boolean integerFlow = WeightFunction.isInteger(capacityOrig);
		final boolean integerCost = WeightFunction.isInteger(costOrig);

		IndexGraphBuilder builder = IndexGraphBuilder.directed();
		builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
		builder.ensureEdgeCapacity(gOrig.edges().size() + sources.size() + sinks.size());

		/* Add all original vertices and edges */
		builder.addVertices(gOrig.vertices());
		builder.addEdges(EdgeSet.allOf(gOrig));
		/* any edge with index smaller than this threshold is an original edge of the graph */
		final int origEdgesThreshold = builder.edges().size();

		/* Add two artificial terminal vertices, a source and a sink */
		final int source = builder.addVertexInt();
		final int sink = builder.addVertexInt();

		/* Connect the source to the sources with high capacity edges */
		/* Connect the sinks to the sink with high capacity edges */
		Object capacities;
		if (integerFlow) {
			IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
			int[] capacities0 = new int[sources.size() + sinks.size()];
			int capIdx = 0;
			for (int s : sources) {
				builder.addEdge(source, s);
				capacities0[capIdx++] = Flows.vertexMaxSupply(gOrig, capacityOrigInt, s);
			}
			for (int t : sinks) {
				builder.addEdge(t, sink);
				capacities0[capIdx++] = Flows.vertexMaxDemand(gOrig, capacityOrigInt, t);
			}
			capacities = capacities0;
		} else {
			double[] capacities0 = new double[sources.size() + sinks.size()];
			int capIdx = 0;
			for (int s : sources) {
				builder.addEdge(source, s);
				capacities0[capIdx++] = Flows.vertexMaxSupply(gOrig, capacityOrig, s);
			}
			for (int t : sinks) {
				builder.addEdge(t, sink);
				capacities0[capIdx++] = Flows.vertexMaxDemand(gOrig, capacityOrig, t);
			}
			capacities = capacities0;
		}

		IndexGraph g = builder.build();

		/*
		 * Create a network for the new graph by storing capacities and flows of the artificial edges and by reducing
		 * the capacities of edges by their lower bound
		 */
		IWeightFunction capacity;
		if (integerFlow) {
			IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
			int[] caps = (int[]) capacities;
			IWeightFunctionInt capacityInt =
					e -> e < origEdgesThreshold ? capacityOrigInt.weightInt(e) : caps[e - origEdgesThreshold];
			capacity = capacityInt;
		} else {
			double[] caps = (double[]) capacities;
			capacity = e -> e < origEdgesThreshold ? capacityOrig.weight(e) : caps[e - origEdgesThreshold];
		}

		IWeightFunction cost;
		if (integerCost) {
			IWeightFunctionInt costOrigInt = (IWeightFunctionInt) costOrig;
			IWeightFunctionInt costInt = e -> e < origEdgesThreshold ? costOrigInt.weightInt(e) : 0;
			cost = costInt;
		} else {
			cost = e -> e < origEdgesThreshold ? costOrig.weight(e) : 0;
		}

		/* Compute a min-cost max-flow in the new graph and network */
		IFlow flow0 = computeMinCostMaxFlow(g, capacity, cost, source, sink);
		double[] flow = new double[gOrig.edges().size()];
		for (int e : range(gOrig.edges().size()))
			flow[e] = flow0.getFlow(e);
		return newFlow(gOrig, flow);
	}

	@Override
	protected IFlow computeMinCostMaxFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IWeightFunction costOrig,
			IWeightFunction lowerBound, IntCollection sources, IntCollection sinks) {
		Objects.requireNonNull(gOrig);
		Objects.requireNonNull(capacityOrig);
		Objects.requireNonNull(costOrig);
		Objects.requireNonNull(lowerBound);

		Assertions.onlyDirected(gOrig);
		Assertions.flowCheckLowerBound(gOrig, capacityOrig, lowerBound);

		final boolean integerFlow = WeightFunction.isInteger(capacityOrig) && WeightFunction.isInteger(lowerBound);
		final boolean integerCost = WeightFunction.isInteger(costOrig);

		/*
		 * To solve the problem of minimum-cost maximum-flow between a set of sources and sinks, with a flow lower bound
		 * for each edge, we perform a reduction to min-cost max-flow between a single source and a sink sink without
		 * lower bounds. To get rid of the lower bound, remove from each edge capacity its lower bound, and add/remove
		 * supply from the edge endpoints. This reduction is slightly more complicated than the others, as some vertices
		 * (the sources/sinks) require 'infinite' supply, while others (other vertices with supply) require finite
		 * supply. We create a new graph with all the vertices and edges of the original graph, with addition of a new
		 * source and sink, and connect the source to the sources with high capacity edges, the source to vertices with
		 * a positive supply with capacity equal to the supply, the sinks to the sink with high capacity edges and
		 * lastly the vertices with negative supply to the sink with capacity equal to the supply.
		 */

		/* For each edge with lower bound add/remove supply to the edge endpoints */
		IWeightFunction supply = computeSupply(gOrig, capacityOrig, lowerBound, null);

		IndexGraphBuilder builder = IndexGraphBuilder.directed();
		builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
		builder.ensureEdgeCapacity(gOrig.edges().size() + sources.size() + sinks.size() + gOrig.vertices().size());

		/* Add all original vertices and edges */
		builder.addVertices(gOrig.vertices());
		builder.addEdges(EdgeSet.allOf(gOrig));
		/* any edge with index smaller than this threshold is an original edge of the graph */
		final int origEdgesThreshold = builder.edges().size();

		/* determine a great enough capacity ('infinite') for edges to sources (from sinks) */

		/* Add two artificial terminal vertices, a source and a sink */
		final int source = builder.addVertexInt();
		final int sink = builder.addVertexInt();

		/* Connect the source to the sources with high capacity edges */
		/* Connect the sinks to the sink with high capacity edges */
		final List<?> capacities;
		if (integerFlow) {
			IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
			IntList capacities0 = new IntArrayList(sources.size() + sinks.size());
			for (int s : sources) {
				builder.addEdge(source, s);
				capacities0.add(Flows.vertexMaxSupply(gOrig, capacityOrigInt, s));
			}
			for (int t : sinks) {
				builder.addEdge(t, sink);
				capacities0.add(Flows.vertexMaxDemand(gOrig, capacityOrigInt, t));
			}
			capacities = capacities0;
		} else {
			DoubleList capacities0 = new DoubleArrayList(sources.size() + sinks.size());
			for (int s : sources) {
				builder.addEdge(source, s);
				capacities0.add(Flows.vertexMaxSupply(gOrig, capacityOrig, s));
			}
			for (int t : sinks) {
				builder.addEdge(t, sink);
				capacities0.add(Flows.vertexMaxDemand(gOrig, capacityOrig, t));
			}
			capacities = capacities0;
		}
		/*
		 * Any edge with index smaller than this threshold and equal or greater than origEdgesThreshold is an edge
		 * connect source-sources or sinks-sink. Any edge with index greater or equal to this threshold is an edge
		 * connecting a source to a vertex with positive supply or a vertex with negative supply to a sink.
		 */
		final int sourcesSinksThreshold = builder.edges().size();

		/*
		 * Connect the source to all vertices with positive supply and the vertices with negative supply to the sink
		 */
		if (integerFlow) {
			IWeightFunctionInt supplyInt = (IWeightFunctionInt) supply;
			IntList capacities0 = (IntList) capacities;
			for (int v : range(gOrig.vertices().size())) {
				int sup = supplyInt.weightInt(v);
				if (sup > 0) {
					builder.addEdge(source, v);
					capacities0.add(sup);
				} else if (sup < 0) {
					builder.addEdge(v, sink);
					capacities0.add(-sup);
				}
			}
		} else {
			DoubleList capacities0 = (DoubleList) capacities;
			for (int v : range(gOrig.vertices().size())) {
				double sup = supply.weight(v);
				if (sup > 0) {
					builder.addEdge(source, v);
					capacities0.add(sup);
				} else if (sup < 0) {
					builder.addEdge(v, sink);
					capacities0.add(-sup);
				}
			}
		}

		IndexGraph g = builder.build();

		/*
		 * Create a network for the new graph by storing capacities and flows of the artificial edges and by reducing
		 * the capacities of edges by their lower bound
		 */
		IWeightFunction capacity;
		if (integerFlow) {
			IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
			IWeightFunctionInt lowerBoundInt = (IWeightFunctionInt) lowerBound;
			int[] caps = ((IntArrayList) capacities).elements();
			IWeightFunctionInt capacityInt =
					edge -> edge < origEdgesThreshold ? capacityOrigInt.weightInt(edge) - lowerBoundInt.weightInt(edge)
							: caps[edge - origEdgesThreshold];
			capacity = capacityInt;
		} else {
			double[] caps = ((DoubleArrayList) capacities).elements();
			capacity = edge -> edge < origEdgesThreshold ? capacityOrig.weight(edge) - lowerBound.weight(edge)
					: caps[edge - origEdgesThreshold];
		}

		IWeightFunction cost;
		if (integerCost) {
			/*
			 * Create a cost function for the new graph: original edges have their original costs, big negative cost for
			 * edges that connect vertices with supply as we must satisfy them, and zero cost for edges connecting
			 * source-sources or sinks-sink
			 */
			IWeightFunctionInt costOrigInt = (IWeightFunctionInt) costOrig;
			final int supplyEdgeCost = -hugeCost(gOrig, costOrigInt);
			IWeightFunctionInt costInt = e -> {
				if (e < origEdgesThreshold)
					return costOrigInt.weightInt(e); /* original edge */
				if (e < sourcesSinksThreshold)
					return 0; /* edge to source/sink */
				return supplyEdgeCost; /* edge to a non source/sink vertex with non-zero supply */
			};
			cost = costInt;
		} else {
			/*
			 * Create a cost function for the new graph: original edges have their original costs, big negative cost for
			 * edges that connect vertices with supply as we must satisfy them, and zero cost for edges connecting
			 * source-sources or sinks-sink
			 */
			final double supplyEdgeCost = -hugeCost(gOrig, costOrig);
			cost = e -> {
				if (e < origEdgesThreshold)
					return costOrig.weight(e); /* original edge */
				if (e < sourcesSinksThreshold)
					return 0; /* edge to source/sink */
				return supplyEdgeCost; /* edge to a non source/sink vertex with non-zero supply */
			};
		}

		/* Compute a min-cost max-flow in the new graph and network */
		IFlow flow0 = computeMinCostMaxFlow(g, capacity, cost, source, sink);

		/* assert all supply was provided */
		double eps = range(sourcesSinksThreshold, g.edges().size())
				.mapToDouble(capacity::weight)
				.filter(c -> c > 0)
				.min()
				.orElse(0);
		assert range(sourcesSinksThreshold, g.edges().size())
				.allMatch(e -> Math.abs(flow0.getFlow(e) - capacity.weight(e)) < eps);

		double[] flow = new double[gOrig.edges().size()];
		for (int e : range(gOrig.edges().size()))
			flow[e] = flow0.getFlow(e) + lowerBound.weight(e);
		return newFlow(gOrig, flow);
	}

	@Override
	protected IFlow computeMinCostFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IWeightFunction costOrig,
			IWeightFunction supply) {
		Assertions.onlyDirected(gOrig);
		Assertions.flowCheckSupply(gOrig, supply);
		capacityOrig = IWeightFunction.replaceNullWeightFunc(capacityOrig);
		costOrig = IWeightFunction.replaceNullWeightFunc(costOrig);
		supply = IWeightFunction.replaceNullWeightFunc(supply);

		final boolean integerFlow = WeightFunction.isInteger(capacityOrig) && WeightFunction.isInteger(supply);
		final boolean integerCost = WeightFunction.isInteger(costOrig);

		/*
		 * To solve the minimum cost flow of given supply we use a reduction to minimum-cost maximum-flow between two
		 * terminal vertices, source and sink. We add an edge from the source to each vertex with positive supply with
		 * capacity equal to the supply, and an edge from each vertex with negative supply to the sink with capacity
		 * equal to the supply.
		 */

		IndexGraphBuilder builder = IndexGraphBuilder.directed();
		builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
		builder.ensureEdgeCapacity(gOrig.edges().size() + gOrig.vertices().size());

		/* Add all original vertices and edges */
		builder.addVertices(gOrig.vertices());
		builder.addEdges(EdgeSet.allOf(gOrig));
		/* any edge with index greater than this threshold is not an original edge of the graph */
		final int origEdgesThreshold = builder.edges().size();

		/* Add two artificial vertices, source and sink */
		final int source = builder.addVertexInt();
		final int sink = builder.addVertexInt();

		/* Connect the source to vertices with positive supply and vertices with negative supply to the sink */
		List<?> capacities;
		if (integerFlow) {
			IWeightFunctionInt supplyInt = (IWeightFunctionInt) supply;
			IntList capacities0 = new IntArrayList();
			for (int v : range(gOrig.vertices().size())) {
				int sup = supplyInt.weightInt(v);
				if (sup > 0) {
					builder.addEdge(source, v);
					capacities0.add(sup);
				} else if (sup < 0) {
					builder.addEdge(v, sink);
					capacities0.add(-sup);
				}
			}
			capacities = capacities0;
		} else {
			DoubleList capacities0 = new DoubleArrayList();
			for (int v : range(gOrig.vertices().size())) {
				double sup = supply.weight(v);
				if (sup > 0) {
					builder.addEdge(source, v);
					capacities0.add(sup);
				} else if (sup < 0) {
					builder.addEdge(v, sink);
					capacities0.add(-sup);
				}
			}
			capacities = capacities0;
		}

		IndexGraph g = builder.build();

		/*
		 * Create a network for the new graph by using two new arrays for the artificial edges capacities and flows
		 */
		IWeightFunction capacity;
		if (integerFlow) {
			IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
			int[] caps = ((IntArrayList) capacities).elements();
			IWeightFunctionInt capacityInt = edge -> edge < origEdgesThreshold ? capacityOrigInt.weightInt(edge)
					: caps[edge - origEdgesThreshold];
			capacity = capacityInt;
		} else {
			double[] caps = ((DoubleArrayList) capacities).elements();
			IWeightFunction capacityOrig0 = capacityOrig;
			capacity = edge -> edge < origEdgesThreshold ? capacityOrig0.weight(edge) : caps[edge - origEdgesThreshold];
		}

		/*
		 * All the artificial edges should not have a cost, if its possible to satisfy the supply they will be saturated
		 * anyway
		 */
		IWeightFunction cost;
		if (integerCost) {
			IWeightFunctionInt costOrigInt = (IWeightFunctionInt) costOrig;
			IWeightFunctionInt costInt = e -> e < origEdgesThreshold ? costOrigInt.weightInt(e) : 0;
			cost = costInt;
		} else {
			IWeightFunction costOrig0 = costOrig;
			cost = e -> e < origEdgesThreshold ? costOrig0.weight(e) : 0;
		}

		/* Compute a minimum-cost maximum-flow between the two artificial vertices */
		IFlow flow0 = computeMinCostMaxFlow(g, capacity, cost, source, sink);
		double[] flow = new double[gOrig.edges().size()];
		for (int e : range(gOrig.edges().size()))
			flow[e] = flow0.getFlow(e);
		return newFlow(gOrig, flow);
	}

}
