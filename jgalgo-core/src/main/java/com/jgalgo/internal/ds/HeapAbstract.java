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

import java.util.Comparator;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;

abstract class HeapAbstract<E> implements Heap<E> {

	final Comparator<? super E> c;

	HeapAbstract(Comparator<? super E> c) {
		this.c = c;
	}

	@Override
	public void meld(Heap<? extends E> heap) {
		Assertions.heapNoMeldWithSelf(this, heap);
		Assertions.heapEqualComparatorBeforeMeld(this, heap);
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

}
