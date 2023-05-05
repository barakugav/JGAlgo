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

package com.jgalgo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.jgalgo.MaximumMatchingWeightedBlossoms.Evens;
import com.jgalgo.Utils.NullList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Edmonds' Blossom algorithm for Maximum weighted matching with Gabow's dynamic LCA data structure.
 * <p>
 * This algorithm runs in \(O(m n + n^2 \log n)\) time and uses linear space.
 * <p>
 * Based on the original paper 'Paths, Trees, and Flowers' by Jack Edmonds (1965), later improved by 'An Efficient
 * Implementation of Edmonds Algorithm for Maximum Matching on Graphs' by Harold N. Gabow (1976), and using the
 * efficient dynamic LCA from 'Data Structures for Weighted Matching and Nearest Common Ancestors with Linking' by
 * Harold N. Gabow (1990) resulting in the final running time.
 *
 * @author Barak Ugav
 */
public class MaximumMatchingWeightedGabow1990 implements MaximumMatchingWeighted {

	private final DebugPrintsManager debugPrintManager = new DebugPrintsManager();
	private HeapReferenceable.Builder heapBuilder = HeapPairing::new;

	private static final double EPS = 0.00001;

	/**
	 * Create a new maximum weighted matching object.
	 */
	public MaximumMatchingWeightedGabow1990() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	public void setHeapBuilder(HeapReferenceable.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	@Override
	public IntCollection computeMaximumMatching(Graph g, EdgeWeightFunc w) {
		ArgumentCheck.onlyUndirected(g);
		return new Worker(g, w, heapBuilder, debugPrintManager).computeMaxMatching(false);

	}

	@Override
	public IntCollection computeMaximumPerfectMatching(Graph g, EdgeWeightFunc w) {
		ArgumentCheck.onlyUndirected(g);
		return new Worker(g, w, heapBuilder, debugPrintManager).computeMaxMatching(true);
	}

	private static class Worker {

		/* the graph */
		final Graph g;

		final Weights<EdgeVal> edgeVal;

		/* the weight function */
		final EdgeWeightFunc w;

		/* vertex -> matched edge */
		final int[] matched;

		/* vertex -> trivial blossom */
		final Blossom[] blossoms;

		/*
		 * Union find data structure for even blossoms
		 */
		final Evens<Blossom> evens;

		/*
		 * Split find data structure for odd and out blossoms
		 */
		final Odds odds;

		/*
		 * index used to check whether a blossom was reached in the current blossom traverse
		 */
		int blossomVisitIdx;

		/* accumulated delta from the beginning of current search */
		double delta;

		/* dual value of a vertex at the beginning of the current search. y0(u) */
		final double[] vertexDualValBase;

		/* Edge with minimum slack going to each vertex: vertex -> next grow edge */
		final EdgeEvent[] vToGrowEvent;

		/* Heap storing all the grow events */
		final HeapReferenceable<EdgeEvent> growEvents;

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

		/* Heap storing all expand events for odd vertices */
		final HeapReferenceable<Blossom> expandEvents;

		/* queue used during blossom creation to union all vertices */
		final IntPriorityQueue unionQueue;

		/* queue used during blossom creation to remember all new vertex to scan from */
		final IntPriorityQueue scanQueue;

		/* Manage debug prints */
		final DebugPrintsManager debug;

		static class Blossom extends MaximumMatchingWeightedBlossoms.Blossom<Blossom> {

			/*
			 * the edge that connected this blossom and it's left brother, null if left is null
			 */
			int toLeftEdge = -1;

			/*
			 * the edge that connected this blossom and it's right brother, null if right is null
			 */
			int toRightEdge = -1;

			/*
			 * index of root vertex in the search tree, -1 if this blossom is out. relevant only for top blossoms
			 */
			int root;

			/* edge that connect this blossom to the parent blossom in the search tree */
			int treeParentEdge = -1;

			/*
			 * true if this blossom is even, maintained only for trivial blossoms and top blossoms
			 */
			boolean isEven;

			/*
			 * find1 data structure label the vertices with indices, these are the first and last (exclusive) indices of
			 * all vertices in this blossoms. only relevant if odd
			 */
			int find1SeqBegin;
			int find1SeqEnd;

			/* dual value of this blossom at the beginning of the current search */
			double z0;

			/* the value of delta at the time this blossom became even */
			double delta0;

			/* the value of delta at the time this blossom became odd */
			double delta1;

			/*
			 * the accumulated deltas this blossom was part of odd blossom, doesn't include the time this blossom is top
			 * odd blossom
			 */
			double deltaOdd;

			/*
			 * pointer to the grow event for this blossom, relevant only if this blossom is out
			 */
			HeapReference<EdgeEvent> growRef;

			/* delta threshold for this blossom to be expanded */
			double expandDelta;

			/*
			 * pointer to the expand event for this blossom, relevant only if this blossom is top odd
			 */
			HeapReference<Blossom> expandRef;

			Blossom(int base) {
				this.base = base;
			}

			@Override
			public String toString() {
				return "" + (root == -1 ? 'X' : isEven ? 'E' : 'O') + base;
			}

		}

		static class EdgeVal {
			final int e;
			final int twin;
			Blossom b0;
			Blossom b1;

			EdgeVal(int e, int twin) {
				this.e = e;
				this.twin = twin;
			}
		}

		private static class EdgeEvent {
			final int e;
			final double slack;

			EdgeEvent(int e, double slack) {
				this.e = e;
				this.slack = slack;
			}

			@Override
			public String toString() {
				return "" + e + "[" + slack + "]";
			}
		}

		private static final Object EdgeValKey = new Object();

		Worker(Graph g0, EdgeWeightFunc w, HeapReferenceable.Builder heapBuilder, DebugPrintsManager debugPrint) {
			int n = g0.vertices().size();
			this.g = new GraphArrayDirected(n);
			edgeVal = g.addEdgesWeights(EdgeValKey, EdgeVal.class);
			this.w = e -> w.weight(edgeVal.get(e).e);

			for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g0.edgeSource(e), v = g0.edgeTarget(e);
				int e1 = g.addEdge(u, v);
				int e2 = g.addEdge(v, u);
				EdgeVal val1 = new EdgeVal(e, e2);
				EdgeVal val2 = new EdgeVal(e, e1);
				edgeVal.set(e1, val1);
				edgeVal.set(e2, val2);
			}

			matched = new int[n];
			Arrays.fill(matched, -1);

			blossoms = new Blossom[n];
			evens = new Evens<>(n);
			odds = new Odds(n);
			blossomVisitIdx = 0;

			delta = 0;
			vertexDualValBase = new double[n];

			vToGrowEvent = new EdgeEvent[n];
			vToSMFId = new SubtreeMergeFindMin.Node[n];
			oddBlossomPath = new int[n];
			growEvents = heapBuilder.build((e1, e2) -> Double.compare(growEventsKey(e1), growEventsKey(e2)));
			smf = new SubtreeMergeFindMinImpl<>((e1, e2) -> Double.compare(e1.slack, e2.slack));
			expandEvents = heapBuilder.build((b1, b2) -> Double.compare(b1.expandDelta, b2.expandDelta));

			unionQueue = new IntArrayFIFOQueue();
			scanQueue = new IntArrayFIFOQueue();

			this.debug = debugPrint;
		}

		private IntCollection computeMaxMatching(boolean perfect) {
			int n = g.vertices().size();

			// init dual value of all vertices as maxWeight / 2
			double maxWeight = Double.MIN_VALUE;
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxWeight = Math.max(maxWeight, w.weight(e));
			}
			double delta1Threshold = maxWeight / 2;
			for (int u = 0; u < n; u++)
				vertexDualValBase[u] = delta1Threshold;

			// init all trivial (singleton) blossoms
			for (int u = 0; u < n; u++)
				blossoms[u] = new Blossom(u);

			mainLoop: for (;;) {

				// Reset find0 and find1
				evens.init(n);
				odds.init(n);
				Arrays.fill(vToSMFId, null);
				smfRootOfRoots = smf.initTree();

				// Init unmatched blossoms as even and all other as out
				for (Blossom b : topBlossoms()) {
					if (isMatched(b.base)) {
						// Out blossom
						odds.initIndexing(b);
						odds.split(b);
						b.delta1 = delta;

					} else {
						// Unmatched even blossom
						b.root = b.base;
						b.isEven = true;
						b.delta0 = delta;
						int base = b.base;

						/* Update SMF data structure */
						SubtreeMergeFindMin.Node baseSMFNode = smfAddLeaf(base, smfRootOfRoots);
						for (IntIterator uit = b.vertices(); uit.hasNext();) {
							int u = uit.nextInt();
							blossoms[u].isEven = true;
							evens.union(base, u);

							if (u != base) {
								SubtreeMergeFindMin.Node smfNode = smfAddLeaf(u, baseSMFNode);
								smf.mergeSubTrees(baseSMFNode, smfNode);
							}
						}
						evens.setBlossom(base, b);
					}
				}
				// Insert grow and blossom events into heaps
				for (Blossom U : topBlossoms()) {
					if (!isMatched(U.base)) { /* only root blossoms */
						for (IntIterator uit = U.vertices(); uit.hasNext();) {
							int u = uit.nextInt();
							insertGrowEventsFromVertex(u);
							insertBlossomEventsFromVertex(u);
						}
					}
				}

				/* [debug] print current roots */
				debug.printExec(() -> {
					debug.print("roots:");
					for (Blossom b : topBlossoms())
						if (!isMatched(b.base))
							debug.print(" ", b);
					debug.println();
				});

				currentSearch: for (;;) {
					double delta1 = delta1Threshold;
					double delta2 = growEvents.isEmpty() ? Double.MAX_VALUE : growEventsKey(growEvents.findMin());
					double delta3 =
							!smf.hasNonTreeEdge() ? Double.MAX_VALUE : smf.findMinNonTreeEdge().edgeData().slack / 2;
					double delta4 = expandEvents.isEmpty() ? Double.MAX_VALUE : expandEvents.findMin().expandDelta;

					double deltaNext = Math.min(delta2, Math.min(delta3, delta4));
					if (deltaNext == Double.MAX_VALUE || (!perfect && delta1 < deltaNext))
						break mainLoop;

					debug.print("delta ", Double.valueOf(deltaNext), " (+", Double.valueOf(deltaNext - delta), ")");
					assert deltaNext + EPS >= delta;
					delta = deltaNext;

					debug.printExec(() -> {
						debug.print(" ", Arrays.asList(blossoms).stream().map(b -> String.valueOf(dualVal(b.base)))
								.collect(Collectors.joining(", ", "[", "]")));
						List<Blossom> topLevelBlossoms = new ArrayList<>();
						for (Blossom b : blossoms) {
							for (; b.parent != null; b = b.parent);
							topLevelBlossoms.add(b);
						}
						debug.print(" ", topLevelBlossoms.stream().distinct().filter(b -> !b.isSingleton())
								.map(b -> "" + b + " " + dualVal(b)).collect(Collectors.joining(", ", "[", "]")));

						debug.print("\nMatched: ");
						debug.println(Arrays.toString(matched));
					});

					if (deltaNext == delta2) {
						growStep();
					} else if (deltaNext == delta3) {
						assert Utils.isEqual(delta, smf.findMinNonTreeEdge().edgeData().slack / 2);
						int e = smf.findMinNonTreeEdge().edgeData().e;
						int u = g.edgeSource(e), v = g.edgeTarget(e);
						assert isEven(u) && isEven(v);

						if (evens.findBlossom(u).root == evens.findBlossom(v).root)
							blossomStep(e);
						else {
							augmentStep(e);
							break currentSearch;
						}
					} else if (deltaNext == delta4) {
						expandStep();
					} else
						throw new IllegalStateException();
				}

				// Update dual values
				for (int u = 0; u < n; u++)
					vertexDualValBase[u] = dualVal(u);
				for (Blossom b : allBlossoms()) {
					if (!b.isSingleton())
						b.z0 = dualVal(b);
					b.delta0 = b.delta1 = b.deltaOdd = 0;
				}
				delta1Threshold -= delta;
				delta = 0;

				// Reset blossoms search tree
				for (Blossom b : allBlossoms()) {
					b.root = -1;
					b.treeParentEdge = -1;
					b.isEven = false;
					b.find1SeqBegin = b.find1SeqEnd = 0;
					b.growRef = null;
					b.expandDelta = 0;
					b.expandRef = null;
				}

				// Reset heaps
				Arrays.fill(vToGrowEvent, null);
				growEvents.clear();
				smf.clear();
				expandEvents.clear();
			}

			IntList res = new IntArrayList();
			for (int u = 0; u < n; u++)
				if (isMatched(u) && u < g.edgeEndpoint(matched[u], u))
					res.add(edgeVal.get(matched[u]).e);
			return res;
		}

		private void growStep() {
			debug.print("growStep (root=",
					Integer.valueOf(evens.findBlossom(g.edgeSource(growEvents.findMin().e)).root), "): ",
					Integer.valueOf(growEvents.findMin().e));

			// Grow step
			assert delta == growEventsKey(growEvents.findMin());
			int e = growEvents.extractMin().e;
			int u = g.edgeSource(e), v = g.edgeTarget(e);

			Blossom U = evens.findBlossom(u), V = odds.findBlossom(v);
			assert !V.isEven && !isInTree(V);

			// Add odd vertex
			V.root = U.root;
			V.treeParentEdge = edgeVal.get(e).twin;
			V.isEven = false;
			V.delta1 = delta;
			assert V.growRef.get().e == e;
			V.growRef = null;
			if (!V.isSingleton()) {
				V.expandDelta = V.z0 / 2 + V.delta1;
				V.expandRef = expandEvents.insert(V);
			}
			debug.print(" ", V);

			int pathLen = computePath(V, v, oddBlossomPath);
			assert pathLen > 0;
			assert oddBlossomPath[0] == v;
			assert vToSMFId[u] != null;
			assert vToSMFId[v] == null;
			SubtreeMergeFindMin.Node smfParent = smfAddLeaf(v, vToSMFId[u]);
			for (int i = 1; i < pathLen; i++) {
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
				growEvents.removeRef(V.growRef);
				V.growRef = null;
			}
			assert vToSMFId[V.base] == null;
			smfAddLeaf(V.base, smfParent);
			makeEven(V);
			debug.println(" ", V);
		}

		private void blossomStep(int e) {
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
						expandEvents.removeRef(b.expandRef);
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

		private void makeEven(Blossom V) {
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

		private void expandStep() {
			debug.println("expandStep");

			assert Utils.isEqual(delta, expandEvents.findMin().expandDelta);
			final Blossom B = expandEvents.extractMin();

			assert B.root != -1 && !B.isEven && !B.isSingleton() && dualVal(B) <= EPS;

			int baseV = B.base, topV = g.edgeSource(B.treeParentEdge);
			Blossom base = null;
			Blossom top = null;
			// Remove parent pointer from all children, and find the sub blossom containing
			// the base ('base') and the sub blossom containing the vertex of the edge from
			// parent in search tree ('top')
			for (Blossom b : B.children()) {
				if (odds.isInBlossom(b, baseV)) {
					assert base == null;
					base = b;
				}
				if (odds.isInBlossom(b, topV)) {
					assert top == null;
					top = b;
				}
				b.parent = null;
			}
			B.deltaOdd += delta - B.delta1;
			B.delta0 = delta;

			// Iterate over sub blossom that should stay in the tree
			boolean left = matched[g.edgeSource(top.toLeftEdge)] == top.toLeftEdge;
			Consumer<Blossom> inBlossom = b -> {
				b.root = B.root;
				b.treeParentEdge = left ? b.toRightEdge : b.toLeftEdge;
				b.deltaOdd = B.deltaOdd;
			};
			Function<Blossom, Blossom> next = b -> left ? b.left : b.right;
			for (Blossom b = top;;) {
				// sub blossom odd
				inBlossom.accept(b);
				b.isEven = false;
				b.delta1 = delta;
				odds.split(b);
				assert b.expandRef == null;
				if (!b.isSingleton()) {
					b.expandDelta = b.z0 / 2 + b.delta1;
					b.expandRef = expandEvents.insert(b);
				}
				if (b == base)
					break;
				b = next.apply(b);

				// sub blossom even
				inBlossom.accept(b);
				makeEven(b);
				b = next.apply(b);
			}
			top.treeParentEdge = B.treeParentEdge;
			B.root = -1;

			// Iterate over sub blossoms that should not stay in the tree
			for (Blossom b = base;;) {
				b = next.apply(b);
				if (b == top)
					break;
				assert vToSMFId[b.base] == null;
				b.root = -1;
				b.treeParentEdge = -1;
				b.isEven = false;
				odds.split(b);
				b.deltaOdd = B.deltaOdd;
				assert b.growRef == null;
				EdgeEvent inEdgeEvent = odds.findMin(b.base);
				if (inEdgeEvent != null)
					b.growRef = growEvents.insert(inEdgeEvent);
			}

			// Disassemble right and left pointers of sub blossoms
			for (Blossom b = top;;) {
				Blossom nextB = b.left;
				b.right = b.left = null;
				EdgeVal bRightData = edgeVal.get(b.toRightEdge);
				EdgeVal bRightTwinData = edgeVal.get(bRightData.twin);
				EdgeVal bLeftData = edgeVal.get(b.toLeftEdge);
				EdgeVal bLeftTwinData = edgeVal.get(bLeftData.twin);
				bRightData.b0 = bRightData.b1 = null;
				bRightTwinData.b0 = bRightTwinData.b1 = null;
				bLeftData.b0 = bLeftData.b1 = null;
				bLeftTwinData.b0 = bLeftTwinData.b1 = null;
				b.toRightEdge = b.toLeftEdge = -1;
				if (nextB == top)
					break;
				b = nextB;
			}
		}

		private void augmentStep(int bridge) {
			debug.print("augStep:");
			final int bu = g.edgeSource(bridge), bv = g.edgeTarget(bridge);
			Blossom U = topBlossom(bu), V = topBlossom(bv);
			for (Blossom b : new Blossom[] { U, V }) {

				int e = -1;
				for (int u = b == U ? bu : bv;;) {
					assert b.isEven;
					augmentPath(b, u);
					if (e != -1) {
						int eu = g.edgeSource(e), ev = g.edgeTarget(e);
						matched[eu] = e;
						matched[ev] = edgeVal.get(e).twin;

						debug.print(" ", Integer.valueOf(e));
						assert matched[eu] != -1;
						assert matched[ev] != -1;
					}
					if (b.treeParentEdge == -1)
						break;
					// Odd
					b = topBlossom(g.edgeTarget(b.treeParentEdge));
					assert !b.isEven;
					u = g.edgeSource(b.treeParentEdge);
					augmentPath(b, u);

					// Even
					e = b.treeParentEdge;
					u = g.edgeTarget(e);
					b = topBlossom(u);
				}
			}
			matched[bu] = bridge;
			matched[bv] = edgeVal.get(bridge).twin;
			debug.println(" ", Integer.valueOf(bridge));
		}

		private void augmentPath(Blossom B, int u) {
			if (B.base == u)
				return;

			int m = matched[u];
			int mu = g.edgeSource(m), mv = g.edgeTarget(m);
			matched[mu] = matched[mv] = -1;
			EdgeVal mData = edgeVal.get(m);
			int v;
			Blossom b0, b1, b2;
			if (mu == u) {
				// u = m.source();
				v = mv;
				b0 = mData.b0;
				b1 = mData.b1;
			} else {
				// u = m.target();
				v = mu;
				b0 = mData.b1;
				b1 = mData.b0;
			}

			int xy;
			if (b0.right == b1) {
				b2 = b1.right;
				xy = b1.toRightEdge;
			} else {
				assert b0.left == b1;
				b2 = b1.left;
				xy = b1.toLeftEdge;
			}

			assert b0 != b1;
			assert b1 != b2;
			assert b2 != b0;
			assert b1.base == v;

			int xyU = g.edgeSource(xy), xyV = g.edgeTarget(xy);
			augmentPath(b1, xyU);
			augmentPath(B, xyV);
			matched[xyU] = xy;
			matched[xyV] = edgeVal.get(xy).twin;

			assert g.edgeSource(matched[xyU]) == xyU;
			assert g.edgeTarget(matched[xyU]) == xyV;
			assert g.edgeSource(matched[xyV]) == xyV;
			assert g.edgeSource(matched[xyV]) == xyV;

			assert isMatched(b1.base);
			assert isMatched(b2.base);

			debug.print(" ", Integer.valueOf(xy));
			for (Blossom p = b0.parent;; p = p.parent) {
				if (p == B.parent)
					break;
				p.base = u;
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

		private boolean isEven(int v) {
			return blossoms[v].isEven;
		}

		private boolean isMatched(int v) {
			return matched[v] != -1;
		}

		private boolean isInTree(int v) {
			return topBlossom(v).root != -1;
		}

		private boolean isInTree(Blossom b) {
			return b.parent != null ? isInTree(b.base) : b.root != -1;
		}

		private Blossom topBlossom(int v) {
			return isEven(v) ? evens.findBlossom(v) : odds.findBlossom(v);
		}

		private Blossom topBlossom(Blossom v) {
			assert v.isSingleton();
			return v.isEven ? evens.findBlossom(v.base) : odds.findBlossom(v.base);
		}

		private Iterable<Blossom> allBlossoms() {
			return () -> MaximumMatchingWeightedBlossoms.allBlossoms(blossoms, ++blossomVisitIdx);
		}

		private Iterable<Blossom> topBlossoms() {
			return () -> MaximumMatchingWeightedBlossoms.topBlossoms(blossoms, ++blossomVisitIdx);
		}

		private double dualVal(int v) {
			Blossom b = odds.findBlossom(v);
			double deltaB = b == null ? 0 : b.deltaOdd;
			double val = vertexDualValBase[v] + deltaB;
			boolean isEven;

			if (b == null)
				// v was part of an even blossom from the beginning of the current search
				val -= delta;
			else if ((isEven = isEven(v)) || b.root != -1)
				// v was part of an out blossom, b is max blossom before v became even
				val += isEven ? -(delta - b.delta0) : +(delta - b.delta1);
			return val;
		}

		private double dualVal(Blossom b) {
			assert !b.isSingleton();
			double zb = b.z0;
			if (b.parent == null && b.root != -1)
				zb += 2 * (b.isEven ? +(delta - b.delta0) : -(delta - b.delta1));
			return zb;
		}

		private double growEventsKey(EdgeEvent event) {
			int v = g.edgeTarget(event.e);
			assert !isEven(v);
			return odds.findBlossom(v).deltaOdd + event.slack;
		}

		private void insertGrowEventsFromVertex(int u) {
			double Yu = delta + dualVal(u);
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (isEven(v))
					continue;
				double slackBar = Yu + vertexDualValBase[v] - w.weight(e);
				if (vToGrowEvent[v] == null || slackBar < vToGrowEvent[v].slack) {
					EdgeEvent event = vToGrowEvent[v] = new EdgeEvent(e, slackBar);
					if (!odds.decreaseKey(v, event))
						continue;
					assert odds.findMin(v) == event;

					Blossom V = odds.findBlossom(v);
					if (!isInTree(V)) {
						if (V.growRef == null)
							V.growRef = growEvents.insert(event);
						else
							growEvents.decreaseKey(V.growRef, event);
					}
				}
			}
		}

		private void insertBlossomEventsFromVertex(int u) {
			assert isEven(u);
			Blossom U = evens.findBlossom(u);
			double Yu = delta + dualVal(u);
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				final int e = eit.nextInt();
				int v = eit.target();
				if (!isEven(v))
					continue;
				Blossom V = evens.findBlossom(v);
				if (U == V)
					continue;
				double Yv = delta + dualVal(v);
				double slackBar = Yu + Yv - w.weight(e);

				assert slackBar >= 0;
				smf.addNonTreeEdge(vToSMFId[u], vToSMFId[v], new EdgeEvent(e, slackBar));
			}
		};

		private void connectSubBlossoms(Blossom left, Blossom right, int leftToRightEdge, boolean reverse) {
			if (reverse) {
				Blossom temp = left;
				left = right;
				right = temp;
				leftToRightEdge = edgeVal.get(leftToRightEdge).twin;
			}
			EdgeVal edgeData = edgeVal.get(leftToRightEdge);
			EdgeVal twinData = edgeVal.get(edgeData.twin);
			left.right = right;
			left.toRightEdge = leftToRightEdge;
			right.left = left;
			right.toLeftEdge = edgeData.twin;
			edgeData.b0 = left;
			edgeData.b1 = right;
			twinData.b0 = right;
			twinData.b1 = left;
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

		private Blossom lcaInSearchTree(Blossom b1, Blossom b2) {
			int visitIdx = ++blossomVisitIdx;
			for (Blossom[] bs = new Blossom[] { b1, b2 };;) {
				if (bs[0] == null && bs[1] == null)
					return null;
				for (int i = 0; i < bs.length; i++) {
					Blossom b = bs[i];
					if (b == null)
						continue;
					if (b.lastVisitIdx == visitIdx)
						return b;
					b.lastVisitIdx = visitIdx;
					bs[i] = b.treeParentEdge == -1 ? null : topBlossom(g.edgeTarget(b.treeParentEdge));
				}
			}
		}

		static class Odds {

			/*
			 * Split find data structure for odd and out blossoms, used with vToSf and findToBlossom:
			 * findToBlossom[df.find(vToSf[v])]
			 */
			final SplitFindMin<EdgeEvent> sf;

			/* vertex -> splitFind index */
			final int[] vToSf;

			/* used to assign splitFind index to each vertex in an odd blossom */
			int nextIdx;

			/* df.find() result -> blossom */
			final Blossom[] findToBlossom;

			Odds(int n) {
				sf = new SplitFindMinArray<>();
				vToSf = new int[n];
				findToBlossom = new Blossom[n];
			}

			void init(int n) {
				Comparator<EdgeEvent> edgeSlackBarComparator =
						(e1, e2) -> (e2 == null ? -1 : e1 == null ? 1 : Double.compare(e1.slack, e2.slack));

				Arrays.fill(vToSf, -1);
				sf.init(new NullList<>(n), edgeSlackBarComparator);
				nextIdx = 0;
			}

			private Blossom findBlossom(int v) {
				int idx = vToSf[v];
				return idx < 0 ? null : findToBlossom[sf.find(idx)];
			}

			/* Init find1 indexing for all vertices contained in the blossomD */
			private void initIndexing(Blossom b) {
				b.find1SeqBegin = nextIdx;
				if (b.child == null) {
					b.isEven = false;
					vToSf[b.base] = nextIdx++;
				} else {
					for (Blossom sub : b.children())
						initIndexing(sub);
				}
				b.find1SeqEnd = nextIdx;
			}

			/* Split a blossom from a bigger blossom in the find1 data structure */
			private void split(Blossom b) {
				int begin = b.find1SeqBegin, end = b.find1SeqEnd;
				Blossom b1 = begin > 0 ? findToBlossom[sf.find(begin - 1)] : null;
				Blossom b2 = end < findToBlossom.length ? findToBlossom[sf.find(end)] : null;

				if (begin > 0) {
					sf.split(begin);
					findToBlossom[sf.find(begin - 1)] = b1;
				}
				if (end < findToBlossom.length) {
					sf.split(end);
					findToBlossom[sf.find(end)] = b2;
				}
				findToBlossom[sf.find(b.find1SeqBegin)] = b;
			}

			boolean isInBlossom(Blossom b, int v) {
				int idx = vToSf[v];
				return b.find1SeqBegin <= idx && idx < b.find1SeqEnd;
			}

			EdgeEvent findMin(int v) {
				return sf.getKey(vToSf[v]);
			}

			boolean decreaseKey(int v, EdgeEvent newKey) {
				return sf.decreaseKey(vToSf[v], newKey);
			}

		}

	}

}
