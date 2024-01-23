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
import java.util.Objects;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

final class IEdgeSetView extends AbstractIntSet implements IEdgeSet {

	private final IntSet edges;
	private final IndexGraph g;

	IEdgeSetView(IntSet edges, IndexGraph g) {
		this.edges = Objects.requireNonNull(edges);
		this.g = Objects.requireNonNull(g);
	}

	IntSet idsSet() {
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
	public Object[] toArray() {
		return edges.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return edges.toArray(a);
	}

	@Override
	public int[] toIntArray() {
		return edges.toIntArray();
	}

	@Override
	public int[] toArray(int[] a) {
		return edges.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return edges.containsAll(c);
	}

	@Override
	public boolean containsAll(IntCollection c) {
		return edges.containsAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return edges.retainAll(c);
	}

	@Override
	public boolean retainAll(IntCollection c) {
		return edges.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return edges.removeAll(c);
	}

	@Override
	public boolean removeAll(IntCollection c) {
		return edges.removeAll(c);
	}

	@Override
	public void clear() {
		edges.clear();
	}

	@Override
	public boolean remove(int k) {
		return edges.remove(k);
	}

	@Override
	public boolean contains(int key) {
		return edges.contains(key);
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
	public IEdgeIter iterator() {
		return new IEdgeIter() {

			IntIterator it = edges.iterator();
			int lastEdge = -1;

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int nextInt() {
				return lastEdge = it.nextInt();
			}

			@Override
			public int peekNextInt() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int sourceInt() {
				if (lastEdge < 0)
					throw new IllegalStateException();
				return g.edgeSource(lastEdge);
			}

			@Override
			public int targetInt() {
				if (lastEdge < 0)
					throw new IllegalStateException();
				return g.edgeTarget(lastEdge);
			}

			@Override
			public void remove() {
				it.remove();
				lastEdge = -1;
			}
		};
	}

}
