package com.jgalgo;

import java.util.Objects;

/**
 * Static RMQ algorithm using {@code O(n log n)} space and answering a query in
 * {@code O(1)} time.
 * <p>
 * An array of size {@code [log n][n]} is created, and at each entry
 * {@code [k][i]} the index of the minimum element in range {@code [i, i+k)} is
 * stored. A query can be answered by two access to the array, giving a total
 * query time of {@code O(1)}.
 *
 * @author Barak Ugav
 */
public class RMQStaticPowerOf2Table implements RMQStatic {

	@Override
	public RMQStatic.DataStructure preProcessSequence(RMQStaticComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException("Invalid length: " + n);
		Objects.requireNonNull(c);
		return new DS(c, n);
	}

	private static class DS implements RMQStatic.DataStructure {
		private int n;
		private int[][] arr;
		private RMQStaticComparator c;

		DS(RMQStaticComparator c, int n) {
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
		public int findMinimumInRange(int i, int j) {
			if (!(0 <= i && i <= j && j < n))
				throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
			if (i == j)
				return i;
			j++;

			int k = Utils.log2(j - i);
			int kSize = 1 << k;

			int idx0 = arr[k - 1][i];
			int idx1 = arr[k - 1][j - kSize];
			return c.compare(idx0, idx1) < 0 ? idx0 : idx1;
		}
	}

}