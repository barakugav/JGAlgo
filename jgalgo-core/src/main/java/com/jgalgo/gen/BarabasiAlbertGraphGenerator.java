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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.IntBinaryOperator;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Generates a Barabási–Albert graph.
 *
 * <p>
 * A Barabási–Albert graph is a random graph with a power law degree distribution, which is a good model for many real
 * networks. The graph begins with an initial clique of size {@code initCliqueSize}, and then adds vertices one by one,
 * each with {@code k} edges that are attached to existing vertices. The probability that a new vertex is connected to
 * vertex \(v\) is proportional to the degree of \(v\) divided by the sum of degrees of all vertices in the graph.
 *
 * <p>
 * By default the initial clique size is \(20\) and the number of edges added each time step (denoted {@code k}) is
 * \(10\). The generated graph(s) may be directed or undirected, and by default it is undirected. Self edges are never
 * created.
 *
 * <p>
 * For deterministic behavior, set the seed of the generator using {@link #setSeed(long)}.
 *
 * <p>
 * Based on 'Emergence of scaling in random networks' by Albert-László Barabási and Réka Albert.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class BarabasiAlbertGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private Collection<V> vertices;
	private int initCliqueSize = 20;
	private int k = 10;
	private BiFunction<V, V, E> edgeBuilder;
	private boolean directed = false;
	private Random rand = new Random();

	private BarabasiAlbertGraphGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new Barabási–Albert graph generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new Barabási–Albert graph generator
	 */
	public static <V, E> BarabasiAlbertGraphGenerator<V, E> newInstance() {
		return new BarabasiAlbertGraphGenerator<>(false);
	}

	/**
	 * Creates a new Barabási–Albert graph generator for {@link IntGraph}.
	 *
	 * @return a new Barabási–Albert graph generator for {@link IntGraph}
	 */
	public static BarabasiAlbertGraphGenerator<Integer, Integer> newIntInstance() {
		return new BarabasiAlbertGraphGenerator<>(true);
	}

	/**
	 * Set the vertices of the generated graph(s).
	 *
	 * <p>
	 * If the generator is used to generate multiple graphs, the same vertices set is used for all of them.
	 *
	 * @param vertices the vertices set
	 */
	@SuppressWarnings("unchecked")
	public void setVertices(Collection<V> vertices) {
		if (intGraph) {
			this.vertices =
					(Collection<V>) new IntArrayList(IntAdapters.asIntCollection((Collection<Integer>) vertices));
		} else {
			this.vertices = new ObjectArrayList<>(vertices);
		}
	}

	/**
	 * Set the vertices set of the generated graph(s) from a supplier.
	 *
	 * <p>
	 * The supplier will be called exactly {@code verticesNum} times, and the same set of vertices created will be used
	 * for multiple graphs if {@link #generate()} is called multiple times.
	 *
	 * @param verticesNum    the number of vertices
	 * @param vertexSupplier the supplier of vertices
	 */
	@SuppressWarnings("unchecked")
	public void setVertices(int verticesNum, Supplier<V> vertexSupplier) {
		if (intGraph) {
			IntList vertices = new IntArrayList(verticesNum);
			IntSupplier vSupplier = IntAdapters.asIntSupplier((Supplier<Integer>) vertexSupplier);
			for (int i = 0; i < verticesNum; i++)
				vertices.add(vSupplier.getAsInt());
			this.vertices = (Collection<V>) vertices;
		} else {
			List<V> vertices = new ObjectArrayList<>(verticesNum);
			for (int i = 0; i < verticesNum; i++)
				vertices.add(vertexSupplier.get());
			this.vertices = vertices;
		}
	}

	/**
	 * Set the edge supplier of the generated graph(s).
	 *
	 * <p>
	 * The supplier will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, Supplier)}, where the supplier is used to generate a set of vertices which is reused for
	 * any generated graph.
	 *
	 * @param edgeSupplier the edge supplier
	 */
	public void setEdges(Supplier<E> edgeSupplier) {
		Objects.requireNonNull(edgeSupplier);
		setEdges((u, v) -> edgeSupplier.get());
	}

	/**
	 * Set the edge builder function of the generated graph(s).
	 *
	 * <p>
	 * The function will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, Supplier)}, where the supplier is used to generate a set of vertices which is reused for
	 * any generated graph.
	 *
	 * @param edgeBuilder the edge builder function
	 */
	public void setEdges(BiFunction<V, V, E> edgeBuilder) {
		this.edgeBuilder = Objects.requireNonNull(edgeBuilder);
	}

	/**
	 * Set the initial clique size of the generated graph(s).
	 *
	 * <p>
	 * The initial clique is a complete graph of size {@code initCliqueSize}. After the initial clique is created, the
	 * generator adds vertices one by one, each with {@code k} edges that are attached to existing vertices. The
	 * probability that a new vertex is connected to vertex \(v\) is proportional to the degree of \(v\) divided by the
	 * sum of degrees of all vertices in the graph.
	 *
	 * <p>
	 * By default, the initial clique size is \(20\). The initial clique size must not be greater than the number of
	 * vertices provided by {@link #setVertices}
	 *
	 * @param initCliqueSize the initial clique size
	 */
	public void setInitialCliqueSize(int initCliqueSize) {
		if (initCliqueSize < 0)
			throw new IllegalArgumentException("initCliqueSize must be non-negative: " + initCliqueSize);
		this.initCliqueSize = initCliqueSize;
	}

	/**
	 * Set the number of edges added each time step (k) when generated graph(s).
	 *
	 * <p>
	 * The initial clique is a complete graph of size {@link #setInitialCliqueSize}. After the initial clique is
	 * created, the generator adds vertices one by one, each with {@code k} edges that are attached to existing
	 * vertices. The probability that a new vertex is connected to vertex \(v\) is proportional to the degree of \(v\)
	 * divided by the sum of degrees of all vertices in the graph.
	 *
	 * <p>
	 * By default, the number of edges added per time step is \(10\). The number of edges per time step must not be
	 * greater than the initial clique size provided by {@link #setInitialCliqueSize}.
	 *
	 * @param k the number of edges added to each vertex added to the graph after the initial clique
	 */
	public void setEdgesToAddPerStep(int k) {
		if (k < 0)
			throw new IllegalArgumentException("k must be non-negative: " + k);
		this.k = k;
	}

	/**
	 * Determine if the generated graph(s) is directed or undirected.
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @param directed {@code true} if the generated graph(s) will be directed, {@code false} if undirected
	 */
	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	/**
	 * Set the seed of the random number generator used to generate the graph(s).
	 *
	 * <p>
	 * By default, a random seed is used. For deterministic behavior, set the seed of the generator.
	 *
	 * @param seed the seed of the random number generator
	 */
	public void setSeed(long seed) {
		rand = new Random(seed);
	}

	@SuppressWarnings("unchecked")
	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge supplier not set");
		if (initCliqueSize > vertices.size())
			throw new IllegalStateException(
					"initCliqueSize must be smaller than vertices num: " + initCliqueSize + " > " + vertices.size());
		if (k > initCliqueSize)
			throw new IllegalStateException("k must be smaller than initCliqueSize: " + k + " > " + initCliqueSize);
		final int n = vertices.size();

		int initEdgesNum = initCliqueSize * (initCliqueSize - 1) / (directed ? 1 : 2);
		int addedEdgesNum = (vertices.size() - initCliqueSize) * k;
		int[] endpoints = new int[(initEdgesNum + addedEdgesNum) * 2];
		int edgeNum = 0;

		/* start with a complete graph of size initCliqueSize */
		for (int u = 0; u < initCliqueSize; u++) {
			if (directed) {
				for (int v = 0; v < u; v++) {
					int e = edgeNum++;
					endpoints[e * 2 + 0] = u;
					endpoints[e * 2 + 1] = v;
				}
			}
			for (int v = u + 1; v < initCliqueSize; v++) {
				int e = edgeNum++;
				endpoints[e * 2 + 0] = u;
				endpoints[e * 2 + 1] = v;
			}
		}

		/* add n-initCliqueSize vertices, each with m edges */
		for (int vNum = initCliqueSize; vNum < n; vNum++) {
			final int edgeNumAtStart = edgeNum;
			final int u = vNum;
			for (int i = 0; i < k; i++) {
				/* by sampling from the current endpoints, we sample a vertex with prob of its degree */
				int v = endpoints[rand.nextInt(edgeNumAtStart * 2)];
				int e = edgeNum++;
				int u0, v0;
				if (directed && rand.nextBoolean()) {
					u0 = u;
					v0 = v;
				} else {
					u0 = v;
					v0 = u;
				}
				endpoints[e * 2 + 0] = u0;
				endpoints[e * 2 + 1] = v0;
			}
		}

		if (intGraph) {
			IntGraphFactory factory = directed ? IntGraphFactory.newDirected() : IntGraphFactory.newUndirected();
			IntGraphBuilder g = factory.allowParallelEdges().newBuilder();
			g.expectedVerticesNum(n);
			g.expectedVerticesNum(edgeNum);
			final int[] vertices = IntAdapters.asIntCollection((Collection<Integer>) this.vertices).toIntArray();
			for (int v : vertices)
				g.addVertex(v);
			IntBinaryOperator edgeBuilder =
					IntAdapters.asIntBiOperator((BiFunction<Integer, Integer, Integer>) this.edgeBuilder);
			for (int eIdx = 0; eIdx < edgeNum; eIdx++) {
				int u = vertices[endpoints[eIdx * 2 + 0]];
				int v = vertices[endpoints[eIdx * 2 + 1]];
				g.addEdge(u, v, edgeBuilder.applyAsInt(u, v));
			}
			return (GraphBuilder<V, E>) g;

		} else {
			GraphFactory<V, E> factory = directed ? GraphFactory.newDirected() : GraphFactory.newUndirected();
			GraphBuilder<V, E> g = factory.allowParallelEdges().newBuilder();
			g.expectedVerticesNum(n);
			g.expectedVerticesNum(edgeNum);
			final V[] vertices = (V[]) this.vertices.toArray();
			for (V v : vertices)
				g.addVertex(v);
			for (int eIdx = 0; eIdx < edgeNum; eIdx++) {
				V u = vertices[endpoints[eIdx * 2 + 0]];
				V v = vertices[endpoints[eIdx * 2 + 1]];
				g.addEdge(u, v, edgeBuilder.apply(u, v));
			}
			return g;
		}
	}

}
