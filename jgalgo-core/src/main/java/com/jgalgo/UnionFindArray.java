package com.jgalgo;

import java.util.Arrays;

/**
 * Array implementation of the Union Find data structure.
 * <p>
 * The elements are represented in a continuos array, which is most efficient
 * for storage, and performance as the rate of cache miss is low. This
 * implementation should be used as the default implementation for the
 * {@link UnionFind} interface.
 * <p>
 * The running time of {@code m} operations on the data structure is
 * {@code O(m \alpha (m, n))} where {@code \alpha} is the inverse Ackermann's
 * function. The inverse Ackermann's function is extremely slow and for any
 * practical use should be treated as constant.
 *
 * @author Barak Ugav
 */
public class UnionFindArray implements UnionFind {

	int[] parent;
	byte[] rank;
	int size;

	static final int NO_PARENT = -1;

	/**
	 * Create an empty Union Find data structure with no elements.
	 */
	public UnionFindArray() {
		this(0);
	}

	/**
	 * Create a new Union Find data structure with {@code n} elements with ids
	 * {@code 0,1,2,...,n-1}, each of them form a set of a single element.
	 *
	 * @param n the number of initial elements in the data structure
	 */
	public UnionFindArray(int n) {
		if (n < 0)
			throw new IllegalArgumentException("n is negative: " + n);
		int arrSize = n == 0 ? 2 : n;
		parent = new int[arrSize];
		rank = new byte[arrSize];
		Arrays.fill(parent, NO_PARENT);
		size = n;
	}

	@Override
	public int make() {
		if (parent.length <= size) {
			int oldLength = parent.length;
			parent = Arrays.copyOf(parent, size * 2);
			rank = Arrays.copyOf(rank, size * 2);
			Arrays.fill(parent, oldLength, parent.length, NO_PARENT);
		}
		return size++;
	}

	@Override
	public int find(int x) {
		if (x < 0 || x >= size)
			throw new IllegalArgumentException("Illegal identifier " + x);
		return find0(x);
	}

	int find0(int x) {
		int[] p = parent;

		/* Find root */
		int r;
		for (r = x; p[r] != NO_PARENT; r = p[r])
			;

		/* path compression */
		for (; x != r;) {
			int next = p[x];
			p[x] = r;
			x = next;
		}

		return r;
	}

	@Override
	public int union(int a, int b) {
		a = find(a);
		b = find(b);
		if (a == b)
			return a;
		byte[] r = rank;

		if (r[a] < r[b]) {
			int temp = a;
			a = b;
			b = temp;
		} else if (r[a] == r[b])
			r[a]++;

		unionSetParent(b, a);
		return a;
	}

	void unionSetParent(int c, int p) {
		parent[c] = p;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		Arrays.fill(parent, 0, size, NO_PARENT);
		size = 0;
	}

}
