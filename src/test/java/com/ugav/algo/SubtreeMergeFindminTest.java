package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class SubtreeMergeFindminTest extends TestUtils {

	@Test
	public static void randOps() {
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096), phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			testRandOps(SubtreeMergeFindminImpl::new, n, m);
		});
	}

	private static enum Op {
		AddLeaf, AddNonTreeEdge, Merge, findMinNonTreeEdge
	}

	private static class TrackerNode {
		final int id;
		final int depth;

		TrackerNode(int id, int depth) {
			this.id = id;
			this.depth = depth;
		}
	}

	@SuppressWarnings({ "boxing", "unchecked" })
	private static void testRandOps(Supplier<? extends SubtreeMergeFindmin<TrackerNode, Integer>> builder, int n,
			int m) {
		if (n < 2)
			throw new IllegalArgumentException();
		Random rand = new Random(nextRandSeed());

		/* generate random ops without the two initial initTree and addLeaf ops */
		Op[] notAddLeafOps = { Op.AddNonTreeEdge, Op.Merge, Op.findMinNonTreeEdge };
		Op[] ops = new Op[n - 2 + m];
		for (int i = 0; i < n - 2 + m; i++)
			ops[i] = i < n - 2 ? Op.AddLeaf : notAddLeafOps[rand.nextInt(notAddLeafOps.length)];
		ops = suffle(ops, nextRandSeed());

		/* append a single add leaf at the start */
		Op[] ops2 = new Op[n - 1 + m];
		System.arraycopy(ops, 0, ops2, 1, ops.length);
		ops2[0] = Op.AddLeaf;
		ops = ops2;

		List<SubtreeMergeFindmin.Node<TrackerNode>> nodes = new ArrayList<>();
		UnionFind uf = new UnionFindArray();
		List<int[]>[] subtreeEdges = new List[n];
		int[] edgeInsertWeights = randPermutation(ops.length, nextRandSeed());
		int edgeInsertWeightsIdx = 0;

		SubtreeMergeFindmin<TrackerNode, Integer> algo = builder.get();
		SubtreeMergeFindmin.Node<TrackerNode> root = algo.initTree(new TrackerNode(uf.make(), 0));
		nodes.add(root);
		subtreeEdges[root.getNodeData().id] = new LinkedList<>();

		for (Op op : ops) {
			switch (op) {
			case AddLeaf: {
				SubtreeMergeFindmin.Node<TrackerNode> p = nodes.get(rand.nextInt(nodes.size()));
				SubtreeMergeFindmin.Node<TrackerNode> node = algo.addLeaf(p,
						new TrackerNode(uf.make(), p.getNodeData().depth + 1));
				nodes.add(node);
				subtreeEdges[node.getNodeData().id] = new LinkedList<>();
				break;
			}
			case AddNonTreeEdge: {
				SubtreeMergeFindmin.Node<TrackerNode> u = nodes.get(rand.nextInt(nodes.size()));
				SubtreeMergeFindmin.Node<TrackerNode> v = nodes.get(rand.nextInt(nodes.size()));
				int weight = edgeInsertWeights[edgeInsertWeightsIdx++];
				algo.addNonTreeEdge(u, v, weight);
				subtreeEdges[uf.find(u.getNodeData().id)]
						.add(new int[] { u.getNodeData().id, v.getNodeData().id, weight });
				subtreeEdges[uf.find(u.getNodeData().id)]
						.add(new int[] { u.getNodeData().id, v.getNodeData().id, weight });
				break;
			}
			case Merge: {
				SubtreeMergeFindmin.Node<TrackerNode> u, v;
				for (;;) {
					u = nodes.get(rand.nextInt(nodes.size()));
					v = nodes.get(rand.nextInt(nodes.size()));
					/* assume u0 is upper */
					SubtreeMergeFindmin.Node<TrackerNode> u0 = u.getNodeData().depth <= v.getNodeData().depth ? u : v;
					SubtreeMergeFindmin.Node<TrackerNode> v0 = u.getNodeData().depth <= v.getNodeData().depth ? v : u;
					SubtreeMergeFindmin.Node<TrackerNode> p;
					for (p = v0; p.getNodeData().depth > u0.getNodeData().depth + 1;)
						p = p.getParent();
					if (uf.find(p.getNodeData().id) == uf.find(v0.getNodeData().id) && p.getParent() == u0)
						break;
				}
				algo.mergeSubTrees(u, v);
				u = nodes.get(uf.find(u.getNodeData().id));
				v = nodes.get(uf.find(v.getNodeData().id));
				if (u == v)
					break;
				int w = uf.union(u.getNodeData().id, v.getNodeData().id);
				for (SubtreeMergeFindmin.Node<TrackerNode> z : new SubtreeMergeFindmin.Node[] { u, v }) {
					for (Iterator<int[]> it = subtreeEdges[z.getNodeData().id].iterator(); it.hasNext();) {
						int[] edge = it.next();
						int eU = edge[0], eV = edge[1];
						if (uf.find(eU) == uf.find(eV))
							it.remove();
					}
				}
				List<int[]> elU = subtreeEdges[u.getNodeData().id], elV = subtreeEdges[v.getNodeData().id];
				List<int[]> elW = new LinkedList<>();
				elW.addAll(elU);
				elW.addAll(elV);
				elU.clear();
				elV.clear();
				subtreeEdges[u.getNodeData().id] = subtreeEdges[v.getNodeData().id] = null;
				subtreeEdges[w] = elW;
				break;
			}
			case findMinNonTreeEdge: {
				int[] min = null;
				boolean[] visited = new boolean[nodes.size()];
				for (int v = 0; v < nodes.size(); v++) {
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

				SubtreeMergeFindmin.MinEdge<TrackerNode, Integer> actual0 = algo.findMinNonTreeEdge();
				int[] expected = min;
				int[] actual = actual0 == null ? null
						: new int[] { actual0.u().getNodeData().id, actual0.v().getNodeData().id, actual0.edgeData() };
				if (expected != null && actual != null && expected[0] != actual[0]) {
					int temp = expected[0];
					expected[0] = expected[1];
					expected[1] = temp;
				}
				assertTrue(Arrays.equals(expected, actual), "Algo found unexpected edge: ", Arrays.toString(expected),
						" != ", Arrays.toString(actual), "\n");
				break;
			}
			default:
				throw new InternalError();
			}
		}
	}

}
