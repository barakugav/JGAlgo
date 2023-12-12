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
import com.jgalgo.alg.BipartiteGraphs;
import com.jgalgo.alg.VertexBiPartition;
import com.jgalgo.gen.BipartiteGenerators.Direction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Generates a random bipartite graph using the \(G(n_1,n_2,p)\) model in which every edge exists with probability
 * \(p\).
 *
 * The \(G(n_1,n_2,p)\) model generates a bipartite graph by connecting each pair of a left vertex and a right vertex
 * with probability \(p\). Each edge is created with probability independent of the others. The generator accept two
 * sets of vertices, the left and right vertices sets of the bipartite graph, and the probability \(p\).
 *
 * <p>
 * Both undirected and directed graphs can be generated. If the graph is directed, there are three options for the
 * considered (each generated independently with probability \(p\)) edges between each pair of left and right vertices:
 * two edges in both directions, one edge from the left vertex to the right vertex, or one edge from the right vertex to
 * the left vertex. See {@link #setDirectedAll()}, {@link #setDirectedLeftToRight()} and
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
 * By default, the value of \(p\) is \(0.1\) and the graph is undirected. Self and parallel edges are never created.
 *
 * <p>
 * For deterministic behavior, set the seed of the generator using {@link #setSeed(long)}.
 *
 * <p>
 * This generator is the bipartite version of {@link GnpGraphGenerator}.
 *
 * @see    BipartiteGraphs
 * @author Barak Ugav
 */
public class GnpBipartiteGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private Collection<V> leftVertices;
	private Collection<V> rightVertices;
	private BiFunction<V, V, E> edgeBuilder;
	private Direction direction = Direction.Undirected;
	private double p = 0.1;
	private Random rand = new Random();

	private GnpBipartiteGraphGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new \(G(n_1,n_2,p)\) generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new \(G(n_1,n_2,p)\) generator
	 */
	public static <V, E> GnpBipartiteGraphGenerator<V, E> newInstance() {
		return new GnpBipartiteGraphGenerator<>(false);
	}

	/**
	 * Creates a new \(G(n_1,n_2,p)\) generator for {@link IntGraph}.
	 *
	 * @return a new \(G(n_1,n_2,p)\) generator for {@link IntGraph}
	 */
	public static GnpBipartiteGraphGenerator<Integer, Integer> newIntInstance() {
		return new GnpBipartiteGraphGenerator<>(true);
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
					(Collection<V>) new IntArrayList(IntAdapters.asIntCollection((Collection<Integer>) leftVertices));
			this.rightVertices =
					(Collection<V>) new IntArrayList(IntAdapters.asIntCollection((Collection<Integer>) rightVertices));
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
			this.leftVertices = (Collection<V>) leftVertices;
			this.rightVertices = (Collection<V>) rightVertices;
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
	 * Set the edge supplier of the generated graph(s).
	 *
	 * <p>
	 * The supplier will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, int, Supplier)}, where the supplier is used to generate a set of vertices which is
	 * reused for any generated graph.
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
	 * {@link #setVertices(int, int, Supplier)}, where the supplier is used to generate a set of vertices which is
	 * reused for any generated graph.
	 *
	 * @param edgeBuilder the edge builder function
	 */
	public void setEdges(BiFunction<V, V, E> edgeBuilder) {
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
	 * Calling this method will cause the generated graph(s) to be directed, and for each pair of left and right
	 * vertices, two edges, opposite in direction of one another, will be considered and generated (independently) with
	 * probability \(p\). The maximum number of edges will be \(2 \cdot |U| \cdot |V|\).
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
	 * Calling this method will cause the generated graph(s) to be directed, and for each pair of left and right
	 * vertices a single edge from the left vertex to the right one will be considered and generated with probability
	 * \(p\). The maximum number of edges will be \(|U| \cdot |V|\).
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
	 * Calling this method will cause the generated graph(s) to be directed, and for each pair of left and right
	 * vertices a single edge from the right vertex to the left one will be considered and generated with probability
	 * \(p\). The maximum number of edges will be \(|U| \cdot |V|\).
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
	 * Set the probability each edge will exists in the generated graph(s).
	 *
	 * <p>
	 * For each pair of left and right vertices an edge is created with probability \(p\). If the graph is directed,
	 * either a single edge (from left to right, or right to left) or two edges (of opposite directions) will be
	 * considered and generated with probability \(p\). The direction of edges can be set using
	 * {@link #setUndirected()}, {@link #setDirectedAll()}, {@link #setDirectedLeftToRight()} or
	 * {@link #setDirectedRightToLeft()}.
	 *
	 * <p>
	 * By default, the probability is \(0.1\).
	 *
	 * @param p the probability each edge will exists in the generated graph(s)
	 */
	public void setEdgeProbability(double p) {
		if (!(0 <= p && p <= 1))
			throw new IllegalArgumentException("edge probability must be in [0,1]");
		this.p = p;
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
		if (leftVertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge supplier not set");

		if (intGraph) {
			IntGraphBuilder g = IntGraphBuilder.newInstance(direction != Direction.Undirected);
			final int[] leftVertices =
					IntAdapters.asIntCollection((Collection<Integer>) this.leftVertices).toIntArray();
			final int[] rightVertices =
					IntAdapters.asIntCollection((Collection<Integer>) this.rightVertices).toIntArray();
			g.ensureVertexCapacity(leftVertices.length + rightVertices.length);

			IWeightsBool partition = g.addVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey, boolean.class);
			for (int v : leftVertices) {
				g.addVertex(v);
				partition.set(v, true);
			}
			for (int v : rightVertices) {
				g.addVertex(v);
				partition.set(v, false);
			}

			if (p > 0) {
				IntBinaryOperator edgeBuilder =
						IntAdapters.asIntBiOperator((BiFunction<Integer, Integer, Integer>) this.edgeBuilder);
				if (direction != Direction.DirectedRightToLeft)
					for (int vLeft : leftVertices)
						for (int vRight : rightVertices)
							if (rand.nextDouble() <= p)
								g.addEdge(vLeft, vRight, edgeBuilder.applyAsInt(vLeft, vRight));
				if (direction == Direction.DirectedAll || direction == Direction.DirectedRightToLeft)
					for (int vRight : rightVertices)
						for (int vLeft : leftVertices)
							if (rand.nextDouble() <= p)
								g.addEdge(vRight, vLeft, edgeBuilder.applyAsInt(vRight, vLeft));
			}
			return (GraphBuilder<V, E>) g;

		} else {
			GraphBuilder<V, E> g = GraphBuilder.newInstance(direction != Direction.Undirected);
			final V[] leftVertices = (V[]) this.leftVertices.toArray();
			final V[] rightVertices = (V[]) this.rightVertices.toArray();
			g.ensureVertexCapacity(leftVertices.length + rightVertices.length);

			WeightsBool<V> partition = g.addVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey, boolean.class);
			for (V v : leftVertices) {
				g.addVertex(v);
				partition.set(v, true);
			}
			for (V v : rightVertices) {
				g.addVertex(v);
				partition.set(v, false);
			}

			if (direction != Direction.DirectedRightToLeft)
				for (V vLeft : leftVertices)
					for (V vRight : rightVertices)
						if (rand.nextDouble() <= p)
							g.addEdge(vLeft, vRight, edgeBuilder.apply(vLeft, vRight));
			if (direction == Direction.DirectedAll || direction == Direction.DirectedRightToLeft)
				for (V vRight : rightVertices)
					for (V vLeft : leftVertices)
						if (rand.nextDouble() <= p)
							g.addEdge(vRight, vLeft, edgeBuilder.apply(vRight, vLeft));
			return g;
		}
	}

}
