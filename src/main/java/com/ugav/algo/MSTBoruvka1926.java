package com.ugav.algo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;

public class MSTBoruvka1926 implements MST {

	/*
	 * O(m log n)
	 */

	public MSTBoruvka1926() {
	}

	@Override
	public IntCollection calcMST(Graph g, EdgeWeightFunc w) {
		return calcMST0(g, w, Integer.MAX_VALUE).e3;
	}

	static <E, R> Pair<UGraph, IntCollection> runBoruvka(Graph g, EdgeWeightFunc w, int numberOfRounds,
			Int2ObjectFunction<R> edgeValAssigner, String edgeValKey) {
		if (numberOfRounds <= 0)
			throw new IllegalArgumentException();
		Triple<int[], Integer, IntCollection> r = calcMST0(g, w, numberOfRounds);
		int[] tree = r.e1;
		int treeNum = r.e2.intValue();
		IntCollection mstEdges = r.e3;

		UGraph contractedG = new GraphArrayUndirected(treeNum);
		EdgesWeight<R> contractedGData = contractedG.newEdgeWeight(edgeValKey);
		int m = g.edgesNum();
		for (int e = 0; e < m; e++) {
			int u = tree[g.edgeSource(e)];
			int v = tree[g.edgeTarget(e)];
			if (u == v)
				continue;
			int ne = contractedG.addEdge(u, v);
			contractedGData.set(ne, edgeValAssigner.apply(e));
		}
		return Pair.of(contractedG, mstEdges);
	}

	private static Triple<int[], Integer, IntCollection> calcMST0(Graph g0, EdgeWeightFunc w, int numberOfRounds) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		UGraph g = (UGraph) g0;
		int n = g.verticesNum();

		int treeNum = n;
		int[] vTree = new int[n];
		int[] vTreeNext = new int[n];
		for (int v = 0; v < n; v++)
			vTree[v] = v;

		int[] minEdges = new int[n];
		Arrays.fill(minEdges, -1);
		double[] minEdgesWeight = new double[n];
		int[] path = new int[n];

		IntCollection mst = new IntArrayList();
		for (int i = 0; i < numberOfRounds; i++) {
			Arrays.fill(minEdgesWeight, 0, treeNum, Double.MAX_VALUE);

			/* find minimum edge going out of each tree */
			for (int u = 0; u < n; u++) {
				int tree = vTree[u];

				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					if (tree == vTree[v])
						continue;

					double eWeight = w.weight(e);
					if (eWeight < minEdgesWeight[tree]) {
						minEdges[tree] = e;
						minEdgesWeight[tree] = eWeight;
					}
				}
			}

			/* add min edges to MST */
			for (int tree = 0; tree < treeNum; tree++) {
				if (minEdges[tree] != -1) {
					int e = minEdges[tree];
					int ut = vTree[g.edgeSource(e)], vt = vTree[g.edgeTarget(e)];
					if (tree == vt) {
						int temp = ut;
						ut = vt;
						vt = temp;
					} else {
						assert tree == ut;
					}
					if (minEdges[vt] != e || ut < vt)
						mst.add(minEdges[tree]);
				}
			}

			/*
			 * the graph of the trees (vertex per tree, minimum out edges of the trees) is a
			 * graph where each vertex has one out edge at most, and we want to find all the
			 * connectivity components between the trees and label the vertices of G with
			 * new trees indices
			 */
			final int UNVISITED = -1;
			final int IN_PATH = -2;
			Arrays.fill(vTreeNext, 0, treeNum, UNVISITED);
			int treeNumNext = 0;
			for (int t = 0; t < treeNum; t++) {
				int pathLength = 0;
				/* find all reachable trees from t */
				for (int tPtr = t;;) {
					if (vTreeNext[tPtr] == UNVISITED) {
						/* another vertex on the path, continue */
						path[pathLength++] = tPtr;
						vTreeNext[tPtr] = IN_PATH;

						if (minEdges[tPtr] != -1) {
							int nextTPtr;
							if ((nextTPtr = vTree[g.edgeSource(minEdges[tPtr])]) == tPtr)
								nextTPtr = vTree[g.edgeTarget(minEdges[tPtr])];
							assert nextTPtr != tPtr;
							tPtr = nextTPtr;
							continue;
						}
					}

					/* if found label tree use it label, else - add new label */
					int newTree = vTreeNext[tPtr] >= 0 ? vTreeNext[tPtr] : treeNumNext++;
					/* assign the new label to all trees on path */
					while (pathLength-- > 0)
						vTreeNext[path[pathLength]] = newTree;
					break;
				}
			}

			if (treeNum == treeNumNext)
				break;
			treeNum = treeNumNext;
			Arrays.fill(minEdges, 0, treeNum, -1);

			/* assign new tree indices to G's vertices */
			for (int v = 0; v < n; v++)
				vTree[v] = vTreeNext[vTree[v]];
		}

		return Triple.valueOf(vTree, Integer.valueOf(treeNum), mst);
	}

}
