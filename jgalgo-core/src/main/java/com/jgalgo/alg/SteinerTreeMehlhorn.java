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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.function.IntConsumer;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Mehlhorn algorithm for Steiner tree approximation.
 *
 * <p>
 * The Steiner tree problem is NP-hard, and this algorithm provides a \(2(1-1/l)\)-approximation where \(l\) is the
 * minimum number of leaves in any Steiner tree for the given graph. Note that \(l\) is always smaller or equal to the
 * number of terminals vertices.
 *
 * <p>
 * The algorithm runs in \(O(m+n \log n)\) time and use linear space.
 *
 * <p>
 * Based on 'A faster approximation algorithm for the Steiner problem in graphs' by Kurt Mehlhorn.
 *
 * @author Barak Ugav
 */
class SteinerTreeMehlhorn implements SteinerTreeAlgoBase {

	private final VoronoiAlgo voronoiAlgo = VoronoiAlgo.newInstance();
	private final MinimumSpanningTree mstAlgo = MinimumSpanningTree.newInstance();

	@Override
	public SteinerTreeAlgo.IResult computeSteinerTree(IndexGraph g, IWeightFunction w, IntCollection terminals) {
		Assertions.onlyUndirected(g);
		final int n = g.vertices().size();
		if (terminals.isEmpty())
			throw new IllegalArgumentException("no terminals provided");
		final int terminalNum = terminals.size();
		Bitmap isTerminal = new Bitmap(n);
		for (int t : terminals) {
			if (isTerminal.get(t))
				throw new IllegalArgumentException("Duplicate terminal: " + t);
			isTerminal.set(t);
		}

		/* 1.1. Compute the Voronoi cells of the terminals */
		VoronoiAlgo.IResult cells = (VoronoiAlgo.IResult) voronoiAlgo.computeVoronoiCells(g, terminals, w);

		/*
		 * 1.2. Build the subgraph G'1 where each vertex is a terminal node and the edge connecting each pair of
		 * terminal nodes (s,t) is the minimum edge {d(s,u),(u,v),(v,t)} where u is in the cell of s and v is in the
		 * cell of t.
		 */
		IndexGraphBuilder g1Builder = IndexGraphBuilder.undirected();
		g1Builder.addVertices(range(terminalNum));
		IntArrayList g1EdgeRef0 = new IntArrayList();
		DoubleArrayList g1EdgeWeight0 = new DoubleArrayList();
		int[] neighborsBestEdge = new int[terminalNum];
		double[] neighborsBestWeight = new double[terminalNum];
		Arrays.fill(neighborsBestEdge, -1);
		IntList neighbors = new IntArrayList(terminalNum);
		for (int t = 0; t < terminalNum; t++) {
			assert neighbors.isEmpty() && Arrays.stream(neighborsBestEdge).allMatch(e -> e == -1);
			for (int u : cells.blockVertices(t)) {
				double uDistance = cells.distance(u);
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					int vT = cells.vertexBlock(v);
					if (vT <= t)
						continue;
					double ew = uDistance + w.weight(e) + cells.distance(v);
					if (neighborsBestEdge[vT] == -1) {
						neighborsBestEdge[vT] = e;
						neighborsBestWeight[vT] = ew;
						neighbors.add(vT);
					} else if (ew < neighborsBestWeight[vT]) {
						neighborsBestEdge[vT] = e;
						neighborsBestWeight[vT] = ew;
					}
				}
			}
			for (int vT : neighbors) {
				g1Builder.addEdge(t, vT);
				g1EdgeRef0.add(neighborsBestEdge[vT]);
				g1EdgeWeight0.add(neighborsBestWeight[vT]);
				neighborsBestEdge[vT] = -1;
			}
			neighbors.clear();
		}
		IndexGraph g1 = g1Builder.build();
		g1Builder.clear();
		int[] g1EdgeRef = g1EdgeRef0.elements();
		double[] g1EdgeWeight = g1EdgeWeight0.elements();
		IWeightFunction g1WeightFunc = e -> g1EdgeWeight[e];

		/* 2. Find a minimum spanning tree G2 of G1 */
		IntCollection g2 = ((MinimumSpanningTree.IResult) mstAlgo.computeMinimumSpanningTree(g1, g1WeightFunc)).edges();

		/* 3. Construct a subgraph G3 of G by replacing each edge in G2 by its corresponding shortest path in G */
		IndexGraphBuilder g3Builder = g1Builder;
		g3Builder.addVertices(g.vertices());
		Bitmap g3Edges = new Bitmap(g.edges().size());
		IntArrayList g3EdgeRef0 = new IntArrayList();
		DoubleArrayList g3EdgeWeight0 = g1EdgeWeight0;
		g3EdgeWeight0.clear();
		IntConsumer g3AddEdge = e -> {
			if (!g3Edges.get(e)) {
				g3Builder.addEdge(g.edgeSource(e), g.edgeTarget(e));
				g3EdgeRef0.add(e);
				g3EdgeWeight0.add(w.weight(e));
				g3Edges.set(e);
			}
		};
		for (int e2 : g2) {
			int e = g1EdgeRef[e2];
			/* add path from a terminal node to the original source of e */
			for (int e0 : cells.getPath(g.edgeSource(e)).edges())
				g3AddEdge.accept(e0);
			/* add cross voronoi-cells edge e */
			g3AddEdge.accept(e);
			/* add path from the original target of e to a terminal node */
			for (int e0 : cells.getPath(g.edgeTarget(e)).edges())
				g3AddEdge.accept(e0);
		}
		IndexGraph g3 = g3Builder.build();
		g3Builder.clear();
		int[] g3EdgeRef = g3EdgeRef0.elements();
		double[] g3EdgeWeight = g3EdgeWeight0.elements();
		IWeightFunction g3WeightFunc = e -> g3EdgeWeight[e];

		/* 4. Find the minimum spanning tree G4 of G3 */
		IntCollection g4 = ((MinimumSpanningTree.IResult) mstAlgo.computeMinimumSpanningTree(g3, g3WeightFunc)).edges();
		int[] g4Edges = new int[g4.size() * 2];
		int[] g4EdgesOffset = new int[n + 1];
		for (int e4 : g4) {
			g4EdgesOffset[g3.edgeSource(e4)]++;
			g4EdgesOffset[g3.edgeTarget(e4)]++;
		}
		for (int s = 0, v = 0; v < n; v++) {
			int k = g4EdgesOffset[v];
			g4EdgesOffset[v] = s;
			s += k;
		}
		for (int e4 : g4) {
			g4Edges[g4EdgesOffset[g3.edgeSource(e4)]++] = e4;
			g4Edges[g4EdgesOffset[g3.edgeTarget(e4)]++] = e4;
		}
		for (int v = n; v > 0; v--)
			g4EdgesOffset[v] = g4EdgesOffset[v - 1];
		assert g4EdgesOffset[n] == g4Edges.length;
		g4EdgesOffset[0] = 0;

		/* 5. Construct a Steiner tree G5 from G4 by deleting edges so that no leaves are Steiner vertices */
		int[] g5Degree = new int[n];
		for (int v = 0; v < n; v++)
			g5Degree[v] = g4EdgesOffset[v + 1] - g4EdgesOffset[v];
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		for (int v = 0; v < n; v++)
			if (g5Degree[v] == 1 && !isTerminal.get(v))
				queue.enqueue(v);
		Bitmap g5Edges = new Bitmap(g4Edges.length);
		for (int e4 : g4)
			g5Edges.set(e4);
		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			assert g5Degree[u] <= 1;
			if (g5Degree[u] == 0)
				continue; /* parent edge already removed */
			int e4;
			for (int eIdx = g4EdgesOffset[u];; eIdx++) {
				assert eIdx < g4EdgesOffset[u + 1];
				if (g5Edges.get(g4Edges[eIdx])) {
					e4 = g4Edges[eIdx];
					break;
				}
			}
			g5Edges.clear(e4);
			int v = g3.edgeEndpoint(e4, u);
			g5Degree[u]--;
			g5Degree[v]--;
			if (g5Degree[v] == 1 && !isTerminal.get(v))
				queue.enqueue(v);
		}
		int[] g5 = new int[g5Edges.cardinality()];
		int g5EdgeIdx = 0;
		for (int e3 : g5Edges)
			g5[g5EdgeIdx++] = g3EdgeRef[e3];

		return new SteinerTrees.IndexResult(ImmutableIntArraySet.withNaiveContains(g5));
	}

}
