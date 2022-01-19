package com.ugav.algo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class HeapBinary<E> extends HeapAbstract<E> {

	private E[] arr;
	private int size;
	private final Comparator<? super E> c;

	public HeapBinary() {
		this(null);
	}

	public HeapBinary(Comparator<? super E> c) {
		this.c = c != null ? c : Utils.getDefaultComparator();
		arr = newArr(16);
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}

	private void grow() {
		E[] old = arr;
		arr = newArr(arr.length * 2);
		System.arraycopy(old, 0, arr, 0, size);
	}

	@Override
	public Handle<E> insert(E e) {
		if (arr.length == size)
			grow();

		moveUp(size, e);
		size++;

		return null;
	}

	@Override
	public boolean remove(Object e0) {
		@SuppressWarnings("unchecked")
		E e = (E) e0;
		int s = size;
		E[] a = arr;

		int i;
		for (i = 0; i < s; i++)
			if (c.compare(e, a[i]) == 0)
				break;
		if (i == s)
			return false; /* not found */

		remove(i);
		return true;
	}

	@Override
	public void clear() {
		Arrays.fill(arr, 0, size, null);
		size = 0;
	}

	@Override
	public E findMin() {
		if (size == 0)
			throw new IllegalStateException();
		return arr[0];
	}

	private void remove(int idx) {
		E[] a = arr;

		E old = a[idx];
		E e = a[size-- - 1];
		a[size] = null;

		if (c.compare(e, old) <= 0)
			moveUp(idx, e);
		else
			moveDown(idx, e);
	}

	@Override
	public E extractMin() {
		if (size == 0)
			throw new IllegalStateException();
		E min = arr[0];
		remove(0);
		return min;
	}

	@Override
	public boolean addAll(Collection<? extends E> other) {
		if (other.isEmpty())
			return false;
		int combinedSize = size + other.size();
		if (arr.length <= combinedSize) {
			E[] old = arr;
			arr = newArr(Math.max(arr.length * 2, combinedSize * 3 / 2));
			System.arraycopy(old, 0, arr, 0, size);
		}

		int reconstructionCost = combinedSize;
		int addAllCost = other.size() + Utils.log2ceil(combinedSize);
		if (reconstructionCost >= addAllCost) {
			for (E e : other)
				add(e);
		} else {
			E[] a = arr;
			int s = size;
			for (E e : other)
				a[s++] = e;
			size = s;

			if (s > 1) {
				int lastLayer = Utils.log2ceil(s + 1) - 1;
				int lastParent = (1 << lastLayer) - 2;
				for (int parent = lastParent; parent >= 0; parent--)
					moveDown(parent, a[parent]);
			}
		}

		return true;
	}

	@Override
	public Iterator<E> iterator() {
		return new It();
	}

	private class It implements Iterator<E> {

		int i;

		It() {
			i = 0;
		}

		@Override
		public boolean hasNext() {
			return i < size;
		}

		@Override
		public E next() {
			if (i >= size)
				throw new NoSuchElementException();
			return arr[i++];
		}

	}

	private void moveUp(int i, E e) {
		E[] a = arr;

		for (;;) {
			int p;
			if (i == 0 || c.compare(e, a[p = (i - 1) / 2]) >= 0) { /* reached root or parent is smaller */
				a[i] = e;
				return;
			}

			/* e is smaller than parent, continue up */
			a[i] = a[p];
			i = p;
		}
	}

	private void moveDown(int i, E e) {
		E[] a = arr;
		int s = size;

		for (;;) {
			int c01i, c0i = i * 2 + 1, c1i = i * 2 + 2;
			if (c0i >= s)
				break;

			E c01, c0 = a[c0i], c1;
			if (c1i < s && c.compare(c1 = a[c1i], c0) < 0) {
				c01i = c1i;
				c01 = c1;
			} else {
				c01i = c0i;
				c01 = c0;
			}

			if (c.compare(e, c01) <= 0)
				break;

			/* continue down */
			a[i] = c01;
			i = c01i;
		}
		a[i] = e;
	}

	@SuppressWarnings("unchecked")
	private static <E> E[] newArr(int n) {
		return (E[]) new Object[n];
	}

}
