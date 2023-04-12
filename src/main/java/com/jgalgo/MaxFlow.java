package com.jgalgo;

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

		public double getCapacity(int edge);

		public void setCapacity(int edge, double capacity);

		public double getFlow(int edge);

		public void setFlow(int edge, double flow);

		public static FlowNetwork createAsEdgeWeight(Graph g) {
			Weights.Double capacityWeights = g.addEdgesWeights(new Object(), double.class);
			Weights.Double flowWeights = g.addEdgesWeights(new Object(), double.class);
			return new FlowNetwork() {

				private static final double EPS = 0.0001;

				@Override
				public double getCapacity(int edge) {
					return capacityWeights.getDouble(edge);
				}

				@Override
				public void setCapacity(int edge, double capacity) {
					capacityWeights.set(edge, capacity);
				}

				@Override
				public double getFlow(int e) {
					return flowWeights.getDouble(e);
				}

				@Override
				public void setFlow(int edge, double flow) {
					double capacity = getCapacity(edge);
					if (flow > capacity + EPS)
						throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
					flowWeights.set(edge, Math.min(flow, capacity));
				}
			};
		}

	}

}
