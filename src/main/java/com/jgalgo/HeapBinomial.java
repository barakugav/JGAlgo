package com.jgalgo;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import com.jgalgo.Trees.TreeNode;

public class HeapBinomial<E> extends HeapReferenceableAbstract<E> {

	private Node<E>[] roots;
	private int rootsLen;
	private int size;
	private final Set<HeapReference<E>> refsSet;

	public HeapBinomial() {
		this(null);
	}

	public HeapBinomial(Comparator<? super E> c) {
		super(c);
		roots = newArr(4);
		rootsLen = 0;
		size = 0;

		refsSet = new AbstractSet<>() {

			@Override
			public int size() {
				return HeapBinomial.this.size();
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Iterator<HeapReference<E>> iterator() {
				return (Iterator) new Itr();
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean remove(Object o) {
				HeapBinomial.this.removeRef((HeapReference<E>) o);
				return true;
			}

			@Override
			public void clear() {
				HeapBinomial.this.clear();
			}
		};
	}

	private void swapParentChild(Node<E> parent, Node<E> child) {
		Node<E> t, pNext = parent.next, pPrev = parent.prev, pParent = parent.parent, pChild = parent.child;

		parent.next = (t = child.next);
		if (t != null)
			t.prev = parent;
		parent.prev = (t = child.prev);
		if (t != null)
			t.next = parent;
		parent.child = child.child;
		for (Node<E> p = child.child; p != null; p = p.next)
			p.parent = parent;

		child.next = pNext;
		if (pNext != null)
			pNext.prev = child;
		child.prev = pPrev;
		if (pPrev != null)
			pPrev.next = child;
		child.child = pChild == child ? parent : pChild;
		for (Node<E> p = child.child; p != null; p = p.next)
			p.parent = child;
		child.parent = pParent;
		if (pParent != null && pParent.child == parent)
			pParent.child = child;

		if (pParent == null) {
			/* Switched a root, fix roots array */
			Node<E>[] rs = roots;
			for (int i = 0; i < rootsLen; i++) {
				if (rs[i] == parent) {
					rs[i] = child;
					break;
				}
			}
		}
	}

	@Override
	public void decreaseKey(HeapReference<E> ref, E e) {
		Node<E> node = (Node<E>) ref;
		if (compare(e, node.value) > 0)
			throw new IllegalArgumentException("new key is greater than existing one");
		node.value = e;

		if (c == null) {
			for (Node<E> p; (p = node.parent) != null;) {
				if (Utils.cmpDefault(p.value, e) <= 0)
					break;
				swapParentChild(p, node);
			}
		} else {
			for (Node<E> p; (p = node.parent) != null;) {
				if (c.compare(p.value, e) <= 0)
					break;
				swapParentChild(p, node);
			}
		}
	}

	@Override
	public void removeRef(HeapReference<E> ref) {
		Node<E> node = (Node<E>) ref;

		/* propagate to top of the tree */
		for (Node<E> p; (p = node.parent) != null;)
			swapParentChild(p, node);

		Node<E>[] rs = roots;
		int rootIdx = -1;
		for (int i = 0; i < rootsLen; i++) {
			if (rs[i] == node) {
				rs[i] = null;
				rootIdx = i;
				break;
			}
		}
		if (rootIdx == -1)
			throw new ConcurrentModificationException();

		Node<E>[] childs = newArr(rootIdx);
		Node<E> next, p = node.child;
		for (int i = 0; i < rootIdx; i++, p = next) {
			next = p.next;
			p.parent = null;
			p.next = null;
			p.prev = null;
			childs[rootIdx - i - 1] = p;
		}

		meld(childs, childs.length);
		size--;
	}

	@Override
	public void clear() {
		Node<E>[] rs = roots;
		int rslen = rootsLen;

		for (int i = 0; i < rslen; i++) {
			if (rs[i] != null) {
				Trees.clear(rs[i], n -> n.value = null);
				rs[i] = null;
			}
		}

		size = 0;
		rootsLen = 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public HeapReference<E> insert(E e) {
		Node<E> node = new Node<>(e);
		Node<E>[] h2 = newArr(1);
		h2[0] = node;
		size += meld(h2, 1);
		return node;
	}

	@Override
	public Set<HeapReference<E>> refsSet() {
		return refsSet;
	}

	private Node<E> mergeTreesDefaultCmp(Node<E> r1, Node<E> r2) {
		assert r1 != r2;
		if (r1 == r2)
			throw new IllegalStateException();
		if (Utils.cmpDefault(r1.value, r2.value) > 0) {
			Node<E> t = r1;
			r1 = r2;
			r2 = t;
		}
		r2.next = r1.child;
		Node<E> next = r1.child;
		if (next != null)
			next.prev = r2;
		r1.child = r2;
		r2.parent = r1;

		return r1;
	}

	private Node<E> mergeTreesCustomCmp(Node<E> r1, Node<E> r2) {
		assert r1 != r2;
		if (r1 == r2)
			throw new IllegalStateException();
		if (c.compare(r1.value, r2.value) > 0) {
			Node<E> t = r1;
			r1 = r2;
			r2 = t;
		}
		r2.next = r1.child;
		Node<E> next = r1.child;
		if (next != null)
			next.prev = r2;
		r1.child = r2;
		r2.parent = r1;

		return r1;
	}

	private int meld(Node<E>[] rs2, int rs2len) {
		Node<E>[] rs1 = roots;
		Node<E>[] rs = rs1.length >= rs2.length ? rs1 : rs2;
		int rs1len = rootsLen;
		int rslen = rs1len > rs2len ? rs1len : rs2len;
		int h2size = 0;

		Node<E> carry = null;
		if (c == null) {
			for (int i = 0; i < rslen; i++) {
				Node<E> r1 = i < rs1len ? rs1[i] : null;
				Node<E> r2 = i < rs2len ? rs2[i] : null;

				if (r2 != null)
					h2size += 1 << i;

				if ((r1 == null && r2 == null) || (r1 != null && r2 != null)) {
					rs[i] = carry;
					carry = (r1 != null && r2 != null) ? mergeTreesDefaultCmp(r1, r2) : null;
				} else {
					Node<E> r = r1 != null ? r1 : r2;
					if (carry == null)
						rs[i] = r;
					else {
						rs[i] = null;
						carry = mergeTreesDefaultCmp(carry, r);
					}
				}
			}
		} else {
			for (int i = 0; i < rslen; i++) {
				Node<E> r1 = i < rs1len ? rs1[i] : null;
				Node<E> r2 = i < rs2len ? rs2[i] : null;

				if (r2 != null)
					h2size += 1 << i;

				if ((r1 == null && r2 == null) || (r1 != null && r2 != null)) {
					rs[i] = carry;
					carry = (r1 != null && r2 != null) ? mergeTreesCustomCmp(r1, r2) : null;
				} else {
					Node<E> r = r1 != null ? r1 : r2;
					if (carry == null)
						rs[i] = r;
					else {
						rs[i] = null;
						carry = mergeTreesCustomCmp(carry, r);
					}
				}
			}
		}
		if (carry != null) {
			if (rslen + 1 >= rs.length) {
				Node<E>[] newRoots = newArr(rs.length * 2);
				System.arraycopy(rs, 0, newRoots, 0, rslen);
				rs = newRoots;
			}
			rs[rslen++] = carry;
		}

		roots = rs;
		rootsLen = rslen;
		return h2size;
	}

	@Override
	public void meld(Heap<? extends E> h0) {
		if (h0 == this || h0.isEmpty())
			return;
		if (!(h0 instanceof HeapBinomial)) {
			super.meld(h0);
			return;
		}
		@SuppressWarnings("unchecked")
		HeapBinomial<E> h = (HeapBinomial<E>) h0;
		if (!Objects.equals(comparator(), h.comparator()))
			throw new IllegalArgumentException("Heaps have different comparators");
		size += meld(h.roots, h.rootsLen);
	}

	@Override
	public HeapReference<E> findMinRef() {
		if (isEmpty())
			throw new IllegalStateException();
		Node<E>[] rs = roots;
		int rsLen = rootsLen;
		Node<E> min = null;

		if (c == null) {
			for (int i = 0; i < rsLen; i++)
				if (rs[i] != null && (min == null || Utils.cmpDefault(min.value, rs[i].value) > 0))
					min = rs[i];
		} else {
			for (int i = 0; i < rsLen; i++)
				if (rs[i] != null && (min == null || c.compare(min.value, rs[i].value) > 0))
					min = rs[i];
		}
		return min;
	}

	@SuppressWarnings("unchecked")
	private static <E> Node<E>[] newArr(int n) {
		return new Node[n];
	}

	private static class Node<E> implements HeapReference<E>, TreeNode<Node<E>> {

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

	private class Itr extends Trees.PreOrderIter<Node<E>> {

		private int nextRootIdx;

		Itr() {
			super(null);
			nextRootIdx = 0;
		}

		@Override
		public boolean hasNext() {
			while (!super.hasNext()) {
				if (nextRootIdx >= rootsLen)
					return false;
				int i;
				for (i = nextRootIdx; i < rootsLen; i++) {
					if (roots[i] != null) {
						reset(roots[i]);
						break;
					}
				}
				nextRootIdx = i + 1;
			}
			return true;
		}

	}

}
