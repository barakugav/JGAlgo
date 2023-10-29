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
import java.util.Iterator;
import java.util.Objects;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class PathImpl implements Path {

	private final IndexGraph g;
	private final int source;
	private final int target;
	private final IntList edges;
	private IntList vertices;
	private boolean isSimple, isSimpleValid;

	/**
	 * Construct a new path in a graph from an edge list, a source and a target vertices.
	 *
	 * @param g      a graph
	 * @param source a source vertex
	 * @param target a target vertex
	 * @param edges  a list of edges that form a path from the {@code source} to the {@code target} vertices in the
	 *                   graph.
	 */
	PathImpl(IndexGraph g, int source, int target, IntList edges) {
		assert Path.isPath(g, source, target, edges);
		this.g = g;
		this.source = source;
		this.target = target;
		this.edges = edges instanceof IntLists.UnmodifiableList || edges instanceof IntImmutableList ? edges
				: IntLists.unmodifiable(edges);
	}

	@Override
	public int source() {
		return source;
	}

	@Override
	public int target() {
		return target;
	}

	@Override
	public IEdgeIter edgeIter() {
		return g.isDirected() ? new IterDirected(g, edges) : new IterUndirected(g, edges, source);
	}

	@Override
	public IntList edges() {
		return edges;
	}

	@Override
	public IntList vertices() {
		if (vertices == null) {
			if (edges.isEmpty()) {
				vertices = IntLists.emptyList();
			} else {
				int[] res = new int[edges().size() + (isCycle() ? 0 : 1)];
				int resIdx = 0;
				for (IEdgeIter it = edgeIter();;) {
					it.nextInt();
					res[resIdx++] = it.sourceInt();
					if (!it.hasNext()) {
						if (!isCycle()) {
							assert it.targetInt() == target();
							res[resIdx++] = target();
						}
						break;
					}
				}
				vertices = IntImmutableList.of(res);
			}
		}
		return vertices;
	}

	@Override
	public boolean isSimple() {
		if (!isSimpleValid) {
			final int n = g.vertices().size();
			IntList vs;
			if (edges().isEmpty()) {
				assert source == target;
				isSimple = true; /* a single vertex */

			} else if (source == target) {
				isSimple = false; /* a cycle */

			} else if ((vs = vertices()).size() > n) {
				isSimple = false; /* path with length greater than the vertices num */

			} else if (vs.size() * 4 > n / 8) {
				BitSet visited = new BitSet(n);
				isSimple = true;
				for (int v : vs) {
					if (visited.get(v)) {
						isSimple = false;
						break;
					}
					visited.set(v);
				}

			} else {
				IntSet visited = new IntOpenHashSet();
				isSimple = true;
				for (int v : vs) {
					if (visited.contains(v)) {
						isSimple = false;
						break;
					}
					visited.add(v);
				}

			}
			isSimpleValid = true;
		}
		return isSimple;
	}

	private static class IterUndirected implements IEdgeIter {

		private final IndexGraph g;
		private final IntListIterator it;
		private int e = -1, v = -1;

		IterUndirected(IndexGraph g, IntList path, int source) {
			Assertions.Graphs.onlyUndirected(g);
			this.g = g;
			v = source;
			it = path.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			e = it.nextInt();
			assert v == g.edgeSource(e) || v == g.edgeTarget(e);
			v = g.edgeEndpoint(e, v);
			return e;
		}

		@Override
		public int peekNextInt() {
			int peek = it.nextInt();
			it.previousInt(); /* go back */
			return peek;
		}

		@Override
		public int sourceInt() {
			return g.edgeEndpoint(e, v);
		}

		@Override
		public int targetInt() {
			return v;
		}

	}

	private static class IterDirected implements IEdgeIter {

		private final IndexGraph g;
		private final IntListIterator it;
		private int e = -1;

		IterDirected(IndexGraph g, IntList path) {
			Assertions.Graphs.onlyDirected(g);
			this.g = g;
			it = path.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			int eNext = it.nextInt();
			if (e != -1)
				assert g.edgeTarget(e) == g.edgeSource(eNext);
			return e = eNext;
		}

		@Override
		public int peekNextInt() {
			int peek = it.nextInt();
			it.previousInt(); /* go back */
			return peek;
		}

		@Override
		public int sourceInt() {
			return g.edgeSource(e);
		}

		@Override
		public int targetInt() {
			return g.edgeTarget(e);
		}

	}

	@Override
	public String toString() {
		return edges().toString();
	}

	static Path pathFromIndexPath(Path path, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
		return path == null ? null : new PathFromIndexPath(path, viMap, eiMap);
	}

	static Path findPath(IndexGraph g, final int source, final int target) {
		if (source == target)
			return new PathImpl(g, source, target, IntLists.emptyList());
		int n = g.vertices().size();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		IntArrayList path = new IntArrayList();
		for (BfsIter it = BfsIter.newInstanceBackward(g, target); it.hasNext();) {
			int p = it.nextInt();
			backtrack[p] = it.lastEdge();
			if (p == source)
				break;
		}

		if (backtrack[source] == -1)
			return null;

		for (int p = source; p != target;) {
			int e = backtrack[p];
			path.add(e);
			p = g.edgeEndpoint(e, p);
		}

		return new PathImpl(g, source, target, path);
	}

	private static class PathFromIndexPath implements Path {

		private final Path path;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		PathFromIndexPath(Path path, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			this.path = Objects.requireNonNull(path);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public int source() {
			return viMap.indexToIdInt(path.source());
		}

		@Override
		public int target() {
			return viMap.indexToIdInt(path.target());
		}

		@Override
		public IEdgeIter edgeIter() {
			return IndexIdMaps.indexToIdEdgeIter(path.edgeIter(), viMap, eiMap);
		}

		@Override
		public IntList edges() {
			return IndexIdMaps.indexToIdList(path.edges(), eiMap);
		}

		@Override
		public IntList vertices() {
			return IndexIdMaps.indexToIdList(path.vertices(), viMap);
		}

		@Override
		public boolean isSimple() {
			return path.isSimple();
		}

		@Override
		public String toString() {
			return edges().toString();
		}
	}

	static class IterFromIndexIter implements Iterator<Path> {

		private final Iterator<Path> res;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IterFromIndexIter(Iterator<Path> res, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public boolean hasNext() {
			return res.hasNext();
		}

		@Override
		public Path next() {
			return pathFromIndexPath(res.next(), viMap, eiMap);
		}

	}

}
