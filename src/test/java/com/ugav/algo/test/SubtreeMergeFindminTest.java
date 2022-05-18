package com.ugav.algo.test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.ugav.algo.SubtreeMergeFindmin;
import com.ugav.algo.UnionFind;
import com.ugav.algo.UnionFindArray;

public class SubtreeMergeFindminTest extends TestUtils {

	@Test
	public static boolean randOps() {
		initTestRand(0);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096), phase(1, 4096, 16384));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			int m = args[1];
			return testRandOps(SubtreeMergeFindmin::new, n, m);
		});
	}

	private static enum Op {
		AddLeaf, AddNonTreeEdge, Merge, findMinNonTreeEdge
	}

	@SuppressWarnings({ "boxing", "unchecked" })
	private static boolean testRandOps(Supplier<? extends SubtreeMergeFindmin<Integer>> builder, int n, int m) {
		if (n < 2)
			throw new IllegalArgumentException();
		Random rand = new Random(nextRandSeed());

		/* generate random ops without the two initial initTree and addLeaf ops */
		Op[] notAddLeafOps = { Op.AddNonTreeEdge, Op.Merge, Op.findMinNonTreeEdge };
		Op[] ops = new Op[n - 2 + m];
		for (int i = 0; i < n - 2 + m; i++)
			ops[i] = i < n - 2 ? Op.AddLeaf : notAddLeafOps[rand.nextInt(notAddLeafOps.length)];
		ops = Utils.suffle(ops, nextRandSeed());

		/* append a single add leaf at the start */
		Op[] ops2 = new Op[n - 1 + m];
		System.arraycopy(ops, 0, ops2, 1, ops.length);
		ops2[0] = Op.AddLeaf;
		ops = ops2;

		int[] algoLables = new int[n];
		int nodesCount = 0;
		int[] parent = new int[n];
		int[] depth = new int[n];
		UnionFind uf = new UnionFindArray();
		List<int[]>[] subtreeEdges = new List[n];
		int[] edgeInsertWeights = Utils.randPermutation(ops.length, nextRandSeed());
		int edgeInsertWeightsIdx = 0;

		SubtreeMergeFindmin<Integer> algo = builder.get();
		int root = nodesCount++;
		algoLables[root] = algo.initTree();
		parent[root] = -1;
		depth[root] = 0;
		int ufIdx = uf.make();
		assert ufIdx == root;
		subtreeEdges[root] = new LinkedList<>();

		for (Op op : ops) {
			switch (op) {
			case AddLeaf: {
				int p = rand.nextInt(nodesCount);
				int node = nodesCount++;
				algoLables[node] = algo.addLeaf(p);
				parent[node] = p;
				depth[node] = depth[p] + 1;
				ufIdx = uf.make();
				assert ufIdx == node;
				subtreeEdges[ufIdx] = new LinkedList<>();
				break;
			}
			case AddNonTreeEdge: {
				int u = rand.nextInt(nodesCount);
				int v = rand.nextInt(nodesCount);
				int weight = edgeInsertWeights[edgeInsertWeightsIdx++];
				algo.addNonTreeEdge(u, v, weight);
				subtreeEdges[uf.find(u)].add(new int[] { u, v, weight });
				subtreeEdges[uf.find(v)].add(new int[] { u, v, weight });
				break;
			}
			case Merge: {
				int u, v;
				for (;;) {
					u = rand.nextInt(nodesCount);
					v = rand.nextInt(nodesCount);
					/* assume u0 is upper */
					int u0 = depth[u] <= depth[v] ? u : v;
					int v0 = depth[u] <= depth[v] ? v : u;
					int p;
					for (p = v0; depth[p] > depth[u0] + 1;)
						p = parent[p];
					if (uf.find(p) == uf.find(v0) && parent[p] == u0)
						break;
				}
				algo.mergeSubTrees(u, v);
				u = uf.find(u);
				v = uf.find(v);
				if (u == v)
					break;
				int w = uf.union(u, v);
				for (int z : new int[] { u, v }) {
					for (Iterator<int[]> it = subtreeEdges[z].iterator(); it.hasNext();) {
						int[] edge = it.next();
						int eU = edge[0], eV = edge[1];
						if (uf.find(eU) == uf.find(eV))
							it.remove();
					}
				}
				List<int[]> elU = subtreeEdges[u], elV = subtreeEdges[v];
				List<int[]> elW = new LinkedList<>();
				elW.addAll(elU);
				elW.addAll(elV);
				elU.clear();
				elV.clear();
				subtreeEdges[u] = subtreeEdges[v] = null;
				subtreeEdges[w] = elW;
				break;
			}
			case findMinNonTreeEdge: {
				int[] min = null;
				boolean[] visited = new boolean[nodesCount];
				for (int v = 0; v < nodesCount; v++) {
					int V = uf.find(v);
					if (visited[V])
						continue;
					for (Iterator<int[]> it = subtreeEdges[V].iterator(); it.hasNext();) {
						int[] edge = it.next();
						int eU = edge[0], eV = edge[1], eW = edge[2];
						if (uf.find(eU) == uf.find(eV))
							it.remove();
						else if (min == null || eW < min[2])
							min = edge;
					}
					visited[V] = true;
				}

				SubtreeMergeFindmin.Edge<Integer> actual0 = algo.findMinNonTreeEdge();
				int[] expected = min;
				int[] actual = actual0 == null ? null : new int[] { actual0.u, actual0.v, actual0.weight };
				if (expected != null && actual != null && expected[0] != actual[0]) {
					int temp = expected[0];
					expected[0] = expected[1];
					expected[1] = temp;
				}
				if (!Arrays.equals(expected, actual)) {
					printTestStr("Algo found unexpected edge: ", Arrays.toString(expected), " != ",
							Arrays.toString(actual), "\n");
					return false;
				}

				break;
			}
			default:
				throw new InternalError();
			}
		}

		return true;
	}

}
