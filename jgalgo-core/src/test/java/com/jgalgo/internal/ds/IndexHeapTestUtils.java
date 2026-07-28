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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

class IndexHeapTestUtils extends TestBase {

	static interface IndexHeapI<K> {

		void insert(int node, K key);

		boolean isInserted(int node);

		void decreaseKey(int node, K key);

		void increaseKey(int node, K key);

		int findMin();

		int extractMin();

		void remove(int node);

		K key(int node);

		boolean isEmpty();

		boolean isNotEmpty();

		void clear();

	}

	static <K> void randOps(Int2ObjectFunction<? extends IndexHeapI<K>> heapBuilder, Comparator<K> compare,
			Function<Random, K> keyGen) {
		final SeedGenerator seedGen = new SeedGenerator(0xd0facfb963b398c1L);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 18).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(32);
		tester.addPhase().withArgs(32, 64).repeat(16);
		tester.run((n, m) -> randOps(heapBuilder, compare, keyGen, n, m, seedGen.nextSeed()));
	}

	private static <K> void randOps(Int2ObjectFunction<? extends IndexHeapI<K>> heapBuilder, Comparator<K> compare,
			Function<Random, K> keyGen, int n, int m, long seed) {
		final Random rand = new Random(seed);

		IndexHeapI<K> heap = heapBuilder.apply(n);
		@SuppressWarnings("unchecked")
		K[] expectedKeys = (K[]) new Object[n];
		IntSortedSet insertedSet = new IntRBTreeSet((IntComparator) (n1, n2) -> {
			int c = compare.compare(expectedKeys[n1], expectedKeys[n2]);
			return c != 0 ? c : Integer.compare(n1, n2);
		});
		IntList insertedList = new IntArrayList(n);
		int[] posInInsertedList = new int[n];
		IntList notInsertedList = new IntArrayList(n);

		Arrays.fill(posInInsertedList, -1);
		notInsertedList.addAll(range(n));

		ToIntFunction<IntList> randElm = list -> list.getInt(rand.nextInt(list.size()));
		ToIntFunction<IntList> extractRand = list -> {
			int i = rand.nextInt(list.size());
			int node = list.getInt(i);
			list.set(i, list.getInt(list.size() - 1));
			list.removeInt(list.size() - 1);
			return node;
		};

		final int Insert = 0;
		final int DecreaseKey = 1;
		final int IncreaseKey = 2;
		final int FindMin = 3;
		final int ExtractMin = 4;
		final int Remove = 5;
		final int OpTypes = 6;
		for (int op = 0; op < m; op++) {
			switch (rand.nextInt(OpTypes)) {
				case Insert: {
					if (notInsertedList.isEmpty())
						break;
					int node = extractRand.applyAsInt(notInsertedList);
					K key = keyGen.apply(rand);
					assertFalse(heap.isInserted(node));
					heap.insert(node, key);
					expectedKeys[node] = key;
					assertTrue(heap.isInserted(node));
					assertEquals(key, heap.key(node));
					insertedSet.add(node);
					posInInsertedList[node] = insertedList.size();
					insertedList.add(node);
					break;
				}
				case DecreaseKey: {
					if (insertedList.isEmpty())
						break;
					int node = randElm.applyAsInt(insertedList);
					K newKey;
					do {
						newKey = keyGen.apply(rand);
					} while (compare.compare(newKey, heap.key(node)) > 0);

					assertTrue(heap.isInserted(node));
					assertEquals(expectedKeys[node], heap.key(node));
					insertedSet.remove(node);
					heap.decreaseKey(node, newKey);
					expectedKeys[node] = newKey;
					insertedSet.add(node);

					assertEquals(newKey, heap.key(node));
					assertTrue(heap.isInserted(node));
					break;
				}
				case IncreaseKey: {
					if (insertedList.isEmpty())
						break;
					int node = randElm.applyAsInt(insertedList);
					K newKey;
					do {
						newKey = keyGen.apply(rand);
					} while (compare.compare(newKey, heap.key(node)) < 0);

					assertTrue(heap.isInserted(node));
					assertEquals(expectedKeys[node], heap.key(node));
					insertedSet.remove(node);
					heap.increaseKey(node, newKey);
					expectedKeys[node] = newKey;
					insertedSet.add(node);

					assertEquals(newKey, heap.key(node));
					assertTrue(heap.isInserted(node));
					break;
				}
				case FindMin: {
					if (insertedList.isEmpty())
						break;
					K expectedKey = expectedKeys[insertedSet.firstInt()];
					int minNode = heap.findMin();
					assertEquals(expectedKey, heap.key(minNode));
					break;
				}
				case ExtractMin: {
					if (insertedList.isEmpty())
						break;
					K expectedKey = expectedKeys[insertedSet.firstInt()];
					int minNode = heap.extractMin();
					assertEquals(expectedKey, heap.key(minNode));

					insertedSet.remove(minNode);
					int pos = posInInsertedList[minNode];
					assertEquals(minNode, insertedList.getInt(pos));
					if (pos != insertedList.size() - 1) {
						int lastNode = insertedList.getInt(insertedList.size() - 1);
						insertedList.set(pos, lastNode);
						posInInsertedList[lastNode] = pos;
					}
					insertedList.removeInt(insertedList.size() - 1);
					posInInsertedList[minNode] = -1;
					notInsertedList.add(minNode);
					break;
				}
				case Remove: {
					if (insertedList.isEmpty())
						break;
					int node = randElm.applyAsInt(insertedList);
					heap.remove(node);

					insertedSet.remove(node);
					int pos = posInInsertedList[node];
					assertEquals(node, insertedList.getInt(pos));
					if (pos != insertedList.size() - 1) {
						int lastNode = insertedList.getInt(insertedList.size() - 1);
						insertedList.set(pos, lastNode);
						posInInsertedList[lastNode] = pos;
					}
					insertedList.removeInt(insertedList.size() - 1);
					posInInsertedList[node] = -1;
					notInsertedList.add(node);
					break;
				}
				default:
					throw new AssertionError();
			}
			assertEqualsBool(insertedSet.isEmpty(), heap.isEmpty());
			assertEqualsBool(!insertedSet.isEmpty(), heap.isNotEmpty());
		}
		heap.clear();
		assertTrue(heap.isEmpty());
		for (int v : range(n))
			assertFalse(heap.isInserted(v));
	}

	@Test
	public void defaultIndexHeap() {
		assertEquals(IndexPairingHeap.class, IndexHeap.newInstance(8, Integer::compare).getClass());
		assertEquals(IndexPairingHeapInt.class, IndexHeapInt.newInstance(8).getClass());
		assertEquals(IndexPairingHeapDouble.class, IndexHeapDouble.newInstance(8).getClass());
		assertEquals(IndexPairingHeapObj.class, IndexHeapObj.newInstance(8).getClass());
	}

}
