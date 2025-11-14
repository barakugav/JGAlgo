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
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntPair;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Generates a uniformly random bipartite graph among all graphs with \(n\) vertices and \(m\) edges.
 *
 * <p>
 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices. The
 * generator uses the \(G(n_1,n_2,m)\) model to generate a uniformly random bipartite graph among all graphs with
 * \(n_1\) left vertices, \(n_2\) right vertices and \(m\) edges.
 *
 * <p>
 * Both undirected and directed graphs can be generated. If the graph is directed, there are three options for the
 * considered edges between each pair of left and right vertices: edges in both directions, edge(s) from the left vertex
 * to the right vertex, or edge(s) from the right vertex to the left vertex. If no parallel edges are generated
 * ({@link #parallelEdges(boolean)}) than at most a single edge is generated from the left to right vertex, and another
 * one from right to left. See {@link #directedAll()}, {@link #directedLeftToRight()} and {@link #directedRightToLeft()}
 * for more details.
 *
 * <p>
 * The generated graph(s) will have vertex {@linkplain WeightsBool boolean weights} with key
 * {@link BipartiteGraphs#VertexBiPartitionWeightKey} which is the partition of the vertices into the left and right set
 * of vertices. The weight is set to {@code true} for vertices in the left set, and {@code false} for vertices in the
 * right set. The {@link VertexBiPartition} can be created later using
 * {@link BipartiteGraphs#getExistingPartition(Graph)}.
 *
 * <p>
 * By default, the generated graph(s) will be undirected without parallel edges. Self edges are never generated.
 *
 * <p>
 * In the following example, a bipartite graph with four left vertices and six right vertices is generated. The graph
 * will have ten edges, and will be undirected without parallel edges. The seed of the generator is set to ensure
 * deterministic behavior.
 *
 * <pre> {@code
 * Graph<Integer, Integer> g = new GnmBipartiteGraphGenerator<>(IntGraphFactory.undirected())
 * 		.undirected()
 * 		.vertices(4, 6)
 * 		.edges(10)
 * 		.parallelEdges(false)
 * 		.seed(0x7d0c16fa09e05751L)
 * 		.generate();
 * } </pre>
 *
 * <p>
 * For deterministic behavior, set the seed of the generator using {@link #seed(long)}.
 *
 * <p>
 * This generator is the bipartite version of {@link GnmGraphGenerator}.
 *
 * @see        BipartiteGraphs
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class GnmBipartiteGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Variant2<List<V>[], Pair<IntIntPair, IdBuilder<V>>> vertices;
	private IntObjectPair<IdBuilder<E>> edges;
	private Direction direction = Direction.Undirected;
	private boolean parallelEdges = true;
	private final Random rand = new Random();

	/**
	 * Create a new \(G(n_1,n_2,m)\) generator that will use the default graph factory.
	 *
	 * <p>
	 * The default graph factory does not have default vertex and edge builders, so if only the number of vertices and
	 * edges is set using {@link #vertices(int, int)} and {@link #edges(int)}, the vertex and edge builders must be set
	 * explicitly using {@code graphFactory().setVertexBuilder(...)} and {@code graphFactory().setEdgeBuilder(...)}.
	 * Alternatively, the methods {@link #vertices(int, int, IdBuilder)} and {@link #edges(int, IdBuilder)} can be used
	 * to set the number of vertices and edges and provide a vertex/edge builder that will override the (maybe non
	 * existing) vertex/edge builder of the graph factory. The vertex set can also be set explicitly using
	 * {@link #vertices(Collection, Collection)}.
	 */
	public GnmBipartiteGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new \(G(n_1,n_2,m)\) generator that will use the given graph factory.
	 *
	 * <p>
	 * If the factory has vertex and/or edge builders, they will be used to generate the vertices and edges of the
	 * generated graph(s) if only the number of vertices or edges is set using {@link #vertices(int, int)} or
	 * {@link #edges(int)}.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration. If parallel edges are generated (see
	 * {@link #parallelEdges(boolean)}), the method {@link GraphFactory#allowParallelEdges()} will also be called.
	 *
	 * <p>
	 * To generate {@linkplain IntGraph int graphs}, pass an instance of {@linkplain IntGraphFactory} to this
	 * constructor.
	 *
	 * @param factory the graph factory that will be used to create the generated graph(s)
	 */
	public GnmBipartiteGraphGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	/**
	 * Get the graph factory that will be used to create the generated graph(s).
	 *
	 * <p>
	 * It's possible to customize the factory before generating the graph(s), for example by using
	 * {@link GraphFactory#addHint(GraphFactory.Hint)} to optimize the generated graph(s) for a specific algorithm. The
	 * vertex and edge builders will be used to generate the vertices and edges of the generated graph(s) if only the
	 * number of vertices or edges is set using {@link #vertices(int, int)} or {@link #edges(int)}. Set the vertex/edge
	 * builder of the factory to use these functions.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration. If parallel edges are generated (see
	 * {@link #parallelEdges(boolean)}), the method {@link GraphFactory#allowParallelEdges()} will also be called.
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
	public GnmBipartiteGraphGenerator<V, E> vertices(Collection<? extends V> leftVertices,
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
	 * {@linkplain #GnmBipartiteGraphGenerator(GraphFactory) constructor}. Another alternative is to use
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
	public GnmBipartiteGraphGenerator<V, E> vertices(int leftVerticesNum, int rightVerticesNum) {
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
	public GnmBipartiteGraphGenerator<V, E> vertices(int leftVerticesNum, int rightVerticesNum,
			IdBuilder<V> vertexBuilder) {
		if (leftVerticesNum < 0 || rightVerticesNum < 0)
			throw new IllegalArgumentException("number of vertices must be non-negative");
		vertices = Variant2.ofB(Pair.of(IntIntPair.of(leftVerticesNum, rightVerticesNum), vertexBuilder));
		return this;
	}

	/**
	 * Set the number of edges what will be generated for each graph.
	 *
	 * <p>
	 * The number of edges must be non-negative, and if parallel edges are not allowed, it must be at most \(n_1 \cdot
	 * n_2\) for undirected graphs and directed graphs in which only one direction is allowed
	 * ({@linkplain #directedLeftToRight() left to right}, or {@linkplain #directedRightToLeft() right to left}), and at
	 * most \(2 \cdot n_1 \cdot n_2\) for directed graphs in which both directions are allowed
	 * ({@linkplain #directedAll() all directions}).
	 *
	 * <p>
	 * The edges will be generated using the edge builder of the graph factory, see
	 * {@link GraphFactory#setEdgeBuilder(IdBuilder)}. The default graph factory does not have an edge builder, so it
	 * must be set explicitly, or {@link IntGraphFactory}, which does have such builder, should be passed in the
	 * {@linkplain #GnmBipartiteGraphGenerator(GraphFactory) constructor}. Another alternative is to use
	 * {@link #edges(int, IdBuilder)} which set the number of edges and provide an edge builder that will override the
	 * (maybe non existing) edge builder of the graph factory. The generation will happen independently for each graph
	 * generated. If there is no edge builder, an exception will be thrown during generation. This method override all
	 * previous calls to {@link #edges(int)} or {@link #edges(int, IdBuilder)}.
	 *
	 * @param  edgesNum                 the number of edges
	 * @return                          this generator
	 * @throws IllegalArgumentException if {@code edgesNum} is negative
	 */
	public GnmBipartiteGraphGenerator<V, E> edges(int edgesNum) {
		edges(edgesNum, null);
		return this;
	}

	/**
	 * Set the number of edges what will be generated for each graph, and the edge builder that will be used to generate
	 * them.
	 *
	 * <p>
	 * The number of edges must be non-negative, and if parallel edges are not allowed, it must be at most \(n_1 \cdot
	 * n_2\) for undirected graphs and directed graphs in which only one direction is allowed
	 * ({@linkplain #directedLeftToRight() left to right}, or {@linkplain #directedRightToLeft() right to left}), and at
	 * most \(2 \cdot n_1 \cdot n_2\) for directed graphs in which both directions are allowed
	 * ({@linkplain #directedAll() all directions}).
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
	public GnmBipartiteGraphGenerator<V, E> edges(int edgesNum, IdBuilder<E> edgeBuilder) {
		if (edgesNum < 0)
			throw new IllegalArgumentException("number of edges must be non-negative");
		this.edges = IntObjectPair.of(edgesNum, edgeBuilder);
		return this;
	}

	/**
	 * Sets the generated graph(s) to be undirected.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * Calling this method will cause the generated graph(s) to be undirected, and a single edge between each pair of
	 * left and right vertices will considered and generated with probability \(p\). The maximum number of edges will be
	 * \(|U| \cdot |V|\).
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @see    #directedAll()
	 * @see    #directedLeftToRight()
	 * @see    #directedRightToLeft()
	 * @return this generator
	 */
	public GnmBipartiteGraphGenerator<V, E> undirected() {
		direction = Direction.Undirected;
		return this;
	}

	/**
	 * Sets the generated graph(s) to be directed with edges in both directions.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * Calling this method will cause the generated graph(s) to be directed, and edges in both directions (from left
	 * vertices to right vertices and visa versa) may be generated. In case parallel edges are not allowed, the maximum
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
	public GnmBipartiteGraphGenerator<V, E> directedAll() {
		direction = Direction.DirectedAll;
		return this;
	}

	/**
	 * Sets the generated graph(s) to be directed with edges from left to right.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * Calling this method will cause the generated graph(s) to be directed, and only edges from left vertices to right
	 * vertices may be generated. In case parallel edges are not allowed, the maximum number of edges will be \(|U|
	 * \cdot |V|\).
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @see    #undirected()
	 * @see    #directedAll()
	 * @see    #directedRightToLeft()
	 * @return this generator
	 */
	public GnmBipartiteGraphGenerator<V, E> directedLeftToRight() {
		direction = Direction.DirectedLeftToRight;
		return this;
	}

	/**
	 * Sets the generated graph(s) to be directed with edges from right to left.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * Calling this method will cause the generated graph(s) to be directed, and only edges from right vertices to left
	 * vertices may be generated. In case parallel edges are not allowed, the maximum number of edges will be \(|U|
	 * \cdot |V|\).
	 *
	 * <p>
	 * By default, the generated graph(s) is undirected.
	 *
	 * @see    #undirected()
	 * @see    #directedAll()
	 * @see    #directedLeftToRight()
	 * @return this generator
	 */
	public GnmBipartiteGraphGenerator<V, E> directedRightToLeft() {
		direction = Direction.DirectedRightToLeft;
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
	public GnmBipartiteGraphGenerator<V, E> parallelEdges(boolean parallelEdges) {
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
	public GnmBipartiteGraphGenerator<V, E> seed(long seed) {
		rand.setSeed(seed);
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edges == null)
			throw new IllegalStateException("Edges not set");
		@SuppressWarnings("boxing")
		final int leftSize = vertices.map(p -> p[0].size(), p -> p.first().firstInt()).intValue();
		@SuppressWarnings("boxing")
		final int rightSize = vertices.map(p -> p[1].size(), p -> p.first().secondInt()).intValue();
		final int edgeNum = edges.firstInt();

		final int maxNumberOfEdges = leftSize * rightSize * (direction == Direction.DirectedAll ? 2 : 1);
		if (leftSize == 0 && edgeNum > 0)
			throw new IllegalArgumentException("left vertices set is empty, can't add edges");
		if (rightSize == 0 && edgeNum > 0)
			throw new IllegalArgumentException("right vertices set is empty, can't add edges");
		if (!parallelEdges && edgeNum > maxNumberOfEdges)
			throw new IllegalArgumentException("number of edges must be at most " + maxNumberOfEdges);

		factory.setDirected(direction != Direction.Undirected);
		if (parallelEdges)
			factory.allowParallelEdges();
		GraphBuilder<V, E> g = factory.newBuilder();
		IdBuilder<E> edgeBuilder = edges.second() != null ? edges.second() : g.edgeBuilder();
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
		if (parallelEdges || edgeNum <= maxNumberOfEdges / 2) {
			/* Start with an empty graph and add edges one by one */

			LongSet edges = parallelEdges ? null : new LongOpenHashSet(edgeNum);
			while (g.edges().size() < edgeNum) {
				V u, v;
				int uIdx, vIdx;
				if (direction != Direction.DirectedRightToLeft
						&& (direction != Direction.DirectedAll || rand.nextBoolean())) {
					/* left to right edge */
					uIdx = rand.nextInt(leftSize);
					vIdx = rand.nextInt(rightSize);
					u = leftVertices.get(uIdx);
					v = rightVertices.get(vIdx);
					vIdx += leftSize;
				} else {
					/* right to left edge */
					uIdx = rand.nextInt(rightSize);
					vIdx = rand.nextInt(leftSize);
					u = rightVertices.get(uIdx);
					v = leftVertices.get(vIdx);
					uIdx += leftSize;
				}
				if (parallelEdges || edges.add(IntPair.of(uIdx, vIdx)))
					g.addEdge(u, v, edgeBuilder.build(g.edges()));
			}

		} else {
			/* Start with a complete bipartite graph and remove edges one by one */

			Bitmap edges = new Bitmap(maxNumberOfEdges);
			edges.setAll();
			for (int edgesNum = maxNumberOfEdges; edgesNum > edgeNum;) {
				int i = rand.nextInt(maxNumberOfEdges);
				if (edges.get(i)) {
					edges.clear(i);
					edgesNum--;
				}
			}

			int i = 0;
			if (direction != Direction.DirectedRightToLeft)
				for (V u : leftVertices)
					for (V v : rightVertices)
						if (edges.get(i++))
							g.addEdge(u, v, edgeBuilder.build(g.edges()));
			if (direction == Direction.DirectedRightToLeft || direction == Direction.DirectedAll)
				for (V u : rightVertices)
					for (V v : leftVertices)
						if (edges.get(i++))
							g.addEdge(u, v, edgeBuilder.build(g.edges()));
		}

		return g;
	}

}
