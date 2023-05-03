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
 * A discrete undirected graph with vertices and edges.
 * <p>
 * An extension to the {@link Graph} interface, where edges are undirected, namely an edge \(e(u, v)\) will appear in
 * the iteration of {@code edgesOut(u)}, {@code edgesIn(v)}, {@code edgesOut(v)} and {@code edgesIn(u)}. Also
 * {@link #edgesOut(int)} and {@link #edgesIn(int)} are equivalent for the same vertex, same for {@link #degreeIn(int)}
 * and {@link #degreeOut(int)}, and similarly {@link #removeEdgesOf(int)}, {@link #removeEdgesInOf(int)} and
 * {@link #removeEdgesOutOf(int)}.
 * <p>
 * Use {@link GraphArrayUndirected} as a default implementation of undirected graphs, its the most efficient for most
 * use cases.
 *
 * @see    DiGraph
 * @author Barak Ugav
 */
public interface UGraph extends Graph {

	@Override
	default EdgeIter edgesIn(int v) {
		return edgesOut(v);
	}

	@Override
	default void removeEdgesOf(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Override
	default void removeEdgesOutOf(int u) {
		removeEdgesOf(u);
	}

	@Override
	default void removeEdgesInOf(int v) {
		removeEdgesOf(v);
	}

	@Override
	default int degreeIn(int v) {
		return degreeOut(v);
	}

	/**
	 * Create an undirected graph builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link DiGraph} object.
	 *
	 * @return a new builder that can build {@link DiGraph} objects
	 */
	static UGraph.Builder newBuilder() {
		return new GraphBuilderImpl.ArrayUndirected();

	}

	/**
	 * A builder for {@link UGraph} objects.
	 *
	 * @see    UGraph#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends Graph.Builder {

		/**
		 * Create a new undirected graph.
		 *
		 * @return a new undirected graph with the builder options
		 */
		@Override
		UGraph build();

		@Override
		UGraph.Builder setVerticesNum(int n);

		@Override
		UGraph.Builder setEdgesIDStrategy(Class<? extends IDStrategy> edgesIDStrategy);
	}

}
