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

import java.util.Arrays;
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
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntSet;

class IntGraphImpl extends GraphBase<Integer, Integer> implements IntGraph {

	final IndexGraph indexGraph;
	final IdIdxMapImpl viMap;
	final IdIdxMapImpl eiMap;
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
		assert g.vertices().isEmpty();
		assert g.edges().isEmpty();

		indexGraph = g;
		viMap = IdIdxMapImpl.newInstance(indexGraph, expectedVerticesNum, false);
		eiMap = IdIdxMapImpl.newInstance(indexGraph, expectedEdgesNum, true);
	}

	IntGraphImpl(IndexGraph indexGraph, IndexIdMap<Integer> viMap, IndexIdMap<Integer> eiMap,
			IndexGraphBuilder.ReIndexingMap vReIndexing, IndexGraphBuilder.ReIndexingMap eReIndexing) {
		this.indexGraph = Objects.requireNonNull(indexGraph);
		this.viMap = IdIdxMapImpl.reindexedCopyOf(viMap, vReIndexing, this.indexGraph, false);
		this.eiMap = IdIdxMapImpl.reindexedCopyOf(eiMap, eReIndexing, this.indexGraph, true);
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
	public int addVertex() {
		int id = vIdStrategy.applyAsInt(vertices());
		int vIdx = indexGraph.addVertex();
		viMap.addId(id, vIdx);
		return id;
	}

	@Override
	public void addVertex(int vertex) {
		if (vertex < 0)
			throw new IllegalArgumentException("Vertex must be non negative");
		if (vertices().contains(vertex))
			throw new IllegalArgumentException("Graph already contain such a vertex: " + vertex);
		int vIdx = indexGraph.addVertex();
		viMap.addId(vertex, vIdx);
	}

	@Override
	public void removeVertex(int vertex) {
		int vIdx = viMap.idToIndex(vertex);
		indexGraph.removeVertex(vIdx);
	}

	@Override
	public void renameVertex(int vertex, int newId) {
		if (newId < 0)
			throw new IllegalArgumentException("Vertex must be non negative");
		if (vertices().contains(newId))
			throw new IllegalArgumentException("Graph already contain such a vertex: " + newId);
		viMap.renameId(vertex, newId);
	}

	@Override
	public IEdgeSet outEdges(int source) {
		return new EdgeSetMapped(indexGraph.outEdges(viMap.idToIndex(source)));
	}

	@Override
	public IEdgeSet inEdges(int target) {
		return new EdgeSetMapped(indexGraph.inEdges(viMap.idToIndex(target)));
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
		IEdgeSet s = indexGraph.getEdges(uIdx, vIdx);
		return new EdgeSetMapped(s);
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
		if (edges().contains(edge))
			throw new IllegalArgumentException("Graph already contain such a edge: " + edge);
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.addEdge(uIdx, vIdx);
		eiMap.addId(edge, eIdx);
	}

	@Override
	public void removeEdge(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		indexGraph.removeEdge(eIdx);
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
		if (edges().contains(newId))
			throw new IllegalArgumentException("Graph already contain such a edge: " + newId);
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
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends IWeights<T>> WeightsT getVerticesIWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.getVerticesIWeights(key);
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
	public <T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.getEdgesIWeights(key);
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

	class EdgeSetMapped extends AbstractIntSet implements IEdgeSet {

		private final IEdgeSet set;

		EdgeSetMapped(IEdgeSet set) {
			this.set = Objects.requireNonNull(set);
		}

		@Override
		public boolean remove(int edge) {
			return set.remove(eiMap.idToIndexIfExist(edge));
		}

		@Override
		public boolean contains(int edge) {
			return set.contains(eiMap.idToIndexIfExist(edge));
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public boolean isEmpty() {
			return set.isEmpty();
		}

		@Override
		public void clear() {
			set.clear();
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterMapped(set.iterator());
		}
	}

	class EdgeIterMapped implements IEdgeIter {

		private final IEdgeIter it;

		EdgeIterMapped(IEdgeIter it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			int eIdx = it.nextInt();
			return eiMap.indexToIdInt(eIdx);
		}

		@Override
		public int peekNextInt() {
			int eIdx = it.peekNextInt();
			return eiMap.indexToIdInt(eIdx);
		}

		@Override
		public void remove() {
			it.remove();
		}

		@Override
		public int targetInt() {
			int vIdx = it.targetInt();
			return viMap.indexToIdInt(vIdx);
		}

		@Override
		public int sourceInt() {
			int uIdx = it.sourceInt();
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

	private static class IdIdxMapImpl implements IndexIntIdMap {

		private final IntSet indicesSet;
		private final IntSet idsSet = new IdSet();
		private final Int2IntOpenHashMap idToIndex;
		private int[] indexToId;
		private final boolean isEdges;
		private final boolean immutable;

		IdIdxMapImpl(IndexGraph g, int expectedSize, boolean isEdges) {
			this.indicesSet = isEdges ? g.edges() : g.vertices();
			idToIndex = new Int2IntOpenHashMap(expectedSize);
			idToIndex.defaultReturnValue(-1);
			indexToId = expectedSize == 0 ? IntArrays.DEFAULT_EMPTY_ARRAY : new int[expectedSize];
			this.isEdges = isEdges;
			immutable = false;
			initListeners(g);
		}

		IdIdxMapImpl(IndexIdMap<Integer> orig, IndexGraphBuilder.ReIndexingMap reIndexing, IndexGraph g,
				boolean isEdges) {
			this.indicesSet = isEdges ? g.edges() : g.vertices();
			int elementsSize = indicesSet.size();
			if (orig instanceof IdIdxMapImpl && reIndexing == null) {
				IdIdxMapImpl orig0 = (IdIdxMapImpl) orig;
				idToIndex = new Int2IntOpenHashMap(orig0.idToIndex);
				idToIndex.defaultReturnValue(-1);
				indexToId = Arrays.copyOf(orig0.indexToId, elementsSize);

			} else {
				idToIndex = new Int2IntOpenHashMap(elementsSize);
				idToIndex.defaultReturnValue(-1);
				if (indicesSet.isEmpty()) {
					indexToId = IntArrays.DEFAULT_EMPTY_ARRAY;
				} else {
					indexToId = new int[elementsSize];
					if (reIndexing == null) {
						for (int idx = 0; idx < elementsSize; idx++) {
							int id = orig.indexToId(idx).intValue();
							if (id < 0)
								throw new IllegalArgumentException("negative id: " + id);
							indexToId[idx] = id;

							int oldIdx = idToIndex.put(id, idx);
							if (oldIdx != -1)
								throw new IllegalArgumentException("duplicate id: " + id);
						}

					} else {
						for (int idx = 0; idx < elementsSize; idx++) {
							int id = orig.indexToId(reIndexing.reIndexedToOrig(idx)).intValue();
							if (id < 0)
								throw new IllegalArgumentException("negative id: " + id);
							indexToId[idx] = id;

							int oldIdx = idToIndex.put(id, idx);
							if (oldIdx != -1)
								throw new IllegalArgumentException("duplicate id: " + id);
						}
					}
				}
			}
			this.isEdges = isEdges;
			immutable = g instanceof ImmutableGraph;
			initListeners(g);
		}

		static IdIdxMapImpl newInstance(IndexGraph g, int expectedSize, boolean isEdges) {
			return new IdIdxMapImpl(g, expectedSize, isEdges);
		}

		static IdIdxMapImpl reindexedCopyOf(IndexIdMap<Integer> orig, IndexGraphBuilder.ReIndexingMap reIndexing,
				IndexGraph g, boolean isEdges) {
			return new IdIdxMapImpl(orig, reIndexing, g, isEdges);
		}

		private void initListeners(IndexGraph g) {
			IndexRemoveListener listener = new IndexRemoveListener() {

				@Override
				public void swapAndRemove(int removedIdx, int swappedIdx) {
					int id1 = indexToId[removedIdx];
					int id2 = indexToId[swappedIdx];
					indexToId[removedIdx] = id2;
					// indexToId[swappedIdx] = -1;
					int oldIdx1 = idToIndex.remove(id1);
					int oldIdx2 = idToIndex.put(id2, removedIdx);
					assert removedIdx == oldIdx1;
					assert swappedIdx == oldIdx2;
				}

				@Override
				public void removeLast(int removedIdx) {
					int id = indexToId[removedIdx];
					// indexToId[removedIdx] = -1;
					idToIndex.remove(id);
				}
			};
			if (isEdges) {
				g.addEdgeRemoveListener(listener);
			} else {
				g.addVertexRemoveListener(listener);
			}
		}

		void addId(int id, int idx) {
			assert id >= 0;
			assert idx == idToIndex.size();
			int oldIdx = idToIndex.put(id, idx);
			assert oldIdx == -1;

			if (idx == indexToId.length)
				indexToId = Arrays.copyOf(indexToId, Math.max(2, 2 * indexToId.length));
			indexToId[idx] = id;
		}

		void idsClear() {
			// Arrays.fill(indexToId, 0, idToIndex.size(), -1);
			idToIndex.clear();
		}

		@Override
		public int indexToIdInt(int index) {
			Assertions.Graphs.checkId(index, indicesSet.size(), isEdges);
			return indexToId[index];
		}

		@Override
		public int indexToIdIfExistInt(int index) {
			if (!(0 <= index && index < indicesSet.size()))
				return -1;
			return indexToId[index];
		}

		@Override
		public int idToIndex(int id) {
			int idx = idToIndex.get(id);
			if (idx < 0) {
				if (isEdges) {
					throw NoSuchEdgeException.ofEdge(id);
				} else {
					throw NoSuchVertexException.ofVertex(id);
				}
			}
			return idx;
		}

		@Override
		public int idToIndexIfExist(int id) {
			return idToIndex.get(id);
		}

		void renameId(int oldId, int newId) {
			if (immutable) {
				if (isEdges) {
					throw new UnsupportedOperationException("graph is immutable, cannot rename vertices");
				} else {
					throw new UnsupportedOperationException("graph is immutable, cannot rename edges");
				}
			}
			int idx = idToIndex.remove(oldId);
			if (idx < 0) {
				if (isEdges) {
					throw NoSuchEdgeException.ofEdge(oldId);
				} else {
					throw NoSuchVertexException.ofVertex(oldId);
				}
			}
			int oldIdx = idToIndex.put(newId, idx);
			assert oldIdx == -1;
			indexToId[idx] = newId;
		}

		IntSet idSet() {
			return idsSet;
		}

		private class IdSet extends AbstractIntSet {

			@Override
			public int size() {
				return indicesSet.size();
			}

			@Override
			public boolean contains(int key) {
				return idToIndex.containsKey(key);
			}

			@Override
			public boolean containsAll(IntCollection c) {
				return idToIndex.keySet().containsAll(c);
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				return idToIndex.keySet().containsAll(c);
			}

			@Override
			public IntIterator iterator() {
				return IntIterators.wrap(indexToId, 0, indicesSet.size());
			}

			@Override
			public int[] toIntArray() {
				return Arrays.copyOf(indexToId, indicesSet.size());
			}

			@Override
			public int[] toArray(int[] a) {
				int size = indicesSet.size();
				if (a.length < size)
					a = java.util.Arrays.copyOf(a, size);
				System.arraycopy(indexToId, 0, a, 0, size);
				return a;
			}
		}
	}

	static class Factory implements IntGraphFactory {
		private final IndexGraphFactoryImpl factory;

		Factory(boolean directed) {
			this.factory = new IndexGraphFactoryImpl(directed);
		}

		Factory(IntGraph g) {
			this.factory = new IndexGraphFactoryImpl(g.indexGraph());
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
		public IntGraphFactory setDirected(boolean directed) {
			factory.setDirected(directed);
			return this;
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
