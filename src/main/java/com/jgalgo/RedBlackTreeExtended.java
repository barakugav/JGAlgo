package com.jgalgo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class RedBlackTreeExtended<E> extends RedBlackTree<E> {

	private final List<Extension<E>> extensions;
	private final int extObjNum;
	private final int extIntNum;

	RedBlackTreeExtended(Comparator<? super E> c, Collection<? extends Extension<E>> extensions) {
		super(c);
		this.extensions = new ArrayList<>(extensions);

		int objNum = 0, intNum = 0;
		for (Extension<E> ext : extensions) {
			if (ext instanceof ExtensionObj<?, ?>)
				objNum++;
			else if (ext instanceof ExtensionInt<?>)
				intNum++;
			else
				throw new IllegalArgumentException("Unknown extension type: " + ext);
		}

		this.extObjNum = objNum;
		this.extIntNum = intNum;
	}

	@Override
	Node<E> newNode(E e) {
		Node<E> n = new Node<>(e, extObjNum, extIntNum);
		for (Extension<E> extension : extensions)
			extension.initNode(n);
		return n;
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

		private final Object[] extensionsData;
		private final int[] extensionsDataInt;

		Node(E e, int extensionsNum, int extensionsNumInt) {
			super(e);
			extensionsData = extensionsNum > 0 ? new Object[extensionsNum] : null;
			extensionsDataInt = extensionsNumInt > 0 ? new int[extensionsNumInt] : null;
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

	static abstract class Extension<E> {

		private RedBlackTreeExtended<E> tree;
		private int extIdx = -1;

		private Extension() {
		}

		RedBlackTreeExtended<E> getTree() {
			return tree;
		}

		void setTree(RedBlackTreeExtended<E> tree) {
			this.tree = tree;
		}

		int getExtIdx() {
			return extIdx;
		}

		void setExtIdx(int extIdx) {
			if (this.extIdx != -1)
				throw new IllegalStateException();
			this.extIdx = extIdx;
		}

		protected void initNode(Node<E> n) {
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

	static abstract class ExtensionObj<E, D> extends Extension<E> {

		public ExtensionObj() {
		}

		@SuppressWarnings("unchecked")
		public D getNodeData(Node<E> n) {
			return (D) n.extensionsData[getExtIdx()];
		}

		public void setNodeData(Node<E> n, D data) {
			n.extensionsData[getExtIdx()] = data;
		}

	}

	static abstract class ExtensionInt<E> extends Extension<E> {

		public ExtensionInt() {
		}

		public int getNodeData(Node<E> n) {
			return n.extensionsDataInt[getExtIdx()];
		}

		public void setNodeData(Node<E> n, int data) {
			n.extensionsDataInt[getExtIdx()] = data;
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
		private final List<ExtensionObj<E, ?>> extsObj;
		private final List<ExtensionInt<E>> extsInt;

		public Builder() {
			c = null;
			extsObj = new ArrayList<>();
			extsInt = new ArrayList<>();
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

		@SuppressWarnings("unchecked")
		private <T extends Extension<E>> T addExtension(T extension) {
			if (extension instanceof ExtensionObj<?, ?>) {
				extension.setExtIdx(extsObj.size());
				extsObj.add((ExtensionObj<E, ?>) extension);
			} else if (extension instanceof ExtensionInt<?>) {
				extension.setExtIdx(extsObj.size());
				extsInt.add((ExtensionInt<E>) extension);
			} else
				throw new IllegalArgumentException(extension.toString());
			return extension;
		}

		public void clear() {
			c = null;
			extsObj.clear();
			extsInt.clear();
		}

		public RedBlackTree<E> build() {
			List<Extension<E>> extensions = new ArrayList<>();
			extensions.addAll(extsObj);
			extensions.addAll(extsInt);
			if (extensions.isEmpty())
				return new RedBlackTree<>(c);

			RedBlackTreeExtended<E> tree = new RedBlackTreeExtended<>(c, extensions);
			for (Extension<E> ext : extensions)
				ext.setTree(tree);
			return tree;
		}

	}

}
