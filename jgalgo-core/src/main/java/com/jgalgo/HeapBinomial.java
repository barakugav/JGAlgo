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
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * A binomial heap implementation.
 * <p>
 * Pointer based data structure that support user references to the internal nodes, allowing efficient \(O(\log n)\)
 * implementation of the {@link #remove(HeapReference)} and {@link #decreaseKey(HeapReference, Object)} operations. The
 * regular operations like {@link #insert(Object)}, {@link #extractMin()} and {@link #findMin()} are also implemented in
 * \(O(\log n)\) time. Another advantage of the binomial heap is its ability to merge with another binomial heap in
 * \(O(\log n)\) time, which is much faster than the required \(O(n)\) time of binary heaps.
 * <p>
 * Although it has great complexities bounds, {@link #decreaseKey(HeapReference, Object)} can be implemented faster
 * using {@link HeapPairing} or {@link HeapFibonacci}.
 *
 * @param  <K> the keys type
 * @param  <V> the values type
 * @see        <a href="https://en.wikipedia.org/wiki/Binomial_heap">Wikipedia</a>
 * @author     Barak Ugav
 */
class HeapBinomial<K, V> extends HeapReferenceableAbstract<K, V> {

	private Node<K, V>[] roots;
	private int rootsLen;
	private int size;

	@SuppressWarnings("rawtypes")
	private static final Node[] EmptyNodeArr = new Node[0];

	/**
	 * Constructs a new, empty binomial heap, ordered according to the natural ordering of its keys.
	 * <p>
	 * All keys inserted into the heap must implement the {@link Comparable} interface. Furthermore, all such keys must
	 * be <i>mutually comparable</i>: {@code k1.compareTo(k2)} must not throw a {@code ClassCastException} for any keys
	 * {@code k1} and {@code k2} in the heap. If the user attempts to insert a key to the heap that violates this
	 * constraint (for example, the user attempts to insert a string element to a heap whose keys are integers), the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 */
	HeapBinomial() {
		this(null);
	}

	/**
	 * Constructs a new, empty binomial heap, with keys ordered according to the specified comparator.
	 * <p>
	 * All keys inserted into the heap must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(k1, k2)} must not throw a {@code ClassCastException} for any keys {@code k1} and
	 * {@code k2} in the heap. If the user attempts to insert a key to the heap that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this heap. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the keys will be used.
	 */
	@SuppressWarnings("unchecked")
	HeapBinomial(Comparator<? super K> comparator) {
		super(comparator);
		roots = EmptyNodeArr;
		rootsLen = 0;
		size = 0;
	}

	private void swapParentChild(Node<K, V> parent, Node<K, V> child) {
		Node<K, V> t, pNext = parent.next, pPrev = parent.prev, pParent = parent.parent, pChild = parent.child;

		parent.next = (t = child.next);
		if (t != null)
			t.prev = parent;
		parent.prev = (t = child.prev);
		if (t != null)
			t.next = parent;
		parent.child = child.child;
		for (Node<K, V> p = child.child; p != null; p = p.next)
			p.parent = parent;

		child.next = pNext;
		if (pNext != null)
			pNext.prev = child;
		child.prev = pPrev;
		if (pPrev != null)
			pPrev.next = child;
		child.child = pChild == child ? parent : pChild;
		for (Node<K, V> p = child.child; p != null; p = p.next)
			p.parent = child;
		child.parent = pParent;
		if (pParent != null && pParent.child == parent)
			pParent.child = child;

		if (pParent == null) {
			/* Switched a root, fix roots array */
			Node<K, V>[] rs = roots;
			for (int i = 0; i < rootsLen; i++) {
				if (rs[i] == parent) {
					rs[i] = child;
					break;
				}
			}
		}
	}

	@Override
	public void decreaseKey(HeapReference<K, V> ref, K newKey) {
		Node<K, V> node = (Node<K, V>) ref;
		makeSureDecreaseKeyIsSmaller(node.key, newKey);
		node.key = newKey;

		if (c == null) {
			for (Node<K, V> p; (p = node.parent) != null;) {
				if (Utils.cmpDefault(p.key, newKey) <= 0)
					break;
				swapParentChild(p, node);
			}
		} else {
			for (Node<K, V> p; (p = node.parent) != null;) {
				if (c.compare(p.key, newKey) <= 0)
					break;
				swapParentChild(p, node);
			}
		}
	}

	@Override
	public void remove(HeapReference<K, V> ref) {
		Node<K, V> node = (Node<K, V>) ref;

		/* propagate to top of the tree */
		for (Node<K, V> p; (p = node.parent) != null;)
			swapParentChild(p, node);

		Node<K, V>[] rs = roots;
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

		Node<K, V>[] childs = newArr(rootIdx);
		Node<K, V> next, p = node.child;
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
		Node<K, V>[] rs = roots;
		int rslen = rootsLen;

		for (int i = 0; i < rslen; i++) {
			if (rs[i] != null) {
				Trees.clear(rs[i], n -> n.key = null);
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
	public HeapReference<K, V> insert(K key) {
		Node<K, V> node = new Node<>(key);
		Node<K, V>[] h2 = newArr(1);
		h2[0] = node;
		size += meld(h2, 1);
		return node;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<HeapReference<K, V>> iterator() {
		return (Iterator) new Itr();
	}

	private Node<K, V> mergeTreesDefaultCmp(Node<K, V> r1, Node<K, V> r2) {
		assert r1 != r2;
		if (r1 == r2)
			throw new IllegalStateException();
		if (Utils.cmpDefault(r1.key, r2.key) > 0) {
			Node<K, V> t = r1;
			r1 = r2;
			r2 = t;
		}
		r2.next = r1.child;
		Node<K, V> next = r1.child;
		if (next != null)
			next.prev = r2;
		r1.child = r2;
		r2.parent = r1;

		return r1;
	}

	private Node<K, V> mergeTreesCustomCmp(Node<K, V> r1, Node<K, V> r2) {
		assert r1 != r2;
		if (r1 == r2)
			throw new IllegalStateException();
		if (c.compare(r1.key, r2.key) > 0) {
			Node<K, V> t = r1;
			r1 = r2;
			r2 = t;
		}
		r2.next = r1.child;
		Node<K, V> next = r1.child;
		if (next != null)
			next.prev = r2;
		r1.child = r2;
		r2.parent = r1;

		return r1;
	}

	private int meld(Node<K, V>[] rs2, int rs2len) {
		Node<K, V>[] rs1 = roots;
		Node<K, V>[] rs = rs1.length >= rs2.length ? rs1 : rs2;
		int rs1len = rootsLen;
		int rslen = rs1len > rs2len ? rs1len : rs2len;
		int h2size = 0;

		Node<K, V> carry = null;
		if (c == null) {
			for (int i = 0; i < rslen; i++) {
				Node<K, V> r1 = i < rs1len ? rs1[i] : null;
				Node<K, V> r2 = i < rs2len ? rs2[i] : null;

				if (r2 != null)
					h2size += 1 << i;

				if ((r1 == null && r2 == null) || (r1 != null && r2 != null)) {
					rs[i] = carry;
					carry = (r1 != null && r2 != null) ? mergeTreesDefaultCmp(r1, r2) : null;
				} else {
					Node<K, V> r = r1 != null ? r1 : r2;
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
				Node<K, V> r1 = i < rs1len ? rs1[i] : null;
				Node<K, V> r2 = i < rs2len ? rs2[i] : null;

				if (r2 != null)
					h2size += 1 << i;

				if ((r1 == null && r2 == null) || (r1 != null && r2 != null)) {
					rs[i] = carry;
					carry = (r1 != null && r2 != null) ? mergeTreesCustomCmp(r1, r2) : null;
				} else {
					Node<K, V> r = r1 != null ? r1 : r2;
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
			if (rslen + 1 >= rs.length)
				rs = Arrays.copyOf(rs, rs.length * 2);
			rs[rslen++] = carry;
		}

		roots = rs;
		rootsLen = rslen;
		return h2size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void meld(HeapReferenceable<? extends K, ? extends V> heap) {
		makeSureNoMeldWithSelf(heap);
		makeSureMeldWithSameImpl(HeapBinomial.class, heap);
		makeSureEqualComparatorBeforeMeld(heap);

		HeapBinomial<K, V> h = (HeapBinomial<K, V>) heap;
		size += meld(h.roots, h.rootsLen);

		h.roots = EmptyNodeArr;
		h.rootsLen = 0;
		h.size = 0;
	}

	@Override
	public HeapReference<K, V> findMin() {
		if (isEmpty())
			throw new IllegalStateException();
		Node<K, V>[] rs = roots;
		int rsLen = rootsLen;
		Node<K, V> min = null;

		if (c == null) {
			for (int i = 0; i < rsLen; i++)
				if (rs[i] != null && (min == null || Utils.cmpDefault(min.key, rs[i].key) > 0))
					min = rs[i];
		} else {
			for (int i = 0; i < rsLen; i++)
				if (rs[i] != null && (min == null || c.compare(min.key, rs[i].key) > 0))
					min = rs[i];
		}
		return min;
	}

	@SuppressWarnings("unchecked")
	private static <K, V> Node<K, V>[] newArr(int n) {
		return new Node[n];
	}

	private static class Node<K, V> extends Trees.TreeNodeImpl<Node<K, V>> implements HeapReference<K, V> {

		K key;
		V value;

		Node(K key) {
			parent = null;
			next = null;
			prev = null;
			child = null;
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

	private class Itr extends Trees.PreOrderIter<Node<K, V>> {

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
