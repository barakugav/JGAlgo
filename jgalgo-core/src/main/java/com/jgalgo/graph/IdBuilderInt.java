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

import java.util.Set;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Builder for unique identifiers of vertices or edges in an {@link IntGraph}.
 *
 * <p>
 * An {@link IntGraph} allows adding vertices by either providing an identifier ({@link IntGraph#addVertex(int)}) or
 * without ({@link IntGraph#addVertexInt()}). If no identifier is provided, the graph will generate one using an
 * instance of this interface. The same is true for edges, see {@link IntGraph#addEdge(int, int, int)} and
 * {@link IntGraph#addEdge(int, int)}. The graph expose its vertex and edge builders using
 * {@link IntGraph#vertexBuilder()} and {@link IntGraph#edgeBuilder()}, which may return {@code null}. The identifiers
 * returned by this interface must be unique in the graph.
 *
 * <p>
 * This interface is shared for both vertices and edges, but an instance of this interface is used only for one of them
 * at a time.
 *
 * <p>
 * This interface is a specific version of {@link IdBuilder} for {@link IntGraph}.
 *
 * @author Barak Ugav
 */
@FunctionalInterface
public interface IdBuilderInt extends IdBuilder<Integer> {

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated use {@link #build(IntSet)} instead to avoid unnecessary un/boxing
	 */
	@Deprecated
	@Override
	default Integer build(Set<Integer> existing) {
		return Integer.valueOf(build(IntAdapters.asIntSet(existing)));
	}

	/**
	 * Builds a unique identifier for a vertex or an edge.
	 *
	 * @param  existing the identifiers of the vertices or edges already in the graph
	 * @return          a unique identifier
	 */
	int build(IntSet existing);

}
