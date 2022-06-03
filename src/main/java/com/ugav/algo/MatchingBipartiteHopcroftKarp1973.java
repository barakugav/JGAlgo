package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Utils.QueueIntFixSize;

public class MatchingBipartiteHopcroftKarp1973 implements Matching {

	/*
	 * Maximum matching in unweighted undirected bipartite graph in O(m * n^0.5)
	 */

	public MatchingBipartiteHopcroftKarp1973() {
	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g0) {
		if (!(g0 instanceof GraphBipartite))
			throw new IllegalArgumentException("only bipartite graphs are supported");
		GraphBipartite<E> g = (GraphBipartite<E>) g0;
		if (g.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();

		/* BFS */
		int[] depths = new int[n];
		QueueIntFixSize bfsQueue = new QueueIntFixSize(n);

		/* DFS */
		boolean[] visited = new boolean[n];
		@SuppressWarnings("unchecked")
		Iterator<Edge<E>>[] edges = new Iterator[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] dfsPath = new Edge[n];

		@SuppressWarnings("unchecked")
		Edge<E>[] matched = new Edge[n];
		Graph<E> f = new GraphArray<>(DirectedType.Undirected, n);

		while (true) {
			/* Perform BFS to build the alternating forest */
			bfsQueue.clear();
			Arrays.fill(depths, Integer.MAX_VALUE);
			for (int u = 0; u < n; u++) {
				if (!g.isVertexInS(u) || matched[u] != null)
					continue;
				depths[u] = 0;
				bfsQueue.push(u);
			}
			int unmatchedTDepth = Integer.MAX_VALUE;
			while (!bfsQueue.isEmpty()) {
				int u = bfsQueue.pop();
				int depth = depths[u];
				if (depth >= unmatchedTDepth)
					continue;

				for (Edge<E> e : Utils.iterable(g.edges(u))) {
					int v = e.v();
					if (depths[v] < depth)
						continue;
					f.addEdge(e);
					if (depths[v] != Integer.MAX_VALUE)
						continue;
					depths[v] = depth + 1;

					Edge<E> matchedEdge = matched[v];
					if (matchedEdge != null) {
						f.addEdge(matchedEdge);
						v = matchedEdge.v();
						depths[v] = depth + 2;
						bfsQueue.push(v);
					} else
						unmatchedTDepth = depth + 1;
				}
			}
			if (unmatchedTDepth == Integer.MAX_VALUE)
				break;

			/*
			 * Run DFS to find the maximal number of paths from unmatched S vertices to
			 * unmatched T vertices
			 */
			for (int u = 0; u < n; u++) {
				if (!g.isVertexInS(u) || matched[u] != null)
					continue;

				edges[0] = f.edges(u);
				visited[u] = true;

				for (int depth = 0;;) {
					if (edges[depth].hasNext()) {
						Edge<E> e = edges[depth].next();
						int v = e.v();
						if (visited[v] || depth >= depths[v])
							continue;
						visited[v] = true;
						dfsPath[depth++] = e;

						Edge<E> matchedEdge = matched[v];
						if (matchedEdge == null) {
							// Augmented path found
							for (int i = 0; i < depth; i += 2) {
								Edge<E> e1 = dfsPath[i];
								matched[e1.u()] = e1;
								matched[e1.v()] = e1.twin();
							}
							break;
						}
						dfsPath[depth] = matchedEdge;
						v = matchedEdge.v();

						edges[++depth] = g.edges(v);
					} else if ((depth -= 2) < 0)
						break;
				}
			}
			Arrays.fill(visited, false);
			f.edges().clear();
		}
		Arrays.fill(edges, null);
		Arrays.fill(dfsPath, null);

		List<Edge<E>> res = new ArrayList<>();
		for (int u = 0; u < n; u++)
			if (g.isVertexInS(u) && matched[u] != null)
				res.add(matched[u]);
		return res;
	}

}
