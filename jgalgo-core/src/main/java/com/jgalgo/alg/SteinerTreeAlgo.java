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

import java.util.BitSet;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueLongNoReduce;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

/**
 * An algorithm for the Steiner tree problem.
 * <p>
 * The Steiner tree problem is a generalization of the minimum spanning tree problem. Given a graph \(G=(V,E)\) and a
 * set of terminals vertices \(T \subseteq V\), the Steiner tree problem is to find a minimum weight tree that spans all
 * the terminals. The tree may contain additional vertices that are not terminals, which are usually called Steiner
 * vertices. The Steiner tree problem is NP-hard, therefore algorithms implementing this interface are heuristics, and
 * do not guarantee to find the optimal solution, only a solution with bounded approximation factor.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Steiner_tree_problem">Wikipedia</a>
 * @see    MinimumSpanningTree
 * @author Barak Ugav
 */
public interface SteinerTreeAlgo {

	/**
	 * Compute the minimum Steiner tree of a given graph.
	 * <p>
	 * The algorithm with search for the minimum Steiner tree that spans all the terminals with respect to the given
	 * edge weight function. The tree may contain additional vertices that are not terminals.
	 *
	 * @param  g         a graph
	 * @param  w         an edge weight function
	 * @param  terminals a set of terminals vertices
	 * @return           a result object containing all the edges of the computed tree
	 */
	SteinerTreeAlgo.Result computeSteinerTree(IntGraph g, IWeightFunction w, IntCollection terminals);

	/**
	 * A result object for {@link SteinerTreeAlgo} computation.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get all the edges that form the Steiner tree.
		 *
		 * @return a collection of the Steiner tree edges.
		 */
		IntCollection edges();
	}

	/**
	 * Check whether a given set of edges is a valid Steiner tree for a given graph and terminals.
	 * <p>
	 * A set of edges is a valid Steiner tree if it spans all the terminals, does not contain any cycles, form a single
	 * connected components, and there are no non-terminal leaves in the tree.
	 *
	 * @param  g         a graph
	 * @param  terminals a set of terminals vertices
	 * @param  edges     a set of edges
	 * @return           {@code true} if the given set of edges is a valid Steiner tree
	 */
	static boolean isSteinerTree(IntGraph g, IntCollection terminals, IntCollection edges) {
		Assertions.Graphs.onlyUndirected(g);
		IndexGraph ig;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
		} else {
			ig = g.indexGraph();
			terminals = IndexIdMaps.idToIndexCollection(terminals, g.indexGraphVerticesMap());
			edges = IndexIdMaps.idToIndexCollection(edges, g.indexGraphEdgesMap());
		}
		final int m = ig.edges().size();
		final int n = ig.vertices().size();
		if (n == 0) {
			assert m == 0;
			return terminals.isEmpty() && edges.isEmpty();
		}
		if (terminals.isEmpty() || terminals.size() == 1)
			return edges.isEmpty();

		BitSet edgesBitmap = new BitSet(m);
		for (int e : edges) {
			if (!ig.edges().contains(e))
				throw new IllegalArgumentException("invalid edge index " + e);
			if (edgesBitmap.get(e))
				throw new IllegalArgumentException("edge with index " + e + " is included more than once in the tree");
			edgesBitmap.set(e);
		}

		BitSet visited = new BitSet(n);
		LongPriorityQueue queue = new FIFOQueueLongNoReduce();
		int root = terminals.iterator().nextInt();
		visited.set(root);
		queue.enqueue(JGAlgoUtils.longPack(root, -1));
		while (!queue.isEmpty()) {
			long l = queue.dequeueLong();
			int u = JGAlgoUtils.long2low(l);
			int parentEdge = JGAlgoUtils.long2high(l);
			for (IEdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (!edgesBitmap.get(e) || e == parentEdge)
					continue;
				int v = eit.targetInt();
				if (visited.get(v))
					return false; /* cycle */
				visited.set(v);
				queue.enqueue(JGAlgoUtils.longPack(v, e));
			}
		}
		for (int t : terminals)
			if (!visited.get(t))
				return false; /* not all terminals are connected */

		/* check for non-terminal leaves */
		BitSet isTerminal = visited;
		isTerminal.clear();
		for (int t : terminals) {
			if (isTerminal.get(t))
				throw new IllegalArgumentException("Duplicate terminal: " + t);
			isTerminal.set(t);
		}
		int[] degree = new int[n];
		for (int e : edges) {
			degree[ig.edgeSource(e)]++;
			degree[ig.edgeTarget(e)]++;
		}
		for (int v = 0; v < n; v++)
			if (degree[v] == 1 && !isTerminal.get(v))
				return false; /* non-terminal leaf */

		return true;
	}

	/**
	 * Create a new Steiner tree algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link SteinerTreeAlgo} object. The
	 * {@link SteinerTreeAlgo.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link SteinerTreeAlgo}
	 */
	static SteinerTreeAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new Steiner tree algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link SteinerTreeAlgo} objects
	 */
	static SteinerTreeAlgo.Builder newBuilder() {
		return SteinerTreeMehlhorn::new;
	}

	/**
	 * A builder for {@link SteinerTreeAlgo} objects.
	 *
	 * @see    SteinerTreeAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for Steiner tree computation.
		 *
		 * @return a new Steiner tree algorithm
		 */
		SteinerTreeAlgo build();

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
		default SteinerTreeAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
