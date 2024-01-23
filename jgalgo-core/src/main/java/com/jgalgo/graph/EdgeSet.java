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
package com.jgalgo.graph;

import java.util.Set;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Set of graph edges.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * String vertex = ...;
 * for (EdgeIter<String, Integer> eit = g.outEdges(vertex).iterator(); eit.hasNext();) {
 * 	Integer e = eit.next();
 * 	String u = eit.source();
 * 	String v = eit.target();
 * 	assert vertex.equals(u);
 * 	System.out.println("Out edge of " + vertex + ": " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * <p>
 * Note that sets implementing this interface should define {@link Object#equals(Object)}, {@link Object#hashCode()} and
 * {@link Object#toString()} compatible with the {@link Set} interface. Namely, the above methods should not take into
 * account the endpoints of the edges, only the edges identifiers themselves.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @see        EdgeIter
 * @author     Barak Ugav
 */
public interface EdgeSet<V, E> extends Set<E> {

	/**
	 * Return an edge iterator that iterate over the edges in this set.
	 */
	@Override
	EdgeIter<V, E> iterator();

	/**
	 * Create an edge set object from a plain set of edges.
	 *
	 * <p>
	 * A 'plain' set of edges is a set of the edges identifiers. It does not include any information about the endpoints
	 * (source and target) of each edge. This method creates an {@link EdgeSet}, which does contains information about
	 * the endpoints, given a plain set of edges and the graph in which the edges are defined in.
	 *
	 * <p>
	 * The returned set is a view of the given set and graph. Namely, its updated when the original set is updated and
	 * visa vera, and the endpoints in the {@link EdgeSet} will be up to date with the given graph.
	 *
	 * <p>
	 * No validation is performed to ensure that all the given edges are actually in the given graph. If this is not the
	 * case, an exception may be thrown later when the graph is queried for an edge source and target.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, a {@link IEdgeSet} is returned. In that case, prefer to pass {@link IntSet} as
	 * {@code edges} to avoid un/boxing, or use {@link IEdgeSet#of(IntSet, IntGraph)} instead.
	 *
	 * <p>
	 * Usually an {@link EdgeSet} object is obtained via one of the method of a {@link Graph}, and rarely a user need to
	 * a create one, but it may be used to add multiple edges to a graph using the {@link Graph#addEdges(EdgeSet)}
	 * method. In the following snippet, a maximum cardinality matching is computed on a graph, and a new graph
	 * containing only the matching edges is created:
	 *
	 * <pre> {@code
	 * Graph<V, E> g = ...;
	 * Set<E> matching = MatchingAlgo.newInstance().computeMaximumMatching(g, null).edges();
	 *
	 * Graph<V,E> matchingGraph = Graph.newUndirected();
	 * matchingGraph.addVertices(g.vertices());
	 * matchingGraph.addEdges(EdgeSet.of(matching, g));
	 * }</pre>
	 *
	 * @param  <V>   the vertices type
	 * @param  <E>   the edges type
	 * @param  edges a set of edges identifiers
	 * @param  g     the graph in which the edges are defined in, and from which the endpoints of the edges should be
	 *                   retrieved
	 * @return       an {@link EdgeSet} with the given edges, containing the endpoints information from the graph
	 */
	@SuppressWarnings("unchecked")
	static <V, E> EdgeSet<V, E> of(Set<E> edges, Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			IntSet edges0 = IntAdapters.asIntSet((Set<Integer>) edges);
			return (EdgeSet<V, E>) new IEdgeSetView(edges0, (IndexGraph) g);
		} else {
			IndexGraph ig = g.indexGraph();
			IntSet iEdges = IndexIdMaps.idToIndexSet(edges, g.indexGraphEdgesMap());
			IEdgeSet iEdgeSet = new IEdgeSetView(iEdges, ig);
			return IndexIdMaps.indexToIdEdgeSet(iEdgeSet, g);
		}
	}

	/**
	 * Create an edge set object of all the edges in a graph.
	 *
	 * <p>
	 * The edge set returned by {@link Graph#edges()} is a 'plain' set of edges, namely it is a set of the edge
	 * identifiers themselves but does not include any information about the endpoints (source and target) of each edge.
	 * This method creates an {@link EdgeSet}, which does contains information about the endpoints, of all the edges in
	 * a given graph.
	 *
	 * <p>
	 * The returned set is a view of the given set and graph. Namely, its updated when the original set is updated and
	 * visa vera, and the endpoints in the {@link EdgeSet} will be up to date with the given graph.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, a {@link IEdgeSet} is returned. In that case, prefer to pass {@link IntSet} as
	 * {@code edges} to avoid un/boxing, or use {@link IEdgeSet#allOf(IntGraph)} instead.
	 *
	 * <p>
	 * Usually an {@link EdgeSet} object is obtained via one of the method of a {@link Graph}, and rarely a user need to
	 * a create one, but it may be used to add multiple edges to a graph using the {@link Graph#addEdges(EdgeSet)}
	 * method. In the following snippet, an auxillary graph is created which is a copy of an original graph {@code g}
	 * with additional vertex connected to all the original vertices, for shortest path potential function computation
	 * (used in Johnson APSP):
	 *
	 * <pre> {@code
	 * V auxillaryVertex = ...;
	 * Graph<V, E> auxillaryGraph = Graph.newDirected();
	 * auxillaryGraph.addVertices(g.vertices());
	 * auxillaryGraph.addVertex(auxillaryVertex);
	 * auxillaryGraph.addEdges(EdgeSet.allOf(g));
	 * for (V v : g.vertices())
	 * 	auxillaryGraph.addEdge(auxillaryVertex, v, ...
	 * }</pre>
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     an {@link EdgeSet} of all the edges in the graph
	 */
	static <V, E> EdgeSet<V, E> allOf(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return EdgeSet.of(g.edges(), g);
		} else {
			IndexGraph ig = g.indexGraph();
			IntSet iEdges = ig.edges();
			IEdgeSet iEdgeSet = new IEdgeSetView(iEdges, ig);
			return IndexIdMaps.indexToIdEdgeSet(iEdgeSet, g);
		}
	}

}
