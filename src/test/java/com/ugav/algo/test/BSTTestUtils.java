package com.ugav.algo.test;

import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.ugav.algo.BST;
import com.ugav.algo.HeapDirectAccessed.Handle;

class BSTTestUtils extends TestUtils {

	private BSTTestUtils() {
		throw new InternalError();
	}

	static boolean findPredecessors(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return BSTTestUtils.findPredecessor(treeBuilder, n);
		});
	}

	static boolean findSuccessors(Supplier<? extends BST<Integer>> treeBuilder) {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return BSTTestUtils.findSuccessor(treeBuilder, n);
		});
	}

	static boolean findPredecessor(Supplier<? extends BST<Integer>> treeBuilder, int n) {
		return findPredecessorSuccessor(treeBuilder, n, true);
	}

	static boolean findSuccessor(Supplier<? extends BST<Integer>> treeBuilder, int n) {
		return findPredecessorSuccessor(treeBuilder, n, false);
	}

	@SuppressWarnings("boxing")
	private static boolean findPredecessorSuccessor(Supplier<? extends BST<Integer>> treeBuilder, int n,
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

}
