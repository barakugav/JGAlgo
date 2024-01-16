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

import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;

/**
 * A mapping between two graphs that preserves the structure of the graphs.
 *
 * <p>
 * Given two graphs, an isomorphism is a mapping functions that maps the first graph vertices to the second graph
 * vertices, while preserving the structure of the graph. There are few variants, described in the different types of
 * isomorphism, see {@link IsomorphismTester}. Some types of isomorphism map only a subset of the vertices or edges, and
 * in such case the mapping will return {@code null} for vertices or edges that are not mapped.
 *
 * @param  <V1> the type of vertices of the first graph
 * @param  <E1> the type of edges of the first graph
 * @param  <V2> the type of vertices of the second graph
 * @param  <E2> the type of edges of the second graph
 * @see         IsomorphismTester
 * @see         <a href= "https://en.wikipedia.org/wiki/Graph_isomorphism">Wikipedia</a>
 * @author      Barak Ugav
 */
public interface IsomorphismMapping<V1, E1, V2, E2> {

	/**
	 * Map a vertex from the first graph to a vertex of the second graph.
	 *
	 * @param  vertex                the vertex to map
	 * @return                       the mapped vertex, or {@code null} if {@code v1} is not mapped
	 * @throws NoSuchVertexException if the vertex does not exist in the first graph
	 */
	V2 mapVertex(V1 vertex);

	/**
	 * Map an edge from the first graph to an edge of the second graph.
	 *
	 * @param  edge                the edge to map
	 * @return                     the mapped edge, or {@code null} if {@code e1} is not mapped
	 * @throws NoSuchEdgeException if the edge does not exist in the first graph
	 */
	E2 mapEdge(E1 edge);

	/**
	 * Get the inverse mapping.
	 *
	 * @return the inverse mapping
	 */
	IsomorphismMapping<V2, E2, V1, E1> inverse();

	/**
	 * Get the source graph.
	 *
	 * <p>
	 * The 'source' graph contains the vertices and edges of the <b>domain</b> of the mapping, namely these vertices and
	 * edges are mapped to vertices and edges of the target (range) graph.
	 *
	 * @return the source graph
	 */
	Graph<V1, E1> sourceGraph();

	/**
	 * Get the target graph.
	 *
	 * <p>
	 * The 'target' graph contains the vertices and edges of the <b>range</b> of the mapping, namely the vertices and
	 * edges of another graph (the source or domain graph) are mapped to these vertices and edges.
	 *
	 * @return the target graph
	 */
	Graph<V2, E2> targetGraph();

	/**
	 * Get the set of the vertices that are mapped out of all the vertices of the {@linkplain #sourceGraph() source
	 * graph}.
	 *
	 * <p>
	 * The mapping may not map all the vertices of the source graph, in case the first graph is smaller than the second
	 * graph, and a (maybe induced) sub graph isomorphism was searched. This method can be used to get the set of
	 * vertices for which there is a corresponding vertex in the target graph.
	 *
	 * <p>
	 * Together with {@link #mappedEdges()}, this method can be used to construct the subgraph mapped to the target
	 * graph:
	 *
	 * <pre> {@code
	 * Set<V1> vertices = mapping.mappedVertices();
	 * Set<E1> edges = mapping.mappedEdges();
	 * Graph<V1, E1> mappedSubGraph = mapping.sourceGraph().subGraphCopy(vertices, edges);
	 * } </pre>
	 *
	 * @return the set of the mapped vertices
	 */
	Set<V1> mappedVertices();

	/**
	 * Get the set of the edges that are mapped out of all the edges of the {@linkplain #sourceGraph() source graph}.
	 *
	 * <p>
	 * The mapping may not map all the edges of the source graph, in case the first graph is smaller than the second
	 * graph, and a (maybe induced) sub graph isomorphism was searched. This method can be used to get the set of edges
	 * for which there is a corresponding edge in the target graph.
	 *
	 * <p>
	 * Together with {@link #mappedVertices()}, this method can be used to construct the subgraph mapped to the target
	 * graph:
	 *
	 * <pre> {@code
	 * Set<V1> vertices = mapping.mappedVertices();
	 * Set<E1> edges = mapping.mappedEdges();
	 * Graph<V1, E1> mappedSubGraph = mapping.sourceGraph().subGraphCopy(vertices, edges);
	 * } </pre>
	 *
	 * @return the set of the mapped edges
	 */
	Set<E1> mappedEdges();

}
