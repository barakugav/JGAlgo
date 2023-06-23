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
 * A builder for {@linkplain Graph graphs}.
 * <p>
 * The builder is used to construct <b>non-empty</b> graphs. Differing from {@link GraphFactory} which create new empty
 * graphs, the builder is used to add vertices and edges before actually creating the graph. This capability is required
 * to create immutable graphs, but can also be used to build mutable graph and may gain a performance boost compared to
 * creating an empty graph and adding the same vertices and edges.
 *
 * @see    GraphBuilder#newUndirected()
 * @see    GraphBuilder#newDirected()
 * @see    IndexGraphBuilder
 * @see    GraphFactory
 * @author Barak Ugav
 */
public interface GraphBuilder {

	/**
	 * Get the set of vertices that were added to the graph.
	 *
	 * @return the graph vertices
	 */
	IntSet vertices();

	/**
	 * Get the set of edges that were added to the graph.
	 *
	 * @return the graph edges
	 */
	IntSet edges();

	/**
	 * Add a new vertex to the graph.
	 * <p>
	 * The builder will choose identifier not used for any existing vertex, and will return it. It is also possible to
	 * add a new vertex and choose its identifier by using {@link #addVertex(int)}. Only one of {@link #addVertex()} and
	 * {@link #addVertex(int)} can be used during the construction of a graph.
	 *
	 * @return the new vertex identifier
	 */
	int addVertex();

	/**
	 * Add a new vertex to the graph, with user-chosen identifier.
	 * <p>
	 * This function is similar to {@link #addVertex()}, but let the user to choose the the identifier of the new
	 * vertex. Only one of {@link #addVertex()} and {@link #addVertex(int)} can be used during the construction of a
	 * graph.
	 *
	 * @param vertex the new vertex identifier
	 */
	void addVertex(int vertex);

	/**
	 * Add a new edge to the graph.
	 * <p>
	 * The builder will choose identifier not used for any existing edge, and will return it. It is also possible to add
	 * a new edge and choose its identifier by using {@link #addEdge(int, int, int)}. Only one of
	 * {@link #addEdge(int, int)} and {@link #addEdge(int, int, int)} can be used during the construction of a graph.
	 *
	 * @param  source the source vertex of the new edge
	 * @param  target the target vertex of the new edge
	 * @return        the new edge identifier
	 */
	int addEdge(int source, int target);

	/**
	 * Add a new edge to the graph, with user-chosen identifier.
	 * <p>
	 * This function is similar to {@link #addEdge(int, int)}, but let the user to choose the identifier of the new
	 * edge. Only one of {@link #addEdge(int, int)} and {@link #addEdge(int, int, int)} can be used during the
	 * construction of a graph.
	 *
	 * @param source the source vertex of the new edge
	 * @param target the target vertex of the new edge
	 * @param edge   the identifier of the new edge
	 */
	void addEdge(int source, int target, int edge);

	/**
	 * Clear the builder by removing all vertices and edges added to it.
	 */
	void clear();

	/**
	 * Build a new immutable graph with the builder vertices and edges.
	 *
	 * @return a new immutable graph with the vertices and edges that were added to the builder.
	 */
	Graph build();

	/**
	 * Build a new mutable graph with the builder vertices and edges.
	 *
	 * @return a new mutable graph with the vertices and edges that were added to the builder.
	 */
	Graph buildMutable();

	/**
	 * Create a new builder that builds undirected graphs.
	 *
	 * @return a new empty builder for undirected graphs
	 */
	static GraphBuilder newUndirected() {
		return new GraphBuilderImpl.Undirected();
	}

	/**
	 * Create a new builder that builds directed graphs.
	 *
	 * @return a new empty builder for directed graphs
	 */
	static GraphBuilder newDirected() {
		return new GraphBuilderImpl.Directed();
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges.
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  g a graph
	 * @return   a builder initialized with the given graph vertices and edges
	 */
	static GraphBuilder newFrom(Graph g) {
		GraphBuilder builder = g.getCapabilities().directed() ? newDirected() : newUndirected();
		for (int v : g.vertices())
			builder.addVertex(v);
		for (int e : g.edges())
			builder.addEdge(g.edgeSource(e), g.edgeTarget(e), e);
		return builder;
	}

}
