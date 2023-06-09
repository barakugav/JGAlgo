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
			boolean mappedCapacity = net0.capacityWeights instanceof WeightsImpl.Mapped.Double;
			boolean mappedFlow = net0.flowWeights instanceof WeightsImpl.Mapped.Double;
			if (mappedCapacity && mappedFlow) {
				/* The network is a composition of edge weights */
				WeightsImpl.Mapped.Double capacityWeightsMapped = ((WeightsImpl.Mapped.Double) net0.capacityWeights);
				WeightsImpl.Mapped.Double flowWeightsMapped = ((WeightsImpl.Mapped.Double) net0.flowWeights);

				/* The weights are a mapped wrappers to index weights containers */
				/* Get the underlying index weights containers */
				Weights.Double capacityWeights = capacityWeightsMapped.weights();
				Weights.Double flowWeights = flowWeightsMapped.weights();

				/* Create a network from the underlying index weights containers */
				return new EdgeWeightsFlowNetwork(capacityWeights, flowWeights);
			}

		} else if (net instanceof EdgeWeightsFlowNetworkInt) {
			EdgeWeightsFlowNetworkInt net0 = (EdgeWeightsFlowNetworkInt) net;
			boolean mappedCapacity = net0.capacityWeights instanceof WeightsImpl.Mapped.Int;
			boolean mappedFlow = net0.flowWeights instanceof WeightsImpl.Mapped.Int;
			if (mappedCapacity && mappedFlow) {
				/* The network is a composition of edge weights */
				WeightsImpl.Mapped.Int capacityWeightsMapped = ((WeightsImpl.Mapped.Int) net0.capacityWeights);
				WeightsImpl.Mapped.Int flowWeightsMapped = ((WeightsImpl.Mapped.Int) net0.flowWeights);

				/* The weights are a mapped wrappers to index weights containers */
				/* Get the underlying index weights containers */
				Weights.Int capacityWeights = capacityWeightsMapped.weights();
				Weights.Int flowWeights = flowWeightsMapped.weights();

				/* Create a network from the underlying index weights containers */
				return new EdgeWeightsFlowNetworkInt(capacityWeights, flowWeights);
			}
		}

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

	static class Worker {
		final IndexGraph gOrig;
		final int source;
		final int sink;
		final int n;
		final FlowNetwork net;

		final IndexGraph g;
		final Weights.Int edgeRef;
		final Weights.Int twin;

		static final Object VertexRefWeightKey = new Utils.Obj("refToOrig");
		static final Object EdgeRefWeightKey = new Utils.Obj("refToOrig");
		static final Object EdgeTwinWeightKey = new Utils.Obj("twin");

		Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			ArgumentCheck.sourceSinkNotTheSame(source, sink);
			positiveCapacitiesOrThrow(gOrig, net);
			this.gOrig = gOrig;
			this.source = source;
			this.sink = sink;
			this.n = gOrig.vertices().size();
			this.net = net;

			g = IndexGraph.newBuilderDirected().expectedVerticesNum(n).build();
			for (int v = 0; v < n; v++)
				g.addVertex();
			edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
			twin = g.addEdgesWeights(EdgeTwinWeightKey, int.class, Integer.valueOf(-1));

			boolean directed = gOrig.getCapabilities().directed();
			for (int e : gOrig.edges()) {
				int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (u == v)
					continue;
				if (directed && (u == sink || v == source))
					continue;
				int e1 = g.addEdge(u, v);
				int e2 = g.addEdge(v, u);
				edgeRef.set(e1, e);
				edgeRef.set(e2, e);
				twin.set(e1, e2);
				twin.set(e2, e1);
			}
		}

		void initCapacitiesAndFlows(Weights.Double flow, Weights.Double capacity) {
			if (gOrig.getCapabilities().directed()) {
				for (int e : g.edges()) {
					capacity.set(e, isOriginalEdge(e) ? net.getCapacity(edgeRef.getInt(e)) : 0);
					flow.set(e, 0);
				}
			} else {
				for (int e : g.edges()) {
					double cap =
							(g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? net.getCapacity(edgeRef.getInt(e))
									: 0;
					capacity.set(e, cap);
					flow.set(e, 0);
				}
			}
		}

		void initCapacitiesAndFlows(Weights.Int flow, Weights.Int capacity) {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			if (gOrig.getCapabilities().directed()) {
				for (int e : g.edges()) {
					capacity.set(e, isOriginalEdge(e) ? net.getCapacityInt(edgeRef.getInt(e)) : 0);
					flow.set(e, 0);
				}
			} else {
				for (int e : g.edges()) {
					int cap = (g.edgeTarget(e) != source && g.edgeSource(e) != sink)
							? net.getCapacityInt(edgeRef.getInt(e))
							: 0;
					capacity.set(e, cap);
					flow.set(e, 0);
				}
			}
		}

		double constructResult(Weights.Double flow) {
			for (int e : g.edges()) {
				if (isOriginalEdge(e))
					/* The flow of e might be negative if the original graph is undirected, which is fine */
					net.setFlow(edgeRef.getInt(e), flow.getDouble(e));
			}

			double totalFlow = 0;
			if (gOrig.getCapabilities().directed()) {
				for (int e : gOrig.outEdges(source))
					totalFlow += net.getFlow(e);
				for (int e : gOrig.inEdges(source))
					totalFlow -= net.getFlow(e);
			} else {
				for (int e : g.outEdges(source))
					totalFlow += flow.getDouble(e);
			}
			return totalFlow;
		}

		int constructResult(Weights.Int flow) {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			for (int e : g.edges()) {
				if (isOriginalEdge(e))
					/* The flow of e might be negative if the original graph is undirected, which is fine */
					net.setFlow(edgeRef.getInt(e), flow.getInt(e));
			}

			int totalFlow = 0;
			if (gOrig.getCapabilities().directed()) {
				for (int e : gOrig.outEdges(source))
					totalFlow += net.getFlowInt(e);
				for (int e : gOrig.inEdges(source))
					totalFlow -= net.getFlowInt(e);
			} else {
				for (int e : g.outEdges(source))
					totalFlow += flow.getInt(e);
			}
			return totalFlow;
		}

		boolean isOriginalEdge(int e) {
			return g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e));
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
