package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ugav.algo.Graph.Edge;

public class MatchingGabow1976 implements Matching {

	/*
	 * Maximum matching in unweighted undirected graph in O(m * n * alpha(m,n))
	 * (alpha is inverse Ackermann's function)
	 */

	private MatchingGabow1976() {
	}

	private static final MatchingGabow1976 INSTANCE = new MatchingGabow1976();

	public static MatchingGabow1976 getInstance() {
		return INSTANCE;
	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g) {
		if (g.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();

		int[] queue = new int[n];
		int[] root = new int[n];
		boolean[] isEven = new boolean[n];

		@SuppressWarnings("unchecked")
		Edge<E>[] matched = new Edge[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] bridge = new Edge[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] parent = new Edge[n]; // vertex -> edge

		@SuppressWarnings("unchecked")
		Edge<E>[] augPath = new Edge[n];
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

			int queueBegin = 0, queueEnd = 0;
			for (int u = 0; u < n; u++) {
				if (matched[u] != null)
					continue;
				root[u] = u;
				isEven[u] = true;
				queue[queueEnd++] = u;

			}
			bfs: while (queueBegin != queueEnd) {
				int u = queue[queueBegin++];
				int uRoot = root[u];

				for (Edge<E> e : Utils.iterable(g.edges(u))) {
					int v = e.v();
					int vRoot = root[v];

					if (vRoot == -1) {
						// unexplored vertex, add to tree
						Edge<E> matchedEdge = matched[v];
						root[v] = uRoot;
						parent[v] = e.twin();

						v = matchedEdge.v();
						root[v] = uRoot;
						isEven[v] = true;
						queue[queueEnd++] = v;
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
									p = parent[matched[p].v()].v(); // move 2 up
									ps[i] = bases[uf.find(p)];
								} else
									ps[i] = -1;
							}
						}

						// Find all vertices of the blossom
						int blossomVerticesSize = 0;
						for (int p : new int[] { uBase, vBase }) {
							Edge<E> brigeEdge = p == uBase ? e : e.twin();
							while (p != base) {
								// handle even vertex
								blossomVertices[blossomVerticesSize++] = p;

								// handle odd vertex
								p = matched[p].v();
								blossomVertices[blossomVerticesSize++] = p;
								queue[queueEnd++] = p; // add the odd vertex that became even to the queue
								bridge[p] = brigeEdge;

								p = bases[uf.find(parent[p].v())];
							}
						}

						// Union all UF elements in the new blossom
						for (int i = 0; i < blossomVerticesSize; i++)
							uf.union(base, blossomVertices[i]);
						bases[uf.find(base)] = base; // make sure the UF value is the base

					} else {
						// augmenting path
						augPathSize = findPath(u, uRoot, isEven, matched, parent, bridge, augPath, 0);
						augPath[augPathSize++] = e;
						augPathSize = findPath(v, vRoot, isEven, matched, parent, bridge, augPath, augPathSize);
						break bfs;
					}
				}
			}
			if (augPathSize == 0)
				break;

			for (int i = 0; i < augPathSize; i++) {
				Edge<E> e = augPath[i];
				setmatch[i] = matched[e.u()] == null || matched[e.u()].v() != e.v();
			}
			for (int i = 0; i < augPathSize; i++) {
				Edge<E> e = augPath[i];
				if (setmatch[i]) {
					matched[e.u()] = e;
					matched[e.v()] = e.twin();
				}
			}

			Arrays.fill(isEven, false);
			uf.clear();
		}

		List<Edge<E>> res = new ArrayList<>();
		for (int u = 0; u < n; u++)
			if (matched[u] != null && u < matched[u].v())
				res.add(matched[u]);
		return res;
	}

	private static <E> int findPath(int s, int t, boolean[] isEven, Edge<E>[] match, Edge<E>[] parent, Edge<E>[] bridge,
			Edge<E>[] path, int pathSize) {
		if (s == t)
			return pathSize;
		if (isEven[s]) {
			path[pathSize++] = match[s];
			path[pathSize++] = parent[match[s].v()];
			return findPath(parent[match[s].v()].v(), t, isEven, match, parent, bridge, path, pathSize);
		} else {
			Edge<E> vw = bridge[s];
			int v = vw.u(), w = vw.v();
			path[pathSize++] = match[s];
			pathSize = findPath(v, match[s].v(), isEven, match, parent, bridge, path, pathSize);
			path[pathSize++] = vw;
			return findPath(w, t, isEven, match, parent, bridge, path, pathSize);
		}
	}

}
