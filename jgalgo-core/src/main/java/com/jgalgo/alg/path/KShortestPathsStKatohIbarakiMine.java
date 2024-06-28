/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jgalgo.alg.path;

import static com.jgalgo.internal.util.Numbers.isEqual;
import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.List;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.IndexHeap;
import com.jgalgo.internal.ds.IndexHeapDouble;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

/**
 * Katoh, Ibaraki and Mine algorithm for K shortest simple paths in undirected graphs.
 *
 * <p>
 * The algorithm is similar to Yen's algorithm, but to compute the best 'replacement' path (deviation path, the next
 * shortest path that replace at least one edge from the previous k-th shortest path) a more carful subroutine is used,
 * running in time \(O(m + n \log n)\) using two shortest path trees. Yen's algorithm runs a S-T shortest path
 * computation for each edge in the last k-th shortest path, therefore each such iteration runs in time \(O(n(m + n \log
 * n))\).
 *
 * <p>
 * The total running time of this algorithm is \(O(k(m + n \log n))\). In practice Yen's algorithm may be faster for
 * wide type of instances, especially when k is small, but this algorithm should theoretically be faster for large k.
 *
 * <p>
 * Based on the paper 'An efficient algorithm for K shortest simple paths' by Katoh, Ibaraki and Mine.
 *
 * @author Barak Ugav
 */
public class KShortestPathsStKatohIbarakiMine extends KShortestPathsStBasedPathsTree {

	private int fastReplacementThreshold = 50;

	/**
	 * Create a new instance of the algorithm.
	 *
	 * <p>
	 * Please prefer using {@link KShortestPathsSt#newInstance()} to get a default implementation for the
	 * {@link KShortestPathsSt} interface.
	 */
	public KShortestPathsStKatohIbarakiMine() {}

	@Override
	protected List<IPath> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k) {
		Assertions.onlyUndirected(g);
		return super.computeKShortestPaths(g, w, source, target, k);
	}

	@Override
	protected KShortestPathsStBasedPathsTree.ShortestPathSubroutine newShortestPathSubroutine(IndexGraph g,
			IWeightFunction w, int target, Bitmap edgesMask) {
		final int n = g.vertices().size();
		double[] sDistances = new double[n];
		double[] tDistances = new double[n];
		return new ShortestPathSubroutine(g, w, target, edgesMask, sDistances, tDistances);
	}

	/**
	 * Set the threshold for the fast replacement algorithm.
	 *
	 * <p>
	 * Given a path \(P\), a replacement path \(P'\) is the shortest path with the same source and target vertices that
	 * replace at least one edge from \(P\). During the running of the algorithm, replacement paths are computed for
	 * different paths with different lengths. Yen's algorithm uses a black box S-T shortest path computation for each
	 * possible deviation edge, which is a time consuming operation. The fast replacement algorithm computes the
	 * replacement path in time \(O(m + n \log n)\) using two shortest path trees. In practice, Yen's solution might be
	 * faster, specifically when the path \(P\) is short, as the fast replacement algorithm is more complex and it's
	 * theoretical advantage is relevant when a large number of S-T computation are required in Yen's algorithm.
	 *
	 * <p>
	 * This method sets a threshold for which for any path with length greater or equal than this threshold, the fast
	 * replacement algorithm is used. The default value is 50.
	 *
	 * @param threshold if the path is greater or equal to this threshold, the fast replacement algorithm is not used
	 */
	public void setFastReplacementThreshold(int threshold) {
		fastReplacementThreshold = threshold;
	}

	private class ShortestPathSubroutine extends KShortestPathsStBasedPathsTree.ShortestPathSubroutine {

		private final IndexHeap heap;
		private final double[] sDistances;
		private final double[] tDistances;
		private final int[] sXi;
		private final int[] tXi;
		private final Bitmap visited;

		ShortestPathSubroutine(IndexGraph g, IWeightFunction w, int target, Bitmap edgesMask, double[] sDistances,
				double[] tDistances) {
			super(g, w, target, edgesMask, IndexHeapDouble.newInstance(sDistances),
					IndexHeapDouble.newInstance(tDistances));

			final int n = g.vertices().size();
			this.sDistances = sDistances;
			this.tDistances = tDistances;
			sXi = new int[n];
			tXi = new int[n];
			visited = new Bitmap(n);

			double[] heapDistances = tDistances;
			int[] heapXi = tXi;
			heap = IndexHeap.newInstance(n, (v1, v2) -> {
				int c = Double.compare(heapDistances[v1], heapDistances[v2]);
				return c != 0 ? c : Integer.compare(heapXi[v1], heapXi[v2]);
			});
		}

		private void computeShortestPathTrees(int source, IntList prevSp) {
			final int n = g.vertices().size();
			final double[] heapDistances = tDistances;
			final int[] heapXi = tXi;

			for (boolean direction : new boolean[] { true, false }) {
				int[] backtrack = direction ? sBacktrack : tBacktrack;

				Arrays.fill(heapDistances, Double.POSITIVE_INFINITY);
				Arrays.fill(heapXi, Integer.MAX_VALUE);
				Arrays.fill(backtrack, -1);

				int spVertex = direction ? source : target, spDistance = 0;
				int nextXi = 0;
				for (IntListIterator eit = prevSp.listIterator(direction ? 0 : prevSp.size());;) {
					int v = spVertex;
					heapDistances[v] = spDistance;
					assert heapXi[v] == Integer.MAX_VALUE;
					heapXi[v] = nextXi++;
					heap.insert(v);
					visited.set(v);
					if (!(direction ? eit.hasNext() : eit.hasPrevious()))
						break;
					int e = direction ? eit.nextInt() : eit.previousInt();
					spVertex = g.edgeEndpoint(e, v);
					spDistance += w.weight(e);
					backtrack[spVertex] = e;
				}

				while (heap.isNotEmpty()) {
					int u = heap.extractMin();
					visited.set(u);
					final int uXi = heapXi[u];
					final double uDistance = heapDistances[u];

					for (int e : g.outEdges(u)) {
						if (edgesMask.get(e))
							continue;
						int v = g.edgeEndpoint(e, u);
						// if (verticesMask.get(v))
						// continue;
						if (visited.get(v))
							continue;
						double ew = w.weight(e);
						double distance = uDistance + ew;

						if (!heap.isInserted(v)) {
							heapDistances[v] = distance;
							heap.insert(v);
							backtrack[v] = e;
							heapXi[v] = uXi;
						} else if (distance < heapDistances[v]
								|| (isEqual(distance, heapDistances[v], 0) && uXi < heapXi[v])) {
							heapDistances[v] = distance;
							heap.decreaseKey(v);
							backtrack[v] = e;
							heapXi[v] = uXi;
						}
					}
				}
				visited.clear();
				if (direction) {
					System.arraycopy(heapDistances, 0, sDistances, 0, n);
					System.arraycopy(heapXi, 0, sXi, 0, n);
				} else {
					// No need, heapDistances == tDistances and heapXi == tXi
					// System.arraycopy(heapDistances, 0, tDistances, 0, n);
					// System.arraycopy(heapXi, 0, tXi, 0, n);
				}
			}

			/* We computed xi of the sp tree from t in reverse order, namely tXi[target]=0 */
			/* We flip the xi to match the same values of the sp tree from s */
			final int maxXi = prevSp.size();
			for (int v : range(n))
				if (tXi[v] != Integer.MAX_VALUE)
					tXi[v] = maxXi - tXi[v];
			assert IterTools.stream(IPath.verticesIter(g, source, prevSp)).allMatch(v -> sXi[v] == tXi[v]);
		}

		/* FSP(G, s, t R) */
		@Override
		public FastReplacementAlgoResult computeBestDeviationPath(int source, IntList prevSp,
				int /* alpha */ maxDeviationPoint) {
			if (maxDeviationPoint < fastReplacementThreshold)
				return FastReplacementAlgoResult.ofFailure();

			computeShortestPathTrees(source, prevSp);

			double bestWeight = Double.POSITIVE_INFINITY;
			int bestVertex = -1, bestEdge = -1;
			for (int u : range(g.vertices().size())) {
				/* We assume property (2.4) holds */
				assert sXi[u] <= tXi[u];
				if (sXi[u] >= maxDeviationPoint)
					continue;
				if (sXi[u] < tXi[u]) {
					double d = sDistances[u] + tDistances[u];
					if (d < bestWeight) {
						bestVertex = u;
						bestEdge = -1;
						bestWeight = d;
					}

				} else { /* sXi[v] == tXi[v] */
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						if (edgesMask.get(e))
							continue;
						int v = eit.targetInt();
						// if (verticesMask.get(v))
						// continue;
						if (sXi[u] >= sXi[v])
							continue;
						if (sBacktrack[u] == e || sBacktrack[v] == e || tBacktrack[u] == e || tBacktrack[v] == e)
							continue; /* e is in S or T shortest path tree */
						double d = sDistances[u] + w.weight(e) + tDistances[v];
						if (d < bestWeight) {
							bestVertex = u;
							bestEdge = e;
							bestWeight = d;
						}
					}
				}
			}

			if (bestVertex < 0)
				return FastReplacementAlgoResult.ofSuccess(null);

			/*
			 * imagine we have the following graph: (s,t),(s,u),(u,t),(u,v), namely a triangle of s,t,u with additional
			 * vertex v connected to u. If the edge (u,v) has a weight of 0, we can choose either of u or v as the
			 * bestVertex when searching for a replacement path for (s,t). However, [s,u,v,u,t] is not a simple path.
			 * The paper avoid these kind of paths by scanning the vertices in a DFS order, and the first vertex with
			 * the best weight will always yield a simple path (the u vertex in the example graph). DFS is a bit
			 * overkill, instead we use an arbitrary order, but if the backtracking edge of the vertex in both shortest
			 * path tree is the same, we choose the other endpoint of the backtracking edge, which have a weight of
			 * zero, and it removes the two duplications of the edge from the path. We perform this operation
			 * repeatedly.
			 */
			if (bestEdge < 0) {
				while (sBacktrack[bestVertex] == tBacktrack[bestVertex]) {
					assert w.weight(sBacktrack[bestVertex]) == 0;
					bestVertex = g.edgeEndpoint(sBacktrack[bestVertex], bestVertex);
				}
			}

			IntArrayList path = new IntArrayList();

			/* Add edges from source to bestVertex */
			for (int v = bestVertex, e; (e = sBacktrack[v]) >= 0; v = g.edgeEndpoint(e, v))
				path.add(e);
			IntArrays.reverse(path.elements(), 0, path.size());

			/* Add the connecting edge that is not included in both shortest path trees */
			int v = bestVertex;
			if (bestEdge >= 0) {
				path.add(bestEdge);
				v = g.edgeEndpoint(bestEdge, v);
			}

			/* Add edges from bestVertex to target */
			for (int e; (e = tBacktrack[v]) >= 0; v = g.edgeEndpoint(e, v))
				path.add(e);

			assert Path.valueOf(g, Integer.valueOf(source), Integer.valueOf(target), path).isSimple();
			return FastReplacementAlgoResult.ofSuccess(ObjectDoublePair.of(path, bestWeight));
		}
	}
}
