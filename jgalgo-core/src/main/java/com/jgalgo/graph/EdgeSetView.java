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

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

final class EdgeSetView<V, E> implements EdgeSet<V, E> {

	private final Set<E> edges;
	private final Graph<V, E> g;

	EdgeSetView(Set<E> edges, Graph<V, E> g) {
		this.edges = Objects.requireNonNull(edges);
		this.g = Objects.requireNonNull(g);
	}

	Set<E> idsSet() {
		return edges;
	}

	@Override
	public int size() {
		return edges.size();
	}

	@Override
	public boolean isEmpty() {
		return edges.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return edges.contains(o);
	}

	@Override
	public Object[] toArray() {
		return edges.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return edges.toArray(a);
	}

	@Override
	public boolean add(E e) {
		return edges.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return edges.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return edges.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return edges.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return edges.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return edges.removeAll(c);
	}

	@Override
	public void clear() {
		edges.clear();;
	}

	@Override
	public boolean equals(Object o) {
		return edges.equals(o);
	}

	@Override
	public int hashCode() {
		return edges.hashCode();
	}

	@Override
	public String toString() {
		return edges.toString();
	}

	@Override
	public EdgeIter<V, E> iterator() {
		return new EdgeIter<>() {

			Iterator<E> it = edges.iterator();
			E lastEdge = null;

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public E next() {
				return lastEdge = it.next();
			}

			@Override
			public E peekNext() {
				throw new UnsupportedOperationException();
			}

			@Override
			public V source() {
				if (lastEdge == null)
					throw new IllegalStateException();
				return g.edgeSource(lastEdge);
			}

			@Override
			public V target() {
				if (lastEdge == null)
					throw new IllegalStateException();
				return g.edgeTarget(lastEdge);
			}

			@Override
			public void remove() {
				it.remove();
				lastEdge = null;
			}
		};
	}

}
