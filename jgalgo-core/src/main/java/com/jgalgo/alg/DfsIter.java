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
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.NoSuchVertexException;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Depth first search (DFS) iterators static class.
 *
 * <p>
 * The DFS iterator is used to iterate over the vertices of a graph is a depth first manner, namely it explore as far as
 * possible along each branch before backtracking. The iterator will visit every vertex \(v\) for which there is a path
 * from the source(s) to \(v\). Each such vertex will be visited exactly once.
 *
 * <p>
 * The graph should not be modified during the DFS iteration.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * String sourceVertex = ...;
 * for (DfsIter<String, Integer> iter = DfsIter.newInstance(g, sourceVertex); iter.hasNext();) {
 *     String v = iter.next();
 *     List<E> edgePath = iter.edgePath();
 *     System.out.println("Reached vertex " + v + " using the edges: " + edgePath.edges());
 * }
 * }</pre>
 *
 * @see        BfsIter
 * @see        <a href="https://en.wikipedia.org/wiki/Depth-first_search">Wikipedia</a>
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface DfsIter<V, E> extends Iterator<V> {

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
	 * Get the path from the source to the last vertex returned by {@link #next()}.
	 *
	 * <p>
	 * The behavior is undefined if {@link #next()} was not called yet.
	 *
	 * @return list of edges forming a path from the source to the last vertex returned by {@link #next()}. The returned
	 *         list should not be modified by the user.
	 */
	public List<E> edgePath();

	/**
	 * A DFS iterator for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface Int extends DfsIter<Integer, Integer>, IntIterator {

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
			return Integer.valueOf(nextInt());
		}

		@Override
		public IntList edgePath();
	}

	/**
	 * Create a DFS iterator.
	 *
	 * @param  <V>                   the vertices type
	 * @param  <E>                   the edges type
	 * @param  g                     a graph
	 * @param  source                a vertex in the graph from which the search will start from
	 * @return                       a DFS iterator that iterate over the vertices of the graph
	 * @throws NoSuchVertexException if the source vertex is not in the graph
	 */
	@SuppressWarnings("unchecked")
	static <V, E> DfsIter<V, E> newInstance(Graph<V, E> g, V source) {
		if (g instanceof IntGraph)
			return (DfsIter<V, E>) newInstance((IntGraph) g, ((Integer) source).intValue());

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		int iSource = viMap.idToIndex(source);
		DfsIter.Int indexIter = new DfsIterImpl(iGraph, iSource);
		return new DfsIterImpl.ObjDfsFromIndexDfs<>(g, indexIter);
	}

	/**
	 * Create a DFS iterator for an int graph.
	 *
	 * @param  g                     a graph
	 * @param  source                a vertex in the graph from which the search will start from
	 * @return                       a DFS iterator that iterate over the vertices of the graph
	 * @throws NoSuchVertexException if the source vertex is not in the graph
	 */
	static DfsIter.Int newInstance(IntGraph g, int source) {
		if (g instanceof IndexGraph)
			return new DfsIterImpl((IndexGraph) g, source);

		IndexGraph iGraph = g.indexGraph();
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		int iSource = viMap.idToIndex(source);
		DfsIter.Int indexIter = new DfsIterImpl(iGraph, iSource);
		return new DfsIterImpl.IntDfsFromIndexDfs(g, indexIter);
	}

	/**
	 * Create a tree from all the vertices and edges traversed by a depth first search.
	 *
	 * <p>
	 * The created graph will contain only the vertices reachable from the source vertex. For each such vertex other
	 * than the source vertex, the graph will contain the edge that led to it during the search. If there are \(k\)
	 * reachable vertices, the graph will contain \(k-1\) edges.
	 *
	 * <p>
	 * The returned graph will be directed if the original graph is directed. In such case, the tree is directed from
	 * the source to the other vertices. To control the directionality of the returned graph, use
	 * {@link #dfsTree(Graph, Object, boolean)}.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument, {@link IntGraph} is returned.
	 *
	 * @param  <V>                   the vertices type
	 * @param  <E>                   the edges type
	 * @param  g                     a graph
	 * @param  source                a vertex in the graph from which the search will start from
	 * @return                       a tree graph that contains all the vertices and edges traversed by a depth first
	 *                               search rooted at the source vertex
	 * @throws NoSuchVertexException if the source vertex is not in the graph
	 */
	public static <V, E> Graph<V, E> dfsTree(Graph<V, E> g, V source) {
		return dfsTree(g, source, g.isDirected());
	}

	/**
	 * Create a tree from all the vertices and edges traversed by a depth first search, optionally directed or
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
	 * @param  <V>                   the vertices type
	 * @param  <E>                   the edges type
	 * @param  g                     a graph
	 * @param  source                a vertex in the graph from which the search will start from
	 * @param  directed              if {@code true} the returned tree will be directed. If the original graph was
	 *                                   undirected and a directed tree is created, the edges in the tree will be
	 *                                   directed from the source towards the other vertices
	 * @return                       a tree graph that contains all the vertices and edges traversed by a depth first
	 *                               search rooted at the source vertex
	 * @throws NoSuchVertexException if the source vertex is not in the graph
	 */
	public static <V, E> Graph<V, E> dfsTree(Graph<V, E> g, V source, boolean directed) {
		if (g instanceof IndexGraph) {
			IndexGraph ig = (IndexGraph) g;
			int src = ((Integer) source).intValue();
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			for (DfsIter.Int iter = newInstance(ig, src); iter.hasNext();) {
				int v = iter.nextInt();
				b.addVertex(v);
				if (v == src)
					continue;
				IntList path = iter.edgePath();
				int e = path.getInt(path.size() - 1);
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
			for (DfsIter.Int iter = newInstance(ig, srcIdx); iter.hasNext();) {
				int vIdx = iter.nextInt();
				int v = viMap.indexToIdInt(vIdx);
				b.addVertex(v);
				if (vIdx == srcIdx)
					continue;
				IntList path = iter.edgePath();
				int eIdx = path.getInt(path.size() - 1);
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
			for (DfsIter.Int iter = newInstance(ig, srcIdx); iter.hasNext();) {
				int vIdx = iter.nextInt();
				V v = viMap.indexToId(vIdx);
				b.addVertex(v);
				if (vIdx == srcIdx)
					continue;
				IntList path = iter.edgePath();
				int eIdx = path.getInt(path.size() - 1);
				int uIdx = ig.edgeEndpoint(eIdx, vIdx);
				E e = eiMap.indexToId(eIdx);
				V u = viMap.indexToId(uIdx);
				b.addEdge(u, v, e);
			}
			return b.build();
		}
	}

}
