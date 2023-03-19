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
	public IntCollection calcMaxMatching(Graph<?> g0) {
		if (!(g0 instanceof Graph.Undirected<?>))
			throw new IllegalArgumentException("only undirected graphs are supported");
		Graph.Undirected<?> g = (Graph.Undirected<?>) g0;
		int n = g.vertices();

		QueueIntFixSize queue = new QueueIntFixSize(n);
		int[] root = new int[n];
		boolean[] isEven = new boolean[n];

		final int EdgeNone = -1;
		int[] matched = new int[n];
		Arrays.fill(matched, EdgeNone);
		int[] bridgeE = new int[n]; // TODO use a single array twice the size
		int[] bridgeU = new int[n];
		int[] parent = new int[n]; // vertex -> edge

		int[] augPath = new int[n];
		boolean[] setmatch = new boolean[n];

		int[] blossomBaseSearchNotes = new int[n];
		int blossomBaseSearchNotesIndex = 0;
		int[] blossomVertices = new int[n];

		UnionFind uf = new UnionFindArray();
		int[] bases = new int[n];

		while (true) {
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

				for (EdgeIter<?> eit = g.edges(u); eit.hasNext();) {
					final int e = eit.nextInt();
					int v = eit.v();
					int vRoot = root[v];

					if (vRoot == -1) {
						// unexplored vertex, add to tree
						int matchedEdge = matched[v];
						root[v] = uRoot;
						parent[v] = e;

						v = g.getEdgeEndpoint(matchedEdge, v);
						root[v] = uRoot;
						isEven[v] = true;
						queue.push(v);
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
									p = g.getEdgeEndpoint(matched[p], p);
									p = g.getEdgeEndpoint(parent[p], p); // move 2 up
									ps[i] = bases[uf.find(p)];
								} else
									ps[i] = -1;
							}
						}

						// Find all vertices of the blossom
						int blossomVerticesSize = 0;
						for (int p : new int[] { uBase, vBase }) {
							final int brigeEdge = e;
							while (p != base) {
								// handle even vertex
								blossomVertices[blossomVerticesSize++] = p;

								// handle odd vertex
								p = g.getEdgeEndpoint(matched[p], p);
								blossomVertices[blossomVerticesSize++] = p;
								queue.push(p); // add the odd vertex that became even to the queue
								bridgeE[p] = brigeEdge;
								bridgeU[p] = u;

								p = bases[uf.find(g.getEdgeEndpoint(parent[p], p))];
							}
						}

						// Union all UF elements in the new blossom
						for (int i = 0; i < blossomVerticesSize; i++)
							uf.union(base, blossomVertices[i]);
						bases[uf.find(base)] = base; // make sure the UF value is the base

					} else {
						// augmenting path
						augPathSize = findPath(g, u, uRoot, isEven, matched, parent, bridgeE, bridgeU, augPath, 0);
						augPath[augPathSize++] = e;
						augPathSize = findPath(g, v, vRoot, isEven, matched, parent, bridgeE, bridgeU, augPath,
								augPathSize);
						break bfs;
					}
				}
			}
			if (augPathSize == 0)
				break;

			for (int i = 0; i < augPathSize; i++) {
				int e = augPath[i];
				int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
				setmatch[i] = matched[u] == EdgeNone || g.getEdgeTarget(matched[u]) != v;
			}
			for (int i = 0; i < augPathSize; i++) {
				int e = augPath[i];
				if (setmatch[i])
					matched[g.getEdgeSource(e)] = matched[g.getEdgeTarget(e)] = e;
			}

			Arrays.fill(isEven, false);
			uf.clear();
		}

		IntList res = new IntArrayList();
		for (int u = 0; u < n; u++)
			if (matched[u] != EdgeNone && u == g.getEdgeSource(matched[u]))
				res.add(matched[u]);
		return res;
	}

	private static int findPath(Graph.Undirected<?> g, int s, int t, boolean[] isEven, int[] match, int[] parent,
			int[] bridgeE, int[] bridgeU, int[] path, int pathSize) {
		if (s == t)
			return pathSize;
		if (isEven[s]) {
			int v = g.getEdgeEndpoint(match[s], s);
			path[pathSize++] = match[s];
			path[pathSize++] = parent[v];
			return findPath(g, g.getEdgeEndpoint(parent[v], v), t, isEven, match, parent, bridgeE, bridgeU, path,
					pathSize);
		} else {
			int vw = bridgeE[s];
			int v = bridgeU[s], w = g.getEdgeEndpoint(vw, v);
			path[pathSize++] = match[s];
			pathSize = findPath(g, v, g.getEdgeEndpoint(match[s], s), isEven, match, parent, bridgeE, bridgeU, path,
					pathSize);
			path[pathSize++] = vw;
			return findPath(g, w, t, isEven, match, parent, bridgeE, bridgeU, path, pathSize);
		}
	}

}
