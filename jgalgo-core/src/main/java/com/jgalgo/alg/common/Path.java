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

package com.jgalgo.alg.common;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.IntAdapters;
import com.jgalgo.internal.util.Fastutil;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A path of edges in a graph.
 *
 * <p>
 * A path is a list of edges \(e_1,e_2,\ldots\) where each target vertex of edge \(e_i\) is the source vertex of the
 * next edge \(e_{i+1}\). If the graph is undirected the definition of a 'source' and 'target' are interchangeable, and
 * each pair of consecutive edges simply share an endpoint.
 *
 * <p>
 * A Path object might be used to represent a cycle as well, if the source and target of the path are the same vertex.
 *
 * <p>
 * If the underlying graph was modified after the Path object was created, the Path object should not be used.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * String sourceVertex = ...;
 * String targetVertex = ...;
 * Path<String, Integer> p = Path.findPath(g, sourceVertex, targetVertex);
 *
 * System.out.println("The path between u and v consist of the following edges:");
 * for (EdgeIter<String, Integer> it = p.edgeIter(); it.hasNext();) {
 * 	Integer e = it.next();
 * 	String u = it.source(), v = it.target();
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface Path<V, E> {

	/**
	 * Get the source vertex of the path.
	 *
	 * <p>
	 * If the returned vertex is the same as {@link #target()}, the represented path is actually a cycle.
	 *
	 * @return the source vertex of the path.
	 */
	V source();

	/**
	 * Get the target vertex of the path.
	 *
	 * <p>
	 * If the returned vertex is the same as {@link #source()}, the represented path is actually a cycle.
	 *
	 * @return the target vertex of the path.
	 */
	V target();

	/**
	 * Check whether this path form a cycle.
	 *
	 * <p>
	 * A cycle is a path which start and ends at the same vertex.
	 *
	 * @return {@code true} if this path form a cycle, else {@code false}
	 */
	default boolean isCycle() {
		return source().equals(target());
	}

	/**
	 * Check whether this path is simple.
	 *
	 * <p>
	 * A path is <a href= "https://en.wikipedia.org/wiki/Path_(graph_theory)#simple_path">simple</a> if the path does
	 * not visit the same vertex twice. Specifically, a cycle is not simple.
	 *
	 * @return {@code true} if this path is simple, else {@code false}
	 */
	boolean isSimple();

	/**
	 * Get an {@link EdgeIter} that iterate over the edges of the path.
	 *
	 * @return an {@link EdgeIter} that iterate over the edges of the path.
	 */
	EdgeIter<V, E> edgeIter();

	/**
	 * Get the edges forming this path.
	 *
	 * <p>
	 * The path is defined as a list of edges \(e_1,e_2,\ldots\), where each target vertex of an edge \(e_i\) is the
	 * source vertex of the next edge \(e_{i+1}\).
	 *
	 * @return the edges forming this path, by the path order
	 */
	List<E> edges();

	/**
	 * Get the vertices forming this path.
	 *
	 * <p>
	 * The path is defined as a list of edges \(e_1,e_2,\ldots\), where each target vertex of an edge \(e_i\) is the
	 * source vertex of the next edge \(e_{i+1}\). The list of <b>vertices</b> of this path is the vertices visited by
	 * this path, ordered by their visit order. The first vertex is the source of the path and the last vertex is the
	 * target of the path. The size of the returned list is always the size of the edges list plus one.
	 *
	 * <p>
	 * Note that if this path is a cycle, the first and last vertices in the returned list are the same vertex.
	 *
	 * @return the vertices visited by this path, by the path order
	 */
	List<V> vertices();

	/**
	 * Get the graph this path is defined on.
	 *
	 * @return the graph this path is defined on.
	 */
	Graph<V, E> graph();

	/**
	 * Create a sub path of this path that contains the edges between the specified {@code fromEdgeIndex}, inclusive,
	 * and {@code toEdgeIndex}, exclusive.
	 *
	 * <p>
	 * This method is equivalent to the following code:
	 *
	 * <pre>{@code
	 * List<E> edges = edges().subList(fromEdgeIndex, toEdgeIndex);
	 * V source = vertices().get(fromEdgeIndex);
	 * V target = vertices().get(toEdgeIndex);
	 * return Path.valueOf(graph(), source, target, edges);
	 * }</pre>
	 *
	 * @param  fromEdgeIndex             low endpoint (inclusive) of the edges subList
	 * @param  toEdgeIndex               high endpoint (exclusive) of the edges subList
	 * @return                           a sub path of the specified edges range within this path edges list
	 * @throws IndexOutOfBoundsException if {@code fromEdgeIndex < 0} or {@code toEdgeIndex > edgesNum} or
	 *                                       {@code fromEdgeIndex > toEdgeIndex}
	 */
	Path<V, E> subPath(int fromEdgeIndex, int toEdgeIndex);

	/**
	 * Check whether this path equals another object.
	 *
	 * <p>
	 * If the given object is not a path, this function returns {@code false}. For two paths to be equal, they must be
	 * defined in the same graph, which is compared using the naive {@code ==} operator, and they must represent the
	 * same path in the graph. If one path is a cycle and the other is not, they are not equal. If both are not cycles,
	 * they must have the same source and target vertices, and the same edges in the same order. If both are cycles,
	 * they must have the same edges in the same order, but an offset between the lists of edges is allowed. For
	 * example, the two paths \(e_1,e_2,e_3\) and \(e_2,e_3,e_1\) are equal (if they form a cycle!). Note that the
	 * sources and targets vertices are not equal in the example. For undirected graphs, all the above rules holds with
	 * the option to reverse one of the list of edges.
	 *
	 * @param  o the object to compare to
	 * @return   {@code true} if this path equals the given object, else {@code false}
	 */
	@Override
	boolean equals(Object o);

	/**
	 * Create a new path from an edge list, a source and a target vertices.
	 *
	 * <p>
	 * The edges list passed to this function is used for the whole lifetime of the returned path. The list should not
	 * be modified after passed to this method, as it is not cloned.
	 *
	 * <p>
	 * Note that this function does not check whether the given edge list is a valid path in the given graph. To check
	 * for validity, use {@link #isPath(Graph, Object, Object, List)}.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as argument, the returned path will be an {@link IPath} object.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      the graph
	 * @param  source a source vertex
	 * @param  target a target vertex
	 * @param  edges  a list of edges that form a path from the {@code source} to the {@code target} vertices in the
	 * @return        a new path
	 */
	@SuppressWarnings("unchecked")
	static <V, E> Path<V, E> valueOf(Graph<V, E> g, V source, V target, List<E> edges) {
		if (g instanceof IntGraph) {
			int iSource = ((Integer) source).intValue();
			int iTarget = ((Integer) target).intValue();
			IntList iEdges = IntAdapters.asIntList((List<Integer>) edges);
			return (Path<V, E>) IPath.valueOf((IntGraph) g, iSource, iTarget, iEdges);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			IntList iEdges = Fastutil.list(IndexIdMaps.idToIndexCollection(edges, eiMap).toIntArray());

			IPath indexPath = Paths.valueOf(iGraph, iSource, iTarget, iEdges);
			return pathFromIndexPath(g, indexPath);
		}
	}

	/**
	 * Create a path view from a path in the index graph of the given graph.
	 *
	 * @param  <V>       the vertices type
	 * @param  <E>       the edges type
	 * @param  g         the graph, must not be an index graph
	 * @param  indexPath the path in the index graph of the given graph
	 * @return           a path view
	 */
	@SuppressWarnings("unchecked")
	static <V, E> Path<V, E> pathFromIndexPath(Graph<V, E> g, IPath indexPath) {
		if (indexPath == null)
			return null;
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (Path<V, E>) new Paths.IntPathFromIndexPath((IntGraph) g, indexPath);
		} else {
			return new Paths.ObjPathFromIndexPath<>(g, indexPath);
		}
	}

	/**
	 * Check whether the given edge list is a valid path in the given graph.
	 *
	 * <p>
	 * A list of edges is a valid path in the graph if it is a list of edges \(e_1,e_2,\ldots\) where each target vertex
	 * of an edge \(e_i\) is the source vertex of the next edge \(e_{i+1}\). If the graph is undirected the definition
	 * of a 'source' and 'target' are interchangeable, and each pair of consecutive edges simply share an endpoint. In
	 * addition, the edge list must start with the {@code source} vertex and end with the {@code target} vertex.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source a source vertex
	 * @param  target a target vertex
	 * @param  edges  a list of edges that form a path from the {@code source} to the {@code target} vertices in the
	 *                    graph.
	 * @return        {@code true} if the given edge list is a valid path in the given graph, else {@code false}
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isPath(Graph<V, E> g, V source, V target, List<E> edges) {
		IndexGraph ig;
		int source0, target0;
		IntList edges0;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			source0 = ((Integer) source).intValue();
			target0 = ((Integer) target).intValue();
			edges0 = IntAdapters.asIntList((List<Integer>) edges);
		} else {
			ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			source0 = viMap.idToIndex(source);
			target0 = viMap.idToIndex(target);
			edges0 = IndexIdMaps.idToIndexList(edges, eiMap);
		}

		return Paths.isPath(ig, source0, target0, edges0);
	}

	/**
	 * Create an iterator that iterate over the vertices visited by an edge path.
	 *
	 * <p>
	 * The returned iterator will be identical to the iterator of {@link Path#vertices()}}.
	 *
	 * <p>
	 * This method assume the list of edges is a valid path in the graph starting from the given source vertex, no
	 * validation is performed to check that.
	 *
	 * <p>
	 * If the passed graph is an instance of {@link IntGraph}, the returned iterator will be an instance of
	 * {@link IntIterator}.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source the source vertex of the path
	 * @param  edges  a list of edges that from a path starting from {@code source}
	 * @return        an iterator that iterate over the vertices visited by the path
	 */
	@SuppressWarnings("unchecked")
	static <V, E> Iterator<V> verticesIter(Graph<V, E> g, V source, List<E> edges) {
		if (g instanceof IntGraph) {
			int src = ((Integer) source).intValue();
			IntList edges0 = IntAdapters.asIntList((List<Integer>) edges);
			return (Iterator<V>) IPath.verticesIter((IntGraph) g, src, edges0);
		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			int src = viMap.idToIndex(source);
			IntList edges0 = IndexIdMaps.idToIndexList(edges, eiMap);
			IntIterator indexIter = IPath.verticesIter(ig, src, edges0);
			return IndexIdMaps.indexToIdIterator(indexIter, viMap);
		}
	}

	/**
	 * Find a valid path from \(u\) to \(v\).
	 *
	 * <p>
	 * This function uses BFS, which will result in the shortest path in the number of edges.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as argument, the returned path will be an {@link IPath} object.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source source vertex
	 * @param  target target vertex
	 * @return        a path from \(u\) to \(v\), or {@code null} if no such path was found
	 */
	@SuppressWarnings("unchecked")
	static <V, E> Path<V, E> findPath(Graph<V, E> g, V source, V target) {
		if (g instanceof IntGraph)
			return (Path<V, E>) IPath
					.findPath((IntGraph) g, ((Integer) source).intValue(), ((Integer) target).intValue());

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		int iSource = viMap.idToIndex(source);
		int iTarget = viMap.idToIndex(target);

		IPath indexPath = Paths.findPath(iGraph, iSource, iTarget);
		return Path.pathFromIndexPath(g, indexPath);
	}

	/**
	 * Find all the vertices reachable from a given source vertex.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source a source vertex
	 * @return        a set of all the vertices reachable from the given source vertex
	 */
	@SuppressWarnings("unchecked")
	static <V, E> Set<V> reachableVertices(Graph<V, E> g, V source) {
		if (g instanceof IntGraph) {
			return (Set<V>) IPath.reachableVertices((IntGraph) g, ((Integer) source).intValue());

		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			int iSource = viMap.idToIndex(source);
			IntSet indexRes = IPath.reachableVertices(ig, iSource);
			return IndexIdMaps.indexToIdSet(indexRes, viMap);
		}
	}

	/**
	 * Find all the vertices reachable from a given set of source vertices.
	 *
	 * @param  <V>     the vertices type
	 * @param  <E>     the edges type
	 * @param  g       a graph
	 * @param  sources an iterator over a set of source vertices
	 * @return         a set of all the vertices reachable from the given source vertices
	 */
	@SuppressWarnings("unchecked")
	static <V, E> Set<V> reachableVertices(Graph<V, E> g, Iterator<V> sources) {
		if (g instanceof IntGraph) {
			IntIterator iSources = IntAdapters.asIntIterator((Iterator<Integer>) sources);
			return (Set<V>) IPath.reachableVertices((IntGraph) g, iSources);

		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IntIterator iSources = IndexIdMaps.idToIndexIterator(sources, viMap);
			IntSet indexRes = IPath.reachableVertices(ig, iSources);
			return IndexIdMaps.indexToIdSet(indexRes, viMap);
		}
	}

}
