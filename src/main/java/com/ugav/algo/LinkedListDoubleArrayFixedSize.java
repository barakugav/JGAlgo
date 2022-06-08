package com.ugav.algo;

import java.util.NoSuchElementException;

class LinkedListDoubleArrayFixedSize {

	private final Impl impl;
	private static final int none = -1;

	LinkedListDoubleArrayFixedSize(int n) {
		int len = n * 2;
		if (len <= 1 << Byte.SIZE)
			impl = new ImplByte(len);
		else if (len <= 1 << Short.SIZE)
			impl = new ImplShort(len);
		else
			impl = new ImplInt(len);

		for (int i = 0; i < len; i++)
			impl.set(i, none);
	}

	int size() {
		return impl.legnth() / 2;
	}

	int next(int x) {
		return impl.get(x * 2);
	}

	void setNext(int x, int y) {
		impl.set(x * 2, y);
	}

	boolean hasNext(int x) {
		return next(x) != none;
	}

	int prev(int x) {
		return impl.get(x * 2 + 1);
	}

	void setPrev(int x, int y) {
		impl.set(x * 2 + 1, y);
	}

	boolean hasPrev(int x) {
		return prev(x) != none;
	}

	void add(int l, int x) {
		if (next(x) != none || prev(x) != none)
			throw new IllegalArgumentException();
		int n = next(l);
		setNext(l, x);
		setPrev(x, l);
		if (n != none) {
			setNext(x, n);
			setPrev(n, x);
		}
	}

	void remove(int x) {
		int p = prev(x), n = next(x);
		if (p != none) {
			setNext(p, n);
			setPrev(x, none);
		}
		if (n != none) {
			setPrev(n, p);
			setNext(x, none);
		}
	}

	void clear(int l) {
		for (int n; (n = next(l)) != none; l = n) {
			setNext(l, none);
			setPrev(n, none);
		}
	}

	void clear() {
		int len = impl.legnth();
		for (int i = 0; i < len; i++)
			impl.set(i, none);
	}

	IteratorInt iterator(int x) {
		checkIdx(x);
		return new Iter(x);
	}

	private void checkIdx(int x) {
		if (!(0 <= x && x < size()))
			throw new IllegalArgumentException();
	}

	private class Iter implements IteratorInt {

		int p;

		Iter(int x) {
			p = x;
		}

		@Override
		public boolean hasNext() {
			return p != none;
		}

		@Override
		public int next() {
			if (!hasNext())
				throw new NoSuchElementException();
			int ret = p;
			p = LinkedListDoubleArrayFixedSize.this.next(p);
			return ret;
		}

	}

	private static interface Impl {

		int legnth();

		int get(int idx);

		void set(int idx, int val);

	}

	private static class ImplByte implements Impl {

		final byte[] a;

		ImplByte(int len) {
			assert len <= 1 << Byte.SIZE;
			a = new byte[len];
		}

		@Override
		public int legnth() {
			return a.length;
		}

		@Override
		public int get(int idx) {
			return a[idx];
		}

		@Override
		public void set(int idx, int val) {
			assert val < Byte.MAX_VALUE;
			a[idx] = (byte) val;
		}

	}

	private static class ImplShort implements Impl {

		final short[] a;

		ImplShort(int len) {
			assert len <= 1 << Short.SIZE;
			a = new short[len];
		}

		@Override
		public int legnth() {
			return a.length;
		}

		@Override
		public int get(int idx) {
			return a[idx];
		}

		@Override
		public void set(int idx, int val) {
			assert val < Short.MAX_VALUE;
			a[idx] = (short) val;
		}

	}

	private static class ImplInt implements Impl {

		final int[] a;

		ImplInt(int len) {
			a = new int[len];
		}

		@Override
		public int legnth() {
			return a.length;
		}

		@Override
		public int get(int idx) {
			return a[idx];
		}

		@Override
		public void set(int idx, int val) {
			a[idx] = val;
		}

	}

}
