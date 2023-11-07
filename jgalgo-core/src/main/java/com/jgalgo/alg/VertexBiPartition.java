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

import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

/**
 * A partition of the vertices of a graph into two blocks.
 *
 * <p>
 * This interface is a specific case of {@link VertexPartition} where the number of blocks is 2. It can be used to
 * represent a <a href= "https://en.wikipedia.org/wiki/Cut_(graph_theory)">cut</a>, or a
 * <a href= "https://en.wikipedia.org/wiki/Bipartite_graph">bipartite</a> partition of a graph.
 *
 * <p>
 * The two blocks (or sets) are called left and right. The left block is the block with index 0, and the right block is
 * the block with index 1, and few methods with 'left/right' names are provided for convenience.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface VertexBiPartition<V, E> extends VertexPartition<V, E> {

	@Override
	default int numberOfBlocks() {
		return 2;
	}

	/**
	 * Check whether a vertex is contained in the left block (block 0).
	 *
	 * @param  vertex a vertex in the graph
	 * @return        {@code true} if the vertex is contained in the left block, {@code false} otherwise
	 */
	boolean isLeft(V vertex);

	/**
	 * Check whether a vertex is contained in the right block (block 1).
	 *
	 * @param  vertex a vertex in the graph
	 * @return        {@code true} if the vertex is contained in the right block, {@code false} otherwise
	 */
	default boolean isRight(V vertex) {
		return !isLeft(vertex);
	}

	@Override
	default int vertexBlock(V vertex) {
		return isLeft(vertex) ? 0 : 1;
	}

	/**
	 * Get the vertices in the 'left' block.
	 *
	 * <p>
	 * The left block is the block with index 0.
	 *
	 * @return the vertices in the left block
	 */
	default Set<V> leftVertices() {
		return blockVertices(0);
	}

	/**
	 * Get the vertices in the 'right' block.
	 *
	 * <p>
	 * The right block is the block with index 1.
	 *
	 * @return the vertices in the right block
	 */
	default Set<V> rightVertices() {
		return blockVertices(1);
	}

	/**
	 * Get the edges that are contained in the left block.
	 *
	 * <p>
	 * The left block is the block with index 0, and edges contained in it are edges with both endpoints in the left
	 * block.
	 *
	 * @return the edges that are contained in the left block
	 */
	default Set<E> leftEdges() {
		return blockEdges(0);
	}

	/**
	 * Get the edges that are contained in the right block.
	 *
	 * <p>
	 * The right block is the block with index 1, and edges contained in it are edges with both endpoints in the right
	 * block.
	 *
	 * @return the edges that are contained in the right block
	 */
	default Set<E> rightEdges() {
		return blockEdges(1);
	}

	/**
	 * Get the edges that cross between the left and right blocks.
	 *
	 * <p>
	 * An edge \((u,v)\) is said to cross between two blocks \(b_1\) and \(b_2\) if \(u\) is contained in \(b_1\) and
	 * \(v\) is contained in \(b_2\). Note that if the graph is directed, the cross edges of \((b_1,b_2)\) are different
	 * that \((b_2,b_1)\), since the direction of the edge matters. In that case, the edges returned by this functions
	 * are edges sourced in the left block and targeted in the right block. To get the edges sourced in the right block
	 * and targeted in the left block, use {@code crossEdges(1, 0)}.
	 *
	 * @return the edges that cross between the left and right blocks
	 */
	default Set<E> crossEdges() {
		return crossEdges(0, 1);
	}

	/**
	 * Create a new vertex bi-partition from a vertex-side map.
	 *
	 * <p>
	 * Note that this function does not validate the input. For that, see {@link #isPartition(Graph, Predicate)}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @param  map a map from vertex to either {@code true} or {@code false}
	 * @return     a new vertex bi-partition
	 */
	static <V, E> VertexBiPartition<V, E> fromMap(Graph<V, E> g, Object2BooleanMap<V> map) {
		return fromMapping(g, map::getBoolean);
	}

	/**
	 * Create a new vertex bi-partition from a vertex-side mapping function.
	 *
	 * <p>
	 * Note that this function does not validate the input. For that, see {@link #isPartition(Graph, Predicate)}.
	 *
	 * @param  <V>     the vertices type
	 * @param  <E>     the edges type
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to either {@code true} or {@code false}
	 * @return         a new vertex bi-partition
	 */
	@SuppressWarnings("unchecked")
	static <V, E> VertexBiPartition<V, E> fromMapping(Graph<V, E> g, Predicate<V> mapping) {
		if (g instanceof IntGraph) {
			IntPredicate mapping0 = IntAdapters.asIntPredicate((Predicate<Integer>) mapping);
			return (VertexBiPartition<V, E>) IVertexBiPartition.fromMapping((IntGraph) g, mapping0);
		} else {
			final int n = g.vertices().size();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			Bitmap vertexToBlock = new Bitmap(n, vIdx -> mapping.test(viMap.indexToId(vIdx)));
			IVertexBiPartition indexPartition = new VertexBiPartitions.FromBitmap(g.indexGraph(), vertexToBlock);
			return new VertexBiPartitions.ObjBiPartitionFromIndexBiPartition<>(g, indexPartition);
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
	 * @param  <V>     the vertices type
	 * @param  <E>     the edges type
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to either {@code true} or {@code false}
	 * @return         {@code true} if the mapping is a valid bi-partition of the vertices of the graph, {@code false}
	 *                 otherwise
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isPartition(Graph<V, E> g, Predicate<V> mapping) {
		if (g instanceof IntGraph) {
			IntPredicate mapping0 = IntAdapters.asIntPredicate((Predicate<Integer>) mapping);
			return IVertexBiPartition.isPartition((IntGraph) g, mapping0);
		}
		final int n = g.vertices().size();
		if (n < 2)
			return false;

		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		Bitmap vertexToBlock = new Bitmap(n, vIdx -> mapping.test(viMap.indexToId(vIdx)));
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
