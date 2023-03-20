package com.ugav.algo;

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

//	public static class FlowNetworkDefault implements FlowNetwork<FlowEdgeDataDefault> {
//
//		private static final double EPS = 0.0001;
//
//		public FlowNetworkDefault() {
//		}
//
//		@Override
//		public double getCapacity(Edge<? extends FlowEdgeDataDefault> e) {
//			return e.data().capacity;
//		}
//
//		@Override
//		public double getFlow(Edge<? extends FlowEdgeDataDefault> e) {
//			return e.data().flow;
//		}
//
//		@Override
//		public void setFlow(Edge<? extends FlowEdgeDataDefault> e, double flow) {
//			if (flow > e.data().capacity + EPS)
//				throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + e);
//			e.data().flow = Math.min(flow, e.data().capacity);
//		}
//
//	}

}
