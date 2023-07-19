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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.jgalgo.MatchingWeightedGabow1990Abstract.Worker.EdgeEvent;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.data.HeapReference;
import com.jgalgo.internal.data.HeapReferenceable;
import com.jgalgo.internal.data.SplitFindMin;
import com.jgalgo.internal.data.UnionFind;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.DebugPrintsManager;
import com.jgalgo.internal.util.IntArrayFIFOQueue;
import com.jgalgo.internal.util.Utils;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

abstract class MatchingWeightedGabow1990Abstract extends Matchings.AbstractMaximumMatchingImpl {

	final DebugPrintsManager debugPrintManager = new DebugPrintsManager(false);
	HeapReferenceable.Builder<Object, Object> heapBuilder = HeapReferenceable.newBuilder();
	static final double EPS = 0.00001;

	@Override
	Matching computeMaximumWeightedMatching(IndexGraph g, WeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		return newWorker(g, w, heapBuilder, debugPrintManager).computeMaxMatching(false);

	}

	@Override
	Matching computeMaximumWeightedPerfectMatching(IndexGraph g, WeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		return newWorker(g, w, heapBuilder, debugPrintManager).computeMaxMatching(true);
	}

	abstract Worker newWorker(IndexGraph gOrig, WeightFunction w, HeapReferenceable.Builder<Object, Object> heapBuilder,
			DebugPrintsManager debugPrint);

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(HeapReferenceable.Builder<Object, Object> heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	static class Blossom {

		/* base vertex of this blossom */
		int base;

		/* parent blossom, null if top blossom */
		Blossom parent;

		/* child blossom, null if trivial blossom (blossom of one vertex) */
		Blossom child;

		/*
		 * left brother in current sub blossoms level (share parent), null if top blossom
		 */
		Blossom left;

		/*
		 * right brother in current sub blossoms level (share parent), null if top blossom
		 */
		Blossom right;

		/*
		 * true if this blossom is even, maintained only for trivial blossoms and top blossoms
		 */
		boolean isEven;

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
		 * find1 data structure label the vertices with indices, these are the first and last (exclusive) indices of all
		 * vertices in this blossoms. only relevant if odd
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
		 * the accumulated deltas this blossom was part of odd blossom, doesn't include the time this blossom is top odd
		 * blossom
		 */
		double deltaOdd;

		/*
		 * pointer to the grow event for this blossom, relevant only if this blossom is out
		 */
		HeapReference<EdgeEvent, Void> growRef;

		/*
		 * pointer to the expand event for this blossom, relevant only if this blossom is top odd
		 */
		HeapReference<Double, Blossom> expandRef;

		/* field used to keep track which blossoms were visited during traverse */
		int lastVisitIdx;

		Blossom(int base) {
			this.base = base;
		}

		@Override
		public String toString() {
			return "" + (root == -1 ? 'X' : isEven ? 'E' : 'O') + base;
		}

		boolean isSingleton() {
			return child == null;
		}

		Iterable<Blossom> children() {
			return new Iterable<>() {

				@Override
				public Iterator<Blossom> iterator() {
					if (child == null)
						return Collections.emptyIterator();
					return new Iterator<>() {

						final Blossom begin = child;
						Blossom c = begin;

						@Override
						public boolean hasNext() {
							return c != null;
						}

						@Override
						public Blossom next() {
							Assertions.Iters.hasNext(this);
							Blossom ret = c;
							c = ret.right;
							if (c == begin)
								c = null;
							return ret;
						}
					};
				}
			};
		}

		IntIterator vertices() {
			if (isSingleton())
				return IntIterators.singleton(base);
			return new IntIterator() {

				int next;
				final Stack<Iterator<Blossom>> stack = new ObjectArrayList<>();
				{
					for (Iterator<Blossom> it = children().iterator();;) {
						Blossom c = it.next();
						if (it.hasNext())
							stack.push(it);
						if (c.isSingleton()) {
							next = c.base;
							break;
						}
						it = c.children().iterator();
					}
				}

				@Override
				public boolean hasNext() {
					return next != -1;
				}

				@Override
				public int nextInt() {
					Assertions.Iters.hasNext(this);
					int ret = next;

					if (!stack.isEmpty()) {
						for (Iterator<Blossom> it = stack.pop();;) {
							Blossom c = it.next();
							if (it.hasNext())
								stack.push(it);
							if (c.isSingleton()) {
								next = c.base;
								break;
							}
							it = c.children().iterator();
						}
					} else {
						next = -1;
					}

					return ret;
				}

			};
		}

	}

	static class Evens {

		/*
		 * Union find data structure for even blossoms, used with findToBlossoms: findToBlossoms[uf.find(v)]
		 */
		final UnionFind uf;

		/* uf result -> blossom */
		final Blossom[] findToBlossoms;

		Evens(int n) {
			uf = UnionFind.newBuilder().expectedSize(n).build();
			findToBlossoms = new Blossom[n];
		}

		void init(int n) {
			uf.clear();
			for (int i = 0; i < n; i++)
				uf.make();
		}

		void union(int u, int v) {
			uf.union(u, v);
		}

		void setBlossom(int v, Blossom b) {
			findToBlossoms[uf.find(v)] = b;
		}

		Blossom findBlossom(int v) {
			return findToBlossoms[uf.find(v)];
		}

	}

	static class Odds {

		/*
		 * Split find data structure for odd and out blossoms, used with vToSf and findToBlossom:
		 * findToBlossom[sf.find(vToSf[v])]
		 */
		final SplitFindMin<EdgeEvent> sf;

		/* vertex -> splitFind index */
		final int[] vToSf;

		/* used to assign splitFind index to each vertex in an odd blossom */
		int nextIdx;

		/* sf.find() result -> blossom */
		final Blossom[] findToBlossom;

		Odds(int n) {
			sf = SplitFindMin.newBuilder().buildWithFindMin();
			vToSf = new int[n];
			findToBlossom = new Blossom[n];
		}

		void init(int n) {
			Comparator<EdgeEvent> edgeSlackBarComparator =
					(e1, e2) -> (e2 == null ? -1 : e1 == null ? 1 : Double.compare(e1.slack, e2.slack));

			Arrays.fill(vToSf, -1);
			sf.init(Utils.nullList(n), edgeSlackBarComparator);
			nextIdx = 0;
		}

		Blossom findBlossom(int v) {
			int idx = vToSf[v];
			return idx < 0 ? null : (Blossom) findToBlossom[sf.find(idx)];
		}

		/* Init find1 indexing for all vertices contained in the blossomD */
		void initIndexing(Blossom b) {
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
		void split(Blossom b) {
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

	static abstract class Worker {

		/* The original graph */
		final IndexGraph gOrig;

		/* the graph */
		final IndexGraph g;

		final Weights<EdgeVal> edgeVal;

		/* the weight function */
		final WeightFunction w;

		/* vertex -> matched edge */
		final int[] matched;

		/* vertex -> trivial blossom */
		final Blossom[] blossoms;

		/*
		 * Union find data structure for even blossoms
		 */
		final Evens evens;

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
		final HeapReferenceable<EdgeEvent, Void> growEvents;

		/* Heap storing all expand events for odd vertices */
		final HeapReferenceable<Double, Blossom> expandEvents;

		/* queue used during blossom creation to union all vertices */
		final IntPriorityQueue unionQueue;

		/* queue used during blossom creation to remember all new vertex to scan from */
		final IntPriorityQueue scanQueue;

		/* Manage debug prints */
		final DebugPrintsManager debug;

		static class EdgeEvent {
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

		private static final Object EdgeValKey = new Utils.Obj("edgeVal");

		Worker(IndexGraph gOrig, WeightFunction w, HeapReferenceable.Builder<Object, Object> heapBuilder,
				DebugPrintsManager debugPrint) {
			int n = gOrig.vertices().size();
			this.gOrig = gOrig;
			this.g = IndexGraphFactory.newDirected().expectedVerticesNum(n).newGraph();
			for (int v = 0; v < n; v++)
				g.addVertex();
			edgeVal = g.addEdgesWeights(EdgeValKey, EdgeVal.class);
			WeightFunction wLocal = WeightFunctions.localEdgeWeightFunction(gOrig, w);
			this.w = e -> wLocal.weight(edgeVal.get(e).e);

			for (int e : gOrig.edges()) {
				int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
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
			evens = new Evens(n);
			odds = new Odds(n);
			blossomVisitIdx = 0;

			delta = 0;
			vertexDualValBase = new double[n];

			vToGrowEvent = new EdgeEvent[n];
			growEvents = heapBuilder.<EdgeEvent>keysTypeObj().valuesTypeVoid()
					.build((e1, e2) -> Double.compare(growEventsKey(e1), growEventsKey(e2)));
			expandEvents = heapBuilder.keysTypePrimitive(double.class).<Blossom>valuesTypeObj().build();

			unionQueue = new IntArrayFIFOQueue();
			scanQueue = new IntArrayFIFOQueue();

			this.debug = debugPrint;
		}

		void initDataStructuresSearchBegin() {
			int n = g.vertices().size();

			// Reset find0 and find1
			evens.init(n);
			odds.init(n);
		}

		void searchEnd() {
			Arrays.fill(vToGrowEvent, null);
			growEvents.clear();
			expandEvents.clear();
		}

		void initUnmatchedEvenBlossom(Blossom b) {
			b.root = b.base;
			b.isEven = true;
			b.delta0 = delta;
			int base = b.base;

			for (IntIterator uit = b.vertices(); uit.hasNext();) {
				int u = uit.nextInt();
				blossoms[u].isEven = true;
				evens.union(base, u);
			}
			evens.setBlossom(base, b);
		}

		abstract double computeNextDelta3();

		abstract EdgeEvent extractNextBlossomEvent();

		private Matching computeMaxMatching(boolean perfect) {
			int n = g.vertices().size();

			// init dual value of all vertices as maxWeight / 2
			double maxWeight = Double.MIN_VALUE;
			for (int e : g.edges())
				maxWeight = Math.max(maxWeight, w.weight(e));
			double delta1Threshold = maxWeight / 2;
			for (int u = 0; u < n; u++)
				vertexDualValBase[u] = delta1Threshold;

			// init all trivial (singleton) blossoms
			for (int u = 0; u < n; u++)
				blossoms[u] = new Blossom(u);

			mainLoop: for (;;) {
				initDataStructuresSearchBegin();

				// Init unmatched blossoms as even and all other as out
				for (Blossom b : topBlossoms()) {
					if (isMatched(b.base)) {
						// Out blossom
						odds.initIndexing(b);
						odds.split(b);
						b.delta1 = delta;

					} else {
						// Unmatched even blossom
						initUnmatchedEvenBlossom(b);
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
					double delta2 = growEvents.isEmpty() ? Double.MAX_VALUE : growEventsKey(growEvents.findMin().key());

					double delta3 = computeNextDelta3();

					double delta4 =
							expandEvents.isEmpty() ? Double.MAX_VALUE : expandEvents.findMin().key().doubleValue();

					double deltaNext = Math.min(delta2, Math.min(delta3, delta4));
					if (deltaNext == Double.MAX_VALUE || (!perfect && delta1 < deltaNext))
						break mainLoop;

					debug.print("delta ", Double.valueOf(deltaNext), " (+", Double.valueOf(deltaNext - delta), ")");
					assert deltaNext + EPS >= delta;
					delta = deltaNext;

					debug.printExec(() -> {
						debug.print(" ", Arrays.asList(blossoms).stream().map(b -> String.valueOf(dualVal(b.base)))
								.collect(Collectors.joining(", ", "[", "]")));
						List<Blossom> topLevelBlossoms = new ObjectArrayList<>();
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
						EdgeEvent event = extractNextBlossomEvent();
						assert Utils.isEqual(delta, event.slack / 2);
						int e = event.e;
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
					b.expandRef = null;
				}

				// Reset heaps
				searchEnd();
			}

			IntList matchingEdges = new IntArrayList();
			for (int u = 0; u < n; u++)
				if (isMatched(u) && u < g.edgeEndpoint(matched[u], u))
					matchingEdges.add(edgeVal.get(matched[u]).e);
			return new Matchings.MatchingImpl(gOrig, matchingEdges);
		}

		abstract void makeEven(Blossom V);

		abstract void growStep();

		abstract void blossomStep(int e);

		void expandStep() {
			debug.println("expandStep");

			assert Utils.isEqual(delta, expandEvents.findMin().key().doubleValue());
			final Blossom B = expandEvents.extractMin().value();

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
				if (!b.isSingleton())
					b.expandRef = expandEvents.insert(Double.valueOf(b.z0 / 2 + b.delta1), b);
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

		void augmentStep(int bridge) {
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
			int v = g.edgeEndpoint(m, u);
			matched[u] = matched[v] = -1;
			EdgeVal mData = edgeVal.get(m);
			Blossom b0 = mData.b0;
			Blossom b1 = mData.b1;

			int xy;
			Blossom b2;
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

		boolean isEven(int v) {
			return blossoms[v].isEven;
		}

		boolean isMatched(int v) {
			return matched[v] != -1;
		}

		boolean isInTree(int v) {
			return topBlossom(v).root != -1;
		}

		boolean isInTree(Blossom b) {
			return b.parent != null ? isInTree(b.base) : b.root != -1;
		}

		Blossom topBlossom(int v) {
			return isEven(v) ? evens.findBlossom(v) : odds.findBlossom(v);
		}

		Blossom topBlossom(Blossom v) {
			assert v.isSingleton();
			return v.isEven ? evens.findBlossom(v.base) : odds.findBlossom(v.base);
		}

		Iterable<Blossom> allBlossoms() {
			return () -> new Iterator<>() {
				final int visitIdx = ++blossomVisitIdx;
				final int n = blossoms.length;
				int v = 0;
				Blossom b = blossoms[v];

				@Override
				public boolean hasNext() {
					return b != null;
				}

				@Override
				public Blossom next() {
					Assertions.Iters.hasNext(this);
					Blossom ret = b;
					ret.lastVisitIdx = visitIdx;

					b = b.parent;
					if (b == null || b.lastVisitIdx == visitIdx) {
						if (++v < n) {
							b = blossoms[v];
						} else {
							b = null;
						}
					}
					return ret;
				}
			};
		}

		Iterable<Blossom> topBlossoms() {
			return () -> new Iterator<>() {

				final int visitIdx = ++blossomVisitIdx;
				final int n = blossoms.length;
				int v = 0;
				Blossom b;

				{
					for (b = blossoms[v]; b.parent != null;) {
						b.lastVisitIdx = visitIdx;
						b = b.parent;
					}
				}

				@Override
				public boolean hasNext() {
					return b != null;
				}

				@Override
				public Blossom next() {
					Assertions.Iters.hasNext(this);
					Blossom ret = b;
					ret.lastVisitIdx = visitIdx;

					nextSingleton: for (;;) {
						if (++v >= n) {
							b = null;
							break;
						}
						for (b = blossoms[v]; b.parent != null;) {
							b.lastVisitIdx = visitIdx;
							b = b.parent;
							if (b.lastVisitIdx == visitIdx)
								continue nextSingleton;
						}
						break;
					}

					return ret;
				}
			};
		}

		double dualVal(int v) {
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

		double dualVal(Blossom b) {
			assert !b.isSingleton();
			double zb = b.z0;
			if (b.parent == null && b.root != -1)
				zb += 2 * (b.isEven ? +(delta - b.delta0) : -(delta - b.delta1));
			return zb;
		}

		double growEventsKey(EdgeEvent event) {
			int v = g.edgeTarget(event.e);
			assert !isEven(v);
			return odds.findBlossom(v).deltaOdd + event.slack;
		}

		void insertGrowEventsFromVertex(int u) {
			double Yu = delta + dualVal(u);
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
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

		void insertBlossomEventsFromVertex(int u) {
			assert isEven(u);
			Blossom U = evens.findBlossom(u);
			double Yu = delta + dualVal(u);
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
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
				addBlossomEvent(e, slackBar);
			}
		};

		abstract void addBlossomEvent(int e, double slackBar);

		void connectSubBlossoms(Blossom left, Blossom right, int leftToRightEdge, boolean reverse) {
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

		Blossom lcaInSearchTree(Blossom b1, Blossom b2) {
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

	}

}
