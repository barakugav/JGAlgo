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
package com.jgalgo.alg.planar;

import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;

/**
 * Planar embedding of an {@link IntGraph}.
 *
 * <p>
 * This interface is a specific version of {@link PlanarEmbedding} for {@link IntGraph}. See the generic interface for
 * the full documentation.
 *
 * @author Barak Ugav
 */
public interface IPlanarEmbedding extends PlanarEmbedding<Integer, Integer> {

	/**
	 * Get an iterator over all the edges of a vertex in clockwise order.
	 *
	 * <p>
	 * This is a specification of {@link #allEdges(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  vertex the vertex
	 * @return        an iterator over all the edges of a vertex in clockwise order
	 */
	IEdgeIter allEdges(int vertex);

	@Deprecated
	@Override
	default IEdgeIter allEdges(Integer vertex) {
		return allEdges(vertex.intValue());
	}

	/**
	 * Get an iterator over all the edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #allEdges(Integer, Integer)} for the {@code int} type. See the documentation of
	 * the generic method for the full documentation.
	 *
	 * @param  vertex                   the vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an edge of {@code vertex}
	 */
	IEdgeIter allEdges(int vertex, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter allEdges(Integer vertex, Integer precedingEdge) {
		return allEdges(vertex.intValue(), precedingEdge.intValue());
	}

	/**
	 * Get an iterator over all the outgoing edges of a vertex in clockwise order.
	 *
	 * <p>
	 * This is a specification of {@link #outEdges(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  source the source vertex
	 * @return        an iterator over all the outgoing edges of a vertex in clockwise order
	 */
	IEdgeIter outEdges(int source);

	@Deprecated
	@Override
	default IEdgeIter outEdges(Integer source) {
		return outEdges(source.intValue());
	}

	/**
	 * Get an iterator over all the outgoing edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #outEdges(Integer, Integer)} for the {@code int} type. See the documentation of
	 * the generic method for the full documentation.
	 *
	 * @param  source                   the source vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the outgoing edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an outgoing edge of {@code source}
	 */
	IEdgeIter outEdges(int source, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter outEdges(Integer source, Integer precedingEdge) {
		return outEdges(source.intValue(), precedingEdge.intValue());
	}

	/**
	 * Get an iterator over all the incoming edges of a vertex in clockwise order.
	 *
	 * <p>
	 * This is a specification of {@link #inEdges(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  target the target vertex
	 * @return        an iterator over all the incoming edges of a vertex in clockwise order
	 */
	IEdgeIter inEdges(int target);

	@Deprecated
	@Override
	default IEdgeIter inEdges(Integer target) {
		return inEdges(target.intValue());
	}

	/**
	 * Get an iterator over all the incoming edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #inEdges(Integer, Integer)} for the {@code int} type. See the documentation of
	 * the generic method for the full documentation.
	 *
	 * @param  target                   the target vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the incoming edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an incoming edge of {@code target}
	 */
	IEdgeIter inEdges(int target, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter inEdges(Integer target, Integer precedingEdge) {
		return inEdges(target.intValue(), precedingEdge.intValue());
	}

	@Override
	IPlanarEmbedding reversed();

	@Override
	IntGraph graph();

}
