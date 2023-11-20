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

import java.util.Optional;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightsLong;

/**
 * Planar embedding of a graph.
 *
 * <p>
 * A graph is planar if it can be drawn in the plane in such a way that its edges intersect only at their endpoints. In
 * other words, it can be drawn in such a way that no edges cross each other. A planar embedding of a graph is a
 * specification on the order of edges around each vertex, such that the graph can be drawn in the plane using this
 * order.
 *
 * <p>
 * The embedding specify for each vertex the order of its edges, including in and out edges in the same order. The terms
 * 'clock wise' and 'counter-clockwise' are used to distinguish between the two opposite directions, but the embedding
 * can be reversed and remain valid. The order of each vertex is of course cyclic, but all the iterators returned by the
 * embedding will not wrap around after a single iteration over each edge. It is possible to iterate over the edges of a
 * vertex starting after a given edge, see {@link #allEdgesCw(Object, Object)} and {@link #allEdgesCcw(Object, Object)}.
 *
 * <p>
 * Planar embedding are usually computed by a planarity testing algorithm, and the embedding can be stored as a graph
 * edge {@code long} weights with key {@link #EdgeOrderWeightKey}. If weights with this key exist in a graph, algorithms
 * can assume the graph is planar and use the embedding information. See the documentation of this static field for more
 * information.
 *
 * @see        <a href="https://en.wikipedia.org/wiki/Planar_graph">Wikipedia</a>
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface PlanarEmbedding<V, E> {

	/**
	 * Get an iterator over all the edges of a vertex in clockwise order.
	 *
	 * <p>
	 * For directed graphs, the planar embedding should specify the order of <b>all</b> the edges around a vertex, also
	 * between out and in edges. This function returns an iterator over all the edges of a vertex in clockwise order.
	 * The {@link EdgeIter#source()} and {@link EdgeIter#target()} methods of the iterator will return the source and
	 * target returned by {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, which is different from
	 * {@link #outEdgesCw(Object)} for undirected graphs.
	 *
	 * <p>
	 * Note that self edges appear once in the iteration, and not twice, and its outgoing point in the order and
	 * incoming point in the order are the same.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  vertex the vertex
	 * @return        an iterator over all the edges of a vertex in clockwise order
	 */
	EdgeIter<V, E> allEdgesCw(V vertex);

	/**
	 * Get an iterator over all the edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * For directed graphs, the planar embedding should specify the order of <b>all</b> the edges around a vertex, also
	 * between out and in edges. This function returns an iterator over all the edges of a vertex in clockwise order.
	 * The {@link EdgeIter#source()} and {@link EdgeIter#target()} methods of the iterator will return the source and
	 * target returned by {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, which is different from
	 * {@link #outEdgesCw(Object, Object)} for undirected graphs.
	 *
	 * <p>
	 * Note that self edges appear once in the iteration, and not twice, and its outgoing point in the order and
	 * incoming point in the order are the same.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  vertex                   the vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an edge of {@code vertex}
	 */
	EdgeIter<V, E> allEdgesCw(V vertex, E precedingEdge);

	/**
	 * Get an iterator over all the edges of a vertex in counter-clockwise order.
	 *
	 * <p>
	 * For directed graphs, the planar embedding should specify the order of <b>all</b> the edges around a vertex, also
	 * between out and in edges. This function returns an iterator over all the edges of a vertex in counter-clockwise
	 * order. The {@link EdgeIter#source()} and {@link EdgeIter#target()} methods of the iterator will return the source
	 * and target returned by {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, which is different
	 * from {@link #outEdgesCcw(Object)} for undirected graphs.
	 *
	 * <p>
	 * Note that self edges appear once in the iteration, and not twice, and its outgoing point in the order and
	 * incoming point in the order are the same.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  vertex the vertex
	 * @return        an iterator over all the edges of a vertex in counter-clockwise order
	 */
	EdgeIter<V, E> allEdgesCcw(V vertex);

	/**
	 * Get an iterator over all the edges of a vertex in counter-clockwise order, starting after a given edge.
	 *
	 * <p>
	 * For directed graphs, the planar embedding should specify the order of <b>all</b> the edges around a vertex, also
	 * between out and in edges. This function returns an iterator over all the edges of a vertex in counter-clockwise
	 * order. The {@link EdgeIter#source()} and {@link EdgeIter#target()} methods of the iterator will return the source
	 * and target returned by {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, which is different
	 * from {@link #outEdgesCcw(Object, Object)} for undirected graphs.
	 *
	 * <p>
	 * Note that self edges appear once in the iteration, and not twice, and its outgoing point in the order and
	 * incoming point in the order are the same.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  vertex                   the vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the edges of a vertex in counter-clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an edge of {@code vertex}
	 */
	EdgeIter<V, E> allEdgesCcw(V vertex, E precedingEdge);

	/**
	 * Get an iterator over all the outgoing edges of a vertex in clockwise order.
	 *
	 * <p>
	 * For directed graphs, note that this method fails to describe all the embedding information, as the order of the
	 * outgoing edges should be defined in relation to the incoming edges. Use {@link #allEdgesCw(Object)} instead.
	 *
	 * <p>
	 * The {@link EdgeIter#source()} method of the returned iterator will always return the input {@code source} and the
	 * {@link EdgeIter#target()} method will return the other endpoint of the iterated edge. This behavior is identical
	 * to {@link Graph#outEdges(Object)}. For directed graphs these methods will return the same vertices as
	 * {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, but for undirected graphs it may be
	 * different. This is also different from {@link #allEdgesCw(Object)}.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  source the source vertex
	 * @return        an iterator over all the outgoing edges of a vertex in clockwise order
	 */
	EdgeIter<V, E> outEdgesCw(V source);

	/**
	 * Get an iterator over all the outgoing edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * For directed graphs, note that this method fails to describe all the embedding information, as the order of the
	 * outgoing edges should be defined in relation to the incoming edges. Use {@link #allEdgesCw(Object, Object)}
	 * instead.
	 *
	 * <p>
	 * The {@link EdgeIter#source()} method of the returned iterator will always return the input {@code source} and the
	 * {@link EdgeIter#target()} method will return the other endpoint of the iterated edge. This behavior is identical
	 * to {@link Graph#outEdges(Object)}. For directed graphs these methods will return the same vertices as
	 * {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, but for undirected graphs it may be
	 * different. This is also different from {@link #allEdgesCw(Object, Object)}.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  source                   the source vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the outgoing edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an outgoing edge of {@code source}
	 */
	EdgeIter<V, E> outEdgesCw(V source, E precedingEdge);

	/**
	 * Get an iterator over all the outgoing edges of a vertex in counter-clockwise order.
	 *
	 * <p>
	 * For directed graphs, note that this method fails to describe all the embedding information, as the order of the
	 * outgoing edges should be defined in relation to the incoming edges. Use {@link #allEdgesCcw(Object)} instead.
	 *
	 * <p>
	 * The {@link EdgeIter#source()} method of the returned iterator will always return the input {@code source} and the
	 * {@link EdgeIter#target()} method will return the other endpoint of the iterated edge. This behavior is identical
	 * to {@link Graph#outEdges(Object)}. For directed graphs these methods will return the same vertices as
	 * {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, but for undirected graphs it may be
	 * different. This is also different from {@link #allEdgesCcw(Object)}.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  source the source vertex
	 * @return        an iterator over all the outgoing edges of a vertex in counter-clockwise order
	 */
	EdgeIter<V, E> outEdgesCcw(V source);

	/**
	 * Get an iterator over all the outgoing edges of a vertex in counter-clockwise order, starting after a given edge.
	 *
	 * <p>
	 * For directed graphs, note that this method fails to describe all the embedding information, as the order of the
	 * outgoing edges should be defined in relation to the incoming edges. Use {@link #allEdgesCcw(Object, Object)}
	 * instead.
	 *
	 * <p>
	 * The {@link EdgeIter#source()} method of the returned iterator will always return the input {@code source} and the
	 * {@link EdgeIter#target()} method will return the other endpoint of the iterated edge. This behavior is identical
	 * to {@link Graph#outEdges(Object)}. For directed graphs these methods will return the same vertices as
	 * {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, but for undirected graphs it may be
	 * different. This is also different from {@link #allEdgesCcw(Object, Object)}.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  source                   the source vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the outgoing edges of a vertex in counter-clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an outgoing edge of {@code source}
	 */
	EdgeIter<V, E> outEdgesCcw(V source, E precedingEdge);

	/**
	 * Get an iterator over all the incoming edges of a vertex in clockwise order.
	 *
	 * <p>
	 * For directed graphs, note that this method fails to describe all the embedding information, as the order of the
	 * incoming edges should be defined in relation to the outgoing edges. Use {@link #allEdgesCw(Object)} instead.
	 *
	 * <p>
	 * The {@link EdgeIter#target()} method of the returned iterator will always return the input {@code target} and the
	 * {@link EdgeIter#source()} method will return the other endpoint of the iterated edge. This behavior is identical
	 * to {@link Graph#inEdges(Object)}. For directed graphs these methods will return the same vertices as
	 * {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, but for undirected graphs it may be
	 * different.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  target the target vertex
	 * @return        an iterator over all the incoming edges of a vertex in clockwise order
	 */
	EdgeIter<V, E> inEdgesCw(V target);

	/**
	 * Get an iterator over all the incoming edges of a vertex in clockwise order, starting after a given edge.
	 *
	 * <p>
	 * For directed graphs, note that this method fails to describe all the embedding information, as the order of the
	 * incoming edges should be defined in relation to the outgoing edges. Use {@link #allEdgesCw(Object, Object)}
	 * instead.
	 *
	 * <p>
	 * The {@link EdgeIter#target()} method of the returned iterator will always return the input {@code target} and the
	 * {@link EdgeIter#source()} method will return the other endpoint of the iterated edge. This behavior is identical
	 * to {@link Graph#inEdges(Object)}. For directed graphs these methods will return the same vertices as
	 * {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, but for undirected graphs it may be
	 * different.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  target                   the target vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the incoming edges of a vertex in clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an incoming edge of {@code target}
	 */
	EdgeIter<V, E> inEdgesCw(V target, E precedingEdge);

	/**
	 * Get an iterator over all the incoming edges of a vertex in counter-clockwise order.
	 *
	 * <p>
	 * For directed graphs, note that this method fails to describe all the embedding information, as the order of the
	 * incoming edges should be defined in relation to the outgoing edges. Use {@link #allEdgesCcw(Object)} instead.
	 *
	 * <p>
	 * The {@link EdgeIter#target()} method of the returned iterator will always return the input {@code target} and the
	 * {@link EdgeIter#source()} method will return the other endpoint of the iterated edge. This behavior is identical
	 * to {@link Graph#inEdges(Object)}. For directed graphs these methods will return the same vertices as
	 * {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, but for undirected graphs it may be
	 * different.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  target the target vertex
	 * @return        an iterator over all the incoming edges of a vertex in counter-clockwise order
	 */
	EdgeIter<V, E> inEdgesCcw(V target);

	/**
	 * Get an iterator over all the incoming edges of a vertex in counter-clockwise order, starting after a given edge.
	 *
	 * <p>
	 * For directed graphs, note that this method fails to describe all the embedding information, as the order of the
	 * incoming edges should be defined in relation to the outgoing edges. Use {@link #allEdgesCcw(Object, Object)}
	 * instead.
	 *
	 * <p>
	 * The {@link EdgeIter#target()} method of the returned iterator will always return the input {@code target} and the
	 * {@link EdgeIter#source()} method will return the other endpoint of the iterated edge. This behavior is identical
	 * to {@link Graph#inEdges(Object)}. For directed graphs these methods will return the same vertices as
	 * {@link Graph#edgeSource(Object)} and {@link Graph#edgeTarget(Object)}, but for undirected graphs it may be
	 * different.
	 *
	 * <p>
	 * The notion of 'clockwise' and 'counter-clockwise' can be reversed without violating the planar embedding, and its
	 * only called so for the distinction between the two opposite directions.
	 *
	 * @param  target                   the target vertex
	 * @param  precedingEdge            the edge to start after, which it to be iterated <b>last</b>: the first edge
	 *                                      returned by the iterator will be the edge <b>after</b> the given edge
	 * @return                          an iterator over all the incoming edges of a vertex in counter-clockwise order
	 * @throws IllegalArgumentException if {@code precedingEdge} is not an incoming edge of {@code target}
	 */
	EdgeIter<V, E> inEdgesCcw(V target, E precedingEdge);

	// TODO
	@SuppressWarnings("unchecked")
	public static <V, E> Optional<PlanarEmbedding<V, E>> getExistingEmbedding(Graph<V, E> g) {
		IndexGraph ig = g instanceof IndexGraph ? (IndexGraph) g : g.indexGraph();
		Object existingPartition = ig.getVerticesWeights(EdgeOrderWeightKey);
		if (existingPartition == null)
			return Optional.empty();
		if (!(existingPartition instanceof IWeightsLong))
			throw new IllegalArgumentException(
					"found edge weights with key '" + EdgeOrderWeightKey + "' but it is not a long weights container");
		IWeightsLong partition = (IWeightsLong) existingPartition;

		IPlanarEmbedding indexEmbedding = new PlanarEmbeddings.Impl(ig, partition);
		PlanarEmbedding<V, E> resultEmbedding;
		if (g instanceof IndexGraph) {
			resultEmbedding = (PlanarEmbedding<V, E>) indexEmbedding;
		} else {
			resultEmbedding = PlanarEmbeddings.embeddingFromIndexEmbedding(g, indexEmbedding);
		}
		return Optional.of(resultEmbedding);
	}

	/**
	 * The key of the edge weights that holds the planar embedding.
	 *
	 * <p>
	 * Planar embedding can be fully described by a clock wise (or counter clock wise) order of the edges around each
	 * vertex. The order should be specified for all the vertex edges, even between an in-edge and an out-edge. This
	 * order can be represented by two numbers per edge, the index of the edge in the order of the source vertex, and
	 * the index of the edge in the order of the target vertex. These two numbers are packed in a single {@code long}
	 * number, stored as a weight of the edge. This static field is the string key used to store the weights in graphs.
	 *
	 * <p>
	 * The long weights should not be used directly via the {@link WeightsLong} interface, but rather only via
	 * {@link PlanarEmbedding}, which can be obtained from a graph using {@link #getExistingEmbedding(Graph)}.
	 *
	 * <p>
	 * If weights with this key exist in a graph, algorithms can assume the graph is planar and use the embedding
	 * information. Note that if the graph is changed after the embedding is computed, the embedding may become invalid,
	 * and its recommended to remove the weights from the graph using {@link Graph#removeEdgesWeights(String)}.
	 */
	public static final String EdgeOrderWeightKey = "_planar_embedding_order";

}
