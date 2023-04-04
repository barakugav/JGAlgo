package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public class Path implements IntIterable {

	private final Graph g;
	public final int source;
	public final int target;
	public final IntList edges;

	Path(Graph g, int source, int target, IntList edges) {
		this.g = g;
		this.source = source;
		this.target = target;
		this.edges = edges instanceof IntLists.UnmodifiableList ? edges : IntLists.unmodifiable(edges);
	}

	@Override
	public EdgeIter iterator() {
		if (g instanceof UGraph g0) {
			return new IterUndirected(g0, edges, source);
		} else if (g instanceof DiGraph g0) {
			return new IterDirected(g0, edges);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static class IterUndirected implements EdgeIter {

		private final UGraph g;
		private final IntIterator it;
		private int e = -1, v = -1;

		IterUndirected(UGraph g, IntList path, int source) {
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
		public int u() {
			return g.edgeEndpoint(e, v);
		}

		@Override
		public int v() {
			return v;
		}

	}

	private static class IterDirected implements EdgeIter {

		private final DiGraph g;
		private final IntIterator it;
		private int e = -1;

		IterDirected(DiGraph g, IntList path) {
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
		public int u() {
			return g.edgeSource(e);
		}

		@Override
		public int v() {
			return g.edgeTarget(e);
		}

	}

}
