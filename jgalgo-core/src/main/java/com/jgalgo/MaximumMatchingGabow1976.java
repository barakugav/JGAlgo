package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Gabow's implementation of Endmond's algorithm for cardinality maximum
 * matching in general graphs.
 * <p>
 * The algorithm runs {@code n} iterations, in each one the matching is
 * increased by one. In each iteration, a BFS is run from the unmatched vertices
 * using only alternating paths, searching for an augmenting path. When a
 * blossom is detected, the algorithm contract it to a 'super vertex' and
 * continue in the BFS. Instead of storing the blossom explicitly as stated in
 * the paper, we use {@link UnionFind} to implicitly represent the blossoms.
 * Each iteration require {@code m \alpha (m, n)} time, where
 * {@code \alpha (m, n)} is inverse Ackermann's function.
 * <p>
 * Based on 'An Efficient Implementation of Edmonds Algorithm for Maximum
 * Matching on Graphs' by Harold N. Gabow (1976). Although the original paper
 * stated the running time is {@code O(n^3)}, we implement it using
 * {@link UnionFind}, and the running time is {@code O(m n \alpha (m, n))}.
 *
 * @author Barak Ugav
 */
public class MaximumMatchingGabow1976 implements MaximumMatching {

	/**
	 * Create a new maximum matching object.
	 */
	public MaximumMatchingGabow1976() {
	}

	@Override
	public IntCollection computeMaximumMatching(UGraph g) {
		int n = g.vertices().size();

		IntPriorityQueue queue = new IntArrayFIFOQueue();
		int[] root = new int[n];
		BitSet isEven = new BitSet(n);

		final int EdgeNone = -1;
		int[] matched = new int[n];
		Arrays.fill(matched, EdgeNone);
		int[] bridge = new int[n * 2];
		int[] parent = new int[n]; // vertex -> edge

		int[] augPath = new int[n];
		BitSet setmatch = new BitSet(n);

		int[] blossomBaseSearchNotes = new int[n];
		int blossomBaseSearchNotesIndex = 0;
		int[] blossomVertices = new int[n];

		UnionFind uf = new UnionFindArray();
		int[] bases = new int[n];

		for (;;) {
			Arrays.fill(root, -1);

			for (int u = 0; u < n; u++) {
				uf.make();
				bases[u] = u;
			}

			int augPathSize = 0;

			queue.clear();
			for (int u = 0; u < n; u++) {
				if (matched[u] != EdgeNone)
					continue;
				root[u] = u;
				isEven.set(u);
				queue.enqueue(u);
			}

			bfs: while (!queue.isEmpty()) {
				final int u = queue.dequeueInt();
				int uRoot = root[u];

				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					final int e = eit.nextInt();
					final int v = eit.v();
					int vRoot = root[v];

					if (vRoot == -1) {
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
							for (int i = 0; i < ps.length; i++) {
								int p = ps[i];
								if (p == -1)
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
						for (int i = 0; i < blossomVerticesSize; i++)
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

			for (int i = 0; i < augPathSize; i++) {
				int e = augPath[i];
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				setmatch.set(i, matched[u] == EdgeNone || g.edgeTarget(matched[u]) != v);
			}
			for (int i = 0; i < augPathSize; i++) {
				int e = augPath[i];
				if (setmatch.get(i))
					matched[g.edgeSource(e)] = matched[g.edgeTarget(e)] = e;
			}

			isEven.clear();
			uf.clear();
		}

		IntList res = new IntArrayList();
		for (int u = 0; u < n; u++)
			if (matched[u] != EdgeNone && u == g.edgeSource(matched[u]))
				res.add(matched[u]);
		return res;
	}

	private static int findPath(UGraph g, int s, int t, BitSet isEven, int[] match, int[] parent, int[] bridge,
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
