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

import java.util.Objects;
import java.util.Set;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;

/**
 * An adapter from a JGAlgo graph to a Guava graph.
 *
 * <p>
 * The adapter is constructed with a {@linkplain com.jgalgo.graph.Graph JGAlgo graph} and implements the
 * {@linkplain com.google.common.graph.Graph Guava graph} interface, and can be used with any Guava algorithm. The
 * adapter is a live view, so any change in the JGAlgo graph is reflected in the Guava graph and vice versa, but the
 * underlying JGAlgo graph should not be modified directly.
 *
 * <p>
 * The basic {@linkplain com.google.common.graph.Graph Guava graph} is immutable, so as this adapter. A subclass of this
 * adapter, {@link GuavaMutableGraphAdapter}, is a mutable adapter that implements the
 * {@linkplain com.google.common.graph.MutableGraph Guava mutable graph} interface.
 *
 * <p>
 * Parallel edges are not supported by {@linkplain com.google.common.graph.Graph Guava basic graph}, therefore the
 * adapter will throw an {@linkplain UnsupportedOperationException} if the underlying JGAlgo graph allows parallel
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
 * internally for safe access to the underlying JGAlgo graph.
 *
 * <p>
 * Among Guava {@link com.google.common.graph.Graph}, {@link com.google.common.graph.ValueGraph} and
 * {@link com.google.common.graph.Network}, the network is the most similar to JGAlgo graphs, as vertices and edges have
 * unique identifiers, and queries of edges are answered with the edges identifiers. On the other had, the Graph and
 * ValueGraph do not support unique identifiers for the edges, and operations on edges are addressed by a pair of nodes.
 * The ValueGraph does associate a value with each edge, be it does not have to be unique, and it is more similar to
 * weights in JGAlgo graphs.
 *
 * <p>
 * For adapting the other way around, from Guava to JGAlgo, only from {@linkplain com.google.common.graph.Network Guava
 * Network} is supported, see {@link GuavaNetworkWrapper}.
 *
 * @see        com.jgalgo.graph.Graph
 * @see        com.google.common.graph.Graph
 * @see        GuavaMutableGraphAdapter
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class GuavaGraphAdapter<V, E> extends com.google.common.graph.AbstractGraph<V> {

	final com.jgalgo.graph.Graph<V, E> graph;
	private final ElementOrder<V> incidentEdgeOrder = ElementOrder.unordered();
	private final ElementOrder<V> nodeOrder = ElementOrder.unordered();

	/**
	 * Constructs a new adapter from the given JGAlgo graph.
	 *
	 * @param  graph                         the JGAlgo graph
	 * @throws UnsupportedOperationException if the graph allows parallel edges
	 */
	public GuavaGraphAdapter(com.jgalgo.graph.Graph<V, E> graph) {
		this.graph = Objects.requireNonNull(graph);
		if (graph.isAllowParallelEdges())
			throw new UnsupportedOperationException("Parallel edges are not supported by Guava Graph object");
	}

	@Override
	public Set<V> nodes() {
		return graph.vertices();
	}

	@Override
	public Set<EndpointPair<V>> edges() {
		return GuavaAdapters.edgesEndpoints(graph);
	}

	@Override
	public Set<V> successors(V node) {
		return GuavaAdapters.successors(graph, node);
	}

	@Override
	public Set<V> predecessors(V node) {
		return GuavaAdapters.predecessors(graph, node);
	}

	@Override
	public Set<V> adjacentNodes(V node) {
		return GuavaAdapters.adjacentNodes(graph, node);
	}

	@Override
	public boolean hasEdgeConnecting(EndpointPair<V> endpoints) {
		return GuavaAdapters.hasEdgeConnecting(graph, endpoints);
	}

	@Override
	public boolean hasEdgeConnecting(V nodeU, V nodeV) {
		return GuavaAdapters.hasEdgeConnecting(graph, nodeU, nodeV);
	}

	@Override
	public Set<EndpointPair<V>> incidentEdges(V node) {
		return GuavaAdapters.incidentEdges(graph, node);
	}

	@Override
	public int degree(V node) {
		return GuavaAdapters.degree(graph, node);
	}

	@Override
	public int outDegree(V node) {
		return GuavaAdapters.outDegree(graph, node);
	}

	@Override
	public int inDegree(V node) {
		return GuavaAdapters.inDegree(graph, node);
	}

	@Override
	public boolean isDirected() {
		return graph.isDirected();
	}

	@Override
	public boolean allowsSelfLoops() {
		return graph.isAllowSelfEdges();
	}

	@Override
	public ElementOrder<V> incidentEdgeOrder() {
		return incidentEdgeOrder;
	}

	@Override
	public ElementOrder<V> nodeOrder() {
		return nodeOrder;
	}

}
