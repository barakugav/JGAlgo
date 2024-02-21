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

import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.DoubleObjBinarySearchTree;
import com.jgalgo.internal.ds.DoubleObjReferenceableHeap;
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
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

/**
 * Yen's algorithm for computing the K shortest paths between two vertices in a graph.
 *
 * <p>
 * This implementation looks very different from the original algorithm, but it is indeed the same algorithm. A set of
 * paths is represented by a tree, in which each node represent some sub path, which is the longest suffix shared among
 * all paths that have the same prefix up to that node. Each node has a candidate deviation path, which is the shortest
 * path from the global source to the global target that has the same prefix up to the node but deviates before reaching
 * the target of the local path, namely it does not use the full local path of the node. This way of maintaining the
 * paths is more efficient than the original algorithm, and improvements such as Lawler's are not necessary.
 *
 * <p>
 * The algorithms runs in \(O(nk(m+n \log n))\) time.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Yen%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class KShortestPathsSTYen implements KShortestPathsSTBase {

	@Override
	public List<IPath> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k) {
		if (!g.vertices().contains(source) || !g.vertices().contains(target))
			throw new IllegalArgumentException("source or target not in graph");
		if (k < 1)
			throw new IllegalArgumentException("k must be positive");
		w = IWeightFunction.replaceNullWeightFunc(w);
		Assertions.onlyPositiveEdgesWeights(g, w);
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
		IntSet globalSourceUsedOutEdges = new IntOpenHashSet(2);

		/* Compute the (first) shortest path and the next candidate */
		{
			ObjectDoublePair<IntList> sp1Pair = spSubRoutine.computeShortestPathSt(source);
			if (sp1Pair != null) {
				IntList sp1 = sp1Pair.first();
				result.add(IPath.valueOf(g, source, target, sp1));
				if (k == 1)
					return result;

				List<ObjectDoublePair<IntList>> sp2s = spSubRoutine.computeNextShortestPaths(source, sp1, sp1.size());
				ObjectDoublePair<IntList> sp2 = sp2s
						.stream()
						.filter(Objects::nonNull)
						.min((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble()))
						.orElse(null);
				if (sp2 != null) {
					globalSourceUsedOutEdges.add(sp1.getInt(0));
					Node rootNode = new Node(null, sp1, sp1, source, 0, sp2s, 0, true);
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
			candidateNum--;
			Node currentNode = minNode.value();
			IntList kthLocalPathOrig = currentNode.bestDeviationPath;
			IntList kthLocalPath = kthLocalPathOrig.subList(currentNode.deviationPathsSkip, kthLocalPathOrig.size());
			// double kthPathWeight = minNode.key();
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
			assert kthLocalPathOrig == currentNode.deviationPaths.get(commonPrefixLength).first();
			currentNode.deviationPaths.set(commonPrefixLength, null); /* don't use the path again */

			int splitVertex;
			if (g.isDirected()) {
				splitVertex = g.edgeSource(currentNode.localPath.getInt(commonPrefixLength));
			} else {
				splitVertex = IterTools
						.get(IPath.verticesIter(g, currentNode.localPathSource, kthLocalPath), commonPrefixLength);
			}

			newCandidates.clear();
			if (commonPrefixLength == 0) {
				/* branches at the source of local path */
				newCandidates.add(currentNode);

			} else {
				/* have some prefix in common */
				IntList prefixPath = currentNode.localPath.subList(0, commonPrefixLength);
				int prefixSource = currentNode.localPathSource;
				List<ObjectDoublePair<IntList>> prefixDeviationPaths =
						currentNode.deviationPaths.subList(0, commonPrefixLength);
				Node prefixNode = new Node(parent, currentNode.spSuffix, prefixPath, prefixSource,
						currentNode.weightToSource, prefixDeviationPaths, currentNode.deviationPathsSkip, false);
				prefixNode.targetUsedOutEdges.add(currentNode.localPath.getInt(commonPrefixLength));
				newCandidates.add(prefixNode);

				currentNode.parent = parent = prefixNode;
				currentNode.localPathSource = splitVertex;
				currentNode.weightToSource += w.weightSum(prefixPath);
				currentNode.spSuffix = currentNode.spSuffix.subList(commonPrefixLength, currentNode.spSuffix.size());
				currentNode.localPath = currentNode.localPath.subList(commonPrefixLength, currentNode.localPath.size());
				currentNode.deviationPaths =
						currentNode.deviationPaths.subList(commonPrefixLength, currentNode.deviationPaths.size());
				currentNode.deviationPathsSkip += commonPrefixLength;
				assert !currentNode.localPath.isEmpty();
				newCandidates.add(currentNode);
			}

			/* Create a node for the suffix of the kth path, after branching from current node local path */
			assert commonPrefixLength < kthLocalPath.size();
			{
				IntList suffixPath = kthLocalPath.subList(commonPrefixLength, kthLocalPath.size());
				Node suffixNode = new Node(parent, suffixPath, suffixPath, splitVertex, currentNode.weightToSource,
						null, 0, true);
				newCandidates.add(suffixNode);
			}

			/* Compute for each modified or new node a candidate deviation path */
			currentNode.clearBestDeviation();
			for (Node node : newCandidates) {
				if (node.deviationPaths == null) {

					// verticesMask.clear();
					edgesMask.clear();

					/* mask the path up to the current node */
					for (Node p = node.parent; p != null; p = p.parent) {
						IntIterator vIter = IPath.verticesIter(g, p.localPathSource, p.localPath);
						for (int i = p.localPath.size(); i-- > 0;) {
							int v = vIter.nextInt();
							// verticesMask.set(v);
							for (int vEdge : g.outEdges(v))
								edgesMask.set(vEdge);
							for (int vEdge : g.inEdges(v))
								edgesMask.set(vEdge);
						}
					}

					IntSet sourceUsedOutEdges =
							node.parent != null ? node.parent.targetUsedOutEdges : globalSourceUsedOutEdges;
					for (int e : sourceUsedOutEdges)
						edgesMask.set(e);

					node.deviationPaths = spSubRoutine
							.computeNextShortestPaths(node.localPathSource, node.localPath, node.localPath.size())
							.stream()
							.map(p -> p == null ? null
									: ObjectDoublePair.of(p.first(), p.secondDouble() + node.weightToSource))
							.collect(toList());

					sourceUsedOutEdges.add(node.localPath.getInt(0));
				}

				ObjectDoublePair<IntList> bestDeviation = node.deviationPaths
						.stream()
						.filter(Objects::nonNull)
						.min((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble()))
						.orElse(null);
				if (bestDeviation != null) {
					node.bestDeviationPath = bestDeviation.first();
					candidates.insert(bestDeviation.secondDouble(), node);
					candidateNum++;
				}
			}

			/* No need to keep more than k candidates, actually k-{#already_found} */
			for (; candidateNum > k - result.size(); candidateNum--)
				candidates.extractMax();
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
		/* The weight from the global source along the whole ancestors paths until this node local source */
		double weightToSource;
		/*
		 * The set of edges for which there is already a child for the parent->target. For each parent node, there is a
		 * single child which is responsible for computing the shortest paths deviating for the parent target vertex,
		 * which is the source vertex of all the children nodes. Only for that special child, the set is not null.
		 */
		final IntSet targetUsedOutEdges;
		/* The best deviation path for this node, null if there is no such one */
		IntList bestDeviationPath;

		List<ObjectDoublePair<IntList>> deviationPaths;
		int deviationPathsSkip;

		Node(Node parent, IntList spSuffix, IntList localPath, int localPathSource, double weightToSource,
				List<ObjectDoublePair<IntList>> deviationPaths, int deviationPathsSkip, boolean toTarget) {
			assert !localPath.isEmpty();
			this.parent = parent;
			this.spSuffix = spSuffix;
			this.localPath = localPath;
			this.localPathSource = localPathSource;
			this.weightToSource = weightToSource;
			this.deviationPaths = deviationPaths;
			this.deviationPathsSkip = deviationPathsSkip;
			this.targetUsedOutEdges = toTarget ? null : new IntOpenHashSet();
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

		private final int[] sBacktrack;
		private final int[] tBacktrack;
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
			sBacktrack = new int[n];
			tBacktrack = new int[n];

			heapS = IndexHeapDouble.newInstance(n);
			heapT = IndexHeapDouble.newInstance(n);
			visitedS = new BitmapSet(n);
			visitedT = new BitmapSet(n);
		}

		ObjectDoublePair<IntList> computeShortestPathSt(int source) {
			final ObjectDoublePair<IntList> res = computeShortestPathSt0(source);
			heapS.clear();
			heapT.clear();
			visitedS.clear();
			visitedT.clear();
			return res;
		}

		private ObjectDoublePair<IntList> computeShortestPathSt0(int source) {
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
			if (g.isDirected()) {
				for (int u = middle, e; u != source; u = g.edgeSource(e))
					path.add(e = sBacktrack[u]);
			} else {
				for (int u = middle, e; u != source; u = g.edgeEndpoint(e, u))
					path.add(e = sBacktrack[u]);
			}
			IntArrays.reverse(path.elements(), 0, path.size());

			/* add edges from middle to target */
			if (g.isDirected()) {
				for (int u = middle, e; u != target; u = g.edgeTarget(e))
					path.add(e = tBacktrack[u]);
			} else {
				for (int u = middle, e; u != target; u = g.edgeEndpoint(e, u))
					path.add(e = tBacktrack[u]);
			}
			return ObjectDoublePair.of(path, mu);
		}

		List<ObjectDoublePair<IntList>> computeNextShortestPaths(int source, IntList prevSp,
				int /* alpha */ maxDeviationPoint) {
			assert IPath.isPath(g, source, target, prevSp);

			List<ObjectDoublePair<IntList>> paths = new ArrayList<>(maxDeviationPoint);
			IntList maskedEdges = new IntArrayList();
			int deviationVertex = source, lastEdge = -1;
			for (int replacedEdge : prevSp.subList(0, maxDeviationPoint)) {
				edgesMask.set(replacedEdge);
				paths.add(computeShortestPathSt(source));
				edgesMask.clear(replacedEdge);

				for (int e : g.outEdges(deviationVertex))
					if (e != replacedEdge && e != lastEdge && edgesMask.set(e))
						maskedEdges.add(e);
				deviationVertex = g.edgeEndpoint(replacedEdge, deviationVertex);
				lastEdge = replacedEdge;
			}
			for (int e : maskedEdges)
				edgesMask.clear(e);
			return paths;
		}
	}

}
