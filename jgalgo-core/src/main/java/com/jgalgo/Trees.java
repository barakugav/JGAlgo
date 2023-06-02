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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Static methods class for tree graphs.
 *
 * @author Barak Ugav
 */
public class Trees {

	private Trees() {}

	/**
	 * Check if an undirected graph is a tree.
	 * <p>
	 * An undirected graph is a tree if its connected and contains no cycle, therefore \(n-1\) edges.
	 * <p>
	 * This method runs in linear time.
	 *
	 * @param  g                        a graph
	 * @return                          {@code true} if the graph is a tree, else {@code false}
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	public static boolean isTree(Graph g) {
		ArgumentCheck.onlyUndirected(g);
		return g.vertices().isEmpty() ? true : isTree(g, g.vertices().iterator().nextInt());
	}

	/**
	 * Check if a graph is a tree rooted as some vertex.
	 * <p>
	 * For undirected graphs, a graph which is a tree rooted at some vertex can be rooted at any other vertex and will
	 * always be a tree. For directed graphs however this is not true. A directed graph might be a tree rooted at some
	 * vertex, but will no be connected if we root it at another vertex.
	 * <p>
	 * This method runs in linear time.
	 *
	 * @param  g    a graph
	 * @param  root a root vertex
	 * @return      {@code true} if the graph is a tree rooted at {@code root}, else {@code false}.
	 */
	public static boolean isTree(Graph g, int root) {
		return isForest(g, IntIterators.singleton(root));
	}

	/**
	 * Check if a graph is a forest.
	 * <p>
	 * A forest is a graph which can be divided into trees, which is equivalent to saying a graph with no cycles.
	 * <p>
	 * This method runs in linear time.
	 *
	 * @param  g a graph
	 * @return   {@code true} if the graph is a forest, else {@code false}
	 */
	public static boolean isForest(Graph g) {
		return isForest(g, g.vertices().iterator(), true);
	}

	/**
	 * Check if a graph is a forest rooted at the given roots.
	 * <p>
	 * A forest is a graph which can be divided into trees, which is equivalent to saying a graph with no cycles. For a
	 * graph to be a forest rooted at some given roots, all vertices must be reachable from the roots, and the roots can
	 * not be reached from another root.
	 * <p>
	 * This method runs in linear time.
	 *
	 * @param  g     a graph
	 * @param  roots a set of roots
	 * @return       true if the graph is a forest rooted at the given roots.
	 */
	private static boolean isForest(Graph g, IntIterator roots) {
		return isForest(g, roots, false);
	}

	private static boolean isForest(Graph g, IntIterator roots, boolean allowVisitedRoot) {
		if (g instanceof IndexGraph)
			return isForest((IndexGraph) g, roots, allowVisitedRoot);
		IndexGraph iGraph = g.indexGraph();
		IndexGraphMap viMap = g.indexGraphVerticesMap();
		roots = new IndexGraphMapUtils.IndexIteratorFromIterator(roots, viMap);
		return isForest(iGraph, roots, allowVisitedRoot);
	}

	private static boolean isForest(IndexGraph g, IntIterator roots, boolean allowVisitedRoot) {
		int n = g.vertices().size();
		if (n == 0)
			return true;
		boolean directed = g.getCapabilities().directed();

		BitSet visited = new BitSet(n);
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

				for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.target();
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

	interface TreeNode<Node extends TreeNode<Node>> {

		Node parent();

		Node next();

		Node prev();

		Node child();

		void setParent(Node x);

		void setNext(Node x);

		void setPrev(Node x);

		void setChild(Node x);

	}

	static class TreeNodeImpl<Node extends TreeNodeImpl<Node>> implements TreeNode<Node> {

		Node parent;
		Node next;
		Node prev;
		Node child;

		@Override
		public Node parent() {
			return parent;
		}

		@Override
		public Node next() {
			return next;
		}

		@Override
		public Node prev() {
			return prev;
		}

		@Override
		public Node child() {
			return child;
		}

		@Override
		public void setParent(Node x) {
			parent = x;
		}

		@Override
		public void setNext(Node x) {
			next = x;
		}

		@Override
		public void setPrev(Node x) {
			prev = x;
		}

		@Override
		public void setChild(Node x) {
			child = x;
		}
	}

	static <Node extends TreeNode<Node>> void clear(Node root, Consumer<? super Node> finalizer) {
		for (Node p = root;;) {
			while (p.child() != null) {
				p = p.child();
				while (p.next() != null)
					p = p.next();
			}

			Node prev;
			if (p.prev() != null) {
				prev = p.prev();
				prev.setNext(null);
				p.setPrev(null);
			} else if (p.parent() != null) {
				prev = p.parent();
				prev.setChild(null);
				p.setParent(null);
			} else {
				prev = null;
			}

			finalizer.accept(p);
			if (prev == null)
				break;
			p = prev;
		}
	}

	static class PreOrderIter<Node extends TreeNode<Node>> implements Iterator<Node> {

		private Node p;

		PreOrderIter(Node p) {
			reset(p);
		}

		void reset(Node p) {
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

		@Override
		public Node next() {
			if (!hasNext())
				throw new NoSuchElementException();
			final Node ret = p;

			Node next;
			if ((next = ret.child()) != null) {
				p = next;
			} else {
				Node p0 = ret;
				do {
					if ((next = p0.next()) != null) {
						p0 = next;
						break;
					}
				} while ((p0 = p0.parent()) != null);
				p = p0;
			}

			return ret;
		}

	}

	static class PostOrderIter<N extends TreeNode<N>> implements Iterator<N> {

		private N p;

		PostOrderIter(N p) {
			reset(p);
		}

		void reset(N p) {
			for (N next; (next = p.child()) != null;)
				p = next;
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

		@Override
		public N next() {
			if (!hasNext())
				throw new NoSuchElementException();
			final N ret = p;

			N next;
			if ((next = ret.next()) != null) {
				/* lower child */
				for (N child; (child = next.child()) != null;)
					next = child;
				p = next;
			} else {
				p = ret.parent();
			}

			return ret;
		}

	}

}
