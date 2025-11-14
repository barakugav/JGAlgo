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

import static com.jgalgo.internal.util.Range.range;
import java.util.Objects;
import java.util.Random;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;

class NeighborSamplers {

    private NeighborSamplers() {}

    private abstract static class SamplerBase implements NeighborSampler.Int {

        final IndexGraph g;
        final Random rand = new Random();
        final int n;

        SamplerBase(IndexGraph g) {
            this.g = g;
            this.n = g.vertices().size();
        }

        @Override
        public void setSeed(long seed) {
            rand.setSeed(seed);
        }

        @Override
        public IntGraph graph() {
            return g;
        }
    }

    static final class UniformNeighborSampler extends SamplerBase {

        private final int[] edges;
        private final int[] edgesOffset;

        UniformNeighborSampler(IndexGraph g) {
            super(g);

            final int edgesArrSize;
            if (g.isDirected()) {
                edgesArrSize = g.edges().size();
            } else {
                edgesArrSize = 2 * g.edges().size() - Graphs.selfEdges(g).size();
            }

            edges = new int[edgesArrSize];
            edgesOffset = new int[n + 1];
            int offset = 0;
            for (int u : range(n)) {
                edgesOffset[u] = offset;
                for (int e : g.outEdges(u))
                    edges[offset++] = e;
            }
            assert offset == edgesArrSize;
            edgesOffset[n] = offset;
        }

        @Override
        public int sample(int vertex) {
            Assertions.checkVertex(vertex, n);
            int vOutEdgesNum = edgesOffset[vertex + 1] - edgesOffset[vertex];
            if (vOutEdgesNum <= 0)
                return -1;
            return edges[edgesOffset[vertex] + rand.nextInt(vOutEdgesNum)];
        }
    }

    static final class WeightedNeighborSampler extends SamplerBase {

        private final int[] edges;
        private final int[] edgesOffset;
        private final double[] edgesWeights;

        WeightedNeighborSampler(IndexGraph g, IWeightFunction weightFunc) {
            super(g);
            Objects.requireNonNull(weightFunc);

            int outEdgesSize = 0;
            for (int u : range(n)) {
                for (int e : g.outEdges(u)) {
                    double ew = weightFunc.weight(e);
                    if (ew < 0)
                        throw new IllegalArgumentException("only positive weights are supported: " + ew);
                    if (ew > 0) /* discard edges with weight 0 */
                        outEdgesSize++;
                }
            }

            edges = new int[outEdgesSize];
            edgesOffset = new int[n + 1];
            edgesWeights = new double[outEdgesSize];
            int offset = 0;
            for (int u : range(n)) {
                edgesOffset[u] = offset;
                double weightSum = 0;
                for (int e : g.outEdges(u)) {
                    double ew = weightFunc.weight(e);
                    if (ew == 0)
                        continue;
                    weightSum += ew;
                    edges[offset] = e;
                    edgesWeights[offset] = weightSum;
                    offset++;
                }
            }
            assert offset == outEdgesSize;
            edgesOffset[n] = offset;
        }

        @Override
        public int sample(int vertex) {
            Assertions.checkVertex(vertex, n);
            int from = edgesOffset[vertex];
            int to = edgesOffset[vertex + 1];
            if (from >= to)
                return -1;

            final double maxWeight = edgesWeights[edgesOffset[vertex + 1] - 1];
            final double randWeight = rand.nextDouble() * maxWeight;

            if (to - from <= 64) {
                /* linear search */
                for (; from < to; from++)
                    if (edgesWeights[from] >= randWeight)
                        break;
            } else {
                /* binary search */
                for (to--; from <= to;) {
                    final int mid = (from + to) >>> 1;
                    if (edgesWeights[mid] < randWeight) {
                        from = mid + 1;
                    } else {
                        to = mid - 1;
                    }
                }
            }

            return edges[from];
        }
    }

    private static class IntImpl implements NeighborSampler.Int {

        private final IntGraph g;
        private final NeighborSampler.Int indexSampler;
        private final IndexIntIdMap vIdMap;
        private final IndexIntIdMap eIdMap;

        IntImpl(NeighborSampler.Int indexSampler, IntGraph g) {
            this.g = g;
            this.indexSampler = Objects.requireNonNull(indexSampler);
            this.vIdMap = g.indexGraphVerticesMap();
            this.eIdMap = g.indexGraphEdgesMap();
        }

        @Override
        public void setSeed(long seed) {
            indexSampler.setSeed(seed);
        }

        @Override
        public int sample(int vertex) {
            int vId = vIdMap.idToIndex(vertex);
            int eId = indexSampler.sample(vId);
            return eId < 0 ? -1 : eIdMap.indexToIdInt(eId);
        }

        @Override
        public IntGraph graph() {
            return g;
        }
    }

    private static class ObjImpl<V, E> implements NeighborSampler<V, E> {

        private final Graph<V, E> g;
        private final NeighborSampler.Int indexSampler;
        private final IndexIdMap<V> vIdMap;
        private final IndexIdMap<E> eIdMap;

        ObjImpl(NeighborSampler.Int indexSampler, Graph<V, E> g) {
            this.g = g;
            this.indexSampler = Objects.requireNonNull(indexSampler);
            this.vIdMap = g.indexGraphVerticesMap();
            this.eIdMap = g.indexGraphEdgesMap();
        }

        @Override
        public void setSeed(long seed) {
            indexSampler.setSeed(seed);
        }

        @Override
        public E sample(V vertex) {
            int vId = vIdMap.idToIndex(vertex);
            int eId = indexSampler.sample(vId);
            return eId < 0 ? null : eIdMap.indexToId(eId);
        }

        @Override
        public Graph<V, E> graph() {
            return g;
        }
    }

    @SuppressWarnings("unchecked")
    static <V, E> NeighborSampler<V, E> fromIndexSampler(NeighborSampler.Int indexSampler, Graph<V, E> graph) {
        assert !(graph instanceof IndexGraph);
        if (graph instanceof IntGraph) {
            return (NeighborSampler<V, E>) new IntImpl(indexSampler, (IntGraph) graph);
        } else {
            return new ObjImpl<>(indexSampler, graph);
        }
    }

}
