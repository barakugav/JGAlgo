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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator used to iterate over graph edges.
 * <p>
 * Each value returned by {@link #next()} is an edge iterated by the iterator. The source and target of the last
 * iterated edge are available by {@link #source()} and {@link #target()}.
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
 * @see        EdgeSet
 * @author     Barak Ugav
 */
public interface EdgeIter<V, E> extends Iterator<E> {

	/**
	 * Peek at the next edge of the iterator without advancing it.
	 * <p>
	 * Similar to {@link #next()} but without advancing the iterator.
	 *
	 * @return                        the next edge of the iterator
	 * @throws NoSuchElementException if there is no 'next' element
	 */
	E peekNext();

	/**
	 * Get the source vertex of the last returned edge.
	 * <p>
	 * The behavior is undefined if {@link #next()} was not called yet.
	 *
	 * @return the source vertex of the last returned edge
	 */
	V source();

	/**
	 * Get the target vertex of the last returned edge.
	 * <p>
	 * The behavior is undefined if {@link #next()} was not called yet.
	 *
	 * @return the target vertex of the last returned edge
	 */
	V target();

}
