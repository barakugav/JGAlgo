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

import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Iterator used to iterate over int graph edges.
 *
 * <p>
 * This interface is a specific version of {@link EdgeIter} for {@link IntGraph}.
 *
 * <p>
 * Each {@code int} returned by {@link #nextInt()} is an ID of an edge iterated by the iterator. The source and target
 * of the last iterated edge are available by {@link #sourceInt()} and {@link #targetInt()}.
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
 * @see    IEdgeSet
 * @author Barak Ugav
 */
public interface IEdgeIter extends EdgeIter<Integer, Integer>, IntIterator {

	/**
	 * Peek at the next edge of the iterator without advancing it.
	 *
	 * <p>
	 * Similar to {@link #nextInt()} but without advancing the iterator.
	 *
	 * @return                        the next edge of the iterator
	 * @throws NoSuchElementException if there is no 'next' element
	 */
	int peekNextInt();

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #peekNextInt()} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer peekNext() {
		return Integer.valueOf(peekNextInt());
	}

	/**
	 * Get the source vertex of the last returned edge.
	 *
	 * <p>
	 * The behavior is undefined if {@link #nextInt()} was not called yet.
	 *
	 * @return the source vertex of the last returned edge
	 */
	int sourceInt();

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #sourceInt()} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer source() {
		return Integer.valueOf(sourceInt());
	}

	/**
	 * Get the target vertex of the last returned edge.
	 *
	 * <p>
	 * The behavior is undefined if {@link #nextInt()} was not called yet.
	 *
	 * @return the target vertex of the last returned edge
	 */
	int targetInt();

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #targetInt()} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer target() {
		return Integer.valueOf(targetInt());
	}

}
