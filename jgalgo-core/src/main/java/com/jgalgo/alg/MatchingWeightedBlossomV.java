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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.HeapReference;
import com.jgalgo.internal.ds.HeapReferenceable;
import com.jgalgo.internal.ds.Heaps;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.DebugPrinter;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

/**
 * Blossom V implementation for maximum weighted matching.
 *
 * <p>
 * The implementation is based on 'Blossom V: A new implementation of a minimum cost perfect matching algorithm' by
 * Vladimir Kolmogorov. It is an implementation of Edmonds 'Blossom' algorithm, using priory queues
 * ({@link HeapReferenceable}, pairing heaps) to find the next tight edge each iteration. In contrast to
 * {@link MatchingWeightedGabow1990}, it achieve a worse \(O(n^3 m)\) running time in the worst case, but runs faster in
 * practice.
 *
 * <p>
 * The implementation actually computes minimum perfect matching, and assume such perfect matching always exists.
 * Maximum or non-perfect matchings are computed by a reduction to a minimum perfect matching instance.
 *
 * @author Barak Ugav
 */
class MatchingWeightedBlossomV extends Matchings.AbstractMinimumMatchingImpl {

	@Override
	IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w) {
		/*
		 * The BlossomV algorithm support perfect matching only, and assume such matching always exists. To support
		 * non-perfect matching, we perform a reduction: we create a new graph containing two identical copies of the
		 * original graph, namely for each original vertex v we create two vertices v1 v2, and for each edge e(u,v) in
		 * the original graph we create two edges e1(u1,v1) e2(u2,v2) in the new graph, with the same weights as e. In
		 * addition to the two copies of the original graph, we connect each pair of duplicate vertices v1 v2 using a
		 * 'dummy' edge, with weight 0. We get a solution for the graph with the two copies and extract a matching from
		 * only one of the copies. If the perfect-matching algorithm chose to match a dummy edge (v1,v2), it means it
		 * doesn't have to match v1 or v2 with any other vertex, and it will not be included in any copy (half) of the
		 * graph. This essentially allows the algorithm to avoid matching a vertex in the original graph.
		 */

		Assertions.Graphs.onlyUndirected(g);
		IndexGraphBuilder b = IndexGraphBuilder.newUndirected();
		b.expectedVerticesNum(g.vertices().size() * 2);
		b.expectedEdgesNum(g.edges().size() * 2 + g.vertices().size());

		/* Add two vertices for each original vertex */
		for (int n = g.vertices().size(), v = 0; v < n; v++) {
			int v1 = b.addVertex();
			int v2 = b.addVertex();
			assert v1 == v * 2 + 0;
			assert v2 == v * 2 + 1;
		}
		/* Add two edges for each original edge */
		final int dummyEdgesThreshold = g.edges().size() * 2;
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			int source = g.edgeSource(e);
			int target = g.edgeTarget(e);
			int e1 = b.addEdge(source * 2 + 0, target * 2 + 0);
			int e2 = b.addEdge(source * 2 + 1, target * 2 + 1);
			assert e1 == e * 2 + 0;
			assert e2 == e * 2 + 1;
			assert e1 < dummyEdgesThreshold;
			assert e2 < dummyEdgesThreshold;
		}

		/* Add dummy edges between each pair of duplicated vertices */
		for (int n = g.vertices().size(), v = 0; v < n; v++) {
			int v1 = v * 2 + 0;
			int v2 = v * 2 + 1;
			int e = b.addEdge(v1, v2);
			assert e >= dummyEdgesThreshold;
		}

		/* Compute a perfect matching in the new graph */
		IWeightFunction wDup;
		if (WeightFunction.isInteger(w)) {
			IWeightFunctionInt wInt = (IWeightFunctionInt) w;
			IWeightFunctionInt wDupInt = e -> e < dummyEdgesThreshold ? wInt.weightInt(e / 2) : 0;
			wDup = wDupInt;
		} else {
			wDup = e -> e < dummyEdgesThreshold ? w.weight(e / 2) : 0;
		}
		IndexGraph gDup = b.build();
		IMatching matchDup = computeMinimumWeightedPerfectMatching(gDup, wDup);

		/* Convert matching to the original graph */
		int[] matched = new int[g.vertices().size()];
		for (int n = g.vertices().size(), v = 0; v < n; v++) {
			int vDup = v * 2 + 0;
			int eDup = matchDup.getMatchedEdge(vDup);
			assert eDup >= 0 : "vertex " + vDup + " is not matched";
			int e = eDup < dummyEdgesThreshold ? eDup / 2 : -1;
			matched[v] = e;
		}
		return new Matchings.MatchingImpl(g, matched);
	}

	@Override
	IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		if (w == null)
			w = IWeightFunction.CardinalityWeightFunction;
		return new Worker(g, w).solve();
	}

	private static class Worker {

		/* The input graph */
		private final IndexGraph g;
		/* A dummy blossom used as a head of a linked lists of the current roots */
		private final Blossom treeList;
		/* per original vertex blossom */
		final Blossom[] singletonNodes;
		/* current number of trees, same as number of unmatched vertices */
		/* we are done when it reaches zero */
		private int treeNum;

		/* temporary pointer to an augmentation edge found during grow/shrink/expand */
		private Edge eAugment = null;

		/* Temp list used during expand */
		private final List<Blossom> expandTemp = new ArrayList<>();

		private static final boolean OptimizationGrowSubTree = Boolean.parseBoolean("true");

		Worker(IndexGraph g, IWeightFunction w) {
			Debug.reset();

			this.g = g;
			final int n = g.vertices().size();
			if (n % 2 != 0)
				throw new IllegalArgumentException("number of vertices is odd: perfect matching cannot exist");
			singletonNodes = new Blossom[n];
			for (int v = 0; v < n; v++)
				singletonNodes[v] = new Blossom();
			Debug.init(this);

			/* Init all (singleton) blossoms to unmatched even + */
			for (int v = 0; v < n; v++) {
				Blossom b = singletonNodes[v];
				b.setEven();
				b.setOuter(true);
			}
			treeNum = n;

			/* Create all edges objects */
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				Blossom U = singletonNodes[g.edgeSource(e)];
				Blossom V = singletonNodes[g.edgeTarget(e)];
				if (U == V)
					continue; /* can't match self edges */
				Edge E = new Edge(e, U, V);
				U.addEdgeOut(E);
				V.addEdgeIn(E);

				/* We multiply here by 2 so integers weights will result in round dual values */
				/* any constant factor is fine here */
				E.slack = w.weight(e) * 2;
			}

			treeList = new Blossom() {
				@Override
				public String toString() {
					return "treeList";
				}
			};
		}

		IMatching solve() {
			dbgLog.format("\n\nsolve()\n");
			initGreedy();
			Debug.assertConstraints(this);

			/* initialize auxiliary graph */
			for (Blossom root : roots()) {
				Tree tree = root.tree;
				for (Edge e : root.outEdges()) {
					Blossom v = e.target;
					if (v.isOut()) {
						tree.pqInsertEvenOut(e);

					} else if (v.isProcessed()) {
						ensureTreesEdgeExists(tree, v.tree);
						v.tree.currentEdge.pqInsertEvenEven(e);
					}
				}
				for (Edge e : root.inEdges()) {
					Blossom v = e.source;
					if (v.isOut()) {
						tree.pqInsertEvenOut(e);

					} else if (v.isProcessed()) {
						ensureTreesEdgeExists(tree, v.tree);
						v.tree.currentEdge.pqInsertEvenEven(e);
					}
				}
				root.setProcessed(true);
				for (TreesEdge e : tree.outEdges())
					e.target.currentEdge = null;
				for (TreesEdge e : tree.inEdges())
					e.source.currentEdge = null;
			}
			Debug.assertConstraints(this);

			/* main loop */
			for (;;) {
				final int iterationTreeNum = treeNum;
				treesLoop: for (Blossom root : rootsDuringAugmentation()) {
					assert root.isTreeRoot();
					assert root == root.tree.root;

					Tree tree = root.tree;
					/* Check for augmentation, set currentEdge */
					for (TreesEdge e : tree.outEdgesAndProneRemoved()) {
						Tree t2 = e.target;
						t2.currentEdge = e;

						if (e.pqEvenEven.isNotEmpty()) {
							Edge minEdge = e.pqEvenEven.findMin().key();
							if (minEdge.slack - tree.eps <= t2.eps) {
								augment(minEdge);
								assert e.pqEvenEven.isEmpty() || e.pqEvenEven.findMin()
										.key() != minEdge : "edge was not removed during augment";
								continue treesLoop;
							}
						}
					}
					for (TreesEdge e : tree.inEdgesAndProneRemoved()) {
						Tree t2 = e.source;
						t2.currentEdge = e;

						Edge minEdge;
						if (e.pqEvenEven.isNotEmpty()
								&& (minEdge = e.pqEvenEven.findMin().key()).slack - tree.eps <= t2.eps) {
							/* Augment step */
							augment(minEdge);
							assert e.pqEvenEven.isEmpty()
									|| e.pqEvenEven.findMin().key() != minEdge : "edge was not removed during augment";
							continue treesLoop;
						}
					}
					Debug.assertConstraints(this);

					/* Grow tree */
					for (;;) {
						Edge minEdge;
						Blossom minBlossom;

						if (tree.pqEvenOut.isNotEmpty()
								&& (minEdge = tree.pqEvenOut.findMin().key()).slack <= tree.eps) {
							/* Grow step */
							/* no need to extractMin(), all Even-Out edges of minEdge's target will be removed */
							boolean augmentPerformed = grow(minEdge);
							if (augmentPerformed)
								continue treesLoop;

						} else if (tree.pqEvenEven.isNotEmpty()
								&& (minEdge = tree.pqEvenEven.findMin().key()).slack <= tree.eps * 2) {
							/* Shrink step */
							tree.pqRemoveEvenEven(minEdge);
							if (processEdgeEvenEven(minEdge, true)) {
								boolean augmentPerformed = shrink(minEdge);
								if (augmentPerformed)
									continue treesLoop;
							}

						} else if (tree.pqOdd.isNotEmpty()
								&& (minBlossom = tree.pqOdd.findMin().key()).dual <= tree.eps) {
							/* Expand step */
							tree.pqRemoveOdd(minBlossom);
							boolean augmentPerformed = expand(minBlossom);
							if (augmentPerformed)
								continue treesLoop;

						} else {
							break;
						}
					}
					Debug.assertConstraints(this);

					/* Clear currentEdge */
					assert tree.currentEdge == null;
					for (TreesEdge e : tree.outEdges()) {
						assert e.source == tree;
						assert e.target.currentEdge == e;
						e.target.currentEdge = null;
					}
					for (TreesEdge e : tree.inEdges()) {
						assert e.target == tree;
						assert e.source.currentEdge == e;
						e.source.currentEdge = null;
					}
				}
				Debug.assertConstraints(this);

				if (treeNum == 0)
					break;

				boolean augmentPerformed = treeNum != iterationTreeNum;
				if (!augmentPerformed) {
					updateDuals();
					// TODO if regular (CC) update doesnt achieve progress, try ComputeEpsSingle
				}
				Debug.assertConstraints(this);
			}
			return finish();
		}

		private void initGreedy() {
			dbgLog.format("initGreedy()\n");

			/* Init each node dual to be minVertexEdgeWeight/2 */
			for (Blossom node : singletonNodes) {
				assert !node.isBlossom();
				double minSlack = Double.POSITIVE_INFINITY;
				for (Edge e : node.outEdges())
					minSlack = Math.min(minSlack, e.slack);
				for (Edge e : node.inEdges())
					minSlack = Math.min(minSlack, e.slack);
				assert node.isOuter();
				node.dual = minSlack / 2;
			}
			/* Update edges slack to weight-e.source.dual-e.target.dual */
			for (Blossom node : singletonNodes) {
				for (Edge e : node.outEdges()) {
					assert node == e.source;
					e.slack -= (node.dual + e.target.dual);
				}
			}
			/* assert all edges have non negative slack */
			for (Blossom node : singletonNodes)
				for (Edge e : node.outEdges())
					assert e.slack >= 0;

			/* Iterate over the nodes, increase dual and choose matched edge greedily */
			for (Blossom node : singletonNodes) {
				if (node.isOut()) /* already matched */
					continue;
				double slackMin = Double.POSITIVE_INFINITY;
				for (Edge e : node.outEdges())
					slackMin = Math.min(slackMin, e.slack);
				for (Edge e : node.inEdges())
					slackMin = Math.min(slackMin, e.slack);
				node.dual += slackMin;

				/* After we increased the dual of the node, at least one edge should be tight */
				Iterator<Edge> outIter = node.outEdges().iterator();
				if (node.isEven()) {
					while (outIter.hasNext()) {
						Edge e = outIter.next();
						assert node == e.source;
						if (e.slack <= slackMin && e.target.isEven()) {
							dbgLog.format("\tmatch %s\n", e);
							node.setOut();
							e.target.setOut();
							node.match = e.target.match = e;
							treeNum -= 2;
							e.slack -= slackMin;
							break;
						}
						e.slack -= slackMin;
					}
				}
				while (outIter.hasNext()) {
					Edge e = outIter.next();
					assert node == e.source;
					e.slack -= slackMin;
				}
				Iterator<Edge> inIter = node.inEdges().iterator();
				if (node.isEven()) {
					while (inIter.hasNext()) {
						Edge e = inIter.next();
						assert node == e.target;
						if (e.slack <= slackMin && node.isEven() && e.source.isEven()) {
							dbgLog.format("\tmatch %s\n", e);
							node.setOut();
							e.source.setOut();
							node.match = e.source.match = e;
							treeNum -= 2;
							e.slack -= slackMin;
							break;
						}
						e.slack -= slackMin;
					}
				}
				while (inIter.hasNext()) {
					Edge e = inIter.next();
					assert node == e.target;
					e.slack -= slackMin;
				}
			}

			/* Create all trees and tree list */
			Blossom lastRoot = treeList;
			for (Blossom node : singletonNodes) {
				if (!node.isEven())
					continue; /* we already matched this vertex */
				node.setIsTreeRoot(true);
				node.treeSiblingPrev = lastRoot;
				lastRoot.treeSiblingNext = node;
				lastRoot = node;

				Tree tree = new Tree(node);
				node.tree = tree;
				dbgLog.format("\troot %s\n", node);
			}
			lastRoot.treeSiblingNext = null;
		}

		/**
		 * Grow the forest by adding a 'grow edge' and its descending matched edge.
		 *
		 * @param  growEdge the edge from an even node to an out node
		 * @return          {@code true} if augmentation was performed
		 */
		private boolean grow(Edge growEdge) {
			Blossom u, v;
			if (growEdge.target.isOut()) {
				u = growEdge.getSourceOuterBlossom();
				v = growEdge.target;
			} else {
				assert growEdge.source.isOut();
				u = growEdge.getTargetOuterBlossom();
				v = growEdge.source;
			}
			assert u.isOuter() && u.isEven();
			assert v.isOuter() && v.isOut();

			addGrowBlossomsToTree(u, v, growEdge);
			assert v.isMatched();
			Blossom w = v.matchedNode();
			assert w.isEven();

			/* Grow the new sub tree iteratively, until no more grow step is available */
			for (Blossom j = w;;) {
				boolean augmentPerformed = grow0(j);
				if (augmentPerformed)
					return true;

				/* continue exploring sub tree for more grow operations */
				if (j.firstTreeChild != null) {
					j = j.firstTreeChild;
				} else {
					for (;;) {
						if (j == w)
							return false;
						if (j.treeSiblingNext != null)
							break;
						j = j.matchedNode().getTreeParent();
					}
					j = j.treeSiblingNext;
				}
			}
		}

		private static void addGrowBlossomsToTree(Edge growEdge) {
			Blossom u, v;
			if (growEdge.target.isOut()) {
				u = growEdge.getSourceOuterBlossom();
				v = growEdge.target;
			} else {
				assert growEdge.source.isOut();
				u = growEdge.getTargetOuterBlossom();
				v = growEdge.source;
			}
			addGrowBlossomsToTree(u, v, growEdge);
		}

		private static void addGrowBlossomsToTree(Blossom u, Blossom v, Edge growEdge) {
			assert u.isOuter() && u.isEven();
			assert v.isOuter() && v.isOut();
			Tree tree = u.tree;

			v.setOdd();
			// assert v.tree == null;
			v.tree = tree;
			v.treeParentEdge = growEdge;
			v.dual += tree.eps;

			assert v.isMatched();
			Blossom w = v.matchedNode();
			w.setEven();
			w.tree = tree;
			w.dual -= tree.eps;

			/* Although v is the direct 'parent' of w, the child-parent is a relationship between even nodes only */
			addTreeChild(u, w);
		}

		/**
		 * Grow the forest given the new even node grown.
		 *
		 * @param  w the new even node grown
		 * @return   {@code true} if augmentation was performed
		 */
		private boolean grow0(Blossom w) {
			Blossom v = w.matchedNode();
			Blossom u = v.getTreeParent();
			dbgLog.format("grow(%s, %s, %s)\n", u, v, v.matchedNode());
			assert u.isOuter() && u.isEven();
			assert v.isOuter() && v.isOdd();
			assert w.isOuter() && w.isEven();
			Tree tree = u.tree;

			/* Add v as an odd (-) node to the tree */
			/* Handle (v,w) edges, which may become (-,+) */
			for (Edge e : v.outEdges()) {
				Blossom x = e.getTargetOuterBlossom();
				if (x.isEven() && x.isProcessed()) {
					// assert e.pqEvenOutRef != null;
					// if (e.pqEvenOutRef != null)
					if (!OptimizationGrowSubTree || e.pqRef != null) {
						assert e.pqRef != null;
						x.tree.pqRemoveEvenOut(e);
					}
					e.slack -= tree.eps;
					if (x.tree != tree) {
						ensureTreesEdgeExists(tree, x.tree);
						x.tree.pqEdgeInsertEvenOdd(x.tree.currentEdge, e);
					}
				} else {
					e.slack -= tree.eps;
				}
			}
			/* Handle (v,w) edges, which may become (-,+) */
			for (Edge e : v.inEdges()) {
				Blossom x = e.getSourceOuterBlossom();
				if (x.isEven() && x.isProcessed()) {
					// assert e.pqEvenOutRef != null;
					// if (e.pqEvenOutRef != null)
					if (!OptimizationGrowSubTree || e.pqRef != null) {
						assert e.pqRef != null;
						x.tree.pqRemoveEvenOut(e);
					}
					e.slack -= tree.eps;
					if (x.tree != tree) {
						ensureTreesEdgeExists(tree, x.tree);
						x.tree.pqEdgeInsertEvenOdd(x.tree.currentEdge, e);
					}
				} else {
					e.slack -= tree.eps;
				}
			}

			/* Add w as an even (+) node to the tree */
			eAugment = null;
			for (Edge e : w.outEdges()) {
				Blossom x = e.getTargetOuterBlossom();
				e.slack += tree.eps;
				if (x.isOut()) {
					/* e is (+,x) edge */
					if (OptimizationGrowSubTree && e.slack <= 0) {
						/* grow e immediately, no need to insert into PQ and extract it in the next iteration */
						addGrowBlossomsToTree(e);
					} else {
						tree.pqInsertEvenOut(e);
					}

				} else if (x.isEven() && x.isProcessed()) {
					// if (e.pqEvenOutRef != null)
					if (!OptimizationGrowSubTree || e.pqRef != null) {
						assert e.pqRef != null;
						x.tree.pqRemoveEvenOut(e);
					}
					if (tree != x.tree) {
						/* Cross-trees (+,+) edge */
						if (e.slack <= x.tree.eps)
							/* we can augment using e, prioritize augmentation over other operations */
							eAugment = e;
						ensureTreesEdgeExists(tree, x.tree);
						x.tree.currentEdge.pqInsertEvenEven(e);
					} else {
						/* Inner (+,+) edge */
						tree.pqInsertEvenEven(e);
					}
				} else if (x.isOdd() && tree != x.tree) {
					/* Cross-trees (+,-) edge */
					ensureTreesEdgeExists(tree, x.tree);
					tree.pqEdgeInsertEvenOdd(x.tree.currentEdge, e);
				}
			}
			for (Edge e : w.inEdges()) {
				Blossom x = e.getSourceOuterBlossom();
				e.slack += tree.eps;
				if (x.isOut()) {
					/* e is (+,x) edge */
					if (OptimizationGrowSubTree && e.slack <= 0) {
						/* grow e immediately, no need to insert into PQ and extract it in the next iteration */
						addGrowBlossomsToTree(e);
					} else {
						tree.pqInsertEvenOut(e);
					}

				} else if (x.isEven() && x.isProcessed()) {
					// assert e.pqEvenOutRef != null;
					if (!OptimizationGrowSubTree || e.pqRef != null) {
						assert e.pqRef != null;
						x.tree.pqRemoveEvenOut(e);
					}
					if (tree != x.tree) {
						/* Cross-trees (+,+) edge */
						if (e.slack <= x.tree.eps)
							/* we can augment using e, prioritize augmentation over other operations */
							eAugment = e;
						ensureTreesEdgeExists(tree, x.tree);
						x.tree.currentEdge.pqInsertEvenEven(e);
					} else {
						/* Inner (+,+) edge */
						tree.pqInsertEvenEven(e);
					}
				} else if (x.isOdd() && tree != x.tree) {
					/* Cross-trees (+,-) edge */
					ensureTreesEdgeExists(tree, x.tree);
					tree.pqEdgeInsertEvenOdd(x.tree.currentEdge, e);
				}
			}
			assert !w.isProcessed();
			w.setProcessed(true);

			assert !v.isProcessed();
			v.setProcessed(true);
			if (v.isBlossom())
				tree.pqInsertOdd(v);

			/* prioritize augmentation over other operations */
			return augmentIfEdgeFound();
		}

		private static void addTreeChild(Blossom parent, Blossom child) {
			assert parent.isEven();
			assert child.isEven();

			// assert child.firstTreeChild == null;
			// assert child.treeSiblingNext == null;
			// assert child.treeSiblingPrev == null;
			child.firstTreeChild = null;
			child.treeSiblingNext = null;
			child.treeSiblingPrev = null;

			Blossom firstChild = parent.firstTreeChild;
			if (firstChild != null) {
				child.treeSiblingNext = firstChild;
				child.treeSiblingPrev = firstChild.treeSiblingPrev;
				firstChild.treeSiblingPrev = child;
			} else {
				child.treeSiblingPrev = child;
			}
			parent.firstTreeChild = child;
		}

		/**
		 * Shrink an odd alternating path in the forest to a blossom.
		 *
		 * @param  bridge the edge connecting between the two branches of the new blossom
		 * @return        {@code true} if augmentation was performed
		 */
		private boolean shrink(Edge bridge) {
			dbgLog.format("shrink(%s)\n", bridge);
			Blossom root;
			{ /* Find LCA of the edge endpoints */
				Blossom rootAncestor;
				Blossom[] ptr = new Blossom[] { bridge.source, bridge.target };
				for (int side = 0;; side = 1 - side) {
					Blossom b = ptr[side];
					assert b.isOuter() && b.isEven();
					if (b.isMarked()) {
						/* Reached a node we already visited, its the LCA */
						root = b;
						/* Other Ptr went to far up, clean it later */
						rootAncestor = ptr[1 - side];
						break;
					}
					b.setMarked(true);
					if (b.isTreeRoot()) {
						/* This Ptr reached the root, advance other Ptr until they meet */
						rootAncestor = b;
						b = ptr[1 - side];
						while (!b.isMarked()) {
							b.setMarked(true);
							b = b.matchedNode();
							assert b.isOuter() && b.isOdd();
							assert !b.isMarked();
							b.setMarked(true);
							b = b.getTreeParent();
							assert b.isOuter() && b.isEven();
						}
						root = b;
						break;
					}
					/* Sides didn't merge yet, continue up */
					b = b.matchedNode();
					assert !b.isMarked();
					b.setMarked(true);
					assert b.isOuter() && b.isOdd();
					ptr[side] = b = b.getTreeParent();
					assert b.isOuter() && b.isEven();
				}
				/* Cleanup from the LCA root to rootAncestor, which is the side that went too much up */
				if (root != rootAncestor) {
					for (Blossom b = root;;) {
						assert b.isOuter() && b.isEven();
						b = b.matchedNode();
						assert b.isOuter() && b.isOdd();
						assert b.isMarked();
						b.setMarked(false);
						b = b.getTreeParent();
						assert b.isOuter() && b.isEven();
						if (b == rootAncestor) {
							b.setMarked(false);
							break;
						}
						assert b.isMarked();
						b.setMarked(false);
					}
				}
			}

			/* Traverse over all sub-blossom, setOuter(false) and move their children to B */
			Tree tree = root.tree;
			Blossom B = new Blossom();
			for (Blossom b : new Blossom[] { bridge.source, bridge.target }) {
				while (b != root) {
					dbgLog.format("\t%s\n", b);
					assert b.isOuter() && b.isEven();
					removeFromTreeChildrenList(b);
					moveTreeChildren(b, B);
					b.setOuter(false);
					// b.tree = null;

					b = b.matchedNode();
					dbgLog.format("\t%s\n", b);
					assert b.isOuter() && b.isOdd();
					b.setOuter(false);
					// b.tree = null;
					if (b.isBlossom())
						tree.pqRemoveOdd(b);

					b = b.getTreeParent();
				}
			}
			dbgLog.format("\t%s (root)\n", root);
			assert root.isOuter() && root.isEven();
			root.setOuter(false);
			moveTreeChildren(root, B);

			/* Init new blossom B */
			B.setOuter(true);
			B.setEven();
			B.setIsBlossom(true);
			B.setIsTreeRoot(root.isTreeRoot());
			B.setProcessed(true);
			B.tree = tree;
			B.dual = -tree.eps;

			/* Swap 'root' and B in the tree (children, siblings) */
			B.treeSiblingPrev = root.treeSiblingPrev;
			B.treeSiblingNext = root.treeSiblingNext;
			// root.treeSiblingPrev = root.treeSiblingNext = null;
			Blossom bParent = null;
			if (!B.isTreeRoot())
				bParent = root.matchedNode().getTreeParent();
			if (B.treeSiblingPrev.treeSiblingNext != null) {
				/* B is not the first in the children/roots list */
				B.treeSiblingPrev.treeSiblingNext = B;
			} else {
				/* B is the first in the children list */
				bParent.firstTreeChild = B;
			}
			if (B.treeSiblingNext != null) {
				/* B is the last in the children/roots list */
				B.treeSiblingNext.treeSiblingPrev = B;
			} else if (bParent != null) {
				/* B is the last in the children list */
				bParent.firstTreeChild.treeSiblingPrev = B;
			}
			if (B.isTreeRoot()) {
				assert tree.root == root;
				assert root.match == null;
				tree.root = B;
			} else {
				assert tree.root != root;
				assert root.match != null;
				B.match = root.match;
			}

			eAugment = null;
			Edge prevBlossomSibling = bridge;
			for (boolean sideSource : new boolean[] { true, false }) {
				Blossom b = sideSource ? bridge.source : bridge.target;
				if (!sideSource && b == root)
					break; /* reach root only from bridge.source side */

				for (;;) {
					if (b.isEven()) {
						b.dual += tree.eps;
					} else {
						assert b.isOdd();
						b.dual -= tree.eps;
					}
					b.setProcessed(false);

					if (b.isOdd()) {
						/* Update all edges that changed their type (when we change b from odd - to even +) */
						for (Iterator<Edge> eit = b.outEdges().iterator(); eit.hasNext();) {
							Edge e = eit.next();
							Blossom target, oldTarget = e.target;
							for (target = oldTarget; !target.isOuter() && !target.isMarked(); target =
									target.blossomParent);
							if (target != oldTarget)
								e.moveEdgeIn(oldTarget, target);

							if (target.isMarked()) { /* inner blossom edge */
								if (target.isOdd())
									e.slack += tree.eps;

							} else { /* cross blossoms edge */

								/* Remove the edge from the list of b (inner blossom) and add it to B (outer one) */
								eit.remove();
								B.addEdgeOut(e);

								handleCrossBlossomOutEdgeOfOddToEven(e);
							}
						}
						for (Iterator<Edge> eit = b.inEdges().iterator(); eit.hasNext();) {
							Edge e = eit.next();
							Blossom source, oldSource = e.source;
							for (source = oldSource; !source.isOuter() && !source.isMarked(); source =
									source.blossomParent);
							if (source != oldSource)
								e.moveEdgeOut(oldSource, source);

							if (source.isMarked()) { /* inner blossom edge */
								if (source.isOdd())
									e.slack += tree.eps;

							} else { /* cross blossoms edge */

								/* Remove the edge from the list of b (inner blossom) and add it to B (outer one) */
								eit.remove();
								B.addEdgeIn(e);

								handleCrossBlossomInEdgeOfOddToEven(e);
							}
						}
					} /* isOdd */

					Edge nextBlossomSibling = b.isEven() ? b.match : b.treeParentEdge;
					b.blossomParent = B;
					b.blossomGrandparent = B;
					b.match = null;
					b.selfLoops = null;

					if (sideSource) {
						if (b == root)
							break;
						b.blossomSibling = nextBlossomSibling;
						b = nextBlossomSibling.getOtherEndpoint(b);
					} else {
						b.blossomSibling = prevBlossomSibling;
						prevBlossomSibling = nextBlossomSibling;
						b = nextBlossomSibling.getOtherEndpoint(b);
						if (b == root)
							break;
					}
				}
			}
			root.blossomSibling = prevBlossomSibling;
			root.setIsTreeRoot(false);

			/* clear isMarked */
			for (Blossom p = root.blossomSibling();; p = p.blossomSibling()) {
				assert p.isMarked();
				p.setMarked(false);
				p.blossomEps = tree.eps;
				if (p == root)
					break;
			}

			/* Move matched edge to point to B instead of root */
			if (B.isMatched()) {
				if (B.match.source == root) {
					B.match.moveEdgeOut(root, B);
				} else {
					assert B.match.target == root;
					B.match.moveEdgeIn(root, B);
				}
			}
			dbgLog.format("\t%s (new blossom)\n", B);

			/* prioritize augmentation over other operations */
			return augmentIfEdgeFound();
		}

		private static void removeFromTreeChildrenList(Blossom b) {
			assert b.isEven();
			if (b.treeSiblingNext != null) {
				b.treeSiblingNext.treeSiblingPrev = b.treeSiblingPrev;
			} else {
				Blossom next = b.matchedNode().getTreeParent().firstTreeChild;
				next.treeSiblingPrev = b.treeSiblingPrev;
			}
			if (b.treeSiblingPrev.treeSiblingNext != null) {
				/* don't set next of parent->first->prev->{next} */
				b.treeSiblingPrev.treeSiblingNext = b.treeSiblingNext;
			} else {
				Blossom parent = b.matchedNode().getTreeParent();
				parent.firstTreeChild = b.treeSiblingNext;
			}
			b.treeSiblingNext = b.treeSiblingPrev = null;
		}

		private static void moveTreeChildren(Blossom from, Blossom to) {
			if (from.firstTreeChild == null)
				return;
			/* Moves the children of 'from' to be the children of 'to' */
			Blossom fromFirst = from.firstTreeChild;
			if (to.firstTreeChild == null) {
				to.firstTreeChild = fromFirst;
			} else {
				Blossom last = fromFirst.treeSiblingPrev;
				fromFirst.treeSiblingPrev = to.firstTreeChild.treeSiblingPrev;
				to.firstTreeChild.treeSiblingPrev.treeSiblingNext = fromFirst;
				to.firstTreeChild.treeSiblingPrev = last;
			}
		}

		private void handleCrossBlossomOutEdgeOfOddToEven(Edge e) {
			handleCrossBlossomEdgeOfOddToEven(e, true);
		}

		private void handleCrossBlossomInEdgeOfOddToEven(Edge e) {
			handleCrossBlossomEdgeOfOddToEven(e, false);
		}

		private void handleCrossBlossomEdgeOfOddToEven(Edge e, boolean outEdge) {
			/*
			 * When a blossom is changed from odd to even, we traverse its edges one by one, and if the edge is a
			 * cross-blossom edge, namely its endpoints does not lie in the blossoms, we need to add/remove it from/to
			 * the relevant PQs.
			 */

			Tree tree;
			Blossom other;
			if (outEdge) {
				tree = e.source.tree;
				other = e.target;
			} else {
				tree = e.target.tree;
				other = e.source;
			}
			assert tree != null;

			/* If target was even, e was part of pqOddEven - remove it */
			if (other.isEven() && tree != other.tree) {
				/* e was (-,+) edge, remove it from PQ as b is now + (even) */
				other.tree.pqEdgeRemoveEvenOdd(other.tree.currentEdge, e);
				if (e.slack + tree.eps <= other.tree.eps)
					eAugment = e;
			}

			/* Add e to all relevant PQs */
			e.slack += 2 * tree.eps;
			if (other.isOut()) {
				/* e is an (+,out) edge */
				tree.pqInsertEvenOut(e);

			} else if (other.isEven()) {
				if (tree != other.tree) {
					/* e is an (+,+) edge between trees */
					ensureTreesEdgeExists(tree, other.tree);
					other.tree.currentEdge.pqInsertEvenEven(e);
				} else {
					/* e is an (+,+) edge within a tree */
					tree.pqInsertEvenEven(e);
				}
			} else if (tree != other.tree) {
				/* e is an (+,-) edge between trees */
				assert other.isOdd();
				ensureTreesEdgeExists(tree, other.tree);
				tree.pqEdgeInsertEvenOdd(other.tree.currentEdge, e);
			}
		}

		/**
		 * Expand an existing blossom to its sub blossoms.
		 *
		 * @param  B the blossom to expand
		 * @return   {@code true} if augmentation was performed
		 */
		private boolean expand(Blossom B) {
			dbgLog.format("expand(%s)\n", B);
			assert B.isBlossom();
			assert B.isOuter();
			assert B.isOdd();

			Tree tree = B.tree;
			double eps = tree.eps;

			Blossom treeParent = B.getTreeParent(); /* fix edge pointer */
			Blossom successor = B.treeParentEdge.getEndpointOrig(B).getPenultimateBlossom();
			B.treeParentEdge.moveEdge(B, successor);

			Blossom base = B.match.getEndpointOrig(B).getPenultimateBlossom();
			B.match.moveEdge(B, base);

			Consumer<Blossom> processSelfLoops = b -> {
				b.setOuter(true);
				for (Edge e = b.selfLoops, next; e != null; e = next) {
					next = e.nextOutEdge;
					Blossom source = e.source.getPenultimateBlossom();
					Blossom target = e.target.getPenultimateBlossom();
					if (source != target) {
						e.nextOutEdge = null;
						source.addEdgeOut(e);
						target.addEdgeIn(e);
						e.slack -= 2 * source.blossomEps;
					} else {
						e.nextOutEdge = source.selfLoops;
						source.selfLoops = e;
					}
				}
				b.selfLoops = null;
				b.setOuter(false);
			};
			for (Blossom b1 = base.blossomSibling();;) {
				assert B == b1.blossomParent;
				assert B == b1.blossomGrandparent; // maybe not
				expandTemp.add(b1);
				b1.setOut();
				processSelfLoops.accept(b1);
				if (b1 == base)
					break;
				b1.match = b1.blossomSibling;

				Blossom b2 = b1.matchedNode();
				expandTemp.add(b2);
				b2.setOut();
				processSelfLoops.accept(b2);
				b2.match = b1.match;

				b1 = b2.blossomSibling();
			}
			base.match = B.match;

			successor.setOdd();
			successor.tree = tree;
			successor.dual += eps;
			successor.treeParentEdge = B.treeParentEdge;
			if (successor != base) {
				Blossom firstChild = null;
				if (successor.match == successor.blossomSibling) {
					for (Blossom b = successor, prev = null;;) {
						b = b.match.getOtherEndpoint(b);

						final Edge aa = b.blossomSibling;
						b.setEven();
						b.tree = tree;
						b.dual -= eps;
						if (prev == null) {
							firstChild = b;
						} else {
							prev.firstTreeChild = b;
						}
						prev = b;
						b.treeSiblingPrev = b;
						b.treeSiblingNext = null;
						b = aa.getOtherEndpoint(b);
						b.setOdd();
						b.tree = tree;
						b.dual += eps;
						b.treeParentEdge = aa;
						if (b == base) {
							prev.firstTreeChild = base.match.getOtherEndpoint(base);
							break;
						}
					}

				} else {
					Blossom child = base.match.getOtherEndpoint(base);
					for (Blossom b = base;;) {
						b.treeParentEdge = b.blossomSibling;
						b.setOdd();
						b.tree = tree;
						b.dual += eps;

						b = b.treeParentEdge.getOtherEndpoint(b);
						b.setEven();
						b.tree = tree;
						b.dual -= eps;
						b.firstTreeChild = child;
						child = b;
						b.treeSiblingPrev = b;
						b.treeSiblingNext = null;

						b = b.match.getOtherEndpoint(b);
						if (b.isOdd())
							break;
					}
					firstChild = child;
				}

				Blossom b = base.match.getOtherEndpoint(base);
				firstChild.treeSiblingPrev = b.treeSiblingPrev;
				firstChild.treeSiblingNext = b.treeSiblingNext;
				if (b.treeSiblingPrev.treeSiblingNext != null) {
					b.treeSiblingPrev.treeSiblingNext = firstChild;
				} else {
					treeParent.firstTreeChild = firstChild;
				}
				if (b.treeSiblingNext != null) {
					b.treeSiblingNext.treeSiblingPrev = firstChild;
				} else {
					treeParent.firstTreeChild.treeSiblingPrev = firstChild;
				}
				b.treeSiblingPrev = b;
				b.treeSiblingNext = null;
			}

			for (Blossom b = base;;) {
				assert b.isOdd();
				dbgLog.format("\t%s\n", b);
				if (b.isBlossom())
					tree.pqInsertOdd(b);
				for (Edge e : b.outEdges())
					if (!e.target.isEven())
						e.slack -= eps;
				for (Edge e : b.inEdges())
					if (!e.source.isEven())
						e.slack -= eps;
				b.setProcessed(true);

				if (b.treeParentEdge == B.treeParentEdge)
					break;

				b = b.treeParentEdge.getOtherEndpoint(b);
				assert b.isEven();
				dbgLog.format("\t%s\n", b);

				for (Edge e : b.outEdges()) {
					if (e.target.isOut()) {
						e.slack += eps;
						tree.pqInsertEvenOut(e);
					} else if (e.target.isEven() && System.identityHashCode(b) < System.identityHashCode(e.target)) {
						e.slack += 2 * eps;
						tree.pqInsertEvenEven(e);
					}
				}
				for (Edge e : b.inEdges()) {
					if (e.source.isOut()) {
						e.slack += eps;
						tree.pqInsertEvenOut(e);
					} else if (e.source.isEven() && System.identityHashCode(b) < System.identityHashCode(e.source)) {
						e.slack += 2 * eps;
						tree.pqInsertEvenEven(e);
					}
				}
				b.setProcessed(true);
				b = b.match.getOtherEndpoint(b);
			}

			if (B.outEdges != null)
				B.outEdges.prevOutEdge.nextOutEdge = null;
			if (B.inEdges != null)
				B.inEdges.prevInEdge.nextInEdge = null;
			eAugment = null;
			for (Edge e : B.outEdges()) {
				Blossom b = e.sourceOrig.getPenultimateBlossomAndUpdateGrandparentToGrandchild();
				/* its ok to modify it during iteration, e.nextOutEdge already saved in iterator */
				e.nextOutEdge = e.prevOutEdge = null;
				b.addEdgeOut(e);
				Blossom bOther = e.getTargetOuterBlossom();

				if (b.isOdd())
					continue;

				if (bOther.isEven() && bOther.tree != tree)
					bOther.tree.pqEdgeRemoveEvenOdd(bOther.tree.currentEdge, e);

				if (b.isOut()) {
					e.slack += eps;
					if (bOther.isEven())
						bOther.tree.pqInsertEvenOut(e);
				} else {
					e.slack += 2 * eps;
					if (bOther.isOut()) {
						tree.pqInsertEvenOut(e);

					} else if (bOther.isEven()) {
						if (bOther.tree == tree) {
							tree.pqInsertEvenEven(e);
						} else {
							if (e.slack <= bOther.tree.eps + eps)
								eAugment = e;
							ensureTreesEdgeExists(tree, bOther.tree);
							bOther.tree.currentEdge.pqInsertEvenEven(e);
						}
					} else if (tree != bOther.tree) {
						assert bOther.isOdd();
						ensureTreesEdgeExists(tree, bOther.tree);
						tree.pqEdgeInsertEvenOdd(bOther.tree.currentEdge, e);
					}
				}
			}
			for (Edge e : B.inEdges()) {
				Blossom b = e.targetOrig.getPenultimateBlossomAndUpdateGrandparentToGrandchild();
				/* its ok to modify it during iteration, e.nextInEdge already saved in iterator */
				e.nextInEdge = e.prevInEdge = null;
				b.addEdgeIn(e);
				Blossom bOther = e.getSourceOuterBlossom();

				if (b.isOdd())
					continue;

				if (bOther.isEven() && bOther.tree != tree)
					bOther.tree.pqEdgeRemoveEvenOdd(bOther.tree.currentEdge, e);

				if (b.isOut()) {
					e.slack += eps;
					if (bOther.isEven())
						bOther.tree.pqInsertEvenOut(e);
				} else {
					e.slack += 2 * eps;
					if (bOther.isOut()) {
						tree.pqInsertEvenOut(e);

					} else if (bOther.isEven()) {
						if (bOther.tree == tree) {
							tree.pqInsertEvenEven(e);
						} else {
							if (e.slack <= bOther.tree.eps + eps)
								eAugment = e;
							ensureTreesEdgeExists(tree, bOther.tree);
							bOther.tree.currentEdge.pqInsertEvenEven(e);
						}
					} else if (tree != bOther.tree) {
						assert bOther.isOdd();
						ensureTreesEdgeExists(tree, bOther.tree);
						tree.pqEdgeInsertEvenOdd(bOther.tree.currentEdge, e);
					}
				}
			}

			for (Blossom b : expandTemp) {
				b.setOuter(true);
				b.blossomParent = null;
				b.blossomGrandparent = b;
			}
			expandTemp.clear();

			/* prioritize augmentation over other operations */
			return augmentIfEdgeFound();
		}

		private boolean augmentIfEdgeFound() {
			if (eAugment != null) {
				augment(eAugment);
				eAugment = null;
				return true;
			} else {
				return false;
			}
		}

		private void augment(Edge e) {
			dbgLog.format("augment(%s)\n", e);
			Blossom u = e.getSourceOuterBlossom();
			Blossom v = e.getTargetOuterBlossom();
			augmentBranch(u);
			augmentBranch(v);
			u.match = v.match = e;
		}

		private void augmentBranch(Blossom b) {
			dbgLog.format("augmentBranch(%s)\n", b);
			assert b.isEven();
			Tree tree = b.tree;
			double eps = tree.eps;

			/* We want to delete the current tree */
			/* Iterate over the tree (trees) edges and remove the pointers to the tree */
			for (TreesEdge e : tree.outEdgesAndProneRemoved()) {
				Tree t2 = e.target;
				e.source = null; /* mark for deletion */
				t2.currentEdge = e;
			}
			for (TreesEdge e : tree.inEdgesAndProneRemoved()) {
				Tree t2 = e.source;
				e.target = null; /* mark for deletion */
				t2.currentEdge = e;
			}

			for (Blossom even : tree.evenNodesWithoutRoot()) {
				assert even.isOuter();
				assert even.isEven();

				Blossom odd = even.matchedNode();
				assert odd.isOuter();
				assert odd.isOdd();

				if (odd.isProcessed()) {
					/* Remove blossom expand event from PQ */
					if (odd.isBlossom())
						tree.pqRemoveOdd(odd);

					/* Blossom become 'out', add even-out events from other trees to it */
					for (Edge e : odd.outEdges()) {
						Blossom target = e.getTargetOuterBlossom();
						if (target.isEven() && target.isProcessed()) {
							if (target.tree != tree) {
								e.slack += eps;
								// if (e.pqEvenOutRef == null && e.pqEvenEvenRef == null && e.pqEvenOddRef == null)
								// if (e.pqRef == null)
								// target.tree.pqInsertEvenOut(e);
								assert e.pqRef != null;
							}
						} else {
							e.slack += eps;
						}
					}
					for (Edge e : odd.inEdges()) {
						Blossom source = e.getSourceOuterBlossom();
						if (source.isEven() && source.isProcessed()) {
							if (source.tree != tree) {
								e.slack += eps;
								// if (e.pqEvenOutRef == null && e.pqEvenEvenRef == null && e.pqEvenOddRef == null)
								// if (e.pqRef == null)
								// source.tree.pqInsertEvenOut(e);
								assert e.pqRef != null;
							}
						} else {
							e.slack += eps;
						}
					}
				}
			}

			Consumer<Edge> handleEvenPqEdge = edge -> {
				edge.slack -= eps;
				edge.getSourceOuterBlossom(); /* fix source pointer */
				edge.getTargetOuterBlossom(); /* fix target pointer */
			};
			for (TreesEdge e : tree.outEdges()) {
				Tree target = e.target;
				target.currentEdge = null;

				/* All odd nodes of this tree become out, meld into EvenOut of the other tree */
				// for (HeapReference<Edge, Void> ref : e.pqOddEven) {
				// Edge ePq = ref.key();
				// assert ePq.pqEvenOddRef == ref;
				// ePq.pqEvenOddRef = null;
				// assert ePq.pqEvenOutRef == null;
				// ePq.pqEvenOutRef = ref;
				// }
				target.pqEvenOut.meld(e.pqOddEven);

				/* All even nodes of this tree become out, meld into EvenOut of the other tree */
				for (HeapReference<Edge, Void> ref : e.pqEvenEven) {
					handleEvenPqEdge.accept(ref.key());
					// Edge ePq = ref.key();
					// assert ePq.pqEvenEvenRef == ref;
					// ePq.pqEvenEvenRef = null;
					// assert ePq.pqEvenOutRef == null;
					// ePq.pqEvenOutRef = ref;
				}
				target.pqEvenOut.meld(e.pqEvenEven);

				/* All even nodes of this tree become out, clear EvenOdd PQ */
				for (HeapReference<Edge, Void> ref : e.pqEvenOdd) {
					Edge ePq = ref.key();
					// assert ePq.pqEvenOddRef == ref;
					// ePq.pqEvenOddRef = null;
					assert ePq.pqRef == ref;
					ePq.pqRef = null;

					handleEvenPqEdge.accept(ePq);
				}
				e.pqEvenOdd.clear();
			}
			for (TreesEdge e : tree.inEdges()) {
				Tree source = e.source;
				source.currentEdge = null;

				/* All odd nodes of this tree become out, meld into EvenOut of the other tree */
				// for (HeapReference<Edge, Void> ref : e.pqEvenOdd) {
				// Edge ePq = ref.key();
				// assert ePq.pqEvenOddRef == ref;
				// ePq.pqEvenOddRef = null;
				// assert ePq.pqEvenOutRef == null;
				// ePq.pqEvenOutRef = ref;
				// }
				source.pqEvenOut.meld(e.pqEvenOdd);

				/* All even nodes of this tree become out, meld into EvenOut of the other tree */
				for (HeapReference<Edge, Void> ref : e.pqEvenEven) {
					handleEvenPqEdge.accept(ref.key());
					// Edge ePq = ref.key();
					// assert ePq.pqEvenEvenRef == ref;
					// ePq.pqEvenEvenRef = null;
					// assert ePq.pqEvenOutRef == null;
					// ePq.pqEvenOutRef = ref;
				}
				source.pqEvenOut.meld(e.pqEvenEven);

				/* All even nodes of this tree become out, clear OddEven PQ */
				for (HeapReference<Edge, Void> ref : e.pqOddEven) {
					Edge ePq = ref.key();
					// assert ePq.pqEvenOddRef == ref;
					// ePq.pqEvenOddRef = null;
					assert ePq.pqRef == ref;
					ePq.pqRef = null;

					handleEvenPqEdge.accept(ePq);
				}
				e.pqOddEven.clear();
			}

			/* All even nodes of this tree become out, clear EvenOut PQ */
			for (HeapReference<Edge, Void> ref : tree.pqEvenOut) {
				Edge ePq = ref.key();
				// assert ePq.pqEvenOutRef == ref;
				// ePq.pqEvenOutRef = null;
				assert ePq.pqRef == ref;
				ePq.pqRef = null;

				handleEvenPqEdge.accept(ePq);
			}
			tree.pqEvenOut.clear();

			/* All even nodes of this tree become out, clear EvenEven PQ */
			for (HeapReference<Edge, Void> ref : tree.pqEvenEven) {
				Edge ePq = ref.key();
				// assert ePq.pqEvenEvenRef == ref;
				// ePq.pqEvenEvenRef = null;
				assert ePq.pqRef == ref;
				ePq.pqRef = null;

				processEdgeEvenEven(ePq, true);
			}
			tree.pqEvenEven.clear();

			/* Set all tree nodes to be out */
			for (Blossom even : tree.evenNodesWithoutRoot()) {
				Blossom odd = even.matchedNode();
				assert even.isEven();
				assert odd.isOdd();
				even.setOut();
				odd.setOut();
				even.setProcessed(false);
				odd.setProcessed(false);
				even.dual += eps;
				odd.dual -= eps;
			}
			Blossom root = tree.root;
			root.setOut();
			root.setProcessed(false);
			root.dual += eps;

			/* Actually augment, update matched edges along path from b to root */
			if (!b.isTreeRoot()) {
				Blossom odd = b.matchedNode();
				Blossom even = odd.getTreeParent();
				Edge match = odd.match = odd.treeParentEdge;
				while (!even.isTreeRoot()) {
					odd = even.matchedNode();
					even.match = match;
					even = odd.getTreeParent();
					match = odd.match = odd.treeParentEdge;
				}
				even.match = match;
			}

			root.setIsTreeRoot(false);
			root.treeSiblingPrev.treeSiblingNext = root.treeSiblingNext;
			if (root.treeSiblingNext != null)
				root.treeSiblingNext.treeSiblingPrev = root.treeSiblingPrev;

			/* help GC */
			root.treeSiblingNext = root.treeSiblingPrev = null;
			tree.clear();
			treeNum--;
		}

		private IMatching finish() {
			for (Blossom blossom : singletonNodes) {
				if (blossom.isMatched())
					continue;
				Blossom prev = null;
				do {
					blossom.blossomGrandparent = prev;
					prev = blossom;
					blossom = blossom.blossomParent;
				} while (!blossom.isOuter());
				assert blossom.isMatched();

				Blossom prevPrev = prev.blossomGrandparent;
				for (;;) {
					/* find the root penultimate blossom */
					Blossom k;
					if (blossom == blossom.match.source) {
						k = blossom.match.sourceOrig;
					} else if (blossom == blossom.match.target) {
						k = blossom.match.targetOrig;
					} else {
						for (Blossom k1 = blossom.match.sourceOrig, k2 = blossom.match.targetOrig;; k1 =
								k1.blossomParent, k2 = k2.blossomParent) {
							if (k1 == null) {
								k = k2;
								break;
							}
							if (k2 == null) {
								k = k1;
								break;
							}
							if (k1.blossomParent == blossom) {
								k = k1;
								break;
							}
							if (k2.blossomParent == blossom) {
								k = k2;
								break;
							}
						}
					}
					while (k.blossomParent != blossom)
						k = k.blossomParent;

					/* propagate the matched edge of the blossom downwards */
					k.match = blossom.match;

					for (Blossom i = k.blossomSibling(); i != k;) {
						assert i.blossomParent == k.blossomParent;
						i.match = i.blossomSibling;
						Blossom j = i.matchedNode();
						assert j.blossomParent == k.blossomParent;
						j.match = i.match;
						i = j.blossomSibling();
					}

					blossom = prev;
					if (!blossom.isBlossom())
						break;
					prev = prevPrev;
					prevPrev = prev.blossomGrandparent;
				}

				// Blossom node = blossom.matchedNode();
				// node.setProcessed(true);
			}

			final int n = g.vertices().size();
			int[] matched = new int[n];
			for (int u = 0; u < n; u++) {
				assert singletonNodes[u].isMatched();
				matched[u] = singletonNodes[u].match.id;
			}
			return new Matchings.MatchingImpl(g, matched);
		}

		private boolean updateDuals() {
			dbgLog.format("updateDuals()\n");
			for (Blossom root : roots()) {
				Tree tree = root.tree;

				double eps = Double.POSITIVE_INFINITY, epsOther;
				if (tree.pqEvenOut.isNotEmpty() && (epsOther = tree.pqEvenOut.findMin().key().slack) < eps)
					eps = epsOther;
				if (tree.pqOdd.isNotEmpty() && (epsOther = tree.pqOdd.findMin().key().dual) < eps)
					eps = epsOther;
				while (tree.pqEvenEven.isNotEmpty()) {
					Edge minEdge = tree.pqEvenEven.findMin().key();
					if (processEdgeEvenEven(minEdge, false)) {
						epsOther = minEdge.slack / 2;
						if (epsOther < eps)
							eps = epsOther;
						break;
					}
					tree.pqRemoveEvenEven(minEdge);
				}
				tree.epsDelta = eps - tree.eps;
			}

			/*
			 * Although the paper suggest multiple approaches for dual updates, we use a single one, the connected
			 * components approach
			 */
			updateDualsCC();

			double delta = 0;
			for (Blossom root : roots()) {
				Tree tree = root.tree;
				if (tree.epsDelta > 0) {
					delta += tree.epsDelta;
					tree.eps += tree.epsDelta;
				}
			}
			return delta > 1e-12; /* epsilon error */
		}

		private static final Tree BfsProcessed = new Tree(null);

		private void updateDualsCC() {
			for (Blossom root : roots())
				root.tree.bfsNext = null;

			/* Perform a BFS on the trees, starting from different each time, until all trees are visited */
			for (Blossom root : roots()) {
				Tree tree = root.tree;
				if (tree.bfsNext != null) /* isVisited */
					continue;
				double eps = tree.epsDelta;

				/* Perform BFS to find all trees in the connected component */
				Tree bfsBegin, bfsHead, bfsTail;
				bfsBegin = bfsHead = bfsTail = tree;
				bfsTail.bfsNext = bfsTail; /* mark tail */
				for (;;) {
					for (TreesEdge e : bfsHead.outEdges()) {
						Tree currentTree = bfsHead;
						Tree otherTree = e.target;

						double epsEvenEven = e.pqEvenEven.isEmpty() ? Double.POSITIVE_INFINITY
								: e.pqEvenEven.findMin().key().slack - currentTree.eps - otherTree.eps;
						if (otherTree.bfsNext != null && otherTree.bfsNext != BfsProcessed) { /* isInCurrentBfs */
							if (2 * eps > epsEvenEven)
								eps = epsEvenEven / 2;
							continue;
						}

						double epsEvenOdd = e.pqEvenOdd.isEmpty() ? Double.POSITIVE_INFINITY
								: e.pqEvenOdd.findMin().key().slack - currentTree.eps + otherTree.eps;
						double epsOddEven = e.pqOddEven.isEmpty() ? Double.POSITIVE_INFINITY
								: e.pqOddEven.findMin().key().slack - otherTree.eps + currentTree.eps;

						double epsTarget;
						if (otherTree.bfsNext == BfsProcessed) {
							epsTarget = otherTree.epsDelta;
						} else if (epsEvenOdd > 0 && epsOddEven > 0) {
							epsTarget = 0;
						} else {
							/* append target to BFS queue end */
							bfsTail.bfsNext = otherTree;
							bfsTail = otherTree;
							bfsTail.bfsNext = bfsTail; /* mark tail */

							eps = Math.min(eps, epsEvenEven);
							eps = Math.min(eps, otherTree.epsDelta);
							continue;
						}
						eps = Math.min(eps, epsEvenEven - epsTarget);
						eps = Math.min(eps, epsTarget + epsEvenOdd);
					}
					for (TreesEdge e : bfsHead.inEdges()) {
						Tree currentTree = bfsHead;
						Tree otherTree = e.source;

						double epsEvenEven = e.pqEvenEven.isEmpty() ? Double.POSITIVE_INFINITY
								: e.pqEvenEven.findMin().key().slack - currentTree.eps - otherTree.eps;
						if (otherTree.bfsNext != null && otherTree.bfsNext != BfsProcessed) { /* isInCurrentBfs */
							if (2 * eps > epsEvenEven)
								eps = epsEvenEven / 2;
							continue;
						}

						double epsOddEven = e.pqOddEven.isEmpty() ? Double.POSITIVE_INFINITY
								: e.pqOddEven.findMin().key().slack - currentTree.eps + otherTree.eps;
						double epsEvenOdd = e.pqEvenOdd.isEmpty() ? Double.POSITIVE_INFINITY
								: e.pqEvenOdd.findMin().key().slack - otherTree.eps + currentTree.eps;

						double epsTarget;
						if (otherTree.bfsNext == BfsProcessed) {
							epsTarget = otherTree.epsDelta;
						} else if (epsEvenOdd > 0 && epsOddEven > 0) {
							epsTarget = 0;
						} else {
							/* append target to BFS queue end */
							bfsTail.bfsNext = otherTree;
							bfsTail = otherTree;
							bfsTail.bfsNext = bfsTail; /* mark tail */

							eps = Math.min(eps, epsEvenEven);
							eps = Math.min(eps, otherTree.epsDelta);
							continue;
						}
						eps = Math.min(eps, epsEvenEven - epsTarget);
						eps = Math.min(eps, epsTarget + epsOddEven);
					}

					if (bfsHead.bfsNext == bfsHead)
						break; /* end of BFS queue */
					bfsHead = bfsHead.bfsNext;
				}

				/* Update dual for all trees in connected components */
				for (Tree t = bfsBegin, tNext;; t = tNext) {
					t.epsDelta = eps;
					tNext = t.bfsNext;
					t.bfsNext = BfsProcessed; /* mark as processed */
					if (t == tNext)
						break;
				}
			}
		}

		private Iterable<Blossom> roots() {
			return new Iterable<>() {

				@Override
				public Iterator<Blossom> iterator() {
					return new Iterator<>() {
						Blossom root = treeList.treeSiblingNext;

						@Override
						public boolean hasNext() {
							return root != null;
						}

						@Override
						public Blossom next() {
							Assertions.Iters.hasNext(this);
							Blossom ret = root;
							root = root.treeSiblingNext;
							return ret;
						}
					};
				}
			};
		}

		private Iterable<Blossom> rootsDuringAugmentation() {
			return new Iterable<>() {

				@Override
				public Iterator<Blossom> iterator() {
					return new Iterator<>() {
						Blossom next = treeList.treeSiblingNext;
						Blossom nextNext = next == null ? null : next.treeSiblingNext;

						@Override
						public boolean hasNext() {
							if (next != null && next.isTreeRoot())
								return true;
							next = nextNext;
							nextNext = null;
							return next != null;
						}

						@Override
						public Blossom next() {
							Assertions.Iters.hasNext(this);
							Blossom ret = next;
							next = next.treeSiblingNext;
							nextNext = next == null ? null : next.treeSiblingNext;
							return ret;
						}
					};
				}
			};
		}

		private static void ensureTreesEdgeExists(Tree mainTree, Tree otherTree) {
			assert mainTree != otherTree;
			if (otherTree.currentEdge == null) {
				TreesEdge e = new TreesEdge();
				e.source = mainTree;
				e.target = otherTree;
				e.nextOutEdge = mainTree.outEdges;
				mainTree.outEdges = e;
				e.nextInEdge = otherTree.inEdges;
				otherTree.inEdges = e;
				otherTree.currentEdge = e;
			}
		}

		private static boolean processEdgeEvenEven(Edge e, boolean updateBoundaryEdge) {
			Blossom penultimateSource, penultimateTarget;
			Blossom source = e.source, target = e.target;
			if (!source.isOuter()) {
				penultimateSource = source.getPenultimateBlossom();
				source = penultimateSource.blossomParent;
			} else {
				penultimateSource = null;
			}
			if (!target.isOuter()) {
				penultimateTarget = target.getPenultimateBlossom();
				target = penultimateTarget.blossomParent;
			} else {
				penultimateTarget = null;
			}

			if (source != target) {
				if (e.source != source)
					e.moveEdgeOut(e.source, source);
				if (e.target != target)
					e.moveEdgeIn(e.target, target);
				if (updateBoundaryEdge)
					e.slack -= 2 * source.tree.eps;
				return true;
			}
			if (penultimateSource != penultimateTarget) {
				assert penultimateSource != null && penultimateTarget != null;
				if (e.source != penultimateSource)
					e.moveEdgeOut(e.source, penultimateSource);
				if (e.target != penultimateTarget)
					e.moveEdgeIn(e.target, penultimateTarget);
				if (updateBoundaryEdge)
					e.slack -= 2 * penultimateSource.blossomEps;
			} else {
				e.source.removeEdgeOut(e);
				e.target.removeEdgeIn(e);
				e.nextOutEdge = penultimateSource.selfLoops;
				penultimateSource.selfLoops = e;
			}
			return false;
		}

	}

	/**
	 * A blossom is either a vertex of a graph, or odd number of blossoms connected by alternating matched/unmatched
	 * edges.
	 *
	 * <p>
	 * Edges of the blossoms (originated from the original vertices) are stored using two linked lists: out-edges and
	 * in-edges. Whether an edge is out or in is arbitrary, but it helps to manage the undirected edges. When a blossom
	 * is created from a set of other blossoms, their edges are moved to the new super blossom, unless they are fully
	 * contained in the blossom, in which case they are stored in a separated 'self-loops' linked lists. When a blossom
	 * is expanded, its edges are moved back to the sub-blossoms, and self-loops edges are handled separately.
	 *
	 * @author Barak Ugav
	 */
	private static class Blossom {

		private static final byte EvenOddOutMask;
		private static final byte EvenOddOut_Even;
		private static final byte EvenOddOut_Odd;
		private static final byte EvenOddOut_Out;
		private static final byte ProcessedMask;
		private static final byte Processed_False;
		private static final byte Processed_True;
		private static final byte OuterMask;
		private static final byte Outer_False;
		private static final byte Outer_True;
		private static final byte IsBlossomMask;
		private static final byte IsBlossom_False;
		private static final byte IsBlossom_True;
		private static final byte MarkedMask;
		private static final byte Marked_False;
		private static final byte Marked_True;
		private static final byte IsTreeRootMask;
		private static final byte IsTreeRoot_False;
		private static final byte IsTreeRoot_True;

		static {
			BitmapBuilder b = new BitmapBuilder(byte.class);
			BitmapBuilder.Field f;

			f = b.newField(2);
			EvenOddOutMask = (byte) f.mask;
			EvenOddOut_Even = (byte) (0 << f.shift);
			EvenOddOut_Odd = (byte) (1 << f.shift);
			EvenOddOut_Out = (byte) (2 << f.shift);
			f = b.newField(1);
			ProcessedMask = (byte) f.mask;
			Processed_False = (byte) (0 << f.shift);
			Processed_True = (byte) (1 << f.shift);
			f = b.newField(1);
			OuterMask = (byte) f.mask;
			Outer_False = (byte) (0 << f.shift);
			Outer_True = (byte) (1 << f.shift);
			f = b.newField(1);
			IsBlossomMask = (byte) f.mask;
			IsBlossom_False = (byte) (0 << f.shift);
			IsBlossom_True = (byte) (1 << f.shift);
			f = b.newField(1);
			MarkedMask = (byte) f.mask;
			Marked_False = (byte) (0 << f.shift);
			Marked_True = (byte) (1 << f.shift);
			f = b.newField(1);
			IsTreeRootMask = (byte) f.mask;
			IsTreeRoot_False = (byte) (0 << f.shift);
			IsTreeRoot_True = (byte) (1 << f.shift);
		}

		private static class BitmapBuilder {
			private int usedBits;
			private final int maxSize;

			BitmapBuilder(Class<?> intType) {
				if (intType == long.class) {
					maxSize = Long.SIZE;
				} else if (intType == int.class) {
					maxSize = Integer.SIZE;
				} else if (intType == short.class) {
					maxSize = Short.SIZE;
				} else if (intType == byte.class) {
					maxSize = Byte.SIZE;
				} else {
					throw new IllegalArgumentException("unknown int type: " + intType);
				}
			}

			Field newField(int size) {
				assert usedBits + size < maxSize;
				int shift = usedBits;
				int mask = ((1 << size) - 1) << shift;
				usedBits += size;
				return new Field(shift, mask);
			}

			static class Field {
				final int shift;
				final int mask;

				Field(int shift, int mask) {
					this.shift = shift;
					this.mask = mask;

				}
			}
		}

		/* Flags bitmap */
		byte flags;

		/* The tree containing this blossom */
		Tree tree;
		/* current matched edge of the blossom */
		/* may no be updated if this blossom is not outer, will be updated in finish() */
		Edge match;

		/* circular bi-directional linked lists */
		Edge outEdges;
		Edge inEdges;
		/* One directional linked list (using the nextOut/prevOut) of self edges */
		/* a self edge is an edge that both its endpoints are contained in the blossom */
		Edge selfLoops;

		/*
		 * The tree constructed from even and odd blossoms is represented by child-parent connections between even
		 * blossoms only, as each odd blossom is matched to an even blossom it can be deduced. Each even blossom point
		 * to its first even child. The children of each blossom are connected in a bidirectional linked list, with out
		 * exception: {@code firstChild->prev->next == null}, or equivalently {@code last->next == null}.
		 */
		Blossom firstTreeChild;
		Blossom treeSiblingNext;
		Blossom treeSiblingPrev;

		/* The blossom parent containing this blossom, used for non-outer blossoms */
		Blossom blossomParent;
		/* The edge connecting a blossom to its sibling, both contained as children in some other blossom */
		Edge blossomSibling;
		/* Helper pointer used to determine fast the outer blossom of a vertex/blossom */
		Blossom blossomGrandparent;

		/* Edge connecting a odd - blossom to its parent */
		Edge treeParentEdge;

		/* The dual value of this blossom/vertex */
		double dual;
		/* Temporary change in the dual value of this blossom, used dual updates */
		double blossomEps;

		/* Reference to the expand element in the PQ, used for odd non-singleton blossoms */
		HeapReference<Blossom, Void> expandRef;

		/* Comparator that compare the dual value of blossoms, used for expand PQ */
		static final Comparator<Blossom> dualComparator = (b1, b2) -> Double.compare(b1.dual, b2.dual);

		Blossom() {
			flags = EvenOddOut_Out;
		}

		boolean isEven() {
			return (flags & EvenOddOutMask) == EvenOddOut_Even;
		}

		boolean isOdd() {
			return (flags & EvenOddOutMask) == EvenOddOut_Odd;
		}

		boolean isOut() {
			return (flags & EvenOddOutMask) == EvenOddOut_Out;
		}

		void setEven() {
			flags = (byte) ((flags & ~EvenOddOutMask) | EvenOddOut_Even);
		}

		void setOdd() {
			flags = (byte) ((flags & ~EvenOddOutMask) | EvenOddOut_Odd);
		}

		void setOut() {
			flags = (byte) ((flags & ~EvenOddOutMask) | EvenOddOut_Out);
		}

		boolean isOuter() {
			return (flags & OuterMask) == Outer_True;
		}

		void setOuter(boolean isOuter) {
			flags = (byte) ((flags & ~OuterMask) | (isOuter ? Outer_True : Outer_False));
		}

		boolean isBlossom() {
			return (flags & IsBlossomMask) == IsBlossom_True;
		}

		void setIsBlossom(boolean isBlossom) {
			flags = (byte) ((flags & ~IsBlossomMask) | (isBlossom ? IsBlossom_True : IsBlossom_False));
		}

		boolean isProcessed() {
			return (flags & ProcessedMask) == Processed_True;
		}

		void setProcessed(boolean processed) {
			flags = (byte) ((flags & ~ProcessedMask) | (processed ? Processed_True : Processed_False));
		}

		boolean isMarked() {
			return (flags & MarkedMask) == Marked_True;
		}

		void setMarked(boolean marked) {
			flags = (byte) ((flags & ~MarkedMask) | (marked ? Marked_True : Marked_False));
		}

		boolean isTreeRoot() {
			return (flags & IsTreeRootMask) == IsTreeRoot_True;
		}

		void setIsTreeRoot(boolean isTreeRoot) {
			flags = (byte) ((flags & ~IsTreeRootMask) | (isTreeRoot ? IsTreeRoot_True : IsTreeRoot_False));
		}

		boolean isMatched() {
			return match != null;
		}

		Blossom matchedNode() {
			return match.getOtherEndpoint(this);
		}

		Blossom blossomSibling() {
			return blossomSibling.getOtherEndpoint(this);
		}

		Blossom getTreeParent() {
			if (this == treeParentEdge.source) {
				return treeParentEdge.getTargetOuterBlossom();
			} else {
				assert this == treeParentEdge.target;
				return treeParentEdge.getSourceOuterBlossom();
			}
		}

		private Blossom getPenultimateBlossom() {
			Blossom b = this;
			for (;;) {
				if (!b.blossomGrandparent.isOuter()) {
					b = b.blossomGrandparent;
				} else if (b.blossomGrandparent != b.blossomParent) {
					b.blossomGrandparent = b.blossomParent;
				} else {
					break;
				}
			}
			for (Blossom p = this, next; p != b; p = next) {
				next = p.blossomGrandparent;
				p.blossomGrandparent = b;
			}
			return b;
		}

		private Blossom getPenultimateBlossomAndUpdateGrandparentToGrandchild() {
			Blossom b = this;
			Blossom prev = null;
			for (;;) {
				if (!b.blossomGrandparent.isOuter()) {
					b = (prev = b).blossomGrandparent;
				} else if (b.blossomGrandparent != b.blossomParent) {
					b.blossomGrandparent = b.blossomParent;
				} else {
					break;
				}
			}
			if (prev != null) {
				for (Blossom p = this, next; p != prev; p = next) {
					next = p.blossomGrandparent;
					p.blossomGrandparent = prev;
				}
			}
			return b;
		}

		private void addEdgeOut(Edge e) {
			assert e.nextOutEdge == null && e.prevOutEdge == null;
			if (outEdges != null) {
				e.prevOutEdge = outEdges.prevOutEdge;
				e.nextOutEdge = outEdges;
				outEdges.prevOutEdge.nextOutEdge = e;
				outEdges.prevOutEdge = e;
			} else {
				outEdges = e.prevOutEdge = e.nextOutEdge = e;
			}
			e.source = this;
		}

		private void addEdgeIn(Edge e) {
			assert e.nextInEdge == null && e.prevInEdge == null;
			if (inEdges != null) {
				e.prevInEdge = inEdges.prevInEdge;
				e.nextInEdge = inEdges;
				inEdges.prevInEdge.nextInEdge = e;
				inEdges.prevInEdge = e;
			} else {
				inEdges = e.prevInEdge = e.nextInEdge = e;
			}
			e.target = this;
		}

		private void removeEdgeOut(Edge e) {
			assert e.source == this;
			if (e.prevOutEdge == e) {
				assert outEdges == e;
				outEdges = null;
			} else {
				e.prevOutEdge.nextOutEdge = e.nextOutEdge;
				e.nextOutEdge.prevOutEdge = e.prevOutEdge;
				outEdges = e.nextOutEdge;
			}
			e.nextOutEdge = e.prevOutEdge = null;
		}

		private void removeEdgeIn(Edge e) {
			assert e.target == this;
			if (e.prevInEdge == e) {
				assert inEdges == e;
				inEdges = null;
			} else {
				e.prevInEdge.nextInEdge = e.nextInEdge;
				e.nextInEdge.prevInEdge = e.prevInEdge;
				inEdges = e.nextInEdge;
			}
			e.nextInEdge = e.prevInEdge = null;
		}

		Iterable<Edge> outEdges() {
			return new Iterable<>() {

				@Override
				public Iterator<Edge> iterator() {
					return new Iterator<>() {
						Edge current = null;
						Edge nextEdge = outEdges;

						@Override
						public boolean hasNext() {
							return nextEdge != null;
						}

						@Override
						public Edge next() {
							Assertions.Iters.hasNext(this);
							current = nextEdge;
							nextEdge = nextEdge.nextOutEdge;
							/* outEdges is a circular list of edges */
							if (nextEdge == outEdges)
								nextEdge = null;
							return current;
						}

						@Override
						public void remove() {
							if (current == null)
								throw new IllegalStateException();

							if (outEdges != current) {
								Edge prev = current.prevOutEdge, next = current.nextOutEdge;
								assert prev != current && next != current;
								prev.nextOutEdge = next;
								next.prevOutEdge = prev;
							} else {
								outEdges = nextEdge;
								if (nextEdge != null) {
									Edge prev = current.prevOutEdge;
									prev.nextOutEdge = nextEdge;
									nextEdge.prevOutEdge = prev;
								}
							}

							current = current.nextOutEdge = current.prevOutEdge = null;
						}
					};
				}
			};
		}

		Iterable<Edge> inEdges() {
			return new Iterable<>() {

				@Override
				public Iterator<Edge> iterator() {
					return new Iterator<>() {
						Edge current = null;
						Edge nextEdge = inEdges;

						@Override
						public boolean hasNext() {
							return nextEdge != null;
						}

						@Override
						public Edge next() {
							Assertions.Iters.hasNext(this);
							current = nextEdge;
							nextEdge = nextEdge.nextInEdge;
							/* inEdges is a circular list of edges */
							if (nextEdge == inEdges)
								nextEdge = null;
							return current;
						}

						@Override
						public void remove() {
							if (current == null)
								throw new IllegalStateException();

							if (inEdges != current) {
								Edge prev = current.prevInEdge, next = current.nextInEdge;
								assert prev != current && next != current;
								prev.nextInEdge = next;
								next.prevInEdge = prev;
							} else {
								inEdges = nextEdge;
								if (nextEdge != null) {
									Edge prev = current.prevInEdge;
									prev.nextInEdge = nextEdge;
									nextEdge.prevInEdge = prev;
								}
							}

							current = current.nextInEdge = current.prevInEdge = null;
						}
					};
				}
			};
		}

		@Override
		public String toString() {
			String b = isBlossom() ? (isOuter() ? "B" : "b") : (isOuter() ? "V" : "v");
			String id = Debug.blossomId(this);
			String parity = isEven() ? "+" : (isOdd() ? "-" : "x");
			String root = isTreeRoot() ? "r" : "";
			return String.format("%s%s%s%s", root, b, id, parity);
		}
	}

	/**
	 * A edge between two blossoms.
	 *
	 * <p>
	 * During init, an Edge object is created for each edge in the original graph, connecting two singleton blossoms
	 * corresponding to the original vertices endpoints of the edge. When a blossom is created from a set of blossoms,
	 * edges are moved to the new super blossom. When a blossom is expanded, its edges are moved back to the
	 * sub-blossom.
	 *
	 * <p>
	 * Each edge is contained in out-edges linked list of its source and in-edges linked list of its target.
	 *
	 * @author Barak Ugav
	 */
	private static class Edge {
		/* The input graph id of the edge */
		final int id;
		/* The endpoints of the edge */
		/* When a blossom is created from a set of blossoms, edges are moved to the new super blossom */
		/* When a blossom is expanded, its edges are moved back to the sub-blossom */
		Blossom source, target;
		/* The original endpoints of the edge */
		final Blossom sourceOrig, targetOrig;

		/* Each edge is contained in out-edges linked list of source and in-edges linked list of target */
		/* next edge in the out-edges linked list */
		Edge nextOutEdge;
		/* prev edge in the out-edges linked list */
		Edge prevOutEdge;
		/* next edge in the in-edges linked list */
		Edge nextInEdge;
		/* prev edge in the in-edges linked list */
		Edge prevInEdge;

		/* The slack of the edge. An edge is called tight if its slack is zero */
		/* The slack is defined as 2*w(e)-dual(source)-dual(target) */
		/* The alternating tree is composed of tight edges */
		double slack;

		// HeapReference<Edge, Void> pqEvenEvenRef;
		// HeapReference<Edge, Void> pqEvenOddRef;
		// HeapReference<Edge, Void> pqEvenOutRef;
		/* Reference to either to the PQ containing this edge */
		HeapReference<Edge, Void> pqRef;

		static final Comparator<Edge> slackComparator = (e1, e2) -> Double.compare(e1.slack, e2.slack);

		Edge(int id, Blossom source, Blossom target) {
			assert !source.isBlossom();
			assert !target.isBlossom();
			this.id = id;
			this.source = sourceOrig = Objects.requireNonNull(source);
			this.target = targetOrig = Objects.requireNonNull(target);
		}

		Blossom getOtherEndpoint(Blossom b) {
			if (b == source) {
				return target;
			} else {
				assert b == target;
				return source;
			}
		}

		Blossom getEndpointOrig(Blossom b) {
			if (b == source) {
				return sourceOrig;
			} else {
				assert b == target;
				return targetOrig;
			}
		}

		private void moveEdge(Blossom oldNode, Blossom newNode) {
			if (oldNode == source) {
				moveEdgeOut(oldNode, newNode);
			} else {
				assert oldNode == target;
				moveEdgeIn(oldNode, newNode);
			}
		}

		private void moveEdgeOut(Blossom oldSource, Blossom newSource) {
			oldSource.removeEdgeOut(this);
			newSource.addEdgeOut(this);
		}

		private void moveEdgeIn(Blossom oldTarget, Blossom newTarget) {
			oldTarget.removeEdgeIn(this);
			newTarget.addEdgeIn(this);
		}

		private Blossom getSourceOuterBlossom() {
			Blossom b = source;
			if (!b.isOuter()) {
				Blossom bOrig = b;
				b = b.getPenultimateBlossom().blossomParent;
				moveEdgeOut(bOrig, b);
			}
			return b;
		}

		private Blossom getTargetOuterBlossom() {
			Blossom b = target;
			if (!b.isOuter()) {
				Blossom bOrig = b;
				b = b.getPenultimateBlossom().blossomParent;
				moveEdgeIn(bOrig, b);
			}
			return b;
		}

		@SuppressWarnings("boxing")
		@Override
		public String toString() {
			return String.format("(%s, %s, %s)", source, target, slack);
		}
	}

	private static class Tree {
		/* The unmatched root blossom of the tree */
		Blossom root;

		/* PQ containing all (+,+) edges between two even blossoms in this tree, by their slack */
		final HeapReferenceable<Edge, Void> pqEvenEven = newHeap(Edge.slackComparator);
		/* PQ containing all (+,-) edges between even blossom in this tree to out blossoms, by their slack */
		final HeapReferenceable<Edge, Void> pqEvenOut = newHeap(Edge.slackComparator);
		/* PQ containing all odd non-singleton blossoms of this tree, by their expand dual value */
		final HeapReferenceable<Blossom, Void> pqOdd = newHeap(Blossom.dualComparator);

		/*
		 * Dual value implicitly added to all even blossoms and removed from all odd blossoms. We want to change the
		 * dual values of the tree blossoms while maintaining the tightness of the edges, so we increase it to all even
		 * blossoms and decrease it to all odd blossoms. Updating this explicitly is too expensive, so we do so
		 * implicitly by storing the eps for each tree.
		 */
		double eps;

		/* Linked lists of out and in trees-edges */
		TreesEdge outEdges, inEdges;
		TreesEdge currentEdge;

		/* change of eps during dual updates */
		double epsDelta;
		/* used during dual updates */
		Tree bfsNext;

		Tree(Blossom root) {
			this.root = root;
		}

		void clear() {
			root = null;
			currentEdge = null;
			outEdges = inEdges = null;
			bfsNext = null;
		}

		Iterable<TreesEdge> outEdges() {
			return new Iterable<>() {

				@Override
				public Iterator<TreesEdge> iterator() {
					return new Iterator<>() {
						TreesEdge nextEdge = outEdges;

						@Override
						public boolean hasNext() {
							return nextEdge != null;
						}

						@Override
						public TreesEdge next() {
							Assertions.Iters.hasNext(this);
							TreesEdge ret = nextEdge;
							nextEdge = nextEdge.nextOutEdge;
							return ret;
						}
					};
				}
			};
		}

		Iterable<TreesEdge> inEdges() {
			return new Iterable<>() {

				@Override
				public Iterator<TreesEdge> iterator() {
					return new Iterator<>() {
						TreesEdge nextEdge = inEdges;

						@Override
						public boolean hasNext() {
							return nextEdge != null;
						}

						@Override
						public TreesEdge next() {
							Assertions.Iters.hasNext(this);
							TreesEdge ret = nextEdge;
							nextEdge = nextEdge.nextInEdge;
							return ret;
						}
					};
				}
			};
		}

		Iterable<TreesEdge> outEdgesAndProneRemoved() {
			return new Iterable<>() {

				@Override
				public Iterator<TreesEdge> iterator() {
					return new Iterator<>() {
						TreesEdge nextEdge = outEdges;
						TreesEdge prev = null;

						{
							advance();
						}

						private void advance() {
							while (nextEdge != null && nextEdge.target == null) {
								/* marked for removal */
								nextEdge = nextEdge.nextOutEdge;
								if (prev == null) {
									outEdges = nextEdge;
								} else {
									prev.nextOutEdge = nextEdge;
								}
							}
						}

						@Override
						public boolean hasNext() {
							return nextEdge != null;
						}

						@Override
						public TreesEdge next() {
							Assertions.Iters.hasNext(this);
							prev = nextEdge;
							nextEdge = nextEdge.nextOutEdge;
							advance();

							assert prev.source != null;
							assert prev.target != null;
							return prev;
						}
					};
				}
			};
		}

		Iterable<TreesEdge> inEdgesAndProneRemoved() {
			return new Iterable<>() {

				@Override
				public Iterator<TreesEdge> iterator() {
					return new Iterator<>() {
						TreesEdge nextEdge = inEdges;
						TreesEdge prev = null;

						{
							advance();
						}

						private void advance() {
							while (nextEdge != null && nextEdge.source == null) {
								/* marked for removal */
								nextEdge = nextEdge.nextInEdge;
								if (prev == null) {
									inEdges = nextEdge;
								} else {
									prev.nextInEdge = nextEdge;
								}
							}
						}

						@Override
						public boolean hasNext() {
							return nextEdge != null;
						}

						@Override
						public TreesEdge next() {
							Assertions.Iters.hasNext(this);
							prev = nextEdge;
							nextEdge = nextEdge.nextInEdge;
							advance();

							assert prev.source != null;
							assert prev.target != null;
							return prev;
						}
					};
				}
			};
		}

		Iterable<Blossom> evenNodesWithoutRoot() {
			return new Iterable<>() {

				@Override
				public Iterator<Blossom> iterator() {
					return new Iterator<>() {

						Blossom b = root.firstTreeChild; /* skip root */

						@Override
						public boolean hasNext() {
							return b != null;
						}

						@Override
						public Blossom next() {
							Assertions.Iters.hasNext(this);
							Blossom ret = b;

							/* firstTreeChild points only to even children two levels down, skipping odd nodes */
							if (b.firstTreeChild != null) {
								/* Go down */
								b = b.firstTreeChild;
							} else {
								for (;;) {
									if (b.isTreeRoot()) {
										/* Done */
										assert b.isEven();
										b = null;
										break;
									}
									if (b.treeSiblingNext != null) {
										/* Go side */
										b = b.treeSiblingNext;
										break;
									}
									/* To up (2) */
									// assert b.isEven(); // this way fail when we deconstruct a tree
									b = b.matchedNode();
									// assert b.isOdd(); // this way fail when we deconstruct a tree
									b = b.getTreeParent();
									// assert b.isEven(); // this way fail when we deconstruct a tree
								}
							}

							assert ret.isEven();
							assert ret.isOuter();
							assert ret.tree == Tree.this;
							return ret;
						}
					};
				}
			};
		}

		private void pqInsertEvenOut(Edge edge) {
			// assert edge.pqEvenEvenRef == null;
			// assert edge.pqEvenOddRef == null;
			// assert edge.pqEvenOutRef == null;
			// edge.pqEvenOutRef = pqEvenOut.insert(edge);
			assert edge.pqRef == null;
			edge.pqRef = pqEvenOut.insert(edge);
		}

		private void pqRemoveEvenOut(Edge edge) {
			// assert edge.pqEvenOutRef != null;
			// pqEvenOut.remove(edge.pqEvenOutRef);
			// edge.pqEvenOutRef = null;
			// assert edge.pqEvenEvenRef == null;
			// assert edge.pqEvenOddRef == null;
			// assert edge.pqEvenOutRef == null;
			assert edge.pqRef != null;
			pqEvenOut.remove(edge.pqRef);
			edge.pqRef = null;
		}

		private void pqInsertEvenEven(Edge edge) {
			// assert edge.pqEvenEvenRef == null;
			// assert edge.pqEvenOddRef == null;
			// assert edge.pqEvenOutRef == null;
			// edge.pqEvenEvenRef = pqEvenEven.insert(edge);
			assert edge.pqRef == null;
			edge.pqRef = pqEvenEven.insert(edge);
		}

		private void pqRemoveEvenEven(Edge edge) {
			// assert edge.pqEvenEvenRef != null;
			// pqEvenEven.remove(edge.pqEvenEvenRef);
			// edge.pqEvenEvenRef = null;
			// assert edge.pqEvenEvenRef == null;
			// assert edge.pqEvenOddRef == null;
			// assert edge.pqEvenOutRef == null;
			assert edge.pqRef != null;
			pqEvenEven.remove(edge.pqRef);
			edge.pqRef = null;
		}

		private void pqEdgeInsertEvenOdd(TreesEdge treesEdge, Edge edge) {
			HeapReferenceable<Edge, Void> pqEvenOdd;
			if (this == treesEdge.source) {
				pqEvenOdd = treesEdge.pqEvenOdd;
			} else {
				assert this == treesEdge.target;
				pqEvenOdd = treesEdge.pqOddEven;
			}

			// assert edge.pqEvenEvenRef == null;
			// assert edge.pqEvenOddRef == null;
			// assert edge.pqEvenOutRef == null;
			// edge.pqEvenOddRef = pqEvenOdd.insert(edge);
			assert edge.pqRef == null;
			edge.pqRef = pqEvenOdd.insert(edge);
		}

		private void pqEdgeRemoveEvenOdd(TreesEdge treesEdge, Edge edge) {
			HeapReferenceable<Edge, Void> pqEvenOdd;
			if (this == treesEdge.source) {
				pqEvenOdd = treesEdge.pqEvenOdd;
			} else {
				assert this == treesEdge.target;
				pqEvenOdd = treesEdge.pqOddEven;
			}
			// assert edge.pqEvenOddRef != null;
			// pqEvenOdd.remove(edge.pqEvenOddRef);
			// edge.pqEvenOddRef = null;
			// assert edge.pqEvenEvenRef == null;
			// assert edge.pqEvenOddRef == null;
			// assert edge.pqEvenOutRef == null;
			assert edge.pqRef != null;
			pqEvenOdd.remove(edge.pqRef);
			edge.pqRef = null;
		}

		private void pqInsertOdd(Blossom b) {
			assert b.expandRef == null;
			b.expandRef = pqOdd.insert(b);
		}

		private void pqRemoveOdd(Blossom b) {
			assert b.expandRef != null;
			pqOdd.remove(b.expandRef);
			b.expandRef = null;
		}

		@Override
		public String toString() {
			return String.format("T[%s]", root);
		}
	}

	/**
	 * An edge between two trees.
	 *
	 * <p>
	 * The edge does not represent a real edge of the original graph, rather it is used to store information of all the
	 * edges crossing between two edges. In particular, it contain three heaps with the edges crossing between the two
	 * trees, of types (+,+), (+,-) and (-,+).
	 *
	 * @author Barak Ugav
	 */
	private static class TreesEdge {
		/* The trees endpoints of this edge */
		Tree source, target;
		/* Each edge is contained in out-edges linked list of source and in-edges linked list of target */
		/* these lists are one directional */
		TreesEdge nextOutEdge, nextInEdge;

		/* PQ containing all (+,+) edges between the two trees */
		final HeapReferenceable<Edge, Void> pqEvenEven = newHeap(Edge.slackComparator);
		/* PQ containing all (+,-) edges between an even blossom in source tree and odd blossom in target tree */
		final HeapReferenceable<Edge, Void> pqEvenOdd = newHeap(Edge.slackComparator);
		/* PQ containing all (+,-) edges between an odd blossom in source tree and even blossom in target tree */
		final HeapReferenceable<Edge, Void> pqOddEven = newHeap(Edge.slackComparator);

		private void pqInsertEvenEven(Edge edge) {
			assert edge.source != edge.target;
			// assert edge.pqEvenEvenRef == null;
			// assert edge.pqEvenOddRef == null;
			// assert edge.pqEvenOutRef == null;
			// edge.pqEvenEvenRef = pqEvenEven.insert(edge);
			assert edge.pqRef == null;
			edge.pqRef = pqEvenEven.insert(edge);
		}

		@Override
		public String toString() {
			return String.format("(%s, %s)", source, target);
		}
	}

	private static <K> HeapReferenceable<K, Void> newHeap(Comparator<? super K> cmp) {
		return HeapReferenceable.newBuilder().<K>keysTypeObj().valuesTypeVoid().build(cmp);
	}

	private static class Debug {
		private static final boolean Enable = false;
		private static final Impl Impl = Enable ? new Impl() : null;

		private static class Impl {
			private int nextBlossomId;
			private final Reference2IntMap<Blossom> blossomIds = new Reference2IntOpenHashMap<>();
		}

		static String blossomId(Blossom b) {
			return Enable ? String.valueOf(Impl.blossomIds.computeIfAbsent(b, k -> Impl.nextBlossomId++)) : "";
		}

		static void init(MatchingWeightedBlossomV.Worker worker) {
			if (!Enable)
				return;
			assert Impl.blossomIds.isEmpty();
			for (int i = 0; i < worker.singletonNodes.length; i++)
				Impl.blossomIds.put(worker.singletonNodes[i], i);
		}

		static void reset() {
			if (!Enable)
				return;
			Impl.nextBlossomId = 0;
			Impl.blossomIds.clear();
		}

		static void assertConstraints(MatchingWeightedBlossomV.Worker worker) {
			if (!Enable)
				return;

			/* assert heaps constraints */
			for (Blossom root : worker.roots()) {
				Tree tree = root.tree;
				Heaps.assertHeapConstraints(tree.pqEvenOut);
				Heaps.assertHeapConstraints(tree.pqEvenEven);
				Heaps.assertHeapConstraints(tree.pqOdd);
				for (TreesEdge treesEdge : tree.outEdges()) {
					Heaps.assertHeapConstraints(treesEdge.pqEvenEven);
					Heaps.assertHeapConstraints(treesEdge.pqEvenOdd);
					Heaps.assertHeapConstraints(treesEdge.pqOddEven);
				}
			}
		}
	}

	private static final DebugPrinter dbgLog = new DebugPrinter(Debug.Enable);

}
