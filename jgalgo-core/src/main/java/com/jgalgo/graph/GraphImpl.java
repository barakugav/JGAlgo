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
import com.jgalgo.graph.GraphElementSet.IdAddRemoveListener;
import com.jgalgo.internal.JGAlgoConfigImpl;
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

	GraphImpl(IndexGraph g, int expectedVerticesNum, int expectedEdgesNum) {
		assert g.vertices().isEmpty();
		assert g.edges().isEmpty();

		indexGraph = (IndexGraphImpl) g;
		viMap = IdIdxMapImpl.newInstance(indexGraph.vertices(), expectedVerticesNum, false);
		eiMap = IdIdxMapImpl.newInstance(indexGraph.edges(), expectedEdgesNum, true);
	}

	GraphImpl(IndexGraph indexGraph, IndexIdMap viMap, IndexIdMap eiMap) {
		this.indexGraph = (IndexGraphImpl) Objects.requireNonNull(indexGraph);
		this.viMap = IdIdxMapImpl.copyOf(viMap, this.indexGraph.vertices(), false);
		this.eiMap = IdIdxMapImpl.copyOf(eiMap, this.indexGraph.edges(), true);
	}

	/* copy constructor */
	GraphImpl(Graph orig, IndexGraphFactory indexGraphFactory, boolean copyWeights) {
		this(indexGraphFactory.newCopyOf(orig.indexGraph(), copyWeights), orig.indexGraphVerticesMap(),
				orig.indexGraphEdgesMap());
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
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(String key) {
		proneRemovedVerticesWeights();
		WeightsImpl.Index<V> indexWeights = indexGraph.getVerticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.Mapped.newInstance(iw, indexGraphVerticesMap()));
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		proneRemovedVerticesWeights();
		return indexGraph.getVerticesWeightsKeys();
	}

	@Override
	public void removeVerticesWeights(String key) {
		indexGraph.removeVerticesWeights(key);
		proneRemovedVerticesWeights();
	}

	private void proneRemovedVerticesWeights() {
		List<Weights<?>> indexWeights = new ReferenceArrayList<>();
		for (String key : indexGraph.getVerticesWeightsKeys())
			indexWeights.add(indexGraph.getVerticesWeights(key));
		verticesWeights.keySet().removeIf(w -> !indexWeights.contains(w));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(String key) {
		proneRemovedEdgesWeights();
		WeightsImpl.Index<E> indexWeights = indexGraph.getEdgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.Mapped.newInstance(iw, indexGraphEdgesMap()));
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(String key, Class<? super V> type, V defVal) {
		proneRemovedVerticesWeights();
		indexGraph.addVerticesWeights(key, type, defVal);
		return getVerticesWeights(key);
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(String key, Class<? super E> type, E defVal) {
		proneRemovedEdgesWeights();
		indexGraph.addEdgesWeights(key, type, defVal);
		return getEdgesWeights(key);
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		proneRemovedEdgesWeights();
		return indexGraph.getEdgesWeightsKeys();
	}

	@Override
	public void removeEdgesWeights(String key) {
		indexGraph.removeEdgesWeights(key);
		proneRemovedEdgesWeights();
	}

	private void proneRemovedEdgesWeights() {
		List<Weights<?>> indexWeights = new ReferenceArrayList<>();
		for (String key : indexGraph.getEdgesWeightsKeys())
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

	static class Directed extends GraphImpl {

		Directed(IndexGraph indexGraph, int expectedVerticesNum, int expectedEdgesNum) {
			super(indexGraph, expectedVerticesNum, expectedEdgesNum);
			Assertions.Graphs.onlyDirected(indexGraph);
		}

		Directed(IndexGraph indexGraph, IndexIdMap viMap, IndexIdMap eiMap) {
			super(indexGraph, viMap, eiMap);
			Assertions.Graphs.onlyDirected(indexGraph);
		}

		/* copy constructor */
		Directed(Graph orig, IndexGraphFactory indexGraphFactory, boolean copyWeights) {
			super(orig, indexGraphFactory, copyWeights);
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

		Undirected(IndexGraph indexGraph, int expectedVerticesNum, int expectedEdgesNum) {
			super(indexGraph, expectedVerticesNum, expectedEdgesNum);
			Assertions.Graphs.onlyUndirected(indexGraph);
		}

		/* copy constructor */
		Undirected(Graph orig, IndexGraphFactory indexGraphFactory, boolean copyWeights) {
			super(orig, indexGraphFactory, copyWeights);
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
		private final WeightsImplInt.IndexImpl indexToId;
		private int userChosenId = -1;
		private final boolean isEdges;

		IdIdxMapImpl(GraphElementSet elements, int expectedSize, boolean isEdges) {
			idToIndex = new Int2IntOpenHashMap(expectedSize);
			idToIndex.defaultReturnValue(-1);
			idsView = IntSets.unmodifiable(idToIndex.keySet());
			indexToId = new WeightsImplInt.IndexMutable(elements, -1);
			this.isEdges = isEdges;
			initListeners(elements);
		}

		IdIdxMapImpl(IndexIdMap orig, GraphElementSet elements, boolean isEdges) {
			if (orig instanceof IdIdxMapImpl) {
				IdIdxMapImpl orig0 = (IdIdxMapImpl) orig;
				idToIndex = new Int2IntOpenHashMap(orig0.idToIndex);
				idToIndex.defaultReturnValue(-1);
				indexToId = new WeightsImplInt.IndexMutable(orig0.indexToId, elements);
			} else {
				idToIndex = new Int2IntOpenHashMap(elements.size());
				idToIndex.defaultReturnValue(-1);
				indexToId = new WeightsImplInt.IndexMutable(elements, -1);
				if (elements.size() > 0) {
					((WeightsImplInt.IndexMutable) indexToId).expand(elements.size());
					for (int idx : elements) {
						int id = orig.indexToId(idx);
						if (indexToId.get(idx) != -1)
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
			this.isEdges = isEdges;
			idsView = IntSets.unmodifiable(idToIndex.keySet());
			initListeners(elements);
		}

		static IdIdxMapImpl newInstance(GraphElementSet elements, int expectedSize, boolean isEdges) {
			return JGAlgoConfigImpl.GraphIdRandom ? new GraphImpl.IdIdxMapRand(elements, expectedSize, isEdges)
					: new GraphImpl.IdIdxMapCounter(elements, expectedSize, isEdges);
		}

		static IdIdxMapImpl copyOf(IndexIdMap orig, GraphElementSet elements, boolean isEdges) {
			return JGAlgoConfigImpl.GraphIdRandom ? new GraphImpl.IdIdxMapRand(orig, elements, isEdges)
					: new GraphImpl.IdIdxMapCounter(orig, elements, isEdges);
		}

		private void initListeners(GraphElementSet elements) {
			elements.addIdSwapListener((idx1, idx2) -> {
				int id1 = indexToId.get(idx1);
				int id2 = indexToId.get(idx2);
				indexToId.set(idx1, id2);
				indexToId.set(idx2, id1);
				int oldIdx1 = idToIndex.put(id1, idx2);
				int oldIdx2 = idToIndex.put(id2, idx1);
				assert idx1 == oldIdx1;
				assert idx2 == oldIdx2;
			});
			elements.addIdAddRemoveListener(new IdAddRemoveListener() {

				WeightsImplInt.IndexMutable indexToId() {
					return (WeightsImplInt.IndexMutable) indexToId;
				}

				@Override
				public void idRemove(int idx) {
					final int id = indexToId.get(idx);
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
			return indexToId.get(index);
		}

		@Override
		public int idToIndex(int id) {
			int idx = idToIndex.get(id);
			if (idx < 0)
				throw new IndexOutOfBoundsException("No such " + (isEdges ? "edge" : "vertex") + ": " + id);
			return idx;
		}

		IntSet idSet() {
			return idsView;
		}

	}

	private static class IdIdxMapCounter extends IdIdxMapImpl {

		private int counter;

		IdIdxMapCounter(GraphElementSet elements, int expectedSize, boolean isEdges) {
			super(elements, expectedSize, isEdges);
			// We prefer non zero IDs because fastutil handle zero (null) keys separately
			counter = 1;
		}

		IdIdxMapCounter(IndexIdMap orig, GraphElementSet elements, boolean isEdges) {
			super(orig, elements, isEdges);
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

		IdIdxMapRand(GraphElementSet elements, int expectedSize, boolean isEdges) {
			super(elements, expectedSize, isEdges);
		}

		IdIdxMapRand(IndexIdMap orig, GraphElementSet elements, boolean isEdges) {
			super(orig, elements, isEdges);
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
		private final IndexGraphFactoryImpl factory;

		Factory(boolean directed) {
			this.factory = new IndexGraphFactoryImpl(directed);
		}

		Factory(Graph g) {
			this.factory = new IndexGraphFactoryImpl(g.indexGraph());
		}

		@Override
		public Graph newGraph() {
			IndexGraph indexGraph = factory.newGraph();
			if (indexGraph.isDirected()) {
				return new GraphImpl.Directed(indexGraph, factory.expectedVerticesNum, factory.expectedEdgesNum);
			} else {
				return new GraphImpl.Undirected(indexGraph, factory.expectedVerticesNum, factory.expectedEdgesNum);
			}
		}

		@Override
		public Graph newCopyOf(Graph g, boolean copyWeights) {
			if (g.isDirected()) {
				return new GraphImpl.Directed(g, factory, copyWeights);
			} else {
				return new GraphImpl.Undirected(g, factory, copyWeights);
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
