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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntComparator;

public class IndexPairingHeapTest extends TestBase {

	@SuppressWarnings("boxing")
	@Test
	public void randOps() {
		IndexHeapTestUtils.<Integer>randOps(n -> new IndexHeapTestUtils.IndexHeapI<>() {

			final int[] keys = new int[n];
			final IndexHeap heap = new IndexPairingHeap(n, (n1, n2) -> Integer.compare(keys[n1], keys[n2]));

			@Override
			public void insert(int node, Integer key) {
				keys[node] = key;
				heap.insert(node);
			}

			@Override
			public boolean isInserted(int node) {
				return heap.isInserted(node);
			}

			@Override
			public void decreaseKey(int node, Integer key) {
				keys[node] = key;
				heap.decreaseKey(node);
			}

			@Override
			public void increaseKey(int node, Integer key) {
				keys[node] = key;
				heap.increaseKey(node);
			}

			@Override
			public int findMin() {
				return heap.findMin();
			}

			@Override
			public int extractMin() {
				return heap.extractMin();
			}

			@Override
			public void remove(int node) {
				heap.remove(node);
			}

			@Override
			public Integer key(int node) {
				return keys[node];
			}

			@Override
			public boolean isEmpty() {
				return heap.isEmpty();
			}

			@Override
			public boolean isNotEmpty() {
				return heap.isNotEmpty();
			}

			@Override
			public void clear() {
				heap.clear();
			}

		}, Integer::compare, Random::nextInt);
	}

	@Test
	public void comparator() {
		IntComparator cmp = (n1, n2) -> Integer.compare(n2, n1);
		IndexHeap heap = new IndexPairingHeap(2, cmp);
		heap.insert(0);
		heap.insert(1);
		assertEquals(1, heap.extractMin());
		assertEquals(0, heap.extractMin());
		assertEquals(cmp, heap.comparator());
	}

}
