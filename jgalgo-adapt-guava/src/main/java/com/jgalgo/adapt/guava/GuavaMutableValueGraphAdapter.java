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
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;

/**
 * An adapter from a JGAlgo graph to a mutable Guava value graph.
 *
 * <p>
 * The adapter is constructed with a {@linkplain com.jgalgo.graph.Graph JGAlgo graph} and implements the
 * {@linkplain com.google.common.graph.MutableValueGraph mutable Guava value graph} interface, and can be used with any
 * Guava algorithm. The adapter is a live view, so any change in the JGAlgo graph is reflected in the Guava graph and
 * vice versa, but the underlying JGAlgo graph should not be modified directly.
 *
 * <p>
 * The {@linkplain com.google.common.graph.MutableValueGraph mutable Guava graph} is a mutable variant of the
 * {@linkplain com.google.common.graph.ValueGraph Guava value graph}. If mutability is not required, consider using the
 * {@linkplain GuavaValueGraphAdapter immutable adapter} instead.
 *
 * <p>
 * Parallel edges are not supported by {@linkplain com.google.common.graph.MutableValueGraph Guava value graph},
 * therefore the adapter will throw an {@linkplain UnsupportedOperationException} if the underlying JGAlgo graph allows
 * parallel edges. Whether this graph is directed or not, and whether it support self edges or not, is determined by the
 * underlying JGAlgo graph.
 *
 * <p>
 * The {@linkplain com.google.common.graph.MutableValueGraph Guava value graph} represent connections between nodes with
 * value for each connection. Each connection has a non unique value, similar to a map from the endpoints to the value.
 * The values of the edges are represented as {@linkplain com.jgalgo.graph.Weights weights} in the underlying JGAlgo
 * graph, and the weights key is passed in the
 * {@linkplain #GuavaMutableValueGraphAdapter(com.jgalgo.graph.Graph, String) constructor}, see
 * {@linkplain com.jgalgo.graph.Graph#getEdgesWeights(String)}. The type of the value is specified as a generic
 * {@code ValueT} parameter. The edge generic type {@code E} is not reflected in the Guava graph, and is only used
 * internally for safe access to the underlying JGAlgo graph. To create new connections (edges) in the graph, the
 * adapter must be able to create a new identifier for the created edge in the JGAlgo graph. This is done via the
 * {@linkplain com.jgalgo.graph.Graph#edgeBuilder() edge builder}, which is a requirement for creating a mutable
 * adapter.
 *
 * <p>
 * Guava support different {@linkplain com.google.common.graph.ElementOrder element orders} for the nodes and the
 * incident edges. This adapter uses the default element order, which is unordered, and cannot be changed as the
 * underlying JGAlgo graph does not support any other order.
 *
 * @see             com.jgalgo.graph.Graph
 * @see             com.google.common.graph.MutableValueGraph
 * @see             GuavaValueGraphAdapter
 * @param  <V>      the vertices type
 * @param  <E>      the edges type
 * @param  <ValueT> the values type
 * @author          Barak Ugav
 */
public class GuavaMutableValueGraphAdapter<V, E, ValueT> extends GuavaValueGraphAdapter<V, E, ValueT>
		implements MutableValueGraph<V, ValueT> {

	/**
	 * Constructs a new adapter from the given JGAlgo graph.
	 *
	 * <p>
	 * An {@linkplain com.jgalgo.graph.Graph#edgeBuilder() edge builder} is required, as the adapter will need to create
	 * edge identifiers in the JGAlgo graph to add new connections in the Guava graph.
	 *
	 * @param  graph                         the JGAlgo graph
	 * @param  edgeWeightKey                 the key of the weights of the edges, which are the values of the value
	 *                                           graph
	 * @throws IllegalArgumentException      if the graph does not have edge builder or if the graph does not contain
	 *                                           edge weights for the given key
	 * @throws UnsupportedOperationException if the graph allows parallel edges
	 */
	public GuavaMutableValueGraphAdapter(com.jgalgo.graph.Graph<V, E> graph, String edgeWeightKey) {
		super(graph, edgeWeightKey);
		if (graph.edgeBuilder() == null)
			throw new IllegalArgumentException("Graph must have edge builder");
	}

	@Override
	public boolean addNode(V node) {
		if (graph.vertices().contains(Objects.requireNonNull(node)))
			return false;
		graph.addVertex(node);
		return true;
	}

	@Override
	public ValueT putEdgeValue(V nodeU, V nodeV, ValueT value) {
		boolean verticesAdded = false;
		verticesAdded |= addNode(nodeU);
		verticesAdded |= addNode(nodeV);
		E edge;
		ValueT oldVal;
		if (!verticesAdded && (edge = graph.getEdge(nodeU, nodeV)) != null) {
			oldVal = weights.getAsObj(edge);
		} else {
			edge = graph.addEdge(nodeU, nodeV);
			oldVal = null;
		}
		weights.setAsObj(edge, value);
		return oldVal;
	}

	@Override
	public ValueT putEdgeValue(EndpointPair<V> endpoints, ValueT value) {
		validateEndpoints(endpoints);
		return putEdgeValue(endpoints.nodeU(), endpoints.nodeV(), value);
	}

	@Override
	public boolean removeNode(V node) {
		if (!graph.vertices().contains(Objects.requireNonNull(node)))
			return false;
		graph.removeVertex(node);
		return true;
	}

	@Override
	public ValueT removeEdge(V nodeU, V nodeV) {
		E e = GuavaAdapters.getEdge(graph, nodeU, nodeV);
		if (e == null)
			return null;
		ValueT oldVal = weights.getAsObj(e);
		graph.removeEdge(e);
		return oldVal;
	}

	@Override
	public ValueT removeEdge(EndpointPair<V> endpoints) {
		validateEndpoints(endpoints);
		return removeEdge(endpoints.nodeU(), endpoints.nodeV());
	}

}
