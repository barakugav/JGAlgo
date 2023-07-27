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
package com.jgalgo;

import java.util.Arrays;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;

abstract class MaximumFlowAbstract extends MinimumCutSTUtils.AbstractImpl implements MaximumFlow {

	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		if (g instanceof IndexGraph)
			return computeMaximumFlow((IndexGraph) g, net, source, sink);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
		int iSource = viMap.idToIndex(source);
		int iSink = viMap.idToIndex(sink);
		return computeMaximumFlow(iGraph, iNet, iSource, iSink);
	}

	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, IntCollection sources, IntCollection sinks) {
		if (g instanceof IndexGraph)
			return computeMaximumFlow((IndexGraph) g, net, sources, sinks);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
		IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
		IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);
		return computeMaximumFlow(iGraph, iNet, iSources, iSinks);
	}

	abstract double computeMaximumFlow(IndexGraph g, FlowNetwork net, int source, int sink);

	abstract double computeMaximumFlow(IndexGraph g, FlowNetwork net, IntCollection sources, IntCollection sinks);

	@Override
	Cut computeMinimumCut(IndexGraph g, WeightFunction w, int source, int sink) {
		return MinimumCutSTUtils.computeMinimumCutUsingMaxFlow(g, w, source, sink, this);
	}

	@Override
	Cut computeMinimumCut(IndexGraph g, WeightFunction w, IntCollection sources, IntCollection sinks) {
		return MinimumCutSTUtils.computeMinimumCutUsingMaxFlow(g, w, sources, sinks, this);
	}

	static abstract class WithoutResidualGraph extends MaximumFlowAbstract {

		static class Worker {
			final IndexGraph g;
			final int source;
			final int sink;
			final int n;
			final FlowNetwork net;
			final boolean directed;

			Worker(IndexGraph g, FlowNetwork net, int source, int sink) {
				Assertions.Flows.sourceSinkNotTheSame(source, sink);
				Assertions.Flows.positiveCapacities(g, net);
				this.g = g;
				this.source = source;
				this.sink = sink;
				this.n = g.vertices().size();
				this.net = net;
				directed = g.getCapabilities().directed();
			}

			void initCapacities(int[] capacities) {
				FlowNetwork.Int net = (FlowNetwork.Int) this.net;
				for (int m = g.edges().size(), e = 0; e < m; e++)
					capacities[e] = net.getCapacityInt(e);
			}

			void initCapacities(double[] capacities) {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					capacities[e] = net.getCapacity(e);
			}

			double constructResult(double[] flow) {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					net.setFlow(e, flow[e]);

				double totalFlow = 0;
				if (directed) {
					for (int e : g.outEdges(source))
						totalFlow += flow[e];
					for (int e : g.inEdges(source))
						totalFlow -= flow[e];
				} else {
					for (int e : g.outEdges(source)) {
						if (source != g.edgeTarget(e)) {
							totalFlow += flow[e];
						} else if (source != g.edgeSource(e)) {
							totalFlow -= flow[e];
						}
					}
				}
				return totalFlow;
			}

			int constructResult(int[] flow) {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					net.setFlow(e, flow[e]);

				int totalFlow = 0;
				if (directed) {
					for (int e : g.outEdges(source))
						totalFlow += flow[e];
					for (int e : g.inEdges(source))
						totalFlow -= flow[e];
				} else {
					for (int e : g.outEdges(source)) {
						if (source != g.edgeTarget(e)) {
							totalFlow += flow[e];
						} else if (source != g.edgeSource(e)) {
							totalFlow -= flow[e];
						}
					}
				}
				return totalFlow;
			}

			double constructResult(double[] capacity, double[] residualCapacity) {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					net.setFlow(e, capacity[e] - residualCapacity[e]);

				double totalFlow = 0;
				if (directed) {
					for (int e : g.outEdges(source))
						totalFlow += capacity[e] - residualCapacity[e];
					for (int e : g.inEdges(source))
						totalFlow -= capacity[e] - residualCapacity[e];
				} else {
					for (int e : g.outEdges(source)) {
						if (source != g.edgeTarget(e)) {
							totalFlow += capacity[e] - residualCapacity[e];
						} else if (source != g.edgeSource(e)) {
							totalFlow -= capacity[e] - residualCapacity[e];
						}
					}
				}
				return totalFlow;
			}

			int constructResult(int[] capacity, int[] residualCapacity) {
				FlowNetwork.Int net = (FlowNetwork.Int) this.net;
				for (int m = g.edges().size(), e = 0; e < m; e++)
					net.setFlow(e, capacity[e] - residualCapacity[e]);

				int totalFlow = 0;
				if (directed) {
					for (int e : g.outEdges(source))
						totalFlow += capacity[e] - residualCapacity[e];
					for (int e : g.inEdges(source))
						totalFlow -= capacity[e] - residualCapacity[e];
				} else {
					for (int e : g.outEdges(source)) {
						if (source != g.edgeTarget(e)) {
							totalFlow += capacity[e] - residualCapacity[e];
						} else if (source != g.edgeSource(e)) {
							totalFlow -= capacity[e] - residualCapacity[e];
						}
					}
				}
				return totalFlow;
			}

		}

		@Override
		double computeMaximumFlow(IndexGraph gOrig, FlowNetwork netOrig, IntCollection sources, IntCollection sinks) {
			Assertions.Flows.sourcesSinksNotTheSame(sources, sinks);

			IndexGraphBuilder builder = gOrig.getCapabilities().directed() ? IndexGraphBuilder.newDirected()
					: IndexGraphBuilder.newUndirected();
			for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
				builder.addVertex();
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			final int originalEdgesThreshold = builder.edges().size();

			final int source = builder.addVertex();
			final int sink = builder.addVertex();
			for (int s : sources)
				builder.addEdge(source, s);
			for (int t : sinks)
				builder.addEdge(t, sink);
			IndexGraph g = builder.build();

			FlowNetwork net;
			if (netOrig instanceof FlowNetwork.Int) {
				FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
				int sourcesOutCapacity = 0;
				int sinksInCapacity = 0;
				for (int s : sources)
					for (int e : gOrig.outEdges(s))
						sourcesOutCapacity += netOrigInt.getCapacityInt(e);
				for (int s : sinks)
					for (int e : gOrig.inEdges(s))
						sinksInCapacity += netOrigInt.getCapacityInt(e);
				final int hugeCapacity = 1 + Math.max(sourcesOutCapacity, sinksInCapacity);

				FlowNetwork.Int netInt = new FlowNetwork.Int() {
					final int[] flows = new int[g.edges().size() - originalEdgesThreshold];

					@Override
					public int getCapacityInt(int edge) {
						return edge < originalEdgesThreshold ? netOrigInt.getCapacityInt(edge) : hugeCapacity;
					}

					@Override
					public void setCapacity(int edge, int capacity) {
						throw new UnsupportedOperationException();
					}

					@Override
					public int getFlowInt(int edge) {
						return edge < originalEdgesThreshold ? netOrigInt.getFlowInt(edge)
								: flows[edge - originalEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, int flow) {
						if (edge < originalEdgesThreshold) {
							netOrigInt.setFlow(edge, flow);
						} else {
							flows[edge - originalEdgesThreshold] = flow;
						}
					}
				};
				net = netInt;

			} else {
				double sourcesOutCapacity = 0;
				double sinksInCapacity = 0;
				for (int s : sources)
					for (int e : gOrig.outEdges(s))
						sourcesOutCapacity += netOrig.getCapacity(e);
				for (int s : sinks)
					for (int e : gOrig.inEdges(s))
						sinksInCapacity += netOrig.getCapacity(e);
				final double hugeCapacity = 1 + Math.max(sourcesOutCapacity, sinksInCapacity);

				net = new FlowNetwork() {
					final double[] flows = new double[g.edges().size() - originalEdgesThreshold];

					@Override
					public double getCapacity(int edge) {
						return edge < originalEdgesThreshold ? netOrig.getCapacity(edge) : hugeCapacity;
					}

					@Override
					public void setCapacity(int edge, double capacity) {
						throw new UnsupportedOperationException();
					}

					@Override
					public double getFlow(int edge) {
						return edge < originalEdgesThreshold ? netOrig.getFlow(edge)
								: flows[edge - originalEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, double flow) {
						if (edge < originalEdgesThreshold) {
							netOrig.setFlow(edge, flow);
						} else {
							flows[edge - originalEdgesThreshold] = flow;
						}
					}
				};
			}

			return computeMaximumFlow(g, net, source, sink);
		}

	}

	static abstract class WithResidualGraph extends MaximumFlowAbstract {

		static class Worker {
			final IndexGraph gOrig;
			final int source;
			final int sink;
			final int n;
			final FlowNetwork net;

			final IndexGraph g;
			final int[] edgeRef;
			final int[] twin;

			final boolean multiSourceMultiSink;
			final IntCollection sources;
			final IntCollection sinks;

			Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
				Assertions.Flows.sourceSinkNotTheSame(source, sink);
				Assertions.Flows.positiveCapacities(gOrig, net);
				this.gOrig = gOrig;
				this.source = source;
				this.sink = sink;
				this.sources = IntLists.singleton(source);
				this.sinks = IntLists.singleton(sink);
				this.n = gOrig.vertices().size();
				this.net = net;
				multiSourceMultiSink = false;

				FlowNetworks.ResidualGraph.Builder builder = new FlowNetworks.ResidualGraph.Builder(gOrig);
				builder.addAllOriginalEdges();

				FlowNetworks.ResidualGraph residualGraph = builder.build();
				g = residualGraph.g;
				edgeRef = residualGraph.edgeRef;
				twin = residualGraph.twin;
			}

			Worker(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
				Assertions.Flows.sourcesSinksNotTheSame(sources, sinks);
				Assertions.Flows.positiveCapacities(gOrig, net);
				this.gOrig = gOrig;
				this.n = gOrig.vertices().size() + 2;
				this.sources = sources;
				this.sinks = sinks;
				this.net = net;
				multiSourceMultiSink = true;

				FlowNetworks.ResidualGraph.Builder builder = new FlowNetworks.ResidualGraph.Builder(gOrig);
				builder.addAllOriginalEdges();

				source = builder.addVertex();
				sink = builder.addVertex();
				for (int s : sources)
					builder.addEdge(source, s, -1);
				for (int t : sinks)
					builder.addEdge(t, sink, -1);

				FlowNetworks.ResidualGraph residualGraph = builder.build();
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
				if (gOrig.getCapabilities().directed()) {
					for (int m = g.edges().size(), e = 0; e < m; e++) {
						residualCapacity[e] = isOriginalEdge(e) ? net.getCapacity(edgeRef[e]) : 0;
					}
				} else {
					for (int m = g.edges().size(), e = 0; e < m; e++) {
						int eRef = edgeRef[e];
						double cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink)
								? net.getCapacity(eRef)
								: 0;
						residualCapacity[e] = cap;
					}
				}
				if (multiSourceMultiSink) {
					double capacitySum = 0;
					for (int m = gOrig.edges().size(), e = 0; e < m; e++)
						capacitySum += net.getCapacity(e);

					/* init edges from super-source to sources and from sinks to super-sink */
					for (int m = g.edges().size(), e = 0; e < m; e++)
						if (edgeRef[e] == -1)
							residualCapacity[e] =
									source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
				}
			}

			void initCapacities(int[] residualCapacity) {
				FlowNetwork.Int net = (FlowNetwork.Int) this.net;
				if (gOrig.getCapabilities().directed()) {
					for (int m = g.edges().size(), e = 0; e < m; e++) {
						residualCapacity[e] = isOriginalEdge(e) ? net.getCapacityInt(edgeRef[e]) : 0;
					}
				} else {
					for (int m = g.edges().size(), e = 0; e < m; e++) {
						int eRef = edgeRef[e];
						int cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink)
								? net.getCapacityInt(eRef)
								: 0;
						residualCapacity[e] = cap;
					}
				}
				if (multiSourceMultiSink) {
					int capacitySum = 0;
					for (int m = gOrig.edges().size(), e = 0; e < m; e++) {
						int cap = net.getCapacityInt(e);
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

			double constructResult(double[] flow) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					if (isOriginalEdge(e))
						/* The flow of e might be negative if the original graph is undirected, which is fine */
						net.setFlow(edgeRef[e], flow[e]);
				}

				double totalFlow = 0;
				if (gOrig.getCapabilities().directed()) {
					for (int s : sources) {
						for (int e : gOrig.outEdges(s))
							totalFlow += net.getFlow(e);
						for (int e : gOrig.inEdges(s))
							totalFlow -= net.getFlow(e);
					}
				} else {
					for (int s : sources)
						for (int e : g.outEdges(s))
							if (g.edgeTarget(e) != source)
								totalFlow += flow[e];
				}
				return totalFlow;
			}

			int constructResult(int[] flow) {
				FlowNetwork.Int net = (FlowNetwork.Int) this.net;
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					if (isOriginalEdge(e))
						/* The flow of e might be negative if the original graph is undirected, which is fine */
						net.setFlow(edgeRef[e], flow[e]);
				}

				int totalFlow = 0;
				if (gOrig.getCapabilities().directed()) {
					for (int s : sources) {
						for (int e : gOrig.outEdges(s))
							totalFlow += net.getFlowInt(e);
						for (int e : gOrig.inEdges(s))
							totalFlow -= net.getFlowInt(e);
					}
				} else {
					for (int s : sources)
						for (int e : g.outEdges(s))
							if (g.edgeTarget(e) != source)
								totalFlow += flow[e];
				}
				return totalFlow;
			}

			double constructResult(double[] capacity, double[] residualCapacity) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					if (isOriginalEdge(e))
						/* The flow of e might be negative if the original graph is undirected, which is fine */
						net.setFlow(edgeRef[e], capacity[e] - residualCapacity[e]);
				}

				double totalFlow = 0;
				if (gOrig.getCapabilities().directed()) {
					for (int s : sources) {
						for (int e : gOrig.outEdges(s))
							totalFlow += net.getFlow(e);
						for (int e : gOrig.inEdges(s))
							totalFlow -= net.getFlow(e);
					}
				} else {
					for (int s : sources)
						for (int e : g.outEdges(s))
							if (g.edgeTarget(e) != source)
								totalFlow += capacity[e] - residualCapacity[e];
				}
				return totalFlow;
			}

			int constructResult(int[] capacity, int[] residualCapacity) {
				FlowNetwork.Int net = (FlowNetwork.Int) this.net;
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					if (isOriginalEdge(e))
						/* The flow of e might be negative if the original graph is undirected, which is fine */
						net.setFlow(edgeRef[e], capacity[e] - residualCapacity[e]);
				}

				int totalFlow = 0;
				if (gOrig.getCapabilities().directed()) {
					for (int s : sources) {
						for (int e : gOrig.outEdges(s))
							totalFlow += net.getFlowInt(e);
						for (int e : gOrig.inEdges(s))
							totalFlow -= net.getFlowInt(e);
					}
				} else {
					for (int s : sources)
						for (int e : g.outEdges(s))
							if (g.edgeTarget(e) != source)
								totalFlow += capacity[e] - residualCapacity[e];
				}
				return totalFlow;
			}

			boolean isOriginalEdge(int e) {
				int eOrig = edgeRef[e];
				return eOrig != -1 && g.edgeSource(e) == gOrig.edgeSource(eOrig);

			}
		}

	}

}
