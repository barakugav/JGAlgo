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
package com.jgalgo.adapt.guava;

import static com.jgalgo.internal.util.Range.range;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import com.google.common.graph.MutableNetwork;
import com.jgalgo.graph.AbstractGraph;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IdBuilder;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexRemoveListener;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * An adapter from Guava Network to JGAlgo Graph.
 *
 * <p>
 * The adapter is constructed with a {@linkplain com.google.common.graph.Network Guava network} and implements the
 * {@linkplain com.jgalgo.graph.Graph JGAlgo graph} interface, and can be used with any JGAlgo algorithm. The adapter is
 * a live view, so any change in the JGAlgo graph is reflected in the JGraphT graph and vice versa, but the underlying
 * Guava network should <b>not be modified</b> directly. Modifying the original Guava network will invalidate the
 * adapter.
 *
 * <p>
 * The capabilities of the graph ({@link com.jgalgo.graph.Graph#isDirected()},
 * {@link com.jgalgo.graph.Graph#isAllowParallelEdges()}, and {@link com.jgalgo.graph.Graph#isAllowSelfEdges()}) are
 * determined by the corresponding capabilities of the underlying Guava network. Weights are not supported.
 *
 * <p>
 * Guava networks are mutable only they implement the {@link com.google.common.graph.MutableNetwork} interface. This
 * interface can be used for mutable and immutable networks. Whether or not the adapter is mutable is determined by the
 * constructor {@link #GuavaNetworkWrapper(com.google.common.graph.Network, boolean)}, which accept a {@code mutable}
 * flag. An immutable adapter can be constructed with either a mutable or an immutable Guava network. A mutable adapter
 * can be constructed only with a mutable Guava network. The constructor
 * {@link #GuavaNetworkWrapper(com.google.common.graph.Network)} determine the mutability of the adapter according to
 * the mutability of the Guava network, namely the adapter will be mutable if the network implements
 * {@link com.google.common.graph.MutableNetwork}. If this adapter is immutable, any attempt to modify it will throw an
 * {@link UnsupportedOperationException}.
 *
 * <p>
 * The adapter has much worse performance than the a regular JGAlgo graph. If memory is not an issue, it is probably
 * better to copy the adapter to a regular JGAlgo graph and use it instead. For example:
 *
 * <pre> {@code
 * com.google.common.graph.Network<V,E> originalNetwork = ...;
 * com.jgalgo.graph.Graph<V,E> wrappedGraph = new GuavaNetworkWrapper<>(originalGraph);
 * com.jgalgo.graph.Graph<V,E> regularGraph = wrappedGraph.immutableCopy(); // or just copy()
 * ...
 * }</pre>
 *
 * <p>
 * For adapting the other way around, from JGAlgo to Guava Network, see {@link GuavaNetworkAdapter}.
 *
 * @see        com.jgalgo.graph.Graph
 * @see        com.google.common.graph.Network
 * @see        com.google.common.graph.MutableNetwork
 * @see        GuavaNetworkAdapter
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class GuavaNetworkWrapper<V, E> extends AbstractGraph<V, E> {

	private final com.google.common.graph.Network<V, E> network;
	private final boolean mutable;

	private final IndexIdMapImpl<V> indexGraphVerticesMap;
	private final IndexIdMapImpl<E> indexGraphEdgesMap;
	private final IndexGraph indexGraph;

	/**
	 * Constructs a new adapter from the given Guava network.
	 *
	 * <p>
	 * Whether or not the adapter is mutable is determined by the mutability of the Guava network, namely the adapter
	 * will be mutable if the network implements {@link com.google.common.graph.MutableNetwork}. If this adapter is
	 * immutable, any attempt to modify it will throw an {@link UnsupportedOperationException}.
	 *
	 * @param network the Guava network
	 */
	public GuavaNetworkWrapper(com.google.common.graph.Network<V, E> network) {
		this(network, network instanceof MutableNetwork);
	}

	/**
	 * Constructs a new adapter from the given Guava network.
	 *
	 * <p>
	 * The adapter will be mutable if {@code mutable} is {@code true}, and immutable otherwise. If {@code mutable} is
	 * {@code true}, the Guava network must implement {@link com.google.common.graph.MutableNetwork}. If {@code mutable}
	 * is {@code false}, the Guava network can be either mutable or immutable. If this adapter is immutable, any attempt
	 * to modify it will throw an {@link UnsupportedOperationException}.
	 *
	 * @param  network                  the Guava network
	 * @param  mutable                  whether the adapter is mutable
	 * @throws IllegalArgumentException if {@code mutable} is {@code true} and the Guava network does not implement
	 *                                      {@link com.google.common.graph.MutableNetwork}
	 */
	public GuavaNetworkWrapper(com.google.common.graph.Network<V, E> network, boolean mutable) {
		if (mutable && !(network instanceof MutableNetwork))
			throw new IllegalArgumentException("network is not mutable");
		this.network = Objects.requireNonNull(network);
		this.mutable = mutable;

		indexGraphVerticesMap = new IndexIdMapImpl<>(false);
		indexGraphEdgesMap = new IndexIdMapImpl<>(true);
		for (V vertex : network.nodes())
			indexGraphVerticesMap.add(vertex);
		for (E edge : network.edges())
			indexGraphEdgesMap.add(edge);
		indexGraph = new IndexGraphImpl<>(network, indexGraphVerticesMap, indexGraphEdgesMap);
	}

	@Override
	public Set<V> vertices() {
		return network.nodes();
	}

	@Override
	public Set<E> edges() {
		return network.edges();
	}

	@Override
	public EdgeSet<V, E> outEdges(V source) {
		Set<E> edges;
		try {
			edges = network.outEdges(source);
		} catch (IllegalArgumentException e) {
			if (!network.nodes().contains(source))
				e = NoSuchVertexException.ofVertex(source);
			throw e;
		}
		return new OutEdges<>(source, edges, network);
	}

	@Override
	public EdgeSet<V, E> inEdges(V target) {
		Set<E> edges;
		try {
			edges = network.inEdges(target);
		} catch (IllegalArgumentException e) {
			if (!network.nodes().contains(target))
				e = NoSuchVertexException.ofVertex(target);
			throw e;
		}
		return new InEdges<>(target, edges, network);
	}

	@Override
	public E getEdge(V source, V target) {
		try {
			return network.edgeConnectingOrNull(source, target);
		} catch (IllegalArgumentException e) {
			if (!network.nodes().contains(source))
				e = NoSuchVertexException.ofVertex(source);
			if (!network.nodes().contains(target))
				e = NoSuchVertexException.ofVertex(target);
			throw e;
		}
	}

	@Override
	public EdgeSet<V, E> getEdges(V source, V target) {
		Set<E> edges;
		try {
			edges = network.edgesConnecting(source, target);
		} catch (IllegalArgumentException e) {
			if (!network.nodes().contains(source))
				e = NoSuchVertexException.ofVertex(source);
			if (!network.nodes().contains(target))
				e = NoSuchVertexException.ofVertex(target);
			throw e;
		}
		return new SourceTargetEdgeSet<>(source, target, edges, network);
	}

	private abstract static class EdgeSetBase<V, E> extends AbstractSet<E> implements EdgeSet<V, E> {

		final Set<E> edges;
		final com.google.common.graph.Network<V, E> network;

		EdgeSetBase(Set<E> edges, com.google.common.graph.Network<V, E> network) {
			this.edges = edges;
			this.network = network;
		}

		@Override
		public int size() {
			return edges.size();
		}

		@Override
		public boolean isEmpty() {
			return edges.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return edges.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return edges.containsAll(c);
		}

		abstract class IterBase implements EdgeIter<V, E> {

			private final Iterator<E> iter = edges.iterator();
			private E nextEdge = iter.hasNext() ? iter.next() : null;
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
				nextEdge = iter.hasNext() ? iter.next() : null;
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

	private static class OutEdges<V, E> extends EdgeSetBase<V, E> {

		private final V source;

		OutEdges(V source, Set<E> edges, com.google.common.graph.Network<V, E> network) {
			super(edges, network);
			this.source = source;
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new IterBase() {
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
					return network.incidentNodes(lastEdge).adjacentNode(source);
				}
			};
		}
	}

	private static class InEdges<V, E> extends EdgeSetBase<V, E> {

		private final V target;

		InEdges(V target, Set<E> edges, com.google.common.graph.Network<V, E> network) {
			super(edges, network);
			this.target = target;
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new IterBase() {
				@Override
				public V source() {
					if (lastEdge == null)
						throw new IllegalStateException();
					return network.incidentNodes(lastEdge).adjacentNode(target);
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

	private static class SourceTargetEdgeSet<V, E> extends EdgeSetBase<V, E> {

		private final V source;
		private final V target;

		public SourceTargetEdgeSet(V source, V target, Set<E> edges, com.google.common.graph.Network<V, E> network) {
			super(edges, network);
			this.source = source;
			this.target = target;
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new IterBase() {
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
	public V edgeSource(E edge) {
		try {
			return network.incidentNodes(edge).nodeU();
		} catch (IllegalArgumentException e) {
			if (!network.edges().contains(edge))
				e = NoSuchEdgeException.ofEdge(edge);
			throw e;
		}
	}

	@Override
	public V edgeTarget(E edge) {
		try {
			return network.incidentNodes(edge).nodeV();
		} catch (IllegalArgumentException e) {
			if (!network.edges().contains(edge))
				e = NoSuchEdgeException.ofEdge(edge);
			throw e;
		}
	}

	@Override
	public V edgeEndpoint(E edge, V endpoint) {
		try {
			return network.incidentNodes(edge).adjacentNode(endpoint);
		} catch (IllegalArgumentException e) {
			if (!network.edges().contains(edge))
				e = NoSuchEdgeException.ofEdge(edge);
			if (!network.nodes().contains(endpoint))
				e = NoSuchVertexException.ofVertex(endpoint);
			throw e;
		}
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
	public Set<String> getVerticesWeightsKeys() {
		return Set.of();
	}

	@Override
	public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		throw new UnsupportedOperationException("Guava Network does not support vertices weights");
	}

	@Override
	public void removeVerticesWeights(String key) {
		throw new IllegalArgumentException("no vertices weights with key '" + key + "'");
	}

	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
		return null;
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return Set.of();
	}

	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal) {
		throw new UnsupportedOperationException("Guava Network does not support edges weights");
	}

	@Override
	public void removeEdgesWeights(String key) {
		throw new IllegalArgumentException("no edges weights with key '" + key + "'");
	}

	@Override
	public boolean isDirected() {
		return network.isDirected();
	}

	@Override
	public boolean isAllowSelfEdges() {
		return network.allowsSelfLoops();
	}

	@Override
	public boolean isAllowParallelEdges() {
		return network.allowsParallelEdges();
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

	/* ------------------------- */
	/* --- mutable functions --- */
	/* ------------------------- */

	private MutableNetwork<V, E> mutableNetwork(String errMsg) {
		if (!mutable)
			throw new UnsupportedOperationException(errMsg);
		return (MutableNetwork<V, E>) network;
	}

	@Override
	public void addVertex(V vertex) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't add vertices");
		boolean modified = net.addNode(vertex);
		if (!modified)
			throw new IllegalArgumentException("vertex already in the graph: " + vertex);
		indexGraphVerticesMap.add(vertex);
	}

	@Override
	public void addVertices(Collection<? extends V> vertices) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't add vertices");
		int addVertices = 0;
		for (V vertex : vertices) {
			boolean modified = net.addNode(vertex);
			if (!modified) {
				/* roll back */
				for (Iterator<? extends V> it = vertices.iterator();;) {
					if (addVertices-- <= 0)
						break;
					boolean removed = net.removeNode(it.next());
					assert removed;
				}
				throw new IllegalArgumentException("duplicate vertex: " + vertex);
			}
			addVertices++;
		}
		for (V vertex : vertices)
			indexGraphVerticesMap.add(vertex);
	}

	@Override
	public void removeVertex(V vertex) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't remove vertices");
		if (!net.nodes().contains(vertex))
			throw NoSuchVertexException.ofVertex(vertex);
		for (E e : net.incidentEdges(vertex))
			indexGraphEdgesMap.remove(e);
		net.removeNode(vertex);
		indexGraphVerticesMap.remove(vertex);
	}

	@Override
	public void removeVertices(Collection<? extends V> vertices) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't remove vertices");
		for (V vertex : vertices)
			if (!net.nodes().contains(vertex))
				throw NoSuchVertexException.ofVertex(vertex);
		if (!(vertices instanceof Set)) {
			Set<V> vertices0 = new HashSet<>(vertices.size());
			for (V vertex : vertices) {
				boolean added = vertices0.add(vertex);
				if (!added)
					throw new IllegalArgumentException("duplicate vertex: " + vertex);
			}
		}
		for (V vertex : vertices) {
			for (E e : net.incidentEdges(vertex))
				indexGraphEdgesMap.remove(e);
			net.removeNode(vertex);
			indexGraphVerticesMap.remove(vertex);
		}
	}

	@Override
	public void renameVertex(V vertex, V newId) {
		throw new UnsupportedOperationException("Not supported by Guava Network");
	}

	@Override
	public void addEdge(V source, V target, E edge) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't add edges");
		if (!net.nodes().contains(source))
			throw NoSuchVertexException.ofVertex(source);
		if (!net.nodes().contains(target))
			throw NoSuchVertexException.ofVertex(target);
		boolean modified = net.addEdge(source, target, edge);
		if (!modified)
			throw new IllegalArgumentException("edge already in the graph: " + edge);
		indexGraphEdgesMap.add(edge);
	}

	@Override
	public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't add edges");
		@SuppressWarnings("unchecked")
		EdgeSet<V, E> es = (EdgeSet<V, E>) edges;
		int addedEdges = 0;
		for (EdgeIter<V, E> eit = es.iterator(); eit.hasNext();) {
			E e = eit.next();
			V u = eit.source();
			V v = eit.target();
			try {
				addEdge(u, v, e);
			} catch (RuntimeException ex) {
				/* roll back */
				for (Iterator<E> it = es.iterator();;) {
					if (addedEdges-- <= 0)
						break;
					E e1 = it.next();
					boolean removed = net.removeEdge(e1);
					assert removed;
					indexGraphEdgesMap.remove(e1);
				}
				throw ex;
			}
			addedEdges++;
		}
	}

	@Override
	public void removeEdge(E edge) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't remove edges");
		boolean modified = net.removeEdge(edge);
		if (!modified)
			throw NoSuchEdgeException.ofEdge(edge);
		indexGraphEdgesMap.remove(edge);
	}

	@Override
	public void removeEdges(Collection<? extends E> edges) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't remove edges");
		for (E edge : edges)
			if (!net.edges().contains(edge))
				throw NoSuchEdgeException.ofEdge(edge);
		if (!(edges instanceof Set)) {
			Set<E> edges0 = new HashSet<>(edges.size());
			for (E edge : edges) {
				boolean added = edges0.add(edge);
				if (!added)
					throw new IllegalArgumentException("duplicate edge: " + edge);
			}
		}
		removeEdgesImpl(edges);
	}

	private void removeEdgesImpl(Collection<? extends E> edges) {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't remove edges");
		for (E edge : edges) {
			boolean removed = net.removeEdge(edge);
			assert removed;
			indexGraphEdgesMap.remove(edge);
		}
	}

	@Override
	public void removeEdgesOf(V vertex) {
		Object[] edges = network.incidentEdges(vertex).toArray();
		@SuppressWarnings("unchecked")
		List<E> edges0 = ObjectList.of((E[]) edges);
		removeEdgesImpl(edges0);
	}

	@Override
	public void removeOutEdgesOf(V source) {
		Object[] edges = network.outEdges(source).toArray();
		@SuppressWarnings("unchecked")
		List<E> edges0 = ObjectList.of((E[]) edges);
		removeEdgesImpl(edges0);
	}

	@Override
	public void removeInEdgesOf(V target) {
		Object[] edges = network.inEdges(target).toArray();
		@SuppressWarnings("unchecked")
		List<E> edges0 = ObjectList.of((E[]) edges);
		removeEdgesImpl(edges0);
	}

	@Override
	public void renameEdge(E edge, E newId) {
		throw new UnsupportedOperationException("Not supported by Guava Network");
	}

	@Override
	public void moveEdge(E edge, V newSource, V newTarget) {
		throw new UnsupportedOperationException("Not supported by Guava Network");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't remove vertices");
		for (Object vertex : net.nodes().toArray())
			net.removeNode((V) vertex);
		indexGraphVerticesMap.clear();
		indexGraphEdgesMap.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clearEdges() {
		MutableNetwork<V, E> net = mutableNetwork("graph is immutable, can't remove edges");
		for (Object edge : net.edges().toArray())
			net.removeEdge((E) edge);
		indexGraphEdgesMap.clear();
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

	private static class IndexGraphImpl<V, E> extends com.jgalgo.graph.AbstractGraph<Integer, Integer>
			implements IndexGraph {

		private final com.google.common.graph.Network<V, E> network;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;
		private IndexIntIdMap verticesIdentityIndexMap;
		private IndexIntIdMap edgesIdentityIndexMap;
		private final IntSet vertices;
		private final IntSet edges;

		public IndexGraphImpl(com.google.common.graph.Network<V, E> network, IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
			this.network = network;
			this.viMap = viMap;
			this.eiMap = eiMap;
			vertices = new IndicesSet(network.nodes()); // TODO network.nodes() might not be a view
			edges = new IndicesSet(network.edges()); // TODO network.nodes() might not be a view
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
			Set<E> edges = network.outEdges(source0);
			return new OutEdges(source0, source, edges);
		}

		@Override
		public IEdgeSet inEdges(int target) {
			V target0 = viMap.indexToId(target);
			Set<E> edges = network.inEdges(target0);
			return new InEdges(target0, target, edges);
		}

		@Override
		public int getEdge(int source, int target) {
			V source0 = viMap.indexToId(source);
			V target0 = viMap.indexToId(target);
			Set<E> edges = network.edgesConnecting(source0, target0);
			return edges.isEmpty() ? -1 : eiMap.idToIndex(edges.iterator().next());
		}

		@Override
		public IEdgeSet getEdges(int source, int target) {
			V source0 = viMap.indexToId(source);
			V target0 = viMap.indexToId(target);
			Set<E> edges = network.edgesConnecting(source0, target0);
			return new SourceTargetEdgeSet(source, target, edges);
		}

		@Override
		public void moveEdge(int edge, int newSource, int newTarget) {
			throw new UnsupportedOperationException("modifying the graph via the IndexGraph is not supported");
		}

		@Override
		public int edgeSource(int edge) {
			E edge0 = eiMap.indexToId(edge);
			V source = network.incidentNodes(edge0).nodeU();
			return viMap.idToIndex(source);
		}

		@Override
		public int edgeTarget(int edge) {
			E edge0 = eiMap.indexToId(edge);
			V target = network.incidentNodes(edge0).nodeV();
			return viMap.idToIndex(target);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			E edge0 = eiMap.indexToId(edge);
			V endpoint0 = viMap.indexToId(endpoint);
			V otherEndpoint = network.incidentNodes(edge0).adjacentNode(endpoint0);
			return viMap.idToIndex(otherEndpoint);
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key) {
			return null;
		}

		@Override
		public <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
			return null;
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
			return Set.of();
		}

		@Override
		public boolean isDirected() {
			return network.isDirected();
		}

		@Override
		public boolean isAllowSelfEdges() {
			return network.allowsSelfLoops();
		}

		@Override
		public boolean isAllowParallelEdges() {
			return network.allowsParallelEdges();
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
			throw new UnsupportedOperationException("not supported by " + GuavaNetworkWrapper.class.getSimpleName());
		}

		@Override
		public void removeVertexRemoveListener(IndexRemoveListener listener) {
			/* TODO its possible to support this */
			throw new UnsupportedOperationException("not supported by " + GuavaNetworkWrapper.class.getSimpleName());
		}

		@Override
		public void addEdgeRemoveListener(IndexRemoveListener listener) {
			/* TODO its possible to support this */
			throw new UnsupportedOperationException("not supported by " + GuavaNetworkWrapper.class.getSimpleName());
		}

		@Override
		public void removeEdgeRemoveListener(IndexRemoveListener listener) {
			/* TODO its possible to support this */
			throw new UnsupportedOperationException("not supported by " + GuavaNetworkWrapper.class.getSimpleName());
		}

		@Deprecated
		@Override
		public IndexIntIdMap indexGraphVerticesMap() {
			if (verticesIdentityIndexMap == null)
				verticesIdentityIndexMap = IndexIntIdMap.identityVerticesMap(vertices);
			return verticesIdentityIndexMap;
		}

		@Deprecated
		@Override
		public IndexIntIdMap indexGraphEdgesMap() {
			if (edgesIdentityIndexMap == null)
				edgesIdentityIndexMap = IndexIntIdMap.identityEdgesMap(edges);
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

			public OutEdges(V source, int sourceIdx, Set<E> edges) {
				super(edges);
				this.source = source;
				this.sourceIdx = sourceIdx;
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
						V target = network.incidentNodes(lastEdge).adjacentNode(source);
						return viMap.idToIndex(target);
					}
				};
			}
		}

		private class InEdges extends EdgeSetBase {

			private final V target;
			private final int targetIdx;

			public InEdges(V target, int targetIdx, Set<E> edges) {
				super(edges);
				this.target = target;
				this.targetIdx = targetIdx;
			}

			@Override
			public IEdgeIter iterator() {
				return new IterBase() {

					@Override
					public int sourceInt() {
						if (lastEdge == null)
							throw new IllegalStateException();
						V source = network.incidentNodes(lastEdge).adjacentNode(target);
						return viMap.idToIndex(source);
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
