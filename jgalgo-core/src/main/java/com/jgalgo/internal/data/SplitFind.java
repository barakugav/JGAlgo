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

import com.jgalgo.internal.util.BuilderAbstract;

/**
 * Data structure of a finite set of elements supporting split and find operations.
 * <p>
 * The Split Find data structure stores a collection of disjoint sets. Each such set has some representative element,
 * which is an arbitrary element from the set. The data structure is created with a known number of elements, and
 * support two basic operations afterwards:
 * <ul>
 * <li>{@link #find(int)} - find the representative of the set of an element (return the same representative for any
 * element in the set).</li>
 * <li>{@link #split(int)} - split a set into two, to a set with elements smaller than the given element and a set with
 * elements greater or equals the given element.</li>
 * </ul>
 *
 * <pre> {@code
 * SplitFind sf = SplitFind.newBuilder().build();
 * sf.init(5);
 * assert sf.find(1) == sf.find(2);
 * assert sf.find(1) == sf.find(3);
 * assert sf.find(1) == sf.find(4);
 *
 * sf.split(3);
 * assert sf.find(1) == sf.find(2);
 * assert sf.find(1) != sf.find(3);
 * assert sf.find(3) == sf.find(4);
 * }</pre>
 *
 * @see    UnionFind
 *
 * @author Barak Ugav
 */
interface SplitFind {

	/**
	 * Init the data structure with a sequence {@code [0, size)}.
	 *
	 * @param size the size of the sequence
	 */
	void init(int size);

	/**
	 * Find the set an element belongs to
	 *
	 * @param  x an element in the data structure
	 * @return   some element in the set {@code x} belongs to. {@code find(x1)==find(x2)} for two elements {@code x1,x2}
	 *           if and only if they are in the same set.
	 */
	int find(int x);

	/**
	 * Split an element's sequence into two separate sequences relative to the given element
	 * <p>
	 * For example, if the data structure was initialized with size 5, it will contain a single sequence
	 * {@code [0,1,2,3,4]}. After {@code split(2)}, it will contains two sequences {@code [0,1], [2,3,4]}.
	 *
	 * @param x an element in the data structure. The element itself will be included in the greater sequence
	 */
	void split(int x);

	/**
	 * Create a new split-find data structure builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link SplitFind} object.
	 *
	 * @return a new builder that can build {@link SplitFind} objects
	 */
	static SplitFind.Builder newBuilder() {
		return SplitFindMinArray::new;
	}

	/**
	 * A builder for {@link SplitFind} objects.
	 *
	 * @see    SplitFind#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<SplitFind.Builder> {

		/**
		 * Create a new split-find data structure
		 *
		 * @return a new split-find data structure
		 */
		SplitFind build();
	}

}
