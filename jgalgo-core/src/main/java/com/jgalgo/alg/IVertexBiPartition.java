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

import java.util.function.IntPredicate;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
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

	/**
	 * Create a new vertex bi-partition from a vertex-side map.
	 *
	 * <p>
	 * Note that this function does not validate the input. For that, see {@link #isPartition(IntGraph, IntPredicate)}.
	 *
	 * @param  g   the graph
	 * @param  map a map from vertex to either {@code true} or {@code false}
	 * @return     a new vertex bi-partition
	 */
	static IVertexBiPartition fromMap(IntGraph g, Int2BooleanMap map) {
		return fromMapping(g, map::get);
	}

	/**
	 * Create a new vertex bi-partition from a vertex-side mapping function.
	 *
	 * <p>
	 * Note that this function does not validate the input. For that, see {@link #isPartition(IntGraph, IntPredicate)}.
	 *
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to either {@code true} or {@code false}
	 * @return         a new vertex bi-partition
	 */
	static IVertexBiPartition fromMapping(IntGraph g, IntPredicate mapping) {
		final int n = g.vertices().size();
		if (g instanceof IndexGraph) {
			return new VertexBiPartitions.FromBitmap((IndexGraph) g, new Bitmap(n, mapping));
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			Bitmap vertexToBlock = new Bitmap(n, vIdx -> mapping.test(viMap.indexToIdInt(vIdx)));
			IVertexBiPartition indexPartition = new VertexBiPartitions.FromBitmap(g.indexGraph(), vertexToBlock);
			return new VertexBiPartitions.IntBiPartitionFromIndexBiPartition(g, indexPartition);
		}
	}

	/**
	 * Check if a mapping is a valid bi-partition of the vertices of a graph.
	 *
	 * <p>
	 * A valid vertex bi-partition is a mapping from each vertex to either {@code true} or {@code false}, in which there
	 * are not 'empty blocks', namely at least one vertex is mapped to {@code true} and another one is mapped to
	 * {@code true}.
	 *
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to either {@code true} or {@code false}
	 * @return         {@code true} if the mapping is a valid bi-partition of the vertices of the graph, {@code false}
	 *                 otherwise
	 */
	static boolean isPartition(IntGraph g, IntPredicate mapping) {
		final int n = g.vertices().size();
		if (n < 2)
			return false;
		Bitmap vertexToBlock;
		if (g instanceof IndexGraph) {
			vertexToBlock = new Bitmap(n, mapping);
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			vertexToBlock = new Bitmap(n, vIdx -> mapping.test(viMap.indexToIdInt(vIdx)));
		}
		if (vertexToBlock.get(0)) {
			for (int v = 1; v < n; v++)
				if (!vertexToBlock.get(v))
					return true;
		} else {
			for (int v = 1; v < n; v++)
				if (vertexToBlock.get(v))
					return true;
		}
		return false;
	}

}
