package com.jgalgo;

import java.util.Arrays;

/**
 * Static LCA implementation using RMQ.
 * <p>
 * By traversing the tree once and assigning for each vertex a number
 * corresponding to its depth, the LCA query is equivalent to a range minimum
 * query. This RMQ problem is a special case of RMQ, as the different between
 * any pair of consecutive elements is always +1/-1, and
 * {@link RMQStaticPlusMinusOne} can be used.
 * <p>
 * The algorithm require preprocessing of {@code O(n)} time and space and answer
 * queries in {@code O(1)} time.
 * <p>
 * Based on 'Fast Algorithms for Finding Nearest Common Ancestors' by D. Harel,
 * R. Tarjan (1984).
 *
 * @author Barak Ugav
 */
public class LCAStaticRMQ implements LCAStatic {

	private final RMQStatic rmq;

	/**
	 * Create a new static LCA algorithm object.
	 */
	public LCAStaticRMQ() {
		rmq = new RMQStaticPlusMinusOne();
	}

	@Override
	public LCAStatic.DataStructure preProcessTree(Graph t, int r) {
		if (!Trees.isTree(t, r))
			throw new IllegalArgumentException();

		int n = t.vertices().size();
		int[] depths = new int[n * 2];
		int[] vs = new int[n * 2];
		int[] parent = new int[n];

		EdgeIter[] edges = new EdgeIter[n];

		parent[0] = -1;
		edges[0] = t.edgesOut(r);

		int aLen = 0;
		dfs: for (int u = r, depth = 0;;) {
			depths[aLen] = depth;
			vs[aLen] = u;
			aLen++;

			for (EdgeIter eit = edges[depth]; eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (v == parent[depth])
					continue;
				depth++;
				parent[depth] = u;
				edges[depth] = t.edgesOut(v);
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

		RMQStatic.DataStructure rmqDS = rmq.preProcessSequence(RMQStaticComparator.ofIntArray(depths), depths.length);
		return new DS(vs, vToDepthsIdx, rmqDS);
	}

	private static class DS implements LCAStatic.DataStructure {

		private final int[] vs;
		private final int[] vToDepthsIdx;
		private final RMQStatic.DataStructure rmqDS;

		DS(int[] vs, int[] vToDepthsIdx, RMQStatic.DataStructure rmqDS) {
			this.vs = vs;
			this.vToDepthsIdx = vToDepthsIdx;
			this.rmqDS = rmqDS;
		}

		@Override
		public int findLowestCommonAncestor(int u, int v) {
			int uIdx = vToDepthsIdx[u];
			int vIdx = vToDepthsIdx[v];
			if (uIdx > vIdx) {
				int temp = uIdx;
				uIdx = vIdx;
				vIdx = temp;
			}
			int lcaIdx = rmqDS.findMinimumInRange(uIdx, vIdx);
			return vs[lcaIdx];
		}
	}

}
