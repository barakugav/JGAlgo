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
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilder;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.Bitmap;

/**
 * Generates the complement graph of a given graph.
 *
 * <p>
 * The complement graph of a graph \(G\) is a graph \(H\) on the same vertices such that \((u,v) \in E(H)\) if and only
 * if \((u,v) \notin E(G)\). If \(G\) is a directed graph, then \(H\) is also directed. Whether or not self edges are
 * generated is controlled by {@link #selfEdges(boolean)}.
 *
 * <p>
 * In the following example, the complement graph of a given graph is generated. For each edge in the complement graph,
 * it is asserted that an edge with the same endpoints does not exist in the original graph:
 *
 * <pre> {@code
 * Graph<Integer, Integer> origGraph = null;
 * Graph<Integer, Integer> complement = new ComplementGraphGenerator<Integer, Integer>()
 * 		.selfEdges(true)
 * 		.edges(IdBuilderInt.defaultBuilder())
 * 		.generate();
 * for (Integer u : origGraph.vertices())
 * 	for (Integer v : origGraph.vertices())
 * 		assert origGraph.containsEdge(u, v) != complement.containsEdge(u, v);
 * }</pre>
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class ComplementGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Graph<V, Object> graph;
	private IdBuilder<E> edgeBuilder;
	private boolean selfEdges;

	/**
	 * Create a new complement graph generator that will use the default graph factory.
	 *
	 * <p>
	 * The graph factory will be used to create the generated graph(s) and it will also provide an edge builder if one
	 * was no explicitly set using {@link #edges(IdBuilder)}. The default graph builder does not have an edge builder,
	 * therefore it must be set manually.
	 */
	public ComplementGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new complement graph generator that will use the given graph factory.
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
	public ComplementGraphGenerator(GraphFactory<V, E> factory) {
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
	 * called to align the created graph with the generator configuration. If self edges are generated, the method
	 * {@link GraphFactory#allowSelfEdges()} will be called as well (see {@link #selfEdges(boolean)}).
	 *
	 * @return the graph factory that will be used to create the generated graph(s)
	 */
	public GraphFactory<V, E> graphFactory() {
		return factory;
	}

	/**
	 * Set the input graph whose complement graph will be generated.
	 *
	 * <p>
	 * Given a graph \(G\), the complement graph \(H\) is a graph on the same vertices such that \((u,v) \in E(H)\) if
	 * and only if \((u,v) \notin E(G)\). This method sets the input graph \(G\).
	 *
	 * <p>
	 * This method must be called before the graph(s) generation.
	 *
	 * <p>
	 * The edges type of the input graph is not reflected in the generated graph(s), as new edges will be generated
	 * which can be of any type.
	 *
	 * @param  graph the input graph whose complement graph will be generated
	 * @return       this generator
	 */
	@SuppressWarnings("unchecked")
	public ComplementGraphGenerator<V, E> graph(Graph<V, ?> graph) {
		this.graph = (Graph<V, Object>) Objects.requireNonNull(graph);
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
	public ComplementGraphGenerator<V, E> edges(IdBuilder<E> edgeBuilder) {
		this.edgeBuilder = edgeBuilder;
		return this;
	}

	/**
	 * Set whether or not to generate self edges.
	 *
	 * <p>
	 * By default, self edges are not generated.
	 *
	 * @param  selfEdges if {@code true}, self edges will be generated
	 * @return           this generator
	 */
	public ComplementGraphGenerator<V, E> selfEdges(boolean selfEdges) {
		this.selfEdges = selfEdges;
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (graph == null)
			throw new IllegalStateException("Input graph not provided");

		if (selfEdges)
			factory.allowSelfEdges();
		GraphBuilder<V, E> complement = factory.setDirected(graph.isDirected()).newBuilder();
		IdBuilder<E> edgeBuilder = this.edgeBuilder != null ? this.edgeBuilder : complement.edgeBuilder();
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge builder not provided and graph factory does not have one");

		IndexGraph ig = graph.indexGraph();
		final int n = ig.vertices().size();
		Bitmap edges = new Bitmap(n * n);
		final boolean directed = ig.isDirected();
		for (int e : range(ig.edges().size())) {
			int u = ig.edgeSource(e);
			int v = ig.edgeTarget(e);
			if (!directed && u > v) {
				int tmp = u;
				u = v;
				v = tmp;
			}
			edges.set(u * n + v);
		}

		/*
		 * if no self edges are generated, clear these edges from the bitmap, to count how many edges we are expected to
		 * generate correctly
		 */
		if (!selfEdges)
			for (int v : range(n))
				edges.clear(v * n + v);

		complement.addVertices(graph.vertices());
		int maxEdgesNum = n * (n - 1);
		if (!graph.isDirected())
			maxEdgesNum /= 2;
		if (selfEdges)
			maxEdgesNum += n;
		complement.ensureEdgeCapacity(maxEdgesNum - edges.cardinality());

		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		for (int uIdx : range(n)) {
			for (int vIdx = directed ? 0 : uIdx; vIdx < uIdx; vIdx++) {
				if (!edges.get(uIdx * n + vIdx)) {
					V u = viMap.indexToId(uIdx);
					V v = viMap.indexToId(vIdx);
					complement.addEdge(u, v, edgeBuilder.build(complement.edges()));
				}
			}
			for (int vIdx : range(uIdx + (selfEdges ? 0 : 1), n)) {
				if (!edges.get(uIdx * n + vIdx)) {
					V u = viMap.indexToId(uIdx);
					V v = viMap.indexToId(vIdx);
					complement.addEdge(u, v, edgeBuilder.build(complement.edges()));
				}
			}
		}

		return complement;
	}

}
