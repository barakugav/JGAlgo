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
package com.jgalgo.gen;

import static com.jgalgo.internal.util.Range.range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.ToLongBiFunction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Generate the union graph of two or more given graphs.
 *
 * <p>
 * The union graph of two graphs is a graph with all the vertices and edges from both graphs, without duplications. The
 * set of vertices in the union graph is the union of the sets of vertices in the input graphs, as defined in sets
 * theory. As for the edges, there are two ways to define the union:
 * <ul>
 * <li>Union by id: each edge is identified by its id. All edges of the two graphs are added to the union graph, and if
 * an edge with the same id exist in both graph, it must have the same endpoints in both graphs. Otherwise, an exception
 * is thrown. See {@link #edgeUnionById()} for more details.</li>
 * <li>Union by endpoints: Each edge is identified by its endpoints. If an edge with the same endpoints exists in both
 * graphs, it is added to the union graph only once. The edges are added by the order of the input graphs, and the first
 * edge of each equivalent class of endpoints determine the identifier of the edge in the generated graph(s). The input
 * graphs must not contains parallel edges. See {@link #edgeUnionByEndpoints()} for more details.</li>
 * </ul>
 * The above rules generalize to union of more than two graphs.
 *
 * <p>
 * By default, the edges union is by id. Use {@link #edgeUnionByEndpoints()} and {@link #edgeUnionById()} to change the
 * union of edges.
 *
 * <p>
 * Weights are not copied by default. Use {@link #copyWeights(boolean, boolean)} to copy the vertices/edges weights.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class UnionGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private final List<Graph<V, E>> graphs;
	private boolean unionById = true;
	private boolean verticesWeights, edgesWeights;
	private Set<String> verticesWeightsKeys, edgesWeightsKeys;

	/**
	 * Create a new union graph generator that will use the default graph factory.
	 */
	public UnionGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new union graph generator that will use the given graph factory.
	 *
	 * <p>
	 * The graph factory will be used to create the generated graph(s).
	 *
	 * <p>
	 * To generate {@linkplain IntGraph int graphs}, pass an instance of {@linkplain IntGraphFactory} to this
	 * constructor.
	 *
	 * @param factory the graph factory that will be used to create the generated graph(s)
	 */
	public UnionGraphGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
		graphs = new ArrayList<>();
	}

	/**
	 * Get the graph factory that will be used to create the generated graph(s).
	 *
	 * <p>
	 * It's possible to customize the factory before generating the graph(s), for example by using
	 * {@link GraphFactory#addHint(GraphFactory.Hint)} to optimize the generated graph(s) for a specific algorithm.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration. If one of the input graphs have self edges,
	 * the method {@link GraphFactory#allowSelfEdges()} will be called. If one of the input graphs have parallel edges,
	 * or if two edges with the same endpoints and different identifiers exist in the input graphs, the method
	 * {@link GraphFactory#allowParallelEdges()} will be called. Note that parallel edges are not allowed (in each input
	 * graph independently) when union by endpoints is used (see {@link #edgeUnionByEndpoints()}).
	 *
	 * @return the graph factory that will be used to create the generated graph(s)
	 */
	public GraphFactory<V, E> graphFactory() {
		return factory;
	}

	/**
	 * Set the input graphs whose union graph will be generated.
	 *
	 * <p>
	 * Given two or more graphs, the union graph of the graphs is a graph with all the vertices and edges from all the
	 * graphs, without duplications. The set of vertices in the union graph is the union of the sets of vertices in the
	 * input graphs, as defined in sets theory. As for the edges, the union can be done by the
	 * {@linkplain #edgeUnionById() edges identifiers}, or by the {@linkplain #edgeUnionByEndpoints() edges endpoints}
	 * (see the documentation of these methods for more details).
	 *
	 * @param  graphs                   the input graphs
	 * @return                          this generator
	 * @throws IllegalArgumentException if less than two input graphs are provided, or if the graphs have different
	 *                                      directionality
	 */
	public UnionGraphGenerator<V, E> graphs(Collection<? extends Graph<V, E>> graphs) {
		if (graphs.size() < 2)
			throw new IllegalArgumentException("At least two graphs must be provided");
		if (graphs.stream().map(Graph::isDirected).distinct().count() != 1)
			throw new IllegalArgumentException("Input graphs must have the same directionality");
		this.graphs.clear();
		this.graphs.addAll(graphs);
		return this;
	}

	/**
	 * Set the input graphs whose union graph will be generated (variable arguments version).
	 *
	 * <p>
	 * Given two or more graphs, the union graph of the graphs is a graph with all the vertices and edges from all the
	 * graphs, without duplications. The set of vertices in the union graph is the union of the sets of vertices in the
	 * input graphs, as defined in sets theory. As for the edges, the union can be done by the
	 * {@linkplain #edgeUnionById() edges identifiers}, or by the {@linkplain #edgeUnionByEndpoints() edges endpoints}
	 * (see the documentation of these methods for more details).
	 *
	 * @param  graphs                   the input graphs
	 * @return                          this generator
	 * @throws IllegalArgumentException if less than two input graphs are provided, or if the graphs have different
	 *                                      directionality
	 */
	@SafeVarargs
	public final UnionGraphGenerator<V, E> graphs(Graph<V, E>... graphs) {
		return graphs(ObjectList.of(graphs));
	}

	/**
	 * Set the union of edges to be by the edges identifiers.
	 *
	 * <p>
	 * The union graph of two or more graphs using this method is a graph with all the vertex set that is the union of
	 * the vertex sets of the input graphs, and edge set constructed as follows: for each edge in the input graphs, if
	 * the edge identifier does not exist in the union graph, it is added to the union graph. If the edge identifier
	 * exists in the union graph, it must have the same endpoints in the input graphs and in the union graph. Otherwise,
	 * an exception is thrown.
	 *
	 * <p>
	 * A call to this method override a previous call to {@link #edgeUnionByEndpoints()}. By default, the union of edges
	 * is by id.
	 *
	 * <p>
	 * Self edges may be generated if one of the input graphs have self edges. Parallel edges may be generated if one of
	 * the input graphs have parallel edges, or if two edges with the same endpoints and different identifiers exist in
	 * the input graphs.
	 *
	 * <p>
	 * In the following example two directed graphs are created, and the union graph of the two graphs is generated by
	 * the edges identifiers:
	 *
	 * <pre> {@code
	 * Graph<String, Integer> graph1 = Graph.newDirected();
	 * graph1.addVertices(List.of("A", "B", "C"));
	 * graph1.addEdge("A", "B", 1);
	 * graph1.addEdge("A", "C", 2);
	 *
	 * Graph<String, Integer> graph2 = Graph.newDirected();
	 * graph2.addVertices(List.of("A", "B", "D"));
	 * graph2.addEdge("A", "B", 1);
	 * graph2.addEdge("A", "D", 5);
	 *
	 * Graph<String, Integer> unionGraph = new UnionGraphGenerator<String, Integer>()
	 * 		.graphs(graph1, graph2)
	 * 		.edgeUnionById() // override previous call to edgeUnionByEndpoints() if have been called
	 * 		.generate();
	 * assert Set.of("A", "B", "C", "D").equals(unionGraph.vertices());
	 * assert Set.of(1, 2, 5).equals(unionGraph.edges());
	 * assert unionGraph.edgeSource(1).equals("A") && unionGraph.edgeTarget(1).equals("B");
	 * assert unionGraph.edgeSource(2).equals("A") && unionGraph.edgeTarget(2).equals("C");
	 * assert unionGraph.edgeSource(5).equals("A") && unionGraph.edgeTarget(5).equals("D");
	 * }</pre>
	 *
	 * @return this generator
	 */
	public UnionGraphGenerator<V, E> edgeUnionById() {
		unionById = true;
		return this;
	}

	/**
	 * Set the union of edges to be by the edges endpoints.
	 *
	 * <p>
	 * The union graph of two or more graphs using this method is a graph with all the vertex set that is the union of
	 * the vertex sets of the input graphs, and edge set constructed as follows: for each edge in the input graphs, if
	 * no edge with the same endpoints exist in the union graph, it is added to the union graph, with the same
	 * identifier. If an edge with the same endpoints exists in the union graph, it is not added to the union graph.
	 *
	 * <p>
	 * The input graphs must not contains parallel edges. If one of the input graphs contains parallel edges, an
	 * exception is thrown during the graph(s) generation. Self edges may be generated if one of the input graphs have
	 * self edges. Parallel edges are never generated when union by endpoints is used.
	 *
	 * <p>
	 * A call to this method override a previous call to {@link #edgeUnionById()}. By default, the union of edges is by
	 * id.
	 *
	 * <p>
	 * In the following example two directed graphs are created, and the union graph of the two graphs is generated by
	 * the edges endpoints. Note that the edge \(A, B\) exists in both graphs, but with different identifiers. In the
	 * generated graph, the edge \(A, B\) will appear only once, with identifier from the first graph:
	 *
	 * <pre> {@code
	 * Graph<String, Integer> graph1 = Graph.newDirected();
	 * graph1.addVertices(List.of("A", "B", "C"));
	 * graph1.addEdge("A", "B", 10);
	 * graph1.addEdge("A", "C", 20);
	 *
	 * Graph<String, Integer> graph2 = Graph.newDirected();
	 * graph2.addVertices(List.of("A", "B", "D"));
	 * graph2.addEdge("A", "B", 50);
	 * graph2.addEdge("A", "D", 100);
	 *
	 * Graph<String, Integer> unionGraph = new UnionGraphGenerator<String, Integer>()
	 * 		.graphs(graph1, graph2)
	 * 		.edgeUnionByEndpoints() // override previous call to edgeUnionById() if have been called
	 * 		.generate();
	 * assert Set.of("A", "B", "C", "D").equals(unionGraph.vertices());
	 * assert Set.of(10, 20, 100).equals(unionGraph.edges());
	 * assert unionGraph.edgeSource(10).equals("A") && unionGraph.edgeTarget(10).equals("B");
	 * assert unionGraph.edgeSource(20).equals("A") && unionGraph.edgeTarget(20).equals("C");
	 * assert unionGraph.edgeSource(100).equals("A") && unionGraph.edgeTarget(100).equals("D");
	 * }</pre>
	 *
	 * @return this generator
	 */
	public UnionGraphGenerator<V, E> edgeUnionByEndpoints() {
		unionById = false;
		return this;
	}

	/**
	 * Determine if the vertices/edges weights will be copied from the input graphs to the generated graph(s).
	 *
	 * <p>
	 * When creating a union of two graphs, where both input graphs have exactly the same weights, and there are no
	 * duplicate vertices and edges (the sets of vertices (edges) are disjoint), we simply copy the weights from the
	 * input graphs to the generated graph. However, when there are duplicate vertices and/or edges, or when the input
	 * graphs have different weights, we need to decide how to copy the weights to the generated graph. To define the
	 * rules, we first define the <i>equivalence class</i> of a vertex (edge) \(v\) in the generated graph as the set of
	 * vertices (edges) in the input graphs that are mapped to \(v\). These are the vertices with the same identifier
	 * across the input graphs. The equivalence class of an edge \(e=(u,v)\) depends on the union of edges rule: if the
	 * union of edges is by id, the equivalence class is the set of edges in the input graphs with the same identifier
	 * as \(e\). If the union of edges is by endpoints, the equivalence class is the set of edges in the input graphs
	 * with the endpoints as \((u,v)\). Given the definition of the equivalence class, we define the rules for copying
	 * the weights of each vertex (edge) \(v\) as follows: We examine the equivalence class of \(v\) ordered by the
	 * order of the input graphs. For each vertex (edge) \(v_i\) in the equivalence class, if it has a weight keyed by a
	 * key not yet seen in the equivalence class, we copy the weight to \(v\), else we ignore it. Note that we do not
	 * distinguish between {@linkplain Weights#defaultWeightAsObj() default weights} and user set weights, as long as
	 * the graph containing \(v_i\) have the weights we consider it as \(v_i\) having the weight. For each weights type
	 * in the generated graph(s), the default weight is the default weight of the first input graph that have this
	 * weights type.
	 *
	 * <p>
	 * By default, the vertices/edges weights are not copied. By default, if the vertices/edges weights are copied, all
	 * the weights are copied. Use {@link #verticesWeightsKeys(Set)} and {@link #edgesWeightsKeys(Set)} to copy only
	 * specific weights.
	 *
	 * <p>
	 * In the following example, two directed graphs are created, both have a vertices weights keyed by "w1", but with
	 * different default weights. The union graph of the two graphs is generated, and the vertices weights are copied
	 * from the input graphs. The default weight of the vertices weights in the generated graph is the default weight of
	 * the first input graph that have this weights type. Vertex \(0\) exists in both graphs, so its weight is copied
	 * from the first graph. Vertex \(1\) exists only in the second graph, so its weight is copied from the second
	 * graph:
	 *
	 * <pre> {@code
	 * Graph<Integer, Integer> graph1 = Graph.newDirected();
	 * WeightsInt<Integer> g1_w1 = graph1.addVerticesWeights("w1", int.class, -1);
	 * graph1.addVertex(0);
	 * g1_w1.set(0, 100);
	 *
	 * Graph<Integer, Integer> graph2 = Graph.newDirected();
	 * WeightsInt<Integer> g2_w1 = graph2.addVerticesWeights("w1", int.class, -2);
	 * graph2.addVertex(0);
	 * graph2.addVertex(1);
	 * g2_w1.set(0, 200);
	 * g2_w1.set(1, 500);
	 *
	 * Graph<Integer, Integer> union = new UnionGraphGenerator<Integer, Integer>()
	 * 		.graphs(graph1, graph2)
	 * 		.copyWeights(true, false)
	 * 		.verticesWeightsKeys(Set.of("w1"))
	 * 		.generate();
	 * assert union.verticesWeightsKeys().equals(Set.of("w1"));
	 * WeightsInt<Integer> union_w1 = union.verticesWeights("w1");
	 * assert union_w1.defaultWeight() == -1;
	 * assert union_w1.get(0) == 100;
	 * assert union_w1.get(1) == 500;
	 * } </pre>
	 *
	 * <p>
	 * If multiple input graphs have weights with the same key but different types, an exception is thrown during the
	 * graph(s) generation.
	 *
	 * @param  verticesWeights if {@code true}, the vertices weights will be copied
	 * @param  edgesWeights    if {@code true}, the edges weights will be copied
	 * @return                 this generator
	 */
	public UnionGraphGenerator<V, E> copyWeights(boolean verticesWeights, boolean edgesWeights) {
		this.verticesWeights = verticesWeights;
		this.edgesWeights = edgesWeights;
		return this;
	}

	/**
	 * Set the vertices weights keys to be copied from the input graphs to the generated graph(s).
	 *
	 * <p>
	 * By default, no weights are copied to the union graph. Using {@link #copyWeights(boolean, boolean)} the user can
	 * determine if the vertices weights will be copied, and if the edges weights will be copied, and all vertices/edges
	 * will be copied. This method allows the user to copy only specific weights. For this method to have any effect,
	 * copying the vertices weights must be enabled in the first place.
	 *
	 * @param  keys the vertices weights keys to be copied
	 * @return      this generator
	 */
	public UnionGraphGenerator<V, E> verticesWeightsKeys(Set<String> keys) {
		verticesWeightsKeys = keys == null ? null : new HashSet<>(keys);
		return this;
	}

	/**
	 * Set the edges weights keys to be copied from the input graphs to the generated graph(s).
	 *
	 * <p>
	 * By default, no weights are copied to the union graph. Using {@link #copyWeights(boolean, boolean)} the user can
	 * determine if the vertices weights will be copied, and if the edges weights will be copied, and all vertices/edges
	 * will be copied. This method allows the user to copy only specific weights. For this method to have any effect,
	 * copying the edges weights must be enabled in the first place.
	 *
	 * @param  keys the edges weights keys to be copied
	 * @return      this generator
	 */
	public UnionGraphGenerator<V, E> edgesWeightsKeys(Set<String> keys) {
		edgesWeightsKeys = keys == null ? null : new HashSet<>(keys);
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (graphs.isEmpty())
			throw new IllegalStateException("Input graphs not provided");

		/* Create a union vertex set, assign a number for each such vertex and remember its origin graph idx */
		Object2IntMap<V> globalVertexIndexing = new Object2IntOpenHashMap<>(graphs.get(0).vertices().size());
		globalVertexIndexing.defaultReturnValue(-1);

		final boolean trackGraphsContainingVertices = verticesWeights;
		List<Bitmap> graphsContainingVertexBitmaps =
				trackGraphsContainingVertices ? new ArrayList<>(graphs.get(0).vertices().size()) : null;

		for (int graphIdx : range(graphs.size())) {
			Graph<V, E> graph = graphs.get(graphIdx);
			for (V v : graph.vertices()) {
				int vGlobalIdx = globalVertexIndexing.size();
				int existingIdx = globalVertexIndexing.putIfAbsent(v, vGlobalIdx);

				if (trackGraphsContainingVertices) {
					if (existingIdx >= 0)
						vGlobalIdx = existingIdx;
					if (existingIdx < 0)
						graphsContainingVertexBitmaps.add(new Bitmap(graphs.size()));
					graphsContainingVertexBitmaps.get(vGlobalIdx).set(graphIdx);
				}
			}
		}
		Set<V> unionVertices = globalVertexIndexing.keySet();

		final boolean directed = graphs.get(0).isDirected();
		factory.setDirected(directed);

		/* Determine if there will be self edges in the union graph */
		if (graphs.stream().anyMatch(g -> !Graphs.selfEdges(g).isEmpty()))
			factory.allowSelfEdges();

		/* Determine if there will be parallel edges in the union graph */
		if (unionById) {
			int[] lastVisit = new int[unionVertices.size()];
			int nextVisitIdx = 1;
			boolean parallelEdges = false;
			parallelEdgesCheck: for (V u : unionVertices) {
				final int visitIdx = nextVisitIdx++;
				/* check all edges (u, _) in all the graphs */
				for (Graph<V, E> g : graphs) {
					IndexIdMap<V> viMap = g.indexGraphVerticesMap();
					int uIdx = viMap.idToIndexIfExist(u);
					if (uIdx < 0)
						continue; /* g does not contain u */
					IndexGraph ig = g.indexGraph();
					for (IEdgeIter eit = ig.outEdges(uIdx).iterator(); eit.hasNext();) {
						eit.nextInt();
						V v = viMap.indexToId(eit.targetInt());
						int vGlobalIdx = globalVertexIndexing.getInt(v);
						if (lastVisit[vGlobalIdx] == visitIdx) {
							parallelEdges = true;
							break parallelEdgesCheck;
						}
						lastVisit[vGlobalIdx] = visitIdx;
					}
				}
			}
			if (parallelEdges)
				factory.allowParallelEdges();
		}

		GraphBuilder<V, E> union = factory.newBuilder();
		List<WeightsCopier<V>> verticesWeightsCopiers = verticesWeights ? createVerticesWeights(union) : null;
		List<WeightsCopier<E>> edgesWeightsCopiers = edgesWeights ? createEdgesWeights(union) : null;

		/* Add all vertices to the union graph */
		union.ensureVertexCapacity(unionVertices.size());
		for (var entry : Object2IntMaps.fastIterable(globalVertexIndexing)) {
			V v = entry.getKey();
			union.addVertex(v);

			if (verticesWeights) {
				int vGlobalIdx = entry.getIntValue();
				Bitmap graphsContainingVertex = graphsContainingVertexBitmaps.get(vGlobalIdx);
				for (WeightsCopier<V> weightsCopier : verticesWeightsCopiers) {
					OptionalInt graphIdx = weightsCopier.copySrcIdx(graphsContainingVertex);
					if (graphIdx.isPresent())
						weightsCopier.copy(v, v, graphIdx.getAsInt());
				}
			}
		}

		ToLongBiFunction<V, V> endpointsFunc = (u, v) -> {
			int uGlobalIdx = globalVertexIndexing.getInt(u), vGlobalIdx = globalVertexIndexing.getInt(v);
			if (!directed && uGlobalIdx > vGlobalIdx) {
				int temp = uGlobalIdx;
				uGlobalIdx = vGlobalIdx;
				vGlobalIdx = temp;
			}
			return JGAlgoUtils.longPack(uGlobalIdx, vGlobalIdx);
		};

		/* Add all edges to the union graph */
		if (unionById) {
			Object2IntMap<E> globalEdgeIndexing = new Object2IntOpenHashMap<>(graphs.get(0).edges().size());
			globalEdgeIndexing.defaultReturnValue(-1);
			LongList globalEdgeEndpoints = new LongArrayList(graphs.get(0).edges().size());

			final boolean trackGraphsContainingEdges = edgesWeights;
			List<Bitmap> graphsContainingEdgeBitmaps =
					trackGraphsContainingEdges ? new ArrayList<>(graphs.get(0).edges().size()) : null;

			for (int graphIdx : range(graphs.size())) {
				Graph<V, E> graph = graphs.get(graphIdx);
				IndexGraph ig = graph.indexGraph();
				IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = graph.indexGraphEdgesMap();

				for (int eIdx : range(ig.edges().size())) {
					int uIdx = ig.edgeSource(eIdx), vIdx = ig.edgeTarget(eIdx);
					V u = viMap.indexToId(uIdx), v = viMap.indexToId(vIdx);
					E e = eiMap.indexToId(eIdx);

					long endpoints = endpointsFunc.applyAsLong(u, v);
					int eGlobalId = globalEdgeIndexing.size();
					int existingIdx = globalEdgeIndexing.putIfAbsent(e, eGlobalId);
					if (existingIdx < 0) {
						union.addEdge(u, v, e);
						globalEdgeEndpoints.add(endpoints);
					} else {
						if (globalEdgeEndpoints.getLong(existingIdx) != endpoints)
							throw new IllegalArgumentException(
									"Input graphs have the same edge with different endpoints");
					}
					if (trackGraphsContainingEdges) {
						if (existingIdx >= 0)
							eGlobalId = existingIdx;
						if (existingIdx < 0)
							graphsContainingEdgeBitmaps.add(new Bitmap(graphs.size()));
						graphsContainingEdgeBitmaps.get(eGlobalId).set(graphIdx);
					}
				}
			}

			if (edgesWeights) {
				for (var entry : Object2IntMaps.fastIterable(globalEdgeIndexing)) {
					E e = entry.getKey();
					int eGlobalIdx = entry.getIntValue();
					Bitmap graphsContainingEdge = graphsContainingEdgeBitmaps.get(eGlobalIdx);
					for (WeightsCopier<E> weightsCopier : edgesWeightsCopiers) {
						OptionalInt graphIdx = weightsCopier.copySrcIdx(graphsContainingEdge);
						if (graphIdx.isPresent())
							weightsCopier.copy(e, e, graphIdx.getAsInt());
					}
				}
			}

		} else { /* union by endpoints */
			if (graphs.stream().anyMatch(Graphs::containsParallelEdges))
				throw new IllegalArgumentException(
						"Cannot union by endpoints when input graphs contain parallel edges");

			Long2IntMap globalEdgeIndexing = new Long2IntOpenHashMap(graphs.get(0).edges().size());
			globalEdgeIndexing.defaultReturnValue(-1);

			final boolean tackEdgeIdentifiers = edgesWeights;
			List<E[]> globalIdxToIdentifiers =
					tackEdgeIdentifiers ? new ArrayList<>(graphs.get(0).edges().size()) : null;

			for (int graphIdx : range(graphs.size())) {
				Graph<V, E> graph = graphs.get(graphIdx);
				IndexGraph ig = graph.indexGraph();
				IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = graph.indexGraphEdgesMap();

				for (int eIdx : range(ig.edges().size())) {
					int uIdx = ig.edgeSource(eIdx), vIdx = ig.edgeTarget(eIdx);
					V u = viMap.indexToId(uIdx), v = viMap.indexToId(vIdx);
					long endpoints = endpointsFunc.applyAsLong(u, v);
					int eGlobalId = globalEdgeIndexing.size();
					int existingIdx = globalEdgeIndexing.putIfAbsent(endpoints, eGlobalId);
					E e = null;

					if (existingIdx < 0) {
						e = eiMap.indexToId(eIdx);
						union.addEdge(u, v, e);
					}

					if (tackEdgeIdentifiers) {
						if (existingIdx >= 0) {
							eGlobalId = existingIdx;
							e = eiMap.indexToId(eIdx);
						}
						if (existingIdx < 0) {
							@SuppressWarnings("unchecked")
							E[] graphsContainingEdgeBitmap = (E[]) new Object[graphs.size()];
							globalIdxToIdentifiers.add(graphsContainingEdgeBitmap);
						}
						globalIdxToIdentifiers.get(eGlobalId)[graphIdx] = e;
					}
				}
			}

			if (edgesWeights) {
				for (int eGlobalIdx : range(globalEdgeIndexing.size())) {
					E[] edgeIdentifiers = globalIdxToIdentifiers.get(eGlobalIdx);
					Bitmap graphsContainingEdge = Bitmap.fromPredicate(graphs.size(), i -> edgeIdentifiers[i] != null);
					E e = edgeIdentifiers[graphsContainingEdge.iterator().nextInt()];

					for (WeightsCopier<E> weightsCopier : edgesWeightsCopiers) {
						OptionalInt graphIdx = weightsCopier.copySrcIdx(graphsContainingEdge);
						if (graphIdx.isPresent()) {
							E srcEdge = edgeIdentifiers[graphIdx.getAsInt()];
							weightsCopier.copy(srcEdge, e, graphIdx.getAsInt());
						}
					}
				}
			}
		}
		return union;
	}

	private static class WeightsCopier<K> {

		private final Weights<K, ?>[] graphIdxToWeights;
		private final Weights<K, ?> builderWeights;

		WeightsCopier(Weights<K, ?>[] graphIdxToWeights, Weights<K, ?> builderWeights) {
			this.graphIdxToWeights = graphIdxToWeights;
			this.builderWeights = builderWeights;
		}

		OptionalInt copySrcIdx(Bitmap graphsContainingElm) {
			return IterTools
					.stream(graphsContainingElm)
					.filter(graphIdx -> graphIdxToWeights[graphIdx] != null)
					.findFirst();
		}

		@SuppressWarnings("unchecked")
		void copy(K srcElm, K dstElm, int graphIdx) {
			Weights<K, Object> builderWeights = (Weights<K, Object>) this.builderWeights;
			Weights<K, Object> inputWeights = (Weights<K, Object>) graphIdxToWeights[graphIdx];
			builderWeights.setAsObj(dstElm, inputWeights.getAsObj(srcElm));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<WeightsCopier<V>> createVerticesWeights(GraphBuilder<V, E> builder) {
		return (List) createWeights(builder, verticesWeightsKeys, true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<WeightsCopier<E>> createEdgesWeights(GraphBuilder<V, E> builder) {
		return (List) createWeights(builder, edgesWeightsKeys, false);
	}

	private List<WeightsCopier<?>> createWeights(GraphBuilder<V, E> builder, Set<String> copiedKeys, boolean vertices) {
		Map<String, Weights<?, ?>[]> graphsContainingWeightsBitmaps = new HashMap<>();
		final int graphsNum = graphs.size();
		for (int graphIdx : range(graphsNum)) {
			Graph<V, E> graph = graphs.get(graphIdx);
			for (String key : vertices ? graph.verticesWeightsKeys() : graph.edgesWeightsKeys()) {
				Weights<?, ?> weights = vertices ? graph.verticesWeights(key) : graph.edgesWeights(key);
				Weights<?, ?>[] weightsPerGraphArr =
						graphsContainingWeightsBitmaps.computeIfAbsent(key, k -> new Weights<?, ?>[graphsNum]);
				weightsPerGraphArr[graphIdx] = weights;
			}
		}

		List<WeightsCopier<?>> weightsFuncs = new ArrayList<>();
		for (var entry : graphsContainingWeightsBitmaps.entrySet()) {
			String key = entry.getKey();
			if (copiedKeys != null && !copiedKeys.contains(key))
				continue;
			Weights<?, ?>[] graphIdxToWeights = entry.getValue();
			Weights<?, ?> weights0 = Arrays.stream(graphIdxToWeights).filter(Objects::nonNull).findFirst().get();

			Class<?> weightsType;
			if (weights0 instanceof WeightsByte) {
				checkWeightsCompatibility(graphIdxToWeights, WeightsByte.class);
				weightsType = byte.class;

			} else if (weights0 instanceof WeightsShort) {
				checkWeightsCompatibility(graphIdxToWeights, WeightsShort.class);
				weightsType = short.class;

			} else if (weights0 instanceof WeightsInt) {
				checkWeightsCompatibility(graphIdxToWeights, WeightsInt.class);
				weightsType = int.class;

			} else if (weights0 instanceof WeightsLong) {
				checkWeightsCompatibility(graphIdxToWeights, WeightsLong.class);
				weightsType = long.class;

			} else if (weights0 instanceof WeightsFloat) {
				checkWeightsCompatibility(graphIdxToWeights, WeightsFloat.class);
				weightsType = float.class;

			} else if (weights0 instanceof WeightsDouble) {
				checkWeightsCompatibility(graphIdxToWeights, WeightsDouble.class);
				weightsType = double.class;

			} else if (weights0 instanceof WeightsBool) {
				checkWeightsCompatibility(graphIdxToWeights, WeightsBool.class);
				weightsType = boolean.class;

			} else if (weights0 instanceof WeightsChar) {
				checkWeightsCompatibility(graphIdxToWeights, WeightsChar.class);
				weightsType = char.class;

			} else {
				checkWeightsCompatibility(graphIdxToWeights, WeightsObj.class);
				weightsType = Object.class;
			}

			Weights<?, ?> builderWeights;
			Object defWeight = weights0.defaultWeightAsObj();
			@SuppressWarnings("unchecked")
			Class<Object> weightsType0 = (Class<Object>) weightsType;
			if (vertices) {
				builderWeights = builder.addVerticesWeights(key, weightsType0, defWeight);
			} else {
				builderWeights = builder.addEdgesWeights(key, weightsType0, defWeight);
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			WeightsCopier<?> weightsCopier = new WeightsCopier(graphIdxToWeights, builderWeights);
			weightsFuncs.add(weightsCopier);
		}
		return weightsFuncs;
	}

	private static void checkWeightsCompatibility(Weights<?, ?>[] weights, Class<?> weightsType) {
		if (!Arrays.stream(weights).filter(Objects::nonNull).allMatch(weightsType::isInstance))
			throw new IllegalArgumentException("Weights are not compatible");
	}

}
