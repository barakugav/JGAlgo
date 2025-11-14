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

import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;

public class IndexPairingHeapDouble extends IndexPairingHeapBase implements IndexHeapDouble {

	private double[] key;

	public IndexPairingHeapDouble(double[] key) {
		super(key.length);
		this.key = key;
	}

	public IndexPairingHeapDouble(int size) {
		this(new double[size]);
	}

	@Override
	public double key(int node) {
		return key[node];
	}

	@Override
	public void insert(int node, double key) {
		this.key[node] = key;
		insertNode(node);
	}

	private void insertNode(int node) {
		assert !isInserted(node);
		if (minRoot < 0) {
			minRoot = node;
		} else {
			minRoot = meld(minRoot, node);
		}
	}

	@Override
	public int extractMin() {
		int min = minRoot;
		removeRoot();
		return min;
	}

	@Override
	public void remove(int node) {
		assert isInserted(node);
		if (node != minRoot) {
			cut(node);
			addChild(node, minRoot);
			minRoot = node;
		}
		removeRoot();
	}

	private int meld(int n1, int n2) {
		assert isSubtreeRoot(n1) && isSubtreeRoot(n2);

		/* assume n1 has smaller key than n2 */
		if (key[n1] > key[n2]) {
			int temp = n1;
			n1 = n2;
			n2 = temp;
		}

		addChild(n1, n2);
		return n1;
	}

	private void removeRoot() {
		if (child(minRoot) < 0) {
			minRoot = -1;
			return;
		}

		/* meld pairs from left to right */
		int tail;
		for (int prev = minRoot, next = child(minRoot);;) {
			int n1 = next;
			if (n1 < 0) {
				tail = prev;
				break;
			}

			int n2 = next(n1);
			if (n2 < 0) {
				prevOrParent(n1, prev);
				tail = n1;
				break;
			}
			next = next(n2);
			next(n1, -1);
			next(n2, -1);
			prevOrParent(n1, -1);
			prevOrParent(n2, -1);
			n1 = meld(n1, n2);

			prevOrParent(n1, prev);
			prev = n1;
		}
		child(minRoot, -1);

		/* meld all from right to left */
		int root = tail, prev = prevOrParent(root);
		prevOrParent(root, -1);
		for (;;) {
			int other = prev;
			if (other == minRoot) {
				minRoot = root;
				break;
			}
			prev = prevOrParent(other);
			prevOrParent(other, -1);
			root = meld(root, other);
		}
	}

	@Override
	public void decreaseKey(int node, double newKey) {
		Assertions.heapDecreaseKeyIsSmaller(key[node], newKey, (DoubleComparator) null);
		key[node] = newKey;

		if (node != minRoot) {
			cut(node);
			minRoot = meld(minRoot, node);
		}
	}

	@Override
	public void increaseKey(int node, double newKey) {
		Assertions.heapIncreaseKeyIsGreater(key[node], newKey, (DoubleComparator) null);
		remove(node);
		key[node] = newKey;
		insertNode(node);
	}

}
