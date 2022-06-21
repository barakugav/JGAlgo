package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.ugav.algo.BST;
import com.ugav.algo.DebugPrintsManager;
import com.ugav.algo.Heap;
import com.ugav.algo.HeapDirectAccessed.Handle;
import com.ugav.algo.test.HeapTestUtils.HeapTracker;
import com.ugav.algo.test.HeapTestUtils.HeapTrackerIdGenerator;
import com.ugav.algo.test.HeapTestUtils.TestMode;

class BSTTestUtils extends TestUtils {

	private BSTTestUtils() {
		throw new InternalError();
	}

	static boolean testFindSmallers(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			return testFindSmallerGreater(treeBuilder, n, true);
		});
	}

	static boolean testFindGreaters(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			return testFindSmallerGreater(treeBuilder, n, false);
		});
	}

	@SuppressWarnings("boxing")
	private static boolean testFindSmallerGreater(Supplier<? extends BST<Integer>> treeBuilder, int n,
			boolean smaller) {
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

			if (!Objects.equals(actual, expected)) {
				printTestStr("Failed to find smaller/greater of ", searchedElm, " : ", actual, " != ", expected, "\n");
				return false;
			}
		}
		return true;
	}

	static boolean testGetPredecessors(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			return testGetPredecessorSuccessor(treeBuilder, n, true);
		});
	}

	static boolean testGetSuccessors(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			return testGetPredecessorSuccessor(treeBuilder, n, false);
		});
	}

	@SuppressWarnings("boxing")
	private static boolean testGetPredecessorSuccessor(Supplier<? extends BST<Integer>> treeBuilder, int n,
			boolean predecessor) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		Random rand = new Random(nextRandSeed());
		int[] a = Utils.randPermutation(n, nextRandSeed());

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
			if (h == null) {
				printTestStr("Failed to find handle for ", searchedElm, "\n");
				return false;
			}

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

			if (!Objects.equals(actual, expected)) {
				printTestStr("Failed to find predecessor/successor of ", searchedElm, " : ", actual, " != ", expected,
						"\n");
				return false;
			}
		}
		return true;
	}

	static boolean testSplit(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(128, 8), phase(64, 32), phase(16, 128), phase(8, 256), phase(4, 1024));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			return BSTTestUtils.testSplit(treeBuilder, n);
		});
	}

	@SuppressWarnings("boxing")
	private static boolean testSplit(Supplier<? extends BST<Integer>> treeBuilder, int tCount) {
		Random rand = new Random(nextRandSeed());
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(nextRandSeed());
		Set<BSTTracker> trees = new HashSet<>();
		final int maxVal = tCount * (1 << 12);

		for (int i = 0; i < tCount; i++) {
			BSTTracker tracker = new BSTTracker(treeBuilder.get(), heapTrackerIdGen.nextId());
			int[] elms = Utils.randArray(16, 0, maxVal, nextRandSeed());
			if (!HeapTestUtils.testHeap(tracker, 16, TestMode.InsertFirst, elms))
				return false;
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

		BooleanSupplier doRandOps = () -> {
			for (BSTTracker h : trees) {
				int opsNum = 512 / trees.size();
				int[] elms = Utils.randArray(opsNum, 0, maxVal, nextRandSeed());
				if (!HeapTestUtils.testHeap(h, opsNum, TestMode.Normal, elms))
					return false;
			}
			return true;
		};

		while (trees.size() > 1) {
			/*
			 * Each iteration reduce the number of trees by 2, double it, and halve it.
			 * Reducing the number of tree by a factor of 2 in total
			 */
			meld.run();
			if (!doRandOps.getAsBoolean())
				return false;
			split.run();
			if (!doRandOps.getAsBoolean())
				return false;
			meld.run();
			if (!doRandOps.getAsBoolean())
				return false;
		}

		return true;

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
