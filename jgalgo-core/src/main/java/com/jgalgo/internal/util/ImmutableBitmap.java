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
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Immutable version of {@link Bitmap}.
 *
 * <p>
 * This class can be used efficiently as keys in hash tables, as we can cache the hash code.
 *
 * @author Barak Ugav
 */
public final class ImmutableBitmap extends BitmapBase {

	private int hashCode;

	ImmutableBitmap(int size, long[] words) {
		super(size, words);
	}

	public static ImmutableBitmap of(Bitmap bitmap) {
		long[] words = bitmap.words.clone();
		return new ImmutableBitmap(bitmap.size, words);
	}

	public static ImmutableBitmap fromOnes(int size, IntIterator ones) {
		checkSize(size);
		long[] words = new long[wordsNum(size)];
		for (int i : IterTools.foreach(ones)) {
			checkIdx(i, size);
			words[word(i)] |= bit(i);
		}
		return new ImmutableBitmap(size, words);
	}

	@Override
	public int hashCode() {
		if (hashCode == 0)
			hashCode = 1 + 31 * size + Arrays.hashCode(words);
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ImmutableBitmap))
			return false;
		ImmutableBitmap o = (ImmutableBitmap) obj;
		return hashCode() == o.hashCode() && size == o.size && Arrays.equals(words, o.words);
	}

}
