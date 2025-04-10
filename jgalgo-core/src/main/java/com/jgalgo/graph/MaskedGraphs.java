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

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterables;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

class MaskedGraphs {

    private MaskedGraphs() {}

    private abstract static class MaskedGraphBase<V, E> extends AbstractGraph<V, E> {

        private final Graph<V, E> g;

        MaskedGraphBase(Graph<V, E> g) {
            this.g = Objects.requireNonNull(g);
        }

        Graph<V, E> g() {
            return g;
        }

        @Override
        public final boolean isDirected() {
            return g.isDirected();
        }

        @Override
        public Set<String> verticesWeightsKeys() {
            return g.verticesWeightsKeys();
        }

        @Override
        public Set<String> edgesWeightsKeys() {
            return g.edgesWeightsKeys();
        }

        @Override
        public void removeEdgesWeights(String key) {
            g.removeEdgesWeights(key);
        }

        @Override
        public void removeVertices(Collection<? extends V> vertices) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeEdges(Collection<? extends E> edges) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void ensureVertexCapacity(int vertexCapacity) {}

        @Override
        public void ensureEdgeCapacity(int edgeCapacity) {}

        @Override
        public boolean isAllowSelfEdges() {
            return g.isAllowSelfEdges();
        }

        @Override
        public boolean isAllowParallelEdges() {
            return g.isAllowParallelEdges();
        }

        @Override
        public void removeVerticesWeights(String key) {
            g.removeVerticesWeights(key);
        }

        @Override
        public void addVertices(Collection<? extends V> vertices) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void clearEdges() {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }
    }

    private abstract static class MaskedIntGraphBase extends MaskedGraphBase<Integer, Integer> implements IntGraph {

        MaskedIntGraphBase(IntGraph g) {
            super(g);
        }

        @Override
        IntGraph g() {
            return (IntGraph) super.g();
        }

        @Override
        public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
                T defVal) {
            g().addVerticesWeights(key, type, defVal);
            return verticesWeights(key);
        }

        @Override
        public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
                T defVal) {
            g().addEdgesWeights(key, type, defVal);
            return edgesWeights(key);
        }

        @Override
        public int addVertexInt() {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeVertex(int vertex) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public int addEdge(int source, int target) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeEdge(int edge) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeEdgesOf(int vertex) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeOutEdgesOf(int source) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeInEdgesOf(int target) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }
    }

    static class MaskedRenamedIndexGraph extends MaskedIntGraphBase implements IndexGraph {

        private final GraphElementSet vertices;
        private final GraphElementSet edges;

        // Mapping from original vertices indices to API indices.
        //
        // If an original vertex is not contained in this map, its index remains the same.
        // If an original vertex is mapped to -1, it is masked.
        // If an original vertex is mapped to any other value (always positive), its hes new index.
        final Int2IntMap vOrig2Api;

        // Mapping from API vertices indices to original indices.
        //
        // If an API vertex is not contained in this map, its index remains the same.
        // If an API vertex is mapped to any value (always positive), it was renamed and its hes original index.
        final Int2IntMap vApi2OrigRenamed;

        final Int2IntMap eOrig2Api;
        final Int2IntMap eApi2OrigRenamed;

        private IndexIntIdMap verticesIdMap;
        private IndexIntIdMap edgesIdMap;

        private static final int MASKED = -1;

        static final Int2IntMap EmptyMap;

        static {
            Int2IntMap emptyMap = new Int2IntOpenHashMap(0);
            emptyMap.defaultReturnValue(Integer.MAX_VALUE);
            // emptyEdgeMap = Int2IntMaps.unmodifiable(emptyEdgeMap);
            EmptyMap = emptyMap;
        }

        static Int2IntMap fixedSizedMap(int size) {
            if (size == 0) {
                return EmptyMap;
            } else {
                Int2IntMap map = new Int2IntOpenHashMap(size);
                map.defaultReturnValue(Integer.MAX_VALUE);
                return map;
            }
        }

        MaskedRenamedIndexGraph(IndexGraph graph, int[] maskedVertices, int[] maskedEdges) {
            super(graph);
            final int nOrig = g().vertices().size();
            final int mOrig = g().edges().size();

            JGAlgoUtils.sort(maskedVertices, JGAlgoConfigImpl.ParallelByDefault);
            assert maskedVertices.length == 0 || (0 <= maskedVertices[0] && maskedVertices[0] < nOrig);

            JGAlgoUtils.sort(maskedEdges, JGAlgoConfigImpl.ParallelByDefault);
            assert maskedEdges.length == 0 || (0 <= maskedEdges[0] && maskedEdges[0] < mOrig);

            var verticesMaps = computeMaps(nOrig, maskedVertices);
            vOrig2Api = verticesMaps.first();
            vApi2OrigRenamed = verticesMaps.second();

            var edgesMaps = computeMaps(mOrig, maskedEdges);
            eOrig2Api = edgesMaps.first();
            eApi2OrigRenamed = edgesMaps.second();

            this.vertices = GraphElementSet.Immutable.ofVertices(nOrig - maskedVertices.length);
            this.edges = GraphElementSet.Immutable.ofEdges(mOrig - maskedEdges.length);
        }

        private static Pair<Int2IntMap, Int2IntMap> computeMaps(int origElementsNum, int[] masked) {
            int movedElementsNum = (int) Arrays.stream(masked).filter(i -> i < origElementsNum - masked.length).count();

            Int2IntMap orig2api = fixedSizedMap(masked.length + movedElementsNum);
            Int2IntMap api2origRenamed = fixedSizedMap(masked.length);
            for (int maskBegin = 0, maskEnd = masked.length - 1, origIdx = origElementsNum - 1; maskBegin <= maskEnd;) {
                assert masked[maskEnd] <= origIdx;
                if (masked[maskEnd] == origIdx) {
                    int maskedElm = masked[maskEnd--];
                    orig2api.put(maskedElm, MASKED);
                    origIdx--;
                } else {
                    int maskedElm = masked[maskBegin++];
                    orig2api.put(maskedElm, MASKED);
                    orig2api.put(origIdx, maskedElm);
                    api2origRenamed.put(maskedElm, origIdx);
                    origIdx--;
                }
            }

            assert orig2api.size() == masked.length + movedElementsNum;
            assert api2origRenamed.size() == movedElementsNum;

            return Pair.of(orig2api, api2origRenamed);
        }

        @Override
        IndexGraph g() {
            return (IndexGraph) super.g();
        }

        int vOrig(int vApi) {
            assert vApi >= 0;
            return vApi2OrigRenamed.getOrDefault(vApi, vApi);
        }

        int vApi(int vOrig) {
            return vOrig2Api.getOrDefault(vOrig, vOrig);
        }

        int eOrig(int eApi) {
            assert eApi >= 0;
            return eApi2OrigRenamed.getOrDefault(eApi, eApi);
        }

        int eApi(int eOrig) {
            return eOrig2Api.getOrDefault(eOrig, eOrig);
        }

        @Override
        public final GraphElementSet vertices() {
            return vertices;
        }

        @Override
        public final GraphElementSet edges() {
            return edges;
        }

        void checkVertex(int vertex) {
            Assertions.checkVertex(vertex, vertices.size);
        }

        void checkEdge(int edge) {
            Assertions.checkEdge(edge, edges.size);
        }

        @Override
        public final int edgeSource(int edge) {
            checkEdge(edge);
            return vApi(g().edgeSource(eOrig(edge)));
        }

        @Override
        public final int edgeTarget(int edge) {
            checkEdge(edge);
            return vApi(g().edgeTarget(eOrig(edge)));
        }

        @Override
        public int getEdge(int source, int target) {
            checkVertex(source);
            checkVertex(target);
            int u = vOrig(source), v = vOrig(target);

            int eOrig = g().getEdge(u, v), e;
            if (eOrig < 0)
                return -1; // not found
            if ((e = eApi(eOrig)) >= 0)
                return e; // found an edge, not masked
            if (!g().isAllowParallelEdges())
                return -1; // found a masked edge, there are no more edges as the graph doesnt support parallel edges

            IEdgeIter iter = new MaskedEdgeSet(g().getEdges(u, v)).iterator();
            return iter.hasNext() ? iter.nextInt() : -1;
        }

        @Override
        public int edgeEndpoint(int edge, int endpoint) {
            checkEdge(edge);
            checkVertex(endpoint);
            return vApi(g().edgeEndpoint(eOrig(edge), vOrig(endpoint)));
        }

        @Override
        public void moveEdge(int edge, int newSource, int newTarget) {
            checkEdge(edge);
            checkVertex(newSource);
            checkVertex(newTarget);
            g().moveEdge(eOrig(edge), vOrig(newSource), vOrig(newTarget));
        }

        @Override
        public IEdgeSet outEdges(int source) {
            checkVertex(source);
            return new MaskedEdgeSet(g().outEdges(vOrig(source)));
        }

        @Override
        public IEdgeSet inEdges(int target) {
            checkVertex(target);
            return new MaskedEdgeSet(g().inEdges(vOrig(target)));
        }

        @Override
        public IEdgeSet getEdges(int source, int target) {
            checkVertex(source);
            checkVertex(target);
            return new MaskedEdgeSet(g().getEdges(vOrig(source), vOrig(target)));
        }

        class MaskedEdgeSet extends AbstractIntSet implements IEdgeSet {

            private final IEdgeSet set;

            MaskedEdgeSet(IEdgeSet set) {
                this.set = set;
            }

            @Override
            public int size() {
                return (int) IntIterables.size(this);
            }

            @Override
            public boolean isEmpty() {
                return !iterator().hasNext();
            }

            @Override
            public boolean contains(int key) {
                return MaskedRenamedIndexGraph.this.edges().contains(key) && set.contains(eOrig(key));
            }

            @Override
            public boolean containsAll(IntCollection c) {
                return MaskedRenamedIndexGraph.this.edges().containsAll(c)
                        && c.intStream().allMatch(key -> set.contains(eOrig(key)));
            }

            @Override
            public IEdgeIter iterator() {
                return new MaskedEdgeIter(set.iterator());
            }
        }

        class MaskedEdgeIter implements EdgeIters.IBase {

            private final IEdgeIter iter;
            private int nextEdge;
            private int sourceOrig, targetOrig;

            MaskedEdgeIter(IEdgeIter iter) {
                this.iter = iter;
                sourceOrig = targetOrig = -1;
                advance();
            }

            private void advance() {
                int nextEdge = -1;
                while (iter.hasNext() && (nextEdge = eApi(iter.peekNextInt())) < 0)
                    iter.nextInt();
                this.nextEdge = nextEdge;
            }

            @Override
            public boolean hasNext() {
                return nextEdge >= 0;
            }

            @Override
            public int nextInt() {
                iter.nextInt();
                int e = nextEdge;
                sourceOrig = iter.sourceInt();
                targetOrig = iter.targetInt();
                advance();
                return e;
            }

            @Override
            public int peekNextInt() {
                if (nextEdge < 0)
                    throw new NoSuchElementException();
                return nextEdge;
            }

            @Override
            public int sourceInt() {
                return vApi(sourceOrig);
            }

            @Override
            public int targetInt() {
                return vApi(targetOrig);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
            IWeights<T> weights = g().verticesWeights(key);
            if (weights == null)
                return null;
            return (WeightsT) WeightsImpl.maskedIndexWeights(weights, vertices, true, vApi2OrigRenamed);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
            IWeights<T> weights = g().edgesWeights(key);
            if (weights == null)
                return null;
            return (WeightsT) WeightsImpl.maskedIndexWeights(weights, edges, false, eApi2OrigRenamed);
        }

        @Override
        public IntSet addEdgesReassignIds(IEdgeSet edges) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void addVertexRemoveListener(IndexRemoveListener listener) {}

        @Override
        public void removeVertexRemoveListener(IndexRemoveListener listener) {}

        @Override
        public void addEdgeRemoveListener(IndexRemoveListener listener) {}

        @Override
        public void removeEdgeRemoveListener(IndexRemoveListener listener) {}

        @Deprecated
        @Override
        public IndexIntIdMap indexGraphVerticesMap() {
            if (verticesIdMap == null)
                verticesIdMap = IndexIntIdMap.identityVerticesMap(vertices);
            return verticesIdMap;
        }

        @Deprecated
        @Override
        public IndexIntIdMap indexGraphEdgesMap() {
            if (edgesIdMap == null)
                edgesIdMap = IndexIntIdMap.identityEdgesMap(edges);
            return edgesIdMap;
        }
    }

    static class MaskedIntGraph extends MaskedIntGraphBase {

        private final IntSet maskedVertices;
        private final IntSet maskedEdges;

        private final IntSet vertices;
        private final IntSet edges;

        private MaskedRenamedIndexGraph indexGraph;
        private IndexIntIdMap indexGraphVerticesMap;
        private IndexIntIdMap indexGraphEdgesMap;

        MaskedIntGraph(IntGraph g, Collection<Integer> maskedVertices, Collection<Integer> maskedEdges) {
            super(g);

            this.maskedVertices = new IntOpenHashSet(maskedVertices);
            if (this.maskedVertices.size() != maskedVertices.size())
                throw new IllegalArgumentException("masked vertices must be unique");
            if (!g.vertices().containsAll(this.maskedVertices))
                throw NoSuchVertexException
                        .ofVertex(this.maskedVertices
                                .intStream()
                                .filter(i -> !g.vertices().contains(i))
                                .findAny()
                                .getAsInt());
            vertices = new MaskedSet(g.vertices(), this.maskedVertices);

            this.maskedEdges = new IntOpenHashSet(maskedEdges);
            if (this.maskedEdges.size() != maskedEdges.size())
                throw new IllegalArgumentException("masked edges must be unique");
            if (!g.edges().containsAll(this.maskedEdges))
                throw NoSuchEdgeException
                        .ofEdge(this.maskedEdges.intStream().filter(i -> !g.edges().contains(i)).findAny().getAsInt());
            if (g().isDirected()) {
                for (int v : this.maskedVertices) {
                    this.maskedEdges.addAll(g().outEdges(v));
                    this.maskedEdges.addAll(g().inEdges(v));
                }
            } else {
                for (int v : this.maskedVertices)
                    this.maskedEdges.addAll(g().outEdges(v));
            }
            edges = new MaskedSet(g.edges(), this.maskedEdges);
        }

        @Override
        public IntSet vertices() {
            return vertices;
        }

        @Override
        public IntSet edges() {
            return edges;
        }

        private static class MaskedSet extends AbstractIntSet {

            private final IntSet set;
            private final IntSet mask;

            MaskedSet(IntSet set, IntSet mask) {
                this.set = set;
                this.mask = mask;
            }

            @Override
            public boolean contains(int key) {
                return set.contains(key) && !mask.contains(key);
            }

            @Override
            public boolean containsAll(IntCollection c) {
                return set.containsAll(c) && c.intStream().noneMatch(mask::contains);
            }

            @Override
            public int size() {
                return set.size() - mask.size();
            }

            @Override
            public IntIterator iterator() {
                return new MaskedIterator(set.iterator(), mask);
            }
        }

        private static class MaskedIterator implements IterTools.Peek.Int {

            private final IterTools.Peek.Int iter;
            private final IntSet mask;

            MaskedIterator(IntIterator iter, IntSet mask) {
                this.iter =
                        (iter instanceof IterTools.Peek.Int) ? (IterTools.Peek.Int) iter : IterTools.Peek.Int.of(iter);
                this.mask = Objects.requireNonNull(mask);
                advance();
            }

            private void advance() {
                while (iter.hasNext() && mask.contains(iter.peekNextInt()))
                    iter.nextInt();
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public int nextInt() {
                int next = iter.nextInt();
                advance();
                return next;
            }

            @Override
            public int peekNextInt() {
                return iter.peekNextInt();
            }
        }

        private void checkVertexNotMasked(int v) {
            if (maskedVertices.contains(v))
                throw NoSuchVertexException.ofVertex(v);
        }

        private void checkEdgeNotMasked(int e) {
            if (maskedEdges.contains(e))
                throw NoSuchEdgeException.ofEdge(e);
        }

        @Override
        public int edgeSource(int edge) {
            checkEdgeNotMasked(edge);
            return g().edgeSource(edge);
        }

        @Override
        public int edgeTarget(int edge) {
            checkEdgeNotMasked(edge);
            return g().edgeTarget(edge);
        }

        @Override
        public int edgeEndpoint(int edge, int endpoint) {
            checkEdgeNotMasked(edge);
            assert !maskedVertices.contains(endpoint) : "edges of masked vertices should be masked";
            return g().edgeEndpoint(edge, endpoint);
        }

        @Override
        public int getEdge(int source, int target) {
            checkVertexNotMasked(source);
            checkVertexNotMasked(target);

            int e = g().getEdge(source, target);
            if (e < 0)
                return -1; // not found
            if (!maskedEdges.contains(e))
                return e; // found an edge, not masked
            if (!g().isAllowParallelEdges())
                return -1; // found a masked edge, there are no more edges as the graph doesnt support parallel edges

            IEdgeIter iter = new MaskedEdgeSet(g().getEdges(source, target), maskedEdges).iterator();
            return iter.hasNext() ? iter.nextInt() : -1;
        }

        @Override
        public IEdgeSet getEdges(int source, int target) {
            checkVertexNotMasked(source);
            checkVertexNotMasked(target);
            return new MaskedEdgeSet(g().getEdges(source, target), maskedEdges);
        }

        @Override
        public IEdgeSet outEdges(int source) {
            checkVertexNotMasked(source);
            return new MaskedEdgeSet(g().outEdges(source), maskedEdges);
        }

        @Override
        public IEdgeSet inEdges(int target) {
            checkVertexNotMasked(target);
            return new MaskedEdgeSet(g().inEdges(target), maskedEdges);
        }

        private static class MaskedEdgeSet extends AbstractIntSet implements IEdgeSet {

            private final IEdgeSet set;
            private final IntSet maskedEdges;

            MaskedEdgeSet(IEdgeSet set, IntSet mask) {
                this.set = set;
                this.maskedEdges = mask;
            }

            @Override
            public int size() {
                return (int) IntIterables.size(this);
            }

            @Override
            public boolean isEmpty() {
                return !iterator().hasNext();
            }

            @Override
            public boolean contains(int key) {
                return set.contains(key) && !maskedEdges.contains(key);
            }

            @Override
            public boolean containsAll(IntCollection c) {
                return set.containsAll(c) && c.intStream().noneMatch(maskedEdges::contains);
            }

            @Override
            public IEdgeIter iterator() {
                return new MaskedEdgeIter(set.iterator(), maskedEdges);
            }
        }
        private static class MaskedEdgeIter implements EdgeIters.IBase {

            private final IEdgeIter iter;
            private final IntSet mask;
            private int source, target;

            MaskedEdgeIter(IEdgeIter iter, IntSet mask) {
                this.iter = iter;
                this.mask = Objects.requireNonNull(mask);
                source = target = -1;
                advance();
            }

            private void advance() {
                while (iter.hasNext() && mask.contains(iter.peekNextInt()))
                    iter.nextInt();
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public int nextInt() {
                int next = iter.nextInt();
                source = iter.sourceInt();
                target = iter.targetInt();
                advance();
                return next;
            }

            @Override
            public int peekNextInt() {
                return iter.peekNextInt();
            }

            @Override
            public int sourceInt() {
                return source;
            }

            @Override
            public int targetInt() {
                return target;
            }
        }

        @Override
        public void moveEdge(int edge, int newSource, int newTarget) {
            checkEdgeNotMasked(edge);
            checkVertexNotMasked(newSource);
            checkVertexNotMasked(newTarget);
            g().moveEdge(edge, newSource, newTarget);
        }

        @Override
        public IdBuilderInt vertexBuilder() {
            return g().vertexBuilder();
        }

        @Override
        public IdBuilderInt edgeBuilder() {
            return g().edgeBuilder();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
            IWeights<T> weights = g().verticesWeights(key);
            if (weights == null)
                return null;
            return (WeightsT) WeightsImpl.maskedIntWeights(weights, maskedVertices, true);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
            IWeights<T> weights = g().edgesWeights(key);
            if (weights == null)
                return null;
            return (WeightsT) WeightsImpl.maskedIntWeights(weights, maskedEdges, false);
        }

        @Override
        public void addVertex(int vertex) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void addEdge(int source, int target, int edge) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void renameVertex(int vertex, int newId) {
            throw new UnsupportedOperationException("Can't change identifier in a masked graph");
        }

        @Override
        public void renameEdge(int edge, int newId) {
            throw new UnsupportedOperationException("Can't change identifier in a masked graph");
        }

        @Override
        public IndexGraph indexGraph() {
            if (indexGraph == null) {
                IndexIntIdMap viMap = g().indexGraphVerticesMap();
                IndexIntIdMap eiMap = g().indexGraphEdgesMap();

                int[] maskedVerticesArr = maskedVertices.intStream().map(viMap::idToIndex).toArray();
                int[] maskedEdgesArr = maskedEdges.intStream().map(eiMap::idToIndex).toArray();

                indexGraph = new MaskedRenamedIndexGraph(g().indexGraph(), maskedVerticesArr, maskedEdgesArr);

                indexGraphVerticesMap = new IndexIntIdMapImpl(viMap, indexGraph.vOrig2Api, indexGraph.vApi2OrigRenamed,
                        vertices.size(), true);
                indexGraphEdgesMap = new IndexIntIdMapImpl(eiMap, indexGraph.eOrig2Api, indexGraph.eApi2OrigRenamed,
                        edges.size(), false);
            }
            return indexGraph;
        }

        private static class IndexIntIdMapImpl implements IndexIntIdMap {

            private final IndexIntIdMap origMap;
            private final Int2IntMap orig2api;
            private final Int2IntMap api2origRenamed;
            private final int elementNum;
            private final boolean isVertices;

            IndexIntIdMapImpl(IndexIntIdMap origMap, Int2IntMap orig2api, Int2IntMap api2origRenamed, int elementNum,
                    boolean isVertices) {
                this.origMap = origMap;
                this.orig2api = orig2api;
                this.api2origRenamed = api2origRenamed;
                this.elementNum = elementNum;
                this.isVertices = isVertices;
            }

            @Override
            public int indexToIdInt(int index) {
                int id = indexToIdIfExistInt(index);
                if (id < 0) {
                    if (isVertices) {
                        throw NoSuchVertexException.ofIndex(index);
                    } else {
                        throw NoSuchEdgeException.ofIndex(index);
                    }
                }
                return id;
            }

            @Override
            public int indexToIdIfExistInt(int index) {
                if (index < 0 || index >= elementNum)
                    return -1;
                int indexOrig = api2origRenamed.getOrDefault(index, index);
                return origMap.indexToIdInt(indexOrig);
            }

            @Override
            public int idToIndex(int id) {
                int idxOrig = origMap.idToIndex(id);
                int idxApi = orig2api.getOrDefault(idxOrig, idxOrig);
                if (idxApi < 0) {
                    if (isVertices) {
                        throw NoSuchVertexException.ofVertex(id);
                    } else {
                        throw NoSuchEdgeException.ofEdge(id);
                    }
                }
                return idxApi;

            }

            @Override
            public int idToIndexIfExist(int id) {
                int idxOrig = origMap.idToIndexIfExist(id);
                if (idxOrig < 0)
                    return -1;
                return orig2api.getOrDefault(idxOrig, idxOrig);
            }
        }

        @Override
        public IndexIntIdMap indexGraphVerticesMap() {
            indexGraph(); // init index graph and maps
            assert indexGraphVerticesMap != null;
            return indexGraphVerticesMap;
        }

        @Override
        public IndexIntIdMap indexGraphEdgesMap() {
            indexGraph(); // init index graph and maps
            assert indexGraphEdgesMap != null;
            return indexGraphEdgesMap;
        }
    }

    static class MaskedObjGraph<V, E> extends MaskedGraphBase<V, E> {

        private final ObjectSet<V> maskedVertices;
        private final ObjectSet<E> maskedEdges;

        private final ObjectSet<V> vertices;
        private final ObjectSet<E> edges;

        private MaskedRenamedIndexGraph indexGraph;
        private IndexIdMap<V> indexGraphVerticesMap;
        private IndexIdMap<E> indexGraphEdgesMap;

        MaskedObjGraph(Graph<V, E> g, Collection<V> maskedVertices, Collection<E> maskedEdges) {
            super(g);

            this.maskedVertices = new ObjectOpenHashSet<>(maskedVertices);
            if (this.maskedVertices.size() != maskedVertices.size())
                throw new IllegalArgumentException("masked vertices are not unique");
            if (!g.vertices().containsAll(this.maskedVertices))
                throw NoSuchVertexException
                        .ofVertex(this.maskedVertices.stream().filter(v -> !g.vertices().contains(v)).findAny().get());
            vertices = new MaskedSet<>(g.vertices(), this.maskedVertices);

            this.maskedEdges = new ObjectOpenHashSet<>(maskedEdges);
            if (this.maskedEdges.size() != maskedEdges.size())
                throw new IllegalArgumentException("masked edges are not unique");
            if (!g.edges().containsAll(this.maskedEdges))
                throw NoSuchEdgeException
                        .ofEdge(this.maskedEdges.stream().filter(e -> !g.edges().contains(e)).findAny().get());
            if (g.isDirected()) {
                for (V v : this.maskedVertices) {
                    this.maskedEdges.addAll(g.outEdges(v));
                    this.maskedEdges.addAll(g.inEdges(v));
                }
            } else {
                for (V v : this.maskedVertices)
                    this.maskedEdges.addAll(g.outEdges(v));
            }
            edges = new MaskedSet<>(g.edges(), this.maskedEdges);
        }

        @Override
        public Set<V> vertices() {
            return vertices;
        }

        @Override
        public Set<E> edges() {
            return edges;
        }

        private static class MaskedSet<K> extends AbstractObjectSet<K> {

            private final Set<K> set;
            private final Set<K> mask;

            MaskedSet(Set<K> set, Set<K> mask) {
                this.set = set;
                this.mask = mask;
            }

            @Override
            public boolean contains(Object o) {
                return set.contains(o) && !mask.contains(o);
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return set.containsAll(c) && c.stream().noneMatch(mask::contains);
            }

            @Override
            public int size() {
                return set.size() - mask.size();
            }

            @Override
            public ObjectIterator<K> iterator() {
                return new MaskedIterator<>(set.iterator(), mask);
            }
        }

        private static class MaskedIterator<K> implements ObjectIterator<K>, IterTools.Peek<K> {

            private final IterTools.Peek<K> iter;
            private final Set<K> mask;

            MaskedIterator(Iterator<K> iter, Set<K> mask) {
                this.iter = (iter instanceof IterTools.Peek) ? (IterTools.Peek<K>) iter : IterTools.Peek.of(iter);
                this.mask = Objects.requireNonNull(mask);
                advance();
            }

            private void advance() {
                while (iter.hasNext() && mask.contains(iter.peekNext()))
                    iter.next();
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public K next() {
                K next = iter.next();
                advance();
                return next;
            }

            @Override
            public K peekNext() {
                return iter.peekNext();
            }
        }

        private void checkVertexNotMasked(V v) {
            if (maskedVertices.contains(v))
                throw NoSuchVertexException.ofVertex(v);
        }

        private void checkEdgeNotMasked(E e) {
            if (maskedEdges.contains(e))
                throw NoSuchEdgeException.ofEdge(e);
        }

        @Override
        public V edgeSource(E edge) {
            checkEdgeNotMasked(edge);
            return g().edgeSource(edge);
        }

        @Override
        public V edgeTarget(E edge) {
            checkEdgeNotMasked(edge);
            return g().edgeTarget(edge);
        }

        @Override
        public V edgeEndpoint(E edge, V endpoint) {
            checkEdgeNotMasked(edge);
            assert !maskedVertices.contains(endpoint) : "edges of masked vertices should be masked";
            return g().edgeEndpoint(edge, endpoint);
        }

        @Override
        public E getEdge(V source, V target) {
            checkVertexNotMasked(source);
            checkVertexNotMasked(target);

            E e = g().getEdge(source, target);
            if (e == null)
                return null; // not found
            if (!maskedEdges.contains(e))
                return e; // found an edge, not masked
            if (!g().isAllowParallelEdges())
                return null; // found a masked edge, there are no more edges as the graph doesnt support parallel edges

            EdgeIter<V, E> iter = new MaskedEdgeSet<>(g().getEdges(source, target), maskedEdges).iterator();
            return iter.hasNext() ? iter.next() : null;
        }

        @Override
        public EdgeSet<V, E> getEdges(V source, V target) {
            checkVertexNotMasked(source);
            checkVertexNotMasked(target);
            return new MaskedEdgeSet<>(g().getEdges(source, target), maskedEdges);
        }

        @Override
        public EdgeSet<V, E> outEdges(V source) {
            checkVertexNotMasked(source);
            return new MaskedEdgeSet<>(g().outEdges(source), maskedEdges);
        }

        @Override
        public EdgeSet<V, E> inEdges(V target) {
            checkVertexNotMasked(target);
            return new MaskedEdgeSet<>(g().inEdges(target), maskedEdges);
        }

        private static class MaskedEdgeSet<V, E> extends AbstractSet<E> implements EdgeSet<V, E> {

            private final EdgeSet<V, E> set;
            private final ObjectSet<E> maskedEdges;

            MaskedEdgeSet(EdgeSet<V, E> set, ObjectSet<E> mask) {
                this.set = set;
                this.maskedEdges = mask;
            }

            @Override
            public int size() {
                int c = 0;
                for (@SuppressWarnings("unused")
                E dummy : this)
                    c++;
                return c;
            }

            @Override
            public boolean isEmpty() {
                return !iterator().hasNext();
            }

            @Override
            public boolean contains(Object key) {
                return set.contains(key) && !maskedEdges.contains(key);
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return set.containsAll(c) && c.stream().noneMatch(maskedEdges::contains);
            }

            @Override
            public EdgeIter<V, E> iterator() {
                return new MaskedEdgeIter<>(set.iterator(), maskedEdges);
            }
        }
        private static class MaskedEdgeIter<V, E> implements EdgeIters.Base<V, E>, ObjectIterator<E> {

            private final EdgeIter<V, E> iter;
            private final Set<E> mask;
            private V source, target;

            MaskedEdgeIter(EdgeIter<V, E> iter, ObjectSet<E> mask) {
                this.iter = iter;
                this.mask = Objects.requireNonNull(mask);
                advance();
            }

            private void advance() {
                while (iter.hasNext() && mask.contains(iter.peekNext()))
                    iter.next();
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public E next() {
                E next = iter.next();
                source = iter.source();
                target = iter.target();
                advance();
                return next;
            }

            @Override
            public E peekNext() {
                return iter.peekNext();
            }

            @Override
            public V source() {
                return source;
            }

            @Override
            public V target() {
                return target;
            }
        }

        @Override
        public void moveEdge(E edge, V newSource, V newTarget) {
            checkEdgeNotMasked(edge);
            checkVertexNotMasked(newSource);
            checkVertexNotMasked(newTarget);
            g().moveEdge(edge, newSource, newTarget);
        }

        @Override
        public void addVertex(V vertex) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeVertex(V vertex) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void renameVertex(V vertex, V newId) {
            throw new UnsupportedOperationException("Can't change identifier in a masked graph");
        }

        @Override
        public void addEdge(V source, V target, E edge) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeEdge(E edge) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeEdgesOf(V vertex) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeOutEdgesOf(V source) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void removeInEdgesOf(V target) {
            throw new UnsupportedOperationException("Can't add/remove vertices/edges");
        }

        @Override
        public void renameEdge(E edge, E newId) {
            throw new UnsupportedOperationException("Can't change identifier in a masked graph");
        }

        @Override
        public IdBuilder<V> vertexBuilder() {
            return g().vertexBuilder();
        }

        @Override
        public IdBuilder<E> edgeBuilder() {
            return g().edgeBuilder();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T, WeightsT extends Weights<V, T>> WeightsT verticesWeights(String key) {
            Weights<V, T> weights = g().verticesWeights(key);
            if (weights == null)
                return null;
            return (WeightsT) WeightsImpl.<V, T>maskedObjWeights(weights, maskedVertices, true);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T, WeightsT extends Weights<E, T>> WeightsT edgesWeights(String key) {
            Weights<E, T> weights = g().edgesWeights(key);
            if (weights == null)
                return null;
            return (WeightsT) WeightsImpl.<E, T>maskedObjWeights(weights, maskedEdges, false);
        }

        @Override
        public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
                T defVal) {
            g().addVerticesWeights(key, type, defVal);
            return verticesWeights(key);
        }

        @Override
        public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
                T defVal) {
            g().addEdgesWeights(key, type, defVal);
            return edgesWeights(key);
        }

        @Override
        public IndexGraph indexGraph() {
            if (indexGraph == null) {
                IndexIdMap<V> viMap = g().indexGraphVerticesMap();
                IndexIdMap<E> eiMap = g().indexGraphEdgesMap();

                int[] maskedVerticesArr = maskedVertices.stream().mapToInt(viMap::idToIndex).toArray();
                int[] maskedEdgesArr = maskedEdges.stream().mapToInt(eiMap::idToIndex).toArray();

                indexGraph = new MaskedRenamedIndexGraph(g().indexGraph(), maskedVerticesArr, maskedEdgesArr);

                indexGraphVerticesMap = new IndexIdMapImpl<>(viMap, indexGraph.vOrig2Api, indexGraph.vApi2OrigRenamed,
                        vertices.size(), true);
                indexGraphEdgesMap = new IndexIdMapImpl<>(eiMap, indexGraph.eOrig2Api, indexGraph.eApi2OrigRenamed,
                        edges.size(), false);
            }
            return indexGraph;
        }

        private static class IndexIdMapImpl<K> implements IndexIdMap<K> {

            private final IndexIdMap<K> origMap;
            private final Int2IntMap orig2api;
            private final Int2IntMap api2origRenamed;
            private final int elementNum;
            private final boolean isVertices;

            IndexIdMapImpl(IndexIdMap<K> origMap, Int2IntMap orig2api, Int2IntMap api2origRenamed, int elementNum,
                    boolean isVertices) {
                this.origMap = origMap;
                this.orig2api = orig2api;
                this.api2origRenamed = api2origRenamed;
                this.elementNum = elementNum;
                this.isVertices = isVertices;
            }

            @Override
            public K indexToId(int index) {
                K id = indexToIdIfExist(index);
                if (id == null) {
                    if (isVertices) {
                        throw NoSuchVertexException.ofIndex(index);
                    } else {
                        throw NoSuchEdgeException.ofIndex(index);
                    }
                }
                return id;
            }

            @Override
            public K indexToIdIfExist(int index) {
                if (index < 0 || index >= elementNum)
                    return null;
                int indexOrig = api2origRenamed.getOrDefault(index, index);
                return origMap.indexToId(indexOrig);
            }

            @Override
            public int idToIndex(K id) {
                int idxOrig = origMap.idToIndex(id);
                int idxApi = orig2api.getOrDefault(idxOrig, idxOrig);
                if (idxApi < 0) {
                    if (isVertices) {
                        throw NoSuchVertexException.ofVertex(id);
                    } else {
                        throw NoSuchEdgeException.ofEdge(id);
                    }
                }
                return idxApi;

            }

            @Override
            public int idToIndexIfExist(K id) {
                int idxOrig = origMap.idToIndexIfExist(id);
                if (idxOrig < 0)
                    return -1;
                return orig2api.getOrDefault(idxOrig, idxOrig);
            }
        }

        @Override
        public IndexIdMap<V> indexGraphVerticesMap() {
            indexGraph(); // init index graph and maps
            assert indexGraphVerticesMap != null;
            return indexGraphVerticesMap;
        }

        @Override
        public IndexIdMap<E> indexGraphEdgesMap() {
            indexGraph(); // init index graph and maps
            assert indexGraphEdgesMap != null;
            return indexGraphEdgesMap;
        }
    }

}
