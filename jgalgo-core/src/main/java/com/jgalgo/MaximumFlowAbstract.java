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

import java.util.Objects;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilderFixedRemapped;
import com.jgalgo.graph.GraphCSRRemappedDirected;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.Weights;

abstract class MaximumFlowAbstract implements MaximumFlow {

	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		if (g instanceof IndexGraph)
			return computeMaximumFlow((IndexGraph) g, net, source, sink);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		net = indexFlowFromFlow(net, eiMap);
		int iSource = viMap.idToIndex(source);
		int iSink = viMap.idToIndex(sink);
		return computeMaximumFlow(iGraph, net, iSource, iSink);
	}

	abstract double computeMaximumFlow(IndexGraph g, FlowNetwork net, int source, int sink);

	static class EdgeWeightsFlowNetwork implements FlowNetwork {

		private final Weights.Double capacityWeights;
		private final Weights.Double flowWeights;
		private static final double EPS = 0.0001;

		EdgeWeightsFlowNetwork(Weights.Double capacityWeights, Weights.Double flowWeights) {
			this.capacityWeights = Objects.requireNonNull(capacityWeights);
			this.flowWeights = Objects.requireNonNull(flowWeights);
		}

		static EdgeWeightsFlowNetwork newInstance(Graph g) {
			Weights.Double capacityWeights = g.addEdgesWeights(new Utils.Obj("capacity"), double.class);
			Weights.Double flowWeights = g.addEdgesWeights(new Utils.Obj("flow"), double.class);
			return new EdgeWeightsFlowNetwork(capacityWeights, flowWeights);
		}

		@Override
		public double getCapacity(int edge) {
			return capacityWeights.getDouble(edge);
		}

		@Override
		public void setCapacity(int edge, double capacity) {
			if (capacity < 0)
				throw new IllegalArgumentException("capacity can't be negative");
			capacityWeights.set(edge, capacity);
		}

		@Override
		public double getFlow(int edge) {
			return flowWeights.getDouble(edge);
		}

		@Override
		public void setFlow(int edge, double flow) {
			double capacity = getCapacity(edge);
			if (flow > capacity + EPS)
				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
			flowWeights.set(edge, Math.min(flow, capacity));
		}

	}

	static class EdgeWeightsFlowNetworkInt implements FlowNetwork.Int {

		private final Weights.Int capacityWeights;
		private final Weights.Int flowWeights;

		EdgeWeightsFlowNetworkInt(Weights.Int capacityWeights, Weights.Int flowWeights) {
			this.capacityWeights = Objects.requireNonNull(capacityWeights);
			this.flowWeights = Objects.requireNonNull(flowWeights);
		}

		static EdgeWeightsFlowNetworkInt newInstance(Graph g) {
			Weights.Int capacityWeights = g.addEdgesWeights(new Utils.Obj("capacity"), int.class);
			Weights.Int flowWeights = g.addEdgesWeights(new Utils.Obj("flow"), int.class);
			return new EdgeWeightsFlowNetworkInt(capacityWeights, flowWeights);
		}

		@Override
		public int getCapacityInt(int edge) {
			return capacityWeights.getInt(edge);
		}

		@Override
		public void setCapacity(int edge, int capacity) {
			if (capacity < 0)
				throw new IllegalArgumentException("capacity can't be negative");
			capacityWeights.set(edge, capacity);
		}

		@Override
		public int getFlowInt(int edge) {
			return flowWeights.getInt(edge);
		}

		@Override
		public void setFlow(int edge, int flow) {
			int capacity = getCapacityInt(edge);
			if (flow > capacity)
				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
			flowWeights.set(edge, Math.min(flow, capacity));
		}

	}

	private static FlowNetwork indexFlowFromFlow(FlowNetwork net, IndexIdMap eiMap) {
		if (net instanceof EdgeWeightsFlowNetwork) {
			EdgeWeightsFlowNetwork net0 = (EdgeWeightsFlowNetwork) net;

			/* Create a network from the underlying index weights containers */
			Weights.Double capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacityWeights, eiMap);
			Weights.Double flowWeights = IndexIdMaps.idToIndexWeights(net0.flowWeights, eiMap);
			return new EdgeWeightsFlowNetwork(capacityWeights, flowWeights);

		} else if (net instanceof EdgeWeightsFlowNetworkInt) {
			EdgeWeightsFlowNetworkInt net0 = (EdgeWeightsFlowNetworkInt) net;

			/* Create a network from the underlying index weights containers */
			Weights.Int capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacityWeights, eiMap);
			Weights.Int flowWeights = IndexIdMaps.idToIndexWeights(net0.flowWeights, eiMap);
			return new EdgeWeightsFlowNetworkInt(capacityWeights, flowWeights);
		} else {

			/* Unknown network implementation, create a mapped wrapper */
			if (net instanceof FlowNetwork.Int) {
				FlowNetwork.Int netInt = (FlowNetwork.Int) net;
				return new FlowNetwork.Int() {

					@Override
					public int getCapacityInt(int edge) {
						return netInt.getCapacityInt(eiMap.indexToId(edge));
					}

					@Override
					public void setCapacity(int edge, int capacity) {
						netInt.setCapacity(eiMap.indexToId(edge), capacity);
					}

					@Override
					public int getFlowInt(int edge) {
						return netInt.getFlowInt(eiMap.indexToId(edge));
					}

					@Override
					public void setFlow(int edge, int flow) {
						netInt.setFlow(eiMap.indexToId(edge), flow);
					}

				};
			} else {
				return new FlowNetwork() {

					@Override
					public double getCapacity(int edge) {
						return net.getCapacity(eiMap.indexToId(edge));
					}

					@Override
					public void setCapacity(int edge, double capacity) {
						net.setCapacity(eiMap.indexToId(edge), capacity);
					}

					@Override
					public double getFlow(int edge) {
						return net.getFlow(eiMap.indexToId(edge));
					}

					@Override
					public void setFlow(int edge, double flow) {
						net.setFlow(eiMap.indexToId(edge), flow);
					}
				};
			}
		}
	}

	static class Worker {
		final IndexGraph gOrig;
		final int source;
		final int sink;
		final int n;
		final FlowNetwork net;

		final IndexGraph g;
		final int[] edgeRef;
		final int[] twin;

		Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			ArgumentCheck.sourceSinkNotTheSame(source, sink);
			positiveCapacitiesOrThrow(gOrig, net);
			this.gOrig = gOrig;
			this.source = source;
			this.sink = sink;
			this.n = gOrig.vertices().size();
			this.net = net;

			final int mOrig = gOrig.edges().size();
			int[] edgeRefTemp = new int[mOrig * 2];
			int[] twinTemp = new int[mOrig * 2];

			GraphCSRRemappedDirected.Builder gBuilder = new GraphCSRRemappedDirected.Builder();
			if (gOrig.getCapabilities().directed()) {
				for (int u = 0; u < n; u++) {
					int vCsr = gBuilder.addVertex();
					assert u == vCsr;
					for (EdgeIter eit = gOrig.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.target();
						if (u == v)
							continue;
						if (u == sink || v == source)
							continue;
						int e1Csr = gBuilder.addEdge(u, v);
						int e2Csr = gBuilder.addEdge(v, u);
						edgeRefTemp[e1Csr] = e;
						edgeRefTemp[e2Csr] = e;
						twinTemp[e1Csr] = e2Csr;
						twinTemp[e2Csr] = e1Csr;
					}
				}
			} else {
				for (int u = 0; u < n; u++) {
					int vCsr = gBuilder.addVertex();
					assert u == vCsr;
					for (EdgeIter eit = gOrig.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						if (gOrig.edgeSource(e) != u)
							continue; // each edge will appear twice
						int v = eit.target();
						if (u == v)
							continue;
						int e1Csr = gBuilder.addEdge(u, v);
						int e2Csr = gBuilder.addEdge(v, u);
						edgeRefTemp[e1Csr] = e;
						edgeRefTemp[e2Csr] = e;
						twinTemp[e1Csr] = e2Csr;
						twinTemp[e2Csr] = e1Csr;
					}
				}
			}
			GraphBuilderFixedRemapped.BuilderResult csrRes = gBuilder.build();
			g = csrRes.graph;
			final int m = g.edges().size();
			edgeRef = new int[m];
			twin = new int[m];
			for (int eCsr = 0; eCsr < m; eCsr++) {
				edgeRef[eCsr] = edgeRefTemp[csrRes.edgesFixedToInsertIdx[eCsr]];
				twin[eCsr] = csrRes.edgesInsertIdxToFixed[twinTemp[csrRes.edgesFixedToInsertIdx[eCsr]]];
			}
		}

		void initCapacitiesAndFlows(double[] flow, double[] capacity) {
			if (gOrig.getCapabilities().directed()) {
				for (int e : g.edges()) {
					capacity[e] = isOriginalEdge(e) ? net.getCapacity(edgeRef[e]) : 0;
					flow[e] = 0;
				}
			} else {
				for (int e : g.edges()) {
					double cap =
							(g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? net.getCapacity(edgeRef[e]) : 0;
					capacity[e] = cap;
					flow[e] = 0;
				}
			}
		}

		void initCapacitiesAndFlows(int[] flow, int[] capacity) {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			if (gOrig.getCapabilities().directed()) {
				for (int e : g.edges()) {
					capacity[e] = isOriginalEdge(e) ? net.getCapacityInt(edgeRef[e]) : 0;
					flow[e] = 0;
				}
			} else {
				for (int e : g.edges()) {
					int cap =
							(g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? net.getCapacityInt(edgeRef[e]) : 0;
					capacity[e] = cap;
					flow[e] = 0;
				}
			}
		}

		double constructResult(double[] flow) {
			for (int e : g.edges()) {
				if (isOriginalEdge(e))
					/* The flow of e might be negative if the original graph is undirected, which is fine */
					net.setFlow(edgeRef[e], flow[e]);
			}

			double totalFlow = 0;
			if (gOrig.getCapabilities().directed()) {
				for (int e : gOrig.outEdges(source))
					totalFlow += net.getFlow(e);
				for (int e : gOrig.inEdges(source))
					totalFlow -= net.getFlow(e);
			} else {
				for (int e : g.outEdges(source))
					totalFlow += flow[e];
			}
			return totalFlow;
		}

		int constructResult(int[] flow) {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			for (int e : g.edges()) {
				if (isOriginalEdge(e))
					/* The flow of e might be negative if the original graph is undirected, which is fine */
					net.setFlow(edgeRef[e], flow[e]);
			}

			int totalFlow = 0;
			if (gOrig.getCapabilities().directed()) {
				for (int e : gOrig.outEdges(source))
					totalFlow += net.getFlowInt(e);
				for (int e : gOrig.inEdges(source))
					totalFlow -= net.getFlowInt(e);
			} else {
				for (int e : g.outEdges(source))
					totalFlow += flow[e];
			}
			return totalFlow;
		}

		boolean isOriginalEdge(int e) {
			return g.edgeSource(e) == gOrig.edgeSource(edgeRef[e]);
		}

		private static void positiveCapacitiesOrThrow(Graph g, FlowNetwork net) {
			if (net instanceof FlowNetwork.Int) {
				FlowNetwork.Int netInt = (FlowNetwork.Int) net;
				for (int e : g.edges()) {
					int cap = netInt.getCapacityInt(e);
					if (cap < 0)
						throw new IllegalArgumentException("negative capacity of edge (" + e + "): " + cap);
				}
			} else {
				for (int e : g.edges()) {
					double cap = net.getCapacity(e);
					if (cap < 0)
						throw new IllegalArgumentException("negative capacity of edge (" + e + "): " + cap);
				}
			}
		}

	}
}
