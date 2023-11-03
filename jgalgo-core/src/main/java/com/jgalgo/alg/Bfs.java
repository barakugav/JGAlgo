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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Bread first search (BFS) iterator.
 * <p>
 * The BFS iterator is used to iterate over the vertices of a graph in a bread first manner, namely by the cardinality
 * distance of the vertices from some source(s) vertex. The iterator will visit every vertex \(v\) for which there is a
 * path from the source(s) to \(v\). Each such vertex will be visited exactly once.
 * <p>
 * The graph should not be modified during the BFS iteration.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * String sourceVertex = ...;
 * for (Bfs.Iter<String, Integer> iter = Bfs.newInstance(g, sourceVertex); iter.hasNext();) {
 *     String v = iter.next();
 *     Integer e = iter.inEdge();
 *     int layer = iter.layer();
 *     System.out.println("Reached vertex " + v + " at layer " + layer + " using edge " + e);
 * }
 * }</pre>
 *
 * @see    Dfs
 * @see    <a href= "https://en.wikipedia.org/wiki/Breadth-first_search">Wikipedia</a>
 * @author Barak Ugav
 */
public interface Bfs {

	/**
	 * Create a BFS iterator.
	 * <p>
	 * If an {@link IntGraph} is passed as an argument {@link Bfs.IntIter} is returned.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> Bfs.Iter<V, E> newInstance(Graph<V, E> g, V source) {
		if (g instanceof IntGraph)
			return (Bfs.Iter<V, E>) newInstance((IntGraph) g, ((Integer) source).intValue());
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		Bfs.IntIter indexBFS = new BfsIterImpl.Forward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.ObjBfsFromIndexBfs<>(g, indexBFS);
	}

	/**
	 * Create a backward BFS iterator.
	 * <p>
	 * The regular BFS uses the out-edges of each vertex to explore its neighbors, while the <i>backward</i> BFS uses
	 * the in-edges to do so.
	 * <p>
	 * If an {@link IntGraph} is passed as an argument {@link Bfs.IntIter} is returned.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph using the in-edges
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> Bfs.Iter<V, E> newInstanceBackward(Graph<V, E> g, V source) {
		if (g instanceof IntGraph)
			return (Bfs.Iter<V, E>) newInstanceBackward((IntGraph) g, ((Integer) source).intValue());
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		Bfs.IntIter indexBFS = new BfsIterImpl.Backward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.ObjBfsFromIndexBfs<>(g, indexBFS);
	}

	/**
	 * Create a BFS iterator in an int graph.
	 *
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph
	 */
	public static Bfs.IntIter newInstance(IntGraph g, int source) {
		if (g instanceof IndexGraph)
			return new BfsIterImpl.Forward((IndexGraph) g, source);
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		Bfs.IntIter indexBFS = new BfsIterImpl.Forward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.IntBfsFromIndexBfs(g, indexBFS);
	}

	/**
	 * Create a backward BFS iterator in an int graph.
	 * <p>
	 * The regular BFS uses the out-edges of each vertex to explore its neighbors, while the <i>backward</i> BFS uses
	 * the in-edges to do so.
	 *
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph using the in-edges
	 */
	public static Bfs.IntIter newInstanceBackward(IntGraph g, int source) {
		if (g instanceof IndexGraph)
			return new BfsIterImpl.Backward((IndexGraph) g, source);
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		Bfs.IntIter indexBFS = new BfsIterImpl.Backward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.IntBfsFromIndexBfs(g, indexBFS);
	}

	/**
	 * A BFS iterator.
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
		 * Get the edge that led to the last vertex returned by {@link #next()}.
		 * <p>
		 * The behavior is undefined if {@link #next()} was not called yet.
		 *
		 * @return the edge that led to the last vertex returned by {@link #next()}
		 */
		public E lastEdge();

		/**
		 * Get the layer of the last vertex returned by {@link #next()}.
		 * <p>
		 * The layer of a vertex is the cardinality distance, the number of edges in the path, from the source(s) to the
		 * vertex.
		 * <p>
		 * The behavior is undefined if {@link #next()} was not called yet.
		 *
		 * @return the layer of the last vertex returned by {@link #next()}.
		 */
		public int layer();
	}

	/**
	 * A BFS iterator for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IntIter extends Bfs.Iter<Integer, Integer>, IntIterator {

		/**
		 * Advance the iterator and return a vertex that was not visited by the iterator yet.
		 */
		@Override
		public int nextInt();

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		default Integer next() {
			return IntIterator.super.next();
		}

		/**
		 * Get the edge that led to the last vertex returned by {@link #nextInt()}.
		 * <p>
		 * The behavior is undefined if {@link #nextInt()} was not called yet.
		 *
		 * @return the edge that led to the last vertex returned by {@link #nextInt()}
		 */
		public int lastEdgeInt();

		@Deprecated
		@Override
		default Integer lastEdge() {
			return Integer.valueOf(lastEdgeInt());
		}
	}

}
