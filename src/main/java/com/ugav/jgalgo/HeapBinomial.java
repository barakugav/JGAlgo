package com.ugav.jgalgo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;

import com.ugav.jgalgo.Trees.TreeNode;

public class HeapBinomial<E> extends HeapAbstractDirectAccessed<E> {

	private Node<E>[] roots;
	private int rootsLen;
	private int size;

	public HeapBinomial() {
		this(null);
	}

	public HeapBinomial(Comparator<? super E> c) {
		super(c);
		roots = newArr(4);
		rootsLen = 0;
		size = 0;
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
	public void decreaseKey(Handle<E> handle, E e) {
		Node<E> node = (Node<E>) handle;
		node.value = e;

		for (Node<E> p; (p = node.parent) != null;) {
			if (c.compare(p.value, e) <= 0)
				break;
			swapParentChild(p, node);
		}
	}

	@Override
	public void removeHandle(Handle<E> handle) {
		Node<E> node = (Node<E>) handle;

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
	public Handle<E> insert(E e) {
		Node<E> node = new Node<>(e);
		Node<E>[] h2 = newArr(1);
		h2[0] = node;
		size += meld(h2, 1);
		return node;
	}

	@Override
	public Iterator<? extends Handle<E>> handleIterator() {
		return new Itr();
	}

	private class Itr extends Trees.Iter<Node<E>> {

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

	private Node<E> mergeTrees(Node<E> r1, Node<E> r2) {
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
		for (int i = 0; i < rslen; i++) {
			Node<E> r1 = i < rs1len ? rs1[i] : null;
			Node<E> r2 = i < rs2len ? rs2[i] : null;

			if (r2 != null)
				h2size += 1 << i;

			if ((r1 == null && r2 == null) || (r1 != null && r2 != null)) {
				rs[i] = carry;
				carry = (r1 != null && r2 != null) ? mergeTrees(r1, r2) : null;
			} else {
				Node<E> r = r1 != null ? r1 : r2;
				if (carry == null)
					rs[i] = r;
				else {
					rs[i] = null;
					carry = mergeTrees(carry, r);
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
		size += meld(h.roots, h.rootsLen);
	}

	@Override
	public Handle<E> findHanlde(E e) {
		for (Node<E> p : Utils.iterable(new Itr()))
			if (c.compare(e, p.value) == 0)
				return p;
		return null;
	}

	@Override
	public Handle<E> findMinHandle() {
		if (isEmpty())
			throw new IllegalStateException();
		Node<E>[] rs = roots;
		int rsLen = rootsLen;
		Node<E> min = null;

		for (int i = 0; i < rsLen; i++)
			if (rs[i] != null && (min == null || c.compare(min.value, rs[i].value) > 0))
				min = rs[i];
		return min;
	}

	@SuppressWarnings("unchecked")
	private static <E> Node<E>[] newArr(int n) {
		return new Node[n];
	}

	@Override
	public String toString() {
		return formatHeap();
	}

	public String formatHeap() {
		try {
			return formatHeap(new StringBuilder()).toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public Appendable formatHeap(Appendable s) throws IOException {
		Node<E>[] rs = roots;
		int rsLen = rootsLen;

		String[][][] elms = new String[rsLen][][];

		/* trees to elms array */
		for (int i = 0; i < rsLen; i++) {
			if (rs[i] == null)
				continue;
			int width = i == 0 ? 1 : 1 << (i - 1);
			elms[i] = new String[i + 1][width];
			formatTree(rs[i], elms[i], 0, 0, i);
		}

		int longest = 0, deepest = -1, weedest = -1;
		for (int i = 0; i < rsLen; i++) {
			if (elms[i] == null)
				continue;
			for (int y = 0; y < elms[i].length; y++) {
				for (int x = 0; x < elms[i][y].length; x++) {
					String elmStr = elms[i][y][x];
					if (elmStr != null && elmStr.length() > longest)
						longest = elmStr.length();
				}
			}
			if (elms[i].length > deepest)
				deepest = elms[i].length;
			if (elms[i][0].length > weedest)
				weedest = elms[i][0].length;
		}

		CharSequence emptyCell = new CharMultiply(' ', longest + 1);
		for (int y = 0; y < deepest; y++) {
			for (int i = 0; i < rsLen; i++) {
				if (elms[i] == null)
					continue;
				for (int x = 0; x < weedest; x++) {
					String elmStr = y < elms[i].length && x < elms[i][y].length ? elms[i][y][x] : null;
					if (elmStr == null)
						s.append(emptyCell);
					else {
						s.append(elmStr);
						s.append(new CharMultiply(' ', longest - elmStr.length() + 1));
					}
				}

			}
			s.append('\n');
		}

		return s;
	}

	private void formatTree(Node<E> r, String[][] elms, int x, int y, int rank) {
		elms[y][x] = Objects.toString(r.value);
		int xOffset = 0, childRank = rank - 1;
		for (Node<E> c = r.child; c != null; c = c.next) {
			formatTree(c, elms, x + xOffset, y + 1, childRank);
			xOffset += childRank == 0 ? 1 : 1 << (childRank - 1);
			childRank--;
		}
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

	private static class CharMultiply implements CharSequence {

		final char c;
		final int n;

		CharMultiply(char c, int n) {
			if (n <= 0)
				throw new IllegalArgumentException();
			this.c = c;
			this.n = n;
		}

		@Override
		public int length() {
			return n;
		}

		@Override
		public char charAt(int index) {
			if (index < 0 || index >= n)
				throw new IndexOutOfBoundsException(index);
			return c;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			if (start < 0 || end >= n || start >= end)
				throw new IndexOutOfBoundsException();
			return start == 0 && end == n ? this : new CharMultiply(c, end - start);
		}

	}

}
