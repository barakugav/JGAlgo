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
package com.jgalgo.adapt.jgrapht;

import static com.jgalgo.internal.util.Range.range;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.jgrapht.GraphType;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IdBuilder;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexRemoveListener;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * An adapter from JGraphT graph to JGAlgo graph.
 *
 * <p>
 * The adapter is constructed with a {@linkplain org.jgrapht.Graph JGraphT graph} and implements the
 * {@linkplain com.jgalgo.graph.Graph JGAlgo graph} interface, and can be used with any JGAlgo algorithm. The adapter is
 * a live view, so any change in the JGAlgo graph is reflected in the JGraphT graph and vice versa, but the underlying
 * JGraphT graph should <b>not be modified</b> directly. Modifying the original JGraphT graph will invalidate the
 * adapter.
 *
 * <p>
 * The capabilities of the graph ({@link com.jgalgo.graph.Graph#isDirected()},
 * {@link com.jgalgo.graph.Graph#isAllowParallelEdges()}, and {@link com.jgalgo.graph.Graph#isAllowSelfEdges()}) are
 * determined by the {@link GraphType} of the JGraphT graph. If the original JGraphT graph was weighted, the adapter
 * will expose a single {@linkplain WeightsDouble double weight} type for edges, with the key passed in the
 * {@linkplain #JGraphTWrapper(org.jgrapht.Graph, String) constructor}. New weights of vertices or edges can not be
 * added to the graph as the underlying JGraphT graph support only a single double edge weight type.
 *
 * <p>
 * The adapter has much worse performance than the a regular JGAlgo graph. If memory is not an issue, it is probably
 * better to copy the adapter to a regular JGAlgo graph and use it instead. For example:
 *
 * <pre> {@code
 * org.jgrapht.Graph<V,E> originalGraph = ...;
 * com.jgalgo.graph.Graph<V,E> wrappedGraph = new JGraphTWrapper<>(originalGraph);
 * com.jgalgo.graph.Graph<V,E> regularGraph = wrappedGraph.immutableCopy(); // or just copy()
 * ...
 * }</pre>
 *
 * <p>
 * For adapting the other way around, from JGAlgo to JGraphT, see {@link JGraphTAdapter}.
 *
 * @see        org.jgrapht.Graph
 * @see        com.jgalgo.graph.Graph
 * @see        JGraphTAdapter
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class JGraphTWrapper<V, E> implements com.jgalgo.graph.Graph<V, E> {

	private final org.jgrapht.Graph<V, E> graph;
	private final String edgeWeightKey;
	private final WeightsDouble<E> weights;

	private final IndexIdMapImpl<V> indexGraphVerticesMap;
	private final IndexIdMapImpl<E> indexGraphEdgesMap;
	private final IndexGraph indexGraph;

	/**
	 * Constructs a new adapter from the given JGraphT graph without weights.
	 *
	 * @param graph the JGraphT graph to adapt
	 */
	public JGraphTWrapper(org.jgrapht.Graph<V, E> graph) {
		this(graph, null);
	}

	/**
	 * Constructs a new adapter from the given JGraphT graph optionally with weights.
	 *
	 * <p>
	 * If the JGraphT graph is weighted, and the {@code edgeWeightKey} is not {@code null}, the adapter will expose a
	 * single {@linkplain WeightsDouble double weight} type for edges, with the given key. If the JGraphT graph is not
	 * weighted, the {@code edgeWeightKey} must be {@code null}.
	 *
	 * @param  graph                    the JGraphT graph to adapt
	 * @param  edgeWeightKey            the key of the edge weight to use, or {@code null} if the graph is not weighted
	 * @throws IllegalArgumentException if the graph not is weighted and the {@code edgeWeightKey} is not {@code null}
	 */
	public JGraphTWrapper(org.jgrapht.Graph<V, E> graph, String edgeWeightKey) {
		this.graph = Objects.requireNonNull(graph);
		this.edgeWeightKey = edgeWeightKey;
		if (edgeWeightKey == null) {
			weights = null;
		} else {
			if (!graph.getType().isWeighted())
				throw new IllegalArgumentException("graph is not weighted");
			weights = new WeightsDouble<>() {

				@Override
				public double get(E element) {
					return graph.getEdgeWeight(element);
				}

				@Override
				public void set(E element, double weight) {
					graph.setEdgeWeight(element, weight);
				}

				@Override
				public double defaultWeight() {
					return 1;
				}
			};
		}

		indexGraphVerticesMap = new IndexIdMapImpl<>(false);
		indexGraphEdgesMap = new IndexIdMapImpl<>(true);
		for (V vertex : graph.vertexSet())
			indexGraphVerticesMap.add(vertex);
		for (E edge : graph.edgeSet())
			indexGraphEdgesMap.add(edge);
		indexGraph = new IndexGraphImpl<>(graph, edgeWeightKey, weights, indexGraphVerticesMap, indexGraphEdgesMap);
	}

	@Override
	public Set<V> vertices() {
		return graph.vertexSet();
	}

	@Override
	public Set<E> edges() {
		return graph.edgeSet();
	}

	@Override
	public void addVertex(V vertex) {
		if (graph.containsVertex(vertex))
			throw new IllegalArgumentException("Vertex '" + vertex + "' already exists");
		graph.addVertex(vertex);
		indexGraphVerticesMap.add(vertex);
	}

	@Override
	public void addVertices(Collection<? extends V> vertices) {
		for (V vertex : vertices)
			addVertex(vertex);
	}

	@Override
	public void removeVertex(V vertex) {
		checkVertex(vertex);
		removeEdgesOf(vertex);
		graph.removeVertex(vertex);
		indexGraphVerticesMap.remove(vertex);
	}

	@Override
	public void removeVertices(Collection<? extends V> vertices) {
		for (V vertex : vertices)
			removeVertex(vertex);
	}

	@Override
	public void renameVertex(V vertex, V newId) {
		throw new UnsupportedOperationException("unsupported by JGraphT graphs");
	}

	@Override
	public EdgeSet<V, E> outEdges(V source) {
		checkVertex(source);
		return new OutEdges<>(source, graph.outgoingEdgesOf(source), graph);
	}

	@Override
	public EdgeSet<V, E> inEdges(V target) {
		checkVertex(target);
		return new InEdges<>(target, graph.incomingEdgesOf(target), graph);
	}

	@Override
	public E getEdge(V source, V target) {
		checkVertex(source);
		checkVertex(target);
		return graph.getEdge(source, target);
	}

	@Override
	public EdgeSet<V, E> getEdges(V source, V target) {
		checkVertex(source);
		checkVertex(target);
		return new SourceTargetEdgeSet<>(source, target, graph.getAllEdges(source, target), graph);
	}

	private abstract static class BaseEdgeSet<V, E> extends AbstractSet<E> implements EdgeSet<V, E> {

		final Set<E> edges;
		final org.jgrapht.Graph<V, E> graph;
		final boolean directed;

		public BaseEdgeSet(Set<E> edges, org.jgrapht.Graph<V, E> graph) {
			this.edges = Objects.requireNonNull(edges);
			this.graph = graph;
			this.directed = graph.getType().isDirected();
		}

		@Override
		public int size() {
			return edges.size();
		}

		@Override
		public boolean contains(Object o) {
			return edges.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return edges.containsAll(c);
		}

		@Override
		public Object[] toArray() {
			return edges.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return edges.toArray(a);
		}

		private abstract class BaseIter implements EdgeIter<V, E> {

			Iterator<E> it = edges.iterator();
			E nextEdge = it.hasNext() ? it.next() : null;
			E lastEdge = null;

			@Override
			public boolean hasNext() {
				return nextEdge != null;
			}

			@Override
			public E next() {
				if (!hasNext())
					throw new NoSuchElementException();
				lastEdge = nextEdge;
				nextEdge = it.hasNext() ? it.next() : null;
				return lastEdge;
			}

			@Override
			public E peekNext() {
				if (!hasNext())
					throw new NoSuchElementException();
				return nextEdge;
			}

		}
	}

	private static class OutEdges<V, E> extends BaseEdgeSet<V, E> {

		private final V source;

		public OutEdges(V source, Set<E> edges, org.jgrapht.Graph<V, E> graph) {
			super(edges, graph);
			this.source = source;
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new BaseEdgeSet<V, E>.BaseIter() {
				@Override
				public V source() {
					if (lastEdge == null)
						throw new IllegalStateException();
					return source;
				}

				@Override
				public V target() {
					if (lastEdge == null)
						throw new IllegalStateException();
					V target = graph.getEdgeTarget(lastEdge);
					return directed || !target.equals(source) ? target : graph.getEdgeSource(lastEdge);
				}
			};
		}
	}

	private static class InEdges<V, E> extends BaseEdgeSet<V, E> {

		private final V target;

		public InEdges(V target, Set<E> edges, org.jgrapht.Graph<V, E> graph) {
			super(edges, graph);
			this.target = target;
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new BaseEdgeSet<V, E>.BaseIter() {
				@Override
				public V source() {
					if (lastEdge == null)
						throw new IllegalStateException();
					V source = graph.getEdgeSource(lastEdge);
					return directed || !source.equals(target) ? source : graph.getEdgeTarget(lastEdge);
				}

				@Override
				public V target() {
					if (lastEdge == null)
						throw new IllegalStateException();
					return target;
				}
			};
		}
	}

	private static class SourceTargetEdgeSet<V, E> extends BaseEdgeSet<V, E> {

		private final V source;
		private final V target;

		public SourceTargetEdgeSet(V source, V target, Set<E> edges, org.jgrapht.Graph<V, E> graph) {
			super(edges, graph);
			this.source = source;
			this.target = target;
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new BaseEdgeSet<V, E>.BaseIter() {
				@Override
				public V source() {
					if (lastEdge == null)
						throw new IllegalStateException();
					return source;
				}

				@Override
				public V target() {
					if (lastEdge == null)
						throw new IllegalStateException();
					return target;
				}
			};
		}
	}

	@Override
	public void addEdge(V source, V target, E edge) {
		if (graph.containsEdge(edge))
			throw new IllegalArgumentException("Edge '" + edge + "' already exists");
		graph.addEdge(source, target, edge);
		indexGraphEdgesMap.add(edge);
	}

	@Override
	public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
		@SuppressWarnings("unchecked")
		EdgeSet<V, E> edges0 = (EdgeSet<V, E>) edges;
		for (EdgeIter<V, E> eit = edges0.iterator(); eit.hasNext();) {
			E edge = eit.next();
			V source = eit.source();
			V target = eit.target();
			addEdge(source, target, edge);
		}
	}

	@Override
	public void removeEdge(E edge) {
		checkEdge(edge);
		graph.removeEdge(edge);
		indexGraphEdgesMap.remove(edge);
	}

	@Override
	public void removeEdges(Collection<? extends E> edges) {
		for (E edge : edges)
			removeEdge(edge);
	}

	@Override
	public void removeEdgesOf(V vertex) {
		List<E> edges = new ArrayList<>(graph.edgesOf(vertex));
		graph.removeAllEdges(edges);
		for (E edge : edges)
			indexGraphEdgesMap.remove(edge);
	}

	@Override
	public void removeOutEdgesOf(V vertex) {
		List<E> edges = new ArrayList<>(graph.outgoingEdgesOf(vertex));
		graph.removeAllEdges(edges);
		for (E edge : edges)
			indexGraphEdgesMap.remove(edge);
	}

	@Override
	public void removeInEdgesOf(V vertex) {
		List<E> edges = new ArrayList<>(graph.incomingEdgesOf(vertex));
		graph.removeAllEdges(edges);
		for (E edge : edges)
			indexGraphEdgesMap.remove(edge);
	}

	@Override
	public void renameEdge(E edge, E newId) {
		throw new UnsupportedOperationException("unsupported by JGraphT graphs");
	}

	@Override
	public void moveEdge(E edge, V newSource, V newTarget) {
		throw new UnsupportedOperationException("unsupported by JGraphT graphs");
	}

	@Override
	public V edgeSource(E edge) {
		checkEdge(edge);
		return graph.getEdgeSource(edge);
	}

	@Override
	public V edgeTarget(E edge) {
		checkEdge(edge);
		return graph.getEdgeTarget(edge);
	}

	@Override
	public V edgeEndpoint(E edge, V endpoint) {
		checkEdge(edge);
		checkVertex(endpoint);
		V source = graph.getEdgeSource(edge);
		V target = graph.getEdgeTarget(edge);
		if (source.equals(endpoint))
			return target;
		if (target.equals(endpoint))
			return source;
		throw new IllegalArgumentException("Vertex '" + endpoint + "' is not an endpoint of edge '" + edge + "'");
	}

	@Override
	public void clear() {
		graph.removeAllVertices(new ArrayList<>(graph.vertexSet()));
		indexGraphVerticesMap.clear();
		indexGraphEdgesMap.clear();
	}

	@Override
	public void clearEdges() {
		graph.removeAllEdges(new ArrayList<>(graph.edgeSet()));
		indexGraphEdgesMap.clear();
	}

	@Override
	public IdBuilder<V> vertexBuilder() {
		return null;
	}

	@Override
	public IdBuilder<E> edgeBuilder() {
		return null;
	}

	@Override
	public void ensureVertexCapacity(int vertexCapacity) {}

	@Override
	public void ensureEdgeCapacity(int edgeCapacity) {}

	@Override
	public <T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key) {
		return null;
	}

	@Override
	public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		throw new UnsupportedOperationException("unsupported by JGraphT graphs");
	}

	@Override
	public void removeVerticesWeights(String key) {
		throw new IllegalArgumentException("no vertices weights with key '" + key + "'");
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		return Set.of();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
		if (edgeWeightKey == null || !edgeWeightKey.equals(key))
			return null;
		return (WeightsT) weights;
	}

	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal) {
		throw new UnsupportedOperationException("unsupported by JGraphT graphs");
	}

	@Override
	public void removeEdgesWeights(String key) {
		if (edgeWeightKey != null && edgeWeightKey.equals(key)) {
			throw new UnsupportedOperationException("unsupported by JGraphT graphs");
		} else {
			throw new IllegalArgumentException("no edges weights with key '" + key + "'");
		}
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return edgeWeightKey != null ? Set.of(edgeWeightKey) : Set.of();
	}

	@Override
	public boolean isDirected() {
		return graph.getType().isDirected();
	}

	@Override
	public boolean isAllowSelfEdges() {
		return graph.getType().isAllowingSelfLoops();
	}

	@Override
	public boolean isAllowParallelEdges() {
		return graph.getType().isAllowingMultipleEdges();
	}

	@Override
	public IndexGraph indexGraph() {
		return indexGraph;
	}

	@Override
	public IndexIdMap<V> indexGraphVerticesMap() {
		return indexGraphVerticesMap;
	}

	@Override
	public IndexIdMap<E> indexGraphEdgesMap() {
		return indexGraphEdgesMap;
	}

	private void checkVertex(V vertex) {
		if (!graph.containsVertex(vertex))
			throw NoSuchVertexException.ofVertex(vertex);
	}

	private void checkEdge(E edge) {
		if (!graph.containsEdge(edge))
			throw NoSuchEdgeException.ofEdge(edge);
	}

	private static class IndexIdMapImpl<K> implements IndexIdMap<K> {

		private final Object2IntMap<K> idToIdx;
		private final List<K> idxToId;
		private final boolean isEdges;

		public IndexIdMapImpl(boolean isEdges) {
			idToIdx = new Object2IntOpenHashMap<>();
			idToIdx.defaultReturnValue(-1);
			idxToId = new ObjectArrayList<>();
			this.isEdges = isEdges;
		}

		void add(K id) {
			int idx = idxToId.size();
			idxToId.add(id);
			int oldVal = idToIdx.put(id, idx);
			assert oldVal == -1;
		}

		void remove(K id) {
			int idx = idToIdx.removeInt(id);
			int lastIdx = idxToId.size() - 1;
			if (idx == lastIdx) {
				idxToId.remove(lastIdx);
			} else {
				K lastId = idxToId.remove(lastIdx);
				idxToId.set(idx, lastId);
				int oldIdx = idToIdx.put(lastId, idx);
				assert oldIdx == lastIdx;
			}
		}

		void clear() {
			idToIdx.clear();
			idxToId.clear();
		}

		@Override
		public K indexToId(int index) {
			if (!(0 <= index && index < idxToId.size())) {
				if (isEdges) {
					throw NoSuchEdgeException.ofIndex(index);
				} else {
					throw NoSuchVertexException.ofIndex(index);
				}
			}
			return idxToId.get(index);
		}

		@Override
		public K indexToIdIfExist(int index) {
			if (!(0 <= index && index < idxToId.size()))
				return null;
			return idxToId.get(index);
		}

		@Override
		public int idToIndex(K id) {
			int idx = idToIdx.getInt(id);
			if (idx == -1) {
				if (isEdges) {
					throw NoSuchEdgeException.ofEdge(id);
				} else {
					throw NoSuchVertexException.ofVertex(id);
				}
			}
			return idx;
		}

		@Override
		public int idToIndexIfExist(K id) {
			return idToIdx.getInt(id);
		}
	}

	private static class IndexGraphImpl<V, E> implements IndexGraph {

		private final org.jgrapht.Graph<V, E> graph;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;
		private final IndexIntIdMap verticesIdentityIndexMap;
		private final IndexIntIdMap edgesIdentityIndexMap;
		private final IntSet vertices;
		private final IntSet edges;
		private final String edgeWeightKey;
		private final IWeightsDouble weights;

		public IndexGraphImpl(org.jgrapht.Graph<V, E> graph, String edgeWeightKey, WeightsDouble<E> idWeights,
				IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
			this.graph = graph;
			this.viMap = viMap;
			this.eiMap = eiMap;
			vertices = new IndicesSet(graph.vertexSet());
			edges = new IndicesSet(graph.edgeSet());
			this.verticesIdentityIndexMap = IndexIntIdMap.identityVerticesMap(vertices);
			this.edgesIdentityIndexMap = IndexIntIdMap.identityEdgesMap(edges);
			this.edgeWeightKey = edgeWeightKey;
			if (idWeights == null) {
				weights = null;
			} else {
				weights = new IWeightsDouble() {

					@Override
					public double get(int element) {
						return idWeights.get(eiMap.indexToId(element));
					}

					@Override
					public void set(int element, double weight) {
						idWeights.set(eiMap.indexToId(element), weight);
					}

					@Override
					public double defaultWeight() {
						return idWeights.defaultWeight();
					}
				};
			}
		}

		private static class IndicesSet extends AbstractIntSet {

			private final Set<?> idSet;

			public IndicesSet(Set<?> idSet) {
				this.idSet = idSet;
			}

			@Override
			public int size() {
				return idSet.size();
			}

			@Override
			public boolean contains(int key) {
				return 0 <= key && key < idSet.size();
			}

			@Override
			public boolean containsAll(IntCollection c) {
				return range(idSet.size()).containsAll(c);
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				return range(idSet.size()).containsAll(c);
			}

			@Override
			public int[] toIntArray() {
				return range(idSet.size()).toIntArray();
			}

			@Override
			public int[] toArray(int[] a) {
				return range(idSet.size()).toArray(a);
			}

			@Override
			public Object[] toArray() {
				return range(idSet.size()).toArray();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				return range(idSet.size()).toArray(a);
			}

			@Override
			public IntIterator iterator() {
				return range(idSet.size()).iterator();
			}
		}

		@Override
		public IEdgeSet outEdges(int source) {
			V source0 = viMap.indexToId(source);
			Set<E> edges = graph.outgoingEdgesOf(source0);
			return new OutEdges(source0, source, edges);
		}

		@Override
		public IEdgeSet inEdges(int target) {
			V target0 = viMap.indexToId(target);
			Set<E> edges = graph.incomingEdgesOf(target0);
			return new InEdges(target0, target, edges);
		}

		@Override
		public int getEdge(int source, int target) {
			V source0 = viMap.indexToId(source);
			V target0 = viMap.indexToId(target);
			E edge = graph.getEdge(source0, target0);
			return edge == null ? -1 : eiMap.idToIndex(edge);
		}

		@Override
		public IEdgeSet getEdges(int source, int target) {
			V source0 = viMap.indexToId(source);
			V target0 = viMap.indexToId(target);
			Set<E> edges = graph.getAllEdges(source0, target0);
			return new SourceTargetEdgeSet(source, target, edges);
		}

		@Override
		public void moveEdge(int edge, int newSource, int newTarget) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public int edgeSource(int edge) {
			E edge0 = eiMap.indexToId(edge);
			V source = graph.getEdgeSource(edge0);
			return viMap.idToIndex(source);
		}

		@Override
		public int edgeTarget(int edge) {
			E edge0 = eiMap.indexToId(edge);
			V target = graph.getEdgeTarget(edge0);
			return viMap.idToIndex(target);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			E edge0 = eiMap.indexToId(edge);
			V endpoint0 = viMap.indexToId(endpoint);
			V source = graph.getEdgeSource(edge0);
			V target = graph.getEdgeTarget(edge0);
			if (source.equals(endpoint0))
				return viMap.idToIndex(target);
			if (target.equals(endpoint0))
				return viMap.idToIndex(source);
			throw new IllegalArgumentException("Vertex '" + endpoint0 + "' is not an endpoint of edge '" + edge0 + "'");
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
			if (edgeWeightKey == null || !edgeWeightKey.equals(key))
				return null;
			return (WeightsT) weights;
		}

		@Override
		public void removeVertices(Collection<? extends Integer> vertices) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void removeEdges(Collection<? extends Integer> edges) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void ensureVertexCapacity(int vertexCapacity) {}

		@Override
		public void ensureEdgeCapacity(int edgeCapacity) {}

		@Override
		public Set<String> getVerticesWeightsKeys() {
			return Set.of();
		}

		@Override
		public Set<String> getEdgesWeightsKeys() {
			return edgeWeightKey != null ? Set.of(edgeWeightKey) : Set.of();
		}

		@Override
		public boolean isDirected() {
			return graph.getType().isDirected();
		}

		@Override
		public boolean isAllowSelfEdges() {
			return graph.getType().isAllowingSelfLoops();
		}

		@Override
		public boolean isAllowParallelEdges() {
			return graph.getType().isAllowingMultipleEdges();
		}

		@Override
		public IntSet vertices() {
			return vertices;
		}

		@Override
		public IntSet edges() {
			return edges;
		}

		@Override
		public int addVertexInt() {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void addVertices(Collection<? extends Integer> vertices) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void removeVertex(int vertex) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public int addEdge(int source, int target) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public IntSet addEdgesReassignIds(IEdgeSet edges) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void removeEdge(int edge) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void removeEdgesOf(int vertex) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void removeOutEdgesOf(int source) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void removeInEdgesOf(int target) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void clearEdges() {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
				T defVal) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void removeVerticesWeights(String key) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
				T defVal) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void removeEdgesWeights(String key) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public void addVertexRemoveListener(IndexRemoveListener listener) {
			/* TODO its possible to support this */
			throw new UnsupportedOperationException("not supported by " + JGraphTWrapper.class.getSimpleName());
		}

		@Override
		public void removeVertexRemoveListener(IndexRemoveListener listener) {
			/* TODO its possible to support this */
			throw new UnsupportedOperationException("not supported by " + JGraphTWrapper.class.getSimpleName());
		}

		@Override
		public void addEdgeRemoveListener(IndexRemoveListener listener) {
			/* TODO its possible to support this */
			throw new UnsupportedOperationException("not supported by " + JGraphTWrapper.class.getSimpleName());
		}

		@Override
		public void removeEdgeRemoveListener(IndexRemoveListener listener) {
			/* TODO its possible to support this */
			throw new UnsupportedOperationException("not supported by " + JGraphTWrapper.class.getSimpleName());
		}

		@Override
		public IndexIntIdMap indexGraphVerticesMap() {
			return verticesIdentityIndexMap;
		}

		@Override
		public IndexIntIdMap indexGraphEdgesMap() {
			return edgesIdentityIndexMap;
		}

		private abstract class EdgeSetBase extends AbstractIntSet implements IEdgeSet {

			private final Set<E> edges;

			public EdgeSetBase(Set<E> edges) {
				this.edges = Objects.requireNonNull(edges);
			}

			@Override
			public int size() {
				return edges.size();
			}

			@Override
			public boolean contains(int edge) {
				return edges.contains(eiMap.indexToIdIfExist(edge));
			}

			abstract class IterBase implements IEdgeIter {

				Iterator<E> it = edges.iterator();
				E nextEdge;
				E lastEdge;

				{
					advance();
				}

				private void advance() {
					if (it.hasNext()) {
						nextEdge = it.next();
					} else {
						nextEdge = null;
					}
				}

				@Override
				public boolean hasNext() {
					return nextEdge != null;
				}

				@Override
				public int nextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					lastEdge = nextEdge;
					advance();
					return eiMap.idToIndex(lastEdge);
				}

				@Override
				public int peekNextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					return eiMap.idToIndex(nextEdge);
				}

			}

		}

		private class OutEdges extends EdgeSetBase {

			private final V source;
			private final int sourceIdx;
			private final boolean directed;

			public OutEdges(V source, int sourceIdx, Set<E> edges) {
				super(edges);
				this.source = source;
				this.sourceIdx = sourceIdx;
				this.directed = graph.getType().isDirected();
			}

			@Override
			public IEdgeIter iterator() {
				return new IterBase() {

					@Override
					public int sourceInt() {
						if (lastEdge == null)
							throw new IllegalStateException();
						return sourceIdx;
					}

					@Override
					public int targetInt() {
						if (lastEdge == null)
							throw new IllegalStateException();
						V target = graph.getEdgeTarget(lastEdge);
						return directed || !source.equals(target) ? viMap.idToIndex(target)
								: viMap.idToIndex(graph.getEdgeSource(lastEdge));
					}
				};
			}
		}

		private class InEdges extends EdgeSetBase {

			private final V target;
			private final int targetIdx;
			private final boolean directed;

			public InEdges(V target, int targetIdx, Set<E> edges) {
				super(edges);
				this.target = target;
				this.targetIdx = targetIdx;
				this.directed = graph.getType().isDirected();
			}

			@Override
			public IEdgeIter iterator() {
				return new IterBase() {

					@Override
					public int sourceInt() {
						if (lastEdge == null)
							throw new IllegalStateException();
						V source = graph.getEdgeSource(lastEdge);
						return directed || !target.equals(source) ? viMap.idToIndex(source)
								: viMap.idToIndex(graph.getEdgeTarget(lastEdge));
					}

					@Override
					public int targetInt() {
						if (lastEdge == null)
							throw new IllegalStateException();
						return targetIdx;
					}
				};
			}
		}

		private class SourceTargetEdgeSet extends EdgeSetBase {

			private final int sourceIdx;
			private final int targetIdx;

			public SourceTargetEdgeSet(int sourceIdx, int targetIdx, Set<E> edges) {
				super(edges);
				this.sourceIdx = sourceIdx;
				this.targetIdx = targetIdx;
			}

			@Override
			public IEdgeIter iterator() {
				return new IterBase() {

					@Override
					public int sourceInt() {
						if (lastEdge == null)
							throw new IllegalStateException();
						return sourceIdx;
					}

					@Override
					public int targetInt() {
						if (lastEdge == null)
							throw new IllegalStateException();
						return targetIdx;
					}
				};
			}
		}
	}

}
