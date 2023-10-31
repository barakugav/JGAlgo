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

import java.util.Iterator;
import java.util.List;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Depth first search (DFS) iterator.
 * <p>
 * The DFS iterator is used to iterate over the vertices of a graph is a depth first manner, namely it explore as far as
 * possible along each branch before backtracking. The iterator will visit every vertex \(v\) for which there is a path
 * from the source(s) to \(v\). Each such vertex will be visited exactly once.
 * <p>
 * The graph should not be modified during the DFS iteration.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * String sourceVertex = ...;
 * for (Dfs.Iter<String, Integer> iter = Dfs.newInstance(g, sourceVertex); iter.hasNext();) {
 *     String v = iter.next();
 *     List<E> edgePath = iter.edgePath();
 *     System.out.println("Reached vertex " + v + " using the edges: " + edgePath.edges());
 * }
 * }</pre>
 *
 * @see    Bfs
 * @see    <a href="https://en.wikipedia.org/wiki/Depth-first_search">Wikipedia</a>
 * @author Barak Ugav
 */
public interface Dfs {

	/**
	 * Create a DFS iterator.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a DFS iterator that iterate over the vertices of the graph
	 */
	@SuppressWarnings("unchecked")
	static <V, E> Dfs.Iter<V, E> newInstance(Graph<V, E> g, V source) {
		if (g instanceof IntGraph)
			return (Dfs.Iter<V, E>) newInstance((IntGraph) g, ((Integer) source).intValue());

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		IndexIdMap<E> eiMap = g.indexGraphEdgesMap();

		int iSource = viMap.idToIndex(source);
		Dfs.IntIter indexIter = new DfsIterImpl(iGraph, iSource);
		return new DfsIterImpl.ObjDfsFromIndexDfs<>(indexIter, viMap, eiMap);
	}

	/**
	 * Create a DFS iterator for an int graph.
	 *
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a DFS iterator that iterate over the vertices of the graph
	 */
	static Dfs.IntIter newInstance(IntGraph g, int source) {
		if (g instanceof IndexGraph)
			return new DfsIterImpl((IndexGraph) g, source);

		IndexGraph iGraph = g.indexGraph();
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		IndexIntIdMap eiMap = g.indexGraphEdgesMap();

		int iSource = viMap.idToIndex(source);
		Dfs.IntIter indexIter = new DfsIterImpl(iGraph, iSource);
		return new DfsIterImpl.IntDfsFromIndexDfs(indexIter, viMap, eiMap);
	}

	/**
	 * A DFS iterator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	static interface Iter<V, E> extends Iterator<V> {

		/**
		 * Check whether there is more vertices to iterate over.
		 */
		@Override
		public boolean hasNext();

		/**
		 * Advance the iterator and return a vertex that was not visited by the iterator yet.
		 */
		@Override
		public V next();

		/**
		 * Get the path from the source to the last vertex returned by {@link nextInt}.
		 * <p>
		 * The behavior is undefined if {@link nextInt} was not called yet.
		 *
		 * @return list of edges forming a path from the source to the last vertex returned by {@link nextInt}. The
		 *         returned list should not be modified by the user.
		 */
		public List<E> edgePath();
	}

	/**
	 * A DFS iterator for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IntIter extends Dfs.Iter<Integer, Integer>, IntIterator {

		/**
		 * Advance the iterator and return a vertex that was not visited by the iterator yet.
		 */
		@Override
		public int nextInt();

		@Deprecated
		@Override
		default Integer next() {
			return Integer.valueOf(nextInt());
		}

		@Override
		public IntList edgePath();
	}
}
