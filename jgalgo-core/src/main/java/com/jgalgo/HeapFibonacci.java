/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import com.jgalgo.Trees.TreeNode;

/**
 * A Fibonacci heap implementation.
 * <p>
 * A pointer based heap implementation that support almost any operation in \(O(1)\) amortized time, except
 * {@link #removeRef(HeapReference)} which takes \(O(\log n)\) time amortized.
 * <p>
 * Using this heap, {@link SSSPDijkstra} can be implemented in time \(O(m + n \log n)\) rather than \(O(m \log n)\) as
 * the {@link #decreaseKey(HeapReference, Object)} operation is performed in \(O(1)\) time amortized.
 * <p>
 * In practice, the Fibonacci heaps are quire complex, and in some cases is better to use {@link HeapPairing}.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Fibonacci_heap">Wikipedia</a>
 * @see    HeapPairing
 * @author Barak Ugav
 */
public class HeapFibonacci<E> extends HeapReferenceableAbstract<E> {

	private Node<E> minRoot;
	private Node<E> begin;
	private Node<E> end;
	private int size;
	private final Set<HeapReference<E>> refsSet;

	/**
	 * Constructs a new, empty Fibonacci heap, sorted according to the natural ordering of its elements.
	 * <p>
	 * All elements inserted into the heap must implement the {@link Comparable} interface. Furthermore, all such
	 * elements must be <i>mutually comparable</i>: {@code e1.compareTo(e2)} must not throw a {@code ClassCastException}
	 * for any elements {@code e1} and {@code e2} in the heap. If the user attempts to insert an element to the heap
	 * that violates this constraint (for example, the user attempts to insert a string element to a heap whose elements
	 * are integers), the {@code insert} call will throw a {@code ClassCastException}.
	 */
	public HeapFibonacci() {
		this(null);
	}

	/**
	 * Constructs a new, empty Fibonacci heap, sorted according to the specified comparator.
	 * <p>
	 * All elements inserted into the heap must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(e1, e2)} must not throw a {@code ClassCastException} for any elements {@code e1} and
	 * {@code e2} in the heap. If the user attempts to insert an element to the heap that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this heap. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the elements will be used.
	 */
	public HeapFibonacci(Comparator<? super E> comparator) {
		super(comparator);
		begin = end = minRoot = null;
		size = 0;

		refsSet = new AbstractSet<>() {

			@Override
			public int size() {
				return HeapFibonacci.this.size();
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Iterator<HeapReference<E>> iterator() {
				return (Iterator) new Trees.PreOrderIter<>(begin);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean remove(Object o) {
				HeapFibonacci.this.removeRef((HeapReference<E>) o);
				return true;
			}

			@Override
			public void clear() {
				HeapFibonacci.this.clear();
			}
		};
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
	public HeapReference<E> insert(E e) {
		Node<E> n = new Node<>(e);
		if (minRoot != null) {
			Node<E> last = end;
			last.next = n;
			n.prev = last;
			if (compare(minRoot.value, e) > 0)
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
	public Set<HeapReference<E>> refsSet() {
		return refsSet;
	}

	@Override
	public void meld(Heap<? extends E> heap) {
		makeSureNoMeldWithSelf(heap);
		makeSureMeldWithSameImpl(HeapFibonacci.class, heap);
		makeSureEqualComparatorBeforeMeld(heap);
		@SuppressWarnings("unchecked")
		HeapFibonacci<E> h = (HeapFibonacci<E>) heap;
		if (h.isEmpty())
			return;

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
	public Node<E> findMinRef() {
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
		if (compare(minRoot.value, p.value) > 0)
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
	public void decreaseKey(HeapReference<E> ref, E e) {
		Node<E> parent, n = (Node<E>) ref;
		makeSureDecreaseKeyIsSmaller(n.value, e);
		n.value = e;

		if ((parent = n.parent) == null)
			compareToMinRoot(n);
		if (parent != null && compare(e, n.parent.value) < 0) {
			cut(n);
			addRoot(n);
			compareToMinRoot(n);
			mark(parent);
		}
	}

	@Override
	public void removeRef(HeapReference<E> ref) {
		Node<E> prev, n = (Node<E>) ref;

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
		if (c == null) {
			for (Node<E> next, p = begin; p != null; p = next) {
				next = p.next;

				int degree;
				for (Node<E> q; (q = newRoots[degree = p.degree]) != null;) {
					newRoots[degree] = null;
					p = unionDefaultCmp(p, q);
				}

				newRoots[degree] = p;
			}
		} else {
			for (Node<E> next, p = begin; p != null; p = next) {
				next = p.next;

				int degree;
				for (Node<E> q; (q = newRoots[degree = p.degree]) != null;) {
					newRoots[degree] = null;
					p = unionCustomCmp(p, q);
				}

				newRoots[degree] = p;
			}
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
			if (c == null) {
				for (Node<E> p : newRoots) {
					if (p == null)
						continue;
					if (min == null || Utils.cmpDefault(min.value, p.value) > 0)
						min = p;
				}
			} else {
				for (Node<E> p : newRoots) {
					if (p == null)
						continue;
					if (min == null || c.compare(min.value, p.value) > 0)
						min = p;
				}
			}
			minRoot = min;
		}
	}

	private Node<E> unionDefaultCmp(Node<E> u, Node<E> v) {
		if (v == minRoot || Utils.cmpDefault(u.value, v.value) > 0) {
			Node<E> temp = u;
			u = v;
			v = temp;
		}
		assert Utils.cmpDefault(u.value, v.value) <= 0;

		v.parent = u;
		v.prev = null;
		v.next = u.child;
		if (u.child != null)
			v.next.prev = v;
		u.child = v;
		u.degree++;

		return u;
	}

	private Node<E> unionCustomCmp(Node<E> u, Node<E> v) {
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

	private static class Node<E> implements HeapReference<E>, TreeNode<Node<E>> {

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
