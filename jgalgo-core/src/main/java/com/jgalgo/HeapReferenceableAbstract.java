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

import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.Objects;

abstract class HeapReferenceableAbstract<K, V> extends AbstractCollection<HeapReference<K, V>>
		implements HeapReferenceable<K, V> {

	final Comparator<? super K> c;

	HeapReferenceableAbstract(Comparator<? super K> c) {
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

	void makeSureDecreaseKeyIsSmaller(K oldKey, K newKey) {
		if (compare(oldKey, newKey) < 0)
			throw new IllegalArgumentException("New key is greater than existing one");
	}

	void makeSureNoMeldWithSelf(HeapReferenceable<? extends K, ? extends V> other) {
		if (other == this)
			throw new IllegalArgumentException("A heap can't meld with itself");
	}

	@SuppressWarnings("rawtypes")
	void makeSureMeldWithSameImpl(Class<? extends HeapReferenceable> impl,
			HeapReferenceable<? extends K, ? extends V> other) {
		if (!impl.isAssignableFrom(other.getClass()))
			throw new IllegalArgumentException("Can't meld heaps with different implementations");
	}

	void makeSureEqualComparatorBeforeMeld(HeapReferenceable<? extends K, ? extends V> other) {
		if (!Objects.equals(comparator(), other.comparator()))
			throw new IllegalArgumentException("Can't meld, heaps have different comparators");
	}

}