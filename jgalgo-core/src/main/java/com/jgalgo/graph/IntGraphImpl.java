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

import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import com.jgalgo.graph.Graphs.ImmutableGraph;
import com.jgalgo.internal.JGAlgoConfigImpl;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;

class IntGraphImpl extends GraphBase<Integer, Integer> implements IntGraph {

	final IndexGraph indexGraph;
	final IndexIntIdMapImpl viMap;
	final IndexIntIdMapImpl eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> edgesWeights = new IdentityHashMap<>();
	private final ToIntFunction<IntSet> vIdStrategy = IdStrategy.get();
	private final ToIntFunction<IntSet> eIdStrategy = IdStrategy.get();

	private static final Supplier<ToIntFunction<IntSet>> IdStrategy;

	static {
		Object strat = JGAlgoConfigImpl.GraphIdStrategy;
		if (strat == null)
			strat = "counter"; // default
		if (strat instanceof String) {
			String strategyName = (String) strat;

			switch (strategyName.toLowerCase()) {
				case "counter":
					IdStrategy = () -> {
						return new ToIntFunction<>() {
							private int counter;

							@Override
							public int applyAsInt(IntSet ids) {
								for (;;) {
									int id = ++counter;
									if (!ids.contains(id))
										return id;
								}
							}
						};
					};
					break;
				case "rand":
				case "random":
					IdStrategy = () -> {
						final Random rand = new Random();
						return (IntSet idSet) -> {
							for (;;) {
								int id = rand.nextInt();
								if (id >= 1 && !idSet.contains(id))
									// We prefer non zero IDs because fastutil handle zero (null) keys
									// separately
									return id;
							}
						};
					};
					break;

				default:
					throw new IllegalArgumentException("unknown id strategy: " + strategyName);
			}

		} else if (strat instanceof Supplier) {
			@SuppressWarnings("unchecked")
			Supplier<ToIntFunction<IntSet>> stratFunc = (Supplier<ToIntFunction<IntSet>>) strat;
			IdStrategy = stratFunc;
		} else {
			throw new IllegalArgumentException("Unknown graph ID strategy: " + JGAlgoConfigImpl.GraphIdStrategy);
		}
	}

	IntGraphImpl(IndexGraph g, int expectedVerticesNum, int expectedEdgesNum) {
		indexGraph = g;
		viMap = IndexIntIdMapImpl.newEmpty(indexGraph.vertices(), false, expectedVerticesNum);
		eiMap = IndexIntIdMapImpl.newEmpty(indexGraph.edges(), true, expectedEdgesNum);
		viMap.initListeners(indexGraph);
		eiMap.initListeners(indexGraph);
	}

	IntGraphImpl(IndexGraph indexGraph, IndexIdMap<Integer> viMap, IndexIdMap<Integer> eiMap,
			IndexGraphBuilder.ReIndexingMap vReIndexing, IndexGraphBuilder.ReIndexingMap eReIndexing) {
		this.indexGraph = indexGraph;
		boolean immutable = this.indexGraph instanceof ImmutableGraph;
		this.viMap = IndexIntIdMapImpl.newCopyOf(viMap, vReIndexing, this.indexGraph.vertices(), false, immutable);
		this.eiMap = IndexIntIdMapImpl.newCopyOf(eiMap, eReIndexing, this.indexGraph.edges(), true, immutable);
		if (!immutable) {
			this.viMap.initListeners(this.indexGraph);
			this.eiMap.initListeners(this.indexGraph);
		}
	}

	/* copy constructor */
	IntGraphImpl(Graph<Integer, Integer> orig, IndexGraphFactory indexGraphFactory, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		this(indexGraphFactory.newCopyOf(orig.indexGraph(), copyVerticesWeights, copyEdgesWeights),
				orig.indexGraphVerticesMap(), orig.indexGraphEdgesMap(), null, null);
	}

	@Override
	public IndexGraph indexGraph() {
		return indexGraph;
	}

	@Override
	public IndexIntIdMap indexGraphVerticesMap() {
		return viMap;
	}

	@Override
	public IndexIntIdMap indexGraphEdgesMap() {
		return eiMap;
	}

	@Override
	public IntSet vertices() {
		return viMap.idSet();
	}

	@Override
	public IntSet edges() {
		return eiMap.idSet();
	}

	@Override
	public int addVertexInt() {
		int id = vIdStrategy.applyAsInt(vertices());
		int vIdx = indexGraph.addVertexInt();
		viMap.addId(id, vIdx);
		return id;
	}

	@Override
	public void addVertex(int vertex) {
		if (vertex < 0)
			throw new IllegalArgumentException("Vertex must be non negative");
		int vIdx = indexGraph.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = indexGraph.addVertexInt();
		assert vIdx == vIdx2;
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
		if (!(vertices instanceof IntCollection))
			for (Integer vertex : vertices)
				if (vertex == null)
					throw new NullPointerException("Vertex must be non null");
		for (int vertex : vertices)
			if (vertex < 0)
				throw new IllegalArgumentException("Vertex must be non negative");

		final int verticesNumBefore = indexGraph.vertices().size();
		ensureVertexCapacity(verticesNumBefore + vertices.size());
		int nextIdx = verticesNumBefore;
		for (int vertex : vertices) {
			boolean added = viMap.addIdIfNotDuplicate(vertex, nextIdx);
			if (!added) {
				for (; nextIdx-- > verticesNumBefore;)
					viMap.rollBackRemove(nextIdx);
				throw new IllegalArgumentException("Duplicate vertex: " + vertex);
			}
			nextIdx++;
		}
		indexGraph.addVertices(range(verticesNumBefore, nextIdx));
	}

	@Override
	public void removeVertex(int vertex) {
		int vIdx = viMap.idToIndex(vertex);
		indexGraph.removeVertex(vIdx);
	}

	@Override
	public void removeVertices(Collection<? extends Integer> vertices) {
		@SuppressWarnings("unchecked")
		IntCollection vIdxs = IndexIdMaps.idToIndexCollection((Collection<Integer>) vertices, viMap);
		indexGraph.removeVertices(vIdxs);
	}

	@Override
	public void renameVertex(int vertex, int newId) {
		if (newId < 0)
			throw new IllegalArgumentException("Vertex must be non negative");
		viMap.renameId(vertex, newId);
	}

	@Override
	public IEdgeSet outEdges(int source) {
		IEdgeSet indexSet = indexGraph.outEdges(viMap.idToIndex(source));
		return new IndexToIdEdgeSet(indexSet, viMap, eiMap);
	}

	@Override
	public IEdgeSet inEdges(int target) {
		IEdgeSet indexSet = indexGraph.inEdges(viMap.idToIndex(target));
		return new IndexToIdEdgeSet(indexSet, viMap, eiMap);
	}

	@Override
	public int getEdge(int source, int target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.getEdge(uIdx, vIdx);
		return eIdx == -1 ? -1 : eiMap.indexToIdInt(eIdx);
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		IEdgeSet indexSet = indexGraph.getEdges(uIdx, vIdx);
		return new IndexToIdEdgeSet(indexSet, viMap, eiMap);
	}

	@Override
	public int addEdge(int source, int target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int id = eIdStrategy.applyAsInt(edges());
		int eIdx = indexGraph.addEdge(uIdx, vIdx);
		eiMap.addId(id, eIdx);
		return id;
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		if (edge < 0)
			throw new IllegalArgumentException("Edge must be non negative");

		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.edges().size();
		eiMap.addId(edge, eIdx);

		try {
			int eIdx2 = indexGraph.addEdge(uIdx, vIdx);
			assert eIdx == eIdx2;
		} catch (RuntimeException e) {
			eiMap.rollBackRemove(eIdx);
			throw e;
		}
	}

	@Override
	public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
		final int edgesNumBefore = indexGraph.edges().size();
		ensureEdgeCapacity(edgesNumBefore + edges.size());
		int nextMapIdx = edgesNumBefore;
		try {
			for (int edge : edges) {
				if (edge < 0)
					throw new IllegalArgumentException("Edge must be non negative");
				boolean added = eiMap.addIdIfNotDuplicate(edge, nextMapIdx);
				if (!added)
					throw new IllegalArgumentException("Duplicate edge: " + edge);
				nextMapIdx++;
			}

			@SuppressWarnings("unchecked")
			EdgeSet<Integer, Integer> edges0 = (EdgeSet<Integer, Integer>) edges;
			indexGraph.addEdgesReassignIds(new AddEdgesIgnoreIdsIndexSet(edges0, viMap));

		} catch (RuntimeException e) {
			for (; nextMapIdx-- > edgesNumBefore;)
				eiMap.rollBackRemove(nextMapIdx);
			throw e;
		}
	}

	static class AddEdgesIgnoreIdsIndexSet extends AbstractIntSet implements IEdgeSet {

		private final EdgeSet<Integer, Integer> idSet;
		private final IndexIntIdMap viMap;

		AddEdgesIgnoreIdsIndexSet(EdgeSet<Integer, Integer> idSet, IndexIntIdMap viMap) {
			this.idSet = idSet;
			this.viMap = viMap;
		}

		@Override
		public int size() {
			return idSet.size();
		}

		@Override
		public IEdgeIter iterator() {
			return new IEdgeIter() {

				EdgeIter<Integer, Integer> idIter = idSet.iterator();

				@Override
				public boolean hasNext() {
					return idIter.hasNext();
				}

				@Override
				public int nextInt() {
					idIter.next();
					return -1; /* ignore edges IDs */
				}

				@Override
				public int peekNextInt() {
					idIter.peekNext();
					return -1; /* ignore edges IDs */
				}

				@Override
				public int sourceInt() {
					return viMap.idToIndex(idIter.source().intValue());
				}

				@Override
				public int targetInt() {
					return viMap.idToIndex(idIter.target().intValue());
				}
			};
		}
	}

	@Override
	public void removeEdge(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		indexGraph.removeEdge(eIdx);
	}

	@Override
	public void removeEdges(Collection<? extends Integer> edges) {
		@SuppressWarnings("unchecked")
		IntCollection eIdxs = IndexIdMaps.idToIndexCollection((Collection<Integer>) edges, eiMap);
		indexGraph.removeEdges(eIdxs);
	}

	@Override
	public void removeEdgesOf(int source) {
		int uIdx = viMap.idToIndex(source);
		indexGraph.removeEdgesOf(uIdx);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		indexGraph.removeOutEdgesOf(viMap.idToIndex(source));
	}

	@Override
	public void removeInEdgesOf(int target) {
		indexGraph.removeInEdgesOf(viMap.idToIndex(target));
	}

	@Override
	public void renameEdge(int edge, int newId) {
		if (newId < 0)
			throw new IllegalArgumentException("Edge must be non negative");
		eiMap.renameId(edge, newId);
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		indexGraph.moveEdge(eiMap.idToIndex(edge), viMap.idToIndex(newSource), viMap.idToIndex(newTarget));
	}

	@Override
	public int edgeSource(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		int uIdx = indexGraph.edgeSource(eIdx);
		return viMap.indexToIdInt(uIdx);
	}

	@Override
	public int edgeTarget(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		int vIdx = indexGraph.edgeTarget(eIdx);
		return viMap.indexToIdInt(vIdx);
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		int eIdx = eiMap.idToIndex(edge);
		int endpointIdx = viMap.idToIndex(endpoint);
		int resIdx = indexGraph.edgeEndpoint(eIdx, endpointIdx);
		return viMap.indexToIdInt(resIdx);
	}

	@Override
	public void clear() {
		indexGraph.clear();
		viMap.idsClear();
		eiMap.idsClear();
	}

	@Override
	public void clearEdges() {
		indexGraph.clearEdges();
		eiMap.idsClear();
	}

	@Override
	public void ensureVertexCapacity(int vertexCapacity) {
		indexGraph.ensureVertexCapacity(vertexCapacity);
		viMap.ensureCapacity(vertexCapacity);
	}

	@Override
	public void ensureEdgeCapacity(int edgeCapacity) {
		indexGraph.ensureEdgeCapacity(edgeCapacity);
		eiMap.ensureCapacity(edgeCapacity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.getVerticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.IntMapped.newInstance(iw, indexGraphVerticesMap()));
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		return indexGraph.getVerticesWeightsKeys();
	}

	@Override
	public void removeVerticesWeights(String key) {
		indexGraph.removeVerticesWeights(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.getEdgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.IntMapped.newInstance(iw, indexGraphEdgesMap()));
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		indexGraph.addVerticesWeights(key, type, defVal);
		return getVerticesWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
		indexGraph.addEdgesWeights(key, type, defVal);
		return getEdgesWeights(key);
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return indexGraph.getEdgesWeightsKeys();
	}

	@Override
	public void removeEdgesWeights(String key) {
		indexGraph.removeEdgesWeights(key);
	}

	static class IndexToIdEdgeSet extends AbstractIntSet implements IEdgeSet {

		private final IEdgeSet indexSet;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IndexToIdEdgeSet(IEdgeSet indexSet, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			this.indexSet = Objects.requireNonNull(indexSet);
			this.viMap = viMap;
			this.eiMap = eiMap;
		}

		@Override
		public boolean remove(int edge) {
			return indexSet.remove(eiMap.idToIndexIfExist(edge));
		}

		@Override
		public boolean contains(int edge) {
			return indexSet.contains(eiMap.idToIndexIfExist(edge));
		}

		@Override
		public int size() {
			return indexSet.size();
		}

		@Override
		public boolean isEmpty() {
			return indexSet.isEmpty();
		}

		@Override
		public void clear() {
			indexSet.clear();
		}

		@Override
		public IEdgeIter iterator() {
			return new IndexToIdEdgeIter(indexSet.iterator(), viMap, eiMap);
		}
	}

	static class IndexToIdEdgeIter implements IEdgeIter {

		private final IEdgeIter indexIter;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IndexToIdEdgeIter(IEdgeIter indexIter, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			this.indexIter = indexIter;
			this.viMap = viMap;
			this.eiMap = eiMap;
		}

		@Override
		public boolean hasNext() {
			return indexIter.hasNext();
		}

		@Override
		public int nextInt() {
			int eIdx = indexIter.nextInt();
			return eiMap.indexToIdInt(eIdx);
		}

		@Override
		public int peekNextInt() {
			int eIdx = indexIter.peekNextInt();
			return eiMap.indexToIdInt(eIdx);
		}

		@Override
		public void remove() {
			indexIter.remove();
		}

		@Override
		public int targetInt() {
			int vIdx = indexIter.targetInt();
			return viMap.indexToIdInt(vIdx);
		}

		@Override
		public int sourceInt() {
			int uIdx = indexIter.sourceInt();
			return viMap.indexToIdInt(uIdx);
		}
	}

	@Override
	public boolean isDirected() {
		return indexGraph.isDirected();
	}

	@Override
	public boolean isAllowSelfEdges() {
		return indexGraph.isAllowSelfEdges();
	}

	@Override
	public boolean isAllowParallelEdges() {
		return indexGraph.isAllowParallelEdges();
	}

	static class Factory implements IntGraphFactory {
		private final IndexGraphFactoryImpl factory;

		Factory(boolean directed) {
			this.factory = new IndexGraphFactoryImpl(directed);
		}

		@Override
		public IntGraph newGraph() {
			return new IntGraphImpl(factory.newGraph(), factory.expectedVerticesNum, factory.expectedEdgesNum);
		}

		@Override
		public IntGraph newCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
			return new IntGraphImpl(g, factory, copyVerticesWeights, copyEdgesWeights);
		}

		@Override
		public IntGraphBuilder newBuilder() {
			return new IntGraphBuilderImpl(factory.newBuilder());
		}

		@Override
		public IntGraphBuilder newBuilderCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights,
				boolean copyEdgesWeights) {
			return new IntGraphBuilderImpl(factory, g, copyVerticesWeights, copyEdgesWeights);
		}

		@Override
		public IntGraphFactory allowSelfEdges(boolean selfEdges) {
			factory.allowSelfEdges(selfEdges);
			return this;
		}

		@Override
		public IntGraphFactory allowParallelEdges(boolean parallelEdges) {
			factory.allowParallelEdges(parallelEdges);
			return this;
		}

		@Override
		public IntGraphFactory expectedVerticesNum(int expectedVerticesNum) {
			factory.expectedVerticesNum(expectedVerticesNum);
			return this;
		}

		@Override
		public IntGraphFactory expectedEdgesNum(int expectedEdgesNum) {
			factory.expectedEdgesNum(expectedEdgesNum);
			return this;
		}

		@Override
		public IntGraphFactory addHint(GraphFactory.Hint hint) {
			factory.addHint(hint);
			return this;
		}

		@Override
		public IntGraphFactory removeHint(GraphFactory.Hint hint) {
			factory.removeHint(hint);
			return this;
		}

		@Override
		public IntGraphFactory setOption(String key, Object value) {
			factory.setOption(key, value);
			return this;
		}
	}

}
