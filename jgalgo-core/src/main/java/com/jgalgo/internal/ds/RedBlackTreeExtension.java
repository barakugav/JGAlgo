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

import java.util.Arrays;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

/**
 * An extension property to Red Black tree nodes such as subtree size/min/max.
 * <p>
 * Each node in the balanced binary tree can maintain properties such as its subtree size, or a reference to the
 * minimum/maximum element in its subtree. These properties can be updated during the regular operations of the red
 * black tree without increasing the asymptotical running time.
 * <p>
 * Each extension should be used in exactly one red black tree. If an extension was used in another tree, it should not
 * be passed to a new one for reuse.
 *
 * <pre> {@code
 * RedBlackTreeExtension.Size<Integer, String> sizeExt = new RedBlackTreeExtension.Size<>();
 * RedBlackTreeExtension.Max<Integer, String> maxExt = new RedBlackTreeExtension.Max<>();
 * RedBlackTreeExtended<Integer> tree = new RedBlackTreeExtended<>(List.of(sizeExt, maxExt));
 *
 * HeapReference<Integer, String> e1 = tree.insert(15, "Alice");
 * tree.insert(5, "Bob");
 * tree.insert(3, "Charlie");
 * tree.insert(1, "Door");
 * ...
 * tree.insert(1, "Zebra");
 *
 * int subTreeSize = sizeExt.getSubTreeSize(e1);
 * HeapReference<Integer, String> subTreeMax = maxExt.getSubTreeMax(e1);
 * System.out.println("The subtree of " + e1 + " is of size " + subTreeSize);
 * System.out.println("The maximum element in the sub tree of " + e1 + " is " + subTreeMax);
 * }</pre>
 *
 * @param  <K> the keys type
 * @param  <V> the values type
 * @author     Barak Ugav
 */
class RedBlackTreeExtension<K, V> {

	final ExtensionData data;

	private RedBlackTreeExtension(ExtensionData data) {
		this.data = data;
	}

	void initNode(RedBlackTreeExtended.Node<K, V> n) {}

	void removeNodeData(RedBlackTreeExtended.Node<K, V> n) {}

	void afterInsert(RedBlackTreeExtended.Node<K, V> n) {}

	void beforeRemove(RedBlackTreeExtended.Node<K, V> n) {}

	void beforeNodeSwap(RedBlackTreeExtended.Node<K, V> a, RedBlackTreeExtended.Node<K, V> b) {}

	void beforeRotateLeft(RedBlackTreeExtended.Node<K, V> n) {}

	void beforeRotateRight(RedBlackTreeExtended.Node<K, V> n) {}

	/**
	 * A subtree size extension to a red black tree.
	 * <p>
	 * The extension will keep track of the subtree size of each node in the red black tree. This is implemented without
	 * increasing the asymptotical running time.
	 *
	 * <pre> {@code
	 * RedBlackTreeExtension.Size<Integer, String> sizeExt = new RedBlackTreeExtension.Size<>();
	 * RedBlackTreeExtended<Integer, String> tree = new RedBlackTreeExtended<>(List.of(sizeExt));
	 *
	 * HeapReference<Integer, String> e1 = tree.insert(15, "Alice");
	 * HeapReference<Integer, String> e2 = tree.insert(5, "Bob");
	 * tree.insert(3, "Charlie");
	 * tree.insert(1, "Dude");
	 * ...
	 * tree.insert(1, "Koala");
	 *
	 * System.out.println("The subtree of " + e1 + " is of size " + sizeExt.getSubTreeSize(e1));
	 * System.out.println("The subtree of " + e2 + " is of size " + sizeExt.getSubTreeSize(e2));
	 * }</pre>
	 *
	 * @param  <K> the keys type
	 * @param  <V> the values type
	 * @author     Barak Ugav
	 */
	static class Size<K, V> extends RedBlackTreeExtension.Int<K, V> {

		/**
		 * Create a new subtree size extension.
		 * <p>
		 * A new extension should be used for each red black tree individually.
		 */
		Size() {}

		/**
		 * Get the number of nodes in the subtree of given red black tree node.
		 *
		 * @param  ref a reference to a red black tree node
		 * @return     the number of nodes in the subtree of given red black tree node. The counting include the node
		 *             itself, therefore the returned value is always greater or equal to one.
		 */
		int getSubTreeSize(HeapReference<K, V> ref) {
			return getNodeData((RedBlackTreeExtended.Node<K, V>) ref);
		}

		@Override
		void initNode(RedBlackTreeExtended.Node<K, V> n) {
			setNodeData(n, 1);
		}

		@Override
		void afterInsert(RedBlackTreeExtended.Node<K, V> n) {
			/* for each ancestor, increase sub tree size by 1 */
			for (; (n = n.parent()) != null;)
				setNodeData(n, getNodeData(n) + 1);
		}

		@Override
		void beforeRemove(RedBlackTreeExtended.Node<K, V> n) {
			/* for each ancestor, decrease sub tree size by 1 */
			for (; (n = n.parent()) != null;)
				setNodeData(n, getNodeData(n) - 1);
		}

		@Override
		void beforeNodeSwap(RedBlackTreeExtended.Node<K, V> a, RedBlackTreeExtended.Node<K, V> b) {
			int s = getNodeData(a);
			setNodeData(a, getNodeData(b));
			setNodeData(b, s);
		}

		@Override
		void beforeRotateLeft(RedBlackTreeExtended.Node<K, V> n) {
			RedBlackTreeExtended.Node<K, V> child = n.right(), grandchild = child.left();
			int childSize = getNodeData(child), grandchildSize = grandchild != null ? getNodeData(grandchild) : 0;

			setNodeData(n, getNodeData(n) - childSize + grandchildSize);
			setNodeData(child, childSize - grandchildSize + getNodeData(n));
		}

		@Override
		void beforeRotateRight(RedBlackTreeExtended.Node<K, V> n) {
			RedBlackTreeExtended.Node<K, V> child = n.left(), grandchild = child.right();
			int childSize = getNodeData(child), grandchildSize = grandchild != null ? getNodeData(grandchild) : 0;

			setNodeData(n, getNodeData(n) - childSize + grandchildSize);
			setNodeData(child, childSize - grandchildSize + getNodeData(n));
		}

	}

	/**
	 * A subtree minimum element extension to a red black tree.
	 * <p>
	 * The extension will keep track of the element with the minimum key in the subtree of each node in the red black
	 * tree. This is implemented without increasing the asymptotical running time.
	 *
	 * <pre> {@code
	 * RedBlackTreeExtension.Min<Integer, String> minExt = new RedBlackTreeExtension.Min<>();
	 * RedBlackTreeExtended<Integer, String> tree = new RedBlackTreeExtended<>(List.of(minExt));
	 *
	 * HeapReference<Integer, String> e1 = tree.insert(15, "Alice");
	 * HeapReference<Integer, String> e2 = tree.insert(5, "Bob");
	 * tree.insert(3, "Charlie");
	 * tree.insert(1, "The Doors");
	 * ...
	 * tree.insert(1, "Led Zeppelin");
	 *
	 * HeapReference<Integer, String> e1SubtreeMin = minExt.getSubTreeMin(e1);
	 * HeapReference<Integer, String> e2SubtreeMin = minExt.getSubTreeMin(e2);
	 * System.out.println("The minimum element in the subtree of " + e1 + " is " + e1SubtreeMin);
	 * System.out.println("The minimum element in the subtree of " + e2 + " is " + e2SubtreeMin);
	 * }</pre>
	 *
	 * @param  <K> the keys type
	 * @param  <V> the values type
	 * @author     Barak Ugav
	 */
	static class Min<K, V> extends RedBlackTreeExtension.Obj<K, V, RedBlackTreeExtended.Node<K, V>> {

		/**
		 * Create a new subtree minimum extension.
		 * <p>
		 * A new extension should be used for each red black tree individually.
		 */
		Min() {}

		/**
		 * Get a reference to the node with the minimum key in the subtree of given red black tree node.
		 *
		 * @param  ref a reference to a red black tree node
		 * @return     a reference to the node with the minimum key in the subtree of given red black tree node. The
		 *             subtree include the given node itself, therefore the returned element is always smaller or equal
		 *             to the provided node.
		 */
		HeapReference<K, V> getSubTreeMin(HeapReference<K, V> ref) {
			return getNodeData((RedBlackTreeExtended.Node<K, V>) ref);
		}

		@Override
		void initNode(RedBlackTreeExtended.Node<K, V> n) {
			/* minimum node of subtree of the single node is the node itself */
			setNodeData(n, n);
		}

		@Override
		void afterInsert(RedBlackTreeExtended.Node<K, V> n) {
			for (RedBlackTreeExtended.Node<K, V> p = n; p.parent() != null && p == p.parent().left(); p = p.parent())
				setNodeData(p.parent(), n);
		}

		@Override
		void beforeRemove(RedBlackTreeExtended.Node<K, V> n) {
			RedBlackTreeExtended.Node<K, V> min;
			if (n.left() != null)
				min = getNodeData(n.left());
			else if (n.right() != null)
				min = getNodeData(n.right());
			else
				min = n.parent();

			for (RedBlackTreeExtended.Node<K, V> p = n; p.parent() != null && p == p.parent().left(); p = p.parent())
				setNodeData(p.parent(), min);
		}

		@Override
		void beforeNodeSwap(RedBlackTreeExtended.Node<K, V> a, RedBlackTreeExtended.Node<K, V> b) {
			if (getNodeData(b) == a) {
				RedBlackTreeExtended.Node<K, V> temp = a;
				a = b;
				b = temp;
			}
			if (getNodeData(a) == b) {
				assert getNodeData(b) == b;
				for (RedBlackTreeExtended.Node<K, V> p = b; p.parent() != null && p == p.parent().left(); p =
						p.parent())
					setNodeData(p.parent(), a);
				assert getNodeData(a) == a : "a should be ancestor of b";
				setNodeData(b, a);
				return;
			}

			RedBlackTreeExtended.Node<K, V> aData, bData;
			if (a.hasLeftChild()) {
				assert getNodeData(a) == getNodeData(a.left());
				bData = getNodeData(a);
			} else {
				assert getNodeData(a) == a;
				for (RedBlackTreeExtended.Node<K, V> p = a; p.parent() != null && p == p.parent().left(); p =
						p.parent()) {
					assert p.parent() != b;
					setNodeData(p.parent(), b);
				}
				bData = b;
			}
			if (b.hasLeftChild()) {
				assert getNodeData(b) == getNodeData(b.left());
				aData = getNodeData(b);
			} else {
				assert getNodeData(b) == b;
				for (RedBlackTreeExtended.Node<K, V> p = b; p.parent() != null && p == p.parent().left(); p =
						p.parent())
					setNodeData(p.parent(), a);
				aData = a;
			}
			setNodeData(a, aData);
			setNodeData(b, bData);
		}

		@Override
		void beforeRotateLeft(RedBlackTreeExtended.Node<K, V> n) {
			RedBlackTreeExtended.Node<K, V> child = n.right();
			setNodeData(child, getNodeData(n));
		}

		@Override
		void beforeRotateRight(RedBlackTreeExtended.Node<K, V> n) {
			RedBlackTreeExtended.Node<K, V> grandchild = n.left().right();
			setNodeData(n, grandchild != null ? getNodeData(grandchild) : n);
		}

	}

	/**
	 * A subtree maximum element extension to a red black tree.
	 * <p>
	 * The extension will keep track of the element with the maximum key in the subtree of each node in the red black
	 * tree. This is implemented without increasing the asymptotical running time.
	 *
	 * <pre> {@code
	 * RedBlackTreeExtension.Max<Integer, String> maxExt = new RedBlackTreeExtension.Max<>();
	 * RedBlackTreeExtended<Integer, String> tree = new RedBlackTreeExtended<>(List.of(maxExt));
	 *
	 * HeapReference<Integer, String> e1 = tree.insert(15, "Alice");
	 * HeapReference<Integer, String> e2 = tree.insert(5, "Bob");
	 * tree.insert(3, "Charlie");
	 * tree.insert(1, "Dollar");
	 * ...
	 * tree.insert(1, "Euro");
	 *
	 * HeapReference<Integer, String> e1SubtreeMax = maxExt.getSubTreeMax(e1);
	 * HeapReference<Integer, String> e2SubtreeMax = maxExt.getSubTreeMax(e2);
	 * System.out.println("The maximum element in the subtree of " + e1 + " is " + e1SubtreeMax);
	 * System.out.println("The maximum element in the subtree of " + e2 + " is " + e2SubtreeMax);
	 * }</pre>
	 *
	 * @param  <K> the keys type
	 * @param  <V> the values type
	 * @author     Barak Ugav
	 */
	static class Max<K, V> extends RedBlackTreeExtension.Obj<K, V, RedBlackTreeExtended.Node<K, V>> {

		/**
		 * Create a new subtree maximum extension.
		 * <p>
		 * A new extension should be used for each red black tree individually.
		 */
		Max() {}

		/**
		 * Get a reference to the node with maximal key in the subtree of given red black tree node.
		 *
		 * @param  ref a reference to a red black tree node
		 * @return     a reference to the node with the maximal key in the subtree of given red black tree node. The
		 *             subtree include the given node itself, therefore the returned element is always greater or equal
		 *             to the provided node.
		 */
		HeapReference<K, V> getSubTreeMax(HeapReference<K, V> ref) {
			return getNodeData((RedBlackTreeExtended.Node<K, V>) ref);
		}

		@Override
		void initNode(RedBlackTreeExtended.Node<K, V> n) {
			/* maximum node of subtree of the single node is the node itself */
			setNodeData(n, n);
		}

		@Override
		void afterInsert(RedBlackTreeExtended.Node<K, V> n) {
			for (RedBlackTreeExtended.Node<K, V> p = n; p.parent() != null && p == p.parent().right(); p = p.parent())
				setNodeData(p.parent(), n);
		}

		@Override
		void beforeRemove(RedBlackTreeExtended.Node<K, V> n) {
			RedBlackTreeExtended.Node<K, V> max;
			if (n.right() != null)
				max = getNodeData(n.right());
			else if (n.left() != null)
				max = getNodeData(n.left());
			else
				max = n.parent();

			for (RedBlackTreeExtended.Node<K, V> p = n; p.parent() != null && p == p.parent().right(); p = p.parent())
				setNodeData(p.parent(), max);
		}

		@Override
		void beforeNodeSwap(RedBlackTreeExtended.Node<K, V> a, RedBlackTreeExtended.Node<K, V> b) {
			if (getNodeData(b) == a) {
				RedBlackTreeExtended.Node<K, V> temp = a;
				a = b;
				b = temp;
			}
			if (getNodeData(a) == b) {
				assert getNodeData(b) == b;
				for (RedBlackTreeExtended.Node<K, V> p = b; p.parent() != null && p == p.parent().right(); p =
						p.parent())
					setNodeData(p.parent(), a);
				assert getNodeData(a) == a : "a should be ancestor of b";
				setNodeData(b, a);
				return;
			}

			RedBlackTreeExtended.Node<K, V> aData, bData;
			if (a.hasRightChild()) {
				assert getNodeData(a) == getNodeData(a.right());
				bData = getNodeData(a);
			} else {
				assert getNodeData(a) == a;
				for (RedBlackTreeExtended.Node<K, V> p = a; p.parent() != null && p == p.parent().right(); p =
						p.parent()) {
					assert p.parent() != b;
					setNodeData(p.parent(), b);
				}
				bData = b;
			}
			if (b.hasRightChild()) {
				assert getNodeData(b) == getNodeData(b.right());
				aData = getNodeData(b);
			} else {
				assert getNodeData(b) == b;
				for (RedBlackTreeExtended.Node<K, V> p = b; p.parent() != null && p == p.parent().right(); p =
						p.parent())
					setNodeData(p.parent(), a);
				aData = a;
			}
			setNodeData(a, aData);
			setNodeData(b, bData);
		}

		@Override
		void beforeRotateLeft(RedBlackTreeExtended.Node<K, V> n) {
			RedBlackTreeExtended.Node<K, V> grandchild = n.right().left();
			setNodeData(n, grandchild != null ? getNodeData(grandchild) : n);
		}

		@Override
		void beforeRotateRight(RedBlackTreeExtended.Node<K, V> n) {
			RedBlackTreeExtended.Node<K, V> child = n.left();
			setNodeData(child, getNodeData(n));
		}

	}

	private static abstract class Obj<K, V, D> extends RedBlackTreeExtension<K, V> {

		Obj() {
			super(new ExtensionData.Obj<D>());
		}

		@SuppressWarnings("unchecked")
		private ExtensionData.Obj<D> data() {
			return (ExtensionData.Obj<D>) data;
		}

		D getNodeData(RedBlackTreeExtended.Node<K, V> n) {
			return data().get(n.idx);
		}

		void setNodeData(RedBlackTreeExtended.Node<K, V> n, D data) {
			data().set(n.idx, data);
		}

	}

	private static abstract class Int<K, V> extends RedBlackTreeExtension<K, V> {

		Int() {
			super(new ExtensionData.Int());
		}

		private ExtensionData.Int data() {
			return (ExtensionData.Int) data;
		}

		int getNodeData(RedBlackTreeExtended.Node<K, V> n) {
			return data().get(n.idx);
		}

		void setNodeData(RedBlackTreeExtended.Node<K, V> n, int data) {
			data().set(n.idx, data);
		}
	}

	static abstract class ExtensionData {
		abstract void swap(int idx1, int idx2);

		abstract void clear(int idx);

		abstract void expand(int newCapacity);

		static class Obj<D> extends ExtensionData {
			private Object[] data;

			Obj() {
				data = ObjectArrays.EMPTY_ARRAY;
			}

			@SuppressWarnings("unchecked")
			D get(int idx) {
				return (D) data[idx];
			}

			void set(int idx, D d) {
				data[idx] = d;
			}

			@Override
			void swap(int idx1, int idx2) {
				ObjectArrays.swap(data, idx1, idx2);
			}

			@Override
			void clear(int idx) {
				data[idx] = null;
			}

			@Override
			void expand(int newCapacity) {
				data = Arrays.copyOf(data, newCapacity);
			}
		}

		static class Int extends ExtensionData {
			private int[] data;

			Int() {
				data = IntArrays.EMPTY_ARRAY;
			}

			int get(int idx) {
				return data[idx];
			}

			void set(int idx, int d) {
				data[idx] = d;
			}

			@Override
			void swap(int idx1, int idx2) {
				IntArrays.swap(data, idx1, idx2);
			}

			@Override
			void clear(int idx) {
				data[idx] = 0;
			}

			@Override
			void expand(int newCapacity) {
				data = Arrays.copyOf(data, newCapacity);
			}
		}
	}

}
