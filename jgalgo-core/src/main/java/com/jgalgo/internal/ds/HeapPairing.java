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

package com.jgalgo.internal.ds;

import java.util.Comparator;
import java.util.Iterator;
import com.jgalgo.internal.ds.Heaps.AbstractHeapReferenceable;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * A Pairing heap implementation.
 *
 * <p>
 * A pointer based heap implementation that support almost any operation in \(O(1)\) amortized time, except
 * {@link #remove(HeapReference)} and {@link #decreaseKey(HeapReference, Object)} which takes \(O(\log n)\) time
 * amortized.
 *
 * <p>
 * Using this heap, {@link ShortestPathSingleSourceDijkstra} can be implemented in time \(O(m + n \log n)\) rather than
 * \(O(m \log n)\) as the {@link #decreaseKey(HeapReference, Object)} operation is performed in \(O(1)\) time amortized.
 *
 * <p>
 * Pairing heaps are one of the best pointer based heaps implementations in practice, and should be used as a default
 * choice for the general use case.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Pairing_heap">Wikipedia</a>
 * @author Barak Ugav
 */
abstract class HeapPairing<K, V, NodeT extends HeapPairing.NodeBase<K, V, NodeT>>
		extends AbstractHeapReferenceable<K, V> {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <K, V> HeapReferenceable<K, V> newHeap(Class<? extends K> keysType, Class<? extends V> valuesType,
			Comparator<? super K> comparator) {
		if (keysType == int.class) {
			return IntBase.newHeap(valuesType, (Comparator) comparator);
		} else if (keysType == double.class) {
			return DoubleBase.newHeap(valuesType, (Comparator) comparator);
		} else {
			return ObjBase.newHeap(valuesType, comparator);
		}
	}

	NodeT minRoot;

	HeapPairing(Comparator<? super K> c) {
		super(c);
	}

	@Override
	public HeapReference<K, V> findMin() {
		Assertions.Heaps.notEmpty(this);
		return minRoot;
	}

	@Override
	public boolean isEmpty() {
		return minRoot == null;
	}

	@Override
	public boolean isNotEmpty() {
		return minRoot != null;
	}

	private static <K, V, NodeT extends NodeBase<K, V, NodeT>> void cut(NodeT n) {
		NodeT next = n.next;
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

	static <K, V, NodeT extends NodeBase<K, V, NodeT>> void addChild(NodeT parent, NodeT newChild) {
		assert newChild.prevOrParent == null;
		assert newChild.next == null;
		NodeT oldChild = parent.child;
		if (oldChild != null) {
			oldChild.prevOrParent = newChild;
			newChild.next = oldChild;
		}
		parent.child = newChild;
		newChild.prevOrParent = parent;
	}

	void insertNode(NodeT n) {
		if (minRoot == null) {
			minRoot = n;
		} else {
			minRoot = meld(minRoot, n);
		}
	}

	void afterKeyDecrease(NodeT n) {
		if (n == minRoot)
			return;
		cut(n);
		minRoot = meld(minRoot, n);
	}

	@Override
	public void remove(HeapReference<K, V> ref) {
		@SuppressWarnings("unchecked")
		NodeT n = (NodeT) ref;
		assert minRoot != null;
		if (n != minRoot) {
			cut(n);
			addChild(n, minRoot);
			minRoot = n;
		}
		removeRoot();
	}

	abstract void removeRoot();

	@Override
	public void meld(HeapReferenceable<? extends K, ? extends V> heap) {
		Assertions.Heaps.noMeldWithSelf(this, heap);
		Assertions.Heaps.meldWithSameImpl(getClass(), heap);
		Assertions.Heaps.equalComparatorBeforeMeld(this, heap);
		@SuppressWarnings("unchecked")
		HeapPairing<K, V, NodeT> h = (HeapPairing<K, V, NodeT>) heap;

		if (minRoot == null) {
			minRoot = h.minRoot;
		} else if (h.minRoot != null) {
			minRoot = meld(minRoot, h.minRoot);
		}

		h.minRoot = null;
	}

	private NodeT meld(NodeT n1, NodeT n2) {
		return c == null ? meldDefaultCmp(n1, n2) : meldCustomCmp(n1, n2);
	}

	abstract NodeT meldDefaultCmp(NodeT n1, NodeT n2);

	abstract NodeT meldCustomCmp(NodeT n1, NodeT n2);

	@Override
	public void clear() {
		if (minRoot == null)
			return;

		for (NodeT p = minRoot;;) {
			while (p.child != null) {
				p = p.child;
				while (p.next != null)
					p = p.next;
			}
			p.clearUserData();
			NodeT prev = p.prevOrParent;
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
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<HeapReference<K, V>> iterator() {
		return (Iterator) new PreOrderIter<>(minRoot);
	}

	abstract int compareNodesKeys(NodeT n1, NodeT n2);

	abstract static class NodeBase<K, V, NodeT extends NodeBase<K, V, NodeT>> implements HeapReference<K, V> {

		NodeT prevOrParent;
		NodeT next;
		NodeT child;

		@Override
		public String toString() {
			return "{" + key() + ":" + value() + "}";
		}

		abstract void clearUserData();
	}

	private static interface NodeVoidValBase<K> extends HeapReference<K, Void> {
		@Override
		default Void value() {
			return null;
		}

		@Override
		default void setValue(Void val) {
			assert val == null;
		}
	}

	static class PreOrderIter<K, V, NodeT extends NodeBase<K, V, NodeT>> implements Iterator<NodeT> {

		private final Stack<NodeT> path = new ObjectArrayList<>();

		PreOrderIter(NodeT p) {
			if (p != null)
				path.push(p);
		}

		@Override
		public boolean hasNext() {
			return !path.isEmpty();
		}

		@Override
		public NodeT next() {
			Assertions.Iters.hasNext(this);
			final NodeT ret = path.top();

			NodeT next;
			if ((next = ret.child) != null) {
				path.push(next);
			} else {
				NodeT p0;
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

	private abstract static class ObjBase<K, V> extends HeapPairing<K, V, ObjBase.Node<K, V>> {

		ObjBase(Comparator<? super K> comparator) {
			super(comparator);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		static <K, V> HeapReferenceable<K, V> newHeap(Class<? extends V> valuesType, Comparator<? super K> comparator) {
			if (valuesType == int.class) {
				return new HeapPairing.ObjInt(comparator);
			} else if (valuesType == void.class) {
				return new HeapPairing.ObjVoid(comparator);
			} else {
				return new HeapPairing.ObjObj(comparator);
			}
		}

		@Override
		public HeapReference<K, V> insert(K key) {
			Node<K, V> n = newNode(key);
			insertNode(n);
			return n;
		}

		abstract Node<K, V> newNode(K key);

		@Override
		void removeRoot() {
			if (minRoot.child == null) {
				minRoot = null;
				return;
			}

			if (c == null) {
				/* meld pairs from left to right */
				Node<K, V> tail;
				for (Node<K, V> prev = minRoot, next = minRoot.child;;) {
					Node<K, V> n1 = next;
					if (n1 == null) {
						tail = prev;
						break;
					}

					Node<K, V> n2 = n1.next;
					if (n2 == null) {
						n1.prevOrParent = prev;
						tail = n1;
						break;
					}
					next = n2.next;
					n1.next = null;
					n2.next = null;
					n1.prevOrParent = null;
					n2.prevOrParent = null;
					n1 = meldDefaultCmp(n1, n2);

					n1.prevOrParent = prev;
					prev = n1;
				}
				minRoot.child = null;

				/* meld all from right to left */
				Node<K, V> root = tail, prev = root.prevOrParent;
				root.prevOrParent = null;
				for (;;) {
					Node<K, V> other = prev;
					if (other == minRoot) {
						minRoot = root;
						break;
					}
					prev = other.prevOrParent;
					other.prevOrParent = null;
					root = meldDefaultCmp(root, other);
				}
			} else {

				/* meld pairs from left to right */
				Node<K, V> tail;
				for (Node<K, V> prev = minRoot, next = minRoot.child;;) {
					Node<K, V> n1 = next;
					if (n1 == null) {
						tail = prev;
						break;
					}

					Node<K, V> n2 = n1.next;
					if (n2 == null) {
						n1.prevOrParent = prev;
						tail = n1;
						break;
					}
					next = n2.next;
					n1.next = null;
					n2.next = null;
					n1.prevOrParent = null;
					n2.prevOrParent = null;
					n1 = meldCustomCmp(n1, n2);

					n1.prevOrParent = prev;
					prev = n1;
				}
				minRoot.child = null;

				/* meld all from right to left */
				Node<K, V> root = tail, prev = root.prevOrParent;
				root.prevOrParent = null;
				for (;;) {
					Node<K, V> other = prev;
					if (other == minRoot) {
						minRoot = root;
						break;
					}
					prev = other.prevOrParent;
					other.prevOrParent = null;
					root = meldCustomCmp(root, other);
				}
			}
		}

		@Override
		public void decreaseKey(HeapReference<K, V> ref, K newKey) {
			Node<K, V> n = (Node<K, V>) ref;
			Assertions.Heaps.decreaseKeyIsSmaller(n.key, newKey, c);
			n.key = newKey;
			afterKeyDecrease(n);
		}

		@Override
		Node<K, V> meldDefaultCmp(Node<K, V> n1, Node<K, V> n2) {
			assert n1.prevOrParent == null;
			assert n1.next == null;
			assert n2.prevOrParent == null;
			assert n2.next == null;

			/* assume n1 has smaller key than n2 */
			if (JGAlgoUtils.cmpDefault(n1.key, n2.key) > 0) {
				Node<K, V> temp = n1;
				n1 = n2;
				n2 = temp;
			}

			addChild(n1, n2);
			return n1;
		}

		@Override
		Node<K, V> meldCustomCmp(Node<K, V> n1, Node<K, V> n2) {
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

		@Override
		int compareNodesKeys(Node<K, V> n1, Node<K, V> n2) {
			return compare(n1.key, n2.key);
		}

		abstract static class Node<K, V> extends NodeBase<K, V, Node<K, V>> {
			K key;

			Node(K key) {
				this.key = key;
			}

			@Override
			public K key() {
				return key;
			}

			@Override
			void clearUserData() {
				key = null;
			}
		}
	}

	private static class ObjObj<K, V> extends ObjBase<K, V> {

		ObjObj(Comparator<? super K> comparator) {
			super(comparator);
		}

		@Override
		ObjBase.Node<K, V> newNode(K key) {
			return new Node<>(key);
		}

		private static class Node<K, V> extends ObjBase.Node<K, V> {
			private V value;

			Node(K key) {
				super(key);
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
			void clearUserData() {
				super.clearUserData();
				value = null;
			}
		}
	}

	private static class ObjInt<K> extends ObjBase<K, Integer> {

		ObjInt(Comparator<? super K> comparator) {
			super(comparator);
		}

		@Override
		ObjBase.Node<K, Integer> newNode(K key) {
			return new Node<>(key);
		}

		private static class Node<K> extends ObjBase.Node<K, Integer> {
			private int value;

			Node(K key) {
				super(key);
			}

			@Override
			public Integer value() {
				return Integer.valueOf(value);
			}

			@Override
			public void setValue(Integer val) {
				value = val.intValue();
			}
		}
	}

	private static class ObjVoid<K> extends ObjBase<K, Void> {

		ObjVoid(Comparator<? super K> comparator) {
			super(comparator);
		}

		@Override
		ObjBase.Node<K, Void> newNode(K key) {
			return new Node<>(key);
		}

		private static class Node<K> extends ObjBase.Node<K, Void> implements NodeVoidValBase<K> {
			Node(K key) {
				super(key);
			}

			@Override
			public String toString() {
				return "{" + key() + "}";
			}
		}
	}

	private abstract static class DoubleBase<V> extends HeapPairing<Double, V, DoubleBase.Node<V>> {

		private final DoubleComparator doubleCmp;

		DoubleBase(Comparator<? super Double> comparator) {
			super(comparator);
			doubleCmp = comparator == null || comparator instanceof DoubleComparator ? (DoubleComparator) comparator
					: (k1, k2) -> comparator.compare(Double.valueOf(k1), Double.valueOf(k2));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		static <V> HeapReferenceable<Double, V> newHeap(Class<? extends V> valuesType,
				Comparator<? super Double> comparator) {
			if (valuesType == int.class) {
				return (HeapReferenceable) new HeapPairing.DoubleInt(comparator);
			} else if (valuesType == void.class) {
				return (HeapReferenceable) new HeapPairing.DoubleVoid(comparator);
			} else {
				return new HeapPairing.DoubleObj(comparator);
			}
		}

		@Override
		public HeapReference<Double, V> insert(Double key) {
			Node<V> n = newNode(key.doubleValue());
			insertNode(n);
			return n;
		}

		abstract Node<V> newNode(double key);

		@Override
		void removeRoot() {
			if (minRoot.child == null) {
				minRoot = null;
				return;
			}

			if (c == null) {
				/* meld pairs from left to right */
				Node<V> tail;
				for (Node<V> prev = minRoot, next = minRoot.child;;) {
					Node<V> n1 = next;
					if (n1 == null) {
						tail = prev;
						break;
					}

					Node<V> n2 = n1.next;
					if (n2 == null) {
						n1.prevOrParent = prev;
						tail = n1;
						break;
					}
					next = n2.next;
					n1.next = null;
					n2.next = null;
					n1.prevOrParent = null;
					n2.prevOrParent = null;
					n1 = meldDefaultCmp(n1, n2);

					n1.prevOrParent = prev;
					prev = n1;
				}
				minRoot.child = null;

				/* meld all from right to left */
				Node<V> root = tail, prev = root.prevOrParent;
				root.prevOrParent = null;
				for (;;) {
					Node<V> other = prev;
					if (other == minRoot) {
						minRoot = root;
						break;
					}
					prev = other.prevOrParent;
					other.prevOrParent = null;
					root = meldDefaultCmp(root, other);
				}
			} else {

				/* meld pairs from left to right */
				Node<V> tail;
				for (Node<V> prev = minRoot, next = minRoot.child;;) {
					Node<V> n1 = next;
					if (n1 == null) {
						tail = prev;
						break;
					}

					Node<V> n2 = n1.next;
					if (n2 == null) {
						n1.prevOrParent = prev;
						tail = n1;
						break;
					}
					next = n2.next;
					n1.next = null;
					n2.next = null;
					n1.prevOrParent = null;
					n2.prevOrParent = null;
					n1 = meldCustomCmp(n1, n2);

					n1.prevOrParent = prev;
					prev = n1;
				}
				minRoot.child = null;

				/* meld all from right to left */
				Node<V> root = tail, prev = root.prevOrParent;
				root.prevOrParent = null;
				for (;;) {
					Node<V> other = prev;
					if (other == minRoot) {
						minRoot = root;
						break;
					}
					prev = other.prevOrParent;
					other.prevOrParent = null;
					root = meldCustomCmp(root, other);
				}
			}
		}

		@Override
		public void decreaseKey(HeapReference<Double, V> ref, Double newKey) {
			double newKeyDouble = newKey.doubleValue();
			Node<V> n = (Node<V>) ref;
			Assertions.Heaps.decreaseKeyIsSmaller(n.key, newKeyDouble, doubleCmp);
			n.key = newKeyDouble;
			afterKeyDecrease(n);
		}

		@Override
		Node<V> meldDefaultCmp(Node<V> n1, Node<V> n2) {
			assert n1.prevOrParent == null;
			assert n1.next == null;
			assert n2.prevOrParent == null;
			assert n2.next == null;

			/* assume n1 has smaller key than n2 */
			if (Double.compare(n1.key, n2.key) > 0) {
				Node<V> temp = n1;
				n1 = n2;
				n2 = temp;
			}

			addChild(n1, n2);
			return n1;
		}

		@Override
		Node<V> meldCustomCmp(Node<V> n1, Node<V> n2) {
			assert n1.prevOrParent == null;
			assert n1.next == null;
			assert n2.prevOrParent == null;
			assert n2.next == null;

			/* assume n1 has smaller key than n2 */
			if (doubleCmp.compare(n1.key, n2.key) > 0) {
				Node<V> temp = n1;
				n1 = n2;
				n2 = temp;
			}

			addChild(n1, n2);
			return n1;
		}

		@Override
		int compareNodesKeys(Node<V> n1, Node<V> n2) {
			return doubleCmp.compare(n1.key, n2.key);
		}

		abstract static class Node<V> extends NodeBase<Double, V, Node<V>> {
			double key;

			Node(double key) {
				this.key = key;
			}

			@Override
			public Double key() {
				return Double.valueOf(key);
			}

			@Override
			void clearUserData() {}
		}
	}

	private static class DoubleObj<V> extends DoubleBase<V> {

		DoubleObj(Comparator<? super Double> comparator) {
			super(comparator);
		}

		@Override
		DoubleBase.Node<V> newNode(double key) {
			return new Node<>(key);
		}

		private static class Node<V> extends DoubleBase.Node<V> {
			private V value;

			Node(double key) {
				super(key);
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
			void clearUserData() {
				super.clearUserData();
				value = null;
			}
		}
	}

	private static class DoubleInt extends DoubleBase<Integer> {

		DoubleInt(Comparator<? super Double> comparator) {
			super(comparator);
		}

		@Override
		DoubleBase.Node<Integer> newNode(double key) {
			return new Node(key);
		}

		private static class Node extends DoubleBase.Node<Integer> {
			private int value;

			Node(double key) {
				super(key);
			}

			@Override
			public Integer value() {
				return Integer.valueOf(value);
			}

			@Override
			public void setValue(Integer val) {
				value = val.intValue();
			}
		}
	}

	private static class DoubleVoid extends DoubleBase<Void> {

		DoubleVoid(Comparator<? super Double> comparator) {
			super(comparator);
		}

		@Override
		DoubleBase.Node<Void> newNode(double key) {
			return new Node(key);
		}

		private static class Node extends DoubleBase.Node<Void> implements NodeVoidValBase<Double> {
			Node(double key) {
				super(key);
			}

			@Override
			public String toString() {
				return "{" + key() + "}";
			}
		}
	}

	private abstract static class IntBase<V> extends HeapPairing<Integer, V, IntBase.Node<V>> {

		private final IntComparator intCmp;

		IntBase(Comparator<? super Integer> comparator) {
			super(comparator);
			intCmp = comparator == null || comparator instanceof IntComparator ? (IntComparator) comparator
					: (k1, k2) -> comparator.compare(Integer.valueOf(k1), Integer.valueOf(k2));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		static <V> HeapReferenceable<Integer, V> newHeap(Class<? extends V> valuesType,
				Comparator<? super Integer> comparator) {
			if (valuesType == int.class) {
				return (HeapReferenceable) new HeapPairing.IntInt(comparator);
			} else if (valuesType == void.class) {
				return (HeapReferenceable) new HeapPairing.IntVoid(comparator);
			} else {
				return new HeapPairing.IntObj(comparator);
			}
		}

		@Override
		public HeapReference<Integer, V> insert(Integer key) {
			Node<V> n = newNode(key.intValue());
			insertNode(n);
			return n;
		}

		abstract Node<V> newNode(int key);

		@Override
		void removeRoot() {
			if (minRoot.child == null) {
				minRoot = null;
				return;
			}

			if (c == null) {
				/* meld pairs from left to right */
				Node<V> tail;
				for (Node<V> prev = minRoot, next = minRoot.child;;) {
					Node<V> n1 = next;
					if (n1 == null) {
						tail = prev;
						break;
					}

					Node<V> n2 = n1.next;
					if (n2 == null) {
						n1.prevOrParent = prev;
						tail = n1;
						break;
					}
					next = n2.next;
					n1.next = null;
					n2.next = null;
					n1.prevOrParent = null;
					n2.prevOrParent = null;
					n1 = meldDefaultCmp(n1, n2);

					n1.prevOrParent = prev;
					prev = n1;
				}
				minRoot.child = null;

				/* meld all from right to left */
				Node<V> root = tail, prev = root.prevOrParent;
				root.prevOrParent = null;
				for (;;) {
					Node<V> other = prev;
					if (other == minRoot) {
						minRoot = root;
						break;
					}
					prev = other.prevOrParent;
					other.prevOrParent = null;
					root = meldDefaultCmp(root, other);
				}
			} else {

				/* meld pairs from left to right */
				Node<V> tail;
				for (Node<V> prev = minRoot, next = minRoot.child;;) {
					Node<V> n1 = next;
					if (n1 == null) {
						tail = prev;
						break;
					}

					Node<V> n2 = n1.next;
					if (n2 == null) {
						n1.prevOrParent = prev;
						tail = n1;
						break;
					}
					next = n2.next;
					n1.next = null;
					n2.next = null;
					n1.prevOrParent = null;
					n2.prevOrParent = null;
					n1 = meldCustomCmp(n1, n2);

					n1.prevOrParent = prev;
					prev = n1;
				}
				minRoot.child = null;

				/* meld all from right to left */
				Node<V> root = tail, prev = root.prevOrParent;
				root.prevOrParent = null;
				for (;;) {
					Node<V> other = prev;
					if (other == minRoot) {
						minRoot = root;
						break;
					}
					prev = other.prevOrParent;
					other.prevOrParent = null;
					root = meldCustomCmp(root, other);
				}
			}
		}

		@Override
		public void decreaseKey(HeapReference<Integer, V> ref, Integer newKey) {
			int newKeyInteger = newKey.intValue();
			Node<V> n = (Node<V>) ref;
			Assertions.Heaps.decreaseKeyIsSmaller(n.key, newKeyInteger, intCmp);
			n.key = newKeyInteger;
			afterKeyDecrease(n);
		}

		@Override
		Node<V> meldDefaultCmp(Node<V> n1, Node<V> n2) {
			assert n1.prevOrParent == null;
			assert n1.next == null;
			assert n2.prevOrParent == null;
			assert n2.next == null;

			/* assume n1 has smaller key than n2 */
			if (Integer.compare(n1.key, n2.key) > 0) {
				Node<V> temp = n1;
				n1 = n2;
				n2 = temp;
			}

			addChild(n1, n2);
			return n1;
		}

		@Override
		Node<V> meldCustomCmp(Node<V> n1, Node<V> n2) {
			assert n1.prevOrParent == null;
			assert n1.next == null;
			assert n2.prevOrParent == null;
			assert n2.next == null;

			/* assume n1 has smaller key than n2 */
			if (intCmp.compare(n1.key, n2.key) > 0) {
				Node<V> temp = n1;
				n1 = n2;
				n2 = temp;
			}

			addChild(n1, n2);
			return n1;
		}

		@Override
		int compareNodesKeys(Node<V> n1, Node<V> n2) {
			return intCmp.compare(n1.key, n2.key);
		}

		abstract static class Node<V> extends NodeBase<Integer, V, Node<V>> {
			int key;

			Node(int key) {
				this.key = key;
			}

			@Override
			public Integer key() {
				return Integer.valueOf(key);
			}

			@Override
			void clearUserData() {}
		}
	}

	private static class IntObj<V> extends IntBase<V> {

		IntObj(Comparator<? super Integer> comparator) {
			super(comparator);
		}

		@Override
		IntBase.Node<V> newNode(int key) {
			return new Node<>(key);
		}

		private static class Node<V> extends IntBase.Node<V> {
			private V value;

			Node(int key) {
				super(key);
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
			void clearUserData() {
				super.clearUserData();
				value = null;
			}
		}
	}

	private static class IntInt extends IntBase<Integer> {

		IntInt(Comparator<? super Integer> comparator) {
			super(comparator);
		}

		@Override
		IntBase.Node<Integer> newNode(int key) {
			return new Node(key);
		}

		private static class Node extends IntBase.Node<Integer> {
			private int value;

			Node(int key) {
				super(key);
			}

			@Override
			public Integer value() {
				return Integer.valueOf(value);
			}

			@Override
			public void setValue(Integer val) {
				value = val.intValue();
			}
		}
	}

	private static class IntVoid extends IntBase<Void> {

		IntVoid(Comparator<? super Integer> comparator) {
			super(comparator);
		}

		@Override
		IntBase.Node<Void> newNode(int key) {
			return new Node(key);
		}

		private static class Node extends IntBase.Node<Void> implements NodeVoidValBase<Integer> {
			Node(int key) {
				super(key);
			}

			@Override
			public String toString() {
				return "{" + key() + "}";
			}
		}
	}

	static <K, V, NodeT extends HeapPairing.NodeBase<K, V, NodeT>> void assertHeapConstraints(
			HeapPairing<K, V, NodeT> heap) {
		if (heap.isEmpty())
			return;

		Stack<NodeT> path = new ObjectArrayList<>();
		path.push(heap.minRoot);
		for (;;) {
			for (NodeT node = path.top(); node.child != null;)
				path.push(node = node.child);
			for (;;) {
				NodeT node = path.pop();
				if (path.isEmpty()) {
					if (node.next != null)
						throw new IllegalArgumentException();
					return;
				}
				NodeT parent = path.top();
				if (heap.compareNodesKeys(node, parent) < 0)
					throw new IllegalArgumentException();
				if (node.next != null) {
					path.push(node.next);
					break;
				}
			}
		}
	}

}
