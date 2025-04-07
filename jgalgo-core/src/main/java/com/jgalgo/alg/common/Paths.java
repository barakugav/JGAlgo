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
package com.jgalgo.alg.common;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.IntImmutableList2;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;

class Paths {

	private Paths() {}

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
			Path<?, ?> o = (Path<?, ?>) obj;
			Graph<?, ?> g = graph();
			if (g != o.graph())
				return false;
			final boolean isCycle = isCycle();
			if (isCycle != o.isCycle())
				return false;
			final List<?> es1 = edges(), es2 = o.edges();
			final int size = es1.size();
			if (size != es2.size())
				return false;
			final Object s1 = source(), s2 = o.source(), t1 = target(), t2 = o.target();
			if (size == 0) {
				assert s1.equals(t1) && s2.equals(t2);
				return s1.equals(s2);
			}
			if (g.isDirected()) {
				if (isCycle) {
					for (int offset : range(size)) {
						boolean eq = es1.subList(0, size - offset).equals(es2.subList(offset, size))
								&& es1.subList(size - offset, size).equals(es2.subList(0, offset));
						if (eq)
							return true;
					}
					return false;

				} else {
					return s1.equals(s2) && t1.equals(t2) && es1.equals(es2);
				}
			} else {
				if (isCycle) {
					for (int offset : range(size)) {
						boolean eq1 = es1.subList(0, size - offset).equals(es2.subList(offset, size))
								&& es1.subList(size - offset, size).equals(es2.subList(0, offset));
						if (eq1)
							return true;
						boolean eq2 = reverseEquals(es1.subList(0, offset + 1), es2.subList(0, offset + 1))
								&& (offset == size - 1
										|| reverseEquals(es1.subList(offset + 1, size), es2.subList(offset + 1, size)));
						if (eq2)
							return true;
					}
					return false;

				} else if (s1.equals(s2) && t1.equals(t2)) {
					return es1.equals(es2);
				} else if (t1.equals(s2) && s1.equals(t2)) {
					return reverseEquals(es1, es2);
				} else {
					return false;
				}
			}
		}

		private static boolean reverseEquals(List<?> es1, List<?> es2) {
			assert es1.size() == es2.size();
			if (es1 instanceof IntList && es2 instanceof IntList) {
				IntIterator it1 = ((IntList) es1).iterator();
				IntListIterator it2 = ((IntList) es2).listIterator(es2.size());
				for (int i = es1.size(); i-- > 0;)
					if (it1.nextInt() != it2.previousInt())
						return false;
			} else {
				Iterator<?> it1 = es1.iterator();
				ListIterator<?> it2 = es2.listIterator(es2.size());
				for (int i = es1.size(); i-- > 0;)
					if (!it1.next().equals(it2.previous()))
						return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = System.identityHashCode(graph());
			if (!isCycle())
				hash ^= source().hashCode() ^ target().hashCode();
			List<?> es = edges();
			if (es instanceof IntList) {
				for (int e : (IntList) es)
					hash ^= e;
			} else {
				for (Object e : es)
					hash ^= e.hashCode();
			}
			return hash;
		}
	}

	static IndexPath valueOf(IndexGraph g, int source, int target, IntList edges) {
		boolean unmodifiable = edges instanceof IntLists.UnmodifiableList || edges instanceof IntImmutableList2;
		return new IndexPath(g, source, target, unmodifiable ? edges : IntLists.unmodifiable(edges));
	}

	static final class IndexPath extends AbstractPath<Integer, Integer> implements IPath {

		private final IndexGraph g;
		private final int source;
		private final int target;
		private final IntList edges;
		private IntList vertices;
		private boolean isSimple, isSimpleValid;

		private IndexPath(IndexGraph g, int source, int target, IntList edges) {
			assert IPath.isPath(g, source, target, edges);
			this.g = g;
			this.source = source;
			this.target = target;
			this.edges = edges;
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
					vertices = Fastutil.list(source);
				} else {
					int[] res = new int[edges().size() + 1];
					IntIterator vit = IPath.verticesIter(g, source, edges);
					for (int i = 0; i < res.length; i++)
						res[i] = vit.nextInt();
					assert !vit.hasNext();
					vertices = Fastutil.list(res);
				}
			}
			return vertices;
		}

		@Override
		public IndexGraph graph() {
			return g;
		}

		@Override
		public IPath subPath(int fromEdgeIndex, int toEdgeIndex) {
			IntList subEdges = edges.subList(fromEdgeIndex, toEdgeIndex);
			int subSource, subTarget;
			if (vertices != null) {
				subSource = vertices.getInt(fromEdgeIndex);
				subTarget = vertices.getInt(toEdgeIndex);
			} else {
				IntIterator vit = IPath.verticesIter(g, source, edges);
				vit.skip(fromEdgeIndex);
				subSource = vit.nextInt();

				if (fromEdgeIndex == toEdgeIndex) {
					subTarget = subSource;
				} else {
					vit.skip(toEdgeIndex - fromEdgeIndex - 1);
					subTarget = vit.nextInt();
				}
			}
			return new IndexPath(g, subSource, subTarget, subEdges);
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

		private static class IterUndirected implements IEdgeIter, IterTools.Peek.Int {

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

			@Deprecated
			@Override
			public Integer peekNext() {
				return IEdgeIter.super.peekNext();
			}
		}

		private static class IterDirected implements IEdgeIter, IterTools.Peek.Int {

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
				if (e >= 0)
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

			@Override
			public int skip(final int n) {
				int skipped = it.skip(n);
				e = -1;
				return skipped;
			}

			@Deprecated
			@Override
			public Integer peekNext() {
				return IEdgeIter.super.peekNext();
			}
		}
	}

	static IPath findPath(IndexGraph g, final int source, final int target) {
		if (source == target)
			return IPath.valueOf(g, source, target, IntLists.emptyList());
		int n = g.vertices().size();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		backtrack[target] = Integer.MAX_VALUE; /* mark as visited */
		queue.enqueue(target);
		while (!queue.isEmpty()) {
			final int v = queue.dequeueInt();
			for (IEdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int u = eit.sourceInt();
				if (backtrack[u] >= 0)
					continue;
				backtrack[u] = e;
				if (u == source) {
					IntArrayList path = new IntArrayList();
					if (g.isDirected()) {
						for (int p = source; p != target;) {
							e = backtrack[p];
							path.add(e);
							p = g.edgeTarget(e);
						}
					} else {
						for (int p = source; p != target;) {
							e = backtrack[p];
							path.add(e);
							p = g.edgeEndpoint(e, p);
						}
					}
					return IPath.valueOf(g, source, target, path);
				}
				queue.enqueue(u);
			}
		}
		return null;

	}

	static boolean isPath(IndexGraph g, int source, int target, IntList edges) {
		if (!g.vertices().contains(source) || !g.vertices().contains(target))
			return false;
		int size = edges.size();
		if (size == 0)
			return source == target;

		IntIterator eit = edges.iterator();
		if (g.isDirected()) {
			int v = source;
			for (int i = size; i-- > 0;) {
				int e = eit.nextInt();
				if (!g.edges().contains(e) || g.edgeSource(e) != v)
					return false;
				v = g.edgeTarget(e);
			}
			return v == target;

		} else {
			int v = source;
			for (int i = size; i-- > 0;) {
				int e = eit.nextInt();
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

	static class ObjPathFromIndexPath<V, E> extends AbstractPath<V, E> {

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
		public Path<V, E> subPath(int fromEdgeIndex, int toEdgeIndex) {
			return new ObjPathFromIndexPath<>(g, indexPath.subPath(fromEdgeIndex, toEdgeIndex));
		}

		@Override
		public boolean isSimple() {
			return indexPath.isSimple();
		}
	}

	static class IntPathFromIndexPath extends AbstractPath<Integer, Integer> implements IPath {

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
		public IPath subPath(int fromEdgeIndex, int toEdgeIndex) {
			return new IntPathFromIndexPath(g, indexPath.subPath(fromEdgeIndex, toEdgeIndex));
		}

		@Override
		public boolean isSimple() {
			return indexPath.isSimple();
		}
	}

}
