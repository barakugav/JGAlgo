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

import java.util.Arrays;
import java.util.Collection;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;

abstract class MaximumFlowAbstract extends MinimumEdgeCutSTUtils.AbstractImpl implements MaximumFlow {

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity, V source, V sink) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			int source0 = ((Integer) source).intValue();
			int sink0 = ((Integer) sink).intValue();
			return (Flow<V, E>) computeMaximumFlow((IndexGraph) g, capacity0, source0, sink0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);
			IFlow indexFlow = computeMaximumFlow(iGraph, iCapacity, iSource, iSink);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Flow<V, E> computeMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity, Collection<V> sources,
			Collection<V> sinks) {
		if (g instanceof IndexGraph) {
			IWeightFunction capacity0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) capacity);
			IntCollection sources0 = IntAdapters.asIntCollection((Collection<Integer>) sources);
			IntCollection sinks0 = IntAdapters.asIntCollection((Collection<Integer>) sinks);
			return (Flow<V, E>) computeMaximumFlow((IndexGraph) g, capacity0, sources0, sinks0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iCapacity = IndexIdMaps.idToIndexWeightFunc(capacity, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);
			IFlow indexFlow = computeMaximumFlow(iGraph, iCapacity, iSources, iSinks);
			return Flows.flowFromIndexFlow(g, indexFlow);
		}
	}

	abstract IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, int source, int sink);

	abstract IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, IntCollection sources,
			IntCollection sinks);

	@Override
	IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, int source, int sink) {
		return MinimumEdgeCutSTUtils.computeMinimumCutUsingMaxFlow(g, w, source, sink, this);
	}

	@Override
	IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, IntCollection sources, IntCollection sinks) {
		return MinimumEdgeCutSTUtils.computeMinimumCutUsingMaxFlow(g, w, sources, sinks, this);
	}

	abstract static class WithoutResidualGraph extends MaximumFlowAbstract {

		static class Worker {
			final IndexGraph g;
			final int source;
			final int sink;
			final int n;
			final IWeightFunction capacity;
			final boolean directed;

			Worker(IndexGraph g, IWeightFunction capacity, int source, int sink) {
				Assertions.Flows.sourceSinkNotTheSame(source, sink);
				Assertions.Flows.positiveCapacities(g, capacity);
				this.g = g;
				this.source = source;
				this.sink = sink;
				this.n = g.vertices().size();
				this.capacity = capacity;
				directed = g.isDirected();
			}

			void initCapacities(int[] capacities) {
				if (WeightFunction.isCardinality(capacity)) {
					Arrays.fill(capacities, 1);
				} else {
					IWeightFunctionInt capacity = (IWeightFunctionInt) this.capacity;
					for (int m = g.edges().size(), e = 0; e < m; e++)
						capacities[e] = capacity.weightInt(e);
				}
			}

			void initCapacities(double[] capacities) {
				if (WeightFunction.isCardinality(capacity)) {
					Arrays.fill(capacities, 1);
				} else {
					for (int m = g.edges().size(), e = 0; e < m; e++)
						capacities[e] = capacity.weight(e);
				}
			}

			IFlow constructResult(double[] capacity, double[] residualCapacity) {
				double[] flow = new double[g.edges().size()];
				for (int m = g.edges().size(), e = 0; e < m; e++)
					flow[e] = capacity[e] - residualCapacity[e];
				return new Flows.FlowImpl(g, flow);
			}

			IFlow constructResult(int[] capacity, int[] residualCapacity) {
				double[] flow = new double[g.edges().size()];
				for (int m = g.edges().size(), e = 0; e < m; e++)
					flow[e] = capacity[e] - residualCapacity[e];
				return new Flows.FlowImpl(g, flow);
			}

		}

		@Override
		IFlow computeMaximumFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IntCollection sources,
				IntCollection sinks) {
			if (sources.size() == 1 && sinks.size() == 1)
				return computeMaximumFlow(gOrig, capacityOrig, sources.iterator().nextInt(),
						sinks.iterator().nextInt());
			Assertions.Flows.sourcesSinksNotTheSame(sources, sinks);

			IndexGraphBuilder builder =
					gOrig.isDirected() ? IndexGraphBuilder.newDirected() : IndexGraphBuilder.newUndirected();
			builder.expectedVerticesNum(gOrig.vertices().size() + 2);
			builder.expectedEdgesNum(gOrig.edges().size() + sources.size() + sinks.size());

			for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
				builder.addVertex();
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			final int originalEdgesThreshold = builder.edges().size();

			final int source = builder.addVertex();
			final int sink = builder.addVertex();
			Object capacities;
			if (WeightFunction.isInteger(capacityOrig)) {
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

			IWeightFunction capacity;
			if (WeightFunction.isInteger(capacityOrig)) {
				IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
				final int[] caps = (int[]) capacities;
				IWeightFunctionInt capacityInt = edge -> edge < originalEdgesThreshold ? capacityOrigInt.weightInt(edge)
						: caps[edge - originalEdgesThreshold];
				capacity = capacityInt;

			} else {
				final double[] caps = (double[]) capacities;
				capacity = edge -> edge < originalEdgesThreshold ? capacityOrig.weight(edge)
						: caps[edge - originalEdgesThreshold];
			}

			IFlow flow0 = computeMaximumFlow(g, capacity, source, sink);
			double[] flow = new double[gOrig.edges().size()];
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				flow[e] = flow0.getFlow(e);
			return new Flows.FlowImpl(gOrig, flow);
		}

	}

	abstract static class WithResidualGraph extends MaximumFlowAbstract {

		static class Worker {
			final IndexGraph gOrig;
			final int source;
			final int sink;
			final int n;
			final IWeightFunction capacityOrig;

			final IndexGraph g;
			final int[] edgeRef;
			final int[] twin;

			final boolean multiSourceMultiSink;
			final IntCollection sources;
			final IntCollection sinks;

			Worker(IndexGraph gOrig, IWeightFunction capacityOrig, int source, int sink) {
				Assertions.Flows.sourceSinkNotTheSame(source, sink);
				Assertions.Flows.positiveCapacities(gOrig, capacityOrig);
				this.gOrig = gOrig;
				this.source = source;
				this.sink = sink;
				this.sources = IntLists.singleton(source);
				this.sinks = IntLists.singleton(sink);
				this.n = gOrig.vertices().size();
				this.capacityOrig = capacityOrig;
				multiSourceMultiSink = false;

				Flows.ResidualGraph.Builder builder = new Flows.ResidualGraph.Builder(gOrig);
				builder.addAllOriginalEdges();

				Flows.ResidualGraph residualGraph = builder.build();
				g = residualGraph.g;
				edgeRef = residualGraph.edgeRef;
				twin = residualGraph.twin;
			}

			Worker(IndexGraph gOrig, IWeightFunction capacityOrig, IntCollection sources, IntCollection sinks) {
				Assertions.Flows.sourcesSinksNotTheSame(sources, sinks);
				Assertions.Flows.positiveCapacities(gOrig, capacityOrig);
				this.gOrig = gOrig;
				this.n = gOrig.vertices().size() + 2;
				this.sources = sources;
				this.sinks = sinks;
				this.capacityOrig = capacityOrig;
				multiSourceMultiSink = true;

				Flows.ResidualGraph.Builder builder = new Flows.ResidualGraph.Builder(gOrig);
				builder.addAllOriginalEdges();

				source = builder.addVertex();
				sink = builder.addVertex();
				for (int s : sources)
					builder.addEdge(source, s, -1);
				for (int t : sinks)
					builder.addEdge(t, sink, -1);

				Flows.ResidualGraph residualGraph = builder.build();
				g = residualGraph.g;
				edgeRef = residualGraph.edgeRef;
				twin = residualGraph.twin;
			}

			void initCapacitiesAndFlows(double[] flow, double[] capacity) {
				Arrays.fill(flow, 0);
				initCapacities(capacity);
			}

			void initCapacitiesAndFlows(int[] flow, int[] capacity) {
				Arrays.fill(flow, 0);
				initCapacities(capacity);
			}

			void initCapacities(double[] residualCapacity) {
				if (gOrig.isDirected()) {
					if (WeightFunction.isCardinality(capacityOrig)) {
						for (int m = g.edges().size(), e = 0; e < m; e++)
							residualCapacity[e] = isOriginalEdge(e) ? 1 : 0;
					} else {
						for (int m = g.edges().size(), e = 0; e < m; e++)
							residualCapacity[e] = isOriginalEdge(e) ? capacityOrig.weight(edgeRef[e]) : 0;
					}
				} else {
					if (WeightFunction.isCardinality(capacityOrig)) {
						for (int m = g.edges().size(), e = 0; e < m; e++) {
							int eRef = edgeRef[e];
							double cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? 1 : 0;
							residualCapacity[e] = cap;
						}
					} else {
						for (int m = g.edges().size(), e = 0; e < m; e++) {
							int eRef = edgeRef[e];
							double cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink)
									? capacityOrig.weight(eRef)
									: 0;
							residualCapacity[e] = cap;
						}
					}
				}
				if (multiSourceMultiSink) {
					double capacitySum = 0;
					for (int m = gOrig.edges().size(), e = 0; e < m; e++)
						capacitySum += capacityOrig.weight(e);

					/* init edges from super-source to sources and from sinks to super-sink */
					for (int m = g.edges().size(), e = 0; e < m; e++)
						if (edgeRef[e] == -1)
							residualCapacity[e] =
									source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
				}
			}

			void initCapacities(int[] residualCapacity) {
				IWeightFunctionInt capacity = (IWeightFunctionInt) this.capacityOrig;
				if (gOrig.isDirected()) {
					if (WeightFunction.isCardinality(capacityOrig)) {
						for (int m = g.edges().size(), e = 0; e < m; e++)
							residualCapacity[e] = isOriginalEdge(e) ? 1 : 0;
					} else {
						for (int m = g.edges().size(), e = 0; e < m; e++)
							residualCapacity[e] = isOriginalEdge(e) ? capacity.weightInt(edgeRef[e]) : 0;
					}
				} else {
					if (WeightFunction.isCardinality(capacityOrig)) {
						for (int m = g.edges().size(), e = 0; e < m; e++) {
							int eRef = edgeRef[e];
							int cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? 1 : 0;
							residualCapacity[e] = cap;
						}
					} else {
						for (int m = g.edges().size(), e = 0; e < m; e++) {
							int eRef = edgeRef[e];
							int cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink)
									? capacity.weightInt(eRef)
									: 0;
							residualCapacity[e] = cap;
						}
					}
				}
				if (multiSourceMultiSink) {
					int capacitySum = 0;
					for (int m = gOrig.edges().size(), e = 0; e < m; e++) {
						int cap = capacity.weightInt(e);
						int capacitySumNext = capacitySum + cap;
						if (((capacitySum ^ capacitySumNext) & (cap ^ capacitySumNext)) < 0) {
							// HD 2-12 Overflow iff both arguments have the opposite sign of the result
							capacitySum = Integer.MAX_VALUE;
							break;
						}
						capacitySum = capacitySumNext;
					}

					/* init edges from super-source to sources and from sinks to super-sink */
					for (int m = g.edges().size(), e = 0; e < m; e++)
						if (edgeRef[e] == -1)
							residualCapacity[e] =
									source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
				}
			}

			IFlow constructResult(double[] flow) {
				double[] flowRes = new double[gOrig.edges().size()];
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					if (isOriginalEdge(e))
						/* The flow of e might be negative if the original graph is undirected, which is fine */
						flowRes[edgeRef[e]] = flow[e];
				}
				return new Flows.FlowImpl(gOrig, flowRes);
			}

			IFlow constructResult(int[] flow) {
				double[] flowRes = new double[gOrig.edges().size()];
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					if (isOriginalEdge(e))
						/* The flow of e might be negative if the original graph is undirected, which is fine */
						flowRes[edgeRef[e]] = flow[e];
				}
				return new Flows.FlowImpl(gOrig, flowRes);
			}

			boolean isOriginalEdge(int e) {
				int eOrig = edgeRef[e];
				return eOrig != -1 && g.edgeSource(e) == gOrig.edgeSource(eOrig);

			}
		}

	}

}
