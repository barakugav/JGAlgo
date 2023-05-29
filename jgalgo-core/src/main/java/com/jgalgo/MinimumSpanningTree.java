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

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Minimum spanning tree algorithm.
 * <p>
 * A spanning tree is an edge sub set of the graph edges which form a tree and connect (span) all the vertices of the
 * graph. A minimum spanning tree (MST) is a spanning tree with the minimum edge weights sum over all spanning trees.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Minimum_spanning_tree">Wikipedia</a>
 * @see    MinimumDirectedSpanningTree
 * @author Barak Ugav
 */
public interface MinimumSpanningTree {

	/**
	 * Compute the minimum spanning tree (MST) of a given graph.
	 *
	 * @param  g a graph
	 * @param  w an edge weight function
	 * @return   a result object containing all the edges of the computed spanning tree, which there are \(n-1\) of them
	 *           (or less, forming a forest if the graph is not connected)
	 */
	MinimumSpanningTree.Result computeMinimumSpanningTree(Graph g, WeightFunction w);

	/**
	 * A result object for {@link MinimumSpanningTree} computation.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get all the edges that form the spanning tree.
		 *
		 * @return a collection of the MST edges.
		 */
		IntCollection edges();

		/**
		 * Get the MST weight with respect to a weight function
		 *
		 * @param  w a weight function
		 * @return   the sum of the tree edges weights
		 */
		double weight(WeightFunction w);
	}

	/**
	 * Create a new minimum spanning tree algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumSpanningTree} object.
	 *
	 * @return a new builder that can build {@link MinimumSpanningTree} objects
	 */
	static MinimumSpanningTree.Builder newBuilder() {
		// TODO check for which graphs sizes Kruskal is faster
		return MinimumSpanningTreePrim::new;
	}

	/**
	 * A builder for {@link MinimumSpanningTree} objects.
	 *
	 * @see    MinimumSpanningTree#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<MinimumSpanningTree.Builder> {

		/**
		 * Create a new algorithm object for minimum spanning tree computation.
		 *
		 * @return a new minimum spanning tree algorithm
		 */
		MinimumSpanningTree build();
	}

}
