package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Utils.QueueIntFixSize;

import it.unimi.dsi.fastutil.ints.IntList;

public class TPMKomlos1985King1997Hagerup2009 implements TPM {

	/*
	 * O(m + n) where m is the number of queries
	 */

	public TPMKomlos1985King1997Hagerup2009() {
	}

	@Override
	public int[] calcTPM(Graph t, WeightFunction w, int[] queries, int queriesNum) {
		if (!(t instanceof Graph.Undirected))
			throw new IllegalArgumentException("only undirected graphs are supported");
		if (queries.length / 2 < queriesNum)
			throw new IllegalArgumentException("queries should be in format [u0, v0, u1, v1, ...]");
		if (!Graphs.isTree(t))
			throw new IllegalArgumentException("only trees are supported");
		if (t.vertices() == 0)
			return new int[queriesNum];
		return new Worker((Graph.Undirected) t, w).calcTPM(queries, queriesNum);
	}

	private static class BitsTable {
		private final BitsLookupTable.Count count;
		private final BitsLookupTable.Ith ith;

		BitsTable(int wordsize) {
			count = new BitsLookupTable.Count(wordsize);
			ith = new BitsLookupTable.Ith(wordsize, count);
		}

		void init() {
			count.init();
			ith.init();
		}
	}

	private static class Worker {

		/*
		 * Original tree, in other functions 't' refers to the Boruvka fully branching
		 * tree
		 */
		final Graph.Undirected tOrig;
		final WeightFunction w;
		final BitsTable bitsTable;

		Worker(Graph.Undirected t, WeightFunction w) {
			this.tOrig = t;
			this.w = w;

			int n = t.vertices();
			bitsTable = new BitsTable(n > 1 ? Utils.log2ceil(n) : 1);
			bitsTable.init();
		}

		int[] calcTPM(int[] queries, int queriesNum) {
			Pair<Graph.Undirected, Integer> r = buildBoruvkaFullyBranchingTree();
			Graph.Undirected t = r.e1;
			int root = r.e2.intValue();

			int[] lcaQueries = splitQueriesIntoLCAQueries(t, root, queries, queriesNum);

			Pair<int[], int[]> r2 = getEdgeToParentsAndDepth(t, root);
			int[] edgeToParent = r2.e1;
			int[] depths = r2.e2;

			int[] q = calcQueriesPerVertex(t, lcaQueries, depths, edgeToParent);
			int[][] a = calcAnswersPerVertex(t, root, q, edgeToParent);
			return extractEdgesFromAnswers(a, q, lcaQueries, depths, t.getEdgeData("edgeData"));
		}

		private int[] extractEdgesFromAnswers(int[][] a, int[] q, int[] lcaQueries, int[] depths,
				EdgeData.Int edgeData) {
			int queriesNum = lcaQueries.length / 4;
			int[] res = new int[queriesNum];

			for (int i = 0; i < queriesNum; i++) {
				int u = lcaQueries[i * 4];
				int v = lcaQueries[i * 4 + 2];
				int lca = lcaQueries[i * 4 + 1];
				int lcaDepth = depths[lca];

				int ua = -1, va = -1;

				int qusize = bitsTable.count.bitCount(q[u]);
				for (int j = 0; j < qusize; j++) {
					if (bitsTable.ith.ithBit(q[u], j) == lcaDepth) {
						ua = a[u][j];
						break;
					}
				}
				int qvsize = bitsTable.count.bitCount(q[v]);
				for (int j = 0; j < qvsize; j++) {
					if (bitsTable.ith.ithBit(q[v], j) == lcaDepth) {
						va = a[v][j];
						break;
					}
				}

				res[i] = (va == -1 || (ua != -1 && w.weight(edgeData.getInt(ua)) >= w.weight(edgeData.getInt(va))))
						? (ua != -1 ? edgeData.getInt(ua) : -1)
						: (va != -1 ? edgeData.getInt(va) : -1);
			}

			return res;
		}

		private int[][] calcAnswersPerVertex(Graph.Undirected t, int root, int[] q, int[] edgeToParent) {
			int n = t.vertices();
			int[] a = new int[n];

			int leavesDepth = Graphs.getFullyBranchingTreeDepth(t, root);

			EdgeData.Int tData = t.getEdgeData("edgeData");
			int[][] res = new int[tOrig.vertices()][];

			Graphs.runDFS(t, root, (v, edgesFromRoot) -> {
				if (edgesFromRoot.isEmpty())
					return true;
				int depth = edgesFromRoot.size();
				int edgeToChild = edgesFromRoot.getInt(depth - 1);
				int u = t.getEdgeEndpoint(edgeToChild, v);

				a[v] = subseq(a[u], q[u], q[v]);
				int j = binarySearch(a[v], w.weight(tData.getInt(edgeToChild)), edgesFromRoot, tData);
				a[v] = repSuf(a[v], depth, j);

				if (depth == leavesDepth) {
					int qvsize = bitsTable.count.bitCount(q[v]);
					int[] resv = new int[qvsize];
					for (int i = 0; i < qvsize; i++) {
						int b = bitsTable.ith.ithBit(q[v], i);
						int s = bitsTable.ith.numberOfTrailingZeros(successor(a[v], 1 << b) >> 1);
						resv[i] = edgesFromRoot.getInt(s);
					}
					res[v] = resv;
				}
				return true;
			});
			return res;
		}

		private static int successor(int a, int b) {
//		int r = 0, bsize = Integer.bitCount(b);
//		for (int i = 0; i < bsize; i++)
//			for (int bit = getIthOneBit(b, i) + 1; bit < Integer.SIZE; bit++)
//				if ((a & (1 << bit)) != 0) {
//					r |= 1 << bit;
//					break;
//				}
//		return r;

			/*
			 * Don't even ask why the commented code above is equivalent to the bit tricks
			 * below. Hagerup 2009.
			 */
			return a & (~(a | b) ^ ((~a | b) + b));
		}

		private static int subseq(int au, int qu, int qv) {
			return successor(au, qv);
		}

		private int binarySearch(int av, double weight, IntList edgesToRoot, EdgeData.Int edgeData) {
			int avsize = bitsTable.count.bitCount(av);
			if (avsize == 0 || w.weight(edgeData.getInt(edgesToRoot.getInt(bitsTable.ith.ithBit(av, 0) - 1))) < weight)
				return 0;

			for (int from = 0, to = avsize;;) {
				if (from == to - 1)
					return bitsTable.ith.ithBit(av, from) + 1;
				int mid = (from + to) / 2;
				int avi = bitsTable.ith.ithBit(av, mid);
				if (w.weight(edgeData.getInt(edgesToRoot.getInt(avi - 1))) >= weight)
					from = mid;
				else
					to = mid;
			}
		}

		private static int repSuf(int av, int depth, int j) {
			av &= (1 << j) - 1;
			av |= 1 << depth;
			return av;
		}

		private Pair<Graph.Undirected, Integer> buildBoruvkaFullyBranchingTree() {
			int n = tOrig.vertices();
			int[] minEdges = new int[n];
			double[] minEdgesWeight = new double[n];
			int[] vNext = new int[n];
			int[] path = new int[n];
			int[] vTv = new int[n];
			int[] vTvNext = new int[n];

			for (int v = 0; v < n; v++)
				vTv[v] = v;

			Graph.Undirected t = new GraphArrayUndirected(n);
			EdgeData.Int tData = t.newEdgeDataInt("edgeData");
			for (Graph.Undirected G = Graphs.referenceGraph(tOrig, "edgeRef"); (n = G.vertices()) > 1;) {
				EdgeData.Int GData = G.getEdgeData("edgeRef");

				// Find minimum edge of each vertex
				Arrays.fill(minEdges, 0, n, -1);
				Arrays.fill(minEdgesWeight, 0, n, Double.MAX_VALUE);
				for (int u = 0; u < n; u++) {
					for (EdgeIter eit = G.edges(u); eit.hasNext();) {
						int e = eit.nextInt();
						double eWeight = w.weight(e);
						if (eWeight < minEdgesWeight[u]) {
							minEdges[u] = e;
							minEdgesWeight[u] = eWeight;
						}
					}
				}

				// find connectivity components, and label each vertex with new super vertex
				final int UNVISITED = -1;
				final int IN_PATH = -2;
				Arrays.fill(vNext, 0, n, UNVISITED);
				int nNext = 0;
				for (int u = 0; u < n; u++) {
					int pathLength = 0;
					// find all reachable vertices from u
					for (int p = u;;) {
						if (vNext[p] == UNVISITED) {
							// another vertex on the path, continue
							path[pathLength++] = p;
							vNext[p] = IN_PATH;

							p = G.getEdgeEndpoint(minEdges[p], p);
							continue;
						}

						// if found label use it label, else - add new label
						int V = vNext[p] >= 0 ? vNext[p] : nNext++;
						// assign the new label to all trees on path
						while (pathLength-- > 0)
							vNext[path[pathLength]] = V;
						break;
					}
				}

				// construct new layer in the output tree graph
				for (int V = 0; V < nNext; V++)
					vTvNext[V] = t.newVertex();
				for (int u = 0; u < n; u++) {
					int e = t.addEdge(vTv[u], vTvNext[vNext[u]]);
					tData.set(e, GData.getInt(minEdges[u]));
				}
				int[] temp = vTv;
				vTv = vTvNext;
				vTvNext = temp;

				// contract G to new graph with the super vertices
				Graph.Undirected gNext = new GraphArrayUndirected(nNext);
				EdgeData.Int gNextData = G.newEdgeDataInt("edgeRef");
				for (int u = 0; u < n; u++) {
					int U = vNext[u];
					for (EdgeIter eit = G.edges(u); eit.hasNext();) {
						int e = eit.nextInt();
						int V = vNext[eit.v()];
						if (U != V) {
							int E = gNext.addEdge(U, V);
							gNextData.set(E, GData.get(e));
						}
					}
				}

				G.clear();
				G = gNext;
			}
			return Pair.of(t, Integer.valueOf(vTv[0]));
		}

		private static int[] splitQueriesIntoLCAQueries(Graph.Undirected t, int root, int[] queries, int queriesNum) {
			int[] lcaQueries = new int[queriesNum * 4];

			LCAStatic lcaAlgo = new LCARMQBenderFarachColton2000();
			lcaAlgo.preprocessLCA(t, root);
			for (int q = 0; q < queriesNum; q++) {
				int u = queries[q * 2], v = queries[q * 2 + 1];
				int lca = lcaAlgo.calcLCA(u, v);
				lcaQueries[q * 4] = u;
				lcaQueries[q * 4 + 1] = lca;
				lcaQueries[q * 4 + 2] = v;
				lcaQueries[q * 4 + 3] = lca;
			}
			return lcaQueries;
		}

		private static Pair<int[], int[]> getEdgeToParentsAndDepth(Graph.Undirected t, int root) {
			int n = t.vertices();
			int[] edgeToParent = new int[n];
			Arrays.fill(edgeToParent, -1);
			int[] depths = new int[n];

			Graphs.runBFS(t, root, (v, e) -> {
				if (e != -1) {
					edgeToParent[v] = e;
					depths[v] = depths[t.getEdgeEndpoint(e, v)] + 1;
				}
				return true;
			});

			return Pair.of(edgeToParent, depths);
		}

		private static int[] calcQueriesPerVertex(Graph.Undirected g, int[] lcaQueries, int[] depths,
				int[] edgeToParent) {
			int n = edgeToParent.length;

			int[] q = new int[n];
			Arrays.fill(q, 0);

			int queriesNum = lcaQueries.length / 2;
			for (int query = 0; query < queriesNum; query++) {
				int u = lcaQueries[query * 2];
				int ancestor = lcaQueries[query * 2 + 1];
				if (u == ancestor)
					continue;
				q[u] |= 1 << depths[ancestor];
			}

			QueueIntFixSize queue = new QueueIntFixSize(n);
			boolean[] queued = new boolean[n];
			for (int u = 0; u < n; u++) {
				queue.push(u);
				queued[u] = true;
			}

			while (!queue.isEmpty()) {
				int u = queue.pop();

				int ep = edgeToParent[u];
				if (ep == -1)
					continue;
				int parent = g.getEdgeEndpoint(ep, u);
				q[parent] |= q[u] & ~(1 << depths[parent]);

				if (queued[parent])
					continue;
				queue.push(parent);
				queued[parent] = true;
			}

			return q;
		}

	}

}
