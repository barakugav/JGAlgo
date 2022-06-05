package com.ugav.algo;

import com.ugav.algo.Graph.Edge;

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
	public <E> double calcMaxFlow(Graph<E> g, FlowNetwork<E> net, int source, int target);

	public static interface FlowNetwork<E> {

		public double getCapacity(Edge<? extends E> e);

		public double getFlow(Edge<? extends E> e);

		public void setFlow(Edge<? extends E> e, double flow);

	}

	public static class FlowEdgeValueDefault {
		public double capacity;
		public double flow;

		public FlowEdgeValueDefault(double capacity) {
			this.capacity = capacity;
		}

		@Override
		public String toString() {
			return "(" + flow + " / " + capacity + ")";
		}
	}

	public static class FlowNetworkDefault implements FlowNetwork<FlowEdgeValueDefault> {

		private static final double EPS = 0.0001;

		public FlowNetworkDefault() {
		}

		@Override
		public double getCapacity(Edge<? extends FlowEdgeValueDefault> e) {
			return e.val().capacity;
		}

		@Override
		public double getFlow(Edge<? extends FlowEdgeValueDefault> e) {
			return e.val().flow;
		}

		@Override
		public void setFlow(Edge<? extends FlowEdgeValueDefault> e, double flow) {
			if (flow > e.val().capacity + EPS)
				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + e);
			e.val().flow = Math.min(flow, e.val().capacity);
		}

	}

}
