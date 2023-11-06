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

import java.util.Arrays;

/**
 * Array implementation of the Union Find data structure.
 *
 * <p>
 * The elements are represented in a continuos array, which is most efficient for storage, and performance as the rate
 * of cache miss is low. This implementation should be used as the default implementation for the {@link UnionFind}
 * interface.
 *
 * <p>
 * The running time of \(m\) operations on \(n\) elements is \(O(m \cdot \alpha (m, n))\) where \(\alpha(\cdot,\cdot)\)
 * is the inverse Ackermann's function. The inverse Ackermann's function is extremely slow and for any practical use
 * should be treated as constant.
 *
 * @author Barak Ugav
 */
class UnionFindArray implements UnionFind {

	int[] parent;
	int size;

	/**
	 * Create an empty Union Find data structure with no elements.
	 */
	UnionFindArray() {
		this(0);
	}

	/**
	 * Create a new empty Union Find data structure with expected number of elements.
	 *
	 * @param expectedSize the expended number of elements in the data structure
	 */
	UnionFindArray(int expectedSize) {
		if (expectedSize < 0)
			throw new IllegalArgumentException("negative expected size: " + expectedSize);
		int arrSize = expectedSize == 0 ? 2 : expectedSize;
		parent = new int[arrSize];
		Arrays.fill(parent, -1);
		size = 0;
	}

	boolean hasParent(int x) {
		return parent[x] >= 0;
	}

	@Override
	public int make() {
		if (parent.length <= size) {
			int oldLength = parent.length;
			parent = Arrays.copyOf(parent, size * 2);
			Arrays.fill(parent, oldLength, parent.length, -1);
		}
		return size++;
	}

	@Override
	public int find(int x) {
		if (x < 0 || x >= size)
			throw new IndexOutOfBoundsException(x);
		return find0(x);
	}

	int find0(int x) {
		/* Find root */
		int r;
		for (r = x; hasParent(r); r = parent[r]);

		/* path compression */
		while (x != r) {
			int next = parent[x];
			parent[x] = r;
			x = next;
		}

		return r;
	}

	@Override
	public int union(int a, int b) {
		a = find(a);
		b = find(b);
		if (a == b)
			return a;

		int aRank = getRank(a);
		int bRank = getRank(b);
		if (aRank < bRank) {
			int temp = a;
			a = b;
			b = temp;
		} else if (aRank == bRank) {
			aRank++;
			setRank(a, aRank);
		}

		unionSetParent(b, a);
		return a;
	}

	void unionSetParent(int c, int p) {
		parent[c] = p;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		Arrays.fill(parent, 0, size, -1);
		size = 0;
	}

	int getRank(int idx) {
		int rank = parent[idx];
		assert rank < 0;
		return -rank;
	}

	void setRank(int idx, int rank) {
		assert rank > 0;
		parent[idx] = -rank;
	}

}
