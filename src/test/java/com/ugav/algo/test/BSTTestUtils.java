package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.ugav.algo.BST;
import com.ugav.algo.HeapDirectAccessed.Handle;
import com.ugav.algo.test.HeapTestUtils.HeapTracker;
import com.ugav.algo.test.HeapTestUtils.HeapTrackerIdGenerator;
import com.ugav.algo.test.HeapTestUtils.TestMode;

class BSTTestUtils extends TestUtils {

	private BSTTestUtils() {
		throw new InternalError();
	}

	static boolean testFindPredecessors(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return testFindPredecessor(treeBuilder, n);
		});
	}

	static boolean testFindSuccessors(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return testFindSuccessor(treeBuilder, n);
		});
	}

	private static boolean testFindPredecessor(Supplier<? extends BST<Integer>> treeBuilder, int n) {
		return testFindPredecessorSuccessor(treeBuilder, n, true);
	}

	private static boolean testFindSuccessor(Supplier<? extends BST<Integer>> treeBuilder, int n) {
		return testFindPredecessorSuccessor(treeBuilder, n, false);
	}

	@SuppressWarnings("boxing")
	private static boolean testFindPredecessorSuccessor(Supplier<? extends BST<Integer>> treeBuilder, int n,
			boolean predecessor) {
		Random rand = new Random(nextRandSeed());
		int[] a = Utils.randPermutation(n, nextRandSeed());

		BST<Integer> tree = treeBuilder.get();
		NavigableSet<Integer> ctrl = new TreeSet<>();

		for (int i = 0; i < n; i++) {
			int newElm = a[i];
			tree.insert(newElm);
			ctrl.add(newElm);

			Integer searchedElm;
			do {
				if (rand.nextBoolean())
					searchedElm = ctrl.lower(rand.nextInt(n));
				else
					searchedElm = ctrl.ceiling(rand.nextInt(n));
			} while (searchedElm == null);

			Handle<Integer> h = tree.findHanlde(searchedElm);
			if (h == null) {
				printTestStr("Failed to find handle for ", searchedElm, "\n");
				return false;
			}

			Integer actual, expected;
			if (predecessor) {
				Handle<Integer> actualH = tree.findPredecessor(h);
				actual = actualH == null ? null : actualH.get();
				expected = ctrl.lower(searchedElm);
			} else {
				Handle<Integer> actualH = tree.findSuccessor(h);
				actual = actualH == null ? null : actualH.get();
				expected = ctrl.higher(searchedElm);
			}

			if (!Objects.equals(actual, expected)) {
				printTestStr("Failed to find predecessor/successor of ", searchedElm, " : ", actual, " != ", expected,
						"\n");
				return false;

			}
		}
		return true;
	}

	static class BSTTracker extends HeapTracker {

		final BST<Integer> tree;

		BSTTracker(BST<Integer> tree, int id) {
			super(tree, id);
			this.tree = tree;
		}

	}

	static boolean testSplit(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(128, 8), phase(64, 32), phase(16, 128), phase(8, 256), phase(4, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return BSTTestUtils.testSplit(treeBuilder, n);
		});
	}

	@SuppressWarnings("boxing")
	private static boolean testSplit(Supplier<? extends BST<Integer>> treeBuilder, int tCount) {
		Random rand = new Random(nextRandSeed());
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(nextRandSeed());
		Set<BSTTracker> trees = new HashSet<>();

		int elm = 0;
		for (int i = 0; i < tCount; i++) {
			BST<Integer> t = treeBuilder.get();
			BSTTracker tracker = new BSTTracker(t, heapTrackerIdGen.nextId());

			int[] elms = new int[16];
			for (int j = 0; j < 16; j++)
				elms[j] = elm++;
			if (!HeapTestUtils.testHeap(tracker, 16, TestMode.InsertFirst, elms))
				return false;
		}

		RandomIntUnique elmGen = new RandomIntUnique(elm + 1, tCount * (1 << 12), nextRandSeed());

		Runnable meld = () -> {
			if (trees.size() < 2)
				return;
			Set<BSTTracker> treesNext = new HashSet<>();
			List<BSTTracker> heapsSuffled = new ArrayList<>(trees);
			Collections.shuffle(heapsSuffled);

			for (int i = 0; i < heapsSuffled.size() / 2; i++) {
				BSTTracker h1 = heapsSuffled.get(i * 2);
				BSTTracker h2 = heapsSuffled.get(i * 2 + 1);
				h1.tree.meld(h2.tree);
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
				if (h.tree.isEmpty())
					continue;

				Integer[] elms = h.tree.toArray(s -> new Integer[s]);
				Arrays.sort(elms, null);

				double idx0 = 0.5 + rand.nextGaussian() / 10;
				idx0 = idx0 < 0 ? 0 : idx0 > 1 ? 1 : idx0;
				int idx = (int) ((elms.length - 1) * idx0);
				Integer val = elms[idx];
				Handle<Integer> handle = h.tree.findHanlde(val);

				BST<Integer> s = h.tree.split(handle);
				BSTTracker t = new BSTTracker(s, heapTrackerIdGen.nextId());
				h.split(val, t);
				treesNext.add(h);
				treesNext.add(t);
			}
		};

		BooleanSupplier doRandOps = () -> {
			for (BSTTracker h : trees) {
				int opsNum = 4096 / trees.size();

				int[] elms = new int[opsNum];
				for (int i = 0; i < elms.length; i++)
					elms[i] = elmGen.next();

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

}
