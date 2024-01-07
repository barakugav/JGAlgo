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
import java.util.Collection;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.ReferenceableHeap;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.FIFOQueueLongNoReduce;
import com.jgalgo.internal.util.IntAdapters;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

/**
 * Minimum spanning tree algorithm.
 *
 * <p>
 * A spanning tree is an edge sub set of the graph edges which form a tree and connect (span) all the vertices of the
 * graph. A minimum spanning tree (MST) is a spanning tree with the minimum edge weights sum over all spanning trees.
 *
 * <p>
 * If a <b>maximum</b> spanning tree is needed, the edge weights can be negated and the MST algorithm can be used to
 * compute the maximum spanning tree.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Minimum_spanning_tree">Wikipedia</a>
 * @see    MinimumDirectedSpanningTree
 * @author Barak Ugav
 */
public interface MinimumSpanningTree {

	/**
	 * Compute the minimum spanning tree (MST) of a given graph.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link MinimumSpanningTree.IResult} object will be returned. In that case,
	 * its better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @param  w   an edge weight function
	 * @return     a result object containing all the edges of the computed spanning tree, which there are \(n-1\) of
	 *             them (or less, forming a forest if the graph is not connected)
	 */
	<V, E> MinimumSpanningTree.Result<V, E> computeMinimumSpanningTree(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * A result object for {@link MinimumSpanningTree} computation.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	@SuppressWarnings("unused")
	static interface Result<V, E> {

		/**
		 * Get all the edges that form the spanning tree.
		 *
		 * @return the set of MST edges.
		 */
		Set<E> edges();
	}

	/**
	 * A result object for {@link MinimumSpanningTree} computation for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends Result<Integer, Integer> {

		@Override
		IntSet edges();
	}

	/**
	 * Check whether a given set of edges is a spanning tree of a given graph.
	 *
	 * <p>
	 * A set of edges is spanning tree if it is a tree and connects all the vertices of the graph. Specifically, if the
	 * graph is not empty, the number of edges must be \(n-1\) where \(n\) denote the number of vertices in the graph.
	 * The edge set should not contain any duplicate edges.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IntCollection} as {@code edges} to avoid
	 * boxing/unboxing.
	 *
	 * @param  <V>   the vertices type
	 * @param  <E>   the edges type
	 * @param  g     a graph
	 * @param  edges a set of edges that should form a spanning tree
	 * @return       {@code true} if the given set of edges is a spanning tree of the given graph, {@code false}
	 *               otherwise
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isSpanningTree(Graph<V, E> g, Collection<E> edges) {
		Assertions.onlyUndirected(g);
		IndexGraph ig;
		IntCollection edges0;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			edges0 = IntAdapters.asIntCollection((Collection<Integer>) edges);
		} else {
			ig = g.indexGraph();
			edges0 = IndexIdMaps.idToIndexCollection(edges, g.indexGraphEdgesMap());
		}
		final int m = ig.edges().size();
		final int n = ig.vertices().size();
		if (n == 0) {
			assert m == 0;
			return edges0.isEmpty();
		}
		if (edges0.size() != n - 1)
			return false;
		Bitmap edgesBitmap = new Bitmap(m);
		for (int e : edges0) {
			if (!ig.edges().contains(e))
				throw new IllegalArgumentException("invalid edge index " + e);
			if (edgesBitmap.get(e))
				throw new IllegalArgumentException(
						"edge with index " + e + " is included more than once in the spanning tree");
			edgesBitmap.set(e);
		}

		/* perform a BFS from some vertex using only the spanning tree edges */
		Bitmap visited = new Bitmap(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		visited.set(0);
		queue.enqueue(0);
		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			for (IEdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (!edgesBitmap.get(e))
					continue;
				int v = eit.targetInt();
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
	 *
	 * <p>
	 * A set of edges is spanning forest if it is a forest (do not contains cycles) which connected any pair of vertices
	 * that are connected in the original graph, namely its connected components are identical to the connected
	 * components of the original graph. Specifically, the number of edges must be \(n-c\) where \(n\) denote the number
	 * of vertices in the graph and \(c\) denote the number of connected components in the graph. The edge set should
	 * not contain any duplicate edges.
	 *
	 * @param  <V>   the vertices type
	 * @param  <E>   the edges type
	 * @param  g     a graph
	 * @param  edges a set of edges that should form a spanning forest
	 * @return       {@code true} if the given set of edges is a spanning forest of the given graph, {@code false}
	 *               otherwise
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isSpanningForest(Graph<V, E> g, Collection<E> edges) {
		Assertions.onlyUndirected(g);
		IndexGraph ig;
		IntCollection edges0;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			edges0 = IntAdapters.asIntCollection((Collection<Integer>) edges);
		} else {
			ig = g.indexGraph();
			edges0 = IndexIdMaps.idToIndexCollection(edges, g.indexGraphEdgesMap());
		}
		final int m = ig.edges().size();
		final int n = ig.vertices().size();
		if (n == 0) {
			assert m == 0;
			return edges0.isEmpty();
		}
		Bitmap edgesBitmap = new Bitmap(m);
		for (int e : edges0) {
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
			queue.enqueue(JGAlgoUtils.longPack(r, -1));
			while (!queue.isEmpty()) {
				long l = queue.dequeueLong();
				int u = JGAlgoUtils.long2low(l);
				int parentEdge = JGAlgoUtils.long2high(l);
				for (IEdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (!edgesBitmap.get(e) || e == parentEdge)
						continue;
					int v = eit.targetInt();
					if (root[v] == r)
						return false; /* cycle */
					root[v] = r;
					queue.enqueue(JGAlgoUtils.longPack(v, e));
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
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumSpanningTree} object. The
	 * {@link MinimumSpanningTree.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MinimumSpanningTree}
	 */
	static MinimumSpanningTree newInstance() {
		return builder().build();
	}

	/**
	 * Create a new minimum spanning tree algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MinimumSpanningTree} objects
	 */
	static MinimumSpanningTree.Builder builder() {
		return new MinimumSpanningTree.Builder() {
			String impl;
			private ReferenceableHeap.Builder heapBuilder;

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
			public void setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					case "heap-builder":
						heapBuilder = (ReferenceableHeap.Builder) value;
						break;
					default:
						MinimumSpanningTree.Builder.super.setOption(key, value);
				}
			}
		};
	}

	/**
	 * A builder for {@link MinimumSpanningTree} objects.
	 *
	 * @see    MinimumSpanningTree#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for minimum spanning tree computation.
		 *
		 * @return a new minimum spanning tree algorithm
		 */
		MinimumSpanningTree build();
	}

}
