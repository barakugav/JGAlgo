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
package com.jgalgo;

import java.util.Objects;
import java.util.Random;
import java.util.Set;
import com.jgalgo.IdStrategyImpl.IdAddRemoveListener;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

abstract class GraphImpl extends GraphBase {

	final GraphBaseIndex indexGraph;
	final IdIdxMapImpl viMap;
	final IdIdxMapImpl eiMap;
	private final WeakIdentityHashMap<WeightsImpl.Index<?>, WeightsImpl.Mapped<?>> verticesWeights =
			new WeakIdentityHashMap<>();
	private final WeakIdentityHashMap<WeightsImpl.Index<?>, WeightsImpl.Mapped<?>> edgesWeights =
			new WeakIdentityHashMap<>();

	GraphImpl(GraphBaseIndex g, boolean rand) {
		this.indexGraph = Objects.requireNonNull(g);
		if (rand) {
			viMap = new GraphImpl.IdIdxMapRand(g.verticesIdStrat);
			eiMap = new GraphImpl.IdIdxMapRand(g.edgesIdStrat);
		} else {
			viMap = new GraphImpl.IdIdxMapCounter(g.verticesIdStrat);
			eiMap = new GraphImpl.IdIdxMapCounter(g.edgesIdStrat);
		}
	}

	GraphImpl(GraphImpl orig) {
		indexGraph = (GraphBaseIndex) orig.indexGraph.copy();
		viMap = orig.viMap.copy(indexGraph.verticesIdStrat);
		eiMap = orig.eiMap.copy(indexGraph.edgesIdStrat);
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
	public EdgeSet edgesOut(int source) {
		return new EdgeSetMapped(indexGraph.edgesOut(viMap.idToIndex(source)));
	}

	@Override
	public EdgeSet edgesIn(int target) {
		return new EdgeSetMapped(indexGraph.edgesIn(viMap.idToIndex(target)));
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
	public void removeEdgesOutOf(int source) {
		indexGraph.removeEdgesOutOf(viMap.idToIndex(source));
	}

	@Override
	public void removeEdgesInOf(int target) {
		indexGraph.removeEdgesInOf(viMap.idToIndex(target));
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
		WeightsImpl.Index<V> indexWeights = indexGraph.getVerticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.Mapped.newInstance(iw, indexGraphVerticesMap()));
	}

	@Override
	public Set<Object> getVerticesWeightKeys() {
		return indexGraph.getVerticesWeightKeys();
	}

	@Override
	public void removeVerticesWeights(Object key) {
		indexGraph.removeVerticesWeights(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
		WeightsImpl.Index<E> indexWeights = indexGraph.getEdgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.Mapped.newInstance(iw, indexGraphEdgesMap()));
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		indexGraph.addVerticesWeights(key, type, defVal);
		return getVerticesWeights(key);
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		indexGraph.addEdgesWeights(key, type, defVal);
		return getEdgesWeights(key);
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		return indexGraph.getEdgesWeightsKeys();
	}

	@Override
	public void removeEdgesWeights(Object key) {
		indexGraph.removeEdgesWeights(key);
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

	class EdgeIterMapped implements EdgeIterImpl {

		private final EdgeIterImpl it;

		EdgeIterMapped(EdgeIter it) {
			this.it = (EdgeIterImpl) it;
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

	private static class Directed extends GraphImpl {

		Directed(GraphBaseIndex g, boolean rand) {
			super(g, rand);
			ArgumentCheck.onlyDirected(g);
		}

		Directed(Directed g) {
			super(g);
		}

		@Override
		public void reverseEdge(int edge) {
			int eIdx = eiMap.idToIndex(edge);
			indexGraph.reverseEdge(eIdx);
		}

		@Override
		public Graph copy() {
			return new Directed(this);
		}
	}

	private static class Undirected extends GraphImpl {

		Undirected(GraphBaseIndex g, boolean rand) {
			super(g, rand);
			ArgumentCheck.onlyUndirected(g);
		}

		Undirected(Undirected g) {
			super(g);
		}

		@Override
		public void reverseEdge(int edge) {
			// Do nothing
		}

		@Override
		public Graph copy() {
			return new Undirected(this);
		}
	}

	private abstract static class IdIdxMapImpl implements IndexIdMap {

		private final Int2IntOpenHashMap idToIdx;
		private final IntSet idsView; // TODO move to graph abstract implementation
		private final WeightsImpl.Index.Int idxToId;
		private int userChosenId = -1;

		IdIdxMapImpl(IdStrategyImpl.Index idStrat) {
			idToIdx = new Int2IntOpenHashMap();
			idToIdx.defaultReturnValue(-1);
			idsView = IntSets.unmodifiable(idToIdx.keySet());
			idxToId = new WeightsImpl.Index.Int(idStrat, -1);
			initListeners(idStrat);
		}

		IdIdxMapImpl(IdIdxMapImpl orig, IdStrategyImpl.Index idStrat) {
			idToIdx = new Int2IntOpenHashMap(orig.idToIdx);
			idToIdx.defaultReturnValue(-1);
			idsView = IntSets.unmodifiable(idToIdx.keySet());
			idxToId = orig.idxToId.copy(idStrat);
			initListeners(idStrat);
		}

		private void initListeners(IdStrategyImpl.Index idStrat) {
			idStrat.addIdSwapListener((idx1, idx2) -> {
				int id1 = idxToId.getInt(idx1);
				int id2 = idxToId.getInt(idx2);
				idxToId.set(idx1, id2);
				idxToId.set(idx2, id1);
				int oldIdx1 = idToIdx.put(id1, idx2);
				int oldIdx2 = idToIdx.put(id2, idx1);
				assert idx1 == oldIdx1;
				assert idx2 == oldIdx2;
			});
			idStrat.addIdAddRemoveListener(new IdAddRemoveListener() {

				@Override
				public void idRemove(int idx) {
					final int id = idxToId.getInt(idx);
					idxToId.clear(idx);
					idToIdx.remove(id);
				}

				@Override
				public void idAdd(int idx) {
					assert idx == idToIdx.size();
					int id = userChosenId != -1 ? userChosenId : nextID();
					assert id >= 0;

					int oldIdx = idToIdx.put(id, idx);
					assert oldIdx == -1;

					if (idx == idxToId.capacity())
						idxToId.expand(Math.max(2, 2 * idxToId.capacity()));
					idxToId.set(idx, id);

					userChosenId = -1;
				}

				@Override
				public void idsClear() {
					idToIdx.clear();
					idxToId.clear();
				}
			});
		}

		abstract int nextID();

		@Override
		public int indexToId(int index) {
			return idxToId.getInt(index);
		}

		@Override
		public int idToIndex(int id) {
			int idx = idToIdx.get(id);
			if (idx < 0)
				throw new IndexOutOfBoundsException(id);
			return idx;
		}

		IntSet idSet() {
			return idsView;
		}

		abstract GraphImpl.IdIdxMapImpl copy(IdStrategyImpl.Index idStrat);
	}

	private static class IdIdxMapCounter extends IdIdxMapImpl {

		private int counter;

		IdIdxMapCounter(IdStrategyImpl.Index idStrat) {
			super(idStrat);
			// We prefer non zero IDs because fastutil handle zero (null) keys separately
			counter = 1;
		}

		IdIdxMapCounter(GraphImpl.IdIdxMapCounter orig, IdStrategyImpl.Index idStrat) {
			super(orig, idStrat);
			this.counter = orig.counter;
		}

		@Override
		int nextID() {
			for (;;) {
				int id = counter++;
				if (!idSet().contains(id))
					return id;
			}
		}

		@Override
		GraphImpl.IdIdxMapCounter copy(IdStrategyImpl.Index idStrat) {
			return new GraphImpl.IdIdxMapCounter(this, idStrat);
		}
	}

	private static class IdIdxMapRand extends IdIdxMapImpl {

		private final Random rand = new Random();

		IdIdxMapRand(IdStrategyImpl.Index idStrat) {
			super(idStrat);
		}

		IdIdxMapRand(GraphImpl.IdIdxMapRand orig, IdStrategyImpl.Index idStrat) {
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

		@Override
		GraphImpl.IdIdxMapRand copy(IdStrategyImpl.Index idStrat) {
			return new GraphImpl.IdIdxMapRand(this, idStrat);
		}
	}

	static class Builder implements Graph.Builder {
		private final IndexGraph.Builder builder;

		Builder(boolean directed) {
			this.builder = new IndexGraphBuilderImpl(directed);
		}

		@Override
		public Graph build() {
			GraphBaseIndex base = (GraphBaseIndex) builder.build();
			final boolean rand = false; // TODO add option to global configuration class
			if (base.getCapabilities().directed()) {
				return new GraphImpl.Directed(base, rand);
			} else {
				return new GraphImpl.Undirected(base, rand);
			}
		}

		@Override
		public Graph.Builder setDirected(boolean directed) {
			builder.setDirected(directed);
			return this;
		}

		@Override
		public Graph.Builder expectedVerticesNum(int expectedVerticesNum) {
			builder.expectedVerticesNum(expectedVerticesNum);
			return this;
		}

		@Override
		public Graph.Builder expectedEdgesNum(int expectedEdgesNum) {
			builder.expectedEdgesNum(expectedEdgesNum);
			return this;
		}

		@Override
		public Graph.Builder addHint(Graph.Builder.Hint hint) {
			builder.addHint(hint);
			return this;
		}

		@Override
		public Graph.Builder removeHint(Graph.Builder.Hint hint) {
			builder.removeHint(hint);
			return this;
		}

		@Override
		public Graph.Builder setOption(String key, Object value) {
			builder.setOption(key, value);
			return this;
		}
	}

}
