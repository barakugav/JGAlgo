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
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Array implementation of the Union Find data structure with values.
 *
 * <p>
 * The elements are represented in a continuos array, which is most efficient for storage, and performance as the rate
 * of cache miss is low.
 *
 * <p>
 * The running time of \(m\) operations on \(n\) elements is \(O(m \cdot \alpha (m, n))\) where \(\alpha(\cdot,\cdot)\)
 * is the inverse Ackermann's function. The inverse Ackermann's function is extremely slow and for any practical use
 * should be treated as constant.
 *
 * @author Barak Ugav
 */
final class UnionFindValueArray extends UnionFindArray implements UnionFindValue {

	private double[] deltas;

	/**
	 * Create an empty Union Find data structure with no elements.
	 */
	UnionFindValueArray() {
		deltas = new double[2];
	}

	@Override
	public int make(double value) {
		int x = super.make();
		deltas[x] = value;
		return x;
	}

	@Override
	public int make() {
		return make(0);
	}

	@Override
	public IntSet makeMany(int count) {
		IntSet elms = super.makeMany(count);
		for (int x : elms)
			deltas[x] = 0;
		return elms;
	}

	@Override
	void ensureCapacity(int capacity) {
		super.ensureCapacity(capacity);
		if (deltas.length < capacity)
			deltas = Arrays.copyOf(deltas, Math.max(deltas.length * 2, capacity));
	}

	@Override
	public double getValue(int x) {
		assert 0 <= x && x < size;
		double sum = 0;
		int r;
		for (r = x; hasParent(r); r = parent[r])
			sum += deltas[r];
		pathCompression(x, r, sum);
		return sum + deltas[r];
	}

	@Override
	public void addValue(int x, double value) {
		deltas[find(x)] += value;
	}

	@Override
	int find0(int x) {
		/* Find root and calc delta sum (not including r's delta) */
		double sum = 0;
		int r;
		for (r = x; hasParent(r); r = parent[r])
			sum += deltas[r];
		pathCompression(x, r, sum);
		return r;
	}

	// deltaSum shouldn't include the delta of the root
	private void pathCompression(int x, int r, double deltaSum) {
		while (x != r) {
			double delta = deltas[x];
			deltas[x] = deltaSum;
			deltaSum -= delta;

			int next = parent[x];
			parent[x] = r;
			x = next;
		}
	}

	@Override
	void unionSetParent(int c, int p) {
		parent[c] = p;
		deltas[c] -= deltas[p];
	}

}
