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
package com.jgalgo.adapt.guava;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableGraph;
import com.jgalgo.graph.Graph;

/**
 * An adapter from a JGAlgo graph to a mutable GUava graph.
 *
 * <p>
 * The adapter is constructed with a {@linkplain com.jgalgo.graph.Graph JGAlgo graph} and implements the
 * {@linkplain com.google.common.graph.MutableGraph mutable Guava graph} interface, and can be used with any Guava
 * algorithm. The adapter is a live view, so any change in the JGAlgo graph is reflected in the Guava graph and vice
 * versa, but the underlying JGAlgo graph should not be modified directly.
 *
 * <p>
 * The {@linkplain com.google.common.graph.MutableGraph mutable Guava graph} is a mutable variant of the basic
 * {@linkplain com.google.common.graph.Graph Guava graph}. If mutability is not required, consider using the
 * {@linkplain GuavaGraphAdapter immutable adapter} instead.
 *
 * <p>
 * Parallel edges are not supported by {@linkplain com.google.common.graph.MutableGraph Guava basic graph}, therefore
 * the adapter will throw an {@linkplain UnsupportedOperationException} if the underlying JGAlgo graph allows parallel
 * edges. Whether this graph is directed or not, and whether it support self edges or not, is determined by the
 * underlying JGAlgo graph.
 *
 * <p>
 * Guava support different {@linkplain com.google.common.graph.ElementOrder element orders} for the nodes and the
 * incident edges. This adapter uses the default element order, which is unordered, and cannot be changed as the
 * underlying JGAlgo graph does not support any other order.
 *
 * <p>
 * The basic {@linkplain com.google.common.graph.Graph Guava graph} only represent connections between nodes, without
 * addressing the edges themselves. The edge generic type {@code E} is not reflected in the Guava graph, and is only
 * internally for safe access to the underlying JGAlgo graph. To create new connections (edges) in the graph, the
 * adapter must be able to create a new identifier for the created edge in the JGAlgo graph. This is done via the
 * {@linkplain com.jgalgo.graph.Graph#edgeBuilder() edge builder}, which is a requirement for creating a mutable
 * adapter.
 *
 * <p>
 * Among Guava {@link com.google.common.graph.Graph}, {@link com.google.common.graph.ValueGraph} and
 * {@link com.google.common.graph.Network}, the network is the most similar to JGAlgo graphs, as vertices and edges have
 * unique identifiers, and queries of edges are answered with the edges identifiers. On the other had, the Graph and
 * ValueGraph do not support unique identifiers for the edges, and operations on edges are addressed by a pair of nodes.
 * The ValueGraph does associate a value with each edge, be it does not have to be unique, and it is more similar to
 * weights in JGAlgo graphs.
 *
 * @see        com.jgalgo.graph.Graph
 * @see        com.google.common.graph.MutableGraph
 * @see        GuavaGraphAdapter
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class GuavaMutableGraphAdapter<V, E> extends GuavaGraphAdapter<V, E> implements MutableGraph<V> {

	/**
	 * Constructs a new mutable adapter from the given JGAlgo graph.
	 *
	 * <p>
	 * An {@linkplain com.jgalgo.graph.Graph#edgeBuilder() edge builder} is required, as the adapter will need to create
	 * edge identifiers in the JGAlgo graph to add new connections in the Guava graph.
	 *
	 * @param  graph                         the JGAlgo graph
	 * @throws UnsupportedOperationException if the graph allows parallel edges
	 * @throws IllegalArgumentException      if the graph does not have edge builder
	 */
	public GuavaMutableGraphAdapter(Graph<V, E> graph) {
		super(graph);
		if (graph.edgeBuilder() == null)
			throw new IllegalArgumentException("Graph must have edge builder");
	}

	@Override
	public boolean addNode(V node) {
		return GuavaAdapters.addNode(graph, node);
	}

	@Override
	public boolean putEdge(V nodeU, V nodeV) {
		boolean modified = false;
		modified |= addNode(nodeU);
		modified |= addNode(nodeV);
		if (modified || !graph.containsEdge(nodeU, nodeV)) {
			graph.addEdge(nodeU, nodeV);
			modified = true;
		}
		return modified;
	}

	@Override
	public boolean putEdge(EndpointPair<V> endpoints) {
		validateEndpoints(endpoints);
		return putEdge(endpoints.nodeU(), endpoints.nodeV());
	}

	@Override
	public boolean removeNode(V node) {
		return GuavaAdapters.removeNode(graph, node);
	}

	@Override
	public boolean removeEdge(V nodeU, V nodeV) {
		E e = GuavaAdapters.getEdge(graph, nodeU, nodeV);
		if (e == null)
			return false;
		graph.removeEdge(e);
		return true;
	}

	@Override
	public boolean removeEdge(EndpointPair<V> endpoints) {
		validateEndpoints(endpoints);
		return removeEdge(endpoints.nodeU(), endpoints.nodeV());
	}

}
