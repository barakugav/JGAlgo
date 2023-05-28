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

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import com.jgalgo.GraphsUtils.UndirectedGraphImpl;
import com.jgalgo.IDStrategy.IDAddRemoveListener;

class GraphBuilderImpl implements GraphBuilder {

	private boolean directed;
	private int expectedVerticesNum;
	private int expectedEdgesNum;
	private boolean fixedEdgesIDs;
	private final EnumSet<GraphBuilder.Hint> hints = EnumSet.noneOf(GraphBuilder.Hint.class);
	private String impl;

	GraphBuilderImpl(boolean directed) {
		this.directed = directed;
	}

	@Override
	public Graph build() {
		BiFunction<Integer, Integer, ? extends GraphBaseContinues> baseBuilderArray =
				directed ? GraphArrayDirected::new : GraphArrayUndirected::new;
		BiFunction<Integer, Integer, ? extends GraphBaseContinues> baseBuilderLinked =
				directed ? GraphLinkedDirected::new : GraphLinkedUndirected::new;
		BiFunction<Integer, Integer, ? extends GraphBaseContinues> baseBuilderTable =
				directed ? GraphTableDirected::new : GraphTableUndirected::new;

		BiFunction<Integer, Integer, ? extends GraphBaseContinues> baseBuilder;
		if (impl != null && !"GraphArray".equals(impl)) {
			if ("GraphArray".equals(impl))
				baseBuilder = baseBuilderArray;
			else if ("GraphLinked".equals(impl))
				baseBuilder = baseBuilderLinked;
			else if ("GraphTable".equals(impl))
				baseBuilder = baseBuilderTable;
			else
				throw new IllegalArgumentException("unknown 'impl' value: " + impl);
		} else {
			if (hints.contains(GraphBuilder.Hint.FastEdgeLookup))
				baseBuilder = baseBuilderTable;
			else if (hints.contains(GraphBuilder.Hint.FastEdgeLookup))
				baseBuilder = baseBuilderLinked;
			else
				baseBuilder = baseBuilderArray;
		}
		@SuppressWarnings("boxing")
		GraphBaseContinues base = baseBuilder.apply(expectedVerticesNum, expectedEdgesNum);

		Graph g;
		if (!fixedEdgesIDs) {
			g = base;
		} else {
			if (directed) {
				g = new GraphCustomIDStrategiesDirected(base, new IDStrategyImpl.Fixed());
			} else {
				g = new GraphCustomIDStrategiesUndirected(base, new IDStrategyImpl.Fixed());
			}
		}
		return g;
	}

	@Override
	public GraphBuilder setDirected(boolean directed) {
		this.directed = directed;
		return this;
	}

	@Override
	public GraphBuilder expectedVerticesNum(int expectedVerticesNum) {
		if (expectedVerticesNum < 0)
			throw new IllegalArgumentException("invalid expected size: " + expectedVerticesNum);
		this.expectedVerticesNum = expectedVerticesNum;
		return this;
	}

	@Override
	public GraphBuilder expectedEdgesNum(int expectedEdgesNum) {
		if (expectedEdgesNum < 0)
			throw new IllegalArgumentException("invalid expected size: " + expectedEdgesNum);
		this.expectedEdgesNum = expectedEdgesNum;
		return this;
	}

	@Override
	public GraphBuilder useFixedEdgesIDs(boolean enable) {
		fixedEdgesIDs = enable;
		return this;
	}

	@Override
	public GraphBuilder addHint(GraphBuilder.Hint hint) {
		hints.add(hint);
		return this;
	}

	@Override
	public GraphBuilder removeHint(GraphBuilder.Hint hint) {
		hints.remove(hint);
		return this;
	}

	@Override
	public GraphBuilder setOption(String key, Object value) {
		if ("impl".equals(key)) {
			impl = (String) value;
		} else {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
		return this;
	}

	private abstract static class GraphCustomIDStrategies extends GraphBase {

		final GraphBaseContinues g;
		private final WeightsImpl.Manager verticesWeights;
		private final WeightsImpl.Manager edgesWeights;

		GraphCustomIDStrategies(GraphBaseContinues g, IDStrategyImpl edgesIDStrategy) {
			super(g.verticesIDStrat.copy(), edgesIDStrategy);
			this.g = Objects.requireNonNull(g);
			verticesWeights = new WeightsImpl.Manager(verticesIDStrat.size());
			edgesWeights = new WeightsImpl.Manager(edgesIDStrategy.size());

			initListenersToUnderlyingGraph();
		}

		GraphCustomIDStrategies(GraphCustomIDStrategies orig) {
			super(orig.verticesIDStrat.copy(), orig.edgesIDStrat.copy());
			this.g = (GraphBaseContinues) orig.g.copy();
			verticesWeights = orig.verticesWeights.copy(verticesIDStrat);
			edgesWeights = orig.edgesWeights.copy(edgesIDStrat);

			initListenersToUnderlyingGraph();
		}

		private void initListenersToUnderlyingGraph() {
			g.getVerticesIDStrategy().addIDSwapListener((vIdx1, vIdx2) -> {
				verticesIDStrat.idxSwap(vIdx1, vIdx2);
				verticesWeights.swapElements(vIdx1, vIdx2);
			});
			g.getVerticesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					verticesIDStrat.removeIdx(id);
					verticesWeights.clearElement(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = verticesIDStrat.newIdx();
					if (idx != id)
						throw new IllegalStateException();
					verticesWeights.ensureCapacity(idx + 1);
				}

				@Override
				public void idsClear() {
					verticesIDStrat.clear();
					verticesWeights.clearContainers();;
				}
			});
			g.getEdgesIDStrategy().addIDSwapListener((eIdx1, eIdx2) -> {
				edgesIDStrat.idxSwap(eIdx1, eIdx2);
				edgesWeights.swapElements(eIdx1, eIdx2);
			});
			g.getEdgesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					edgesIDStrat.removeIdx(id);
					edgesWeights.clearElement(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = edgesIDStrat.newIdx();
					if (idx != id)
						throw new IllegalStateException();
					edgesWeights.ensureCapacity(idx + 1);
				}

				@Override
				public void idsClear() {
					edgesIDStrat.clear();
					edgesWeights.clearContainers();
				}
			});
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
		public EdgeIter edgesOut(int source) {
			EdgeIter it = g.edgesOut(verticesIDStrat.idToIdx(source));
			return new EdgeItr(it);
		}

		@Override
		public EdgeIter edgesIn(int target) {
			EdgeIter it = g.edgesIn(verticesIDStrat.idToIdx(target));
			return new EdgeItr(it);
		}

		@Override
		public int getEdge(int source, int target) {
			int uIdx = verticesIDStrat.idToIdx(source);
			int vIdx = verticesIDStrat.idToIdx(target);
			int eIdx = g.getEdge(uIdx, vIdx);
			return eIdx == -1 ? -1 : edgesIDStrat.idxToId(eIdx);
		}

		@Override
		public EdgeIter getEdges(int source, int target) {
			int uIdx = verticesIDStrat.idToIdx(source);
			int vIdx = verticesIDStrat.idToIdx(target);
			EdgeIter it = g.getEdges(uIdx, vIdx);
			return new EdgeItr(it);
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
			return g.edgeSource(eIdx);
		}

		@Override
		public int edgeTarget(int edge) {
			int eIdx = edgesIDStrat.idToIdx(edge);
			return g.edgeTarget(eIdx);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			int eIdx = edgesIDStrat.idToIdx(edge);
			int endpointIdx = verticesIDStrat.idToIdx(endpoint);
			int resIdx = g.edgeEndpoint(eIdx, endpointIdx);
			return verticesIDStrat.idxToId(resIdx);
		}

		@Override
		public int degreeOut(int source) {
			int uIdx = verticesIDStrat.idToIdx(source);
			return g.degreeOut(uIdx);
		}

		@Override
		public int degreeIn(int source) {
			int uIdx = verticesIDStrat.idToIdx(source);
			return g.degreeIn(uIdx);
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
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			return verticesWeights.getWeights(key);
		}

		@Override
		public Set<Object> getVerticesWeightKeys() {
			return verticesWeights.weightsKeys();
		}

		@Override
		public void removeVerticesWeights(Object key) {
			verticesWeights.removeWeights(key);
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return edgesWeights.getWeights(key);
		}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return edgesWeights.weightsKeys();
		}

		@Override
		public void removeEdgesWeights(Object key) {
			edgesWeights.removeWeights(key);
		}

		@Override
		public IDStrategy.Continues getVerticesIDStrategy() {
			return verticesIDStrat;
		}

		@Override
		public IDStrategy getEdgesIDStrategy() {
			return edgesIDStrat;
		}

		@Override
		void addVerticesWeightsContainer(Object key, Weights<?> weights) {
			verticesWeights.addWeights(key, weights);
		}

		@Override
		void addEdgesWeightsContainer(Object key, Weights<?> weights) {
			edgesWeights.addWeights(key, weights);
		}

		class EdgeItr implements EdgeIterImpl {

			private final EdgeIterImpl it;

			EdgeItr(EdgeIter it) {
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

		GraphCustomIDStrategiesDirected(GraphBaseContinues g, IDStrategyImpl edgesIDStrategy) {
			super(g, edgesIDStrategy);
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

		GraphCustomIDStrategiesUndirected(GraphBaseContinues g, IDStrategyImpl edgesIDStrategy) {
			super(g, edgesIDStrategy);
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
