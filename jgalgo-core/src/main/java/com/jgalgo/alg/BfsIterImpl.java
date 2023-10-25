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
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueLongNoReduce;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

class BfsIterImpl {

	private static abstract class Abstract implements BfsIter {

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
				queue.enqueue(JGAlgoUtils.longCompose(source, -1));
				if (firstVInLayer == -1)
					firstVInLayer = source;
			} while (sources.hasNext());
		}

		@Override
		public boolean hasNext() {
			return !queue.isEmpty();
		}

		@Override
		public int lastEdge() {
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

			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (visited.get(v))
					continue;
				visited.set(v);
				queue.enqueue(JGAlgoUtils.longCompose(v, e));
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

			for (EdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int u = eit.source();
				if (visited.get(u))
					continue;
				visited.set(u);
				queue.enqueue(JGAlgoUtils.longCompose(u, e));
				if (firstVInLayer == -1)
					firstVInLayer = u;
			}

			return v;
		}
	}

	static class BFSFromIndexBFS implements BfsIter {

		private final BfsIter it;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		BFSFromIndexBFS(BfsIter it, IndexIdMap viMap, IndexIdMap eiMap) {
			this.it = Objects.requireNonNull(it);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			return viMap.indexToId(it.nextInt());
		}

		@Override
		public int lastEdge() {
			int e = it.lastEdge();
			return e == -1 ? -1 : eiMap.indexToId(e);
		}

		@Override
		public int layer() {
			return it.layer();
		}

	}
}
