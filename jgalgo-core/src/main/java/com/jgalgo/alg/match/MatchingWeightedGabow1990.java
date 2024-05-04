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

package com.jgalgo.alg.match;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.SubtreeMergeFindMin;
import com.jgalgo.internal.util.DebugPrinter;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Edmonds' Blossom algorithm for Maximum weighted matching with Gabow's dynamic LCA data structure.
 *
 * <p>
 * This algorithm runs in \(O(m n + n^2 \log n)\) time and uses linear space.
 *
 * <p>
 * Based on the original paper 'Paths, Trees, and Flowers' by Jack Edmonds (1965), later improved by 'An Efficient
 * Implementation of Edmonds Algorithm for Maximum Matching on Graphs' by Harold N. Gabow (1976), and using the
 * efficient dynamic LCA from 'Data Structures for Weighted Matching and Nearest Common Ancestors with Linking' by
 * Harold N. Gabow (1990) resulting in the final running time.
 *
 * @author Barak Ugav
 */
public class MatchingWeightedGabow1990 extends MatchingWeightedGabow1990Abstract {

	/**
	 * Create a new maximum weighted matching object.
	 */
	public MatchingWeightedGabow1990() {}

	@Override
	Worker newWorker(IndexGraph gOrig, IWeightFunction w, DebugPrinter debugPrint) {
		return new Worker(gOrig, w, debugPrint);
	}

	private static class Worker extends MatchingWeightedGabow1990Abstract.Worker {

		/* Heap storing all the blossom and augmenting events */
		final SubtreeMergeFindMin<EdgeEvent> smf;

		/* Dummy SMF node, use as root of roots (SMF support only one tree) */
		SubtreeMergeFindMin.Node smfRootOfRoots;

		/* SMF index of each vertex: vertex -> SMF identifier */
		final SubtreeMergeFindMin.Node[] vToSMFId;

		/*
		 * array used to calculate the path from a vertex to blossom base, used to calculate SMF skeleton from odd
		 * blossoms
		 */
		final int[] oddBlossomPath;

		Worker(IndexGraph gOrig, IWeightFunction w, DebugPrinter debugPrint) {
			super(gOrig, w, debugPrint);
			int n = gOrig.vertices().size();
			vToSMFId = new SubtreeMergeFindMin.Node[n];
			oddBlossomPath = new int[n];
			smf = SubtreeMergeFindMin.newInstance((e1, e2) -> Double.compare(e1.slack, e2.slack));
		}

		@Override
		void initDataStructuresSearchBegin() {
			super.initDataStructuresSearchBegin();
			Arrays.fill(vToSMFId, null);
			smfRootOfRoots = smf.initTree();
		}

		@Override
		void initUnmatchedEvenBlossom(Blossom b) {
			super.initUnmatchedEvenBlossom(b);
			/* Update SMF data structure */
			SubtreeMergeFindMin.Node baseSMFNode = smfAddLeaf(b.base, smfRootOfRoots);
			for (IntIterator uit = b.vertices(); uit.hasNext();) {
				int u = uit.nextInt();
				if (u != b.base) {
					SubtreeMergeFindMin.Node smfNode = smfAddLeaf(u, baseSMFNode);
					smf.mergeSubTrees(baseSMFNode, smfNode);
				}
			}
		}

		@Override
		double computeNextDelta3() {
			return !smf.hasNonTreeEdge() ? Double.MAX_VALUE : smf.findMinNonTreeEdge().edgeData().slack / 2;
		}

		@Override
		EdgeEvent extractNextBlossomEvent() {
			return smf.findMinNonTreeEdge().edgeData();
		}

		@Override
		void searchEnd() {
			super.searchEnd();
			smf.clear();
		}

		@Override
		void growStep() {
			debug
					.print("growStep (root=",
							Integer.valueOf(evens.findBlossom(g.edgeSource(growEvents.findMin().key().e)).root), "): ",
							Integer.valueOf(growEvents.findMin().key().e));

			// Grow step
			assert delta == growEventsKey(growEvents.findMin().key());
			int e = growEvents.extractMin().key().e;
			int u = g.edgeSource(e), v = g.edgeTarget(e);

			Blossom U = evens.findBlossom(u), V = odds.findBlossom(v);
			assert !V.isEven && !isInTree(V);

			// Add odd vertex
			V.root = U.root;
			V.treeParentEdge = edgeVal.get(e).twin;
			V.isEven = false;
			V.delta1 = delta;
			assert V.growRef.key().e == e;
			V.growRef = null;
			if (!V.isSingleton())
				V.expandRef = expandEvents.insert(V.z0 / 2 + V.delta1, V);
			debug.print(" ", V);

			int pathLen = computePath(V, v, oddBlossomPath);
			assert pathLen > 0;
			assert oddBlossomPath[0] == v;
			assert vToSMFId[u] != null;
			assert vToSMFId[v] == null;
			SubtreeMergeFindMin.Node smfParent = smfAddLeaf(v, vToSMFId[u]);
			for (int i : range(1, pathLen)) {
				assert vToSMFId[oddBlossomPath[i]] == null;
				smfParent = smfAddLeaf(oddBlossomPath[i], smfParent);
			}
			assert oddBlossomPath[pathLen - 1] == V.base;

			// Immediately add it's matched edge and vertex as even vertex
			int matchedEdge = matched[V.base];
			V = topBlossom(g.edgeTarget(matchedEdge));
			V.root = U.root;
			V.treeParentEdge = edgeVal.get(matchedEdge).twin;
			if (V.growRef != null) {
				growEvents.remove(V.growRef);
				V.growRef = null;
			}
			assert vToSMFId[V.base] == null;
			smfAddLeaf(V.base, smfParent);
			makeEven(V);
			debug.println(" ", V);
		}

		@Override
		void blossomStep(int e) {
			debug.println("blossomStep");
			int eu = g.edgeSource(e), ev = g.edgeTarget(e);
			assert isEven(eu) && isEven(ev);
			Blossom U = evens.findBlossom(eu), V = evens.findBlossom(ev);
			if (U == V)
				return; // Edge in same blossom, ignore

			// Create new blossom
			Blossom base = lcaInSearchTree(U, V);
			Blossom newb = new Blossom(base.base);
			newb.root = base.root;
			newb.treeParentEdge = base.treeParentEdge;
			newb.isEven = true;
			newb.child = base;
			newb.delta0 = delta;

			// Add all sub blossoms
			unionQueue.clear();
			scanQueue.clear();
			Blossom[] bs = new Blossom[] { U, V };
			for (Blossom b : bs) {
				boolean prevIsRight = b == U;
				Blossom prev = b == U ? V : U;
				int toPrevEdge = b == U ? e : edgeVal.get(e).twin;

				for (;;) {
					// handle even sub blossom
					assert b.isEven;
					if (!b.isSingleton())
						b.z0 = dualVal(b);
					b.parent = newb;
					if (b != base)
						smf.mergeSubTrees(vToSMFId[g.edgeTarget(b.treeParentEdge)], vToSMFId[b.base]);
					connectSubBlossoms(b, prev, toPrevEdge, !prevIsRight);
					unionQueue.enqueue(b.base);

					if (b == base)
						break;
					prev = b;
					toPrevEdge = edgeVal.get(matched[b.base]).twin;
					assert matched[b.base] == b.treeParentEdge;
					b = topBlossom(g.edgeSource(toPrevEdge));

					// handle odd vertex
					assert !b.isEven;
					b.deltaOdd += delta - b.delta1;
					if (!b.isSingleton())
						b.z0 = dualVal(b);
					b.parent = newb;
					connectSubBlossoms(b, prev, toPrevEdge, !prevIsRight);
					SubtreeMergeFindMin.Node smfTopNode = vToSMFId[g.edgeSource(b.treeParentEdge)];
					smf.mergeSubTrees(vToSMFId[g.edgeTarget(b.treeParentEdge)], smfTopNode);
					for (IntIterator vit = b.vertices(); vit.hasNext();) {
						int v = vit.nextInt();
						blossoms[v].isEven = true;

						SubtreeMergeFindMin.Node smfId = vToSMFId[v];
						if (smfId == null) {
							smfId = smfAddLeaf(v, smfTopNode);
							smf.mergeSubTrees(smfId, smfTopNode);
						} else {
							while (!smf.isSameSubTree(smfId, smfTopNode)) {
								SubtreeMergeFindMin.Node p = smfParent(smfId);
								smf.mergeSubTrees(smfId, p);
								smfId = p;
							}
						}
						unionQueue.enqueue(v);
						scanQueue.enqueue(v);
					}
					b.delta0 = delta;
					if (!b.isSingleton()) {
						expandEvents.remove(b.expandRef);
						b.expandRef = null;
					}

					prev = b;
					toPrevEdge = edgeVal.get(b.treeParentEdge).twin;
					b = topBlossom(g.edgeSource(toPrevEdge));
				}
			}

			// Union all sub blossom in find0 data structure
			while (!unionQueue.isEmpty())
				evens.union(newb.base, unionQueue.dequeueInt());
			evens.setBlossom(newb.base, newb);

			// Scan new edges from all new even vertices
			while (!scanQueue.isEmpty()) {
				int u = scanQueue.dequeueInt();
				insertGrowEventsFromVertex(u);
				insertBlossomEventsFromVertex(u);
			}
		}

		@Override
		void makeEven(Blossom V) {
			V.isEven = true;
			V.delta0 = delta;
			int base = V.base;
			for (IntIterator vit = V.vertices(); vit.hasNext();) {
				int v = vit.nextInt();
				blossoms[v].isEven = true;
				evens.union(base, v);
			}
			evens.setBlossom(base, V);

			/*
			 * If a SMF structure already exists in the blossom, we can't just merge all the vertices with the base, as
			 * not all of them will be adjacent sub trees. Therefore, we first merge the base to all it's SMF ancestors
			 * in the blossom, and than merging all vertices up to the base sub tree.
			 */
			final SubtreeMergeFindMin.Node smfBaseNode = vToSMFId[base];
			assert smfBaseNode != null;
			for (SubtreeMergeFindMin.Node smfId = smfBaseNode;;) {
				SubtreeMergeFindMin.Node parentSmf = smfParent(smfId);
				if (parentSmf == null || topBlossom(parentSmf.getNodeData()) != V)
					break;
				smf.mergeSubTrees(smfBaseNode, parentSmf);
				smfId = parentSmf;
			}

			for (IntIterator vit = V.vertices(); vit.hasNext();) {
				int v = vit.nextInt();
				SubtreeMergeFindMin.Node smfNode = vToSMFId[v];
				if (smfNode == null) {
					smfNode = smfAddLeaf(v, smfBaseNode);
					smf.mergeSubTrees(smfBaseNode, smfNode);
				} else {
					while (!smf.isSameSubTree(smfNode, smfBaseNode)) {
						SubtreeMergeFindMin.Node p = smfParent(smfNode);
						smf.mergeSubTrees(smfNode, p);
						smfNode = p;
					}
				}
			}

			for (IntIterator vit = V.vertices(); vit.hasNext();) {
				int v = vit.nextInt();
				insertGrowEventsFromVertex(v);
				insertBlossomEventsFromVertex(v);
			}
		}

		/* compute the path from vertex to base */
		private int computePath(Blossom B, int u, int[] path) {
			return computePath(B, u, path, 0, false);
		}

		private int computePath(Blossom B, int u, int[] path, int pathSize, boolean reverse) {
			if (!reverse)
				path[pathSize++] = u;

			if (B.base != u) {
				int m = matched[u];
				EdgeVal mData = edgeVal.get(m);
				// int v;
				Blossom b0, b1 /* , b2 */;
				if (g.edgeSource(m) == u) {
					// v = m.target();
					b0 = mData.b0;
					b1 = mData.b1;
				} else {
					// v = m.source();
					b0 = mData.b1;
					b1 = mData.b0;
				}

				int xy;
				if (b0.right == b1) {
					// b2 = b1.right;
					xy = b1.toRightEdge;
				} else {
					// b2 = b1.left;
					xy = b1.toLeftEdge;
				}

				pathSize = computePath(b1, g.edgeSource(xy), path, pathSize, !reverse);
				pathSize = computePath(B, g.edgeTarget(xy), path, pathSize, reverse);
			}

			if (reverse)
				path[pathSize++] = u;
			return pathSize;
		}

		@Override
		void addBlossomEvent(int e, double slackBar) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			smf.addNonTreeEdge(vToSMFId[u], vToSMFId[v], new EdgeEvent(e, slackBar));
		}

		private SubtreeMergeFindMin.Node smfAddLeaf(int v, SubtreeMergeFindMin.Node parentSmfNode) {
			SubtreeMergeFindMin.Node smfNode = smf.addLeaf(parentSmfNode);
			smfNode.setNodeData(blossoms[v]);
			assert vToSMFId[v] == null;
			return vToSMFId[v] = smfNode;
		}

		private SubtreeMergeFindMin.Node smfParent(SubtreeMergeFindMin.Node smfNode) {
			SubtreeMergeFindMin.Node p = smfNode.getParent();
			return p != smfRootOfRoots ? p : null;
		}

	}

}
