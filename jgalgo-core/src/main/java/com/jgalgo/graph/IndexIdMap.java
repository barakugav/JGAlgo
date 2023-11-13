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
 * A mapping between {@link Graph} IDs to {@link IndexGraph} indices.
 *
 * <p>
 * A regular graph contains vertices and edges which are identified by a fixed hashable identifiers. An
 * {@link IndexGraph} view is provided by the {@link Graph#indexGraph()} method, which is a graph in which all methods
 * are accessed with <b>indices</b> rather than fixed IDs. This interface maps between the indices and the fixed IDs of
 * the graph vertices or edges.
 *
 * <p>
 * Note that the mapping may change during the graph lifetime, as vertices and edges are added and removed from the
 * graph, and a regular graph IDs are fixed, while a index graph indices are always {@code (0,1,2, ...,verticesNum-1)}
 * and {@code (0,1,2, ...,edgesNum-1)}. The mapping object will be updated automatically in such cases.
 *
 * <p>
 * The mapping interface is used for both vertices and edges, and we use a unify term <i>element</i> in the
 * documentation to describe both of them (vertex or edge). If the mapping was obtained by
 * {@link Graph#indexGraphVerticesMap()} it will map between vertices IDs and indices, and if it was obtained by
 * {@link Graph#indexGraphEdgesMap()} it will map between edges IDs and indices.
 *
 * @param  <K> the elements (vertices/edges) type
 * @see        IndexGraph
 * @author     Barak Ugav
 */
public interface IndexIdMap<K> {

	/**
	 * Get the identifier of an element by its index.
	 *
	 * <p>
	 * Whether this method maps vertices or edges depends if the mapping object was obtained by
	 * {@link Graph#indexGraphVerticesMap()} or {@link Graph#indexGraphEdgesMap()}.
	 *
	 * @param  index                     an index of an element (vertex/edge)
	 * @return                           the identifier of the element
	 * @throws IndexOutOfBoundsException if {@code index} is not in range {@code [, elementsNum)} where
	 *                                       {@code elementsNum} is the number of either vertices or edges, depending on
	 *                                       the context
	 */
	K indexToId(int index);

	/**
	 * Get the identifier of an element by its index if it exists, or {@code null} if it doesn't.
	 *
	 * <p>
	 * Whether this method maps vertices or edges depends if the mapping object was obtained by
	 * {@link Graph#indexGraphVerticesMap()} or {@link Graph#indexGraphEdgesMap()}.
	 *
	 * @param  index the index of an element (vertex/edge)
	 * @return       the identifier of the element, or {@code null} if there is not such element
	 */
	K indexToIdIfExist(int index);

	/**
	 * Get the index of an element by its identifier.
	 *
	 * <p>
	 * Whether this method maps vertices or edges depends if the mapping object was obtained by
	 * {@link Graph#indexGraphVerticesMap()} or {@link Graph#indexGraphEdgesMap()}.
	 *
	 * @param  id                    an identifier of an element (vertex/edge)
	 * @return                       the index of the element
	 * @throws NoSuchVertexException if this map maps vertices to ids and {@code id} is not a valid identifier of a
	 *                                   vertex
	 * @throws NoSuchEdgeException   if this map maps edges to ids and {@code id} is not a valid identifier of an edge
	 */
	int idToIndex(K id);

	/**
	 * Get the index of an element by its identifier if it exists, or {@code -1} if it doesn't.
	 *
	 * <p>
	 * Whether this method maps vertices or edges depends if the mapping object was obtained by
	 * {@link Graph#indexGraphVerticesMap()} or {@link Graph#indexGraphEdgesMap()}.
	 *
	 * @param  id an identifier of an element (vertex/edge)
	 * @return    the index of the element, or {@code -1} if there is not such element
	 */
	int idToIndexIfExist(K id);

}
