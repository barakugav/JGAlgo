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

package com.jgalgo.alg.match;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.UnionFind;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Gabow's implementation of Endmond's algorithm for cardinality maximum matching in general graphs.
 *
 * <p>
 * The algorithm runs \(n\) iterations, in each one the matching is increased by one. In each iteration, a BFS is run
 * from the unmatched vertices using only alternating paths, searching for an augmenting path. When a blossom is
 * detected, the algorithm contract it to a 'super vertex' and continue in the BFS. Instead of storing the blossom
 * explicitly as stated in the paper, we use {@link UnionFind} to implicitly represent the blossoms. Each iteration
 * require \(O(m n \cdot \alpha (m, n))\) time, where \(\alpha (\cdot, \cdot)\) is inverse Ackermann's function.
 *
 * <p>
 * Based on 'An Efficient Implementation of Edmonds Algorithm for Maximum Matching on Graphs' by Harold N. Gabow (1976).
 * Although the original paper stated the running time is \(O(n^3)\), we implement it using {@link UnionFind}, and the
 * running time is \(O(m n \alpha (m, n))\).
 *
 * @author Barak Ugav
 */
public class MatchingCardinalityGabow1976 extends MatchingAlgoAbstractCardinality {

	/**
	 * Create a new maximum matching object.
	 */
	public MatchingCardinalityGabow1976() {}

	@Override
	protected IMatching computeMaximumCardinalityMatching(IndexGraph g) {
		Assertions.onlyUndirected(g);
		int n = g.vertices().size();

		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		int[] root = new int[n];
		Bitmap isEven = new Bitmap(n);

		final int EdgeNone = -1;
		int[] matched = new int[n];
		Arrays.fill(matched, EdgeNone);
		int[] bridge = new int[n * 2];
		int[] parent = new int[n]; // vertex -> edge

		int[] augPath = new int[n];
		Bitmap setmatch = new Bitmap(n);

		int[] blossomBaseSearchNotes = new int[n];
		int blossomBaseSearchNotesIndex = 0;
		int[] blossomVertices = new int[n];

		UnionFind uf = UnionFind.newInstance();
		int[] bases = new int[n];

		for (;;) {
			Arrays.fill(root, -1);

			uf.makeMany(n);
			for (int u : range(n))
				bases[u] = u;

			int augPathSize = 0;

			queue.clear();
			for (int u : range(n)) {
				if (matched[u] != EdgeNone)
					continue;
				root[u] = u;
				isEven.set(u);
				queue.enqueue(u);
			}

			bfs: while (!queue.isEmpty()) {
				final int u = queue.dequeueInt();
				int uRoot = root[u];

				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					final int e = eit.nextInt();
					final int v = eit.targetInt();
					int vRoot = root[v];

					if (vRoot < 0) {
						// unexplored vertex, add to tree
						root[v] = uRoot;
						parent[v] = e;

						int matchedEdge = matched[v];
						int w = g.edgeEndpoint(matchedEdge, v);
						root[w] = uRoot;
						isEven.set(w);
						queue.enqueue(w);
						continue;
					}

					int vBase = bases[uf.find(v)];
					if (!isEven.get(vBase))
						// edge to an odd vertex in some tree, ignore
						continue;

					if (vRoot == uRoot) {
						// Blossom
						int uBase = bases[uf.find(u)];
						if (uBase == vBase)
							// edge within existing blossom, ignore
							continue;

						// Find base for the new blossom
						int base, searchIdx = ++blossomBaseSearchNotesIndex;
						blossomBaseSearch: for (int[] ps = new int[] { uBase, vBase };;) {
							for (int i : range(ps.length)) {
								int p = ps[i];
								if (p < 0)
									continue;
								if (blossomBaseSearchNotes[p] == searchIdx) {
									base = p;
									break blossomBaseSearch;
								}
								blossomBaseSearchNotes[p] = searchIdx;
								if (p != uRoot) {
									p = g.edgeEndpoint(matched[p], p);
									p = g.edgeEndpoint(parent[p], p); // move 2 up
									ps[i] = bases[uf.find(p)];
								} else
									ps[i] = -1;
							}
						}

						// Find all vertices of the blossom
						int blossomVerticesSize = 0;
						for (int p : new int[] { uBase, vBase }) {
							final int bridgeEdge = e, bridgeVertex = p == uBase ? u : v;
							while (p != base) {
								// handle even vertex
								blossomVertices[blossomVerticesSize++] = p;

								// handle odd vertex
								p = g.edgeEndpoint(matched[p], p);
								blossomVertices[blossomVerticesSize++] = p;
								queue.enqueue(p); // add the odd vertex that became even to the queue
								bridge[p * 2 + 0] = bridgeEdge;
								bridge[p * 2 + 1] = bridgeVertex;

								p = bases[uf.find(g.edgeEndpoint(parent[p], p))];
							}
						}

						// Union all UF elements in the new blossom
						for (int i : range(blossomVerticesSize))
							uf.union(base, blossomVertices[i]);
						bases[uf.find(base)] = base; // make sure the UF value is the base

					} else {
						// augmenting path
						augPathSize = findPath(g, u, uRoot, isEven, matched, parent, bridge, augPath, 0);
						augPath[augPathSize++] = e;
						augPathSize = findPath(g, v, vRoot, isEven, matched, parent, bridge, augPath, augPathSize);
						break bfs;
					}
				}
			}
			if (augPathSize == 0)
				break;

			for (int i : range(augPathSize)) {
				int e = augPath[i];
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				setmatch.set(i, matched[u] == EdgeNone || g.edgeTarget(matched[u]) != v);
			}
			for (int i : range(augPathSize)) {
				int e = augPath[i];
				if (setmatch.get(i))
					matched[g.edgeSource(e)] = matched[g.edgeTarget(e)] = e;
			}

			isEven.clear();
			uf.clear();
		}

		return new IndexMatching(g, matched);
	}

	private static int findPath(IndexGraph g, int s, int t, Bitmap isEven, int[] match, int[] parent, int[] bridge,
			int[] path, int pathSize) {
		if (s == t)
			return pathSize;
		if (isEven.get(s)) {
			int v = g.edgeEndpoint(match[s], s);
			path[pathSize++] = match[s];
			path[pathSize++] = parent[v];
			return findPath(g, g.edgeEndpoint(parent[v], v), t, isEven, match, parent, bridge, path, pathSize);
		} else {
			int vw = bridge[s * 2 + 0];
			int v = bridge[s * 2 + 1], w = g.edgeEndpoint(vw, v);
			path[pathSize++] = match[s];
			pathSize = findPath(g, v, g.edgeEndpoint(match[s], s), isEven, match, parent, bridge, path, pathSize);
			path[pathSize++] = vw;
			return findPath(g, w, t, isEven, match, parent, bridge, path, pathSize);
		}
	}

}
