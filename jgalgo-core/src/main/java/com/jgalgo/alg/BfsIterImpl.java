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
import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueLongNoReduce;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class BfsIterImpl {

	private abstract static class Abstract implements BfsIter.Int {

		final IndexGraph g;
		final Bitmap visited;
		final LongPriorityQueue queue;
		int inEdge;
		int layer;
		int firstVInLayer;

		private Abstract(IndexGraph g, IntIterator sources) {
			this.g = g;
			int n = g.vertices().size();
			visited = new Bitmap(n);
			queue = new FIFOQueueLongNoReduce();
			inEdge = -1;
			layer = -1;

			firstVInLayer = -1;
			do {
				int source = sources.nextInt();
				Assertions.checkVertex(source, n);
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
			Assertions.hasNext(this);
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
			Assertions.hasNext(this);
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

	static class IntBfsFromIndexBfs implements BfsIter.Int {

		private final BfsIter.Int indexIter;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntBfsFromIndexBfs(IntGraph g, BfsIter.Int indexIter) {
			this.indexIter = Objects.requireNonNull(indexIter);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
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
			return eiMap.indexToIdIfExistInt(e);
		}

		@Override
		public int layer() {
			return indexIter.layer();
		}
	}

	static class ObjBfsFromIndexBfs<V, E> implements BfsIter<V, E> {

		private final BfsIter.Int indexIter;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjBfsFromIndexBfs(Graph<V, E> g, BfsIter.Int indexIter) {
			this.indexIter = Objects.requireNonNull(indexIter);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
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
			return eiMap.indexToIdIfExist(e);
		}

		@Override
		public int layer() {
			return indexIter.layer();
		}
	}

	static List<IntSet> bfsLayers(IndexGraph g, int source) {
		final int n = g.vertices().size();
		Assertions.checkVertex(source, n);

		int[] layer = new int[n];
		int layerSize = 0;
		int[] vToLayer = new int[n];
		Arrays.fill(vToLayer, -1);

		List<IntSet> layers = new ObjectArrayList<>();
		int currentLayerBegin = 0;
		int nextLayer = 0;

		var helperFuncs = new Object() {
			void addLayer(int l, int begin, int end) {
				layers
						.add(ImmutableIntArraySet
								.newInstance(layer, begin, end,
										v -> 0 <= v && v < vToLayer.length && vToLayer[v] == l));
			}
		};
		for (BfsIter.Int iter = BfsIter.newInstance(g, source); iter.hasNext();) {
			int v = iter.nextInt();
			int l = iter.layer();
			if (l == nextLayer) {
				if (l > 0) {
					helperFuncs.addLayer(l - 1, currentLayerBegin, layerSize);
					currentLayerBegin = layerSize;
				}
				nextLayer++;
			}
			vToLayer[v] = l;
			layer[layerSize++] = v;
		}
		helperFuncs.addLayer(nextLayer - 1, currentLayerBegin, layerSize);

		return layers;
	}

}
