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

	private abstract static class EmptyGraph extends GraphBase implements IndexGraphImpl {

		private final IdStrategy verticesIdStrat = new IdStrategy.Empty();
		private final IdStrategy edgesIdStrat = new IdStrategy.Empty();

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
		public EdgeSet outEdges(int source) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public EdgeSet inEdges(int target) {
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
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVerticesWeights(Object key) {}

		@Override
		public Set<Object> getVerticesWeightsKeys() {
			return Collections.emptySet();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return null;
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
		public IdStrategy getVerticesIdStrategy() {
			return verticesIdStrat;
		}

		@Override
		public IdStrategy getEdgesIdStrategy() {
			return edgesIdStrat;
		}
	}

	private static class EmptyGraphUndirected extends EmptyGraph {

		private static final GraphCapabilities Capabilities =
				GraphCapabilitiesBuilder.newUndirected().parallelEdges(false).selfEdges(false).build();

		@Override
		public GraphCapabilities getCapabilities() {
			return Capabilities;
		}
	}

	private static class EmptyGraphDirected extends EmptyGraph {

		private static final GraphCapabilities Capabilities =
				GraphCapabilitiesBuilder.newDirected().parallelEdges(false).selfEdges(false).build();

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

		private static final GraphCapabilities Capabilities =
				GraphCapabilitiesBuilder.newUndirected().parallelEdges(false).selfEdges(false).build();

		@Override
		public EdgeSet outEdges(int source) {
			checkVertex(source);
			return new GraphBase.EdgeSetOutUndirected(source) {
				@Override
				public EdgeIter iterator() {
					return outEdgesIter(source);
				}

				@Override
				public int size() {
					return n <= 1 ? 0 : n - 1;
				}

				@Override
				public boolean isEmpty() {
					return size() == 0;
				}
			};
		}

		@Override
		public EdgeSet inEdges(int target) {
			checkVertex(target);
			return new GraphBase.EdgeSetInUndirected(target) {
				@Override
				public EdgeIter iterator() {
					return inEdgesIter(target);
				}

				@Override
				public int size() {
					return n <= 1 ? 0 : n - 1;
				}

				@Override
				public boolean isEmpty() {
					return size() == 0;
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

		private static final GraphCapabilities Capabilities =
				GraphCapabilitiesBuilder.newDirected().parallelEdges(false).selfEdges(false).build();

		@Override
		public EdgeSet outEdges(int source) {
			checkVertex(source);
			return new GraphBase.EdgeSetOutDirected(source) {
				@Override
				public EdgeIter iterator() {
					return outEdgesIter(source);
				}

				@Override
				public int size() {
					return n <= 1 ? 0 : n - 1;
				}

				@Override
				public boolean isEmpty() {
					return size() == 0;
				}
			};
		}

		@Override
		public EdgeSet inEdges(int target) {
			checkVertex(target);
			return new GraphBase.EdgeSetInDirected(target) {
				@Override
				public EdgeIter iterator() {
					return inEdgesIter(target);
				}

				@Override
				public int size() {
					return n <= 1 ? 0 : n - 1;
				}

				@Override
				public boolean isEmpty() {
					return size() == 0;
				}
			};
		}

	}

	private static abstract class CompleteGraph extends GraphBase implements IndexGraphImpl {

		final int n, m;
		private final IdStrategy verticesIdStrat;
		private final IdStrategy edgesIdStrat;
		private final WeightsImpl.Index.Manager verticesWeights;
		private final WeightsImpl.Index.Manager edgesWeights;

		CompleteGraph(int n, int m) {
			verticesIdStrat = new IdStrategy.Default(n);
			edgesIdStrat = new IdStrategy.Default(m);
			if (n < 0 || m < 0)
				throw new IllegalArgumentException();
			this.n = n;
			this.m = m;
			verticesWeights = new WeightsImpl.Index.Manager(n);
			edgesWeights = new WeightsImpl.Index.Manager(m);
		}

		CompleteGraph(CompleteGraph g) {
			verticesIdStrat = new IdStrategy.Default(g.n);
			edgesIdStrat = new IdStrategy.Default(g.m);
			this.n = g.n;
			this.m = g.m;
			verticesWeights = new WeightsImpl.Index.Manager(g.verticesWeights, verticesIdStrat);
			edgesWeights = new WeightsImpl.Index.Manager(g.edgesWeights, edgesIdStrat);
		}

		@Override
		public IntSet vertices() {
			return verticesIdStrat.indices();
		}

		@Override
		public IntSet edges() {
			return edgesIdStrat.indices();
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

		EdgeIter outEdgesIter(int source) {
			checkVertex(source);
			return new EdgeIter() {
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

		EdgeIter inEdgesIter(int target) {
			checkVertex(target);
			return new EdgeIter() {
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
					return getEdge(nextSource, target);
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
				return new EdgeIter() {

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
			if (source == target)
				return Edges.EmptyEdgeSet;
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
		public void removeEdgesOf(int vertex) {
			checkVertex(vertex);
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeOutEdgesOf(int vertex) {
			checkVertex(vertex);
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeInEdgesOf(int vertex) {
			checkVertex(vertex);
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
		public Set<Object> getVerticesWeightsKeys() {
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
			WeightsImpl.Index<V> weights = WeightsImpl.Index.newInstance(verticesIdStrat, type, defVal);
			verticesWeights.addWeights(key, weights);
			@SuppressWarnings("unchecked")
			WeightsT weights0 = (WeightsT) weights;
			return weights0;
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			WeightsImpl.Index<E> weights = WeightsImpl.Index.newInstance(edgesIdStrat, type, defVal);
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
		public IdStrategy getVerticesIdStrategy() {
			return verticesIdStrat;
		}

		@Override
		public IdStrategy getEdgesIdStrategy() {
			return edgesIdStrat;
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

	private static class UnmodifiableGraph extends GraphBase {

		private final Graph graph;

		UnmodifiableGraph(Graph g) {
			this.graph = Objects.requireNonNull(g);
		}

		@Override
		public IntSet vertices() {
			return graph.vertices();
		}

		@Override
		public IntSet edges() {
			return graph.edges();
		}

		@Override
		public int addVertex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addVertex(int vertex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVertex(int vertex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public EdgeSet outEdges(int source) {
			return new UnmodifiableEdgeSet(graph.outEdges(source));
		}

		@Override
		public EdgeSet inEdges(int target) {
			return new UnmodifiableEdgeSet(graph.inEdges(target));
		}

		@Override
		public EdgeSet getEdges(int source, int target) {
			return new UnmodifiableEdgeSet(graph.getEdges(source, target));
		}

		@Override
		public int addEdge(int source, int target) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addEdge(int source, int target, int edge) {
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
			return graph.edgeSource(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return graph.edgeTarget(edge);
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
			return (WeightsT) ((WeightsImpl<V>) graph.getVerticesWeights(key)).unmodifiableView();
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
		public Set<Object> getVerticesWeightsKeys() {
			return graph.getVerticesWeightsKeys();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return (WeightsT) ((WeightsImpl<E>) graph.getEdgesWeights(key)).unmodifiableView();
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
			return graph.getEdgesWeightsKeys();
		}

		@Override
		public GraphCapabilities getCapabilities() {
			return graph.getCapabilities();
		}

		@Override
		public Graph copy() {
			return graph.copy();
		}

		@Override
		public IndexGraph indexGraph() {
			return this instanceof IndexGraph ? (IndexGraph) this : Graphs.unmodifiableView(graph.indexGraph());
		}

		@Override
		public IndexIdMap indexGraphVerticesMap() {
			return graph.indexGraphVerticesMap();
		}

		@Override
		public IndexIdMap indexGraphEdgesMap() {
			return graph.indexGraphEdgesMap();
		}

		Graph graph() {
			return graph;
		}
	}

	private static class UnmodifiableIndexGraph extends UnmodifiableGraph implements IndexGraphImpl {

		UnmodifiableIndexGraph(IndexGraph g) {
			super(g);
			if (!(g instanceof IndexGraphImpl))
				throw new IllegalArgumentException("unknown graph implementation");
		}

		@Override
		IndexGraphImpl graph() {
			return (IndexGraphImpl) super.graph();
		}

		@Override
		public IndexGraph copy() {
			return graph().copy();
		}

		@Override
		public IdStrategy getVerticesIdStrategy() {
			return graph().getVerticesIdStrategy();
		}

		@Override
		public IdStrategy getEdgesIdStrategy() {
			return graph().getEdgesIdStrategy();
		}

		@Override
		@Deprecated
		public void addVertex(int vertex) {
			IndexGraphImpl.super.addVertex(vertex);
		}

		@Override
		@Deprecated
		public void addEdge(int source, int target, int edge) {
			IndexGraphImpl.super.addEdge(source, target, edge);
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

	private static class UnmodifiableEdgeIter implements EdgeIter {
		private final EdgeIter it;

		UnmodifiableEdgeIter(EdgeIter it) {
			this.it = Objects.requireNonNull(it);
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

	private static class ReverseGraph extends GraphBase {

		private final Graph graph;

		ReverseGraph(Graph g) {
			this.graph = Objects.requireNonNull(g);
		}

		Graph graph() {
			return graph;
		}

		@Override
		public IntSet vertices() {
			return graph.vertices();
		}

		@Override
		public IntSet edges() {
			return graph.edges();
		}

		@Override
		public int addVertex() {
			return graph.addVertex();
		}

		@Override
		public void addVertex(int vertex) {
			graph.addVertex(vertex);
		}

		@Override
		public void removeVertex(int vertex) {
			graph.removeVertex(vertex);
		}

		@Override
		public EdgeSet outEdges(int source) {
			return new ReversedEdgeSet(graph.inEdges(source));
		}

		@Override
		public EdgeSet inEdges(int target) {
			return new ReversedEdgeSet(graph.outEdges(target));
		}

		@Override
		public EdgeSet getEdges(int source, int target) {
			return new ReversedEdgeSet(graph.getEdges(target, source));
		}

		@Override
		public int addEdge(int source, int target) {
			return graph.addEdge(target, source);
		}

		@Override
		public void addEdge(int source, int target, int edge) {
			graph.addEdge(source, target, edge);
		}

		@Override
		public void removeEdge(int edge) {
			graph.removeEdge(edge);
		}

		@Override
		public int edgeSource(int edge) {
			return graph.edgeTarget(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return graph.edgeSource(edge);
		}

		@Override
		public void clear() {
			graph.clear();
		}

		@Override
		public void clearEdges() {
			graph.clearEdges();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			return graph.getVerticesWeights(key);
		}

		@Override
		public Set<Object> getVerticesWeightsKeys() {
			return graph.getVerticesWeightsKeys();
		}

		@Override
		public void removeVerticesWeights(Object key) {
			graph.removeVerticesWeights(key);
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return graph.getEdgesWeights(key);
		}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return graph.getEdgesWeightsKeys();
		}

		@Override
		public void removeEdgesWeights(Object key) {
			graph.removeEdgesWeights(key);
		}

		@Override
		public GraphCapabilities getCapabilities() {
			return graph.getCapabilities();
		}

		@Override
		public void reverseEdge(int edge) {
			graph.reverseEdge(edge);
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			return graph.addVerticesWeights(key, type, defVal);
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			return graph.addEdgesWeights(key, type, defVal);
		}

		@Override
		public IndexGraph indexGraph() {
			return this instanceof IndexGraph ? (IndexGraph) this : Graphs.reverseView(graph.indexGraph());
		}

		@Override
		public IndexIdMap indexGraphVerticesMap() {
			return graph.indexGraphVerticesMap();
		}

		@Override
		public IndexIdMap indexGraphEdgesMap() {
			return graph.indexGraphEdgesMap();
		}

	}

	private static class ReverseIndexGraph extends ReverseGraph implements IndexGraphImpl {

		ReverseIndexGraph(IndexGraph g) {
			super(g);
			if (!(g instanceof IndexGraphImpl))
				throw new IllegalArgumentException("unknown graph implementation");
		}

		@Override
		IndexGraphImpl graph() {
			return (IndexGraphImpl) super.graph();
		}

		@Override
		public IdStrategy getVerticesIdStrategy() {
			return graph().getVerticesIdStrategy();
		}

		@Override
		public IdStrategy getEdgesIdStrategy() {
			return graph().getEdgesIdStrategy();
		}

		@Override
		@Deprecated
		public void addVertex(int vertex) {
			IndexGraphImpl.super.addVertex(vertex);
		}

		@Override
		@Deprecated
		public void addEdge(int source, int target, int edge) {
			IndexGraphImpl.super.addEdge(source, target, edge);
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

	private static class ReversedEdgeIter implements EdgeIter {
		final EdgeIter it;

		ReversedEdgeIter(EdgeIter it) {
			this.it = it;
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
		return g instanceof ReverseGraph ? ((ReverseGraph) g).graph : new ReverseGraph(g);
	}

	static IndexGraph reverseView(IndexGraph g) {
		return g instanceof ReverseGraph ? ((ReverseGraph) g).graph.indexGraph() : new ReverseIndexGraph(g);
	}

	static String getIndexGraphImpl(IndexGraph g) {
		for (;;) {
			IndexGraph g0 = g;
			if (g instanceof ReverseIndexGraph)
				g = ((ReverseIndexGraph) g).graph();
			if (g instanceof UnmodifiableIndexGraph)
				g = ((UnmodifiableIndexGraph) g).graph();
			if (g instanceof GraphArrayAbstract)
				return "GraphArray";
			if (g instanceof GraphLinkedAbstract)
				return "GraphLinked";
			if (g instanceof GraphTableAbstract)
				return "GraphTable";
			if (g == g0)
				return null;
		}
	}

}
