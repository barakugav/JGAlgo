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

package com.jgalgo.alg.span;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Random;
import com.jgalgo.alg.IVertexPartition;
import com.jgalgo.alg.RandomizedAlgorithm;
import com.jgalgo.alg.connect.WeaklyConnectedComponentsAlgo;
import com.jgalgo.alg.tree.TreePathMaxima;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Karger, Klein and Tarjan randomized linear minimum spanning tree algorithm
 *
 * <p>
 * The algorithm runs in \(O(n + m)\) expected time, and uses linear space in expectation. In practice, this algorithm
 * is out-performed by almost all simpler algorithms. Note that only undirected graphs are supported.
 *
 * <p>
 * Based on "A randomized linear-time algorithm to find minimum spanning trees" by Karger, David R.; Klein, Philip N.;
 * Tarjan, Robert E. (1995).
 *
 * @author Barak Ugav
 */
class MinimumSpanningTreeKargerKleinTarjan extends MinimumSpanningTrees.AbstractUndirected
		implements RandomizedAlgorithm {

	private final Random rand = new Random();
	private final WeaklyConnectedComponentsAlgo ccAlg = WeaklyConnectedComponentsAlgo.newInstance();
	private final MinimumSpanningTreeBoruvka boruvka = new MinimumSpanningTreeBoruvka();
	private final TreePathMaxima tpm = TreePathMaxima.newInstance();

	private final AllocatedMemory allocatedMem = new AllocatedMemory();

	@Override
	public void setSeed(long seed) {
		rand.setSeed(seed);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);

		IntArrayList mst = computeMst(g, w);
		IntSet mstSet = ImmutableIntArraySet.withNaiveContains(mst.elements(), 0, mst.size());
		return new MinimumSpanningTrees.IndexResult(mstSet);
	}

	private IntArrayList computeMst(IndexGraph g, IWeightFunction w) {
		if (g.vertices().size() == 0 || g.edges().size() == 0)
			return new IntArrayList();

		/* Run Boruvka to reduce the number of vertices by a factor of 4 by contraction */
		MinimumSpanningTreeBoruvka.RunBoruvkaResult g0Res = boruvka.runBoruvka(g, w, 2);
		IndexGraph g0 = g0Res.contractedG;
		IntArrayList f0 = new IntArrayList(g0Res.mstEdges);
		int[] g0Ref = g0Res.edgeRef;

		/* Find a random subgraph G1 in the contracted graph G0, by choosing each edge with probability 0.5 */
		Pair<IndexGraph, int[]> g1Res = randSubgraph(g0, g0Ref);
		IndexGraph g1 = g1Res.first();
		int[] g1Ref = g1Res.second();
		IWeightsDouble g1W = assignWeightsFromEdgeRef(g1, w, g1Ref);

		/* Compute an MST (actually a forest) F1 in the random subgraph G1 */
		IntCollection f1Edges = computeMst(g1, g1W);
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
		IntCollection f2 = computeMst(g2, g2W);
		for (int eRef : f2)
			f0.add(g2Ref[eRef]);
		return f0;
	}

	static Pair<IndexGraph, int[]> subGraph(IndexGraph g, IntCollection edgeSet, int[] edgeRef) {
		final int n = g.vertices().size();
		IndexGraphBuilder subBuilder = IndexGraphBuilder.undirected();
		subBuilder.addVertices(range(n));
		subBuilder.ensureEdgeCapacity(edgeSet.size());
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
		for (int e : range(g.edges().size()))
			w2.set(e, w.weight(edgeRef[e]));
		return w2;
	}

	private Pair<IndexGraph, int[]> randSubgraph(IndexGraph g, int[] edgeRef) {
		allocatedMem.allocateForRandSubGraph();
		IntCollection edgeSet = allocatedMem.edgeList;
		edgeSet.clear();
		for (int e : range(g.edges().size()))
			if (rand.nextBoolean())
				edgeSet.add(e);
		return subGraph(g, edgeSet, edgeRef);
	}

	private IntCollection lightEdges(IndexGraph g, Int2DoubleFunction gw, IndexGraph f, Int2DoubleFunction fw) {
		final int n = f.vertices().size();
		/* find connected components in the forest, each one of them is a tree */
		IVertexPartition connectivityRes = (IVertexPartition) ccAlg.findWeaklyConnectedComponents(f);
		final int treeCount = connectivityRes.numberOfBlocks();
		Int2IntFunction vToTree = connectivityRes::vertexBlock;

		allocatedMem.allocateForLightEdges(n, treeCount);

		IndexGraph[] trees = allocatedMem.trees;
		IWeightsDouble[] treeData = new IWeightsDouble[treeCount];
		for (int t : range(treeCount))
			treeData[t] = trees[t].addEdgesWeights("weight", double.class);

		int[] vToVnew = allocatedMem.vToVnew;
		for (int u : range(n)) {
			int ut = vToTree.applyAsInt(u);
			vToVnew[u] = trees[ut].addVertexInt();
		}

		for (int e : range(f.edges().size())) {
			int u = f.edgeSource(e), v = f.edgeTarget(e);
			int un = vToVnew[u], vn = vToVnew[v];
			int treeIdx = vToTree.applyAsInt(u);
			int en = trees[treeIdx].addEdge(un, vn);
			treeData[treeIdx].set(en, fw.get(e));
		}

		// use the tree path maxima to find the heaviest edge in the path connecting u v for each edge in g
		TreePathMaxima.IQueries[] tpmQueries = allocatedMem.tpmQueries;
		for (int e : range(g.edges().size())) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree.applyAsInt(u);
			if (ut != vToTree.applyAsInt(v))
				continue;
			tpmQueries[ut].addQuery(vToVnew[u], vToVnew[v]);
		}

		TreePathMaxima.IResult[] tpmResults = allocatedMem.tpmResults;
		for (int t : range(treeCount)) {
			tpmResults[t] =
					(TreePathMaxima.IResult) tpm.computeHeaviestEdgeInTreePaths(trees[t], treeData[t], tpmQueries[t]);
			tpmQueries[t].clear();
		}

		// Find all light edge by comparing each edge in g to the heaviest edge on the path from u to v in f
		IntCollection lightEdges = allocatedMem.edgeList;
		lightEdges.clear();
		int[] tpmIdx = allocatedMem.vToVnew;
		Arrays.fill(tpmIdx, 0, treeCount, 0);
		for (int e : range(g.edges().size())) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree.applyAsInt(u);
			if (ut != vToTree.applyAsInt(v)
					|| gw.get(e) <= treeData[ut].weight(tpmResults[ut].getHeaviestEdgeInt(tpmIdx[ut]++)))
				lightEdges.add(e);
		}
		for (int t : range(treeCount))
			trees[t].clear();
		return lightEdges;
	}

	private static class AllocatedMemory {
		IntList edgeList;

		IndexGraph[] trees = new IndexGraph[0];
		int[] vToVnew = IntArrays.EMPTY_ARRAY;

		TreePathMaxima.IQueries[] tpmQueries = new TreePathMaxima.IQueries[0];
		TreePathMaxima.IResult[] tpmResults = new TreePathMaxima.IResult[0];

		void allocateForRandSubGraph() {
			if (edgeList == null)
				edgeList = new IntArrayList();
		}

		void allocateForLightEdges(int n, int treeCount) {
			if (edgeList == null)
				edgeList = new IntArrayList();

			if (trees.length < treeCount) {
				int oldLen = trees.length;
				trees = Arrays.copyOf(trees, treeCount);
				for (int tIdx : range(oldLen, treeCount))
					trees[tIdx] = IndexGraph.newUndirected();
			}
			if (vToVnew.length < n)
				vToVnew = new int[n];

			if (tpmQueries.length < treeCount) {
				int oldLen = tpmQueries.length;
				tpmQueries = Arrays.copyOf(tpmQueries, treeCount);
				for (int tIdx : range(oldLen, treeCount))
					tpmQueries[tIdx] = TreePathMaxima.IQueries.newInstance();
			}
			if (tpmResults.length < treeCount)
				tpmResults = Arrays.copyOf(tpmResults, treeCount);
		}
	}

}
