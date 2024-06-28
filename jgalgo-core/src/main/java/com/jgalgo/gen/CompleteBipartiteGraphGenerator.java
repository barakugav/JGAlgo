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
import com.jgalgo.alg.bipartite.BipartiteGraphs;
import com.jgalgo.alg.common.VertexBiPartition;
import com.jgalgo.gen.BipartiteGenerators.Direction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilder;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Generates a complete bipartite graph.
 *
 * <p>
 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices. A
 * complete bipartite graph is a bipartite graph in which each pair of left and right vertices is connected by an edge.
 *
 * <p>
 * Both undirected and directed graphs can be generated. If the graph is directed, there are three options for the
 * generated edges between each pair of left and right vertices: generate two edges in both directions, generate one
 * edge from the left vertex to the right vertex, or generate one edge from the right vertex to the left vertex. See
 * {@link #directedAll()}, {@link #directedLeftToRight()} and {@link #directedRightToLeft()} for more details.
 *
 * <p>
 * The generated graph(s) will have vertex {@linkplain WeightsBool boolean weights} with key
 * {@link BipartiteGraphs#VertexBiPartitionWeightKey} which is the partition of the vertices into the left and right set
 * of vertices. The weight is set to {@code true} for vertices in the left set, and {@code false} for vertices in the
 * right set. The {@link VertexBiPartition} can be created later using
 * {@link BipartiteGraphs#getExistingPartition(Graph)}.
 *
 * <p>
 * In the following example, a complete bipartite graph with four left and six right vertices is generated:
 *
 * <pre> {@code
 * Graph<Integer, Integer> g = new CompleteBipartiteGraphGenerator<>(IntGraphFactory.directed())
 * 		.directedAll()
 * 		.vertices(4, 6)
 * 		.edges(IdBuilderInt.defaultBuilder())
 * 		.generate();
 * } </pre>
 *
 * <p>
 * By default, the generated graph(s) is undirected. Self and parallel edges are never created.
 *
 * <p>
 * This generator is the bipartite version of {@link CompleteGraphGenerator}.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @see        BipartiteGraphs
 * @author     Barak Ugav
 */
public class CompleteBipartiteGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Variant2<List<V>[], Pair<IntIntPair, IdBuilder<V>>> vertices;
	private IdBuilder<E> edgeBuilder;
	private Direction direction = Direction.Undirected;

	/**
	 * Create a new complete bipartite graph generator that will use the default graph factory.
	 *
	 * <p>
	 * The default graph factory does not have vertex builder, so if only the number of vertices is set using
	 * {@link #vertices(int, int)}, the vertex builder must be set explicitly using
	 * {@code graphFactory().setVertexBuilder(...)}. Same holds for edges, for which the number of them is determine by
	 * the number of vertices and the direction of the generated edges. Alternatively, the methods
	 * {@link #vertices(int, int, IdBuilder)} and {@link #edges(IdBuilder)} can be used to set the number of vertices
	 * and provide a vertex/edge builder that will override the (maybe non existing) vertex/edge builder of the graph
	 * factory. The vertex set can also be set explicitly using {@link #vertices(Collection, Collection)}.
	 */
	public CompleteBipartiteGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new complete bipartite graph generator that will use the given graph factory.
	 *
	 * <p>
	 * If the factory has a vertex builder it will be used to generate the vertices of the generated graph(s) if only
	 * the number of vertices is set using {@link #vertices(int, int)}. If the factory has an edge builder it will be
	 * used to generate the edges of the generated graph(s) if it will not be overridden by {@link #edges(IdBuilder)}.
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
	public CompleteBipartiteGraphGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	/**
	 * Get the graph factory that will be used to create the generated graph(s).
	 *
	 * <p>
	 * It's possible to customize the factory before generating the graph(s), for example by using
	 * {@link GraphFactory#addHint(GraphFactory.Hint)} to optimize the generated graph(s) for a specific algorithm. If
	 * the factory has a vertex builder it will be used to generate the vertices of the generated graph(s) if only the
	 * number of vertices is set using {@link #vertices(int, int)}. If the factory has an edge builder it will be used
	 * to generate the edges of the generated graph(s) if it will not be overridden by {@link #edges(IdBuilder)}.
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
	 * Set the vertices of the generated graph(s).
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * This method sets these two sets.
	 *
	 * <p>
	 * If the generator is used to generate multiple graphs, the same vertex sets will be used for all of them. This
	 * method override all previous calls to any of {@link #vertices(Collection, Collection)},
	 * {@link #vertices(int, int)} or {@link #vertices(int, int, IdBuilder)}.
	 *
	 * @param  leftVertices  the set of left vertices of the generated graph(s)
	 * @param  rightVertices the set of right vertices of the generated graph(s)
	 * @return               this generator
	 */
	@SuppressWarnings("unchecked")
	public CompleteBipartiteGraphGenerator<V, E> vertices(Collection<? extends V> leftVertices,
			Collection<? extends V> rightVertices) {
		if (factory instanceof IntGraphFactory) {
			leftVertices = (List<V>) new IntArrayList((Collection<Integer>) leftVertices);
			rightVertices = (List<V>) new IntArrayList((Collection<Integer>) rightVertices);
		} else {
			leftVertices = new ObjectArrayList<>(leftVertices);
			rightVertices = new ObjectArrayList<>(rightVertices);
		}
		List<V>[] vertices = new List[] { (List<V>) leftVertices, (List<V>) rightVertices };
		this.vertices = Variant2.ofA(vertices);
		return this;
	}

	/**
	 * Set the number of vertices that will be generated for each graph.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * This method sets these two sets.
	 *
	 * <p>
	 * The vertices will be generated using the vertex builder of the graph factory, see
	 * {@link GraphFactory#setVertexBuilder(IdBuilder)}. The default graph factory does not have a vertex builder, so it
	 * must be set explicitly, or {@link IntGraphFactory}, which does have such builder, should be passed in the
	 * {@linkplain #CompleteBipartiteGraphGenerator(GraphFactory) constructor}. Another alternative is to use
	 * {@link #vertices(int, int, IdBuilder)} which set the number of vertices and provide a vertex builder that will
	 * override the (maybe non existing) vertex builder of the graph factory. The generation will happen independently
	 * for each graph generated. If there is no vertex builder, an exception will be thrown during generation. This
	 * method override all previous calls to any of {@link #vertices(Collection, Collection)},
	 * {@link #vertices(int, int)} or {@link #vertices(int, int, IdBuilder)}.
	 *
	 * @param  leftVerticesNum          the number of vertices that will be generated in the left set for each graph
	 * @param  rightVerticesNum         the number of vertices that will be generated in the right set for each graph
	 * @return                          this generator
	 * @throws IllegalArgumentException if {@code leftVerticesNum} or {@code rightVerticesNum} are negative
	 */
	public CompleteBipartiteGraphGenerator<V, E> vertices(int leftVerticesNum, int rightVerticesNum) {
		vertices(leftVerticesNum, rightVerticesNum, null);
		return this;
	}

	/**
	 * Set the number of vertices that will be generated for each graph, and the vertex builder that will be used to
	 * generate them.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * This method sets these two sets.
	 *
	 * <p>
	 * The vertices will be generated using the provided vertex builder, and the vertex generator provided by the
	 * {@linkplain #graphFactory() graph factory} (if exists) will be ignored. The generation will happen independently
	 * for each graph generated. This method override all previous calls to any of
	 * {@link #vertices(Collection, Collection)}, {@link #vertices(int, int)} or {@link #vertices(int, int, IdBuilder)}.
	 *
	 * @param  leftVerticesNum          the number of vertices that will be generated in the left set for each graph
	 * @param  rightVerticesNum         the number of vertices that will be generated in the right set for each graph
	 * @param  vertexBuilder            the vertex builder, or {@code null} to use the vertex builder of the
	 *                                      {@linkplain #graphFactory() graph factory}
	 * @return                          this generator
	 * @throws IllegalArgumentException if {@code leftVerticesNum} or {@code rightVerticesNum} are negative
	 */
	public CompleteBipartiteGraphGenerator<V, E> vertices(int leftVerticesNum, int rightVerticesNum,
			IdBuilder<V> vertexBuilder) {
		if (leftVerticesNum < 0 || rightVerticesNum < 0)
			throw new IllegalArgumentException("number of vertices must be non-negative");
		vertices = Variant2.ofB(Pair.of(IntIntPair.of(leftVerticesNum, rightVerticesNum), vertexBuilder));
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
	public CompleteBipartiteGraphGenerator<V, E> edges(IdBuilder<E> edgeBuilder) {
		this.edgeBuilder = edgeBuilder;
		return this;
	}

	/**
	 * Sets the generated graph(s) to be undirected.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * Calling this method will cause the generated graph(s) to be undirected, all a single edge will be generated for
	 * each pair of left and right vertices. The number of edges will be \(|U| \cdot |V|\).
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @see    #directedAll()
	 * @see    #directedLeftToRight()
	 * @see    #directedRightToLeft()
	 * @return this generator
	 */
	public CompleteBipartiteGraphGenerator<V, E> undirected() {
		direction = Direction.Undirected;
		return this;
	}

	/**
	 * Sets the generated graph(s) to be directed with edges in both directions.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * Calling this method will cause the generated graph(s) to be directed, and all edges from the left vertices set to
	 * the right vertices set will be generated, and also from the right vertices set to the left vertices set. The
	 * number of edges will be \(2 \cdot |U| \cdot |V|\).
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @see    #undirected()
	 * @see    #directedLeftToRight()
	 * @see    #directedRightToLeft()
	 * @return this generator
	 */
	public CompleteBipartiteGraphGenerator<V, E> directedAll() {
		direction = Direction.DirectedAll;
		return this;
	}

	/**
	 * Sets the generated graph(s) to be directed with edges from left to right.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * Calling this method will cause the generated graph(s) to be directed, and all edges will be directed from the
	 * left vertices set to the right vertices set. The number of edges will be \(|U| \cdot |V|\).
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @see    #undirected()
	 * @see    #directedAll()
	 * @see    #directedRightToLeft()
	 * @return this generator
	 */
	public CompleteBipartiteGraphGenerator<V, E> directedLeftToRight() {
		direction = Direction.DirectedLeftToRight;
		return this;
	}

	/**
	 * Sets the generated graph(s) to be directed with edges from right to left.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * Calling this method will cause the generated graph(s) to be directed, and all edges will be directed from the
	 * right vertices set to the left vertices set. The number of edges will be \(|U| \cdot |V|\).
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @see    #undirected()
	 * @see    #directedAll()
	 * @see    #directedLeftToRight()
	 * @return this generator
	 */
	public CompleteBipartiteGraphGenerator<V, E> directedRightToLeft() {
		direction = Direction.DirectedRightToLeft;
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		@SuppressWarnings("boxing")
		final int leftSize = vertices.map(p -> p[0].size(), p -> p.first().firstInt()).intValue();
		@SuppressWarnings("boxing")
		final int rightSize = vertices.map(p -> p[1].size(), p -> p.first().secondInt()).intValue();
		final int edgeNum = leftSize * rightSize / (direction != Direction.DirectedAll ? 2 : 1);

		GraphBuilder<V, E> g = factory.setDirected(direction != Direction.Undirected).newBuilder();
		IdBuilder<E> edgeBuilder = this.edgeBuilder != null ? this.edgeBuilder : g.edgeBuilder();
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge builder not provided and graph factory does not have one");

		final List<V> leftVertices;
		final List<V> rightVertices;
		g.ensureVertexCapacity(leftSize + rightSize);
		if (this.vertices.contains(List[].class)) {
			@SuppressWarnings("unchecked")
			List<V>[] vertices = this.vertices.get(List[].class);
			g.addVertices(leftVertices = vertices[0]);
			g.addVertices(rightVertices = vertices[1]);
		} else {
			@SuppressWarnings("unchecked")
			Pair<IntIntPair, IdBuilder<V>> p = this.vertices.get(Pair.class);
			IdBuilder<V> vertexBuilder = p.second() != null ? p.second() : g.vertexBuilder();
			if (vertexBuilder == null)
				throw new IllegalStateException("Vertex builder not provided and graph factory does not have one");
			if (g instanceof IntGraphBuilder) {
				@SuppressWarnings("unchecked")
				List<V> leftVertices0 = (List<V>) new IntArrayList(leftSize);
				@SuppressWarnings("unchecked")
				List<V> rightVertices0 = (List<V>) new IntArrayList(rightSize);
				leftVertices = leftVertices0;
				rightVertices = rightVertices0;
			} else {
				leftVertices = new ObjectArrayList<>(leftSize);
				rightVertices = new ObjectArrayList<>(rightSize);
			}
			for (int i = 0; i < leftSize; i++) {
				V vertex = vertexBuilder.build(g.vertices());
				g.addVertex(vertex);
				leftVertices.add(vertex);
			}
			for (int i = 0; i < rightSize; i++) {
				V vertex = vertexBuilder.build(g.vertices());
				g.addVertex(vertex);
				rightVertices.add(vertex);
			}
		}
		WeightsBool<V> partition = g.addVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey, boolean.class);
		for (V v : leftVertices)
			partition.set(v, true);
		for (V v : rightVertices)
			partition.set(v, false);

		g.ensureEdgeCapacity(edgeNum);
		if (direction != Direction.DirectedRightToLeft)
			for (V vLeft : leftVertices)
				for (V vRight : rightVertices)
					g.addEdge(vLeft, vRight, edgeBuilder.build(g.edges()));
		if (direction == Direction.DirectedAll || direction == Direction.DirectedRightToLeft)
			for (V vRight : rightVertices)
				for (V vLeft : leftVertices)
					g.addEdge(vRight, vLeft, edgeBuilder.build(g.edges()));
		return g;
	}

}
