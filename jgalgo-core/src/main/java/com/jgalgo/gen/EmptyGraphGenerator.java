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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilder;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Generate a graph with no edges.
 *
 * <p>
 * An empty graph is a graph with no edges, and it may contains some vertices.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class EmptyGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Variant2<List<V>, IntObjectPair<IdBuilder<V>>> vertices;
	private boolean directed = false;

	/**
	 * Create a new empty generator that will use the default graph factory.
	 *
	 * <p>
	 * The default graph factory does not have a default vertex builder, so if only the number of vertices is set using
	 * {@link #vertices(int)}, the vertex builder must be set explicitly using
	 * {@code graphFactory().setVertexBuilder(...)}. Alternatively, the methods {@link #vertices(int, IdBuilder)} can be
	 * used to set the number of vertices and provide a vertex/edge builder that will override the (maybe non existing)
	 * vertex builder of the graph factory. The vertex set can also be set explicitly using
	 * {@link #vertices(Collection)}.
	 */
	public EmptyGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new empty generator that will use the given graph factory.
	 *
	 * <p>
	 * If the factory has a vertex builder, it will be used to generate the vertices of the generated graph(s) if only
	 * the number of vertices is set using {@link #vertices(int)}. Set
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
	public EmptyGraphGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	/**
	 * Get the graph factory that will be used to create the generated graph(s).
	 *
	 * <p>
	 * It's possible to customize the factory before generating the graph(s), for example by using
	 * {@link GraphFactory#addHint(GraphFactory.Hint)} to optimize the generated graph(s) for a specific algorithm. The
	 * vertex builder will be used to generate the vertices of the generated graph(s) if only the number of vertices is
	 * set using {@link #vertices(int)}. Set the vertex builder of the factory to use these functions.
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
	 * Generate a new empty undirected graph.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  vertices the vertices of the generated graph
	 * @return          a new empty undirected graph
	 */
	public static <V, E> Graph<V, E> emptyGraph(Collection<? extends V> vertices) {
		return new EmptyGraphGenerator<V, E>().vertices(vertices).generate();
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
	public EmptyGraphGenerator<V, E> vertices(Collection<? extends V> vertices) {
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
	 * {@linkplain #EmptyGraphGenerator(GraphFactory) constructor}. Another alternative is to use
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
	public EmptyGraphGenerator<V, E> vertices(int verticesNum) {
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
	public EmptyGraphGenerator<V, E> vertices(int verticesNum, IdBuilder<V> vertexBuilder) {
		if (verticesNum < 0)
			throw new IllegalArgumentException("number of vertices must be non-negative");
		this.vertices = Variant2.ofB(IntObjectPair.of(verticesNum, vertexBuilder));
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
	public EmptyGraphGenerator<V, E> directed(boolean directed) {
		this.directed = directed;
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		GraphBuilder<V, E> g = factory.setDirected(directed).newBuilder();
		if (vertices != null) {
			if (this.vertices.contains(List.class)) {
				@SuppressWarnings("unchecked")
				List<V> vertices = this.vertices.get(List.class);
				g.addVertices(vertices);
			} else {
				@SuppressWarnings("unchecked")
				IntObjectPair<IdBuilder<V>> p = this.vertices.get(IntObjectPair.class);
				int verticesNum = p.firstInt();
				IdBuilder<V> vertexBuilder = p.second() != null ? p.second() : g.vertexBuilder();
				if (vertexBuilder == null)
					throw new IllegalStateException("Vertex builder not provided and graph factory does not have one");
				g.ensureVertexCapacity(verticesNum);
				for (int i = 0; i < verticesNum; i++)
					g.addVertex(vertexBuilder.build(g.vertices()));
			}
		}
		return g;
	}

}
