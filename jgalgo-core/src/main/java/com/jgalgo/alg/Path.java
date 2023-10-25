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

import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * A path of edges in a graph.
 * <p>
 * A path is a list of edges \(e_1,e_2,\ldots\) where each target vertex of edge \(e_i\) is the source vertex of the
 * next edge \(e_{i+1}\). If the graph is undirected the definition of a 'source' and 'target' are interchangeable, and
 * each pair of consecutive edges simply share an endpoint.
 * <p>
 * A Path object might be used to represent a cycle as well, if the source and target of the path are the same vertex.
 * <p>
 * If the underlying graph was modified after the Path object was created, the Path object should not be used.
 *
 * <pre> {@code
 * Graph g = ...;
 * int sourceVertex = ...;
 * int targetVertex = ...;
 * Path p = Path.findPath(g, sourceVertex, targetVertex);
 *
 * System.out.println("The path between u and v consist of the following edges:");
 * for (EdgeIter it = p.edgeIter(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	int u = it.source(), v = it.target();
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface Path {

	/**
	 * Get the source vertex of the path.
	 * <p>
	 * If the returned vertex is the same as {@link #target()}, the represented path is actually a cycle.
	 *
	 * @return the source vertex of the path.
	 */
	int source();

	/**
	 * Get the target vertex of the path.
	 * <p>
	 * If the returned vertex is the same as {@link #source()}, the represented path is actually a cycle.
	 *
	 * @return the target vertex of the path.
	 */
	int target();

	/**
	 * Check whether this path form a cycle.
	 * <p>
	 * A cycle is a path which start and ends at the same vertex.
	 *
	 * @return {@code true} if this path form a cycle, else {@code false}
	 */
	default boolean isCycle() {
		return source() == target();
	}

	/**
	 * Get an {@link EdgeIter} that iterate over the edges of the path.
	 *
	 * @return an {@link EdgeIter} that iterate over the edges of the path.
	 */
	EdgeIter edgeIter();

	/**
	 * Get the edges forming this path.
	 * <p>
	 * The path is defined as a list of edges \(e_1,e_2,\ldots\), where each target vertex of an edge \(e_i\) is the
	 * source vertex of the next edge \(e_{i+1}\).
	 *
	 * @return the edges forming this path, by the path order
	 */
	IntList edges();

	/**
	 * Get the vertices forming this path.
	 * <p>
	 * The path is defined as a list of edges \(e_1,e_2,\ldots\), where each target vertex of an edge \(e_i\) is the
	 * source vertex of the next edge \(e_{i+1}\). The list of <b>vertices</b> of this path is the vertices visited by
	 * this path, ordered by their visit order. If this path form a cycle, the vertices list size is the same as the
	 * edge list, otherwise it is greater by one.
	 *
	 * @return the vertices visited by this path, by the path order
	 */
	IntList vertices();

	/**
	 * Find a valid path from \(u\) to \(v\).
	 * <p>
	 * This function uses BFS, which will result in the shortest path in the number of edges.
	 *
	 * @param  g      a graph
	 * @param  source source vertex
	 * @param  target target vertex
	 * @return        a path from \(u\) to \(v\), or {@code null} if no such path was found
	 */
	public static Path findPath(Graph g, int source, int target) {
		if (g instanceof IndexGraph)
			return PathImpl.findPath((IndexGraph) g, source, target);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();
		int iSource = viMap.idToIndex(source);
		int iTarget = viMap.idToIndex(target);

		Path indexPath = PathImpl.findPath(iGraph, iSource, iTarget);
		return PathImpl.pathFromIndexPath(indexPath, viMap, eiMap);
	}

}
