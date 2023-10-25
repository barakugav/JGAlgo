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

package com.jgalgo.alg;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.HeapReferenceable;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Minimum spanning tree algorithm.
 * <p>
 * A spanning tree is an edge sub set of the graph edges which form a tree and connect (span) all the vertices of the
 * graph. A minimum spanning tree (MST) is a spanning tree with the minimum edge weights sum over all spanning trees.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
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
	}

	/**
	 * Create a new MST algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumSpanningTree} object. The
	 * {@link MinimumSpanningTree.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MinimumSpanningTree}
	 */
	static MinimumSpanningTree newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new minimum spanning tree algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MinimumSpanningTree} objects
	 */
	static MinimumSpanningTree.Builder newBuilder() {
		return new MinimumSpanningTree.Builder() {
			String impl;
			private HeapReferenceable.Builder<?, ?> heapBuilder;

			@Override
			public MinimumSpanningTree build() {
				if (impl != null) {
					switch (impl) {
						case "kruskal":
							return new MinimumSpanningTreeKruskal();
						case "prim":
							return new MinimumSpanningTreePrim();
						case "boruvka":
							return new MinimumSpanningTreeBoruvka();
						case "yao":
							return new MinimumSpanningTreeYao();
						case "fredman-tarjan":
							return new MinimumSpanningTreeFredmanTarjan();
						case "karger-klein-tarjan":
							return new MinimumSpanningTreeKargerKleinTarjan();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}

				// TODO check for which graphs sizes Kruskal is faster
				MinimumSpanningTreePrim algo = new MinimumSpanningTreePrim();
				if (heapBuilder != null)
					algo.setHeapBuilder(heapBuilder);
				return algo;
			}

			@Override
			public MinimumSpanningTree.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					case "heap-builder":
						heapBuilder = (HeapReferenceable.Builder<?, ?>) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MinimumSpanningTree} objects.
	 *
	 * @see    MinimumSpanningTree#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum spanning tree computation.
		 *
		 * @return a new minimum spanning tree algorithm
		 */
		MinimumSpanningTree build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default MinimumSpanningTree.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
