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
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IterTools;

public class LinkedListFixedSize {
	private LinkedListFixedSize() {}

	public static final int None = -1;

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
			return next(id) != None;
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
					return p != None;
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

	public static class Doubly {

		private final int[] arr;

		public Doubly(int n) {
			arr = new int[n * 2];
			Arrays.fill(arr, None);
		}

		private static int idxOfNext(int id) {
			return id * 2 + 0;
		}

		private static int idxOfPrev(int id) {
			return id * 2 + 1;
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
			return next(id) != None;
		}

		public int prev(int id) {
			return arr[idxOfPrev(id)];
		}

		public void setPrev(int id, int prev) {
			arr[idxOfPrev(id)] = prev;
		}

		public boolean hasPrev(int id) {
			return prev(id) != None;
		}

		public void insert(int prev, int id) {
			// if (hasNext(id) || hasPrev(id))
			// throw new IllegalArgumentException();
			int next = next(prev);
			setNext(prev, id);
			setPrev(id, prev);
			if (next != None) {
				setNext(id, next);
				setPrev(next, id);
			}
		}

		public void connect(int prev, int next) {
			// if (hasNext(prev) || hasPrev(next))
			// throw new IllegalArgumentException();
			setNext(prev, next);
			setPrev(next, prev);
		}

		public void disconnect(int id) {
			int prev = prev(id), next = next(id);
			if (prev != None) {
				setNext(prev, next);
				setPrev(id, None);
			}
			if (next != None) {
				setPrev(next, prev);
				setNext(id, None);
			}
		}

		public void clear() {
			Arrays.fill(arr, None);
		}

		public IterTools.Peek.Int iterator(int id) {
			Assertions.checkArrayIndex(id, 0, size());
			return new Iter(id);
		}

		public IterTools.Peek.Int emptyIter() {
			return new Iter(None);
		}

		private class Iter implements IterTools.Peek.Int {

			int p;

			Iter(int start) {
				p = start;
			}

			@Override
			public boolean hasNext() {
				return p != None;
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

	}

}
