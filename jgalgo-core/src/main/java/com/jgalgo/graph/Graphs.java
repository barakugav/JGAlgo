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
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterables;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

/**
 * Static methods class for graphs.
 *
 * @author Barak Ugav
 */
public class Graphs {
	private Graphs() {}

	/**
	 * Tag interface for graphs that can not be muted/changed/altered.
	 *
	 * @author Barak Ugav
	 */
	static interface ImmutableGraph {
	}

	private static class ImmutableGraphView<V, E> extends GraphBase<V, E> implements ImmutableGraph {

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
		public void removeVertex(V vertex) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove vertices");
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
		public EdgeSet<V, E> getEdges(V source, V target) {
			return new ImmutableEdgeSet<>(graph.getEdges(source, target));
		}

		@Override
		public void addEdge(V source, V target, E edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public void removeEdge(E edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot remove edges");
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

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key) {
			return (WeightsT) WeightsImpl.ObjImmutableView.newInstance(graph.getVerticesWeights(key));
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
		public Set<String> getVerticesWeightsKeys() {
			return graph.getVerticesWeightsKeys();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
			return (WeightsT) WeightsImpl.ObjImmutableView.newInstance(graph.getEdgesWeights(key));
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
		public Set<String> getEdgesWeightsKeys() {
			return graph.getEdgesWeightsKeys();
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

	private abstract static class ImmutableIntGraphViewBase extends GraphBase<Integer, Integer>
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
		public int addVertex() {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices");
		}

		@Override
		public void addVertex(int vertex) {
			throw new UnsupportedOperationException("graph is immutable, cannot add vertices");
		}

		@Override
		public void removeVertex(int vertex) {
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
		public IEdgeSet getEdges(int source, int target) {
			return new ImmutableIEdgeSet(graph.getEdges(source, target));
		}

		@Override
		public int addEdge(int source, int target) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public void addEdge(int source, int target, int edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot add edges");
		}

		@Override
		public void removeEdge(int edge) {
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

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends IWeights<T>> WeightsT getVerticesIWeights(String key) {
			return (WeightsT) WeightsImpl.IntImmutableView.newInstance(graph.getVerticesIWeights(key));
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
		public Set<String> getVerticesWeightsKeys() {
			return graph.getVerticesWeightsKeys();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key) {
			return (WeightsT) WeightsImpl.IntImmutableView.newInstance(graph.getEdgesIWeights(key));
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
		public Set<String> getEdgesWeightsKeys() {
			return graph.getEdgesWeightsKeys();
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
		@Deprecated
		public void addVertex(int vertex) {
			IndexGraph.super.addVertex(vertex);
		}

		@Override
		@Deprecated
		public void addEdge(int source, int target, int edge) {
			IndexGraph.super.addEdge(source, target, edge);
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

	private static class ImmutableEdgeIter<V, E> implements EdgeIter<V, E> {
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
	}

	private static class ImmutableIEdgeIter implements IEdgeIter {
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
	}

	@SuppressWarnings("unchecked")
	static <V, E> Graph<V, E> immutableView(Graph<V, E> g) {
		if (g instanceof ImmutableGraph)
			return g;
		if (g instanceof IndexGraph)
			return (Graph<V, E>) new ImmutableIndexGraphView((IndexGraph) g);
		if (g instanceof IntGraph)
			return (Graph<V, E>) new ImmutableIntGraphView((IntGraph) g);
		return new ImmutableGraphView<>(g);
	}

	private abstract static class GraphViewBase<V, E> extends GraphBase<V, E> {

		private final Graph<V, E> graph;

		GraphViewBase(Graph<V, E> g) {
			this.graph = Objects.requireNonNull(g);
		}

		public Graph<V, E> graph() {
			return graph;
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
		public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
				T defVal) {
			return graph().addVerticesWeights(key, type, defVal);
		}

		@Override
		public void removeVerticesWeights(String key) {
			graph().removeVerticesWeights(key);
		}

		@Override
		public Set<String> getVerticesWeightsKeys() {
			return graph().getVerticesWeightsKeys();
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
		public Set<String> getEdgesWeightsKeys() {
			return graph().getEdgesWeightsKeys();
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

	private abstract static class GraphView<V, E> extends GraphViewBase<V, E> {

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
		public <T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key) {
			return graph().getVerticesWeights(key);
		}

		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
			return graph().getEdgesWeights(key);
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

	private abstract static class IntGraphViewBase extends GraphViewBase<Integer, Integer> implements IntGraph {

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
		public int addVertex() {
			return graph().addVertex();
		}

		@Override
		public void addVertex(int vertex) {
			graph().addVertex(vertex);
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
		public void addEdge(int source, int target, int edge) {
			graph().addEdge(source, target, edge);
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
		public <T, WeightsT extends IWeights<T>> WeightsT getVerticesIWeights(String key) {
			return graph().getVerticesIWeights(key);
		}

		@Override
		public <T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key) {
			return graph().getEdgesIWeights(key);
		}
	}

	private abstract static class EdgeIterView<V, E> implements EdgeIter<V, E> {
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
	}

	private abstract static class IEdgeIterView implements IEdgeIter {
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
	}

	private static interface ReverseGraph<V, E> {
		Graph<V, E> graph();
	};

	private static class ObjReverseGraph<V, E> extends GraphView<V, E> implements ReverseGraph<V, E> {

		ObjReverseGraph(Graph<V, E> g) {
			super(g);
		}

		@Override
		public EdgeSet<V, E> outEdges(V source) {
			return new ReversedEdgeSet<>(graph().inEdges(source));
		}

		@Override
		public EdgeSet<V, E> inEdges(V target) {
			return new ReversedEdgeSet<>(graph().outEdges(target));
		}

		@Override
		public E getEdge(V source, V target) {
			return graph().getEdge(target, source);
		}

		@Override
		public EdgeSet<V, E> getEdges(V source, V target) {
			return new ReversedEdgeSet<>(graph().getEdges(target, source));
		}

		@Override
		public void addEdge(V source, V target, E edge) {
			graph().addEdge(target, source, edge);
		}

		@Override
		public void removeInEdgesOf(V vertex) {
			graph().removeOutEdgesOf(vertex);
		}

		@Override
		public void removeOutEdgesOf(V vertex) {
			graph().removeInEdgesOf(vertex);
		}

		@Override
		public void moveEdge(E edge, V newSource, V newTarget) {
			graph().moveEdge(edge, newTarget, newSource);
		}

		@Override
		public V edgeSource(E edge) {
			return graph().edgeTarget(edge);
		}

		@Override
		public V edgeTarget(E edge) {
			return graph().edgeSource(edge);
		}

		@Override
		public IndexGraph indexGraph() {
			return graph().indexGraph().reverseView();
		}
	}

	private abstract static class ReverseIntGraphBase extends IntGraphViewBase
			implements ReverseGraph<Integer, Integer> {

		ReverseIntGraphBase(IntGraph g) {
			super(g);
		}

		@Override
		public IEdgeSet outEdges(int source) {
			return new ReversedIEdgeSet(graph().inEdges(source));
		}

		@Override
		public IEdgeSet inEdges(int target) {
			return new ReversedIEdgeSet(graph().outEdges(target));
		}

		@Override
		public int getEdge(int source, int target) {
			return graph().getEdge(target, source);
		}

		@Override
		public IEdgeSet getEdges(int source, int target) {
			return new ReversedIEdgeSet(graph().getEdges(target, source));
		}

		@Override
		public int addEdge(int source, int target) {
			return graph().addEdge(target, source);
		}

		@Override
		public void addEdge(int source, int target, int edge) {
			graph().addEdge(target, source, edge);
		}

		@Override
		public void removeInEdgesOf(int vertex) {
			graph().removeOutEdgesOf(vertex);
		}

		@Override
		public void removeOutEdgesOf(int vertex) {
			graph().removeInEdgesOf(vertex);
		}

		@Override
		public void moveEdge(int edge, int newSource, int newTarget) {
			graph().moveEdge(edge, newTarget, newSource);
		}

		@Override
		public int edgeSource(int edge) {
			return graph().edgeTarget(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return graph().edgeSource(edge);
		}
	}

	private static class ReverseIntGraph extends ReverseIntGraphBase {

		ReverseIntGraph(IntGraph g) {
			super(g);
		}

		@Override
		public IndexGraph indexGraph() {
			return graph().indexGraph().reverseView();
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

	private static class ReverseIndexGraph extends ReverseIntGraphBase implements IndexGraph {

		ReverseIndexGraph(IndexGraph g) {
			super(g);
		}

		@Override
		public IndexGraph graph() {
			return (IndexGraph) super.graph();
		}

		@Override
		@Deprecated
		public void addVertex(int vertex) {
			IndexGraph.super.addVertex(vertex);
		}

		@Override
		@Deprecated
		public void addEdge(int source, int target, int edge) {
			IndexGraph.super.addEdge(source, target, edge);
		}

		@Override
		public void addVertexRemoveListener(IndexRemoveListener listener) {
			graph().addVertexRemoveListener(listener);
		}

		@Override
		public void removeVertexRemoveListener(IndexRemoveListener listener) {
			graph().removeVertexRemoveListener(listener);
		}

		@Override
		public void addEdgeRemoveListener(IndexRemoveListener listener) {
			graph().addEdgeRemoveListener(listener);
		}

		@Override
		public void removeEdgeRemoveListener(IndexRemoveListener listener) {
			graph().removeEdgeRemoveListener(listener);
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

	private static class ReversedEdgeSet<V, E> extends AbstractSet<E> implements EdgeSet<V, E> {

		private final EdgeSet<V, E> set;

		ReversedEdgeSet(EdgeSet<V, E> set) {
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
		public boolean remove(Object edge) {
			return set.remove(edge);
		}

		@Override
		public void clear() {
			set.clear();
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new ReversedEdgeIter<>(set.iterator());
		}
	}

	private static class ReversedIEdgeSet extends AbstractIntSet implements IEdgeSet {

		private final IEdgeSet set;

		ReversedIEdgeSet(IEdgeSet set) {
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
		public boolean remove(int edge) {
			return set.remove(edge);
		}

		@Override
		public void clear() {
			set.clear();
		}

		@Override
		public IEdgeIter iterator() {
			return new ReversedIEdgeIter(set.iterator());
		}
	}

	private static class ReversedEdgeIter<V, E> extends EdgeIterView<V, E> {
		ReversedEdgeIter(EdgeIter<V, E> it) {
			super(it);
		}

		@Override
		public V source() {
			return it.target();
		}

		@Override
		public V target() {
			return it.source();
		}
	}

	private static class ReversedIEdgeIter extends IEdgeIterView {
		ReversedIEdgeIter(IEdgeIter it) {
			super(it);
		}

		@Override
		public int sourceInt() {
			return it.targetInt();
		}

		@Override
		public int targetInt() {
			return it.sourceInt();
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> Graph<V, E> reverseView(Graph<V, E> g) {
		if (g instanceof ReverseGraph)
			return ((ReverseGraph<V, E>) g).graph();
		if (g instanceof IndexGraph)
			return (Graph<V, E>) new ReverseIndexGraph((IndexGraph) g);
		if (g instanceof IntGraph)
			return (Graph<V, E>) new ReverseIntGraph((IntGraph) g);
		return new ObjReverseGraph<>(g);
	}

	private static class UndirectedView<V, E> extends GraphView<V, E> {

		UndirectedView(Graph<V, E> g) {
			super(g);
			assert g.isDirected();
		}

		@Override
		public EdgeSet<V, E> outEdges(V source) {
			return new EdgeSetOut(source);
		}

		@Override
		public EdgeSet<V, E> inEdges(V target) {
			return new EdgeSetIn(target);
		}

		@Override
		public E getEdge(V source, V target) {
			E e = graph().getEdge(source, target);
			return e != null ? e : graph().getEdge(target, source);
		}

		@Override
		public EdgeSet<V, E> getEdges(V source, V target) {
			if (source.equals(target))
				return graph().getEdges(source, target);
			return new EdgeSetSourceTarget(source, target);
		}

		@Override
		public void removeInEdgesOf(V vertex) {
			graph().removeEdgesOf(vertex);
		}

		@Override
		public void removeOutEdgesOf(V vertex) {
			graph().removeEdgesOf(vertex);
		}

		@Override
		public IndexGraph indexGraph() {
			return graph().indexGraph().undirectedView();
		}

		@Override
		public boolean isDirected() {
			return false;
		}

		@Override
		public boolean isAllowParallelEdges() {
			/*
			 * We do not enforce that (u,v) and (v,u) both exists in the original graph. Although this function return
			 * true, the original graph may no support parallel edges. See {@link Graph#undirectedView()}.
			 */
			return true;
		}

		private abstract class EdgeSetBase extends AbstractSet<E> implements EdgeSet<V, E> {

			final EdgeSet<V, E> out;
			final EdgeSet<V, E> in;

			EdgeSetBase(EdgeSet<V, E> out, EdgeSet<V, E> in) {
				this.out = out;
				this.in = in;
			}

			@Override
			public boolean contains(Object o) {
				return out.contains(o) || in.contains(o);
			}

			@Override
			public boolean remove(Object o) {
				return out.remove(o) || in.remove(o);
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				boolean changed = false;
				changed |= out.removeAll(c);
				changed |= in.removeAll(c);
				return changed;
			}

			@Override
			public void clear() {
				out.clear();
				in.clear();
			}
		}

		private abstract class EdgeSetOutOrInBase extends EdgeSetBase {

			final V vertex;

			EdgeSetOutOrInBase(V vertex) {
				super(graph().outEdges(vertex), graph().inEdges(vertex));
				this.vertex = vertex;
			}

			@Override
			public int size() {
				return (int) ObjectIterables.size(this);
			}

			@Override
			public boolean isEmpty() {
				return !iterator().hasNext();
			}
		}

		private abstract class EdgeIterOutOrInBase implements EdgeIter<V, E> {

			private EdgeIter<V, E> outIt, inIt;
			final V vertex;
			V endpoint;

			EdgeIterOutOrInBase(V vertex) {
				outIt = graph().outEdges(vertex).iterator();
				inIt = graph().inEdges(vertex).iterator();
				this.vertex = vertex;
				advance();
			}

			private void advance() {
				if (outIt != null) {
					if (outIt.hasNext())
						return;
					outIt = null;
				}
				for (; inIt.hasNext(); inIt.next()) {
					E e = inIt.peekNext();
					/* we skip self edges in the in-edges iterator */
					if (!vertex.equals(graph().edgeSource(e)))
						return;
				}
				inIt = null;
			}

			@Override
			public boolean hasNext() {
				return inIt != null;
			}

			@Override
			public E next() {
				Assertions.Iters.hasNext(this);
				E e;
				if (outIt != null) {
					e = outIt.next();
					endpoint = outIt.target();
				} else {
					e = inIt.next();
					endpoint = inIt.source();
				}
				advance();
				return e;
			}

			@Override
			public E peekNext() {
				Assertions.Iters.hasNext(this);
				return outIt != null ? outIt.peekNext() : inIt.peekNext();
			}
		}

		private class EdgeSetOut extends EdgeSetOutOrInBase {
			EdgeSetOut(V source) {
				super(source);
			}

			@Override
			public EdgeIter<V, E> iterator() {
				return new EdgeIterOut(vertex);
			}
		}

		private class EdgeIterOut extends EdgeIterOutOrInBase {
			EdgeIterOut(V source) {
				super(source);
			}

			@Override
			public V source() {
				return vertex;
			}

			@Override
			public V target() {
				return endpoint;
			}
		}

		private class EdgeSetIn extends EdgeSetOutOrInBase {
			EdgeSetIn(V target) {
				super(target);
			}

			@Override
			public EdgeIter<V, E> iterator() {
				return new EdgeIterIn(vertex);
			}
		}

		private class EdgeIterIn extends EdgeIterOutOrInBase {
			EdgeIterIn(V target) {
				super(target);
			}

			@Override
			public V source() {
				return endpoint;
			}

			@Override
			public V target() {
				return vertex;
			}
		}

		private class EdgeSetSourceTarget extends EdgeSetBase {

			private final V source, target;

			EdgeSetSourceTarget(V source, V target) {
				super(graph().getEdges(source, target), graph().getEdges(target, source));
				this.source = source;
				this.target = target;
			}

			@Override
			public int size() {
				return out.size() + in.size();
			}

			@Override
			public EdgeIter<V, E> iterator() {
				return new EdgeIter<>() {

					private final EdgeIter<V, E> stIt = out.iterator();
					private final EdgeIter<V, E> tsIt = in.iterator();
					private EdgeIter<V, E> it = stIt;
					{
						advance();
					}

					private void advance() {
						if (it.hasNext())
							return;
						if (it == stIt && tsIt.hasNext()) {
							it = tsIt;
						} else {
							it = null;
						}
					}

					@Override
					public boolean hasNext() {
						return it != null;
					}

					@Override
					public E next() {
						Assertions.Iters.hasNext(this);
						E e = it.next();
						advance();
						return e;
					}

					@Override
					public E peekNext() {
						Assertions.Iters.hasNext(this);
						return it.peekNext();
					}

					@Override
					public V source() {
						return source;
					}

					@Override
					public V target() {
						return target;
					}
				};
			}
		}
	}

	private abstract static class UndirectedViewIntBase extends IntGraphViewBase {

		UndirectedViewIntBase(IntGraph g) {
			super(g);
			assert g.isDirected();
		}

		@Override
		public IEdgeSet outEdges(int source) {
			return new EdgeSetOut(source);
		}

		@Override
		public IEdgeSet inEdges(int target) {
			return new EdgeSetIn(target);
		}

		@Override
		public int getEdge(int source, int target) {
			int e = graph().getEdge(source, target);
			return e != -1 ? e : graph().getEdge(target, source);
		}

		@Override
		public IEdgeSet getEdges(int source, int target) {
			if (source == target)
				return graph().getEdges(source, target);
			return new EdgeSetSourceTarget(source, target);
		}

		@Override
		public void removeInEdgesOf(int vertex) {
			graph().removeEdgesOf(vertex);
		}

		@Override
		public void removeOutEdgesOf(int vertex) {
			graph().removeEdgesOf(vertex);
		}

		@Override
		public boolean isDirected() {
			return false;
		}

		@Override
		public boolean isAllowParallelEdges() {
			/*
			 * We do not enforce that (u,v) and (v,u) both exists in the original graph. Although this function return
			 * true, the original graph may no support parallel edges. See {@link Graph#undirectedView()}.
			 */
			return true;
		}

		private abstract class EdgeSetBase extends AbstractIntSet implements IEdgeSet {

			final IEdgeSet out;
			final IEdgeSet in;

			EdgeSetBase(IEdgeSet out, IEdgeSet in) {
				this.out = out;
				this.in = in;
			}

			@Override
			public boolean contains(int o) {
				return out.contains(o) || in.contains(o);
			}

			@Override
			public boolean remove(int o) {
				return out.remove(o) || in.remove(o);
			}

			@Override
			public boolean removeAll(IntCollection c) {
				boolean changed = false;
				changed |= out.removeAll(c);
				changed |= in.removeAll(c);
				return changed;
			}

			@Override
			public void clear() {
				out.clear();
				in.clear();
			}
		}

		private abstract class EdgeSetOutOrInBase extends EdgeSetBase {

			final int vertex;

			EdgeSetOutOrInBase(int vertex) {
				super(graph().outEdges(vertex), graph().inEdges(vertex));
				this.vertex = vertex;
			}

			@Override
			public int size() {
				return (int) ObjectIterables.size(this);
			}

			@Override
			public boolean isEmpty() {
				return !iterator().hasNext();
			}
		}

		private abstract class EdgeIterOutOrInBase implements IEdgeIter {

			private IEdgeIter outIt, inIt;
			final int vertex;
			int endpoint;

			EdgeIterOutOrInBase(int vertex) {
				outIt = graph().outEdges(vertex).iterator();
				inIt = graph().inEdges(vertex).iterator();
				this.vertex = vertex;
				advance();
			}

			private void advance() {
				if (outIt != null) {
					if (outIt.hasNext())
						return;
					outIt = null;
				}
				for (; inIt.hasNext(); inIt.nextInt()) {
					int e = inIt.peekNextInt();
					/* we skip self edges in the in-edges iterator */
					if (vertex != graph().edgeSource(e))
						return;
				}
				inIt = null;
			}

			@Override
			public boolean hasNext() {
				return inIt != null;
			}

			@Override
			public int nextInt() {
				Assertions.Iters.hasNext(this);
				int e;
				if (outIt != null) {
					e = outIt.nextInt();
					endpoint = outIt.targetInt();
				} else {
					e = inIt.nextInt();
					endpoint = inIt.sourceInt();
				}
				advance();
				return e;
			}

			@Override
			public int peekNextInt() {
				Assertions.Iters.hasNext(this);
				return outIt != null ? outIt.peekNextInt() : inIt.peekNextInt();
			}
		}

		private class EdgeSetOut extends EdgeSetOutOrInBase {
			EdgeSetOut(int source) {
				super(source);
			}

			@Override
			public IEdgeIter iterator() {
				return new EdgeIterOut(vertex);
			}
		}

		private class EdgeIterOut extends EdgeIterOutOrInBase {
			EdgeIterOut(int source) {
				super(source);
			}

			@Override
			public int sourceInt() {
				return vertex;
			}

			@Override
			public int targetInt() {
				return endpoint;
			}
		}

		private class EdgeSetIn extends EdgeSetOutOrInBase {
			EdgeSetIn(int target) {
				super(target);
			}

			@Override
			public IEdgeIter iterator() {
				return new EdgeIterIn(vertex);
			}
		}

		private class EdgeIterIn extends EdgeIterOutOrInBase {
			EdgeIterIn(int target) {
				super(target);
			}

			@Override
			public int sourceInt() {
				return endpoint;
			}

			@Override
			public int targetInt() {
				return vertex;
			}
		}

		private class EdgeSetSourceTarget extends EdgeSetBase {

			private final int source, target;

			EdgeSetSourceTarget(int source, int target) {
				super(graph().getEdges(source, target), graph().getEdges(target, source));
				this.source = source;
				this.target = target;
			}

			@Override
			public int size() {
				return out.size() + in.size();
			}

			@Override
			public IEdgeIter iterator() {
				return new IEdgeIter() {

					private final IEdgeIter stIt = out.iterator();
					private final IEdgeIter tsIt = in.iterator();
					private IEdgeIter it = stIt;
					{
						advance();
					}

					private void advance() {
						if (it.hasNext())
							return;
						if (it == stIt && tsIt.hasNext()) {
							it = tsIt;
						} else {
							it = null;
						}
					}

					@Override
					public boolean hasNext() {
						return it != null;
					}

					@Override
					public int nextInt() {
						Assertions.Iters.hasNext(this);
						int e = it.nextInt();
						advance();
						return e;
					}

					@Override
					public int peekNextInt() {
						Assertions.Iters.hasNext(this);
						return it.peekNextInt();
					}

					@Override
					public int sourceInt() {
						return source;
					}

					@Override
					public int targetInt() {
						return target;
					}
				};
			}
		}

	}

	private static class UndirectedViewInt extends UndirectedViewIntBase {

		UndirectedViewInt(IntGraph g) {
			super(g);
		}

		@Override
		public IndexGraph indexGraph() {
			return graph().indexGraph().undirectedView();
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

	private static class UndirectedViewIndex extends UndirectedViewIntBase implements IndexGraph {

		UndirectedViewIndex(IndexGraph g) {
			super(g);
		}

		@Override
		public IndexGraph graph() {
			return (IndexGraph) super.graph();
		}

		@Override
		@Deprecated
		public void addVertex(int vertex) {
			IndexGraph.super.addVertex(vertex);
		}

		@Override
		@Deprecated
		public void addEdge(int source, int target, int edge) {
			IndexGraph.super.addEdge(source, target, edge);
		}

		@Override
		public void addVertexRemoveListener(IndexRemoveListener listener) {
			graph().addVertexRemoveListener(listener);
		}

		@Override
		public void removeVertexRemoveListener(IndexRemoveListener listener) {
			graph().removeVertexRemoveListener(listener);
		}

		@Override
		public void addEdgeRemoveListener(IndexRemoveListener listener) {
			graph().addEdgeRemoveListener(listener);
		}

		@Override
		public void removeEdgeRemoveListener(IndexRemoveListener listener) {
			graph().removeEdgeRemoveListener(listener);
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

	@SuppressWarnings("unchecked")
	static <V, E> Graph<V, E> undirectedView(Graph<V, E> g) {
		if (!g.isDirected()) {
			return g;
		} else if (g instanceof IndexGraph) {
			return (Graph<V, E>) new UndirectedViewIndex((IndexGraph) g);
		} else if (g instanceof IntGraph) {
			return (Graph<V, E>) new UndirectedViewInt((IntGraph) g);
		} else {
			return new UndirectedView<>(g);
		}
	}

	static String getIndexGraphImpl(IndexGraph g) {
		for (;;) {
			IndexGraph g0 = g;
			if (g instanceof ReverseIndexGraph)
				g = ((ReverseIndexGraph) g).graph();
			if (g instanceof ImmutableIndexGraphView)
				g = ((ImmutableIndexGraphView) g).graph();
			if (g instanceof UndirectedViewIndex)
				g = ((UndirectedViewIndex) g).graph();
			if (g instanceof GraphArrayAbstract)
				return g.isAllowSelfEdges() ? "array-selfedges" : "array";
			if (g instanceof GraphLinkedAbstract)
				return g.isAllowSelfEdges() ? "linked-list-selfedges" : "linked-list";
			if (g instanceof GraphLinkedPtrAbstract)
				return g.isAllowSelfEdges() ? "linked-list-ptr-selfedges" : "linked-list-ptr";
			if (g instanceof GraphHashmapAbstract)
				return g.isAllowSelfEdges() ? "hashtable-selfedges" : "hashtable";
			if (g instanceof GraphHashmapMultiAbstract)
				return g.isAllowSelfEdges() ? "hashtable-multi-selfedges" : "hashtable-multi";
			if (g instanceof GraphMatrixAbstract)
				return g.isAllowSelfEdges() ? "matrix-selfedges" : "matrix";
			if (g == g0)
				return null;
		}
	}

	static class EdgeSetSourceTargetSingleton extends AbstractIntSet implements IEdgeSet {

		private final IndexGraph g;
		private final int source, target;
		private int edge;
		private static final int EdgeNone = -1;

		EdgeSetSourceTargetSingleton(IndexGraph g, int source, int target, int edge) {
			this.g = g;
			this.source = source;
			this.target = target;
			this.edge = edge;
		}

		@Override
		public boolean remove(int edge) {
			if (!contains(edge))
				return false;
			g.removeEdge(edge);
			this.edge = EdgeNone;
			return true;
		}

		@Override
		public boolean contains(int edge) {
			return this.edge != EdgeNone && this.edge == edge;
		}

		@Override
		public int size() {
			return edge != EdgeNone ? 1 : 0;
		}

		@Override
		public void clear() {
			if (edge != EdgeNone) {
				g.removeEdge(edge);
				edge = EdgeNone;
			}
		}

		@Override
		public IEdgeIter iterator() {
			return new IEdgeIter() {

				boolean beforeNext = edge != EdgeNone;

				@Override
				public boolean hasNext() {
					return beforeNext;
				}

				@Override
				public int nextInt() {
					Assertions.Iters.hasNext(this);
					beforeNext = false;
					return edge;
				}

				@Override
				public int peekNextInt() {
					Assertions.Iters.hasNext(this);
					return edge;
				}

				@Override
				public int sourceInt() {
					return source;
				}

				@Override
				public int targetInt() {
					return target;
				}

				@Override
				public void remove() {
					if (beforeNext)
						throw new IllegalStateException();
					g.removeEdge(edge);
					edge = EdgeNone;
				}
			};
		}
	}

	/**
	 * Create a new graph that is an induced subgraph of the given graph.
	 *
	 * <p>
	 * An induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' =
	 * \{\{u,v\} \mid u,v \in V', \{u,v\} \in E\}\). The created graph will have the same type (directed/undirected) as
	 * the given graph. The vertices and edges of the created graph will be a subset of the vertices and edges of the
	 * given graph.
	 *
	 * <p>
	 * The weights of both vertices and edges will not be copied to the new sub graph. For more flexible sub graph
	 * creation, see {@link #subGraph(Graph, Collection, Collection, boolean, boolean)}.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned sub graph is also an {@link IntGraph}.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  g        the graph to create a sub graph from
	 * @param  vertices the vertices of the sub graph
	 * @return          a new graph that is an induced subgraph of the given graph
	 */
	public static <V, E> Graph<V, E> subGraph(Graph<V, E> g, Collection<V> vertices) {
		return subGraph(g, Objects.requireNonNull(vertices), null);
	}

	/**
	 * Create a new graph that is a subgraph of the given graph.
	 *
	 * <p>
	 * If {@code edges} is {@code null}, then the created graph will be an induced subgraph of the given graph, namely
	 * an induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' =
	 * \{\{u,v\} \mid u,v \in V', \{u,v\} \in E\}\). The behavior is similar to {@link #subGraph(Graph, Collection)}.
	 * {@code vertices} must not be {@code null} in this case.
	 *
	 * <p>
	 * If {@code vertices} is {@code null}, then {@code edges} must not be {@code null}, and the sub graph will contain
	 * all the vertices which are either a source or a target of an edge in {@code edges}.
	 *
	 * <p>
	 * The created graph will have the same type (directed/undirected) as the given graph. The vertices and edges of the
	 * created graph will be a subset of the vertices and edges of the given graph.
	 *
	 * <p>
	 * The weights of both vertices and edges will not be copied to the new sub graph. For more flexible sub graph
	 * creation, see {@link #subGraph(Graph, Collection, Collection, boolean, boolean)}.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned sub graph is also an {@link IntGraph}.
	 *
	 * @param  <V>                  the vertices type
	 * @param  <E>                  the edges type
	 * @param  g                    the graph to create a sub graph from
	 * @param  vertices             the vertices of the sub graph, if {@code null} then {@code edges} must not be
	 *                                  {@code null} and the vertices of the sub graph will be all the vertices which
	 *                                  are either a source or a target of an edge in {@code edges}
	 * @param  edges                the edges of the sub graph, if {@code null} then {@code vertices} must not be
	 *                                  {@code null} and the sub graph will be an induced subgraph of the given graph
	 * @return                      a new graph that is a subgraph of the given graph
	 * @throws NullPointerException if both {@code vertices} and {@code edges} are {@code null}
	 */
	public static <V, E> Graph<V, E> subGraph(Graph<V, E> g, Collection<V> vertices, Collection<E> edges) {
		return subGraph(g, vertices, edges, false, false);
	}

	/**
	 * Create a new graph that is a subgraph of the given graph, with option to copy weights.
	 *
	 * <p>
	 * If {@code edges} is {@code null}, then the created graph will be an induced subgraph of the given graph, namely
	 * an induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' =
	 * \{\{u,v\} \mid u,v \in V', \{u,v\} \in E\}\). The behavior is similar to {@link #subGraph(Graph, Collection)}.
	 * {@code vertices} must not be {@code null} in this case.
	 *
	 * <p>
	 * If {@code vertices} is {@code null}, then {@code edges} must not be {@code null}, and the sub graph will contain
	 * all the vertices which are either a source or a target of an edge in {@code edges}.
	 *
	 * <p>
	 * The created graph will have the same type (directed/undirected) as the given graph. The vertices and edges of the
	 * created graph will be a subset of the vertices and edges of the given graph.
	 *
	 * <p>
	 * An additional parameter options for copying the weights of the vertices and edges of the given graph to the new
	 * sub graph are provided. If {@code copyVerticesWeights} is {@code true}, then all the vertices weights of the
	 * given graph will be copied to the new sub graph. If {@code copyEdgesWeights} is {@code true}, then all the edges
	 * weights of the given graph will be copied to the new sub graph.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned sub graph is also an {@link IntGraph}.
	 *
	 * @param  <V>                  the vertices type
	 * @param  <E>                  the edges type
	 * @param  g                    the graph to create a sub graph from
	 * @param  vertices             the vertices of the sub graph, if {@code null} then {@code edges} must not be
	 *                                  {@code null} and the vertices of the sub graph will be all the vertices which
	 *                                  are either a source or a target of an edge in {@code edges}
	 * @param  edges                the edges of the sub graph, if {@code null} then {@code vertices} must not be
	 *                                  {@code null} and the sub graph will be an induced subgraph of the given graph
	 * @param  copyVerticesWeights  if {@code true} then all the vertices weights of the given graph will be copied to
	 *                                  the new sub graph
	 * @param  copyEdgesWeights     if {@code true} then all the edges weights of the given graph will be copied to the
	 *                                  new sub graph
	 * @return                      a new graph that is a subgraph of the given graph
	 * @throws NullPointerException if both {@code vertices} and {@code edges} are {@code null}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "cast" })
	public static <V, E> Graph<V, E> subGraph(Graph<V, E> g, Collection<V> vertices, Collection<E> edges,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		if (g instanceof IntGraph) {
			IntCollection vs = vertices == null ? null : IntAdapters.asIntCollection((Collection<Integer>) vertices);
			IntCollection es = edges == null ? null : IntAdapters.asIntCollection((Collection<Integer>) edges);
			return (Graph<V, E>) subGraph((IntGraph) g, vs, es, copyVerticesWeights, copyEdgesWeights);
		}

		if (vertices == null && edges == null)
			throw new NullPointerException("Either vertices or edges can be null, not both.");
		GraphBuilder<V, E> gb = g.isDirected() ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();

		if (vertices == null) {
			vertices = new ObjectOpenHashSet();
			for (E e : edges) {
				vertices.add(g.edgeSource(e));
				vertices.add(g.edgeTarget(e));
			}
		}
		gb.expectedVerticesNum(vertices.size());
		for (V v : vertices)
			gb.addVertex(v);

		if (edges == null) {
			if (g.isDirected()) {
				for (V u : gb.vertices()) {
					for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
						E e = eit.next();
						V v = eit.target();
						if (gb.vertices().contains(v))
							gb.addEdge(u, v, e);
					}
				}
			} else {
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				for (V u : gb.vertices()) {
					for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
						E e = eit.next();
						V v = eit.target();
						if (viMap.idToIndex(u) <= viMap.idToIndex(v) && gb.vertices().contains(v))
							gb.addEdge(u, v, e);
					}
				}
			}
		} else {
			for (E e : edges)
				gb.addEdge(g.edgeSource(e), g.edgeTarget(e), e);
		}

		if (copyVerticesWeights) {
			for (String key : g.getVerticesWeightsKeys()) {
				Weights wSrc = g.getVerticesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				Weights wDst = gb.addVerticesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.vertices());
			}
		}
		if (copyEdgesWeights) {
			for (String key : g.getEdgesWeightsKeys()) {
				Weights wSrc = g.getEdgesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				Weights wDst = gb.addEdgesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.edges());
			}
		}

		return gb.build();
	}

	/**
	 * Create a new graph that is a subgraph of the given int graph, with option to copy weights.
	 *
	 * <p>
	 * If {@code edges} is {@code null}, then the created graph will be an induced subgraph of the given graph, namely
	 * an induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' =
	 * \{\{u,v\} \mid u,v \in V', \{u,v\} \in E\}\).
	 *
	 * <p>
	 * If {@code vertices} is {@code null}, then {@code edges} must not be {@code null}, and the sub graph will contain
	 * all the vertices which are either a source or a target of an edge in {@code edges}.
	 *
	 * <p>
	 * The created graph will have the same type (directed/undirected) as the given graph. The vertices and edges of the
	 * created graph will be a subset of the vertices and edges of the given graph.
	 *
	 * <p>
	 * An additional parameter options for copying the weights of the vertices and edges of the given graph to the new
	 * sub graph are provided. If {@code copyVerticesWeights} is {@code true}, then all the vertices weights of the
	 * given graph will be copied to the new sub graph. If {@code copyEdgesWeights} is {@code true}, then all the edges
	 * weights of the given graph will be copied to the new sub graph.
	 *
	 * @param  g                    the graph to create a sub graph from
	 * @param  vertices             the vertices of the sub graph, if {@code null} then {@code edges} must not be
	 *                                  {@code null} and the vertices of the sub graph will be all the vertices which
	 *                                  are either a source or a target of an edge in {@code edges}
	 * @param  edges                the edges of the sub graph, if {@code null} then {@code vertices} must not be
	 *                                  {@code null} and the sub graph will be an induced subgraph of the given graph
	 * @param  copyVerticesWeights  if {@code true} then all the vertices weights of the given graph will be copied to
	 *                                  the new sub graph
	 * @param  copyEdgesWeights     if {@code true} then all the edges weights of the given graph will be copied to the
	 *                                  new sub graph
	 * @return                      a new graph that is a subgraph of the given graph
	 * @throws NullPointerException if both {@code vertices} and {@code edges} are {@code null}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "cast" })
	public static IntGraph subGraph(IntGraph g, IntCollection vertices, IntCollection edges,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		if (vertices == null && edges == null)
			throw new NullPointerException("Either vertices or edges can be null, not both.");
		IntGraphBuilder gb = g.isDirected() ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();

		if (vertices == null) {
			vertices = new IntOpenHashSet();
			for (int e : edges) {
				vertices.add(g.edgeSource(e));
				vertices.add(g.edgeTarget(e));
			}
		}
		gb.expectedVerticesNum(vertices.size());
		for (int v : vertices)
			gb.addVertex(v);

		if (edges == null) {
			if (g.isDirected()) {
				for (int u : gb.vertices()) {
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.targetInt();
						if (gb.vertices().contains(v))
							gb.addEdge(u, v, e);
					}
				}
			} else {
				for (int u : gb.vertices()) {
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.targetInt();
						if (u <= v && gb.vertices().contains(v))
							gb.addEdge(u, v, e);
					}
				}
			}
		} else {
			for (int e : edges)
				gb.addEdge(g.edgeSource(e), g.edgeTarget(e), e);
		}

		if (copyVerticesWeights) {
			for (String key : g.getVerticesWeightsKeys()) {
				IWeights wSrc = g.getVerticesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				IWeights wDst = (IWeights) gb.addVerticesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.vertices());
			}
		}
		if (copyEdgesWeights) {
			for (String key : g.getEdgesWeightsKeys()) {
				IWeights wSrc = g.getEdgesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				IWeights wDst = (IWeights) gb.addEdgesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.edges());
			}
		}

		return gb.build();
	}

	@SuppressWarnings("unchecked")
	private static <K> void copyWeights(Weights<K, ?> src, Weights<K, ?> dst, Class<?> type, Collection<K> elements) {
		if (type == byte.class) {
			WeightsByte<K> src0 = (WeightsByte<K>) src;
			WeightsByte<K> dst0 = (WeightsByte<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == short.class) {
			WeightsShort<K> src0 = (WeightsShort<K>) src;
			WeightsShort<K> dst0 = (WeightsShort<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == int.class) {
			WeightsInt<K> src0 = (WeightsInt<K>) src;
			WeightsInt<K> dst0 = (WeightsInt<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == long.class) {
			WeightsLong<K> src0 = (WeightsLong<K>) src;
			WeightsLong<K> dst0 = (WeightsLong<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == float.class) {
			WeightsFloat<K> src0 = (WeightsFloat<K>) src;
			WeightsFloat<K> dst0 = (WeightsFloat<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == double.class) {
			WeightsDouble<K> src0 = (WeightsDouble<K>) src;
			WeightsDouble<K> dst0 = (WeightsDouble<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == boolean.class) {
			WeightsBool<K> src0 = (WeightsBool<K>) src;
			WeightsBool<K> dst0 = (WeightsBool<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == char.class) {
			WeightsChar<K> src0 = (WeightsChar<K>) src;
			WeightsChar<K> dst0 = (WeightsChar<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else {
			assert type == Object.class;
			WeightsObj<K, Object> src0 = (WeightsObj<K, Object>) src;
			WeightsObj<K, Object> dst0 = (WeightsObj<K, Object>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void copyWeights(IWeights<?> src, IWeights<?> dst, Class<?> type, IntCollection elements) {
		if (type == byte.class) {
			IWeightsByte src0 = (IWeightsByte) src;
			IWeightsByte dst0 = (IWeightsByte) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == short.class) {
			IWeightsShort src0 = (IWeightsShort) src;
			IWeightsShort dst0 = (IWeightsShort) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == int.class) {
			IWeightsInt src0 = (IWeightsInt) src;
			IWeightsInt dst0 = (IWeightsInt) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == long.class) {
			IWeightsLong src0 = (IWeightsLong) src;
			IWeightsLong dst0 = (IWeightsLong) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == float.class) {
			IWeightsFloat src0 = (IWeightsFloat) src;
			IWeightsFloat dst0 = (IWeightsFloat) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == double.class) {
			IWeightsDouble src0 = (IWeightsDouble) src;
			IWeightsDouble dst0 = (IWeightsDouble) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == boolean.class) {
			IWeightsBool src0 = (IWeightsBool) src;
			IWeightsBool dst0 = (IWeightsBool) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == char.class) {
			IWeightsChar src0 = (IWeightsChar) src;
			IWeightsChar dst0 = (IWeightsChar) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else {
			assert type == Object.class;
			IWeightsObj src0 = (IWeightsObj) src;
			IWeightsObj dst0 = (IWeightsObj) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		}
	}

	private static Class<?> getWeightsType(Weights<?, ?> w) {
		if (w instanceof WeightsByte)
			return byte.class;
		if (w instanceof WeightsShort)
			return short.class;
		if (w instanceof WeightsInt)
			return int.class;
		if (w instanceof WeightsLong)
			return long.class;
		if (w instanceof WeightsFloat)
			return float.class;
		if (w instanceof WeightsDouble)
			return double.class;
		if (w instanceof WeightsBool)
			return boolean.class;
		if (w instanceof WeightsChar)
			return char.class;
		assert w instanceof WeightsObj;
		return Object.class;
	}

	/**
	 * Get a random vertex from the given graph.
	 *
	 * @param  <V>  the vertices type
	 * @param  g    the graph
	 * @param  rand the random number generator
	 * @return      a random vertex from the given graph
	 */
	public static <V> V randVertex(Graph<V, ?> g, Random rand) {
		return g.indexGraphVerticesMap().indexToId(rand.nextInt(g.vertices().size()));
	}

	/**
	 * Get a random vertex from the given int graph.
	 *
	 * @param  g    the graph
	 * @param  rand the random number generator
	 * @return      a random vertex from the given graph
	 */
	public static int randVertex(IntGraph g, Random rand) {
		return g.indexGraphVerticesMap().indexToIdInt(rand.nextInt(g.vertices().size()));
	}

	/**
	 * Get a random edge from the given graph.
	 *
	 * @param  <E>  the edges type
	 * @param  g    the graph
	 * @param  rand the random number generator
	 * @return      a random edge from the given graph
	 */
	public static <E> E randEdge(Graph<?, E> g, Random rand) {
		return g.indexGraphEdgesMap().indexToId(rand.nextInt(g.edges().size()));
	}

	/**
	 * Get a random edge from the given int graph.
	 *
	 * @param  g    the graph
	 * @param  rand the random number generator
	 * @return      a random edge from the given graph
	 */
	public static int randEdge(IntGraph g, Random rand) {
		return g.indexGraphEdgesMap().indexToIdInt(rand.nextInt(g.edges().size()));
	}

	@SuppressWarnings("unchecked")
	static boolean isEquals(Graph<?, ?> g1, Graph<?, ?> g2) {
		if (g1 == g2)
			return true;
		if (g1 instanceof IntGraph && g2 instanceof IntGraph)
			return isEquals((IntGraph) g1, (IntGraph) g2);

		if (g1.isDirected() != g2.isDirected())
			return false;
		if (!g1.vertices().equals(g2.vertices()))
			return false;
		if (!g1.edges().equals(g2.edges()))
			return false;
		Graph<Object, Object> g10 = (Graph<Object, Object>) g1, g20 = (Graph<Object, Object>) g2;
		return isEquals0(g10, g20);
	}

	private static <V, E> boolean isEquals0(Graph<V, E> g1, Graph<V, E> g2) {
		if (g1.isDirected()) {
			for (E e : g1.edges())
				if (!g1.edgeSource(e).equals(g2.edgeSource(e)) || !g1.edgeTarget(e).equals(g2.edgeTarget(e)))
					return false;
		} else {
			for (E e : g1.edges()) {
				V s1 = g1.edgeSource(e), t1 = g1.edgeTarget(e);
				V s2 = g2.edgeSource(e), t2 = g2.edgeTarget(e);
				if (!(s1.equals(s2) && t1.equals(t2)) && !(s1.equals(t2) && t1.equals(s2)))
					return false;
			}
		}

		if (!g1.getVerticesWeightsKeys().equals(g2.getVerticesWeightsKeys()))
			return false;
		for (String key : g1.getVerticesWeightsKeys()) {
			Weights<V, ?> w1 = g1.getVerticesWeights(key), w2 = g2.getVerticesWeights(key);
			if (!WeightsImpl.isEqual(g1.vertices(), w1, w2))
				return false;
		}
		if (!g1.getEdgesWeightsKeys().equals(g2.getEdgesWeightsKeys()))
			return false;
		for (String key : g1.getEdgesWeightsKeys()) {
			Weights<E, ?> w1 = g1.getEdgesWeights(key), w2 = g2.getEdgesWeights(key);
			if (!WeightsImpl.isEqual(g1.edges(), w1, w2))
				return false;
		}

		return true;
	}

	private static boolean isEquals(IntGraph g1, IntGraph g2) {
		if (g1.isDirected() != g2.isDirected())
			return false;
		if (!g1.vertices().equals(g2.vertices()))
			return false;
		if (!g1.edges().equals(g2.edges()))
			return false;
		if (g1.isDirected()) {
			for (int e : g1.edges())
				if (g1.edgeSource(e) != g2.edgeSource(e) || g1.edgeTarget(e) != g2.edgeTarget(e))
					return false;
		} else {
			for (int e : g1.edges()) {
				int s1 = g1.edgeSource(e), t1 = g1.edgeTarget(e);
				int s2 = g2.edgeSource(e), t2 = g2.edgeTarget(e);
				if (!(s1 == s2 && t1 == t2) && !(s1 == t2 && t1 == s2))
					return false;
			}
		}

		if (!g1.getVerticesWeightsKeys().equals(g2.getVerticesWeightsKeys()))
			return false;
		for (String key : g1.getVerticesWeightsKeys()) {
			IWeights<?> w1 = g1.getVerticesIWeights(key), w2 = g2.getVerticesIWeights(key);
			if (!WeightsImpl.isEqual(g1.vertices(), w1, w2))
				return false;
		}
		if (!g1.getEdgesWeightsKeys().equals(g2.getEdgesWeightsKeys()))
			return false;
		for (String key : g1.getEdgesWeightsKeys()) {
			IWeights<?> w1 = g1.getEdgesIWeights(key), w2 = g2.getEdgesIWeights(key);
			if (!WeightsImpl.isEqual(g1.edges(), w1, w2))
				return false;
		}

		return true;
	}

	static <V, E> int hashCode(Graph<V, E> g) {
		if (g instanceof IntGraph)
			return hashCode((IntGraph) g);

		int h = Boolean.hashCode(g.isDirected());
		h += g.vertices().hashCode();
		h += g.edges().hashCode();
		if (g.isDirected()) {
			for (E e : g.edges())
				h += g.edgeSource(e).hashCode() + 31 * g.edgeTarget(e).hashCode();
		} else {
			for (E e : g.edges())
				h += g.edgeSource(e).hashCode() + g.edgeTarget(e).hashCode();
		}
		for (String key : g.getVerticesWeightsKeys())
			h += WeightsImpl.hashCode(g.vertices(), g.getVerticesWeights(key));
		for (String key : g.getEdgesWeightsKeys())
			h += WeightsImpl.hashCode(g.edges(), g.getEdgesWeights(key));
		return h;
	}

	private static int hashCode(IntGraph g) {
		int h = Boolean.hashCode(g.isDirected());
		h += g.vertices().hashCode();
		h += g.edges().hashCode();
		if (g.isDirected()) {
			for (int e : g.edges())
				h += g.edgeSource(e) + 31 * g.edgeTarget(e);
		} else {
			for (int e : g.edges())
				h += g.edgeSource(e) + g.edgeTarget(e);
		}
		for (String key : g.getVerticesWeightsKeys())
			h += WeightsImpl.hashCode(g.vertices(), g.getVerticesIWeights(key));
		for (String key : g.getEdgesWeightsKeys())
			h += WeightsImpl.hashCode(g.edges(), g.getEdgesIWeights(key));
		return h;
	}

	static <V, E> String toString(Graph<V, E> g) {
		if (g instanceof IntGraph)
			return toString((IntGraph) g);

		StringBuilder s = new StringBuilder();
		s.append('{');

		Set<String> verticesWeightsKeys = g.getVerticesWeightsKeys();
		Collection<Weights<V, ?>> verticesWeights = new ObjectArrayList<>(verticesWeightsKeys.size());
		for (String key : verticesWeightsKeys)
			verticesWeights.add(g.getVerticesWeights(key));

		Set<String> edgesWeightsKeys = g.getEdgesWeightsKeys();
		Collection<Weights<E, ?>> edgesWeights = new ObjectArrayList<>(edgesWeightsKeys.size());
		for (String key : edgesWeightsKeys)
			edgesWeights.add(g.getEdgesWeights(key));

		BiConsumer<Collection<Weights<V, ?>>, V> appendVertexWeights = (weights, vertex) -> {
			s.append('[');
			boolean firstData = true;
			for (Weights<V, ?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.getAsObj(vertex));
			}
			s.append(']');
		};
		BiConsumer<Collection<Weights<E, ?>>, E> appendEdgeWeights = (weights, edge) -> {
			s.append('[');
			boolean firstData = true;
			for (Weights<E, ?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.getAsObj(edge));
			}
			s.append(']');
		};

		boolean firstVertex = true;
		for (V u : g.vertices()) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}
			s.append('v').append(u);
			if (!verticesWeights.isEmpty())
				appendVertexWeights.accept(verticesWeights, u);

			s.append(": [");
			boolean firstEdge = true;
			for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
				E e = eit.next();
				V v = eit.target();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append(e).append('(').append(u).append(", ").append(v);
				if (!edgesWeights.isEmpty()) {
					s.append(", ");
					appendEdgeWeights.accept(edgesWeights, e);
				}
				s.append(')');
			}
			s.append(']');
		}
		s.append('}');
		return s.toString();
	}

	private static String toString(IntGraph g) {
		StringBuilder s = new StringBuilder();
		s.append('{');

		Set<String> verticesWeightsKeys = g.getVerticesWeightsKeys();
		Collection<IWeights<?>> verticesWeights = new ObjectArrayList<>(verticesWeightsKeys.size());
		for (String key : verticesWeightsKeys)
			verticesWeights.add(g.getVerticesIWeights(key));

		Set<String> edgesWeightsKeys = g.getEdgesWeightsKeys();
		Collection<IWeights<?>> edgesWeights = new ObjectArrayList<>(edgesWeightsKeys.size());
		for (String key : edgesWeightsKeys)
			edgesWeights.add(g.getEdgesIWeights(key));

		ObjIntConsumer<Collection<IWeights<?>>> appendWeights = (weights, elm) -> {
			s.append('[');
			boolean firstData = true;
			for (IWeights<?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.getAsObj(elm));
			}
			s.append(']');
		};

		boolean firstVertex = true;
		for (int u : g.vertices()) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}
			s.append('v').append(u);
			if (!verticesWeights.isEmpty())
				appendWeights.accept(verticesWeights, u);

			s.append(": [");
			boolean firstEdge = true;
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append(e).append('(').append(u).append(", ").append(v);
				if (!edgesWeights.isEmpty()) {
					s.append(", ");
					appendWeights.accept(edgesWeights, e);
				}
				s.append(')');
			}
			s.append(']');
		}
		s.append('}');
		return s.toString();
	}

	/**
	 * Get a view of all the self edges in a graph.
	 *
	 * <p>
	 * The returned set is a view, namely it will be updated when the graph is updated.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     a view of all the self edges in the graph
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> Set<E> selfEdges(Graph<V, E> g) {
		if (g instanceof IntGraph)
			return (Set<E>) selfEdges((IntGraph) g);
		if (!g.isAllowSelfEdges())
			return ObjectSets.emptySet();
		IntSet indexSelfEdges = selfEdges(g.indexGraph());
		return IndexIdMaps.indexToIdSet(indexSelfEdges, g.indexGraphEdgesMap());
	}

	/**
	 * Get a view of all the self edges in an int graph.
	 *
	 * <p>
	 * The returned set is a view, namely it will be updated when the graph is updated.
	 *
	 * @param  g an int graph
	 * @return   a view of all the self edges in the graph
	 */
	public static IntSet selfEdges(IntGraph g) {
		if (!g.isAllowSelfEdges())
			return IntSets.EMPTY_SET;
		if (g instanceof IndexGraph)
			return selfEdges((IndexGraph) g);
		IntSet indexSelfEdges = selfEdges(g.indexGraph());
		return IndexIdMaps.indexToIdSet(indexSelfEdges, g.indexGraphEdgesMap());
	}

	private static IntSet selfEdges(IndexGraph g) {
		return new AbstractIntSet() {

			@Override
			public boolean contains(int edge) {
				return 0 <= edge && edge < g.edges().size() && g.edgeSource(edge) == g.edgeTarget(edge);
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
			public IntIterator iterator() {
				return new IntIterator() {
					final int m = g.edges().size();
					int nextEdge = 0;
					{
						advance();
					}

					private void advance() {
						for (; nextEdge < m; nextEdge++)
							if (g.edgeSource(nextEdge) == g.edgeTarget(nextEdge))
								break;
					}

					@Override
					public boolean hasNext() {
						return nextEdge < m;
					}

					@Override
					public int nextInt() {
						Assertions.Iters.hasNext(this);
						int edge = nextEdge++;
						advance();
						return edge;
					}
				};
			}
		};
	}

	/**
	 * Check whether a graph contain parallel edges.
	 *
	 * <p>
	 * Two parallel edges are edges that have the same source and target vertices.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     {@code true} if the graph contain at least one pair of parallel edges, else {@code false}
	 */
	public static <V, E> boolean containsParallelEdges(Graph<V, E> g) {
		if (!g.isAllowParallelEdges())
			return false;
		IndexGraph ig = g.indexGraph();
		int n = ig.vertices().size();
		int[] lastVisit = new int[n];
		for (int u = 0; u < n; u++) {
			final int visitIdx = u + 1;
			for (IEdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (lastVisit[v] == visitIdx)
					return true;
				lastVisit[v] = visitIdx;
			}
		}
		return false;
	}

}
