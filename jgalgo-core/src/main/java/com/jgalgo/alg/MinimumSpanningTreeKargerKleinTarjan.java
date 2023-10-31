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

package com.jgalgo.alg;

import java.util.Arrays;
import java.util.Random;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Karger, Klein and Tarjan randomized linear minimum spanning tree algorithm
 * <p>
 * The algorithm runs in \(O(n + m)\) expected time, and uses linear space in expectation. In practice, this algorithm
 * is out-performed by almost all simpler algorithms. Note that only undirected graphs are supported.
 * <p>
 * Based on "A randomized linear-time algorithm to find minimum spanning trees" by Karger, David R.; Klein, Philip N.;
 * Tarjan, Robert E. (1995).
 *
 * @author Barak Ugav
 */
class MinimumSpanningTreeKargerKleinTarjan extends MinimumSpanningTreeUtils.AbstractUndirected {

	private final Random rand;
	private final WeaklyConnectedComponentsAlgo ccAlg = WeaklyConnectedComponentsAlgo.newInstance();
	private final MinimumSpanningTreeBoruvka boruvka = new MinimumSpanningTreeBoruvka();
	private final TreePathMaxima tpm = new TreePathMaximaHagerup();

	private final AllocatedMemory allocatedMem = new AllocatedMemory();

	/**
	 * Create a new MST algorithm with random seed.
	 */
	MinimumSpanningTreeKargerKleinTarjan() {
		this(System.nanoTime() ^ 0x905a1dad25b30034L);
	}

	/**
	 * Create a new MST algorithm with the given seed.
	 *
	 * @param seed a seed used for all random generators
	 */
	public MinimumSpanningTreeKargerKleinTarjan(long seed) {
		rand = new Random(seed ^ 0x1af7babf9783fd8bL);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		return new MinimumSpanningTreeUtils.ResultImpl(computeMST(g, w));
	}

	private IntCollection computeMST(IndexGraph g, IWeightFunction w) {
		if (g.vertices().size() == 0 || g.edges().size() == 0)
			return IntLists.emptyList();

		/* Run Boruvka to reduce the number of vertices by a factor of 4 by contraction */
		MinimumSpanningTreeBoruvka.RunBoruvkaResult g0Res = boruvka.runBoruvka(g, w, 2);
		IndexGraph g0 = g0Res.contractedG;
		IntCollection f0 = g0Res.mstEdges;
		int[] g0Ref = g0Res.edgeRef;

		/* Find a random subgraph G1 in the contracted graph G0, by choosing each edge with probability 0.5 */
		Pair<IndexGraph, int[]> g1Res = randSubgraph(g0, g0Ref);
		IndexGraph g1 = g1Res.first();
		int[] g1Ref = g1Res.second();
		IWeightsDouble g1W = assignWeightsFromEdgeRef(g1, w, g1Ref);

		/* Compute an MST (actually a forest) F1 in the random subgraph G1 */
		IntCollection f1Edges = computeMST(g1, g1W);
		Pair<IndexGraph, int[]> f1Res = subGraph(g1, f1Edges, g1Ref);
		IndexGraph f1 = f1Res.first();
		int[] f1Ref = f1Res.second();

		/* Find all the light edges in G0 with respect to the computed forest F1 */
		IntCollection e2 = lightEdges(g0, e -> w.weight(g0Ref[e]), f1, e -> w.weight(f1Ref[e]));
		Pair<IndexGraph, int[]> g2Res = subGraph(g0, e2, g0Ref);
		IndexGraph g2 = g2Res.first();
		int[] g2Ref = g2Res.second();
		IWeightsDouble g2W = assignWeightsFromEdgeRef(g2, w, g2Ref);

		/* The result is F0 and F2 */
		IntCollection f2 = computeMST(g2, g2W);
		for (int eRef : f2)
			f0.add(g2Ref[eRef]);
		return f0;
	}

	static Pair<IndexGraph, int[]> subGraph(IndexGraph g, IntCollection edgeSet, int[] edgeRef) {
		final int n = g.vertices().size();
		IndexGraphBuilder subBuilder = IndexGraphBuilder.newUndirected();
		subBuilder.expectedVerticesNum(n);
		subBuilder.expectedEdgesNum(edgeSet.size());
		for (int v = 0; v < n; v++) {
			int vSub = subBuilder.addVertex();
			assert v == vSub;
		}
		int[] edgeRefSub = new int[edgeSet.size()];
		for (int e : edgeSet) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int eSub = subBuilder.addEdge(u, v);
			edgeRefSub[eSub] = edgeRef[e];
		}
		IndexGraph subG = subBuilder.build();
		return Pair.of(subG, edgeRefSub);
	}

	static IWeightsDouble assignWeightsFromEdgeRef(IndexGraph g, IWeightFunction w, int[] edgeRef) {
		IWeightsDouble w2 = IWeights.createExternalEdgesWeights(g, double.class);
		for (int m = g.edges().size(), e = 0; e < m; e++)
			w2.set(e, w.weight(edgeRef[e]));
		return w2;
	}

	private Pair<IndexGraph, int[]> randSubgraph(IndexGraph g, int[] edgeRef) {
		allocatedMem.allocateForRandSubGraph();
		IntCollection edgeSet = allocatedMem.edgeList;
		edgeSet.clear();
		for (int m = g.edges().size(), e = 0; e < m; e++)
			if (rand.nextBoolean())
				edgeSet.add(e);
		return subGraph(g, edgeSet, edgeRef);
	}

	private IntCollection lightEdges(IndexGraph g, Int2DoubleFunction gw, IndexGraph f, Int2DoubleFunction fw) {
		final int n = f.vertices().size();
		/* find connected components in the forest, each one of them is a tree */
		IVertexPartition connectivityRes = ccAlg.findWeaklyConnectedComponents(f);
		final int treeCount = connectivityRes.numberOfBlocks();
		Int2IntFunction vToTree = connectivityRes::vertexBlock;

		allocatedMem.allocateForLightEdges(n, treeCount);

		IndexGraph[] trees = allocatedMem.trees;
		IWeightsDouble[] treeData = allocatedMem.treeData;
		for (int t = 0; t < treeCount; t++)
			treeData[t] = trees[t].getEdgesWeights("weight");

		int[] vToVnew = allocatedMem.vToVnew;
		for (int u = 0; u < n; u++) {
			int ut = vToTree.applyAsInt(u);
			vToVnew[u] = trees[ut].addVertex();
		}

		for (int m = f.edges().size(), e = 0; e < m; e++) {
			int u = f.edgeSource(e), v = f.edgeTarget(e);
			int un = vToVnew[u], vn = vToVnew[v];
			int treeIdx = vToTree.applyAsInt(u);
			int en = trees[treeIdx].addEdge(un, vn);
			treeData[treeIdx].set(en, fw.get(e));
		}

		// use the tree path maxima to find the heaviest edge in the path connecting u v for each edge in g
		TreePathMaxima.IQueries[] tpmQueries = allocatedMem.tpmQueries;
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree.applyAsInt(u);
			if (ut != vToTree.applyAsInt(v))
				continue;
			tpmQueries[ut].addQuery(vToVnew[u], vToVnew[v]);
		}

		TreePathMaxima.IResult[] tpmResults = allocatedMem.tpmResults;
		for (int t = 0; t < treeCount; t++) {
			tpmResults[t] =
					(TreePathMaxima.IResult) tpm.computeHeaviestEdgeInTreePaths(trees[t], treeData[t], tpmQueries[t]);
			tpmQueries[t].clear();
		}

		// Find all light edge by comparing each edge in g to the heaviest edge on the path from u to v in f
		IntCollection lightEdges = allocatedMem.edgeList;
		lightEdges.clear();
		int[] tpmIdx = allocatedMem.vToVnew;
		Arrays.fill(tpmIdx, 0, treeCount, 0);
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree.applyAsInt(u);
			if (ut != vToTree.applyAsInt(v)
					|| gw.get(e) <= treeData[ut].weight(tpmResults[ut].getHeaviestEdgeInt(tpmIdx[ut]++)))
				lightEdges.add(e);
		}
		for (int t = 0; t < treeCount; t++)
			trees[t].clear();
		return lightEdges;
	}

	private static class AllocatedMemory {
		IntList edgeList;

		IndexGraph[] trees = MemoryReuse.EmptyGraphArr;
		int[] vToVnew = IntArrays.EMPTY_ARRAY;
		IWeightsDouble[] treeData = MemoryReuse.EmptyWeightsDoubleArr;

		TreePathMaxima.IQueries[] tpmQueries = MemoryReuse.EmptyTpmQueriesArr;
		TreePathMaxima.IResult[] tpmResults = MemoryReuse.EmptyTpmResultArr;

		void allocateForRandSubGraph() {
			edgeList = MemoryReuse.ensureAllocated(edgeList, IntArrayList::new);
		}

		void allocateForLightEdges(int n, int treeCount) {
			edgeList = MemoryReuse.ensureAllocated(edgeList, IntArrayList::new);

			trees = MemoryReuse.ensureLength(trees, treeCount);

			treeData = MemoryReuse.ensureLength(treeData, treeCount);
			vToVnew = MemoryReuse.ensureLength(vToVnew, n);

			tpmQueries = MemoryReuse.ensureLength(tpmQueries, treeCount);
			tpmResults = MemoryReuse.ensureLength(tpmResults, treeCount);

			for (int tIdx = 0; tIdx < treeCount; tIdx++) {
				IndexGraph tree =
						trees[tIdx] = MemoryReuse.ensureAllocated(trees[tIdx], () -> IndexGraph.newUndirected());
				treeData[tIdx] =
						MemoryReuse.ensureAllocated(treeData[tIdx], () -> tree.addEdgesWeights("weight", double.class));
				tpmQueries[tIdx] =
						MemoryReuse.ensureAllocated(tpmQueries[tIdx], () -> TreePathMaxima.IQueries.newInstance());
			}
		}
	}

}
