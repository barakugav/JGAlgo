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

import java.util.Arrays;
import java.util.BitSet;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.HeapReferenceable;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.FIFOQueueLongNoReduce;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

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
	 * Check whether a given set of edges is a spanning tree of a given graph.
	 * <p>
	 * A set of edges is spanning tree if it is a tree and connects all the vertices of the graph. Specifically, if the
	 * graph is not empty, the number of edges must be \(n-1\) where \(n\) denote the number of vertices in the graph.
	 * The edge set should not contain any duplicate edges.
	 *
	 * @param  g     a graph
	 * @param  edges a set of edges that should form a spanning tree
	 * @return       {@code true} if the given set of edges is a spanning tree of the given graph, {@code false}
	 *               otherwise
	 */
	static boolean isSpanningTree(Graph g, IntCollection edges) {
		Assertions.Graphs.onlyUndirected(g);
		IndexGraph ig;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
		} else {
			ig = g.indexGraph();
			edges = IndexIdMaps.idToIndexCollection(edges, g.indexGraphEdgesMap());
		}
		final int m = ig.edges().size();
		final int n = ig.vertices().size();
		if (n == 0) {
			assert m == 0;
			return edges.isEmpty();
		}
		if (edges.size() != n - 1)
			return false;
		BitSet edgesBitmap = new BitSet(m);
		for (int e : edges) {
			if (!ig.edges().contains(e))
				throw new IllegalArgumentException("invalid edge index " + e);
			if (edgesBitmap.get(e))
				throw new IllegalArgumentException(
						"edge with index " + e + " is included more than once in the spanning tree");
			edgesBitmap.set(e);
		}

		/* perform a BFS from some vertex using only the spanning tree edges */
		BitSet visited = new BitSet(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		visited.set(0);
		queue.enqueue(0);
		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			for (EdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (!edgesBitmap.get(e))
					continue;
				int v = eit.target();
				if (visited.get(v))
					continue;
				visited.set(v);
				queue.enqueue(v);
			}
		}
		/* make sure we reached all vertices */
		return visited.nextClearBit(0) == n;
	}

	/**
	 * Check whether a given set of edges is a spanning forest of a given graph.
	 * <p>
	 * A set of edges is spanning forest if it is a forest (do not contains cycles) which connected any pair of vertices
	 * that are connected in the original graph, namely its connected components are identical to the connected
	 * components of the original graph. Specifically, the number of edges must be \(n-c\) where \(n\) denote the number
	 * of vertices in the graph and \(c\) denote the number of connected components in the graph. The edge set should
	 * not contain any duplicate edges.
	 *
	 * @param  g     a graph
	 * @param  edges a set of edges that should form a spanning forest
	 * @return       {@code true} if the given set of edges is a spanning forest of the given graph, {@code false}
	 *               otherwise
	 */
	static boolean isSpanningForest(Graph g, IntCollection edges) {
		Assertions.Graphs.onlyUndirected(g);
		IndexGraph ig;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
		} else {
			ig = g.indexGraph();
			edges = IndexIdMaps.idToIndexCollection(edges, g.indexGraphEdgesMap());
		}
		final int m = ig.edges().size();
		final int n = ig.vertices().size();
		if (n == 0) {
			assert m == 0;
			return edges.isEmpty();
		}
		BitSet edgesBitmap = new BitSet(m);
		for (int e : edges) {
			if (!ig.edges().contains(e))
				throw new IllegalArgumentException("invalid edge index " + e);
			if (edgesBitmap.get(e))
				throw new IllegalArgumentException(
						"edge with index " + e + " is included more than once in the spanning tree");
			edgesBitmap.set(e);
		}

		int[] root = new int[n];
		Arrays.fill(root, -1);
		LongPriorityQueue queue = new FIFOQueueLongNoReduce();
		for (int r = 0; r < n; r++) {
			if (root[r] != -1)
				continue;
			root[r] = r;
			queue.enqueue(JGAlgoUtils.longCompose(r, -1));
			while (!queue.isEmpty()) {
				long l = queue.dequeueLong();
				int u = JGAlgoUtils.long2low(l);
				int parentEdge = JGAlgoUtils.long2high(l);
				for (EdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (!edgesBitmap.get(e) || e == parentEdge)
						continue;
					int v = eit.target();
					if (root[v] == r)
						return false; /* cycle */
					root[v] = r;
					queue.enqueue(JGAlgoUtils.longCompose(v, e));
				}
			}
		}
		for (int e = 0; e < m; e++)
			if (!edgesBitmap.get(e) && root[ig.edgeSource(e)] != root[ig.edgeTarget(e)])
				return false; /* two connected components of the given forest could have been connected */
		return true;
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
