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

import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class DfsIterImpl implements Dfs.IntIter {

	private final IndexGraph g;
	private final Bitmap visited;
	private final Stack<IEdgeIter> edgeIters;
	private final IntArrayList nextEdgePath;
	private final IntArrayList edgePath;
	private final IntList edgePathView;
	private int nextV = -1;
	private int edgePathAndNextEdgePathCommonElmsNum = 0;

	/**
	 * Create a DFS iterator rooted at some source vertex.
	 *
	 * @param g      a graph
	 * @param source a vertex in the graph from which the search will start from
	 */
	public DfsIterImpl(IndexGraph g, int source) {
		int n = g.vertices().size();
		this.g = g;
		visited = new Bitmap(n);
		edgeIters = new ObjectArrayList<>();
		nextEdgePath = new IntArrayList();
		edgePath = new IntArrayList();
		edgePathView = IntLists.unmodifiable(edgePath);

		visited.set(source);
		edgeIters.push(g.outEdges(source).iterator());
		nextV = source;
	}

	@Override
	public boolean hasNext() {
		return nextV != -1;
	}

	@Override
	public int nextInt() {
		Assertions.hasNext(this);

		final int ret = nextV;
		/* sync edgePath to naxEdgePath */
		edgePath.removeElements(edgePathAndNextEdgePathCommonElmsNum, edgePath.size());
		edgePath
				.addElements(edgePath.size(), nextEdgePath.elements(), edgePathAndNextEdgePathCommonElmsNum,
						nextEdgePath.size() - edgePathAndNextEdgePathCommonElmsNum);
		assert edgePath.equals(nextEdgePath);
		edgePathAndNextEdgePathCommonElmsNum = edgePath.size();

		if (edgeIters.isEmpty()) {
			nextV = -1;
		} else {
			advance: for (;;) {
				for (IEdgeIter eit = edgeIters.top(); eit.hasNext();) {
					final int e = eit.nextInt();
					int v = eit.targetInt();
					if (visited.get(v))
						continue;
					visited.set(v);
					edgeIters.push(g.outEdges(v).iterator());
					nextEdgePath.add(e);
					nextV = v;
					break advance;
				}
				edgeIters.pop();
				if (edgeIters.isEmpty()) {
					assert nextEdgePath.isEmpty();
					nextV = -1;
					break advance;
				}
				nextEdgePath.popInt();
				edgePathAndNextEdgePathCommonElmsNum =
						Math.min(edgePathAndNextEdgePathCommonElmsNum, nextEdgePath.size());
			}
		}

		return ret;
	}

	@Override
	public IntList edgePath() {
		return edgePathView;
	}

	static class IntDfsFromIndexDfs implements Dfs.IntIter {

		private final Dfs.IntIter indexIter;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntDfsFromIndexDfs(IntGraph g, Dfs.IntIter indexIter) {
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
		public IntList edgePath() {
			return IndexIdMaps.indexToIdList(indexIter.edgePath(), eiMap);
		}
	}

	static class ObjDfsFromIndexDfs<V, E> implements Dfs.Iter<V, E> {

		private final Dfs.IntIter indexIter;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjDfsFromIndexDfs(Graph<V, E> g, Dfs.IntIter indexIter) {
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
		public List<E> edgePath() {
			return IndexIdMaps.indexToIdList(indexIter.edgePath(), eiMap);
		}
	}

}
