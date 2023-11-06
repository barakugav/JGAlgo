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
package com.jgalgo.alg;

import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A partition of the vertices of an int graph into two blocks.
 *
 * <p>
 * This interface is a specific version of {@link VertexBiPartition} for {@link IntGraph}. See {@link VertexBiPartition}
 * for the complete documentation.
 *
 * @author Barak Ugav
 */
public interface IVertexBiPartition extends IVertexPartition, VertexBiPartition<Integer, Integer> {

	/**
	 * Check whether a vertex is contained in the left block (block 0).
	 *
	 * @param  vertex a vertex in the graph
	 * @return        {@code true} if the vertex is contained in the left block, {@code false} otherwise
	 */
	boolean isLeft(int vertex);

	@Deprecated
	@Override
	default boolean isLeft(Integer vertex) {
		return isLeft(vertex.intValue());
	}

	/**
	 * Check whether a vertex is contained in the right block (block 1).
	 *
	 * @param  vertex a vertex in the graph
	 * @return        {@code true} if the vertex is contained in the right block, {@code false} otherwise
	 */
	default boolean isRight(int vertex) {
		return !isLeft(vertex);
	}

	@Deprecated
	@Override
	default int vertexBlock(Integer vertex) {
		return isLeft(vertex) ? 0 : 1;
	}

	@Override
	default int vertexBlock(int vertex) {
		return isLeft(vertex) ? 0 : 1;
	}

	@Override
	default IntSet leftVertices() {
		return (IntSet) VertexBiPartition.super.leftVertices();
	}

	@Override
	default IntSet rightVertices() {
		return (IntSet) VertexBiPartition.super.rightVertices();
	}

	@Override
	default IntSet leftEdges() {
		return (IntSet) VertexBiPartition.super.leftEdges();
	}

	@Override
	default IntSet rightEdges() {
		return (IntSet) VertexBiPartition.super.rightEdges();
	}

	@Override
	default IntSet crossEdges() {
		return (IntSet) VertexBiPartition.super.crossEdges();
	}

}
