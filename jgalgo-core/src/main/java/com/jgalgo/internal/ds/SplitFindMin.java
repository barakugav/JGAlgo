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

package com.jgalgo.internal.ds;

import java.util.Collection;
import java.util.Comparator;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * An extension to {@link SplitFind} that support value keys and {@code findMin} operations.
 *
 * <p>
 * As the {@link SplitFind}, a data structure implementing this interface maintain a collection disjoint sets and
 * support {@link #find(int)} and {@link #split(int)} operations. In addition, each element have a key, which is
 * comparable to any other key by a provided comparator, and the minimum key in each set can be queried using
 * {@link #findMin(int)}.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
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

	@SuppressWarnings("unchecked")
	@Override
	default void init(int size) {
		init(ObjectList.of((K[]) new Object[size]), (k1, k2) -> 0);
	}

	/**
	 * Get the key associated with an element.
	 *
	 * @param  x an element in the data structure
	 * @return   the key associated with the element
	 */
	K getKey(int x);

	/**
	 * Find the element with the minimum key in the sequence of {@code x}.
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
	 * Create a new split-find-min object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link SplitFindMin} object.
	 *
	 * @param  <K> the keys type
	 * @return     a default implementation of {@link SplitFindMin}
	 */
	static <K> SplitFindMin<K> newInstance() {
		return new SplitFindMinArray<>();
	}

}
