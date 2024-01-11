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
import java.util.Arrays;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;

class MaximumFlows {

	private MaximumFlows() {}

	abstract static class WithoutResidualGraph implements MaximumFlowBase {

		static class Worker {
			final IndexGraph g;
			final int source;
			final int sink;
			final int n;
			final IWeightFunction capacity;
			final boolean directed;

			Worker(IndexGraph g, IWeightFunction capacity, int source, int sink) {
				Assertions.flowSourceSinkNotTheSame(source, sink);
				Assertions.flowPositiveCapacities(g, capacity);
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
					for (int e : range(g.edges().size()))
						capacities[e] = capacity.weightInt(e);
				}
			}

			void initCapacities(double[] capacities) {
				if (WeightFunction.isCardinality(capacity)) {
					Arrays.fill(capacities, 1);
				} else {
					for (int e : range(g.edges().size()))
						capacities[e] = capacity.weight(e);
				}
			}

			IFlow constructResult(double[] capacity, double[] residualCapacity) {
				double[] flow = new double[g.edges().size()];
				for (int e : range(g.edges().size()))
					flow[e] = capacity[e] - residualCapacity[e];
				return new Flows.FlowImpl(g, flow);
			}

			IFlow constructResult(int[] capacity, int[] residualCapacity) {
				double[] flow = new double[g.edges().size()];
				for (int e : range(g.edges().size()))
					flow[e] = capacity[e] - residualCapacity[e];
				return new Flows.FlowImpl(g, flow);
			}

		}

		@Override
		public IFlow computeMaximumFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IntCollection sources,
				IntCollection sinks) {
			if (sources.size() == 1 && sinks.size() == 1)
				return computeMaximumFlow(gOrig, capacityOrig, sources.iterator().nextInt(),
						sinks.iterator().nextInt());
			Assertions.flowSourcesSinksNotTheSame(sources, sinks);

			IndexGraphBuilder builder = IndexGraphBuilder.newInstance(gOrig.isDirected());
			builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
			builder.ensureEdgeCapacity(gOrig.edges().size() + sources.size() + sinks.size());

			builder.addVertices(gOrig.vertices());
			builder.addEdges(EdgeSet.allOf(gOrig));
			final int originalEdgesThreshold = builder.edges().size();

			final int source = builder.addVertexInt();
			final int sink = builder.addVertexInt();
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
			for (int e : range(gOrig.edges().size()))
				flow[e] = flow0.getFlow(e);
			return new Flows.FlowImpl(gOrig, flow);
		}

	}

	abstract static class WithResidualGraph implements MaximumFlowBase {

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
				Assertions.flowSourceSinkNotTheSame(source, sink);
				Assertions.flowPositiveCapacities(gOrig, capacityOrig);
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
				Assertions.flowSourcesSinksNotTheSame(sources, sinks);
				Assertions.flowPositiveCapacities(gOrig, capacityOrig);
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
						for (int e : range(g.edges().size()))
							residualCapacity[e] = isOriginalEdge(e) ? 1 : 0;
					} else {
						for (int e : range(g.edges().size()))
							residualCapacity[e] = isOriginalEdge(e) ? capacityOrig.weight(edgeRef[e]) : 0;
					}
				} else {
					if (WeightFunction.isCardinality(capacityOrig)) {
						for (int e : range(g.edges().size())) {
							int eRef = edgeRef[e];
							double cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? 1 : 0;
							residualCapacity[e] = cap;
						}
					} else {
						for (int e : range(g.edges().size())) {
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
					for (int e : range(gOrig.edges().size()))
						capacitySum += capacityOrig.weight(e);

					/* init edges from super-source to sources and from sinks to super-sink */
					for (int e : range(g.edges().size()))
						if (edgeRef[e] == -1)
							residualCapacity[e] =
									source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
				}
			}

			void initCapacities(int[] residualCapacity) {
				IWeightFunctionInt capacity = (IWeightFunctionInt) this.capacityOrig;
				if (gOrig.isDirected()) {
					if (WeightFunction.isCardinality(capacityOrig)) {
						for (int e : range(g.edges().size()))
							residualCapacity[e] = isOriginalEdge(e) ? 1 : 0;
					} else {
						for (int e : range(g.edges().size()))
							residualCapacity[e] = isOriginalEdge(e) ? capacity.weightInt(edgeRef[e]) : 0;
					}
				} else {
					if (WeightFunction.isCardinality(capacityOrig)) {
						for (int e : range(g.edges().size())) {
							int eRef = edgeRef[e];
							int cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? 1 : 0;
							residualCapacity[e] = cap;
						}
					} else {
						for (int e : range(g.edges().size())) {
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
					for (int e : range(gOrig.edges().size())) {
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
					for (int e : range(g.edges().size()))
						if (edgeRef[e] == -1)
							residualCapacity[e] =
									source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
				}
			}

			IFlow constructResult(double[] flow) {
				double[] flowRes = new double[gOrig.edges().size()];
				for (int e : range(g.edges().size())) {
					if (isOriginalEdge(e))
						/* The flow of e might be negative if the original graph is undirected, which is fine */
						flowRes[edgeRef[e]] = flow[e];
				}
				return new Flows.FlowImpl(gOrig, flowRes);
			}

			IFlow constructResult(int[] flow) {
				double[] flowRes = new double[gOrig.edges().size()];
				for (int e : range(g.edges().size())) {
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
