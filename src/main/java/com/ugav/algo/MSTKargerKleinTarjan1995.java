package com.ugav.algo;

import java.util.Arrays;
import java.util.Random;



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
	public IntCollection calcMST(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		return calcMST0((UGraph) g0, w);
	}

	private IntCollection calcMST0(UGraph g, EdgeWeightFunc w) {
		if (g.verticesNum() == 0 || g.edgesNum() == 0)
			return IntLists.emptyList();
		/*
		 * we run Boruvka to reduce the number of vertices by a factor of 4, and the
		 * constructed graph contains now edges with different edge indices. Therefore,
		 * the data stored in each edge is a reference to the old edge. This is a little
		 * bit clumsy, but didn't find another way.
		 */
		Pair<UGraph, IntCollection> r = MSTBoruvka1926.runBoruvka(g, w, 2, e -> new Ref(e, w.weight(e)), "ref");
		UGraph g0 = r.e1;
		IntCollection f0 = r.e2;
		UGraph g1 = randSubgraph(g0);
		GraphWeights<Ref> g1Ref = g1.edgesWeight("ref");
		IntCollection f1Edges = calcMST0(g1, e -> g1Ref.get(e).w);
		UGraph f1 = Graphs.subGraph(g1, f1Edges);
		IntCollection e2 = lightEdges(g0, f1);
		UGraph g2 = Graphs.subGraph(g0, e2);
		GraphWeights<Ref> g2Ref = g2.edgesWeight("ref");
		IntCollection f2 = calcMST0(g2, e -> g2Ref.get(e).w);

		for (IntIterator it = f2.iterator(); it.hasNext();) {
			int eRef = it.nextInt();
			int e = g2Ref.get(eRef).e;
			f0.add(e);
		}
		return f0;
	}

	private UGraph randSubgraph(UGraph g) {
		Random rand = new Random(seedGenerator.nextLong() ^ 0x043a4a7a193827bcL);
		IntCollection edgeSet = new IntArrayList(g.edgesNum());
		for (int e = 0; e < g.edgesNum(); e++)
			if (rand.nextBoolean())
				edgeSet.add(e);
		return Graphs.subGraph(g, edgeSet);
	}

	private static IntCollection lightEdges(UGraph g, UGraph f) {
		int n = f.verticesNum();
		/* find connectivity components in the forest, each one of them is a tree */
		Pair<Integer, int[]> r = Graphs.findConnectivityComponents(f);
		int treeCount = r.e1.intValue();
		int[] vToTree = r.e2;
		int[] treeSizes = new int[treeCount];
		for (int u = 0; u < n; u++)
			treeSizes[vToTree[u]]++;

		UGraph[] trees = new UGraph[treeSizes.length];
		GraphWeights.Double[] treeData = new GraphWeights.Double[treeSizes.length];
		for (int t = 0; t < trees.length; t++) {
			trees[t] = new GraphArrayUndirected(treeSizes[t]);
			treeData[t] = trees[t].newEdgeWeightDouble("weight");
		}

		int[] vToVnew = new int[n];
		int[] treeToNextv = new int[trees.length];
		for (int u = 0; u < n; u++)
			vToVnew[u] = treeToNextv[vToTree[u]]++;

		GraphWeights<Ref> fRef = f.edgesWeight("ref");
		for (int e = 0; e < f.edgesNum(); e++) {
			int u = f.edgeSource(e), v = f.edgeTarget(e);
			int un = vToVnew[u], vn = vToVnew[v];
			int treeIdx = vToTree[u];
			int en = trees[treeIdx].addEdge(un, vn);
			treeData[treeIdx].set(en, fRef.get(e).w);
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

		for (int e = 0; e < g.edgesNum(); e++) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
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
		for (int t = 0; t < trees.length; t++)
			tpmResults[t] = tpm.calcTPM(trees[t], treeData[t], tpmQueries[t], tpmQueriesNum[t]);

		/*
		 * Find all light edge by comparing each edge in g to the heaviest edge on the
		 * path from u to v in f
		 */
		GraphWeights<Ref> gRef = g.edgesWeight("ref");
		IntCollection lightEdges = new IntArrayList();
		int[] tpmIdx = new int[trees.length];
		for (int e = 0; e < g.edgesNum(); e++) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree[u];
			if (ut != vToTree[v] || gRef.get(e).w <= treeData[ut].weight(tpmResults[ut][tpmIdx[ut]++]))
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
