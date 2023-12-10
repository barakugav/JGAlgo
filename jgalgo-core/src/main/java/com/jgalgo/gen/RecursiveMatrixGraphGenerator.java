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
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

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
 * For deterministic behavior, set the seed of the generator using {@link #setSeed(long)}.
 *
 * <p>
 * Based on 'R-MAT: A Recursive Model for Graph Mining' by Chakrabarti et al.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class RecursiveMatrixGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private Collection<V> vertices;
	private int m = -1;
	private BiFunction<V, V, E> edgeBuilder;
	private boolean directed = false;
	private double a, b, c, d;
	private boolean abcdDefault = true;
	private Random rand = new Random();

	private RecursiveMatrixGraphGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new R-MAT generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new R-MAT generator
	 */
	public static <V, E> RecursiveMatrixGraphGenerator<V, E> newInstance() {
		return new RecursiveMatrixGraphGenerator<>(false);
	}

	/**
	 * Creates a new R-MAT generator for {@link IntGraph}.
	 *
	 * @return a new R-MAT generator for {@link IntGraph}
	 */
	public static RecursiveMatrixGraphGenerator<Integer, Integer> newIntInstance() {
		return new RecursiveMatrixGraphGenerator<>(true);
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
	 * @param m            the number of edges to generate
	 * @param edgeSupplier the edge supplier
	 */
	public void setEdges(int m, Supplier<E> edgeSupplier) {
		Objects.requireNonNull(edgeSupplier);
		setEdges(m, (u, v) -> edgeSupplier.get());
	}

	/**
	 * Set the edge builder function of the generated graph(s).
	 *
	 * <p>
	 * The function will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, Supplier)}, where the supplier is used to generate a set of vertices which is reused for
	 * any generated graph.
	 *
	 * @param m           the number of edges to generate
	 * @param edgeBuilder the edge builder function
	 */
	public void setEdges(int m, BiFunction<V, V, E> edgeBuilder) {
		if (m < 0)
			throw new IllegalArgumentException("m must be non-negative: " + m);
		this.m = m;
		this.edgeBuilder = Objects.requireNonNull(edgeBuilder);
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
	 * @param a the probability of the edge to be in the first quadrant
	 * @param b the probability of the edge to be in the second quadrant
	 * @param c the probability of the edge to be in the third quadrant
	 * @param d the probability of the edge to be in the fourth quadrant
	 */
	public void setEdgeProbabilities(double a, double b, double c, double d) {
		if ((a < 0 || b < 0 || c < 0 || d < 0) || Math.abs(a + b + c + d - 1) > 1e-6)
			throw new IllegalArgumentException("edge probabilities a,b,c,d must be in [0,1] and sum to 1");
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		abcdDefault = false;
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
		assert m >= 0;
		final int n = vertices.size();
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

		if (intGraph) {
			IntGraphBuilder g = IntGraphFactory.newInstance(directed).allowSelfEdges().newBuilder();
			final int[] vertices = IntAdapters.asIntCollection((Collection<Integer>) this.vertices).toIntArray();
			g.addVertices(IntList.of(vertices));

			g.expectedEdgesNum(m);
			IntBinaryOperator edgeBuilder =
					IntAdapters.asIntBiOperator((BiFunction<Integer, Integer, Integer>) this.edgeBuilder);
			if (directed) {
				for (int uIdx = 0; uIdx < n; uIdx++) {
					for (int vIdx = 0; vIdx < n; vIdx++) {
						int u = vertices[uIdx];
						if (edges.get(uIdx * N + vIdx)) {
							int v = vertices[vIdx];
							g.addEdge(u, v, edgeBuilder.applyAsInt(u, v));
						}
					}
				}
			} else {
				for (int uIdx = 0; uIdx < n; uIdx++) {
					int u = vertices[uIdx];
					for (int vIdx = uIdx; vIdx < n; vIdx++) {
						if (edges.get(uIdx * N + vIdx)) {
							int v = vertices[vIdx];
							g.addEdge(u, v, edgeBuilder.applyAsInt(u, v));
						}
					}
				}
			}
			return (GraphBuilder<V, E>) g;

		} else {
			GraphFactory<V, E> factory = GraphFactory.newInstance(directed);
			GraphBuilder<V, E> g = factory.allowSelfEdges().newBuilder();
			final V[] vertices = (V[]) this.vertices.toArray();
			g.addVertices(ObjectList.of(vertices));

			g.expectedEdgesNum(m);
			if (directed) {
				for (int uIdx = 0; uIdx < n; uIdx++) {
					for (int vIdx = 0; vIdx < n; vIdx++) {
						V u = vertices[uIdx];
						if (edges.get(uIdx * N + vIdx)) {
							V v = vertices[vIdx];
							g.addEdge(u, v, edgeBuilder.apply(u, v));
						}
					}
				}
			} else {
				for (int uIdx = 0; uIdx < n; uIdx++) {
					V u = vertices[uIdx];
					for (int vIdx = uIdx; vIdx < n; vIdx++) {
						if (edges.get(uIdx * N + vIdx)) {
							V v = vertices[vIdx];
							g.addEdge(u, v, edgeBuilder.apply(u, v));
						}
					}
				}
			}
			return g;
		}
	}

	private static int nextPowerOf2(int x) {
		return x == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(x - 1);
	}

}
