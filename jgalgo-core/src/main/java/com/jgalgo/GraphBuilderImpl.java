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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import com.jgalgo.GraphsUtils.UndirectedGraphImpl;
import com.jgalgo.IDStrategy.IDAddRemoveListener;

class GraphBuilderImpl implements GraphBuilder {

	private boolean directed;
	private Class<? extends IDStrategy> edgesIDStrategy;
	private final EnumSet<GraphBuilder.Hint> hints = EnumSet.noneOf(GraphBuilder.Hint.class);
	private String impl;

	GraphBuilderImpl(boolean directed) {
		this.directed = directed;
	}

	@Override
	public Graph build(int verticesNum) {
		IntFunction<? extends GraphBaseContinues> baseBuilderArray =
				directed ? GraphArrayDirected::new : GraphArrayUndirected::new;
		IntFunction<? extends GraphBaseContinues> baseBuilderLinked =
				directed ? GraphLinkedDirected::new : GraphLinkedUndirected::new;
		IntFunction<? extends GraphBaseContinues> baseBuilderTable =
				directed ? GraphTableDirected::new : GraphTableUndirected::new;

		IntFunction<? extends GraphBaseContinues> baseBuilder;
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
		GraphBaseContinues base = baseBuilder.apply(verticesNum);

		Graph g;
		IDStrategy eIDStrat = createIDStrategy(edgesIDStrategy);
		if (eIDStrat == null) {
			g = base;
		} else {
			if (directed) {
				g = new GraphCustomIDStrategiesDirected(base, eIDStrat);
			} else {
				g = new GraphCustomIDStrategiesUndirected(base, eIDStrat);
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
	public GraphBuilder setEdgesIDStrategy(Class<? extends IDStrategy> edgesIDStrategy) {
		if (edgesIDStrategy != null
				&& !List.of(IDStrategy.Continues.class, IDStrategy.Fixed.class, IDStrategy.Rand.class)
						.contains(edgesIDStrategy))
			throw new IllegalArgumentException("unknown ID strategy: " + edgesIDStrategy.toString());
		this.edgesIDStrategy = edgesIDStrategy;
		return this;
	}

	private static IDStrategy createIDStrategy(Class<? extends IDStrategy> strategyClass) {
		if (strategyClass == null)
			return null;
		if (strategyClass == IDStrategy.Continues.class) {
			return null; /* Use default */
		} else if (strategyClass == IDStrategy.Fixed.class) {
			return new IDStrategy.Fixed();
		} else if (strategyClass == IDStrategy.Rand.class) {
			return new IDStrategy.Rand();
		} else {
			throw new IllegalArgumentException(strategyClass.toString());
		}
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

		GraphCustomIDStrategies(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(new IDStrategy.Continues(g.vertices().size()), edgesIDStrategy);
			this.g = Objects.requireNonNull(g);

			g.getVerticesIDStrategy().addIDSwapListener((vIdx1, vIdx2) -> verticesIDStrategy.idxSwap(vIdx1, vIdx2));
			g.getVerticesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					verticesIDStrategy.removeIdx(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = verticesIDStrategy.newIdx();
					if (idx != id)
						throw new IllegalStateException();
				}

				@Override
				public void idsClear() {
					verticesIDStrategy.clear();
				}
			});
			g.getEdgesIDStrategy().addIDSwapListener((eIdx1, eIdx2) -> edgesIDStrategy.idxSwap(eIdx1, eIdx2));
			g.getEdgesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					edgesIDStrategy.removeIdx(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = edgesIDStrategy.newIdx();
					if (idx != id)
						throw new IllegalStateException();
				}

				@Override
				public void idsClear() {
					edgesIDStrategy.clear();
				}
			});
		}

		@Override
		public int addVertex() {
			int uIdx = g.addVertex();
			return verticesIDStrategy.idxToId(uIdx);
		}

		@Override
		public void removeVertex(int vertex) {
			int vIdx = verticesIDStrategy.idToIdx(vertex);
			g.removeVertex(vIdx);
		}

		// @Override
		// public void removeVertices(IntCollection vs) {
		// g.removeVertices(new AbstractIntCollection() {

		// @Override
		// public int size() {
		// return vs.size();
		// }

		// @Override
		// public IntIterator iterator() {
		// return new IntIterator() {
		// final IntIterator it = vs.iterator();

		// @Override
		// public boolean hasNext() {
		// return it.hasNext();
		// }

		// @Override
		// public int nextInt() {
		// return verticesIDStrategy.idToIdx(it.nextInt());
		// }
		// };
		// }

		// @Override
		// public boolean contains(int uIdx) {
		// return vs.contains(verticesIDStrategy.idxToId(uIdx));
		// }

		// });
		// }

		@Override
		public EdgeIter edgesOut(int source) {
			EdgeIter it = g.edgesOut(verticesIDStrategy.idToIdx(source));
			return new EdgeItr(it);
		}

		@Override
		public EdgeIter edgesIn(int target) {
			EdgeIter it = g.edgesIn(verticesIDStrategy.idToIdx(target));
			return new EdgeItr(it);
		}

		@Override
		public int getEdge(int source, int target) {
			int uIdx = verticesIDStrategy.idToIdx(source);
			int vIdx = verticesIDStrategy.idToIdx(target);
			int eIdx = g.getEdge(uIdx, vIdx);
			return eIdx == -1 ? -1 : edgesIDStrategy.idxToId(eIdx);
		}

		@Override
		public EdgeIter getEdges(int source, int target) {
			int uIdx = verticesIDStrategy.idToIdx(source);
			int vIdx = verticesIDStrategy.idToIdx(target);
			EdgeIter it = g.getEdges(uIdx, vIdx);
			return new EdgeItr(it);
		}

		@Override
		public int addEdge(int source, int target) {
			int uIdx = verticesIDStrategy.idToIdx(source);
			int vIdx = verticesIDStrategy.idToIdx(target);
			int eIdx = g.addEdge(uIdx, vIdx);
			return edgesIDStrategy.idxToId(eIdx);
		}

		@Override
		public void removeEdge(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			g.removeEdge(eIdx);
		}

		// @Override
		// public void removeEdges(IntCollection edges) {
		// g.removeEdges(new AbstractIntCollection() {

		// @Override
		// public int size() {
		// return edges.size();
		// }

		// @Override
		// public IntIterator iterator() {
		// return new IntIterator() {
		// final IntIterator it = edges.iterator();

		// @Override
		// public boolean hasNext() {
		// return it.hasNext();
		// }

		// @Override
		// public int nextInt() {
		// return edgesIDStrategy.idToIdx(it.nextInt());
		// }
		// };
		// }

		// @Override
		// public boolean contains(int eIdx) {
		// return edges.contains(edgesIDStrategy.idxToId(eIdx));
		// }

		// });
		// }

		@Override
		public void removeEdgesOf(int source) {
			int uIdx = verticesIDStrategy.idToIdx(source);
			g.removeEdgesOf(uIdx);
		}

		@Override
		public void removeEdgesOutOf(int source) {
			g.removeEdgesOutOf(verticesIDStrategy.idToIdx(source));
		}

		@Override
		public void removeEdgesInOf(int target) {
			g.removeEdgesInOf(verticesIDStrategy.idToIdx(target));
		}

		@Override
		public int edgeSource(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			return g.edgeSource(eIdx);
		}

		@Override
		public int edgeTarget(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			return g.edgeTarget(eIdx);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			int endpointIdx = verticesIDStrategy.idToIdx(endpoint);
			int resIdx = g.edgeEndpoint(eIdx, endpointIdx);
			return verticesIDStrategy.idxToId(resIdx);
		}

		@Override
		public int degreeOut(int source) {
			int uIdx = verticesIDStrategy.idToIdx(source);
			return g.degreeOut(uIdx);
		}

		@Override
		public int degreeIn(int source) {
			int uIdx = verticesIDStrategy.idToIdx(source);
			return g.degreeIn(uIdx);
		}

		@Override
		public void clear() {
			g.clear();
			verticesIDStrategy.clear();
			edgesIDStrategy.clear();
		}

		@Override
		public void clearEdges() {
			g.clearEdges();
			edgesIDStrategy.clear();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			return g.getVerticesWeights(key);
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
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return g.getEdgesWeights(key);
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
		public IDStrategy.Continues getVerticesIDStrategy() {
			return verticesIDStrategy;
		}

		@Override
		public IDStrategy getEdgesIDStrategy() {
			return edgesIDStrategy;
		}

		@Override
		void addVerticesWeightsContainer(Object key, Weights<?> weights) {
			g.addVerticesWeightsContainer(key, weights);
		}

		@Override
		void addEdgesWeightsContainer(Object key, Weights<?> weights) {
			g.addEdgesWeightsContainer(key, weights);
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
				return edgesIDStrategy.idxToId(eIdx);
			}

			@Override
			public int peekNext() {
				int eIdx = it.peekNext();
				return edgesIDStrategy.idxToId(eIdx);
			}

			@Override
			public void remove() {
				it.remove();
			}

			@Override
			public int target() {
				int vIdx = it.target();
				return verticesIDStrategy.idxToId(vIdx);
			}

			@Override
			public int source() {
				int uIdx = it.source();
				return verticesIDStrategy.idxToId(uIdx);
			}

		}

		@Override
		public GraphCapabilities getCapabilities() {
			return g.getCapabilities();
		}

	}

	private static class GraphCustomIDStrategiesDirected extends GraphCustomIDStrategies {

		GraphCustomIDStrategiesDirected(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(g, edgesIDStrategy);
			ArgumentCheck.onlyDirected(g);
		}

		@Override
		public void reverseEdge(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			g.reverseEdge(eIdx);
		}

	}

	private static class GraphCustomIDStrategiesUndirected extends GraphCustomIDStrategies
			implements UndirectedGraphImpl {

		GraphCustomIDStrategiesUndirected(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(g, edgesIDStrategy);
			ArgumentCheck.onlyUndirected(g);
		}

	}

}
