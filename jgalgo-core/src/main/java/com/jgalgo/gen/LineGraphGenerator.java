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
import java.net.URI;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IdBuilder;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsObj;

/**
 * Generates the line graph given an existing graph.
 *
 * <p>
 * Given a graph \(G=(V,E)\), the line graph \(L=(E,H)\) is the graph whose vertices are the edges of \(G\) and a pair
 * of such two vertices are connected by an edge if the corresponding edges in the original graph share an endpoint. For
 * directed graphs, two vertices are connected if their corresponding edges form a path of size two in the original
 * graph. The line graph \(L\) is directed if and only if the original graph \(G\) is directed.
 *
 * <p>
 * The generate graph will not contains self edges, and no parallel edges. Specifically, if the original graph contains
 * two parallel edges, meaning they share two endpoints rather than one, the generated line graph will contain only one
 * edge between their corresponding vertices.
 *
 * <p>
 * In the following example, the line graph of the original graph is generated. The vertices type of the original graph
 * is {@link URI}, and the edges type is {@link String}. The vertices type of the generated line graph is {@link String}
 * (the edges type of the original graph), and the edges type is {@link Integer}. The edges of the generated line graph
 * are generated using the default edge builder provided by {@link IdBuilderInt#defaultBuilder()}.
 *
 * <pre> {@code
 * Graph<URI, String> origGraph = ...;
 * Graph<String, Integer> lineGraph = new LineGraphGenerator<String, Integer>()
 * 		.graph(origGraph)
 * 		.edges(IdBuilderInt.defaultBuilder())
 * 		.generate();
 * } </pre>
 *
 * @param  <V> the vertices type, which is the edges type of the input graph
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class LineGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Graph<Object, V> graph;
	private IdBuilder<E> edgeBuilder;
	private String commonVertexWeightsKey;

	/**
	 * Create a new line graph generator that will use the default graph factory.
	 *
	 * <p>
	 * The graph factory will be used to create the generated graph(s) and it will also provide an edge builder if one
	 * was no explicitly set using {@link #edges(IdBuilder)}. The default graph builder does not have an edge builder,
	 * therefore it must be set manually.
	 */
	public LineGraphGenerator() {
		this(GraphFactory.undirected());

	}

	/**
	 * Create a new line graph generator that will use the given graph factory.
	 *
	 * <p>
	 * The graph factory will be used to create the generated graph(s) and it will also provide an edge builder if one
	 * was no explicitly set using {@link #edges(IdBuilder)}.
	 *
	 * <p>
	 * To generate {@linkplain IntGraph int graphs}, pass an instance of {@linkplain IntGraphFactory} to this
	 * constructor.
	 *
	 * @param factory the graph factory that will be used to create the generated graph(s)
	 */
	public LineGraphGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	/**
	 * Get the graph factory that will be used to create the generated graph(s).
	 *
	 * <p>
	 * It's possible to customize the factory before generating the graph(s), for example by using
	 * {@link GraphFactory#addHint(GraphFactory.Hint)} to optimize the generated graph(s) for a specific algorithm. If
	 * the factory has an edge builder it will be used to generate the edges of the generated graph(s) if it will not be
	 * overridden by {@link #edges(IdBuilder)}.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration.
	 *
	 * @return the graph factory that will be used to create the generated graph(s)
	 */
	public GraphFactory<V, E> graphFactory() {
		return factory;
	}

	/**
	 * Set the input graph whose line graph this generator should generate.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), the line graph \(L=(E,H)\) is the graph whose vertices are the edges of \(G\) and a
	 * pair of such two vertices are connected by an edge if the corresponding edges in the original graph share an
	 * endpoint. For directed graphs, two vertices are connected if their corresponding edges form a path of size two in
	 * the original graph. The line graph \(L\) is directed if and only if the original graph \(G\) is directed. This
	 * method sets the input graph \(G\), and the generator will generate the line graph \(L\).
	 *
	 * <p>
	 * The edges type of the input graph are the vertices type of the generated line graph. The vertices type of the
	 * input graph is not reflected in the output graph.
	 *
	 * @param  graph                the input graph for the generator, whose line graph should be generated. The
	 *                                  generated graph(s) will be directed if and only if this input graph is directed
	 * @return                      this generator
	 * @throws NullPointerException if {@code graph} is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public LineGraphGenerator<V, E> graph(Graph<?, V> graph) {
		this.graph = (Graph<Object, V>) Objects.requireNonNull(graph);
		return this;
	}

	/**
	 * Set the edge builder that will be used to generate edges.
	 *
	 * <p>
	 * The edges will be generated using the provided edge builder, and the edge generator provided by the
	 * {@linkplain #graphFactory() graph factory} (if exists) will be ignored. The generation will happen independently
	 * for each graph generated. If this method is not called, or called with a {@code null} argument, the edge builder
	 * of the graph factory will be used. If the graph factory does not have an edge builder, an exception will be
	 * thrown during generation.
	 *
	 * @param  edgeBuilder the edge builder, or {@code null} to use the edge builder of the {@linkplain #graphFactory()
	 *                         graph factory}
	 * @return             this generator
	 */
	public LineGraphGenerator<V, E> edges(IdBuilder<E> edgeBuilder) {
		this.edgeBuilder = edgeBuilder;
		return this;
	}

	/**
	 * Set the weights key that will store the common vertex between two edges.
	 *
	 * <p>
	 * Each edge in the line graph connect two vertices if the two corresponding edges share a common endpoint. This
	 * method cause each such common endpoint to be saved as weight of an edge in the generated line graph(s). By
	 * default, no such weights are added to the generated graph(s). If two edges share more than one endpoint (parallel
	 * edges), an arbitrary one will be stored as weight. The weights will be stored in the generate graph(s) as edges
	 * {@linkplain WeightsObj object weights} by default, or as {@linkplain WeightsInt int weights} if the input graph
	 * was an {@link IntGraph}. See the following example:
	 *
	 * <pre> {@code
	 * Graph<URI, String> origGraph = ...;
	 * Graph<String, Integer> lineGraph = new LineGraphGenerator<String, Integer>()
	 * 		.graph(origGraph)
	 * 		.edges(IdBuilderInt.defaultBuilder())
	 * 		.commonVertexWeights("common-vertex")
	 * 		.generate();
	 *
	 * 	WeightsObj<Integer, URI> commonVertexWeights = g.edgesWeight("common-vertex");
	 * 	for (Integer lineEdge : g.edges()) {
	 * 		String e1 = g.edgeSource(lineEdge);
	 * 		String e2 = g.edgeTarget(lineEdge);
	 * 		URI commonVertex = commonVertexWeights.get(lineEdge);
	 * 		System.out.format("The vertex %s is a common vertex between two edges: %d %d", commonVertex, e1, e2);
	 * 	}
	 * } </pre>
	 *
	 * @param  weightsKey key of the edges weights in the generated graph(s) that will store the common (original)
	 *                        endpoint vertex that is shared between the two connected corresponding edges, or
	 *                        {@code null} to not store these weights
	 * @return            this generator
	 */
	public LineGraphGenerator<V, E> commonVertexWeights(String weightsKey) {
		commonVertexWeightsKey = weightsKey;
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (graph == null)
			throw new IllegalStateException("Input graph not provided");

		GraphBuilder<V, E> g = factory.setDirected(graph.isDirected()).newBuilder();
		IdBuilder<E> edgeBuilder = this.edgeBuilder != null ? this.edgeBuilder : g.edgeBuilder();
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge builder not provided and graph factory does not have one");

		final boolean hasCommonVertexWeights = commonVertexWeightsKey != null;
		final Weights<E, Object> commonVertexWeights;
		if (!hasCommonVertexWeights) {
			commonVertexWeights = null;
		} else if ((Graph<?, ?>) graph instanceof IntGraph) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Weights<E, Object> commonVertexWeights0 = (Weights) g.addEdgesWeights(commonVertexWeightsKey, int.class);
			commonVertexWeights = commonVertexWeights0;
		} else {
			commonVertexWeights = g.addEdgesWeights(commonVertexWeightsKey, Object.class);
		}

		g.addVertices(graph.edges());
		if (g.isDirected()) {
			IndexGraph ig = graph.indexGraph();
			IndexIdMap<Object> viMap = graph.indexGraphVerticesMap();
			IndexIdMap<V> eiMap = graph.indexGraphEdgesMap();
			final int maxInDegree = ig.vertices().intStream().map(v -> ig.inEdges(v).size()).max().orElse(0);
			final int maxOutDegree = ig.vertices().intStream().map(v -> ig.outEdges(v).size()).max().orElse(0);
			int[] intEdges = new int[maxInDegree];
			int[] outEdges = new int[maxOutDegree];
			for (int uIdx : ig.vertices()) {
				Object u = viMap.indexToId(uIdx);
				IEdgeSet inEs = ig.inEdges(uIdx);
				IEdgeSet outEs = ig.outEdges(uIdx);
				Object toArrRes1 = inEs.toArray(intEdges);
				Object toArrRes2 = outEs.toArray(outEdges);
				assert toArrRes1 == intEdges;
				assert toArrRes2 == outEdges;
				final int inEsSize = inEs.size();
				final int outEsSize = outEs.size();
				for (int i : range(inEsSize)) {
					int e1Idx = intEdges[i];
					V e1 = eiMap.indexToId(e1Idx);
					for (int j : range(outEsSize)) {
						int e2Idx = outEdges[j];
						if (e1Idx == e2Idx)
							continue; /* avoid generating self edge of original self edge */
						V e2 = eiMap.indexToId(e2Idx);
						E e = edgeBuilder.build(g.edges());
						g.addEdge(e1, e2, e);
						if (hasCommonVertexWeights)
							commonVertexWeights.setAsObj(e, u);
					}
				}
			}

		} else {
			IndexGraph ig = graph.indexGraph();
			IndexIdMap<Object> viMap = graph.indexGraphVerticesMap();
			IndexIdMap<V> eiMap = graph.indexGraphEdgesMap();
			final int maxDegree = ig.vertices().intStream().map(v -> ig.outEdges(v).size()).max().orElse(0);
			int[] edges = new int[maxDegree];
			for (int uIdx : ig.vertices()) {
				Object u = viMap.indexToId(uIdx);
				IEdgeSet es = ig.outEdges(uIdx);
				Object toArrRes = es.toArray(edges);
				assert toArrRes == edges;
				final int esSize = es.size();
				for (int i : range(esSize)) {
					int e1Idx = edges[i];
					int v1Idx = ig.edgeEndpoint(e1Idx, uIdx);
					V e1 = eiMap.indexToId(e1Idx);
					for (int j : range(i + 1, esSize)) {
						int e2Idx = edges[j];
						int v2Idx = ig.edgeEndpoint(e2Idx, uIdx);
						if (v1Idx == v2Idx && uIdx > v1Idx)
							continue; /* avoid generating two edges between parallel original edges */
						V e2 = eiMap.indexToId(e2Idx);
						E e = edgeBuilder.build(g.edges());
						g.addEdge(e1, e2, e);
						if (hasCommonVertexWeights)
							commonVertexWeights.setAsObj(e, u);
					}
				}
			}
		}
		return g;
	}

}
