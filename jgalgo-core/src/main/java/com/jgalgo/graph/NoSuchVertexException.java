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
 * Exception thrown when a vertex is not found in a graph.
 *
 * @see    NoSuchEdgeException
 * @author Barak Ugav
 */
public class NoSuchVertexException extends IllegalArgumentException {

	private static final long serialVersionUID = -743969638589879515L;

	private NoSuchVertexException(String message) {
		super(message);
	}

	/**
	 * Create a new exception of a missing vertex by its index.
	 *
	 * <p>
	 * The index of a vertex is the index of the vertex in the {@code IndexGraph} of the graph, accessed by
	 * {@link Graph#indexGraph()}. In case the graph is an index graph, and identifier and the index of a vertex are the
	 * same.
	 *
	 * @param  vertexIdx the index of the missing vertex
	 * @return           the exception
	 */
	public static NoSuchVertexException ofIndex(int vertexIdx) {
		return new NoSuchVertexException("No vertex with index " + vertexIdx);
	}

	/**
	 * Create a new exception of a missing vertex by its identifier.
	 *
	 * @param  vertexId the identifier of the missing vertex
	 * @return          the exception
	 */
	public static NoSuchVertexException ofVertex(Object vertexId) {
		return new NoSuchVertexException("No vertex '" + vertexId + "'");
	}

	/**
	 * Create a new exception of a missing vertex by its {@code int} identifier.
	 *
	 * <p>
	 * This is a specification of {@link #ofVertex(Object)} for {@link IntGraph}.
	 *
	 * @param  vertexId the identifier of the missing vertex
	 * @return          the exception
	 */
	public static NoSuchVertexException ofVertex(int vertexId) {
		return new NoSuchVertexException("No vertex " + vertexId);
	}

}
