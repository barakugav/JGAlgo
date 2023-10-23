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

import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

class VertexPartitions {

	static class ImplIndex implements VertexPartition {
		private final IndexGraph g;
		private final int blockNum;
		private final int[] vertexToBlock;
		private IntList[] blockVertices;
		private IntList[] blockEdges;

		ImplIndex(IndexGraph g, int blockNum, int[] vertexToBlock) {
			this.g = Objects.requireNonNull(g);
			this.blockNum = blockNum;
			this.vertexToBlock = Objects.requireNonNull(vertexToBlock);
		}

		@Override
		public int vertexBlock(int vertex) {
			return vertexToBlock[vertex];
		}

		@Override
		public int numberOfBlocks() {
			return blockNum;
		}

		@Override
		public String toString() {
			return Arrays.toString(vertexToBlock);
		}

		@Override
		public IntCollection blockVertices(int block) {
			if (blockVertices == null) {
				blockVertices = new IntList[blockNum];
				for (int b = 0; b < blockNum; b++)
					blockVertices[b] = new IntArrayList();
				final int n = vertexToBlock.length;
				for (int u = 0; u < n; u++)
					blockVertices[vertexToBlock[u]].add(u);
				for (int b = 0; b < blockNum; b++)
					blockVertices[b] = IntLists.unmodifiable(blockVertices[b]);
			}
			return blockVertices[block];
		}

		@Override
		public IntCollection blockEdges(int block) {
			if (blockEdges == null) {
				blockEdges = new IntList[blockNum];
				for (int c = 0; c < blockNum; c++)
					blockEdges[c] = new IntArrayList();
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int b1 = vertexToBlock[g.edgeSource(e)];
					int b2 = vertexToBlock[g.edgeTarget(e)];
					if (b1 == b2)
						blockEdges[b1].add(e);
				}
				for (int b = 0; b < blockNum; b++)
					blockEdges[b] = IntLists.unmodifiable(blockEdges[b]);
			}
			return blockEdges[block];
		}

	}

	static class ResultFromIndexResult implements VertexPartition {

		private final VertexPartition res;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		ResultFromIndexResult(VertexPartition res, IndexIdMap viMap, IndexIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public int vertexBlock(int vertex) {
			return res.vertexBlock(viMap.idToIndex(vertex));
		}

		@Override
		public int numberOfBlocks() {
			return res.numberOfBlocks();
		}

		@Override
		public IntCollection blockVertices(int block) {
			return IndexIdMaps.indexToIdCollection(res.blockVertices(block), viMap);
		}

		@Override
		public IntCollection blockEdges(int block) {
			return IndexIdMaps.indexToIdCollection(res.blockEdges(block), eiMap);
		}

	}

}
