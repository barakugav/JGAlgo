package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import com.ugav.algo.Graph.Edge;

public class MSTBoruvka1926 implements MST {

	/*
	 * O(m log n)
	 */

	public MSTBoruvka1926() {
	}

	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w) {
		return calcMST0(g, w, Integer.MAX_VALUE).e3;
	}

	static <E, R> Pair<Graph.Undirected<R>, Collection<Edge<E>>> runBoruvka(Graph<E> g, Graph.WeightFunction<E> w,
			int numberOfRounds, Function<Edge<E>, R> edgeValAssigner) {
		if (numberOfRounds <= 0)
			throw new IllegalArgumentException();
		Triple<int[], Integer, Collection<Edge<E>>> r = calcMST0(g, w, numberOfRounds);
		int[] tree = r.e1;
		int treeNum = r.e2.intValue();
		Collection<Edge<E>> mstEdges = r.e3;

		Graph.Undirected<R> contractedG = new GraphArrayUndirected<>(treeNum);
		for (Edge<E> e : g.edges()) {
			int u = tree[e.u()];
			int v = tree[e.v()];
			if (u == v)
				continue;
			contractedG.addEdge(u, v).setData(edgeValAssigner.apply(e));
		}
		return Pair.of(contractedG, mstEdges);
	}

	private static <E> Triple<int[], Integer, Collection<Edge<E>>> calcMST0(Graph<E> g, Graph.WeightFunction<E> w,
			int numberOfRounds) {
		if (g instanceof Graph.Directed<?>)
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();

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
		for (int i = 0; i < numberOfRounds; i++) {
			Arrays.fill(minEdgesWeight, 0, treeNum, Double.MAX_VALUE);

			/* find minimum edge going out of each tree */
			for (int u = 0; u < n; u++) {
				int tree = vTree[u];

				for (Edge<E> e : Utils.iterable(g.edges(u))) {
					if (tree == vTree[e.v()])
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
			Arrays.fill(minEdges, 0, treeNum, null);

			/* assign new tree indices to G's vertices */
			for (int v = 0; v < n; v++)
				vTree[v] = vTreeNext[vTree[v]];
		}

		return Triple.valueOf(vTree, Integer.valueOf(treeNum), mst);
	}

}
