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

/**
 * Generates a complete graph.
 *
 * <p>
 * A complete graph is a graph in which each pair of graph vertices is connected by an edge. If the graph is directed,
 * then there are two edges between two pair of vertices, one in each direction. Self edges are optional, but are
 * disabled by default. Parallel edges are never created.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class CompleteGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private Collection<V> vertices;
	private BiFunction<V, V, E> edgeBuilder;
	private boolean directed = false;
	private boolean selfEdges = false;

	private CompleteGraphGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new complete graph generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new complete graph generator
	 */
	public static <V, E> CompleteGraphGenerator<V, E> newInstance() {
		return new CompleteGraphGenerator<>(false);
	}

	/**
	 * Creates a new complete graph generator for {@link IntGraph}.
	 *
	 * @return a new complete graph generator for {@link IntGraph}
	 */
	public static CompleteGraphGenerator<Integer, Integer> newIntInstance() {
		return new CompleteGraphGenerator<>(true);
	}

	/**
	 * Set the vertices of the generated graph(s).
	 *
	 * <p>
	 * If the generator is used to generate multiple graphs, the same vertex set will be used for all of them.
	 *
	 * @param vertices the vertices of the generated graph(s)
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

	@SuppressWarnings("unchecked")
	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge supplier not set");

		final int n = vertices.size();
		int m = n * (n - 1);
		if (!directed)
			m /= 2;
		if (selfEdges)
			m += n;

		if (intGraph) {
			IntGraphBuilder g = IntGraphBuilder.newInstance(directed);
			g.expectedVerticesNum(n);
			g.expectedEdgesNum(m);
			final int[] vertices = IntAdapters.asIntCollection((Collection<Integer>) this.vertices).toIntArray();
			for (int v : vertices)
				g.addVertex(v);
			IntBinaryOperator edgeBuilder =
					IntAdapters.asIntBiOperator((BiFunction<Integer, Integer, Integer>) this.edgeBuilder);
			for (int uIdx = 0; uIdx < n; uIdx++) {
				int u = vertices[uIdx];
				if (directed) {
					for (int vIdx = 0; vIdx < uIdx; vIdx++) {
						int v = vertices[vIdx];
						g.addEdge(u, v, edgeBuilder.applyAsInt(u, v));
					}
				}
				if (selfEdges)
					g.addEdge(u, u, edgeBuilder.applyAsInt(u, u));
				for (int vIdx = uIdx + 1; vIdx < n; vIdx++) {
					int v = vertices[vIdx];
					g.addEdge(u, v, edgeBuilder.applyAsInt(u, v));
				}
			}
			return (GraphBuilder<V, E>) g;

		} else {
			GraphBuilder<V, E> g = GraphBuilder.newInstance(directed);
			g.expectedVerticesNum(n);
			g.expectedEdgesNum(m);
			final V[] vertices = (V[]) this.vertices.toArray();
			for (V v : vertices)
				g.addVertex(v);
			for (int uIdx = 0; uIdx < n; uIdx++) {
				V u = vertices[uIdx];
				if (directed) {
					for (int vIdx = 0; vIdx < uIdx; vIdx++) {
						V v = vertices[vIdx];
						g.addEdge(u, v, edgeBuilder.apply(u, v));
					}
				}
				if (selfEdges)
					g.addEdge(u, u, edgeBuilder.apply(u, u));
				for (int vIdx = uIdx + 1; vIdx < n; vIdx++) {
					V v = vertices[vIdx];
					g.addEdge(u, v, edgeBuilder.apply(u, v));
				}
			}
			return g;
		}
	}

}
