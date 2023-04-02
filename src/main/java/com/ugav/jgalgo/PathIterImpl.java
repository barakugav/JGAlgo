package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

class PathIterImpl {

	private PathIterImpl() {
	}

	static class Undirected implements PathIter {

		private final UGraph g;
		private final IntIterator it;
		private int e = -1, v = -1;

		Undirected(UGraph g, IntList path) {
			this.g = g;
			if (path.size() == 1) {
				v = g.edgeTarget(path.getInt(0));
			} else if (path.size() >= 2) {
				int e0 = path.getInt(0), e1 = path.getInt(1);
				int u0 = g.edgeSource(e0), v0 = g.edgeTarget(e0);
				int u1 = g.edgeSource(e1), v1 = g.edgeTarget(e1);
				if (v0 == u1 || v0 == v1) {
					v = u0;
				} else {
					v = v0;
					assert (u0 == u1 || u0 == v1) : "not a path";
				}
			}
			it = path.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextEdge() {
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

	static class Directed implements PathIter {

		private final DiGraph g;
		private final IntIterator it;
		private int e = -1;

		Directed(DiGraph g, IntList path) {
			this.g = g;
			it = path.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextEdge() {
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
