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
import com.jgalgo.GraphsUtils.UndirectedGraphImpl;
import com.jgalgo.IDStrategy.IDAddRemoveListener;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class GraphBuilderImpl implements Graph.Builder {

	private final IndexGraph.Builder builder;

	GraphBuilderImpl(boolean directed) {
		this.builder = new IndexGraphBuilderImpl(directed);
	}

	@Override
	public Graph build() {
		GraphBaseContinues base = (GraphBaseContinues) builder.build();
		final boolean rand = true; // TODO
		if (base.getCapabilities().directed()) {
			return new GraphCustomIDStrategiesDirected(base, rand);
		} else {
			return new GraphCustomIDStrategiesUndirected(base, rand);
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

	private abstract static class GraphCustomIDStrategies extends GraphBase {

		final GraphBaseContinues g;
		final FixedAbstract verticesIDStrat;
		final FixedAbstract edgesIDStrat;
		private final WeakIdentityHashMap<WeightsImpl.Index<?>, WeightsImpl.Mapped<?>> verticesWeights =
				new WeakIdentityHashMap<>();
		private final WeakIdentityHashMap<WeightsImpl.Index<?>, WeightsImpl.Mapped<?>> edgesWeights =
				new WeakIdentityHashMap<>();

		GraphCustomIDStrategies(GraphBaseContinues g, boolean rand) {
			this.g = Objects.requireNonNull(g);
			if (rand) {
				verticesIDStrat = new GraphBuilderImpl.Rand(g.verticesIDStrat);
				edgesIDStrat = new GraphBuilderImpl.Rand(g.edgesIDStrat);
			} else {
				verticesIDStrat = new GraphBuilderImpl.Fixed(g.verticesIDStrat);
				edgesIDStrat = new GraphBuilderImpl.Fixed(g.edgesIDStrat);
			}
		}

		GraphCustomIDStrategies(GraphCustomIDStrategies orig) {
			this.g = (GraphBaseContinues) orig.g.copy();
			verticesIDStrat = orig.verticesIDStrat.copy(this.g.verticesIDStrat);
			edgesIDStrat = orig.edgesIDStrat.copy(this.g.edgesIDStrat);
		}

		@Override
		public IndexGraph indexGraph() {
			return g;
		}

		@Override
		public IndexGraphMap indexGraphVerticesMap() {
			return verticesIDStrat;
		}

		@Override
		public IndexGraphMap indexGraphEdgesMap() {
			return edgesIDStrat;
		}

		@Override
		public IntSet vertices() {
			return verticesIDStrat.idSet();
		}

		@Override
		public IntSet edges() {
			return edgesIDStrat.idSet();
		}

		@Override
		public int addVertex() {
			int uIdx = g.addVertex();
			return verticesIDStrat.indexToId(uIdx);
		}

		@Override
		public void removeVertex(int vertex) {
			int vIdx = verticesIDStrat.idToIndex(vertex);
			g.removeVertex(vIdx);
		}

		@Override
		public EdgeSet edgesOut(int source) {
			return new EdgeSetMapped(g.edgesOut(verticesIDStrat.idToIndex(source)));
		}

		@Override
		public EdgeSet edgesIn(int target) {
			return new EdgeSetMapped(g.edgesIn(verticesIDStrat.idToIndex(target)));
		}

		@Override
		public int getEdge(int source, int target) {
			int uIdx = verticesIDStrat.idToIndex(source);
			int vIdx = verticesIDStrat.idToIndex(target);
			int eIdx = g.getEdge(uIdx, vIdx);
			return eIdx == -1 ? -1 : edgesIDStrat.indexToId(eIdx);
		}

		@Override
		public EdgeSet getEdges(int source, int target) {
			int uIdx = verticesIDStrat.idToIndex(source);
			int vIdx = verticesIDStrat.idToIndex(target);
			EdgeSet s = g.getEdges(uIdx, vIdx);
			return new EdgeSetMapped(s);
		}

		@Override
		public int addEdge(int source, int target) {
			int uIdx = verticesIDStrat.idToIndex(source);
			int vIdx = verticesIDStrat.idToIndex(target);
			int eIdx = g.addEdge(uIdx, vIdx);
			return edgesIDStrat.indexToId(eIdx);
		}

		@Override
		public void removeEdge(int edge) {
			int eIdx = edgesIDStrat.idToIndex(edge);
			g.removeEdge(eIdx);
		}

		@Override
		public void removeEdgesOf(int source) {
			int uIdx = verticesIDStrat.idToIndex(source);
			g.removeEdgesOf(uIdx);
		}

		@Override
		public void removeEdgesOutOf(int source) {
			g.removeEdgesOutOf(verticesIDStrat.idToIndex(source));
		}

		@Override
		public void removeEdgesInOf(int target) {
			g.removeEdgesInOf(verticesIDStrat.idToIndex(target));
		}

		@Override
		public int edgeSource(int edge) {
			int eIdx = edgesIDStrat.idToIndex(edge);
			int uIdx = g.edgeSource(eIdx);
			return verticesIDStrat.indexToId(uIdx);
		}

		@Override
		public int edgeTarget(int edge) {
			int eIdx = edgesIDStrat.idToIndex(edge);
			int vIdx = g.edgeTarget(eIdx);
			return verticesIDStrat.indexToId(vIdx);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			int eIdx = edgesIDStrat.idToIndex(edge);
			int endpointIdx = verticesIDStrat.idToIndex(endpoint);
			int resIdx = g.edgeEndpoint(eIdx, endpointIdx);
			return verticesIDStrat.indexToId(resIdx);
		}

		@Override
		public void clear() {
			g.clear();
		}

		@Override
		public void clearEdges() {
			g.clearEdges();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			WeightsImpl.Index<V> indexWeights = g.getVerticesWeights(key);
			if (indexWeights == null)
				return null;
			return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
					iw -> WeightsImpl.Mapped.newInstance(iw, indexGraphVerticesMap()));
		}

		@Override
		public Set<Object> getVerticesWeightKeys() {
			return g.getVerticesWeightKeys();
		}

		@Override
		public void removeVerticesWeights(Object key) {
			g.removeVerticesWeights(key);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			WeightsImpl.Index<E> indexWeights = g.getEdgesWeights(key);
			if (indexWeights == null)
				return null;
			return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
					iw -> WeightsImpl.Mapped.newInstance(iw, indexGraphEdgesMap()));
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			g.addVerticesWeights(key, type, defVal);
			return getVerticesWeights(key);
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			g.addEdgesWeights(key, type, defVal);
			return getEdgesWeights(key);
		}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return g.getEdgesWeightsKeys();
		}

		@Override
		public void removeEdgesWeights(Object key) {
			g.removeEdgesWeights(key);
		}

		class EdgeSetMapped extends AbstractIntSet implements EdgeSet {

			private final EdgeSet set;

			EdgeSetMapped(EdgeSet set) {
				this.set = Objects.requireNonNull(set);
			}

			@Override
			public boolean remove(int edge) {
				int eIdx = edgesIDStrat.idToIndex(edge);
				return set.remove(eIdx);
			}

			@Override
			public boolean contains(int edge) {
				int eIdx = edgesIDStrat.idToIndex(edge);
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
				return edgesIDStrat.indexToId(eIdx);
			}

			@Override
			public int peekNext() {
				int eIdx = it.peekNext();
				return edgesIDStrat.indexToId(eIdx);
			}

			@Override
			public void remove() {
				it.remove();
			}

			@Override
			public int target() {
				int vIdx = it.target();
				return verticesIDStrat.indexToId(vIdx);
			}

			@Override
			public int source() {
				int uIdx = it.source();
				return verticesIDStrat.indexToId(uIdx);
			}

		}

		@Override
		public GraphCapabilities getCapabilities() {
			return g.getCapabilities();
		}

	}

	private static class GraphCustomIDStrategiesDirected extends GraphCustomIDStrategies {

		GraphCustomIDStrategiesDirected(GraphBaseContinues g, boolean rand) {
			super(g, rand);
			ArgumentCheck.onlyDirected(g);
		}

		GraphCustomIDStrategiesDirected(GraphCustomIDStrategiesDirected g) {
			super(g);
		}

		@Override
		public void reverseEdge(int edge) {
			int eIdx = edgesIDStrat.idToIndex(edge);
			g.reverseEdge(eIdx);
		}

		@Override
		public Graph copy() {
			return new GraphCustomIDStrategiesDirected(this);
		}

	}

	private static class GraphCustomIDStrategiesUndirected extends GraphCustomIDStrategies
			implements UndirectedGraphImpl {

		GraphCustomIDStrategiesUndirected(GraphBaseContinues g, boolean rand) {
			super(g, rand);
			ArgumentCheck.onlyUndirected(g);
		}

		GraphCustomIDStrategiesUndirected(GraphCustomIDStrategiesUndirected g) {
			super(g);
		}

		@Override
		public Graph copy() {
			return new GraphCustomIDStrategiesUndirected(this);
		}
	}

	private abstract static class FixedAbstract implements IndexGraphMap {

		private final Int2IntOpenHashMap idToIdx;
		private final IntSet idsView; // move to graph abstract implementation
		private final WeightsImpl.Index.Int idxToId;

		FixedAbstract(IDStrategyImpl idStrat) {
			idToIdx = new Int2IntOpenHashMap();
			idToIdx.defaultReturnValue(-1);
			idsView = IntSets.unmodifiable(idToIdx.keySet());
			idxToId = new WeightsImpl.Index.Int(idStrat, -1);
			initListeners(idStrat);
		}

		FixedAbstract(FixedAbstract orig, IDStrategyImpl idStrat) {
			idToIdx = new Int2IntOpenHashMap(orig.idToIdx);
			idToIdx.defaultReturnValue(-1);
			idsView = IntSets.unmodifiable(idToIdx.keySet());
			idxToId = orig.idxToId.copy(idStrat);
			initListeners(idStrat);
		}

		private void initListeners(IDStrategyImpl idStrat) {
			idStrat.addIDSwapListener((idx1, idx2) -> {
				int id1 = idxToId.getInt(idx1);
				int id2 = idxToId.getInt(idx2);
				idxToId.set(idx1, id2);
				idxToId.set(idx2, id1);
				int oldIdx1 = idToIdx.put(id1, idx2);
				int oldIdx2 = idToIdx.put(id2, idx1);
				assert idx1 == oldIdx1;
				assert idx2 == oldIdx2;
			});
			idStrat.addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int idx) {
					final int id = idxToId.getInt(idx);
					idxToId.clear(idx);
					idToIdx.remove(id);
				}

				@Override
				public void idAdd(int idx) {
					assert idx == idToIdx.size();
					int id = nextID();
					assert id >= 0;
					idToIdx.put(id, idx);
					if (idx == idxToId.capacity())
						idxToId.expand(Math.max(2, 2 * idxToId.capacity()));
					idxToId.set(idx, id);
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

		abstract GraphBuilderImpl.FixedAbstract copy(IDStrategyImpl idStrat);
	}

	private static class Fixed extends FixedAbstract {

		private int counter;

		Fixed(IDStrategyImpl idStrat) {
			super(idStrat);
			// We prefer non zero IDs because fastutil handle zero (null) keys separately
			counter = 1;
		}

		Fixed(GraphBuilderImpl.Fixed orig, IDStrategyImpl idStrat) {
			super(orig, idStrat);
			this.counter = orig.counter;
		}

		@Override
		int nextID() {
			return counter++;
		}

		@Override
		GraphBuilderImpl.Fixed copy(IDStrategyImpl idStrat) {
			return new GraphBuilderImpl.Fixed(this, idStrat);
		}
	}

	private static class Rand extends FixedAbstract {

		private final Random rand = new Random();

		Rand(IDStrategyImpl idStrat) {
			super(idStrat);
		}

		Rand(GraphBuilderImpl.Rand orig, IDStrategyImpl idStrat) {
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
		GraphBuilderImpl.Rand copy(IDStrategyImpl idStrat) {
			return new GraphBuilderImpl.Rand(this, idStrat);
		}
	}

}
