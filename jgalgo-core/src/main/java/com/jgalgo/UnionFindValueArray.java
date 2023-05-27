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

package com.jgalgo;

import java.util.Arrays;

/**
 * Array implementation of the Union Find data structure with values.
 * <p>
 * The elements are represented in a continuos array, which is most efficient for storage, and performance as the rate
 * of cache miss is low.
 * <p>
 * The running time of \(m\) operations on \(n\) elements is \(O(m \cdot \alpha (m, n))\) where \(\alpha(\cdot,\cdot)\)
 * is the inverse Ackermann's function. The inverse Ackermann's function is extremely slow and for any practical use
 * should be treated as constant.
 *
 * @author Barak Ugav
 */
class UnionFindValueArray extends UnionFindArray implements UnionFindValue {

	private double[] deltas;

	/**
	 * Create an empty Union Find data structure with no elements.
	 */
	UnionFindValueArray() {
		this(0);
	}


	/**
	 * Create a new empty Union Find data structure with expected number of elements.
	 *
	 * @param expectedSize the expended number of elements in the data structure
	 */
	UnionFindValueArray(int expectedSize) {
		super(expectedSize);
		int arrSize = expectedSize == 0 ? 2 : expectedSize;
		deltas = new double[arrSize];
	}

	@Override
	public int make(double value) {
		if (deltas.length <= size)
			deltas = Arrays.copyOf(deltas, size * 2);
		int x = super.make();
		deltas[x] = value;
		return x;
	}

	@Override
	public int make() {
		return make(0);
	}

	@Override
	public double getValue(int x) {
		if (x < 0 || x >= size)
			throw new IndexOutOfBoundsException(x);
		int[] p = parent;
		double sum = 0;
		for (int r = x;; r = p[r]) {
			if (p[r] == NO_PARENT) {
				pathCompression(x, r, sum);
				return sum + deltas[r];
			}
			sum += deltas[r];
		}
	}

	@Override
	public void addValue(int x, double value) {
		deltas[find(x)] += value;
	}

	@Override
	int find0(int x) {
		int[] p = parent;

		/* Find root and calc delta sum (not including r's delta) */
		double sum = 0;
		int r;
		for (r = x; p[r] != NO_PARENT; r = p[r])
			sum += deltas[r];
		pathCompression(x, r, sum);

		return r;
	}

	// deltaSum shouldn't include the delta of the root
	private void pathCompression(int x, int r, double deltaSum) {
		int[] p = parent;
		for (; x != r;) {
			double delta = deltas[x];
			deltas[x] = deltaSum;
			deltaSum -= delta;

			int next = p[x];
			p[x] = r;
			x = next;
		}
	}

	@Override
	void unionSetParent(int c, int p) {
		parent[c] = p;
		deltas[c] -= deltas[p];
	}

}
