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

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A partition of the vertices of a graph.
 * <p>
 * A partition of a set is a division of the set into a number of disjoint subsets, such that their union is the
 * original set. The sets may also be called 'components' or 'blocks'. We use the term 'block' instead of 'set' to avoid
 * confusion with the get/set convention.
 * <p>
 * The partition represent a mapping from the vertices of a graph to \(B\) blocks, each block is assigned a number in
 * range \([0,B)\). To check to which block a vertex is assigned use {@link #vertexBlock(int)}.
 *
 * @author Barak Ugav
 */
public interface VertexPartition {

	/**
	 * Get the block containing a vertex.
	 *
	 * @param  vertex a vertex in the graph
	 * @return        index of the block containing the vertex, in range \([0, blocksNum)\)
	 */
	int vertexBlock(int vertex);

	/**
	 * Get the number of blocks in the partition.
	 *
	 * @return the number of blocks in the partition, non negative number
	 */
	int numberOfBlocks();

	/**
	 * Get all the vertices that are part of a block.
	 *
	 * @param  block                     index of a block
	 * @return                           the vertices that are part of the blocks
	 * @throws IndexOutOfBoundsException if {@code block} is not in range \([0, blocksNum)\)
	 */
	IntSet blockVertices(int block);

	/**
	 * Get all the edges that are contained in a block.
	 * <p>
	 * An edge \((u,v)\) is contained in a block if both \(u\) and \(v\) are contained in the block.
	 *
	 * @param  block                     index of a block
	 * @return                           the edges that are contained in the blocks
	 * @throws IndexOutOfBoundsException if {@code block} is not in range \([0, blocksNum)\)
	 */
	IntSet blockEdges(int block);

}
