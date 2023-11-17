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

package com.jgalgo.internal.ds;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.JGAlgoUtils;

abstract class HeapAbstract<E> implements Heap<E> {

	final Comparator<? super E> c;

	HeapAbstract(Comparator<? super E> c) {
		this.c = c;
	}

	@Override
	public void meld(Heap<? extends E> heap) {
		Assertions.Heaps.noMeldWithSelf(this, heap);
		Assertions.Heaps.equalComparatorBeforeMeld(this, heap);
		for (E elm : heap)
			insert(elm);
		heap.clear();
	}

	@Override
	public Comparator<? super E> comparator() {
		return c;
	}

	int compare(E e1, E e2) {
		return c == null ? JGAlgoUtils.cmpDefault(e1, e2) : c.compare(e1, e2);
	}

	static <K> Heap<K> fromHeapReferenceable(HeapReferenceable<K, ?> h) {
		return new HeapFromReferenceable<>(h);
	}

	private static class HeapFromReferenceable<K> extends HeapAbstract<K> {

		private final HeapReferenceable<K, ?> h;

		HeapFromReferenceable(HeapReferenceable<K, ?> h) {
			super(h.comparator());
			this.h = Objects.requireNonNull(h);
		}

		@Override
		public Iterator<K> iterator() {
			return IterTools.map(h.iterator(), HeapReference::key);
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

		@Override
		public boolean isEmpty() {
			return h.isEmpty();
		}

		@Override
		public boolean isNotEmpty() {
			return h.isNotEmpty();
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public boolean remove(K elm) {
			HeapReference<K, ?> ref = h.find(elm);
			if (ref == null)
				return false;
			h.remove((HeapReference) ref);
			return true;
		}
	}

}