package com.ugav.jgalgo;

import java.util.Arrays;

public class UnionFindArray implements UnionFind {

	int[] parent;
	byte[] rank;
	int size;

	static final int NO_PARENT = -1;

	public UnionFindArray() {
		this(0);
	}

	public UnionFindArray(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
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
