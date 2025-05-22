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

import java.util.Arrays;
import java.util.function.IntPredicate;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntPair;
import com.jgalgo.internal.util.IterTools;

public class LinkedList {
	private LinkedList() {}

	public static final int None = -1;
	public static final long HeadTailNone = headTail(None, None);

	public static boolean isNone(int entry) {
		assert entry >= 0 || entry == None;
		return entry < 0;
	}

	public static int head(long headTail) {
		return IntPair.first(headTail);
	}

	public static int tail(long headTail) {
		return IntPair.second(headTail);
	}

	public static long headTail(int head, int tail) {
		return IntPair.of(head, tail);
	}

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
					p = LinkedList.Singly.this.next(p);
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

		public long addFirst(long list, int id) {
			int head = head(list), tail = tail(list);
			assert !hasNext(id) && !hasPrev(id);
			if (isNone(head)) {
				head = tail = id;
			} else {
				connect(id, head);
				head = id;
			}
			assert !hasPrev(head);
			return headTail(head, tail);
		}

		public long addLast(long list, int id) {
			int head = head(list), tail = tail(list);
			assert !hasNext(id) && !hasPrev(id);
			if (isNone(tail)) {
				head = tail = id;
			} else {
				connect(tail, id);
				tail = id;
			}
			assert !hasPrev(head);
			return headTail(head, tail);
		}

		public long concat(long list1, long list2) {
			int head1 = head(list1), tail1 = tail(list1);
			int head2 = head(list2), tail2 = tail(list2);
			assert isNone(head2) == isNone(tail2);
			assert isNone(head1) == isNone(tail1);
			if (isNone(head2))
				return list1;

			if (isNone(tail1)) {
				head1 = head2;
				tail1 = tail2;
			} else {
				assert !hasNext(tail1);
				connect(tail1, head2);
				tail1 = tail2;
			}
			assert !hasPrev(head1);
			return headTail(head1, tail1);
		}

		public long remove(long list, int id) {
			int head = head(list), tail = tail(list);
			int prev = prev(id), next = next(id);
			if (isNone(prev)) {
				assert head == id;
				head = next;
			} else {
				setNext(prev, next);
				setPrev(id, None);
			}
			if (!isNone(next)) {
				setPrev(next, prev);
				setNext(id, None);
			} else {
				assert tail == id;
				tail = prev;
			}
			return headTail(head, tail);
		}

		public long removeIf(long list, IntPredicate pred) {
			return removeIf(head(list), pred);
		}

		public long removeIf(int head, IntPredicate pred) {
			var x = removeIf0(head, pred);
			int newHead = head(x), newTail = tail(x);
			assert newHead < 0 == newTail < 0;
			if (newHead >= 0 && newTail >= 0) {
				assert !hasPrev(newHead);
				assert !hasNext(newTail);
			}
			return x;
		}

		private long removeIf0(int head, IntPredicate pred) {
			if (isNone(head))
				return headTail(None, None);
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
						return headTail(head, tail);
					}
				} else {
					if (isNone(next)) {
						int tail = curr;
						return headTail(head, tail);
					}
					prev = curr;
				}
				curr = next;
			}
		}

		public long reverse(long list) {
			int head = head(list), tail = tail(list);
			int newHead = reverse(head);
			assert newHead == tail;
			return headTail(tail, head);
		}

		// returns new head
		public int reverse(final int head) {
			final int origPrev = prev(head);
			for (int curr = head;;) {
				int prev = prev(curr), next = next(curr);
				setNext(curr, prev);
				setPrev(curr, next);
				if (isNone(next)) {
					if (!isNone(origPrev))
						throw new IllegalArgumentException("Given start vertex is not a head of a list");
					return curr;
				}
				if (next == head)
					return head;
				curr = next;
			}
		}

		public void clear() {
			Arrays.fill(arr, None);
		}

		public IterTools.Peek.Int iterator(long list) {
			return iterMaybeNone(head(list));
		}

		public IterTools.Peek.Int iterator(int id) {
			Assertions.checkArrayIndex(id, 0, size());
			return iterMaybeNone(id);
		}

		public IterTools.Peek.Int iterMaybeNone(int idMaybeNone) {
			assert idMaybeNone >= 0 || idMaybeNone == None;
			return new Iter(idMaybeNone);
		}

		public IterTools.Peek.Int iterRev(long list) {
			return iterRev(tail(list));
		}

		public IterTools.Peek.Int iterRev(int id) {
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
				p = LinkedList.Doubly.this.next(p);
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
				p = LinkedList.Doubly.this.prev(p);
				return ret;
			}

			@Override
			public int peekNextInt() {
				Assertions.hasNext(this);
				return p;
			}
		}

	}

}
