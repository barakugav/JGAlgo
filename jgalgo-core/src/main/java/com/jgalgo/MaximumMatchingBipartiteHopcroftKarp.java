package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Hopcroft–Karp maximum unweighted matching algorithm for undirected bipartite
 * graphs.
 * <p>
 * The algorithm runs in {@code O(m} <span>&#8730;</span> {@code n)} and it uses
 * linear space.
 * <p>
 * Based on "A n^5/2 Algorithm for Maximum Matchings in Bipartite Graphs" by J.
 * Hopcroft and R. Karp (1973).
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class MaximumMatchingBipartiteHopcroftKarp implements MaximumMatching {

	private Object bipartiteVerticesWeightKey = Weights.DefaultBipartiteWeightKey;
	private static final Object EdgeRefWeightKey = new Object();

	/**
	 * Create a new maximum matching object.
	 */
	public MaximumMatchingBipartiteHopcroftKarp() {
	}

	/**
	 * Set the key used to get the bipartiteness property of vertices.
	 * <p>
	 * The algorithm run on bipartite graphs and expect the user to provide the
	 * vertices partition by a boolean vertices weights using
	 * {@link Graph#verticesWeight(Object)}. By default, the weights are searched
	 * using the key {@link Weights#DefaultBipartiteWeightKey}. To override this
	 * default behavior, use this function to choose a different key.
	 *
	 * @param key an object key that will be used to get the bipartite vertices
	 *            partition by {@code g.verticesWeight(key)}.
	 */
	public void setBipartiteVerticesWeightKey(Object key) {
		bipartiteVerticesWeightKey = key;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException     if the bipartiteness vertices weights is not
	 *                                  found. See
	 *                                  {@link #setBipartiteVerticesWeightKey(Object)}.
	 * @throws IllegalArgumentException if the graph is no bipartite with respect to
	 *                                  the provided partition
	 */
	@Override
	public IntCollection computeMaximumMatching(UGraph g) {
		int n = g.vertices().size();

		Weights.Bool partition = g.verticesWeight(bipartiteVerticesWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight " + bipartiteVerticesWeightKey);
		if (Bipartite.isValidBipartitePartition(g, partition))
			throw new IllegalArgumentException("the graph is not bipartite");

		/* BFS */
		int[] depths = new int[n];
		IntPriorityQueue bfsQueue = new IntArrayFIFOQueue();

		/* DFS */
		BitSet visited = new BitSet(n);
		EdgeIter[] edges = new EdgeIter[n];
		int[] dfsPath = new int[n];

		int[] matched = new int[n];
		final int MatchedNone = -1;
		Arrays.fill(matched, MatchedNone);
		UGraph f = new GraphArrayUndirected(n);
		Weights.Int edgeRef = f.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));

		for (;;) {
			/* Perform BFS to build the alternating forest */
			bfsQueue.clear();
			Arrays.fill(depths, Integer.MAX_VALUE);
			for (int u = 0; u < n; u++) {
				if (!partition.getBool(u) || matched[u] != MatchedNone)
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

				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
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
						int w = g.edgeEndpoint(matchedEdge, v);
						edgeRef.set(f.addEdge(v, w), matchedEdge);
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
			 * Run DFS to find the maximal number of paths from unmatched S vertices to
			 * unmatched T vertices
			 */
			for (int u = 0; u < n; u++) {
				if (!partition.getBool(u) || matched[u] != MatchedNone)
					continue;

				edges[0] = f.edgesOut(u);
				visited.set(u);

				for (int depth = 0; depth >= 0;) {
					EdgeIter eit = edges[depth];
					if (eit.hasNext()) {
						int e = eit.nextInt();
						int v = eit.v();
						if (visited.get(v) || depth >= depths[v])
							continue;
						visited.set(v);
						dfsPath[depth++] = edgeRef.getInt(e);

						int matchedEdge = matched[v];
						if (matchedEdge == MatchedNone) {
							// Augmenting path found
							for (int i = 0; i < depth; i += 2) {
								int e1 = dfsPath[i];
								matched[g.edgeSource(e1)] = matched[g.edgeTarget(e1)] = e1;
							}
							break;
						}
						dfsPath[depth] = matchedEdge;
						v = g.edgeEndpoint(matchedEdge, v);

						edges[++depth] = f.edgesOut(v);
					} else {
						/*
						 * Pop two edges (one from the matching and the other not in the matching) from
						 * the DFS path
						 */
						depth -= 2;
					}
				}
			}
			visited.clear();
			f.clearEdges();
		}
		Arrays.fill(edges, null);

		IntList res = new IntArrayList();
		for (int u = 0; u < n; u++)
			if (partition.getBool(u) && matched[u] != MatchedNone)
				res.add(matched[u]);
		return res;
	}

}
