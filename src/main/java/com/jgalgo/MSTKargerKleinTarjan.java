package com.jgalgo;

import java.util.Random;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Karger, Klein and Tarjan randomized linear minimum spanning tree algorithm
 * <p>
 * The algorithm runs in {@code O(m + n)} expected time, and uses linear space
 * in expectation. In practice, this algorithm is out-performed by almost all
 * simpler algorithms. Note that only undirected graphs are supported.
 * <p>
 * Based on "A randomized linear-time algorithm to find minimum spanning trees"
 * by Karger, David R.; Klein, Philip N.; Tarjan, Robert E. (1995).
 *
 * @author Barak Ugav
 */
public class MSTKargerKleinTarjan implements MST {

	private final Random seedGenerator;

	/**
	 * Create a new MST algorithm with random seed.
	 */
	public MSTKargerKleinTarjan() {
		this(System.nanoTime() ^ 0x905a1dad25b30034L);
	}

	/**
	 * Create a new MST algorithm with the given seed.
	 *
	 * @param seed a seed used for all random generators
	 */
	public MSTKargerKleinTarjan(long seed) {
		seedGenerator = new Random(seed ^ 0x1af7babf9783fd8bL);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	public IntCollection computeMinimumSpanningTree(Graph g, EdgeWeightFunc w) {
		if (!(g instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		return computeMST((UGraph) g, w);
	}

	private IntCollection computeMST(UGraph g, EdgeWeightFunc w) {
		if (g.vertices().size() == 0 || g.edges().size() == 0)
			return IntLists.emptyList();
		/*
		 * we run Boruvka to reduce the number of vertices by a factor of 4, and the
		 * constructed graph contains now edges with different edge indices. Therefore,
		 * the data stored in each edge is a reference to the old edge. This is a little
		 * bit clumsy, but didn't find another way.
		 */
		Pair<UGraph, IntCollection> r = MSTBoruvka.runBoruvka(g, w, 2, e -> new Ref(e, w.weight(e)), "ref");
		UGraph g0 = r.e1;
		IntCollection f0 = r.e2;
		UGraph g1 = randSubgraph(g0);
		Weights<Ref> g1Ref = g1.edgesWeight("ref");
		IntCollection f1Edges = computeMST(g1, e -> g1Ref.get(e).w);
		UGraph f1 = Graphs.subGraph(g1, f1Edges);
		IntCollection e2 = lightEdges(g0, f1);
		UGraph g2 = Graphs.subGraph(g0, e2);
		Weights<Ref> g2Ref = g2.edgesWeight("ref");
		IntCollection f2 = computeMST(g2, e -> g2Ref.get(e).w);

		for (IntIterator it = f2.iterator(); it.hasNext();) {
			int eRef = it.nextInt();
			int e = g2Ref.get(eRef).e;
			f0.add(e);
		}
		return f0;
	}

	private UGraph randSubgraph(UGraph g) {
		Random rand = new Random(seedGenerator.nextLong() ^ 0x043a4a7a193827bcL);
		IntCollection edgeSet = new IntArrayList(g.edges().size());
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (rand.nextBoolean())
				edgeSet.add(e);
		}
		return Graphs.subGraph(g, edgeSet);
	}

	private static IntCollection lightEdges(UGraph g, UGraph f) {
		int n = f.vertices().size();
		/* find connectivity components in the forest, each one of them is a tree */
		Connectivity.Result connectivityRes = Connectivity.findConnectivityComponents(f);
		int treeCount = connectivityRes.getNumberOfCC();
		Int2IntFunction vToTree = connectivityRes::getVertexCc;
		int[] treeSizes = new int[treeCount];
		for (int u = 0; u < n; u++)
			treeSizes[vToTree.applyAsInt(u)]++;

		UGraph[] trees = new UGraph[treeSizes.length];
		Weights.Double[] treeData = new Weights.Double[treeSizes.length];
		for (int t = 0; t < trees.length; t++) {
			trees[t] = new GraphArrayUndirected(treeSizes[t]);
			treeData[t] = trees[t].addEdgesWeights("weight", double.class);
		}

		int[] vToVnew = new int[n];
		int[] treeToNextv = new int[trees.length];
		for (int u = 0; u < n; u++)
			vToVnew[u] = treeToNextv[vToTree.applyAsInt(u)]++;

		Weights<Ref> fRef = f.edgesWeight("ref");
		for (IntIterator it = f.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = f.edgeSource(e), v = f.edgeTarget(e);
			int un = vToVnew[u], vn = vToVnew[v];
			int treeIdx = vToTree.applyAsInt(u);
			int en = trees[treeIdx].addEdge(un, vn);
			treeData[treeIdx].set(en, fRef.get(e).w);
		}

		/*
		 * use the tree path maxima to find the heaviest edge in the path connecting u v
		 * for each edge in g
		 */
		TPM tpm = new TPMHagerup();
		TPM.Queries[] tpmQueries = new TPM.Queries[trees.length];
		for (int t = 0; t < trees.length; t++)
			tpmQueries[t] = new TPM.Queries();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree.applyAsInt(u);
			if (ut != vToTree.applyAsInt(v))
				continue;
			tpmQueries[ut].addQuery(vToVnew[u], vToVnew[v]);
		}

		int[][] tpmResults = new int[trees.length][];
		for (int t = 0; t < trees.length; t++)
			tpmResults[t] = tpm.computeHeaviestEdgeInTreePaths(trees[t], treeData[t], tpmQueries[t]);

		/*
		 * Find all light edge by comparing each edge in g to the heaviest edge on the
		 * path from u to v in f
		 */
		Weights<Ref> gRef = g.edgesWeight("ref");
		IntCollection lightEdges = new IntArrayList();
		int[] tpmIdx = new int[trees.length];
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree.applyAsInt(u);
			if (ut != vToTree.applyAsInt(v) || gRef.get(e).w <= treeData[ut].weight(tpmResults[ut][tpmIdx[ut]++]))
				lightEdges.add(e);
		}
		return lightEdges;
	}

	private static class Ref {

		final int e;
		final double w;

		Ref(int e, double w) {
			this.e = e;
			this.w = w;
		}

		@Override
		public String toString() {
			return e != -1 ? String.valueOf(e) : Double.toString(w);
		}

	}

}
