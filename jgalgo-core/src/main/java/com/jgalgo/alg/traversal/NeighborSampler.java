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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

/**
 *
 *
 * @author Barak Ugav
 */
public interface NeighborSampler<V, E> {

    E sample(V vertex);

    Graph<V, E> graph();

    @SuppressWarnings("unchecked")
    static <V, E> NeighborSampler<V, E> uniform(Graph<V, E> g) {
        if (g instanceof IndexGraph) {
            return (NeighborSampler<V, E>) new NeighborSamplers.UniformNeighborSampler((IndexGraph) g);
        } else {
            NeighborSampler.Int indexSampler = new NeighborSamplers.UniformNeighborSampler(g.indexGraph());
            return NeighborSamplers.fromIndexSampler(indexSampler, g);
        }
    }

    @SuppressWarnings("unchecked")
    static <V, E> NeighborSampler<V, E> weighted(Graph<V, E> g, WeightFunction<E> weights) {
        if (g instanceof IndexGraph) {
            IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) weights);
            return (NeighborSampler<V, E>) new NeighborSamplers.WeightedNeighborSampler((IndexGraph) g, w0);
        } else {
            IWeightFunction w0 = IndexIdMaps.idToIndexWeightFunc(weights, g.indexGraphEdgesMap());
            NeighborSampler.Int indexSampler = new NeighborSamplers.WeightedNeighborSampler(g.indexGraph(), w0);
            return NeighborSamplers.fromIndexSampler(indexSampler, g);
        }
    }

    static interface Int extends NeighborSampler<Integer, Integer> {

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
