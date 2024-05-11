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

import static com.jgalgo.internal.util.Range.range;
import java.util.Map;
import java.util.function.IntPredicate;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A partition of the vertices of an int graph into two blocks.
 *
 * <p>
 * This interface is a specification of {@link VertexBiPartition} for {@link IntGraph}. See {@link VertexBiPartition}
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

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #isLeft(int)} instead to avoid un/boxing.
	 */
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

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #isRight(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default boolean isRight(Integer vertex) {
		return !isLeft(vertex.intValue());
	}

	@Override
	default int vertexBlock(int vertex) {
		return isLeft(vertex) ? 0 : 1;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #vertexBlock(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default int vertexBlock(Integer vertex) {
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
	static IVertexBiPartition fromMap(IntGraph g, Map<Integer, Boolean> map) {
		return fromMapping(g, map::get);
	}

	/**
	 * Create a new vertex bi-partition from a vertex-side mapping function.
	 *
	 * <p>
	 * Note that this function does not validate the input. For that, see {@link #isPartition(IntGraph, IntPredicate)}.
	 *
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to either {@code true} or {@code false}, where
	 *                     {@code true} means the vertex is contained in the left block and {@code false} means the
	 *                     vertex is contained in the right block
	 * @return         a new vertex bi-partition
	 */
	static IVertexBiPartition fromMapping(IntGraph g, IntPredicate mapping) {
		final int n = g.vertices().size();
		if (g instanceof IndexGraph) {
			return new VertexBiPartitions.IndexImpl((IndexGraph) g, Bitmap.fromPredicate(n, mapping)::get);
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			Bitmap vertexToBlock = Bitmap.fromPredicate(n, vIdx -> mapping.test(viMap.indexToIdInt(vIdx)));
			IVertexBiPartition indexPartition = new VertexBiPartitions.IndexImpl(g.indexGraph(), vertexToBlock::get);
			return new VertexBiPartitions.IntBiPartitionFromIndexBiPartition(g, indexPartition);
		}
	}

	/**
	 * Create a new vertex bi-partition from a vertex-side weights container.
	 *
	 * @param  g       the graph
	 * @param  weights a weights container that maps from a vertex to either {@code true} or {@code false}, where
	 *                     {@code true} means the vertex is contained in the left block and {@code false} means the
	 *                     vertex is contained in the right block
	 * @return         a new vertex bi-partition
	 */
	static IVertexBiPartition fromWeights(IntGraph g, IWeightsBool weights) {
		if (g instanceof IndexGraph) {
			return new VertexBiPartitions.IndexImpl((IndexGraph) g, weights::get);
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IWeightsBool indexWeights = IndexIdMaps.idToIndexWeights(weights, viMap);
			IVertexBiPartition indexPartition = new VertexBiPartitions.IndexImpl(g.indexGraph(), indexWeights::get);
			return new VertexBiPartitions.IntBiPartitionFromIndexBiPartition(g, indexPartition);
		}
	}

	/**
	 * Create a new vertex bi-partition from a bitmap.
	 *
	 * <p>
	 * This function can be used only for index graphs.
	 *
	 * @param  g      the index graph
	 * @param  bitmap a bitmap where {@code true} means the vertex is contained in the left block and {@code false}
	 *                    means the vertex is contained in the right block. The bitmap is not copied, and it is assumed
	 *                    the caller of this function will not modify the bitmap after calling this function
	 * @return        a new vertex bi-partition
	 */
	static IVertexBiPartition fromBitmap(IndexGraph g, Bitmap bitmap) {
		return new VertexBiPartitions.IndexImpl(g, bitmap::get);
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
			vertexToBlock = Bitmap.fromPredicate(n, mapping);
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			vertexToBlock = Bitmap.fromPredicate(n, vIdx -> mapping.test(viMap.indexToIdInt(vIdx)));
		}
		if (vertexToBlock.get(0)) {
			for (int v : range(1, n))
				if (!vertexToBlock.get(v))
					return true;
		} else {
			for (int v : range(1, n))
				if (vertexToBlock.get(v))
					return true;
		}
		return false;
	}

}
