package com.jgalgo;

import java.util.Objects;

public interface MaxFlow {

	/**
	 * Calculate the maximum flow in a flow network
	 *
	 * The function will set the edges flow by FlowNetwork.setFlow
	 *
	 * @param g      a graph
	 * @param net    network flow
	 * @param source a source vertex
	 * @param target a target (sink) vertex
	 * @return the maximum flow in the network from the source to the target
	 */
	public double calcMaxFlow(Graph g, FlowNetwork net, int source, int target);

	public static interface FlowNetwork {

		public double getCapacity(int e);

		public double getFlow(int e);

		public void setFlow(int e, double flow);

	}

	public static class FlowEdgeDataDefault {
		public final double capacity;
		public double flow;

		public FlowEdgeDataDefault(double capacity) {
			this.capacity = capacity;
		}

		@Override
		public String toString() {
			return "(" + flow + " / " + capacity + ")";
		}
	}

	public static class FlowNetworkDefault implements FlowNetwork {

		private static final double EPS = 0.0001;
		private final Weights<FlowEdgeDataDefault> data;

		public FlowNetworkDefault(Weights<FlowEdgeDataDefault> data) {
			this.data = Objects.requireNonNull(data);
		}

		@Override
		public double getCapacity(int e) {
			return data.get(e).capacity;
		}

		@Override
		public double getFlow(int e) {
			return data.get(e).flow;
		}

		@Override
		public void setFlow(int e, double flow) {
			FlowEdgeDataDefault d = data.get(e);
			if (flow > d.capacity + EPS)
				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + e);
			d.flow = Math.min(flow, d.capacity);
		}

	}

}
