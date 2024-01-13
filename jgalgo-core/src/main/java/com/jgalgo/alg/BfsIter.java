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
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Bread first search (BFS) iterator.
 *
 * <p>
 * The BFS iterator is used to iterate over the vertices of a graph in a bread first manner, namely by the cardinality
 * distance of the vertices from some source(s) vertex. The iterator will visit every vertex \(v\) for which there is a
 * path from the source(s) to \(v\). Each such vertex will be visited exactly once.
 *
 * <p>
 * The graph should not be modified during the BFS iteration.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * String sourceVertex = ...;
 * for (BfsIter<String, Integer> iter = BfsIter.newInstance(g, sourceVertex); iter.hasNext();) {
 *     String v = iter.next();
 *     Integer e = iter.inEdge();
 *     int layer = iter.layer();
 *     System.out.println("Reached vertex " + v + " at layer " + layer + " using edge " + e);
 * }
 * }</pre>
 *
 * @see        DfsIter
 * @see        <a href= "https://en.wikipedia.org/wiki/Breadth-first_search">Wikipedia</a>
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface BfsIter<V, E> extends Iterator<V> {

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
	 *
	 * <p>
	 * The behavior is undefined if {@link #next()} was not called yet.
	 *
	 * @return the edge that led to the last vertex returned by {@link #next()}
	 */
	public E lastEdge();

	/**
	 * Get the layer of the last vertex returned by {@link #next()}.
	 *
	 * <p>
	 * The layer of a vertex is the cardinality distance, the number of edges in the path, from the source(s) to the
	 * vertex.
	 *
	 * <p>
	 * The behavior is undefined if {@link #next()} was not called yet.
	 *
	 * @return the layer of the last vertex returned by {@link #next()}.
	 */
	public int layer();

	/**
	 * A BFS iterator for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface Int extends BfsIter<Integer, Integer>, IntIterator {

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
		 *
		 * <p>
		 * The behavior is undefined if {@link #nextInt()} was not called yet.
		 *
		 * @return the edge that led to the last vertex returned by {@link #nextInt()}
		 */
		public int lastEdgeInt();

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #lastEdgeInt()} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer lastEdge() {
			int e = lastEdgeInt();
			return e == -1 ? null : Integer.valueOf(e);
		}
	}

	/**
	 * Create a BFS iterator.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument {@link BfsIter.Int} is returned.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> BfsIter<V, E> newInstance(Graph<V, E> g, V source) {
		if (g instanceof IntGraph)
			return (BfsIter<V, E>) newInstance((IntGraph) g, ((Integer) source).intValue());
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		BfsIter.Int indexBfs = new BfsIterImpl.Forward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.ObjBfsFromIndexBfs<>(g, indexBfs);
	}

	/**
	 * Create a BFS iterator in an int graph.
	 *
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph
	 */
	public static BfsIter.Int newInstance(IntGraph g, int source) {
		if (g instanceof IndexGraph)
			return new BfsIterImpl.Forward((IndexGraph) g, source);
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		BfsIter.Int indexBfs = new BfsIterImpl.Forward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.IntBfsFromIndexBfs(g, indexBfs);
	}

	/**
	 * Create a backward BFS iterator.
	 *
	 * <p>
	 * The regular BFS uses the out-edges of each vertex to explore its neighbors, while the <i>backward</i> BFS uses
	 * the in-edges to do so.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument {@link BfsIter.Int} is returned.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph using the in-edges
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> BfsIter<V, E> newInstanceBackward(Graph<V, E> g, V source) {
		if (g instanceof IntGraph)
			return (BfsIter<V, E>) newInstanceBackward((IntGraph) g, ((Integer) source).intValue());
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		BfsIter.Int indexBfs = new BfsIterImpl.Backward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.ObjBfsFromIndexBfs<>(g, indexBfs);
	}

	/**
	 * Create a backward BFS iterator in an int graph.
	 *
	 * <p>
	 * The regular BFS uses the out-edges of each vertex to explore its neighbors, while the <i>backward</i> BFS uses
	 * the in-edges to do so.
	 *
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph using the in-edges
	 */
	public static BfsIter.Int newInstanceBackward(IntGraph g, int source) {
		if (g instanceof IndexGraph)
			return new BfsIterImpl.Backward((IndexGraph) g, source);
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		BfsIter.Int indexBfs = new BfsIterImpl.Backward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.IntBfsFromIndexBfs(g, indexBfs);
	}

	/**
	 * Create a tree from all the vertices and edges traversed by a bread first search.
	 *
	 * <p>
	 * The created graph will contain only the vertices reachable from the source vertex. For each such vertex other
	 * than the source vertex, the graph will contain the edge that led to it during the search. If there are \(k\)
	 * reachable vertices, the graph will contain \(k-1\) edges.
	 *
	 * <p>
	 * The returned graph will be directed if the original graph is directed. In such case, the tree is directed from
	 * the source to the other vertices. To control the directionality of the returned graph, use
	 * {@link #bfsTree(Graph, Object, boolean)}.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument, {@link IntGraph} is returned.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a tree graph that contains all the vertices and edges traversed by a bread first search rooted at
	 *                the source vertex
	 */
	public static <V, E> Graph<V, E> bfsTree(Graph<V, E> g, V source) {
		return bfsTree(g, source, g.isDirected());
	}

	/**
	 * Create a tree from all the vertices and edges traversed by a bread first search, optionally directed or
	 * undirected.
	 *
	 * <p>
	 * The created graph will contain only the vertices reachable from the source vertex. For each such vertex other
	 * than the source vertex, the graph will contain the edge that led to it during the search. If there are \(k\)
	 * reachable vertices, the graph will contain \(k-1\) edges.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument, {@link IntGraph} is returned.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  g        a graph
	 * @param  source   a vertex in the graph from which the search will start from
	 * @param  directed if {@code true} the returned tree will be directed. If the original graph was undirected and a
	 *                      directed tree is created, the edges in the tree will be directed from the source towards the
	 *                      other vertices
	 * @return          a tree graph that contains all the vertices and edges traversed by a bread first search rooted
	 *                  at the source vertex
	 */
	public static <V, E> Graph<V, E> bfsTree(Graph<V, E> g, V source, boolean directed) {
		if (g instanceof IndexGraph) {
			IndexGraph ig = (IndexGraph) g;
			int src = ((Integer) source).intValue();
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			for (BfsIter.Int iter = newInstance(ig, src); iter.hasNext();) {
				int v = iter.nextInt();
				b.addVertex(v);
				if (v == src)
					continue;
				int e = iter.lastEdgeInt();
				assert e >= 0;
				b.addEdge(ig.edgeEndpoint(e, v), v, e);
			}
			@SuppressWarnings("unchecked")
			Graph<V, E> tree = (Graph<V, E>) b.build();
			return tree;

		} else if (g instanceof IntGraph) {
			IndexGraph ig = g.indexGraph();
			IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
			IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
			int srcIdx = viMap.idToIndex(((Integer) source).intValue());
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			for (BfsIter.Int iter = newInstance(ig, srcIdx); iter.hasNext();) {
				int vIdx = iter.nextInt();
				int v = viMap.indexToIdInt(vIdx);
				b.addVertex(v);
				if (vIdx == srcIdx)
					continue;
				int eIdx = iter.lastEdgeInt();
				int uIdx = ig.edgeEndpoint(eIdx, vIdx);
				int e = eiMap.indexToIdInt(eIdx);
				int u = viMap.indexToIdInt(uIdx);
				b.addEdge(u, v, e);
			}
			@SuppressWarnings("unchecked")
			Graph<V, E> tree = (Graph<V, E>) b.build();
			return tree;

		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			int srcIdx = viMap.idToIndex(source);
			GraphBuilder<V, E> b = GraphBuilder.newInstance(directed);
			for (BfsIter.Int iter = newInstance(ig, srcIdx); iter.hasNext();) {
				int vIdx = iter.nextInt();
				V v = viMap.indexToId(vIdx);
				b.addVertex(v);
				if (vIdx == srcIdx)
					continue;
				int eIdx = iter.lastEdgeInt();
				int uIdx = ig.edgeEndpoint(eIdx, vIdx);
				E e = eiMap.indexToId(eIdx);
				V u = viMap.indexToId(uIdx);
				b.addEdge(u, v, e);
			}
			return b.build();
		}
	}

}
