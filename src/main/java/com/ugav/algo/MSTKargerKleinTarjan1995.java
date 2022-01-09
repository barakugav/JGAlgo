package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class MSTKargerKleinTarjan1995 implements MST {

	/*
	 * Randomize algorithm for MST. O(m+n)
	 */

	private final Random seedGenerator;

	public MSTKargerKleinTarjan1995() {
		this(new Random().nextLong());
	}

	public MSTKargerKleinTarjan1995(long seed) {
		seedGenerator = new Random(seed ^ 0x1af7babf9783fd8bL);
	}

	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, WeightFunction<E> w) {
		if (g.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		if (g.edges().size() == 0)
			return Collections.emptyList();
		return calcMST0(g, w);
	}

	private static class Ref<E> {
		final Edge<E> e;
		final double w;

		Ref(Edge<E> e, double w) {
			this.e = e;
			this.w = w;
		}
	}

	private <E> Collection<Edge<E>> calcMST0(Graph<E> g, WeightFunction<E> w) {
		if (g.vertices() == 0 || g.edges().isEmpty())
			return Collections.emptyList();
		/*
		 * we run Boruvka to reduce the number of vertices by a factor of 4, and the
		 * constructed graph contains now edges with different edge indices. Therefore,
		 * the value stored in each edge is a reference to the old edge. This is a
		 * little bit clumsy, but didn't find another way.
		 */
		WeightFunction<Ref<E>> w0 = e -> e.val().w;
		Pair<Graph<Ref<E>>, Collection<Edge<E>>> r = MSTBoruvka1926.runBoruvka(g, w, 2, e -> new Ref<>(e, w.weight(e)));
		Graph<Ref<E>> g0 = r.e1;
		Collection<Edge<E>> f0 = r.e2;
		Graph<Ref<E>> g1 = randSubgraph(g0);
		Collection<Edge<Ref<E>>> f1Edges = calcMST0(g1, w0);
		Graph<Ref<E>> f1 = GraphArray.valueOf(g1.vertices(), f1Edges, DirectedType.Undirected);
		Collection<Edge<Ref<E>>> e2 = lightEdges(g0, f1, w0);
		Graph<Ref<E>> g2 = GraphArray.valueOf(g0.vertices(), e2, DirectedType.Undirected);
		Collection<Edge<Ref<E>>> f2 = calcMST0(g2, w0);

		for (Edge<Ref<E>> e : f2)
			f0.add(e.val().e);
		return f0;
	}

	private <E> Graph<E> randSubgraph(Graph<E> g) {
		Random rand = new Random(seedGenerator.nextLong());
		Graph<E> g1 = new GraphArray<>(DirectedType.Undirected, g.vertices());

		for (Edge<E> e : g.edges()) {
			if (rand.nextBoolean())
				continue;
			g1.addEdge(e.u(), e.v()).val(e.val());
		}

		return g1;
	}

	private static <E> Collection<Edge<E>> lightEdges(Graph<E> g, Graph<E> f, WeightFunction<E> w) {
		int n = f.vertices();
		/* find connectivity components in the forest, each one of them is a tree */
		Pair<Integer, int[]> r = Graphs.findConnectivityComponents(f);
		int treeCount = r.e1;
		int[] vToTree = r.e2;
		int[] treeSizes = new int[treeCount];
		for (int u = 0; u < n; u++)
			treeSizes[vToTree[u]]++;

		@SuppressWarnings("unchecked")
		Graph<Double>[] trees = new Graph[treeSizes.length];
		for (int t = 0; t < trees.length; t++)
			trees[t] = new GraphArray<>(DirectedType.Undirected, treeSizes[t]);

		int[] vToVnew = new int[n];
		int[] treeToNextv = new int[trees.length];
		for (int u = 0; u < n; u++)
			vToVnew[u] = treeToNextv[vToTree[u]]++;

		for (Edge<E> e : f.edges()) {
			int un = vToVnew[e.u()], vn = vToVnew[e.v()];
			trees[vToTree[e.u()]].addEdge(un, vn).val(w.weight(e));
		}

		/*
		 * use the tree path maxima to find the heaviest edge in the path connecting u v
		 * for each edge in g
		 */
		TPM tpm = TPMKomlos1985King1997Hagerup2009.getInstace();
		int[][] tpmQueries = new int[trees.length][];
		for (int t = 0; t < trees.length; t++)
			tpmQueries[t] = new int[2];
		int[] tpmQueriesNum = new int[trees.length];

		for (Edge<E> e : g.edges()) {
			int u = e.u(), v = e.v(), ut = vToTree[u];
			if (ut != vToTree[v])
				continue;
			if (tpmQueries[ut].length <= (tpmQueriesNum[ut] + 1) * 2)
				tpmQueries[ut] = Arrays.copyOf(tpmQueries[ut], tpmQueries[ut].length * 2);
			tpmQueries[ut][tpmQueriesNum[ut] * 2] = vToVnew[u];
			tpmQueries[ut][tpmQueriesNum[ut] * 2 + 1] = vToVnew[v];
			tpmQueriesNum[ut]++;
		}

		@SuppressWarnings("unchecked")
		Edge<Double>[][] tpmResults = new Edge[trees.length][];
		for (int t = 0; t < trees.length; t++)
			tpmResults[t] = tpm.calcTPM(trees[t], Graphs.WEIGHT_FUNC_DEFAULT, tpmQueries[t], tpmQueriesNum[t]);

		/*
		 * Find all light edge by comparing each edge in g to the heaviest edge on the
		 * path from u to v in f
		 */
		Collection<Edge<E>> lightEdges = new ArrayList<>();
		int[] tpmIdx = new int[trees.length];
		for (Edge<E> e : g.edges()) {
			int u = e.u(), v = e.v(), ut = vToTree[u];
			if (ut != vToTree[v] || w.weight(e) <= tpmResults[ut][tpmIdx[ut]++].val())
				lightEdges.add(e);
		}

		return lightEdges;
	}

}
