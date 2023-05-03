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
 * Similar to a {@link SSSP}, but with weights of \(1\) to all edges. A simple BFS is performed from the source vertex
 * until all vertices that can be reached are reached. The algorithm runs in linear time.
 *
 * @see    SSSP
 * @see    BFSIter
 * @author Barak Ugav
 */
public class SSSPCardinality {

	/**
	 * Construct a new cardinality SSSP algorithm object.
	 */
	public SSSPCardinality() {}

	/**
	 * Compute the shortest paths from a source to all other vertices with cardinality weight function.
	 *
	 * @param  g      a graph
	 * @param  source a source vertex
	 * @return        a result object containing the distances and shortest paths from the source to any other vertex
	 * @see           SSSP#computeShortestPaths(Graph, EdgeWeightFunc, int)
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
