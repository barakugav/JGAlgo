package com.jgalgo;

/**
 * Calculate the maximum flow in a flow network.
 * <p>
 * A maximum flow is firstly a valid flow, namely for each vertex except the
 * source and sink the sum of flow units going along {@link Graph#edgesIn(int)}
 * must be equal to the sum of flow units going along
 * {@link Graph#edgesOut(int)}. In addition, a maximum flow maximize the number
 * of flow units originated at the source and reach the sink, which is
 * equivalent to the sum of flows going out(in) of the source(sink) subtracted
 * by the sum of flows going in(out) to the source(sink).
 *
 * <pre> {@code
 * DiGraph g = ...;
 * FlowNetwork net = FlowNetwork.createAsEdgeWeight(g);
 * for (IntIterator edgeIter = g.edges().iterator(); edgeIter.hasNext();)
 *  f.setCapacity(edgeIter.nextInt(), 1);
 *
 * int sourceVertex = ...;
 * int targetVertex = ...;
 * MaxFlow maxFlowAlg = MaximumFlow.newBuilder().build();
 *
 * double totalFlow = maxFlowAlg.computeMaximumFlow(g, net, sourceVertex, targetVertex);
 * System.out.println("The maximum flow that can be pushed in the network is " + totalFlow);
 * for (IntIterator it = g.edges().iterator(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	double capacity = net.getCapacity(e);
 * 	double flow = net.getFlow(e);
 * 	System.out.println("flow on edge " + e + ": " + flow + "/" + capacity);
 * }
 * }</pre>
 *
 * @see FlowNetwork
 * @author Barak Ugav
 */
public interface MaximumFlow {

	/**
	 * Calculate the maximum flow in a flow network.
	 * <p>
	 * The function will set the edges flow by
	 * {@link FlowNetwork#setFlow(int, double)}.
	 *
	 * @param g      a graph
	 * @param net    network flow
	 * @param source a source vertex
	 * @param sink   a sink vertex
	 * @return the maximum flow in the network from the source to the sink
	 */
	double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink);

	/**
	 * Create a new maximum flow algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MaximumFlow}
	 * object.
	 *
	 * @return a new builder that can build {@link MaximumFlow} objects
	 */
	static MaximumFlow.Builder newBuilder() {
		return MaximumFlowPushRelabelHighestFirst::new;
	}

	/**
	 * A builder for {@link MaximumFlow} objects.
	 *
	 * @see MaximumFlow#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for maximum flow computation.
		 *
		 * @return a new maximum flow algorithm
		 */
		MaximumFlow build();

	}

}
