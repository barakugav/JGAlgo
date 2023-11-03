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
 * <p>
 * The factory can be used to create new empty graphs, with different options and capabilities. Few methods are
 * available to optimize the graph implementation choice. The factory can also be used to create a copy of an existing
 * graphs, with the same vertices and edges, with/without copying the vertices/edges weights.
 * <p>
 * Both the graph factory and {@link IntGraphBuilder} are used to create new graphs. The difference is that vertices and
 * edges can be added to the builder, which is then used to construct non empty graphs, while the factory is only used
 * to choose a graph implementation and create an empty graph.
 * <p>
 * This interface is a specific version of {@link GraphFactory} for {@link IntGraph}.
 *
 * @see    IntGraphFactory#newDirected()
 * @see    IntGraphFactory#newUndirected()
 * @see    IntGraph
 * @see    IntGraphBuilder
 * @author Barak Ugav
 */
public interface IntGraphFactory extends GraphFactory<Integer, Integer> {

	@Override
	IntGraph newGraph();

	@Override
	default IntGraph newCopyOf(Graph<Integer, Integer> g) {
		return (IntGraph) GraphFactory.super.newCopyOf(g);
	}

	@Override
	IntGraph newCopyOf(Graph<Integer, Integer> g, boolean copyWeights);

	@Override
	IntGraphBuilder newBuilder();

	@Override
	IntGraphFactory setDirected(boolean directed);

	@Override
	IntGraphFactory allowSelfEdges(boolean selfEdges);

	@Override
	IntGraphFactory allowParallelEdges(boolean parallelEdges);

	@Override
	IntGraphFactory expectedVerticesNum(int expectedVerticesNum);

	@Override
	IntGraphFactory expectedEdgesNum(int expectedEdgesNum);

	@Override
	IntGraphFactory addHint(GraphFactory.Hint hint);

	@Override
	IntGraphFactory removeHint(GraphFactory.Hint hint);

	/**
	 * Create an undirected int graph factory.
	 *
	 * @return a new factory that can build undirected int graphs
	 */
	public static IntGraphFactory newUndirected() {
		return new IntGraphImpl.Factory(false);
	}

	/**
	 * Create a directed int graph factory.
	 *
	 * @return a new factory that can build directed int graphs
	 */
	public static IntGraphFactory newDirected() {
		return new IntGraphImpl.Factory(true);
	}

	/**
	 * Create a new graph factory based on a given implementation.
	 * <p>
	 * The new factory will build graphs with the same capabilities (inclusive) as the given graph, possibly choosing to
	 * use a similar implementation. The factory will NOT copy the graph itself (the vertices, edges and weights), for
	 * such use case see {@link IntGraph#copy()} or {@link IntGraphFactory#newCopyOf(Graph)}.
	 *
	 * @param  g a graph from which the factory should copy its capabilities (inclusive)
	 * @return   a new graph factory that will create graphs with the same capabilities (inclusive) of the given graph
	 */
	public static IntGraphFactory newFrom(IntGraph g) {
		return new IntGraphImpl.Factory(g);
	}

	@Override
	default IntGraphFactory setOption(String key, Object value) {
		return (IndexGraphFactory) GraphFactory.super.setOption(key, value);
	}
}
