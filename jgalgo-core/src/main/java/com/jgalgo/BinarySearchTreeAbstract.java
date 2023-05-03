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

abstract class BinarySearchTreeAbstract<E> extends HeapReferenceableAbstract<E> implements BinarySearchTree<E> {

	public BinarySearchTreeAbstract(Comparator<? super E> c) {
		super(c);
	}

	@Override
	public E findMax() {
		return findMaxRef().get();
	}

	@Override
	public E extractMax() {
		HeapReference<E> max = findMaxRef();
		E val = max.get();
		removeRef(max);
		return val;
	}

}
