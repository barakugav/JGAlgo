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
 * RedBlackTreeExtension.Size<Integer> sizeExt = new RedBlackTreeExtension.Size<>();
 * RedBlackTreeExtension.Max<Integer> maxExt = new RedBlackTreeExtension.Max<>();
 * RedBlackTreeExtended<Integer> tree = new RedBlackTreeExtended<>(List.of(sizeExt, maxExt));
 *
 * HeapReference<Integer> e1 = tree.insert(15);
 * tree.insert(5);
 * tree.insert(3);
 * tree.insert(1);
 * ...
 * tree.insert(1);
 *
 * int subTreeSize = sizeExt.getSubTreeSize(e1);
 * HeapReference<Integer> subTreeMax = maxExt.getSubTreeMax(e1);
 * System.out.println("The subtree of " + e1 + " is of size " + subTreeSize);
 * System.out.println("The maximum element in the sub tree of " + e1 + " is " + subTreeMax);
 * }</pre>
 *
 * @author Barak Ugav
 */
public class RedBlackTreeExtension<E> {

	final ExtensionData data;

	private RedBlackTreeExtension(ExtensionData data) {
		this.data = data;
	}

	void initNode(RedBlackTreeExtended.Node<E> n) {}

	void removeNodeData(RedBlackTreeExtended.Node<E> n) {}

	void afterInsert(RedBlackTreeExtended.Node<E> n) {}

	void beforeRemove(RedBlackTreeExtended.Node<E> n) {}

	void beforeNodeSwap(RedBlackTreeExtended.Node<E> a, RedBlackTreeExtended.Node<E> b) {}

	void beforeRotateLeft(RedBlackTreeExtended.Node<E> n) {}

	void beforeRotateRight(RedBlackTreeExtended.Node<E> n) {}

	/**
	 * A subtree size extension to a red black tree.
	 * <p>
	 * The extension will keep track of the subtree size of each node in the red black tree. This is implemented without
	 * increasing the asymptotical running time.
	 *
	 * <pre> {@code
	 * RedBlackTreeExtension.Size<Integer> sizeExt = new RedBlackTreeExtension.Size<>();
	 * RedBlackTreeExtended<Integer> tree = new RedBlackTreeExtended<>(List.of(sizeExt));
	 *
	 * HeapReference<Integer> e1 = tree.insert(15);
	 * HeapReference<Integer> e2 = tree.insert(5);
	 * tree.insert(3);
	 * tree.insert(1);
	 * ...
	 * tree.insert(1);
	 *
	 * System.out.println("The subtree of " + e1 + " is of size " + sizeExt.getSubTreeSize(e1));
	 * System.out.println("The subtree of " + e2 + " is of size " + sizeExt.getSubTreeSize(e2));
	 * }</pre>
	 *
	 * @author Barak Ugav
	 */
	public static class Size<E> extends RedBlackTreeExtension.Int<E> {

		/**
		 * Create a new subtree size extension.
		 * <p>
		 * A new extension should be used for each red black tree individually.
		 */
		public Size() {}

		/**
		 * Get the number of nodes in the subtree of given red black tree node.
		 *
		 * @param  ref a reference to a red black tree node
		 * @return     the number of nodes in the subtree of given red black tree node. The counting include the node
		 *             itself, therefore the returned value is always greater or equal to one.
		 */
		public int getSubTreeSize(HeapReference<E> ref) {
			return getNodeData((RedBlackTreeExtended.Node<E>) ref);
		}

		@Override
		void initNode(RedBlackTreeExtended.Node<E> n) {
			setNodeData(n, 1);
		}

		@Override
		void afterInsert(RedBlackTreeExtended.Node<E> n) {
			/* for each ancestor, increase sub tree size by 1 */
			for (; (n = n.parent()) != null;)
				setNodeData(n, getNodeData(n) + 1);
		}

		@Override
		void beforeRemove(RedBlackTreeExtended.Node<E> n) {
			/* for each ancestor, decrease sub tree size by 1 */
			for (; (n = n.parent()) != null;)
				setNodeData(n, getNodeData(n) - 1);
		}

		@Override
		void beforeNodeSwap(RedBlackTreeExtended.Node<E> a, RedBlackTreeExtended.Node<E> b) {
			int s = getNodeData(a);
			setNodeData(a, getNodeData(b));
			setNodeData(b, s);
		}

		@Override
		void beforeRotateLeft(RedBlackTreeExtended.Node<E> n) {
			RedBlackTreeExtended.Node<E> child = n.right(), grandchild = child.left();
			int childSize = getNodeData(child), grandchildSize = grandchild != null ? getNodeData(grandchild) : 0;

			setNodeData(n, getNodeData(n) - childSize + grandchildSize);
			setNodeData(child, childSize - grandchildSize + getNodeData(n));
		}

		@Override
		void beforeRotateRight(RedBlackTreeExtended.Node<E> n) {
			RedBlackTreeExtended.Node<E> child = n.left(), grandchild = child.right();
			int childSize = getNodeData(child), grandchildSize = grandchild != null ? getNodeData(grandchild) : 0;

			setNodeData(n, getNodeData(n) - childSize + grandchildSize);
			setNodeData(child, childSize - grandchildSize + getNodeData(n));
		}

	}

	/**
	 * A subtree minimum element extension to a red black tree.
	 * <p>
	 * The extension will keep track of the minimum element in the subtree of each node in the red black tree. This is
	 * implemented without increasing the asymptotical running time.
	 *
	 * <pre> {@code
	 * RedBlackTreeExtension.Min<Integer> minExt = new RedBlackTreeExtension.Min<>();
	 * RedBlackTreeExtended<Integer> tree = new RedBlackTreeExtended<>(List.of(minExt));
	 *
	 * HeapReference<Integer> e1 = tree.insert(15);
	 * HeapReference<Integer> e2 = tree.insert(5);
	 * tree.insert(3);
	 * tree.insert(1);
	 * ...
	 * tree.insert(1);
	 *
	 * HeapReference<Integer> e1SubtreeMin = minExt.getSubTreeMin(e1);
	 * HeapReference<Integer> e2SubtreeMin = minExt.getSubTreeMin(e2);
	 * System.out.println("The minimum element in the subtree of " + e1 + " is " + e1SubtreeMin);
	 * System.out.println("The minimum element in the subtree of " + e2 + " is " + e2SubtreeMin);
	 * }</pre>
	 *
	 * @author Barak Ugav
	 */
	public static class Min<E> extends RedBlackTreeExtension.Obj<E, RedBlackTreeExtended.Node<E>> {

		/**
		 * Create a new subtree minimum extension.
		 * <p>
		 * A new extension should be used for each red black tree individually.
		 */
		public Min() {}

		/**
		 * Get a reference to the minimum node in the subtree of given red black tree node.
		 *
		 * @param  ref a reference to a red black tree node
		 * @return     a reference to the minimum node in the subtree of given red black tree node. The subtree include
		 *             the given node itself, therefore the returned element is always smaller or equal to the provided
		 *             node.
		 */
		public HeapReference<E> getSubTreeMin(HeapReference<E> ref) {
			return getNodeData((RedBlackTreeExtended.Node<E>) ref);
		}

		@Override
		void initNode(RedBlackTreeExtended.Node<E> n) {
			/* minimum node of subtree of the single node is the node itself */
			setNodeData(n, n);
		}

		@Override
		void afterInsert(RedBlackTreeExtended.Node<E> n) {
			for (RedBlackTreeExtended.Node<E> p = n; p.parent() != null && p == p.parent().left(); p = p.parent())
				setNodeData(p.parent(), n);
		}

		@Override
		void beforeRemove(RedBlackTreeExtended.Node<E> n) {
			RedBlackTreeExtended.Node<E> min;
			if (n.left() != null)
				min = getNodeData(n.left());
			else if (n.right() != null)
				min = getNodeData(n.right());
			else
				min = n.parent();

			for (RedBlackTreeExtended.Node<E> p = n; p.parent() != null && p == p.parent().left(); p = p.parent())
				setNodeData(p.parent(), min);
		}

		@Override
		void beforeNodeSwap(RedBlackTreeExtended.Node<E> a, RedBlackTreeExtended.Node<E> b) {
			if (getNodeData(b) == a) {
				RedBlackTreeExtended.Node<E> temp = a;
				a = b;
				b = temp;
			}
			if (getNodeData(a) == b) {
				assert getNodeData(b) == b;
				for (RedBlackTreeExtended.Node<E> p = b; p.parent() != null && p == p.parent().left(); p = p.parent())
					setNodeData(p.parent(), a);
				assert getNodeData(a) == a : "a should be ancestor of b";
				setNodeData(b, a);
				return;
			}

			RedBlackTreeExtended.Node<E> aData, bData;
			if (a.hasLeftChild()) {
				assert getNodeData(a) == getNodeData(a.left());
				bData = getNodeData(a);
			} else {
				assert getNodeData(a) == a;
				for (RedBlackTreeExtended.Node<E> p = a; p.parent() != null && p == p.parent().left(); p = p.parent()) {
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
				for (RedBlackTreeExtended.Node<E> p = b; p.parent() != null && p == p.parent().left(); p = p.parent())
					setNodeData(p.parent(), a);
				aData = a;
			}
			setNodeData(a, aData);
			setNodeData(b, bData);
		}

		@Override
		void beforeRotateLeft(RedBlackTreeExtended.Node<E> n) {
			RedBlackTreeExtended.Node<E> child = n.right();
			setNodeData(child, getNodeData(n));
		}

		@Override
		void beforeRotateRight(RedBlackTreeExtended.Node<E> n) {
			RedBlackTreeExtended.Node<E> grandchild = n.left().right();
			setNodeData(n, grandchild != null ? getNodeData(grandchild) : n);
		}

	}

	/**
	 * A subtree maximum element extension to a red black tree.
	 * <p>
	 * The extension will keep track of the maximum element in the subtree of each node in the red black tree. This is
	 * implemented without increasing the asymptotical running time.
	 *
	 * <pre> {@code
	 * RedBlackTreeExtension.Max<Integer> maxExt = new RedBlackTreeExtension.Max<>();
	 * RedBlackTreeExtended<Integer> tree = new RedBlackTreeExtended<>(List.of(maxExt));
	 *
	 * HeapReference<Integer> e1 = tree.insert(15);
	 * HeapReference<Integer> e2 = tree.insert(5);
	 * tree.insert(3);
	 * tree.insert(1);
	 * ...
	 * tree.insert(1);
	 *
	 * HeapReference<Integer> e1SubtreeMax = maxExt.getSubTreeMax(e1);
	 * HeapReference<Integer> e2SubtreeMax = maxExt.getSubTreeMax(e2);
	 * System.out.println("The maximum element in the subtree of " + e1 + " is " + e1SubtreeMax);
	 * System.out.println("The maximum element in the subtree of " + e2 + " is " + e2SubtreeMax);
	 * }</pre>
	 *
	 * @author Barak Ugav
	 */
	public static class Max<E> extends RedBlackTreeExtension.Obj<E, RedBlackTreeExtended.Node<E>> {

		/**
		 * Create a new subtree maximum extension.
		 * <p>
		 * A new extension should be used for each red black tree individually.
		 */
		public Max() {}

		/**
		 * Get a reference to the maximum node in the subtree of given red black tree node.
		 *
		 * @param  ref a reference to a red black tree node
		 * @return     a reference to the maximum node in the subtree of given red black tree node. The subtree include
		 *             the given node itself, therefore the returned element is always greater or equal to the provided
		 *             node.
		 */
		public HeapReference<E> getSubTreeMax(HeapReference<E> ref) {
			return getNodeData((RedBlackTreeExtended.Node<E>) ref);
		}

		@Override
		void initNode(RedBlackTreeExtended.Node<E> n) {
			/* maximum node of subtree of the single node is the node itself */
			setNodeData(n, n);
		}

		@Override
		void afterInsert(RedBlackTreeExtended.Node<E> n) {
			for (RedBlackTreeExtended.Node<E> p = n; p.parent() != null && p == p.parent().right(); p = p.parent())
				setNodeData(p.parent(), n);
		}

		@Override
		void beforeRemove(RedBlackTreeExtended.Node<E> n) {
			RedBlackTreeExtended.Node<E> max;
			if (n.right() != null)
				max = getNodeData(n.right());
			else if (n.left() != null)
				max = getNodeData(n.left());
			else
				max = n.parent();

			for (RedBlackTreeExtended.Node<E> p = n; p.parent() != null && p == p.parent().right(); p = p.parent())
				setNodeData(p.parent(), max);
		}

		@Override
		void beforeNodeSwap(RedBlackTreeExtended.Node<E> a, RedBlackTreeExtended.Node<E> b) {
			if (getNodeData(b) == a) {
				RedBlackTreeExtended.Node<E> temp = a;
				a = b;
				b = temp;
			}
			if (getNodeData(a) == b) {
				assert getNodeData(b) == b;
				for (RedBlackTreeExtended.Node<E> p = b; p.parent() != null && p == p.parent().right(); p = p.parent())
					setNodeData(p.parent(), a);
				assert getNodeData(a) == a : "a should be ancestor of b";
				setNodeData(b, a);
				return;
			}

			RedBlackTreeExtended.Node<E> aData, bData;
			if (a.hasRightChild()) {
				assert getNodeData(a) == getNodeData(a.right());
				bData = getNodeData(a);
			} else {
				assert getNodeData(a) == a;
				for (RedBlackTreeExtended.Node<E> p = a; p.parent() != null && p == p.parent().right(); p =
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
				for (RedBlackTreeExtended.Node<E> p = b; p.parent() != null && p == p.parent().right(); p = p.parent())
					setNodeData(p.parent(), a);
				aData = a;
			}
			setNodeData(a, aData);
			setNodeData(b, bData);
		}

		@Override
		void beforeRotateLeft(RedBlackTreeExtended.Node<E> n) {
			RedBlackTreeExtended.Node<E> grandchild = n.right().left();
			setNodeData(n, grandchild != null ? getNodeData(grandchild) : n);
		}

		@Override
		void beforeRotateRight(RedBlackTreeExtended.Node<E> n) {
			RedBlackTreeExtended.Node<E> child = n.left();
			setNodeData(child, getNodeData(n));
		}

	}

	private static abstract class Obj<E, D> extends RedBlackTreeExtension<E> {

		Obj() {
			super(new ExtensionData.Obj<D>());
		}

		@SuppressWarnings("unchecked")
		private ExtensionData.Obj<D> data() {
			return (ExtensionData.Obj<D>) data;
		}

		D getNodeData(RedBlackTreeExtended.Node<E> n) {
			return data().get(n.idx);
		}

		void setNodeData(RedBlackTreeExtended.Node<E> n, D data) {
			data().set(n.idx, data);
		}

	}

	private static abstract class Int<E> extends RedBlackTreeExtension<E> {

		Int() {
			super(new ExtensionData.Int());
		}

		private ExtensionData.Int data() {
			return (ExtensionData.Int) data;
		}

		int getNodeData(RedBlackTreeExtended.Node<E> n) {
			return data().get(n.idx);
		}

		void setNodeData(RedBlackTreeExtended.Node<E> n, int data) {
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
				Object temp = data[idx1];
				data[idx1] = data[idx2];
				data[idx2] = temp;
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
				int temp = data[idx1];
				data[idx1] = data[idx2];
				data[idx2] = temp;
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
