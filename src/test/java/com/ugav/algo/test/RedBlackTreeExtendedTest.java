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
import com.ugav.algo.RedBlackTreeExtended.ExtensionSize;
import com.ugav.algo.test.HeapTestUtils.TestMode;

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

	@SuppressWarnings("boxing")
	@Test
	public static boolean randOps() {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			int m = args[1];

			RedBlackTreeExtended.Builder<Integer> builder = new RedBlackTreeExtended.Builder<>();
			builder.comparator(null);
			ExtensionSize<Integer> sizeExt = builder.addSizeExtension();
			RedBlackTree<Integer> tree = builder.build();

			if (!HeapTestUtils.testHeap(tree, n, m, TestMode.Normal, false))
				return false;

			for (Handle<Integer> node : Utils.iterable(tree.handleIterator())) {
				final var expectedSizeWrapper = new Object() {
					int val = 0;
				};
				tree.forEachNodeInSubTree(node, descendant -> expectedSizeWrapper.val++);
				int expectedSize = expectedSizeWrapper.val;
				int actualSize = sizeExt.getSubTreeSize(node);
				if (expectedSize != actualSize) {
					printTestStr("Sixe extension repored wrong value: ", expectedSize, " != ", actualSize, "\n");
					return false;
				}
			}
			return true;
		});
	}

}
