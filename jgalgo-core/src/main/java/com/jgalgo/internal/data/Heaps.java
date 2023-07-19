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
package com.jgalgo.internal.data;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Utils;

public class Heaps {

	static abstract class AbstractHeap<E> extends AbstractCollection<E> implements Heap<E> {

		final Comparator<? super E> c;

		AbstractHeap(Comparator<? super E> c) {
			this.c = c;
		}

		@Override
		public boolean add(E e) {
			insert(e);
			return true;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			for (Object e : c)
				if (remove(e))
					modified = true;
			return modified;
		}

		@Override
		public void meld(Heap<? extends E> heap) {
			Assertions.Heaps.noMeldWithSelf(this, heap);
			Assertions.Heaps.equalComparatorBeforeMeld(this, heap);
			addAll(heap);
			heap.clear();
		}

		@Override
		public Comparator<? super E> comparator() {
			return c;
		}

		int compare(E e1, E e2) {
			return c == null ? Utils.cmpDefault(e1, e2) : c.compare(e1, e2);
		}

		static <K> Heap<K> fromHeapReferenceable(HeapReferenceable<K, ?> h) {
			return new HeapFromReferenceable<>(h);
		}

		private static class HeapFromReferenceable<K> extends AbstractHeap<K> {

			private final HeapReferenceable<K, ?> h;

			HeapFromReferenceable(HeapReferenceable<K, ?> h) {
				super(h.comparator());
				this.h = Objects.requireNonNull(h);
			}

			@Override
			public int size() {
				return h.size();
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean contains(Object o) {
				return h.find((K) o) != null;
			}

			@Override
			public Iterator<K> iterator() {
				return Utils.iterMap(h.iterator(), HeapReference::key);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean remove(Object o) {
				HeapReference<K, ?> ref = h.find((K) o);
				if (ref == null)
					return false;
				h.remove(ref);
				return true;
			}

			@Override
			public void clear() {
				h.clear();
			}

			@Override
			public void insert(K key) {
				h.insert(key);
			}

			@Override
			public void insertAll(Collection<? extends K> elms) {
				for (K elm : elms)
					h.insert(elm);
			}

			@Override
			public K findMin() {
				return h.findMin().key();
			}

			@Override
			public K extractMin() {
				return h.extractMin().key();
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void meld(Heap<? extends K> heap) {
				if (!(heap instanceof HeapFromReferenceable<?>))
					throw new IllegalArgumentException();
				HeapReferenceable<K, ?> oh = ((HeapFromReferenceable<K>) heap).h;
				h.meld((HeapReferenceable) oh);
			}
		}

	}

	static abstract class AbstractHeapReferenceable<K, V> extends AbstractCollection<HeapReference<K, V>>
			implements HeapReferenceable<K, V> {

		final Comparator<? super K> c;

		AbstractHeapReferenceable(Comparator<? super K> c) {
			this.c = c;
		}

		@Override
		public Comparator<? super K> comparator() {
			return c;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object o) {
			if (!(o instanceof HeapReference<?, ?>))
				return false;
			return o == find(((HeapReference<K, ?>) o).key());
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object o) {
			remove((HeapReference<K, V>) o);
			return true;
		}

		@Override
		public HeapReference<K, V> extractMin() {
			HeapReference<K, V> min = findMin();
			remove(min);
			return min;
		}

		int compare(K k1, K k2) {
			return c == null ? Utils.cmpDefault(k1, k2) : c.compare(k1, k2);
		}

	}

	public static void assertHeapConstraints(HeapReferenceable<?, ?> heap) {
		if (heap instanceof HeapPairing) {
			HeapPairing.assertHeapConstraints((HeapPairing<?, ?, ?>) heap);
		} else {
			throw new IllegalArgumentException("unsupported heap type: " + heap.getClass().getName());
		}
	}

}
