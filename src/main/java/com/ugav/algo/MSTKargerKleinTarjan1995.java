package com.ugav.algo;

import java.util.Arrays;
import java.util.Random;

import com.ugav.algo.Graph.WeightFunction;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MSTKargerKleinTarjan1995 implements MST {

	/*
	 * Randomize algorithm for MST. O(m + n)
	 */

	private final Random seedGenerator;

	public MSTKargerKleinTarjan1995() {
		this(new Random().nextLong());
	}

	public MSTKargerKleinTarjan1995(long seed) {
		seedGenerator = new Random(seed ^ 0x1af7babf9783fd8bL);
	}

	@Override
	public IntCollection calcMST(Graph<?> g0, WeightFunction w) {
		if (!(g0 instanceof Graph.Undirected<?>))
			throw new IllegalArgumentException("only undirected graphs are supported");
		Graph.Undirected<?> g = (Graph.Undirected<?>) g0;
		if (g.edges() == 0)
			return IntLists.emptyList();
		return calcMST0(g, w);
	}

	private IntCollection calcMST0(Graph<?> g, WeightFunction w) {
		if (g.vertices() == 0 || g.edges() == 0)
			return IntLists.emptyList();
		/*
		 * we run Boruvka to reduce the number of vertices by a factor of 4, and the
		 * constructed graph contains now edges with different edge indices. Therefore,
		 * the data stored in each edge is a reference to the old edge. This is a little
		 * bit clumsy, but didn't find another way.
		 */
		Pair<Graph.Undirected<Ref>, IntCollection> r = MSTBoruvka1926.runBoruvka(g, w, 2, e -> new Ref(e, w.weight(e)));
		Graph.Undirected<Ref> g0 = r.e1;
		IntCollection f0 = r.e2;
		WeightFunction w0 = e -> g0.edgeData().get(e).w;
		Graph.Undirected<Ref> g1 = randSubgraph(g0);
		IntCollection f1Edges = calcMST0(g1, w0);
		Graph.Undirected<Ref> f1 = subGraph(g1, f1Edges);
		IntCollection e2 = lightEdges(g0, f1, w0);
		Graph<Ref> g2 = subGraph(g0, e2);
		IntCollection f2 = calcMST0(g2, w0);

		for (IntIterator it = f2.iterator(); it.hasNext();) {
			int eRef = it.nextInt();
			int e = g0.edgeData().get(eRef).e;
			f0.add(e);
		}
		return f0;
	}

	private static <E> Graph.Undirected<E> subGraph(Graph.Undirected<E> g, IntCollection edgeSet) {
		// TODO move to graph array class
		Graph.Undirected<E> g1 = new GraphArrayUndirected<>(g.vertices());

		EdgeData<E> gData = g.edgeData();
		EdgeData<E> g1Data = new EdgeDataArray.Obj<>(g.edges());
		for (IntIterator it = edgeSet.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
			g1.addEdge(u, v);
			g1Data.set(e, gData.get(e));
		}
		g1.setEdgesData(g1Data);

		return g1;
	}

	private <E> Graph.Undirected<E> randSubgraph(Graph.Undirected<E> g) {
		Random rand = new Random(seedGenerator.nextLong() ^ 0x043a4a7a193827bcL);
		IntCollection edgeSet = new IntArrayList(g.edges());
		for (int e = 0; e < g.edges(); e++)
			if (rand.nextBoolean())
				edgeSet.add(e);
		return subGraph(g, edgeSet);
	}

	private static IntCollection lightEdges(Graph.Undirected<?> g, Graph.Undirected<?> f, WeightFunction w) {
		int n = f.vertices();
		/* find connectivity components in the forest, each one of them is a tree */
		Pair<Integer, int[]> r = Graphs.findConnectivityComponents(f);
		int treeCount = r.e1.intValue();
		int[] vToTree = r.e2;
		int[] treeSizes = new int[treeCount];
		for (int u = 0; u < n; u++)
			treeSizes[vToTree[u]]++;

		@SuppressWarnings("unchecked")
		Graph.Undirected<Double>[] trees = new Graph.Undirected[treeSizes.length];
		for (int t = 0; t < trees.length; t++) {
			trees[t] = new GraphArrayUndirected<>(treeSizes[t]);
			trees[t].setEdgesData(new EdgeDataArray.Double());
		}

		int[] vToVnew = new int[n];
		int[] treeToNextv = new int[trees.length];
		for (int u = 0; u < n; u++)
			vToVnew[u] = treeToNextv[vToTree[u]]++;

		for (int e = 0; e < f.edges(); e++) {
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
			int un = vToVnew[u], vn = vToVnew[v];
			Graph<Double> tree = trees[vToTree[u]];
			int en = tree.addEdge(un, vn);
			((EdgeData.Double) tree.edgeData()).set(en, w.weight(e));
		}

		/*
		 * use the tree path maxima to find the heaviest edge in the path connecting u v
		 * for each edge in g
		 */
		TPM tpm = new TPMKomlos1985King1997Hagerup2009();
		int[][] tpmQueries = new int[trees.length][];
		for (int t = 0; t < trees.length; t++)
			tpmQueries[t] = new int[2];
		int[] tpmQueriesNum = new int[trees.length];

		for (int e = 0; e < g.edges(); e++) {
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
			int ut = vToTree[u];
			if (ut != vToTree[v])
				continue;
			if (tpmQueries[ut].length <= (tpmQueriesNum[ut] + 1) * 2)
				tpmQueries[ut] = Arrays.copyOf(tpmQueries[ut], tpmQueries[ut].length * 2);
			tpmQueries[ut][tpmQueriesNum[ut] * 2] = vToVnew[u];
			tpmQueries[ut][tpmQueriesNum[ut] * 2 + 1] = vToVnew[v];
			tpmQueriesNum[ut]++;
		}

		int[][] tpmResults = new int[trees.length][];
		for (int t = 0; t < trees.length; t++) {
			WeightFunction treeW = (EdgeData.Double) trees[t].edgeData();
			tpmResults[t] = tpm.calcTPM(trees[t], treeW, tpmQueries[t], tpmQueriesNum[t]);
		}

		/*
		 * Find all light edge by comparing each edge in g to the heaviest edge on the
		 * path from u to v in f
		 */
		IntCollection lightEdges = new IntArrayList();
		int[] tpmIdx = new int[trees.length];
		for (int e = 0; e < g.edges(); e++) {
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
			int ut = vToTree[u];
			WeightFunction treeW = (EdgeData.Double) trees[ut].edgeData();
			if (ut != vToTree[v] || w.weight(e) <= treeW.weight(tpmResults[ut][tpmIdx[ut]++]))
				lightEdges.add(e);
		}

		return lightEdges;
	}

	public static class Ref {

		public final int e;
		public final double w;

		public Ref(int e, double w) {
			this.e = e;
			this.w = w;
		}

		@Override
		public int hashCode() {
			return e;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Ref))
				return false;

			Ref o = (Ref) other;
			return e == o.e;
		}

		@Override
		public String toString() {
			return e != -1 ? String.valueOf(e) : Double.toString(w);
		}

	}

}
