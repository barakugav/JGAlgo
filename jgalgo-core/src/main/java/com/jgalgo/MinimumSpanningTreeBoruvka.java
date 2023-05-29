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
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Boruvka minimum spanning tree algorithm.
 * <p>
 * The algorithm run in iterations. In each iteration it finds the minimum edge incident for each vertex, and adds all
 * of these edges to the forest. Each connected component in the forest become a 'super vertex' in the next iteration.
 * The algorithm terminate when there is a single super vertex in the case the original graph was connected, or when
 * there are no incident edges to the remaining super vertices.
 * <p>
 * The running time of the algorithm is \(O(m \log n)\) and it uses linear space. Note that only undirected graphs are
 * supported.
 * <p>
 * Based on "O jistém problému minimálním" [About a certain minimal problem] by Borůvka, Otakar (1926).
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Bor%C5%AFvka%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class MinimumSpanningTreeBoruvka implements MinimumSpanningTree {

	/**
	 * Construct a new MST algorithm object.
	 */
	MinimumSpanningTreeBoruvka() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	public MinimumSpanningTree.Result computeMinimumSpanningTree(Graph g, EdgeWeightFunc w) {
		return new MinimumSpanningTreeResultImpl(computeMST(g, w, Integer.MAX_VALUE).mst);
	}

	Pair<Graph, IntCollection> runBoruvka(Graph g, EdgeWeightFunc w, int numberOfRounds, Object edgeRefKey) {
		if (numberOfRounds <= 0)
			throw new IllegalArgumentException();
		Res mstRes = computeMST(g, w, numberOfRounds);

		Graph contractedG = GraphBuilder.newUndirected().expectedVerticesNum(mstRes.treeNum).build();
		for (int v = 0; v < mstRes.treeNum; v++)
			contractedG.addVertex();
		Weights.Int edgeRef = contractedG.addEdgesWeights(edgeRefKey, int.class);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = mstRes.vToTree[g.edgeSource(e)];
			int v = mstRes.vToTree[g.edgeTarget(e)];
			if (u == v)
				continue;
			int ne = contractedG.addEdge(u, v);
			edgeRef.set(ne, e);
		}
		return Pair.of(contractedG, mstRes.mst);
	}

	private static Res computeMST(Graph g, EdgeWeightFunc w, int numberOfRounds) {
		ArgumentCheck.onlyUndirected(g);
		final int n = g.vertices().size();

		int[] vTree = new int[n];
		int[] vTreeNext = new int[n];
		for (int v = 0; v < n; v++)
			vTree[v] = v;

		int[] minEdges = new int[n];
		double[] minEdgesWeights = new double[n];
		int[] path = new int[n];

		int treeNum = n;
		IntCollection mst = new IntArrayList();
		for (int i = 0; i < numberOfRounds; i++) {

			/* find minimum edge going out of each tree */
			Arrays.fill(minEdges, 0, treeNum, -1);
			Arrays.fill(minEdgesWeights, 0, treeNum, Double.MAX_VALUE);
			for (int u = 0; u < n; u++) {
				int tree = vTree[u];

				for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					if (tree == vTree[v])
						continue;

					double eWeight = w.weight(e);
					if (eWeight < minEdgesWeights[tree]) {
						minEdges[tree] = e;
						minEdgesWeights[tree] = eWeight;
					}
				}
			}

			/* add min edges to MST */
			for (int tree = 0; tree < treeNum; tree++) {
				int e = minEdges[tree];
				if (e == -1)
					continue;
				int ut = vTree[g.edgeSource(e)], vt = vTree[g.edgeTarget(e)];
				if (tree == vt) {
					int temp = ut;
					ut = vt;
					vt = temp;
				} else {
					assert tree == ut;
				}
				if (ut < vt || minEdges[vt] != e)
					mst.add(e);
			}

			/*
			 * the graph of the trees (vertex per tree, minimum out edges of the trees) is a graph where each vertex has
			 * one out edge at most, and we want to find all the connectivity components between the trees and label the
			 * vertices of G with new trees indices
			 */
			final int UNVISITED = -1;
			final int IN_PATH = -2;
			Arrays.fill(vTreeNext, 0, treeNum, UNVISITED);
			int treeNumNext = 0;

			for (int t = 0; t < treeNum; t++) {
				/* find all reachable trees from t */
				int pathLength = 0;
				int deepestTree;
				for (int tPtr = t;;) {
					if (vTreeNext[tPtr] != UNVISITED) {
						deepestTree = tPtr;
						break;
					}

					/* another tree on the path, continue */
					path[pathLength++] = tPtr;
					vTreeNext[tPtr] = IN_PATH;
					int minEdge = minEdges[tPtr];
					if (minEdge == -1) {
						deepestTree = tPtr;
						break;
					}

					int nextTPtr;
					if ((nextTPtr = vTree[g.edgeSource(minEdge)]) == tPtr)
						nextTPtr = vTree[g.edgeTarget(minEdge)];
					assert nextTPtr != tPtr;
					tPtr = nextTPtr;
				}

				/* if found labeled tree, use it label, else, add a new label */
				int newTree = vTreeNext[deepestTree] >= 0 ? vTreeNext[deepestTree] : treeNumNext++;
				/* assign the new label to all trees on path */
				while (pathLength-- > 0)
					vTreeNext[path[pathLength]] = newTree;
			}

			if (treeNum == treeNumNext)
				break;
			treeNum = treeNumNext;

			/* assign new tree indices to G's vertices */
			for (int v = 0; v < n; v++)
				vTree[v] = vTreeNext[vTree[v]];
		}

		return new Res(vTree, treeNum, mst);
	}

	private static class Res {
		final int[] vToTree;
		final int treeNum;
		final IntCollection mst;

		Res(int[] vToTree, int treeNum, IntCollection mst) {
			this.vToTree = vToTree;
			this.treeNum = treeNum;
			this.mst = mst;
		}
	}

}
