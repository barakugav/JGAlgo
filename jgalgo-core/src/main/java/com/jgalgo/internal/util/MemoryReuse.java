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

	public static int[] alloc(int[] a, int size) {
		return a.length >= size ? a : new int[newLength(a.length, size)];
	}

	public static int[] alloc(int[] a, int size, int initVal) {
		a = alloc(a, size);
		Arrays.fill(a, 0, size, initVal);
		return a;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] alloc(T[] a, int size) {
		return a.length >= size ? a
				: (T[]) Array.newInstance(a.getClass().getComponentType(), newLength(a.length, size));
	}

	public static <T> T[] alloc(T[] a, int size, T initVal) {
		a = alloc(a, size);
		Arrays.fill(a, 0, size, initVal);
		return a;
	}

	public static class IntArr {
		private int[] arr = IntArrays.DEFAULT_EMPTY_ARRAY;

		public int[] alloc(int size) {
			return arr = MemoryReuse.alloc(arr, size);
		}

		public int[] alloc(int size, int initVal) {
			return arr = MemoryReuse.alloc(arr, size, initVal);
		}
	}

	public static class ObjArr<T> {
		private T[] arr;

		public ObjArr(T[] emptyArr) {
			arr = emptyArr;
		}

		public T[] alloc(int size, T initVal) {
			return arr = MemoryReuse.alloc(arr, size, initVal);
		}

		public T[] alloc(int size) {
			return arr = MemoryReuse.alloc(arr, size);
		}
	}

	public static int[][] ensureLength(int[][] a, int rows, int columns) {
		if (a.length < rows) {
			int oldLen = a.length;
			a = Arrays.copyOf(a, newLength(a.length, rows));
			Arrays.fill(a, oldLen, a.length, IntArrays.EMPTY_ARRAY);
		}
		for (int r : range(rows))
			if (a[r].length < columns)
				a[r] = Arrays.copyOf(a[r], newLength(a[r].length, columns));
		return a;
	}

	public static double[] ensureLength(double[] a, int size) {
		return a.length >= size ? a : new double[newLength(a.length, size)];
	}

	public static double[][] ensureLength(double[][] a, int rows, int columns) {
		if (a.length < rows) {
			int oldLen = a.length;
			a = Arrays.copyOf(a, newLength(a.length, rows));
			Arrays.fill(a, oldLen, a.length, DoubleArrays.EMPTY_ARRAY);
		}
		for (int r : range(rows))
			if (a[r].length < columns)
				a[r] = Arrays.copyOf(a[r], newLength(a[r].length, columns));
		return a;
	}

	public static boolean[] ensureLength(boolean[] a, int size) {
		return a.length >= size ? a : new boolean[newLength(a.length, size)];
	}

	public static <T> T ensureAllocated(T a, Supplier<? extends T> builder) {
		return a != null ? a : builder.get();
	}

	private static <T> int newLength(int arrLen, int size) {
		return Math.max(arrLen * 2, size);
	}

}
