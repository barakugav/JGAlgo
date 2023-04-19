package com.jgalgo;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * A Pairing heap implementation.
 * <p>
 * A pointer based heap implementation that support almost any operation in
 * {@code O(1)} amortized time, except {@link #removeRef(HeapReference)} and
 * {@link #decreaseKey(HeapReference, Object)} which takes {@code O(log n)} time
 * amortized.
 * <p>
 * Using this heap, {@link SSSPDijkstra} can be implemented in time
 * {@code O(m + n log n)} rather than {@code O(m log n)} as the
 * {@link #decreaseKey(HeapReference, Object)} operation is performed in
 * {@code O(1)} time amortized.
 * <p>
 * Pairing heaps are one of the best pointer based heaps implementations in
 * practice, and should be used as a default choice for the general use case.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Pairing_heap">Wikipedia</a>
 * @author Barak Ugav
 */
public class HeapPairing<E> extends HeapReferenceableAbstract<E> {

	private Node<E> minRoot;
	private int size;

	/**
	 * Constructs a new, empty Pairing heap, sorted according to the natural
	 * ordering of its elements.
	 * <p>
	 * All elements inserted into the heap must implement the {@link Comparable}
	 * interface. Furthermore, all such elements must be <i>mutually comparable</i>:
	 * {@code e1.compareTo(e2)} must not throw a {@code ClassCastException} for any
	 * elements {@code e1} and {@code e2} in the heap. If the user attempts to
	 * insert an element to the heap that violates this constraint (for example, the
	 * user attempts to insert a string element to a heap whose elements are
	 * integers), the {@code insert} call will throw a {@code ClassCastException}.
	 */
	public HeapPairing() {
		this(null);
	}

	/**
	 * Constructs a new, empty Pairing heap, sorted according to the specified
	 * comparator.
	 * <p>
	 * All elements inserted into the heap must be <i>mutually comparable</i> by the
	 * specified comparator: {@code comparator.compare(e1, e2)} must not throw a
	 * {@code ClassCastException} for any elements {@code e1} and {@code e2} in the
	 * heap. If the user attempts to insert an element to the heap that violates
	 * this constraint, the {@code insert} call will throw a
	 * {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this heap.
	 *                   If {@code null}, the {@linkplain Comparable natural
	 *                   ordering} of the elements will be used.
	 */
	public HeapPairing(Comparator<? super E> comparator) {
		super(comparator);
	}

	@Override
	public HeapReference<E> findMinRef() {
		if (isEmpty())
			throw new IllegalStateException();
		return minRoot;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public HeapReference<E> insert(E e) {
		Node<E> n = new Node<>(e);
		if (minRoot == null) {
			minRoot = n;
			assert size == 0;
		} else {
			minRoot = meld(minRoot, n);
		}
		size++;
		return n;
	}

	@Override
	public void decreaseKey(HeapReference<E> ref, E e) {
		Node<E> n = (Node<E>) ref;
		if (compare(e, n.value) > 0)
			throw new IllegalArgumentException("new key is greater than existing one");
		n.value = e;
		if (n == minRoot)
			return;
		cut(n);
		minRoot = meld(minRoot, n);
	}

	private static <E> void cut(Node<E> n) {
		Node<E> next = n.next;
		if (next != null) {
			next.prevOrParent = n.prevOrParent;
			n.next = null;
		}
		if (n.prevOrParent.child == n) { /* n.parent.child == n */
			n.prevOrParent.child = next;
		} else {
			n.prevOrParent.next = next;
		}
		n.prevOrParent = null;
	}

	@Override
	public void removeRef(HeapReference<E> ref) {
		Node<E> n = (Node<E>) ref;
		assert minRoot != null;
		if (n != minRoot) {
			cut(n);
			addChild(n, minRoot);
			minRoot = n;
		}
		removeRoot();
		size--;
	}

	private void removeRoot() {
		if (minRoot.child == null) {
			minRoot = null;
			return;
		}

		/* collect and disassemble children */
		int heapsNum = childrenNum(minRoot);
		@SuppressWarnings("unchecked")
		Node<E>[] heaps = new Node[heapsNum];
		disassembleChildren(minRoot, heaps);

		/* meld all sub heaps */
		minRoot = meld(heaps);
	}

	private static int childrenNum(Node<?> n) {
		int count = 0;
		for (Node<?> p = n.child; p != null; p = p.next)
			count++;
		return count;
	}

	private static <E> void disassembleChildren(Node<E> n, Node<E>[] childrenOut) {
		int idx = 0;
		for (Node<E> p = n.child, next; p != null; p = next) {
			next = p.next;
			p.next = p.prevOrParent = null;
			childrenOut[idx++] = p;
		}
		n.child = null;
	}

	@Override
	public void meld(Heap<? extends E> h0) {
		if (h0 == this || h0.isEmpty())
			return;
		if (!(h0 instanceof HeapPairing)) {
			super.meld(h0);
			return;
		}
		@SuppressWarnings("unchecked")
		HeapPairing<E> h = (HeapPairing<E>) h0;
		if (!Objects.equals(comparator(), h.comparator()))
			throw new IllegalArgumentException("Heaps have different comparators");

		if (size == 0) {
			assert minRoot == null;
			minRoot = h.minRoot;
		} else if (h.minRoot != null) {
			minRoot = meld(minRoot, h.minRoot);
		}
		size += h.size;

		h.minRoot = null;
		h.size = 0;
	}

	private void addChild(Node<E> parent, Node<E> newChild) {
		assert newChild.prevOrParent == null;
		assert newChild.next == null;
		Node<E> oldChild = parent.child;
		newChild.next = oldChild;
		if (oldChild != null)
			oldChild.prevOrParent = newChild;
		parent.child = newChild;
		newChild.prevOrParent = parent;
	}

	private Node<E> meld(Node<E> n1, Node<E> n2) {
		return c == null ? meldDefaultCmp(n1, n2) : meldCustomCmp(n1, n2);
	}

	private Node<E> meldDefaultCmp(Node<E> n1, Node<E> n2) {
		assert n1.prevOrParent == null;
		assert n1.next == null;
		assert n2.prevOrParent == null;
		assert n2.next == null;

		/* assume n1 has smaller key than n2 */
		if (Utils.cmpDefault(n1.value, n2.value) > 0) {
			Node<E> temp = n1;
			n1 = n2;
			n2 = temp;
		}

		addChild(n1, n2);
		return n1;
	}

	private Node<E> meldCustomCmp(Node<E> n1, Node<E> n2) {
		assert n1.prevOrParent == null;
		assert n1.next == null;
		assert n2.prevOrParent == null;
		assert n2.next == null;

		/* assume n1 has smaller key than n2 */
		if (c.compare(n1.value, n2.value) > 0) {
			Node<E> temp = n1;
			n1 = n2;
			n2 = temp;
		}

		addChild(n1, n2);
		return n1;
	}

	private Node<E> meld(Node<E>[] heaps) {
		int heapsNum = heaps.length;

		if (c == null) {
			/* meld pairs from left to right */
			for (int i = 0; i < heapsNum / 2; i++) {
				Node<E> n1 = heaps[i * 2 + 0];
				Node<E> n2 = heaps[i * 2 + 1];
				heaps[i] = meldDefaultCmp(n1, n2);
			}
			/* handle last heap in case heapNum is odd */
			if (heapsNum % 2 != 0)
				heaps[heapsNum / 2] = heaps[heapsNum - 1];
			/* div by two ceil */
			heapsNum = (heapsNum + 1) / 2;

			/* meld all from right to left */
			while (heapsNum > 1) {
				Node<E> n1 = heaps[heapsNum - 2];
				Node<E> n2 = heaps[heapsNum - 1];
				heaps[heapsNum - 2] = meldDefaultCmp(n1, n2);
				heapsNum--;
			}
		} else {
			/* meld pairs from left to right */
			for (int i = 0; i < heapsNum / 2; i++) {
				Node<E> n1 = heaps[i * 2 + 0];
				Node<E> n2 = heaps[i * 2 + 1];
				heaps[i] = meldCustomCmp(n1, n2);
			}
			/* handle last heap in case heapNum is odd */
			if (heapsNum % 2 != 0)
				heaps[heapsNum / 2] = heaps[heapsNum - 1];
			/* div by two ceil */
			heapsNum = (heapsNum + 1) / 2;

			/* meld all from right to left */
			while (heapsNum > 1) {
				Node<E> n1 = heaps[heapsNum - 2];
				Node<E> n2 = heaps[heapsNum - 1];
				heaps[heapsNum - 2] = meldCustomCmp(n1, n2);
				heapsNum--;
			}
		}

		Node<E> root = heaps[0];
		Arrays.fill(heaps, null); // help GC
		return root;
	}

	@Override
	public void clear() {
		if (minRoot == null) {
			assert size == 0;
			return;
		}

		List<Node<E>> stack = new ArrayList<>();
		stack.add(minRoot);
		do {
			int idx = stack.size() - 1;
			Node<E> n = stack.get(idx);
			stack.remove(idx);

			for (Node<E> p = n.child; p != null; p = p.next)
				stack.add(p);

			n.prevOrParent = n.next = n.child = null;
			n.value = null;
		} while (!stack.isEmpty());

		minRoot = null;
		size = 0;
	}

	@Override
	public Set<HeapReference<E>> refsSet() {
		return new AbstractSet<>() {

			@Override
			public int size() {
				return size;
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Iterator<HeapReference<E>> iterator() {
				return (Iterator) new PreOrderIter<>(minRoot);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean remove(Object o) {
				removeRef((HeapReference<E>) o);
				return true;
			}

			@Override
			public void clear() {
				HeapPairing.this.clear();
			}

		};
	}

	private static class Node<E> implements HeapReference<E> {

		Node<E> prevOrParent;
		Node<E> next;
		Node<E> child;
		E value;

		Node(E v) {
			value = v;
		}

		@Override
		public E get() {
			return value;
		}

		@Override
		public String toString() {
			return "{" + value + "}";
		}

	}

	private static class PreOrderIter<E> implements Iterator<Node<E>> {

		private final List<Node<E>> path = new ArrayList<>();

		PreOrderIter(Node<E> p) {
			if (p != null)
				path.add(p);
		}

		@Override
		public boolean hasNext() {
			return !path.isEmpty();
		}

		@Override
		public Node<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			final Node<E> ret = path.get(path.size() - 1);

			Node<E> next;
			if ((next = ret.child) != null) {
				path.add(next);
			} else {
				Node<E> p0;
				do {
					p0 = path.remove(path.size() - 1);
					if ((next = p0.next) != null) {
						path.add(next);
						break;
					}
				} while (!path.isEmpty());
			}

			return ret;
		}

	}

}
