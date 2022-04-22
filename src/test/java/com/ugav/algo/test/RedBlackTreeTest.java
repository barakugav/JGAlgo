package com.ugav.algo.test;

import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Random;
import java.util.TreeSet;

import com.ugav.algo.Heap.Handle;
import com.ugav.algo.RedBlackTree;

@SuppressWarnings("boxing")
public class RedBlackTreeTest extends TestUtils {

	@Test
	public static boolean randOps() {
		return HeapTestUtils.testRandOps(RedBlackTree::new);
	}

	@Test
	public static boolean randOpsAfterManyInserts() {
		return HeapTestUtils.testRandOpsAfterManyInserts(RedBlackTree::new);
	}

	@Test
	public static boolean meld() {
		return HeapTestUtils.testMeld(RedBlackTree::new);
	}

	@Test
	public static boolean findPredecessor() {
		return findPredecessorSuccessor(true);
	}

	@Test
	public static boolean findSuccessor() {
		return findPredecessorSuccessor(false);
	}

	private static boolean findPredecessorSuccessor(boolean predecessor) {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			int[] a = Utils.randPermutation(n, nextRandSeed());

			RedBlackTree<Integer> tree = new RedBlackTree<>();
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
					Handle<Integer> actualH = tree.findPredecessorHandle(h);
					actual = actualH == null ? null : actualH.get();
					expected = ctrl.lower(searchedElm);
				} else {
					Handle<Integer> actualH = tree.findSuccessorHandle(h);
					actual = actualH == null ? null : actualH.get();
					expected = ctrl.higher(searchedElm);
				}

				if (!Objects.equals(actual, expected)) {
					printTestStr("Failed to find predecessor/successor of ", searchedElm, " : ", actual, " != ",
							expected, "\n");
					return false;

				}
			}
			return true;
		});
	}
}
