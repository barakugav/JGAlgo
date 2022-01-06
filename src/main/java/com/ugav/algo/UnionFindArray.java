package com.ugav.algo;

import java.util.Arrays;

public class UnionFindArray implements UnionFind {

	private int[] parent;
	private byte[] rank;
	private int size;

	private static final int NO_PARENT = -1;

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
			throw new IllegalArgumentException();

		/* Find root */
		int r;
		for (r = x; parent[r] != NO_PARENT; r = parent[r])
			;

		/* path compression */
		for (; x != r;) {
			int next = parent[x];
			parent[x] = r;
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

		if (rank[a] < rank[b]) {
			int temp = a;
			a = b;
			b = temp;
		} else if (rank[a] == rank[b])
			rank[a]++;

		parent[b] = a;
		return a;
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
