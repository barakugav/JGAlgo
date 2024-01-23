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

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Set of int graph edges.
 *
 * <p>
 * This interface is a specific version of {@link EdgeSet} for {@link IntGraph}.
 *
 * <p>
 * A set of integers, each represent an edge ID in a graph
 *
 * <pre> {@code
 * IntGraph g = ...;
 * int vertex = ...;
 * for (IEdgeIter eit = g.outEdges(vertex).iterator(); eit.hasNext();) {
 * 	int e = eit.nextInt();
 * 	int u = eit.sourceInt();
 * 	int v = eit.targetInt();
 * 	assert vertex == u;
 * 	System.out.println("Out edge of " + vertex + ": " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    IEdgeIter
 * @author Barak Ugav
 */
public interface IEdgeSet extends EdgeSet<Integer, Integer>, IntSet {

	/**
	 * Return an edge iterator that iterate over the edges in this set.
	 */
	@Override
	IEdgeIter iterator();

	/**
	 * Return an edge iterator that iterate over the edges in this set.
	 */
	@Override
	default IEdgeIter intIterator() {
		return iterator();
	}

	/**
	 * Create an edge set object from a plain set of edges.
	 *
	 * <p>
	 * A 'plain' set of edges is a set of the edges identifiers. It does not include any information about the endpoints
	 * (source and target) of each edge. This method creates an {@link IEdgeSet}, which does contains information about
	 * the endpoints, given a plain set of edges and the graph in which the edges are defined in.
	 *
	 * <p>
	 * The returned set is a view of the given set and graph. Namely, its updated when the original set is updated and
	 * visa vera, and the endpoints in the {@link IEdgeSet} will be up to date with the given graph.
	 *
	 * <p>
	 * No validation is performed to ensure that all the given edges are actually in the given graph. If this is not the
	 * case, an exception may be thrown later when the graph is queried for an edge source and target.
	 *
	 *
	 * <p>
	 * Usually an {@link IEdgeSet} object is obtained via one of the method of a {@link IntGraph}, and rarely a user
	 * need to a create one, but it may be used to add multiple edges to a graph using the
	 * {@link IntGraph#addEdges(EdgeSet)} method. In the following snippet, a maximum cardinality matching is computed
	 * on a graph, and a new graph containing only the matching edges is created:
	 *
	 * <pre> {@code
	 * IntGraph g = ...;
	 * IntSet matching = (IntSet) MatchingAlgo.newInstance().computeMaximumMatching(g, null).edges();
	 *
	 * IntGraph matchingGraph = IntGraph.newUndirected();
	 * matchingGraph.addVertices(g.vertices());
	 * matchingGraph.addEdges(IEdgeSet.of(matching, g));
	 * }</pre>
	 *
	 * @param  edges a set of edges identifiers
	 * @param  g     the graph in which the edges are defined in, and from which the endpoints of the edges should be
	 *                   retrieved
	 * @return       an {@link IEdgeSet} with the given edges, containing the endpoints information from the graph
	 */
	static IEdgeSet of(IntSet edges, IntGraph g) {
		return (IEdgeSet) EdgeSet.of(edges, g);
	}

	/**
	 * Create an edge set object of all the edges in a graph.
	 *
	 * <p>
	 * The edge set returned by {@link IntGraph#edges()} is a 'plain' set of edges, namely it is a set of the edge
	 * identifiers themselves but does not include any information about the endpoints (source and target) of each edge.
	 * This method creates an {@link IEdgeSet}, which does contains information about the endpoints, of all the edges in
	 * a given graph.
	 *
	 * <p>
	 * The returned set is a view of the given set and graph. Namely, its updated when the original set is updated and
	 * visa vera, and the endpoints in the {@link IEdgeSet} will be up to date with the given graph.
	 *
	 * <p>
	 * Usually an {@link IEdgeSet} object is obtained via one of the method of a {@link IntGraph}, and rarely a user
	 * need to a create one, but it may be used to add multiple edges to a graph using the
	 * {@link IntGraph#addEdges(EdgeSet)} method. In the following snippet, an auxillary graph is created which is a
	 * copy of an original graph {@code g} with additional vertex connected to all the original vertices, for shortest
	 * path potential function computation (used in Johnson APSP):
	 *
	 * <pre> {@code
	 * int auxillaryVertex = ...;
	 * IntGraph auxillaryGraph = IntGraph.newDirected();
	 * auxillaryGraph.addVertices(g.vertices());
	 * auxillaryGraph.addVertex(auxillaryVertex);
	 * auxillaryGraph.addEdges(IEdgeSet.allOf(g));
	 * for (int v : g.vertices())
	 * 	auxillaryGraph.addEdge(auxillaryVertex, v);
	 * }</pre>
	 *
	 * @param  g a graph
	 * @return   an {@link IEdgeSet} of all the edges in the graph
	 */
	static IEdgeSet allOf(IntGraph g) {
		return (IEdgeSet) EdgeSet.allOf(g);
	}

}
