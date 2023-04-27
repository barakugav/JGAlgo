package com.jgalgo;

/**
 * An extension to a dynamic trees implementation.
 * <p>
 * Extension such as {@link DynamicTreeExtension.TreeSize} can be added to a
 * {@link DynamicTree} implementation without increasing the asymptotical
 * running time of any of the operations.
 *
 * <pre> {@code
 * DynamicTree.Builder builder = DynamicTree.newBuilder();
 * builder.addExtension(DynamicTreeExtension.TreeSize.class);
 *
 * DynamicTree dt = builder.build();
 * DynamicTreeExtension.TreeSize treeSizeExt = dt.getExtension(DynamicTreeExtension.TreeSize.class);
 * ...
 * DynamicTree.Node n1 = dt.makeTree();
 * DynamicTree.Node n2 = dt.makeTree();
 *
 * System.out.println("The number of nodes in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
 * }</pre>
 *
 * @see DynamicTreeSplayExtended
 * @see DynamicTreeSplayIntExtended
 * @author Barak Ugav
 */
public interface DynamicTreeExtension {

	/**
	 * An extension to {@link DynamicTree} that keep track on the number of nodes in
	 * each tree.
	 * <p>
	 * The extension add some fields to each node, and maintain them during
	 * operation on the forest. The asymptotical running time of all the operations
	 * does not increase, and an addition operation that query the number of nodes
	 * in the current tree of any given node is added via the
	 * {@link #getTreeSize(com.jgalgo.DynamicTree.Node)} method.
	 *
	 * <pre> {@code
	 * DynamicTree.Builder builder = DynamicTree.newBuilder();
	 * builder.addExtension(DynamicTreeExtension.TreeSize.class);
	 *
	 * DynamicTree dt = builder.build();
	 * DynamicTreeExtension.TreeSize treeSizeExt = dt.getExtension(DynamicTreeExtension.TreeSize.class);
	 * ...
	 * DynamicTree.Node n1 = dt.makeTree();
	 * DynamicTree.Node n2 = dt.makeTree();
	 *
	 * System.out.println("The number of nodes in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
	 * }</pre>
	 *
	 * @author Barak Ugav
	 */
	static interface TreeSize extends DynamicTreeExtension {

		/**
		 * Get the number of nodes in the current tree of a given node.
		 *
		 * @param node a node in the dynamic tree data structure
		 * @return the number nodes in the tree of the node
		 */
		int getTreeSize(DynamicTree.Node node);
	}

}
