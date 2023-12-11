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

import java.util.function.IntUnaryOperator;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A partition of the vertices of an int graph.
 *
 * <p>
 * This interface is a specific version of {@link VertexPartition} for {@link IntGraph}.
 *
 * <p>
 * A partition of a set is a division of the set into a number of disjoint subsets, such that their union is the
 * original set. The sets may also be called 'components' or 'blocks'. We use the term 'block' instead of 'set' to avoid
 * confusion with the get/set convention.
 *
 * <p>
 * The partition represent a mapping from the vertices of a graph to \(B\) blocks, each block is assigned a number in
 * range \([0,B)\). To check to which block a vertex is assigned use {@link #vertexBlock(int)}.
 *
 * @author Barak Ugav
 */
public interface IVertexPartition extends VertexPartition<Integer, Integer> {

	/**
	 * Get the block containing a vertex.
	 *
	 * @param  vertex a vertex in the graph
	 * @return        index of the block containing the vertex, in range \([0, blocksNum)\)
	 */
	int vertexBlock(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #vertexBlock(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default int vertexBlock(Integer vertex) {
		return vertexBlock(vertex.intValue());
	}

	@Override
	IntSet blockVertices(int block);

	@Override
	IntSet blockEdges(int block);

	@Override
	IntSet crossEdges(int block1, int block2);

	@Override
	IntGraph graph();

	@Override
	default IntGraph blockSubGraph(int block) {
		return (IntGraph) VertexPartition.super.blockSubGraph(block);
	}

	@Override
	default IntGraph blockSubGraph(int block, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return (IntGraph) VertexPartition.super.blockSubGraph(block, copyVerticesWeights, copyEdgesWeights);
	}

	@Override
	default IntGraph blocksGraph() {
		return (IntGraph) VertexPartition.super.blocksGraph();
	}

	@Override
	IntGraph blocksGraph(boolean parallelEdges, boolean selfEdges);

	/**
	 * Create a new vertex partition from a vertex-blockIndex map.
	 *
	 * <p>
	 * Note that this function does not validate the input, namely it does not check that the block numbers are all the
	 * range [0, maxBlockIndex], and that there are no 'empty' blocks.
	 *
	 * @param  g   the graph
	 * @param  map a map from vertex to block index
	 * @return     a new vertex partition
	 */
	static IVertexPartition fromMap(IntGraph g, Int2IntMap map) {
		return fromMapping(g, map::get);
	}

	/**
	 * Create a new vertex partition from a vertex-blockIndex mapping function.
	 *
	 * <p>
	 * Note that this function does not validate the input, namely it does not check that the block numbers are all the
	 * range [0, maxBlockIndex], and that there are no 'empty' blocks.
	 *
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to block index
	 * @return         a new vertex partition
	 */
	static IVertexPartition fromMapping(IntGraph g, IntUnaryOperator mapping) {
		final int n = g.vertices().size();
		int[] vertexToBlock = new int[n];
		if (g instanceof IndexGraph) {
			int maxBlock = -1;
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = mapping.applyAsInt(v);
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
			return new VertexPartitions.Impl((IndexGraph) g, maxBlock + 1, vertexToBlock);
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			int maxBlock = -1;
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = mapping.applyAsInt(viMap.indexToIdInt(v));
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
			IVertexPartition indexPartition = new VertexPartitions.Impl(g.indexGraph(), maxBlock + 1, vertexToBlock);
			return new VertexPartitions.IntPartitionFromIndexPartition(g, indexPartition);
		}
	}

	/**
	 * Check if a mapping is a valid partition of the vertices of a graph.
	 *
	 * <p>
	 * A valid vertex partition is a mapping from each vertex to an integer number in range [0, numberOfBlocks), in
	 * which there are not 'empty blocks', namely at least one vertex is mapped to each block.
	 *
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to block index
	 * @return         {@code true} if the mapping is a valid partition of the vertices of the graph, {@code false}
	 *                 otherwise
	 */
	static boolean isPartition(IntGraph g, IntUnaryOperator mapping) {
		final int n = g.vertices().size();
		int[] vertexToBlock = new int[n];
		int maxBlock = -1;
		if (g instanceof IndexGraph) {
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = mapping.applyAsInt(v);
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = mapping.applyAsInt(viMap.indexToIdInt(v));
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
		}
		final int blockNum = maxBlock + 1;
		if (maxBlock > n)
			return false;
		Bitmap seenBlocks = new Bitmap(blockNum);
		for (int b : vertexToBlock) {
			if (b < 0)
				return false;
			seenBlocks.set(b);
		}
		return seenBlocks.nextClearBit(0) == blockNum;
	}

}
