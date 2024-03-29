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
package com.jgalgo.alg.span;

import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueLongNoReduce;
import com.jgalgo.internal.util.IntAdapters;
import com.jgalgo.internal.util.IntPair;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

/**
 * An algorithm for the Steiner tree problem.
 *
 * <p>
 * The Steiner tree problem is a generalization of the minimum spanning tree problem. Given a graph \(G=(V,E)\) and a
 * set of terminals vertices \(T \subseteq V\), the Steiner tree problem is to find a minimum weight tree that spans all
 * the terminals. The tree may contain additional vertices that are not terminals, which are usually called Steiner
 * vertices. The Steiner tree problem is NP-hard, therefore algorithms implementing this interface are heuristics, and
 * do not guarantee to find the optimal solution, only a solution with bounded approximation factor.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Steiner_tree_problem">Wikipedia</a>
 * @see    MinimumSpanningTree
 * @author Barak Ugav
 */
public interface SteinerTreeAlgo {

	/**
	 * Compute the minimum Steiner tree of a given graph.
	 *
	 * <p>
	 * The algorithm with search for the minimum Steiner tree that spans all the terminals with respect to the given
	 * edge weight function. The tree may contain additional vertices that are not terminals.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link SteinerTreeAlgo.IResult} object will be returned. In that case, its
	 * better to pass a {@link IWeightFunction} as {@code w}, and {@link IntCollection} as {@code terminals} to avoid
	 * boxing/unboxing.
	 *
	 * @param  <V>       the vertices type
	 * @param  <E>       the edges type
	 * @param  g         a graph
	 * @param  w         an edge weight function
	 * @param  terminals a set of terminals vertices
	 * @return           a result object containing all the edges of the computed tree
	 */
	<V, E> SteinerTreeAlgo.Result<V, E> computeSteinerTree(Graph<V, E> g, WeightFunction<E> w, Collection<V> terminals);

	/**
	 * A result object for {@link SteinerTreeAlgo} computation.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	@SuppressWarnings("unused")
	static interface Result<V, E> {

		/**
		 * Get all the edges that form the Steiner tree.
		 *
		 * @return a collection of the Steiner tree edges.
		 */
		Set<E> edges();
	}

	/**
	 * A result object for {@link SteinerTreeAlgo} computation for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends SteinerTreeAlgo.Result<Integer, Integer> {

		@Override
		IntSet edges();
	}

	/**
	 * Check whether a given set of edges is a valid Steiner tree for a given graph and terminals.
	 *
	 * <p>
	 * A set of edges is a valid Steiner tree if it spans all the terminals, does not contain any cycles, form a single
	 * connected components, and there are no non-terminal leaves in the tree.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IntCollection} as {@code terminals} and
	 * {@code edges} to avoid boxing/unboxing.
	 *
	 * @param  <V>       the vertices type
	 * @param  <E>       the edges type
	 * @param  g         a graph
	 * @param  terminals a set of terminals vertices
	 * @param  edges     a set of edges
	 * @return           {@code true} if the given set of edges is a valid Steiner tree
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isSteinerTree(Graph<V, E> g, Collection<V> terminals, Collection<E> edges) {
		Assertions.onlyUndirected(g);
		IndexGraph ig;
		IntCollection terminals0;
		IntCollection edges0;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			terminals0 = IntAdapters.asIntCollection((Collection<Integer>) terminals);
			edges0 = IntAdapters.asIntCollection((Collection<Integer>) edges);

		} else {
			ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			terminals0 = IndexIdMaps.idToIndexCollection(terminals, viMap);
			edges0 = IndexIdMaps.idToIndexCollection(edges, eiMap);
		}

		final int m = ig.edges().size();
		final int n = ig.vertices().size();
		if (n == 0) {
			assert m == 0;
			return terminals0.isEmpty() && edges0.isEmpty();
		}
		if (terminals0.isEmpty() || terminals0.size() == 1)
			return edges0.isEmpty();

		Bitmap edgesBitmap = new Bitmap(m);
		for (int e : edges0) {
			if (!ig.edges().contains(e))
				throw new IllegalArgumentException("invalid edge index " + e);
			if (edgesBitmap.get(e))
				throw new IllegalArgumentException("edge with index " + e + " is included more than once in the tree");
			edgesBitmap.set(e);
		}

		Bitmap visited = new Bitmap(n);
		LongPriorityQueue queue = new FIFOQueueLongNoReduce();
		int root = terminals0.iterator().nextInt();
		visited.set(root);
		queue.enqueue(IntPair.of(root, -1));
		while (!queue.isEmpty()) {
			long l = queue.dequeueLong();
			int u = IntPair.first(l);
			int parentEdge = IntPair.second(l);
			for (IEdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (!edgesBitmap.get(e) || e == parentEdge)
					continue;
				int v = eit.targetInt();
				if (visited.get(v))
					return false; /* cycle */
				visited.set(v);
				queue.enqueue(IntPair.of(v, e));
			}
		}
		for (int t : terminals0)
			if (!visited.get(t))
				return false; /* not all terminals are connected */

		/* check for non-terminal leaves */
		Bitmap isTerminal = visited;
		isTerminal.clear();
		for (int t : terminals0) {
			if (isTerminal.get(t))
				throw new IllegalArgumentException("Duplicate terminal: " + t);
			isTerminal.set(t);
		}
		int[] degree = new int[n];
		for (int e : edges0) {
			degree[ig.edgeSource(e)]++;
			degree[ig.edgeTarget(e)]++;
		}
		for (int v : range(n))
			if (degree[v] == 1 && !isTerminal.get(v))
				return false; /* non-terminal leaf */

		return true;
	}

	/**
	 * Create a new Steiner tree algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link SteinerTreeAlgo} object.
	 *
	 * @return a default implementation of {@link SteinerTreeAlgo}
	 */
	static SteinerTreeAlgo newInstance() {
		return new SteinerTreeMehlhorn();
	}

}
