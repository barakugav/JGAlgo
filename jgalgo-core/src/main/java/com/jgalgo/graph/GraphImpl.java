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

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import com.jgalgo.JGAlgoConfig;
import com.jgalgo.graph.IdStrategy.IdAddRemoveListener;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

abstract class GraphImpl extends GraphBase {

	final IndexGraphImpl indexGraph;
	final IdIdxMapImpl viMap;
	final IdIdxMapImpl eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.Mapped<?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.Mapped<?>> edgesWeights = new IdentityHashMap<>();

	GraphImpl(IndexGraph indexGraph, IndexIdMap viMap, IndexIdMap eiMap) {
		this.indexGraph = (IndexGraphImpl) indexGraph;
		if (viMap == null) {
			this.viMap = IdIdxMapImpl.newInstance(this.indexGraph.getVerticesIdStrategy());
		} else {
			this.viMap = IdIdxMapImpl.copyOf(viMap, this.indexGraph.getVerticesIdStrategy());
		}
		if (viMap == null) {
			this.eiMap = IdIdxMapImpl.newInstance(this.indexGraph.getEdgesIdStrategy());
		} else {
			this.eiMap = IdIdxMapImpl.copyOf(eiMap, this.indexGraph.getEdgesIdStrategy());
		}
	}

	GraphImpl(IndexGraph g) {
		this(g, null, null);
		assert indexGraph.getVerticesIdStrategy().size() == 0;
		assert indexGraph.getEdgesIdStrategy().size() == 0;
	}

	/* copy constructor */
	GraphImpl(Graph orig, IndexGraphFactory indexGraphFactory) {
		this(indexGraphFactory.newCopyOf(orig.indexGraph()), orig.indexGraphVerticesMap(), orig.indexGraphEdgesMap());
	}

	@Override
	public IndexGraph indexGraph() {
		return indexGraph;
	}

	@Override
	public IndexIdMap indexGraphVerticesMap() {
		return viMap;
	}

	@Override
	public IndexIdMap indexGraphEdgesMap() {
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
		int uIdx = indexGraph.addVertex();
		return viMap.indexToId(uIdx);
	}

	@Override
	public void addVertex(int vertex) {
		if (vertex < 0)
			throw new IllegalArgumentException("User chosen vertex ID must be non negative: " + vertex);
		if (vertices().contains(vertex))
			throw new IllegalArgumentException("Graph already contain a vertex with the specified ID: " + vertex);
		/* The listener of new IDs will be called by the index graph implementation, and the user ID will be used */
		viMap.userChosenId = vertex;
		indexGraph.addVertex();
	}

	@Override
	public void removeVertex(int vertex) {
		int vIdx = viMap.idToIndex(vertex);
		indexGraph.removeVertex(vIdx);
	}

	@Override
	public EdgeSet outEdges(int source) {
		return new EdgeSetMapped(indexGraph.outEdges(viMap.idToIndex(source)));
	}

	@Override
	public EdgeSet inEdges(int target) {
		return new EdgeSetMapped(indexGraph.inEdges(viMap.idToIndex(target)));
	}

	@Override
	public int getEdge(int source, int target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.getEdge(uIdx, vIdx);
		return eIdx == -1 ? -1 : eiMap.indexToId(eIdx);
	}

	@Override
	public EdgeSet getEdges(int source, int target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		EdgeSet s = indexGraph.getEdges(uIdx, vIdx);
		return new EdgeSetMapped(s);
	}

	@Override
	public int addEdge(int source, int target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.addEdge(uIdx, vIdx);
		return eiMap.indexToId(eIdx);
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		if (edge < 0)
			throw new IllegalArgumentException("User chosen edge ID must be non negative: " + edge);
		if (edges().contains(edge))
			throw new IllegalArgumentException("Graph already contain a edge with the specified ID: " + edge);
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		/* The listener of new IDs will be called by the index graph implementation, and the user ID will be used */
		eiMap.userChosenId = edge;
		indexGraph.addEdge(uIdx, vIdx);
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
	public int edgeSource(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		int uIdx = indexGraph.edgeSource(eIdx);
		return viMap.indexToId(uIdx);
	}

	@Override
	public int edgeTarget(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		int vIdx = indexGraph.edgeTarget(eIdx);
		return viMap.indexToId(vIdx);
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		int eIdx = eiMap.idToIndex(edge);
		int endpointIdx = viMap.idToIndex(endpoint);
		int resIdx = indexGraph.edgeEndpoint(eIdx, endpointIdx);
		return viMap.indexToId(resIdx);
	}

	@Override
	public void clear() {
		indexGraph.clear();
	}

	@Override
	public void clearEdges() {
		indexGraph.clearEdges();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
		proneRemovedVerticesWeights();
		WeightsImpl.Index<V> indexWeights = indexGraph.getVerticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.Mapped.newInstance(iw, indexGraphVerticesMap()));
	}

	@Override
	public Set<Object> getVerticesWeightsKeys() {
		proneRemovedVerticesWeights();
		return indexGraph.getVerticesWeightsKeys();
	}

	@Override
	public void removeVerticesWeights(Object key) {
		indexGraph.removeVerticesWeights(key);
		proneRemovedVerticesWeights();
	}

	private void proneRemovedVerticesWeights() {
		List<Weights<?>> indexWeights = new ReferenceArrayList<>();
		for (Object key : indexGraph.getVerticesWeightsKeys())
			indexWeights.add(indexGraph.getVerticesWeights(key));
		verticesWeights.keySet().removeIf(w -> !indexWeights.contains(w));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
		proneRemovedEdgesWeights();
		WeightsImpl.Index<E> indexWeights = indexGraph.getEdgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.Mapped.newInstance(iw, indexGraphEdgesMap()));
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		proneRemovedVerticesWeights();
		indexGraph.addVerticesWeights(key, type, defVal);
		return getVerticesWeights(key);
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		proneRemovedEdgesWeights();
		indexGraph.addEdgesWeights(key, type, defVal);
		return getEdgesWeights(key);
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		proneRemovedEdgesWeights();
		return indexGraph.getEdgesWeightsKeys();
	}

	@Override
	public void removeEdgesWeights(Object key) {
		indexGraph.removeEdgesWeights(key);
		proneRemovedEdgesWeights();
	}

	private void proneRemovedEdgesWeights() {
		List<Weights<?>> indexWeights = new ReferenceArrayList<>();
		for (Object key : indexGraph.getEdgesWeightsKeys())
			indexWeights.add(indexGraph.getEdgesWeights(key));
		edgesWeights.keySet().removeIf(w -> !indexWeights.contains(w));
	}

	class EdgeSetMapped extends AbstractIntSet implements EdgeSet {

		private final EdgeSet set;

		EdgeSetMapped(EdgeSet set) {
			this.set = Objects.requireNonNull(set);
		}

		@Override
		public boolean remove(int edge) {
			int eIdx = eiMap.idToIndex(edge);
			return set.remove(eIdx);
		}

		@Override
		public boolean contains(int edge) {
			int eIdx = eiMap.idToIndex(edge);
			return set.contains(eIdx);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public void clear() {
			set.clear();
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterMapped(set.iterator());
		}
	}

	class EdgeIterMapped implements EdgeIter {

		private final EdgeIter it;

		EdgeIterMapped(EdgeIter it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			int eIdx = it.nextInt();
			return eiMap.indexToId(eIdx);
		}

		@Override
		public int peekNext() {
			int eIdx = it.peekNext();
			return eiMap.indexToId(eIdx);
		}

		@Override
		public void remove() {
			it.remove();
		}

		@Override
		public int target() {
			int vIdx = it.target();
			return viMap.indexToId(vIdx);
		}

		@Override
		public int source() {
			int uIdx = it.source();
			return viMap.indexToId(uIdx);
		}
	}

	@Override
	public GraphCapabilities getCapabilities() {
		return indexGraph.getCapabilities();
	}

	static class Directed extends GraphImpl {

		Directed(IndexGraph indexGraph, IndexIdMap viMap, IndexIdMap eiMap) {
			super(indexGraph, viMap, eiMap);
			Assertions.Graphs.onlyDirected(indexGraph);
		}

		Directed(IndexGraph indexGraph) {
			super(indexGraph);
			Assertions.Graphs.onlyDirected(indexGraph);
		}

		/* copy constructor */
		Directed(Graph orig, IndexGraphFactory indexGraphFactory) {
			super(orig, indexGraphFactory);
			Assertions.Graphs.onlyDirected(orig);
			Assertions.Graphs.onlyDirected(indexGraph);
		}

		@Override
		public void reverseEdge(int edge) {
			int eIdx = eiMap.idToIndex(edge);
			indexGraph.reverseEdge(eIdx);
		}
	}

	static class Undirected extends GraphImpl {

		Undirected(IndexGraph indexGraph, IndexIdMap viMap, IndexIdMap eiMap) {
			super(indexGraph, viMap, eiMap);
			Assertions.Graphs.onlyUndirected(indexGraph);
		}

		Undirected(IndexGraph indexGraph) {
			super(indexGraph);
			Assertions.Graphs.onlyUndirected(indexGraph);
		}

		/* copy constructor */
		Undirected(Graph orig, IndexGraphFactory indexGraphFactory) {
			super(orig, indexGraphFactory);
			Assertions.Graphs.onlyUndirected(orig);
			Assertions.Graphs.onlyUndirected(indexGraph);
		}

		@Override
		public void reverseEdge(int edge) {
			int eIdx = eiMap.idToIndex(edge);
			indexGraph.reverseEdge(eIdx);
		}
	}

	private abstract static class IdIdxMapImpl implements IndexIdMap {

		private final Int2IntOpenHashMap idToIndex;
		private final IntSet idsView; // TODO move to graph abstract implementation
		private final WeightsImpl.Index.Int indexToId;
		private int userChosenId = -1;

		IdIdxMapImpl(IdStrategy idStrat) {
			idToIndex = new Int2IntOpenHashMap();
			idToIndex.defaultReturnValue(-1);
			idsView = IntSets.unmodifiable(idToIndex.keySet());
			indexToId = new WeightsImpl.IndexMutable.Int(idStrat, -1);
			initListeners(idStrat);
		}

		IdIdxMapImpl(IndexIdMap orig, IdStrategy idStrat) {
			if (orig instanceof IdIdxMapImpl) {
				IdIdxMapImpl orig0 = (IdIdxMapImpl) orig;
				idToIndex = new Int2IntOpenHashMap(orig0.idToIndex);
				idToIndex.defaultReturnValue(-1);
				indexToId = new WeightsImpl.IndexMutable.Int(orig0.indexToId, idStrat);
			} else {
				idToIndex = new Int2IntOpenHashMap(idStrat.size());
				idToIndex.defaultReturnValue(-1);
				indexToId = new WeightsImpl.IndexMutable.Int(idStrat, -1);
				if (idStrat.size() > 0) {
					((WeightsImpl.IndexMutable.Int) indexToId).expand(idStrat.size());
					for (int idx : idStrat.indices()) {
						int id = orig.indexToId(idx);
						if (indexToId.getInt(idx) != -1)
							throw new IllegalArgumentException("duplicate index: " + idx);
						if (id < 0)
							throw new IllegalArgumentException("negative id: " + id);
						indexToId.set(idx, id);

						int oldIdx = idToIndex.put(id, idx);
						if (oldIdx != -1)
							throw new IllegalArgumentException("duplicate id: " + id);
					}
				}
			}
			idsView = IntSets.unmodifiable(idToIndex.keySet());
			initListeners(idStrat);
		}

		private static Supplier<Boolean> IsGraphIdRandom =
				Objects.requireNonNull(JGAlgoConfig.getOption("GraphIdRandom"));

		private static boolean isGraphIdRandom() {
			return IsGraphIdRandom.get().booleanValue();
		}

		static IdIdxMapImpl newInstance(IdStrategy idStrat) {
			return isGraphIdRandom() ? new GraphImpl.IdIdxMapRand(idStrat) : new GraphImpl.IdIdxMapCounter(idStrat);
		}

		static IdIdxMapImpl copyOf(IndexIdMap orig, IdStrategy idStrat) {
			return isGraphIdRandom() ? new GraphImpl.IdIdxMapRand(orig, idStrat)
					: new GraphImpl.IdIdxMapCounter(orig, idStrat);
		}

		private void initListeners(IdStrategy idStrat) {
			idStrat.addIdSwapListener((idx1, idx2) -> {
				int id1 = indexToId.getInt(idx1);
				int id2 = indexToId.getInt(idx2);
				indexToId.set(idx1, id2);
				indexToId.set(idx2, id1);
				int oldIdx1 = idToIndex.put(id1, idx2);
				int oldIdx2 = idToIndex.put(id2, idx1);
				assert idx1 == oldIdx1;
				assert idx2 == oldIdx2;
			});
			idStrat.addIdAddRemoveListener(new IdAddRemoveListener() {

				WeightsImpl.IndexMutable.Int indexToId() {
					return (WeightsImpl.IndexMutable.Int) indexToId;
				}

				@Override
				public void idRemove(int idx) {
					final int id = indexToId.getInt(idx);
					indexToId().clear(idx);
					idToIndex.remove(id);
				}

				@Override
				public void idAdd(int idx) {
					assert idx == idToIndex.size();
					int id = userChosenId != -1 ? userChosenId : nextID();
					assert id >= 0;

					int oldIdx = idToIndex.put(id, idx);
					assert oldIdx == -1;

					if (idx == indexToId().capacity())
						indexToId().expand(Math.max(2, 2 * indexToId().capacity()));
					indexToId.set(idx, id);

					userChosenId = -1;
				}

				@Override
				public void idsClear() {
					idToIndex.clear();
					indexToId().clear();
				}
			});
		}

		abstract int nextID();

		@Override
		public int indexToId(int index) {
			return indexToId.getInt(index);
		}

		@Override
		public int idToIndex(int id) {
			int idx = idToIndex.get(id);
			if (idx < 0)
				throw new IndexOutOfBoundsException(id);
			return idx;
		}

		IntSet idSet() {
			return idsView;
		}

	}

	private static class IdIdxMapCounter extends IdIdxMapImpl {

		private int counter;

		IdIdxMapCounter(IdStrategy idStrat) {
			super(idStrat);
			// We prefer non zero IDs because fastutil handle zero (null) keys separately
			counter = 1;
		}

		IdIdxMapCounter(IndexIdMap orig, IdStrategy idStrat) {
			super(orig, idStrat);
			if (orig instanceof IdIdxMapCounter) {
				counter = ((IdIdxMapCounter) orig).counter;
			} else {
				counter = 1;
			}
		}

		@Override
		int nextID() {
			for (;;) {
				int id = counter++;
				if (!idSet().contains(id))
					return id;
			}
		}
	}

	private static class IdIdxMapRand extends IdIdxMapImpl {

		private final Random rand = new Random();

		IdIdxMapRand(IdStrategy idStrat) {
			super(idStrat);
		}

		IdIdxMapRand(IndexIdMap orig, IdStrategy idStrat) {
			super(orig, idStrat);
		}

		@Override
		int nextID() {
			for (;;) {
				int id = rand.nextInt();
				if (id >= 1 && !idSet().contains(id))
					// We prefer non zero IDs because fastutil handle zero (null) keys separately
					return id;
			}
		}
	}

	static class Factory implements GraphFactory {
		private final IndexGraphFactory factory;

		Factory(boolean directed) {
			this.factory = new IndexGraphFactoryImpl(directed);
		}

		Factory(Graph g) {
			this.factory = new IndexGraphFactoryImpl(g.indexGraph());
		}

		@Override
		public Graph newGraph() {
			IndexGraph indexGraph = factory.newGraph();
			if (indexGraph.getCapabilities().directed()) {
				return new GraphImpl.Directed(indexGraph);
			} else {
				return new GraphImpl.Undirected(indexGraph);
			}
		}

		@Override
		public Graph newCopyOf(Graph g) {
			if (g.getCapabilities().directed()) {
				return new GraphImpl.Directed(g, factory);
			} else {
				return new GraphImpl.Undirected(g, factory);
			}
		}

		@Override
		public GraphFactory setDirected(boolean directed) {
			factory.setDirected(directed);
			return this;
		}

		@Override
		public GraphFactory allowSelfEdges(boolean selfEdges) {
			factory.allowSelfEdges(selfEdges);
			return this;
		}

		@Override
		public GraphFactory allowParallelEdges(boolean parallelEdges) {
			factory.allowParallelEdges(parallelEdges);
			return this;
		}

		@Override
		public GraphFactory expectedVerticesNum(int expectedVerticesNum) {
			factory.expectedVerticesNum(expectedVerticesNum);
			return this;
		}

		@Override
		public GraphFactory expectedEdgesNum(int expectedEdgesNum) {
			factory.expectedEdgesNum(expectedEdgesNum);
			return this;
		}

		@Override
		public GraphFactory addHint(GraphFactory.Hint hint) {
			factory.addHint(hint);
			return this;
		}

		@Override
		public GraphFactory removeHint(GraphFactory.Hint hint) {
			factory.removeHint(hint);
			return this;
		}

		@Override
		public GraphFactory setOption(String key, Object value) {
			factory.setOption(key, value);
			return this;
		}
	}

}
