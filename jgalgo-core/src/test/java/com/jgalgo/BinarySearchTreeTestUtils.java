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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.jgalgo.HeapTestUtils.HeapTracker;
import com.jgalgo.HeapTestUtils.HeapTrackerIdGenerator;
import com.jgalgo.HeapTestUtils.TestMode;

class BinarySearchTreeTestUtils extends TestUtils {

	private BinarySearchTreeTestUtils() {}

	@SuppressWarnings("boxing")
	static void testExtractMax(BinarySearchTree.Builder treeBuilder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		Comparator<? super Integer> compare = null;
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];

			BSTTracker tracker = new BSTTracker(treeBuilder.build(compare), 0, compare, seedGen.nextSeed());
			HeapTestUtils.testHeap(tracker, n, TestMode.InsertFirst,
					randArray(n / 2, 0, Integer.MAX_VALUE, seedGen.nextSeed()), compare, seedGen.nextSeed());

			for (int repeat = 0; repeat < 4; repeat++) {
				HeapTestUtils.testHeap(tracker, n, TestMode.Normal,
						randArray(n / 2, 0, Integer.MAX_VALUE, seedGen.nextSeed()), compare, seedGen.nextSeed());

				for (int i = 0; i < 2; i++) {
					int x = rand.nextInt();
					HeapReference<Integer> ref = tracker.heap.insert(x);
					tracker.insert(x, ref);
				}
				int expected = tracker.extractMax();
				int actual = tracker.tree().extractMax();
				assertEquals(expected, actual, "failed extractMax");
			}
		});
	}

	static void testFindSmallersDefaultCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testFindSmallers(treeBuilder, null, seed);
	}

	static void testFindSmallersCustomCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testFindSmallers(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testFindSmallers(BinarySearchTree.Builder treeBuilder, Comparator<? super Integer> compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testFindSmallerGreater(treeBuilder, compare, seedGen.nextSeed(), n, true);
		});
	}

	static void testFindGreatersDefaultCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testFindGreaters(treeBuilder, null, seed);
	}

	static void testFindGreatersCustomCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testFindGreaters(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testFindGreaters(BinarySearchTree.Builder treeBuilder, Comparator<? super Integer> compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testFindSmallerGreater(treeBuilder, compare, seedGen.nextSeed(), n, false);
		});
	}

	@SuppressWarnings("boxing")
	private static void testFindSmallerGreater(BinarySearchTree.Builder treeBuilder,
			Comparator<? super Integer> compare, long seed, int n, boolean smaller) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		BSTTracker tracker = new BSTTracker(treeBuilder.build(compare), 0, compare, seedGen.nextSeed());

		for (int i = 0; i < n; i++) {
			int newElm = rand.nextInt(n);
			debug.println("Insert(", newElm, ")");
			HeapReference<Integer> ref = tracker.tree().insert(newElm);
			tracker.insert(newElm, ref);

			int searchedElm = rand.nextInt(n);

			HeapReference<Integer> actualRef;
			Integer actual, expected;
			if (smaller) {
				if (rand.nextBoolean()) {
					actualRef = tracker.tree().findSmaller(searchedElm);
					expected = tracker.lower(searchedElm);
				} else {
					actualRef = tracker.tree().findOrSmaller(searchedElm);
					expected = tracker.floor(searchedElm);
				}
			} else {
				if (rand.nextBoolean()) {
					actualRef = tracker.tree().findGreater(searchedElm);
					expected = tracker.higher(searchedElm);
				} else {
					actualRef = tracker.tree().findOrGreater(searchedElm);
					expected = tracker.ceiling(searchedElm);
				}
			}
			actual = actualRef == null ? null : actualRef.get();

			assertEquals(expected, actual, "Failed to find smaller/greater of " + searchedElm);
		}
	}

	static void testGetPredecessorsDefaultCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testGetPredecessors(treeBuilder, null, seed);
	}

	static void testGetPredecessorsCustomCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testGetPredecessors(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testGetPredecessors(BinarySearchTree.Builder treeBuilder, Comparator<? super Integer> compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testGetPredecessorSuccessor(treeBuilder, n, compare, seedGen.nextSeed(), true);
		});
	}

	static void testGetSuccessorsDefaultCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testGetSuccessors(treeBuilder, null, seed);
	}

	static void testGetSuccessorsCustomCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testGetSuccessors(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testGetSuccessors(BinarySearchTree.Builder treeBuilder, Comparator<? super Integer> compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testGetPredecessorSuccessor(treeBuilder, n, compare, seedGen.nextSeed(), false);
		});
	}

	@SuppressWarnings("boxing")
	private static void testGetPredecessorSuccessor(BinarySearchTree.Builder treeBuilder, int n,
			Comparator<? super Integer> compare, long seed, boolean predecessor) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		DebugPrintsManager debug = new DebugPrintsManager(false);
		Random rand = new Random(seedGen.nextSeed());
		int[] a = randPermutation(n, seedGen.nextSeed());

		BSTTracker tracker = new BSTTracker(treeBuilder.build(compare), 0, compare, seedGen.nextSeed());

		for (int i = 0; i < n; i++) {
			int newElm = a[i];
			debug.println("Insert(", newElm, ")");
			HeapReference<Integer> ref = tracker.tree().insert(newElm);
			tracker.insert(newElm,ref);

			Integer searchedElm;
			do {
				if (rand.nextBoolean())
					searchedElm = tracker.floor(rand.nextInt(n));
				else
					searchedElm = tracker.ceiling(rand.nextInt(n));
			} while (searchedElm == null);

			HeapReference<Integer> h = tracker.tree().findRef(searchedElm);
			assertNotNull(h, "Failed to find ref for " + searchedElm);

			Integer actual, expected;
			if (predecessor) {
				HeapReference<Integer> actualH = tracker.tree().getPredecessor(h);
				actual = actualH == null ? null : actualH.get();
				expected = tracker.lower(searchedElm);
			} else {
				HeapReference<Integer> actualH = tracker.tree().getSuccessor(h);
				actual = actualH == null ? null : actualH.get();
				expected = tracker.higher(searchedElm);
			}

			assertEquals(expected, actual, "Failed to find predecessor/successor of " + searchedElm);
		}
	}

	static void testSplitDefaultCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testSplit(treeBuilder, null, seed);
	}

	static void testSplitCustomCompare(BinarySearchTree.Builder treeBuilder, long seed) {
		testSplit(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testSplit(BinarySearchTree.Builder treeBuilder, Comparator<? super Integer> compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 8), phase(64, 32), phase(16, 128), phase(8, 256), phase(4, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			BinarySearchTreeTestUtils.testSplit(treeBuilder, n, compare, seedGen.nextSeed());
		});
	}

	@SuppressWarnings("boxing")
	private static void testSplit(BinarySearchTree.Builder treeBuilder, int tCount, Comparator<? super Integer> compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(seedGen.nextSeed());
		Set<BSTTracker> trees = new HashSet<>();
		final int maxVal = tCount * (1 << 12);

		for (int i = 0; i < tCount; i++) {
			BSTTracker tracker =
					new BSTTracker(treeBuilder.build(compare), heapTrackerIdGen.nextId(), compare, seedGen.nextSeed());
			int[] elms = randArray(16, 0, maxVal, seedGen.nextSeed());
			HeapTestUtils.testHeap(tracker, 16, TestMode.InsertFirst, elms, compare, seedGen.nextSeed());
			trees.add(tracker);
		}

		Runnable meld = () -> {
			if (trees.size() < 2)
				return;
			Set<BSTTracker> treesNext = new HashSet<>();
			List<BSTTracker> heapsSuffled = new ArrayList<>(trees);
			Collections.shuffle(heapsSuffled, new Random(seedGen.nextSeed()));

			for (int i = 0; i < heapsSuffled.size() / 2; i++) {
				BSTTracker h1 = heapsSuffled.get(i * 2);
				BSTTracker h2 = heapsSuffled.get(i * 2 + 1);
				h1.tree().meld(h2.tree());
				h1.meld(h2);
				treesNext.add(h1);
			}
			trees.clear();
			trees.addAll(treesNext);
			return;
		};

		Runnable split = () -> {
			Set<BSTTracker> treesNext = new HashSet<>();
			for (BSTTracker h : trees) {
				if (h.tree().isEmpty())
					continue;

				Integer[] elms = h.tree().toArray(s -> new Integer[s]);
				Arrays.sort(elms, null);

				double idx0 = 0.5 + rand.nextGaussian() / 10;
				idx0 = idx0 < 0 ? 0 : idx0 > 1 ? 1 : idx0;
				int idx = (int) ((elms.length - 1) * idx0);
				Integer val = elms[idx];

				BinarySearchTree<Integer> s = h.tree().splitGreater(val);
				BSTTracker t = new BSTTracker(s, heapTrackerIdGen.nextId(), compare, seedGen.nextSeed());
				h.split(val, t);
				treesNext.add(h);
				treesNext.add(t);
			}
		};

		Runnable doRandOps = () -> {
			for (BSTTracker h : trees) {
				int opsNum = 512 / trees.size();
				int[] elms = randArray(opsNum, 0, maxVal, seedGen.nextSeed());
				HeapTestUtils.testHeap(h, opsNum, TestMode.Normal, elms, compare, seedGen.nextSeed());

			}
		};

		while (trees.size() > 1) {
			/*
			 * Each iteration reduce the number of trees by 2, double it, and halve it. Reducing the number of tree by a
			 * factor of 2 in total
			 */
			meld.run();
			doRandOps.run();
			split.run();
			doRandOps.run();
			meld.run();
			doRandOps.run();
		}
	}

	@SuppressWarnings("boxing")
	static class BSTTracker extends HeapTracker {

		BSTTracker(Heap<Integer> heap, int id, Comparator<? super Integer> compare, long seed) {
			super(heap, id, compare, seed);
		}

		BinarySearchTree<Integer> tree() {
			return (BinarySearchTree<Integer>) heap;
		}

		int extractMax() {
			Integer x = elms.lastKey();
			HeapReference<Integer> ref = elms.get(x).get(0);
			remove(x, ref);
			return x;
		}

		Integer lower(int x) {
			return elms.lowerKey(x);
		}

		Integer higher(int x) {
			return elms.higherKey(x);
		}

		Integer floor(int x) {
			return elms.floorKey(x);
		}

		Integer ceiling(int x) {
			return elms.ceilingKey(x);
		}
	}

}
