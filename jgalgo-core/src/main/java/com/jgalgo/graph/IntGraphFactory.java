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
 * <p>
 * The factory can be used to create new empty graphs, with different options and capabilities. Few methods are
 * available to optimize the graph implementation choice. The factory can also be used to create a copy of an existing
 * graphs, with the same vertices and edges, with/without copying the vertices/edges weights.
 *
 * <p>
 * Both the graph factory and {@link IntGraphBuilder} are used to create new graphs. The difference is that vertices and
 * edges can be added to the builder, which is then used to construct non empty graphs, while the factory is only used
 * to choose a graph implementation and create an empty graph.
 *
 * <p>
 * This interface is a specific version of {@link GraphFactory} for {@link IntGraph}.
 *
 * @see    IntGraphFactory#directed()
 * @see    IntGraphFactory#undirected()
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
	IntGraph newCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights);

	@Override
	IntGraphBuilder newBuilder();

	@Override
	default IntGraphBuilder newBuilderCopyOf(Graph<Integer, Integer> g) {
		return (IntGraphBuilder) GraphFactory.super.newBuilderCopyOf(g);
	}

	@Override
	IntGraphBuilder newBuilderCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights);

	@Override
	default IntGraphFactory allowSelfEdges() {
		return (IntGraphFactory) GraphFactory.super.allowSelfEdges();
	}

	@Override
	IntGraphFactory allowSelfEdges(boolean selfEdges);

	@Override
	default IntGraphFactory allowParallelEdges() {
		return (IntGraphFactory) GraphFactory.super.allowParallelEdges();
	}

	@Override
	IntGraphFactory allowParallelEdges(boolean parallelEdges);

	@Override
	IntGraphFactory expectedVerticesNum(int expectedVerticesNum);

	@Override
	IntGraphFactory expectedEdgesNum(int expectedEdgesNum);

	/**
	 * {@inheritDoc}
	 *
	 * By default, graphs created by this factory will have a vertex builder that uses a counter and assign the next id
	 * to each new vertex by incrementing the counter until there is no vertex with that id.
	 */
	@Override
	IntGraphFactory setVertexBuilder(IdBuilder<Integer> vertexBuilder);

	/**
	 * {@inheritDoc}
	 *
	 * By default, graphs created by this factory will have an edge builder that uses a counter and assign the next id
	 * to each new edge by incrementing the counter until there is no edge with that id.
	 */
	@Override
	IntGraphFactory setEdgeBuilder(IdBuilder<Integer> edgeBuilder);

	@Override
	IntGraphFactory addHint(GraphFactory.Hint hint);

	@Override
	IntGraphFactory removeHint(GraphFactory.Hint hint);

	/**
	 * Create an undirected int graph factory.
	 *
	 * @return a new factory that can build undirected int graphs
	 */
	public static IntGraphFactory undirected() {
		return new IntGraphFactoryImpl(false);
	}

	/**
	 * Create a directed int graph factory.
	 *
	 * @return a new factory that can build directed int graphs
	 */
	public static IntGraphFactory directed() {
		return new IntGraphFactoryImpl(true);
	}

	/**
	 * Create a new un/directed int graph factory.
	 *
	 * @param  directed whether the graphs created by the factory should be directed
	 * @return          a new factory that can build un/directed int graphs
	 */
	public static IntGraphFactory newInstance(boolean directed) {
		return new IntGraphFactoryImpl(directed);
	}

	@Override
	default IntGraphFactory setOption(String key, Object value) {
		return (IndexGraphFactory) GraphFactory.super.setOption(key, value);
	}
}
