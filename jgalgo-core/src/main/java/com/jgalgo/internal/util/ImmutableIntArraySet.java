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
import java.util.Objects;
import java.util.function.IntPredicate;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

public final class ImmutableIntArraySet extends AbstractIntSet {

	final int[] arr;
	final int from, to, size;
	private final IntPredicate containsFunc;
	private int hashCode = 0;

	private ImmutableIntArraySet(int[] arr, int from, int to, IntPredicate containsFunc) {
		Assertions.checkArrayFromTo(from, to, arr.length);
		this.arr = Objects.requireNonNull(arr);
		this.from = from;
		this.to = to;
		this.size = to - from;
		this.containsFunc = containsFunc;

		assert new IntOpenHashSet(this).size() == size : "Duplicate elements in array";
	}

	public static IntSet newInstance(int[] arr, IntPredicate containsFunc) {
		return newInstance(arr, 0, arr.length, containsFunc);
	}

	public static IntSet newInstance(int[] arr, int from, int to, IntPredicate containsFunc) {
		return new ImmutableIntArraySet(arr, from, to, Objects.requireNonNull(containsFunc));
	}

	public static IntSet withNaiveContains(int[] arr) {
		return withNaiveContains(arr, 0, arr.length);
	}

	public static IntSet withNaiveContains(int[] arr, int from, int to) {
		return new ImmutableIntArraySet(arr, from, to, null);
	}

	public static IntSet withBitmap(Bitmap bitmap) {
		return withBitmap(bitmap.toArray(), bitmap);
	}

	public static IntSet withBitmap(int[] arr, int bitmapSize) {
		Bitmap bitmap = new Bitmap(bitmapSize);
		for (int i : arr)
			bitmap.set(i);
		return withBitmap(arr, bitmap);
	}

	public static IntSet withBitmap(IntCollection elms, int bitmapSize) {
		if (!(elms instanceof ImmutableIntArraySet))
			return withBitmap(elms.toIntArray(), bitmapSize);

		ImmutableIntArraySet elms0 = (ImmutableIntArraySet) elms;
		Bitmap bitmap = new Bitmap(bitmapSize);
		for (int x : elms)
			bitmap.set(x);
		return withBitmap(elms0.arr, elms0.from, elms0.to, bitmap);
	}

	private static IntSet withBitmap(int[] arr, Bitmap bitmap) {
		return withBitmap(arr, 0, arr.length, bitmap);
	}

	private static IntSet withBitmap(int[] arr, int from, int to, Bitmap bitmap) {
		Objects.requireNonNull(bitmap);
		return newInstance(arr, from, to, key -> 0 <= key && key < bitmap.capacity() && bitmap.get(key));
	}

	@Override
	public boolean contains(int key) {
		return containsFunc != null ? containsFunc.test(key) : range(from, to).anyMatch(i -> key == arr[i]);
	}

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
		for (int i : range(from, to))
			a[i - from] = Integer.valueOf(arr[i]);
		return a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size)
			a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		for (int i : range(from, to))
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
		if (other instanceof ImmutableIntArraySet && hashCode() != other.hashCode())
			return false;
		return super.equals(other);
	}

	@Override
	public IntIterator iterator() {
		return IntIterators.wrap(arr, from, to - from);
	}

}
