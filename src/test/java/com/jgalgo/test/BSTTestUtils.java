package com.jgalgo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;

import com.jgalgo.BST;
import com.jgalgo.Heap;
import com.jgalgo.HeapDirectAccessed.Handle;
import com.jgalgo.test.HeapTestUtils.HeapTracker;
import com.jgalgo.test.HeapTestUtils.HeapTrackerIdGenerator;
import com.jgalgo.test.HeapTestUtils.TestMode;

class BSTTestUtils extends TestUtils {

	private BSTTestUtils() {
	}

	static void testFindSmallersDefaultCompare(
			Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder, long seed) {
		testFindSmallers(treeBuilder, null, seed);
	}

	static void testFindSmallersCustomCompare(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			long seed) {
		testFindSmallers(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testFindSmallers(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testFindSmallerGreater(treeBuilder, compare, seedGen.nextSeed(), n, true);
		});
	}

	static void testFindGreatersDefaultCompare(
			Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder, long seed) {
		testFindGreaters(treeBuilder, null, seed);
	}

	static void testFindGreatersCustomCompare(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			long seed) {
		testFindGreaters(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testFindGreaters(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testFindSmallerGreater(treeBuilder, compare, seedGen.nextSeed(), n, false);
		});
	}

	@SuppressWarnings("boxing")
	private static void testFindSmallerGreater(
			Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			Comparator<? super Integer> compare, long seed, int n, boolean smaller) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		BSTTracker tracker = new BSTTracker(treeBuilder.apply(compare), 0, compare, seedGen.nextSeed());

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

	static void testGetPredecessorsDefaultCompare(
			Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder, long seed) {
		testGetPredecessors(treeBuilder, null, seed);
	}

	static void testGetPredecessorsCustomCompare(
			Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder, long seed) {
		testGetPredecessors(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testGetPredecessors(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testGetPredecessorSuccessor(treeBuilder, n, compare, seedGen.nextSeed(), true);
		});
	}

	static void testGetSuccessorsDefaultCompare(
			Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder, long seed) {
		testGetSuccessors(treeBuilder, null, seed);
	}

	static void testGetSuccessorsCustomCompare(
			Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder, long seed) {
		testGetSuccessors(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testGetSuccessors(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testGetPredecessorSuccessor(treeBuilder, n, compare, seedGen.nextSeed(), false);
		});
	}

	@SuppressWarnings("boxing")
	private static void testGetPredecessorSuccessor(
			Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder, int n,
			Comparator<? super Integer> compare, long seed, boolean predecessor) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		DebugPrintsManager debug = new DebugPrintsManager(false);
		Random rand = new Random(seedGen.nextSeed());
		int[] a = randPermutation(n, seedGen.nextSeed());

		BSTTracker tracker = new BSTTracker(treeBuilder.apply(compare), 0, compare, seedGen.nextSeed());

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

	static void testSplitDefaultCompare(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			long seed) {
		testSplit(treeBuilder, null, seed);
	}

	static void testSplitCustomCompare(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			long seed) {
		testSplit(treeBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testSplit(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 8), phase(64, 32), phase(16, 128), phase(8, 256), phase(4, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			BSTTestUtils.testSplit(treeBuilder, n, compare, seedGen.nextSeed());
		});
	}

	@SuppressWarnings("boxing")
	private static void testSplit(Function<Comparator<? super Integer>, ? extends BST<Integer>> treeBuilder, int tCount,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(seedGen.nextSeed());
		Set<BSTTracker> trees = new HashSet<>();
		final int maxVal = tCount * (1 << 12);

		for (int i = 0; i < tCount; i++) {
			BSTTracker tracker = new BSTTracker(treeBuilder.apply(compare), heapTrackerIdGen.nextId(), compare,
					seedGen.nextSeed());
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

				BST<Integer> s = h.tree().splitGreater(val);
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

		BSTTracker(Heap<Integer> heap, int id, Comparator<? super Integer> compare, long seed) {
			super(heap, id, compare, seed);
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
