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
package com.jgalgo.alg;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.List;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.DoubleObjBinarySearchTree;
import com.jgalgo.internal.ds.DoubleObjReferenceableHeap;
import com.jgalgo.internal.ds.IndexHeap;
import com.jgalgo.internal.ds.IndexHeapDouble;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.BitmapSet;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

/**
 * Hershberger, Maxel and Suri algorithm for K shortest simple paths in directed graphs.
 *
 * <p>
 * The algorithm is similar to Yen's algorithm, but to compute the best 'replacement' path (deviation path, the next
 * shortest path that replace at least one edge from the previous k-th shortest path) it compute it in time \(O(m + n
 * \log n)\) using two shortest path trees. Yen's algorithm runs a S-T shortest path computation for each edge in the
 * last k-th shortest path, therefore each such iteration runs in time \(O(n(m + n \log n))\). Unlike the undirected
 * case, see {@link KShortestPathsSTKatohIbarakiMine}, in directed graphs the fast replacement algorithm may fail to
 * find a deviation path, in which case the regular Yen's algorithm is used.
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
class KShortestPathsSTHershbergerMaxelSuri implements KShortestPathsSTBase {

	private final ShortestPathST stSpAlgo = ShortestPathST.newInstance();

	@Override
	public List<IPath> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k) {
		if (!g.vertices().contains(source) || !g.vertices().contains(target))
			throw new IllegalArgumentException("source or target not in graph");
		if (k < 1)
			throw new IllegalArgumentException("k must be positive");
		w = IWeightFunction.replaceNullWeightFunc(w);
		Assertions.onlyPositiveEdgesWeights(g, w);
		Assertions.onlyDirected(g);
		if (source == target)
			return List.of(IPath.valueOf(g, source, target, Fastutil.list()));

		final int n = g.vertices().size();
		final int m = g.edges().size();
		// final Bitmap verticesMask = new Bitmap(n); /* no need, we mask the edges of masked vertices */
		final Bitmap edgesMask = new Bitmap(m);
		final ShortestPathSubroutine spSubRoutine =
				new ShortestPathSubroutine(g, w, target, /* verticesMask, */ edgesMask);

		List<IPath> result = new ObjectArrayList<>(k <= m ? k : 16);
		DoubleObjBinarySearchTree<Node> candidates = DoubleObjBinarySearchTree.newInstance();
		int candidateNum = 0;

		/* Compute the (first) shortest path and the next candidate */
		{
			ObjectDoublePair<Path<Integer, Integer>> sp1Pair =
					stSpAlgo.computeShortestPathAndWeight(g, w, Integer.valueOf(source), Integer.valueOf(target));
			if (sp1Pair != null) {
				IPath sp1 = (IPath) sp1Pair.first();
				double sp1Weight = sp1Pair.secondDouble();
				result.add(sp1);
				if (k == 1)
					return result;

				ObjectDoublePair<IntList> sp2 =
						spSubRoutine.computeNextShortestPath(source, sp1.edges(), sp1.edges().size());
				if (sp2 != null) {
					Node rootNode = new Node(null, sp1.edges(), sp1.edges(), source, sp1Weight, true);
					rootNode.bestDeviationPath = sp2.first();
					candidates.insert(sp2.secondDouble(), rootNode);
					candidateNum++;
				}
			}
		}

		int[] kthPathArr = new int[n];
		List<Node> newCandidates = new ObjectArrayList<>(3);
		while (candidateNum > 0 /* && result.size() < k (checked when adding new paths) */) {
			/* Find path with lowest weight out of all candidates */
			DoubleObjReferenceableHeap.Ref<Node> minNode = candidates.extractMin();
			Node currentNode = minNode.value();
			candidateNum--;
			IntList kthLocalPath = currentNode.bestDeviationPath;
			double kthPathWeight = minNode.key();
			Node parent = currentNode.parent;
			{ /* Build the kth path and add to result list */
				int kthPathBegin = n;
				kthLocalPath.getElements(0, kthPathArr, kthPathBegin -= kthLocalPath.size(), kthLocalPath.size());
				for (Node node = parent; node != null; node = node.parent) {
					IntList p = node.localPath;
					p.getElements(0, kthPathArr, kthPathBegin -= p.size(), p.size());
				}
				/* add the kth path to the result */
				IntList kthPathList = Fastutil.list(IntArrays.copy(kthPathArr, kthPathBegin, n - kthPathBegin));
				result.add(IPath.valueOf(g, source, target, kthPathList));
				if (result.size() == k)
					break;
			}

			int commonPrefixLength = 0;
			for (;; commonPrefixLength++)
				if (currentNode.localPath.getInt(commonPrefixLength) != kthLocalPath.getInt(commonPrefixLength))
					break;

			int splitVertex = g.edgeSource(currentNode.localPath.getInt(commonPrefixLength));

			newCandidates.clear();
			double prefixWeight = parent != null ? parent.pathWeight : 0;
			if (commonPrefixLength == 0) {
				/* branches at the source of local path */
				currentNode.sourceUsedOutEdges.add(kthLocalPath.getInt(commonPrefixLength));
				newCandidates.add(currentNode);

			} else {
				/* have some prefix in common */
				IntList prefixPath = currentNode.localPath.subList(0, commonPrefixLength);
				prefixWeight += w.weightSum(prefixPath);
				int prefixSource = currentNode.localPathSource;
				Node prefixNode = new Node(parent, currentNode.spSuffix, prefixPath, prefixSource, prefixWeight,
						currentNode.sourceUsedOutEdges);
				newCandidates.add(prefixNode);

				currentNode.parent = parent = prefixNode;
				currentNode.localPathSource = splitVertex;
				currentNode.sourceUsedOutEdges = new IntOpenHashSet(2);
				currentNode.sourceUsedOutEdges.add(currentNode.localPath.getInt(commonPrefixLength));
				currentNode.sourceUsedOutEdges.add(kthLocalPath.getInt(commonPrefixLength));

				currentNode.spSuffix = currentNode.spSuffix.subList(commonPrefixLength, currentNode.spSuffix.size());
				currentNode.localPath = currentNode.localPath.subList(commonPrefixLength, currentNode.localPath.size());
				assert !currentNode.localPath.isEmpty();
				newCandidates.add(currentNode);
			}

			/* Create a node for the suffix of the kth path, after branching from current node local path */
			if (commonPrefixLength < kthLocalPath.size() - 1) {
				IntList suffixPath = kthLocalPath.subList(commonPrefixLength, kthLocalPath.size());
				double suffixWeight = kthPathWeight - prefixWeight;
				Node suffixNode = new Node(parent, suffixPath, suffixPath, splitVertex, suffixWeight, false);
				newCandidates.add(suffixNode);
			}

			/* Compute for each modified or new node a candidate deviation path */
			currentNode.clearBestDeviation();
			for (Node node : newCandidates) {
				final boolean allowDeviateFromSource = node.sourceUsedOutEdges != null;
				if (!allowDeviateFromSource && node.localPath.size() == 1)
					continue;
				final int localSource = node.localPathSource;
				// verticesMask.clear();
				edgesMask.clear();

				/* mask the path up to the current node */
				for (Node p = node.parent; p != null; p = p.parent) {
					for (int e : p.localPath) {
						int v = g.edgeSource(e);
						// verticesMask.set(v);
						for (int vEdge : g.outEdges(v))
							edgesMask.set(vEdge);
						for (int vEdge : g.inEdges(v))
							edgesMask.set(vEdge);
					}
				}

				/* mask source edges already used */
				if (allowDeviateFromSource) {
					for (int e : node.sourceUsedOutEdges)
						edgesMask.set(e);
				} else {
					int requiredFirstEdge = node.localPath.getInt(0);
					for (int e : g.outEdges(localSource))
						if (e != requiredFirstEdge)
							edgesMask.set(e);
				}

				ObjectDoublePair<IntList> bestDeviation =
						spSubRoutine.computeNextShortestPath(localSource, node.spSuffix, node.localPath.size());
				if (bestDeviation != null) {
					node.bestDeviationPath = bestDeviation.first();
					double deviationWeight =
							(node.parent != null ? node.parent.pathWeight : 0) + bestDeviation.secondDouble();
					candidates.insert(deviationWeight, node);
					candidateNum++;
				}
			}

			while (candidateNum > k - result.size()) {
				candidates.extractMax();
				candidateNum--;
			}
		}

		return result;
	}

	/**
	 * A node is a sub path in the tree of all the k-th shortest paths.
	 *
	 * <p>
	 * Initially there is a single node for the first shortest path. Each time a k-th shortest path is computed, the
	 * longest prefix of the k-th path that is common to the previous k-th path is found, and the tree is branched for
	 * the suffix only. Each node contains a local path, a full path can be reconstructed by following the parent
	 * pointers.
	 *
	 * @author Barak Ugav
	 */
	private static class Node {

		/* The local path of the node, the full path can be reconstructed by following the parent pointers */
		IntList localPath;
		/* The first vertex visited by the local path. */
		int localPathSource;
		/* The parent node, null for the root node */
		Node parent;
		/*
		 * The first shortest path computed from the local path source to the global target. Used to compute xi in
		 * shortest path subroutine
		 */
		IntList spSuffix;
		/* The weight from the global source along the whole ancestors paths until this node local targets */
		double pathWeight;
		/*
		 * The set of edges for which there is already a child for the parent->target. For each parent node, there is a
		 * single child which is responsible for computing the shortest paths deviating for the parent target vertex,
		 * which is the source vertex of all the children nodes. Only for that special child, the set is not null.
		 */
		IntSet sourceUsedOutEdges;
		/* The best deviation path for this node, null if there is no such one */
		IntList bestDeviationPath;

		Node(Node parent, IntList spSuffix, IntList localPath, int localPathSource, double pathWeight,
				IntSet sourceUsedOutEdges) {
			assert !localPath.isEmpty();
			this.parent = parent;
			this.spSuffix = spSuffix;
			this.localPath = localPath;
			this.localPathSource = localPathSource;
			this.pathWeight = pathWeight;
			this.sourceUsedOutEdges = sourceUsedOutEdges;
		}

		Node(Node parent, IntList spSuffix, IntList localPath, int localPathSource, double pathWeight,
				boolean allowDeviateFromSource) {
			this(parent, spSuffix, localPath, localPathSource, pathWeight,
					allowDeviateFromSource ? new IntOpenHashSet() : null);
		}

		void clearBestDeviation() {
			bestDeviationPath = null;
		}
	}

	private static class ShortestPathSubroutine {

		private final IndexGraph g;
		private final IWeightFunction w;
		private final int target;

		// private final Bitmap verticesMask;
		private final Bitmap edgesMask;

		/* Shared data structures (both the fast replacement and Yen's algorithms) */
		private final int[] sBacktrack;
		private final int[] tBacktrack;

		/* Fast replacement algorithm data structures */
		private final IndexHeap heap;
		private final double[] sDistances;
		private final double[] tDistances;
		private final int[] sXi;
		private final int[] tXi;
		private final Bitmap visited;

		/* Yen's algorithm data structures */
		private final IndexHeapDouble heapS;
		private final IndexHeapDouble heapT;
		private final BitmapSet visitedS;
		private final BitmapSet visitedT;

		ShortestPathSubroutine(IndexGraph g, IWeightFunction w, int target,
				/* Bitmap verticesMask, */ Bitmap edgesMask) {
			this.g = g;
			this.w = w;
			this.target = target;

			// this.verticesMask = verticesMask;
			this.edgesMask = edgesMask;

			final int n = g.vertices().size();
			sDistances = new double[n];
			tDistances = new double[n];
			sBacktrack = new int[n];
			tBacktrack = new int[n];
			sXi = new int[n];
			tXi = new int[n];
			visited = new Bitmap(n);

			double[] heapDistances = tDistances;
			int[] heapXi = tXi;
			heap = IndexHeap.newInstance(n, (v1, v2) -> {
				int c = Double.compare(heapDistances[v1], heapDistances[v2]);
				return c != 0 ? c : Integer.compare(heapXi[v1], heapXi[v2]);
			});

			heapS = IndexHeapDouble.newInstance(n);
			heapT = IndexHeapDouble.newInstance(n);
			visitedS = new BitmapSet(n);
			visitedT = new BitmapSet(n);
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

		private FastReplacementAlgoResult fastReplacementAlgo(int source, IntList prevSp,
				int /* alpha */ maxDeviationPoint) {

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

		private static class FastReplacementAlgoResult {
			final boolean success;
			final ObjectDoublePair<IntList> value;

			private FastReplacementAlgoResult(boolean success, ObjectDoublePair<IntList> value) {
				this.success = success;
				this.value = value;
			}

			static FastReplacementAlgoResult ofSuccess(ObjectDoublePair<IntList> value) {
				return new FastReplacementAlgoResult(true, value);
			}

			static FastReplacementAlgoResult ofFailure() {
				return new FastReplacementAlgoResult(false, null);
			}
		}

		ObjectDoublePair<IntList> computeShortestPathYen(int source) {
			final ObjectDoublePair<IntList> res = computeShortestPathYen0(source);
			heapS.clear();
			heapT.clear();
			visitedS.clear();
			visitedT.clear();
			return res;
		}

		private ObjectDoublePair<IntList> computeShortestPathYen0(int source) {
			assert source != target;
			// assert !verticesMask.get(source);
			// assert !verticesMask.get(target);

			assert visitedS.isEmpty();
			assert visitedT.isEmpty();
			assert heapS.isEmpty();
			assert heapT.isEmpty();

			int middle = -1;
			double mu = Double.POSITIVE_INFINITY;
			heapS.insert(source, 0);
			heapT.insert(target, 0);
			while (heapS.isNotEmpty() && heapT.isNotEmpty()) {

				int uS = heapS.extractMin();
				double uDistanceS = heapS.key(uS);
				visitedS.set(uS);

				int uT = heapT.extractMin();
				double uDistanceT = heapT.key(uT);
				visitedT.set(uT);

				for (IEdgeIter eit = g.outEdges(uS).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (edgesMask.get(e))
						continue;
					int v = eit.targetInt();
					// if (verticesMask.get(v))
					// continue;
					if (visitedS.get(v))
						continue;
					double ew = w.weight(e);
					double vDistance = uDistanceS + ew;
					if (visitedT.get(v)) {
						if (mu > vDistance + heapT.key(v)) {
							mu = vDistance + heapT.key(v);
							middle = v;
						}
					}

					if (!heapS.isInserted(v)) {
						heapS.insert(v, vDistance);
						sBacktrack[v] = e;
					} else if (vDistance < heapS.key(v)) {
						heapS.decreaseKey(v, vDistance);
						sBacktrack[v] = e;
					}
				}

				for (IEdgeIter eit = g.inEdges(uT).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (edgesMask.get(e))
						continue;
					int v = eit.sourceInt();
					// if (verticesMask.get(v))
					// continue;
					if (visitedT.get(v))
						continue;
					double ew = w.weight(e);
					double vDistance = uDistanceT + ew;
					if (visitedS.get(v)) {
						if (mu > vDistance + heapS.key(v)) {
							mu = vDistance + heapS.key(v);
							middle = v;
						}
					}

					if (!heapT.isInserted(v)) {
						heapT.insert(v, vDistance);
						tBacktrack[v] = e;
					} else if (vDistance < heapT.key(v)) {
						heapT.decreaseKey(v, vDistance);
						tBacktrack[v] = e;
					}
				}

				if (uDistanceS + uDistanceT >= mu)
					break;
			}
			if (middle < 0)
				return null;

			IntArrayList path = new IntArrayList();

			/* add edges from source to middle */
			for (int u = middle, e; u != source; u = g.edgeSource(e))
				path.add(e = sBacktrack[u]);
			IntArrays.reverse(path.elements(), 0, path.size());

			/* add edges from middle to target */
			for (int u = middle, e; u != target; u = g.edgeTarget(e))
				path.add(e = tBacktrack[u]);
			return ObjectDoublePair.of(path, mu);
		}

		ObjectDoublePair<IntList> computeNextShortestPath(int source, IntList prevSp,
				int /* alpha */ maxDeviationPoint) {
			FastReplacementAlgoResult result = fastReplacementAlgo(source, prevSp, maxDeviationPoint);
			if (result.success)
				return result.value;

			/* The fast algorithm failed, we must use Yen's regular algorithm */
			ObjectDoublePair<IntList> bestSp = null;
			for (int maskedEdge : prevSp.subList(0, maxDeviationPoint)) {
				edgesMask.set(maskedEdge);
				ObjectDoublePair<IntList> sp = computeShortestPathYen(source);
				if (sp != null && (bestSp == null || sp.secondDouble() < bestSp.secondDouble()))
					bestSp = sp;
				edgesMask.clear(maskedEdge);
			}
			return bestSp;
		}
	}

}
