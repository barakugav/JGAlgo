package com.jgalgo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

public class RedBlackTreeExtended<E> extends RedBlackTree<E> {

	private Node<E>[] nodes;
	private int nodesNextIdx;
	private final Extension<E>[] extensions;

	@SuppressWarnings("rawtypes")
	private static final Node[] EmptyNodesArray = new Node[0];

	@SuppressWarnings("unchecked")
	RedBlackTreeExtended(Comparator<? super E> c, Collection<? extends Extension<E>> extensions) {
		super(c);
		this.extensions = extensions.toArray(len -> new Extension[len]);
		nodes = EmptyNodesArray;
	}

	@Override
	Node<E> newNode(E e) {
		int idx = nodesNextIdx++;
		if (idx >= nodes.length) {
			int newLen = Math.max(2, nodes.length * 2);
			nodes = Arrays.copyOf(nodes, newLen);
			for (Extension<E> extension : extensions)
				extension.data.expand(newLen);
		}
		assert nodes[idx] == null;
		Node<E> n = nodes[idx] = new Node<>(e, idx);
		for (Extension<E> extension : extensions)
			extension.initNode(n);
		return n;
	}

	@Override
	void removeNode(RedBlackTree.Node<E> n0) {
		super.removeNode(n0);
		Node<E> n = (Node<E>) n0;
		assert nodes[n.idx] == n;
		nodes[n.idx] = null;
		for (Extension<E> extension : extensions)
			extension.data.clear(n.idx);
		if (size() < nodesNextIdx / 2)
			reassignIndices();
	}

	private void reassignIndices() {
		Node<E>[] nodes = this.nodes;
		int maxNodeIdx = nodesNextIdx;
		int newNextIdx = 0;
		for (int idx = 0; idx < maxNodeIdx; idx++) {
			Node<E> node = nodes[idx];
			if (node == null)
				continue;
			assert node.idx == idx;
			int newIdx = newNextIdx++;
			nodes[idx] = null;
			nodes[node.idx = newIdx] = node;
			for (Extension<E> extension : extensions)
				extension.data.swap(idx, newIdx);
		}
		nodesNextIdx = newNextIdx;
	}

	@Override
	void swap(RedBlackTree.Node<E> a, RedBlackTree.Node<E> b) {
		Node<E> n1 = (Node<E>) a, n2 = (Node<E>) b;
		for (Extension<E> extension : extensions)
			extension.beforeNodeSwap((Node<E>) a, (Node<E>) b);
		super.swap(n1, n2);
	}

	@Override
	void afterInsert(RedBlackTree.Node<E> n) {
		for (Extension<E> extension : extensions)
			extension.afterInsert((Node<E>) n);
	}

	@Override
	void beforeRemove(RedBlackTree.Node<E> n) {
		for (Extension<E> extension : extensions)
			extension.beforeRemove((Node<E>) n);
	}

	@Override
	void beforeRotateLeft(RedBlackTree.Node<E> n) {
		for (Extension<E> extension : extensions)
			extension.beforeRotateLeft((Node<E>) n);
	}

	@Override
	void beforeRotateRight(RedBlackTree.Node<E> n) {
		for (Extension<E> extension : extensions)
			extension.beforeRotateRight((Node<E>) n);
	}

	private static class Node<E> extends RedBlackTree.Node<E> {

		private int idx;

		Node(E e, int idx) {
			super(e);
			this.idx = idx;
		}

		Node<E> parent() {
			return (Node<E>) parent;
		}

		Node<E> left() {
			return (Node<E>) left;
		}

		Node<E> right() {
			return (Node<E>) right;
		}

	}

	private static abstract class ExtensionData {
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

	private static abstract class Extension<E> {
		final ExtensionData data;

		private Extension(ExtensionData data) {
			this.data = data;
		}

		protected void initNode(Node<E> n) {
		}

		protected void removeNodeData(Node<E> n) {
		}

		protected void afterInsert(Node<E> n) {
		}

		protected void beforeRemove(Node<E> n) {
		}

		protected void beforeNodeSwap(Node<E> a, Node<E> b) {
		}

		protected void beforeRotateLeft(Node<E> n) {
		}

		protected void beforeRotateRight(Node<E> n) {
		}

	}

	private static abstract class ExtensionObj<E, D> extends Extension<E> {

		public ExtensionObj() {
			super(new ExtensionData.Obj<D>());
		}

		@SuppressWarnings("unchecked")
		private ExtensionData.Obj<D> data() {
			return (ExtensionData.Obj<D>) data;
		}

		public D getNodeData(Node<E> n) {
			return data().get(n.idx);
		}

		public void setNodeData(Node<E> n, D data) {
			data().set(n.idx, data);
		}

	}

	private static abstract class ExtensionInt<E> extends Extension<E> {

		public ExtensionInt() {
			super(new ExtensionData.Int());
		}

		private ExtensionData.Int data() {
			return (ExtensionData.Int) data;
		}

		public int getNodeData(Node<E> n) {
			return data().get(n.idx);
		}

		public void setNodeData(Node<E> n, int data) {
			data().set(n.idx, data);
		}
	}

	public static class ExtensionSize<E> extends ExtensionInt<E> {

		ExtensionSize() {
		}

		public int getSubTreeSize(Handle<E> handle) {
			return getNodeData((Node<E>) handle);
		}

		@Override
		protected void initNode(Node<E> n) {
			setNodeData(n, 1);
		}

		@Override
		protected void afterInsert(Node<E> n) {
			/* for each ancestor, increase sub tree size by 1 */
			for (; (n = n.parent()) != null;)
				setNodeData(n, getNodeData(n) + 1);
		}

		@Override
		protected void beforeRemove(Node<E> n) {
			/* for each ancestor, decrease sub tree size by 1 */
			for (; (n = n.parent()) != null;)
				setNodeData(n, getNodeData(n) - 1);
		}

		@Override
		protected void beforeNodeSwap(Node<E> a, Node<E> b) {
			int s = getNodeData(a);
			setNodeData(a, getNodeData(b));
			setNodeData(b, s);
		}

		@Override
		protected void beforeRotateLeft(Node<E> n) {
			Node<E> child = n.right(), grandchild = child.left();
			int childSize = getNodeData(child), grandchildSize = grandchild != null ? getNodeData(grandchild) : 0;

			setNodeData(n, getNodeData(n) - childSize + grandchildSize);
			setNodeData(child, childSize - grandchildSize + getNodeData(n));
		}

		@Override
		protected void beforeRotateRight(Node<E> n) {
			Node<E> child = n.left(), grandchild = child.right();
			int childSize = getNodeData(child), grandchildSize = grandchild != null ? getNodeData(grandchild) : 0;

			setNodeData(n, getNodeData(n) - childSize + grandchildSize);
			setNodeData(child, childSize - grandchildSize + getNodeData(n));
		}

	}

	public static class ExtensionMin<E> extends ExtensionObj<E, Node<E>> {

		ExtensionMin() {
		}

		public Handle<E> getSubTreeMin(Handle<E> handle) {
			return getNodeData((Node<E>) handle);
		}

		@Override
		protected void initNode(Node<E> n) {
			/* minimum node of subtree of the single node is the node itself */
			setNodeData(n, n);
		}

		@Override
		protected void afterInsert(Node<E> n) {
			for (Node<E> p = n; p.parent() != null && p == p.parent().left(); p = p.parent())
				setNodeData(p.parent(), n);
		}

		@Override
		protected void beforeRemove(Node<E> n) {
			Node<E> min;
			if (n.left() != null)
				min = getNodeData(n.left());
			else if (n.right() != null)
				min = getNodeData(n.right());
			else
				min = n.parent();

			for (Node<E> p = n; p.parent() != null && p == p.parent().left(); p = p.parent())
				setNodeData(p.parent(), min);
		}

		@Override
		protected void beforeNodeSwap(Node<E> a, Node<E> b) {
			if (getNodeData(b) == a) {
				Node<E> temp = a;
				a = b;
				a = temp;
			}
			if (getNodeData(a) == b) {
				assert getNodeData(b) == b;
				for (Node<E> p = b; p.parent() != null && p == p.parent().left(); p = p.parent())
					setNodeData(p.parent(), a);
				assert getNodeData(a) == a : "a should be ancestor of b";
				setNodeData(b, a);
				return;
			}

			Node<E> aData, bData;
			if (a.hasLeftChild()) {
				assert getNodeData(a) == getNodeData(a.left());
				bData = getNodeData(a);
			} else {
				assert getNodeData(a) == a;
				for (Node<E> p = a; p.parent() != null && p == p.parent().left(); p = p.parent()) {
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
				for (Node<E> p = b; p.parent() != null && p == p.parent().left(); p = p.parent())
					setNodeData(p.parent(), a);
				aData = a;
			}
			setNodeData(a, aData);
			setNodeData(b, bData);
		}

		@Override
		protected void beforeRotateLeft(Node<E> n) {
			Node<E> child = n.right();
			setNodeData(child, getNodeData(n));
		}

		@Override
		protected void beforeRotateRight(Node<E> n) {
			Node<E> grandchild = n.left().right();
			setNodeData(n, grandchild != null ? getNodeData(grandchild) : n);
		}

	}

	public static class ExtensionMax<E> extends ExtensionObj<E, Node<E>> {

		ExtensionMax() {
		}

		public Handle<E> getSubTreeMax(Handle<E> handle) {
			return getNodeData((Node<E>) handle);
		}

		@Override
		protected void initNode(Node<E> n) {
			/* maximum node of subtree of the single node is the node itself */
			setNodeData(n, n);
		}

		@Override
		protected void afterInsert(Node<E> n) {
			for (Node<E> p = n; p.parent() != null && p == p.parent().right(); p = p.parent())
				setNodeData(p.parent(), n);
		}

		@Override
		protected void beforeRemove(Node<E> n) {
			Node<E> max;
			if (n.right() != null)
				max = getNodeData(n.right());
			else if (n.left() != null)
				max = getNodeData(n.left());
			else
				max = n.parent();

			for (Node<E> p = n; p.parent() != null && p == p.parent().right(); p = p.parent())
				setNodeData(p.parent(), max);
		}

		@Override
		protected void beforeNodeSwap(Node<E> a, Node<E> b) {
			if (getNodeData(b) == a) {
				Node<E> temp = a;
				a = b;
				a = temp;
			}
			if (getNodeData(a) == b) {
				assert getNodeData(b) == b;
				for (Node<E> p = b; p.parent() != null && p == p.parent().right(); p = p.parent())
					setNodeData(p.parent(), a);
				assert getNodeData(a) == a : "a should be ancestor of b";
				setNodeData(b, a);
				return;
			}

			Node<E> aData, bData;
			if (a.hasRightChild()) {
				assert getNodeData(a) == getNodeData(a.right());
				bData = getNodeData(a);
			} else {
				assert getNodeData(a) == a;
				for (Node<E> p = a; p.parent() != null && p == p.parent().right(); p = p.parent()) {
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
				for (Node<E> p = b; p.parent() != null && p == p.parent().right(); p = p.parent())
					setNodeData(p.parent(), a);
				aData = a;
			}
			setNodeData(a, aData);
			setNodeData(b, bData);
		}

		@Override
		protected void beforeRotateLeft(Node<E> n) {
			Node<E> grandchild = n.right().left();
			setNodeData(n, grandchild != null ? getNodeData(grandchild) : n);
		}

		@Override
		protected void beforeRotateRight(Node<E> n) {
			Node<E> child = n.left();
			setNodeData(child, getNodeData(n));
		}

	}

	public static class Builder<E> {

		private Comparator<? super E> c;
		private final List<Extension<E>> extensions;

		public Builder() {
			c = null;
			extensions = new ArrayList<>();
		}

		public void comparator(Comparator<? super E> c) {
			this.c = c;
		}

		public ExtensionSize<E> addSizeExtension() {
			return addExtension(new ExtensionSize<>());
		}

		public ExtensionMin<E> addMinExtension() {
			return addExtension(new ExtensionMin<>());
		}

		public ExtensionMax<E> addMaxExtension() {
			return addExtension(new ExtensionMax<>());
		}

		private <T extends Extension<E>> T addExtension(T extension) {
			extensions.add(extension);
			return extension;
		}

		public void clear() {
			c = null;
			extensions.clear();
		}

		public RedBlackTree<E> build() {
			return extensions.isEmpty() ? new RedBlackTree<>(c) : new RedBlackTreeExtended<>(c, extensions);
		}

	}

}
