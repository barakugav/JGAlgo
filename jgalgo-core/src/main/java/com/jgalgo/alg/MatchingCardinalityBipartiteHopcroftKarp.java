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
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Hopcroft–Karp maximum unweighted matching algorithm for undirected bipartite graphs.
 *
 * <p>
 * The algorithm runs in \(O(m \sqrt{n})\) and it uses linear space.
 *
 * <p>
 * Based on "A n^5/2 Algorithm for Maximum Matchings in Bipartite Graphs" by J. Hopcroft and R. Karp (1973).
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class MatchingCardinalityBipartiteHopcroftKarp extends Matchings.AbstractCardinalityMatchingImpl {

	/**
	 * Create a new maximum matching object.
	 */
	MatchingCardinalityBipartiteHopcroftKarp() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException     if the bipartiteness vertices weights is not found. See
	 *                                      {@link BipartiteGraphs#VertexBiPartitionWeightKey}.
	 * @throws IllegalArgumentException if the graph is no bipartite with respect to the provided partition
	 */
	@Override
	IMatching computeMaximumCardinalityMatching(IndexGraph g) {
		Assertions.Graphs.onlyUndirected(g);
		int n = g.vertices().size();

		IWeightsBool partition = Assertions.Graphs.onlyBipartite(g);

		/* BFS */
		int[] depths = new int[n];
		IntPriorityQueue bfsQueue = new FIFOQueueIntNoReduce();

		/* DFS */
		Bitmap visited = new Bitmap(n);
		IEdgeIter[] edges = new IEdgeIter[n / 2 + 1];
		int[] dfsPath = new int[(n - 1) / 2 + 1];

		int[] matched = new int[n];
		final int MatchedNone = -1;
		Arrays.fill(matched, MatchedNone);

		Bitmap es = new Bitmap(g.edges().size());

		for (;;) {
			/* Perform BFS to build the alternating forest */
			bfsQueue.clear();
			Arrays.fill(depths, Integer.MAX_VALUE);
			for (int u = 0; u < n; u++) {
				if (!partition.get(u) || matched[u] != MatchedNone)
					continue;
				depths[u] = 0;
				bfsQueue.enqueue(u);
			}
			int unmatchedTDepth = Integer.MAX_VALUE;
			while (!bfsQueue.isEmpty()) {
				int u = bfsQueue.dequeueInt();
				int depth = depths[u];
				if (depth >= unmatchedTDepth)
					continue;

				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					if (depths[v] < depth)
						continue;
					es.set(e);
					if (depths[v] != Integer.MAX_VALUE)
						continue;
					depths[v] = depth + 1;

					int matchedEdge = matched[v];
					if (matchedEdge != MatchedNone) {
						es.set(matchedEdge);
						int w = g.edgeEndpoint(matchedEdge, v);
						v = w;
						depths[v] = depth + 2;
						bfsQueue.enqueue(v);
					} else {
						unmatchedTDepth = depth + 1;
					}
				}
			}
			if (unmatchedTDepth == Integer.MAX_VALUE)
				break;

			/*
			 * Run DFS to find the maximal number of paths from unmatched S vertices to unmatched T vertices
			 */
			for (int u = 0; u < n; u++) {
				if (!partition.get(u) || matched[u] != MatchedNone)
					continue;

				edges[0] = g.outEdges(u).iterator();
				visited.set(u);

				for (int depth = 0; depth >= 0;) {
					IEdgeIter eit = edges[depth];
					if (eit.hasNext()) {
						int e = eit.nextInt();
						if (!es.get(e))
							continue;
						int v = eit.targetInt();
						if (visited.get(v) || depth >= depths[v])
							continue;
						visited.set(v);
						dfsPath[depth++] = e;

						int matchedEdge = matched[v];
						if (matchedEdge == MatchedNone) {
							/* Augmenting path found */
							for (int i = 0; i < depth; i++) {
								int e1 = dfsPath[i];
								matched[g.edgeSource(e1)] = matched[g.edgeTarget(e1)] = e1;
							}
							break;
						}
						v = g.edgeEndpoint(matchedEdge, v);
						edges[depth] = g.outEdges(v).iterator();
					} else {
						/* go back up in the DFS */
						depth--;
					}
				}
			}
			visited.clear();
			es.clear();
		}
		Arrays.fill(edges, null);
		return new Matchings.MatchingImpl(g, matched);
	}

}
