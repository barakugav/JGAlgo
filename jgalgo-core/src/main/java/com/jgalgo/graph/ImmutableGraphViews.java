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
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graphs.ImmutableGraph;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

class ImmutableGraphViews {

	private ImmutableGraphViews() {}

	private static class ImmutableGraphView<V, E> extends AbstractGraph<V, E> implements ImmutableGraph {

		private final Graph<V, E> graph;

		ImmutableGraphView(Graph<V, E> g) {
			this.graph = Objects.requireNonNull(g);
		}

		@Override
		public Set<V> vertices() {
			return graph.vertices();
		}

		@Override
		public Set<E> edges() {
			return graph.edges();
		}

		@Override
		public void addVertex(V vertex) {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices");
		}

		@Override
		public void addVertices(Collection<? extends V> vertices) {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices");
		}

		@Override
		public void removeVertex(V vertex) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices");
		}

		@Override
		public void removeVertices(Collection<? extends V> vertices) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices");
		}

		@Override
		public void renameVertex(V vertex, V newId) {
			throw new UnsupportedOperationException("graph is immutable, cannot rename vertices");
		}

		@Override
		public EdgeSet<V, E> outEdges(V source) {
			return new ImmutableEdgeSet<>(graph.outEdges(source));
		}

		@Override
		public EdgeSet<V, E> inEdges(V target) {
			return new ImmutableEdgeSet<>(graph.inEdges(target));
		}

		@Override
		public E getEdge(V source, V target) {
			return graph.getEdge(source, target);
		}

		@Override
		public EdgeSet<V, E> getEdges(V source, V target) {
			return new ImmutableEdgeSet<>(graph.getEdges(source, target));
		}

		@Override
		public void addEdge(V source, V target, E edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public void removeEdge(E edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void removeEdges(Collection<? extends E> edges) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void removeEdgesOf(V vertex) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void removeOutEdgesOf(V source) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void removeInEdgesOf(V target) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void renameEdge(E edge, E newId) {
			throw new UnsupportedOperationException("graph is immutable, cannot rename edges");
		}

		@Override
		public void moveEdge(E edge, V newSource, V newTarget) {
			throw new UnsupportedOperationException("graph is immutable, cannot move edges");
		}

		@Override
		public V edgeSource(E edge) {
			return graph.edgeSource(edge);
		}

		@Override
		public V edgeTarget(E edge) {
			return graph.edgeTarget(edge);
		}

		@Override
		public V edgeEndpoint(E edge, V endpoint) {
			return graph.edgeEndpoint(edge, endpoint);
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices and edges");
		}

		@Override
		public void clearEdges() {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public IdBuilder<V> vertexBuilder() {
			return graph.vertexBuilder();
		}

		@Override
		public IdBuilder<E> edgeBuilder() {
			return graph.edgeBuilder();
		}

		@Override
		public void ensureVertexCapacity(int vertexCapacity) {}

		@Override
		public void ensureEdgeCapacity(int edgeCapacity) {}

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends Weights<V, T>> WeightsT verticesWeights(String key) {
			return (WeightsT) WeightsImpl.ObjImmutableView.newInstance(graph.verticesWeights(key));
		}

		@Override
		public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
				T defVal) {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices weights");
		}

		@Override
		public void removeVerticesWeights(String key) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices weights");
		}

		@Override
		public Set<String> verticesWeightsKeys() {
			return graph.verticesWeightsKeys();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT edgesWeights(String key) {
			return (WeightsT) WeightsImpl.ObjImmutableView.newInstance(graph.edgesWeights(key));
		}

		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
				T defVal) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges weights");
		}

		@Override
		public void removeEdgesWeights(String key) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges weights");
		}

		@Override
		public Set<String> edgesWeightsKeys() {
			return graph.edgesWeightsKeys();
		}

		@Override
		public boolean isDirected() {
			return graph.isDirected();
		}

		@Override
		public boolean isAllowSelfEdges() {
			return graph.isAllowSelfEdges();
		}

		@Override
		public boolean isAllowParallelEdges() {
			return graph.isAllowParallelEdges();
		}

		@Override
		public IndexGraph indexGraph() {
			return graph.indexGraph().immutableView();
		}

		@Override
		public IndexIdMap<V> indexGraphVerticesMap() {
			return graph.indexGraphVerticesMap();
		}

		@Override
		public IndexIdMap<E> indexGraphEdgesMap() {
			return graph.indexGraphEdgesMap();
		}

	}

	private abstract static class ImmutableIntGraphViewBase extends AbstractGraph<Integer, Integer>
			implements IntGraph, ImmutableGraph {

		private final IntGraph graph;

		ImmutableIntGraphViewBase(IntGraph g) {
			this.graph = Objects.requireNonNull(g);
		}

		@Override
		public IntSet vertices() {
			return graph.vertices();
		}

		@Override
		public IntSet edges() {
			return graph.edges();
		}

		@Override
		public void addVertices(Collection<? extends Integer> vertices) {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices");
		}

		@Override
		public void removeVertex(int vertex) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices");
		}

		@Override
		public void removeVertices(Collection<? extends Integer> vertices) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices");
		}

		@Override
		public IEdgeSet outEdges(int source) {
			return new ImmutableIEdgeSet(graph.outEdges(source));
		}

		@Override
		public IEdgeSet inEdges(int target) {
			return new ImmutableIEdgeSet(graph.inEdges(target));
		}

		@Override
		public int getEdge(int source, int target) {
			return graph.getEdge(source, target);
		}

		@Override
		public IEdgeSet getEdges(int source, int target) {
			return new ImmutableIEdgeSet(graph.getEdges(source, target));
		}

		@Override
		public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public void removeEdge(int edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void removeEdges(Collection<? extends Integer> edges) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void removeEdgesOf(int vertex) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void removeOutEdgesOf(int source) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void removeInEdgesOf(int target) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void moveEdge(int edge, int newSource, int newTarget) {
			throw new UnsupportedOperationException("graph is immutable, cannot move edges");
		}

		@Override
		public int edgeSource(int edge) {
			return graph.edgeSource(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return graph.edgeTarget(edge);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			return graph.edgeEndpoint(edge, endpoint);
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices and edges");
		}

		@Override
		public void clearEdges() {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
		}

		@Override
		public void ensureVertexCapacity(int vertexCapacity) {}

		@Override
		public void ensureEdgeCapacity(int edgeCapacity) {}

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
			return (WeightsT) WeightsImpl.IntImmutableView.newInstance(graph.verticesWeights(key));
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
				T defVal) {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices weights");
		}

		@Override
		public void removeVerticesWeights(String key) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices weights");
		}

		@Override
		public Set<String> verticesWeightsKeys() {
			return graph.verticesWeightsKeys();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
			return (WeightsT) WeightsImpl.IntImmutableView.newInstance(graph.edgesWeights(key));
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
				T defVal) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges weights");
		}

		@Override
		public void removeEdgesWeights(String key) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges weights");
		}

		@Override
		public Set<String> edgesWeightsKeys() {
			return graph.edgesWeightsKeys();
		}

		@Override
		public boolean isDirected() {
			return graph.isDirected();
		}

		@Override
		public boolean isAllowSelfEdges() {
			return graph.isAllowSelfEdges();
		}

		@Override
		public boolean isAllowParallelEdges() {
			return graph.isAllowParallelEdges();
		}

		IntGraph graph() {
			return graph;
		}
	}

	private static class ImmutableIntGraphView extends ImmutableIntGraphViewBase {

		ImmutableIntGraphView(IntGraph g) {
			super(g);
		}

		@Override
		public void addVertex(int vertex) {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices");
		}

		@Override
		public void renameVertex(int vertex, int newId) {
			throw new UnsupportedOperationException("graph is immutable, cannot rename vertices");
		}

		@Override
		public void addEdge(int source, int target, int edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public void renameEdge(int edge, int newId) {
			throw new UnsupportedOperationException("graph is immutable, cannot rename edges");
		}

		@Override
		public IdBuilderInt vertexBuilder() {
			return graph().vertexBuilder();
		}

		@Override
		public IdBuilderInt edgeBuilder() {
			return graph().edgeBuilder();
		}

		@Override
		public IndexGraph indexGraph() {
			return graph().indexGraph().immutableView();
		}

		@Override
		public IndexIntIdMap indexGraphVerticesMap() {
			return graph().indexGraphVerticesMap();
		}

		@Override
		public IndexIntIdMap indexGraphEdgesMap() {
			return graph().indexGraphEdgesMap();
		}
	}

	private static class ImmutableIndexGraphView extends ImmutableIntGraphViewBase implements IndexGraph {

		ImmutableIndexGraphView(IndexGraph g) {
			super(g);
		}

		@Override
		IndexGraph graph() {
			return (IndexGraph) super.graph();
		}

		@Override
		public int addVertexInt() {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices");
		}

		@Override
		public int addEdge(int source, int target) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public IntSet addEdgesReassignIds(IEdgeSet edges) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public void addVertexRemoveListener(IndexRemoveListener listener) {
			throw new UnsupportedOperationException("graph is immutable, cannot add a listener");
		}

		@Override
		public void removeVertexRemoveListener(IndexRemoveListener listener) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove a listener");
		}

		@Override
		public void addEdgeRemoveListener(IndexRemoveListener listener) {
			throw new UnsupportedOperationException("graph is immutable, cannot add a listener");
		}

		@Override
		public void removeEdgeRemoveListener(IndexRemoveListener listener) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove a listener");
		}

		@Deprecated
		@Override
		public IndexIntIdMap indexGraphVerticesMap() {
			return graph().indexGraphVerticesMap();
		}

		@Deprecated
		@Override
		public IndexIntIdMap indexGraphEdgesMap() {
			return graph().indexGraphEdgesMap();
		}
	}

	private static class ImmutableEdgeSet<V, E> extends AbstractSet<E> implements EdgeSet<V, E> {

		private final EdgeSet<V, E> set;

		ImmutableEdgeSet(EdgeSet<V, E> set) {
			this.set = Objects.requireNonNull(set);
		}

		@Override
		public boolean contains(Object edge) {
			return set.contains(edge);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new ImmutableEdgeIter<>(set.iterator());
		}
	}

	private static class ImmutableIEdgeSet extends AbstractIntSet implements IEdgeSet {

		private final IEdgeSet set;

		ImmutableIEdgeSet(IEdgeSet set) {
			this.set = Objects.requireNonNull(set);
		}

		@Override
		public boolean contains(int edge) {
			return set.contains(edge);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public IEdgeIter iterator() {
			return new ImmutableIEdgeIter(set.iterator());
		}
	}

	private static class ImmutableEdgeIter<V, E> implements EdgeIters.Base<V, E>, ObjectIterator<E> {
		private final EdgeIter<V, E> it;

		ImmutableEdgeIter(EdgeIter<V, E> it) {
			this.it = Objects.requireNonNull(it);
		}

		@Override
		public V source() {
			return it.source();
		}

		@Override
		public V target() {
			return it.target();
		}

		@Override
		public E next() {
			return it.next();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public E peekNext() {
			return it.peekNext();
		}

		@Override
		public int skip(int n) {
			return JGAlgoUtils.objIterSkip(it, n);
		}
	}

	private static class ImmutableIEdgeIter implements EdgeIters.IBase {
		private final IEdgeIter it;

		ImmutableIEdgeIter(IEdgeIter it) {
			this.it = Objects.requireNonNull(it);
		}

		@Override
		public int sourceInt() {
			return it.sourceInt();
		}

		@Override
		public int targetInt() {
			return it.targetInt();
		}

		@Override
		public int nextInt() {
			return it.nextInt();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int peekNextInt() {
			return it.peekNextInt();
		}

		@Override
		public int skip(int n) {
			return it.skip(n);
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> Graph<V, E> of(Graph<V, E> g) {
		if (g instanceof ImmutableGraph)
			return g;
		if (g instanceof IndexGraph)
			return (Graph<V, E>) new ImmutableIndexGraphView((IndexGraph) g);
		if (g instanceof IntGraph)
			return (Graph<V, E>) new ImmutableIntGraphView((IntGraph) g);
		return new ImmutableGraphView<>(g);
	}

}
