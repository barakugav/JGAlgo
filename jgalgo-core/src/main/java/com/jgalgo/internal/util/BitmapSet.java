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

import java.util.Arrays;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;

public final class BitmapSet extends AbstractIntSet {

	private final int capacity;
	private final long[] words;

	private int size;
	private final int[] ones;

	private static final int WordSize = Long.SIZE;

	public BitmapSet(int capacity) {
		if (capacity < 0)
			throw new IllegalArgumentException("Negative capacity: " + capacity);
		this.capacity = capacity;
		words = new long[wordsNum(capacity)];
		ones = new int[capacity];
	}

	private static int wordsNum(int size) {
		return (size + WordSize - 1) / WordSize;
	}

	private void checkIdx(int idx) {
		checkIdx(idx, capacity);
	}

	private static void checkIdx(int idx, int size) {
		if (idx < 0 || idx >= size)
			throw new IndexOutOfBoundsException("Index: " + idx + ", Size: " + size);
	}

	private static int word(int idx) {
		return idx / WordSize;
	}

	private static long bit(int idx) {
		return 1L << (idx % WordSize);
	}

	/**
	 * Check whether the bit at the specified index is set ({@code true}).
	 *
	 * @param  idx                       the index of the bit
	 * @return                           {@code true} if the bit is set, {@code false} otherwise
	 * @throws IndexOutOfBoundsException if the specified index is no in range [0, size)
	 */
	public boolean get(int idx) {
		checkIdx(idx);
		return (words[word(idx)] & bit(idx)) != 0;
	}

	/**
	 * Set the bit at the specified index to {@code true}.
	 *
	 * @param  idx                       the index of the bit
	 * @return                           {@code true} if the bit was modified
	 * @throws IndexOutOfBoundsException if the specified index is no in range [0, size)
	 */
	public boolean set(int idx) {
		if (get(idx))
			return false;
		words[word(idx)] |= bit(idx);
		ones[size++] = idx;
		return true;
	}

	public int pop() {
		if (size == 0)
			throw new IllegalStateException("Empty set");
		int idx = ones[--size];
		words[word(idx)] &= ~bit(idx);
		return idx;
	}

	/**
	 * Set all bits to {@code false}.
	 */
	@Override
	public void clear() {
		boolean perBitClear = size <= capacity / WordSize;
		if (perBitClear) {
			for (int idx : ones)
				words[word(idx)] &= ~bit(idx);
		} else {
			Arrays.fill(words, 0L);
		}
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}

	public int capacity() {
		return capacity;
	}

	@Override
	public boolean add(int key) {
		return set(key);
	}

	@Override
	public boolean contains(int key) {
		return 0 <= key && key < capacity && get(key);
	}

	@Override
	public IntIterator iterator() {
		return IntIterators.wrap(ones, 0, size);
	}

	@Override
	public boolean remove(int key) {
		throw new UnsupportedOperationException();
	}

}
