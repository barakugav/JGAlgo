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
 * Generate a uniform random tree.
 *
 * <p>
 * The generator uses the Prufer sequence method to generate a uniform random tree. The Prufer sequence is a sequence of
 * {@code n-2} integers sampled uniformly in the range {@code [0,n)}, where {@code n} is the number of vertices. The
 * sequence is than converted to a tree using the algorithm described in the paper "An Optimal Algorithm for Prufer
 * Codes" by Xiaodong Wang, Lei Wang and Yingjie Wu. The algorithm runs in linear time.
 *
 * <p>
 * The generator generate undirected graphs only. If zero vertices are set, an empty graph is generated.
 *
 * <p>
 * For deterministic behavior, set the seed of the generator using {@link #seed(long)}.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class UniformTreeGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Variant2<List<V>, IntObjectPair<IdBuilder<V>>> vertices;
	private IdBuilder<E> edgeBuilder;
	private Random rand = new Random();

	/**
	 * Create a new uniform tree generator that will use the default graph factory.
	 *
	 * <p>
	 * The default graph factory does not have vertex builder, so if only the number of vertices is set using
	 * {@link #vertices(int)}, the vertex builder must be set explicitly using
	 * {@code graphFactory().setVertexBuilder(...)}. Alternatively, the method {@link #vertices(int, IdBuilder)} can be
	 * used to set the number of vertices and provide a vertex builder that will override the (maybe non existing)
	 * vertex builder of the graph factory. The vertex set can also be set explicitly using
	 * {@link #vertices(Collection)}. For edges, an edge builder is mandatory and it can be set using
	 * {@link #edges(IdBuilder)}.
	 */
	public UniformTreeGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new uniform tree generator that will use the given graph factory.
	 *
	 * <p>
	 * If the factory has a vertex builder it will be used to generate the vertices of the generated graph(s) if only
	 * the number of vertices is set using {@link #vertices(int)}. If the factory has an edge builder it will be used to
	 * generate the edges of the generated graph(s) if it will not be overridden by {@link #edges(IdBuilder)}.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration.
	 *
	 * <p>
	 * To generate {@linkplain IntGraph int graphs}, pass an instance of {@linkplain IntGraphFactory} to this
	 * constructor.
	 *
	 * @param factory the graph factory that will be used to create the generated graph(s)
	 */
	public UniformTreeGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	/**
	 * Get the graph factory that will be used to create the generated graph(s).
	 *
	 * <p>
	 * It's possible to customize the factory before generating the graph(s), for example by using
	 * {@link GraphFactory#addHint(GraphFactory.Hint)} to optimize the generated graph(s) for a specific algorithm. If
	 * the factory has a vertex builder it will be used to generate the vertices of the generated graph(s) if only the
	 * number of vertices is set using {@link #vertices(int)}. If the factory has an edge builder it will be used to
	 * generate the edges of the generated graph(s) if it will not be overridden by {@link #edges(IdBuilder)}.
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
	public UniformTreeGenerator<V, E> vertices(Collection<? extends V> vertices) {
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
	 * {@linkplain #UniformTreeGenerator(GraphFactory) constructor}. Another alternative is to use
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
	public UniformTreeGenerator<V, E> vertices(int verticesNum) {
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
	public UniformTreeGenerator<V, E> vertices(int verticesNum, IdBuilder<V> vertexBuilder) {
		if (verticesNum < 0)
			throw new IllegalArgumentException("number of vertices must be non-negative");
		this.vertices = Variant2.ofB(IntObjectPair.of(verticesNum, vertexBuilder));
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
	public UniformTreeGenerator<V, E> edges(IdBuilder<E> edgeBuilder) {
		this.edgeBuilder = edgeBuilder;
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
	public UniformTreeGenerator<V, E> seed(long seed) {
		rand = new Random(seed);
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		final int n = vertices.map(List::size, IntObjectPair::firstInt).intValue();

		GraphBuilder<V, E> g = factory.setDirected(false).newBuilder();
		IdBuilder<E> edgeBuilder = this.edgeBuilder != null ? this.edgeBuilder : g.edgeBuilder();
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge builder not provided and graph factory does not have one");

		if (n == 0)
			return g;
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
		if (n == 1)
			return g;

		/* generate a random Prufer code of length n-2 */
		int[] pruferCode = new int[n - 2];
		for (int i = 0; i < n - 2; i++)
			pruferCode[i] = rand.nextInt(n);

		/* 'decode' the Prufer code to a tree of size n */
		int[] degree = new int[n];
		for (int c : pruferCode)
			degree[c]++;
		int index, uIdx;
		V u = vertices.get(uIdx = index = range(n).filter(i -> degree[i] == 0).findFirst().getAsInt());
		Bitmap hasParent = new Bitmap(n);
		for (int vIdx : pruferCode) {
			V v = vertices.get(vIdx);
			g.addEdge(u, v, edgeBuilder.build(g.edges()));
			hasParent.set(uIdx);

			degree[vIdx]--;
			if (vIdx < index && degree[vIdx] == 0) {
				u = v;
				uIdx = vIdx;
			} else {
				u = vertices.get(uIdx = index = range(index + 1, n).filter(i -> degree[i] == 0).findFirst().getAsInt());
			}
		}

		/* After the above loop, there should be two vertices with no parent. Connect them */
		assert Bitmap.fromPredicate(n, v -> !hasParent.get(v)).cardinality() == 2;
		int root1Idx = hasParent.nextClearBit(0);
		int root2Idx = hasParent.nextClearBit(root1Idx + 1);
		V root1 = vertices.get(root1Idx);
		V root2 = vertices.get(root2Idx);
		g.addEdge(root1, root2, edgeBuilder.build(g.edges()));

		return g;
	}

}
