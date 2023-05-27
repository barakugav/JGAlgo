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
package com.jgalgo;

/**
 * A builder for {@link Graph} objects.
 *
 * @author Barak Ugav
 */
public interface GraphBuilder extends BuilderAbstract<GraphBuilder> {

	/**
	 * Create a new empty graph.
	 *
	 * @return a new graph with the builder options
	 */
	Graph build();

	/**
	 * Create an undirected graph builder.
	 * <p>
	 * This is the recommended way to instantiate a new undirected graph.
	 *
	 * @return a new builder that can build undirected graphs
	 */
	static GraphBuilder newUndirected() {
		return new GraphBuilderImpl(false);
	}

	/**
	 * Create a directed graph builder.
	 * <p>
	 * This is the recommended way to instantiate a new directed graph.
	 *
	 * @return a new builder that can build directed graphs
	 */
	static GraphBuilder newDirected() {
		return new GraphBuilderImpl(true);
	}

	/**
	 * Determine if graphs built by this builder should be directed or not.
	 *
	 * @param  directed if {@code true}, graphs built by this builder will be directed
	 * @return          this builder
	 */
	GraphBuilder setDirected(boolean directed);

	/**
	 * Set the expected number of vertices that will exist in the graph.
	 *
	 * @param  expectedVerticesNum the expected number of vertices in the graph
	 * @return                     this builder
	 */
	GraphBuilder expectedVerticesNum(int expectedVerticesNum);

	/**
	 * Set the expected number of edges that will exist in the graph.
	 *
	 * @param  expectedEdgesNum the expected number of edges in the graph
	 * @return                     this builder
	 */
	GraphBuilder expectedEdgesNum(int expectedEdgesNum);

	/**
	 * Enable/disable fixed edges IDs for graphs built by this builder.
	 * <p>
	 * By default, IDs of both vertices and edges are always {@code 0,1,2,...,verticesNum-1} (and
	 * {@code 0,1,2,...edgesNum-1}). To maintain this invariant, graphs must rename vertices/edges when an vertex/edge
	 * is removed. The IDs of the edges can be fixed, namely once an edge is assigned an ID, it will never change. If
	 * such option is chosen, the edges IDs will not always be {@code 0,1,2,...,edgesNum-1}.
	 * <p>
	 * Note that by using fixed IDs, some map is required, and therefore its slightly less efficient.
	 *
	 * @param  enable if {@code true}, graphs built by this builder will have fixed IDs for edges
	 * @return        this builder
	 * @see           IDStrategy
	 */
	GraphBuilder useFixedEdgesIDs(boolean enable);

	/**
	 * Add a hint to this builder.
	 * <p>
	 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
	 *
	 * @param  hint the hint to add
	 * @return      this builder
	 */
	GraphBuilder addHint(GraphBuilder.Hint hint);

	/**
	 * Remove a hint from this builder.
	 * <p>
	 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
	 *
	 * @param  hint the hint to remove
	 * @return      this builder
	 */
	GraphBuilder removeHint(GraphBuilder.Hint hint);

	/**
	 * Hints for a graph builder.
	 * <p>
	 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
	 *
	 * @author Barak Ugav
	 */
	static enum Hint {
		/** The graph should support fast edge removal via {@link Graph#removeEdge(int)} */
		FastEdgeRemoval,
		/** The graph should support fast edge lookup via {@link Graph#getEdge(int, int)} */
		FastEdgeLookup,
	}

}
