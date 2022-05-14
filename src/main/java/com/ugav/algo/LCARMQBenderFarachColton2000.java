package com.ugav.algo;

import java.util.Arrays;
import java.util.Iterator;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.RMQ.ArrayIntComparator;

public class LCARMQBenderFarachColton2000 implements LCAStatic {

	/*
	 * This implementation of static LCA (Lowest common ancestor) perform a
	 * preprocessing of O(E+V) and later answer queries of LCA queries in O(1).
	 *
	 * This is done by traversing the tree with the Euler tour, and using RMQ on the
	 * depths of the tour. This RMQ is a special case of the general RMQ, as the
	 * difference between two consecutive elements is always +1/-1, and therefore
	 * allow more efficient implementation using
	 * RMQPlusMinusOneBenderFarachColton2000.
	 */

	private LCARMQBenderFarachColton2000() {
	}

	private static final LCARMQBenderFarachColton2000 INSTANCE = new LCARMQBenderFarachColton2000();

	public static LCARMQBenderFarachColton2000 getInstace() {
		return INSTANCE;
	}

	@Override
	public <E> Result preprocessLCA(Graph<E> t, int r) {
		if (!Graphs.isTree(t, r))
			throw new IllegalArgumentException();

		int n = t.vertices();
		int[] depths = new int[n * 2];
		int[] vs = new int[n * 2];
		int[] parent = new int[n];

		@SuppressWarnings("unchecked")
		Iterator<Edge<E>>[] edges = new Iterator[n];

		parent[0] = -1;
		edges[0] = t.edges(r);

		int aLen = 0;
		dfs: for (int u = r, depth = 0;;) {
			depths[aLen] = depth;
			vs[aLen] = u;
			aLen++;

			while (edges[depth].hasNext()) {
				Edge<E> e = edges[depth].next();
				int v = e.v();
				if (v == parent[depth])
					continue;
				depth++;
				parent[depth] = u;
				edges[depth] = t.edges(v);
				u = v;
				continue dfs;
			}
			u = parent[depth];
			if (--depth < 0)
				break;
		}

		depths = Arrays.copyOf(depths, aLen);
		vs = Arrays.copyOf(vs, aLen);

		int[] vToDepthsIdx = new int[n];
		Arrays.fill(vToDepthsIdx, -1);
		for (int i = 0; i < aLen; i++) {
			int v = vs[i];
			if (vToDepthsIdx[v] == -1)
				vToDepthsIdx[v] = i;
		}

		RMQ.Result rmq = RMQPlusMinusOneBenderFarachColton2000.getInstace()
				.preprocessRMQ(new ArrayIntComparator(depths), depths.length);
		return new DataStructure(vs, vToDepthsIdx, rmq);
	}

	private static class DataStructure implements LCAStatic.Result {

		private final int[] vs;
		private final int[] vToDepthsIdx;
		private final RMQ.Result rmq;

		DataStructure(int[] vs, int[] vToDepthsIdx, RMQ.Result rmq) {
			this.vs = vs;
			this.vToDepthsIdx = vToDepthsIdx;
			this.rmq = rmq;
		}

		@Override
		public int query(int u, int v) {
			int uIdx = vToDepthsIdx[u];
			int vIdx = vToDepthsIdx[v];
			if (uIdx > vIdx) {
				int temp = uIdx;
				uIdx = vIdx;
				vIdx = temp;
			}
			int lcaIdx = rmq.query(uIdx, vIdx + 1);
			return vs[lcaIdx];
		}

	}

}
