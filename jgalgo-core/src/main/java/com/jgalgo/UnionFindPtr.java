package com.jgalgo;

import java.util.Arrays;

/**
 * Pointer based implementation for the Union Find data structure.
 * <p>
 * Each element is represented as a Object allocated on the heap. This
 * implementation is usually out-performed by the {@link UnionFindArray}
 * implementation.
 * <p>
 * The running time of \(m\) operations on \(n\) elements is
 * \(O(m \cdot \alpha (m, n))\) where \(\alpha(\cdot,\cdot)\) is the inverse
 * Ackermann's function. The inverse Ackermann's function is extremely slow and
 * for any practical use should be treated as constant.
 *
 * @see UnionFindArray
 * @author Barak Ugav
 */
public class UnionFindPtr implements UnionFind {

	private Elm[] elements;
	private int size;

	/**
	 * Create an empty Union Find data structure with no elements.
	 */
	public UnionFindPtr() {
		this(0);
	}

	/**
	 * Create a new Union Find data structure with \(n\) elements with ids
	 * {@code 0,1,2,...,n-1}, each of them form a set of a single element.
	 *
	 * @param n the number of initial elements in the data structure
	 */
	public UnionFindPtr(int n) {
		if (n < 0)
			throw new IllegalArgumentException("n is negative: " + n);
		elements = new Elm[n == 0 ? 2 : n];
		for (int i = 0; i < n; i++)
			elements[i] = new Elm(i);
		size = n;
	}

	@Override
	public int make() {
		if (elements.length <= size)
			elements = Arrays.copyOf(elements, size * 2);
		int x = size++;
		elements[x] = new Elm(x);
		return x;
	}

	@Override
	public int find(int x) {
		return find0(x).idx;
	}

	private Elm find0(int x) {
		if (x < 0 || x >= size)
			throw new IllegalArgumentException();
		Elm e = elements[x];

		/* Find root */
		Elm r;
		for (r = e; r.parent != null; r = r.parent)
			;

		/* path compression */
		for (; e != r;) {
			Elm next = e.parent;
			e.parent = r;
			e = next;
		}

		return r;
	}

	@Override
	public int union(int aIdx, int bIdx) {
		Elm a = find0(aIdx);
		Elm b = find0(bIdx);
		if (a == b)
			return a.idx;

		if (a.rank < b.rank) {
			Elm temp = a;
			a = b;
			b = temp;
		} else if (a.rank == b.rank)
			a.rank++;

		b.parent = a;
		return a.idx;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		for (int i = 0; i < size; i++) {
			Elm e = elements[i];
			e.parent = null;
			elements[i] = null;
		}
		size = 0;
	}

	private static class Elm {

		final int idx;
		Elm parent;
		byte rank;

		Elm(int idx) {
			this.idx = idx;
			parent = null;
			rank = 0;
		}

	}

}
