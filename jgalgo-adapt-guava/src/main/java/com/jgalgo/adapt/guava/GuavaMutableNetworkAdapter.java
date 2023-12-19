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
import com.google.common.graph.MutableNetwork;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;

/**
 * An adapter from a JGAlgo graph to a Guava network.
 *
 * <p>
 * The adapter is constructed with a {@linkplain com.jgalgo.graph.Graph JGAlgo graph} and implements the
 * {@linkplain com.google.common.graph.MutableNetwork mutable Guava network} interface, and can be used with any Guava
 * algorithm. The adapter is a live view, so any change in the JGAlgo graph is reflected in the Guava network and vice
 * versa, but the underlying JGAlgo graph should not be modified directly.
 *
 *
 * <p>
 * The {@linkplain com.google.common.graph.MutableNetwork mutable Guava network} is a mutable variant of the
 * {@linkplain com.google.common.graph.Network Guava value graph}. If mutability is not required, consider using the
 * {@linkplain GuavaNetworkAdapter immutable adapter} instead.
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
 * @see        com.jgalgo.graph.Graph
 * @see        com.google.common.graph.MutableNetwork
 * @see        GuavaNetworkAdapter
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class GuavaMutableNetworkAdapter<V, E> extends GuavaNetworkAdapter<V, E> implements MutableNetwork<V, E> {

	/**
	 * Constructs a new mutable adapter from the given JGAlgo graph.
	 *
	 * @param graph the JGAlgo graph
	 */
	public GuavaMutableNetworkAdapter(Graph<V, E> graph) {
		super(graph);
	}

	@Override
	public boolean addNode(V node) {
		return GuavaAdapters.addNode(graph, node);
	}

	private EndpointPair<V> endpoints(V u, V v) {
		return graph.isDirected() ? EndpointPair.ordered(u, v) : EndpointPair.unordered(u, v);
	}

	@Override
	public boolean addEdge(V nodeU, V nodeV, E edge) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		IndexIdMap<E> eiMap = graph.indexGraphEdgesMap();

		int existingIdx = eiMap.idToIndexIfExist(edge);
		if (existingIdx >= 0) {
			V u = viMap.indexToId(g.edgeSource(existingIdx));
			V v = viMap.indexToId(g.edgeTarget(existingIdx));
			if (!endpoints(u, v).equals(endpoints(nodeU, nodeV)))
				throw new IllegalArgumentException("Edge " + edge + " already exists between the following nodes: "
						+ endpoints(u, v) + ", " + "so it cannot be reused to connect the following nodes: "
						+ endpoints(nodeU, nodeV) + ".");
			return false;
		}

		addNode(nodeU);
		addNode(nodeV);
		graph.addEdge(nodeU, nodeV, edge);
		return true;
	}

	@Override
	public boolean addEdge(EndpointPair<V> endpoints, E edge) {
		validateEndpoints(endpoints);
		return addEdge(endpoints.nodeU(), endpoints.nodeV(), edge);
	}

	@Override
	public boolean removeNode(V node) {
		return GuavaAdapters.removeNode(graph, node);
	}

	@Override
	public boolean removeEdge(E edge) {
		if (!graph.edges().contains(edge))
			return false;
		graph.removeEdge(edge);
		return true;
	}

}
