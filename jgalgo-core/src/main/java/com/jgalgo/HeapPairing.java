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

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * A Pairing heap implementation.
 * <p>
 * A pointer based heap implementation that support almost any operation in \(O(1)\) amortized time, except
 * {@link #remove(HeapReference)} and {@link #decreaseKey(HeapReference, Object)} which takes \(O(\log n)\) time
 * amortized.
 * <p>
 * Using this heap, {@link ShortestPathSingleSourceDijkstra} can be implemented in time \(O(m + n \log n)\) rather than
 * \(O(m \log n)\) as the {@link #decreaseKey(HeapReference, Object)} operation is performed in \(O(1)\) time amortized.
 * <p>
 * Pairing heaps are one of the best pointer based heaps implementations in practice, and should be used as a default
 * choice for the general use case.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Pairing_heap">Wikipedia</a>
 * @author Barak Ugav
 */
class HeapPairing {

	private HeapPairing() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <K, V> HeapReferenceable<K, V> newHeap(Class<? extends K> keysType, Class<? extends V> valuesType,
			Comparator<? super K> comparator) {
		if (keysType == int.class) {
			return new HeapPairing.WithIntKeys(valuesType, comparator);
		} else if (keysType == double.class) {
			return new HeapPairing.WithDoubleKeys(valuesType, comparator);
		} else {
			return new HeapPairing.WithObjKeys(valuesType, comparator);
		}
	}

	private static abstract class Abstract<K, V, Node extends Abstract.INode<K, V, Node>>
			extends HeapReferenceableAbstract<K, V> {

		Node minRoot;
		int size;
		private final Class<? extends V> valueType;

		Abstract(Class<? extends V> valueType, Comparator<? super K> c) {
			super(c);
			this.valueType = valueType;
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

		private static <K, V, Node extends HeapPairing.Abstract.INode<K, V, Node>> void cut(Node n) {
			Node next = n.next;
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

		static <K, V, Node extends HeapPairing.Abstract.INode<K, V, Node>> void addChild(Node parent, Node newChild) {
			assert newChild.prevOrParent == null;
			assert newChild.next == null;
			Node oldChild = parent.child;
			if (oldChild != null) {
				oldChild.prevOrParent = newChild;
				newChild.next = oldChild;
			}
			parent.child = newChild;
			newChild.prevOrParent = parent;
		}

		void insertNode(Node n) {
			if (minRoot == null) {
				minRoot = n;
				assert size == 0;
			} else {
				minRoot = meld(minRoot, n);
			}
			size++;
		}

		void afterKeyDecrease(Node n) {
			if (n == minRoot)
				return;
			cut(n);
			minRoot = meld(minRoot, n);
		}

		@Override
		public void remove(HeapReference<K, V> ref) {
			@SuppressWarnings("unchecked")
			Node n = (Node) ref;
			assert minRoot != null;
			if (n != minRoot) {
				cut(n);
				addChild(n, minRoot);
				minRoot = n;
			}
			removeRoot();
			size--;
		}

		abstract void removeRoot();

		@Override
		public void meld(HeapReferenceable<? extends K, ? extends V> heap) {
			makeSureNoMeldWithSelf(heap);
			makeSureMeldWithSameImpl(getClass(), heap);
			makeSureEqualComparatorBeforeMeld(heap);
			@SuppressWarnings("unchecked")
			HeapPairing.Abstract<K, V, Node> h = (HeapPairing.Abstract<K, V, Node>) heap;
			if (valueType != h.valueType)
				throw new IllegalArgumentException("Can't meld heaps with different implementations");

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

		private Node meld(Node n1, Node n2) {
			return c == null ? meldDefaultCmp(n1, n2) : meldCustomCmp(n1, n2);
		}

		abstract Node meldDefaultCmp(Node n1, Node n2);

		abstract Node meldCustomCmp(Node n1, Node n2);

		@Override
		public void clear() {
			if (minRoot == null) {
				assert size == 0;
				return;
			}

			for (Node p = minRoot;;) {
				while (p.child != null) {
					p = p.child;
					while (p.next != null)
						p = p.next;
				}
				p.clearUserData();
				Node prev = p.prevOrParent;
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
			return (Iterator) new HeapPairing.Abstract.PreOrderIter<>(minRoot);
		}

		static abstract class INode<K, V, Node extends INode<K, V, Node>> implements HeapReference<K, V> {

			Node prevOrParent;
			Node next;
			Node child;

			@Override
			public String toString() {
				return "{" + key() + ":" + value() + "}";
			}

			abstract void clearUserData();
		}

		private static interface NodeVoidVal<K> extends HeapReference<K, Void> {
			@Override
			default Void value() {
				return null;
			}

			@Override
			default void setValue(Void val) {
				assert val == null;
			}
		}

		static class PreOrderIter<K, V, Node extends INode<K, V, Node>> implements Iterator<Node> {

			private final Stack<Node> path = new ObjectArrayList<>();

			PreOrderIter(Node p) {
				if (p != null)
					path.push(p);
			}

			@Override
			public boolean hasNext() {
				return !path.isEmpty();
			}

			@Override
			public Node next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final Node ret = path.top();

				Node next;
				if ((next = ret.child) != null) {
					path.push(next);
				} else {
					Node p0;
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

	private static class WithObjKeys<K, V> extends HeapPairing.Abstract<K, V, HeapPairing.WithObjKeys.Node<K, V>> {

		private final Function<K, Node<K, V>> nodesFactory;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		WithObjKeys(Class<? extends V> valueType, Comparator<? super K> comparator) {
			super(valueType, comparator);
			if (valueType == int.class) {
				nodesFactory = (Function) NodeObjInt::new;
			} else if (valueType == void.class) {
				nodesFactory = (Function) NodeObjVoid::new;
			} else {
				nodesFactory = (Function) NodeObjObj::new;
			}
		}

		@Override
		public HeapReference<K, V> insert(K key) {
			Node<K, V> n = nodesFactory.apply(key);
			insertNode(n);
			return n;
		}

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
			makeSureDecreaseKeyIsSmaller(n.key, newKey);
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
			if (Utils.cmpDefault(n1.key, n2.key) > 0) {
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

		static abstract class Node<K, V> extends HeapPairing.Abstract.INode<K, V, Node<K, V>> {
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

		private static class NodeObjObj<K, V> extends Node<K, V> {
			private V value;

			NodeObjObj(K key) {
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

		private static class NodeObjInt<K> extends Node<K, Integer> {
			private int value;

			NodeObjInt(K key) {
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

		private static class NodeObjVoid<K> extends Node<K, Void> implements HeapPairing.Abstract.NodeVoidVal<K> {
			NodeObjVoid(K key) {
				super(key);
			}
		}

	}

	private static class WithDoubleKeys<V> extends HeapPairing.Abstract<Double, V, HeapPairing.WithDoubleKeys.Node<V>> {

		private final DoubleFunction<Node<V>> nodesFactory;
		private final DoubleComparator doubleCmp;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		WithDoubleKeys(Class<? extends V> valueType, Comparator<? super Double> comparator) {
			super(valueType, comparator);
			doubleCmp = comparator == null || comparator instanceof DoubleComparator ? (DoubleComparator) comparator
					: (k1, k2) -> comparator.compare(Double.valueOf(k1), Double.valueOf(k2));
			if (valueType == int.class) {
				nodesFactory = (DoubleFunction) NodeDoubleInt::new;
			} else if (valueType == void.class) {
				nodesFactory = (DoubleFunction) NodeDoubleVoid::new;
			} else {
				nodesFactory = NodeDoubleObj::new;
			}
		}

		int compare(double k1, double k2) {
			return c == null ? Double.compare(k1, k2) : doubleCmp.compare(k1, k2);
		}

		@Override
		public HeapReference<Double, V> insert(Double key) {
			Node<V> n = nodesFactory.apply(key.doubleValue());
			insertNode(n);
			return n;
		}

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
			if (compare(n.key, newKeyDouble) < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
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

		static abstract class Node<V> extends HeapPairing.Abstract.INode<Double, V, Node<V>> {
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

		private static class NodeDoubleObj<V> extends Node<V> {
			private V value;

			NodeDoubleObj(double key) {
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

		private static class NodeDoubleInt extends Node<Integer> {
			private int value;

			NodeDoubleInt(double key) {
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

		private static class NodeDoubleVoid extends Node<Void> implements HeapPairing.Abstract.NodeVoidVal<Double> {
			NodeDoubleVoid(double key) {
				super(key);
			}
		}

	}

	private static class WithIntKeys<V> extends HeapPairing.Abstract<Integer, V, HeapPairing.WithIntKeys.Node<V>> {

		private final IntFunction<Node<V>> nodesFactory;
		private final IntComparator intCmp;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		WithIntKeys(Class<? extends V> valueType, Comparator<? super Integer> comparator) {
			super(valueType, comparator);
			intCmp = comparator == null || comparator instanceof IntComparator ? (IntComparator) comparator
					: (k1, k2) -> comparator.compare(Integer.valueOf(k1), Integer.valueOf(k2));
			if (valueType == int.class) {
				nodesFactory = (IntFunction) NodeIntegerInt::new;
			} else if (valueType == void.class) {
				nodesFactory = (IntFunction) NodeIntegerVoid::new;
			} else {
				nodesFactory = NodeIntegerObj::new;
			}
		}

		int compare(int k1, int k2) {
			return c == null ? Integer.compare(k1, k2) : intCmp.compare(k1, k2);
		}

		@Override
		public HeapReference<Integer, V> insert(Integer key) {
			Node<V> n = nodesFactory.apply(key.intValue());
			insertNode(n);
			return n;
		}

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
			if (compare(n.key, newKeyInteger) < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
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

		static abstract class Node<V> extends HeapPairing.Abstract.INode<Integer, V, Node<V>> {
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

		private static class NodeIntegerObj<V> extends Node<V> {
			private V value;

			NodeIntegerObj(int key) {
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

		private static class NodeIntegerInt extends Node<Integer> {
			private int value;

			NodeIntegerInt(int key) {
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

		private static class NodeIntegerVoid extends Node<Void> implements HeapPairing.Abstract.NodeVoidVal<Integer> {
			NodeIntegerVoid(int key) {
				super(key);
			}
		}

	}

}
