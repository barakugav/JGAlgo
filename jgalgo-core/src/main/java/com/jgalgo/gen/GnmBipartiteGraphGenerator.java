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
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import com.jgalgo.alg.BipartiteGraphs;
import com.jgalgo.alg.VertexBiPartition;
import com.jgalgo.gen.BipartiteGenerators.Direction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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
 * ({@link #setParallelEdges(boolean)}) than at most a single edge is generated from the left to right vertex, and
 * another one from right to left. See {@link #setDirectedAll()}, {@link #setDirectedLeftToRight()} and
 * {@link #setDirectedRightToLeft()} for more details.
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
 * For deterministic behavior, set the seed of the generator using {@link #setSeed(long)}.
 *
 * <p>
 * This generator is the bipartite version of {@link GnmGraphGenerator}.
 *
 * @see    BipartiteGraphs
 * @author Barak Ugav
 */
public class GnmBipartiteGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private List<V> leftVertices;
	private List<V> rightVertices;
	private int m;
	private BiFunction<V, V, E> edgeBuilder;
	private Direction direction = Direction.Undirected;
	private boolean parallelEdges = true;
	private Random rand = new Random();

	private GnmBipartiteGraphGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new \(G(n_1,n_2,m)\) generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new \(G(n_1,n_2,m)\) generator
	 */
	public static <V, E> GnmBipartiteGraphGenerator<V, E> newInstance() {
		return new GnmBipartiteGraphGenerator<>(false);
	}

	/**
	 * Creates a new \(G(n_1,n_2,m)\) generator for {@link IntGraph}.
	 *
	 * @return a new \(G(n_1,n_2,m)\) generator for {@link IntGraph}
	 */
	public static GnmBipartiteGraphGenerator<Integer, Integer> newIntInstance() {
		return new GnmBipartiteGraphGenerator<>(true);
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
	 * If the generator is used to generate multiple graphs, the same vertex sets will be used for all of them.
	 *
	 * @param leftVertices  the set of left vertices of the generated graph(s)
	 * @param rightVertices the set of right vertices of the generated graph(s)
	 */
	@SuppressWarnings("unchecked")
	public void setVertices(Collection<V> leftVertices, Collection<V> rightVertices) {
		if (intGraph) {
			this.leftVertices =
					(List<V>) new IntArrayList(IntAdapters.asIntCollection((Collection<Integer>) leftVertices));
			this.rightVertices =
					(List<V>) new IntArrayList(IntAdapters.asIntCollection((Collection<Integer>) rightVertices));
		} else {
			this.leftVertices = new ObjectArrayList<>(leftVertices);
			this.rightVertices = new ObjectArrayList<>(rightVertices);
		}
	}

	/**
	 * Set the vertices set of the generated graph(s) from a supplier.
	 *
	 * <p>
	 * A bipartite graph is a graph whose vertices can be divided into two disjoint sets \(U\) and \(V\) such that every
	 * edge connects a vertex in \(U\) to one in \(V\). The two sets are usually called the left and right vertices.
	 * This method sets these two sets.
	 *
	 * <p>
	 * The supplier will be called exactly {@code leftVerticesNum+rightVerticesNum} times, and the same sets of vertices
	 * created will be used for multiple graphs if {@link #generate()} is called multiple times.
	 *
	 * @param leftVerticesNum  the number of vertices in the left set
	 * @param rightVerticesNum the number of vertices in the right set
	 * @param vertexSupplier   the supplier of vertices
	 */
	@SuppressWarnings("unchecked")
	public void setVertices(int leftVerticesNum, int rightVerticesNum, Supplier<V> vertexSupplier) {
		if (intGraph) {
			IntList leftVertices = new IntArrayList(leftVerticesNum);
			IntList rightVertices = new IntArrayList(rightVerticesNum);
			IntSupplier vSupplier = IntAdapters.asIntSupplier((Supplier<Integer>) vertexSupplier);
			for (int i = 0; i < leftVerticesNum; i++)
				leftVertices.add(vSupplier.getAsInt());
			for (int i = 0; i < rightVerticesNum; i++)
				rightVertices.add(vSupplier.getAsInt());
			this.leftVertices = (List<V>) leftVertices;
			this.rightVertices = (List<V>) rightVertices;
		} else {
			List<V> leftVertices = new ObjectArrayList<>(leftVerticesNum);
			List<V> rightVertices = new ObjectArrayList<>(rightVerticesNum);
			for (int i = 0; i < leftVerticesNum; i++)
				leftVertices.add(vertexSupplier.get());
			for (int i = 0; i < rightVerticesNum; i++)
				rightVertices.add(vertexSupplier.get());
			this.leftVertices = leftVertices;
			this.rightVertices = rightVertices;
		}
	}

	/**
	 * Set the number of edges and the edge supplier of the generated graph(s).
	 *
	 * <p>
	 * The number of edges must be non-negative, and if parallel edges are not allowed, it must be at most \(n_1 \cdot
	 * n_2\) for undirected graphs and directed graphs in which only one direction is allowed
	 * ({@linkplain #setDirectedLeftToRight() left to right}, or {@linkplain #setDirectedRightToLeft() right to left}),
	 * and at most \(2 \cdot n_1 \cdot n_2\) for directed graphs in which both directions are allowed
	 * ({@linkplain #setDirectedAll() all directions}).
	 *
	 * <p>
	 * The supplier will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, int, Supplier)}, where the supplier is used to generate a set of vertices which is
	 * reused for any generated graph.
	 *
	 * @param m            the number of edges
	 * @param edgeSupplier the edge supplier
	 */
	public void setEdges(int m, Supplier<E> edgeSupplier) {
		Objects.requireNonNull(edgeSupplier);
		setEdges(m, (u, v) -> edgeSupplier.get());
	}

	/**
	 * Set the number of edges and the edge builder function of the generated graph(s).
	 *
	 * <p>
	 * The number of edges must be non-negative, and if parallel edges are not allowed, it must be at most \(n_1 \cdot
	 * n_2\) for undirected graphs and directed graphs in which only one direction is allowed
	 * ({@linkplain #setDirectedLeftToRight() left to right}, or {@linkplain #setDirectedRightToLeft() right to left}),
	 * and at most \(2 \cdot n_1 \cdot n_2\) for directed graphs in which both directions are allowed
	 * ({@linkplain #setDirectedAll() all directions}).
	 *
	 * <p>
	 * The edge builder will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, int, Supplier)}, where the supplier is used to generate a set of vertices which is
	 * reused for any generated graph.
	 *
	 * @param m           the number of edges
	 * @param edgeBuilder the edge builder function
	 */
	public void setEdges(int m, BiFunction<V, V, E> edgeBuilder) {
		if (m < 0)
			throw new IllegalArgumentException("number of edges must be non-negative");
		this.m = m;
		this.edgeBuilder = Objects.requireNonNull(edgeBuilder);
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
	 * @see #setDirectedAll()
	 * @see #setDirectedLeftToRight()
	 * @see #setDirectedRightToLeft()
	 */
	public void setUndirected() {
		direction = Direction.Undirected;
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
	 * @see #setUndirected()
	 * @see #setDirectedLeftToRight()
	 * @see #setDirectedRightToLeft()
	 */
	public void setDirectedAll() {
		direction = Direction.DirectedAll;
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
	 * @see #setUndirected()
	 * @see #setDirectedAll()
	 * @see #setDirectedRightToLeft()
	 */
	public void setDirectedLeftToRight() {
		direction = Direction.DirectedLeftToRight;
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
	 * @see #setUndirected()
	 * @see #setDirectedAll()
	 * @see #setDirectedLeftToRight()
	 */
	public void setDirectedRightToLeft() {
		direction = Direction.DirectedRightToLeft;
	}

	/**
	 * Determine if the generated graph(s) will contain parallel-edges.
	 *
	 * <p>
	 * Parallel edges are a set of edges that connect the same two vertices. By default, the generated graph(s) will
	 * contain parallel-edges.
	 *
	 * @param parallelEdges {@code true} if the generated graph(s) will contain parallel-edges, {@code false} otherwise
	 */
	public void setParallelEdges(boolean parallelEdges) {
		this.parallelEdges = parallelEdges;
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

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (leftVertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edgeBuilder == null)
			throw new IllegalStateException("Number of edges and edge supplier were not set");

		final int maxNumberOfEdges =
				leftVertices.size() * rightVertices.size() * (direction == Direction.DirectedAll ? 2 : 1);
		if (leftVertices.isEmpty() && m > 0)
			throw new IllegalArgumentException("left vertices set is empty, can't add edges");
		if (rightVertices.isEmpty() && m > 0)
			throw new IllegalArgumentException("right vertices set is empty, can't add edges");
		if (!parallelEdges && m > maxNumberOfEdges)
			throw new IllegalArgumentException("number of edges must be at most " + maxNumberOfEdges);

		GraphBuilder<V, E> g;
		if (intGraph) {
			@SuppressWarnings("unchecked")
			GraphBuilder<V, E> g0 =
					(GraphBuilder<V, E>) (direction != Direction.Undirected ? IntGraphBuilder.newDirected()
							: IntGraphBuilder.newUndirected());
			g = g0;
		} else {
			g = direction != Direction.Undirected ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
		}
		g.expectedVerticesNum(leftVertices.size() + rightVertices.size());
		g.expectedEdgesNum(m);

		WeightsBool<V> partition = g.addVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey, boolean.class);
		for (V v : leftVertices) {
			g.addVertex(v);
			partition.set(v, true);
		}
		for (V v : rightVertices) {
			g.addVertex(v);
			partition.set(v, false);
		}

		if (parallelEdges || m <= maxNumberOfEdges / 2) {
			/* Start with an empty graph and add edges one by one */

			LongSet edges = parallelEdges ? null : new LongOpenHashSet(m);
			final int n1 = leftVertices.size(), n2 = rightVertices.size();
			while (g.edges().size() < m) {
				V u, v;
				int uIdx, vIdx;
				if (direction != Direction.DirectedRightToLeft
						&& (direction != Direction.DirectedAll || rand.nextBoolean())) {
					/* left to right edge */
					uIdx = rand.nextInt(n1);
					vIdx = rand.nextInt(n2);
					u = leftVertices.get(uIdx);
					v = rightVertices.get(vIdx);
					vIdx += n1;
				} else {
					/* right to left edge */
					uIdx = rand.nextInt(n2);
					vIdx = rand.nextInt(n1);
					u = rightVertices.get(uIdx);
					v = leftVertices.get(vIdx);
					uIdx += n1;
				}
				if (parallelEdges || edges.add(JGAlgoUtils.longPack(uIdx, vIdx)))
					g.addEdge(u, v, edgeBuilder.apply(u, v));
			}

		} else {
			/* Start with a complete bipartite graph and remove edges one by one */

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
			if (direction != Direction.DirectedRightToLeft)
				for (V u : leftVertices)
					for (V v : rightVertices)
						if (edges.get(i++))
							g.addEdge(u, v, edgeBuilder.apply(u, v));
			if (direction == Direction.DirectedRightToLeft || direction == Direction.DirectedAll)
				for (V u : rightVertices)
					for (V v : leftVertices)
						if (edges.get(i++))
							g.addEdge(u, v, edgeBuilder.apply(u, v));
		}

		return g;
	}

}
