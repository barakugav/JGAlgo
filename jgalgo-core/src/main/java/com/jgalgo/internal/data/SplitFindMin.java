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

package com.jgalgo.internal.data;

import java.util.Collection;
import java.util.Comparator;
import com.jgalgo.internal.util.Utils;

/**
 * An extension to {@link SplitFind} that support value keys and {@code findMin} operations.
 * <p>
 * As the {@link SplitFind}, a data structure implementing this interface maintain a collection disjoint sets and
 * support {@link #find(int)} and {@link #split(int)} operations. In addition, each element have a key, which is
 * comparable to any other key by a provided comparator, and the minimum key in each set can be queried using
 * {@link #findMin(int)}.
 *
 * @param  <K> the keys type
 * @author     Barak Ugav
 */
public interface SplitFindMin<K> extends SplitFind {

	/**
	 * Init the data structure with a sequence {@code [0, keys.size())} with the given keys.
	 *
	 * @param keys       collection of keys. The size of the collection determine the number of elements in the data
	 *                       structure
	 * @param comparator a comparator to compare the keys of the elements, if {@code null} the default comparator will
	 *                       be used (Comparable interface)
	 */
	void init(Collection<K> keys, Comparator<? super K> comparator);

	@Override
	default void init(int size) {
		init(Utils.nullList(size), (k1, k2) -> 0);
	}

	/**
	 * Get the key associated with an element.
	 *
	 * @param  x an element in the data structure
	 * @return   the key associated with the element
	 */
	K getKey(int x);

	/**
	 * Find the element with the minimum key in the sequence of {@code x}
	 *
	 * @param  x an element in the data structure
	 * @return   the element with the minimum key in the sequence of {@code x}
	 */
	int findMin(int x);

	/**
	 * Decrease the key of an element.
	 *
	 * @param  x      an element in the data structure
	 * @param  newKey new key for the element
	 * @return        {@code true} if the decreased key is the minimum key in the element's sequence
	 */
	boolean decreaseKey(int x, K newKey);

	/**
	 * Create a new split-find-min data structure builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link SplitFindMin} object.
	 *
	 * @return a new builder that can build {@link SplitFindMin} objects
	 */
	static SplitFindMin.Builder newBuilder() {
		return SplitFindMinArray::new;
	}

	/**
	 * A builder for {@link SplitFindMin} objects.
	 *
	 * @see    SplitFindMin#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends SplitFind.Builder {

		/**
		 * Create a new split-find-min data structure
		 *
		 * @return     a new split-find-min data structure
		 * @param  <K> the keys type
		 */
		<K> SplitFindMin<K> buildWithFindMin();

		@SuppressWarnings("rawtypes")
		@Override
		default SplitFindMin build() {
			return buildWithFindMin();
		}

		@Override
		default SplitFindMin.Builder setOption(String key, Object value) {
			SplitFind.Builder.super.setOption(key, value);
			return this;
		}
	}

}
