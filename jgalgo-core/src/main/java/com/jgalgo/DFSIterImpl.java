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
package com.jgalgo;

import java.util.BitSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class DFSIterImpl implements DFSIter {

	private final IndexGraph g;
	private final BitSet visited;
	private final Stack<EdgeIter> edgeIters;
	private final IntArrayList edgePath;
	private final IntList edgePathView;
	private boolean isValid;

	/**
	 * Create a DFS iterator rooted at some source vertex.
	 *
	 * @param g      a graph
	 * @param source a vertex in the graph from which the search will start from
	 */
	public DFSIterImpl(IndexGraph g, int source) {
		int n = g.vertices().size();
		this.g = g;
		visited = new BitSet(n);
		edgeIters = new ObjectArrayList<>();
		edgePath = new IntArrayList();
		edgePathView = IntLists.unmodifiable(edgePath);

		visited.set(source);
		edgeIters.push(g.outEdges(source).iterator());
		isValid = true;
	}

	@Override
	public boolean hasNext() {
		if (isValid)
			return true;
		if (edgeIters.isEmpty())
			return false;
		for (;;) {
			for (EdgeIter eit = edgeIters.top(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (visited.get(v))
					continue;
				visited.set(v);
				edgeIters.push(g.outEdges(v).iterator());
				edgePath.add(e);
				return isValid = true;
			}
			edgeIters.pop();
			if (edgeIters.isEmpty()) {
				assert edgePath.isEmpty();
				return false;
			}
			edgePath.popInt();
		}
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		int ret = edgeIters.top().source();
		isValid = false;
		return ret;
	}

	@Override
	public IntList edgePath() {
		return edgePathView;
	}

	static class DFSFromIndexDFS implements DFSIter {

		private final DFSIter it;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		DFSFromIndexDFS(DFSIter it, IndexIdMap viMap, IndexIdMap eiMap) {
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
		public IntList edgePath() {
			return new IndexIdMapUtils.ListFromIndexList(it.edgePath(), eiMap);
		}

	}
}
