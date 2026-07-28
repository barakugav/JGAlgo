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

/**
 * An extension to a dynamic trees implementation.
 *
 * <p>
 * Extension such as {@link DynamicTreeExtension.TreeSize} can be added to a {@link DynamicTree} implementation without
 * increasing the asymptotical running time of any of the operations.
 *
 * <pre> {@code
 * DynamicTree.Builder builder = DynamicTree.builder();
 * builder.addExtension(DynamicTreeExtension.TreeSize.class);
 *
 * DynamicTree dt = builder.build();
 * DynamicTreeExtension.TreeSize treeSizeExt = dt.getExtension(DynamicTreeExtension.TreeSize.class);
 * ...
 * DynamicTree.Vertex n1 = dt.makeTree();
 * DynamicTree.Vertex n2 = dt.makeTree();
 *
 * System.out.println("The number of vertices in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
 * }</pre>
 *
 * @see    DynamicTreeSplayExtended
 * @see    DynamicTreeSplayIntExtended
 * @author Barak Ugav
 */
public interface DynamicTreeExtension {

	/**
	 * An extension to {@link DynamicTree} that keep track on the number of vertices in each tree.
	 *
	 * <p>
	 * The extension add some fields to each vertex, and maintain them during operation on the forest. The asymptotical
	 * running time of all the operations does not increase, and an addition operation that query the number of vertices
	 * in the current tree of any given vertex is added via the
	 * {@link #getTreeSize(com.jgalgo.internal.ds.DynamicTree.Vertex)} method.
	 *
	 * <pre> {@code
	 * DynamicTree.Builder builder = DynamicTree.builder();
	 * builder.addExtension(DynamicTreeExtension.TreeSize.class);
	 *
	 * DynamicTree dt = builder.build();
	 * DynamicTreeExtension.TreeSize treeSizeExt = dt.getExtension(DynamicTreeExtension.TreeSize.class);
	 * ...
	 * DynamicTree.Vertex n1 = dt.makeTree();
	 * DynamicTree.Vertex n2 = dt.makeTree();
	 *
	 * System.out.println("The number of vertices in the tree of " + n1 + " is " + treeSizeExt.getTreeSize(n1));
	 * }</pre>
	 *
	 * @author Barak Ugav
	 */
	static interface TreeSize extends DynamicTreeExtension {

		/**
		 * Get the number of vertices in the current tree of a given vertex.
		 *
		 * @param  vertex a vertex in the dynamic tree data structure
		 * @return        the number vertices in the tree of the vertex
		 */
		int getTreeSize(DynamicTree.Vertex vertex);
	}

}
