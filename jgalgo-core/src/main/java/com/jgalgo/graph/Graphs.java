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
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterables;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSets;

/**
 * Static methods class for graphs.
 *
 * @author Barak Ugav
 */
public class Graphs {
	private Graphs() {}

	static <V, E> Graph<V, E> copy(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return factoryForCopy(g).newCopyOf(g, copyVerticesWeights, copyEdgesWeights);
	}

	static <V, E> Graph<V, E> immutableCopy(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return factoryForCopy(g).newImmutableCopyOf(g, copyVerticesWeights, copyEdgesWeights);
	}

	@SuppressWarnings("unchecked")
	private static <V, E> GraphFactory<V, E> factoryForCopy(Graph<V, E> g) {
		GraphFactory<V, E> factory;
		if (g instanceof IndexGraph) {
			factory = (GraphFactory<V, E>) IndexGraphFactory.newInstance(g.isDirected());
		} else if (g instanceof IntGraph) {
			factory = (GraphFactory<V, E>) IntGraphFactory.newInstance(g.isDirected());
		} else {
			factory = GraphFactory.newInstance(g.isDirected());
		}
		if (g.isAllowSelfEdges())
			factory.allowSelfEdges();
		if (g.isAllowParallelEdges())
			factory.allowParallelEdges();
		return factory;
	}

	/**
	 * Tag interface for graphs that can not be muted/changed/altered.
	 *
	 * @author Barak Ugav
	 */
	static interface ImmutableGraph {
	}

	static class EdgeSetSourceTargetSingleEdge extends AbstractIntSet implements IEdgeSet {

		private final IndexGraph g;
		private final int source, target;
		private int edge;
		private static final int EdgeNone = -1;

		EdgeSetSourceTargetSingleEdge(IndexGraph g, int source, int target, int edge) {
			this.g = g;
			this.source = source;
			this.target = target;
			this.edge = edge;
		}

		@Override
		public boolean remove(int edge) {
			if (!contains(edge))
				return false;
			g.removeEdge(edge);
			this.edge = EdgeNone;
			return true;
		}

		@Override
		public boolean contains(int edge) {
			return this.edge != EdgeNone && this.edge == edge;
		}

		@Override
		public int size() {
			return edge != EdgeNone ? 1 : 0;
		}

		@Override
		public void clear() {
			if (edge != EdgeNone) {
				g.removeEdge(edge);
				edge = EdgeNone;
			}
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIters.IBase() {

				boolean beforeNext = edge != EdgeNone;

				@Override
				public boolean hasNext() {
					return beforeNext;
				}

				@Override
				public int nextInt() {
					Assertions.hasNext(this);
					beforeNext = false;
					return edge;
				}

				@Override
				public int peekNextInt() {
					Assertions.hasNext(this);
					return edge;
				}

				@Override
				public int sourceInt() {
					return source;
				}

				@Override
				public int targetInt() {
					return target;
				}

				@Override
				public void remove() {
					if (beforeNext)
						throw new IllegalStateException();
					g.removeEdge(edge);
					edge = EdgeNone;
				}
			};
		}
	}

	/**
	 * Create a new graph that is an induced subgraph of the given graph.
	 *
	 * <p>
	 * An induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' =
	 * \{\{u,v\} \mid u,v \in V', \{u,v\} \in E\}\). The created graph will have the same type (directed/undirected) as
	 * the given graph. The vertices and edges of the created graph will be a subset of the vertices and edges of the
	 * given graph.
	 *
	 * <p>
	 * The weights of both vertices and edges will not be copied to the new sub graph. For more flexible sub graph
	 * creation, see {@link #subGraph(Graph, Collection, Collection, boolean, boolean)}.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned sub graph is also an {@link IntGraph}.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  g        the graph to create a sub graph from
	 * @param  vertices the vertices of the sub graph
	 * @return          a new graph that is an induced subgraph of the given graph
	 */
	public static <V, E> Graph<V, E> subGraph(Graph<V, E> g, Collection<V> vertices) {
		return subGraph(g, Objects.requireNonNull(vertices), null);
	}

	/**
	 * Create a new graph that is a subgraph of the given graph.
	 *
	 * <p>
	 * If {@code edges} is {@code null}, then the created graph will be an induced subgraph of the given graph, namely
	 * an induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' =
	 * \{\{u,v\} \mid u,v \in V', \{u,v\} \in E\}\). The behavior is similar to {@link #subGraph(Graph, Collection)}.
	 * {@code vertices} must not be {@code null} in this case.
	 *
	 * <p>
	 * If {@code vertices} is {@code null}, then {@code edges} must not be {@code null}, and the sub graph will contain
	 * all the vertices which are either a source or a target of an edge in {@code edges}.
	 *
	 * <p>
	 * The created graph will have the same type (directed/undirected) as the given graph. The vertices and edges of the
	 * created graph will be a subset of the vertices and edges of the given graph.
	 *
	 * <p>
	 * The weights of both vertices and edges will not be copied to the new sub graph. For more flexible sub graph
	 * creation, see {@link #subGraph(Graph, Collection, Collection, boolean, boolean)}.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned sub graph is also an {@link IntGraph}.
	 *
	 * @param  <V>                  the vertices type
	 * @param  <E>                  the edges type
	 * @param  g                    the graph to create a sub graph from
	 * @param  vertices             the vertices of the sub graph, if {@code null} then {@code edges} must not be
	 *                                  {@code null} and the vertices of the sub graph will be all the vertices which
	 *                                  are either a source or a target of an edge in {@code edges}
	 * @param  edges                the edges of the sub graph, if {@code null} then {@code vertices} must not be
	 *                                  {@code null} and the sub graph will be an induced subgraph of the given graph
	 * @return                      a new graph that is a subgraph of the given graph
	 * @throws NullPointerException if both {@code vertices} and {@code edges} are {@code null}
	 */
	public static <V, E> Graph<V, E> subGraph(Graph<V, E> g, Collection<V> vertices, Collection<E> edges) {
		return subGraph(g, vertices, edges, false, false);
	}

	/**
	 * Create a new graph that is a subgraph of the given graph, with option to copy weights.
	 *
	 * <p>
	 * If {@code edges} is {@code null}, then the created graph will be an induced subgraph of the given graph, namely
	 * an induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' =
	 * \{\{u,v\} \mid u,v \in V', \{u,v\} \in E\}\). The behavior is similar to {@link #subGraph(Graph, Collection)}.
	 * {@code vertices} must not be {@code null} in this case.
	 *
	 * <p>
	 * If {@code vertices} is {@code null}, then {@code edges} must not be {@code null}, and the sub graph will contain
	 * all the vertices which are either a source or a target of an edge in {@code edges}.
	 *
	 * <p>
	 * The created graph will have the same type (directed/undirected) as the given graph. The vertices and edges of the
	 * created graph will be a subset of the vertices and edges of the given graph.
	 *
	 * <p>
	 * An additional parameter options for copying the weights of the vertices and edges of the given graph to the new
	 * sub graph are provided. If {@code copyVerticesWeights} is {@code true}, then all the vertices weights of the
	 * given graph will be copied to the new sub graph. If {@code copyEdgesWeights} is {@code true}, then all the edges
	 * weights of the given graph will be copied to the new sub graph.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned sub graph is also an {@link IntGraph}.
	 *
	 * @param  <V>                  the vertices type
	 * @param  <E>                  the edges type
	 * @param  g                    the graph to create a sub graph from
	 * @param  vertices             the vertices of the sub graph, if {@code null} then {@code edges} must not be
	 *                                  {@code null} and the vertices of the sub graph will be all the vertices which
	 *                                  are either a source or a target of an edge in {@code edges}
	 * @param  edges                the edges of the sub graph, if {@code null} then {@code vertices} must not be
	 *                                  {@code null} and the sub graph will be an induced subgraph of the given graph
	 * @param  copyVerticesWeights  if {@code true} then all the vertices weights of the given graph will be copied to
	 *                                  the new sub graph
	 * @param  copyEdgesWeights     if {@code true} then all the edges weights of the given graph will be copied to the
	 *                                  new sub graph
	 * @return                      a new graph that is a subgraph of the given graph
	 * @throws NullPointerException if both {@code vertices} and {@code edges} are {@code null}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "cast" })
	public static <V, E> Graph<V, E> subGraph(Graph<V, E> g, Collection<V> vertices, Collection<E> edges,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		if (g instanceof IntGraph) {
			IntCollection vs = vertices == null ? null : IntAdapters.asIntCollection((Collection<Integer>) vertices);
			IntCollection es = edges == null ? null : IntAdapters.asIntCollection((Collection<Integer>) edges);
			return (Graph<V, E>) subGraph((IntGraph) g, vs, es, copyVerticesWeights, copyEdgesWeights);
		}
		if (vertices == null && edges == null)
			throw new NullPointerException("Either vertices or edges can be null, not both.");

		GraphBuilder<V, E> gb = GraphBuilder.newInstance(g.isDirected());
		IndexGraph ig = g.indexGraph();
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		IndexIdMap<E> eiMap = g.indexGraphEdgesMap();

		IntCollection verticesIndices;
		if (vertices == null) {
			verticesIndices = new IntOpenHashSet();
			for (E e : edges) {
				int eIdx = eiMap.idToIndex(e);
				verticesIndices.add(ig.edgeSource(eIdx));
				verticesIndices.add(ig.edgeTarget(eIdx));
			}
			vertices = IndexIdMaps.indexToIdCollection(verticesIndices, viMap);
		} else {
			verticesIndices = IndexIdMaps.idToIndexCollection(vertices, viMap);
		}
		gb.addVertices(vertices);

		if (edges != null) {
			gb.ensureEdgeCapacity(edges.size());
			for (E e : edges) {
				int eIdx = eiMap.idToIndex(e);
				int uIdx = ig.edgeSource(eIdx), vIdx = ig.edgeTarget(eIdx);
				V u = viMap.indexToId(uIdx), v = viMap.indexToId(vIdx);
				gb.addEdge(u, v, e);
			}
		} else {
			if (g.isDirected()) {
				for (int uIdx : verticesIndices) {
					V u = viMap.indexToId(uIdx);
					for (IEdgeIter eit = ig.outEdges(uIdx).iterator(); eit.hasNext();) {
						int eIdx = eit.nextInt();
						int vIdx = eit.targetInt();
						if (verticesIndices.contains(vIdx)) {
							E e = eiMap.indexToId(eIdx);
							V v = viMap.indexToId(vIdx);
							gb.addEdge(u, v, e);
						}
					}
				}
			} else {
				for (int uIdx : verticesIndices) {
					V u = viMap.indexToId(uIdx);
					for (IEdgeIter eit = ig.outEdges(uIdx).iterator(); eit.hasNext();) {
						int eIdx = eit.nextInt();
						int vIdx = eit.targetInt();
						if (uIdx <= vIdx && verticesIndices.contains(vIdx)) {
							E e = eiMap.indexToId(eIdx);
							V v = viMap.indexToId(vIdx);
							gb.addEdge(u, v, e);
						}
					}
				}
			}
		}

		if (copyVerticesWeights) {
			for (String key : g.verticesWeightsKeys()) {
				Weights wSrc = g.verticesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				Weights wDst = gb.addVerticesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.vertices());
			}
		}
		if (copyEdgesWeights) {
			for (String key : g.edgesWeightsKeys()) {
				Weights wSrc = g.edgesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				Weights wDst = gb.addEdgesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.edges());
			}
		}

		return gb.build();
	}

	/**
	 * Create a new graph that is a subgraph of the given int graph, with option to copy weights.
	 *
	 * <p>
	 * If {@code edges} is {@code null}, then the created graph will be an induced subgraph of the given graph, namely
	 * an induced subgraph of a graph \(G=(V,E)\) is a graph \(G'=(V',E')\) where \(V' \subseteq V\) and \(E' =
	 * \{\{u,v\} \mid u,v \in V', \{u,v\} \in E\}\).
	 *
	 * <p>
	 * If {@code vertices} is {@code null}, then {@code edges} must not be {@code null}, and the sub graph will contain
	 * all the vertices which are either a source or a target of an edge in {@code edges}.
	 *
	 * <p>
	 * The created graph will have the same type (directed/undirected) as the given graph. The vertices and edges of the
	 * created graph will be a subset of the vertices and edges of the given graph.
	 *
	 * <p>
	 * An additional parameter options for copying the weights of the vertices and edges of the given graph to the new
	 * sub graph are provided. If {@code copyVerticesWeights} is {@code true}, then all the vertices weights of the
	 * given graph will be copied to the new sub graph. If {@code copyEdgesWeights} is {@code true}, then all the edges
	 * weights of the given graph will be copied to the new sub graph.
	 *
	 * @param  g                    the graph to create a sub graph from
	 * @param  vertices             the vertices of the sub graph, if {@code null} then {@code edges} must not be
	 *                                  {@code null} and the vertices of the sub graph will be all the vertices which
	 *                                  are either a source or a target of an edge in {@code edges}
	 * @param  edges                the edges of the sub graph, if {@code null} then {@code vertices} must not be
	 *                                  {@code null} and the sub graph will be an induced subgraph of the given graph
	 * @param  copyVerticesWeights  if {@code true} then all the vertices weights of the given graph will be copied to
	 *                                  the new sub graph
	 * @param  copyEdgesWeights     if {@code true} then all the edges weights of the given graph will be copied to the
	 *                                  new sub graph
	 * @return                      a new graph that is a subgraph of the given graph
	 * @throws NullPointerException if both {@code vertices} and {@code edges} are {@code null}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "cast" })
	public static IntGraph subGraph(IntGraph g, IntCollection vertices, IntCollection edges,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		if (vertices == null && edges == null)
			throw new NullPointerException("Either vertices or edges can be null, not both.");
		if (g instanceof IndexGraph)
			return subGraphCopy((IndexGraph) g, vertices, edges, copyVerticesWeights, copyEdgesWeights);

		IntGraphBuilder gb = IntGraphBuilder.newInstance(g.isDirected());
		IndexGraph ig = g.indexGraph();
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		IndexIntIdMap eiMap = g.indexGraphEdgesMap();

		IntCollection verticesIndices;
		if (vertices == null) {
			verticesIndices = new IntOpenHashSet();
			for (int e : edges) {
				int eIdx = eiMap.idToIndex(e);
				verticesIndices.add(ig.edgeSource(eIdx));
				verticesIndices.add(ig.edgeTarget(eIdx));
			}
			vertices = IndexIdMaps.indexToIdCollection(verticesIndices, viMap);
		} else {
			verticesIndices = IndexIdMaps.idToIndexCollection(vertices, viMap);
		}
		gb.addVertices(vertices);

		if (edges != null) {
			gb.ensureEdgeCapacity(edges.size());
			for (int e : edges) {
				int eIdx = eiMap.idToIndex(e);
				int uIdx = ig.edgeSource(eIdx), vIdx = ig.edgeTarget(eIdx);
				int u = viMap.indexToIdInt(uIdx), v = viMap.indexToIdInt(vIdx);
				gb.addEdge(u, v, e);
			}
		} else {
			if (g.isDirected()) {
				for (int uIdx : verticesIndices) {
					int u = viMap.indexToIdInt(uIdx);
					for (IEdgeIter eit = ig.outEdges(uIdx).iterator(); eit.hasNext();) {
						int eIdx = eit.nextInt();
						int vIdx = eit.targetInt();
						if (verticesIndices.contains(vIdx)) {
							int e = eiMap.indexToIdInt(eIdx);
							int v = viMap.indexToIdInt(vIdx);
							gb.addEdge(u, v, e);
						}
					}
				}
			} else {
				for (int uIdx : verticesIndices) {
					int u = viMap.indexToIdInt(uIdx);
					for (IEdgeIter eit = ig.outEdges(uIdx).iterator(); eit.hasNext();) {
						int eIdx = eit.nextInt();
						int vIdx = eit.targetInt();
						if (uIdx <= vIdx && verticesIndices.contains(vIdx)) {
							int e = eiMap.indexToIdInt(eIdx);
							int v = viMap.indexToIdInt(vIdx);
							gb.addEdge(u, v, e);
						}
					}
				}
			}
		}

		if (copyVerticesWeights) {
			for (String key : g.verticesWeightsKeys()) {
				IWeights wSrc = g.verticesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				IWeights wDst = (IWeights) gb.addVerticesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.vertices());
			}
		}
		if (copyEdgesWeights) {
			for (String key : g.edgesWeightsKeys()) {
				IWeights wSrc = g.edgesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				IWeights wDst = (IWeights) gb.addEdgesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.edges());
			}
		}

		return gb.build();
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "cast" })
	private static IntGraph subGraphCopy(IndexGraph g, IntCollection vertices, IntCollection edges,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		IntGraphBuilder gb = IntGraphBuilder.newInstance(g.isDirected());

		if (vertices == null) {
			vertices = new IntOpenHashSet();
			for (int e : edges) {
				vertices.add(g.edgeSource(e));
				vertices.add(g.edgeTarget(e));
			}
		}
		gb.addVertices(vertices);

		if (edges != null) {
			gb.ensureEdgeCapacity(edges.size());
			for (int e : edges)
				gb.addEdge(g.edgeSource(e), g.edgeTarget(e), e);
		} else {
			if (g.isDirected()) {
				for (int u : vertices) {
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.targetInt();
						if (vertices.contains(v))
							gb.addEdge(u, v, e);
					}
				}
			} else {
				for (int u : vertices) {
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.targetInt();
						if (u <= v && vertices.contains(v))
							gb.addEdge(u, v, e);
					}
				}
			}
		}

		if (copyVerticesWeights) {
			for (String key : g.verticesWeightsKeys()) {
				IWeights wSrc = g.verticesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				IWeights wDst = (IWeights) gb.addVerticesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.vertices());
			}
		}
		if (copyEdgesWeights) {
			for (String key : g.edgesWeightsKeys()) {
				IWeights wSrc = g.edgesWeights(key);
				Class<?> type = (Class) getWeightsType(wSrc);
				IWeights wDst = (IWeights) gb.addEdgesWeights(key, (Class) type, wSrc.defaultWeightAsObj());
				copyWeights(wSrc, wDst, type, gb.edges());
			}
		}

		return gb.build();
	}

	@SuppressWarnings("unchecked")
	private static <K> void copyWeights(Weights<K, ?> src, Weights<K, ?> dst, Class<?> type, Collection<K> elements) {
		if (type == byte.class) {
			WeightsByte<K> src0 = (WeightsByte<K>) src;
			WeightsByte<K> dst0 = (WeightsByte<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == short.class) {
			WeightsShort<K> src0 = (WeightsShort<K>) src;
			WeightsShort<K> dst0 = (WeightsShort<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == int.class) {
			WeightsInt<K> src0 = (WeightsInt<K>) src;
			WeightsInt<K> dst0 = (WeightsInt<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == long.class) {
			WeightsLong<K> src0 = (WeightsLong<K>) src;
			WeightsLong<K> dst0 = (WeightsLong<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == float.class) {
			WeightsFloat<K> src0 = (WeightsFloat<K>) src;
			WeightsFloat<K> dst0 = (WeightsFloat<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == double.class) {
			WeightsDouble<K> src0 = (WeightsDouble<K>) src;
			WeightsDouble<K> dst0 = (WeightsDouble<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == boolean.class) {
			WeightsBool<K> src0 = (WeightsBool<K>) src;
			WeightsBool<K> dst0 = (WeightsBool<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == char.class) {
			WeightsChar<K> src0 = (WeightsChar<K>) src;
			WeightsChar<K> dst0 = (WeightsChar<K>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		} else {
			assert type == Object.class;
			WeightsObj<K, Object> src0 = (WeightsObj<K, Object>) src;
			WeightsObj<K, Object> dst0 = (WeightsObj<K, Object>) dst;
			for (K elm : elements)
				dst0.set(elm, src0.get(elm));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void copyWeights(IWeights<?> src, IWeights<?> dst, Class<?> type, IntCollection elements) {
		if (type == byte.class) {
			IWeightsByte src0 = (IWeightsByte) src;
			IWeightsByte dst0 = (IWeightsByte) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == short.class) {
			IWeightsShort src0 = (IWeightsShort) src;
			IWeightsShort dst0 = (IWeightsShort) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == int.class) {
			IWeightsInt src0 = (IWeightsInt) src;
			IWeightsInt dst0 = (IWeightsInt) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == long.class) {
			IWeightsLong src0 = (IWeightsLong) src;
			IWeightsLong dst0 = (IWeightsLong) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == float.class) {
			IWeightsFloat src0 = (IWeightsFloat) src;
			IWeightsFloat dst0 = (IWeightsFloat) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == double.class) {
			IWeightsDouble src0 = (IWeightsDouble) src;
			IWeightsDouble dst0 = (IWeightsDouble) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == boolean.class) {
			IWeightsBool src0 = (IWeightsBool) src;
			IWeightsBool dst0 = (IWeightsBool) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else if (type == char.class) {
			IWeightsChar src0 = (IWeightsChar) src;
			IWeightsChar dst0 = (IWeightsChar) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		} else {
			assert type == Object.class;
			IWeightsObj src0 = (IWeightsObj) src;
			IWeightsObj dst0 = (IWeightsObj) dst;
			for (int elm : elements)
				dst0.set(elm, src0.get(elm));
		}
	}

	private static Class<?> getWeightsType(Weights<?, ?> w) {
		if (w instanceof WeightsByte)
			return byte.class;
		if (w instanceof WeightsShort)
			return short.class;
		if (w instanceof WeightsInt)
			return int.class;
		if (w instanceof WeightsLong)
			return long.class;
		if (w instanceof WeightsFloat)
			return float.class;
		if (w instanceof WeightsDouble)
			return double.class;
		if (w instanceof WeightsBool)
			return boolean.class;
		if (w instanceof WeightsChar)
			return char.class;
		assert w instanceof WeightsObj;
		return Object.class;
	}

	/**
	 * Get a random vertex from the given graph.
	 *
	 * @param  <V>                      the vertices type
	 * @param  g                        the graph
	 * @param  rand                     the random number generator
	 * @return                          a random vertex from the given graph
	 * @throws IllegalArgumentException if the graph is contains no vertices
	 */
	public static <V> V randVertex(Graph<V, ?> g, Random rand) {
		int n = g.vertices().size();
		if (n == 0)
			throw new IllegalArgumentException("Can't choose a random graph from a graph without vertices");
		return g.indexGraphVerticesMap().indexToId(rand.nextInt(n));
	}

	/**
	 * Get a random vertex from the given int graph.
	 *
	 * @param  g                        the graph
	 * @param  rand                     the random number generator
	 * @return                          a random vertex from the given graph
	 * @throws IllegalArgumentException if the graph is contains no vertices
	 */
	public static int randVertex(IntGraph g, Random rand) {
		int n = g.vertices().size();
		if (n == 0)
			throw new IllegalArgumentException("Can't choose a random graph from a graph without vertices");
		return g.indexGraphVerticesMap().indexToIdInt(rand.nextInt(n));
	}

	/**
	 * Get a random edge from the given graph.
	 *
	 * @param  <E>                      the edges type
	 * @param  g                        the graph
	 * @param  rand                     the random number generator
	 * @return                          a random edge from the given graph
	 * @throws IllegalArgumentException if the graph is contains no edges
	 */
	public static <E> E randEdge(Graph<?, E> g, Random rand) {
		int m = g.edges().size();
		if (m == 0)
			throw new IllegalArgumentException("Can't choose a random graph from a graph without edges");
		return g.indexGraphEdgesMap().indexToId(rand.nextInt(m));
	}

	/**
	 * Get a random edge from the given int graph.
	 *
	 * @param  g                        the graph
	 * @param  rand                     the random number generator
	 * @return                          a random edge from the given graph
	 * @throws IllegalArgumentException if the graph is contains no edges
	 */
	public static int randEdge(IntGraph g, Random rand) {
		int m = g.edges().size();
		if (m == 0)
			throw new IllegalArgumentException("Can't choose a random graph from a graph without edges");
		return g.indexGraphEdgesMap().indexToIdInt(rand.nextInt(m));
	}

	@SuppressWarnings("unchecked")
	static boolean isEquals(Graph<?, ?> g1, Graph<?, ?> g2) {
		if (g1 == g2)
			return true;
		if (g1 instanceof IntGraph && g2 instanceof IntGraph)
			return isEquals((IntGraph) g1, (IntGraph) g2);

		if (g1.isDirected() != g2.isDirected())
			return false;
		if (!g1.vertices().equals(g2.vertices()))
			return false;
		if (!g1.edges().equals(g2.edges()))
			return false;
		Graph<Object, Object> g10 = (Graph<Object, Object>) g1, g20 = (Graph<Object, Object>) g2;
		return isEquals0(g10, g20);
	}

	private static <V, E> boolean isEquals0(Graph<V, E> g1, Graph<V, E> g2) {
		if (g1.isDirected()) {
			for (E e : g1.edges())
				if (!g1.edgeSource(e).equals(g2.edgeSource(e)) || !g1.edgeTarget(e).equals(g2.edgeTarget(e)))
					return false;
		} else {
			for (E e : g1.edges()) {
				V s1 = g1.edgeSource(e), t1 = g1.edgeTarget(e);
				V s2 = g2.edgeSource(e), t2 = g2.edgeTarget(e);
				if (!(s1.equals(s2) && t1.equals(t2)) && !(s1.equals(t2) && t1.equals(s2)))
					return false;
			}
		}

		if (!g1.verticesWeightsKeys().equals(g2.verticesWeightsKeys()))
			return false;
		for (String key : g1.verticesWeightsKeys()) {
			Weights<V, ?> w1 = g1.verticesWeights(key), w2 = g2.verticesWeights(key);
			if (!WeightsImpl.isEqual(g1.vertices(), w1, w2))
				return false;
		}
		if (!g1.edgesWeightsKeys().equals(g2.edgesWeightsKeys()))
			return false;
		for (String key : g1.edgesWeightsKeys()) {
			Weights<E, ?> w1 = g1.edgesWeights(key), w2 = g2.edgesWeights(key);
			if (!WeightsImpl.isEqual(g1.edges(), w1, w2))
				return false;
		}

		return true;
	}

	private static boolean isEquals(IntGraph g1, IntGraph g2) {
		if (g1.isDirected() != g2.isDirected())
			return false;
		if (!g1.vertices().equals(g2.vertices()))
			return false;
		if (!g1.edges().equals(g2.edges()))
			return false;
		if (g1.isDirected()) {
			for (int e : g1.edges())
				if (g1.edgeSource(e) != g2.edgeSource(e) || g1.edgeTarget(e) != g2.edgeTarget(e))
					return false;
		} else {
			for (int e : g1.edges()) {
				int s1 = g1.edgeSource(e), t1 = g1.edgeTarget(e);
				int s2 = g2.edgeSource(e), t2 = g2.edgeTarget(e);
				if (!(s1 == s2 && t1 == t2) && !(s1 == t2 && t1 == s2))
					return false;
			}
		}

		if (!g1.verticesWeightsKeys().equals(g2.verticesWeightsKeys()))
			return false;
		for (String key : g1.verticesWeightsKeys()) {
			IWeights<?> w1 = (IWeights<?>) g1.verticesWeights(key), w2 = (IWeights<?>) g2.verticesWeights(key);
			if (!WeightsImpl.isEqual(g1.vertices(), w1, w2))
				return false;
		}
		if (!g1.edgesWeightsKeys().equals(g2.edgesWeightsKeys()))
			return false;
		for (String key : g1.edgesWeightsKeys()) {
			IWeights<?> w1 = (IWeights<?>) g1.edgesWeights(key), w2 = (IWeights<?>) g2.edgesWeights(key);
			if (!WeightsImpl.isEqual(g1.edges(), w1, w2))
				return false;
		}

		return true;
	}

	static <V, E> int hashCode(Graph<V, E> g) {
		if (g instanceof IntGraph)
			return hashCode((IntGraph) g);

		int h = Boolean.hashCode(g.isDirected());
		h += g.vertices().hashCode();
		h += g.edges().hashCode();
		if (g.isDirected()) {
			for (E e : g.edges())
				h += g.edgeSource(e).hashCode() + 31 * g.edgeTarget(e).hashCode();
		} else {
			for (E e : g.edges())
				h += g.edgeSource(e).hashCode() + g.edgeTarget(e).hashCode();
		}
		for (String key : g.verticesWeightsKeys())
			h += WeightsImpl.hashCode(g.vertices(), g.verticesWeights(key));
		for (String key : g.edgesWeightsKeys())
			h += WeightsImpl.hashCode(g.edges(), g.edgesWeights(key));
		return h;
	}

	private static int hashCode(IntGraph g) {
		int h = Boolean.hashCode(g.isDirected());
		h += g.vertices().hashCode();
		h += g.edges().hashCode();
		if (g.isDirected()) {
			for (int e : g.edges())
				h += g.edgeSource(e) + 31 * g.edgeTarget(e);
		} else {
			for (int e : g.edges())
				h += g.edgeSource(e) + g.edgeTarget(e);
		}
		for (String key : g.verticesWeightsKeys())
			h += WeightsImpl.hashCode(g.vertices(), g.verticesWeights(key));
		for (String key : g.edgesWeightsKeys())
			h += WeightsImpl.hashCode(g.edges(), g.edgesWeights(key));
		return h;
	}

	static <V, E> String toString(Graph<V, E> g) {
		if (g instanceof IntGraph)
			return toString((IntGraph) g);

		StringBuilder s = new StringBuilder();

		Set<String> verticesWeightsKeys = g.verticesWeightsKeys();
		List<Weights<V, ?>> verticesWeights = new ObjectArrayList<>(verticesWeightsKeys.size());
		for (String key : verticesWeightsKeys)
			verticesWeights.add(g.verticesWeights(key));

		Set<String> edgesWeightsKeys = g.edgesWeightsKeys();
		List<Weights<E, ?>> edgesWeights = new ObjectArrayList<>(edgesWeightsKeys.size());
		for (String key : edgesWeightsKeys)
			edgesWeights.add(g.edgesWeights(key));

		BiConsumer<Collection<Weights<Object, ?>>, Object> appendWeights = (weights, id) -> {
			s.append('{');
			boolean firstData = true;
			for (Weights<Object, ?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.getAsObj(id));
			}
			s.append('}');
		};
		Consumer<V> appendVertexWeights = vertex -> {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Collection<Weights<Object, ?>> weights0 = (Collection) verticesWeights;
			appendWeights.accept(weights0, vertex);
		};
		Consumer<E> appendEdgeWeights = edge -> {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Collection<Weights<Object, ?>> weights0 = (Collection) edgesWeights;
			appendWeights.accept(weights0, edge);
		};

		s.append('{');
		boolean firstVertex = true;
		for (V u : g.vertices()) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}
			s.append(u);
			if (!verticesWeights.isEmpty())
				appendVertexWeights.accept(u);

			s.append(": [");
			boolean firstEdge = true;
			for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
				E e = eit.next();
				V v = eit.target();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append(e).append('(').append(u).append(", ").append(v);
				if (!edgesWeights.isEmpty()) {
					s.append(", ");
					appendEdgeWeights.accept(e);
				}
				s.append(')');
			}
			s.append(']');
		}
		s.append('}');
		return s.toString();
	}

	private static String toString(IntGraph g) {
		StringBuilder s = new StringBuilder();

		Set<String> verticesWeightsKeys = g.verticesWeightsKeys();
		Collection<IWeights<?>> verticesWeights = new ObjectArrayList<>(verticesWeightsKeys.size());
		for (String key : verticesWeightsKeys)
			verticesWeights.add((IWeights<?>) g.verticesWeights(key));

		Set<String> edgesWeightsKeys = g.edgesWeightsKeys();
		Collection<IWeights<?>> edgesWeights = new ObjectArrayList<>(edgesWeightsKeys.size());
		for (String key : edgesWeightsKeys)
			edgesWeights.add((IWeights<?>) g.edgesWeights(key));

		ObjIntConsumer<Collection<IWeights<?>>> appendWeights = (weights, id) -> {
			s.append('{');
			boolean firstData = true;
			for (IWeights<?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.getAsObj(id));
			}
			s.append('}');
		};
		IntConsumer appendVertexWeights = vertex -> appendWeights.accept(verticesWeights, vertex);
		IntConsumer appendEdgeWeights = edge -> appendWeights.accept(edgesWeights, edge);

		s.append('{');
		boolean firstVertex = true;
		for (int u : g.vertices()) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}
			s.append(u);
			if (!verticesWeights.isEmpty())
				appendVertexWeights.accept(u);

			s.append(": [");
			boolean firstEdge = true;
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append(e).append('(').append(u).append(", ").append(v);
				if (!edgesWeights.isEmpty()) {
					s.append(", ");
					appendEdgeWeights.accept(e);
				}
				s.append(')');
			}
			s.append(']');
		}
		s.append('}');
		return s.toString();
	}

	/**
	 * Get a view of all the self edges in a graph.
	 *
	 * <p>
	 * The returned set is a view, namely it will be updated when the graph is updated.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     a view of all the self edges in the graph
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> Set<E> selfEdges(Graph<V, E> g) {
		if (g instanceof IntGraph)
			return (Set<E>) selfEdges((IntGraph) g);
		if (!g.isAllowSelfEdges())
			return ObjectSets.emptySet();
		IntSet indexSelfEdges = selfEdges(g.indexGraph());
		return IndexIdMaps.indexToIdSet(indexSelfEdges, g.indexGraphEdgesMap());
	}

	/**
	 * Get a view of all the self edges in an int graph.
	 *
	 * <p>
	 * The returned set is a view, namely it will be updated when the graph is updated.
	 *
	 * @param  g an int graph
	 * @return   a view of all the self edges in the graph
	 */
	public static IntSet selfEdges(IntGraph g) {
		if (!g.isAllowSelfEdges())
			return IntSets.EMPTY_SET;
		if (g instanceof IndexGraph)
			return selfEdges((IndexGraph) g);
		IntSet indexSelfEdges = selfEdges(g.indexGraph());
		return IndexIdMaps.indexToIdSet(indexSelfEdges, g.indexGraphEdgesMap());
	}

	private static IntSet selfEdges(IndexGraph g) {
		return new AbstractIntSet() {

			@Override
			public boolean contains(int edge) {
				return 0 <= edge && edge < g.edges().size() && g.edgeSource(edge) == g.edgeTarget(edge);
			}

			@Override
			public int size() {
				return (int) IntIterables.size(this);
			}

			@Override
			public boolean isEmpty() {
				return !iterator().hasNext();
			}

			@Override
			public IntIterator iterator() {
				return new IntIterator() {
					final int m = g.edges().size();
					int nextEdge = 0;
					{
						advance();
					}

					private void advance() {
						for (; nextEdge < m; nextEdge++)
							if (g.edgeSource(nextEdge) == g.edgeTarget(nextEdge))
								break;
					}

					@Override
					public boolean hasNext() {
						return nextEdge < m;
					}

					@Override
					public int nextInt() {
						Assertions.hasNext(this);
						int edge = nextEdge++;
						advance();
						return edge;
					}
				};
			}
		};
	}

	/**
	 * Check whether a graph contain parallel edges.
	 *
	 * <p>
	 * Two parallel edges are edges that have the same source and target vertices.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     {@code true} if the graph contain at least one pair of parallel edges, else {@code false}
	 */
	public static <V, E> boolean containsParallelEdges(Graph<V, E> g) {
		if (!g.isAllowParallelEdges())
			return false;
		IndexGraph ig = g.indexGraph();
		final int n = ig.vertices().size();
		int[] lastVisit = new int[n];
		for (int u : range(n)) {
			final int visitIdx = u + 1;
			for (IEdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (lastVisit[v] == visitIdx)
					return true;
				lastVisit[v] = visitIdx;
			}
		}
		return false;
	}

	static IdBuilderInt IndexGraphIdBuilder = existingIds -> existingIds.size();

}
