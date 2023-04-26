package com.jgalgo;

import java.util.Arrays;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.IntList;

class MemoryReuse {

	static int[] ensureLength(int[] a, int len) {
		return a.length >= len ? a : new int[Math.max(a.length * 2, len)];
	}

	static double[] ensureLength(double[] a, int len) {
		return a.length >= len ? a : new double[Math.max(a.length * 2, len)];
	}

	static <T> T[] ensureLength(T[] a, int len) {
		return a.length >= len ? a : Arrays.copyOf(a, Math.max(a.length * 2, len));
	}

	static <T> T ensureAllocated(T a, Supplier<? extends T> builder) {
		return a != null ? a : builder.get();
	}

	static final EdgeIter[] EmptyEdgeIterArr = new EdgeIter[0];
	static final IntList[] EmptyIntListArr = new IntList[0];

}
