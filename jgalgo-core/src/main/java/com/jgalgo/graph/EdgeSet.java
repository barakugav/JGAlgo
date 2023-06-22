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
 * Set of graph edges.
 * <p>
 * A set of integers, each represent an edge ID in a graph
 *
 * <pre> {@code
 * Graph g = ...;
 * int vertex = ...;
 * for (EdgeIter eit = g.outEdges(vertex).iterator(); eit.hasNext();) {
 * 	int e = eit.nextInt();
 * 	int u = eit.source();
 * 	int v = eit.target();
 * 	assert vertex == u;
 * 	System.out.println("Out edge of " + vertex + ": " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    EdgeIter
 * @author Barak Ugav
 */
public interface EdgeSet extends IntSet {

	/**
	 * Return an edge iterator that iterate over the edges in this set
	 */
	@Override
	EdgeIter iterator();

	/**
	 * Return an edge iterator that iterate over the edges in this set
	 */
	@Override
	default EdgeIter intIterator() {
		return iterator();
	}

}
