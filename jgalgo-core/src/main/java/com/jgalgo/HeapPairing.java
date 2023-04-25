package com.jgalgo;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
	private final Set<HeapReference<E>> refsSet;
	@SuppressWarnings("unchecked")
	private Node<E>[] tempHeapArray = new Node[4];

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
		refsSet = new AbstractSet<>() {

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

		/* disassemble children */
		Node<E>[] heaps = tempHeapArray;
		int heapsNum = 0;
		for (Node<E> p = minRoot.child, next;; p = next) {
			if (heapsNum == heaps.length)
				tempHeapArray = heaps = Arrays.copyOf(heaps, heaps.length * 2);
			heaps[heapsNum++] = p;

			p.prevOrParent = null;
			next = p.next;
			if (next == null)
				break;
			p.next = null;
		}
		minRoot.child = null;

		/* meld all sub heaps */
		minRoot = meld(heaps, heapsNum);
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
		if (oldChild != null) {
			oldChild.prevOrParent = newChild;
			newChild.next = oldChild;
		}
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

	private Node<E> meld(Node<E>[] heaps, int heapsNum) {
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
			Node<E> root = heaps[--heapsNum];
			for (; heapsNum > 0; heapsNum--)
				root = meldDefaultCmp(heaps[heapsNum - 1], root);
			return root;
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
			Node<E> root = heaps[--heapsNum];
			for (; heapsNum > 0; heapsNum--)
				root = meldCustomCmp(heaps[heapsNum - 1], root);
			return root;
		}
	}

	@Override
	public void clear() {
		Arrays.fill(tempHeapArray, null); // help GC
		if (minRoot == null) {
			assert size == 0;
			return;
		}

		for (Node<E> p = minRoot;;) {
			while (p.child != null) {
				p = p.child;
				while (p.next != null)
					p = p.next;
			}
			p.value = null;
			Node<E> prev = p.prevOrParent;
			if (prev == null)
				break;
			p.prevOrParent = null;
			if (prev.next == p) {
				prev.next = null;
			} else {
				prev.child = null;
			}
			p = prev;
		}

		minRoot = null;
		size = 0;
	}

	@Override
	public Set<HeapReference<E>> refsSet() {
		return refsSet;
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

		private final Stack<Node<E>> path = new ObjectArrayList<>();

		PreOrderIter(Node<E> p) {
			if (p != null)
				path.push(p);
		}

		@Override
		public boolean hasNext() {
			return !path.isEmpty();
		}

		@Override
		public Node<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			final Node<E> ret = path.top();

			Node<E> next;
			if ((next = ret.child) != null) {
				path.push(next);
			} else {
				Node<E> p0;
				do {
					p0 = path.pop();
					if ((next = p0.next) != null) {
						path.push(next);
						break;
					}
				} while (!path.isEmpty());
			}

			return ret;
		}

	}

}
