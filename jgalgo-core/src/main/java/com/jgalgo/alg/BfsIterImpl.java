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

import java.util.BitSet;
import java.util.Objects;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueLongNoReduce;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

class BfsIterImpl {

	private static abstract class Abstract implements Bfs.IntIter {

		final IndexGraph g;
		final BitSet visited;
		final LongPriorityQueue queue;
		int inEdge;
		int layer;
		int firstVInLayer;

		private Abstract(IndexGraph g, IntIterator sources) {
			this.g = g;
			int n = g.vertices().size();
			visited = new BitSet(n);
			queue = new FIFOQueueLongNoReduce();
			inEdge = -1;
			layer = -1;

			firstVInLayer = -1;
			do {
				int source = sources.nextInt();
				visited.set(source);
				queue.enqueue(JGAlgoUtils.longPack(source, -1));
				if (firstVInLayer == -1)
					firstVInLayer = source;
			} while (sources.hasNext());
		}

		@Override
		public boolean hasNext() {
			return !queue.isEmpty();
		}

		@Override
		public int lastEdgeInt() {
			return inEdge;
		}

		@Override
		public int layer() {
			return layer;
		}
	}

	static class Forward extends BfsIterImpl.Abstract {

		/**
		 * Create a BFS iterator rooted at a single source vertex.
		 *
		 * @param g      a graph
		 * @param source a vertex in the graph from which the search will start from
		 */
		Forward(IndexGraph g, int source) {
			this(g, IntIterators.singleton(source));
		}

		/**
		 * Create a BFS iterator rooted at multiple sources vertices.
		 *
		 * @param  g                        a graph
		 * @param  sources                  multiple sources vertices in the graph from which the search will start from
		 * @throws IllegalArgumentException if the sources iterator is empty
		 */
		private Forward(IndexGraph g, IntIterator sources) {
			super(g, sources);
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			long l = queue.dequeueLong();
			final int u = JGAlgoUtils.long2low(l);
			inEdge = JGAlgoUtils.long2high(l);
			if (u == firstVInLayer) {
				layer++;
				firstVInLayer = -1;
			}

			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				if (visited.get(v))
					continue;
				visited.set(v);
				queue.enqueue(JGAlgoUtils.longPack(v, e));
				if (firstVInLayer == -1)
					firstVInLayer = v;
			}

			return u;
		}
	}

	static class Backward extends BfsIterImpl.Abstract {

		Backward(IndexGraph g, int source) {
			this(g, IntIterators.singleton(source));
		}

		private Backward(IndexGraph g, IntIterator sources) {
			super(g, sources);
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			long l = queue.dequeueLong();
			final int v = JGAlgoUtils.long2low(l);
			inEdge = JGAlgoUtils.long2high(l);
			if (v == firstVInLayer) {
				layer++;
				firstVInLayer = -1;
			}

			for (IEdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int u = eit.sourceInt();
				if (visited.get(u))
					continue;
				visited.set(u);
				queue.enqueue(JGAlgoUtils.longPack(u, e));
				if (firstVInLayer == -1)
					firstVInLayer = u;
			}

			return v;
		}
	}

	static class IntBFSFromIndexBFS implements Bfs.IntIter {

		private final Bfs.IntIter indexIter;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntBFSFromIndexBFS(Bfs.IntIter indexIter, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			this.indexIter = Objects.requireNonNull(indexIter);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
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
			int e = indexIter.lastEdgeInt();
			return e == -1 ? -1 : eiMap.indexToIdInt(e);
		}

		@Override
		public int layer() {
			return indexIter.layer();
		}
	}

	static class ObjBFSFromIndexBFS<V, E> implements Bfs.Iter<V, E> {

		private final Bfs.IntIter indexIter;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjBFSFromIndexBFS(Bfs.IntIter indexIter, IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
			this.indexIter = Objects.requireNonNull(indexIter);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
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
			int e = indexIter.lastEdgeInt();
			return e == -1 ? null : eiMap.indexToId(e);
		}

		@Override
		public int layer() {
			return indexIter.layer();
		}
	}

}
