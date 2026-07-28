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
import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * An adapter from a JGAlgo graph to a Guava network.
 *
 * <p>
 * The adapter is constructed with a {@linkplain com.jgalgo.graph.Graph JGAlgo graph} and implements the
 * {@linkplain com.google.common.graph.Network Guava network} interface, and can be used with any Guava algorithm. The
 * adapter is a live view, so any change in the JGAlgo graph is reflected in the Guava network and vice versa, but the
 * underlying JGAlgo graph should not be modified directly.
 *
 * <p>
 * The {@linkplain com.google.common.graph.Network Guava network} is immutable, so as this adapter. A subclass of this
 * adapter, {@link GuavaMutableNetworkAdapter}, is a mutable adapter that implements the
 * {@linkplain com.google.common.graph.MutableNetwork Guava mutable network} interface.
 *
 * <p>
 * The capabilities of the Guava network, such as parallel and self edges or whether the network is directed or
 * undirected, are determined by the underlying JGAlgo graph.
 *
 * <p>
 * Guava support different {@linkplain com.google.common.graph.ElementOrder element orders} for the nodes and the
 * incident edges. This adapter uses the default element order, which is unordered, and cannot be changed as the
 * underlying JGAlgo graph does not support any other order.
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
 * For adapting the other way around, from Guava Network to JGAlgo, see {@link GuavaNetworkWrapper}.
 *
 * @see        com.jgalgo.graph.Graph
 * @see        com.google.common.graph.Network
 * @see        GuavaMutableNetworkAdapter
 * @see        GuavaNetworkWrapper
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class GuavaNetworkAdapter<V, E> extends AbstractNetwork<V, E> {

	final com.jgalgo.graph.Graph<V, E> graph;
	private final ElementOrder<V> nodeOrder = ElementOrder.unordered();
	private final ElementOrder<E> edgeOrder = ElementOrder.unordered();

	/**
	 * Constructs a new adapter from the given JGAlgo graph.
	 *
	 * @param graph the JGAlgo graph
	 */
	public GuavaNetworkAdapter(com.jgalgo.graph.Graph<V, E> graph) {
		this.graph = Objects.requireNonNull(graph);
	}

	@Override
	public Set<V> nodes() {
		return graph.vertices();
	}

	@Override
	public Set<E> edges() {
		return graph.edges();
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
	public Set<E> incidentEdges(V node) {
		return graph.undirectedView().outEdges(node);
	}

	@Override
	public Set<E> inEdges(V node) {
		return graph.inEdges(node);
	}

	@Override
	public Set<E> outEdges(V node) {
		return graph.outEdges(node);
	}

	@Override
	public EndpointPair<V> incidentNodes(E edge) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		int eIdx = graph.indexGraphEdgesMap().idToIndex(edge);
		int uIdx = g.edgeSource(eIdx), vIdx = g.edgeTarget(eIdx);
		V u = viMap.indexToId(uIdx), v = viMap.indexToId(vIdx);
		return g.isDirected() ? EndpointPair.ordered(u, v) : EndpointPair.unordered(u, v);
	}

	@Override
	public Set<E> adjacentEdges(E edge) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<E> eiMap = graph.indexGraphEdgesMap();
		int eIdx = eiMap.idToIndex(edge);
		int uIdx = g.edgeSource(eIdx), vIdx = g.edgeTarget(eIdx);

		IntSet adjEdges = new IntOpenHashSet();
		adjEdges.addAll(g.outEdges(uIdx));
		if (g.isDirected())
			adjEdges.addAll(g.inEdges(uIdx));
		if (uIdx != vIdx) {
			adjEdges.addAll(g.outEdges(vIdx));
			if (g.isDirected())
				adjEdges.addAll(g.inEdges(vIdx));
		}
		boolean removed = adjEdges.remove(eIdx);
		assert removed;

		return IndexIdMaps.indexToIdSet(adjEdges, eiMap);
	}

	@Override
	public Set<E> edgesConnecting(V nodeU, V nodeV) {
		return graph.getEdges(nodeU, nodeV);
	}

	@Override
	public E edgeConnectingOrNull(V nodeU, V nodeV) {
		return graph.getEdge(nodeU, nodeV);
	}

	@Override
	public boolean hasEdgeConnecting(V nodeU, V nodeV) {
		return GuavaAdapters.hasEdgeConnecting(graph, nodeU, nodeV);
	}

	@Override
	public boolean hasEdgeConnecting(EndpointPair<V> endpoints) {
		return GuavaAdapters.hasEdgeConnecting(graph, endpoints);
	}

	@Override
	public boolean isDirected() {
		return graph.isDirected();
	}

	@Override
	public boolean allowsParallelEdges() {
		return graph.isAllowParallelEdges();
	}

	@Override
	public boolean allowsSelfLoops() {
		return graph.isAllowSelfEdges();
	}

	@Override
	public ElementOrder<V> nodeOrder() {
		return nodeOrder;
	}

	@Override
	public ElementOrder<E> edgeOrder() {
		return edgeOrder;
	}

}
