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
import java.util.Set;
import com.jgalgo.GraphsUtils.UndirectedGraphImpl;
import com.jgalgo.IDStrategy.IDAddRemoveListener;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;

class GraphBuilderImpl implements Graph.Builder {

	private final IndexGraph.Builder builder;

	GraphBuilderImpl(boolean directed) {
		this.builder = new IndexGraphBuilderImpl(directed);
	}

	@Override
	public Graph build() {
		GraphBaseContinues base = (GraphBaseContinues) builder.build();
		if (base.getCapabilities().directed()) {
			return new GraphCustomIDStrategiesDirected(base, new IDStrategyImpl.Rand(), new IDStrategyImpl.Rand()); // TODO
		} else {
			return new GraphCustomIDStrategiesUndirected(base, new IDStrategyImpl.Rand(), new IDStrategyImpl.Rand()); // TODO
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
		private final WeakIdentityHashMap<Weights<?>, Weights<?>> verticesWeights = new WeakIdentityHashMap<>();
		private final WeakIdentityHashMap<Weights<?>, Weights<?>> edgesWeights = new WeakIdentityHashMap<>();

		GraphCustomIDStrategies(GraphBaseContinues g, IDStrategyImpl verticesIDStrat, IDStrategyImpl edgesIDStrategy) {
			super(verticesIDStrat, edgesIDStrategy);
			this.g = Objects.requireNonNull(g);

			initListenersToUnderlyingGraph();
		}

		GraphCustomIDStrategies(GraphCustomIDStrategies orig) {
			super(orig.verticesIDStrat.copy(), orig.edgesIDStrat.copy());
			this.g = (GraphBaseContinues) orig.g.copy();

			initListenersToUnderlyingGraph();
		}

		private void initListenersToUnderlyingGraph() {
			g.getVerticesIDStrategy().addIDSwapListener((vIdx1, vIdx2) -> verticesIDStrat.idxSwap(vIdx1, vIdx2));
			g.getVerticesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					verticesIDStrat.removeIdx(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = verticesIDStrat.newIdx();
					if (idx != id)
						throw new IllegalStateException();
				}

				@Override
				public void idsClear() {
					verticesIDStrat.clear();
				}
			});
			g.getEdgesIDStrategy().addIDSwapListener((eIdx1, eIdx2) -> edgesIDStrat.idxSwap(eIdx1, eIdx2));
			g.getEdgesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					edgesIDStrat.removeIdx(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = edgesIDStrat.newIdx();
					if (idx != id)
						throw new IllegalStateException();
				}

				@Override
				public void idsClear() {
					edgesIDStrat.clear();
				}
			});
		}

		@Override
		public IndexGraph indexGraph() {
			return g;
		}

		private final IndexGraphMap indexGraphVerticesMap = new IndexGraphMap() {

			@Override
			public int indexToId(int index) {
				return verticesIDStrat.idxToId(index);
			}

			@Override
			public int idToIndex(int id) {
				return verticesIDStrat.idToIdx(id);
			}

		};

		private final IndexGraphMap indexGraphEdgesMap = new IndexGraphMap() {

			@Override
			public int indexToId(int index) {
				return edgesIDStrat.idxToId(index);
			}

			@Override
			public int idToIndex(int id) {
				return edgesIDStrat.idToIdx(id);
			}

		};

		@Override
		public IndexGraphMap indexGraphVerticesMap() {
			return indexGraphVerticesMap;
		}

		@Override
		public IndexGraphMap indexGraphEdgesMap() {
			return indexGraphEdgesMap;
		}

		@Override
		public int addVertex() {
			int uIdx = g.addVertex();
			return verticesIDStrat.idxToId(uIdx);
		}

		@Override
		public void removeVertex(int vertex) {
			int vIdx = verticesIDStrat.idToIdx(vertex);
			g.removeVertex(vIdx);
		}

		@Override
		public EdgeSet edgesOut(int source) {
			return new EdgeSetMapped(g.edgesOut(verticesIDStrat.idToIdx(source)));
		}

		@Override
		public EdgeSet edgesIn(int target) {
			return new EdgeSetMapped(g.edgesIn(verticesIDStrat.idToIdx(target)));
		}

		@Override
		public int getEdge(int source, int target) {
			int uIdx = verticesIDStrat.idToIdx(source);
			int vIdx = verticesIDStrat.idToIdx(target);
			int eIdx = g.getEdge(uIdx, vIdx);
			return eIdx == -1 ? -1 : edgesIDStrat.idxToId(eIdx);
		}

		@Override
		public EdgeSet getEdges(int source, int target) {
			int uIdx = verticesIDStrat.idToIdx(source);
			int vIdx = verticesIDStrat.idToIdx(target);
			EdgeSet s = g.getEdges(uIdx, vIdx);
			return new EdgeSetMapped(s);
		}

		@Override
		public int addEdge(int source, int target) {
			int uIdx = verticesIDStrat.idToIdx(source);
			int vIdx = verticesIDStrat.idToIdx(target);
			int eIdx = g.addEdge(uIdx, vIdx);
			return edgesIDStrat.idxToId(eIdx);
		}

		@Override
		public void removeEdge(int edge) {
			int eIdx = edgesIDStrat.idToIdx(edge);
			g.removeEdge(eIdx);
		}

		@Override
		public void removeEdgesOf(int source) {
			int uIdx = verticesIDStrat.idToIdx(source);
			g.removeEdgesOf(uIdx);
		}

		@Override
		public void removeEdgesOutOf(int source) {
			g.removeEdgesOutOf(verticesIDStrat.idToIdx(source));
		}

		@Override
		public void removeEdgesInOf(int target) {
			g.removeEdgesInOf(verticesIDStrat.idToIdx(target));
		}

		@Override
		public int edgeSource(int edge) {
			int eIdx = edgesIDStrat.idToIdx(edge);
			int uIdx = g.edgeSource(eIdx);
			return verticesIDStrat.idxToId(uIdx);
		}

		@Override
		public int edgeTarget(int edge) {
			int eIdx = edgesIDStrat.idToIdx(edge);
			int vIdx = g.edgeTarget(eIdx);
			return verticesIDStrat.idxToId(vIdx);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			int eIdx = edgesIDStrat.idToIdx(edge);
			int endpointIdx = verticesIDStrat.idToIdx(endpoint);
			int resIdx = g.edgeEndpoint(eIdx, endpointIdx);
			return verticesIDStrat.idxToId(resIdx);
		}

		@Override
		public void clear() {
			g.clear();
			verticesIDStrat.clear();
			edgesIDStrat.clear();
		}

		@Override
		public void clearEdges() {
			g.clearEdges();
			edgesIDStrat.clear();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			WeightsT indexWeights = g.getVerticesWeights(key);
			if (indexWeights == null)
				return null;
			return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
					iw -> WeightsImpl.wrapContainerMapped(iw, indexGraphVerticesMap()));
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
			WeightsT indexWeights = g.getEdgesWeights(key);
			if (indexWeights == null)
				return null;
			return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
					iw -> WeightsImpl.wrapContainerMapped(iw, indexGraphEdgesMap()));
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

		@Override
		public IDStrategy getVerticesIDStrategy() {
			return verticesIDStrat;
		}

		@Override
		public IDStrategy getEdgesIDStrategy() {
			return edgesIDStrat;
		}

		class EdgeSetMapped extends AbstractIntSet implements EdgeSet {

			private final EdgeSet set;

			EdgeSetMapped(EdgeSet set) {
				this.set = Objects.requireNonNull(set);
			}

			@Override
			public boolean remove(int edge) {
				int eIdx = edgesIDStrat.idToIdx(edge);
				return set.remove(eIdx);
			}

			@Override
			public boolean contains(int edge) {
				int eIdx = edgesIDStrat.idToIdx(edge);
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
				return edgesIDStrat.idxToId(eIdx);
			}

			@Override
			public int peekNext() {
				int eIdx = it.peekNext();
				return edgesIDStrat.idxToId(eIdx);
			}

			@Override
			public void remove() {
				it.remove();
			}

			@Override
			public int target() {
				int vIdx = it.target();
				return verticesIDStrat.idxToId(vIdx);
			}

			@Override
			public int source() {
				int uIdx = it.source();
				return verticesIDStrat.idxToId(uIdx);
			}

		}

		@Override
		public GraphCapabilities getCapabilities() {
			return g.getCapabilities();
		}

	}

	private static class GraphCustomIDStrategiesDirected extends GraphCustomIDStrategies {

		GraphCustomIDStrategiesDirected(GraphBaseContinues g, IDStrategyImpl verticesIDStrat,
				IDStrategyImpl edgesIDStrategy) {
			super(g, verticesIDStrat, edgesIDStrategy);
			ArgumentCheck.onlyDirected(g);
		}

		GraphCustomIDStrategiesDirected(GraphCustomIDStrategiesDirected g) {
			super(g);
		}

		@Override
		public void reverseEdge(int edge) {
			int eIdx = edgesIDStrat.idToIdx(edge);
			g.reverseEdge(eIdx);
		}

		@Override
		public Graph copy() {
			return new GraphCustomIDStrategiesDirected(this);
		}

	}

	private static class GraphCustomIDStrategiesUndirected extends GraphCustomIDStrategies
			implements UndirectedGraphImpl {

		GraphCustomIDStrategiesUndirected(GraphBaseContinues g, IDStrategyImpl verticesIDStrat,
				IDStrategyImpl edgesIDStrategy) {
			super(g, verticesIDStrat, edgesIDStrategy);
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

}
