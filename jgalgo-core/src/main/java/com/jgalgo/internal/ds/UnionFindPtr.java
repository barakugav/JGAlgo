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
 * Pointer based implementation for the Union Find data structure.
 * <p>
 * Each element is represented as a Object allocated on the heap. This implementation is usually out-performed by the
 * {@link UnionFindArray} implementation.
 * <p>
 * The running time of \(m\) operations on \(n\) elements is \(O(m \cdot \alpha (m, n))\) where \(\alpha(\cdot,\cdot)\)
 * is the inverse Ackermann's function. The inverse Ackermann's function is extremely slow and for any practical use
 * should be treated as constant.
 *
 * @see    UnionFindArray
 * @author Barak Ugav
 */
class UnionFindPtr implements UnionFind {

	private Elm[] elements;
	private int size;

	/**
	 * Create an empty Union Find data structure with no elements.
	 */
	UnionFindPtr() {
		this(0);
	}

	/**
	 * Create a new empty Union Find data structure with expected number of elements.
	 *
	 * @param expectedSize the expended number of elements in the data structure
	 */
	UnionFindPtr(int expectedSize) {
		if (expectedSize < 0)
			throw new IllegalArgumentException("negative expected size: " + expectedSize);
		elements = new Elm[expectedSize == 0 ? 2 : expectedSize];
		size = 0;
	}

	@Override
	public int make() {
		if (elements.length <= size)
			elements = Arrays.copyOf(elements, size * 2);
		int x = size++;
		elements[x] = new Elm(x);
		return x;
	}

	@Override
	public int find(int x) {
		return find0(x).idx;
	}

	private Elm find0(int x) {
		if (x < 0 || x >= size)
			throw new IndexOutOfBoundsException(x);
		Elm e = elements[x];

		/* Find root */
		Elm r;
		for (r = e; r.parent != null; r = r.parent);

		/* path compression */
		for (; e != r;) {
			Elm next = e.parent;
			e.parent = r;
			e = next;
		}

		return r;
	}

	@Override
	public int union(int aIdx, int bIdx) {
		Elm a = find0(aIdx);
		Elm b = find0(bIdx);
		if (a == b)
			return a.idx;

		if (a.rank < b.rank) {
			Elm temp = a;
			a = b;
			b = temp;
		} else if (a.rank == b.rank)
			a.rank++;

		b.parent = a;
		return a.idx;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		for (int i = 0; i < size; i++) {
			Elm e = elements[i];
			e.parent = null;
			elements[i] = null;
		}
		size = 0;
	}

	private static class Elm {

		final int idx;
		Elm parent;
		byte rank;

		Elm(int idx) {
			this.idx = idx;
			parent = null;
			rank = 0;
		}

	}

}
