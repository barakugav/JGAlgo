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
package com.jgalgo.adapt.jgrapht;

import java.util.Set;
import java.util.function.Supplier;
import org.jgrapht.GraphType;
import org.jgrapht.graph.DefaultGraphType;
import com.jgalgo.graph.WeightsDouble;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * An adapter from JGAlgo graph to JGraphT graph.
 *
 * <p>
 * The adapter is constructed with a {@linkplain com.jgalgo.graph.Graph JGAlgo graph} and implements the
 * {@linkplain org.jgrapht.Graph JGraphT graph} interface, and can be used with any JGraphT algorithm. The adapter is a
 * live view, so any change in the JGraphT graph is reflected in the JGAlgo graph and vice versa, but the underlying
 * JGAlgo graph should not be modified directly.
 *
 * <p>
 * The {@link GraphType} of the adapter determined by the capabilities of the underlying JGAlgo graph (see
 * {@link com.jgalgo.graph.Graph#isDirected()}, {@link com.jgalgo.graph.Graph#isAllowParallelEdges()},
 * {@link com.jgalgo.graph.Graph#isAllowSelfEdges()}). Wether the adapter is weighted or not is determined in the
 * {@linkplain #JGraphTAdapter(com.jgalgo.graph.Graph, String) constructor} by passing the edge weight key, see
 * {@link com.jgalgo.graph.Graph#getEdgesWeights(String)}. Although JGAlgo graphs support multiple types of weights,
 * both for vertices and edges, JGraphT graphs support only one double weight type, for edges.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class JGraphTAdapter<V, E> extends org.jgrapht.graph.AbstractGraph<V, E> {

	private final com.jgalgo.graph.Graph<V, E> graph;
	private final WeightsDouble<E> weights;
	private Supplier<V> vertexSupplier;
	private Supplier<E> edgeSupplier;
	private final GraphType type;

	/**
	 * Constructs a new unweighted adapter from the given JGAlgo graph.
	 *
	 * @param graph the JGAlgo graph
	 */
	public JGraphTAdapter(com.jgalgo.graph.Graph<V, E> graph) {
		this(graph, null);
	}

	/**
	 * Constructs a new adapter from the given JGAlgo graph, optionally weighted.
	 *
	 * @param  graph                    the JGAlgo graph
	 * @param  edgeWeightKey            the edge weight key of the {@linkplain WeightsDouble double weights} of the
	 *                                      JGAlgo graph (see {@link com.jgalgo.graph.Graph#getEdgesWeights(String)}),
	 *                                      or {@code null} for unweighted
	 * @throws IllegalArgumentException if the edge weight key is not {@code null} and it is not found in the JGAlgo
	 *                                      graph
	 */
	public JGraphTAdapter(com.jgalgo.graph.Graph<V, E> graph, String edgeWeightKey) {
		this.graph = graph;
		if (edgeWeightKey == null) {
			weights = null;
		} else {
			weights = graph.getEdgesWeights(edgeWeightKey);
			if (weights == null)
				throw new IllegalArgumentException("No edges weights for key '" + edgeWeightKey + "'");
		}

		DefaultGraphType.Builder typeBuilder = new DefaultGraphType.Builder();
		if (graph.isDirected()) {
			typeBuilder.directed();
		} else {
			typeBuilder.undirected();
		}
		typeBuilder.allowMultipleEdges(graph.isAllowParallelEdges());
		typeBuilder.allowSelfLoops(graph.isAllowSelfEdges());
		typeBuilder.allowCycles(true);
		typeBuilder.weighted(weights != null);
		type = typeBuilder.build();
	}

	/**
	 * Set the vertex supplier of the adapter.
	 *
	 * @param vertexSupplier the vertex supplier, or {@code null} to not support adding vertices via
	 *                           {@link #addVertex()}
	 */
	public void setVertexSupplier(Supplier<V> vertexSupplier) {
		this.vertexSupplier = vertexSupplier;
	}

	/**
	 * Set the edge supplier of the adapter.
	 *
	 * @param edgeSupplier the edge supplier, or {@code null} to not support adding edges via
	 *                         {@link #addEdge(Object, Object)}
	 */
	public void setEdgeSupplier(Supplier<E> edgeSupplier) {
		this.edgeSupplier = edgeSupplier;
	}

	@Override
	public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
		if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
			return null;
		return graph.getEdges(sourceVertex, targetVertex);
	}

	@Override
	public E getEdge(V sourceVertex, V targetVertex) {
		if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
			return null;
		return graph.getEdge(sourceVertex, targetVertex);
	}

	@Override
	public Supplier<V> getVertexSupplier() {
		return vertexSupplier;
	}

	@Override
	public Supplier<E> getEdgeSupplier() {
		return edgeSupplier;
	}

	@Override
	public E addEdge(V sourceVertex, V targetVertex) {
		assertVertexExist(sourceVertex);
		assertVertexExist(targetVertex);
		if (edgeSupplier == null)
			throw new UnsupportedOperationException("graph does not have an edge supplier");
		E edge = edgeSupplier.get();
		if (containsEdge(edge))
			return null;
		if (!graph.isAllowParallelEdges() && containsEdge(sourceVertex, targetVertex))
			return null;
		graph.addEdge(sourceVertex, targetVertex, edge);
		return edge;
	}

	@Override
	public boolean addEdge(V sourceVertex, V targetVertex, E e) {
		assertVertexExist(sourceVertex);
		assertVertexExist(targetVertex);
		if (containsEdge(e))
			return false;
		if (!graph.isAllowParallelEdges() && containsEdge(sourceVertex, targetVertex))
			return false;
		graph.addEdge(sourceVertex, targetVertex, e);
		return true;
	}

	@Override
	public V addVertex() {
		if (vertexSupplier == null)
			throw new UnsupportedOperationException("graph does not have an vertex supplier");
		V vertex = vertexSupplier.get();
		graph.addVertex(vertex);
		return vertex;
	}

	@Override
	public boolean addVertex(V v) {
		if (containsVertex(v))
			return false;
		graph.addVertex(v);
		return true;
	}

	@Override
	public boolean containsEdge(V sourceVertex, V targetVertex) {
		return containsVertex(sourceVertex) && containsVertex(targetVertex)
				&& graph.containsEdge(sourceVertex, targetVertex);
	}

	@Override
	public boolean containsEdge(E e) {
		return graph.edges().contains(e);
	}

	@Override
	public boolean containsVertex(V v) {
		return graph.vertices().contains(v);
	}

	@Override
	public Set<E> edgeSet() {
		return graph.edges();
	}

	@Override
	public int degreeOf(V vertex) {
		assertVertexExist(vertex);
		if (graph.isDirected())
			return graph.outEdges(vertex).size() + graph.inEdges(vertex).size();
		if (!graph.isAllowSelfEdges())
			return graph.outEdges(vertex).size();
		/* self edges are counted twice in JGraphT graphs */
		int degree = graph.outEdges(vertex).size();
		for (E edge : graph.outEdges(vertex))
			if (graph.edgeSource(edge).equals(graph.edgeTarget(edge)))
				degree++;
		return degree;
	}

	@Override
	public Set<E> edgesOf(V vertex) {
		assertVertexExist(vertex);
		if (!graph.isDirected())
			return graph.outEdges(vertex);
		return graph.undirectedView().outEdges(vertex);
	}

	@Override
	public int inDegreeOf(V vertex) {
		if (!graph.isDirected())
			return degreeOf(vertex);
		assertVertexExist(vertex);
		return graph.inEdges(vertex).size();
	}

	@Override
	public Set<E> incomingEdgesOf(V vertex) {
		assertVertexExist(vertex);
		return graph.inEdges(vertex);
	}

	@Override
	public int outDegreeOf(V vertex) {
		if (!graph.isDirected())
			return degreeOf(vertex);
		assertVertexExist(vertex);
		return graph.outEdges(vertex).size();
	}

	@Override
	public Set<E> outgoingEdgesOf(V vertex) {
		assertVertexExist(vertex);
		return graph.outEdges(vertex);
	}

	@Override
	public Set<E> removeAllEdges(V sourceVertex, V targetVertex) {
		if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
			return null;
		Set<E> edges = new ObjectOpenHashSet<>(graph.getEdges(sourceVertex, targetVertex));
		removeAllEdges(edges);
		return edges;
	}

	@Override
	public E removeEdge(V sourceVertex, V targetVertex) {
		if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
			return null;
		E edge = graph.getEdge(sourceVertex, targetVertex);
		if (edge == null)
			return null;
		graph.removeEdge(edge);
		return edge;
	}

	@Override
	public boolean removeEdge(E e) {
		if (!containsEdge(e))
			return false;
		graph.removeEdge(e);
		return true;
	}

	@Override
	public boolean removeVertex(V v) {
		if (!containsVertex(v))
			return false;
		graph.removeVertex(v);
		return true;
	}

	@Override
	public Set<V> vertexSet() {
		return graph.vertices();
	}

	@Override
	public V getEdgeSource(E e) {
		return graph.edgeSource(e);
	}

	@Override
	public V getEdgeTarget(E e) {
		return graph.edgeTarget(e);
	}

	@Override
	public GraphType getType() {
		return type;
	}

	@Override
	public double getEdgeWeight(E e) {
		return weights != null ? weights.get(e) : 1.0;
	}

	@Override
	public void setEdgeWeight(E e, double weight) {
		if (weights == null)
			throw new UnsupportedOperationException("graph is unweighted");
		weights.set(e, weight);
	}

}
