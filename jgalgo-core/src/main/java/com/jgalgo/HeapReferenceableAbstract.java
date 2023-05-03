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

import java.util.Comparator;
import java.util.Iterator;

abstract class HeapReferenceableAbstract<E> extends HeapAbstract<E> implements HeapReferenceable<E> {

	HeapReferenceableAbstract(Comparator<? super E> c) {
		super(c);
	}

	@Override
	public Iterator<E> iterator() {
		return HeapReferenceable.super.iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		return findRef((E) o) != null;
	}

	@Override
	public E findMin() {
		return findMinRef().get();
	}

	@Override
	public boolean remove(Object o) {
		@SuppressWarnings("unchecked")
		HeapReference<E> ref = findRef((E) o);
		if (ref == null)
			return false;
		removeRef(ref);
		return true;
	}

	@Override
	public E extractMin() {
		HeapReference<E> min = findMinRef();
		E val = min.get();
		removeRef(min);
		return val;
	}

}
