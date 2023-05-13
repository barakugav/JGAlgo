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

import java.util.Arrays;
import java.util.NoSuchElementException;

class LinkedListFixedSize {
	private LinkedListFixedSize() {}

	static final int None = -1;

	private static interface WithNext {
		int next(int id);

		int size();

		default Utils.IterPeekable.Int iterator(int id) {
			if (!(0 <= id && id < size()))
				throw new IndexOutOfBoundsException(id);
			return new Utils.IterPeekable.Int() {
				int p = id;

				@Override
				public boolean hasNext() {
					return p != None;
				}

				@Override
				public int nextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					int ret = p;
					p = LinkedListFixedSize.WithNext.this.next(p);
					return ret;
				}

				@Override
				public int peekNext() {
					if (!hasNext())
						throw new NoSuchElementException();
					return p;
				}

			};
		}
	}

	static class Singly implements WithNext {

		private final Array arr;

		Singly(int n) {
			if (n < 0)
				throw new IllegalArgumentException("negative size: " + n);
			int bitsNum = n == 0 ? 1 : (32 - Integer.numberOfLeadingZeros(n - 1));
			arr = Array.newInstance(bitsNum, n);
			arr.fill(None);
		}

		@Override
		public int size() {
			return arr.length();
		}

		@Override
		public int next(int id) {
			return arr.get(id);
		}

		void setNext(int id, int next) {
			arr.set(id, next);
		}

		boolean hasNext(int id) {
			return next(id) != None;
		}

		void clear() {
			arr.fill(None);
		}

	}

	static class Doubly implements WithNext {

		private final Array arr;

		Doubly(int n) {
			if (n < 0)
				throw new IllegalArgumentException("negative size: " + n);
			int bitsNum = n == 0 ? 1 : (32 - Integer.numberOfLeadingZeros(n - 1));
			arr = Array.newInstance(bitsNum, n * 2);
			arr.fill(None);
		}

		private static int idxOfNext(int id) {
			return id * 2 + 0;
		}

		private static int idxOfPrev(int id) {
			return id * 2 + 1;
		}

		@Override
		public int size() {
			return arr.length() / 2;
		}

		@Override
		public int next(int id) {
			return arr.get(idxOfNext(id));
		}

		void setNext(int id, int next) {
			arr.set(idxOfNext(id), next);
		}

		boolean hasNext(int id) {
			return next(id) != None;
		}

		int prev(int id) {
			return arr.get(idxOfPrev(id));
		}

		void setPrev(int id, int prev) {
			arr.set(idxOfPrev(id), prev);
		}

		boolean hasPrev(int id) {
			return prev(id) != None;
		}

		void insert(int prev, int id) {
			if (hasNext(id) || hasPrev(id))
				throw new IllegalArgumentException();
			int next = next(prev);
			setNext(prev, id);
			setPrev(id, prev);
			if (next != None) {
				setNext(id, next);
				setPrev(next, id);
			}
		}

		void connect(int prev, int next) {
			if (hasNext(prev) || hasPrev(next))
				throw new IllegalArgumentException();
			setNext(prev, next);
			setPrev(next, prev);
		}

		void disconnect(int id) {
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

		void clear() {
			arr.fill(None);
		}

	}

	private static interface Array {
		int length();

		int get(int idx);

		void set(int idx, int val);

		void fill(int val);

		static Array newInstance(int valBits, int length) {
			if (valBits <= java.lang.Byte.SIZE - 1) {
				return new Array.Byte(length);
			} else if (valBits <= java.lang.Short.SIZE - 1) {
				return new Array.Short(length);
			} else {
				return new Array.Int(length);
			}
		}

		static class Byte implements Array {

			final byte[] arr;

			Byte(int len) {
				arr = new byte[len];
			}

			@Override
			public int length() {
				return arr.length;
			}

			@Override
			public int get(int idx) {
				return arr[idx];
			}

			@Override
			public void set(int idx, int val) {
				assert val == -1 || (val & 0xff) == val;
				arr[idx] = (byte) val;
			}

			@Override
			public void fill(int val) {
				assert val == -1 || (val & 0xff) == val;
				Arrays.fill(arr, (byte) val);
			}
		}

		static class Short implements Array {

			final short[] arr;

			Short(int len) {
				arr = new short[len];
			}

			@Override
			public int length() {
				return arr.length;
			}

			@Override
			public int get(int idx) {
				return arr[idx];
			}

			@Override
			public void set(int idx, int val) {
				assert val == -1 || (val & 0xffff) == val;
				arr[idx] = (short) val;
			}

			@Override
			public void fill(int val) {
				assert val == -1 || (val & 0xffff) == val;
				Arrays.fill(arr, (short) val);
			}
		}

		static class Int implements Array {

			final int[] arr;

			Int(int len) {
				arr = new int[len];
			}

			@Override
			public int length() {
				return arr.length;
			}

			@Override
			public int get(int idx) {
				return arr[idx];
			}

			@Override
			public void set(int idx, int val) {
				arr[idx] = val;
			}

			@Override
			public void fill(int val) {
				Arrays.fill(arr, val);
			}
		}
	}

}
