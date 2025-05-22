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
package com.jgalgo.alg.traversal;

import java.util.NoSuchElementException;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;

class RandomWalkIters {

	private RandomWalkIters() {}

	static class IndexIter implements RandomWalkIter.Int {

		private final NeighborSampler.Int sampler;
		int currentVertex;
		int nextEdge;
		int lastEdge = -1;

		IndexIter(NeighborSampler.Int sampler, int source) {
			this.sampler = Objects.requireNonNull(sampler);
			currentVertex = source;
			sampleNextEdge();
		}

		private void sampleNextEdge() {
			nextEdge = sampler.sample(currentVertex);
		}

		@Override
		public boolean hasNext() {
			return nextEdge >= 0;
		}

		@Override
		public int nextInt() {
			if (nextEdge < 0)
				throw new NoSuchElementException();
			lastEdge = nextEdge;
			currentVertex = sampler.graph().edgeEndpoint(lastEdge, currentVertex);
			sampleNextEdge();
			return currentVertex;
		}

		@Override
		public int lastEdgeInt() {
			return lastEdge;
		}

		@Override
		public void setSeed(long seed) {
			sampler.setSeed(seed);
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
