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

/**
 * A factory for {@link Graph} objects.
 *
 * <p>
 * The factory can be used to create new empty graphs, with different options and capabilities. Few methods are
 * available to optimize the graph implementation choice. The factory can also be used to create a copy of an existing
 * graphs, with the same vertices and edges, with/without copying the vertices/edges weights.
 *
 * <p>
 * Both the graph factory and {@link GraphBuilder} are used to create new graphs. The difference is that vertices and
 * edges can be added to the builder, which is then used to construct non empty graphs, while the factory is only used
 * to choose a graph implementation and create an empty graph.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @see        GraphFactory#directed()
 * @see        GraphFactory#undirected()
 * @see        Graph
 * @see        GraphBuilder
 * @author     Barak Ugav
 */
public interface GraphFactory<V, E> {

	/**
	 * Create a new empty graph.
	 *
	 * @return a new graph with the factory options
	 */
	Graph<V, E> newGraph();

	/**
	 * Create a copy of a given graph, with the same vertices and edges, without copying weights.
	 *
	 * <p>
	 * An identical copy of the given graph will be created, with the same vertices and edges, without copying the
	 * vertices/edges weights. The returned Graph will always be modifiable, with no side affects on the original graph.
	 *
	 * <p>
	 * Differing from {@link Graph#copy()}, the capabilities of the new graph are determined by the factory
	 * configuration, rather than copied from the given graph. Note for example that if the factory chooses to use an
	 * implementation that does not (have to) support self edges (if {@link #allowSelfEdges(boolean)} was not called
	 * with {@code true}), attempting to create a copy of a graph that does contains self edges will result in an
	 * exception.
	 *
	 * @param  g the original graph to copy
	 * @return   an identical copy of the given graph, with the same vertices and edges, without the original graph
	 *           weights
	 */
	default Graph<V, E> newCopyOf(Graph<V, E> g) {
		return newCopyOf(g, false, false);
	}

	/**
	 * Create a copy of a given graph, with the same vertices and edges, with/without copying weights.
	 *
	 * <p>
	 * An identical copy of the given graph will be created, with the same vertices and edges, with/without copying the
	 * vertices/edges weights. The returned Graph will always be modifiable, with no side affects on the original graph.
	 *
	 * <p>
	 * Differing from {@link Graph#copy(boolean, boolean)}, the capabilities of the new graph are determined by the
	 * factory configuration, rather than copied from the given graph. Note for example that if the factory chooses to
	 * use an implementation that does not (have to) support self edges (if {@link #allowSelfEdges(boolean)} was not
	 * called with {@code true}), attempting to create a copy of a graph that does contains self edges will result in an
	 * exception.
	 *
	 * @param  g                   the original graph to copy
	 * @param  copyVerticesWeights if {@code true}, the weights of the vertices will be copied to the new graph
	 * @param  copyEdgesWeights    if {@code true}, the weights of the edges will be copied to the new graph
	 * @return                     an identical copy of the given graph, with the same vertices and edges, with/without
	 *                             the original graph weights
	 */
	Graph<V, E> newCopyOf(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights);

	/**
	 * Create a new graph builder with the factory parameters.
	 *
	 * <p>
	 * The created builder can be used to add vertices and edges, and then build a (mutable or immutable) non empty
	 * graph, differing from the factory which only builds empty graphs. The capabilities such as un/directed, support
	 * of self edges, support of parallel edges, and hints such as expected number of vertices and edges, other
	 * {@linkplain GraphFactory.Hint hints}, etc. are copied from the factory to the builder.
	 *
	 * @return a new graph builder with the factory parameters
	 */
	GraphBuilder<V, E> newBuilder();

	/**
	 * Create a new graph builder with the factory parameters initialized with an existing graph vertices and edges,
	 * without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  g a graph
	 * @return   a graph builder with the factory parameters initialized with the given graph vertices and edges,
	 *           without the original graph vertices/edges weights.
	 */
	default GraphBuilder<V, E> newBuilderCopyOf(Graph<V, E> g) {
		return newBuilderCopyOf(g, false, false);
	}

	/**
	 * Create a new graph builder with the factory parameters initialized with an existing graph vertices and edges,
	 * with/without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * @param  g                   a graph
	 * @param  copyVerticesWeights if {@code true}, the weights of the vertices will be copied from the graph to the
	 *                                 builder
	 * @param  copyEdgesWeights    if {@code true}, the weights of the edges will be copied from the graph to the
	 *                                 builder
	 * @return                     a graph builder with the factory parameters initialized with the given graph vertices
	 *                             and edges, with/without the original graph vertices/edges weights.
	 */
	GraphBuilder<V, E> newBuilderCopyOf(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights);

	/**
	 * Change the config of this factory so that the built graphs will support self edges.
	 *
	 * <p>
	 * By default, graphs built by this factory will not support self edges.
	 *
	 * @return this factory
	 */
	default GraphFactory<V, E> allowSelfEdges() {
		return allowSelfEdges(true);
	}

	/**
	 * Determine if graphs built by this factory should be support self edges.
	 *
	 * <p>
	 * By default, graphs built by this factory will not support self edges.
	 *
	 * @param  selfEdges if {@code true} graphs built by this factory will support self edges, else they will not
	 * @return           this factory
	 */
	GraphFactory<V, E> allowSelfEdges(boolean selfEdges);

	/**
	 * Change the config of this factory so that the built graphs will support parallel edges.
	 *
	 * <p>
	 * By default, graphs built by this factory will support parallel edges. The reason is that in order to enforce no
	 * parallel edges are added to the graph, an efficient lookup of edges (keyed by an edge's vertices) is required,
	 * which is an operation most graph algorithms do not use and therefore most implementations are not optimized for.
	 * See {@link GraphFactory.Hint#FastEdgeLookup}.
	 *
	 * @return this factory
	 */
	default GraphFactory<V, E> allowParallelEdges() {
		return allowParallelEdges(true);
	}

	/**
	 * Determine if graphs built by this factory should be support parallel edges.
	 *
	 * <p>
	 * By default, graphs built by this factory will support parallel edges. The reason is that in order to enforce no
	 * parallel edges are added to the graph, an efficient lookup of edges (keyed by an edge's vertices) is required,
	 * which is an operation most graph algorithms do not use and therefore most implementations are not optimized for.
	 * See {@link GraphFactory.Hint#FastEdgeLookup}.
	 *
	 * @param  parallelEdges if {@code true} graphs built by this factory will support parallel edges, else they will
	 *                           not
	 * @return               this factory
	 */
	GraphFactory<V, E> allowParallelEdges(boolean parallelEdges);

	/**
	 * Set the expected number of vertices that will exist in the graph.
	 *
	 * @param  expectedVerticesNum the expected number of vertices in the graph
	 * @return                     this factory
	 */
	GraphFactory<V, E> expectedVerticesNum(int expectedVerticesNum);

	/**
	 * Set the expected number of edges that will exist in the graph.
	 *
	 * @param  expectedEdgesNum the expected number of edges in the graph
	 * @return                  this factory
	 */
	GraphFactory<V, E> expectedEdgesNum(int expectedEdgesNum);

	/**
	 * Add a hint to this factory.
	 *
	 * <p>
	 * Hints do not change the behavior of the graphs built by this factory, by may affect performance.
	 *
	 * @param  hint the hint to add
	 * @return      this factory
	 */
	GraphFactory<V, E> addHint(GraphFactory.Hint hint);

	/**
	 * Remove a hint from this factory.
	 *
	 * <p>
	 * Hints do not change the behavior of the graphs built by this factory, by may affect performance.
	 *
	 * @param  hint the hint to remove
	 * @return      this factory
	 */
	GraphFactory<V, E> removeHint(GraphFactory.Hint hint);

	/**
	 * Hints for a graph factory.
	 *
	 * <p>
	 * Hints do not change the behavior of the graphs built by this factory, by may affect performance.
	 *
	 * @author Barak Ugav
	 */
	static enum Hint {

		/** The graph should support fast edge removal via {@link Graph#removeEdge(Object)}. */
		FastEdgeRemoval,

		/**
		 * The graph should support fast edge lookup via {@link Graph#getEdge(Object, Object)} and
		 * {@link Graph#getEdges(Object, Object)}.
		 */
		FastEdgeLookup,

		/** The graph density (# of edges) will be high, a constant fraction of \(O(n^2)\). */
		DenseGraph,
	}

	/**
	 * Create an undirected graph factory.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new factory that can build undirected graphs
	 */
	public static <V, E> GraphFactory<V, E> undirected() {
		return new GraphImpl.Factory<>(false);
	}

	/**
	 * Create a directed graph factory.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new factory that can build directed graphs
	 */
	public static <V, E> GraphFactory<V, E> directed() {
		return new GraphImpl.Factory<>(true);
	}

	/**
	 * Create a new un/directed graph factory.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  directed whether the graphs created by the factory should be directed
	 * @return          a new factory that can build un/directed graphs
	 */
	public static <V, E> GraphFactory<V, E> newInstance(boolean directed) {
		return new GraphImpl.Factory<>(directed);
	}

	/**
	 * <b>[TL;DR Don't call me!]</b> Set an option.
	 *
	 * <p>
	 * The builder might support different options to customize its implementation. These options never change the
	 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
	 * because they are not part of the API and may change in the future.
	 *
	 * <p>
	 * These options are mainly for debug and benchmark purposes.
	 *
	 * @param  key   the option key
	 * @param  value the option value
	 * @return       this builder
	 */
	default GraphFactory<V, E> setOption(String key, Object value) {
		throw new IllegalArgumentException("unknown option key: " + key);
	}
}
