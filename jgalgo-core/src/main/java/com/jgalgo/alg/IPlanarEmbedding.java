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
package com.jgalgo.alg;

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
	 * This is a specification of {@link #allEdgesCw(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  vertex the vertex
	 * @return        an iterator over all the edges of a vertex in clockwise order
	 */
	IEdgeIter allEdgesCw(int vertex);

	@Deprecated
	@Override
	default IEdgeIter allEdgesCw(Integer vertex) {
		return allEdgesCw(vertex.intValue());
	}

	/**
	 * Get an iterator over all the edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #allEdgesCw(Integer, Integer)} for the {@code int} type. See the documentation
	 * of the generic method for the full documentation.
	 *
	 * @param  vertex                   the vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an edge of {@code vertex}
	 */
	IEdgeIter allEdgesCw(int vertex, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter allEdgesCw(Integer vertex, Integer precedingEdge) {
		return allEdgesCw(vertex.intValue(), precedingEdge.intValue());
	}

	/**
	 * Get an iterator over all the edges of a vertex in counter-clockwise order.
	 *
	 * <p>
	 * This is a specification of {@link #allEdgesCcw(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  vertex the vertex
	 * @return        an iterator over all the edges of a vertex in counter-clockwise order
	 */
	IEdgeIter allEdgesCcw(int vertex);

	@Deprecated
	@Override
	default IEdgeIter allEdgesCcw(Integer vertex) {
		return allEdgesCcw(vertex.intValue());
	}

	/**
	 * Get an iterator over all the edges of a vertex in counter-clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #allEdgesCcw(Integer, Integer)} for the {@code int} type. See the documentation
	 * of the generic method for the full documentation.
	 *
	 * @param  vertex                   the vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the edges of a vertex in counter-clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an edge of {@code vertex}
	 */
	IEdgeIter allEdgesCcw(int vertex, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter allEdgesCcw(Integer vertex, Integer precedingEdge) {
		return allEdgesCcw(vertex.intValue(), precedingEdge.intValue());
	}

	/**
	 * Get an iterator over all the outgoing edges of a vertex in clockwise order.
	 *
	 * <p>
	 * This is a specification of {@link #outEdgesCw(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  source the source vertex
	 * @return        an iterator over all the outgoing edges of a vertex in clockwise order
	 */
	IEdgeIter outEdgesCw(int source);

	@Deprecated
	@Override
	default IEdgeIter outEdgesCw(Integer source) {
		return outEdgesCw(source.intValue());
	}

	/**
	 * Get an iterator over all the outgoing edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #outEdgesCw(Integer, Integer)} for the {@code int} type. See the documentation
	 * of the generic method for the full documentation.
	 *
	 * @param  source                   the source vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the outgoing edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an outgoing edge of {@code source}
	 */
	IEdgeIter outEdgesCw(int source, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter outEdgesCw(Integer source, Integer precedingEdge) {
		return outEdgesCw(source.intValue(), precedingEdge.intValue());
	}

	/**
	 * Get an iterator over all the outgoing edges of a vertex in counter-clockwise order.
	 *
	 * <p>
	 * This is a specification of {@link #outEdgesCcw(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  source the source vertex
	 * @return        an iterator over all the outgoing edges of a vertex in counter-clockwise order
	 */
	IEdgeIter outEdgesCcw(int source);

	@Deprecated
	@Override
	default IEdgeIter outEdgesCcw(Integer source) {
		return outEdgesCcw(source.intValue());
	}

	/**
	 * Get an iterator over all the outgoing edges of a vertex in counter-clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #outEdgesCcw(Integer, Integer)} for the {@code int} type. See the documentation
	 * of the generic method for the full documentation.
	 *
	 * @param  source                   the source vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the outgoing edges of a vertex in counter-clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an outgoing edge of {@code source}
	 */
	IEdgeIter outEdgesCcw(int source, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter outEdgesCcw(Integer source, Integer precedingEdge) {
		return outEdgesCcw(source.intValue(), precedingEdge.intValue());
	}

	/**
	 * Get an iterator over all the incoming edges of a vertex in clockwise order.
	 *
	 * <p>
	 * This is a specification of {@link #inEdgesCw(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  target the target vertex
	 * @return        an iterator over all the incoming edges of a vertex in clockwise order
	 */
	IEdgeIter inEdgesCw(int target);

	@Deprecated
	@Override
	default IEdgeIter inEdgesCw(Integer target) {
		return inEdgesCw(target.intValue());
	}

	/**
	 * Get an iterator over all the incoming edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #inEdgesCw(Integer, Integer)} for the {@code int} type. See the documentation
	 * of the generic method for the full documentation.
	 *
	 * @param  target                   the target vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the incoming edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an incoming edge of {@code target}
	 */
	IEdgeIter inEdgesCw(int target, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter inEdgesCw(Integer target, Integer precedingEdge) {
		return inEdgesCw(target.intValue(), precedingEdge.intValue());
	}

	/**
	 * Get an iterator over all the incoming edges of a vertex in counter-clockwise order.
	 *
	 * <p>
	 * This is a specification of {@link #inEdgesCcw(Integer)} for the {@code int} type. See the documentation of the
	 * generic method for the full documentation.
	 *
	 * @param  target the target vertex
	 * @return        an iterator over all the incoming edges of a vertex in counter-clockwise order
	 */
	IEdgeIter inEdgesCcw(int target);

	@Deprecated
	@Override
	default IEdgeIter inEdgesCcw(Integer target) {
		return inEdgesCcw(target.intValue());
	}

	/**
	 * Get an iterator over all the incoming edges of a vertex in counter-clockwise order, starting after a given edge.
	 *
	 * <p>
	 * This is a specification of {@link #inEdgesCcw(Integer, Integer)} for the {@code int} type. See the documentation
	 * of the generic method for the full documentation.
	 *
	 * @param  target                   the target vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the incoming edges of a vertex in counter-clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an incoming edge of {@code target}
	 */
	IEdgeIter inEdgesCcw(int target, int precedingEdge);

	@Deprecated
	@Override
	default IEdgeIter inEdgesCcw(Integer target, Integer precedingEdge) {
		return inEdgesCcw(target.intValue(), precedingEdge.intValue());
	}

}
