package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graphs.EdgeWeightComparator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;

public class MSTYao1976 implements MST {

	/*
	 * O(m log log n + n log n)
	 */

	public MSTYao1976() {
	}

	@Override
	public IntCollection calcMST(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		UGraph g = (UGraph) g0;
		int n = g.vertices().size();

		int[][][] edges = partitionEdgesToBuckets(g, w);
		int[] firstValidBucketIdxs = new int[n];

		int treeNum = n;
		int[] vTree = new int[n];
		int[] vTreeNext = new int[n];
		for (int v = 0; v < n; v++)
			vTree[v] = v;

		int[] minEdges = new int[n];
		Arrays.fill(minEdges, -1);
		double[] minGraphWeights = new double[n];
		int[] path = new int[n];

		IntCollection mst = new IntArrayList();
		for (;;) {
			Arrays.fill(minGraphWeights, 0, treeNum, Double.MAX_VALUE);

			/* find minimum edge going out of each tree */
			for (int u = 0; u < n; u++) {
				int tree = vTree[u];

				int[][] vertexBuckets = edges[u];
				int b;
				for (b = firstValidBucketIdxs[u]; b < vertexBuckets.length; b++) {
					boolean foundEdge = false;
					for (int i = 0; i < vertexBuckets[b].length; i++) {
						int e = vertexBuckets[b][i];
						if (tree == vTree[g.edgeSource(e)] && tree == vTree[g.edgeTarget(e)])
							continue;
						foundEdge = true;

						double eWeight = w.weight(e);
						if (eWeight < minGraphWeights[tree]) {
							minEdges[tree] = e;
							minGraphWeights[tree] = eWeight;
						}
					}
					if (foundEdge)
						break;
				}
				firstValidBucketIdxs[u] = b;
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
			 * graph where each vertex has one out edge at most we want to find all the
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

		return mst;
	}

	private static int[][][] partitionEdgesToBuckets(Graph g, EdgeWeightFunc w) {
		int n = g.vertices().size(), k = Utils.log2ceil(n);

		int[][][] edges = new int[n][][];
		int[] edgesTemp = new int[n];
		IntComparator edgeComparator = new EdgeWeightComparator(w);

		for (int u = 0; u < n; u++) {
			int edgesCount = 0;
			for (EdgeIter eit = g.edges(u); eit.hasNext();)
				edgesTemp[edgesCount++] = eit.nextInt();

			if (edgesCount <= k) {
				IntArrays.parallelQuickSort(edgesTemp, 0, edgesCount, edgeComparator);
				edges[u] = new int[edgesCount][];
				for (int i = 0; i < edgesCount; i++)
					edges[u][i] = new int[] { edgesTemp[i] };

			} else {
				int bucketSize = (edgesCount - 1) / k + 1;
				int bucketNum = (edgesCount - 1) / bucketSize + 1;
				Array.Int.bucketPartition(edgesTemp, 0, edgesCount, edgeComparator, bucketSize);
				edges[u] = new int[bucketNum][];

				for (int b = 0; b < bucketNum; b++) {
					int bucketBegin = b * bucketSize;
					int bucketEnd = Math.min(bucketBegin + bucketSize, edgesCount);
					int[] bucket = new int[bucketEnd - bucketBegin];
					System.arraycopy(edgesTemp, bucketBegin, bucket, 0, bucketEnd - bucketBegin);
					edges[u][b] = bucket;
				}
			}
		}

		return edges;
	}

}
