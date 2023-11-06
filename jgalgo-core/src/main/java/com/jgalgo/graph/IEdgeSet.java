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
package com.jgalgo.graph;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Set of int graph edges.
 *
 * <p>
 * This interface is a specific version of {@link EdgeSet} for {@link IntGraph}.
 *
 * <p>
 * A set of integers, each represent an edge ID in a graph
 *
 * <pre> {@code
 * IntGraph g = ...;
 * int vertex = ...;
 * for (IEdgeIter eit = g.outEdges(vertex).iterator(); eit.hasNext();) {
 * 	int e = eit.nextInt();
 * 	int u = eit.sourceInt();
 * 	int v = eit.targetInt();
 * 	assert vertex == u;
 * 	System.out.println("Out edge of " + vertex + ": " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    IEdgeIter
 * @author Barak Ugav
 */
public interface IEdgeSet extends EdgeSet<Integer, Integer>, IntSet {

	/**
	 * Return an edge iterator that iterate over the edges in this set.
	 */
	@Override
	IEdgeIter iterator();

	/**
	 * Return an edge iterator that iterate over the edges in this set.
	 */
	@Override
	default IEdgeIter intIterator() {
		return iterator();
	}

}
