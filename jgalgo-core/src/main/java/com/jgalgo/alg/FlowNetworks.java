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

import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;

class FlowNetworks {

	static class NetImplEdgeWeights implements FlowNetwork {

		final WeightsDouble capacities;
		final WeightsDouble flows;
		static final double EPS = 0.0001;

		NetImplEdgeWeights(WeightsDouble capacities, WeightsDouble flows) {
			this.capacities = Objects.requireNonNull(capacities);
			this.flows = Objects.requireNonNull(flows);
		}

		@Override
		public double getCapacity(int edge) {
			return capacities.get(edge);
		}

		@Override
		public void setCapacity(int edge, double capacity) {
			if (capacity < 0)
				throw new IllegalArgumentException("capacity can't be negative");
			capacities.set(edge, capacity);
		}

		@Override
		public double getFlow(int edge) {
			return flows.get(edge);
		}

		@Override
		public void setFlow(int edge, double flow) {
			double capacity = getCapacity(edge);
			if (flow > capacity + EPS)
				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
			flows.set(edge, Math.min(flow, capacity));
		}

	}

	static class NetImplEdgeWeightsInt implements FlowNetworkInt {

		final WeightsInt capacities;
		final WeightsInt flows;

		NetImplEdgeWeightsInt(WeightsInt capacities, WeightsInt flows) {
			this.capacities = Objects.requireNonNull(capacities);
			this.flows = Objects.requireNonNull(flows);
		}

		static FlowNetworkInt addWeightsAndCreateNet(Graph g) {
			WeightsInt capacities = g.addEdgesWeights(JGAlgoUtils.labeledObj("capacity"), int.class);
			WeightsInt flows = g.addEdgesWeights(JGAlgoUtils.labeledObj("flow"), int.class);
			return new NetImplEdgeWeightsInt(capacities, flows);
		}

		static FlowNetworkInt createExternalWeightsAndCreateNet(Graph g) {
			WeightsInt capacities = Weights.createExternalEdgesWeights(g, int.class);
			WeightsInt flows = Weights.createExternalEdgesWeights(g, int.class);
			return new NetImplEdgeWeightsInt(capacities, flows);
		}

		@Override
		public int getCapacityInt(int edge) {
			return capacities.get(edge);
		}

		@Override
		public void setCapacity(int edge, int capacity) {
			if (capacity < 0)
				throw new IllegalArgumentException("capacity can't be negative");
			capacities.set(edge, capacity);
		}

		@Override
		public int getFlowInt(int edge) {
			return flows.get(edge);
		}

		@Override
		public void setFlow(int edge, int flow) {
			int capacity = getCapacityInt(edge);
			if (flow > capacity)
				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
			flows.set(edge, Math.min(flow, capacity));
		}

	}

	static class ResidualGraph {
		final IndexGraph gOrig;
		final IndexGraph g;
		final int[] edgeRef;
		final int[] twin;

		ResidualGraph(IndexGraph gOrig, IndexGraph g, int[] edgeRef, int[] twin) {
			this.gOrig = gOrig;
			this.g = g;
			this.edgeRef = edgeRef;
			this.twin = twin;
		}

		boolean isOriginalEdge(int e) {
			int eOrig = edgeRef[e];
			return eOrig != -1 && g.edgeSource(e) == gOrig.edgeSource(eOrig);
		}

		static class Builder {

			private final IndexGraphBuilder gBuilder;
			private final IndexGraph gOrig;
			private final IntArrayList edgeRef;
			private final IntArrayList twin;

			Builder(IndexGraph gOrig) {
				this.gOrig = Objects.requireNonNull(gOrig);
				gBuilder = IndexGraphBuilder.newDirected();
				edgeRef = new IntArrayList(gOrig.edges().size() * 2);
				twin = new IntArrayList(gOrig.edges().size() * 2);
			}

			void addAllOriginalEdges() {
				gBuilder.expectedVerticesNum(gOrig.vertices().size());
				for (int n = gOrig.vertices().size(), u = 0; u < n; u++) {
					int vBuilder = gBuilder.addVertex();
					assert u == vBuilder;
				}

				gBuilder.expectedEdgesNum(gOrig.edges().size() * 2);
				for (int m = gOrig.edges().size(), e = 0; e < m; e++) {
					int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
					if (u != v)
						addEdge(u, v, e);
				}
			}

			int addVertex() {
				return gBuilder.addVertex();
			}

			void addEdge(int u, int v, int e) {
				int e1Builder = gBuilder.addEdge(u, v);
				int e2Builder = gBuilder.addEdge(v, u);
				assert e1Builder == edgeRef.size();
				edgeRef.add(e);
				assert e2Builder == edgeRef.size();
				edgeRef.add(e);
				assert e1Builder == twin.size();
				twin.add(e2Builder);
				assert e2Builder == twin.size();
				twin.add(e1Builder);
			}

			ResidualGraph build() {
				IndexGraphBuilder.ReIndexedGraph reindexedGraph = gBuilder.reIndexAndBuild(false, true);
				IndexGraph g = reindexedGraph.graph();
				final int m = g.edges().size();
				int[] edgeRefTemp = edgeRef.elements();
				int[] twinTemp = twin.elements();
				int[] edgeRef = new int[m];
				int[] twin = new int[m];
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
				return new ResidualGraph(gOrig, g, edgeRef, twin);
			}

		}

	}

	private static class IndexNetFromNet implements FlowNetwork {

		private final FlowNetwork idNet;
		final IndexIdMap eiMap;

		IndexNetFromNet(FlowNetwork idNet, IndexIdMap eiMap) {
			if (idNet instanceof NetImplEdgeWeights || idNet instanceof NetImplEdgeWeightsInt)
				throw new IllegalArgumentException("net is already an index flow network");
			this.idNet = Objects.requireNonNull(idNet);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		FlowNetwork idNet() {
			return idNet;
		}

		@Override
		public double getCapacity(int edge) {
			return idNet.getCapacity(eiMap.indexToId(edge));
		}

		@Override
		public void setCapacity(int edge, double capacity) {
			idNet.setCapacity(eiMap.indexToId(edge), capacity);
		}

		@Override
		public double getFlow(int edge) {
			return idNet.getFlow(eiMap.indexToId(edge));
		}

		@Override
		public void setFlow(int edge, double flow) {
			idNet.setFlow(eiMap.indexToId(edge), flow);
		}

	}

	private static class IndexNetFromNetInt extends IndexNetFromNet implements FlowNetworkInt {

		IndexNetFromNetInt(FlowNetworkInt idNet, IndexIdMap eiMap) {
			super(idNet, eiMap);
		}

		@Override
		FlowNetworkInt idNet() {
			return (FlowNetworkInt) super.idNet();
		}

		@Override
		public int getCapacityInt(int edge) {
			return idNet().getCapacityInt(eiMap.indexToId(edge));
		}

		@Override
		public void setCapacity(int edge, int capacity) {
			idNet().setCapacity(eiMap.indexToId(edge), capacity);
		}

		@Override
		public int getFlowInt(int edge) {
			return idNet().getFlowInt(eiMap.indexToId(edge));
		}

		@Override
		public void setFlow(int edge, int flow) {
			idNet().setFlow(eiMap.indexToId(edge), flow);
		}

		@Deprecated
		@Override
		public double getCapacity(int edge) {
			return FlowNetworkInt.super.getCapacity(edge);
		}

		@Deprecated
		@Override
		public void setCapacity(int edge, double capacity) {
			FlowNetworkInt.super.setCapacity(edge, capacity);
		}

		@Deprecated
		@Override
		public double getFlow(int edge) {
			return FlowNetworkInt.super.getFlow(edge);
		}

		@Deprecated
		@Override
		public void setFlow(int edge, double flow) {
			FlowNetworkInt.super.setFlow(edge, flow);
		}
	}

	static FlowNetworkInt indexNetFromNet(FlowNetworkInt net, IndexIdMap eiMap) {
		if (net instanceof NetImplEdgeWeightsInt) {
			NetImplEdgeWeightsInt net0 = (NetImplEdgeWeightsInt) net;

			/* Create a network from the underlying index weights containers */
			WeightsInt capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacities, eiMap);
			WeightsInt flowWeights = IndexIdMaps.idToIndexWeights(net0.flows, eiMap);
			return new NetImplEdgeWeightsInt(capacityWeights, flowWeights);
		} else {

			/* Unknown network implementation, create a mapped wrapper */
			return new IndexNetFromNetInt(net, eiMap);
		}
	}

	static FlowNetwork indexNetFromNet(FlowNetwork net, IndexIdMap eiMap) {
		if (net instanceof FlowNetworkInt)
			return indexNetFromNet((FlowNetworkInt) net, eiMap);
		if (net instanceof NetImplEdgeWeights) {
			NetImplEdgeWeights net0 = (NetImplEdgeWeights) net;

			/* Create a network from the underlying index weights containers */
			WeightsDouble capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacities, eiMap);
			WeightsDouble flowWeights = IndexIdMaps.idToIndexWeights(net0.flows, eiMap);
			return new NetImplEdgeWeights(capacityWeights, flowWeights);

		} else {

			/* Unknown network implementation, create a mapped wrapper */
			return new IndexNetFromNet(net, eiMap);
		}
	}

	static double hugeCapacity(IndexGraph g, FlowNetwork net, IntCollection sources, IntCollection sinks) {
		if (net instanceof FlowNetworkInt)
			return hugeCapacityLong(g, (FlowNetworkInt) net, sources, sinks);

		double sourcesOutCapacity = 0;
		double sinksOutCapacity = 0;
		for (int s : sources)
			for (int e : g.outEdges(s))
				sourcesOutCapacity += net.getCapacity(e);
		for (int s : sinks)
			for (int e : g.inEdges(s))
				sinksOutCapacity += net.getCapacity(e);
		return Math.max(sourcesOutCapacity, sinksOutCapacity) + 1;
	}

	static int hugeCapacity(IndexGraph g, FlowNetworkInt net, IntCollection sources, IntCollection sinks) {
		long hugeCapacity = hugeCapacityLong(g, net, sources, sinks);
		int hugeCapacityInt = (int) hugeCapacity;
		if (hugeCapacityInt != hugeCapacity)
			throw new AssertionError("integer overflow, huge capacity can't fit in 32bit int");
		return hugeCapacityInt;
	}

	static long hugeCapacityLong(IndexGraph g, FlowNetworkInt net, IntCollection sources, IntCollection sinks) {
		long sourcesOutCapacity = 0;
		long sinksOutCapacity = 0;
		for (int s : sources)
			for (int e : g.outEdges(s))
				sourcesOutCapacity += net.getCapacityInt(e);
		for (int s : sinks)
			for (int e : g.inEdges(s))
				sinksOutCapacity += net.getCapacityInt(e);
		return Math.max(sourcesOutCapacity, sinksOutCapacity) + 1;
	}

	static double vertexMaxSupply(IndexGraph g, FlowNetwork net, int v) {
		if (net instanceof FlowNetworkInt)
			return vertexMaxSupply(g, (FlowNetworkInt) net, v);

		double maxSupply = 0;
		for (int e : g.outEdges(v))
			maxSupply += net.getCapacity(e);
		return maxSupply;
	}

	static int vertexMaxSupply(IndexGraph g, FlowNetworkInt net, int v) {
		long maxSupply = 0;
		for (int e : g.outEdges(v))
			maxSupply += net.getCapacityInt(e);
		int maxSupplyInt = (int) maxSupply;
		if (maxSupplyInt != maxSupply)
			throw new AssertionError("integer overflow, vertex max supply can't fit in 32bit int");
		return maxSupplyInt;
	}

	static double vertexMaxDemand(IndexGraph g, FlowNetwork net, int v) {
		if (net instanceof FlowNetworkInt)
			return vertexMaxDemand(g, (FlowNetworkInt) net, v);

		double maxDemand = 0;
		for (int e : g.inEdges(v))
			maxDemand += net.getCapacity(e);
		return maxDemand;
	}

	static int vertexMaxDemand(IndexGraph g, FlowNetworkInt net, int v) {
		long maxDemand = 0;
		for (int e : g.inEdges(v))
			maxDemand += net.getCapacityInt(e);
		int maxDemandInt = (int) maxDemand;
		if (maxDemandInt != maxDemand)
			throw new AssertionError("integer overflow, vertex max supply can't fit in 32bit int");
		return maxDemandInt;
	}

}
