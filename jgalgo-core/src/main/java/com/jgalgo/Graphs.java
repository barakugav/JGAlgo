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

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.GraphsUtils.GraphCapabilitiesBuilder;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

/**
 * Static methods class for graphs.
 *
 * @author Barak Ugav
 */
public class Graphs {
	private Graphs() {}

	private abstract static class EmptyGraph implements IndexGraph {

		private final IDStrategy.Continues verticesIDStrat = new IDStrategyImpl.ContinuesEmpty();
		private final IDStrategy.Continues edgesIDStrat = new IDStrategyImpl.ContinuesEmpty();

		@Override
		public IntSet vertices() {
			return IntSets.emptySet();
		}

		@Override
		public IntSet edges() {
			return IntSets.emptySet();
		}

		@Override
		public int addVertex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVertex(int vertex) {
			throw new IndexOutOfBoundsException(vertex);
		}

		@Override
		public EdgeSet edgesOut(int source) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public EdgeSet edgesIn(int target) {
			throw new IndexOutOfBoundsException(target);
		}

		@Override
		public EdgeSet getEdges(int source, int target) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public int addEdge(int source, int target) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public void removeEdge(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public void reverseEdge(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public int edgeSource(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public void clear() {}

		@Override
		public void clearEdges() {}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			return null;
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVerticesWeights(Object key) {}

		@Override
		public Set<Object> getVerticesWeightKeys() {
			return Collections.emptySet();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return null;
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeEdgesWeights(Object key) {}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return Collections.emptySet();
		}

		@Override
		public IDStrategy.Continues getVerticesIDStrategy() {
			return verticesIDStrat;
		}

		@Override
		public IDStrategy.Continues getEdgesIDStrategy() {
			return edgesIDStrat;
		}

		@Override
		public IndexGraph indexGraph() {
			return this;
		}

		@Override
		public IndexGraphMap indexGraphVerticesMap() {
			return GraphsUtils.IndexGraphMapIdentify;
		}

		@Override
		public IndexGraphMap indexGraphEdgesMap() {
			return GraphsUtils.IndexGraphMapIdentify;
		}

		@Override
		public IndexGraph copy() {
			return this;
		}
	}

	private static class EmptyGraphUndirected extends EmptyGraph {

		private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newUndirected().vertexAdd(false)
				.vertexRemove(false).edgeAdd(false).edgeRemove(false).parallelEdges(false).selfEdges(false).build();

		@Override
		public GraphCapabilities getCapabilities() {
			return Capabilities;
		}

	}

	private static class EmptyGraphDirected extends EmptyGraph {

		private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newDirected().vertexAdd(false)
				.vertexRemove(false).edgeAdd(false).edgeRemove(false).parallelEdges(false).selfEdges(false).build();

		@Override
		public GraphCapabilities getCapabilities() {
			return Capabilities;
		}

	}

	/**
	 * An undirected graphs with no vertices and no edges
	 */
	public static final IndexGraph EmptyGraphUndirected = new EmptyGraphUndirected();

	/**
	 * A directed graphs with no vertices and no edges
	 */
	public static final IndexGraph EmptyGraphDirected = new EmptyGraphDirected();

	private static class CompleteGraphUndirected extends CompleteGraph {

		CompleteGraphUndirected(int verticesNum) {
			super(verticesNum, verticesNum * (verticesNum - 1) / 2);
		}

		CompleteGraphUndirected(CompleteGraphUndirected g) {
			super(g);
		}

		@Override
		public int getEdge(int source, int target) {
			checkVertex(source);
			checkVertex(target);
			if (source == target)
				return -1;
			if (source > target) {
				int temp = source;
				source = target;
				target = temp;
			}
			assert source < target;

			if (n % 2 == 0) {
				final int edgesPerSource1 = n / 2;
				final int edgesPerSource2 = n / 2 - 1;
				final int sourcesNum1 = n / 2;

				boolean source1 = source < sourcesNum1;
				boolean edgeIsSourcedAtSource = source + (source1 ? edgesPerSource1 : edgesPerSource2) >= target;
				if (source1 && edgeIsSourcedAtSource) {
					int base = edgesPerSource1 * source;
					int offset = target - source - 1;
					return base + offset;

				} else if (!source1 && edgeIsSourcedAtSource) {
					int base = sourcesNum1 * edgesPerSource1 + (source - sourcesNum1) * edgesPerSource2;
					int offset = target - source - 1;
					return base + offset;

				} else {
					assert !edgeIsSourcedAtSource;
					assert target + edgesPerSource2 >= n;
					assert target + edgesPerSource2 - n >= source;
					int base = sourcesNum1 * edgesPerSource1 + (target - sourcesNum1) * edgesPerSource2;
					int offset = source - target - 1 + n;
					assert 0 <= offset && offset < edgesPerSource2;
					return base + offset;
				}
			} else {
				final int edgesPerSource = (n - 1) / 2;
				if (source + edgesPerSource >= target) {
					int base = edgesPerSource * source;
					int offset = target - source - 1;
					assert 0 <= offset && offset < edgesPerSource;
					return base + offset;
				} else {
					assert target + edgesPerSource >= n;
					assert target + edgesPerSource - n >= source;
					int base = edgesPerSource * target;
					int offset = source - target - 1 + n;
					assert 0 <= offset && offset < edgesPerSource;
					return base + offset;
				}
			}
		}

		@Override
		public int edgeSource(int edge) {
			checkEdge(edge);
			if (n % 2 == 0) {
				final int edgesPerSource1 = n / 2;
				final int edgesPerSource2 = n / 2 - 1;
				final int sourcesNum1 = n / 2;

				int threshold = sourcesNum1 * edgesPerSource1;
				if (edge < threshold) {
					return edge / edgesPerSource1;
				} else {
					return sourcesNum1 + (edge - threshold) / edgesPerSource2;
				}
			} else {
				final int edgesPerSource = (n - 1) / 2;
				return edge / edgesPerSource;
			}
		}

		@Override
		public int edgeTarget(int edge) {
			checkEdge(edge);
			if (n % 2 == 0) {
				final int edgesPerSource1 = n / 2;
				final int edgesPerSource2 = n / 2 - 1;
				final int sourcesNum1 = n / 2;

				int threshold = sourcesNum1 * edgesPerSource1;
				if (edge < threshold) {
					int source = edge / edgesPerSource1;
					int offset = edge % edgesPerSource1;
					return offset + source + 1;
				} else {
					int source = sourcesNum1 + (edge - threshold) / edgesPerSource2;
					int offset = (edge - threshold) % edgesPerSource2;
					int target = offset + source + 1;
					return target < n ? target : target - n;
				}
			} else {
				final int edgesPerSource = (n - 1) / 2;
				int source = edge / edgesPerSource;
				int offset = edge % edgesPerSource;
				int target = offset + source + 1;
				return target < n ? target : target - n;
			}
		}

		@Override
		public void reverseEdge(int edge) {
			checkEdge(edge);
			// do nothing
		}

		@Override
		public GraphCapabilities getCapabilities() {
			return Capabilities;
		}

		private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newUndirected().vertexAdd(false)
				.vertexRemove(false).edgeAdd(false).edgeRemove(false).parallelEdges(false).selfEdges(false).build();

		@Override
		public IndexGraph copy() {
			return new CompleteGraphUndirected(this);
		}

		@Override
		public EdgeSet edgesOut(int source) {
			return new GraphBase.EdgeSetOutUndirected(source) {
				@Override
				public EdgeIter iterator() {
					return edgesOutIter(source);
				}

				@Override
				public int size() {
					return n - 1;
				}
			};
		}

		@Override
		public EdgeSet edgesIn(int target) {
			return new GraphBase.EdgeSetInUndirected(target) {
				@Override
				public EdgeIter iterator() {
					return edgesInIter(target);
				}

				@Override
				public int size() {
					return n - 1;
				}
			};
		}

	}

	private static class CompleteGraphDirected extends CompleteGraph {

		CompleteGraphDirected(int verticesNum) {
			super(verticesNum, verticesNum * (verticesNum - 1));
		}

		CompleteGraphDirected(CompleteGraphDirected g) {
			super(g);
		}

		@Override
		public int getEdge(int source, int target) {
			checkVertex(source);
			checkVertex(target);
			if (source == target)
				return -1;

			int base = source * (n - 1);
			int offset = target < source ? target : target - 1;
			return base + offset;
		}

		@Override
		public int edgeSource(int edge) {
			checkEdge(edge);
			return edge / (n - 1);
		}

		@Override
		public int edgeTarget(int edge) {
			checkEdge(edge);
			int source = edge / (n - 1);
			int target = edge % (n - 1);
			return target < source ? target : target + 1;
		}

		@Override
		public void reverseEdge(int edge) {
			checkEdge(edge);
			throw new UnsupportedOperationException();
		}

		@Override
		public GraphCapabilities getCapabilities() {
			return Capabilities;
		}

		private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newDirected().vertexAdd(false)
				.vertexRemove(false).edgeAdd(false).edgeRemove(false).parallelEdges(false).selfEdges(false).build();

		@Override
		public IndexGraph copy() {
			return new CompleteGraphDirected(this);
		}

		@Override
		public EdgeSet edgesOut(int source) {
			return new GraphBase.EdgeSetOutDirected(source) {
				@Override
				public EdgeIter iterator() {
					return edgesOutIter(source);
				}

				@Override
				public int size() {
					return n - 1;
				}
			};
		}

		@Override
		public EdgeSet edgesIn(int target) {
			return new GraphBase.EdgeSetInDirected(target) {
				@Override
				public EdgeIter iterator() {
					return edgesInIter(target);
				}

				@Override
				public int size() {
					return n - 1;
				}
			};
		}

	}

	private static abstract class CompleteGraph extends GraphBase implements IndexGraph {

		final int n, m;
		private final WeightsImpl.Manager verticesWeights;
		private final WeightsImpl.Manager edgesWeights;

		CompleteGraph(int n, int m) {
			super(new IDStrategyImpl.Continues(n), new IDStrategyImpl.Continues(m));
			if (n < 0 || m < 0)
				throw new IllegalArgumentException();
			this.n = n;
			this.m = m;
			verticesWeights = new WeightsImpl.Manager(n);
			edgesWeights = new WeightsImpl.Manager(m);
		}

		CompleteGraph(CompleteGraph g) {
			super(new IDStrategyImpl.Continues(g.n), new IDStrategyImpl.Continues(g.m));
			this.n = g.n;
			this.m = g.m;
			verticesWeights = new WeightsImpl.Manager(g.verticesWeights, verticesIDStrat, null);
			edgesWeights = new WeightsImpl.Manager(g.edgesWeights, edgesIDStrat, null);
		}

		void checkVertex(int vertex) {
			if (!(0 <= vertex && vertex < n))
				throw new IndexOutOfBoundsException(vertex);
		}

		void checkEdge(int edge) {
			if (!(0 <= edge && edge < m))
				throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public int addVertex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVertex(int vertex) {
			checkVertex(vertex);
			throw new UnsupportedOperationException();
		}

		EdgeIter edgesOutIter(int source) {
			checkVertex(source);
			return new EdgeIterImpl() {
				int nextTarget = 0;
				int target = -1;

				@Override
				public boolean hasNext() {
					for (; nextTarget < n; nextTarget++)
						if (nextTarget != source)
							return true;
					return false;
				}

				@Override
				public int nextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					target = nextTarget;
					nextTarget++;
					return getEdge(source, target);
				}

				@Override
				public int peekNext() {
					if (!hasNext())
						throw new NoSuchElementException();
					return getEdge(source, nextTarget);
				}

				@Override
				public int source() {
					return source;
				}

				@Override
				public int target() {
					return target;
				}
			};
		}

		EdgeIter edgesInIter(int target) {
			checkVertex(target);
			return new EdgeIterImpl() {
				int nextSource = 0;
				int source = -1;

				@Override
				public boolean hasNext() {
					for (; nextSource < n; nextSource++)
						if (nextSource != target)
							return true;
					return false;
				}

				@Override
				public int nextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					source = nextSource;
					nextSource++;
					return getEdge(source, target);
				}

				@Override
				public int peekNext() {
					if (!hasNext())
						throw new NoSuchElementException();
					return getEdge(source, nextSource);
				}

				@Override
				public int source() {
					return source;
				}

				@Override
				public int target() {
					return target;
				}
			};
		}

		private class EdgeSetSourceTarget extends AbstractIntSet implements EdgeSet {

			private final int source, target;

			EdgeSetSourceTarget(int source, int target) {
				this.source = source;
				this.target = target;
			}

			@Override
			public boolean contains(int edge) {
				return getEdge(source, target) == edge;
			}

			@Override
			public int size() {
				return 1;
			}

			@Override
			public EdgeIter iterator() {
				return new EdgeIterImpl() {

					boolean beforeNext = true;

					@Override
					public boolean hasNext() {
						return beforeNext;
					}

					@Override
					public int nextInt() {
						if (!hasNext())
							throw new NoSuchElementException();
						beforeNext = false;
						return getEdge(source, target);
					}

					@Override
					public int peekNext() {
						if (!hasNext())
							throw new NoSuchElementException();
						return getEdge(source, target);
					}

					@Override
					public int source() {
						return source;
					}

					@Override
					public int target() {
						return target;
					}
				};
			}

		}

		@Override
		public EdgeSet getEdges(int source, int target) {
			checkVertex(source);
			checkVertex(target);
			return new EdgeSetSourceTarget(source, target);
		}

		@Override
		public int addEdge(int source, int target) {
			checkVertex(source);
			checkVertex(target);
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeEdge(int edge) {
			checkEdge(edge);
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clearEdges() {
			throw new UnsupportedOperationException();
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
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			IDStrategyImpl idStrat = (IDStrategyImpl) getVerticesIDStrategy();
			DataContainer<V> container = DataContainer.newInstance(idStrat, type, defVal);
			Weights<?> weights = WeightsImpl.wrapContainerDirected(container);
			verticesWeights.addWeights(key, weights);
			@SuppressWarnings("unchecked")
			WeightsT weights0 = (WeightsT) weights;
			return weights0;
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			IDStrategyImpl idStrat = (IDStrategyImpl) getEdgesIDStrategy();
			DataContainer<E> container = DataContainer.newInstance(idStrat, type, defVal);
			Weights<?> weights = WeightsImpl.wrapContainerDirected(container);
			edgesWeights.addWeights(key, weights);
			@SuppressWarnings("unchecked")
			WeightsT weights0 = (WeightsT) weights;
			return weights0;
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
			return (IDStrategy.Continues) super.getVerticesIDStrategy();
		}

		@Override
		public IDStrategy.Continues getEdgesIDStrategy() {
			return (IDStrategy.Continues) super.getEdgesIDStrategy();
		}

		@Override
		public IndexGraph indexGraph() {
			return this;
		}

		@Override
		public IndexGraphMap indexGraphVerticesMap() {
			return GraphsUtils.IndexGraphMapIdentify;
		}

		@Override
		public IndexGraphMap indexGraphEdgesMap() {
			return GraphsUtils.IndexGraphMapIdentify;
		}
	}

	/**
	 * Create a new undirected complete graph with a fixed number of vertices.
	 * <p>
	 * Given a set of vertices \(V\), the complete graph \(G=(V,E)\) is the graph with the edges set \(E=\{\{u,v\} \mid
	 * u,v \in V, u \neq v \}\), namely there is a single edge between any pair of vertices \(u,v\). The created graph
	 * will have a fixed number of vertices \(n\), and a fixed number of edges \({n \choose 2}\). No vertex or edge can
	 * be removed or added, but weights can be added. This graph is useful in cases where all edges exists, but we want
	 * to avoid using \(O(n^2)\) memory, for example for metric TSP, where each two vertices (points in a 2D world) are
	 * connected by an edge.
	 *
	 * @param  numberOfVertices the number of vertices in the graph. Note that its impossible to change the number of
	 *                              vertices after the graph was created.
	 * @return                  a new undirected complete graph
	 */
	public static IndexGraph newCompleteGraphUndirected(int numberOfVertices) {
		return new CompleteGraphUndirected(numberOfVertices);
	}

	/**
	 * Create a new directed complete graph with a fixed number of vertices.
	 * <p>
	 * Given a set of vertices \(V\), the complete graph \(G=(V,E)\) is the graph with the edges set \(E=\{(u,v) \mid
	 * u,v \in V, u \neq v \}\), namely there are two edges between any pair of vertices \(u,v\) where one is the
	 * reverse of the other. The created graph will have a fixed number of vertices \(n\), and a fixed number of edges
	 * \(2 {n \choose 2}\) (the additional factor of \(2\) is due to the directiveness of the edges). No vertex or edge
	 * can be removed or added, but weights can be added. This graph is useful in cases where all edges exists, but we
	 * want to avoid using \(O(n^2)\) memory, for example for metric TSP, where each two vertices (points in a 2D world)
	 * are connected by an edge.
	 *
	 * @param  numberOfVertices the number of vertices in the graph. Note that its impossible to change the number of
	 *                              vertices after the graph was created.
	 * @return                  a new directed complete graph
	 */
	public static IndexGraph newCompleteGraphDirected(int numberOfVertices) {
		return new CompleteGraphDirected(numberOfVertices);
	}

	private static class UnmodifiableGraph implements Graph {

		private final Graph g;

		UnmodifiableGraph(Graph g) {
			this.g = Objects.requireNonNull(g);
		}

		@Override
		public IntSet vertices() {
			return g.vertices();
		}

		@Override
		public IntSet edges() {
			return g.edges();
		}

		@Override
		public int addVertex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVertex(int vertex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public EdgeSet edgesOut(int source) {
			return new UnmodifiableEdgeSet(g.edgesOut(source));
		}

		@Override
		public EdgeSet edgesIn(int target) {
			return new UnmodifiableEdgeSet(g.edgesIn(target));
		}

		@Override
		public EdgeSet getEdges(int source, int target) {
			return new UnmodifiableEdgeSet(g.getEdges(source, target));
		}

		@Override
		public int addEdge(int source, int target) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeEdge(int edge) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reverseEdge(int edge) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int edgeSource(int edge) {
			return g.edgeSource(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return g.edgeTarget(edge);
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clearEdges() {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			return (WeightsT) ((WeightsImpl<V>) g.getVerticesWeights(key)).unmodifiable();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVerticesWeights(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<Object> getVerticesWeightKeys() {
			return g.getVerticesWeightKeys();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return (WeightsT) ((WeightsImpl<E>) g.getEdgesWeights(key)).unmodifiable();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeEdgesWeights(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return g.getEdgesWeightsKeys();
		}

		@Override
		public GraphCapabilities getCapabilities() {
			return g.getCapabilities();
		}

		@Override
		public Graph copy() {
			return g.copy();
		}

		@Override
		public IndexGraph indexGraph() {
			return this instanceof IndexGraph ? (IndexGraph) this : Graphs.unmodifiableView(g.indexGraph());
		}

		@Override
		public IndexGraphMap indexGraphVerticesMap() {
			return g.indexGraphVerticesMap();
		}

		@Override
		public IndexGraphMap indexGraphEdgesMap() {
			return g.indexGraphEdgesMap();
		}

		Graph g() {
			return g;
		}
	}

	private static class UnmodifiableIndexGraph extends UnmodifiableGraph implements IndexGraph {

		UnmodifiableIndexGraph(IndexGraph g) {
			super(g);
		}

		@Override
		IndexGraph g() { // TOOD rename
			return (IndexGraph) super.g();
		}

		@Override
		public IndexGraph copy() {
			return g().copy();
		}

		@Override
		public IDStrategy.Continues getVerticesIDStrategy() {
			return g().getVerticesIDStrategy();
		}

		@Override
		public IDStrategy.Continues getEdgesIDStrategy() {
			return g().getEdgesIDStrategy();
		}

	}

	private static class UnmodifiableEdgeSet extends AbstractIntSet implements EdgeSet {

		private final EdgeSet set;

		UnmodifiableEdgeSet(EdgeSet set) {
			this.set = Objects.requireNonNull(set);
		}

		@Override
		public boolean contains(int edge) {
			return set.contains(edge);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public EdgeIter iterator() {
			return new UnmodifiableEdgeIter(set.iterator());
		}

	}

	private static class UnmodifiableEdgeIter implements EdgeIterImpl {
		private final EdgeIterImpl it;

		UnmodifiableEdgeIter(EdgeIter it) {
			this.it = (EdgeIterImpl) Objects.requireNonNull(it);
		}

		@Override
		public int source() {
			return it.source();
		}

		@Override
		public int target() {
			return it.target();
		}

		@Override
		public int nextInt() {
			return it.nextInt();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int peekNext() {
			return it.peekNext();
		}
	}

	static Graph unmodifiableView(Graph g) {
		if (g instanceof IndexGraph)
			return unmodifiableView((IndexGraph) g);
		return g instanceof UnmodifiableGraph ? (UnmodifiableGraph) g : new UnmodifiableGraph(g);
	}

	static IndexGraph unmodifiableView(IndexGraph g) {
		return g instanceof UnmodifiableIndexGraph ? (UnmodifiableIndexGraph) g : new UnmodifiableIndexGraph(g);
	}

	private static class ReverseGraph implements Graph {

		private final Graph g; // TODO rename

		ReverseGraph(Graph g) {
			this.g = Objects.requireNonNull(g);
		}

		Graph graph() {
			return g;
		}

		@Override
		public IntSet vertices() {
			return g.vertices();
		}

		@Override
		public IntSet edges() {
			return g.edges();
		}

		@Override
		public int addVertex() {
			return g.addVertex();
		}

		@Override
		public void removeVertex(int vertex) {
			g.removeVertex(vertex);
		}

		@Override
		public EdgeSet edgesOut(int source) {
			return new ReversedEdgeSet(g.edgesIn(source));
		}

		@Override
		public EdgeSet edgesIn(int target) {
			return new ReversedEdgeSet(g.edgesOut(target));
		}

		@Override
		public EdgeSet getEdges(int source, int target) {
			return new ReversedEdgeSet(g.getEdges(target, source));
		}

		@Override
		public int addEdge(int source, int target) {
			return g.addEdge(target, source);
		}

		@Override
		public void removeEdge(int edge) {
			g.removeEdge(edge);
		}

		@Override
		public int edgeSource(int edge) {
			return g.edgeTarget(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return g.edgeSource(edge);
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
		public GraphCapabilities getCapabilities() {
			return g.getCapabilities();
		}

		@Override
		public void reverseEdge(int edge) {
			g.reverseEdge(edge);
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type) {
			return g.addVerticesWeights(key, type);
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			return g.addVerticesWeights(key, type, defVal);
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
			return g.addEdgesWeights(key, type);
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			return g.addEdgesWeights(key, type, defVal);
		}

		@Override
		public IndexGraph indexGraph() {
			return this instanceof IndexGraph ? (IndexGraph) this : Graphs.reverseView(g.indexGraph());
		}

		@Override
		public IndexGraphMap indexGraphVerticesMap() {
			return g.indexGraphVerticesMap();
		}

		@Override
		public IndexGraphMap indexGraphEdgesMap() {
			return g.indexGraphEdgesMap();
		}

		@Override
		public Graph copy() {
			return new ReverseGraph(g.copy());
		}

	}

	private static class ReverseIndexGraph extends ReverseGraph implements IndexGraph {

		ReverseIndexGraph(IndexGraph g) {
			super(g);
		}

		@Override
		IndexGraph graph() {
			return (IndexGraph) super.graph();
		}

		@Override
		public IDStrategy.Continues getVerticesIDStrategy() {
			return graph().getVerticesIDStrategy();
		}

		@Override
		public IDStrategy.Continues getEdgesIDStrategy() {
			return graph().getEdgesIDStrategy();
		}

		@Override
		public IndexGraph copy() {
			return new ReverseIndexGraph(graph().copy());
		}

	}

	private static class ReversedEdgeSet extends AbstractIntSet implements EdgeSet {

		private final EdgeSet set;

		ReversedEdgeSet(EdgeSet set) {
			this.set = Objects.requireNonNull(set);
		}

		@Override
		public boolean contains(int edge) {
			return set.contains(edge);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public boolean remove(int edge) {
			return set.remove(edge);
		}

		@Override
		public void clear() {
			set.clear();
		}

		@Override
		public EdgeIter iterator() {
			return new ReversedEdgeIter(set.iterator());
		}

	}

	private static class ReversedEdgeIter implements EdgeIterImpl {
		final EdgeIterImpl it;

		ReversedEdgeIter(EdgeIter it) {
			this.it = (EdgeIterImpl) it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			return it.nextInt();
		}

		@Override
		public int peekNext() {
			return it.peekNext();
		}

		@Override
		public int source() {
			return it.target();
		}

		@Override
		public int target() {
			return it.source();
		}

		@Override
		public void remove() {
			it.remove();
		}
	}

	static Graph reverseView(Graph g) {
		if (g instanceof IndexGraph)
			return reverseView((IndexGraph) g);
		return g instanceof ReverseGraph ? ((ReverseGraph) g).g : new ReverseGraph(g);
	}

	static IndexGraph reverseView(IndexGraph g) {
		return g instanceof ReverseGraph ? ((ReverseGraph) g).g.indexGraph() : new ReverseIndexGraph(g);
	}

}
