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

class IndexPairingHeapBase {

	int minRoot;
	private final int[] nodeData;

	IndexPairingHeapBase(int size) {
		nodeData = new int[3 * size];
		Arrays.fill(nodeData, -1);
		minRoot = -1;
	}

	public boolean isEmpty() {
		return minRoot < 0;
	}

	public boolean isNotEmpty() {
		return minRoot >= 0;
	}

	public int findMin() {
		return minRoot;
	}

	public boolean isInserted(int node) {
		return prevOrParent(node) >= 0 || minRoot == node;
	}

	void cut(int n) {
		int next = next(n);
		if (next >= 0) {
			prevOrParent(next, prevOrParent(n));
			this.next(n, -1);
		}
		if (child(prevOrParent(n)) == n) { /* n.parent.child == n */
			child(prevOrParent(n), next);
		} else {
			next(prevOrParent(n), next);
		}
		prevOrParent(n, -1);
	}

	void addChild(int parent, int newChild) {
		assert prevOrParent(newChild) < 0;
		assert next(newChild) < 0;
		int oldChild = child(parent);
		if (oldChild >= 0) {
			prevOrParent(oldChild, newChild);
			next(newChild, oldChild);
		}
		child(parent, newChild);
		prevOrParent(newChild, parent);
	}

	public void clear() {
		if (minRoot < 0)
			return;

		for (int p = minRoot;;) {
			while (child(p) >= 0) {
				p = child(p);
				while (next(p) >= 0)
					p = next(p);
			}
			int prev = prevOrParent(p);
			if (prev < 0)
				break;
			prevOrParent(p, -1);
			if (next(prev) == p) {
				next(prev, -1);
			} else {
				child(prev, -1);
			}
			p = prev;
		}

		minRoot = -1;
	}

	int next(int node) {
		return nodeData[node * 3 + 0];
	}

	void next(int node, int next) {
		nodeData[node * 3 + 0] = next;
	}

	int prevOrParent(int node) {
		return nodeData[node * 3 + 1];
	}

	void prevOrParent(int node, int prevOrParent) {
		nodeData[node * 3 + 1] = prevOrParent;
	}

	int child(int node) {
		return nodeData[node * 3 + 2];
	}

	void child(int node, int child) {
		nodeData[node * 3 + 2] = child;
	}

}
