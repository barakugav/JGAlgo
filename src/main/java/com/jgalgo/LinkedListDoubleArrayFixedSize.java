package com.jgalgo;

import java.util.Arrays;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntIterator;

interface LinkedListDoubleArrayFixedSize {

	static final int None = -1;

	static LinkedListDoubleArrayFixedSize newInstance(int n) {
		if (n <= 1 << (Byte.SIZE - 1)) {
			return new ImplByte(n);
		} else if (n <= 1 << (Short.SIZE - 1)) {
			return new ImplShort(n);
		} else {
			return new ImplInt(n);
		}
	}

	int size();

	int next(int id);

	void setNext(int id, int next);

	boolean hasNext(int id);

	int prev(int id);

	void setPrev(int id, int prev);

	boolean hasPrev(int id);

	default void insert(int prev, int id) {
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

	default void connect(int prev, int next) {
		if (hasNext(prev) || hasPrev(next))
			throw new IllegalArgumentException();
		setNext(prev, next);
		setPrev(next, prev);
	}

	default void disconnect(int id) {
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

	void clear();

	default IntIterator iterator(int id) {
		if (!(0 <= id && id < size()))
			throw new IndexOutOfBoundsException(id);
		return new IntIterator() {
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
				p = LinkedListDoubleArrayFixedSize.this.next(p);
				return ret;
			}

		};
	}

	abstract static class Abstract implements LinkedListDoubleArrayFixedSize {

		@Override
		public int next(int id) {
			return arrGet(idxOfNext(id));
		}

		@Override
		public void setNext(int id, int next) {
			arrSet(idxOfNext(id), next);
		}

		@Override
		public boolean hasNext(int id) {
			return arrGet(idxOfNext(id)) != None;
		}

		@Override
		public int prev(int id) {
			return arrGet(idxOfPrev(id));
		}

		@Override
		public void setPrev(int id, int prev) {
			arrSet(idxOfPrev(id), prev);
		}

		@Override
		public boolean hasPrev(int id) {
			return arrGet(idxOfPrev(id)) != None;
		}

		abstract int arrGet(int idx);

		abstract void arrSet(int idx, int val);

		private static int idxOfNext(int id) {
			return id * 2 + 0;
		}

		private static int idxOfPrev(int id) {
			return id * 2 + 1;
		}
	}

	static class ImplByte extends Abstract {

		final byte[] a;

		ImplByte(int n) {
			a = new byte[n * 2];
			Arrays.fill(a, (byte) None);
		}

		@Override
		public int size() {
			return a.length / 2;
		}

		@Override
		int arrGet(int idx) {
			return a[idx];
		}

		@Override
		void arrSet(int idx, int val) {
			assert val < Byte.MAX_VALUE;
			a[idx] = (byte) val;
		}

		@Override
		public void clear() {
			Arrays.fill(a, (byte) None);
		}
	}

	static class ImplShort extends Abstract {

		final short[] a;

		ImplShort(int n) {
			a = new short[n * 2];
			Arrays.fill(a, (short) None);
		}

		@Override
		public int size() {
			return a.length / 2;
		}

		@Override
		int arrGet(int idx) {
			return a[idx];
		}

		@Override
		void arrSet(int idx, int val) {
			assert val < Short.MAX_VALUE;
			a[idx] = (short) val;
		}

		@Override
		public void clear() {
			Arrays.fill(a, (short) None);
		}
	}

	static class ImplInt extends Abstract {

		final int[] a;

		ImplInt(int n) {
			a = new int[n * 2];
			Arrays.fill(a, None);
		}

		@Override
		public int size() {
			return a.length / 2;
		}

		@Override
		int arrGet(int idx) {
			return a[idx];
		}

		@Override
		void arrSet(int idx, int val) {
			a[idx] = val;
		}

		@Override
		public void clear() {
			Arrays.fill(a, None);
		}
	}

}
