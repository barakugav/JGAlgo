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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.jgalgo.internal.ds.ReferenceableHeapTestUtils.ReferenceableHeapTracker;
import com.jgalgo.internal.ds.ReferenceableHeapTestUtils.HeapTrackerIdGenerator;
import com.jgalgo.internal.ds.ReferenceableHeapTestUtils.TestMode;
import com.jgalgo.internal.util.DebugPrinter;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class BinarySearchTreeTestUtils extends TestUtils {

	private BinarySearchTreeTestUtils() {}

	static void testExtractMax(IntReferenceableHeap.Builder treeBuilder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		IntComparator compare = null;
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(4096).repeat(8);
		tester.run(n -> {
			BSTTracker tracker = new BSTTracker((IntBinarySearchTree) treeBuilder.build(int.class, void.class, compare),
					0, compare, seedGen.nextSeed());
			ReferenceableHeapTestUtils.testHeap(tracker, n, TestMode.InsertFirst,
					randArray(n / 2, 0, Integer.MAX_VALUE, seedGen.nextSeed()), compare, seedGen.nextSeed());

			for (int repeat = 0; repeat < 4; repeat++) {
				ReferenceableHeapTestUtils.testHeap(tracker, n, TestMode.Normal,
						randArray(n / 2, 0, Integer.MAX_VALUE, seedGen.nextSeed()), compare, seedGen.nextSeed());

				for (int i = 0; i < 2; i++) {
					int x = rand.nextInt();
					IntReferenceableHeap.Ref ref = tracker.heap.insert(x);
					tracker.insert(x, ref);
				}
				int expected = tracker.extractMax();
				int actual = tracker.tree().extractMax().key();
				assertEquals(expected, actual, "failed extractMax");
			}
		});
	}

	static void testFindSmallerDefaultCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testFindSmaller(treeBuilder, null, seed);
	}

	static void testFindSmallerCustomCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testFindSmaller(treeBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testFindSmaller(IntReferenceableHeap.Builder treeBuilder, IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(4096).repeat(8);
		tester.run(n -> {
			testFindSmallerGreater(treeBuilder, compare, seedGen.nextSeed(), n, true);
		});
	}

	static void testFindGreaterDefaultCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testFindGreater(treeBuilder, null, seed);
	}

	static void testFindGreaterCustomCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testFindGreater(treeBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testFindGreater(IntReferenceableHeap.Builder treeBuilder, IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(4096).repeat(8);
		tester.run(n -> {
			testFindSmallerGreater(treeBuilder, compare, seedGen.nextSeed(), n, false);
		});
	}

	@SuppressWarnings("boxing")
	private static void testFindSmallerGreater(IntReferenceableHeap.Builder treeBuilder, IntComparator compare,
			long seed, int n, boolean smaller) {
		DebugPrinter debug = new DebugPrinter(false);
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		BSTTracker tracker = new BSTTracker((IntBinarySearchTree) treeBuilder.build(int.class, void.class, compare), 0,
				compare, seedGen.nextSeed());

		for (int i = 0; i < n; i++) {
			int newElm = rand.nextInt(n);
			debug.println("Insert(", newElm, ")");
			IntReferenceableHeap.Ref ref = tracker.tree().insert(newElm);
			tracker.insert(newElm, ref);

			int searchedElm = rand.nextInt(n);

			IntReferenceableHeap.Ref actualRef;
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
			actual = actualRef == null ? null : actualRef.key();

			assertEquals(expected, actual, "Failed to find smaller/greater of " + searchedElm);
		}
	}

	static void testGetPredecessorsDefaultCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testGetPredecessors(treeBuilder, null, seed);
	}

	static void testGetPredecessorsCustomCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testGetPredecessors(treeBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testGetPredecessors(IntReferenceableHeap.Builder treeBuilder, IntComparator compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(4096).repeat(8);
		tester.run(n -> {
			testGetPredecessorSuccessor(treeBuilder, n, compare, seedGen.nextSeed(), true);
		});
	}

	static void testGetSuccessorsDefaultCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testGetSuccessors(treeBuilder, null, seed);
	}

	static void testGetSuccessorsCustomCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testGetSuccessors(treeBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testGetSuccessors(IntReferenceableHeap.Builder treeBuilder, IntComparator compare,

			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(4096).repeat(8);
		tester.run(n -> {
			testGetPredecessorSuccessor(treeBuilder, n, compare, seedGen.nextSeed(), false);
		});
	}

	@SuppressWarnings("boxing")
	private static void testGetPredecessorSuccessor(IntReferenceableHeap.Builder treeBuilder, int n,
			IntComparator compare, long seed, boolean predecessor) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		DebugPrinter debug = new DebugPrinter(false);
		Random rand = new Random(seedGen.nextSeed());
		int[] a = randPermutation(n, seedGen.nextSeed());

		BSTTracker tracker = new BSTTracker((IntBinarySearchTree) treeBuilder.build(int.class, void.class, compare), 0,
				compare, seedGen.nextSeed());

		for (int i = 0; i < n; i++) {
			int newElm = a[i];
			debug.println("Insert(", newElm, ")");
			IntReferenceableHeap.Ref ref = tracker.tree().insert(newElm);
			tracker.insert(newElm, ref);

			Integer searchedElm;
			do {
				if (rand.nextBoolean())
					searchedElm = tracker.floor(rand.nextInt(n));
				else
					searchedElm = tracker.ceiling(rand.nextInt(n));
			} while (searchedElm == null);

			IntReferenceableHeap.Ref h = tracker.tree().find(searchedElm);
			assertNotNull(h, "Failed to find ref for " + searchedElm);

			Integer actual, expected;
			if (predecessor) {
				IntReferenceableHeap.Ref actualH = tracker.tree().getPredecessor(h);
				actual = actualH == null ? null : actualH.key();
				expected = tracker.lower(searchedElm);
			} else {
				IntReferenceableHeap.Ref actualH = tracker.tree().getSuccessor(h);
				actual = actualH == null ? null : actualH.key();
				expected = tracker.higher(searchedElm);
			}

			assertEquals(expected, actual, "Failed to find predecessor/successor of " + searchedElm);
		}
	}

	static void testSplitDefaultCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testSplit(treeBuilder, null, seed);
	}

	static void testSplitCustomCompare(IntReferenceableHeap.Builder treeBuilder, long seed) {
		testSplit(treeBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testSplit(IntReferenceableHeap.Builder treeBuilder, IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(128);
		tester.addPhase().withArgs(32).repeat(64);
		tester.addPhase().withArgs(128).repeat(16);
		tester.addPhase().withArgs(256).repeat(8);
		tester.addPhase().withArgs(1024).repeat(4);
		tester.run(n -> {
			BinarySearchTreeTestUtils.testSplit(treeBuilder, n, compare, seedGen.nextSeed());
		});
	}

	@SuppressWarnings("boxing")
	private static void testSplit(IntReferenceableHeap.Builder treeBuilder, int tCount, IntComparator compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(seedGen.nextSeed());
		Set<BSTTracker> trees = new ObjectOpenHashSet<>();
		final int maxVal = tCount * (1 << 12);

		for (int i = 0; i < tCount; i++) {
			BSTTracker tracker = new BSTTracker((IntBinarySearchTree) treeBuilder.build(int.class, void.class, compare),
					heapTrackerIdGen.nextId(), compare, seedGen.nextSeed());
			int[] elms = randArray(16, 0, maxVal, seedGen.nextSeed());
			ReferenceableHeapTestUtils.testHeap(tracker, 16, TestMode.InsertFirst, elms, compare, seedGen.nextSeed());
			trees.add(tracker);
		}

		Runnable meld = () -> {
			if (trees.size() < 2)
				return;
			Set<BSTTracker> treesNext = new ObjectOpenHashSet<>();
			List<BSTTracker> heapsSuffled = new ObjectArrayList<>(trees);
			Collections.shuffle(heapsSuffled, new Random(seedGen.nextSeed()));

			for (int i = 0; i < heapsSuffled.size() / 2; i++) {
				BSTTracker h1 = heapsSuffled.get(i * 2);
				BSTTracker h2 = heapsSuffled.get(i * 2 + 1);
				h1.tree().meld(h2.tree());
				assertTrue(h2.tree().isEmpty());
				h1.meld(h2);
				treesNext.add(h1);
			}
			trees.clear();
			trees.addAll(treesNext);
			return;
		};

		Runnable split = () -> {
			Set<BSTTracker> treesNext = new ObjectOpenHashSet<>();
			for (BSTTracker h : trees) {
				if (h.tree().isEmpty())
					continue;

				Integer[] elms = h.tree().stream().map(IntReferenceableHeap.Ref::key).toArray(Integer[]::new);
				Arrays.sort(elms, null);

				double idx0 = 0.5 + rand.nextGaussian() / 10;
				idx0 = idx0 < 0 ? 0 : idx0 > 1 ? 1 : idx0;
				int idx = (int) ((elms.length - 1) * idx0);
				Integer val = elms[idx];

				IntBinarySearchTree s = h.tree().splitGreater(val);
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
				ReferenceableHeapTestUtils.testHeap(h, opsNum, TestMode.Normal, elms, compare, seedGen.nextSeed());
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
	static class BSTTracker extends ReferenceableHeapTracker {

		BSTTracker(IntBinarySearchTree heap, int id, IntComparator compare, long seed) {
			super(heap, id, compare, seed);
		}

		IntBinarySearchTree tree() {
			return (IntBinarySearchTree) heap;
		}

		int extractMax() {
			Integer x = elms.lastKey();
			IntReferenceableHeap.Ref ref = elms.get(x).get(0);
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
