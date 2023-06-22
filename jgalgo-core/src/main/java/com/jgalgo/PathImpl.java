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

import java.util.Arrays;
import java.util.Objects;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntLists;

class PathImpl extends AbstractIntList implements Path {

	private final IndexGraph g;
	private final int source;
	private final int target;
	private final IntList edges;

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
		this.g = g;
		this.source = source;
		this.target = target;
		this.edges = edges instanceof IntLists.UnmodifiableList ? edges : IntLists.unmodifiable(edges);
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
	public IntListIterator iterator() {
		return edges.iterator();
	}

	@Override
	public EdgeIter edgeIter() {
		return g.getCapabilities().directed() ? new IterDirected(g, edges) : new IterUndirected(g, edges, source);
	}

	private static class IterUndirected implements EdgeIter {

		private final IndexGraph g;
		private final IntListIterator it;
		private int e = -1, v = -1;

		IterUndirected(IndexGraph g, IntList path, int source) {
			ArgumentCheck.onlyUndirected(g);
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
		public int peekNext() {
			int peek = it.nextInt();
			it.previousInt(); /* go back */
			return peek;
		}

		@Override
		public int source() {
			return g.edgeEndpoint(e, v);
		}

		@Override
		public int target() {
			return v;
		}

	}

	private static class IterDirected implements EdgeIter {

		private final IndexGraph g;
		private final IntListIterator it;
		private int e = -1;

		IterDirected(IndexGraph g, IntList path) {
			ArgumentCheck.onlyDirected(g);
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
		public int peekNext() {
			int peek = it.nextInt();
			it.previousInt(); /* go back */
			return peek;
		}

		@Override
		public int source() {
			return g.edgeSource(e);
		}

		@Override
		public int target() {
			return g.edgeTarget(e);
		}

	}

	@Override
	public int size() {
		return edges.size();
	}

	@Override
	public int getInt(int index) {
		return edges.getInt(index);
	}

	@Override
	public int indexOf(int k) {
		return edges.indexOf(k);
	}

	@Override
	public int lastIndexOf(int k) {
		return edges.lastIndexOf(k);
	}

	static Path pathFromIndexPath(Path path, IndexIdMap viMap, IndexIdMap eiMap) {
		return path == null ? null : new PathFromIndexPath(path, viMap, eiMap);
	}

	static Path findPath(IndexGraph g, final int source, final int target) {
		if (source == target)
			return new PathImpl(g, source, target, IntLists.emptyList());
		int n = g.vertices().size();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		IntArrayList path = new IntArrayList();
		for (BFSIter it = BFSIter.newInstanceBackward(g, target); it.hasNext();) {
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

	private static class PathFromIndexPath extends AbstractIntList implements Path {

		private final Path path;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		PathFromIndexPath(Path path, IndexIdMap viMap, IndexIdMap eiMap) {
			this.path = Objects.requireNonNull(path);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public int source() {
			return viMap.indexToId(path.source());
		}

		@Override
		public int target() {
			return viMap.indexToId(path.target());
		}

		@Override
		public int getInt(int index) {
			return eiMap.indexToId(path.getInt(index));
		}

		@Override
		public int size() {
			return path.size();
		}

		@Override
		public EdgeIter edgeIter() {
			return new Edges.EdgeIterFromIndexEdgeIter(path.edgeIter(), viMap, eiMap);
		}
	}

}
