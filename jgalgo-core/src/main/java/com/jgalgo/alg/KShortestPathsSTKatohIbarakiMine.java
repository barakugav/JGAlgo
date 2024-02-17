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
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.ds.DoubleObjBinarySearchTree;
import com.jgalgo.internal.ds.IndexHeap;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

/**
 * Katoh, Ibaraki and Mine algorithm for K shortest simple paths in undirected graphs.
 *
 * <p>
 * The algorithm is similar to Yen's algorithm, but to compute the best 'replacement' path (deviation path, the next
 * shortest path that replace at least one edge from the previous k-th shortest path) it compute it in time \(O(m + n
 * \log n)\) using two shortest path trees. Yen's algorithm runs a S-T shortest path computation for each edge in the
 * last k-th shortest path, therefore each such iteration runs in time \(O(n(m + n \log n))\).
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
class KShortestPathsSTKatohIbarakiMine implements KShortestPathsSTBase {

	private final ShortestPathST stSpAlgo = ShortestPathST.newInstance();

	@Override
	public List<IPath> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k) {
		if (!g.vertices().contains(source) || !g.vertices().contains(target))
			throw new IllegalArgumentException("source or target not in graph");
		if (k < 1)
			throw new IllegalArgumentException("k must be positive");
		w = WeightFunctions.localEdgeWeightFunction(g, w);
		w = IWeightFunction.replaceNullWeightFunc(w);
		Assertions.onlyPositiveEdgesWeights(g, w);
		Assertions.onlyUndirected(g);
		if (source == target)
			return List.of(IPath.valueOf(g, source, target, Fastutil.list()));

		final int n = g.vertices().size();
		final int m = g.edges().size();
		final Bitmap verticesMask = new Bitmap(n);
		final Bitmap edgesMask = new Bitmap(m);
		final ShortestPathSubroutine spSubRoutine = new ShortestPathSubroutine(g, w, target, verticesMask, edgesMask);

		List<IPath> result = new ObjectArrayList<>(k <= m ? k : 16);
		DoubleObjBinarySearchTree<Node> candidates = DoubleObjBinarySearchTree.newInstance();
		int candidateNum = 0;

		/* Compute the (first) shortest path and the next candidate */
		{
			IPath sp1 = (IPath) stSpAlgo.computeShortestPath(g, w, Integer.valueOf(source), Integer.valueOf(target));
			if (sp1 != null) {
				result.add(sp1);
				if (k == 1)
					return result;

				ObjectDoublePair<IntList> sp2 =
						spSubRoutine.computeNextShortestPath(source, sp1.edges(), sp1.edges().size());
				if (sp2 != null) {
					Node rootNode = new Node(null, sp1.edges(), sp1.edges(), source, w.weightSum(sp1.edges()), true);
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
			Node currentNode = candidates.extractMin().value();
			candidateNum--;
			IntList kthLocalPath = currentNode.bestDeviationPath;
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

			int splitVertex = IterTools
					.get(IPath.verticesIter(g, currentNode.localPathSource, currentNode.localPath), commonPrefixLength);

			newCandidates.clear();
			if (commonPrefixLength == 0) {
				/* branches at the source of local path */
				currentNode.sourceUsedOutEdges.add(kthLocalPath.getInt(commonPrefixLength));
				newCandidates.add(currentNode);

			} else {
				/* have some prefix in common */
				IntList prefixPath = currentNode.localPath.subList(0, commonPrefixLength);
				double prefixWeight = (parent != null ? parent.pathWeight : 0) + w.weightSum(prefixPath);
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
				double suffixWeight = (parent != null ? parent.pathWeight : 0) + w.weightSum(suffixPath);
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

				/* mask the path up to the current node */
				verticesMask.clear();
				for (Node p = node.parent; p != null; p = p.parent) {
					for (int v : IterTools.foreach(IPath.verticesIter(g, p.localPathSource, p.localPath))) {
						if (v != localSource) {
							verticesMask.set(v);
							for (int e : g.outEdges(v))
								edgesMask.set(e);
						}
					}
				}

				/* mask source edges already used */
				edgesMask.clear();
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

		private final Bitmap verticesMask;
		private final Bitmap edgesMask;

		private final IndexHeap heap;
		private final double[] sDistances;
		private final double[] tDistances;
		private final int[] sBacktrack;
		private final int[] tBacktrack;
		private final int[] sXi;
		private final int[] tXi;
		private final Bitmap visited;

		ShortestPathSubroutine(IndexGraph g, IWeightFunction w, int target, Bitmap verticesMask, Bitmap edgesMask) {
			this.g = g;
			this.w = w;
			this.target = target;

			this.verticesMask = verticesMask;
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
						if (verticesMask.get(v))
							continue;
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

		/* FSP(G, s, t R) */
		ObjectDoublePair<IntList> computeNextShortestPath(int source, IntList prevSp,
				int /* alpha */ maxDeviationPoint) {
			final int n = g.vertices().size();
			computeShortestPathTrees(source, prevSp);

			double bestWeight = Double.POSITIVE_INFINITY;
			int bestVertex = -1, bestEdge = -1;
			for (int u : range(n)) {
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
						if (verticesMask.get(v))
							continue;
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

			if (bestWeight == Double.POSITIVE_INFINITY)
				return null;
			assert bestVertex >= 0;

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
			for (int v = bestVertex;;) {
				int e = sBacktrack[v];
				if (e < 0)
					break;
				path.add(e);
				v = g.edgeEndpoint(e, v);
			}
			IntArrays.reverse(path.elements(), 0, path.size());

			/* Add the connecting edge that is not included in both shortest path trees */
			int v = bestVertex;
			if (bestEdge >= 0) {
				path.add(bestEdge);
				v = g.edgeEndpoint(bestEdge, v);
			}

			/* Add edges from bestVertex to target */
			for (;;) {
				int e = tBacktrack[v];
				if (e < 0)
					break;
				path.add(e);
				v = g.edgeEndpoint(e, v);
			}

			assert new IntOpenHashSet(IPath.verticesIter(g, source, path)).size() == path.size()
					+ 1 : "path is not simple";
			return ObjectDoublePair.of(path, bestWeight);
		}
	}
}
