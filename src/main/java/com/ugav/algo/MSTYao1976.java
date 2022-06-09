package com.ugav.algo;

import java.util.ArrayList;
import java.util.Collection;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graphs.EdgeWeightComparator;

public class MSTYao1976 implements MST {

	/*
	 * O(m log log n + n log n)
	 */

	public MSTYao1976() {
	}

	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, WeightFunction<E> w) {
		if (g.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();

		Edge<E>[][][] edges = partitionEdgesToBuckets(g, w);
		int[] firstValidBucketIdxs = new int[n];

		int treeNum = n;
		int[] vTree = new int[n];
		int[] vTreeNext = new int[n];
		for (int v = 0; v < n; v++)
			vTree[v] = v;

		@SuppressWarnings("unchecked")
		Edge<E>[] minEdges = new Edge[n];
		double[] minEdgesWeight = new double[n];
		int[] path = new int[n];

		Collection<Edge<E>> mst = new ArrayList<>();
		for (;;) {
			java.util.Arrays.fill(minEdgesWeight, 0, treeNum, Double.MAX_VALUE);

			/* find minimum edge going out of each tree */
			for (int u = 0; u < n; u++) {
				int tree = vTree[u];

				Edge<E>[][] vertexBuckets = edges[u];
				int b;
				for (b = firstValidBucketIdxs[u]; b < vertexBuckets.length; b++) {
					boolean foundEdge = false;
					for (int i = 0; i < vertexBuckets[b].length; i++) {
						Edge<E> e = vertexBuckets[b][i];
						if (tree == vTree[e.v()])
							continue;
						foundEdge = true;

						double eWeight = w.weight(e);
						if (eWeight < minEdgesWeight[tree]) {
							minEdges[tree] = e;
							minEdgesWeight[tree] = eWeight;
						}
					}
					if (foundEdge)
						break;
				}
				firstValidBucketIdxs[u] = b;
			}

			/* add min edges to MST */
			for (int tree = 0; tree < treeNum; tree++) {
				if (minEdges[tree] != null) {
					Edge<E> e = minEdges[tree];
					int ut = vTree[e.u()], vt = vTree[e.v()];
					if (minEdges[vt] != e.twin() || ut < vt)
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
			java.util.Arrays.fill(vTreeNext, 0, treeNum, UNVISITED);
			int treeNumNext = 0;
			for (int t = 0; t < treeNum; t++) {
				int pathLength = 0;
				/* find all reachable trees from t */
				for (int tPtr = t;;) {
					if (vTreeNext[tPtr] == UNVISITED) {
						/* another vertex on the path, continue */
						path[pathLength++] = tPtr;
						vTreeNext[tPtr] = IN_PATH;

						if (minEdges[tPtr] != null) {
							tPtr = vTree[minEdges[tPtr].v()];
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
			java.util.Arrays.fill(minEdges, 0, treeNum, null);

			/* assign new tree indices to G's vertices */
			for (int v = 0; v < n; v++)
				vTree[v] = vTreeNext[vTree[v]];
		}

		return mst;
	}

	@SuppressWarnings("unchecked")
	private static <E> Edge<E>[][][] partitionEdgesToBuckets(Graph<E> g, WeightFunction<E> w) {
		int n = g.vertices(), k = Utils.log2ceil(n);

		Edge<E>[][][] edges = new Edge[n][][];
		Edge<E>[] edgesTemp = new Edge[n];
		EdgeWeightComparator<E> edgeComparator = new EdgeWeightComparator<>(w);

		for (int u = 0; u < n; u++) {
			int edgesCount = 0;
			for (Edge<E> e : Utils.iterable(g.edges(u)))
				edgesTemp[edgesCount++] = e;

			if (edgesCount <= k) {
				java.util.Arrays.sort(edgesTemp, 0, edgesCount, edgeComparator);
				edges[u] = new Edge[edgesCount][];
				for (int i = 0; i < edgesCount; i++)
					edges[u][i] = new Edge[] { edgesTemp[i] };

			} else {
				int bucketSize = (edgesCount - 1) / k + 1;
				int bucketNum = (edgesCount - 1) / bucketSize + 1;
				Arrays.bucketPartition(edgesTemp, 0, edgesCount, edgeComparator, bucketSize);
				edges[u] = new Edge[bucketNum][];

				for (int b = 0; b < bucketNum; b++) {
					int bucketBegin = b * bucketSize;
					int bucketEnd = Math.min(bucketBegin + bucketSize, edgesCount);
					Edge<E>[] bucket = new Edge[bucketEnd - bucketBegin];
					System.arraycopy(edgesTemp, bucketBegin, bucket, 0, bucketEnd - bucketBegin);
					edges[u][b] = bucket;
				}
			}
		}

		return edges;
	}

}
