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
	public IntCollection calcMST(Graph g0, WeightFunction w) {
		if (!(g0 instanceof Graph.Undirected))
			throw new IllegalArgumentException("only undirected graphs are supported");
		return calcMST0((Graph.Undirected) g0, w);
	}

	private IntCollection calcMST0(Graph.Undirected g, WeightFunction w) {
		if (g.vertices() == 0 || g.edges() == 0)
			return IntLists.emptyList();
		/*
		 * we run Boruvka to reduce the number of vertices by a factor of 4, and the
		 * constructed graph contains now edges with different edge indices. Therefore,
		 * the data stored in each edge is a reference to the old edge. This is a little
		 * bit clumsy, but didn't find another way.
		 */
		Pair<Graph.Undirected, IntCollection> r = MSTBoruvka1926.runBoruvka(g, w, 2, e -> new Ref(e, w.weight(e)),
				"ref");
		Graph.Undirected g0 = r.e1;
		IntCollection f0 = r.e2;
		Graph.Undirected g1 = randSubgraph(g0);
		EdgeData<Ref> g1Ref = g1.getEdgeData("ref");
		IntCollection f1Edges = calcMST0(g1, e -> g1Ref.get(e).w);
		Graph.Undirected f1 = subGraph(g1, f1Edges);
		IntCollection e2 = lightEdges(g0, f1);
		Graph.Undirected g2 = subGraph(g0, e2);
		EdgeData<Ref> g2Ref = g2.getEdgeData("ref");
		IntCollection f2 = calcMST0(g2, e -> g2Ref.get(e).w);

		for (IntIterator it = f2.iterator(); it.hasNext();) {
			int eRef = it.nextInt();
			int e = g2Ref.get(eRef).e;
			f0.add(e);
		}
		return f0;
	}

	private static Graph.Undirected subGraph(Graph.Undirected g, IntCollection edgeSet) {
		// TODO move to graph array class
		Graph.Undirected g1 = new GraphArrayUndirected(g.vertices());

		int[] s2e = edgeSet.toIntArray();
		for (int s = 0; s < s2e.length; s++) {
			int e = s2e[s];
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
			int s0 = g1.addEdge(u, v);
			assert s0 == s;
		}
		for (Object key : g.getEdgeDataKeys()) {
			EdgeData<?> data0 = g.getEdgeData(key);

			if (data0 instanceof EdgeData.Int data) {
				EdgeData.Int datas = g1.newEdgeDataInt(key);
				for (int s = 0; s < s2e.length; s++)
					datas.set(s, data.getInt(s2e[s]));

			} else if (data0 instanceof EdgeData.Double data) {
				EdgeData.Double datas = g1.newEdgeDataDouble(key);
				for (int s = 0; s < s2e.length; s++)
					datas.set(s, data.getDouble(s2e[s]));

			} else {
				EdgeData datas = g1.newEdgeData(key);
				for (int s = 0; s < s2e.length; s++)
					datas.set(s, data0.get(s2e[s]));
			}
		}
		return g1;
	}

	private Graph.Undirected randSubgraph(Graph.Undirected g) {
		Random rand = new Random(seedGenerator.nextLong() ^ 0x043a4a7a193827bcL);
		IntCollection edgeSet = new IntArrayList(g.edges());
		for (int e = 0; e < g.edges(); e++)
			if (rand.nextBoolean())
				edgeSet.add(e);
		return subGraph(g, edgeSet);
	}

	private static IntCollection lightEdges(Graph.Undirected g, Graph.Undirected f) {
		int n = f.vertices();
		/* find connectivity components in the forest, each one of them is a tree */
		Pair<Integer, int[]> r = Graphs.findConnectivityComponents(f);
		int treeCount = r.e1.intValue();
		int[] vToTree = r.e2;
		int[] treeSizes = new int[treeCount];
		for (int u = 0; u < n; u++)
			treeSizes[vToTree[u]]++;

		Graph.Undirected[] trees = new Graph.Undirected[treeSizes.length];
		EdgeData.Double[] treeData = new EdgeData.Double[treeSizes.length];
		for (int t = 0; t < trees.length; t++) {
			trees[t] = new GraphArrayUndirected(treeSizes[t]);
			treeData[t] = trees[t].newEdgeDataDouble("weight");
		}

		int[] vToVnew = new int[n];
		int[] treeToNextv = new int[trees.length];
		for (int u = 0; u < n; u++)
			vToVnew[u] = treeToNextv[vToTree[u]]++;

		EdgeData<Ref> fRef = f.getEdgeData("ref");
		for (int e = 0; e < f.edges(); e++) {
			int u = f.getEdgeSource(e), v = f.getEdgeTarget(e);
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
		for (int t = 0; t < trees.length; t++)
			tpmResults[t] = tpm.calcTPM(trees[t], treeData[t], tpmQueries[t], tpmQueriesNum[t]);

		/*
		 * Find all light edge by comparing each edge in g to the heaviest edge on the
		 * path from u to v in f
		 */
		EdgeData<Ref> gRef = g.getEdgeData("ref");
		IntCollection lightEdges = new IntArrayList();
		int[] tpmIdx = new int[trees.length];
		for (int e = 0; e < g.edges(); e++) {
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
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
