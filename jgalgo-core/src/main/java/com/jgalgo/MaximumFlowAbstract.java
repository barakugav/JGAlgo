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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
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
			this.source = n - 2;
			this.sink = n - 1;
			this.sources = sources;
			this.sinks = sinks;
			this.net = net;
			multiSourceMultiSink = true;

			final int gEdgeNumMax = (gOrig.edges().size() + sources.size() + sinks.size()) * 2;
			int[] edgeRefTemp = new int[gEdgeNumMax];
			int[] twinTemp = new int[gEdgeNumMax];

			IndexGraphBuilder gBuilder = IndexGraphBuilder.newDirected();
			addOriginalEdgesToBuilder(gBuilder, gOrig, edgeRefTemp, twinTemp);
			int source0 = gBuilder.addVertex();
			int sink0 = gBuilder.addVertex();
			assert source == source0;
			assert sink == sink0;
			for (int s : sources)
				addEdgeToBuilder(gBuilder, source, s, -1, edgeRefTemp, twinTemp);
			for (int t : sinks)
				addEdgeToBuilder(gBuilder, t, sink, -1, edgeRefTemp, twinTemp);
			IndexGraphBuilder.ReIndexedGraph reindexedGraph = gBuilder.reIndexAndBuild(false, true);
			g = reindexedGraph.graph();
			final int m = g.edges().size();
			edgeRef = new int[m];
			twin = new int[m];
			updateEdgeRefAndTwinFromBuilderReIndexing(reindexedGraph, edgeRefTemp, twinTemp);
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

			final int gEdgeNumMax = gOrig.edges().size() * 2;
			int[] edgeRefTemp = new int[gEdgeNumMax];
			int[] twinTemp = new int[gEdgeNumMax];

			IndexGraphBuilder gBuilder = IndexGraphBuilder.newDirected();
			addOriginalEdgesToBuilder(gBuilder, gOrig, edgeRefTemp, twinTemp);
			IndexGraphBuilder.ReIndexedGraph reindexedGraph = gBuilder.reIndexAndBuild(false, true);
			g = reindexedGraph.graph();
			final int m = g.edges().size();
			edgeRef = new int[m];
			twin = new int[m];
			updateEdgeRefAndTwinFromBuilderReIndexing(reindexedGraph, edgeRefTemp, twinTemp);
		}

		private void addOriginalEdgesToBuilder(IndexGraphBuilder gBuilder, IndexGraph gOrig, int[] edgeRefTemp,
				int[] twinTemp) {
			for (int n = gOrig.vertices().size(), u = 0; u < n; u++) {
				int vBuilder = gBuilder.addVertex();
				assert u == vBuilder;
			}
			if (gOrig.getCapabilities().directed()) {
				for (int m = gOrig.edges().size(), e = 0; e < m; e++) {
					int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
					if (u != v && u != sink && v != source)
						addEdgeToBuilder(gBuilder, u, v, e, edgeRefTemp, twinTemp);
				}
			} else {
				for (int m = gOrig.edges().size(), e = 0; e < m; e++) {
					int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
					if (u != v)
						addEdgeToBuilder(gBuilder, u, v, e, edgeRefTemp, twinTemp);
				}
			}
		}

		private static void addEdgeToBuilder(IndexGraphBuilder gBuilder, int u, int v, int e, int[] edgeRefTemp,
				int[] twinTemp) {
			int e1Builder = gBuilder.addEdge(u, v);
			int e2Builder = gBuilder.addEdge(v, u);
			edgeRefTemp[e1Builder] = e;
			edgeRefTemp[e2Builder] = e;
			twinTemp[e1Builder] = e2Builder;
			twinTemp[e2Builder] = e1Builder;
		}

		private void updateEdgeRefAndTwinFromBuilderReIndexing(IndexGraphBuilder.ReIndexedGraph reindexedGraph,
				int[] edgeRefTemp, int[] twinTemp) {
			final int m = g.edges().size();
			if (reindexedGraph.edgesReIndexing().isPresent()) {
				IndexGraphBuilder.ReIndexingMap eIdxMap = reindexedGraph.edgesReIndexing().get();
				for (int eBuilder = 0; eBuilder < m; eBuilder++) {
					edgeRef[eBuilder] = edgeRefTemp[eIdxMap.reIndexedToOrig(eBuilder)];
					twin[eBuilder] = eIdxMap.origToReIndexed(twinTemp[eIdxMap.reIndexedToOrig(eBuilder)]);
				}
			} else {
				for (int eBuilder = 0; eBuilder < m; eBuilder++) {
					edgeRef[eBuilder] = edgeRefTemp[eBuilder];
					twin[eBuilder] = twinTemp[eBuilder];
				}
			}
		}

		void initCapacitiesAndFlows(double[] flow, double[] capacity) {
			if (gOrig.getCapabilities().directed()) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					capacity[e] = isOriginalEdge(e) ? net.getCapacity(edgeRef[e]) : 0;
					flow[e] = 0;
				}
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int eRef = edgeRef[e];
					double cap =
							(eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? net.getCapacity(eRef)
									: 0;
					capacity[e] = cap;
					flow[e] = 0;
				}
			}
			if (multiSourceMultiSink) {
				double capacitySum = 0;
				for (int m = gOrig.edges().size(), e = 0; e < m; e++)
					capacitySum += net.getCapacity(e);

				/* init edges from super-source to sources and from sinks to super-sink */
				for (int m = g.edges().size(), e = 0; e < m; e++)
					if (edgeRef[e] == -1)
						capacity[e] = source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
			}
		}

		void initCapacitiesAndFlows(int[] flow, int[] capacity) {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			if (gOrig.getCapabilities().directed()) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					capacity[e] = isOriginalEdge(e) ? net.getCapacityInt(edgeRef[e]) : 0;
					flow[e] = 0;
				}
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int eRef = edgeRef[e];
					int cap = (eRef != -1 && g.edgeTarget(e) != source && g.edgeSource(e) != sink)
							? net.getCapacityInt(eRef)
							: 0;
					capacity[e] = cap;
					flow[e] = 0;
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
						capacity[e] = source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
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
