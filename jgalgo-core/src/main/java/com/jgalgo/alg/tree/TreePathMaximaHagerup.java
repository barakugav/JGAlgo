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

package com.jgalgo.alg.tree;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.alg.traversal.DfsIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.ds.BitsLookupTable;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.JGAlgoUtils.BiInt2IntFunc;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Hagerup's Tree Path Maxima (TPM) linear time algorithm.
 *
 * <p>
 * The algorithm runs in \(O(n + m)\) where \(n\) is the number of vertices in the tree and \(m\) is the number of
 * queries. It also uses \(O(n + m)\) space.
 *
 * <p>
 * Based on 'Linear verification for spanning trees' by J Komlos (1985), 'A Simpler Minimum Spanning Tree Verification
 * Algorithm' by V King (1997) and 'An Even Simpler Linear-Time Algorithm for Verifying Minimum Spanning Trees' by T
 * Hagerup (2009).
 *
 * @author Barak Ugav
 */
class TreePathMaximaHagerup extends TreePathMaximaUtils.AbstractImpl {

	private boolean useBitsLookupTables = false;

	/**
	 * Create a new TPM object.
	 */
	TreePathMaximaHagerup() {}

	/**
	 * Enable/disable the use of bits lookup tables.
	 *
	 * <p>
	 * Some operations on integers such such as popcount ({@link Integer#bitCount(int)}) or ctz
	 * ({@link Integer#numberOfTrailingZeros(int)}) are assumed to be implemented in \(O(1)\) by the algorithm.
	 * According to theoretical papers its possible to implement this operations in 'real' \(O(1)\) with lookup tables.
	 * In practice, integers are 32bit numbers and all such operations are fast without any lookup tables.
	 *
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
	TreePathMaxima.IResult computeHeaviestEdgeInTreePaths(IndexGraph tree, IWeightFunction w,
			TreePathMaxima.IQueries queries) {
		Assertions.onlyUndirected(tree);
		Assertions.onlyTree(tree);
		return new Worker(tree, w, useBitsLookupTables).calcTPM(queries);
	}

	private static class Worker {

		private final IndexGraph tOrig;
		private final IWeightFunction w;

		/* The tree we operate on, actually the Boruvka fully branching tree */
		private final IndexGraph tree;
		private int[] parents;
		private int[] depths;
		private int treeHeight;
		/* Map an edge it 'tree' to an edge in 'tOrig' */
		private final int[] edgeRef;

		private final LowestCommonAncestorStatic lcaAlgo = new LowestCommonAncestorStaticRmq2();
		private int root;

		private final Int2IntFunction getBitCount;
		private final BiInt2IntFunc getIthbit;
		private final Int2IntFunction getNumberOfTrailingZeros;

		Worker(IndexGraph tOrig, IWeightFunction w, boolean useBitsLookupTables) {
			this.tOrig = tOrig;
			this.w = WeightFunctions.localEdgeWeightFunction(tOrig, w);

			if (useBitsLookupTables) {
				int n = tOrig.vertices().size();
				int wordsize = n > 1 ? JGAlgoUtils.log2ceil(n) : 1;
				BitsLookupTable.Count count = new BitsLookupTable.Count(wordsize);
				BitsLookupTable.Ith ith = new BitsLookupTable.Ith(wordsize, count);

				getBitCount = count::bitCount;
				getIthbit = ith::ithBit;
				getNumberOfTrailingZeros = ith::numberOfTrailingZeros;
			} else {
				getBitCount = Integer::bitCount;
				getIthbit = (x, i) -> {
					assert 0 <= i && i < getBitCount.applyAsInt(x);
					for (; i > 0; i--) {
						int z = Integer.numberOfTrailingZeros(x);
						x &= ~(1 << z);
					}
					return Integer.numberOfTrailingZeros(x);
				};
				getNumberOfTrailingZeros = Integer::numberOfTrailingZeros;
			}

			Pair<IndexGraph, int[]> t = buildBoruvkaFullyBranchingTree();
			tree = t.first();
			edgeRef = t.second();
		}

		TreePathMaxima.IResult calcTPM(TreePathMaxima.IQueries queries) {
			int[] lcaQueries = splitQueriesIntoLCAQueries(queries);

			int[] q = calcQueriesPerVertex(lcaQueries);
			int[][] a = calcAnswersPerVertex(q);
			return extractEdgesFromAnswers(a, q, lcaQueries);
		}

		private TreePathMaxima.IResult extractEdgesFromAnswers(int[][] a, int[] q, int[] lcaQueries) {
			int queriesNum = lcaQueries.length / 4;
			int[] res = new int[queriesNum];

			for (int i : range(queriesNum)) {
				int u = lcaQueries[i * 4];
				int v = lcaQueries[i * 4 + 2];
				int lca = lcaQueries[i * 4 + 1];
				int lcaDepth = depths[lca];

				int ua = -1, va = -1;

				int qusize = getBitCount.applyAsInt(q[u]);
				for (int j : range(qusize)) {
					if (getIthbit.apply(q[u], j) == lcaDepth) {
						ua = a[u][j];
						break;
					}
				}
				int qvsize = getBitCount.applyAsInt(q[v]);
				for (int j : range(qvsize)) {
					if (getIthbit.apply(q[v], j) == lcaDepth) {
						va = a[v][j];
						break;
					}
				}

				res[i] = (va < 0 || (ua >= 0 && w.weight(edgeRef[ua]) >= w.weight(edgeRef[va])))
						? (ua >= 0 ? edgeRef[ua] : -1)
						: /* va >= 0 */ edgeRef[va];
			}

			return new TreePathMaximaUtils.ResultImpl(res);
		}

		private int[][] calcAnswersPerVertex(int[] q) {
			int n = tree.vertices().size();
			int[] a = new int[n];
			int[][] res = new int[tOrig.vertices().size()][];

			for (DfsIter.Int it = DfsIter.newInstance(tree, root); it.hasNext();) {
				int v = it.nextInt();
				IntList edgesFromRoot = it.edgePath();
				if (edgesFromRoot.isEmpty())
					continue;
				int depth = edgesFromRoot.size();
				int edgeToChild = edgesFromRoot.getInt(depth - 1);
				int u = tree.edgeEndpoint(edgeToChild, v);

				a[v] = subseq(a[u], q[u], q[v]);
				int j = binarySearch(a[v], w.weight(edgeRef[edgeToChild]), edgesFromRoot);
				a[v] = repSuf(a[v], depth, j);

				if (depth == treeHeight - 1) {
					int qvsize = getBitCount.applyAsInt(q[v]);
					int[] resv = new int[qvsize];
					for (int i : range(qvsize)) {
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
			// for (int i : range(bsize))
			// for (int bit : range(getIthOneBit(b, i) + 1, Integer.SIZE))
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

		private int binarySearch(int av, double weight, IntList edgesToRoot) {
			int avsize = getBitCount.applyAsInt(av);
			if (avsize == 0 || w.weight(edgeRef[edgesToRoot.getInt(getIthbit.apply(av, 0) - 1)]) < weight)
				return 0;

			for (int from = 0, to = avsize;;) {
				if (from == to - 1)
					return getIthbit.apply(av, from) + 1;
				int mid = (from + to) / 2;
				int avi = getIthbit.apply(av, mid);
				if (w.weight(edgeRef[edgesToRoot.getInt(avi - 1)]) >= weight)
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

		private Pair<IndexGraph, int[]> buildBoruvkaFullyBranchingTree() {
			int n = tOrig.vertices().size();
			int[] minEdges = new int[n * 2];
			double[] minEdgesWeight = new double[n];
			int[] vNext = new int[n];
			int[] path = new int[n];
			int[] vTv = new int[n];
			int[] vTvNext = new int[n];

			IntArrayList parents = new IntArrayList();
			IntArrayList depths = new IntArrayList();

			IndexGraphBuilder treeBuilder = IndexGraphBuilder.undirected();
			treeBuilder.ensureVertexCapacity(2 * n);
			treeBuilder.ensureEdgeCapacity(2 * n);
			int[] edgeRef = IntArrays.EMPTY_ARRAY;

			/* Create the deepest n vertices of the full Boruvka tree, each corresponding to an original vertex */
			depths.ensureCapacity(depths.size() + n);
			parents.ensureCapacity(parents.size() + n);
			for (int v : range(n)) {
				int vBuilder = vTv[v] = treeBuilder.addVertexInt();
				assert v == vBuilder;
				depths.add(0);
				parents.add(-1);
			}

			/*
			 * During the iterations of Boruvka, we don't create an actual Graph object to represent the contracted
			 * graph of each iteration. Rather, we just maintain the number of vertices (n) and a list of edges (n-1 of
			 * them). Each edge is stored as a triple (e,u,v), where e is the original edge of tOrig, and u and v are
			 * super vertices in the current iteration. Note that tOrig.edgeSource(e) will not return u or v (only in
			 * the first iteration).
			 */
			assert tOrig.edges().size() == n - 1;
			int[] edges = new int[(n - 1) * 3];
			int[] edgesNext = new int[(n / 2 * 1) * 3];
			int edgesNum = 0;
			for (int e : range(tOrig.edges().size())) {
				int u = tOrig.edgeSource(e);
				int v = tOrig.edgeTarget(e);
				edges[edgesNum * 3 + 0] = e;
				edges[edgesNum * 3 + 1] = u;
				edges[edgesNum * 3 + 2] = v;
				edgesNum++;
			}

			for (int height = 1;; height++) {
				if (n <= 1) {
					treeHeight = height;
					break;
				}
				/* Find the minimum edge of each (super) vertex */
				// Arrays.fill(minEdges, 0, n * 2, -1);
				Arrays.fill(minEdgesWeight, 0, n, Double.MAX_VALUE);
				for (int eIdx : range(edgesNum)) {
					int e = edges[eIdx * 3 + 0];
					int u = edges[eIdx * 3 + 1];
					int v = edges[eIdx * 3 + 2];
					double eWeight = w.weight(e);
					if (eWeight < minEdgesWeight[u]) {
						minEdges[u * 2 + 0] = e;
						minEdges[u * 2 + 1] = v;
						minEdgesWeight[u] = eWeight;
					}
					if (eWeight < minEdgesWeight[v]) {
						minEdges[v * 2 + 0] = e;
						minEdges[v * 2 + 1] = u;
						minEdgesWeight[v] = eWeight;
					}
				}

				/* find connected components, and label each vertex with new super vertex */
				final int UNVISITED = -1;
				final int IN_PATH = -2;
				Arrays.fill(vNext, 0, n, UNVISITED);
				int nNext = 0;
				for (int u : range(n)) {
					/* find all reachable vertices from u */
					for (int w = u, pathLength = 0;;) {
						if (vNext[w] != UNVISITED) {
							/* if found labeled vertex, use it label, else, add a new label */
							int V = vNext[w] >= 0 ? vNext[w] : nNext++;
							/* assign the new label to all vertices on path */
							while (pathLength-- > 0)
								vNext[path[pathLength]] = V;
							break;
						}

						/* another vertex on the path, continue */
						path[pathLength++] = w;
						vNext[w] = IN_PATH;
						w = minEdges[w * 2 + 1];
					}
				}

				/* Construct new layer in the output tree graph */
				depths.ensureCapacity(depths.size() + nNext);
				parents.ensureCapacity(parents.size() + nNext);
				for (int V : range(nNext)) {
					int nextV = treeBuilder.addVertexInt();
					vTvNext[V] = nextV;

					/*
					 * We assign height here instead of depth because we build the tree from bottom to up and we don't
					 * know our current depth. After the whole tree will be built, we will update the depths of the
					 * vertices by depth=(treeHeight-height-1).
					 */
					assert depths.size() == nextV;
					depths.add(height);

					/* Will be set in the next iteration (or not, for root) */
					parents.add(-1);
				}
				for (int u : range(n)) {
					int child = vTv[u];
					int parent = vTvNext[vNext[u]];
					int e = treeBuilder.addEdge(child, parent);
					int eOrig = minEdges[u * 2 + 0];
					if (e == edgeRef.length)
						edgeRef = Arrays.copyOf(edgeRef, Math.max(2, 2 * edgeRef.length));
					edgeRef[e] = eOrig;

					parents.set(child, parent);
				}
				int[] temp = vTv;
				vTv = vTvNext;
				vTvNext = temp;

				/* Construct the contracted graph of the next iteration (we just create an edges list) */
				int edgesNumNext = 0;
				for (int eIdx : range(edgesNum)) {
					int u = edges[eIdx * 3 + 1];
					int v = edges[eIdx * 3 + 2];
					int U = vNext[u], V = vNext[v];
					if (U != V) {
						int e = edges[eIdx * 3 + 0];
						edgesNext[edgesNumNext * 3 + 0] = e;
						edgesNext[edgesNumNext * 3 + 1] = U;
						edgesNext[edgesNumNext * 3 + 2] = V;
						edgesNumNext++;
					}
				}
				temp = edges;
				edges = edgesNext;
				edgesNext = temp;

				edgesNum = edgesNumNext;

				n = nNext;
			}
			root = vTv[0];

			/*
			 * We wrote the HEIGHT to the depths container because we didn't know the depth at the time. 'Reverse'
			 * height to depth:
			 */
			this.parents = parents.elements();
			this.depths = depths.elements();
			n = treeBuilder.vertices().size();
			for (int u : range(n))
				this.depths[u] = treeHeight - this.depths[u] - 1;

			return Pair.of(treeBuilder.build(), edgeRef);
		}

		private int[] splitQueriesIntoLCAQueries(TreePathMaxima.IQueries queries) {
			int queriesNum = queries.size();
			int[] lcaQueries = new int[queriesNum * 4];

			LowestCommonAncestorStatic.IDataStructure lcaDS =
					(LowestCommonAncestorStatic.IDataStructure) lcaAlgo.preProcessTree(tree, Integer.valueOf(root));
			for (int q : range(queriesNum)) {
				int u = queries.getQuerySourceInt(q), v = queries.getQueryTargetInt(q);
				if (u == v)
					throw new IllegalArgumentException(
							"Tree path maxima query can not be composed of two identical vertices");
				int lca = lcaDS.findLca(u, v);
				lcaQueries[q * 4] = u;
				lcaQueries[q * 4 + 1] = lca;
				lcaQueries[q * 4 + 2] = v;
				lcaQueries[q * 4 + 3] = lca;
			}
			return lcaQueries;
		}

		private int[] calcQueriesPerVertex(int[] lcaQueries) {
			final int n = tree.vertices().size();

			int[] q = new int[n];
			Arrays.fill(q, 0);

			int queriesNum = lcaQueries.length / 2;
			for (int query : range(queriesNum)) {
				int u = lcaQueries[query * 2];
				int ancestor = lcaQueries[query * 2 + 1];
				if (u == ancestor)
					continue;
				q[u] |= 1 << depths[ancestor];
			}

			/* Start traversing the full branching tree from the leaves upwards */
			IntPriorityQueue queue = new FIFOQueueIntNoReduce();
			Bitmap queued = new Bitmap(n);
			for (int u : range(n)) {
				if (depths[u] == treeHeight - 1) {
					queue.enqueue(u);
					queued.set(u);
				}
			}

			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();

				int parent = parents[u];
				if (parent < 0)
					continue;
				q[parent] |= q[u] & ~(1 << depths[parent]);

				if (queued.get(parent))
					continue;
				queue.enqueue(parent);
				queued.set(parent);
			}

			return q;
		}

	}

}
