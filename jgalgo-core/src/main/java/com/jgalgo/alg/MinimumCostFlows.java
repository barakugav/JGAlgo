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

import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

class MinimumCostFlows {

	private MinimumCostFlows() {}

	private abstract static class AbstractImplBase implements MinimumCostFlow {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity,
				WeightFunction<E> cost, V source, V sink) {
			if (g instanceof IndexGraph) {
				IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
				IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
				int source0 = ((Integer) source).intValue(), sink0 = ((Integer) sink).intValue();
				return (Flow<V, E>) computeMinCostMaxFlow((IndexGraph) g, capacity0, cost0, source0, sink0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
				IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
				int iSource = viMap.idToIndex(source);
				int iSink = viMap.idToIndex(sink);
				IFlow indexFlow = computeMinCostMaxFlow(iGraph, iCapacity, iCost, iSource, iSink);
				return Flows.flowFromIndexFlow(g, indexFlow);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity,
				WeightFunction<E> cost, WeightFunction<E> lowerBound, V source, V sink) {
			if (g instanceof IndexGraph) {
				IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
				IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
				IWeightFunction lowerBound0 =
						WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) lowerBound);
				int source0 = ((Integer) source).intValue(), sink0 = ((Integer) sink).intValue();
				return (Flow<V, E>) computeMinCostMaxFlow((IndexGraph) g, capacity0, cost0, lowerBound0, source0,
						sink0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
				IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
				IWeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
				int iSource = viMap.idToIndex(source);
				int iSink = viMap.idToIndex(sink);
				IFlow indexFlow = computeMinCostMaxFlow(iGraph, iCapacity, iCost, iLowerBound, iSource, iSink);
				return Flows.flowFromIndexFlow(g, indexFlow);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity,
				WeightFunction<E> cost, Collection<V> sources, Collection<V> sinks) {
			if (g instanceof IndexGraph) {
				IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
				IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
				IntCollection sources0 = IntAdapters.asIntCollection((Collection<Integer>) sources);
				IntCollection sinks0 = IntAdapters.asIntCollection((Collection<Integer>) sinks);
				return (Flow<V, E>) computeMinCostMaxFlow((IndexGraph) g, capacity0, cost0, sources0, sinks0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
				IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
				IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
				IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);
				IFlow indexFlow = computeMinCostMaxFlow(iGraph, iCapacity, iCost, iSources, iSinks);
				return Flows.flowFromIndexFlow(g, indexFlow);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Flow<V, E> computeMinCostMaxFlow(Graph<V, E> g, WeightFunction<E> capacity,
				WeightFunction<E> cost, WeightFunction<E> lowerBound, Collection<V> sources, Collection<V> sinks) {
			if (g instanceof IndexGraph) {
				IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
				IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
				IWeightFunction lowerBound0 =
						WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) lowerBound);
				IntCollection sources0 = IntAdapters.asIntCollection((Collection<Integer>) sources);
				IntCollection sinks0 = IntAdapters.asIntCollection((Collection<Integer>) sinks);
				return (Flow<V, E>) computeMinCostMaxFlow((IndexGraph) g, capacity0, cost0, lowerBound0, sources0,
						sinks0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
				IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
				IWeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
				IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
				IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);
				IFlow indexFlow = computeMinCostMaxFlow(iGraph, iCapacity, iCost, iLowerBound, iSources, iSinks);
				return Flows.flowFromIndexFlow(g, indexFlow);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Flow<V, E> computeMinCostFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
				WeightFunction<V> supply) {
			if (g instanceof IndexGraph) {
				IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
				IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
				IWeightFunction supply0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) supply);
				return (Flow<V, E>) computeMinCostFlow((IndexGraph) g, capacity0, cost0, supply0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
				IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
				IWeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);
				IFlow indexFlow = computeMinCostFlow(iGraph, iCapacity, iCost, iSupply);
				return Flows.flowFromIndexFlow(g, indexFlow);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Flow<V, E> computeMinCostFlow(Graph<V, E> g, WeightFunction<E> capacity, WeightFunction<E> cost,
				WeightFunction<E> lowerBound, WeightFunction<V> supply) {
			if (g instanceof IndexGraph) {
				IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
				IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) cost);
				IWeightFunction lowerBound0 =
						WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) lowerBound);
				IWeightFunction supply0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) supply);
				return (Flow<V, E>) computeMinCostFlow((IndexGraph) g, capacity0, cost0, lowerBound0, supply0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
				IWeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
				IWeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
				IWeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);
				IFlow indexFlow = computeMinCostFlow(iGraph, iCapacity, iCost, iLowerBound, iSupply);
				return Flows.flowFromIndexFlow(g, indexFlow);
			}
		}

		abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost, int source,
				int sink);

		abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
				IWeightFunction lowerBound, int source, int sink);

		abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
				IntCollection sources, IntCollection sinks);

		abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
				IWeightFunction lowerBound, IntCollection sources, IntCollection sinks);

		abstract IFlow computeMinCostFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
				IWeightFunction supply);

		abstract IFlow computeMinCostFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
				IWeightFunction lowerBound, IWeightFunction supply);

	}

	abstract static class AbstractImpl extends AbstractImplBase {

		@Override
		IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
				IWeightFunction lowerBound, int source, int sink) {
			Objects.requireNonNull(lowerBound);
			return computeMinCostMaxFlow(g, capacity, cost, lowerBound, IntList.of(source), IntList.of(sink));
		}

		@Override
		IFlow computeMinCostFlow(IndexGraph g, IWeightFunction capacityOrig, IWeightFunction cost,
				IWeightFunction lowerBound, IWeightFunction supply) {
			Assertions.Graphs.onlyDirected(g);
			Assertions.Flows.checkLowerBound(g, capacityOrig, lowerBound);
			Assertions.Flows.checkSupply(g, supply);
			if (capacityOrig == null)
				capacityOrig = IWeightFunction.CardinalityWeightFunction;
			if (cost == null)
				cost = IWeightFunction.CardinalityWeightFunction;

			final boolean integerFlow = WeightFunction.isInteger(capacityOrig) && WeightFunction.isInteger(lowerBound);

			/*
			 * To solve the minimum cost flow for a given supply and edges lower bounds, we perform a reduction to the
			 * problem with given supply without any edges flow lower bounds. For each edge with lower bound we subtract
			 * the lower bound from the capacity of the edge, and add/remove supply to the edge endpoints.
			 */

			/* Create a network by subtracting the lower bound from each edge capacity */
			IWeightFunction capacity;
			if (integerFlow) {
				IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
				IWeightFunctionInt lowerBoundInt = (IWeightFunctionInt) lowerBound;
				IWeightFunctionInt capacityInt =
						edge -> capacityOrigInt.weightInt(edge) - lowerBoundInt.weightInt(edge);
				capacity = capacityInt;
			} else {
				IWeightFunction capacityOrig0 = capacityOrig;
				IWeightFunction lowerBound0 = lowerBound;
				capacity = edge -> capacityOrig0.weight(edge) - lowerBound0.weight(edge);
			}

			/* For each edge with lower bound we add/remove supply from the end endpoints */
			IWeightFunction supply2 = computeSupply(g, capacityOrig, lowerBound, supply);

			/* Solve the reduction problem with only supply without edges lower bounds */
			IFlow flow0 = computeMinCostFlow(g, capacity, cost, supply2);
			double[] flow = new double[g.edges().size()];
			for (int m = g.edges().size(), e = 0; e < m; e++)
				flow[e] = flow0.getFlow(e) + lowerBound.weight(e);
			return new Flows.FlowImpl(g, flow);
		}

		static double hugeCost(IndexGraph g, IWeightFunction cost) {
			if (WeightFunction.isInteger(cost))
				return hugeCostLong(g, (IWeightFunctionInt) cost);

			double costSum = 0;
			for (int m = g.edges().size(), e = 0; e < m; e++)
				costSum += Math.abs(cost.weight(e));
			return 1 + costSum;
		}

		static int hugeCost(IndexGraph g, IWeightFunctionInt cost) {
			long costSum = hugeCostLong(g, cost);
			int costSumInt = (int) costSum;
			if (costSum != costSumInt)
				throw new AssertionError("integer overflow, huge cost can't fit in 32bit int");
			return costSumInt;
		}

		private static long hugeCostLong(IndexGraph g, IWeightFunctionInt cost) {
			long costSum = 0;
			for (int m = g.edges().size(), e = 0; e < m; e++)
				costSum += Math.abs(cost.weightInt(e));
			return costSum + 1;
		}

		static IWeightFunction computeSupply(IndexGraph g, IWeightFunction capacity, IWeightFunction lowerBound,
				IWeightFunction supply) {
			boolean isInt = WeightFunction.isInteger(capacity);
			if (lowerBound != null)
				isInt = isInt && WeightFunction.isInteger(lowerBound);
			if (supply != null)
				isInt = isInt && WeightFunction.isInteger(lowerBound);
			if (isInt)
				return computeSupply(g, (IWeightFunctionInt) capacity, (IWeightFunctionInt) lowerBound,
						(IWeightFunctionInt) supply);

			IWeightsDouble supply2 = IWeights.createExternalVerticesWeights(g, double.class);
			if (supply != null) {
				for (int n = g.vertices().size(), v = 0; v < n; v++)
					supply2.set(v, supply.weight(v));
			}
			if (lowerBound != null) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					double l = lowerBound.weight(e);
					if (l == 0)
						continue;
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					supply2.set(u, supply2.get(u) - l);
					supply2.set(v, supply2.get(v) + l);
				}
			}
			return supply2;
		}

		static IWeightFunctionInt computeSupply(IndexGraph g, IWeightFunctionInt capacity,
				IWeightFunctionInt lowerBound, IWeightFunctionInt supply) {
			IWeightsInt supply2 = IWeights.createExternalVerticesWeights(g, int.class);
			if (supply != null) {
				for (int n = g.vertices().size(), v = 0; v < n; v++)
					supply2.set(v, supply.weightInt(v));
			}
			if (lowerBound != null) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int l = lowerBound.weightInt(e);
					if (l == 0)
						continue;
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					supply2.set(u, supply2.get(u) - l);
					supply2.set(v, supply2.get(v) + l);
				}
			}
			return supply2;
		}

	}

	abstract static class AbstractImplBasedSourceSink extends AbstractImpl {

		@Override
		abstract IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost, int source,
				int sink);

		@Override
		IFlow computeMinCostMaxFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IWeightFunction costOrig,
				IntCollection sources, IntCollection sinks) {
			Assertions.Graphs.onlyDirected(gOrig);

			final boolean integerFlow = WeightFunction.isInteger(capacityOrig);
			final boolean integerCost = WeightFunction.isInteger(costOrig);

			IndexGraphBuilder builder = IndexGraphBuilder.directed();
			builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
			builder.ensureEdgeCapacity(gOrig.edges().size() + sources.size() + sinks.size());

			/* Add all original vertices and edges */
			builder.addVertices(gOrig.vertices());
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index smaller than this threshold is an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			/* Add two artificial terminal vertices, a source and a sink */
			final int source = builder.addVertex();
			final int sink = builder.addVertex();

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
			 * Create a network for the new graph by storing capacities and flows of the artificial edges and by
			 * reducing the capacities of edges by their lower bound
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
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				flow[e] = flow0.getFlow(e);
			return new Flows.FlowImpl(gOrig, flow);
		}

		@Override
		IFlow computeMinCostMaxFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IWeightFunction costOrig,
				IWeightFunction lowerBound, IntCollection sources, IntCollection sinks) {
			Objects.requireNonNull(gOrig);
			Objects.requireNonNull(capacityOrig);
			Objects.requireNonNull(costOrig);
			Objects.requireNonNull(lowerBound);

			Assertions.Graphs.onlyDirected(gOrig);
			Assertions.Flows.checkLowerBound(gOrig, capacityOrig, lowerBound);

			final boolean integerFlow = WeightFunction.isInteger(capacityOrig) && WeightFunction.isInteger(lowerBound);
			final boolean integerCost = WeightFunction.isInteger(costOrig);

			/*
			 * To solve the problem of minimum-cost maximum-flow between a set of sources and sinks, with a flow lower
			 * bound for each edge, we perform a reduction to min-cost max-flow between a single source and a sink sink
			 * without lower bounds. To get rid of the lower bound, remove from each edge capacity its lower bound, and
			 * add/remove supply from the edge endpoints. This reduction is slightly more complicated than the others,
			 * as some vertices (the sources/sinks) require 'infinite' supply, while others (other vertices with supply)
			 * require finite supply. We create a new graph with all the vertices and edges of the original graph, with
			 * addition of a new source and sink, and connect the source to the sources with high capacity edges, the
			 * source to vertices with a positive supply with capacity equal to the supply, the sinks to the sink with
			 * high capacity edges and lastly the vertices with negative supply to the sink with capacity equal to the
			 * supply.
			 */

			/* For each edge with lower bound add/remove supply to the edge endpoints */
			IWeightFunction supply = computeSupply(gOrig, capacityOrig, lowerBound, null);

			IndexGraphBuilder builder = IndexGraphBuilder.directed();
			builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
			builder.ensureEdgeCapacity(gOrig.edges().size() + sources.size() + sinks.size() + gOrig.vertices().size());

			/* Add all original vertices and edges */
			builder.addVertices(gOrig.vertices());
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index smaller than this threshold is an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			/* determine a great enough capacity ('infinite') for edges to sources (from sinks) */

			/* Add two artificial terminal vertices, a source and a sink */
			final int source = builder.addVertex();
			final int sink = builder.addVertex();

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
				for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
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
				for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
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
			 * Create a network for the new graph by storing capacities and flows of the artificial edges and by
			 * reducing the capacities of edges by their lower bound
			 */
			IWeightFunction capacity;
			if (integerFlow) {
				IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
				IWeightFunctionInt lowerBoundInt = (IWeightFunctionInt) lowerBound;
				int[] caps = ((IntArrayList) capacities).elements();
				IWeightFunctionInt capacityInt = edge -> edge < origEdgesThreshold
						? capacityOrigInt.weightInt(edge) - lowerBoundInt.weightInt(edge)
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
				 * Create a cost function for the new graph: original edges have their original costs, big negative cost
				 * for edges that connect vertices with supply as we must satisfy them, and zero cost for edges
				 * connecting source-sources or sinks-sink
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
				 * Create a cost function for the new graph: original edges have their original costs, big negative cost
				 * for edges that connect vertices with supply as we must satisfy them, and zero cost for edges
				 * connecting source-sources or sinks-sink
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
			double eps = range(sourcesSinksThreshold, g.edges().size()).mapToDouble(capacity::weight).filter(c -> c > 0)
					.min().orElse(0);
			assert range(sourcesSinksThreshold, g.edges().size())
					.allMatch(e -> Math.abs(flow0.getFlow(e) - capacity.weight(e)) < eps);

			double[] flow = new double[gOrig.edges().size()];
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				flow[e] = flow0.getFlow(e) + lowerBound.weight(e);
			return new Flows.FlowImpl(gOrig, flow);
		}

		@Override
		IFlow computeMinCostFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IWeightFunction costOrig,
				IWeightFunction supply) {
			Assertions.Graphs.onlyDirected(gOrig);
			Assertions.Flows.checkSupply(gOrig, supply);
			if (capacityOrig == null)
				capacityOrig = IWeightFunction.CardinalityWeightFunction;
			if (costOrig == null)
				costOrig = IWeightFunction.CardinalityWeightFunction;
			if (supply == null)
				supply = IWeightFunction.CardinalityWeightFunction;

			final boolean integerFlow = WeightFunction.isInteger(capacityOrig) && WeightFunction.isInteger(supply);
			final boolean integerCost = WeightFunction.isInteger(costOrig);

			/*
			 * To solve the minimum cost flow of given supply we use a reduction to minimum-cost maximum-flow between
			 * two terminal vertices, source and sink. We add an edge from the source to each vertex with positive
			 * supply with capacity equal to the supply, and an edge from each vertex with negative supply to the sink
			 * with capacity equal to the supply.
			 */

			IndexGraphBuilder builder = IndexGraphBuilder.directed();
			builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
			builder.ensureEdgeCapacity(gOrig.edges().size() + gOrig.vertices().size());

			/* Add all original vertices and edges */
			builder.addVertices(gOrig.vertices());
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index greater than this threshold is not an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			/* Add two artificial vertices, source and sink */
			final int source = builder.addVertex();
			final int sink = builder.addVertex();

			/* Connect the source to vertices with positive supply and vertices with negative supply to the sink */
			List<?> capacities;
			if (integerFlow) {
				IWeightFunctionInt supplyInt = (IWeightFunctionInt) supply;
				IntList capacities0 = new IntArrayList();
				for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
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
				for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
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
				capacity = edge -> edge < origEdgesThreshold ? capacityOrig0.weight(edge)
						: caps[edge - origEdgesThreshold];
			}

			/*
			 * All the artificial edges should not have a cost, if its possible to satisfy the supply they will be
			 * saturated anyway
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
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				flow[e] = flow0.getFlow(e);
			return new Flows.FlowImpl(gOrig, flow);
		}

	}

	abstract static class AbstractImplBasedSupply extends AbstractImpl {

		@Override
		abstract IFlow computeMinCostFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
				IWeightFunction supply);

		@Override
		IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost, int source,
				int sink) {
			return computeMinCostMaxFlow(g, capacity, cost, IntList.of(source), IntList.of(sink));
		}

		@Override
		IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost, IntCollection sources,
				IntCollection sinks) {
			return computeMinCostMaxFlow(g, capacity, cost, null, sources, sinks);
		}

		@Override
		IFlow computeMinCostMaxFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IWeightFunction costOrig,
				IWeightFunction lowerBoundOrig, IntCollection sources, IntCollection sinks) {
			Assertions.Graphs.onlyDirected(gOrig);
			Assertions.Flows.sourcesSinksNotTheSame(sources, sinks);

			final boolean integerFlow =
					WeightFunction.isInteger(capacityOrig) && WeightFunction.isInteger(lowerBoundOrig);
			final boolean integerCost = WeightFunction.isInteger(costOrig);

			IndexGraphBuilder builder = IndexGraphBuilder.directed();
			builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
			builder.ensureEdgeCapacity(gOrig.edges().size() + sources.size() + sinks.size() + 2);

			builder.addVertices(gOrig.vertices());
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index smaller than this threshold is an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			final int source = builder.addVertex();
			final int sink = builder.addVertex();
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
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				flow[e] = flow0.getFlow(e);
			return new Flows.FlowImpl(gOrig, flow);
		}

	}

	static void saturateNegativeCostSelfEdges(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			double[] flow) {
		if (!g.isAllowSelfEdges())
			return;
		if (WeightFunction.isInteger(capacity)) {
			IWeightFunctionInt capacityInt = (IWeightFunctionInt) capacity;
			if (WeightFunction.isInteger(cost)) {
				IWeightFunctionInt costInt = (IWeightFunctionInt) cost;
				for (int e : Graphs.selfEdges(g))
					if (costInt.weightInt(e) < 0)
						flow[e] = capacityInt.weightInt(e);
			} else {
				for (int e : Graphs.selfEdges(g))
					if (cost.weight(e) < 0)
						flow[e] = capacityInt.weightInt(e);
			}
		} else {
			if (WeightFunction.isInteger(cost)) {
				IWeightFunctionInt costInt = (IWeightFunctionInt) cost;
				for (int e : Graphs.selfEdges(g))
					if (costInt.weightInt(e) < 0)
						flow[e] = capacity.weight(e);
			} else {
				for (int e : Graphs.selfEdges(g))
					if (cost.weight(e) < 0)
						flow[e] = capacity.weight(e);
			}
		}
	}

}
