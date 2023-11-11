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
 * Exception thrown when an edge is not found in a graph.
 *
 * @see    NoSuchVertexException
 * @author Barak Ugav
 */
public class NoSuchEdgeException extends RuntimeException {

	private static final long serialVersionUID = 8898125347152262114L;

	private NoSuchEdgeException(String message) {
		super(message);
	}

	/**
	 * Create a new exception of a missing edge by its index.
	 *
	 * <p>
	 * The index of an edge is the index of the edge in the {@code IndexGraph} of the graph, accessed by
	 * {@link Graph#indexGraph()}. In case the graph is an index graph, and identifier and the index of an edge are the
	 * same.
	 *
	 * @param  edgeIdx the index of the missing edge
	 * @return         the exception
	 */
	public static NoSuchEdgeException ofIndex(int edgeIdx) {
		return new NoSuchEdgeException("No edge with index " + edgeIdx);
	}

	/**
	 * Create a new exception of a missing edge by its identifier.
	 *
	 * @param  edgeId the identifier of the missing edge
	 * @return        the exception
	 */
	public static NoSuchEdgeException ofEdge(Object edgeId) {
		return new NoSuchEdgeException("No edge " + edgeId);
	}

	/**
	 * Create a new exception of a missing edge by its {@code int} identifier.
	 *
	 * <p>
	 * This is a specification of {@link #ofEdge(Object)} for {@link IntGraph}.
	 *
	 * @param  edgeId the identifier of the missing edge
	 * @return        the exception
	 */
	public static NoSuchEdgeException ofEdge(int edgeId) {
		return new NoSuchEdgeException("No edge " + edgeId);
	}

}
