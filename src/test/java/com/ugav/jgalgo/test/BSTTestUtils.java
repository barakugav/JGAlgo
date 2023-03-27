package com.ugav.jgalgo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.ugav.jgalgo.BST;
import com.ugav.jgalgo.Heap;
import com.ugav.jgalgo.HeapDirectAccessed.Handle;
import com.ugav.jgalgo.test.HeapTestUtils.HeapTracker;
import com.ugav.jgalgo.test.HeapTestUtils.HeapTrackerIdGenerator;
import com.ugav.jgalgo.test.HeapTestUtils.TestMode;

class BSTTestUtils extends TestUtils {

	private BSTTestUtils() {
	}

	static void testFindSmallers(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testFindSmallerGreater(treeBuilder, n, true);
		});
	}

	static void testFindGreaters(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testFindSmallerGreater(treeBuilder, n, false);
		});
	}

	@SuppressWarnings("boxing")
	private static void testFindSmallerGreater(Supplier<? extends BST<Integer>> treeBuilder, int n, boolean smaller) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		Random rand = new Random(nextRandSeed());

		BSTTracker tracker = new BSTTracker(treeBuilder.get(), 0);

		for (int i = 0; i < n; i++) {
			int newElm = rand.nextInt(n);
			debug.println("Insert(", newElm, ")");
			tracker.tree().insert(newElm);
			tracker.insert(newElm);

			int searchedElm = rand.nextInt(n);

			Handle<Integer> actualH;
			Integer actual, expected;
			if (smaller) {
				if (rand.nextBoolean()) {
					actualH = tracker.tree().findSmaller(searchedElm);
					expected = tracker.lower(searchedElm);
				} else {
					actualH = tracker.tree().findOrSmaller(searchedElm);
					expected = tracker.floor(searchedElm);
				}
			} else {
				if (rand.nextBoolean()) {
					actualH = tracker.tree().findGreater(searchedElm);
					expected = tracker.higher(searchedElm);
				} else {
					actualH = tracker.tree().findOrGreater(searchedElm);
					expected = tracker.ceiling(searchedElm);
				}
			}
			actual = actualH == null ? null : actualH.get();

			Assertions.assertEquals(expected, actual, "Failed to find smaller/greater of " + searchedElm);
		}
	}

	static void testGetPredecessors(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testGetPredecessorSuccessor(treeBuilder, n, true);
		});
	}

	static void testGetSuccessors(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testGetPredecessorSuccessor(treeBuilder, n, false);
		});
	}

	@SuppressWarnings("boxing")
	private static void testGetPredecessorSuccessor(Supplier<? extends BST<Integer>> treeBuilder, int n,
			boolean predecessor) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		Random rand = new Random(nextRandSeed());
		int[] a = randPermutation(n, nextRandSeed());

		BSTTracker tracker = new BSTTracker(treeBuilder.get(), 0);

		for (int i = 0; i < n; i++) {
			int newElm = a[i];
			debug.println("Insert(", newElm, ")");
			tracker.tree().insert(newElm);
			tracker.insert(newElm);

			Integer searchedElm;
			do {
				if (rand.nextBoolean())
					searchedElm = tracker.floor(rand.nextInt(n));
				else
					searchedElm = tracker.ceiling(rand.nextInt(n));
			} while (searchedElm == null);

			Handle<Integer> h = tracker.tree().findHanlde(searchedElm);
			Assertions.assertNotNull(h, "Failed to find handle for " + searchedElm);

			Integer actual, expected;
			if (predecessor) {
				Handle<Integer> actualH = tracker.tree().getPredecessor(h);
				actual = actualH == null ? null : actualH.get();
				expected = tracker.lower(searchedElm);
			} else {
				Handle<Integer> actualH = tracker.tree().getSuccessor(h);
				actual = actualH == null ? null : actualH.get();
				expected = tracker.higher(searchedElm);
			}

			Assertions.assertEquals(expected, actual, "Failed to find predecessor/successor of " + searchedElm);
		}
	}

	static void testSplit(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(128, 8), phase(64, 32), phase(16, 128), phase(8, 256), phase(4, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			BSTTestUtils.testSplit(treeBuilder, n);
		});
	}

	@SuppressWarnings("boxing")
	private static void testSplit(Supplier<? extends BST<Integer>> treeBuilder, int tCount) {
		Random rand = new Random(nextRandSeed());
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(nextRandSeed());
		Set<BSTTracker> trees = new HashSet<>();
		final int maxVal = tCount * (1 << 12);

		for (int i = 0; i < tCount; i++) {
			BSTTracker tracker = new BSTTracker(treeBuilder.get(), heapTrackerIdGen.nextId());
			int[] elms = randArray(16, 0, maxVal, nextRandSeed());
			HeapTestUtils.testHeap(tracker, 16, TestMode.InsertFirst, elms);
			trees.add(tracker);
		}

		Runnable meld = () -> {
			if (trees.size() < 2)
				return;
			Set<BSTTracker> treesNext = new HashSet<>();
			List<BSTTracker> heapsSuffled = new ArrayList<>(trees);
			Collections.shuffle(heapsSuffled, new Random(nextRandSeed()));

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

				BST<Integer> s = h.tree().splitGreater(val);
				BSTTracker t = new BSTTracker(s, heapTrackerIdGen.nextId());
				h.split(val, t);
				treesNext.add(h);
				treesNext.add(t);
			}
		};

		Runnable doRandOps = () -> {
			for (BSTTracker h : trees) {
				int opsNum = 512 / trees.size();
				int[] elms = randArray(opsNum, 0, maxVal, nextRandSeed());
				HeapTestUtils.testHeap(h, opsNum, TestMode.Normal, elms);
			}
		};

		while (trees.size() > 1) {
			/*
			 * Each iteration reduce the number of trees by 2, double it, and halve it.
			 * Reducing the number of tree by a factor of 2 in total
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

		BSTTracker(Heap<Integer> heap, int id) {
			super(heap, id);
		}

		BST<Integer> tree() {
			return (BST<Integer>) heap;
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
