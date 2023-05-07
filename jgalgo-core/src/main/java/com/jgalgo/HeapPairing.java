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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * A Pairing heap implementation.
 * <p>
 * A pointer based heap implementation that support almost any operation in \(O(1)\) amortized time, except
 * {@link #remove(HeapReference)} and {@link #decreaseKey(HeapReference, Object)} which takes \(O(\log n)\) time
 * amortized.
 * <p>
 * Using this heap, {@link SSSPDijkstra} can be implemented in time \(O(m + n \log n)\) rather than \(O(m \log n)\) as
 * the {@link #decreaseKey(HeapReference, Object)} operation is performed in \(O(1)\) time amortized.
 * <p>
 * Pairing heaps are one of the best pointer based heaps implementations in practice, and should be used as a default
 * choice for the general use case.
 *
 * @param  <K> the keys type
 * @param  <V> the values type
 * @see        <a href="https://en.wikipedia.org/wiki/Pairing_heap">Wikipedia</a>
 * @author     Barak Ugav
 */
public class HeapPairing<K, V> extends HeapReferenceableAbstract<K, V> {

	private Node<K, V> minRoot;
	private int size;
	@SuppressWarnings("unchecked")
	private Node<K, V>[] tempHeapArray = new Node[4];

	/**
	 * Constructs a new, empty Pairing heap, ordered according to the natural ordering of its keys.
	 * <p>
	 * All keys inserted into the heap must implement the {@link Comparable} interface. Furthermore, all such keys must
	 * be <i>mutually comparable</i>: {@code k1.compareTo(k2)} must not throw a {@code ClassCastException} for any keys
	 * {@code k1} and {@code k2} in the heap. If the user attempts to insert a key to the heap that violates this
	 * constraint (for example, the user attempts to insert a string element to a heap whose keys are integers), the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 */
	public HeapPairing() {
		this(null);
	}

	/**
	 * Constructs a new, empty Pairing heap, with keys ordered according to the specified comparator.
	 * <p>
	 * All keys inserted into the heap must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(k1, k2)} must not throw a {@code ClassCastException} for any keys {@code k1} and
	 * {@code k2} in the heap. If the user attempts to insert a key to the heap that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this heap. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the keys will be used.
	 */
	public HeapPairing(Comparator<? super K> comparator) {
		super(comparator);
	}

	@Override
	public HeapReference<K, V> findMin() {
		if (isEmpty())
			throw new IllegalStateException();
		return minRoot;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public HeapReference<K, V> insert(K key) {
		Node<K, V> n = new Node<>(key);
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
	public void decreaseKey(HeapReference<K, V> ref, K newKey) {
		Node<K, V> n = (Node<K, V>) ref;
		makeSureDecreaseKeyIsSmaller(n.key, newKey);
		n.key = newKey;
		if (n == minRoot)
			return;
		cut(n);
		minRoot = meld(minRoot, n);
	}

	private static <K, V> void cut(Node<K, V> n) {
		Node<K, V> next = n.next;
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
	public void remove(HeapReference<K, V> ref) {
		Node<K, V> n = (Node<K, V>) ref;
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
		Node<K, V>[] heaps = tempHeapArray;
		int heapsNum = 0;
		for (Node<K, V> p = minRoot.child, next;; p = next) {
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
	public void meld(HeapReferenceable<? extends K, ? extends V> heap) {
		makeSureNoMeldWithSelf(heap);
		makeSureMeldWithSameImpl(HeapPairing.class, heap);
		makeSureEqualComparatorBeforeMeld(heap);
		@SuppressWarnings("unchecked")
		HeapPairing<K, V> h = (HeapPairing<K, V>) heap;

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

	private void addChild(Node<K, V> parent, Node<K, V> newChild) {
		assert newChild.prevOrParent == null;
		assert newChild.next == null;
		Node<K, V> oldChild = parent.child;
		if (oldChild != null) {
			oldChild.prevOrParent = newChild;
			newChild.next = oldChild;
		}
		parent.child = newChild;
		newChild.prevOrParent = parent;
	}

	private Node<K, V> meld(Node<K, V> n1, Node<K, V> n2) {
		return c == null ? meldDefaultCmp(n1, n2) : meldCustomCmp(n1, n2);
	}

	private Node<K, V> meldDefaultCmp(Node<K, V> n1, Node<K, V> n2) {
		assert n1.prevOrParent == null;
		assert n1.next == null;
		assert n2.prevOrParent == null;
		assert n2.next == null;

		/* assume n1 has smaller key than n2 */
		if (Utils.cmpDefault(n1.key, n2.key) > 0) {
			Node<K, V> temp = n1;
			n1 = n2;
			n2 = temp;
		}

		addChild(n1, n2);
		return n1;
	}

	private Node<K, V> meldCustomCmp(Node<K, V> n1, Node<K, V> n2) {
		assert n1.prevOrParent == null;
		assert n1.next == null;
		assert n2.prevOrParent == null;
		assert n2.next == null;

		/* assume n1 has smaller key than n2 */
		if (c.compare(n1.key, n2.key) > 0) {
			Node<K, V> temp = n1;
			n1 = n2;
			n2 = temp;
		}

		addChild(n1, n2);
		return n1;
	}

	private Node<K, V> meld(Node<K, V>[] heaps, int heapsNum) {
		if (c == null) {
			/* meld pairs from left to right */
			for (int i = 0; i < heapsNum / 2; i++) {
				Node<K, V> n1 = heaps[i * 2 + 0];
				Node<K, V> n2 = heaps[i * 2 + 1];
				heaps[i] = meldDefaultCmp(n1, n2);
			}
			/* handle last heap in case heapNum is odd */
			if (heapsNum % 2 != 0)
				heaps[heapsNum / 2] = heaps[heapsNum - 1];
			/* div by two ceil */
			heapsNum = (heapsNum + 1) / 2;

			/* meld all from right to left */
			Node<K, V> root = heaps[--heapsNum];
			for (; heapsNum > 0; heapsNum--)
				root = meldDefaultCmp(heaps[heapsNum - 1], root);
			return root;
		} else {
			/* meld pairs from left to right */
			for (int i = 0; i < heapsNum / 2; i++) {
				Node<K, V> n1 = heaps[i * 2 + 0];
				Node<K, V> n2 = heaps[i * 2 + 1];
				heaps[i] = meldCustomCmp(n1, n2);
			}
			/* handle last heap in case heapNum is odd */
			if (heapsNum % 2 != 0)
				heaps[heapsNum / 2] = heaps[heapsNum - 1];
			/* div by two ceil */
			heapsNum = (heapsNum + 1) / 2;

			/* meld all from right to left */
			Node<K, V> root = heaps[--heapsNum];
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

		for (Node<K, V> p = minRoot;;) {
			while (p.child != null) {
				p = p.child;
				while (p.next != null)
					p = p.next;
			}
			p.key = null;
			Node<K, V> prev = p.prevOrParent;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<HeapReference<K, V>> iterator() {
		return (Iterator) new PreOrderIter<>(minRoot);
	}

	private static class Node<K, V> implements HeapReference<K, V> {

		Node<K, V> prevOrParent;
		Node<K, V> next;
		Node<K, V> child;
		K key;
		V value;

		Node(K key) {
			this.key = key;
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

	private static class PreOrderIter<K, V> implements Iterator<Node<K, V>> {

		private final Stack<Node<K, V>> path = new ObjectArrayList<>();

		PreOrderIter(Node<K, V> p) {
			if (p != null)
				path.push(p);
		}

		@Override
		public boolean hasNext() {
			return !path.isEmpty();
		}

		@Override
		public Node<K, V> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			final Node<K, V> ret = path.top();

			Node<K, V> next;
			if ((next = ret.child) != null) {
				path.push(next);
			} else {
				Node<K, V> p0;
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
