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
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntPredicate;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;

/**
 * A bitmap of fixed size number of bits.
 *
 * @author Barak Ugav
 */
public class Bitmap extends BitmapBase {

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
		super(size);
	}

	/**
	 * Creates a new bitmap of the specified size, with the provided {@code true} 'ones' bits.
	 *
	 * @param  size the number of bits
	 * @param  ones the 'ones' bits, {@code true} bits
	 * @return      a new bitmap of the specified size, with the provided {@code true} 'ones' bits
	 */
	public static Bitmap fromOnes(int size, int[] ones) {
		return fromOnes(size, IntIterators.wrap(ones));
	}

	/**
	 * Creates a new bitmap of the specified size, with the provided {@code true} 'ones' bits.
	 *
	 * @param  size the number of bits
	 * @param  ones the 'ones' bits, {@code true} bits
	 * @return      a new bitmap of the specified size, with the provided {@code true} 'ones' bits
	 */
	public static Bitmap fromOnes(int size, IntIterable ones) {
		return fromOnes(size, ones.iterator());
	}

	/**
	 * Creates a new bitmap of the specified size, with the provided {@code true} 'ones' bits.
	 *
	 * @param  size the number of bits
	 * @param  ones the 'ones' bits, {@code true} bits
	 * @return      a new bitmap of the specified size, with the provided {@code true} 'ones' bits
	 */
	public static Bitmap fromOnes(int size, IntIterator ones) {
		Bitmap bitmap = new Bitmap(size);
		while (ones.hasNext())
			bitmap.set(ones.nextInt());
		return bitmap;
	}

	/**
	 * Creates a new bitmap of the specified size, with the specified initial values provided from a predicate.
	 *
	 * @param size      the number of bits
	 * @param predicate the predicate to provide the initial value of each bit
	 */
	public static Bitmap fromPredicate(int size, IntPredicate predicate) {
		Bitmap bitmap = new Bitmap(size);
		Objects.requireNonNull(predicate);
		for (int idx : range(size))
			if (predicate.test(idx))
				bitmap.words[word(idx)] |= bit(idx);
		return bitmap;
	}

	/**
	 * Create a new bitmap from the specified immutable bitmap.
	 *
	 * @param  bitmap the immutable bitmap
	 * @return        a new bitmap from the specified immutable bitmap
	 */
	public static Bitmap of(ImmutableBitmap bitmap) {
		Bitmap b = new Bitmap(bitmap.capacity());
		for (int i : range(b.words.length))
			b.words[i] = bitmap.words[i];
		return b;
	}

	/**
	 * Set the bit at the specified index to the specified value.
	 *
	 * @param  idx                       the index of the bit
	 * @param  val                       the value to set
	 * @throws IndexOutOfBoundsException if the specified index is no in range [0, size)
	 */
	public void set(int idx, boolean val) {
		if (val) {
			set(idx);
		} else {
			clear(idx);
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
		for (int i : range(words.length - 1))
			words[i] = ~0L;
		words[words.length - 1] = lastWordMask();
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
	 * Flip every bit in this bitmap.
	 */
	public void not() {
		if (words.length == 0)
			return;
		for (int i : range(words.length))
			words[i] = ~words[i];
		words[words.length - 1] &= lastWordMask();
	}

	/**
	 * Perform a bitwise or with another bitmap.
	 *
	 * <p>
	 * This method does not create a new bitmap, but modifies this bitmap.
	 *
	 * @param b the other bitmap
	 */
	public void or(Bitmap b) {
		checkSize(this, b);
		for (int i : range(words.length))
			words[i] |= b.words[i];
	}

	private static void checkSize(Bitmap a, Bitmap b) {
		if (a.size != b.size)
			throw new IllegalArgumentException("Bitmaps must be of the same size");
	}

	private long lastWordMask() {
		return size % WordSize == 0 ? ~0L : (1L << (size % WordSize)) - 1;
	}

}
