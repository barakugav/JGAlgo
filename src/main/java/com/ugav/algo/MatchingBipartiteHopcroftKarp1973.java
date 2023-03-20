package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Utils.QueueIntFixSize;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

public class MatchingBipartiteHopcroftKarp1973 implements Matching {

	/*
	 * Maximum matching in unweighted undirected bipartite graph in O(m n^0.5)
	 */

	public MatchingBipartiteHopcroftKarp1973() {
	}

	@Override
	public IntCollection calcMaxMatching(Graph g0) {
		if (!(g0 instanceof GraphBipartite.Undirected))
			throw new IllegalArgumentException("only undirected bipartite graphs are supported");
		GraphBipartite.Undirected g = (GraphBipartite.Undirected) g0;
		int n = g.vertices();

		/* BFS */
		int[] depths = new int[n];
		QueueIntFixSize bfsQueue = new QueueIntFixSize(n);

		/* DFS */
		boolean[] visited = new boolean[n];
		EdgeIter[] edges = new EdgeIter[n];
		int[] dfsPath = new int[n];

		int[] matched = new int[n];
		final int MatchedNone = -1;
		Arrays.fill(matched, MatchedNone);
		Graph.Undirected f = new GraphArrayUndirected(n);
		EdgeData.Int edgeRef = f.newEdgeDataInt("edgeRef");

		while (true) {
			/* Perform BFS to build the alternating forest */
			bfsQueue.clear();
			Arrays.fill(depths, Integer.MAX_VALUE);
			for (int u = 0; u < n; u++) {
				if (!g.isVertexInS(u) || matched[u] != MatchedNone)
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

				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					if (depths[v] < depth)
						continue;
					edgeRef.set(f.addEdge(u, v), e);
					if (depths[v] != Integer.MAX_VALUE)
						continue;
					depths[v] = depth + 1;

					int matchedEdge = matched[v];
					if (matchedEdge != MatchedNone) {
						int w = g.getEdgeEndpoint(matchedEdge, v);
						edgeRef.set(f.addEdge(v, w), matchedEdge);
						v = w;
						depths[v] = depth + 2;
						bfsQueue.push(v);
					} else {
						unmatchedTDepth = depth + 1;
					}
				}
			}
			if (unmatchedTDepth == Integer.MAX_VALUE)
				break;

			/*
			 * Run DFS to find the maximal number of paths from unmatched S vertices to
			 * unmatched T vertices
			 */
			for (int u = 0; u < n; u++) {
				if (!g.isVertexInS(u) || matched[u] != MatchedNone)
					continue;

				edges[0] = f.edges(u);
				visited[u] = true;

				for (int depth = 0; depth >= 0;) {
					EdgeIter eit = edges[depth];
					if (eit.hasNext()) {
						int e = eit.nextInt();
						int v = eit.v();
						if (visited[v] || depth >= depths[v])
							continue;
						visited[v] = true;
						dfsPath[depth++] = edgeRef.getInt(e);

						int matchedEdge = matched[v];
						if (matchedEdge == MatchedNone) {
							// Augmenting path found
							for (int i = 0; i < depth; i += 2) {
								int e1 = dfsPath[i];
								matched[g.getEdgeSource(e1)] = matched[g.getEdgeTarget(e1)] = e1;
							}
							break;
						}
						dfsPath[depth] = matchedEdge;
						v = g.getEdgeEndpoint(matchedEdge, v);

						edges[++depth] = f.edges(v);
					} else {
						/*
						 * Pop two edges (one from the matching and the other not in the matching) from
						 * the DFS path
						 */
						depth -= 2;
					}
				}
			}
			Arrays.fill(visited, false);
			f.clearEdges();
		}
		Arrays.fill(edges, null);

		IntList res = new IntArrayList();
		for (int u = 0; u < n; u++)
			if (g.isVertexInS(u) && matched[u] != MatchedNone)
				res.add(matched[u]);
		return res;
	}

}
