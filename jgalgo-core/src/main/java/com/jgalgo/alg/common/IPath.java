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

import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A path of edges in an int graph.
 *
 * <p>
 * This interface is a specification of {@link Path} for {@link IntGraph}. For the full documentation see {@link Path}.
 *
 * @author Barak Ugav
 */
public interface IPath extends Path<Integer, Integer> {

	/**
	 * Get the source vertex of the path.
	 *
	 * <p>
	 * If the returned vertex is the same as {@link #targetInt()}, the represented path is actually a cycle.
	 *
	 * @return the source vertex of the path.
	 */
	int sourceInt();

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #sourceInt()} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer source() {
		return Integer.valueOf(sourceInt());
	}

	/**
	 * Get the target vertex of the path.
	 *
	 * <p>
	 * If the returned vertex is the same as {@link #sourceInt()}, the represented path is actually a cycle.
	 *
	 * @return the target vertex of the path.
	 */
	int targetInt();

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #targetInt()} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer target() {
		return Integer.valueOf(targetInt());
	}

	@Override
	default boolean isCycle() {
		return sourceInt() == targetInt();
	}

	@Override
	IEdgeIter edgeIter();

	@Override
	IntList edges();

	@Override
	IntList vertices();

	@Override
	IntGraph graph();

	@Override
	IPath subPath(int fromEdgeIndex, int toEdgeIndex);

	/**
	 * Create a new path from an edge list, a source and a target vertices.
	 *
	 * <p>
	 * The edges list passed to this function is used for the whole lifetime of the returned path. The list should not
	 * be modified after passed to this method, as it is not cloned.
	 *
	 * <p>
	 * Note that this function does not check whether the given edge list is a valid path in the given graph. To check
	 * for validity, use {@link #isPath(IntGraph, int, int, IntList)}.
	 *
	 * @param  g      the graph
	 * @param  source a source vertex
	 * @param  target a target vertex
	 * @param  edges  a list of edges that form a path from the {@code source} to the {@code target} vertices in the
	 * @return        a new path
	 */
	static IPath valueOf(IntGraph g, int source, int target, IntList edges) {
		if (g instanceof IndexGraph) {
			return Paths.valueOf((IndexGraph) g, source, target, edges);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			IntList iEdges = Fastutil.list(IndexIdMaps.idToIndexCollection(edges, eiMap).toIntArray());

			IPath indexPath = Paths.valueOf(iGraph, iSource, iTarget, iEdges);
			return (IPath) Path.pathFromIndexPath(g, indexPath);
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
	 * @param  g      a graph
	 * @param  source a source vertex
	 * @param  target a target vertex
	 * @param  edges  a list of edges that form a path from the {@code source} to the {@code target} vertices in the
	 *                    graph.
	 * @return        {@code true} if the given edge list is a valid path in the given graph, else {@code false}
	 */
	static boolean isPath(IntGraph g, int source, int target, IntList edges) {
		IndexGraph ig;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
		} else {
			ig = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			source = viMap.idToIndex(source);
			target = viMap.idToIndex(target);
			edges = IndexIdMaps.idToIndexList(edges, eiMap);
		}
		return Paths.isPath(ig, source, target, edges);
	}

	/**
	 * Find a valid path from \(u\) to \(v\).
	 *
	 * <p>
	 * This function uses BFS, which will result in the shortest path in the number of edges.
	 *
	 * @param  g      a graph
	 * @param  source source vertex
	 * @param  target target vertex
	 * @return        a path from \(u\) to \(v\), or {@code null} if no such path was found
	 */
	static IPath findPath(IntGraph g, int source, int target) {
		if (g instanceof IndexGraph) {
			return Paths.findPath((IndexGraph) g, source, target);

		} else {

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);

			IPath indexPath = Paths.findPath(iGraph, iSource, iTarget);
			return (IPath) Path.pathFromIndexPath(g, indexPath);
		}
	}

	/**
	 * Find all the vertices reachable from a given source vertex.
	 *
	 * @param  g      a graph
	 * @param  source a source vertex
	 * @return        a set of all the vertices reachable from the given source vertex
	 */
	static IntSet reachableVertices(IntGraph g, int source) {
		return reachableVertices(g, IntIterators.singleton(source));
	}

	/**
	 * Create an iterator that iterate over the vertices visited by an edge path.
	 *
	 * <p>
	 * The returned iterator will be identical to the iterator of {@link IPath#vertices()}}.
	 *
	 * <p>
	 * This method assume the list of edges is a valid path in the graph starting from the given source vertex, no
	 * validation is performed to check that.
	 *
	 * @param  g      a graph
	 * @param  source the source vertex of the path
	 * @param  edges  a list of edges that from a path starting from {@code source}
	 * @return        an iterator that iterate over the vertices visited by the path
	 */
	static IntIterator verticesIter(IntGraph g, int source, IntList edges) {
		IndexGraph ig;
		int src;
		IntList edges0;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			src = source;
			edges0 = edges;
			Assertions.checkVertex(src, ig.vertices().size());
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			ig = g.indexGraph();
			src = viMap.idToIndex(source);
			edges0 = IndexIdMaps.idToIndexList(edges, eiMap);
		}

		IntIterator indexIter = new IntIterator() {
			int v = src;
			IntIterator eit = edges0.iterator();
			final boolean directed = ig.isDirected();

			@Override
			public boolean hasNext() {
				return v >= 0;
			}

			@Override
			public int nextInt() {
				Assertions.hasNext(this);
				int u = v;
				if (eit.hasNext()) {
					int e = eit.nextInt();
					if (directed) {
						assert u == ig.edgeSource(e);
						v = ig.edgeTarget(e);
					} else {
						v = ig.edgeEndpoint(e, u);
					}
				} else {
					v = -1;
				}
				return u;
			}

			@Override
			public int skip(final int n) {
				if (n < 1 || !directed)
					return IntIterator.super.skip(n);

				int skipped = eit.skip(n - 1);
				if (!eit.hasNext()) {
					if (v >= 0) {
						skipped++;
						v = -1;
					}
					return skipped;
				}
				v = ig.edgeTarget(eit.nextInt());
				return n;
			}
		};

		if (g instanceof IndexGraph) {
			return indexIter;
		} else {
			return IndexIdMaps.indexToIdIterator(indexIter, g.indexGraphVerticesMap());
		}
	}

	/**
	 * Find all the vertices reachable from a set of given source vertices.
	 *
	 * @param  g       a graph
	 * @param  sources a set of source vertices
	 * @return         a set of all the vertices reachable from the given source vertices
	 */
	static IntSet reachableVertices(IntGraph g, IntIterator sources) {
		IndexGraph ig;
		IntIterator iSources;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			iSources = sources;
		} else {
			ig = g.indexGraph();
			iSources = IndexIdMaps.idToIndexIterator(sources, g.indexGraphVerticesMap());
		}

		final int n = g.vertices().size();
		Bitmap visited = new Bitmap(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();

		while (iSources.hasNext()) {
			int source = iSources.nextInt();
			if (visited.set(source))
				queue.enqueue(source);
		}

		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			for (IEdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (visited.get(v))
					continue;
				visited.set(v);
				queue.enqueue(v);
			}
		}

		IntSet indexRes = ImmutableIntArraySet.withBitmap(visited);
		if (!(g instanceof IndexGraph))
			indexRes = IndexIdMaps.indexToIdSet(indexRes, g.indexGraphVerticesMap());
		return indexRes;
	}
}
