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
import java.util.Iterator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

/**
 * Static methods class for tree graphs.
 *
 * @author Barak Ugav
 */
public class Trees {

	private Trees() {}

	/**
	 * Check if an undirected graph is a tree.
	 *
	 * <p>
	 * An undirected graph is a tree if its connected and contains no cycle, therefore \(n-1\) edges.
	 *
	 * <p>
	 * This method runs in linear time.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @return                          {@code true} if the graph is a tree, else {@code false}
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	public static <V, E> boolean isTree(Graph<V, E> g) {
		Assertions.Graphs.onlyUndirected(g);
		return g.vertices().isEmpty() ? true : isTree(g, g.vertices().iterator().next());
	}

	/**
	 * Check if a graph is a tree rooted as some vertex.
	 *
	 * <p>
	 * For undirected graphs, a graph which is a tree rooted at some vertex can be rooted at any other vertex and will
	 * always be a tree. For directed graphs however this is not true. A directed graph might be a tree rooted at some
	 * vertex, but will no be connected if we root it at another vertex.
	 *
	 * <p>
	 * This method runs in linear time.
	 *
	 * @param  <V>  the vertices type
	 * @param  <E>  the edges type
	 * @param  g    a graph
	 * @param  root a root vertex
	 * @return      {@code true} if the graph is a tree rooted at {@code root}, else {@code false}.
	 */
	public static <V, E> boolean isTree(Graph<V, E> g, V root) {
		return isForest(g, ObjectIterators.singleton(root));
	}

	/**
	 * Check if a graph is a forest.
	 *
	 * <p>
	 * A forest is a graph which can be divided into trees, which is equivalent to saying a graph with no cycles.
	 *
	 * <p>
	 * This method runs in linear time.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     {@code true} if the graph is a forest, else {@code false}
	 */
	public static <V, E> boolean isForest(Graph<V, E> g) {
		return isForest(g, g.vertices().iterator(), true);
	}

	/**
	 * Check if a graph is a forest rooted at the given roots.
	 *
	 * <p>
	 * A forest is a graph which can be divided into trees, which is equivalent to saying a graph with no cycles. For a
	 * graph to be a forest rooted at some given roots, all vertices must be reachable from the roots, and the roots can
	 * not be reached from another root.
	 *
	 * <p>
	 * This method runs in linear time.
	 *
	 * @param  <V>   the vertices type
	 * @param  <E>   the edges type
	 * @param  g     a graph
	 * @param  roots a set of roots
	 * @return       true if the graph is a forest rooted at the given roots.
	 */
	private static <V, E> boolean isForest(Graph<V, E> g, Iterator<V> roots) {
		return isForest(g, roots, false);
	}

	@SuppressWarnings("unchecked")
	private static <V, E> boolean isForest(Graph<V, E> g, Iterator<V> roots, boolean allowVisitedRoot) {
		if (g instanceof IndexGraph) {
			IntIterator roots0 = IntAdapters.asIntIterator((Iterator<Integer>) roots);
			return isForest((IndexGraph) g, roots0, allowVisitedRoot);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IntIterator roots0 = IndexIdMaps.idToIndexIterator(roots, viMap);
			return isForest(iGraph, roots0, allowVisitedRoot);
		}
	}

	private static boolean isForest(IndexGraph g, IntIterator roots, boolean allowVisitedRoot) {
		int n = g.vertices().size();
		if (n == 0)
			return true;
		boolean directed = g.isDirected();

		Bitmap visited = new Bitmap(n);
		int[] parent = new int[n];
		Arrays.fill(parent, -1);

		IntStack stack = new IntArrayList();
		int visitedCount = 0;

		while (roots.hasNext()) {
			int root = roots.nextInt();
			if (visited.get(root)) {
				if (allowVisitedRoot)
					continue;
				return false;
			}

			stack.push(root);
			visited.set(root);

			while (!stack.isEmpty()) {
				int u = stack.popInt();
				visitedCount++;

				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (!directed && v == parent[u])
						continue;
					if (visited.get(v))
						return false;
					visited.set(v);
					stack.push(v);
					parent[v] = u;
				}
			}
		}

		return visitedCount == n;
	}

}
