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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;

class RandomWalkIters {

	private RandomWalkIters() {}

	static class UnweightedIndexIter implements RandomWalkIter.Int {

		private final IndexGraph g;
		private final int[] edges;
		private final int[] edgesOffset;
		private int currentVertex;
		private int lastEdge = -1;
		private final Random rand = new Random();

		UnweightedIndexIter(IndexGraph g, int source) {
			this.g = g;
			final int n = g.vertices().size();
			Assertions.checkVertex(source, n);

			final int edgesArrSize;
			if (g.isDirected()) {
				edgesArrSize = g.edges().size();
			} else {
				edgesArrSize = range(n).map(u -> g.outEdges(u).size()).sum();
			}

			edges = new int[edgesArrSize];
			edgesOffset = new int[n + 1];
			int offset = 0;
			for (int u : range(n)) {
				edgesOffset[u] = offset;
				for (int e : g.outEdges(u))
					edges[offset++] = e;
			}
			assert offset == edgesArrSize;
			edgesOffset[n] = offset;

			currentVertex = source;
		}

		@Override
		public boolean hasNext() {
			int uOutEdgesNum = edgesOffset[currentVertex + 1] - edgesOffset[currentVertex];
			return uOutEdgesNum > 0;
		}

		@Override
		public int nextInt() {
			int uOutEdgesNum = edgesOffset[currentVertex + 1] - edgesOffset[currentVertex];
			if (uOutEdgesNum <= 0)
				throw new NoSuchElementException();
			lastEdge = edges[edgesOffset[currentVertex] + rand.nextInt(uOutEdgesNum)];
			return currentVertex = g.edgeEndpoint(lastEdge, currentVertex);
		}

		@Override
		public int lastEdgeInt() {
			return lastEdge;
		}

		@Override
		public void setSeed(long seed) {
			rand.setSeed(seed);
		}
	}

	static class WeightedIndexIter implements RandomWalkIter.Int {

		private final IndexGraph g;
		private final int[] edges;
		private final double[] edgesWeights;
		private final int[] edgesOffset;
		private int currentVertex;
		private int lastEdge = -1;
		private final Random rand = new Random();

		WeightedIndexIter(IndexGraph g, int source, IWeightFunction weightFunc) {
			this.g = g;
			final int n = g.vertices().size();
			Assertions.checkVertex(source, n);
			Objects.requireNonNull(weightFunc);

			int outEdgesSize = 0;
			for (int u : range(n)) {
				for (int e : g.outEdges(u)) {
					double ew = weightFunc.weight(e);
					if (ew < 0)
						throw new IllegalArgumentException("only positive weights are supported: " + ew);
					if (ew > 0) /* discard edges with weight 0 */
						outEdgesSize++;
				}
			}

			edges = new int[outEdgesSize];
			edgesWeights = new double[outEdgesSize];
			edgesOffset = new int[n + 1];
			int offset = 0;
			for (int u : range(n)) {
				edgesOffset[u] = offset;
				double weightSum = 0;
				for (int e : g.outEdges(u)) {
					double ew = weightFunc.weight(e);
					if (ew == 0)
						continue;
					weightSum += ew;
					edges[offset] = e;
					edgesWeights[offset] = weightSum;
					offset++;
				}
			}
			assert offset == outEdgesSize;
			edgesOffset[n] = offset;

			currentVertex = source;
		}

		@Override
		public boolean hasNext() {
			int uOutEdgesNum = edgesOffset[currentVertex + 1] - edgesOffset[currentVertex];
			return uOutEdgesNum > 0;
		}

		@Override
		public int nextInt() {
			int uOutEdgesNum = edgesOffset[currentVertex + 1] - edgesOffset[currentVertex];
			if (uOutEdgesNum <= 0)
				throw new NoSuchElementException();

			double maxWeight = edgesWeights[edgesOffset[currentVertex + 1] - 1];
			double randWeight = rand.nextDouble() * maxWeight;

			/* binary search */
			int from = edgesOffset[currentVertex];
			int to = edgesOffset[currentVertex + 1];
			for (to--; from <= to;) {
				final int mid = (from + to) >>> 1;
				double midVal = edgesWeights[mid];
				if (midVal < randWeight) {
					from = mid + 1;
				} else {
					to = mid - 1;
				}
			}

			lastEdge = edges[from];
			return currentVertex = g.edgeEndpoint(lastEdge, currentVertex);
		}

		@Override
		public int lastEdgeInt() {
			return lastEdge;
		}

		@Override
		public void setSeed(long seed) {
			rand.setSeed(seed);
		}
	}

	private static class ObjIterFromIndexIter<V, E> implements RandomWalkIter<V, E> {

		private final RandomWalkIter.Int indexIter;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjIterFromIndexIter(RandomWalkIter.Int indexIter, Graph<V, E> graph) {
			this.indexIter = Objects.requireNonNull(indexIter);
			viMap = graph.indexGraphVerticesMap();
			eiMap = graph.indexGraphEdgesMap();
		}

		@Override
		public void setSeed(long seed) {
			indexIter.setSeed(seed);
		}

		@Override
		public boolean hasNext() {
			return indexIter.hasNext();
		}

		@Override
		public V next() {
			return viMap.indexToId(indexIter.nextInt());
		}

		@Override
		public E lastEdge() {
			return eiMap.indexToIdIfExist(indexIter.lastEdgeInt());
		}
	}

	private static class IntIterFromIndexIter implements RandomWalkIter.Int {

		private final RandomWalkIter.Int indexIter;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntIterFromIndexIter(RandomWalkIter.Int indexIter, IntGraph graph) {
			this.indexIter = Objects.requireNonNull(indexIter);
			viMap = graph.indexGraphVerticesMap();
			eiMap = graph.indexGraphEdgesMap();
		}

		@Override
		public void setSeed(long seed) {
			indexIter.setSeed(seed);
		}

		@Override
		public boolean hasNext() {
			return indexIter.hasNext();
		}

		@Override
		public int nextInt() {
			return viMap.indexToIdInt(indexIter.nextInt());
		}

		@Override
		public int lastEdgeInt() {
			return eiMap.indexToIdIfExistInt(indexIter.lastEdgeInt());
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> RandomWalkIter<V, E> fromIndexIter(RandomWalkIter.Int indexIter, Graph<V, E> graph) {
		assert !(graph instanceof IndexGraph);
		if (graph instanceof IntGraph) {
			return (RandomWalkIter<V, E>) new IntIterFromIndexIter(indexIter, (IntGraph) graph);
		} else {
			return new ObjIterFromIndexIter<>(indexIter, graph);
		}
	}

}
