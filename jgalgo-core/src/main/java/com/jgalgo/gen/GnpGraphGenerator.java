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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Generates a random graph using the \(G(n,p)\) model in which every edge exists with probability \(p\).
 *
 * <p>
 * The \(G(n,p)\) model generates a graph by connecting nodes randomly. Each edge is included in the graph with
 * probability \(p\) independent from every other edge. The model has only two parameters: the vertices set and the
 * probability \(p\). The generated graphs may be either directed or undirected, and may or may not allow self-edges.
 * Parallel edges are never created.
 *
 * <p>
 * By default, the value of \(p\) is \(0.1\) and the graph is undirected and does not generate self-edges.
 *
 * <p>
 * For deterministic behavior, set the seed of the generator using {@link #setSeed(long)}.
 *
 * <p>
 * Based on 'On random graphs' by Erdős and Rényi.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class GnpGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private Collection<V> vertices;
	private BiFunction<V, V, E> edgeBuilder;
	private boolean directed = false;
	private boolean selfEdges = false;
	private double p = 0.1;
	private Random rand = new Random();

	private GnpGraphGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new G(n,p) generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new G(n,p) generator
	 */
	public static <V, E> GnpGraphGenerator<V, E> newInstance() {
		return new GnpGraphGenerator<>(false);
	}

	/**
	 * Creates a new G(n,p) generator for {@link IntGraph}.
	 *
	 * @return a new G(n,p) generator for {@link IntGraph}
	 */
	public static GnpGraphGenerator<Integer, Integer> newIntInstance() {
		return new GnpGraphGenerator<>(true);
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
	 * Set the probability each edge will exists in the generated graph(s).
	 *
	 * <p>
	 * First the set of vertices is determined. Then, for each pair of vertices, an edge is created with probability
	 * \(p\). If the graph is directed, both directions of the edge are created or not created independently with
	 * probability \(p\). If the graph allows self-edges, each vertex is connected to itself with probability \(p\).
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
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge supplier not set");
		final int n = vertices.size();

		if (intGraph) {
			IntGraphBuilder g = IntGraphBuilder.newInstance(directed);
			final int[] vertices = IntAdapters.asIntCollection((Collection<Integer>) this.vertices).toIntArray();
			g.addVertices(IntList.of(vertices));

			if (p > 0) {
				IntBinaryOperator edgeBuilder =
						IntAdapters.asIntBiOperator((BiFunction<Integer, Integer, Integer>) this.edgeBuilder);
				for (int uIdx = 0; uIdx < n; uIdx++) {
					int u = vertices[uIdx];
					if (directed) {
						for (int vIdx = 0; vIdx < uIdx; vIdx++) {
							int v = vertices[vIdx];
							if (rand.nextDouble() <= p)
								g.addEdge(u, v, edgeBuilder.applyAsInt(u, v));
						}
					}
					if (selfEdges && rand.nextDouble() <= p)
						g.addEdge(u, u, edgeBuilder.applyAsInt(u, u));
					for (int vIdx = uIdx + 1; vIdx < n; vIdx++) {
						int v = vertices[vIdx];
						if (rand.nextDouble() <= p)
							g.addEdge(u, v, edgeBuilder.applyAsInt(u, v));
					}
				}
			}
			return (GraphBuilder<V, E>) g;

		} else {
			GraphBuilder<V, E> g = GraphBuilder.newInstance(directed);
			final V[] vertices = (V[]) this.vertices.toArray();
			g.addVertices(ObjectList.of(vertices));

			if (p > 0) {
				for (int uIdx = 0; uIdx < n; uIdx++) {
					V u = vertices[uIdx];
					if (directed) {
						for (int vIdx = 0; vIdx < uIdx; vIdx++) {
							V v = vertices[vIdx];
							if (rand.nextDouble() <= p)
								g.addEdge(u, v, edgeBuilder.apply(u, v));
						}
					}
					if (selfEdges && rand.nextDouble() <= p)
						g.addEdge(u, u, edgeBuilder.apply(u, u));
					for (int vIdx = uIdx + 1; vIdx < n; vIdx++) {
						V v = vertices[vIdx];
						if (rand.nextDouble() <= p)
							g.addEdge(u, v, edgeBuilder.apply(u, v));
					}
				}

			}
			return g;
		}
	}

}
