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
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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
		public void reverseEdge(E edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot reverse edges");
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
			return this instanceof IndexGraph ? (IndexGraph) this : Graphs.immutableView(graph.indexGraph());
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

	private static class ImmutableIntGraphView extends IntGraphBase implements ImmutableGraph {

		private final IntGraph graph;

		ImmutableIntGraphView(IntGraph g) {
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
		public void reverseEdge(int edge) {
			throw new UnsupportedOperationException("graph is immutable, cannot reverse edges");
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

		@Override
		public IntGraph copy() {
			return graph.copy();
		}

		@Override
		public IndexGraph indexGraph() {
			return this instanceof IndexGraph ? (IndexGraph) this : Graphs.immutableView(graph.indexGraph());
		}

		@Override
		public IndexIntIdMap indexGraphVerticesMap() {
			return graph.indexGraphVerticesMap();
		}

		@Override
		public IndexIntIdMap indexGraphEdgesMap() {
			return graph.indexGraphEdgesMap();
		}

		IntGraph graph() {
			return graph;
		}
	}

	private static class ImmutableIndexGraphView extends ImmutableIntGraphView implements IndexGraphImpl {

		ImmutableIndexGraphView(IndexGraph g) {
			super(g);
			assert g instanceof IndexGraphImpl;
		}

		@Override
		IndexGraphImpl graph() {
			return (IndexGraphImpl) super.graph();
		}

		@Override
		public IndexGraph copy() {
			return graph().copy();
		}

		@Override
		public GraphElementSet vertices() {
			return graph().vertices();
		}

		@Override
		public GraphElementSet edges() {
			return graph().edges();
		}

		@Override
		@Deprecated
		public void addVertex(int vertex) {
			IndexGraphImpl.super.addVertex(vertex);
		}

		@Override
		@Deprecated
		public void addEdge(int source, int target, int edge) {
			IndexGraphImpl.super.addEdge(source, target, edge);
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

	static IndexGraph immutableView(IndexGraph g) {
		return g instanceof ImmutableGraph ? g : new ImmutableIndexGraphView(g);
	}

	static IntGraph immutableView(IntGraph g) {
		if (g instanceof IndexGraph)
			return immutableView((IndexGraph) g);
		return g instanceof ImmutableGraph ? g : new ImmutableIntGraphView(g);
	}

	@SuppressWarnings("unchecked")
	static <V, E> Graph<V, E> immutableView(Graph<V, E> g) {
		if (g instanceof IndexGraph)
			return (Graph<V, E>) immutableView((IndexGraph) g);
		if (g instanceof IntGraph)
			return (Graph<V, E>) immutableView((IntGraph) g);
		return g instanceof ImmutableGraph ? g : new ImmutableGraphView<>(g);
	}

	private static class ReverseGraph<V, E> extends GraphBase<V, E> {

		private final Graph<V, E> graph;

		ReverseGraph(Graph<V, E> g) {
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
			graph.addVertex(vertex);
		}

		@Override
		public void removeVertex(V vertex) {
			graph.removeVertex(vertex);
		}

		@Override
		public EdgeSet<V, E> outEdges(V source) {
			return new ReversedEdgeSet<>(graph.inEdges(source));
		}

		@Override
		public EdgeSet<V, E> inEdges(V target) {
			return new ReversedEdgeSet<>(graph.outEdges(target));
		}

		@Override
		public EdgeSet<V, E> getEdges(V source, V target) {
			return new ReversedEdgeSet<>(graph.getEdges(target, source));
		}

		@Override
		public void addEdge(V source, V target, E edge) {
			graph.addEdge(target, source, edge);
		}

		@Override
		public void removeEdge(E edge) {
			graph.removeEdge(edge);
		}

		@Override
		public void reverseEdge(E edge) {
			graph.reverseEdge(edge);
		}

		@Override
		public V edgeSource(E edge) {
			return graph.edgeTarget(edge);
		}

		@Override
		public V edgeTarget(E edge) {
			return graph.edgeSource(edge);
		}

		@Override
		public void clear() {
			graph.clear();
		}

		@Override
		public void clearEdges() {
			graph.clearEdges();
		}

		@Override
		public <T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key) {
			return graph.getVerticesWeights(key);
		}

		@Override
		public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
				T defVal) {
			return graph.addVerticesWeights(key, type, defVal);
		}

		@Override
		public void removeVerticesWeights(String key) {
			graph.removeVerticesWeights(key);
		}

		@Override
		public Set<String> getVerticesWeightsKeys() {
			return graph.getVerticesWeightsKeys();
		}

		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
			return graph.getEdgesWeights(key);
		}

		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
				T defVal) {
			return graph.addEdgesWeights(key, type, defVal);
		}

		@Override
		public void removeEdgesWeights(String key) {
			graph.removeEdgesWeights(key);
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
			return this instanceof IndexGraph ? (IndexGraph) this : Graphs.reverseView(graph.indexGraph());
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

	private static class ReverseIntGraph extends IntGraphBase {

		private final IntGraph graph;

		ReverseIntGraph(IntGraph g) {
			this.graph = Objects.requireNonNull(g);
		}

		IntGraph graph() {
			return graph;
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
			return graph.addVertex();
		}

		@Override
		public void addVertex(int vertex) {
			graph.addVertex(vertex);
		}

		@Override
		public void removeVertex(int vertex) {
			graph.removeVertex(vertex);
		}

		@Override
		public IEdgeSet outEdges(int source) {
			return new ReversedIEdgeSet(graph.inEdges(source));
		}

		@Override
		public IEdgeSet inEdges(int target) {
			return new ReversedIEdgeSet(graph.outEdges(target));
		}

		@Override
		public IEdgeSet getEdges(int source, int target) {
			return new ReversedIEdgeSet(graph.getEdges(target, source));
		}

		@Override
		public int addEdge(int source, int target) {
			return graph.addEdge(target, source);
		}

		@Override
		public void addEdge(int source, int target, int edge) {
			graph.addEdge(target, source, edge);
		}

		@Override
		public void removeEdge(int edge) {
			graph.removeEdge(edge);
		}

		@Override
		public int edgeSource(int edge) {
			return graph.edgeTarget(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return graph.edgeSource(edge);
		}

		@Override
		public void clear() {
			graph.clear();
		}

		@Override
		public void clearEdges() {
			graph.clearEdges();
		}

		@Override
		public <T, WeightsT extends IWeights<T>> WeightsT getVerticesIWeights(String key) {
			return graph.getVerticesWeights(key);
		}

		@Override
		public Set<String> getVerticesWeightsKeys() {
			return graph.getVerticesWeightsKeys();
		}

		@Override
		public void removeVerticesWeights(String key) {
			graph.removeVerticesWeights(key);
		}

		@Override
		public <T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key) {
			return graph.getEdgesWeights(key);
		}

		@Override
		public Set<String> getEdgesWeightsKeys() {
			return graph.getEdgesWeightsKeys();
		}

		@Override
		public void removeEdgesWeights(String key) {
			graph.removeEdgesWeights(key);
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
		public void reverseEdge(int edge) {
			graph.reverseEdge(edge);
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
				T defVal) {
			return graph.addVerticesWeights(key, type, defVal);
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
				T defVal) {
			return graph.addEdgesWeights(key, type, defVal);
		}

		@Override
		public IndexGraph indexGraph() {
			return this instanceof IndexGraph ? (IndexGraph) this : Graphs.reverseView(graph.indexGraph());
		}

		@Override
		public IndexIntIdMap indexGraphVerticesMap() {
			return graph.indexGraphVerticesMap();
		}

		@Override
		public IndexIntIdMap indexGraphEdgesMap() {
			return graph.indexGraphEdgesMap();
		}

	}

	private static class ReverseIndexGraph extends ReverseIntGraph implements IndexGraphImpl {

		ReverseIndexGraph(IndexGraph g) {
			super(g);
			assert g instanceof IndexGraphImpl;
		}

		@Override
		IndexGraphImpl graph() {
			return (IndexGraphImpl) super.graph();
		}

		@Override
		public GraphElementSet vertices() {
			return graph().vertices();
		}

		@Override
		public GraphElementSet edges() {
			return graph().edges();
		}

		@Override
		@Deprecated
		public void addVertex(int vertex) {
			IndexGraphImpl.super.addVertex(vertex);
		}

		@Override
		@Deprecated
		public void addEdge(int source, int target, int edge) {
			IndexGraphImpl.super.addEdge(source, target, edge);
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

	private static class ReversedEdgeIter<V, E> implements EdgeIter<V, E> {
		final EdgeIter<V, E> it;

		ReversedEdgeIter(EdgeIter<V, E> it) {
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

		@Override
		public V source() {
			return it.target();
		}

		@Override
		public V target() {
			return it.source();
		}

		@Override
		public void remove() {
			it.remove();
		}
	}

	private static class ReversedIEdgeIter implements IEdgeIter {
		final IEdgeIter it;

		ReversedIEdgeIter(IEdgeIter it) {
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

		@Override
		public int sourceInt() {
			return it.targetInt();
		}

		@Override
		public int targetInt() {
			return it.sourceInt();
		}

		@Override
		public void remove() {
			it.remove();
		}
	}

	static IndexGraph reverseView(IndexGraph g) {
		return g instanceof ReverseIntGraph ? ((ReverseIntGraph) g).graph.indexGraph() : new ReverseIndexGraph(g);
	}

	static IntGraph reverseView(IntGraph g) {
		if (g instanceof IndexGraph)
			return reverseView((IndexGraph) g);
		return g instanceof ReverseIntGraph ? ((ReverseIntGraph) g).graph : new ReverseIntGraph(g);
	}

	@SuppressWarnings("unchecked")
	static <V, E> Graph<V, E> reverseView(Graph<V, E> g) {
		if (g instanceof IndexGraph)
			return (Graph<V, E>) reverseView((IndexGraph) g);
		if (g instanceof IntGraph)
			return (Graph<V, E>) reverseView((IntGraph) g);
		return g instanceof ReverseGraph ? ((ReverseGraph<V, E>) g).graph : new ReverseGraph<>(g);
	}

	static String getIndexGraphImpl(IndexGraph g) {
		for (;;) {
			IndexGraph g0 = g;
			if (g instanceof ReverseIndexGraph)
				g = ((ReverseIndexGraph) g).graph();
			if (g instanceof ImmutableIndexGraphView)
				g = ((ImmutableIndexGraphView) g).graph();
			if (g instanceof GraphArrayAbstract)
				return "array";
			if (g instanceof GraphLinkedAbstract)
				return "linked-list";
			if (g instanceof GraphHashmapAbstract)
				return "hashtable";
			if (g instanceof GraphMatrixAbstract)
				return "matrix";
			if (g == g0)
				return null;
		}
	}

	static final IndexIntIdMap IndexIdMapIdentify = new IndexGraphMapIdentify();

	private static class IndexGraphMapIdentify implements IndexIntIdMap {
		@Override
		public int indexToIdInt(int index) {
			return index;
		}

		@Override
		public int idToIndex(int id) {
			return id;
		}

		@Override
		public int idToIndexIfExist(int id) {
			return id;
		}
	}

	static class EdgeSetSourceTargetSingleton extends AbstractIntSet implements IEdgeSet {

		private final IntGraph g;
		private final int source, target;
		private int edge;
		private static final int EdgeNone = -1;

		EdgeSetSourceTargetSingleton(IntGraph g, int source, int target, int edge) {
			this.g = g;
			this.source = source;
			this.target = target;
			this.edge = edge;
		}

		@Override
		public boolean remove(int edge) {
			if (this.edge != edge)
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
			throw new NullPointerException();
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
			for (E e : g.edges()) {
				V u = g.edgeSource(e), v = g.edgeTarget(e);
				if (gb.vertices().contains(u) && gb.vertices().contains(v))
					gb.addEdge(u, v, e);
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
			for (int e : g.edges()) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (gb.vertices().contains(u) && gb.vertices().contains(v))
					gb.addEdge(u, v, e);
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
		} else if (type == Object.class) {
			WeightsObj<K, Object> src0 = (WeightsObj<K, Object>) src;
			WeightsObj<K, Object> dst0 = (WeightsObj<K, Object>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else {
			throw new AssertionError();
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
		} else if (type == Object.class) {
			IWeightsObj src0 = (IWeightsObj) src;
			IWeightsObj dst0 = (IWeightsObj) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else {
			throw new AssertionError();
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
		if (w instanceof WeightsObj)
			return Object.class;
		throw new AssertionError();
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

}
