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

import it.unimi.dsi.fastutil.ints.IntSet;

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

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #indexToIdInt(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer indexToId(int index) {
		return Integer.valueOf(indexToIdInt(index));
	}

	/**
	 * Get the identifier of an element by its index if it exists, or {@code -1} if it doesn't.
	 *
	 * <p>
	 * Whether this method maps vertices or edges depends if the mapping object was obtained by
	 * {@link Graph#indexGraphVerticesMap()} or {@link Graph#indexGraphEdgesMap()}.
	 *
	 * @param  index the index of an element (vertex/edge)
	 * @return       the identifier of the element, or {@code -1} if there is not such element
	 */
	int indexToIdIfExistInt(int index);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #indexToIdIfExistInt(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer indexToIdIfExist(int index) {
		int id = indexToIdIfExistInt(index);
		return id < 0 ? null : Integer.valueOf(id);
	}

	/**
	 * Get the index of an element by its ID.
	 *
	 * <p>
	 * Whether this method maps vertices or edges depends if the mapping object was obtained by
	 * {@link IntGraph#indexGraphVerticesMap()} or {@link IntGraph#indexGraphEdgesMap()}.
	 *
	 * @param  id                    an ID of an element (vertex/edge)
	 * @return                       the index of the element
	 * @throws NoSuchVertexException if this map maps vertices to ids and {@code id} is not a valid identifier of a
	 *                                   vertex
	 * @throws NoSuchEdgeException   if this map maps edges to ids and {@code id} is not a valid identifier of an edge
	 */
	int idToIndex(int id);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #idToIndex(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default int idToIndex(Integer id) {
		return idToIndex(id.intValue());
	}

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
	int idToIndexIfExist(int id);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #idToIndexIfExist(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default int idToIndexIfExist(Integer id) {
		return idToIndexIfExist(id.intValue());
	}

	/**
	 * Create an identity mapping between the elements IDs and indices.
	 *
	 * <p>
	 * The identity mapping is a mapping in which the ID of an element is equal to its index. This mapping is used by
	 * {@linkplain IndexGraph index graphs}. The passed set of vertices is expected to be {@code 0,1,2, ..., n-1} where
	 * {@code n} is the number of vertices in the graph, as only for this set of vertices the identity mapping is
	 * possible. The given set is not validated for this condition. The number of vertices, {@code n}, is accessible to
	 * the mapping by {@code vertexSet.size()}, and it is used to determine if an index/id is in range. If the graph is
	 * mutable and the mapping should be used during the graph lifetime, the given set should be a view of the graph
	 * vertices, so the range checks will be valid.
	 *
	 * @param  vertexSet the set of vertices in the graph. The set will be used t
	 * @return           an identity mapping of the vertices
	 */
	static IndexIntIdMap identityVerticesMap(IntSet vertexSet) {
		return new IdentityIndexIdMap(vertexSet, false);
	}

	/**
	 * Create an identity mapping between the elements IDs and indices.
	 *
	 * <p>
	 * The identity mapping is a mapping in which the ID of an element is equal to its index. This mapping is used by
	 * {@linkplain IndexGraph index graphs}. The passed set of edges is expected to be {@code 0,1,2, ..., m-1} where
	 * {@code m} is the number of edges in the graph, as only for this set of edges the identity mapping is possible.
	 * The given set is not validated for this condition. The number of edges, {@code m}, is accessible to the mapping
	 * by {@code edgeSet.size()}, and it is used to determine if an index/id is in range. If the graph is mutable and
	 * the mapping should be used during the graph lifetime, the given set should be a view of the graph edges, so the
	 * range checks will be valid.
	 *
	 * @param  edgeSet the set of edges in the graph. The set will be used t
	 * @return         an identity mapping of the edges
	 */
	static IndexIntIdMap identityEdgesMap(IntSet edgeSet) {
		return new IdentityIndexIdMap(edgeSet, true);
	}

}
