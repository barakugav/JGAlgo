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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilder;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntPair;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Generates a uniformly random graph among all graphs with \(n\) vertices and \(m\) edges.
 *
 * <p>
 * The generator uses the \(G(n,m)\) model to generate a uniformly random graph among all graphs with \(n\) vertices and
 * \(m\) edges. Both directed and undirected graphs are supported, as well as self-edges and parallel-edges. By default,
 * the generated graph(s) is undirected, does not contain self-edges and may contain parallel-edges.
 *
 * <p>
 * A {@link GraphFactory} is used to create the generated graph(s). The factory can be set in the
 * {@linkplain #GnmGraphGenerator(GraphFactory) constructor}, or a default one will be used, which is still available
 * for customization using {@link #graphFactory()}. Vertices and edges can either be provided explicitly, or the number
 * of vertices/edges is passed and they are generated using a {@linkplain IdBuilder vertex/edge builder}. The vertex and
 * edge builders can be set when the number of vertices or edges is set using {@link #vertices(int, IdBuilder)} or
 * {@link #edges(int, IdBuilder)}, or the builders of the graph factory will be used. Note that the default graph
 * factory does not have default vertex and edge builders, unless set explicitly. The {@linkplain IntGraphFactory
 * IntGraph factory} does have these builders by default, pass an instance of it to the constructor to use it (and to
 * generate {@linkplain IntGraph int graphs}).
 *
 * <p>
 * In the following example, a graph with ten vertices and fourteen edges is generated. The graph is directed, contains
 * self-edges and does not contain parallel-edges. The seed of the generator is set to ensure deterministic behavior.
 *
 * <pre> {@code
 * Graph<Integer, Integer> g = new GnmGraphGenerator<>(IntGraphFactory.directed())
 * 		.directed(true)
 * 		.vertices(10)
 * 		.edges(14)
 * 		.selfEdges(true)
 * 		.parallelEdges(false)
 * 		.seed(0x7d0c16fa09e05751L)
 * 		.generate();
 * } </pre>
 *
 * <p>
 * For deterministic behavior, set the seed of the generator using {@link #seed(long)}.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class GnmGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Variant2<List<V>, IntObjectPair<IdBuilder<V>>> vertices;
	private IntObjectPair<IdBuilder<E>> edges;
	private boolean directed = false;
	private boolean selfEdges = false;
	private boolean parallelEdges = true;
	private final Random rand = new Random();

	/**
	 * Create a new \(G(n,m)\) generator that will use the default graph factory.
	 *
	 * <p>
	 * The default graph factory does not have default vertex and edge builders, so if only the number of vertices and
	 * edges is set using {@link #vertices(int)} and {@link #edges(int)}, the vertex and edge builders must be set
	 * explicitly using {@code graphFactory().setVertexBuilder(...)} and {@code graphFactory().setEdgeBuilder(...)}.
	 * Alternatively, the methods {@link #vertices(int, IdBuilder)} and {@link #edges(int, IdBuilder)} can be used to
	 * set the number of vertices and edges and provide a vertex/edge builder that will override the (maybe non
	 * existing) vertex/edge builder of the graph factory. The vertex set can also be set explicitly using
	 * {@link #vertices(Collection)}.
	 */
	public GnmGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new \(G(n,m)\) generator that will use the given graph factory.
	 *
	 * <p>
	 * If the factory has vertex and/or edge builders, they will be used to generate the vertices and edges of the
	 * generated graph(s) if only the number of vertices or edges is set using {@link #vertices(int)} or
	 * {@link #edges(int)}.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration. If self or parallel edges are generated (see
	 * {@link #selfEdges(boolean)} and {@link #parallelEdges(boolean)}), the methods
	 * {@link GraphFactory#allowSelfEdges()} and {@link GraphFactory#allowParallelEdges()} will also be called, resp.
	 *
	 * <p>
	 * To generate {@linkplain IntGraph int graphs}, pass an instance of {@linkplain IntGraphFactory} to this
	 * constructor.
	 *
	 * @param factory the graph factory that will be used to create the generated graph(s)
	 */
	public GnmGraphGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	/**
	 * Get the graph factory that will be used to create the generated graph(s).
	 *
	 * <p>
	 * It's possible to customize the factory before generating the graph(s), for example by using
	 * {@link GraphFactory#addHint(GraphFactory.Hint)} to optimize the generated graph(s) for a specific algorithm. The
	 * vertex and edge builders will be used to generate the vertices and edges of the generated graph(s) if only the
	 * number of vertices or edges is set using {@link #vertices(int)} or {@link #edges(int)}. Set the vertex/edge
	 * builder of the factory to use these functions.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration. If self or parallel edges are generated (see
	 * {@link #selfEdges(boolean)} and {@link #parallelEdges(boolean)}), the methods
	 * {@link GraphFactory#allowSelfEdges()} and {@link GraphFactory#allowParallelEdges()} will also be called, resp.
	 *
	 * @return the graph factory that will be used to create the generated graph(s)
	 */
	public GraphFactory<V, E> graphFactory() {
		return factory;
	}

	/**
	 * Set the vertices set of the generated graph(s).
	 *
	 * <p>
	 * If the generator is used to generate multiple graphs, the same vertex set will be used for all of them. This
	 * method override all previous calls to any of {@link #vertices(Collection)}, {@link #vertices(int)} or
	 * {@link #vertices(int, IdBuilder)}.
	 *
	 * @param  vertices the vertices set
	 * @return          this generator
	 */
	@SuppressWarnings("unchecked")
	public GnmGraphGenerator<V, E> vertices(Collection<? extends V> vertices) {
		if (factory instanceof IntGraphFactory) {
			vertices = (List<V>) new IntArrayList((Collection<Integer>) vertices);
		} else {
			vertices = new ObjectArrayList<>(vertices);
		}
		this.vertices = Variant2.ofA((List<V>) vertices);
		return this;
	}

	/**
	 * Set the number of vertices that will be generated for each graph.
	 *
	 * <p>
	 * The vertices will be generated using the vertex builder of the graph factory, see
	 * {@link GraphFactory#setVertexBuilder(IdBuilder)}. The default graph factory does not have a vertex builder, so it
	 * must be set explicitly, or {@link IntGraphFactory}, which does have such builder, should be passed in the
	 * {@linkplain #GnmGraphGenerator(GraphFactory) constructor}. Another alternative is to use
	 * {@link #vertices(int, IdBuilder)} which set the number of vertices and provide a vertex builder that will
	 * override the (maybe non existing) vertex builder of the graph factory. The generation will happen independently
	 * for each graph generated. If there is no vertex builder, an exception will be thrown during generation. This
	 * method override all previous calls to any of {@link #vertices(Collection)}, {@link #vertices(int)} or
	 * {@link #vertices(int, IdBuilder)}.
	 *
	 * @param  verticesNum              the number of vertices that will be generated for each graph
	 * @return                          this generator
	 * @throws IllegalArgumentException if {@code verticesNum} is negative
	 */
	public GnmGraphGenerator<V, E> vertices(int verticesNum) {
		vertices(verticesNum, null);
		return this;
	}

	/**
	 * Set the number of vertices that will be generated for each graph, and the vertex builder that will be used to
	 * generate them.
	 *
	 * <p>
	 * The vertices will be generated using the provided vertex builder, and the vertex generator provided by the
	 * {@linkplain #graphFactory() graph factory} (if exists) will be ignored. The generation will happen independently
	 * for each graph generated. This method override all previous calls to any of {@link #vertices(Collection)},
	 * {@link #vertices(int)} or {@link #vertices(int, IdBuilder)}.
	 *
	 * @param  verticesNum              the number of vertices that will be generated for each graph
	 * @param  vertexBuilder            the vertex builder, or {@code null} to use the vertex builder of the
	 *                                      {@linkplain #graphFactory() graph factory}
	 * @return                          this generator
	 * @throws IllegalArgumentException if {@code verticesNum} is negative
	 */
	public GnmGraphGenerator<V, E> vertices(int verticesNum, IdBuilder<V> vertexBuilder) {
		if (verticesNum < 0)
			throw new IllegalArgumentException("number of vertices must be non-negative");
		this.vertices = Variant2.ofB(IntObjectPair.of(verticesNum, vertexBuilder));
		return this;
	}

	/**
	 * Set the number of edges what will be generated for each graph.
	 *
	 * <p>
	 * The number of edges must be non-negative, and if parallel edges are not allowed, it must be at most \(n(n-1) /
	 * 2\) for undirected graphs and \(n(n-1)\) for directed graphs, with addition of \(n\) self-edges if self-edges are
	 * allowed.
	 *
	 * <p>
	 * The edges will be generated using the edge builder of the graph factory, see
	 * {@link GraphFactory#setEdgeBuilder(IdBuilder)}. The default graph factory does not have an edge builder, so it
	 * must be set explicitly, or {@link IntGraphFactory}, which does have such builder, should be passed in the
	 * {@linkplain #GnmGraphGenerator(GraphFactory) constructor}. Another alternative is to use
	 * {@link #edges(int, IdBuilder)} which set the number of edges and provide an edge builder that will override the
	 * (maybe non existing) edge builder of the graph factory. The generation will happen independently for each graph
	 * generated. If there is no edge builder, an exception will be thrown during generation. This method override all
	 * previous calls to {@link #edges(int)} or {@link #edges(int, IdBuilder)}.
	 *
	 * @param  edgesNum                 the number of edges
	 * @return                          this generator
	 * @throws IllegalArgumentException if {@code edgesNum} is negative
	 */
	public GnmGraphGenerator<V, E> edges(int edgesNum) {
		edges(edgesNum, null);
		return this;
	}

	/**
	 * Set the number of edges what will be generated for each graph, and the edge builder that will be used to generate
	 * them.
	 *
	 * <p>
	 * The number of edges must be non-negative, and if parallel edges are not allowed, it must be at most \(n(n-1) /
	 * 2\) for undirected graphs and \(n(n-1)\) for directed graphs, with addition of \(n\) self-edges if self-edges are
	 * allowed.
	 *
	 * <p>
	 * The edges will be generated using the provided edge builder, and the edge generator provided by the
	 * {@linkplain #graphFactory() graph factory} (if exists) will be ignored. The generation will happen independently
	 * for each graph generated. This method override all previous calls to {@link #edges(int)} or
	 * {@link #edges(int, IdBuilder)}.
	 *
	 * @param  edgesNum                 the number of edges
	 * @param  edgeBuilder              the edge builder, or {@code null} to use the edge builder of the
	 *                                      {@linkplain #graphFactory() graph factory}
	 * @return                          this generator
	 * @throws IllegalArgumentException if {@code edgesNum} is negative
	 */
	public GnmGraphGenerator<V, E> edges(int edgesNum, IdBuilder<E> edgeBuilder) {
		if (edgesNum < 0)
			throw new IllegalArgumentException("number of edges must be non-negative");
		this.edges = IntObjectPair.of(edgesNum, edgeBuilder);
		return this;
	}

	/**
	 * Determine if the generated graph(s) is directed or undirected.
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @param  directed {@code true} if the generated graph(s) will be directed, {@code false} if undirected
	 * @return          this generator
	 */
	public GnmGraphGenerator<V, E> directed(boolean directed) {
		this.directed = directed;
		return this;
	}

	/**
	 * Determine if the generated graph(s) will contain self-edges.
	 *
	 * <p>
	 * Self edges are edges with the same source and target vertex. By default, the generated graph(s) will not contain
	 * self-edges.
	 *
	 * @param  selfEdges {@code true} if the generated graph(s) will contain self-edges, {@code false} otherwise
	 * @return           this generator
	 */
	public GnmGraphGenerator<V, E> selfEdges(boolean selfEdges) {
		this.selfEdges = selfEdges;
		return this;
	}

	/**
	 * Determine if the generated graph(s) will contain parallel-edges.
	 *
	 * <p>
	 * Parallel edges are a set of edges that connect the same two vertices. By default, the generated graph(s) will
	 * contain parallel-edges.
	 *
	 * @param  parallelEdges {@code true} if the generated graph(s) will contain parallel-edges, {@code false} otherwise
	 * @return               this generator
	 */
	public GnmGraphGenerator<V, E> parallelEdges(boolean parallelEdges) {
		this.parallelEdges = parallelEdges;
		return this;
	}

	/**
	 * Set the seed of the random number generator used to generate the graph(s).
	 *
	 * <p>
	 * By default, a random seed is used. For deterministic behavior, set the seed of the generator.
	 *
	 * @param  seed the seed of the random number generator
	 * @return      this generator
	 */
	public GnmGraphGenerator<V, E> seed(long seed) {
		rand.setSeed(seed);
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edges == null)
			throw new IllegalStateException("Edges not set");
		final int n = vertices.map(List::size, IntObjectPair::firstInt).intValue();
		final int m = edges.firstInt();

		int maxNumberOfEdges = n * (n - 1);
		if (!directed)
			maxNumberOfEdges /= 2;
		if (selfEdges)
			maxNumberOfEdges += n;
		if (n == 0 && m > 0)
			throw new IllegalArgumentException("number of edges must be zero if vertices set is empty");
		if (!parallelEdges && m > maxNumberOfEdges)
			throw new IllegalArgumentException("number of edges must be at most " + maxNumberOfEdges);

		factory.setDirected(directed);
		if (selfEdges)
			factory.allowSelfEdges(selfEdges);
		if (parallelEdges)
			factory.allowParallelEdges(parallelEdges);
		GraphBuilder<V, E> g = factory.newBuilder();

		IdBuilder<E> edgeBuilder = edges.second() != null ? edges.second() : g.edgeBuilder();
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge builder not provided and graph factory does not have one");

		final List<V> vertices;
		if (this.vertices.contains(List.class)) {
			@SuppressWarnings("unchecked")
			List<V> vertices0 = this.vertices.get(List.class);
			g.addVertices(vertices = vertices0);
		} else {
			@SuppressWarnings("unchecked")
			IntObjectPair<IdBuilder<V>> p = this.vertices.get(IntObjectPair.class);
			int verticesNum = p.firstInt();
			IdBuilder<V> vertexBuilder = p.second() != null ? p.second() : g.vertexBuilder();
			if (vertexBuilder == null)
				throw new IllegalStateException("Vertex builder not provided and graph factory does not have one");
			if (g instanceof IntGraphBuilder) {
				@SuppressWarnings("unchecked")
				List<V> vertices0 = (List<V>) new IntArrayList(verticesNum);
				vertices = vertices0;
			} else {
				vertices = new ObjectArrayList<>(verticesNum);
			}
			g.ensureVertexCapacity(verticesNum);
			for (int i = 0; i < verticesNum; i++) {
				V vertex = vertexBuilder.build(g.vertices());
				g.addVertex(vertex);
				vertices.add(vertex);
			}
		}

		g.ensureEdgeCapacity(m);
		if (parallelEdges || m <= maxNumberOfEdges / 2) {
			/* Start with an empty graph and add edges one by one */

			LongSet edges = parallelEdges ? null : new LongOpenHashSet(m);
			while (g.edges().size() < m) {
				int uIdx = rand.nextInt(n), vIdx = rand.nextInt(n);
				if (!selfEdges && uIdx == vIdx)
					continue;
				if (!parallelEdges) {
					if (!directed && uIdx > vIdx) {
						int tmp = uIdx;
						uIdx = vIdx;
						vIdx = tmp;
					}
					if (!edges.add(IntPair.of(uIdx, vIdx)))
						continue;
				}
				V u = vertices.get(uIdx), v = vertices.get(vIdx);
				g.addEdge(u, v, edgeBuilder.build(g.edges()));
			}

		} else {
			/* Start with a complete graph and remove edges one by one */

			Bitmap edges = new Bitmap(maxNumberOfEdges);
			edges.setAll();
			for (int edgesNum = maxNumberOfEdges; edgesNum > m;) {
				int i = rand.nextInt(maxNumberOfEdges);
				if (edges.get(i)) {
					edges.clear(i);
					edgesNum--;
				}
			}

			int i = 0;
			for (int uIdx : range(n)) {
				V u = vertices.get(uIdx);
				if (directed) {
					for (int vIdx = 0; vIdx < uIdx; vIdx++, i++) {
						if (edges.get(i)) {
							V v = vertices.get(vIdx);
							g.addEdge(u, v, edgeBuilder.build(g.edges()));
						}
					}
				}
				if (selfEdges && edges.get(i++))
					g.addEdge(u, u, edgeBuilder.build(g.edges()));
				for (int vIdx = uIdx + 1; vIdx < n; vIdx++, i++) {
					if (edges.get(i)) {
						V v = vertices.get(vIdx);
						g.addEdge(u, v, edgeBuilder.build(g.edges()));
					}
				}
			}
		}

		return g;
	}

}
