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
import java.util.function.Supplier;

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
 * @param  <K> the type of identifiers (vertices/edges)
 * @author     Barak Ugav
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

	/**
	 * Get an default builder for identifiers of the given type.
	 *
	 * <p>
	 * A default builder existing for the following types:
	 * <ul>
	 * <li>{@code byte} and {@link Byte}</li>
	 * <li>{@code short} and {@link Short}</li>
	 * <li>{@code int} and {@link Integer}</li>
	 * <li>{@code long} and {@link Long}</li>
	 * <li>{@code float} and {@link Float}</li>
	 * <li>{@code double} and {@link Double}</li>
	 * <li>{@link String}</li>
	 * <li>any other type that has a public constructor with no arguments</li>
	 * </ul>
	 * For the latter case, the builder will create a new instance using the constructor, and the newly created instance
	 * must no be equal to any of the existing identifiers. This is most suitable for types that do not override the
	 * default {@link Object#equals(Object)} method.
	 *
	 * <p>
	 * The returned builder may be passed to {@link GraphFactory#setVertexBuilder(IdBuilder)}.
	 *
	 * @param  <K>                      the type of the identifiers
	 * @param  idType                   the id type class
	 * @return                          a default builder for identifiers of {@code idType}
	 * @throws IllegalArgumentException if the type is not supported
	 */
	static <K> IdBuilder<K> defaultBuilder(Class<K> idType) {
		return IdBuilder.<K>defaultFactory(idType).get();
	}

	/**
	 * Get an default factory for identifiers of the given type.
	 *
	 * <p>
	 * A default factory existing for the following types:
	 * <ul>
	 * <li>{@code byte} and {@link Byte}</li>
	 * <li>{@code short} and {@link Short}</li>
	 * <li>{@code int} and {@link Integer}</li>
	 * <li>{@code long} and {@link Long}</li>
	 * <li>{@code float} and {@link Float}</li>
	 * <li>{@code double} and {@link Double}</li>
	 * <li>{@link String}</li>
	 * <li>any other type that has a public constructor with no arguments</li>
	 * </ul>
	 * For the latter case, the factory will create a new instance using the constructor, and the newly created instance
	 * must no be equal to any of the existing identifiers. This is most suitable for types that do not override the
	 * default {@link Object#equals(Object)} method.
	 *
	 * <p>
	 * The returned factory may be passed to {@link GraphFactory#setVertexFactory(Supplier)}.
	 *
	 * @param  <K>                      the type of the identifiers
	 * @param  idType                   the id type class
	 * @return                          a default factory for identifiers of {@code idType}
	 * @throws IllegalArgumentException if the type is not supported
	 */
	static <K> Supplier<IdBuilder<K>> defaultFactory(Class<K> idType) {
		return IdBuilders.defaultFactory(idType);
	}

}
