package com.ugav.algo;

import java.util.Arrays;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class MatchingWeightedBipartiteHungarianMethod implements MatchingWeighted {

	/*
	 * O(m n + n^2 log n)
	 */

	private Object bipartiteVerticesWeightKey = VerticesWeights.DefaultBipartiteWeightKey;

	public MatchingWeightedBipartiteHungarianMethod() {
	}

	public void setBipartiteVerticesWeightKey(Object key) {
		bipartiteVerticesWeightKey = key;
	}

	@Override
	public IntCollection calcMaxMatching(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		UGraph g = (UGraph) g0;
		Weights.Bool partition = g.verticesWeight(bipartiteVerticesWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight " + bipartiteVerticesWeightKey);
		return new Worker(g, partition, w).calcMaxMatching(false);
	}

	@Override
	public IntCollection calcPerfectMaxMatching(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		UGraph g = (UGraph) g0;
		Weights.Bool partition = g.verticesWeight(bipartiteVerticesWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight " + bipartiteVerticesWeightKey);
		return new Worker(g, partition, w).calcMaxMatching(true);
	}

	private static class Worker {

		private final UGraph g;
		private final Weights.Bool partition;
		private final EdgeWeightFunc w;

		private final boolean[] inTree;

		private final IntComparator edgeSlackComparator;
		private final HeapDirectAccessed<Integer> nextTightEdge;
		private final HeapDirectAccessed.Handle<Integer>[] nextTightEdgePerOutV;

		private double deltaTotal;
		private final double[] dualValBase;
		private final double[] dualVal0;

		@SuppressWarnings("unchecked")
		Worker(UGraph g, Weights.Bool partition, EdgeWeightFunc w) {
			this.g = g;
			this.partition = partition;
			this.w = w;
			int n = g.vertices().size();

			inTree = new boolean[n];

			edgeSlackComparator = (e1, e2) -> Utils.compare(edgeSlack(e1), edgeSlack(e2));
			nextTightEdge = new HeapFibonacci<>(edgeSlackComparator);
			nextTightEdgePerOutV = new HeapDirectAccessed.Handle[n];

			dualValBase = new double[n];
			dualVal0 = new double[n];
		}

		IntCollection calcMaxMatching(boolean perfect) {
			final int n = g.vertices().size();
			final int EdgeNone = -1;

			int[] parent = new int[n];
			int[] matched = new int[n];
			Arrays.fill(matched, EdgeNone);

			double maxWeight = Double.MIN_VALUE;
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxWeight = Math.max(maxWeight, w.weight(e));
			}
			final double delta1Threshold = maxWeight;
			for (int u = 0; u < n; u++)
				if (partition.getBool(u))
					dualValBase[u] = delta1Threshold;

			mainLoop: for (;;) {
				Arrays.fill(parent, EdgeNone);

				// Start growing tree from all unmatched vertices in S
				for (int u = 0; u < n; u++) {
					if (!partition.getBool(u) || matched[u] != EdgeNone)
						continue;
					vertexAddedToTree(u);
					for (EdgeIter eit = g.edges(u); eit.hasNext();) {
						int e = eit.nextInt();
						nextTightEdgeAdd(u, e);
					}
				}

				currentTree: for (;;) {
					while (!nextTightEdge.isEmpty()) {
						int e = nextTightEdge.findMin().intValue();
						int u0 = g.edgeSource(e), v0 = g.edgeTarget(e);

						if (inTree[u0] && inTree[v0]) {
							// Vertex already in tree, edge is irrelevant
							nextTightEdge.extractMin();
							continue;
						}
						int v = inTree[u0] ? v0 : u0;

						// No more tight edges from the tree, go out and adjust dual values
						if (edgeSlack(e) > 0)
							break;

						// Edge is tight, add it to the tree
						nextTightEdge.extractMin();
						parent[v] = e;
						vertexAddedToTree(v);

						int matchedEdge = matched[v];
						if (matchedEdge == EdgeNone) {
							for (;;) {
								// Augmenting path
								e = parent[v];
								matched[v] = matched[v = g.edgeEndpoint(e, v)] = e;
								// TODO don't set parent[odd vertex]
								e = parent[v];
								if (e == EdgeNone)
									break currentTree;
								v = g.edgeEndpoint(e, v);
							}
						}

						// Added odd vertex, immediately add it's matched edge and even vertex
						v = g.edgeEndpoint(matchedEdge, v);
						parent[v] = matchedEdge;
						vertexAddedToTree(v);

						for (EdgeIter eit = g.edges(v); eit.hasNext();) {
							int e1 = eit.nextInt();
							nextTightEdgeAdd(v, e1);
						}
					}

					// Adjust dual values
					double delta1 = delta1Threshold - deltaTotal;
					double delta2 = nextTightEdge.isEmpty() ? -1 : edgeSlack(nextTightEdge.findMin().intValue());
					if ((!perfect && delta1 <= delta2) || delta2 == -1)
						break mainLoop;
					deltaTotal += delta2;
				}

				// Update dual values base
				for (int u = 0; u < n; u++)
					if (inTree[u])
						dualValBase[u] = dualVal(u);
				Arrays.fill(dualVal0, 0);

				// Reset tree
				Arrays.fill(inTree, false);

				// Reset heap
				nextTightEdge.clear();
				Arrays.fill(nextTightEdgePerOutV, null);
			}

			IntList res = new IntArrayList();
			for (int u = 0; u < n; u++)
				if (partition.getBool(u) && matched[u] != EdgeNone)
					res.add(matched[u]);
			return res;
		}

		private void nextTightEdgeAdd(int u, int e) {
			int v = g.edgeEndpoint(e, u);
			HeapDirectAccessed.Handle<Integer> handle = nextTightEdgePerOutV[v];
			if (handle == null)
				nextTightEdgePerOutV[v] = nextTightEdge.insert(Integer.valueOf(e));
			else if (edgeSlackComparator.compare(e, handle.get().intValue()) < 0)
				nextTightEdge.decreaseKey(handle, Integer.valueOf(e));
		}

		private double dualVal(int v) {
			return inTree[v] ? dualVal0[v] + (partition.getBool(v) ? -deltaTotal : deltaTotal) : dualValBase[v];
		}

		private double edgeSlack(int e) {
			return dualVal(g.edgeSource(e)) + dualVal(g.edgeTarget(e)) - w.weight(e);
		}

		private void vertexAddedToTree(int v) {
			dualVal0[v] = dualValBase[v] + (partition.getBool(v) ? deltaTotal : -deltaTotal);
			inTree[v] = true;
		}

	}

}
