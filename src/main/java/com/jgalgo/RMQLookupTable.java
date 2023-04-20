package com.jgalgo;

import java.util.Objects;

public class RMQLookupTable implements RMQStatic {

	/*
	 * Naive implementation of RMQ, lookup table for each two indices.
	 *
	 * O(n^2) pre processing time, O(n^2) space, O(1) query.
	 */

	@Override
	public RMQStatic.DataStructure preProcessSequence(RMQComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException("Invalid length: " + n);
		Objects.requireNonNull(c);

		if (n <= DS8.LIMIT)
			return new DS8(c, (byte) n);
		else if (n <= DS128.LIMIT)
			return new DS128(c, (short) n);
		else if (n <= DS32768.LIMIT)
			return new DS32768(c, n);
		else
			throw new IllegalArgumentException("N too big (" + n + ")");
	}

	private static int arrSize(int n) {
		return n * (n - 1) / 2;
	}

	private static int indexOf(int n, int i, int j) {
		return (2 * n - i - 1) * i / 2 + j - i - 1;
	}

	private static class DS8 implements RMQStatic.DataStructure {

		private final byte n;
		private final byte[] arr;
		private static final int LIMIT = 1 << ((Byte.SIZE - 1) / 2);

		DS8(RMQComparator c, byte n) {
			arr = new byte[arrSize(n)];
			this.n = n;

			for (byte i = 0; i < n - 1; i++)
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : (byte) (i + 1);
			for (byte i = 0; i < n - 2; i++) {
				for (byte j = (byte) (i + 2); j < n; j++) {
					byte m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			if (i < 0 || j <= i || j > n)
				throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
			if (i + 1 == j)
				return i;
			return arr[indexOf(n, i, j - 1)];
		}
	}

	private static class DS128 implements RMQStatic.DataStructure {

		private final short[] arr;
		private final short n;

		private static final int LIMIT = 1 << ((Short.SIZE - 1) / 2);

		DS128(RMQComparator c, short n) {
			arr = new short[arrSize(n)];
			this.n = n;

			for (short i = 0; i < n - 1; i++)
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : (short) (i + 1);
			for (short i = 0; i < n - 2; i++) {
				for (short j = (short) (i + 2); j < n; j++) {
					short m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			if (i < 0 || j <= i || j > n)
				throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
			if (i + 1 == j)
				return i;
			return arr[indexOf(n, i, j - 1)];
		}
	}

	private static class DS32768 implements RMQStatic.DataStructure {

		private final int[] arr;
		private final int n;

		private static final int LIMIT = 1 << ((Integer.SIZE - 1) / 2);

		DS32768(RMQComparator c, int n) {
			arr = new int[arrSize(n)];
			this.n = n;

			for (int i = 0; i < n - 1; i++)
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : i + 1;
			for (int i = 0; i < n - 2; i++) {
				for (int j = i + 2; j < n; j++) {
					int m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			if (i < 0 || j <= i || j > n)
				throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
			if (i + 1 == j)
				return i;
			return arr[indexOf(n, i, j - 1)];
		}
	}

}