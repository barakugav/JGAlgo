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
package com.jgalgo.alg.traversal;

import com.jgalgo.alg.common.RandomizedAlgorithm;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

/**
 * Random neighbor sampler.
 *
 * <p>
 * A neighbor sampler is a generic interface that implements a single method {@link #sample(Object)} that samples a
 * neighbor of a given vertex. Different implementations of the interface can sample neighbors uniformly or with a
 * given weight function. The {@link RandomWalkIter} iterator is implemented using a neighbor sampler and use it in
 * a specific way by sampling the neighbor of the last returned vertex.
 *
 * @see        RandomWalkIter
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface NeighborSampler<V, E> extends RandomizedAlgorithm {

    /**
     * Sample a neighbor of the given vertex.
     *
     * @param  vertex                the vertex to sample from
     * @return                       the sampled neighbor, maybe be {@code null} if there are no neighbors or some other
     *                               reason the
     *                               implementation decides to return {@code null} (for example if some vertices are
     *                               filtered out).
     * @throws NoSuchVertexException if the vertex is not in the graph
     */
    E sample(V vertex);

    /**
     * Get the graph this sampler is sampling from.
     *
     * @return the graph this sampler is sampling from
     */
    Graph<V, E> graph();

    /**
     * Create a new sampler that samples neighbors according to a uniform distribution of the out edges of a vertex.
     *
     * <p>
     * Given a vertex in the graph, a random out-edge is sampled uniformly from the set of all out edges, and the other
     * endpoint of the edge is returned. This has some implications:
     * <ul>
     * <li>If the graph contains parallel edges, the distribution will be different from a uniform distribution over the
     * neighbors, as a neighbor with two edges will be sampled twice as often as a neighbor with only one edge.</li>
     * <li>If the graph contains self edges, the sampling can return the vertex itself.</li>
     * <li>If the graph is directed, neighbors connected with in-edges will not be sampled.</li>
     * </ul>
     *
     * @param  <V> the vertices type
     * @param  <E> the edges type
     * @param  g   the graph to sample from
     * @return     a new sampler that samples neighbors according to a uniform distribution of the out edges of a vertex
     */
    @SuppressWarnings("unchecked")
    static <V, E> NeighborSampler<V, E> edgeUniform(Graph<V, E> g) {
        if (g instanceof IndexGraph) {
            return (NeighborSampler<V, E>) new NeighborSamplers.UniformNeighborSampler((IndexGraph) g);
        } else {
            NeighborSampler.Int indexSampler = new NeighborSamplers.UniformNeighborSampler(g.indexGraph());
            return NeighborSamplers.fromIndexSampler(indexSampler, g);
        }
    }

    /**
     * Create a new sampler that samples neighbors according to a weighted distribution of the out edges of a vertex.
     *
     * <p>
     * Given a vertex in the graph, a random out-edge is sampled according to the weights of the edges, and the other
     * endpoint of the edge is returned. Self edges are also considered in the sampling, along with parallel edges.
     * Neighbors connected with in-edges will not be sampled.
     *
     * @param  <V>                      the vertices type
     * @param  <E>                      the edges type
     * @param  g                        the graph to sample from
     * @param  weights                  the edge weight function
     * @return                          a new sampler that samples neighbors according to a weighted distribution of the
     *                                  out edges of a
     *                                  vertex
     * @throws IllegalArgumentException if any edge weight is negative
     */
    @SuppressWarnings("unchecked")
    static <V, E> NeighborSampler<V, E> edgeWeighted(Graph<V, E> g, WeightFunction<E> weights) {
        if (g instanceof IndexGraph) {
            IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) weights);
            return (NeighborSampler<V, E>) new NeighborSamplers.WeightedNeighborSampler((IndexGraph) g, w0);
        } else {
            IWeightFunction w0 = IndexIdMaps.idToIndexWeightFunc(weights, g.indexGraphEdgesMap());
            NeighborSampler.Int indexSampler = new NeighborSamplers.WeightedNeighborSampler(g.indexGraph(), w0);
            return NeighborSamplers.fromIndexSampler(indexSampler, g);
        }
    }

    /**
     * Random neighbor sampler for {@link IntGraph}.
     *
     * @author Barak Ugav
     */
    static interface Int extends NeighborSampler<Integer, Integer> {

        /**
         * Sample a neighbor of the given vertex.
         *
         * @param  vertex                the vertex to sample from
         * @return                       the sampled neighbor, maybe be {@code -1} if there are no neighbors or some
         *                               other reason the implementation decides to return {@code -1} (for example if
         *                               some vertices are filtered out).
         * @throws NoSuchVertexException if the vertex is not in the graph
         */
        int sample(int vertex);

        /**
         * {@inheritDoc}
         *
         * @deprecated Please use {@link #sample(int)} instead to avoid un/boxing.
         */
        @Deprecated
        @Override
        default Integer sample(Integer vertex) {
            int e = sample(vertex.intValue());
            return e < 0 ? null : Integer.valueOf(e);
        }

        @Override
        IntGraph graph();
    }

}
