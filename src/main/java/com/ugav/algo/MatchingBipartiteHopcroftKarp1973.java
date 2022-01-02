package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;

public class MatchingBipartiteHopcroftKarp1973 implements MatchingBipartite {

	private MatchingBipartiteHopcroftKarp1973() {
	}

	private static final MatchingBipartiteHopcroftKarp1973 INSTANCE = new MatchingBipartiteHopcroftKarp1973();

	public static MatchingBipartiteHopcroftKarp1973 getInstance() {
		return INSTANCE;
	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(GraphBipartite<E> g) {
		if (g.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();

		/* BFS */
		int[] depths = new int[n];
		int[] bfsQueue = new int[n];

		/* DFS */
		boolean[] visited = new boolean[n];
		@SuppressWarnings("unchecked")
		Iterator<Edge<E>>[] edgeItr = new Iterator[n];
		List<Edge<E>> dfsPath = new ArrayList<>();

		@SuppressWarnings("unchecked")
		Edge<E>[] matched = new Edge[n];
		List<List<Edge<E>>> augPaths = new ArrayList<>();
		Graph<E> f = new GraphArray<>(DirectedType.Undirected, n);

		while (true) {
			/* Perform BFS to build the alternating forest */
			int queueBegin = 0, queueEnd = 0;
			Arrays.fill(depths, Integer.MAX_VALUE);
			for (int u = 0; u < n; u++) {
				if (!g.isVertexInS(u) || matched[u] != null)
					continue;
				depths[u] = 0;
				visited[u] = true;
				bfsQueue[queueEnd++] = u;
			}
			int unmatchedTDepth = Integer.MAX_VALUE;
			while (queueBegin != queueEnd) {
				int u = bfsQueue[queueBegin++];
				int depth = depths[u];
				if (depth >= unmatchedTDepth)
					continue;

				for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();) {
					Edge<E> e = it.next();
					int v = e.v();
					if (depths[v] < depth)
						continue;
					f.addEdge(e);
					if (visited[v])
						continue;
					depths[v] = depth + 1;
					visited[v] = true;

					Edge<E> matchedEdge = matched[v];
					if (matchedEdge != null) {
						f.addEdge(matchedEdge);
						v = matchedEdge.v();
						depths[v] = depth + 2;
						visited[v] = true;
						bfsQueue[queueEnd++] = v;
					} else
						unmatchedTDepth = depth + 1;
				}
			}
			if (unmatchedTDepth == Integer.MAX_VALUE)
				break;
			Arrays.fill(visited, false);

			/*
			 * Run DFS to find the maximal number of paths from unmatched S vertices to
			 * unmatched T vertices
			 */
			for (int u = 0; u < n; u++) {
				if (!g.isVertexInS(u) || matched[u] != null)
					continue;

				edgeItr[0] = f.edges(u);
				visited[u] = true;

				dfs: for (int depth = 0;;) {
					Edge<E> edgeToChild = null;
					while (edgeItr[depth].hasNext()) {
						Edge<E> e = edgeItr[depth].next();
						int v = e.v();
						if (!visited[v] & depth < depths[v]) {
							edgeToChild = e;
							break;
						}
					}
					if (edgeToChild != null) {
						int v = edgeToChild.v();
						visited[v] = true;
						dfsPath.add(edgeToChild);

						Edge<E> matchedEdge = matched[v];
						if (matchedEdge == null) {
							augPaths.add(new ArrayList<>(dfsPath));
							dfsPath.clear();
							break dfs;
						}
						dfsPath.add(matchedEdge);
						v = matchedEdge.v();

						edgeItr[depth += 2] = f.edges(v);
					} else {
						if ((depth -= 2) < 0)
							break;
						dfsPath.remove(dfsPath.size() - 1);
						dfsPath.remove(dfsPath.size() - 1);
					}
				}
			}
			Arrays.fill(visited, false);
			f.edges().clear();

			for (List<Edge<E>> augPath : augPaths) {
				boolean m = true;
				for (Edge<E> e : augPath) {
					if (m) {
						matched[e.u()] = e;
						matched[e.v()] = e.twin();
					}
					m = !m;
				}
			}
			augPaths.clear();
		}

		List<Edge<E>> res = new ArrayList<>();
		for (int u = 0; u < n; u++)
			if (g.isVertexInS(u) && matched[u] != null)
				res.add(matched[u]);
		return res;
	}

}
