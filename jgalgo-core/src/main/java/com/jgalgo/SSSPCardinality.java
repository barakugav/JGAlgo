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

/**
 * Single Source Shortest Path for cardinality weight function.
 * <p>
 * The cardinality length of a path is the number of edges in it. The cardinality shortest path from a source vertex to
 * some other vertex is the path with the minimum number of edges. A simple BFS is performed from the source vertex
 * until all vertices that can be reached are reached. The algorithm runs in linear time.
 *
 * @see    BFSIter
 * @author Barak Ugav
 */
public class SSSPCardinality implements SSSP {

	/**
	 * Construct a new cardinality SSSP algorithm object.
	 */
	public SSSPCardinality() {}

	@Override
	public SSSP.Result computeCardinalityShortestPaths(Graph g, int source) {
		SSSPResultImpl.Int res = new SSSPResultImpl.Int(g, source);
		for (BFSIter it = new BFSIter(g, source); it.hasNext();) {
			int v = it.nextInt();
			res.distances[v] = it.layer();
			res.backtrack[v] = it.inEdge();
		}
		return res;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the weight function {@code w} is not {@code null} or
	 *                                      {@link EdgeWeightFunc#CardinalityEdgeWeightFunction}
	 */
	@Override
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		if (!(w == null || w == EdgeWeightFunc.CardinalityEdgeWeightFunction))
			throw new IllegalArgumentException("only cardinality shortest paths are supported");
		return computeCardinalityShortestPaths(g, source);
	}

}
