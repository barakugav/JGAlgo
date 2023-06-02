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

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Depth first search (DFS) iterator.
 * <p>
 * The DFS iterator is used to iterate over the vertices of a graph is a depth first manner, namely it explore as far as
 * possible along each branch before backtracking. The iterator will visit every vertex \(v\) for which there is a path
 * from the source(s) to \(v\). Each such vertex will be visited exactly once.
 * <p>
 * The graph should not be modified during the DFS iteration.
 *
 * <pre> {@code
 * Graph g = ...;
 * int sourceVertex = ...;
 * for (DFSIter iter = DFSIter.newInstance(g, sourceVertex); iter.hasNext();) {
 *     int v = iter.nextInt();
 *     IntList edgePath = iter.edgePath();
 *     System.out.println("Reached vertex " + v + " using the edges: " + edgePath);
 * }
 * }</pre>
 *
 * @see    BFSIter
 * @see    <a href="https://en.wikipedia.org/wiki/Depth-first_search">Wikipedia</a>
 * @author Barak Ugav
 */
public interface DFSIter extends IntIterator {

	/**
	 * Create a DFS iterator rooted at some source vertex.
	 *
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a DFS iterator that iterate over the vertices of the graph
	 */
	static DFSIter newInstance(Graph g, int source) {
		if (g instanceof IndexGraph)
			return new DFSIterImpl((IndexGraph) g, source);

		IndexGraph iGraph = g.indexGraph();
		IndexGraphMap viMap = g.indexGraphVerticesMap();
		IndexGraphMap eiMap = g.indexGraphEdgesMap();

		int iSource = viMap.idToIndex(source);
		DFSIter indexIter = new DFSIterImpl(iGraph, iSource);
		return new DFSIterImpl.DFSFromIndexDFS(indexIter, viMap, eiMap);
	}

	/**
	 * Check whether there is more vertices to iterate over.
	 */
	@Override
	public boolean hasNext();

	/**
	 * Advance the iterator and return a vertex that was not visited by the iterator yet.
	 */
	@Override
	public int nextInt();

	/**
	 * Get the path from the source to the last vertex returned by {@link nextInt}.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return list of edges forming a path from the source to the last vertex returned by {@link nextInt}. The returned
	 *         list should not be modified by the user.
	 */
	public IntList edgePath();
}
