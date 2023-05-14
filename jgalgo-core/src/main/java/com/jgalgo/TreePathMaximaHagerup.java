/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;
import com.jgalgo.Utils.BiInt2IntFunction;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

/**
 * Hagerup's Tree Path Maxima (TPM) linear time algorithm.
 * <p>
 * The algorithm runs in \(O(n + m)\) where \(n\) is the number of vertices in the tree and \(m\) is the number of
 * queries. It also uses \(O(n + m)\) space.
 * <p>
 * Based on 'Linear verification for spanning trees' by J Komlos (1985), 'A Simpler Minimum Spanning Tree Verification
 * Algorithm' by V King (1997) and 'An Even Simpler Linear-Time Algorithm for Verifying Minimum Spanning Trees' by T
 * Hagerup (2009).
 *
 * @author Barak Ugav
 */
class TreePathMaximaHagerup implements TreePathMaxima {

	private boolean useBitsLookupTables = false;

	private static final Object EdgeRefWeightKey = new Object();

	/**
	 * Create a new TPM object.
	 */
	TreePathMaximaHagerup() {}

	/**
	 * Enable/disable the use of bits lookup tables.
	 * <p>
	 * Some operations on integers such such as popcount ({@link Integer#bitCount(int)}) or ctz
	 * ({@link Integer#numberOfTrailingZeros(int)}) are assumed to be implemented in \(O(1)\) by the algorithm.
	 * According to theoretical papers its possible to implement this operations in 'real' \(O(1)\) with lookup tables.
	 * In practice, integers are 32bit numbers and all such operations are fast without any lookup tables.
	 * <p>
	 * This method enable or disable the use of bits lookup tables.
	 *
	 * @see          BitsLookupTable
	 * @param enable if {@code true} bits lookup table will be constructed and used, else methods from {@link Integer}
	 *                   will be used.
	 */
	void setBitsLookupTablesEnable(boolean enable) {
		useBitsLookupTables = enable;
	}

	@Override
	public TreePathMaxima.Result computeHeaviestEdgeInTreePaths(Graph tree, EdgeWeightFunc w,
			TreePathMaxima.Queries queries) {
		ArgumentCheck.onlyUndirected(tree);
		if (!Trees.isTree(tree))
			throw new IllegalArgumentException("only trees are supported");
		return new Worker(tree, w, useBitsLookupTables).calcTPM(queries);
	}

	private static class Worker {

		/*
		 * Original tree, in other functions 't' refers to the Boruvka fully branching tree
		 */
		final Graph tOrig;
		final EdgeWeightFunc w;
		private final Int2IntFunction getBitCount;
		private final BiInt2IntFunction getIthbit;
		private final Int2IntFunction getNumberOfTrailingZeros;

		Worker(Graph t, EdgeWeightFunc w, boolean useBitsLookupTables) {
			this.tOrig = t;
			this.w = w;

			if (useBitsLookupTables) {
				int n = t.vertices().size();
				int wordsize = n > 1 ? Utils.log2ceil(n) : 1;
				BitsLookupTable.Count count = new BitsLookupTable.Count(wordsize);
				BitsLookupTable.Ith ith = new BitsLookupTable.Ith(wordsize, count);

				getBitCount = count::bitCount;
				getIthbit = ith::ithBit;
				getNumberOfTrailingZeros = ith::numberOfTrailingZeros;
			} else {
				getBitCount = Integer::bitCount;
				getIthbit = (x, i) -> {
					if (i < 0 || i >= getBitCount.applyAsInt(x))
						throw new IndexOutOfBoundsException(Integer.toBinaryString(x) + "[" + i + "]");
					for (; i > 0; i--) {
						int z = Integer.numberOfTrailingZeros(x);
						x &= ~(1 << z);
					}
					return Integer.numberOfTrailingZeros(x);
				};
				getNumberOfTrailingZeros = Integer::numberOfTrailingZeros;
			}
		}

		TreePathMaxima.Result calcTPM(TreePathMaxima.Queries queries) {
			ObjectIntPair<Graph> r = buildBoruvkaFullyBranchingTree();
			Graph tree = r.first();
			int root = r.secondInt();

			int[] lcaQueries = splitQueriesIntoLCAQueries(tree, root, queries);

			Pair<int[], int[]> r2 = getEdgeToParentsAndDepth(tree, root);
			int[] edgeToParent = r2.first();
			int[] depths = r2.second();

			int[] q = calcQueriesPerVertex(tree, lcaQueries, depths, edgeToParent);
			int[][] a = calcAnswersPerVertex(tree, root, q, edgeToParent);
			return extractEdgesFromAnswers(a, q, lcaQueries, depths, tree.getEdgesWeights("edgeData"));
		}

		private TreePathMaxima.Result extractEdgesFromAnswers(int[][] a, int[] q, int[] lcaQueries, int[] depths,
				Weights.Int edgeData) {
			int queriesNum = lcaQueries.length / 4;
			int[] res = new int[queriesNum];

			for (int i = 0; i < queriesNum; i++) {
				int u = lcaQueries[i * 4];
				int v = lcaQueries[i * 4 + 2];
				int lca = lcaQueries[i * 4 + 1];
				int lcaDepth = depths[lca];

				int ua = -1, va = -1;

				int qusize = getBitCount.applyAsInt(q[u]);
				for (int j = 0; j < qusize; j++) {
					if (getIthbit.apply(q[u], j) == lcaDepth) {
						ua = a[u][j];
						break;
					}
				}
				int qvsize = getBitCount.applyAsInt(q[v]);
				for (int j = 0; j < qvsize; j++) {
					if (getIthbit.apply(q[v], j) == lcaDepth) {
						va = a[v][j];
						break;
					}
				}

				res[i] = (va == -1 || (ua != -1 && w.weight(edgeData.getInt(ua)) >= w.weight(edgeData.getInt(va))))
						? (ua != -1 ? edgeData.getInt(ua) : -1)
						: /* va != -1 */ edgeData.getInt(va);
			}

			return new Result(res);
		}

		private int[][] calcAnswersPerVertex(Graph t, int root, int[] q, int[] edgeToParent) {
			int n = t.vertices().size();
			int[] a = new int[n];

			int leavesDepth = GraphsUtils.getFullyBranchingTreeDepth(t, root);

			Weights.Int tData = t.getEdgesWeights("edgeData");
			int[][] res = new int[tOrig.vertices().size()][];

			for (DFSIter it = new DFSIter(t, root); it.hasNext();) {
				int v = it.nextInt();
				IntList edgesFromRoot = it.edgePath();
				if (edgesFromRoot.isEmpty())
					continue;
				int depth = edgesFromRoot.size();
				int edgeToChild = edgesFromRoot.getInt(depth - 1);
				int u = t.edgeEndpoint(edgeToChild, v);

				a[v] = subseq(a[u], q[u], q[v]);
				int j = binarySearch(a[v], w.weight(tData.getInt(edgeToChild)), edgesFromRoot, tData);
				a[v] = repSuf(a[v], depth, j);

				if (depth == leavesDepth) {
					int qvsize = getBitCount.applyAsInt(q[v]);
					int[] resv = new int[qvsize];
					for (int i = 0; i < qvsize; i++) {
						int b = getIthbit.apply(q[v], i);
						int s = getNumberOfTrailingZeros.applyAsInt(successor(a[v], 1 << b) >> 1);
						resv[i] = edgesFromRoot.getInt(s);
					}
					res[v] = resv;
				}
			}
			return res;
		}

		private static int successor(int a, int b) {
			// int r = 0, bsize = Integer.bitCount(b);
			// for (int i = 0; i < bsize; i++)
			// for (int bit = getIthOneBit(b, i) + 1; bit < Integer.SIZE; bit++)
			// if ((a & (1 << bit)) != 0) {
			// r |= 1 << bit;
			// break;
			// }
			// return r;

			/*
			 * Don't even ask why the commented code above is equivalent to the bit tricks below. Hagerup 2009.
			 */
			return a & (~(a | b) ^ ((~a | b) + b));
		}

		private static int subseq(int au, int qu, int qv) {
			return successor(au, qv);
		}

		private int binarySearch(int av, double weight, IntList edgesToRoot, Weights.Int edgeData) {
			int avsize = getBitCount.applyAsInt(av);
			if (avsize == 0 || w.weight(edgeData.getInt(edgesToRoot.getInt(getIthbit.apply(av, 0) - 1))) < weight)
				return 0;

			for (int from = 0, to = avsize;;) {
				if (from == to - 1)
					return getIthbit.apply(av, from) + 1;
				int mid = (from + to) / 2;
				int avi = getIthbit.apply(av, mid);
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

		private ObjectIntPair<Graph> buildBoruvkaFullyBranchingTree() {
			int n = tOrig.vertices().size();
			int[] minEdges = new int[n];
			double[] minGraphWeights = new double[n];
			int[] vNext = new int[n];
			int[] path = new int[n];
			int[] vTv = new int[n];
			int[] vTvNext = new int[n];

			for (int v = 0; v < n; v++)
				vTv[v] = v;

			Graph t = GraphBuilder.newUndirected().setVerticesNum(n).build();
			Weights.Int tData = t.addEdgesWeights("edgeData", int.class, Integer.valueOf(-1));
			for (Graph G = GraphsUtils.referenceGraph(tOrig, EdgeRefWeightKey); (n = G.vertices().size()) > 1;) {
				Weights.Int GData = G.getEdgesWeights(EdgeRefWeightKey);

				// Find minimum edge of each vertex
				Arrays.fill(minEdges, 0, n, -1);
				Arrays.fill(minGraphWeights, 0, n, Double.MAX_VALUE);
				for (int u = 0; u < n; u++) {
					for (EdgeIter eit = G.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						double eWeight = w.weight(GData.getInt(e));
						if (eWeight < minGraphWeights[u]) {
							minEdges[u] = e;
							minGraphWeights[u] = eWeight;
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

							p = G.edgeEndpoint(minEdges[p], p);
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
					vTvNext[V] = t.addVertex();
				for (int u = 0; u < n; u++) {
					int e = t.addEdge(vTv[u], vTvNext[vNext[u]]);
					tData.set(e, GData.getInt(minEdges[u]));
				}
				int[] temp = vTv;
				vTv = vTvNext;
				vTvNext = temp;

				// contract G to new graph with the super vertices
				Graph gNext = GraphBuilder.newUndirected().setVerticesNum(nNext).build();
				Weights.Int gNextData = gNext.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
				for (int u = 0; u < n; u++) {
					int U = vNext[u];
					for (EdgeIter eit = G.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int V = vNext[eit.target()];
						if (U != V) {
							int E = gNext.addEdge(U, V);
							gNextData.set(E, GData.getInt(e));
						}
					}
				}

				G.clear();
				G = gNext;
			}
			return ObjectIntPair.of(t, vTv[0]);
		}

		private static int[] splitQueriesIntoLCAQueries(Graph t, int root, TreePathMaxima.Queries queries) {
			int queriesNum = queries.size();
			int[] lcaQueries = new int[queriesNum * 4];

			LCAStatic lcaAlgo = new LCAStaticRMQ();
			LCAStatic.DataStructure lcaDS = lcaAlgo.preProcessTree(t, root);
			for (int q = 0; q < queriesNum; q++) {
				IntIntPair query = queries.getQuery(q);
				int u = query.firstInt(), v = query.secondInt();
				int lca = lcaDS.findLowestCommonAncestor(u, v);
				lcaQueries[q * 4] = u;
				lcaQueries[q * 4 + 1] = lca;
				lcaQueries[q * 4 + 2] = v;
				lcaQueries[q * 4 + 3] = lca;
			}
			return lcaQueries;
		}

		private static Pair<int[], int[]> getEdgeToParentsAndDepth(Graph t, int root) {
			int n = t.vertices().size();
			int[] edgeToParent = new int[n];
			Arrays.fill(edgeToParent, -1);
			int[] depths = new int[n];

			for (BFSIter it = new BFSIter(t, root); it.hasNext();) {
				int v = it.nextInt();
				int e = it.inEdge();
				if (e != -1) {
					edgeToParent[v] = e;
					depths[v] = depths[t.edgeEndpoint(e, v)] + 1;
				}
			}

			return Pair.of(edgeToParent, depths);
		}

		private static int[] calcQueriesPerVertex(Graph g, int[] lcaQueries, int[] depths, int[] edgeToParent) {
			final int n = g.vertices().size();

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

			/* Start traversing the full branching tree from the leaves upwards */
			int maxDepth = -1;
			for (int u = 0; u < n; u++)
				if (depths[u] > maxDepth)
					maxDepth = depths[u];
			IntPriorityQueue queue = new IntArrayFIFOQueue();
			BitSet queued = new BitSet(n);
			for (int u = 0; u < n; u++) {
				if (depths[u] == maxDepth) {
					queue.enqueue(u);
					queued.set(u);
				}
			}

			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();

				int ep = edgeToParent[u];
				if (ep == -1)
					continue;
				int parent = g.edgeEndpoint(ep, u);
				q[parent] |= q[u] & ~(1 << depths[parent]);

				if (queued.get(parent))
					continue;
				queue.enqueue(parent);
				queued.set(parent);
			}

			return q;
		}

	}

	private static class Result implements TreePathMaxima.Result {

		private final int[] res;

		Result(int[] res) {
			this.res = res;
		}

		@Override
		public int getHeaviestEdge(int queryIdx) {
			return res[queryIdx];
		}

		@Override
		public int size() {
			return res.length;
		}

	}

}
