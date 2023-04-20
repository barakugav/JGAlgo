package com.jgalgo;

import java.util.Arrays;

/**
 * Array implementation of the Union Find data structure with values.
 * <p>
 * The elements are represented in a continuos array, which is most efficient
 * for storage, and performance as the rate of cache miss is low.
 * <p>
 * The running time of {@code m} operations on the data structure is
 * {@code O(m \alpha (m, n))} where {@code \alpha} is the inverse Ackermann's
 * function. The inverse Ackermann's function is extremely slow and for any
 * practical use should be treated as constant.
 *
 * @author Barak Ugav
 */
public class UnionFindValueArray extends UnionFindArray implements UnionFindValue {

	private double[] deltas;

	/**
	 * Create an empty Union Find data structure with no elements.
	 */
	public UnionFindValueArray() {
		this(0);
	}

	/**
	 * Create a new Union Find data structure with {@code n} elements with ids
	 * {@code 0,1,2,...,n-1}, all with value {@code 0}.
	 *
	 * @param n the number of initial elements in the data structure
	 */
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
			throw new IndexOutOfBoundsException(x);
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
