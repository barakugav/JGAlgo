package com.ugav.algo.test;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.ugav.algo.Heap;
import com.ugav.algo.HeapDirectAccessed.Handle;
import com.ugav.algo.RedBlackTree;
import com.ugav.algo.RedBlackTreeExtended;
import com.ugav.algo.RedBlackTreeExtended.ExtensionMax;
import com.ugav.algo.RedBlackTreeExtended.ExtensionMin;
import com.ugav.algo.RedBlackTreeExtended.ExtensionSize;
import com.ugav.algo.test.HeapTestUtils.TestMode;

@SuppressWarnings("boxing")
public class RedBlackTreeExtendedTest extends TestUtils {

	private static class HeapWrapper<E> implements Heap<E> {

		private final Heap<E> h;

		public HeapWrapper(Heap<E> heap) {
			h = Objects.requireNonNull(heap);
		}

		@Override
		public int size() {
			return h.size();
		}

		@Override
		public boolean isEmpty() {
			return h.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return h.contains(o);
		}

		@Override
		public Iterator<E> iterator() {
			return h.iterator();
		}

		@Override
		public Object[] toArray() {
			return h.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return h.toArray(a);
		}

		@Override
		public boolean add(E e) {
			return h.add(e);
		}

		@Override
		public boolean remove(Object o) {
			return h.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return h.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			return h.addAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return h.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return h.retainAll(c);
		}

		@Override
		public void clear() {
			h.clear();
		}

		@Override
		public Handle<E> insert(E e) {
			return h.insert(e);
		}

		@Override
		public E findMin() {
			return h.findMin();
		}

		@Override
		public E extractMin() {
			return h.extractMin();
		}

		@Override
		public void meld(Heap<? extends E> other) {
			h.meld(other);
		}

	}

	private static class SizeValidatorTree<E> extends HeapWrapper<E> {

		private SizeValidatorTree(RedBlackTree<E> tree, ExtensionSize<E> sizeExt) {
			super(tree);
		}

		@SuppressWarnings("unused")
		static <E> SizeValidatorTree<E> newInstance(Comparator<? super E> c) {
			RedBlackTreeExtended.Builder<E> builder = new RedBlackTreeExtended.Builder<>();
			builder.comparator(c);
			ExtensionSize<E> sizeExt = builder.addSizeExtension();
			RedBlackTree<E> tree = builder.build();
			return new SizeValidatorTree<>(tree, sizeExt);
		}

	}

	@Test
	public static void extensionSizeRandOps() {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			RedBlackTreeExtended.Builder<Integer> builder = new RedBlackTreeExtended.Builder<>();
			builder.comparator(null);
			ExtensionSize<Integer> sizeExt = builder.addSizeExtension();
			RedBlackTree<Integer> tree = builder.build();

			HeapTestUtils.testHeap(tree, n, m, TestMode.Normal, false);

			for (Handle<Integer> node : Utils.iterable(tree.handleIterator())) {
				final var expectedSize = new Object() {
					int val = 0;
				};
				tree.forEachNodeInSubTree(node, descendant -> expectedSize.val++);

				int actualSize = sizeExt.getSubTreeSize(node);
				assertEq(expectedSize.val, actualSize, "Size extension repored wrong value");
			}
		});
	}

	@Test
	public static void extensionMinRandOps() {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			RedBlackTreeExtended.Builder<Integer> builder = new RedBlackTreeExtended.Builder<>();
			builder.comparator(null);
			ExtensionMin<Integer> minExt = builder.addMinExtension();
			RedBlackTree<Integer> tree = builder.build();

			HeapTestUtils.testHeap(tree, n, m, TestMode.Normal, false);

			for (Handle<Integer> node : Utils.iterable(tree.handleIterator())) {
				final var expectedMin = new Object() {
					int val = Integer.MAX_VALUE;
				};
				tree.forEachNodeInSubTree(node,
						descendant -> expectedMin.val = Math.min(expectedMin.val, descendant.get()));

				int actualMin = minExt.getSubTreeMin(node).get();
				assertEq(expectedMin.val, actualMin, "Min extension repored wrong value");
			}
		});
	}

	@Test
	public static void extensionMaxRandOps() {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			RedBlackTreeExtended.Builder<Integer> builder = new RedBlackTreeExtended.Builder<>();
			builder.comparator(null);
			ExtensionMax<Integer> maxExt = builder.addMaxExtension();
			RedBlackTree<Integer> tree = builder.build();

			HeapTestUtils.testHeap(tree, n, m, TestMode.Normal, false);
			for (Handle<Integer> node : Utils.iterable(tree.handleIterator())) {
				final var expectedMax = new Object() {
					int val = Integer.MIN_VALUE;
				};
				tree.forEachNodeInSubTree(node,
						descendant -> expectedMax.val = Math.max(expectedMax.val, descendant.get()));

				int actualMax = maxExt.getSubTreeMax(node).get();
				assertEq(expectedMax.val, actualMax, "Max extension repored wrong value");
			}
		});
	}

}
