package com.ugav.algo;

import java.util.Objects;

public class RMQPowerOf2Table implements RMQ {

	/*
	 * During preprocessing create a table with log(n) rows and n columns, in each
	 * cell [i][j] store the value min_in_range[j, j + 2^i]. This allows a query in
	 * O(1) by looking at the correct indices.
	 *
	 * O(n log n) preprocessing time, O(n log n) space, O(1) query.
	 */

	private int n;
	private int[][] arr;
	private RMQ.Comparator c;

	public RMQPowerOf2Table() {
	}

	@Override
	public void preprocessRMQ(RMQ.Comparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException("Invalid legnth: " + n);
		Objects.requireNonNull(c);

		this.n = n;
		arr = new int[Utils.log2ceil(n + 1) - 1][n - 1];
		this.c = c;

		for (int i = 0; i < n - 1; i++)
			arr[0][i] = c.compare(i, i + 1) < 0 ? i : i + 1;

		for (int k = 1; k < arr.length; k++) {
			int pkSize = 1 << k;
			int kSize = 1 << (k + 1);
			for (int i = 0; i < n - kSize + 1; i++) {
				int idx0 = arr[k - 1][i];
				int idx1 = arr[k - 1][Math.min(i + pkSize, n - pkSize)];
				arr[k][i] = c.compare(idx0, idx1) < 0 ? idx0 : idx1;
			}
		}
	}

	@Override
	public int calcRMQ(int i, int j) {
		if (arr == null)
			throw new IllegalStateException("Preprocessing is required before query");
		if (i < 0 || j <= i || j > n)
			throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
		if (i + 1 == j)
			return i;

		int k = Utils.log2(j - i);
		int kSize = 1 << k;

		int idx0 = arr[k - 1][i];
		int idx1 = arr[k - 1][j - kSize];
		return c.compare(idx0, idx1) < 0 ? idx0 : idx1;

	}

}