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

import java.util.BitSet;
import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
 * for (DFSIter iter = new DFSIter(g, sourceVertex); iter.hasNext();) {
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
public class DFSIter implements IntIterator {

	private final Graph g;
	private final BitSet visited;
	private final Stack<EdgeIter> edgeIters;
	private final IntArrayList edgePath;
	private final IntList edgePathView;
	private boolean isValid;

	/**
	 * Create a DFS iterator rooted at some source vertex.
	 *
	 * @param g      a graph
	 * @param source a vertex in the graph from which the search will start from.
	 */
	public DFSIter(Graph g, int source) {
		int n = g.vertices().size();
		this.g = g;
		visited = new BitSet(n);
		edgeIters = new ObjectArrayList<>();
		edgePath = new IntArrayList();
		edgePathView = IntLists.unmodifiable(edgePath);

		visited.set(source);
		edgeIters.push(g.edgesOut(source));
		isValid = true;
	}

	/**
	 * Check whether there is more vertices to iterate over.
	 */
	@Override
	public boolean hasNext() {
		if (isValid)
			return true;
		if (edgeIters.isEmpty())
			return false;
		for (;;) {
			for (EdgeIter eit = edgeIters.top(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (visited.get(v))
					continue;
				visited.set(v);
				edgeIters.push(g.edgesOut(v));
				edgePath.add(e);
				return isValid = true;
			}
			edgeIters.pop();
			if (edgeIters.isEmpty()) {
				assert edgePath.isEmpty();
				return false;
			}
			edgePath.popInt();
		}
	}

	/**
	 * Advance the iterator and return a vertex that was not visited by the iterator yet.
	 */
	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		int ret = edgeIters.top().source();
		isValid = false;
		return ret;
	}

	/**
	 * Get the path from the source to the last vertex returned by {@link nextInt}.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return list of edges forming a path from the source to the last vertex returned by {@link nextInt}. The returned
	 *         list should not be modified by the user.
	 */
	public IntList edgePath() {
		return edgePathView;
	}
}
