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

import java.util.Set;

/**
 * Set of graph edges.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * String vertex = ...;
 * for (EdgeIter eit = g.outEdges(vertex).iterator(); eit.hasNext();) {
 * 	Integer e = eit.next();
 * 	String u = eit.source();
 * 	String v = eit.target();
 * 	assert vertex.equals(u);
 * 	System.out.println("Out edge of " + vertex + ": " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @see        EdgeIter
 * @author     Barak Ugav
 */
public interface EdgeSet<V, E> extends Set<E> {

	/**
	 * Return an edge iterator that iterate over the edges in this set.
	 */
	@Override
	EdgeIter<V, E> iterator();

}
