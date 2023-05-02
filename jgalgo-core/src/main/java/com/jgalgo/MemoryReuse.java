package com.jgalgo;

import java.util.Arrays;
import java.util.function.Supplier;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;

class MemoryReuse {

	static int[] ensureLength(int[] a, int len) {
		return a.length >= len ? a : new int[newLength(a, len)];
	}

	static int[][] ensureLength(int[][] a, int rows, int columns) {
		if (a.length < rows) {
			int oldLen = a.length;
			a = Arrays.copyOf(a, newLength(a, rows));
			Arrays.fill(a, oldLen, a.length, IntArrays.EMPTY_ARRAY);
		}
		for (int r = 0; r < rows; r++)
			if (a[r].length < columns)
				a[r] = Arrays.copyOf(a[r], newLength(a[r], columns));
		return a;
	}

	static double[] ensureLength(double[] a, int len) {
		return a.length >= len ? a : new double[newLength(a, len)];
	}

	static double[][] ensureLength(double[][] a, int rows, int columns) {
		if (a.length < rows) {
			int oldLen = a.length;
			a = Arrays.copyOf(a, newLength(a, rows));
			Arrays.fill(a, oldLen, a.length, DoubleArrays.EMPTY_ARRAY);
		}
		for (int r = 0; r < rows; r++)
			if (a[r].length < columns)
				a[r] = Arrays.copyOf(a[r], newLength(a[r], columns));
		return a;
	}

	static boolean[] ensureLength(boolean[] a, int len) {
		return a.length >= len ? a : new boolean[newLength(a, len)];
	}

	static <T> T[] ensureLength(T[] a, int len) {
		return a.length >= len ? a : Arrays.copyOf(a, newLength(a, len));
	}

	static <T> T ensureAllocated(T a, Supplier<? extends T> builder) {
		return a != null ? a : builder.get();
	}

	private static int newLength(int[] a, int len) {
		return Math.max(a.length * 2, len);
	}

	private static int newLength(double[] a, int len) {
		return Math.max(a.length * 2, len);
	}

	private static int newLength(boolean[] a, int len) {
		return Math.max(a.length * 2, len);
	}

	private static <T> int newLength(T[] a, int len) {
		return Math.max(a.length * 2, len);
	}

	static final EdgeIter[] EmptyEdgeIterArr = new EdgeIter[0];
	static final IntList[] EmptyIntListArr = new IntList[0];
	static final UGraph[] EmptyUGraphArr = new UGraph[0];
	static final Weights.Double[] EmptyWeightsDoubleArr = new Weights.Double[0];
	static final TreePathMaxima.Queries[] EmptyTpmQueriesArr = new TreePathMaxima.Queries[0];

}
