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

import java.util.NoSuchElementException;

public class QueueFixSize<E> {

	private final int idxMask;
	private final Object[] q;
	private int begin, end;

	public QueueFixSize(int maxSize) {
		/* round size of next power of 2 */
		maxSize = 1 << (32 - Integer.numberOfLeadingZeros(maxSize));
		idxMask = maxSize - 1;
		q = new Object[maxSize];
		begin = end = 0;
	}

	public int size() {
		return begin <= end ? end - begin : q.length - begin + end;
	}

	public boolean isEmpty() {
		return begin == end;
	}

	public void push(E x) {
		if (((end + 1) & idxMask) == begin)
			throw new IndexOutOfBoundsException();
		q[end] = x;
		end = (end + 1) & idxMask;
	}

	public E pop() {
		if (isEmpty())
			throw new NoSuchElementException();
		@SuppressWarnings("unchecked")
		E x = (E) q[begin];
		begin = (begin + 1) & idxMask;
		return x;
	}

	public void clear() {
		begin = end = 0;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('[');

		int lastIdx = Math.min(end, q.length) - 1;
		if (begin <= lastIdx) {
			for (int i = begin;; i++) {
				b.append(q[i]);
				if (i == lastIdx) {
					if (end < begin && end != 0)
						b.append(", ");
					break;
				}
				b.append(", ");
			}
		}

		if (end < begin && end != 0) {
			for (int i = 0;; i++) {
				b.append(q[i]);
				if (i == end - 1)
					break;
				b.append(", ");
			}
		}

		return b.append(']').toString();
	}

}
