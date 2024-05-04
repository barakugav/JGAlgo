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

package com.jgalgo.alg.tree;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;

/**
 * Static Lowest Common Ancestor (LCA) algorithm.
 *
 * <p>
 * The lowest common ancestor of two vertices in a tree is the vertex that appear in both vertices paths to the root
 * (common ancestor), and its farthest from the root (lowest). Given a tree \(G=(V,E)\), we would like to pre process it
 * and then answer queries of the type "what is the lower common ancestor of two vertices \(u\) and \(v\)?".
 *
 * <p>
 * Most implementations of this interface achieve linear or near linear preprocessing time and constant or logarithmic
 * query time.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * <pre> {@code
 * Graph<String, Integer> tree = Graph.newUndirected();
 * tree.addVertex("Grandfather Bob");
 * tree.addVertex("Father John");
 * tree.addVertex("Me");
 * tree.addVertex("Sister Jane");
 * tree.addVertex("Uncle Nick");
 * tree.addVertex("Cousin Alice");
 *
 * tree.addEdge("Grandfather Bob", "Father John", 1957);
 * tree.addEdge("Father John", "Me", 1985);
 * tree.addEdge("Father John", "Sister Jane", 1987);
 * tree.addEdge("Grandfather Bob", "Uncle Nick", 1960);
 * tree.addEdge("Uncle Nick", "Cousin Alice", 1990);
 *
 * LowestCommonAncestorStatic lca = LowestCommonAncestorStatic.newInstance();
 * LowestCommonAncestorStatic.DataStructure<String, Integer> lcaDS = lca.preProcessTree(tree, "Grandfather Bob");
 * assert lcaDS.findLowestCommonAncestor("Father John", "Uncle Nick").equals("Grandfather Bob");
 * assert lcaDS.findLowestCommonAncestor("Me", "Sister Jane").equals("Father John");
 * assert lcaDS.findLowestCommonAncestor("Uncle Nick", "Cousin Alice").equals("Uncle Nick");
 * assert lcaDS.findLowestCommonAncestor("Me", "Cousin Alice").equals("Grandfather Bob");
 * }</pre>
 *
 * @see    LowestCommonAncestorDynamic
 * @see    LowestCommonAncestorOffline
 * @author Barak Ugav
 */
public interface LowestCommonAncestorStatic {

	/**
	 * Perform a static pre processing of a tree for future LCA (Lowest common ancestor) queries.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link LowestCommonAncestorStatic.IDataStructure}.
	 *
	 * @param  <V>  the vertices type
	 * @param  <E>  the edges type
	 * @param  tree a tree
	 * @param  root root of the tree
	 * @return      a data structure built from the preprocessing, that can answer LCA queries efficiently
	 */
	<V, E> LowestCommonAncestorStatic.DataStructure<V, E> preProcessTree(Graph<V, E> tree, V root);

	/**
	 * Data structure result created from a static LCA pre-processing.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	@SuppressWarnings("unused")
	interface DataStructure<V, E> {

		/**
		 * Find the lowest common ancestor of two vertices in the tree.
		 *
		 * @param  u the first vertex
		 * @param  v the second vertex
		 * @return   the lowest common ancestor of \(u\) and \(v\)
		 */
		V findLca(V u, V v);
	}

	/**
	 * Data structure result created from a static LCA pre-processing for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	interface IDataStructure extends DataStructure<Integer, Integer> {

		/**
		 * Find the lowest common ancestor of two vertices in the tree.
		 *
		 * @param  u the first vertex
		 * @param  v the second vertex
		 * @return   the lowest common ancestor of \(u\) and \(v\)
		 */
		int findLca(int u, int v);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #findLca(int, int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer findLca(Integer u, Integer v) {
			return Integer.valueOf(findLca(u.intValue(), v.intValue()));
		}
	}

	/**
	 * Create a new algorithm for static LCA queries.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link LowestCommonAncestorStatic} object.
	 *
	 * @return a default implementation of {@link LowestCommonAncestorStatic}
	 */
	static LowestCommonAncestorStatic newInstance() {
		return new LowestCommonAncestorStaticRmq();
	}

}
