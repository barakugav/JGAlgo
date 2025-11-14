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
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Generates a random graph using the R-MAT model.
 *
 * <p>
 * The R-MAT model generates a graph by recursively partitioning the adjacency matrix into four quadrants and assigning
 * edges to each quadrant with different probabilities \((a,b,c,d)\). The generator accept as an input how many edges to
 * generate, and it generate them one by one: each edge is assigned to a quadrant according to the probabilities
 * \((a,b,c,d)\), and then the quadrant is recursively partitioned until a single cell is reached. The cell is then
 * assigned the edge. The process is repeated until the required number of edges is generated. Except the vertices set
 * and the number of edges to generate, the model has four parameters: the probabilities \((a,b,c,d)\). The generated
 * graphs may be either directed or undirected, but parallel edges are never created.
 *
 * <p>
 * The probabilities \((a,b,c,d)\) must be in \([0,1]\) and sum to \(1\). If the graph is undirected, the probabilities
 * \(b\) and \(c\) must be equal. By default, the values of \((a,b,c,d)\) are \((0.57,0.21,0.17,0.05)\) for directed
 * graphs and \((0.57,0.19,0.19,0.05)\) for undirected graphs. The generator will generate undirected graphs by default.
 *
 * <p>
 * In the following example, a graph with \(9\) vertices and \(23\) edges is generated using the R-MAT model. The
 * probabilities \((a,b,c,d)\) are \((0.52,0.26,0.17,0.5)\), and the seed of the random number generator is set to some
 * fixed value to get deterministic behavior.
 *
 * <pre> {@code
 * Graph<Integer, Integer> g = new RecursiveMatrixGraphGenerator<>(IntGraphFactory.directed())
 * 		.directed(true)
 * 		.vertices(9)
 * 		.edges(23)
 * 		.edgeProbabilities(0.52, 0.26, 0.17, 0.5)
 * 		.seed(0x7d0c16fa09e05751L)
 * 		.generate();
 * } </pre>
 *
 * <p>
 * For deterministic behavior, set the seed of the generator using {@link #seed(long)}.
 *
 * <p>
 * Based on 'R-MAT: A Recursive Model for Graph Mining' by Chakrabarti et al.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class RecursiveMatrixGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Variant2<List<V>, IntObjectPair<IdBuilder<V>>> vertices;
	private IntObjectPair<IdBuilder<E>> edges;
	private boolean directed = false;
	private double a, b, c, d;
	private boolean abcdDefault = true;
	private final Random rand = new Random();

	/**
	 * Create a new R-MAT generator that will use the default graph factory.
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
	public RecursiveMatrixGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new R-MAT generator that will use the given graph factory.
	 *
	 * <p>
	 * If the factory has vertex and/or edge builders, they will be used to generate the vertices and edges of the
	 * generated graph(s) if only the number of vertices or edges is set using {@link #vertices(int)} or
	 * {@link #edges(int)}.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration. The method
	 * {@link GraphFactory#allowSelfEdges()} is always called.
	 *
	 * <p>
	 * To generate {@linkplain IntGraph int graphs}, pass an instance of {@linkplain IntGraphFactory} to this
	 * constructor.
	 *
	 * @param factory the graph factory that will be used to create the generated graph(s)
	 */
	public RecursiveMatrixGraphGenerator(GraphFactory<V, E> factory) {
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
	 * called to align the created graph with the generator configuration. The method
	 * {@link GraphFactory#allowSelfEdges()} is always called.
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
	public RecursiveMatrixGraphGenerator<V, E> vertices(Collection<? extends V> vertices) {
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
	 * {@linkplain #RecursiveMatrixGraphGenerator(GraphFactory) constructor}. Another alternative is to use
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
	public RecursiveMatrixGraphGenerator<V, E> vertices(int verticesNum) {
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
	public RecursiveMatrixGraphGenerator<V, E> vertices(int verticesNum, IdBuilder<V> vertexBuilder) {
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
	 * {@linkplain #RecursiveMatrixGraphGenerator(GraphFactory) constructor}. Another alternative is to use
	 * {@link #edges(int, IdBuilder)} which set the number of edges and provide an edge builder that will override the
	 * (maybe non existing) edge builder of the graph factory. The generation will happen independently for each graph
	 * generated. If there is no edge builder, an exception will be thrown during generation. This method override all
	 * previous calls to {@link #edges(int)} or {@link #edges(int, IdBuilder)}.
	 *
	 * @param  edgesNum                 the number of edges
	 * @return                          this generator
	 * @throws IllegalArgumentException if {@code edgesNum} is negative
	 */
	public RecursiveMatrixGraphGenerator<V, E> edges(int edgesNum) {
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
	public RecursiveMatrixGraphGenerator<V, E> edges(int edgesNum, IdBuilder<E> edgeBuilder) {
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
	public RecursiveMatrixGraphGenerator<V, E> directed(boolean directed) {
		this.directed = directed;
		return this;
	}

	/**
	 * Set the edge probabilities of the generated graph(s).
	 *
	 * <p>
	 * The generator accept as an input how many edges to generate, and it generate them one by one: each edge is
	 * assigned to a quadrant according to the probabilities \((a,b,c,d)\), and then the quadrant is recursively
	 * partitioned until a single cell is reached. The cell is then assigned the edge. The process is repeated until the
	 * required number of edges is generated.
	 *
	 * <p>
	 * The probabilities \((a,b,c,d)\) are corresponding to the four quadrants of the adjacency matrix, and they must be
	 * in \([0,1]\) and sum to \(1\). If the graph is undirected, the probabilities \(b\) and \(c\) must be equal. By
	 * default, the values of \((a,b,c,d)\) are \((0.57,0.21,0.17,0.05)\) for directed graphs and
	 * \((0.57,0.19,0.19,0.05)\) for undirected graphs.
	 *
	 * @param  a                        the probability of the edge to be in the first quadrant
	 * @param  b                        the probability of the edge to be in the second quadrant
	 * @param  c                        the probability of the edge to be in the third quadrant
	 * @param  d                        the probability of the edge to be in the fourth quadrant
	 * @return                          this generator
	 * @throws IllegalArgumentException if the probabilities are not in the range \([0,1]\) or do not sum to \(1\)
	 */
	public RecursiveMatrixGraphGenerator<V, E> edgeProbabilities(double a, double b, double c, double d) {
		if ((a < 0 || b < 0 || c < 0 || d < 0) || Math.abs(a + b + c + d - 1) > 1e-6)
			throw new IllegalArgumentException("edge probabilities a,b,c,d must be in [0,1] and sum to 1");
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		abcdDefault = false;
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
	public RecursiveMatrixGraphGenerator<V, E> seed(long seed) {
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
		if (m > 0.75 * n * (n - 1))
			throw new IllegalArgumentException(
					"too many edges for random sampling (max=" + (int) (0.75 * n * (n - 1)) + ")");

		double a, b, c, d;
		if (abcdDefault) {
			if (directed) {
				a = 0.57;
				b = 0.21;
				c = 0.17;
				d = 0.05;
			} else {
				a = 0.57;
				b = 0.19;
				c = 0.19;
				d = 0.05;
			}
		} else {
			a = this.a;
			b = this.b;
			c = this.c;
			d = this.d;
		}
		if (!directed && b != c)
			throw new IllegalArgumentException("b and c must be equal for undirected graphs");
		assert a >= 0 && b >= 0 && c >= 0 && d >= 0;
		assert Math.abs(a + b + c + d - 1) <= 1e-6;
		final double p1 = a;
		final double p2 = a + b;
		final double p3 = a + b + c;
		final double p4 = a + b + c + d;

		final int depth = nextPowerOf2(n);
		final int N = 1 << depth;
		Bitmap edges = new Bitmap(N * N);

		for (int edgeNum = 0; edgeNum < m;) {
			int u = 0, v = 0;
			for (int s = depth; s > 0; s--) {
				double p = rand.nextDouble();
				if (p < p1) {
					/* intentional */
				} else if (p < p2) {
					v += 1 << (s - 1);
				} else if (p < p3) {
					u += 1 << (s - 1);
				} else {
					assert p < p4;
					u += 1 << (s - 1);
					v += 1 << (s - 1);
				}
			}
			if (edges.get(u * N + v))
				continue;
			if (!directed && u > v)
				continue;
			edges.set(u * N + v);
			edgeNum++;
		}

		// TODO: self edges should be optinal
		GraphBuilder<V, E> g = factory.setDirected(directed).allowSelfEdges().newBuilder();

		IdBuilder<E> edgeBuilder = this.edges.second() != null ? this.edges.second() : g.edgeBuilder();
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
		if (directed) {
			for (int uIdx : range(n)) {
				for (int vIdx : range(n)) {
					V u = vertices.get(uIdx);
					if (edges.get(uIdx * N + vIdx)) {
						V v = vertices.get(vIdx);
						g.addEdge(u, v, edgeBuilder.build(g.edges()));
					}
				}
			}
		} else {
			for (int uIdx : range(n)) {
				V u = vertices.get(uIdx);
				for (int vIdx : range(uIdx, n)) {
					if (edges.get(uIdx * N + vIdx)) {
						V v = vertices.get(vIdx);
						g.addEdge(u, v, edgeBuilder.build(g.edges()));
					}
				}
			}
		}
		return g;
	}

	private static int nextPowerOf2(int x) {
		return x == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(x - 1);
	}

}
