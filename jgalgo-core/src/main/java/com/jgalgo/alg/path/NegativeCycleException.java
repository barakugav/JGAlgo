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
package com.jgalgo.alg.path;

import java.util.Objects;
import java.util.function.Supplier;
import com.jgalgo.graph.Graph;

/**
 * Exception thrown when a negative cycle is detected in a graph during a shortest path computation.
 *
 * <p>
 * Negative weights are supported by some implementations of shortest path algorithm, and the 'shortest path' is well
 * defined as long as there are no negative cycle in the graph as a path can loop in the cycle and achieve arbitrary
 * small 'length'. When negative cycles exists, algorithms will raise {@link NegativeCycleException} exception.
 *
 * @see    ShortestPathSingleSource
 * @see    ShortestPathAllPairs
 * @author Barak Ugav
 */
public class NegativeCycleException extends RuntimeException {

	private static final long serialVersionUID = 2383699177200213877L;

	/**
	 * The graph on which the negative cycle was detected.
	 */
	private final Graph<?, ?> graph;

	/**
	 * The negative cycle that was found in the graph.
	 */
	private final Path<?, ?> cycle;

	/**
	 * Create a new instance of the exception.
	 *
	 * @param <V>   the vertices type
	 * @param <E>   the edges type
	 * @param graph the graph on which the negative cycle was detected
	 * @param cycle the negative cycle that was found in the graph
	 */
	public <V, E> NegativeCycleException(Graph<V, E> graph, Path<V, E> cycle) {
		super("Negative cycle detected in graph during shortest path computation");
		this.graph = Objects.requireNonNull(graph);
		this.cycle = Objects.requireNonNull(cycle);
	}

	/**
	 * Get the negative cycle that was found in the graph.
	 *
	 * @return the negative cycle that was found in the graph.
	 */
	public Path<?, ?> cycle() {
		return cycle;
	}

	/**
	 * Get the negative cycle that was found in the graph in type generic safe way.
	 *
	 * <p>
	 * by passing the graph on which the exception was thrown to this method, we can safely conclude the generic types
	 * of the return path and avoid unchecked casts.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph on which the exception was thrown
	 * @return     the negative cycle that was found in the graph.
	 */
	@SuppressWarnings("unchecked")
	public <V, E> Path<V, E> cycle(Graph<V, E> g) {
		if (g != this.graph)
			throw new IllegalArgumentException("Graph does not match");
		return (Path<V, E>) cycle;
	}

	static <V, E> NegativeCycleException fromIndexException(NegativeCycleException e, Graph<V, E> g) {
		assert g.indexGraph() == e.graph;
		IPath indexPath = (IPath) e.cycle(g.indexGraph());
		Path<V, E> path = Path.pathFromIndexPath(g, indexPath);
		return new NegativeCycleException(g, path);
	}

	// static <V, E, R> void runAndConvertException(Graph<V, E> g, Runnable runnable) {
	// try {
	// runnable.run();
	// } catch (NegativeCycleException indexException) {
	// throw NegativeCycleException.fromIndexException(indexException, g);
	// }
	// }

	static <V, E, R> R runAndConvertException(Graph<V, E> g, Supplier<R> runnable) {
		try {
			return runnable.get();
		} catch (NegativeCycleException indexException) {
			throw NegativeCycleException.fromIndexException(indexException, g);
		}
	}

}
