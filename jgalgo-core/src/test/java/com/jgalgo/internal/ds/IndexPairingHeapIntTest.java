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

import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class IndexPairingHeapIntTest extends TestBase {

	@SuppressWarnings("boxing")
	@Test
	public void randOps() {
		IndexHeapTestUtils.<Integer>randOps(n -> new IndexHeapTestUtils.IndexHeapI<>() {

			final IndexHeapInt heap = new IndexPairingHeapInt(n);

			@Override
			public void insert(int node, Integer key) {
				heap.insert(node, key);
			}

			@Override
			public boolean isInserted(int node) {
				return heap.isInserted(node);
			}

			@Override
			public void decreaseKey(int node, Integer key) {
				heap.decreaseKey(node, key);
			}

			@Override
			public void increaseKey(int node, Integer key) {
				heap.increaseKey(node, key);
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
				return heap.key(node);
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

}
