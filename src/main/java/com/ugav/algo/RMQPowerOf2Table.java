package com.ugav.algo;

import java.util.Objects;

public class RMQPowerOf2Table implements RMQ {

	private RMQPowerOf2Table() {
	}

	private static final RMQPowerOf2Table INSTANCE = new RMQPowerOf2Table();

	public static RMQPowerOf2Table getInstace() {
		return INSTANCE;
	}

	static void preprocessRMQ(PowerOf2Table table) {
		RMQ.Comparator c = table.c;
		int n = table.n;

		for (int i = 0; i < n - 1; i++)
			table.arr[0][i] = c.compare(i, i + 1) < 0 ? i : i + 1;

		for (int k = 1; k < table.arr.length; k++) {
			int pkSize = 1 << k;
			int kSize = 1 << (k + 1);
			for (int i = 0; i < n - kSize + 1; i++) {
				int idx0 = table.arr[k - 1][i];
				int idx1 = table.arr[k - 1][Math.min(i + pkSize, n - pkSize)];
				table.arr[k][i] = c.compare(idx0, idx1) < 0 ? idx0 : idx1;
			}
		}
	}

	@Override
	public RMQ.Result preprocessRMQ(RMQ.Comparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);

		PowerOf2Table table = new PowerOf2Table(n, c);

		preprocessRMQ(table);

		return table;
	}

	static class PowerOf2Table implements RMQ.Result {

		final int n;
		final int arr[][];
		final RMQ.Comparator c;

		PowerOf2Table(int n, RMQ.Comparator c) {
			this.n = n;
			arr = new int[Utils.log2ceil(n + 1) - 1][n - 1];
			this.c = c;
		}

		@Override
		public int query(int i, int j) {
			if (i < 0 || j <= i || j > n)
				throw new IllegalArgumentException();
			if (i + 1 == j)
				return i;

			int k = Utils.log2(j - i);
			int kSize = 1 << k;

			int idx0 = arr[k - 1][i];
			int idx1 = arr[k - 1][j - kSize];
			return c.compare(idx0, idx1) < 0 ? idx0 : idx1;

		}
	}

}