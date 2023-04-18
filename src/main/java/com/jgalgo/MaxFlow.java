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
 * <p>
 *
 * <pre> {@code
 * DiGraph g = ...;
 * FlowNetwork net = ...;
 * int sourceVertex = ...;
 * int targetVertex = ...;
 * MaxFlow maxFlowAlg = ...;
 *
 * double totalFlow = maxFlowAlg.calcMaxFlow(g, net, sourceVertex, targetVertex);
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
public interface MaxFlow {

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
	double calcMaxFlow(Graph g, FlowNetwork net, int source, int sink);

}
