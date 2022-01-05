package com.ugav.algo;

import com.ugav.algo.Graph.Edge;

public interface MaxFlow {

	public static interface FlowNetwork<E> {

		public double getCapacity(Edge<E> e);

		public double getFlow(Edge<E> e);

		public void setFlow(Edge<E> e, double flow);

	}

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

}
