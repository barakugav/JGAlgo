package com.jgalgo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.jgalgo.SubtreeMergeFindMin2;
import com.jgalgo.SubtreeMergeFindMinImpl2;
import com.jgalgo.UnionFind;
import com.jgalgo.UnionFindArray;

import it.unimi.dsi.fastutil.objects.ObjectArrays;

public class SubtreeMergeFindminTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0x08f45606b1a84c66L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096), phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			testRandOps(SubtreeMergeFindMinImpl2::new, n, m, seedGen.nextSeed());
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

	private static int getNodeId(SubtreeMergeFindMin2.Node node) {
		return node.<TrackerNode>getNodeData().id;
	}

	@SuppressWarnings({ "boxing", "unchecked" })
	private static void testRandOps(Supplier<? extends SubtreeMergeFindMin2<Integer>> builder, int n, int m, long seed) {
		if (n < 2)
			throw new IllegalArgumentException();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		/* generate random ops without the two initial initTree and addLeaf ops */
		Op[] notAddLeafOps = { Op.AddNonTreeEdge, Op.Merge, Op.findMinNonTreeEdge };
		Op[] ops = new Op[n - 2 + m];
		for (int i = 0; i < n - 2 + m; i++)
			ops[i] = i < n - 2 ? Op.AddLeaf : notAddLeafOps[rand.nextInt(notAddLeafOps.length)];
		ObjectArrays.shuffle(ops, rand);

		/* append a single add leaf at the start */
		Op[] ops2 = new Op[n - 1 + m];
		System.arraycopy(ops, 0, ops2, 1, ops.length);
		ops2[0] = Op.AddLeaf;
		ops = ops2;

		List<SubtreeMergeFindMin2.Node> nodes = new ArrayList<>();
		UnionFind uf = new UnionFindArray();
		List<int[]>[] subtreeEdges = new List[n];
		int[] edgeInsertWeights = randPermutation(ops.length, seedGen.nextSeed());
		int edgeInsertWeightsIdx = 0;

		SubtreeMergeFindMin2<Integer> algo = builder.get();

		SubtreeMergeFindMin2.Node root = algo.initTree();
		root.setNodeData(new TrackerNode(uf.make(), 0));
		nodes.add(root);
		subtreeEdges[getNodeId(root)] = new LinkedList<>();

		for (Op op : ops) {
			switch (op) {
				case AddLeaf: {
					SubtreeMergeFindMin2.Node p = nodes.get(rand.nextInt(nodes.size()));
					SubtreeMergeFindMin2.Node node = algo.addLeaf(p);
					node.setNodeData(new TrackerNode(uf.make(), p.<TrackerNode>getNodeData().depth + 1));
					nodes.add(node);
					subtreeEdges[getNodeId(node)] = new LinkedList<>();
					break;
				}
				case AddNonTreeEdge: {
					SubtreeMergeFindMin2.Node u = nodes.get(rand.nextInt(nodes.size()));
					SubtreeMergeFindMin2.Node v = nodes.get(rand.nextInt(nodes.size()));
					int weight = edgeInsertWeights[edgeInsertWeightsIdx++];
					algo.addNonTreeEdge(u, v, weight);
					subtreeEdges[uf.find(getNodeId(u))].add(new int[] { getNodeId(u), getNodeId(v), weight });
					subtreeEdges[uf.find(getNodeId(u))].add(new int[] { getNodeId(u), getNodeId(v), weight });
					break;
				}
				case Merge: {
					SubtreeMergeFindMin2.Node u, v;
					for (;;) {
						u = nodes.get(rand.nextInt(nodes.size()));
						v = nodes.get(rand.nextInt(nodes.size()));
						/* assume u0 is upper */
						SubtreeMergeFindMin2.Node u0 = u.<TrackerNode>getNodeData().depth <= v
								.<TrackerNode>getNodeData().depth ? u : v;
						SubtreeMergeFindMin2.Node v0 = u.<TrackerNode>getNodeData().depth <= v
								.<TrackerNode>getNodeData().depth ? v : u;
						SubtreeMergeFindMin2.Node p;
						for (p = v0; p.<TrackerNode>getNodeData().depth > u0.<TrackerNode>getNodeData().depth + 1;)
							p = p.getParent();
						if (uf.find(getNodeId(p)) == uf.find(getNodeId(v0)) && p.getParent() == u0)
							break;
					}
					algo.mergeSubTrees(u, v);
					u = nodes.get(uf.find(getNodeId(u)));
					v = nodes.get(uf.find(getNodeId(v)));
					if (u == v)
						break;
					int w = uf.union(getNodeId(u), getNodeId(v));
					for (SubtreeMergeFindMin2.Node z : new SubtreeMergeFindMin2.Node[] { u, v }) {
						for (Iterator<int[]> it = subtreeEdges[getNodeId(z)].iterator(); it.hasNext();) {
							int[] edge = it.next();
							int eU = edge[0], eV = edge[1];
							if (uf.find(eU) == uf.find(eV))
								it.remove();
						}
					}
					List<int[]> elU = subtreeEdges[getNodeId(u)], elV = subtreeEdges[getNodeId(v)];
					List<int[]> elW = new LinkedList<>();
					elW.addAll(elU);
					elW.addAll(elV);
					elU.clear();
					elV.clear();
					subtreeEdges[getNodeId(u)] = subtreeEdges[getNodeId(v)] = null;
					subtreeEdges[w] = elW;
					break;
				}
				case findMinNonTreeEdge: {
					int[] min = null;
					BitSet visited = new BitSet(nodes.size());
					for (int v = 0; v < nodes.size(); v++) {
						int V = uf.find(v);
						if (visited.get(V))
							continue;
						for (Iterator<int[]> it = subtreeEdges[V].iterator(); it.hasNext();) {
							int[] edge = it.next();
							int eU = edge[0], eV = edge[1], eW = edge[2];
							if (uf.find(eU) == uf.find(eV))
								it.remove();
							else if (min == null || eW < min[2])
								min = edge;
						}
						visited.set(V);
					}

					SubtreeMergeFindMin2.MinEdge<Integer> actual0 = algo.findMinNonTreeEdge();
					int[] expected = min;
					int[] actual = actual0 == null ? null
							: new int[] { getNodeId(actual0.u()), getNodeId(actual0.v()), actual0.edgeData() };
					if (expected != null && actual != null && expected[0] != actual[0]) {
						int temp = expected[0];
						expected[0] = expected[1];
						expected[1] = temp;
					}
					assertTrue(Arrays.equals(expected, actual),
							"Algo found unexpected edge: " + Arrays.toString(expected) + " != "
									+ Arrays.toString(actual));
					break;
				}
				default:
					throw new IllegalStateException();
			}
		}
	}

}
