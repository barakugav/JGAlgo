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

/**
 * Builder for unique identifiers of vertices or edges in a graph.
 *
 * <p>
 * A {@link Graph} allows adding vertices by either providing an identifier ({@link Graph#addVertex(Object)}) or without
 * ({@link Graph#addVertex()}). If no identifier is provided, the graph will generate one using an instance of this
 * interface. The same is true for edges, see {@link Graph#addEdge(Object, Object, Object)} and
 * {@link Graph#addEdge(Object, Object)}. The graph expose its vertex and edge builders using
 * {@link Graph#vertexBuilder()} and {@link Graph#edgeBuilder()}, which may return {@code null}. The identifiers
 * returned by this interface must be unique in the graph.
 *
 * <p>
 * This interface is shared for both vertices and edges, but an instance of this interface is used only for one of them
 * at a time.
 *
 * @author Barak Ugav
 */
@FunctionalInterface
public interface IdBuilder<K> {

	/**
	 * Builds a unique identifier for a vertex or an edge.
	 *
	 * @param  existing the identifiers of the vertices or edges already in the graph
	 * @return          a unique identifier
	 */
	K build(Set<K> existing);

}
