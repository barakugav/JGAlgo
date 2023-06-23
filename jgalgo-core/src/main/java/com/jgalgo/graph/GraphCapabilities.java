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

package com.jgalgo.graph;

/**
 * Object specifying the capabilities of a graph implementation.
 * <p>
 * Each implementation of the {@link Graph} interface may are may not support some operations. Each graph provide its
 * capabilities using {@link Graph#getCapabilities()}.
 *
 * @see    GraphFactory
 * @see    GraphArrayUndirected
 * @see    GraphLinkedUndirected
 * @see    GraphTableUndirected
 * @author Barak Ugav
 */
public interface GraphCapabilities {

	/**
	 * Checks whether parallel edges are supported.
	 * <p>
	 * Parallel edges are multiple edges with identical source and target.
	 *
	 * @return {@code true} if the graph support parallel edges, else {@code false}.
	 */
	boolean parallelEdges();

	/**
	 * Checks whether self edges are supported.
	 * <p>
	 * Self edges are edges with the same source and target, namely a vertex with an edge to itself.
	 *
	 * @return {@code true} if the graph support self edges, else {@code false}.
	 */
	boolean selfEdges();

	/**
	 * Checks whether the graph is directed.
	 *
	 * @return {@code true} if the graph is directed, else {@code false}.
	 */
	boolean directed();

}
