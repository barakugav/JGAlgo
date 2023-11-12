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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * A generator of empty graphs.
 *
 * <p>
 * An empty graph is a graph with no edges, and it may contains some vertices.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class EmptyGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private Collection<V> vertices;
	private boolean directed = false;

	private EmptyGraphGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new empty graph generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new empty graph generator
	 */
	public static <V, E> EmptyGraphGenerator<V, E> newInstance() {
		return new EmptyGraphGenerator<>(false);
	}

	/**
	 * Creates a new empty graph generator for {@link IntGraph}.
	 *
	 * @return a new empty graph generator for {@link IntGraph}
	 */
	public static EmptyGraphGenerator<Integer, Integer> newIntInstance() {
		return new EmptyGraphGenerator<>(true);
	}

	/**
	 * Generate a new empty undirected graph.
	 *
	 * @param  <V>      the vertices type
	 * @param  <E>      the edges type
	 * @param  vertices the vertices of the generated graph
	 * @return          a new empty undirected graph
	 */
	public static <V, E> Graph<V, E> emptyGraph(Collection<V> vertices) {
		EmptyGraphGenerator<V, E> gen = EmptyGraphGenerator.newInstance();
		gen.setVertices(vertices);
		return gen.generate();
	}

	/**
	 * Set the vertices of the generated graph(s).
	 *
	 * <p>
	 * If this function is not called before generating a graph, no vertices will be added to the graph.
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

	@SuppressWarnings("unchecked")
	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (intGraph) {
			IntGraphBuilder g = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			if (vertices != null) {
				g.expectedVerticesNum(vertices.size());
				for (int v : IntAdapters.asIntCollection((Collection<Integer>) vertices))
					g.addVertex(v);
			}
			return (GraphBuilder<V, E>) g;
		} else {
			GraphBuilder<V, E> g = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
			if (vertices != null) {
				g.expectedVerticesNum(vertices.size());
				for (V v : vertices)
					g.addVertex(v);
			}
			return g;
		}
	}

}
