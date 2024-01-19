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
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class PathImpl {

	private abstract static class AbstractPath<V, E> implements Path<V, E> {

		@Override
		public String toString() {
			return edges().toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof Path))
				return false;
			Path<?, ?> other = (Path<?, ?>) obj;
			return graph() == other.graph() && source().equals(other.source()) && target().equals(other.target())
					&& edges().equals(other.edges());
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(graph()) ^ source().hashCode() ^ target().hashCode() ^ edges().hashCode();
		}
	}

	static class IndexPath extends AbstractPath<Integer, Integer> implements IPath {

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
		IndexPath(IndexGraph g, int source, int target, IntList edges) {
			assert IPath.isPath(g, source, target, edges);
			this.g = g;
			this.source = source;
			this.target = target;
			this.edges = edges instanceof IntLists.UnmodifiableList || edges instanceof IntImmutableList ? edges
					: IntLists.unmodifiable(edges);
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
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
					assert isCycle();
					vertices = IntList.of(source);
				} else {
					int[] res = new int[edges().size() + (isCycle() ? 0 : 1)];
					int resIdx = 0;
					for (IEdgeIter it = edgeIter();;) {
						it.nextInt();
						res[resIdx++] = it.sourceInt();
						if (!it.hasNext()) {
							if (!isCycle()) {
								assert it.targetInt() == targetInt();
								res[resIdx++] = targetInt();
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
		public IndexGraph graph() {
			return g;
		}

		@Override
		public boolean isSimple() {
			if (!isSimpleValid) {
				final int n = g.vertices().size();
				IntList vs;
				if (source == target) {
					isSimple = edges().isEmpty(); /* a cycle or isolated vertex */

				} else if ((vs = vertices()).size() > n) {
					isSimple = false; /* path with length greater than the vertices num */

				} else if (vs.size() * 4 > n / 8) {
					Bitmap visited = new Bitmap(n);
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
						if (!visited.add(v)) {
							isSimple = false;
							break;
						}
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
				Assertions.onlyUndirected(g);
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
				Assertions.onlyDirected(g);
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
	}

	static IPath intPathFromIndexPath(IntGraph g, IPath indexPath) {
		return indexPath == null ? null : new IntPathFromIndexPath(g, indexPath);
	}

	static <V, E> Path<V, E> objPathFromIndexPath(Graph<V, E> g, IPath indexPath) {
		return indexPath == null ? null : new ObjPathFromIndexPath<>(g, indexPath);
	}

	@SuppressWarnings("unchecked")
	static <V, E> Path<V, E> pathFromIndexPath(Graph<V, E> g, IPath indexPath) {
		if (indexPath == null)
			return null;
		if (g instanceof IntGraph) {
			return (Path<V, E>) new IntPathFromIndexPath((IntGraph) g, indexPath);
		} else {
			return new ObjPathFromIndexPath<>(g, indexPath);
		}
	}

	static IPath findPath(IndexGraph g, final int source, final int target) {
		if (source == target)
			return IPath.valueOf(g, source, target, IntLists.emptyList());
		int n = g.vertices().size();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		IntArrayList path = new IntArrayList();
		for (BfsIter.Int it = BfsIter.newInstanceBackward(g, target); it.hasNext();) {
			int p = it.nextInt();
			backtrack[p] = it.lastEdgeInt();
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

		return IPath.valueOf(g, source, target, path);
	}

	static boolean isPath(IndexGraph g, int source, int target, IntIterator edges) {
		if (!g.vertices().contains(source) || !g.vertices().contains(target))
			return false;
		if (!edges.hasNext())
			return source == target;

		if (g.isDirected()) {
			int v = source;
			while (edges.hasNext()) {
				int e = edges.nextInt();
				if (!g.edges().contains(e) || g.edgeSource(e) != v)
					return false;
				v = g.edgeTarget(e);
			}
			return v == target;

		} else {
			int v = source;
			while (edges.hasNext()) {
				int e = edges.nextInt();
				if (!g.edges().contains(e))
					return false;
				if (g.edgeSource(e) == v) {
					v = g.edgeTarget(e);
				} else if (g.edgeTarget(e) == v) {
					v = g.edgeSource(e);
				} else {
					return false;
				}
			}
			return v == target;
		}
	}

	private static class ObjPathFromIndexPath<V, E> extends AbstractPath<V, E> {

		private final IPath indexPath;
		private final Graph<V, E> g;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjPathFromIndexPath(Graph<V, E> g, IPath indexPath) {
			this.indexPath = Objects.requireNonNull(indexPath);
			this.g = g;
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public V source() {
			return viMap.indexToId(indexPath.sourceInt());
		}

		@Override
		public V target() {
			return viMap.indexToId(indexPath.targetInt());
		}

		@Override
		public EdgeIter<V, E> edgeIter() {
			return IndexIdMaps.indexToIdEdgeIter(g, indexPath.edgeIter());
		}

		@Override
		public List<E> edges() {
			return IndexIdMaps.indexToIdList(indexPath.edges(), eiMap);
		}

		@Override
		public List<V> vertices() {
			return IndexIdMaps.indexToIdList(indexPath.vertices(), viMap);
		}

		@Override
		public Graph<V, E> graph() {
			return g;
		}

		@Override
		public boolean isSimple() {
			return indexPath.isSimple();
		}
	}

	private static class IntPathFromIndexPath extends AbstractPath<Integer, Integer> implements IPath {

		private final IPath indexPath;
		private final IntGraph g;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntPathFromIndexPath(IntGraph g, IPath indexPath) {
			this.indexPath = Objects.requireNonNull(indexPath);
			this.g = g;
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public int sourceInt() {
			return viMap.indexToIdInt(indexPath.sourceInt());
		}

		@Override
		public int targetInt() {
			return viMap.indexToIdInt(indexPath.targetInt());
		}

		@Override
		public IEdgeIter edgeIter() {
			return IndexIdMaps.indexToIdEdgeIter(g, indexPath.edgeIter());
		}

		@Override
		public IntList edges() {
			return IndexIdMaps.indexToIdList(indexPath.edges(), eiMap);
		}

		@Override
		public IntList vertices() {
			return IndexIdMaps.indexToIdList(indexPath.vertices(), viMap);
		}

		@Override
		public IntGraph graph() {
			return g;
		}

		@Override
		public boolean isSimple() {
			return indexPath.isSimple();
		}
	}

}
