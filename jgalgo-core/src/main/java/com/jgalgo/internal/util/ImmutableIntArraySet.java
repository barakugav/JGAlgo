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
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

public abstract class ImmutableIntArraySet extends AbstractIntSet {

	final int[] arr;
	final int from, to, size;
	private int hashCode = 0;

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
	public int hashCode() {
		if (hashCode == 0)
			hashCode = super.hashCode();
		return hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ImmutableIntArraySet) {
			ImmutableIntArraySet o = (ImmutableIntArraySet) other;
			if (hashCode() != o.hashCode())
				return false;
		}
		return super.equals(other);
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
		int[] arr = bitmap.toArray();
		return new WithBitmap(arr, 0, arr.length, bitmap);
	}

	public static ImmutableIntArraySet ofBitmap(int[] arr, int bitmapSize) {
		Bitmap bitmap = new Bitmap(bitmapSize);
		for (int i : arr)
			bitmap.set(i);
		return new WithBitmap(arr, 0, arr.length, bitmap);
	}

	public static ImmutableIntArraySet ofBitmap(IntCollection elms, int bitmapSize) {
		if (!(elms instanceof ImmutableIntArraySet))
			return ofBitmap(elms.toIntArray(), bitmapSize);

		ImmutableIntArraySet elms0 = (ImmutableIntArraySet) elms;
		if (elms0 instanceof WithBitmap && ((WithBitmap) elms0).bitmap.capacity() == bitmapSize)
			return elms0;
		Bitmap bitmap = new Bitmap(bitmapSize);
		for (int i = elms0.from; i < elms0.to; i++)
			bitmap.set(elms0.arr[i]);
		return new WithBitmap(elms0.arr, elms0.from, elms0.to, bitmap);
	}

	private static class WithBitmap extends ImmutableIntArraySet {
		private final Bitmap bitmap;

		WithBitmap(int[] arr, int from, int to, Bitmap bitmap) {
			super(arr, from, to);
			this.bitmap = bitmap;
		}

		@Override
		public boolean contains(int key) {
			return 0 <= key && key < bitmap.capacity() && bitmap.get(key);
		}
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
