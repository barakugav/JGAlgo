package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.RMQ.IntArrayComparator;

public class LCARMQ implements LCA {

	private LCARMQ() {
	}

	private static final LCARMQ INSTANCE = new LCARMQ();

	public static LCARMQ getInstace() {
		return INSTANCE;
	}

	@Override
	public <E> Result preprocessLCA(Graph<E> g, int r) {
		int n = g.vertices();
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
		edgesCount[0] = g.getEdgesArrVs(r, edges, 0);
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
				edgesCount[s] = g.getEdgesArrVs(child, edges, edgesOffset[s]);
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

		RMQ.Result rmq = RMQPlusMinusOne.getInstace().preprocessRMQ(new IntArrayComparator(depths), depths.length);
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
