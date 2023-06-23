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
 * A factory for {@link IndexGraph} objects.
 *
 * @see    IndexGraph#newBuilderDirected()
 * @see    IndexGraph#newBuilderUndirected()
 * @author Barak Ugav
 */
public interface IndexGraphFactory extends BuilderAbstract<IndexGraphFactory> {

	/**
	 * Create a new empty index graph.
	 *
	 * @return a new index graph with the factory options
	 */
	IndexGraph newGraph();

	/**
	 * Create a copy of a given index graph.
	 * <p>
	 * An identical copy of the given graph will be created, with the same vertices, edges and weights. The returned
	 * Graph will always be modifiable, with no side affects on the original graph.
	 * <p>
	 * Differing from {@link IndexGraph#copy()}, the capabilities of the new graph are determined by the factory
	 * configuration, rather than copied from the given graph. Note for example that if the factory chooses to use an
	 * implementation that does not (have to) support self edges (if {@link #allowSelfEdges(boolean)} was not called
	 * with {@code true}), attempting to create a copy of a graph that does contains self edges will result in an
	 * exception.
	 *
	 * @param  g the original graph to copy
	 * @return   an identical copy of the given graph
	 */
	IndexGraph newCopyOf(IndexGraph g);

	/**
	 * Determine if graphs built by this factory should be directed or not.
	 *
	 * @param  directed if {@code true}, graphs built by this factory will be directed
	 * @return          this factory
	 */
	IndexGraphFactory setDirected(boolean directed);

	/**
	 * Determine if graphs built by this factory should be support self edges.
	 *
	 * @param  selfEdges if {@code true}, graphs built by this factory will support self edges
	 * @return           this factory
	 */
	IndexGraphFactory allowSelfEdges(boolean selfEdges);

	/**
	 * Determine if graphs built by this factory should be support parallel edges.
	 *
	 * @param  parallelEdges if {@code true}, graphs built by this factory will support parallel edges
	 * @return               this factory
	 */
	IndexGraphFactory allowParallelEdges(boolean parallelEdges);

	/**
	 * Set the expected number of vertices that will exist in the graph.
	 *
	 * @param  expectedVerticesNum the expected number of vertices in the graph
	 * @return                     this factory
	 */
	IndexGraphFactory expectedVerticesNum(int expectedVerticesNum);

	/**
	 * Set the expected number of edges that will exist in the graph.
	 *
	 * @param  expectedEdgesNum the expected number of edges in the graph
	 * @return                  this factory
	 */
	IndexGraphFactory expectedEdgesNum(int expectedEdgesNum);

	/**
	 * Add a hint to this factory.
	 * <p>
	 * Hints do not change the behavior of the graphs built by this factory, by may affect performance.
	 *
	 * @param  hint the hint to add
	 * @return      this factory
	 */
	IndexGraphFactory addHint(GraphFactory.Hint hint);

	/**
	 * Remove a hint from this factory.
	 * <p>
	 * Hints do not change the behavior of the graphs built by this factory, by may affect performance.
	 *
	 * @param  hint the hint to remove
	 * @return      this factory
	 */
	IndexGraphFactory removeHint(GraphFactory.Hint hint);

	/**
	 * Create an undirected index graph factory.
	 * <p>
	 * This is the recommended way to instantiate a new undirected index graph.
	 *
	 * @return a new factory that can build undirected index graphs
	 */
	public static IndexGraphFactory newUndirected() {
		return new IndexGraphFactoryImpl(false);
	}

	/**
	 * Create a directed index graph factory.
	 * <p>
	 * This is the recommended way to instantiate a new directed index graph.
	 *
	 * @return a new factory that can build directed index graphs
	 */
	public static IndexGraphFactory newDirected() {
		return new IndexGraphFactoryImpl(true);
	}

	/**
	 * Create a new index graph factory based on a given implementation.
	 * <p>
	 * The new factory will build graphs with the same capabilities as the given graph, possibly choosing to use a
	 * similar implementation. The factory will NOT copy the graph itself (the vertices, edges and weights), for such
	 * use case see {@link IndexGraph#copy()} and {@link IndexGraphFactory#newCopyOf(IndexGraph)}.
	 *
	 * @param  g a graph from which the factory should copy its capabilities
	 * @return   a new graph factory that will create graphs with the same capabilities of the given graph
	 */
	public static IndexGraphFactory newFrom(IndexGraph g) {
		return new IndexGraphFactoryImpl(g);
	}

}
