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

public interface IndexGraph extends Graph {

	/**
	 * Get the ID strategy of the vertices of the graph.
	 *
	 * <p>
	 * Each vertex in the graph is identified by a unique non negative int ID, which is determined by some strategy.
	 * Only {@link IDStrategy.Continues} is supported for vertices, which ensure that at all times the vertices IDs are
	 * {@code 0,1,..., verticesNum-1}, and it might rename some vertices when a vertex is removed to maintain this
	 * invariant. This rename can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 *
	 * @see    IDStrategy
	 *
	 * @return the vertices IDs strategy
	 */
	public IDStrategy.Continues getVerticesIDStrategy();

	/**
	 * Get the ID strategy of the edges of the graph.
	 *
	 * <p>
	 * Each edge in the graph is identified by a unique non negative int ID, which is determined by some strategy. For
	 * example, {@link IDStrategy.Continues} ensure that at all times the edges IDs are {@code 0,1,..., edgesNum-1}, and
	 * it might rename some edges when an edge is removed to maintain this invariant. This rename can be subscribed
	 * using {@link IDStrategy#addIDSwapListener}. Another option for an ID strategy is {@link IDStrategy.Fixed} which
	 * ensure once an edge is assigned an ID, it will not change. There might be some performance differences between
	 * different ID strategies.
	 *
	 * @see    IDStrategy
	 *
	 * @return the edges IDs strategy
	 */
	public IDStrategy.Continues getEdgesIDStrategy();

	@Override
	public IndexGraph copy();

	@Override
	default IndexGraph unmodifiableView() {
		return Graphs.unmodifiableView(this);
	}

	@Override
	default IndexGraph reverseView() {
		return Graphs.reverseView(this);
	}

	static IndexGraph.Builder newBuilderUndirected() {
		return new IndexGraphBuilderImpl(false);
	}

	static IndexGraph.Builder newBuilderDirected() {
		return new IndexGraphBuilderImpl(true);
	}

	/**
	 * A builder for {@link IndexGraph} objects.
	 *
	 * @see    IndexGraph#newBuilderDirected()
	 * @see    IndexGraph#newBuilderUndirected()
	 * @author Barak Ugav
	 */
	public interface Builder extends BuilderAbstract<IndexGraph.Builder> {

		/**
		 * Create a new empty graph.
		 *
		 * @return a new graph with the builder options
		 */
		IndexGraph build();

		/**
		 * Determine if graphs built by this builder should be directed or not.
		 *
		 * @param  directed if {@code true}, graphs built by this builder will be directed
		 * @return          this builder
		 */
		IndexGraph.Builder setDirected(boolean directed);

		/**
		 * Set the expected number of vertices that will exist in the graph.
		 *
		 * @param  expectedVerticesNum the expected number of vertices in the graph
		 * @return                     this builder
		 */
		IndexGraph.Builder expectedVerticesNum(int expectedVerticesNum);

		/**
		 * Set the expected number of edges that will exist in the graph.
		 *
		 * @param  expectedEdgesNum the expected number of edges in the graph
		 * @return                  this builder
		 */
		IndexGraph.Builder expectedEdgesNum(int expectedEdgesNum);

		/**
		 * Add a hint to this builder.
		 * <p>
		 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
		 *
		 * @param  hint the hint to add
		 * @return      this builder
		 */
		IndexGraph.Builder addHint(Graph.Builder.Hint hint);

		/**
		 * Remove a hint from this builder.
		 * <p>
		 * Hints do not change the behavior of the graphs built by this builder, by may affect performance.
		 *
		 * @param  hint the hint to remove
		 * @return      this builder
		 */
		IndexGraph.Builder removeHint(Graph.Builder.Hint hint);

	}

}
