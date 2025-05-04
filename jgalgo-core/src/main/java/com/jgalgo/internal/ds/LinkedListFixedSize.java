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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.function.IntPredicate;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntIntPair;

// TODO: Rename to LinkedList
public class LinkedListFixedSize {
	private LinkedListFixedSize() {}

	public static final int None = -1;

	public static boolean isNone(int entry) {
		assert entry >= 0 || entry == None;
		return entry < 0;
	}

	// TODO: rename to undirectional
	public static class Singly {

		private final int[] arr;

		public Singly(int n) {
			arr = new int[n];
			Arrays.fill(arr, None);
		}

		public int size() {
			return arr.length;
		}

		public int next(int id) {
			return arr[id];
		}

		public void setNext(int id, int next) {
			arr[id] = next;
		}

		public boolean hasNext(int id) {
			return !isNone(next(id));
		}

		public void clear() {
			Arrays.fill(arr, None);
		}

		public IterTools.Peek.Int iterator(int id) {
			Assertions.checkArrayIndex(id, 0, size());
			return new IterTools.Peek.Int() {
				int p = id;

				@Override
				public boolean hasNext() {
					return !isNone(p);
				}

				@Override
				public int nextInt() {
					Assertions.hasNext(this);
					int ret = p;
					p = LinkedListFixedSize.Singly.this.next(p);
					return ret;
				}

				@Override
				public int peekNextInt() {
					Assertions.hasNext(this);
					return p;
				}

			};
		}

	}

	// TODO: rename to bidirectional
	public static class Doubly {

		private final int[] arr;

		public Doubly(int n) {
			arr = new int[n * 2];
			Arrays.fill(arr, None);
		}

		private static int idxOfNext(int id) {
			return (id << 1) + 0;
		}

		private static int idxOfPrev(int id) {
			return (id << 1) + 1;
		}

		public int size() {
			return arr.length / 2;
		}

		public int next(int id) {
			return arr[idxOfNext(id)];
		}

		public void setNext(int id, int next) {
			arr[idxOfNext(id)] = next;
		}

		public boolean hasNext(int id) {
			return !isNone(next(id));
		}

		public int prev(int id) {
			return arr[idxOfPrev(id)];
		}

		public void setPrev(int id, int prev) {
			arr[idxOfPrev(id)] = prev;
		}

		public boolean hasPrev(int id) {
			return !isNone(prev(id));
		}

		public void insertAfter(int prev, int id) {
			// if (hasNext(id) || hasPrev(id))
			// throw new IllegalArgumentException();
			int next = next(prev);
			setNext(prev, id);
			setPrev(id, prev);
			if (!isNone(next)) {
				setNext(id, next);
				setPrev(next, id);
			}
		}

		public void insertBefore(int next, int id) {
			int prev = prev(next);
			if (!isNone(prev)) {
				setNext(prev, id);
				setPrev(id, prev);
			}
			setNext(id, next);
			setPrev(next, id);
		}

		public void connect(int prev, int next) {
			// if (hasNext(prev) || hasPrev(next))
			// throw new IllegalArgumentException();
			setNext(prev, next);
			setPrev(next, prev);
		}

		public void disconnect(int id) {
			int prev = prev(id), next = next(id);
			if (!isNone(prev)) {
				setNext(prev, next);
				setPrev(id, None);
			}
			if (!isNone(next)) {
				setPrev(next, prev);
				setNext(id, None);
			}
		}

		public IntIntPair removeIf(int head, IntPredicate pred) {
			var x = removeIf0(head, pred);
			int newHead = x.firstInt(), newTail = x.secondInt();
			assert newHead < 0 == newTail < 0;
			if (newHead >= 0 && newTail >= 0) {
				assert !hasPrev(newHead);
				assert !hasNext(newTail);
			}
			return x;
		}

		private IntIntPair removeIf0(int head, IntPredicate pred) {
			if (isNone(head))
				return IntIntPair.of(None, None);
			if (hasPrev(head))
				throw new IllegalArgumentException("head must not have a previous element");
			for (int prev = None, curr = head;;) {
				int next = next(curr);
				if (pred.test(curr)) {
					if (isNone(prev)) {
						head = next;
					} else {
						setPrev(curr, None);
						setNext(prev, next);
					}
					if (!isNone(next)) {
						setNext(curr, None);
						setPrev(next, prev);
					} else {
						int tail = prev;
						return IntIntPair.of(head, tail);
					}
				} else {
					if (isNone(next)) {
						int tail = curr;
						return IntIntPair.of(head, tail);
					}
					prev = curr;
				}
				curr = next;
			}
		}

		public void reverse(final int start) {
			final int origPrev = prev(start);
			for (int curr = start;;) {
				int prev = prev(curr), next = next(curr);
				setNext(curr, prev);
				setPrev(curr, next);
				if (isNone(next)) {
					if (!isNone(origPrev))
						throw new IllegalArgumentException("Given start vertex is not a head of a list");
					break;
				}
				if (next == start)
					break;
				curr = next;
			}
		}

		public void clear() {
			Arrays.fill(arr, None);
		}

		public IterTools.Peek.Int iterator(int id) {
			Assertions.checkArrayIndex(id, 0, size());
			return iteratorMaybeNone(id);
		}

		public IterTools.Peek.Int iteratorMaybeNone(int idMaybeNone) {
			assert idMaybeNone >= 0 || idMaybeNone == None;
			return new Iter(idMaybeNone);
		}

		public IterTools.Peek.Int iteratorRev(int id) {
			Assertions.checkArrayIndex(id, 0, size());
			return new IterRev(id);
		}

		private class Iter implements IterTools.Peek.Int {

			int p;

			Iter(int start) {
				p = start;
			}

			@Override
			public boolean hasNext() {
				return !isNone(p);
			}

			@Override
			public int nextInt() {
				Assertions.hasNext(this);
				int ret = p;
				p = LinkedListFixedSize.Doubly.this.next(p);
				return ret;
			}

			@Override
			public int peekNextInt() {
				Assertions.hasNext(this);
				return p;
			}
		}

		private class IterRev implements IterTools.Peek.Int {

			int p;

			IterRev(int start) {
				p = start;
			}

			@Override
			public boolean hasNext() {
				return !isNone(p);
			}

			@Override
			public int nextInt() {
				Assertions.hasNext(this);
				int ret = p;
				p = LinkedListFixedSize.Doubly.this.prev(p);
				return ret;
			}

			@Override
			public int peekNextInt() {
				Assertions.hasNext(this);
				return p;
			}
		}

	}

	public static class PoolAllocator {

		private final int[] freeEntries;
		private int freeEntriesNum;

		public PoolAllocator(int n) {
			freeEntries = new int[n];
			for (int i : range(n))
				freeEntries[i] = n - i - 1;
			freeEntriesNum = n;
		}

		public int alloc() {
			if (freeEntriesNum == 0)
				throw new IllegalStateException("No free entries left");
			return freeEntries[--freeEntriesNum];
		}

		public void free(int index) {
			freeEntries[freeEntriesNum++] = index;
		}
	}

}
