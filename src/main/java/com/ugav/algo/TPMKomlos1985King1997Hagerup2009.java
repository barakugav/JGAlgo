package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class TPMKomlos1985King1997Hagerup2009 implements TPM {

	private TPMKomlos1985King1997Hagerup2009() {
	}

	private static final TPMKomlos1985King1997Hagerup2009 INSTANCE = new TPMKomlos1985King1997Hagerup2009();

	public static TPMKomlos1985King1997Hagerup2009 getInstace() {
		return INSTANCE;
	}

	@Override
	public <E> Edge<E>[] calcTPM(Graph<E> t, WeightFunction<E> w, int[] queries) {
		if (t.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		if (queries.length % 2 != 0)
			throw new IllegalArgumentException("queries should be in format [u0, v0, u1, v1, ...]");
		if (!Graphs.isTree(t))
			throw new IllegalArgumentException("only trees are supported");
		int queriesNum = queries.length / 2;
		@SuppressWarnings("unchecked")
		Edge<E>[] res = new Edge[queriesNum];
		if (t.vertices() == 0)
			return res;

		Tuple<Graph<Ref<E>>, Integer> r = buildBoruvkaFullyBranchingTree(t, w);
		Graph<Ref<E>> t0 = r.e1;
		int root = r.e2;

		int[] lcaQueries = splitQueriesIntoLCAQueries(t0, root, queries);

		Tuple<Edge<Ref<E>>[], int[]> r2 = getEdgeToParentsAndDepth(t0, root);
		Edge<Ref<E>>[] edgeToParent = r2.e1;
		int[] depths = r2.e2;

		List<Integer>[] q = calcQueriesPerVertex(lcaQueries, depths, edgeToParent);
		List<Edge<Ref<E>>>[] a = calcAnswersPerVertex(t0, root, q, edgeToParent);
		return extractEdgesFromAnswers(a, q, lcaQueries, depths);
	}

	private static <E> Edge<E>[] extractEdgesFromAnswers(List<Edge<Ref<E>>>[] a, List<Integer>[] q, int[] lcaQueries,
			int[] depths) {
		int queriesNum = lcaQueries.length / 4;
		@SuppressWarnings("unchecked")
		Edge<E>[] res = new Edge[queriesNum];

		for (int i = 0; i < queriesNum; i++) {
			int u = lcaQueries[i * 4];
			int v = lcaQueries[i * 4 + 2];
			int lca = lcaQueries[i * 4 + 1];
			int lcaDepth = depths[lca];

			Edge<Ref<E>> ua = null, va = null;

			for (int j = 0; j < q[u].size(); j++) {
				if (q[u].get(j) == lcaDepth) {
					ua = a[u].get(j);
					break;
				}
			}
			for (int j = 0; j < q[v].size(); j++) {
				if (q[v].get(j) == lcaDepth) {
					va = a[v].get(j);
					break;
				}
			}

			res[i] = (va == null || (ua != null && ua.val().w >= va.val().w)) ? (ua != null ? ua.val().orig : null)
					: (va != null ? va.val().orig : null);
		}

		return res;
	}

	private static <E> List<Edge<Ref<E>>>[] calcAnswersPerVertex(Graph<Ref<E>> t, int root, List<Integer>[] q,
			Edge<Ref<E>>[] edgeToParent) {
		int n = t.vertices();
		@SuppressWarnings("unchecked")
		List<Integer>[] a = new List[n];
		for (int i = 0; i < n; i++) {
			a[i] = new ArrayList<>(q[i].size());
			for (int j = 0; j < q[i].size(); j++)
				a[i].add(-1);
		}

		int leavesDepth = Graphs.getFullyBranchingTreeDepth(t, root);

		// TODO use number of leaves
		@SuppressWarnings("unchecked")
		List<Edge<Ref<E>>>[] res = new List[n];

		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] edges = new Edge[n * 2];
		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] edgesToRoot = new Edge[leavesDepth];

		int[] edgesOffset = new int[n];
		int[] edgesCount = new int[n];
		int[] edgesIdx = new int[n];

		edgesOffset[0] = 0;
		edgesCount[0] = t.getEdgesArr(root, edges, 0);
		edgesIdx[0] = 0;

		for (int depth = 0; depth >= 0;) {
			// find next child from end of DFS path
			Edge<Ref<E>> edgeToChild = null;
			for (int i = edgesIdx[depth]; i < edgesCount[depth]; i++) {
				Edge<Ref<E>> e = edges[edgesOffset[depth] + i];
				if (e != edgeToParent[e.u()]) {
					edgeToChild = edges[edgesOffset[depth] + i];
					edgesIdx[depth] = i + 1;
					break;
				}
			}

			if (edgeToChild != null) {
				int u = edgeToChild.u(); // parent
				int v = edgeToChild.v(); // child

				// TODO more efficient way
				subseq(a[u], q[u], q[v], a[v]);
				int j = binarySearch(a[v], edgeToChild.val().w, edgesToRoot);
				repSuf(a[v], depth, j);

				edgesToRoot[depth] = edgeToChild;
				if (depth + 1 != leavesDepth) {
					// add vertex to end of DFS path
					depth++;
					edgesOffset[depth] = edgesOffset[depth - 1] + edgesCount[depth - 1];
					edgesCount[depth] = t.getEdgesArr(v, edges, edgesOffset[depth]);
					edgesIdx[depth] = 0;
				} else {
					// TODO ?
					List<Edge<Ref<E>>> resv = new ArrayList<>(q[v].size());
					for (int i = 0; i < a[v].size(); i++)
						resv.add(edgesToRoot[a[v].get(i)]);
					res[v] = resv;

					edgesToRoot[depth] = null;
				}
			} else {
				// return to previous vertex in DFS path
				if (edgeToParent[depth] == null)
					break;
				depth--;
			}
		}
		return res;
	}

	private static void subseq(List<Integer> au, List<Integer> qu, List<Integer> qv, List<Integer> av) {
		// TODO bit map tricks
		// TODO remove

		for (int iu = 0, iv = 0;;) {
			if (iu >= qu.size() || iv >= qv.size())
				return;
			int qui = qu.get(iu);
			int qvi = qv.get(iv);
			if (qui == qvi)
				av.set(iv++, au.get(iu));
			else if (qui < qvi)
				iu++;
			else
				iv++;
		}
	}

	private static <E> int binarySearch(List<Integer> av, double w, Edge<Ref<E>>[] edgesToRoot) {
		// TODO binary search or lookup table
		for (int i = av.size() - 1; i >= 0; i--) {
			int avi = av.get(i);
			if (avi != -1 && edgesToRoot[avi].val().w >= w)
				return i + 1;
		}
		return 0;
	}

	private static void repSuf(List<Integer> av, int depth, int j) {
		for (int i = j; i < av.size(); i++)
			av.set(i, depth);
	}

	private static <E> Tuple<Graph<Ref<E>>, Integer> buildBoruvkaFullyBranchingTree(Graph<E> g, WeightFunction<E> w) {
		int n = g.vertices();
		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] minEdges = new Edge[n];
		double[] minEdgesWeight = new double[n];
		int[] vNext = new int[n];
		int[] path = new int[n];
		int[] vTv = new int[n];
		int[] vTvNext = new int[n];

		for (int v = 0; v < n; v++)
			vTv[v] = v;

		Graph.Modifiable<Ref<E>> t = new GraphArray<>(DirectedType.Undirected, n);
		for (Graph<Ref<E>> G = createRefGraph(g, w); (n = G.vertices()) > 1;) {

			// Find minimum edge of each vertex
			Arrays.fill(minEdges, 0, n, null);
			Arrays.fill(minEdgesWeight, 0, n, Double.MAX_VALUE);
			for (int u = 0; u < n; u++) {
				for (Iterator<Edge<Ref<E>>> it = G.edges(u); it.hasNext();) {
					Edge<Ref<E>> e = it.next();

					double eWeight = e.val().w;
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

						p = minEdges[p].v();
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
			for (int u = 0; u < n; u++)
				t.addEdge(vTv[u], vTvNext[vNext[u]]).val(minEdges[u].val());
			int[] temp = vTv;
			vTv = vTvNext;
			vTvNext = temp;

			// contract G to new graph with the super vertices
			Graph.Modifiable<Ref<E>> gNext = new GraphArray<>(DirectedType.Undirected, nNext);
			for (int u = 0; u < n; u++) {
				int U = vNext[u];
				for (Iterator<Edge<Ref<E>>> it = G.edges(u); it.hasNext();) {
					Edge<Ref<E>> e = it.next();
					int V = vNext[e.v()];
					if (U != V)
						gNext.addEdge(U, V).val(e.val());
				}
			}

			@SuppressWarnings("rawtypes")
			Graph.Modifiable tempg = ((Graph.Modifiable) G);
			tempg.clear();
			G = gNext;
		}
		return new Tuple<>(t, vTv[0]);
	}

	private static <E> int[] splitQueriesIntoLCAQueries(Graph<Ref<E>> t, int root, int[] queries) {
		int queriesNum = queries.length / 2;
		int[] lcaQueries = new int[queriesNum * 4];

		LCA lcaAlgo = LCARMQBenderFarachColton2000.getInstace();
		LCA.Result lcaRes = lcaAlgo.preprocessLCA(t, root);
		for (int q = 0; q < queriesNum; q++) {
			int u = queries[q * 2], v = queries[q * 2 + 1];
			int lca = lcaRes.query(u, v);
			lcaQueries[q * 4] = u;
			lcaQueries[q * 4 + 1] = lca;
			lcaQueries[q * 4 + 2] = v;
			lcaQueries[q * 4 + 3] = lca;
		}
		return lcaQueries;
	}

	private static <E> Tuple<Edge<Ref<E>>[], int[]> getEdgeToParentsAndDepth(Graph<Ref<E>> t, int root) {
		int n = t.vertices();
		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] edgeToParent = new Edge[n];
		int[] depths = new int[n];

		int[] layer = new int[n];
		int[] layerNext = new int[n];
		int layerSize = 0;

		layer[layerSize++] = root;

		for (int depth = 0; layerSize > 0; depth++) {
			int layerSizeNext = 0;

			for (int u; layerSize > 0;) {
				u = layer[--layerSize];
				depths[u] = depth;

				Edge<Ref<E>> ep = edgeToParent[u];
				int parent = ep != null ? ep.v() : -1;

				for (Iterator<Edge<Ref<E>>> it = t.edges(u); it.hasNext();) {
					Edge<Ref<E>> e = it.next();
					int v = e.v();
					if (v == parent)
						continue;
					edgeToParent[v] = e.twin();
					layerNext[layerSizeNext++] = v;
				}
			}

			int[] temp = layer;
			layer = layerNext;
			layerNext = temp;
			layerSize = layerSizeNext;
		}
		return new Tuple<>(edgeToParent, depths);
	}

	private static <E> List<Integer>[] calcQueriesPerVertex(int[] lcaQueries, int[] depths,
			Edge<Ref<E>>[] edgeToParent) {
		int n = edgeToParent.length;
		int[] layer = new int[n];
		int[] layerNext = new int[n];
		int layerSize = 0;

//		int[] q = new int[n];
//		Arrays.fill(q, 0);
		@SuppressWarnings("unchecked")
		List<Integer>[] q = new List[n];
		for (int i = 0; i < n; i++)
			q[i] = new ArrayList<>(0);

		int queriesNum = lcaQueries.length / 2;
		for (int query = 0; query < queriesNum; query++) {
			int u = lcaQueries[query * 2];
			int ancestor = lcaQueries[query * 2 + 1];
			if (u == ancestor)
				continue;
//			q[u] |= 1 << depths[ancestor];
			q[u].add(depths[ancestor]);
		}

		for (int u = 0; u < n; u++)
			layer[layerSize++] = u;

		while (layerSize > 0) {
			int layerSizeNext = 0;

			for (int u; layerSize > 0;) {
				u = layer[--layerSize];

				Edge<Ref<E>> ep = edgeToParent[u];
				if (ep == null)
					continue;
				int parent = ep.v();
//				q[parent] |= (q[u] & (1 << depths[parent]));
				for (int qu : q[u])
					if (qu < depths[parent])
						q[parent].add(qu);

				layerNext[layerSizeNext++] = parent;
			}

			int[] temp = layer;
			layer = layerNext;
			layerNext = temp;
			layerSize = layerSizeNext;
		}

		for (int i = 0; i < n; i++)
			q[i].sort(null);
		return q;
	}

	private static class Ref<E> {

		final Edge<E> orig;
		final double w;

		Ref(Edge<E> e, double w) {
			orig = e;
			this.w = w;
		}

		public int hashCode() {
			return orig.hashCode();
		}

		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Ref))
				return false;

			Ref<?> o = (Ref<?>) other;
			return orig.equals(o.orig);
		}

		public String toString() {
			return "R(" + orig + ")";
		}

	}

	private static <E> Graph<Ref<E>> createRefGraph(Graph<E> g, WeightFunction<E> w) {
		Graph.Modifiable<Ref<E>> g0 = GraphLinked.builder().setDirected(false).setVertexNum(g.vertices()).build();
		for (Iterator<Edge<E>> it = g.edges(); it.hasNext();) {
			Edge<E> e = it.next();
			Ref<E> v = new Ref<>(e, w.weight(e));
			g0.addEdge(e.u(), e.v()).val(v);
		}
		return g0;
	}

}
