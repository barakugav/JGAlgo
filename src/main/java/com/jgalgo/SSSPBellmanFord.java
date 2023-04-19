package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class SSSPBellmanFord implements SSSP {

	/*
	 * O(m n)
	 */

	public SSSPBellmanFord() {
	}

	@Override
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		int n = g.vertices().size();
		Result res = new Result(g, source);
		res.distances[source] = 0;

		for (int i = 0; i < n - 1; i++) {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				double d = res.distances[u] + w.weight(e);
				if (d < res.distances[v]) {
					res.distances[v] = d;
					res.backtrack[v] = e;
				}
			}
		}

		for (int v = 0; v < n; v++) {
			int e = res.backtrack[v];
			if (e == -1)
				continue;
			int u = g.edgeSource(e);
			double d = res.distances[u] + w.weight(e);
			if (d < res.distances[v]) {
				BitSet visited = new BitSet(n);
				visited.set(v);
				while (!visited.get(u)) {
					visited.set(u);
					e = res.backtrack[u];
					u = g.edgeSource(e);
				}

				IntArrayList negCycle = new IntArrayList();
				for (int p = u;;) {
					e = res.backtrack[p];
					negCycle.add(e);
					p = g.edgeSource(e);
					if (p == u)
						break;
				}
				IntArrays.reverse(negCycle.elements(), 0, negCycle.size());
				res.negCycle = new Path(g, u, u, negCycle);
				return res;
			}
		}

		return res;
	}

	private static class Result extends SSSPResultImpl {

		private Path negCycle;

		private Result(Graph g, int source) {
			super(g, source);
		}

		@Override
		public double distance(int target) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return super.distance(target);
		}

		@Override
		public Path getPath(int target) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return super.getPath(target);
		}

		@Override
		public boolean foundNegativeCycle() {
			return negCycle != null;
		}

		@Override
		public Path getNegativeCycle() {
			if (!foundNegativeCycle())
				throw new IllegalStateException("no negative cycle found");
			return negCycle;
		}

		@Override
		public String toString() {
			return foundNegativeCycle() ? "[NegCycle=" + negCycle + "]" : super.toString();
		}

	}

}
