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
import java.util.Set;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
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
	 * this path, ordered by their visit order. If this path form a cycle, the vertices list size is the same as the
	 * edge list (it does not include the source vertex, which is also the target vertex, twice), otherwise it is
	 * greater by one.
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
	 * Create a new path from an edge list, a source and a target vertices.
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
			IntList iEdges = IntImmutableList.of(IndexIdMaps.idToIndexCollection(edges, eiMap).toIntArray());

			IPath indexPath = new PathImpl.IndexPath(iGraph, iSource, iTarget, iEdges);
			return PathImpl.objPathFromIndexPath(g, indexPath);
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
		IntIterator eit;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			source0 = ((Integer) source).intValue();
			target0 = ((Integer) target).intValue();
			eit = IntAdapters.asIntIterator(((List<Integer>) edges).iterator());
		} else {
			ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			source0 = viMap.idToIndex(source);
			target0 = viMap.idToIndex(target);
			eit = IndexIdMaps.idToIndexIterator(edges.iterator(), eiMap);
		}

		return PathImpl.isPath(ig, source0, target0, eit);
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

		IPath indexPath = PathImpl.findPath(iGraph, iSource, iTarget);
		return PathImpl.objPathFromIndexPath(g, indexPath);
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
