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
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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
 * For deterministic behavior, set the seed of the generator using {@link #setSeed(long)}.
 *
 * @author Barak Ugav
 */
public class GnmGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private List<V> vertices;
	private int m;
	private BiFunction<V, V, E> edgeBuilder;
	private boolean directed = false;
	private boolean selfEdges = false;
	private boolean parallelEdges = true;
	private Random rand = new Random();

	private GnmGraphGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new \(G(n,m)\) generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new \(G(n,m)\) generator
	 */
	public static <V, E> GnmGraphGenerator<V, E> newInstance() {
		return new GnmGraphGenerator<>(false);
	}

	/**
	 * Creates a new \(G(n,m)\) generator for {@link IntGraph}.
	 *
	 * @return a new \(G(n,m)\) generator for {@link IntGraph}
	 */
	public static GnmGraphGenerator<Integer, Integer> newIntInstance() {
		return new GnmGraphGenerator<>(true);
	}

	/**
	 * Set the vertices set of the generated graph(s).
	 *
	 * <p>
	 * If the generator is used to generate multiple graphs, the same vertex set will be used for all of them.
	 *
	 * @param vertices the vertices set
	 */
	@SuppressWarnings("unchecked")
	public void setVertices(Collection<V> vertices) {
		if (intGraph) {
			this.vertices = (List<V>) new IntArrayList(IntAdapters.asIntCollection((Collection<Integer>) vertices));
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
			this.vertices = (List<V>) vertices;
		} else {
			List<V> vertices = new ObjectArrayList<>(verticesNum);
			for (int i = 0; i < verticesNum; i++)
				vertices.add(vertexSupplier.get());
			this.vertices = vertices;
		}
	}

	/**
	 * Set the number of edges and the edge supplier of the generated graph(s).
	 *
	 * <p>
	 * The number of edges must be non-negative, and if parallel edges are not allowed, it must be at most \(n(n-1) /
	 * 2\) for undirected graphs and \(n(n-1)\) for directed graphs, with addition of \(n\) self-edges if self-edges are
	 * allowed.
	 *
	 * <p>
	 * The supplier will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, Supplier)}, where the supplier is used to generate a set of vertices which is reused for
	 * any generated graph.
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
	 * The number of edges must be non-negative, and if parallel edges are not allowed, it must be at most \(n(n-1) /
	 * 2\) for undirected graphs and \(n(n-1)\) for directed graphs, with addition of \(n\) self-edges if self-edges are
	 * allowed.
	 *
	 * <p>
	 * The edge builder will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, Supplier)}, where the supplier is used to generate a set of vertices which is reused for
	 * any generated graph.
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
	 * Determine if the generated graph(s) will contain self-edges.
	 *
	 * <p>
	 * Self edges are edges with the same source and target vertex. By default, the generated graph(s) will not contain
	 * self-edges.
	 *
	 * @param selfEdges {@code true} if the generated graph(s) will contain self-edges, {@code false} otherwise
	 */
	public void setSelfEdges(boolean selfEdges) {
		this.selfEdges = selfEdges;
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
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edgeBuilder == null)
			throw new IllegalStateException("Number of edges and edge supplier were not set");
		final int n = vertices.size();

		int maxNumberOfEdges = n * (n - 1);
		if (!directed)
			maxNumberOfEdges /= 2;
		if (selfEdges)
			maxNumberOfEdges += n;
		if (!parallelEdges && m > maxNumberOfEdges)
			throw new IllegalArgumentException("number of edges must be at most " + maxNumberOfEdges);

		GraphFactory<V, E> factory;
		if (intGraph) {
			@SuppressWarnings("unchecked")
			GraphFactory<V, E> factory0 =
					(GraphFactory<V, E>) (directed ? IntGraphFactory.newDirected() : IntGraphFactory.newUndirected());
			factory = factory0;
		} else {
			factory = directed ? GraphFactory.newDirected() : GraphFactory.newUndirected();
		}
		GraphBuilder<V, E> g = factory.allowSelfEdges(selfEdges).newBuilder();
		g.expectedVerticesNum(n);
		g.expectedEdgesNum(m);

		for (V v : vertices)
			g.addVertex(v);

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
					if (!edges.add(JGAlgoUtils.longPack(uIdx, vIdx)))
						continue;
				}
				V u = vertices.get(uIdx), v = vertices.get(vIdx);
				g.addEdge(u, v, edgeBuilder.apply(u, v));
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

			for (int i = 0, uIdx = 0; uIdx < n; uIdx++) {
				V u = vertices.get(uIdx);
				if (directed) {
					for (int vIdx = 0; vIdx < uIdx; vIdx++, i++) {
						if (edges.get(i)) {
							V v = vertices.get(vIdx);
							g.addEdge(u, v, edgeBuilder.apply(u, v));
						}
					}
				}
				if (selfEdges && edges.get(i++))
					g.addEdge(u, u, edgeBuilder.apply(u, u));
				for (int vIdx = uIdx + 1; vIdx < n; vIdx++, i++) {
					if (edges.get(i)) {
						V v = vertices.get(vIdx);
						g.addEdge(u, v, edgeBuilder.apply(u, v));
					}
				}
			}
		}

		return g;
	}

}
