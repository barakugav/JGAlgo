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
		int vIdx = indexGraph.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = indexGraph.addVertex();
		assert vIdx == vIdx2;
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
		if (vertices.isEmpty())
			return;
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
		int duplicateVertex = -1;
		for (int vertex : vertices) {
			boolean added = viMap.addIdIfNotDuplicate(vertex, nextIdx);
			if (!added) {
				duplicateVertex = vertex;
				break;
			}
			nextIdx++;
		}
		if (duplicateVertex >= 0) {
			for (; nextIdx-- > verticesNumBefore;)
				viMap.rollBackRemove(nextIdx);
			throw new IllegalArgumentException("Duplicate vertex: " + duplicateVertex);
		}
		indexGraph.addVertices(range(verticesNumBefore, nextIdx));
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
