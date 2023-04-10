package com.jgalgo;

import java.util.Objects;

public class RMQLookupTable implements RMQ {

	/*
	 * Naive implementation of RMQ, lookup table for each two indices.
	 *
	 * O(n^2) pre processing time, O(n^2) space, O(1) query.
	 */

	private LookupTable table;

	public RMQLookupTable() {
		table = null;
	}

	@Override
	public void preProcessRMQ(RMQComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException("Invalid legnth: " + n);
		Objects.requireNonNull(c);

		LookupTable table;
		if (n <= LookupTable8.LIMIT)
			table = new LookupTable8(n);
		else if (n <= LookupTable128.LIMIT)
			table = new LookupTable128(n);
		else if (n <= LookupTable32768.LIMIT)
			table = new LookupTable32768(n);
		else
			throw new IllegalArgumentException("N too big (" + n + ")");

		for (int i = 0; i < n - 1; i++)
			table.set(indexOf(n, i, i + 1), c.compare(i, i + 1) < 0 ? i : i + 1);
		for (int i = 0; i < n - 2; i++) {
			for (int j = i + 2; j < n; j++) {
				int m = table.get(indexOf(n, i, j - 1));
				table.set(indexOf(n, i, j), c.compare(m, j) < 0 ? m : j);
			}
		}
		this.table = table;
	}

	private static int indexOf(int n, int i, int j) {
		return (2 * n - i - 1) * i / 2 + j - i - 1;
	}

	@Override
	public int calcRMQ(int i, int j) {
		if (table == null)
			throw new IllegalStateException("PreProcessing is required before query");
		if (i < 0 || j <= i || j > table.n)
			throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
		if (i + 1 == j)
			return i;
		return table.calcRMQ(i, j);
	}

	private static abstract class LookupTable {

		final int n;

		LookupTable(int n) {
			this.n = n;
		}

		static int arrSize(int n) {
			return n * (n - 1) / 2;
		}

		abstract int get(int idx);

		abstract void set(int idx, int x);

		abstract int calcRMQ(int i, int j);
	}

	private static class LookupTable8 extends LookupTable {

		final byte[] arr;

		private static final int LIMIT = 1 << ((Byte.SIZE - 1) / 2);

		LookupTable8(int n) {
			super(n);
			arr = new byte[arrSize(n)];
		}

		@Override
		int calcRMQ(int i, int j) {
			return arr[indexOf(n, i, j - 1)];
		}

		@Override
		int get(int idx) {
			return arr[idx];
		}

		@Override
		void set(int idx, int x) {
			arr[idx] = (byte) x;
		}

	}

	private static class LookupTable128 extends LookupTable {

		final short[] arr;

		private static final int LIMIT = 1 << ((Short.SIZE - 1) / 2);

		LookupTable128(int n) {
			super(n);
			arr = new short[arrSize(n)];
		}

		@Override
		int calcRMQ(int i, int j) {
			return arr[indexOf(n, i, j - 1)];
		}

		@Override
		int get(int idx) {
			return arr[idx];
		}

		@Override
		void set(int idx, int x) {
			arr[idx] = (short) x;
		}

	}

	private static class LookupTable32768 extends LookupTable {

		final int[] arr;

		private static final int LIMIT = 1 << ((Integer.SIZE - 1) / 2);

		LookupTable32768(int n) {
			super(n);
			arr = new int[arrSize(n)];
		}

		@Override
		int calcRMQ(int i, int j) {
			return arr[indexOf(n, i, j - 1)];
		}

		@Override
		int get(int idx) {
			return arr[idx];
		}

		@Override
		void set(int idx, int x) {
			arr[idx] = x;
		}

	}

}