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
	 * Create a new graph.
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
	 * Set the number of initial vertices in the graph.
	 * <p>
	 * The default value is zero.
	 *
	 * @param  n number of initial vertices in the graph
	 * @return   this builder
	 */
	GraphBuilder setVerticesNum(int n);

	/**
	 * Set the edges ID strategy of this builder.
	 * <p>
	 * The default strategy used by this builder is {@link IDStrategy.Continues}, namely the edges IDs will always be
	 * {@code [0,1,2,...,edgesNum-1]}. This default strategy may perform some IDs rename to maintain its invariant
	 * during the lifetime of the graph. A different strategy such as {@link IDStrategy.Fixed} may be used to ensure no
	 * IDs rename are performed.
	 *
	 * @param  edgesIDStrategy          type of edge ID strategy to use, or {@code null} for default strategy
	 * @return                          this builder
	 * @throws IllegalArgumentException if the strategy type is not supported
	 * @see                             IDStrategy
	 */
	GraphBuilder setEdgesIDStrategy(Class<? extends IDStrategy> edgesIDStrategy);

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
