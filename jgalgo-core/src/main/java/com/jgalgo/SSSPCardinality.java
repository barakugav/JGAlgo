package com.jgalgo;

/**
 * Single Source Shortest Path for cardinality weight function.
 * <p>
 * Similar to a {@link SSSP}, but with weights of {@code 1} to all edges. A
 * simple BFS is performed from the source vertex until all vertices that can be
 * reached are reached. The algorithm runs in linear time.
 *
 * @see SSSP
 * @see BFSIter
 * @author Barak Ugav
 */
public class SSSPCardinality {

	/**
	 * Construct a new cardinality SSSP algorithm object.
	 */
	public SSSPCardinality() {
	}

	/**
	 * Compute the shortest paths from a source to all other vertices with
	 * cardinality weight function.
	 *
	 * @param g      a graph
	 * @param source a source vertex
	 * @return a result object containing the distances and shortest paths from the
	 *         source to any other vertex
	 * @see SSSP#computeShortestPaths(Graph, EdgeWeightFunc, int)
	 */
	public SSSP.Result computeShortestPaths(Graph g, int source) {
		SSSPResultImpl.Int res = new SSSPResultImpl.Int(g, source);
		for (BFSIter it = new BFSIter(g, source); it.hasNext();) {
			int v = it.nextInt();
			res.distances[v] = it.layer();
			res.backtrack[v] = it.inEdge();
		}
		return res;
	}

}
