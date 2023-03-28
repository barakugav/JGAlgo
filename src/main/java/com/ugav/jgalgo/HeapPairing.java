package com.ugav.jgalgo;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import com.ugav.jgalgo.Trees.TreeNode;

public class HeapPairing<E> extends HeapAbstractDirectAccessed<E> {

	private Node<E> minRoot;
	private int size;

	public HeapPairing() {
		this(null);
	}

	public HeapPairing(Comparator<? super E> c) {
		super(c);
	}

	@Override
	public Handle<E> findMinHandle() {
		if (isEmpty())
			throw new IllegalStateException();
		return minRoot;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Handle<E> insert(E e) {
		Node<E> n = new Node<>(e);
		if (minRoot == null) {
			minRoot = n;
			assert size == 0;
		} else {
			minRoot = meld0(minRoot, n);
		}
		size++;
		return n;
	}

	@Override
	public void decreaseKey(Handle<E> handle, E e) {
		Node<E> n = (Node<E>) handle;
		assert c.compare(n.value, e) >= 0;
		n.value = e;
		if (n == minRoot)
			return;
		cut(n);
		minRoot = meld0(minRoot, n);
	}

	private static <E> void cut(Node<E> n) {
		Node<E> next = n.next;
		if (next != null) {
			next.prev = n.prev;
			n.next = null;
		}
		if (n.prev != null) {
			n.prev.next = next;
			n.prev = null;
		} else {
			assert n.parent.child == n;
			n.parent.child = next;
		}
		n.parent = null;
	}

	@Override
	public void removeHandle(Handle<E> handle) {
		Node<E> n = (Node<E>) handle;
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
		minRoot = meld0(heaps);
	}

	private static int childrenNum(Node<?> n) {
		int count = 0; // rename
		for (Node<?> p = n.child; p != null; p = p.next)
			count++;
		return count;
	}

	private static <E> void disassembleChildren(Node<E> n, Node<E>[] childrenOut) {
		int idx = 0;
		for (Node<E> p = n.child, next; p != null; p = next) {
			next = p.next;
			p.next = p.prev = p.parent = null;
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

		if (size == 0) {
			minRoot = h.minRoot;
		} else if (h.minRoot != null) {
			minRoot = meld0(minRoot, h.minRoot);
		}
		size += h.size;

		h.minRoot = null;
		h.size = 0;
	}

	private Node<E> meld0(Node<E> n1, Node<E> n2) {
		assert n1.prev == null;
		assert n1.next == null;
		assert n1.parent == null;
		assert n2.prev == null;
		assert n2.next == null;
		assert n2.parent == null;

		/* assume n1 has smaller key than n2 */
		if (c.compare(n1.value, n2.value) > 0) {
			Node<E> temp = n1;
			n1 = n2;
			n2 = temp;
		}

		addChild(n1, n2);
		return n1;
	}

	private void addChild(Node<E> parent, Node<E> newChild) {
		assert newChild.parent == null;
		assert newChild.prev == null;
		assert newChild.next == null;
		Node<E> oldChild = parent.child;
		newChild.next = oldChild;
		if (oldChild != null)
			oldChild.prev = newChild;
		parent.child = newChild;
		newChild.parent = parent;
	}

	private Node<E> meld0(Node<E>[] heaps) {
		int heapsNum = heaps.length;

		/* meld pairs from left to right */
		for (int i = 0; i < heapsNum / 2; i++) {
			Node<E> n1 = heaps[i * 2 + 0];
			Node<E> n2 = heaps[i * 2 + 1];
			heaps[i] = meld0(n1, n2);
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
			heaps[heapsNum - 2] = meld0(n1, n2);
			heapsNum--;
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
		Trees.clear(minRoot, n -> n.value = null);
		minRoot = null;
		size = 0;
	}

	@Override
	public Set<Handle<E>> handles() {
		return new AbstractSet<>() {

			@Override
			public int size() {
				return size;
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Iterator<Handle<E>> iterator() {
				return (Iterator) new Trees.PreOrderIter<>(minRoot);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean remove(Object o) {
				removeHandle((Handle<E>) o);
				return true;
			}

			@Override
			public void clear() {
				HeapPairing.this.clear();
			}

		};
	}

	private static class Node<E> implements Handle<E>, TreeNode<Node<E>> {

		Node<E> parent;
		Node<E> next;
		Node<E> prev;
		Node<E> child;
		E value;

		Node(E v) {
			parent = null;
			next = null;
			prev = null;
			child = null;
			value = v;
		}

		@Override
		public E get() {
			return value;
		}

		@Override
		public Node<E> parent() {
			return parent;
		}

		@Override
		public Node<E> next() {
			return next;
		}

		@Override
		public Node<E> prev() {
			return prev;
		}

		@Override
		public Node<E> child() {
			return child;
		}

		@Override
		public void setParent(Node<E> x) {
			parent = x;
		}

		@Override
		public void setNext(Node<E> x) {
			next = x;
		}

		@Override
		public void setPrev(Node<E> x) {
			prev = x;
		}

		@Override
		public void setChild(Node<E> x) {
			child = x;
		}

		@Override
		public String toString() {
			return "{" + value + "}";
		}

	}

}
