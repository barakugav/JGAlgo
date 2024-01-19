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

import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A path of edges in an int graph.
 *
 * <p>
 * This interface is a specific version of {@link Path} for {@link IntGraph}. For the full documentation see
 * {@link Path}.
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

	/**
	 * Create a new path from an edge list, a source and a target vertices.
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
			return new PathImpl.IndexPath((IndexGraph) g, source, target, edges);
		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			IntList iEdges = IntImmutableList.of(IndexIdMaps.idToIndexCollection(edges, eiMap).toIntArray());

			IPath indexPath = new PathImpl.IndexPath(iGraph, iSource, iTarget, iEdges);
			return PathImpl.intPathFromIndexPath(g, indexPath);
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
		IntIterator eit;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			eit = edges.iterator();
		} else {
			ig = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			source = viMap.idToIndex(source);
			target = viMap.idToIndex(target);
			eit = IndexIdMaps.idToIndexIterator(edges.iterator(), eiMap);
		}
		return PathImpl.isPath(ig, source, target, eit);
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
		if (g instanceof IndexGraph)
			return PathImpl.findPath((IndexGraph) g, source, target);

		IndexGraph iGraph = g.indexGraph();
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		int iSource = viMap.idToIndex(source);
		int iTarget = viMap.idToIndex(target);

		IPath indexPath = PathImpl.findPath(iGraph, iSource, iTarget);
		return PathImpl.intPathFromIndexPath(g, indexPath);
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
			visited.set(source);
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
