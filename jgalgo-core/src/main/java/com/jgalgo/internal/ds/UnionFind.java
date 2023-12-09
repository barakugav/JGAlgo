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

import com.jgalgo.alg.AlgorithmBuilderBase;

/**
 * Data structure of a finite set of elements supporting union and find operations.
 *
 * <p>
 * The Union Find data structure stores a collection of disjoint sets. Each such set has some representative element,
 * which is an arbitrary element from the set. Three basic operations are supported:
 * <ul>
 * <li>{@link #make()} - create a new element in a new set.</li>
 * <li>{@link #find(int)} - find the representative of the set of an element (return the same representative for any
 * element in the set).</li>
 * <li>{@link #union(int, int)} - union the sets of two elements.</li>
 * </ul>
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * UnionFind uf = UnionFind.newInstance();
 * int x1 = uf.make();
 * int x2 = uf.make();
 * int x3 = uf.make();
 *
 * assert uf.find(x1) == x1;
 * assert uf.find(x2) == x2;
 * assert uf.find(x3) == x3;
 *
 * uf.union(x1, x2);
 * assert uf.find(x1) == uf.find(x2);
 * assert uf.find(x1) != uf.find(x3);
 * }</pre>
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Disjoint-set_data_structure">Wikipedia</a>
 * @author Barak Ugav
 */
public interface UnionFind {

	/**
	 * Create a new element in a singleton set.
	 *
	 * @return identifier of the new element
	 */
	int make();

	/**
	 * Find the set of an element and get an arbitrary element from it.
	 *
	 * <p>
	 * {@code find(a) == find(b)} if an only if {@code a} and {@code b} are in the same set.
	 *
	 * @param  x element in the data structure
	 * @return   arbitrary element from the set of x
	 */
	int find(int x);

	/**
	 * Union the two sets of {@code a} and {@code b}.
	 *
	 * @param  a the first element
	 * @param  b the second element
	 * @return   arbitrary element from the union of sets of {@code a} and {@code b}.
	 */
	int union(int a, int b);

	/**
	 * Get the number of elements in all the sets in the union find data structure.
	 *
	 * @return number of elements in the data structure
	 */
	int size();

	/**
	 * Clear the data structure by removing all elements from all sets.
	 *
	 * <p>
	 * This method can be used to reuse allocated memory of the data structure.
	 */
	void clear();

	/**
	 * Create a new union find algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link UnionFind} object. The {@link UnionFind.Builder} might
	 * support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link UnionFind}
	 */
	static UnionFind newInstance() {
		return builder().build();
	}

	/**
	 * Create a new union-find data structure builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link UnionFind} objects
	 */
	static UnionFind.Builder builder() {
		return new UnionFind.Builder() {
			int expectedSize;
			String impl;

			@Override
			public UnionFind build() {
				if (impl != null) {
					switch (impl) {
						case "array":
							return new UnionFindArray(expectedSize);
						case "ptr":
							return new UnionFindPtr(expectedSize);
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return new UnionFindArray(expectedSize);
			}

			@Override
			public UnionFind.Builder expectedSize(int expectedSize) {
				if (expectedSize < 0)
					throw new IllegalArgumentException("negative expected size: " + expectedSize);
				this.expectedSize = expectedSize;
				return this;
			}

			@Override
			public void setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						UnionFind.Builder.super.setOption(key, value);
				}
			}
		};
	}

	/**
	 * A builder for {@link UnionFind} objects.
	 *
	 * @see    UnionFind#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new empty union-find data structure.
		 *
		 * @return a new empty union-find data structure
		 */
		UnionFind build();

		/**
		 * Hint the implementation the number of elements that will exists in the union-find data structure.
		 *
		 * @param  expectedSize the expected number of elements that will be in the data structure
		 * @return              this builder
		 */
		UnionFind.Builder expectedSize(int expectedSize);

	}

}
