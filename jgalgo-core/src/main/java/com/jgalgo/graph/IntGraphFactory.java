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
 * A factory for {@link IntGraph} objects.
 *
 * @see    IntGraphFactory#newDirected()
 * @see    IntGraphFactory#newUndirected()
 * @author Barak Ugav
 */
public interface IntGraphFactory {

	/**
	 * Create a new empty graph.
	 *
	 * @return a new graph with the factory options
	 */
	IntGraph newGraph();

	/**
	 * Create a copy of a given graph, with the same vertices and edges, without copying weights.
	 * <p>
	 * An identical copy of the given graph will be created, with the same vertices and edges, without copying the
	 * vertices/edges weights. The returned Graph will always be modifiable, with no side affects on the original graph.
	 * <p>
	 * Differing from {@link IntGraph#copy()}, the capabilities of the new graph are determined by the factory
	 * configuration, rather than copied from the given graph. Note for example that if the factory chooses to use an
	 * implementation that does not (have to) support self edges (if {@link #allowSelfEdges(boolean)} was not called
	 * with {@code true}), attempting to create a copy of a graph that does contains self edges will result in an
	 * exception.
	 *
	 * @param  g the original graph to copy
	 * @return   an identical copy of the given graph, with the same vertices and edges, without the original graph
	 *           weights
	 */
	default IntGraph newCopyOf(IntGraph g) {
		return newCopyOf(g, false);
	}

	/**
	 * Create a copy of a given graph, with the same vertices and edges, with/without copying weights.
	 * <p>
	 * An identical copy of the given graph will be created, with the same vertices and edges, with/without copying the
	 * vertices/edges weights. The returned Graph will always be modifiable, with no side affects on the original graph.
	 * <p>
	 * Differing from {@link IntGraph#copy(boolean)}, the capabilities of the new graph are determined by the factory
	 * configuration, rather than copied from the given graph. Note for example that if the factory chooses to use an
	 * implementation that does not (have to) support self edges (if {@link #allowSelfEdges(boolean)} was not called
	 * with {@code true}), attempting to create a copy of a graph that does contains self edges will result in an
	 * exception.
	 *
	 * @param  g           the original graph to copy
	 * @param  copyWeights if {@code true}, the weights of the vertices and edges will be copied to the new graph
	 * @return             an identical copy of the given graph, with the same vertices and edges, with/without the
	 *                     original graph weights
	 */
	IntGraph newCopyOf(IntGraph g, boolean copyWeights);

	/**
	 * Determine if graphs built by this factory should be directed or not.
	 *
	 * @param  directed if {@code true}, graphs built by this factory will be directed
	 * @return          this factory
	 */
	IntGraphFactory setDirected(boolean directed);

	/**
	 * Determine if graphs built by this factory should be support self edges.
	 *
	 * @param  selfEdges if {@code true}, graphs built by this factory will support self edges
	 * @return           this factory
	 */
	IntGraphFactory allowSelfEdges(boolean selfEdges);

	/**
	 * Determine if graphs built by this factory should be support parallel edges.
	 *
	 * @param  parallelEdges if {@code true}, graphs built by this factory will support parallel edges
	 * @return               this factory
	 */
	IntGraphFactory allowParallelEdges(boolean parallelEdges);

	/**
	 * Set the expected number of vertices that will exist in the graph.
	 *
	 * @param  expectedVerticesNum the expected number of vertices in the graph
	 * @return                     this factory
	 */
	IntGraphFactory expectedVerticesNum(int expectedVerticesNum);

	/**
	 * Set the expected number of edges that will exist in the graph.
	 *
	 * @param  expectedEdgesNum the expected number of edges in the graph
	 * @return                  this factory
	 */
	IntGraphFactory expectedEdgesNum(int expectedEdgesNum);

	/**
	 * Add a hint to this factory.
	 * <p>
	 * Hints do not change the behavior of the graphs built by this factory, by may affect performance.
	 *
	 * @param  hint the hint to add
	 * @return      this factory
	 */
	IntGraphFactory addHint(IntGraphFactory.Hint hint);

	/**
	 * Remove a hint from this factory.
	 * <p>
	 * Hints do not change the behavior of the graphs built by this factory, by may affect performance.
	 *
	 * @param  hint the hint to remove
	 * @return      this factory
	 */
	IntGraphFactory removeHint(IntGraphFactory.Hint hint);

	/**
	 * Hints for a graph factory.
	 * <p>
	 * Hints do not change the behavior of the graphs built by this factory, by may affect performance.
	 *
	 * @author Barak Ugav
	 */
	static enum Hint {
		/** The graph should support fast edge removal via {@link IntGraph#removeEdge(int)} */
		FastEdgeRemoval,
		/** The graph should support fast edge lookup via {@link IntGraph#getEdge(int, int)} */
		FastEdgeLookup,
		/** The graph density (# of edges) will be high, a constant fraction of \(O(n^2)\) */
		DenseGraph,
	}

	/**
	 * Create an undirected graph factory.
	 * <p>
	 * This is the recommended way to instantiate a new undirected graph.
	 *
	 * @return a new factory that can build undirected graphs
	 */
	public static IntGraphFactory newUndirected() {
		return new IntGraphImpl.Factory(false);
	}

	/**
	 * Create a directed graph factory.
	 * <p>
	 * This is the recommended way to instantiate a new directed graph.
	 *
	 * @return a new factory that can build directed graphs
	 */
	public static IntGraphFactory newDirected() {
		return new IntGraphImpl.Factory(true);
	}

	/**
	 * Create a new graph factory based on a given implementation.
	 * <p>
	 * The new factory will build graphs with the same capabilities (inclusive) as the given graph, possibly choosing to
	 * use a similar implementation. The factory will NOT copy the graph itself (the vertices, edges and weights), for
	 * such use case see {@link IntGraph#copy()} or {@link IntGraphFactory#newCopyOf(IntGraph)}.
	 *
	 * @param  g a graph from which the factory should copy its capabilities (inclusive)
	 * @return   a new graph factory that will create graphs with the same capabilities (inclusive) of the given graph
	 */
	public static IntGraphFactory newFrom(IntGraph g) {
		return new IntGraphImpl.Factory(g);
	}

	/**
	 * <b>[TL;DR Don't call me!]</b> Set an option.
	 * <p>
	 * The builder might support different options to customize its implementation. These options never change the
	 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
	 * because they are not part of the API and may change in the future.
	 * <p>
	 * These options are mainly for debug and benchmark purposes.
	 *
	 * @param  key   the option key
	 * @param  value the option value
	 * @return       this builder
	 */
	default IntGraphFactory setOption(String key, Object value) {
		throw new IllegalArgumentException("unknown option key: " + key);
	}
}
