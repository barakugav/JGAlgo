/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.internal.util;

import static com.jgalgo.internal.util.Range.range;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Supplier;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.ints.IntArrays;

public class MemoryReuse {

	private MemoryReuse() {}

	public static class IntArr {
		private int[] arr = IntArrays.DEFAULT_EMPTY_ARRAY;

		public int[] alloc(int size, int initVal) {
			boolean newAllocation = size > arr.length;
			if (newAllocation)
				arr = new int[size];
			if (!newAllocation || initVal != 0)
				Arrays.fill(arr, 0, size, initVal);
			return arr;
		}

		public int[] alloc(int size) {
			return alloc(size, 0);
		}
	}

	public static class ObjArr<T> {
		private T[] arr;

		public ObjArr(T[] emptyArr) {
			arr = emptyArr;
		}

		@SuppressWarnings("unchecked")
		public T[] alloc(int size, T initVal) {
			boolean newAllocation = size > arr.length;
			if (newAllocation)
				arr = (T[]) Array.newInstance(arr.getClass().getComponentType(), size);
			if (!newAllocation || initVal != null)
				Arrays.fill(arr, 0, size, initVal);
			return arr;
		}

		public T[] alloc(int size) {
			return alloc(size, null);
		}
	}

	public static int[] ensureLength(int[] a, int len) {
		return a.length >= len ? a : new int[newLength(a, len)];
	}

	public static int[][] ensureLength(int[][] a, int rows, int columns) {
		if (a.length < rows) {
			int oldLen = a.length;
			a = Arrays.copyOf(a, newLength(a, rows));
			Arrays.fill(a, oldLen, a.length, IntArrays.EMPTY_ARRAY);
		}
		for (int r : range(rows))
			if (a[r].length < columns)
				a[r] = Arrays.copyOf(a[r], newLength(a[r], columns));
		return a;
	}

	public static double[] ensureLength(double[] a, int len) {
		return a.length >= len ? a : new double[newLength(a, len)];
	}

	public static double[][] ensureLength(double[][] a, int rows, int columns) {
		if (a.length < rows) {
			int oldLen = a.length;
			a = Arrays.copyOf(a, newLength(a, rows));
			Arrays.fill(a, oldLen, a.length, DoubleArrays.EMPTY_ARRAY);
		}
		for (int r : range(rows))
			if (a[r].length < columns)
				a[r] = Arrays.copyOf(a[r], newLength(a[r], columns));
		return a;
	}

	public static boolean[] ensureLength(boolean[] a, int len) {
		return a.length >= len ? a : new boolean[newLength(a, len)];
	}

	public static <T> T[] ensureLength(T[] a, int len) {
		return a.length >= len ? a : Arrays.copyOf(a, newLength(a, len));
	}

	public static <T> T ensureAllocated(T a, Supplier<? extends T> builder) {
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

}
