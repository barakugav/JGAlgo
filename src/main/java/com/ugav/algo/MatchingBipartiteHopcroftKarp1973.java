package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;

public class MatchingBipartiteHopcroftKarp1973 implements MatchingBipartite {

	/*
	 * Maximum matching in unweighted undirected bipartite graph in O(m * n^0.5)
	 */

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
		@SuppressWarnings("unchecked")
		Edge<E>[] dfsPath = new Edge[n];

		@SuppressWarnings("unchecked")
		Edge<E>[] matched = new Edge[n];
		List<Edge<E>[]> augPaths = new ArrayList<>();
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
						dfsPath[depth++] = edgeToChild;

						Edge<E> matchedEdge = matched[v];
						if (matchedEdge == null) {
							@SuppressWarnings("unchecked")
							Edge<E>[] augPath = new Edge[depth];
							System.arraycopy(dfsPath, 0, augPath, 0, depth);
							augPaths.add(augPath);
							break dfs;
						}
						dfsPath[depth] = matchedEdge;
						v = matchedEdge.v();

						edgeItr[++depth] = f.edges(v);
					} else if ((depth -= 2) < 0)
						break;
				}
			}
			Arrays.fill(visited, false);
			f.edges().clear();

			for (Edge<E>[] augPath : augPaths) {
				for (int i = 0; i < augPath.length; i += 2) {
					Edge<E> e = augPath[i];
					matched[e.u()] = e;
					matched[e.v()] = e.twin();
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
