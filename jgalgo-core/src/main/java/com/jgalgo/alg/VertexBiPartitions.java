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

import java.util.Objects;
import java.util.stream.Collectors;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.Range;
import it.unimi.dsi.fastutil.ints.IntSet;

class VertexBiPartitions {

	static abstract class Impl implements IVertexBiPartition {

		private final IndexGraph g;
		private IntSet leftVertices, rightVertices;
		private IntSet leftEdges, rightEdges;
		private IntSet crossEdges01, crossEdges10;

		Impl(IndexGraph g) {
			this.g = Objects.requireNonNull(g);
		}

		abstract boolean isLeft0(int vertex);

		@Override
		public boolean isLeft(int vertex) {
			if (!(0 <= vertex && vertex < g.vertices().size()))
				throw new IndexOutOfBoundsException(vertex);
			return isLeft0(vertex);
		}

		@Override
		public IntSet blockVertices(int block) {
			if (leftVertices == null) {
				final int n = g.vertices().size();
				int[] sortedVertices = new int[n];
				int i = 0;
				for (int v = 0; v < n; v++)
					if (isLeft0(v))
						sortedVertices[i++] = v;
				final int leftCount = i;
				for (int v = 0; v < n; v++)
					if (!isLeft0(v))
						sortedVertices[i++] = v;
				leftVertices = new ImmutableIntArraySet(sortedVertices, 0, leftCount) {
					@Override
					public boolean contains(int v) {
						return 0 <= v && v < leftCount;
					}
				};
				rightVertices = new ImmutableIntArraySet(sortedVertices, leftCount, n) {
					@Override
					public boolean contains(int v) {
						return leftCount <= v && v < n;
					}
				};
			}
			if (block == 0)
				return leftVertices;
			if (block == 1)
				return rightVertices;
			throw new IndexOutOfBoundsException(block);
		}

		@Override
		public IntSet blockEdges(int block) {
			if (leftEdges == null) {
				final int m = g.edges().size();
				int b1Count = 0, b2Count = 0;
				for (int e = 0; e < m; e++) {
					boolean b1 = isLeft0(g.edgeSource(e)), b2 = isLeft0(g.edgeTarget(e));
					if (b1 == b2) {
						if (b1) {
							b1Count++;
						} else {
							b2Count++;
						}
					}
				}
				int[] sortedEdges = new int[b1Count + b2Count];
				int b1Idx = 0, b2Idx = b1Count;
				for (int e = 0; e < m; e++) {
					boolean b1 = isLeft0(g.edgeSource(e)), b2 = isLeft0(g.edgeTarget(e));
					if (b1 == b2) {
						if (b1) {
							sortedEdges[b1Idx++] = e;
						} else {
							sortedEdges[b2Idx++] = e;
						}
					}
				}
				final int b1Idx0 = b1Idx, b2Idx0 = b2Idx;
				leftEdges = new ImmutableIntArraySet(sortedEdges, 0, b1Idx) {
					@Override
					public boolean contains(int e) {
						return 0 <= e && e < b1Idx0;
					}
				};
				rightEdges = new ImmutableIntArraySet(sortedEdges, b1Idx, b2Idx) {
					@Override
					public boolean contains(int e) {
						return b1Idx0 <= e && e < b2Idx0;
					}
				};
			}
			if (block == 0)
				return leftEdges;
			if (block == 1)
				return rightEdges;
			throw new IndexOutOfBoundsException(block);
		}

		@Override
		public IntSet crossEdges(int block1, int block2) {
			if (block1 == block2)
				return blockEdges(block1);
			if (crossEdges01 == null) {
				final int m = g.edges().size();
				if (g.isDirected()) {
					int crossEdges01Count = 0, crossEdges10Count = 0;
					for (int e = 0; e < m; e++) {
						boolean b1 = isLeft0(g.edgeSource(e)), b2 = isLeft0(g.edgeTarget(e));
						if (b1 != b2) {
							if (b1) {
								crossEdges01Count++;
							} else {
								crossEdges10Count++;
							}
						}
					}
					int[] crossEdges = new int[crossEdges01Count + crossEdges10Count];
					int crossEdges01Idx = 0, crossEdges10Idx = crossEdges01Count;
					for (int e = 0; e < m; e++) {
						boolean b1 = isLeft0(g.edgeSource(e)), b2 = isLeft0(g.edgeTarget(e));
						if (b1 != b2) {
							if (b1) {
								crossEdges[crossEdges01Idx++] = e;
							} else {
								crossEdges[crossEdges10Idx++] = e;
							}
						}
					}
					final int crossEdges01Idx0 = crossEdges01Idx, crossEdges10Idx0 = crossEdges10Idx;
					crossEdges01 = new ImmutableIntArraySet(crossEdges, 0, crossEdges01Idx) {
						@Override
						public boolean contains(int e) {
							return 0 <= e && e < crossEdges01Idx0;
						}
					};
					crossEdges10 = new ImmutableIntArraySet(crossEdges, crossEdges01Idx, crossEdges10Idx) {
						@Override
						public boolean contains(int e) {
							return crossEdges01Idx0 <= e && e < crossEdges10Idx0;
						}
					};

				} else {
					int crossEdgesCount = 0;
					for (int e = 0; e < m; e++)
						if (isLeft0(g.edgeSource(e)) != isLeft0(g.edgeTarget(e)))
							crossEdgesCount++;
					int[] crossEdges = new int[crossEdgesCount];
					for (int i = 0, e = 0; e < m; e++)
						if (isLeft0(g.edgeSource(e)) != isLeft0(g.edgeTarget(e)))
							crossEdges[i++] = e;
					crossEdges01 = crossEdges10 = new ImmutableIntArraySet(crossEdges) {
						@Override
						public boolean contains(int e) {
							return 0 <= e && e < m && isLeft0(g.edgeSource(e)) != isLeft0(g.edgeTarget(e));
						}
					};;
				}
			}
			if (block1 == 0 && block2 == 1)
				return crossEdges01;
			if (block1 == 1 && block2 == 0)
				return crossEdges10;
			throw new IndexOutOfBoundsException(block1 + "," + block2);
		}

		@Override
		public IntGraph graph() {
			return g;
		}

		@Override
		public IntGraph blocksGraph(boolean parallelEdges, boolean selfEdges) {
			return VertexPartitions.blocksGraph(g, this, parallelEdges, selfEdges);
		}

		@Override
		public String toString() {
			return Range.of(numberOfBlocks()).intStream().mapToObj(this::blockVertices).map(Object::toString)
					.collect(Collectors.joining(", ", "[", "]"));
		}
	}

	static class FromBitmap extends Impl {

		private final Bitmap bitSet;

		FromBitmap(IndexGraph g, Bitmap bitSet) {
			super(g);
			this.bitSet = Objects.requireNonNull(bitSet);
		}

		@Override
		boolean isLeft0(int vertex) {
			return bitSet.get(vertex);
		}
	}

	static class FromWeights extends Impl {

		private final IWeightsBool weights;

		FromWeights(IndexGraph g, IWeightsBool weights) {
			super(g);
			this.weights = Objects.requireNonNull(weights);
		}

		@Override
		boolean isLeft0(int vertex) {
			return weights.get(vertex);
		}
	}

	static class IntBiPartitionFromIndexBiPartition extends VertexPartitions.IntPartitionFromIndexPartition
			implements IVertexBiPartition {

		IntBiPartitionFromIndexBiPartition(IntGraph g, IVertexBiPartition indexPartition) {
			super(g, indexPartition);
		}

		IVertexBiPartition indexPartition() {
			return (IVertexBiPartition) indexPartition;
		}

		@Override
		public boolean isLeft(int vertex) {
			return indexPartition().isLeft(viMap.idToIndex(vertex));
		}
	}

	static class ObjBiPartitionFromIndexBiPartition<V, E> extends VertexPartitions.ObjPartitionFromIndexPartition<V, E>
			implements VertexBiPartition<V, E> {

		ObjBiPartitionFromIndexBiPartition(Graph<V, E> g, IVertexBiPartition indexPartition) {
			super(g, indexPartition);
		}

		IVertexBiPartition indexPartition() {
			return (IVertexBiPartition) indexPartition;
		}

		@Override
		public boolean isLeft(V vertex) {
			return indexPartition().isLeft(viMap.idToIndex(vertex));
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> VertexBiPartition<V, E> partitionFromIndexPartition(Graph<V, E> g,
			IVertexBiPartition indexPartition) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (VertexBiPartition<V, E>) new IntBiPartitionFromIndexBiPartition((IntGraph) g, indexPartition);
		} else {
			return new ObjBiPartitionFromIndexBiPartition<>(g, indexPartition);
		}
	}

}
