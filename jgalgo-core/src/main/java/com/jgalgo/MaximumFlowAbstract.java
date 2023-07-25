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
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;

abstract class MaximumFlowAbstract implements MaximumFlow {

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

		Worker(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
			Assertions.Graphs.sourcesSinksNotTheSame(sources, sinks);
			positiveCapacitiesOrThrow(gOrig, net);
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

		Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			Assertions.Graphs.sourceSinkNotTheSame(source, sink);
			positiveCapacitiesOrThrow(gOrig, net);
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
					double cap =
							(eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? net.getCapacity(eRef)
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
						residualCapacity[e] = source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
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
						residualCapacity[e] = source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
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

		private static void positiveCapacitiesOrThrow(Graph g, FlowNetwork net) {
			if (net instanceof FlowNetwork.Int) {
				FlowNetwork.Int netInt = (FlowNetwork.Int) net;
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int cap = netInt.getCapacityInt(e);
					if (cap < 0)
						throw new IllegalArgumentException("negative capacity of edge (" + e + "): " + cap);
				}
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					double cap = net.getCapacity(e);
					if (cap < 0)
						throw new IllegalArgumentException("negative capacity of edge (" + e + "): " + cap);
				}
			}
		}

	}
}
