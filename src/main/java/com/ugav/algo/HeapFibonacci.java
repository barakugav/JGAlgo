package com.ugav.algo;

import java.util.Comparator;
import java.util.Iterator;

import com.ugav.algo.Trees.TreeNode;

public class HeapFibonacci<E> extends HeapAbstractDirectAccessed<E> {

	private Node<E> minRoot;
	private Node<E> begin;
	private Node<E> end;
	private int size;

	public HeapFibonacci() {
		this(null);
	}

	public HeapFibonacci(Comparator<? super E> c) {
		super(c);
		begin = end = minRoot = null;
		size = 0;
	}

	@Override
	public void clear() {
		for (Node<E> p = begin, next; p != null; p = next) {
			next = p.next;
			Trees.clear(p, n -> n.value = null);
		}

		begin = end = minRoot = null;
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Handle<E> insert(E e) {
		Node<E> n = new Node<>(e);
		if (minRoot != null) {
			Node<E> last = end;
			last.next = n;
			n.prev = last;
			if (c.compare(minRoot.value, e) > 0)
				minRoot = n;
		} else {
			begin = n;
			minRoot = n;
		}
		end = n;
		size++;
		return n;
	}

	@Override
	public Iterator<? extends Handle<E>> handleIterator() {
		return new Trees.Iter<>(begin);
	}

	@Override
	public void meld(Heap<? extends E> h0) {
		if (h0 == this || h0.isEmpty())
			return;
		if (!(h0 instanceof HeapFibonacci)) {
			super.meld(h0);
			return;
		}
		@SuppressWarnings("unchecked")
		HeapFibonacci<E> h = (HeapFibonacci<E>) h0;

		if (size == 0) {
			minRoot = h.minRoot;
			begin = h.begin;
			end = h.end;
		} else {
			end.next = h.begin;
			h.begin.prev = end;
			end = h.end;
			compareToMinRoot(h.minRoot);
		}
		size += h.size;

		h.begin = h.end = h.minRoot = null;
		h.size = 0;
	}

	@Override
	public Handle<E> findHanlde(E e) {
		for (Node<E> p : Utils.iterable(new Trees.Iter<>(begin))) {
			if (c.compare(e, p.value) == 0)
				return p;
		}
		return null;
	}

	@Override
	public Node<E> findMinHandle() {
		if (isEmpty())
			throw new IllegalStateException();
		return minRoot;
	}

	private void cut(Node<E> p) {
		assert p.parent != null;
		Node<E> prev = p.prev;
		if (prev != null) {
			prev.next = p.next;
			p.prev = null;
		} else {
			assert p.parent.child == p;
			p.parent.child = p.next;
		}
		p.parent.degree--;
		if (p.next != null) {
			p.next.prev = prev;
			p.next = null;
		}
		p.parent = null;
		p.marked = false;
	}

	private void addRoot(Node<E> p) {
		Node<E> last = end;
		last.next = p;
		p.prev = last;
		end = p;
	}

	private void compareToMinRoot(Node<E> p) {
		assert p.parent == null;
		if (c.compare(minRoot.value, p.value) > 0)
			minRoot = p;
	}

	private void mark(Node<E> p) {
		for (Node<E> q; p.parent != null; p = q) {
			if (!p.marked) {
				p.marked = true;
				break;
			}
			q = p.parent;
			cut(p);
			addRoot(p);
		}
	}

	@Override
	public void decreaseKey(Handle<E> handle, E e) {
		Node<E> parent, n = (Node<E>) handle;
		n.value = e;

		if ((parent = n.parent) == null)
			compareToMinRoot(n);
		if (parent != null && c.compare(e, n.parent.value) < 0) {
			cut(n);
			addRoot(n);
			compareToMinRoot(n);
			mark(parent);
		}
	}

	@Override
	public void removeHandle(Handle<E> handle) {
		Node<E> prev, n = (Node<E>) handle;

		boolean isMinRoot = n == minRoot;
		if (n.parent != null) {
			Node<E> parent = n.parent;
			cut(n);
			mark(parent);
		} else {
			if ((prev = n.prev) != null) {
				prev.next = n.next;
				n.prev = null;
			} else
				begin = n.next;
			if (n.next != null) {
				n.next.prev = prev;
				n.next = null;
			} else {
				end = prev;
			}
		}
		if (--size == 0) {
			minRoot = null;
			return;
		}

		// add n children
		Node<E> first = n.child, last = null;
		if (first != null) {
			for (Node<E> p = first;;) {
				p.parent = null;
				Node<E> next = p.next;
				if (next == null) {
					last = p;
					break;
				}
				p = next;
			}

			if (end != null) {
				end.next = first;
				first.prev = end;
			} else {
				/* root list is empty */
				begin = first;
			}
			end = last;
		}

		// union trees
		@SuppressWarnings("unchecked")
		Node<E>[] newRoots = new Node[getMaxDegree(size)];
		for (Node<E> next, p = begin; p != null; p = next) {
			next = p.next;

			int degree;
			for (Node<E> q; (q = newRoots[degree = p.degree]) != null;) {
				newRoots[degree] = null;
				p = union(p, q);
			}

			newRoots[degree] = p;
		}
		prev = null;
		begin = null;
		for (Node<E> p : newRoots) {
			if (p == null)
				continue;
			if (prev == null)
				begin = p;
			else {
				prev.next = p;
			}
			p.prev = prev;
			prev = p;
		}
		end = prev;
		if (prev != null)
			prev.next = null;

		/* Find new minimum */
		if (isMinRoot) {
			Node<E> min = null;
			for (Node<E> p : newRoots) {
				if (p == null)
					continue;
				if (min == null || c.compare(min.value, p.value) > 0)
					min = p;
			}
			minRoot = min;
		}
	}

	private Node<E> union(Node<E> u, Node<E> v) {
		if (v == minRoot || c.compare(u.value, v.value) > 0) {
			Node<E> temp = u;
			u = v;
			v = temp;
		}
		assert c.compare(u.value, v.value) <= 0;

		v.parent = u;
		v.prev = null;
		v.next = u.child;
		if (u.child != null)
			v.next.prev = v;
		u.child = v;
		u.degree++;

		return u;
	}

	private static class Node<E> implements Handle<E>, TreeNode<Node<E>> {

		Node<E> parent;
		Node<E> next;
		Node<E> prev;
		Node<E> child;
		E value;
		int degree;
		boolean marked;

		Node(E v) {
			parent = null;
			next = null;
			prev = null;
			child = null;
			value = v;
			degree = 0;
			marked = false;
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

	private static final double GOLDEN_RATION = (1 + Math.sqrt(5)) / 2;
	private static final double LOG_GOLDEN_RATION = Math.log(GOLDEN_RATION);
	private static final double LOG_GOLDEN_RATION_INV = 1 / LOG_GOLDEN_RATION;

	private static int getMaxDegree(int size) {
		return (int) (Math.log(size) * LOG_GOLDEN_RATION_INV) + 1;
	}

}
