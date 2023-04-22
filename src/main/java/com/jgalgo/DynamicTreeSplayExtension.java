package com.jgalgo;

import java.util.Arrays;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * An extension to a splay tree dynamic trees implementation.
 * <p>
 * Extension such as {@link DynamicTreeSplayExtension.TreeSize} can be added to
 * either {@link DynamicTreeSplayExtended} or
 * {@link DynamicTreeSplayIntExtended} without increasing asymptotical running
 * time of any of the operations.
 * <p>
 * The extensions must be used in the constructor of the extended dynamic trees
 * implementations, and must not be used more than one time.
 *
 * <pre> {@code
 * DynamicTreeSplayExtension.TreeSize treeSizeExt = new DynamicTreeSplayExtension.TreeSize();
 * double weightLimit = 0;
 * DynamicTree dt = new DynamicTreeSplayExtended(weightLimit, List.of(treeSizeExt));
 * DynamicTree.Node n1 = dt.makeTree();
 * DynamicTree.Node n2 = dt.makeTree();
 * ...
 * dt.link(n1, n2, 5.5);
 *
 * System.out.println("The number of nodes in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
 * }</pre>
 *
 * @see DynamicTreeSplayExtended
 * @see DynamicTreeSplayIntExtended
 * @author Barak Ugav
 */
public abstract class DynamicTreeSplayExtension {

	final ExtensionData data;
	DynamicTree dt;

	private DynamicTreeSplayExtension(ExtensionData data) {
		this.data = data;
	}

	void setDynamicTreeAlgo(DynamicTree dt) {
		if (this.dt != null)
			throw new IllegalStateException("extension was already used in some dynamic tree");
		this.dt = Objects.requireNonNull(dt);
	}

	void splay(SplayTree.Node<?, ?> node) {
		if (dt instanceof DynamicTreeSplay)
			((DynamicTreeSplay) dt).splay((DynamicTreeSplay.SplayNode) node);
		else if (dt instanceof DynamicTreeSplayInt)
			((DynamicTreeSplayInt) dt).splay((DynamicTreeSplayInt.SplayNode) node);
		else
			throw new IllegalStateException();
	}

	abstract void initNode(SplayTree.Node<?, ?> n);

	abstract void beforeCut(SplayTree.Node<?, ?> n);

	abstract void afterLink(SplayTree.Node<?, ?> n);

	abstract void beforeRotate(SplayTree.Node<?, ?> n);

	/**
	 * An extension to {@link DynamicTree} that keep track on the number of nodes in
	 * each tree.
	 * <p>
	 * The extension add some fields to each node, and maintain them during
	 * operation on the forest. The asymptotical running time of any operation does
	 * not increase, and an addition operation that query the number of nodes in the
	 * current tree of any given node is added via the
	 * {@link #getTreeSize(com.jgalgo.DynamicTree.Node)} method.
	 *
	 * <pre> {@code
	 * DynamicTreeSplayExtension.TreeSize treeSizeExt = new DynamicTreeSplayExtension.TreeSize();
	 * double weightLimit = 0;
	 * DynamicTree dt = new DynamicTreeSplayExtended(weightLimit, List.of(treeSizeExt));
	 * DynamicTree.Node n1 = dt.makeTree();
	 * DynamicTree.Node n2 = dt.makeTree();
	 * ...
	 * dt.link(n1, n2, 5.5);
	 *
	 * System.out.println("The number of nodes in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
	 * }</pre>
	 *
	 * @author Barak Ugav
	 */
	public static class TreeSize extends DynamicTreeSplayExtension.Int {

		/**
		 * Create a new Tree Size extensions.
		 * <p>
		 * Each instance of this extension should be used in a single dynamic tree
		 * object.
		 */
		public TreeSize() {
		}

		/**
		 * Get the number of nodes in the current tree of a given node.
		 *
		 * @param node a node in the dynamic tree data structure
		 * @return the number nodes in the tree of the node
		 */
		public int getTreeSize(DynamicTree.Node node) {
			SplayTree.Node<?, ?> n = (SplayTree.Node<?, ?>) node;
			splay(n);
			return getNodeData(n);
		}

		@Override
		void initNode(SplayTree.Node<?, ?> n) {
			setNodeData(n, 1);
		}

		@Override
		void beforeCut(SplayTree.Node<?, ?> n) {
			setNodeData(n, getNodeData(n) - getNodeData(n.right));
		}

		@Override
		void afterLink(SplayTree.Node<?, ?> n) {
			SplayTree.Node<?, ?> parent = (SplayTree.Node<?, ?>) ((DynamicTree.Node) n).getParent();
			setNodeData(parent, getNodeData(parent) + getNodeData(n));
		}

		@Override
		void beforeRotate(SplayTree.Node<?, ?> n) {
			SplayTree.Node<?, ?> parent = n.parent;
			int nSize = getNodeData(n);
			int parentOldSize = getNodeData(parent);

			int parentNewSize;
			if (n.isLeftChild()) {
				parentNewSize = parentOldSize - nSize + (n.hasRightChild() ? getNodeData(n.right) : 0);
			} else {
				assert n.isRightChild();
				parentNewSize = parentOldSize - nSize + (n.hasLeftChild() ? getNodeData(n.left) : 0);
			}
			setNodeData(parent, parentNewSize);

			setNodeData(n, parentOldSize);
		}

	}

	private static abstract class Int extends DynamicTreeSplayExtension {

		Int() {
			super(new ExtensionData.Int());
		}

		private ExtensionData.Int data() {
			return (ExtensionData.Int) data;
		}

		int getNodeData(SplayTree.Node<?, ?> n) {
			return data().get(((SplayNodeExtended) n).idx());
		}

		void setNodeData(SplayTree.Node<?, ?> n, int data) {
			data().set(((SplayNodeExtended) n).idx(), data);
		}
	}

	static abstract class ExtensionData {
		abstract void swap(int idx1, int idx2);

		abstract void clear(int idx);

		abstract void expand(int newCapacity);

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

	static interface SplayNodeExtended {
		int idx();
	}

}
