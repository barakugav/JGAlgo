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
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * A bitmap of fixed size number of bits.
 *
 * @author Barak Ugav
 */
public class Bitmap implements IntIterable {

	private final int size;
	private final long[] words;

	private static final int WordSize = Long.SIZE;
	private static final long WORD_MASK = 0xffffffffffffffffL;

	/**
	 * Creates a new bitmap of the specified size.
	 *
	 * <p>
	 * The {@code size} is the number of bits this bitmap will manage, and it can not be changed during the lifetime of
	 * the bitmap. All bits are initially {@code false}.
	 *
	 * @param size the number of bits
	 */
	public Bitmap(int size) {
		if (size < 0)
			throw new IllegalArgumentException("Negative size: " + size);
		words = new long[(size + WordSize - 1) / WordSize];
		this.size = size;
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
	 * Set the bit at the specified index to the specified value.
	 *
	 * @param  idx                       the index of the bit
	 * @param  val                       the value to set
	 * @throws IndexOutOfBoundsException if the specified index is no in range [0, size)
	 */
	public void set(int idx, boolean val) {
		checkIdx(idx);
		if (val) {
			words[word(idx)] |= bit(idx);
		} else {
			words[word(idx)] &= ~bit(idx);
		}
	}

	/**
	 * Set the bit at the specified index to {@code true}.
	 *
	 * @param  idx                       the index of the bit
	 * @throws IndexOutOfBoundsException if the specified index is no in range [0, size)
	 */
	public void set(int idx) {
		checkIdx(idx);
		words[word(idx)] |= bit(idx);
	}

	/**
	 * Set all bits to {@code val}.
	 *
	 * @param val the value to set
	 */
	public void setAll(boolean val) {
		if (val) {
			setAll();
		} else {
			clear();
		}
	}

	/**
	 * Set all bits to {@code true}.
	 */
	public void setAll() {
		if (words.length == 0)
			return;
		for (int i = 0; i < words.length - 1; i++)
			words[i] = ~0L;
		words[words.length - 1] = size % WordSize == 0 ? ~0L : 1L << (size % WordSize) - 1;
	}

	/**
	 * Set the bit at the specified index to {@code false}.
	 *
	 * @param  idx                       the index of the bit
	 * @throws IndexOutOfBoundsException if the specified index is no in range [0, size)
	 */
	public void clear(int idx) {
		checkIdx(idx);
		words[word(idx)] &= ~bit(idx);
	}

	/**
	 * Set all bits to {@code false}.
	 */
	public void clear() {
		Arrays.fill(words, 0L);
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

	private static int word(int idx) {
		return idx / WordSize;
	}

	private static long bit(int idx) {
		return 1L << (idx % WordSize);
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

	private void checkIdx(int idx) {
		if (idx < 0 || idx >= size)
			throw new IndexOutOfBoundsException("Index: " + idx + ", Size: " + size);
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

}
