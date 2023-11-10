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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

public abstract class ImmutableIntArraySet extends AbstractIntSet {

	final int[] arr;
	final int from, to, size;

	public ImmutableIntArraySet(int[] arr) {
		this(arr, 0, arr.length);
	}

	public ImmutableIntArraySet(int[] arr, int from, int to) {
		if (!(0 <= from && from <= to && to <= arr.length))
			throw new IndexOutOfBoundsException();
		this.arr = Objects.requireNonNull(arr);
		this.from = from;
		this.to = to;
		this.size = to - from;

		assert new IntOpenHashSet(this).size() == size : "Duplicate elements in array";
	}

	@Override
	public abstract boolean contains(int key);

	@Override
	public int size() {
		return size;
	}

	@Override
	public int[] toIntArray() {
		return Arrays.copyOfRange(arr, from, to);
	}

	@Override
	public int[] toArray(int[] a) {
		if (a.length >= size) {
			System.arraycopy(arr, from, a, 0, size);
			return a;
		} else {
			return toIntArray();
		}
	}

	@Override
	public Object[] toArray() {
		if (isEmpty())
			return ObjectArrays.EMPTY_ARRAY;
		Object[] a = new Object[size];
		for (int i = from; i < to; i++)
			a[i - from] = Integer.valueOf(arr[i]);
		return a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size)
			a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		for (int i = from; i < to; i++)
			a[i - from] = (T) Integer.valueOf(arr[i]);
		return a;
	}

	@Override
	public IntIterator iterator() {
		return new Iter();
	}

	private class Iter implements IntIterator {

		private int idx = from;

		@Override
		public boolean hasNext() {
			return idx < to;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			return arr[idx++];
		}

	}

	public static ImmutableIntArraySet withNaiveContains(int[] arr) {
		return new WithNaiveContains(arr);
	}

	public static ImmutableIntArraySet withNaiveContains(int[] arr, int from, int to) {
		return new WithNaiveContains(arr, from, to);
	}

	public static ImmutableIntArraySet ofBitmap(Bitmap bitmap) {
		int s = bitmap.capacity();
		return new ImmutableIntArraySet(bitmap.toArray()) {
			@Override
			public boolean contains(int key) {
				return 0 <= key && key < s && bitmap.get(key);
			}
		};
	}

	private static class WithNaiveContains extends ImmutableIntArraySet {

		WithNaiveContains(int[] arr) {
			this(arr, 0, arr.length);
		}

		WithNaiveContains(int[] arr, int from, int to) {
			super(arr, from, to);
		}

		@Override
		public boolean contains(int key) {
			for (int i = from; i < to; i++)
				if (key == arr[i])
					return true;
			return false;
		}

	}

}
