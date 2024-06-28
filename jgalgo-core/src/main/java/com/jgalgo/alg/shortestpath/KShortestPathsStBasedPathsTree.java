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
package com.jgalgo.alg.shortestpath;

import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.jgalgo.alg.common.IPath;
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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

/**
 * A base class for computing the k-shortest paths from a source vertex to a target vertex in a graph using the
 * compressed paths tree data structure.
 *
 * <p>
 * Given a set of paths, the compressed paths tree is a tree that represents the paths in a compressed form. The tree is
 * built by looking at the longest common prefix among the paths, creating a node for the prefix and an edge for each
 * deviation from the prefix, and then recursively building the tree for the suffix of the paths. This base algorithm
 * find the k-th shortest paths in order to their weights, and maintain the paths tree of the k-1 shortest paths so far.
 * For each node in the tree the best deviation path, which is a path that shares the prefix up to the node but deviates
 * somewhere along the node local path, is computed and the node is added to a priority queue with the deviation path
 * weight. The node with the minimum weight is extracted from the queue, the deviation path is added to the result list
 * and to the paths tree, constant number of new nodes are added to the tree, and the best deviation path for each new
 * node is computed and added to the queue. The classes extending this class are different in the way they compute the
 * the best deviation path for a node. Some algorithms compute a single deviation path for each possible deviation point
 * along a node local path and the best deviation of the node is the minimum among them, and some algorithms are able to
 * compute the best deviation path in a single step. The latter algorithms can fail some times (see
 * {@link KShortestPathsStHershbergerMaxelSuri}), and in that case the algorithm falls back to the former method. In
 * general, computing the best deviation path in a single step is faster, but when the number of deviation points is
 * small, sometimes it is faster to compute all the deviation paths and find the minimum among them, as the computation
 * of the best deviation path in a single step is more complex.
 *
 * @author Barak Ugav
 */
public abstract class KShortestPathsStBasedPathsTree extends KShortestPathsStAbstract {

	/**
	 * Default constructor.
	 */
	public KShortestPathsStBasedPathsTree() {}

	@Override
	protected List<IPath> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k) {
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
		/*
		 * We mask edges to compute shortest path in a sub graph of the original graph instead of creating an actual sub
		 * graph to avoid allocating memory
		 */
		final Bitmap edgesMask = new Bitmap(m);
		final ShortestPathSubroutine spSubRoutine = newShortestPathSubroutine(g, w, target, edgesMask);

		List<IPath> result = new ObjectArrayList<>(k <= m ? k : 16);
		DoubleObjBinarySearchTree<Node> candidates = DoubleObjBinarySearchTree.newInstance();
		int candidateNum = 0;

		/* Compute the (first) shortest path and the next candidate */
		{
			ObjectDoublePair<IntList> sp1Pair = spSubRoutine.computeShortestPathSt(source);
			if (sp1Pair != null) {
				/* The global shortest path from the source to the target */
				IntList sp1 = sp1Pair.first();
				result.add(IPath.valueOf(g, source, target, sp1));
				if (k == 1)
					return result;

				ObjectDoublePair<IntList> sp2;
				List<ObjectDoublePair<IntList>> sp2s;
				FastReplacementAlgoResult sp2Res = spSubRoutine.computeBestDeviationPath(source, sp1, sp1.size());
				if (sp2Res.success) {
					/* The best deviation path was found in a single step */
					sp2 = sp2Res.value;
					sp2s = null;
				} else {
					/* Fall back to computing a deviation path from each possible deviation point */
					sp2s = spSubRoutine.computeAllDeviationsPaths(source, sp1, sp1.size());
					/* Find the best deviation path among all deviation points */
					sp2 = sp2s
							.stream()
							.filter(Objects::nonNull)
							.min((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble()))
							.orElse(null);
				}

				if (sp2 != null) {
					Node rootNode = new Node(null, sp1, sp1, source, 0, sp2s, 0, new IntArrayList());
					rootNode.sourceUsedOutEdges.add(sp1.getInt(0));
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
			if (currentNode.deviationPaths != null) {
				assert kthLocalPathOrig == currentNode.deviationPaths.get(commonPrefixLength).first();
				currentNode.deviationPaths.set(commonPrefixLength, null); /* don't use the path again */
			}

			int splitVertex;
			if (g.isDirected()) {
				splitVertex = g.edgeSource(currentNode.localPath.getInt(commonPrefixLength));
			} else {
				splitVertex = IterTools
						.get(IPath.verticesIter(g, currentNode.localPathSource, kthLocalPath), commonPrefixLength);
			}

			if (commonPrefixLength == 0) {
				/* branches at the source of local path */
				currentNode.bestDeviationPath = null;
				newCandidates.add(currentNode);

			} else {
				/* have some prefix in common */
				IntList prefixPath = currentNode.localPath.subList(0, commonPrefixLength);
				int prefixSource = currentNode.localPathSource;
				List<ObjectDoublePair<IntList>> prefixDeviationPaths = currentNode.deviationPaths == null ? null
						: currentNode.deviationPaths.subList(0, commonPrefixLength);
				Node prefixNode =
						new Node(parent, currentNode.spSuffix, prefixPath, prefixSource, currentNode.weightToSource,
								prefixDeviationPaths, currentNode.deviationPathsSkip, currentNode.sourceUsedOutEdges);
				newCandidates.add(prefixNode);

				currentNode.parent = parent = prefixNode;
				currentNode.localPathSource = splitVertex;
				currentNode.weightToSource += w.weightSum(prefixPath);
				currentNode.sourceUsedOutEdges = null;
				currentNode.spSuffix = currentNode.spSuffix.subList(commonPrefixLength, currentNode.spSuffix.size());
				currentNode.localPath = currentNode.localPath.subList(commonPrefixLength, currentNode.localPath.size());
				if (currentNode.deviationPaths != null) {
					currentNode.deviationPaths =
							currentNode.deviationPaths.subList(commonPrefixLength, currentNode.deviationPaths.size());
					currentNode.deviationPathsSkip += commonPrefixLength;
				}
				assert !currentNode.localPath.isEmpty();
				currentNode.bestDeviationPath = null;
				newCandidates.add(currentNode);
			}

			/* Create a node for the suffix of the kth path, after branching from current node local path */
			assert commonPrefixLength < kthLocalPath.size();
			{
				IntList suffixPath = kthLocalPath.subList(commonPrefixLength, kthLocalPath.size());
				IntList sourceUsedOutEdges;
				if (currentNode.sourceUsedOutEdges != null) {
					sourceUsedOutEdges = currentNode.sourceUsedOutEdges;
					currentNode.sourceUsedOutEdges = null;
					assert sourceUsedOutEdges.contains(currentNode.localPath.getInt(0));
				} else {
					sourceUsedOutEdges = new IntArrayList();
					sourceUsedOutEdges.add(currentNode.localPath.getInt(0));
				}
				sourceUsedOutEdges.add(kthLocalPath.getInt(commonPrefixLength));
				Node suffixNode = new Node(parent, suffixPath, suffixPath, splitVertex, currentNode.weightToSource,
						null, 0, sourceUsedOutEdges);
				newCandidates.add(suffixNode);
			}

			/* Compute for each modified or new node a candidate deviation path */
			for (Node node : newCandidates) {
				ObjectDoublePair<IntList> bestDeviation = null;
				if (node.deviationPaths == null) {
					edgesMask.clear();

					/*
					 * mask the path up to the current node. We only mask edges, there is no need to mask the vertices
					 * as we mask all edges of a 'masked' vertex
					 */
					for (Node p = node.parent; p != null; p = p.parent) {
						IntIterator vIter = IPath.verticesIter(g, p.localPathSource, p.localPath);
						for (int i = p.localPath.size(); i-- > 0;) {
							int v = vIter.nextInt();
							for (int vEdge : g.outEdges(v))
								edgesMask.set(vEdge);
							for (int vEdge : g.inEdges(v))
								edgesMask.set(vEdge);
						}
					}

					/*
					 * If the node holds the list of used out edges of the source, this node, of out all the parent
					 * children nodes, is responsible of computing a deviation path from the parent target vertex (which
					 * is this node local source). If this is not the case, we mask all edges of the source to eliminate
					 * possible paths that deviate from the source.
					 */
					for (int e : node.sourceUsedOutEdges != null ? node.sourceUsedOutEdges
							: g.outEdges(node.localPathSource))
						edgesMask.set(e);
					/* Unmask the first edge of node local path, always allow deviation after it */
					edgesMask.clear(node.localPath.getInt(0));

					FastReplacementAlgoResult fastRes = spSubRoutine
							.computeBestDeviationPath(node.localPathSource, node.spSuffix, node.localPath.size());
					if (fastRes.success) {
						/* Found the best deviation path in a single step, save it (if its null, bestDeviation=null) */
						if (fastRes.value != null)
							bestDeviation = ObjectDoublePair
									.of(fastRes.value.first(), fastRes.value.secondDouble() + node.weightToSource);

					} else {
						/* Fall back to computing a deviation path from each possible deviation point */
						node.deviationPaths = spSubRoutine
								.computeAllDeviationsPaths(node.localPathSource, node.spSuffix, node.localPath.size())
								.stream()
								.map(p -> p == null ? null
										: ObjectDoublePair.of(p.first(), p.secondDouble() + node.weightToSource))
								.collect(toList());
					}
				}

				if (bestDeviation == null && node.deviationPaths != null) {
					/* Find the best deviation path among all deviation points */
					bestDeviation = node.deviationPaths
							.stream()
							.filter(Objects::nonNull)
							.min((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble()))
							.orElse(null);
				}

				/* Add the node to the candidates queue */
				if (bestDeviation != null) {
					node.bestDeviationPath = bestDeviation.first();
					candidates.insert(bestDeviation.secondDouble(), node);
					candidateNum++;
				}
			}
			newCandidates.clear();

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
	private static final class Node {

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
		 * The set of edges for which there is already a child of the local path. For each parent node, there is a
		 * single child which is responsible for computing the shortest paths deviating for the parent target vertex,
		 * which is the source vertex of all the children nodes. Only for that special child, the set is not null.
		 */
		IntList sourceUsedOutEdges;
		/* The best deviation path for this node, null if there is no such one */
		IntList bestDeviationPath;

		List<ObjectDoublePair<IntList>> deviationPaths;
		int deviationPathsSkip;

		Node(Node parent, IntList spSuffix, IntList localPath, int localPathSource, double weightToSource,
				List<ObjectDoublePair<IntList>> deviationPaths, int deviationPathsSkip, IntList sourceUsedOutEdges) {
			assert !localPath.isEmpty();
			this.parent = parent;
			this.spSuffix = spSuffix;
			this.localPath = localPath;
			this.localPathSource = localPathSource;
			this.weightToSource = weightToSource;
			this.deviationPaths = deviationPaths;
			this.deviationPathsSkip = deviationPathsSkip;
			this.sourceUsedOutEdges = sourceUsedOutEdges;
		}

	}

	protected abstract ShortestPathSubroutine newShortestPathSubroutine(IndexGraph g, IWeightFunction w, int target,
			Bitmap edgesMask);

	/**
	 * A result object for the best deviation path computation in a shortest path subroutine.
	 *
	 * <p>
	 * This object is used when there is an attempt to compute the best deviation path in a single step. If the attempt
	 * fails, the algorithm falls back to computing a deviation path from each possible deviation point.
	 *
	 * @author Barak Ugav
	 */
	protected static class FastReplacementAlgoResult {
		final boolean success;
		final ObjectDoublePair<IntList> value;

		private FastReplacementAlgoResult(boolean success, ObjectDoublePair<IntList> value) {
			this.success = success;
			this.value = value;
		}

		public static FastReplacementAlgoResult ofSuccess(ObjectDoublePair<IntList> value) {
			return new FastReplacementAlgoResult(true, value);
		}

		public static FastReplacementAlgoResult ofFailure() {
			return new FastReplacementAlgoResult(false, null);
		}
	}

	/**
	 * A subroutine for computing the best deviation path for a node in the paths tree.
	 *
	 * <p>
	 * The subroutine compute a single shortest path from a source vertex to a target vertex in a graph, with some
	 * masked edges. It is used to compute the best deviation path for a node in the paths tree.
	 *
	 * @author Barak Ugav
	 */
	protected abstract static class ShortestPathSubroutine {

		final IndexGraph g;
		final IWeightFunction w;
		final int target;

		final Bitmap edgesMask;

		final int[] sBacktrack;
		final int[] tBacktrack;
		final IndexHeapDouble heapS;
		final IndexHeapDouble heapT;
		final BitmapSet visitedS;
		final BitmapSet visitedT;

		/**
		 * Constructs a new shortest path subroutine.
		 *
		 * <p>
		 * The subroutine compute a single shortest path from a source vertex to a target vertex in a graph, with some
		 * masked edges. It is used to compute the best deviation path for a node in the paths tree.
		 *
		 * @param g         the graph, should not be modified for the lifetime of the subroutine
		 * @param w         an edge weight function
		 * @param target    the target vertex. The target is fixed, and a source is provided for each subroutine call
		 * @param edgesMask a mask of edges to exclude from the graph, used to force the shortest path to avoid some
		 *                      edges or vertices. The reference is stored to the given object, and the caller should
		 *                      modify the mask as needed before calling the subroutine
		 * @param heapS     a heap for the source side. This argument is provided so the caller can reuse the heap and
		 *                      the distances array
		 * @param heapT     a heap for the target side. This argument is provided so the caller can reuse the heap and
		 *                      the distances array
		 */
		public ShortestPathSubroutine(IndexGraph g, IWeightFunction w, int target, Bitmap edgesMask,
				IndexHeapDouble heapS, IndexHeapDouble heapT) {
			this.g = g;
			this.w = w;
			this.target = target;

			this.edgesMask = edgesMask;

			final int n = g.vertices().size();
			sBacktrack = new int[n];
			tBacktrack = new int[n];

			this.heapS = heapS != null ? heapS : IndexHeapDouble.newInstance(n);
			this.heapT = heapT != null ? heapT : IndexHeapDouble.newInstance(n);
			visitedS = new BitmapSet(n);
			visitedT = new BitmapSet(n);
		}

		public ObjectDoublePair<IntList> computeShortestPathSt(int source) {
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

		public List<ObjectDoublePair<IntList>> computeAllDeviationsPaths(int source, IntList prevSp,
				int maxDeviationPoint) {
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

		public abstract FastReplacementAlgoResult computeBestDeviationPath(int source, IntList prevSp,
				int maxDeviationPoint);

	}

}
