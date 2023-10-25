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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
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

	/**
	 * Get all the edges that cross between two different blocks.
	 * <p>
	 * An edge \((u,v)\) is said to cross between two blocks \(b_1\) and \(b_2\) if \(u\) is contained in \(b_1\) and
	 * \(v\) is contained in \(b_2\). Note that if the graph is directed, the cross edges of \((b_1,b_2)\) are different
	 * that \((b_2,b_1)\), since the direction of the edge matters.
	 *
	 * @param  block1 the first block
	 * @param  block2 the second block
	 * @return        the set of edges that cross between the two blocks
	 */
	IntSet crossEdges(int block1, int block2);

	/**
	 * Create a new vertex partition from a vertex-blockIndex map.
	 * <p>
	 * Note that this function does not validate the input, namely it does not check that the blocks number are all the
	 * range [0, maxBlockIndex].
	 *
	 * @param  g   the graph
	 * @param  map a map from vertex to block index
	 * @return     a new vertex partition
	 */
	static VertexPartition fromMap(Graph g, Int2IntMap map) {
		final int n = g.vertices().size();
		int[] vertexToBlock = new int[n];
		if (g instanceof IndexGraph) {
			int maxBlock = -1;
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = map.get(v);
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
			return new VertexPartitions.ImplIndex((IndexGraph) g, maxBlock + 1, vertexToBlock);
		} else {
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			int maxBlock = -1;
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = map.get(viMap.indexToId(v));
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
			VertexPartition indexPartition =
					new VertexPartitions.ImplIndex(g.indexGraph(), maxBlock + 1, vertexToBlock);
			return new VertexPartitions.PartitionFromIndexPartition(indexPartition, viMap, eiMap);
		}
	}

}
