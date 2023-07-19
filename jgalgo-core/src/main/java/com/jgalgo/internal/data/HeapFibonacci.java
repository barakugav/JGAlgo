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

package com.jgalgo.internal.data;

import java.util.Comparator;
import java.util.Iterator;
import com.jgalgo.internal.data.Heaps.AbstractHeapReferenceable;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;

/**
 * A Fibonacci heap implementation.
 * <p>
 * A pointer based heap implementation that support almost any operation in \(O(1)\) amortized time, except
 * {@link #remove(HeapReference)} which takes \(O(\log n)\) time amortized.
 * <p>
 * Using this heap, Dijkstra's shortest-path algorithm can be implemented in time \(O(m + n \log n)\) rather than \(O(m
 * \log n)\) as the {@link #decreaseKey(HeapReference, Object)} operation is performed in \(O(1)\) time amortized.
 * <p>
 * In practice, the Fibonacci heaps are quire complex, and in some cases is better to use {@link HeapPairing}.
 *
 * @param  <K> the keys type
 * @param  <V> the values type
 * @see        <a href="https://en.wikipedia.org/wiki/Fibonacci_heap">Wikipedia</a>
 * @see        HeapPairing
 * @author     Barak Ugav
 */
class HeapFibonacci<K, V> extends AbstractHeapReferenceable<K, V> {

	private Node<K, V> minRoot;
	private Node<K, V> begin;
	private Node<K, V> end;
	private int size;

	/**
	 * Constructs a new, empty Fibonacci heap, ordered according to the natural ordering of its keys.
	 * <p>
	 * All keys inserted into the heap must implement the {@link Comparable} interface. Furthermore, all such keys must
	 * be <i>mutually comparable</i>: {@code k1.compareTo(k2)} must not throw a {@code ClassCastException} for any keys
	 * {@code k1} and {@code k2} in the heap. If the user attempts to insert a key to the heap that violates this
	 * constraint (for example, the user attempts to insert a string element to a heap whose keys are integers), the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 */
	HeapFibonacci() {
		this(null);
	}

	/**
	 * Constructs a new, empty Fibonacci heap, with keys ordered according to the specified comparator.
	 * <p>
	 * All keys inserted into the heap must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(k1, k2)} must not throw a {@code ClassCastException} for any keys {@code k1} and
	 * {@code k2} in the heap. If the user attempts to insert a key to the heap that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this heap. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the keys will be used.
	 */
	HeapFibonacci(Comparator<? super K> comparator) {
		super(comparator);
		begin = end = minRoot = null;
		size = 0;
	}

	@Override
	public void clear() {
		for (Node<K, V> p = begin, next; p != null; p = next) {
			next = p.next;
			Trees.clear(p, n -> n.key = null);
		}

		begin = end = minRoot = null;
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public HeapReference<K, V> insert(K key) {
		Node<K, V> n = new Node<>(key);
		if (minRoot != null) {
			Node<K, V> last = end;
			last.next = n;
			n.prev = last;
			if (compare(minRoot.key, key) > 0)
				minRoot = n;
		} else {
			begin = n;
			minRoot = n;
		}
		end = n;
		size++;
		return n;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<HeapReference<K, V>> iterator() {
		return (Iterator) new Trees.PreOrderIter<>(begin);
	}

	@Override
	public void meld(HeapReferenceable<? extends K, ? extends V> heap) {
		Assertions.Heaps.noMeldWithSelf(this, heap);
		Assertions.Heaps.meldWithSameImpl(HeapFibonacci.class, heap);
		Assertions.Heaps.equalComparatorBeforeMeld(this, heap);
		@SuppressWarnings("unchecked")
		HeapFibonacci<K, V> h = (HeapFibonacci<K, V>) heap;
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
	public HeapReference<K, V> findMin() {
		Assertions.Heaps.notEmpty(this);
		return minRoot;
	}

	private void cut(Node<K, V> p) {
		assert p.parent != null;
		Node<K, V> prev = p.prev;
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

	private void addRoot(Node<K, V> p) {
		Node<K, V> last = end;
		last.next = p;
		p.prev = last;
		end = p;
	}

	private void compareToMinRoot(Node<K, V> p) {
		assert p.parent == null;
		if (compare(minRoot.key, p.key) > 0)
			minRoot = p;
	}

	private void mark(Node<K, V> p) {
		for (Node<K, V> q; p.parent != null; p = q) {
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
	public void decreaseKey(HeapReference<K, V> ref, K newKey) {
		Node<K, V> parent, n = (Node<K, V>) ref;
		Assertions.Heaps.decreaseKeyIsSmaller(n.key, newKey, c);
		n.key = newKey;

		if ((parent = n.parent) == null)
			compareToMinRoot(n);
		if (parent != null && compare(newKey, n.parent.key) < 0) {
			cut(n);
			addRoot(n);
			compareToMinRoot(n);
			mark(parent);
		}
	}

	@Override
	public void remove(HeapReference<K, V> ref) {
		Node<K, V> prev, n = (Node<K, V>) ref;

		boolean isMinRoot = n == minRoot;
		if (n.parent != null) {
			Node<K, V> parent = n.parent;
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
		Node<K, V> first = n.child, last = null;
		if (first != null) {
			for (Node<K, V> p = first;;) {
				p.parent = null;
				Node<K, V> next = p.next;
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
		Node<K, V>[] newRoots = new Node[getMaxDegree(size)];
		if (c == null) {
			for (Node<K, V> next, p = begin; p != null; p = next) {
				next = p.next;

				int degree;
				for (Node<K, V> q; (q = newRoots[degree = p.degree]) != null;) {
					newRoots[degree] = null;
					p = unionDefaultCmp(p, q);
				}

				newRoots[degree] = p;
			}
		} else {
			for (Node<K, V> next, p = begin; p != null; p = next) {
				next = p.next;

				int degree;
				for (Node<K, V> q; (q = newRoots[degree = p.degree]) != null;) {
					newRoots[degree] = null;
					p = unionCustomCmp(p, q);
				}

				newRoots[degree] = p;
			}
		}
		prev = null;
		begin = null;
		for (Node<K, V> p : newRoots) {
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
			Node<K, V> min = null;
			if (c == null) {
				for (Node<K, V> p : newRoots) {
					if (p == null)
						continue;
					if (min == null || JGAlgoUtils.cmpDefault(min.key, p.key) > 0)
						min = p;
				}
			} else {
				for (Node<K, V> p : newRoots) {
					if (p == null)
						continue;
					if (min == null || c.compare(min.key, p.key) > 0)
						min = p;
				}
			}
			minRoot = min;
		}
	}

	private Node<K, V> unionDefaultCmp(Node<K, V> u, Node<K, V> v) {
		if (v == minRoot || JGAlgoUtils.cmpDefault(u.key, v.key) > 0) {
			Node<K, V> temp = u;
			u = v;
			v = temp;
		}
		assert JGAlgoUtils.cmpDefault(u.key, v.key) <= 0;

		v.parent = u;
		v.prev = null;
		v.next = u.child;
		if (u.child != null)
			v.next.prev = v;
		u.child = v;
		u.degree++;

		return u;
	}

	private Node<K, V> unionCustomCmp(Node<K, V> u, Node<K, V> v) {
		if (v == minRoot || c.compare(u.key, v.key) > 0) {
			Node<K, V> temp = u;
			u = v;
			v = temp;
		}
		assert c.compare(u.key, v.key) <= 0;

		v.parent = u;
		v.prev = null;
		v.next = u.child;
		if (u.child != null)
			v.next.prev = v;
		u.child = v;
		u.degree++;

		return u;
	}

	private static class Node<K, V> extends Trees.TreeNodeImpl<Node<K, V>> implements HeapReference<K, V> {

		K key;
		V value;
		int degree;
		boolean marked;

		Node(K key) {
			parent = null;
			next = null;
			prev = null;
			child = null;
			this.key = key;
			degree = 0;
			marked = false;
		}

		@Override
		public K key() {
			return key;
		}

		@Override
		public V value() {
			return value;
		}

		@Override
		public void setValue(V val) {
			value = val;
		}

		@Override
		public String toString() {
			return "{" + key + ":" + value + "}";
		}

	}

	private static final double GOLDEN_RATION = (1 + Math.sqrt(5)) / 2;
	private static final double LOG_GOLDEN_RATION = Math.log(GOLDEN_RATION);
	private static final double LOG_GOLDEN_RATION_INV = 1 / LOG_GOLDEN_RATION;

	private static int getMaxDegree(int size) {
		return (int) (Math.log(size) * LOG_GOLDEN_RATION_INV) + 1;
	}

}
