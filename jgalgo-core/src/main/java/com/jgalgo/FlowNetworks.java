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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.JGAlgoUtils;

class FlowNetworks {

	static class NetImplEdgeWeights implements FlowNetwork {

		final Weights.Double capacities;
		final Weights.Double flows;
		static final double EPS = 0.0001;

		NetImplEdgeWeights(Weights.Double capacities, Weights.Double flows) {
			this.capacities = Objects.requireNonNull(capacities);
			this.flows = Objects.requireNonNull(flows);
		}

		static NetImplEdgeWeights newInstance(Graph g) {
			Weights.Double capacities = g.addEdgesWeights(JGAlgoUtils.labeledObj("capacity"), double.class);
			Weights.Double flows = g.addEdgesWeights(JGAlgoUtils.labeledObj("flow"), double.class);
			return new NetImplEdgeWeights(capacities, flows);
		}

		@Override
		public double getCapacity(int edge) {
			return capacities.getDouble(edge);
		}

		@Override
		public void setCapacity(int edge, double capacity) {
			if (capacity < 0)
				throw new IllegalArgumentException("capacity can't be negative");
			capacities.set(edge, capacity);
		}

		@Override
		public double getFlow(int edge) {
			return flows.getDouble(edge);
		}

		@Override
		public void setFlow(int edge, double flow) {
			double capacity = getCapacity(edge);
			if (flow > capacity + EPS)
				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
			flows.set(edge, Math.min(flow, capacity));
		}

	}

	static class NetImplEdgeWeightsInt implements FlowNetwork.Int {

		final Weights.Int capacities;
		final Weights.Int flows;

		NetImplEdgeWeightsInt(Weights.Int capacities, Weights.Int flows) {
			this.capacities = Objects.requireNonNull(capacities);
			this.flows = Objects.requireNonNull(flows);
		}

		static NetImplEdgeWeightsInt newInstance(Graph g) {
			Weights.Int capacities = g.addEdgesWeights(JGAlgoUtils.labeledObj("capacity"), int.class);
			Weights.Int flows = g.addEdgesWeights(JGAlgoUtils.labeledObj("flow"), int.class);
			return new NetImplEdgeWeightsInt(capacities, flows);
		}

		@Override
		public int getCapacityInt(int edge) {
			return capacities.getInt(edge);
		}

		@Override
		public void setCapacity(int edge, int capacity) {
			if (capacity < 0)
				throw new IllegalArgumentException("capacity can't be negative");
			capacities.set(edge, capacity);
		}

		@Override
		public int getFlowInt(int edge) {
			return flows.getInt(edge);
		}

		@Override
		public void setFlow(int edge, int flow) {
			int capacity = getCapacityInt(edge);
			if (flow > capacity)
				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
			flows.set(edge, Math.min(flow, capacity));
		}

	}

	static class IndexNetFromNet implements FlowNetwork {

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

	static class IndexNetFromNetInt implements FlowNetwork.Int {

		private final FlowNetwork.Int idNet;
		final IndexIdMap eiMap;

		IndexNetFromNetInt(FlowNetwork.Int idNet, IndexIdMap eiMap) {
			if (idNet instanceof NetImplEdgeWeightsInt)
				throw new IllegalArgumentException("net is already an index flow network");
			this.idNet = Objects.requireNonNull(idNet);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		FlowNetwork.Int idNet() {
			return idNet;
		}

		@Override
		public int getCapacityInt(int edge) {
			return idNet.getCapacityInt(eiMap.indexToId(edge));
		}

		@Override
		public void setCapacity(int edge, int capacity) {
			idNet.setCapacity(eiMap.indexToId(edge), capacity);
		}

		@Override
		public int getFlowInt(int edge) {
			return idNet.getFlowInt(eiMap.indexToId(edge));
		}

		@Override
		public void setFlow(int edge, int flow) {
			idNet.setFlow(eiMap.indexToId(edge), flow);
		}
	}

	static FlowNetwork indexNetFromNet(FlowNetwork net, IndexIdMap eiMap) {
		if (net instanceof NetImplEdgeWeights) {
			NetImplEdgeWeights net0 = (NetImplEdgeWeights) net;

			/* Create a network from the underlying index weights containers */
			Weights.Double capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacities, eiMap);
			Weights.Double flowWeights = IndexIdMaps.idToIndexWeights(net0.flows, eiMap);
			return new NetImplEdgeWeights(capacityWeights, flowWeights);

		} else if (net instanceof NetImplEdgeWeightsInt) {
			NetImplEdgeWeightsInt net0 = (NetImplEdgeWeightsInt) net;

			/* Create a network from the underlying index weights containers */
			Weights.Int capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacities, eiMap);
			Weights.Int flowWeights = IndexIdMaps.idToIndexWeights(net0.flows, eiMap);
			return new NetImplEdgeWeightsInt(capacityWeights, flowWeights);
		} else {

			/* Unknown network implementation, create a mapped wrapper */
			if (net instanceof FlowNetwork.Int) {
				return new IndexNetFromNetInt((FlowNetwork.Int) net, eiMap);
			} else {
				return new IndexNetFromNet(net, eiMap);
			}
		}
	}

}
