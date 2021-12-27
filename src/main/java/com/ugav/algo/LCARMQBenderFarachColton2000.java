package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.RMQ.IntArrayComparator;

public class LCARMQBenderFarachColton2000 implements LCA {

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
		int[] edges = new int[n * 2];

		int s = 0;
		int[] parent = new int[n];
		int[] edgesOffset = new int[n];
		int[] edgesCount = new int[n];
		int[] edgesIdx = new int[n];

		parent[0] = -1;
		edgesOffset[0] = 0;
		edgesCount[0] = t.getEdgesArrVs(r, edges, 0);
		edgesIdx[0] = 0;

		int aLen = 0;
		for (int v = r; v != -1;) {
			depths[aLen] = s;
			vs[aLen] = v;
			aLen++;

			int child = -1;
			for (int i = edgesIdx[s]; i < edgesCount[s]; i++) {
				if (edges[edgesOffset[s] + i] != parent[s]) {
					child = edges[edgesOffset[s] + i];
					edgesIdx[s] = i + 1;
					break;
				}
			}
			if (child != -1) {
				s++;
				parent[s] = v;
				edgesOffset[s] = edgesOffset[s - 1] + edgesCount[s - 1];
				edgesCount[s] = t.getEdgesArrVs(child, edges, edgesOffset[s]);
				edgesIdx[s] = 0;
				v = child;
			} else {
				v = parent[s];
				s--;
			}
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
				.preprocessRMQ(new IntArrayComparator(depths), depths.length);
		return new DataStructure(vs, vToDepthsIdx, rmq);
	}

	private static class DataStructure implements LCA.Result {

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
