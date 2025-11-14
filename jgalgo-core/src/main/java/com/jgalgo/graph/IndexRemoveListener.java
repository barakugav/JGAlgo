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
 * A listener that will be notified when an {@link IndexGraph} remove a vertex or an edge.
 *
 * <p>
 * The {@code int} identifiers an {@link IndexGraph} uses for its vertices are always {@code 0,1,2,...,verticesNum-1}
 * (and similarly for its edges). To maintain this invariant, when a vertex (or an edge) is removed from the graph, the
 * graph implementation way need to swap the identifiers of vertices (edges). If the index of the vertex (edge) is the
 * last index ({@code verticesNum-1}), the vertex can simply be removed. Otherwise, the vertex will be swapped with the
 * last vertex and then removed. It's possible to register to these swaps and removes using
 * {@link IndexGraph#addVertexRemoveListener(IndexRemoveListener)} and
 * {@link IndexGraph#addEdgeRemoveListener(IndexRemoveListener)}. The listener will be called on all vertex (edge)
 * removal functions except {@link IndexGraph#clear()} and {@link IndexGraph#clearEdges()}.
 *
 * <p>
 * The same swap listener interface is used for both vertices and edges (a specific instance is only used to one of
 * them, which can be determined by the context), and we use a unified term <i>element</i> in the documentation to refer
 * to either of them.
 *
 * @author Barak Ugav
 */
public interface IndexRemoveListener {

	/**
	 * A callback that is called when the last element is removed.
	 *
	 * <p>
	 * When the last element (vertex or edge) is removed, no swap is needed, and the element is simply removed. The
	 * index of the last vertex can be accessed using {@code g.vertices().size()-1} (and similarly for edges) but is
	 * passed as an argument for convenience.
	 *
	 * @param removedIdx the index of the removed element, which is the highest index in the graph
	 */
	void removeLast(int removedIdx);

	/**
	 * A callback that is called when {@code removedIdx} is swapped with {@code swappedIdx} and then removed.
	 *
	 * <p>
	 * When an element is removed from an index graph, the graph implementation may need to swap the element with the
	 * last element and then remove it. This is done to maintain the invariant that the element identifiers are always
	 * {@code 0,1,2,...,verticesNum-1} (and similarly for edges).
	 *
	 * @param removedIdx the index of the removed element, before the swap
	 * @param swappedIdx the index of the element that was swapped with {@code removedIdx}, which is the highest index
	 *                       in the graph
	 */
	void swapAndRemove(int removedIdx, int swappedIdx);

}
