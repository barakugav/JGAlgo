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

/**
 * Union Find with {@code double} values for the elements.
 * <p>
 * This interface is an extension to the {@link UnionFind} interface that support, along with regular operation, value
 * of each elements and addition of some value to all elements of a set using the {@link #addValue(int, double)} method.
 *
 * <pre> {@code
 * UnionFindValue uf = UnionFindValue.newBuilder().build();
 * int x1 = uf.make(4);
 * int x2 = uf.make(11);
 * int x3 = uf.make(6);
 *
 * assert uf.getValue(x1) == 4;
 * assert uf.getValue(x2) == 11;
 * assert uf.getValue(x3) == 6;
 *
 * uf.union(x1, x2);
 * uf.addValue(x1, 20);
 * assert uf.getValue(x1) == 24;
 * assert uf.getValue(x2) == 31;
 * assert uf.getValue(x3) == 6;
 *
 * uf.addValue(x3, -2);
 * assert uf.getValue(x1) == 24;
 * assert uf.getValue(x2) == 31;
 * assert uf.getValue(x3) == 4;
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface UnionFindValue extends UnionFind {

	/**
	 * Create a new element with a given value.
	 *
	 * @param  value the value of the new element
	 * @return       identifier of the new element in the union find data structure
	 */
	int make(double value);

	/**
	 * {@inheritDoc}
	 * <p>
	 * The created element will be assigned a value of {@code 0}.
	 */
	@Override
	default int make() {
		return make(0);
	}

	/**
	 * Get the value of an element.
	 *
	 * @param  x an element in the data structure
	 * @return   value of the element
	 */
	double getValue(int x);

	/**
	 * Add value to ALL elements in the set of a given element.
	 *
	 * @param x     an element in the data structure
	 * @param value value to add to all elements of the set of {@code x}
	 */
	void addValue(int x, double value);

	/**
	 * Create a new union-find with values data structure builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link UnionFindValue} object.
	 *
	 * @return a new builder that can build {@link UnionFindValue} objects
	 */
	static UnionFindValue.Builder newBuilder() {
		return new UnionFindValue.Builder() {
			int expectedSize;

			@Override
			public UnionFindValue build() {
				return new UnionFindValueArray(expectedSize);
			}

			@Override
			public UnionFindValue.Builder expectedSize(int expectedSize) {
				if (expectedSize < 0)
					throw new IllegalArgumentException("negative expected size: " + expectedSize);
				this.expectedSize = expectedSize;
				return this;
			}
		};
	}

	/**
	 * A builder for {@link UnionFindValue} objects.
	 *
	 * @see    UnionFindValue#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends UnionFind.Builder {

		@Override
		UnionFindValue build();

		@Override
		UnionFindValue.Builder expectedSize(int expectedSize);

		@Override
		default UnionFindValue.Builder setOption(String key, Object value) {
			UnionFind.Builder.super.setOption(key, value);
			return this;
		}
	}

}
