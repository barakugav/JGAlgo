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
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

class VertexPartitions {

	static class ImplIndex implements VertexPartition {
		private final IndexGraph g;
		private final int blockNum;
		private final int[] vertexToBlock;
		private IntSet[] blockVertices;
		private IntSet[] blockEdges;

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
		public IntSet blockVertices(int block) {
			if (blockVertices == null) {
				final int n = vertexToBlock.length;

				int[] blockSize = new int[blockNum + 1];
				for (int v = 0; v < n; v++)
					blockSize[vertexToBlock[v]]++;
				for (int s = 0, b = 0; b < blockNum; b++) {
					int k = blockSize[b];
					blockSize[b] = s;
					s += k;
				}
				int[] sortedVertices = new int[n];
				int[] blockOffset = blockSize;
				for (int v = 0; v < n; v++)
					sortedVertices[blockOffset[vertexToBlock[v]]++] = v;
				for (int b = blockNum; b > 0; b--)
					blockOffset[b] = blockOffset[b - 1];
				blockOffset[0] = 0;

				blockVertices = new IntSet[blockNum];
				for (int b = 0; b < blockNum; b++) {
					final int b0 = b;
					blockVertices[b] = new ImmutableIntArraySet(sortedVertices, blockOffset[b], blockOffset[b + 1]) {
						@Override
						public boolean contains(int v) {
							return 0 <= v && v < n && vertexToBlock[v] == b0;
						}
					};
				}
			}
			return blockVertices[block];
		}

		@Override
		public IntSet blockEdges(int block) {
			if (blockEdges == null) {

				int[] blockSize = new int[blockNum + 1];
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int b1 = vertexToBlock[g.edgeSource(e)];
					int b2 = vertexToBlock[g.edgeTarget(e)];
					if (b1 == b2)
						blockSize[b1]++;
				}

				int innerEdgesCount = 0;
				for (int b = 0; b < blockNum; b++) {
					int k = blockSize[b];
					blockSize[b] = innerEdgesCount;
					innerEdgesCount += k;
				}
				int[] sortedEdges = new int[innerEdgesCount];
				int[] blockOffset = blockSize;
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int b1 = vertexToBlock[g.edgeSource(e)];
					int b2 = vertexToBlock[g.edgeTarget(e)];
					if (b1 == b2)
						sortedEdges[blockOffset[b1]++] = e;
				}
				for (int b = blockNum; b > 0; b--)
					blockOffset[b] = blockOffset[b - 1];
				blockOffset[0] = 0;

				final int m = g.edges().size();
				blockEdges = new IntSet[blockNum];
				for (int b = 0; b < blockNum; b++) {
					final int b0 = b;
					blockEdges[b] = new ImmutableIntArraySet(sortedEdges, blockOffset[b], blockOffset[b + 1]) {
						@Override
						public boolean contains(int e) {
							return 0 <= e && e < m && vertexToBlock[g.edgeSource(e)] == b0
									&& vertexToBlock[g.edgeSource(e)] == b0;
						}
					};
				}
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
		public IntSet blockVertices(int block) {
			return IndexIdMaps.indexToIdSet(res.blockVertices(block), viMap);
		}

		@Override
		public IntSet blockEdges(int block) {
			return IndexIdMaps.indexToIdSet(res.blockEdges(block), eiMap);
		}

	}

}
