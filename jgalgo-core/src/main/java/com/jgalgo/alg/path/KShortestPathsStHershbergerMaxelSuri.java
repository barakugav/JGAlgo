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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.List;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.IndexHeap;
import com.jgalgo.internal.ds.IndexHeapDouble;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

/**
 * Hershberger, Maxel and Suri algorithm for K shortest simple paths in directed graphs.
 *
 * <p>
 * The algorithm is similar to Yen's algorithm, but to compute the best 'replacement' path (deviation path, the next
 * shortest path that replace at least one edge from the previous k-th shortest path) a more carful subroutine is used,
 * running in time \(O(m + n \log n)\) using two shortest path trees. Yen's algorithm runs a S-T shortest path
 * computation for each edge in the last k-th shortest path, therefore each such iteration runs in time \(O(n(m + n \log
 * n))\). Unlike the undirected case, see {@link KShortestPathsStKatohIbarakiMine}, in directed graphs the fast
 * replacement algorithm may fail to find a deviation path, in which case the regular Yen's algorithm is used.
 *
 * <p>
 * The total running time of this algorithm is \(O(nk(m + n \log n))\) in the worst case, but in practice it usually
 * runs in time \(O(k(m + n \log n))\).
 *
 * <p>
 * Based on the paper 'Finding the k Shortest Simple Paths: A New Algorithm and its Implementation' by John Hershberger,
 * Matthew Maxel and Subhash Suri.
 *
 * @author Barak Ugav
 */
class KShortestPathsStHershbergerMaxelSuri extends KShortestPathsStBasedPathsTree {

	private int fastReplacementThreshold = 50;

	/**
	 * Create a new instance of the algorithm.
	 *
	 * <p>
	 * Please prefer using {@link KShortestPathsSt#newInstance()} to get a default implementation for the
	 * {@link KShortestPathsSt} interface.
	 */
	public KShortestPathsStHershbergerMaxelSuri() {}

	@Override
	protected List<IPath> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k) {
		Assertions.onlyDirected(g);
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
	 * If the path, for each a replacement path is computed, is shorter than the threshold, the algorithm falls back to
	 * Yen's algorithm, as the fast replacement algorithm is more complex and it's theoretical advantage is relevant
	 * when a large number of S-Y computation are required in Yen's algorithm.
	 *
	 * @param threshold if the path is shorter than this threshold, the fast replacement algorithm is not used
	 */
	void setFastReplacementThreshold(int threshold) {
		fastReplacementThreshold = threshold;
	}

	private class ShortestPathSubroutine extends KShortestPathsStBasedPathsTree.ShortestPathSubroutine {

		/* Fast replacement algorithm data structures */
		private final IndexHeap heap;
		private final double[] sDistances;
		private final double[] tDistances;
		private final int[] sXi;
		private final int[] tXi;
		private final Bitmap visited;

		ShortestPathSubroutine(IndexGraph g, IWeightFunction w, int target, /* Bitmap verticesMask, */ Bitmap edgesMask,
				double[] sDistances, double[] tDistances) {
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
					spVertex = direction ? g.edgeTarget(e) : g.edgeSource(e);
					spDistance += w.weight(e);
					backtrack[spVertex] = e;
				}

				while (heap.isNotEmpty()) {
					int u = heap.extractMin();
					visited.set(u);
					final int uXi = heapXi[u];
					final double uDistance = heapDistances[u];

					for (int e : direction ? g.outEdges(u) : g.inEdges(u)) {
						if (edgesMask.get(e))
							continue;
						int v = direction ? g.edgeTarget(e) : g.edgeSource(e);
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
						} else if (distance < heapDistances[v] || (distance == heapDistances[v] && uXi < heapXi[v])) {
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

		@Override
		public FastReplacementAlgoResult computeBestDeviationPath(int source, IntList prevSp, int maxDeviationPoint) {
			if (maxDeviationPoint < fastReplacementThreshold)
				return FastReplacementAlgoResult.ofFailure();

			computeShortestPathTrees(source, prevSp);

			double bestWeight = Double.POSITIVE_INFINITY;
			int bestEdge = -1;
			/* mask the edges on the previous SP so we wont consider them as replacement */
			IntIterator prevSpIt = prevSp.subList(0, maxDeviationPoint).iterator();
			boolean maskedFirstEdge = prevSpIt.hasNext() && edgesMask.set(prevSpIt.nextInt());
			prevSpIt.forEachRemaining(edgesMask::set);
			for (int e : range(g.edges().size())) {
				if (edgesMask.get(e))
					continue;
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				// if (verticesMask.get(u) || verticesMask.get(v))
				// continue;
				if (sXi[u] >= maxDeviationPoint)
					continue;
				if (sXi[u] >= sXi[v])
					continue;

				double d = sDistances[u] + w.weight(e) + tDistances[v];
				if (d < bestWeight) {
					bestEdge = e;
					bestWeight = d;
				}
			}
			/* unmask the edges of the previous SP */
			prevSpIt = prevSp.subList(0, maxDeviationPoint).iterator();
			if (maskedFirstEdge)
				edgesMask.clear(prevSpIt.nextInt());
			prevSpIt.forEachRemaining(edgesMask::clear);

			if (bestEdge < 0)
				return FastReplacementAlgoResult.ofSuccess(null);

			if (sXi[g.edgeSource(bestEdge)] >= tXi[g.edgeTarget(bestEdge)])
				return FastReplacementAlgoResult.ofFailure();

			// TODO is this code needed?
			// if (bestEdge < 0) {
			// while (sBacktrack[bestVertex] == tBacktrack[bestVertex]) {
			// assert w.weight(sBacktrack[bestVertex]) == 0;
			// bestVertex = g.edgeEndpoint(sBacktrack[bestVertex], bestVertex);
			// }
			// }

			IntArrayList path = new IntArrayList();

			/* Add edges from source to bestVertex */
			for (int v = g.edgeSource(bestEdge), e; (e = sBacktrack[v]) >= 0; v = g.edgeSource(e))
				path.add(e);
			IntArrays.reverse(path.elements(), 0, path.size());

			/* Add the connecting edge that is not included in both shortest path trees */
			path.add(bestEdge);

			/* Add edges from bestVertex to target */
			for (int v = g.edgeTarget(bestEdge), e; (e = tBacktrack[v]) >= 0; v = g.edgeTarget(e))
				path.add(e);

			assert Path.valueOf(g, Integer.valueOf(source), Integer.valueOf(target), path).isSimple();
			return FastReplacementAlgoResult.ofSuccess(ObjectDoublePair.of(path, bestWeight));
		}
	}

}
