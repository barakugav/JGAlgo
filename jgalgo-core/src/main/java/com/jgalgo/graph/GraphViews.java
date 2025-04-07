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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

class GraphViews {

	private GraphViews() {}

	private abstract static class GraphViewBase<V, E> extends AbstractGraph<V, E> {

		private final Graph<V, E> graph;

		GraphViewBase(Graph<V, E> g) {
			this.graph = Objects.requireNonNull(g);
		}

		public Graph<V, E> graph() {
			return graph;
		}

		@Override
		public void addVertices(Collection<? extends V> vertices) {
			graph().addVertices(vertices);
		}

		@Override
		public void removeVertices(Collection<? extends V> vertices) {
			graph().removeVertices(vertices);
		}

		@Override
		public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
			graph().addEdges(edges);
		}

		@Override
		public void removeEdges(Collection<? extends E> edges) {
			graph().removeEdges(edges);
		}

		@Override
		public void clear() {
			graph().clear();
		}

		@Override
		public void clearEdges() {
			graph().clearEdges();
		}

		@Override
		public void ensureVertexCapacity(int vertexCapacity) {
			graph().ensureVertexCapacity(vertexCapacity);
		}

		@Override
		public void ensureEdgeCapacity(int edgeCapacity) {
			graph().ensureEdgeCapacity(edgeCapacity);
		}

		@Override
		public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
				T defVal) {
			return graph().addVerticesWeights(key, type, defVal);
		}

		@Override
		public void removeVerticesWeights(String key) {
			graph().removeVerticesWeights(key);
		}

		@Override
		public Set<String> verticesWeightsKeys() {
			return graph().verticesWeightsKeys();
		}

		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
				T defVal) {
			return graph().addEdgesWeights(key, type, defVal);
		}

		@Override
		public void removeEdgesWeights(String key) {
			graph().removeEdgesWeights(key);
		}

		@Override
		public Set<String> edgesWeightsKeys() {
			return graph().edgesWeightsKeys();
		}

		@Override
		public boolean isDirected() {
			return graph().isDirected();
		}

		@Override
		public boolean isAllowSelfEdges() {
			return graph().isAllowSelfEdges();
		}

		@Override
		public boolean isAllowParallelEdges() {
			return graph().isAllowParallelEdges();
		}

	}

	abstract static class GraphView<V, E> extends GraphViewBase<V, E> {

		GraphView(Graph<V, E> g) {
			super(g);
		}

		@Override
		public Set<V> vertices() {
			return graph().vertices();
		}

		@Override
		public Set<E> edges() {
			return graph().edges();
		}

		@Override
		public void addVertex(V vertex) {
			graph().addVertex(vertex);
		}

		@Override
		public void removeVertex(V vertex) {
			graph().removeVertex(vertex);
		}

		@Override
		public void renameVertex(V vertex, V newId) {
			graph().renameVertex(vertex, newId);
		}

		/* outEdges() is overridden by all view implementations */
		// @Override
		// public EdgeSet<V, E> outEdges(V source) {
		// return graph().outEdges(source);
		// }

		/* inEdges() is overridden by all view implementations */
		// @Override
		// public EdgeSet<V, E> inEdges(V target) {
		// return graph().inEdges(target);
		// }

		/* getEdge() is overridden by all view implementations */
		// @Override
		// public E getEdge(V source, V target) {
		// return graph().getEdge(source, target);
		// }

		/* getEdges() is overridden by all view implementations */
		// @Override
		// public EdgeSet<V, E> getEdges(V source, V target) {
		// return graph().getEdges(source, target);
		// }

		@Override
		public void addEdge(V source, V target, E edge) {
			graph().addEdge(source, target, edge);
		}

		@Override
		public void removeEdge(E edge) {
			graph().removeEdge(edge);
		}

		@Override
		public void removeEdgesOf(V vertex) {
			graph().removeEdgesOf(vertex);
		}

		/* removeInEdgesOf() is overridden by all view implementations */
		// @Override
		// public void removeInEdgesOf(V vertex) {
		// graph().removeInEdgesOf(vertex);
		// }

		/* removeOutEdgesOf() is overridden by all view implementations */
		// @Override
		// public void removeOutEdgesOf(V vertex) {
		// graph().removeOutEdgesOf(vertex);
		// }

		@Override
		public void renameEdge(E edge, E newId) {
			graph().renameEdge(edge, newId);
		}

		@Override
		public void moveEdge(E edge, V newSource, V newTarget) {
			graph().moveEdge(edge, newSource, newTarget);
		}

		@Override
		public V edgeSource(E edge) {
			return graph().edgeSource(edge);
		}

		@Override
		public V edgeTarget(E edge) {
			return graph().edgeTarget(edge);
		}

		@Override
		public V edgeEndpoint(E edge, V endpoint) {
			return graph().edgeEndpoint(edge, endpoint);
		}

		@Override
		public IdBuilder<V> vertexBuilder() {
			return graph().vertexBuilder();
		}

		@Override
		public IdBuilder<E> edgeBuilder() {
			return graph().edgeBuilder();
		}

		@Override
		public <T, WeightsT extends Weights<V, T>> WeightsT verticesWeights(String key) {
			return graph().verticesWeights(key);
		}

		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT edgesWeights(String key) {
			return graph().edgesWeights(key);
		}

		@Override
		public IndexIdMap<V> indexGraphVerticesMap() {
			return graph().indexGraphVerticesMap();
		}

		@Override
		public IndexIdMap<E> indexGraphEdgesMap() {
			return graph().indexGraphEdgesMap();
		}
	}

	abstract static class IntGraphViewBase extends GraphViewBase<Integer, Integer> implements IntGraph {

		IntGraphViewBase(IntGraph g) {
			super(g);
		}

		@Override
		public IntGraph graph() {
			return (IntGraph) super.graph();
		}

		@Override
		public IntSet vertices() {
			return graph().vertices();
		}

		@Override
		public IntSet edges() {
			return graph().edges();
		}

		@Override
		public int addVertexInt() {
			return graph().addVertexInt();
		}

		@Override
		public void removeVertex(int vertex) {
			graph().removeVertex(vertex);
		}

		/* outEdges() is overridden by all view implementations */
		// @Override
		// public IEdgeSet outEdges(int source) {
		// return graph().outEdges(source);
		// }

		/* inEdges() is overridden by all view implementations */
		// @Override
		// public IEdgeSet inEdges(int target) {
		// return graph().inEdges(target);
		// }

		/* getEdge() is overridden by all view implementations */
		// @Override
		// public int getEdge(int source, int target) {
		// return graph().getEdge(source, target);
		// }

		/* getEdges() is overridden by all view implementations */
		// @Override
		// public IEdgeSet getEdges(int source, int target) {
		// return graph().getEdges(source, target);
		// }

		@Override
		public int addEdge(int source, int target) {
			return graph().addEdge(source, target);
		}

		@Override
		public void removeEdge(int edge) {
			graph().removeEdge(edge);
		}

		@Override
		public void removeEdgesOf(int vertex) {
			graph().removeEdgesOf(vertex);
		}

		/* removeInEdgesOf() is overridden by all view implementations */
		// @Override
		// public void removeInEdgesOf(int vertex) {
		// graph().removeInEdgesOf(vertex);
		// }

		/* removeOutEdgesOf() is overridden by all view implementations */
		// @Override
		// public void removeOutEdgesOf(int vertex) {
		// graph().removeOutEdgesOf(vertex);
		// }

		@Override
		public void moveEdge(int edge, int newSource, int newTarget) {
			graph().moveEdge(edge, newSource, newTarget);
		}

		@Override
		public int edgeSource(int edge) {
			return graph().edgeSource(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return graph().edgeTarget(edge);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			return graph().edgeEndpoint(edge, endpoint);
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
			return graph().verticesWeights(key);
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
			return graph().edgesWeights(key);
		}
	}

	abstract static class EdgeIterView<V, E> implements EdgeIters.Base<V, E>, ObjectIterator<E> {
		final EdgeIter<V, E> it;

		EdgeIterView(EdgeIter<V, E> it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public E next() {
			return it.next();
		}

		@Override
		public E peekNext() {
			return it.peekNext();
		}

		/* source() is overridden by all view implementations */
		// @Override
		// public V source() {
		// return it.source();
		// }

		/* target() is overridden by all view implementations */
		// @Override
		// public V target() {
		// return it.target();
		// }

		@Override
		public void remove() {
			it.remove();
		}

		@Override
		public int skip(int n) {
			return JGAlgoUtils.objIterSkip(it, n);
		}
	}

	abstract static class IEdgeIterView implements EdgeIters.IBase {
		final IEdgeIter it;

		IEdgeIterView(IEdgeIter it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			return it.nextInt();
		}

		@Override
		public int peekNextInt() {
			return it.peekNextInt();
		}

		/* sourceInt() is overridden by all view implementations */
		// @Override
		// public int sourceInt() {
		// return it.sourceInt();
		// }

		/* targetInt() is overridden by all view implementations */
		// @Override
		// public int targetInt() {
		// return it.targetInt();
		// }

		@Override
		public void remove() {
			it.remove();
		}

		@Override
		public int skip(int n) {
			return it.skip(n);
		}
	}

}
