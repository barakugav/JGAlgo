package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Utils.QueueIntFixSize;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

public class MatchingGabow1976 implements Matching {

	/*
	 * Maximum matching in unweighted undirected graph in O(m n alpha(m, n)) (alpha
	 * is inverse Ackermann's function)
	 */

	public MatchingGabow1976() {
	}

	@Override
	public IntCollection calcMaxMatching(Graph g0) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		UGraph g = (UGraph) g0;
		int n = g.verticesNum();

		QueueIntFixSize queue = new QueueIntFixSize(n);
		int[] root = new int[n];
		boolean[] isEven = new boolean[n];

		final int EdgeNone = -1;
		int[] matched = new int[n];
		Arrays.fill(matched, EdgeNone);
		int[] bridge = new int[n * 2];
		int[] parent = new int[n]; // vertex -> edge

		int[] augPath = new int[n];
		boolean[] setmatch = new boolean[n];

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
				isEven[u] = true;
				queue.push(u);
			}

			bfs: while (!queue.isEmpty()) {
				final int u = queue.pop();
				int uRoot = root[u];

				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					final int e = eit.nextInt();
					final int v = eit.v();
					int vRoot = root[v];

					if (vRoot == -1) {
						// unexplored vertex, add to tree
						int matchedEdge = matched[v];
						root[v] = uRoot;
						parent[v] = e;

						int w = g.edgeEndpoint(matchedEdge, v);
						root[w] = uRoot;
						isEven[w] = true;
						queue.push(w);
						continue;
					}

					int vBase = bases[uf.find(v)];
					if (!isEven[vBase])
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
							final int brigeEdge = e, brigeVertex = p == uBase ? u : v;
							while (p != base) {
								// handle even vertex
								blossomVertices[blossomVerticesSize++] = p;

								// handle odd vertex
								p = g.edgeEndpoint(matched[p], p);
								blossomVertices[blossomVerticesSize++] = p;
								queue.push(p); // add the odd vertex that became even to the queue
								bridge[p * 2 + 0] = brigeEdge;
								bridge[p * 2 + 1] = brigeVertex;

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
				setmatch[i] = matched[u] == EdgeNone || g.edgeTarget(matched[u]) != v;
			}
			for (int i = 0; i < augPathSize; i++) {
				int e = augPath[i];
				if (setmatch[i])
					matched[g.edgeSource(e)] = matched[g.edgeTarget(e)] = e;
			}

			Arrays.fill(isEven, false);
			uf.clear();
		}

		IntList res = new IntArrayList();
		for (int u = 0; u < n; u++)
			if (matched[u] != EdgeNone && u == g.edgeSource(matched[u]))
				res.add(matched[u]);
		return res;
	}

	private static int findPath(UGraph g, int s, int t, boolean[] isEven, int[] match, int[] parent, int[] bridge,
			int[] path, int pathSize) {
		if (s == t)
			return pathSize;
		if (isEven[s]) {
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
