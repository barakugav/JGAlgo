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
import java.util.BitSet;
import java.util.Objects;
import java.util.stream.Collectors;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.JGAlgoUtils.BiInt2LongFunc;
import com.jgalgo.internal.util.JGAlgoUtils.BiInt2ObjFunc;
import com.jgalgo.internal.util.Range;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

class VertexPartitions {

	static class Impl implements VertexPartition {
		final IndexGraph g;
		private final int blockNum;
		private final int[] vertexToBlock;
		private IntSet[] blockVertices;
		private IntSet[] blockEdges;
		private BiInt2ObjFunc<IntSet> crossEdges;

		Impl(IndexGraph g, int blockNum, int[] vertexToBlock) {
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
			return Range.of(numberOfBlocks()).intStream().mapToObj(this::blockVertices).map(Object::toString)
					.collect(Collectors.joining(", ", "[", "]"));
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

		@Override
		public IntSet crossEdges(int block1, int block2) {
			if (crossEdges == null) {
				final int m = g.edges().size();

				if (blockNum * blockNum < m * 4) {
					/* number of blocks is not too high, use 2D table */

					int[][] crossEdgesNum = new int[blockNum][blockNum];
					IntSet[][] crossEdgesMatrix = new IntSet[blockNum][blockNum];

					if (g.isDirected()) {
						for (int e = 0; e < m; e++) {
							int b1 = vertexToBlock[g.edgeSource(e)], b2 = vertexToBlock[g.edgeTarget(e)];
							crossEdgesNum[b1][b2]++;
						}
					} else {
						for (int e = 0; e < m; e++) {
							int b1 = vertexToBlock[g.edgeSource(e)], b2 = vertexToBlock[g.edgeTarget(e)];
							crossEdgesNum[b1][b2]++;
							if (b1 != b2)
								crossEdgesNum[b2][b1]++;
						}
					}
					int crossNumTotal = 0;
					for (int b1 = 0; b1 < blockNum; b1++) {
						for (int b2 = 0; b2 < blockNum; b2++) {
							int k = crossEdgesNum[b1][b2];
							crossEdgesNum[b1][b2] = crossNumTotal;
							crossNumTotal += k;
						}
					}

					int[] sortedEdges = new int[crossNumTotal];
					int[][] crossEdgesOffset = crossEdgesNum;
					if (g.isDirected()) {
						assert crossNumTotal == m;
						for (int e = 0; e < m; e++) {
							int b1 = vertexToBlock[g.edgeSource(e)], b2 = vertexToBlock[g.edgeTarget(e)];
							sortedEdges[crossEdgesOffset[b1][b2]++] = e;
						}

					} else {
						assert crossNumTotal >= m && crossNumTotal <= 2 * m;
						for (int e = 0; e < m; e++) {
							int b1 = vertexToBlock[g.edgeSource(e)], b2 = vertexToBlock[g.edgeTarget(e)];
							sortedEdges[crossEdgesOffset[b1][b2]++] = e;
							if (b1 != b2)
								sortedEdges[crossEdgesOffset[b2][b1]++] = e;
						}
					}

					for (int b1 = blockNum - 1; b1 >= 0; b1--) {
						for (int b2 = blockNum - 1; b2 >= 0; b2--) {
							if (b1 == 0 && b2 == 0)
								continue;
							int p1, p2;
							if (b2 > 0) {
								p1 = b1;
								p2 = b2 - 1;
							} else {
								p1 = b1 - 1;
								p2 = blockNum - 1;
							}
							crossEdgesNum[b1][b2] = crossEdgesNum[p1][p2];
						}
					}
					crossEdgesNum[0][0] = 0;

					for (int b1 = 0; b1 < blockNum; b1++) {
						for (int b2 = 0; b2 < blockNum; b2++) {
							int begin = crossEdgesNum[b1][b2], end;
							if (b2 < blockNum - 1) {
								end = crossEdgesNum[b1][b2 + 1];
							} else if (b1 < blockNum - 1) {
								end = crossEdgesNum[b1 + 1][0];
							} else {
								end = crossNumTotal;
							}
							crossEdgesMatrix[b1][b2] = ImmutableIntArraySet.withNaiveContains(sortedEdges, begin, end);
						}
					}
					crossEdges = (b1, b2) -> crossEdgesMatrix[b1][b2];

				} else {
					/* number of blocks is high, use hashtable */
					Long2ObjectOpenHashMap<int[]> map = new Long2ObjectOpenHashMap<>();
					BiInt2LongFunc buildKey = g.isDirected() ? JGAlgoUtils::longPack : (b1, b2) -> {
						if (b1 < b2) {
							int temp = b1;
							b1 = b2;
							b2 = temp;
						}
						return JGAlgoUtils.longPack(b1, b2);
					};
					for (int e = 0; e < m; e++) {
						int b1 = vertexToBlock[g.edgeSource(e)], b2 = vertexToBlock[g.edgeTarget(e)];
						long key = buildKey.apply(b1, b2);
						int[] arr = map.computeIfAbsent(key, k -> new int[2]);
						int arrSize = arr[0] + 1;
						if (arrSize == arr.length) {
							arr = Arrays.copyOf(arr, arr.length * 2);
							map.put(key, arr);
						}
						arr[arrSize] = e;
						arr[0] = arrSize;
					}
					for (var it = map.long2ObjectEntrySet().fastIterator(); it.hasNext();) {
						var entry = it.next();
						int[] a = entry.getValue();
						int size = a[0];
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Long2ObjectMap.Entry<IntSet> entry0 = (Long2ObjectMap.Entry) entry;
						entry0.setValue(ImmutableIntArraySet.withNaiveContains(a, 1, size + 1));
					}
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Long2ObjectMap<IntSet> crossEdgesMap = (Long2ObjectMap) map;
					crossEdgesMap.defaultReturnValue(IntSets.emptySet());
					crossEdges = (b1, b2) -> crossEdgesMap.get(buildKey.apply(b1, b2));
				}
			}
			return crossEdges.apply(block1, block2);
		}

		@Override
		public IntGraph graph() {
			return g;
		}

		@Override
		public IntGraph blocksGraph(boolean parallelEdges, boolean selfEdges) {
			return VertexPartitions.blocksGraph(g, this, parallelEdges, selfEdges);
		}
	}

	static IntGraph blocksGraph(IndexGraph g, VertexPartition partition, boolean parallelEdges, boolean selfEdges) {
		assert g == partition.graph();
		final int numberOfBlocks = partition.numberOfBlocks();
		final boolean directed = g.isDirected();
		IntGraphBuilder gb = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
		gb.expectedVerticesNum(numberOfBlocks);
		for (int b = 0; b < numberOfBlocks; b++)
			gb.addVertex(b);
		final int m = g.edges().size();
		if (parallelEdges) {
			for (int e = 0; e < m; e++) {
				int b1 = partition.vertexBlock(g.edgeSource(e));
				int b2 = partition.vertexBlock(g.edgeTarget(e));
				if (b1 != b2) {
					gb.addEdge(b1, b2, e);
				} else if (selfEdges) {
					// self edge
					gb.addEdge(b1, b1, e);
				}
			}
		} else {
			BitSet seen = new BitSet(numberOfBlocks);
			IntList seenList = new IntArrayList();
			for (int b1 = 0; b1 < numberOfBlocks; b1++) {
				for (int u : partition.blockVertices(b1)) {
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int b2 = partition.vertexBlock(eit.targetInt());
						if ((!selfEdges && b1 == b2) || (!directed && b1 > b2))
							continue;
						if (seen.get(b2))
							continue;
						seen.set(b2);
						seenList.add(b2);
						gb.addEdge(b1, b2, e);
					}
				}
				for (int s : seenList)
					seen.clear(s);
				seenList.clear();
			}
		}
		return gb.build();
	}

	static class PartitionFromIndexPartition implements VertexPartition {

		private final IntGraph g;
		final VertexPartition res;
		final IndexIntIdMap viMap;
		final IndexIntIdMap eiMap;

		PartitionFromIndexPartition(IntGraph g, VertexPartition res) {
			this.g = Objects.requireNonNull(g);
			assert g.indexGraph() == res.graph();
			this.res = Objects.requireNonNull(res);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
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

		@Override
		public IntSet crossEdges(int block1, int block2) {
			return IndexIdMaps.indexToIdSet(res.crossEdges(block1, block2), eiMap);
		}

		@Override
		public IntGraph graph() {
			return g;
		}

		@Override
		public IntGraph blocksGraph(boolean parallelEdges, boolean selfEdges) {
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			IntGraph ig = VertexPartitions.blocksGraph(g.indexGraph(), res, parallelEdges, selfEdges);
			IntGraphBuilder gb = g.isDirected() ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			gb.expectedVerticesNum(ig.vertices().size());
			gb.expectedEdgesNum(ig.edges().size());
			for (int b : ig.vertices())
				gb.addVertex(b);
			for (int e : ig.edges())
				gb.addEdge(ig.edgeSource(e), ig.edgeTarget(e), eiMap.indexToIdInt(e));
			return gb.build();
		}

		@Override
		public String toString() {
			return Range.of(numberOfBlocks()).intStream().mapToObj(this::blockVertices).map(Object::toString)
					.collect(Collectors.joining(", ", "[", "]"));
		}
	}

}
