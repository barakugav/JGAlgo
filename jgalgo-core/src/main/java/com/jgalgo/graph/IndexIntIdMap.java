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
 * A mapping between {@link IntGraph} IDs to {@link IndexGraph} indices.
 *
 * <p>
 * This interface is a specific version of {@link IndexIdMap} for {@link IntGraph}.
 *
 * <p>
 * A regular graph contains vertices and edges which are identified by a fixed {@code int} IDs. An {@link IndexGraph}
 * view is provided by the {@link IntGraph#indexGraph()} method, which is a graph in which all methods are accessed with
 * <b>indices</b> rather than fixed IDs. This interface maps between the indices and the fixed IDs of the graph vertices
 * or edges.
 *
 * <p>
 * Note that the mapping may change during the graph lifetime, as vertices and edges are added and removed from the
 * graph, and a regular graph IDs are fixed, while a index graph indices are always {@code (0,1,2, ...,verticesNum-1)}
 * and {@code (0,1,2, ...,edgesNum-1)}. The mapping object will be updated automatically in such cases.
 *
 * <p>
 * The mapping interface is used for both vertices and edges, and we use a unify term <i>element</i> in the
 * documentation to describe both of them. If the mapping was obtained by {@link IntGraph#indexGraphVerticesMap()} it
 * will map between vertices IDs and indices, and if it was obtained by {@link IntGraph#indexGraphEdgesMap()} it will
 * map between edges IDs and indices.
 *
 * @see    IndexGraph
 * @author Barak Ugav
 */
public interface IndexIntIdMap extends IndexIdMap<Integer> {

	/**
	 * Get the ID of an element by its index.
	 *
	 * <p>
	 * Whether this method maps vertices or edges depends if the mapping object was obtained by
	 * {@link IntGraph#indexGraphVerticesMap()} or {@link IntGraph#indexGraphEdgesMap()}.
	 *
	 * @param  index                     an index of an element (vertex/edge)
	 * @return                           the ID of the element
	 * @throws IndexOutOfBoundsException if {@code index} is not in range {@code [, elementsNum)} where
	 *                                       {@code elementsNum} is the number of either vertices or edges, depending on
	 *                                       the context
	 */
	int indexToIdInt(int index);

	@Deprecated
	@Override
	default Integer indexToId(int index) {
		return Integer.valueOf(indexToIdInt(index));
	}

	/**
	 * Get the index of an element by its ID.
	 *
	 * <p>
	 * Whether this method maps vertices or edges depends if the mapping object was obtained by
	 * {@link IntGraph#indexGraphVerticesMap()} or {@link IntGraph#indexGraphEdgesMap()}.
	 *
	 * @param  id                        an ID of an element (vertex/edge)
	 * @return                           the index of the element
	 * @throws IndexOutOfBoundsException if {@code id} is not a valid identifier of vertex/edge, depending on the
	 *                                       context
	 */
	int idToIndex(int id);

	@Deprecated
	@Override
	default int idToIndex(Integer id) {
		return idToIndex(id.intValue());
	}

}
