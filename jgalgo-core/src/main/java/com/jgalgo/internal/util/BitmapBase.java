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

import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class BitmapBase implements IntIterable {

	final int size;
	final long[] words;

	static final int WordSize = Long.SIZE;
	static final long WORD_MASK = 0xffffffffffffffffL;

	BitmapBase(int size) {
		checkSize(size);
		this.size = size;
		words = new long[wordsNum(size)];
	}

	BitmapBase(int size, long[] words) {
		checkSize(size);
		if (words.length != wordsNum(size))
			throw new IllegalArgumentException("Wrong number of words: " + words.length);
		this.size = size;
		this.words = words;
	}

	static void checkSize(int size) {
		if (size < 0)
			throw new IllegalArgumentException("Negative size: " + size);
	}

	static int wordsNum(int size) {
		return (size + WordSize - 1) / WordSize;
	}

	void checkIdx(int idx) {
		checkIdx(idx, size);
	}

	static void checkIdx(int idx, int size) {
		if (idx < 0 || idx >= size)
			throw new IndexOutOfBoundsException("Index: " + idx + ", Size: " + size);
	}

	static int word(int idx) {
		return idx / WordSize;
	}

	static long bit(int idx) {
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
	 * Check whether all bits are set to {@code false}.
	 *
	 * @return {@code true} if all bits are {@code false}, {@code false} otherwise
	 */
	public boolean isEmpty() {
		for (long word : words)
			if (word != 0)
				return false;
		return true;
	}

	/**
	 * Returns the index of the first bit that is set to {@code true} that occurs on or after the specified starting
	 * index. If no such bit exists then {@code -1} is returned.
	 *
	 * @param  fromIndex                 the index to start checking from (inclusive)
	 * @return                           the index of the next set bit, or {@code -1} if there is no such bit
	 * @throws IndexOutOfBoundsException if the specified index is no in range [0, size)
	 */
	public int nextSetBit(int fromIndex) {
		checkIdx(fromIndex);

		int wIdx = word(fromIndex);
		long word = words[wIdx] & (WORD_MASK << fromIndex);
		while (true) {
			if (word != 0)
				return (wIdx * WordSize) + Long.numberOfTrailingZeros(word);
			if (++wIdx == words.length)
				return -1;
			word = words[wIdx];
		}
	}

	/**
	 * Returns the index of the first bit that is set to {@code false} that occurs on or after the specified starting
	 * index.
	 *
	 * @param  fromIndex                 the index to start checking from (inclusive)
	 * @return                           the index of the next clear bit
	 * @throws IndexOutOfBoundsException if the specified index is no in range [0, size)
	 */
	public int nextClearBit(int fromIndex) {
		checkIdx(fromIndex);

		int wIdx = word(fromIndex);
		long word = ~words[wIdx] & (WORD_MASK << fromIndex);

		while (true) {
			if (word != 0)
				return (wIdx * WordSize) + Long.numberOfTrailingZeros(word);
			if (++wIdx == words.length)
				return words.length * WordSize;
			word = ~words[wIdx];
		}
	}

	/**
	 * Returns the number of bits in this bitmap.
	 *
	 * @return the number of bits in this bitmap
	 */
	public int capacity() {
		return size;
	}

	/**
	 * Returns the number of bits set to {@code true} in this bitmap.
	 *
	 * @return the number of bits set to {@code true} in this bitmap
	 */
	public int cardinality() {
		int sum = 0;
		for (long word : words)
			sum += Long.bitCount(word);
		return sum;
	}

	/**
	 * Returns an iterator that iterate over the bits current set to {@code true}.
	 *
	 * @return an iterator that iterate over the bits current set to {@code true}
	 */
	@Override
	public IntIterator iterator() {
		return new IntIterator() {

			int bit = nextSetBit(0);

			@Override
			public boolean hasNext() {
				return bit != -1;
			}

			@Override
			public int nextInt() {
				Assertions.Iters.hasNext(this);
				int ret = bit;
				if (bit + 1 < size) {
					bit = nextSetBit(bit + 1);
				} else {
					bit = -1;
				}
				return ret;
			}
		};
	}

	/**
	 * Returns an array containing all of the {@code true} bits in this bitmap in proper sequence (from first to last
	 * bit).
	 *
	 * @return an array containing all of the {@code true} bits in this bitmap
	 */
	public int[] toArray() {
		int[] arr = new int[cardinality()];
		int i = 0;
		for (int b : this)
			arr[i++] = b;
		return arr;
	}

	/**
	 * Returns a copy of this bitmap.
	 *
	 * @return a copy of this bitmap
	 */
	public Bitmap copy() {
		Bitmap ret = new Bitmap(size);
		for (int i = 0; i < words.length; i++)
			ret.words[i] = words[i];
		return ret;
	}

	@Override
	public String toString() {
		return ImmutableIntArraySet.withNaiveContains(toArray()).toString();
	}

}
