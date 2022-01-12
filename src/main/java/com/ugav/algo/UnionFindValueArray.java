package com.ugav.algo;

import java.util.Arrays;

public class UnionFindValueArray extends UnionFindArray implements UnionFindValue {

	private double[] deltas;

	public UnionFindValueArray() {
		this(0);
	}

	public UnionFindValueArray(int n) {
		super(n);
		int arrSize = n == 0 ? 2 : n;
		deltas = new double[arrSize];
	}

	@Override
	public int make(double value) {
		if (deltas.length <= size)
			deltas = Arrays.copyOf(deltas, size * 2);
		int x = super.make();
		deltas[x] = value;
		return x;
	}

	@Override
	public int make() {
		return make(0);
	}

	@Override
	public double getValue(int x) {
		if (x < 0 || x >= size)
			throw new IllegalArgumentException();
		int[] p = parent;
		double sum = 0;
		for (int r = x;; r = p[r]) {
			if (p[r] == NO_PARENT) {
				pathCompression(x, r, sum);
				return sum + deltas[r];
			}
			sum += deltas[r];
		}
	}

	@Override
	public void addValue(int x, double value) {
		deltas[find(x)] += value;
	}

	@Override
	int find0(int x) {
		int[] p = parent;

		/* Find root and calc delta sum (not including r's delta) */
		double sum = 0;
		int r;
		for (r = x; p[r] != NO_PARENT; r = p[r])
			sum += deltas[r];
		pathCompression(x, r, sum);

		return r;
	}

	// deltaSum shouldn't include the delta of the root
	private void pathCompression(int x, int r, double deltaSum) {
		int[] p = parent;
		for (; x != r;) {
			double delta = deltas[x];
			deltas[x] = deltaSum;
			deltaSum -= delta;

			int next = p[x];
			p[x] = r;
			x = next;
		}
	}

	@Override
	void unionSetParent(int c, int p) {
		parent[c] = p;
		deltas[c] -= deltas[p];
	}

}
