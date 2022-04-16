package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Utils.NullList;
import com.ugav.algo.Utils.QueueIntFixSize;

public class MatchingWeightedGabow2018 implements MatchingWeighted {

	/*
	 * O(mn + n^2logn)
	 *
	 * This implementation is WIP, current O(mnlogn) TODO
	 */

	private MatchingWeightedGabow2018() {
	}

	private static final MatchingWeightedGabow2018 INSTANCE = new MatchingWeightedGabow2018();

	public static MatchingWeightedGabow2018 getInstance() {
		return INSTANCE;
	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g, WeightFunction<E> w) {
		if (g.isDirected())
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		return new Worker<>(g, w).calcMaxMatching(false);

	}

	@Override
	public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g, WeightFunction<E> w) {
		if (g.isDirected())
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		return new Worker<>(g, w).calcMaxMatching(true);
	}

	private static class Worker<E> {

		/* the graph */
		final Graph<EdgeVal<E>> g;

		/* the weight function */
		final WeightFunction<E> w;

		/* vertex -> matched edge */
		final Edge<EdgeVal<E>>[] matched;

		/* vertex -> trivial blossom */
		final Blossom<E>[] blossoms;

		/*
		 * Union find data structure for even blossoms, used with find0Blossoms:
		 * find0Blossoms[find0.find(v)]
		 */
		final UnionFind find0;

		/* find0 result -> blossom */
		final Blossom<E>[] find0Blossoms;

		/*
		 * Split find data structure for odd and out blossoms, used with vToFind1Idx and
		 * find1Blossoms: find1Blossoms[find1.find(vToFind1Idx[v])]
		 */
		final SplitFindMin<EdgeEvent<E>> find1;

		/* vertex -> find1 index */
		final int[] vToFind1Idx;

		/* used to assign find1 index to each vertex in an odd blossom */
		int find1IdxNext;

		/* find1 result -> blossom */
		final Blossom<E>[] find1Blossoms;

		/*
		 * index used to check whether a blossom was reached in the current blossom
		 * traversy
		 */
		int blossomVisitIdx;

		/* accumulated delta from the beginning of current search */
		double delta;

		/* dual value of a vertex at the beginning of the current search. y0(u) */
		final double[] vertexDualValBase;

		/* Edge with minimum slack going to each vertex: vertex -> next grow edge */
		final EdgeEvent<E>[] vToGrowEvent;

		/* Heap storing all the grow events */
		final Heap<EdgeEvent<E>> growEvents;

		/* Heap storing all the blossom and augmenting events */
		final Heap<EdgeEvent<E>> blossomEvents;

		/* Heap storing all expand events for odd vertices */
		final Heap<Blossom<E>> expandEvents;

		/* queue used during blossom creation to union all vertices */
		final QueueIntFixSize unionQueue;

		/* queue used during blossom creation to remember all new vertex to scan from */
		final QueueIntFixSize scanQueue;

		static class Blossom<E> {

			/* base vertex of this blossom */
			int base;

			/* parent blossom, null if top blossom */
			Blossom<E> parent;

			/* child blossom, null if trivial blossom (blossom of one vertex) */
			Blossom<E> child;

			/*
			 * left brother in current sub blossoms level (share parent), null if top
			 * blossom
			 */
			Blossom<E> left;

			/*
			 * right brother in current sub blossoms level (share parent), null if top
			 * blossom
			 */
			Blossom<E> right;

			/*
			 * the edge that connected this blossom and it's left brother, null if left is
			 * null
			 */
			Edge<EdgeVal<E>> toLeftEdge;

			/*
			 * the edge that connected this blossom and it's right brother, null if right is
			 * null
			 */
			Edge<EdgeVal<E>> toRightEdge;

			/*
			 * index of root vertex in the search tree, -1 if this blossom is out. relevant
			 * only for top blossoms
			 */
			int root;

			/* edge that connect this blossom to the parent blossom in the search tree */
			Edge<EdgeVal<E>> treeParentEdge;

			/*
			 * true if this blossom is even, maintained only for trivial blossoms and top
			 * blossoms
			 */
			boolean isEven;

			/*
			 * find1 data structure label the vertices with indices, these are the first and
			 * last (exclusive) indices of all vertices in this blossoms. only relevant if
			 * odd
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
			 * the accumulated deltas this blossom was part of odd blossom, doesn't include
			 * the time this blossom is top odd blossom
			 */
			double deltaOdd;

			/*
			 * pointer to the grow event for this blossom, relevant only if this blossom is
			 * out
			 */
			Heap.Handle<EdgeEvent<E>> growHandle;

			/* delta threshold for this blossom to be expanded */
			double expandDelta;

			/*
			 * pointer to the expand event for this blossom, relevant only if this blossom
			 * is top odd
			 */
			Heap.Handle<Blossom<E>> expandHandle;

			/* field used to keep track which blossoms were visited during traversy */
			int lastVisitIdx;

			Blossom(int base) {
				this.base = base;
			}

			boolean isSingleton() {
				return child == null;
			}

			@Override
			public String toString() {
				return "" + (root == -1 ? 'X' : isEven ? 'E' : 'O') + base;
			}

		}

		static class EdgeVal<E> {
			final Edge<E> e;
			Edge<EdgeVal<E>> twin;
			Blossom<E> b0;
			Blossom<E> b1;

			EdgeVal(Edge<E> e) {
				this.e = e;
			}

			@Override
			public String toString() {
				return e.toString();
			}
		}

		private static class EdgeEvent<E> {
			final Edge<EdgeVal<E>> e;
			final double slack;

			EdgeEvent(Edge<EdgeVal<E>> e, double slack) {
				this.e = e;
				this.slack = slack;
			}

			@Override
			public String toString() {
				return "" + e + "[" + slack + "]";
			}
		}

		@SuppressWarnings("unchecked")
		Worker(Graph<E> g, WeightFunction<E> w) {
			int n = g.vertices();
			this.g = new GraphArray<>(DirectedType.Directed, n);
			this.w = w;

			for (Edge<E> e : g.edges()) {
				Edge<EdgeVal<E>> e1, e2;
				(e1 = this.g.addEdge(e.u(), e.v())).val(new EdgeVal<>(e));
				(e2 = this.g.addEdge(e.v(), e.u())).val(new EdgeVal<>(e.twin()));
				e1.val().twin = e2;
				e2.val().twin = e1;
			}

			matched = new Edge[n];

			blossoms = new Blossom[n];
			find0 = new UnionFindArray(n);
			find0Blossoms = new Blossom[n];
			find1 = new SplitFindMinArray<>();
			vToFind1Idx = new int[n];
			find1Blossoms = new Blossom[n];
			blossomVisitIdx = 0;

			delta = 0;
			vertexDualValBase = new double[n];

			vToGrowEvent = new EdgeEvent[n];
			growEvents = new HeapFibonacci<>((e1, e2) -> Utils.compare(growEventsKey(e1), growEventsKey(e2)));
			blossomEvents = new HeapFibonacci<>((e1, e2) -> Utils.compare(e1.slack, e2.slack));
			expandEvents = new HeapFibonacci<>((b1, b2) -> Utils.compare(b1.expandDelta, b2.expandDelta));

			unionQueue = new QueueIntFixSize(n + 1);
			scanQueue = new QueueIntFixSize(n);
		}

		private Collection<Edge<E>> calcMaxMatching(boolean perfect) {
			int n = g.vertices();

			// init dual value of all vertices as maxWeight / 2
			double maxWeight = Double.MIN_VALUE;
			for (Edge<EdgeVal<E>> e : g.edges())
				maxWeight = Math.max(maxWeight, w.weight(e.val().e));
			double delta1Threshold = maxWeight / 2;
			for (int u = 0; u < n; u++)
				vertexDualValBase[u] = delta1Threshold;

			// init all trivial (singleton) blossoms
			for (int u = 0; u < n; u++)
				blossoms[u] = new Blossom<>(u);

			Comparator<EdgeEvent<E>> edgeSlackBarComparator = (e1, e2) -> {
				return e2 == null ? -1 : e1 == null ? 1 : Utils.compare(e1.slack, e2.slack);
			};

			mainLoop: for (;;) {

				// Reset find0 and find1
				find0.clear();
				for (int u = 0; u < n; u++)
					find0.make();
				Arrays.fill(vToFind1Idx, -1);
				find1.init(new NullList<>(n), edgeSlackBarComparator);
				find1IdxNext = 0;

				// Init unmatched blossoms as even and all other as out
				forEachTopBlossom(b -> {
					if (matched[b.base] != null) {
						// Out blossom
						find1InitIndexing(b);
						find1Split(b);
						b.delta1 = delta;

					} else {
						// Unmatched even blossom
						b.root = b.base;
						b.isEven = true;
						b.delta0 = delta;
						int base = b.base;
						forEachVertexInBlossom(b, u -> {
							blossoms[u].isEven = true;
							find0.union(base, u);
						});
						find0Blossoms[find0.find(base)] = b;
					}
				});
				// Insert grow and blossom events into heaps
				forEachTopBlossom(U -> {
					if (matched[U.base] == null) {
						forEachVertexInBlossom(U, this::insertGrowEventsFromVertex);
						forEachVertexInBlossom(U, this::insertBlossomEventsFromVertex);
					}
				});

				currentSearch: for (;;) {
					double delta1 = delta1Threshold;
					double delta2 = growEvents.isEmpty() ? Double.MAX_VALUE : growEventsKey(growEvents.findMin());
					double delta3 = blossomEvents.isEmpty() ? Double.MAX_VALUE : blossomEvents.findMin().slack / 2;
					double delta4 = expandEvents.isEmpty() ? Double.MAX_VALUE : expandEvents.findMin().expandDelta;

					double deltaNext = Math.min(delta2, Math.min(delta3, delta4));
					if (deltaNext == Double.MAX_VALUE || (!perfect && delta1 < deltaNext))
						break mainLoop;
					assert0(deltaNext >= delta);
					delta = deltaNext;

					if (deltaNext == delta2)
						growStep();
					else if (deltaNext == delta3) {
						assert0(delta == blossomEvents.findMin().slack / 2);
						Edge<EdgeVal<E>> e = blossomEvents.extractMin().e;
						assert0(isEven(e.u()) && isEven(e.v()));

						if (find0(e.u()).root == find0(e.v()).root)
							blossomStep(e);
						else {
							augmentStep(e);
							break currentSearch;
						}
					} else if (deltaNext == delta4)
						expandStep();
					else
						throw new InternalError();
				}

				// Update dual values
				for (int u = 0; u < n; u++)
					vertexDualValBase[u] = dualVal(u);
				forEachBlossom(b -> {
					if (!b.isSingleton())
						b.z0 = dualVal(b);
					b.delta0 = b.delta1 = b.deltaOdd = 0;
				});
				delta1Threshold -= delta;
				delta = 0;

				// Reset blossoms search tree
				forEachBlossom(b -> {
					b.root = -1;
					b.treeParentEdge = null;
					b.isEven = false;
					b.find1SeqBegin = b.find1SeqEnd = 0;
					b.growHandle = null;
					b.expandDelta = 0;
					b.expandHandle = null;
				});

				// Reset heaps
				Arrays.fill(vToGrowEvent, null);
				growEvents.clear();
				blossomEvents.clear();
				expandEvents.clear();
			}

			List<Edge<E>> res = new ArrayList<>();
			for (int u = 0; u < n; u++)
				if (matched[u] != null && u < matched[u].v())
					res.add(matched[u].val().e);
			return res;
		}

		private void growStep() {
			// Grow step
			assert0(delta == growEventsKey(growEvents.findMin()));
			Edge<EdgeVal<E>> e = growEvents.extractMin().e;

			Blossom<E> U = find0(e.u()), V = find1(e.v());
			assert0(!V.isEven && !isInTree(V));

			// Add odd vertex
			V.root = U.root;
			V.treeParentEdge = e.val().twin;
			V.isEven = false;
			V.delta1 = delta;
			assert0(V.growHandle.get().e == e);
			V.growHandle = null;
			if (!V.isSingleton()) {
				V.expandDelta = V.z0 / 2 + V.delta1;
				V.expandHandle = expandEvents.insert(V);
			}

			// Immediately add it's matched edge and vertex as even vertex
			Edge<EdgeVal<E>> matchedEdge = matched[V.base];
			V = topBlossom(matchedEdge.v());
			V.root = U.root;
			V.treeParentEdge = matchedEdge.val().twin;
			if (V.growHandle != null) {
				growEvents.removeHandle(V.growHandle);
				V.growHandle = null;
			}
			makeEven(V);
		}

		private void blossomStep(Edge<EdgeVal<E>> e) {
			assert0(isEven(e.u()) && isEven(e.v()));
			Blossom<E> U = find0(e.u()), V = find0(e.v());
			if (U == V)
				return; // Edge in same blossom, ignore

			// Create new blossom
			Blossom<E> base = lcaInSearchTree(U, V);
			Blossom<E> newb = new Blossom<>(base.base);
			newb.root = base.root;
			newb.treeParentEdge = base.treeParentEdge;
			newb.isEven = true;
			newb.child = base;
			newb.delta0 = delta;

			// Add all sub blossoms
			unionQueue.clear();
			scanQueue.clear();
			@SuppressWarnings("unchecked")
			Blossom<E>[] bs = new Blossom[] { U, V };
			for (Blossom<E> b : bs) {
				boolean prevIsRight = b == U;
				Blossom<E> prev = b == U ? V : U;
				Edge<EdgeVal<E>> toPrevEdge = b == U ? e : e.val().twin;

				while (true) {
					// handle even sub blossom
					assert0(b.isEven);
					if (!b.isSingleton())
						b.z0 = dualVal(b);
					b.parent = newb;
					connectSubBlossoms(b, prev, toPrevEdge, !prevIsRight);
					unionQueue.push(b.base);

					if (b == base)
						break;
					prev = b;
					toPrevEdge = matched[b.base].val().twin;
					assert0(matched[b.base] == b.treeParentEdge);
					b = topBlossom(toPrevEdge.u());

					// handle odd vertex
					assert0(!b.isEven);
					b.deltaOdd += delta - b.delta1;
					if (!b.isSingleton())
						b.z0 = dualVal(b);
					b.parent = newb;
					connectSubBlossoms(b, prev, toPrevEdge, !prevIsRight);
					forEachVertexInBlossom(b, v -> {
						blossoms[v].isEven = true;
						unionQueue.push(v);
						scanQueue.push(v);
					});
					b.delta0 = delta;
					if (!b.isSingleton()) {
						expandEvents.removeHandle(b.expandHandle);
						b.expandHandle = null;
					}

					prev = b;
					toPrevEdge = b.treeParentEdge.val().twin;
					b = topBlossom(toPrevEdge.u());
				}
			}

			// Union all sub blossom in find0 data structure
			while (!unionQueue.isEmpty())
				find0.union(newb.base, unionQueue.pop());
			find0Blossoms[find0.find(newb.base)] = newb;

			// Scan new edges from all new even vertices
			while (!scanQueue.isEmpty()) {
				int u = scanQueue.pop();
				insertGrowEventsFromVertex(u);
				insertBlossomEventsFromVertex(u);
			}
		}

		private void makeEven(Blossom<E> V) {
			V.isEven = true;
			V.delta0 = delta;

			int base = V.base;
			forEachVertexInBlossom(V, v -> {
				blossoms[v].isEven = true;
				find0.union(base, v);
			});
			find0Blossoms[find0.find(base)] = V;
			forEachVertexInBlossom(V, this::insertGrowEventsFromVertex);
			forEachVertexInBlossom(V, this::insertBlossomEventsFromVertex);
		}

		private void expandStep() {
			assert0(delta == expandEvents.findMin().expandDelta);
			final Blossom<E> B = expandEvents.extractMin();

			assert0(B.root != -1 && !B.isEven && !B.isSingleton() && dualVal(B) <= 0);

			int baseFind1Idx = vToFind1Idx[B.base];
			int topFind1Idx = vToFind1Idx[B.treeParentEdge.u()];
			Blossom<E> base = null;
			Blossom<E> top = null;
			// Remove parent pointer from all children, and find the sub blossom containing
			// the base ('base') and the sub blossom containing the vertex of the edge from
			// parent in search tree ('top')
			for (Blossom<E> b = B.child;;) {
				if (b.find1SeqBegin <= baseFind1Idx && baseFind1Idx < b.find1SeqEnd) {
					assert0(base == null);
					base = b;
				}
				if (b.find1SeqBegin <= topFind1Idx && topFind1Idx < b.find1SeqEnd) {
					assert0(top == null);
					top = b;
				}
				b.parent = null;
				b = b.right;
				if (b == B.child)
					break;
			}
			B.deltaOdd += delta - B.delta1;
			B.delta0 = delta;

			// Iterate over sub blossom that should stay in the tree
			boolean left = matched[top.toLeftEdge.u()] == top.toLeftEdge;
			Consumer<Blossom<E>> inBlossom = b -> {
				b.root = B.root;
				b.treeParentEdge = left ? b.toRightEdge : b.toLeftEdge;
				b.deltaOdd = B.deltaOdd;
			};
			Function<Blossom<E>, Blossom<E>> next = b -> left ? b.left : b.right;
			for (Blossom<E> b = top;;) {
				// sub blossom odd
				inBlossom.accept(b);
				b.isEven = false;
				b.delta1 = delta;
				find1Split(b);
				assert0(b.expandHandle == null);
				if (!b.isSingleton()) {
					b.expandDelta = b.z0 / 2 + b.delta1;
					b.expandHandle = expandEvents.insert(b);
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
			for (Blossom<E> b = base;;) {
				b = next.apply(b);
				if (b == top)
					break;
				b.root = -1;
				b.treeParentEdge = null;
				b.isEven = false;
				find1Split(b);
				b.deltaOdd = B.deltaOdd;
				assert0(b.growHandle == null);
				EdgeEvent<E> inEdgeEvent = find1.getKey(find1.findMin(vToFind1Idx[b.base]));
				if (inEdgeEvent != null)
					b.growHandle = growEvents.insert(inEdgeEvent);
			}

			// Disassemble right and left pointers of sub blossoms
			for (Blossom<E> b = top;;) {
				Blossom<E> nextB = b.left;
				b.right = b.left = null;
				b.toRightEdge.val().b0 = b.toRightEdge.val().b1 = null;
				b.toRightEdge.val().twin.val().b0 = b.toRightEdge.val().twin.val().b1 = null;
				b.toLeftEdge.val().b0 = b.toLeftEdge.val().b1 = null;
				b.toLeftEdge.val().twin.val().b0 = b.toLeftEdge.val().twin.val().b1 = null;
				b.toRightEdge = b.toLeftEdge = null;
				if (nextB == top)
					break;
				b = nextB;
			}
		}

		private void augmentStep(Edge<EdgeVal<E>> bridge) {
			Blossom<E> U = topBlossom(bridge.u()), V = topBlossom(bridge.v());
			@SuppressWarnings("unchecked")
			Blossom<E>[] bs = new Blossom[] { U, V };
			for (Blossom<E> b : bs) {

				assert0(b.isEven);
				Edge<EdgeVal<E>> e = null;
				for (int u = b == U ? bridge.u() : bridge.v();;) {
					assert0(b.isEven);
					augmentPath(b, u);
					if (e != null) {
						matched[e.u()] = e;
						matched[e.v()] = e.val().twin;
						assert0(matched[e.u()] != null);
						assert0(matched[e.v()] != null);
					}
					if (b.treeParentEdge == null)
						break;
					// Odd
					b = topBlossom(b.treeParentEdge.v());
					assert0(!b.isEven);
					u = b.treeParentEdge.u();
					augmentPath(b, u);

					// Even
					e = b.treeParentEdge;
					u = e.v();
					b = topBlossom(e.v());
				}
			}
			matched[bridge.u()] = bridge;
			matched[bridge.v()] = bridge.val().twin;
		}

		private void augmentPath(Blossom<E> B, int u) {
			if (B.base == u)
				return;

			Edge<EdgeVal<E>> m = matched[u];
			matched[m.u()] = matched[m.v()] = null;
			int v;
			Blossom<E> b0, b1, b2;
			if (m.u() == u) {
//				u = m.u();
				v = m.v();
				b0 = m.val().b0;
				b1 = m.val().b1;
			} else {
//				u = m.v();
				v = m.u();
				b0 = m.val().b1;
				b1 = m.val().b0;
			}

			Edge<EdgeVal<E>> xy;
			if (b0.right == b1) {
				b2 = b1.right;
				xy = b1.toRightEdge;
			} else {
				assert0(b0.left == b1);
				b2 = b1.left;
				xy = b1.toLeftEdge;
			}

			assert0(b0 != b1);
			assert0(b1 != b2);
			assert0(b2 != b0);
			assert0(b1.base == v);

			augmentPath(b1, xy.u());
			augmentPath(B, xy.v());
			matched[xy.u()] = xy;
			matched[xy.v()] = xy.val().twin;

			assert0(matched[xy.u()].u() == xy.u());
			assert0(matched[xy.u()].v() == xy.v());
			assert0(matched[xy.v()].u() == xy.v());
			assert0(matched[xy.v()].u() == xy.v());

			assert0(matched[b1.base] != null);
			assert0(matched[b2.base] != null);

			for (Blossom<E> p = b0.parent;; p = p.parent) {
				if (p == B.parent)
					break;
				p.base = u;
			}
		}

		private boolean isEven(int v) {
			return blossoms[v].isEven;
		}

		private boolean isInTree(int v) {
			return topBlossom(v).root != -1;
		}

		private boolean isInTree(Blossom<E> b) {
			return b.parent != null ? isInTree(b.base) : b.root != -1;
		}

		private Blossom<E> find0(int v) {
			return find0Blossoms[find0.find(v)];
		}

		private Blossom<E> find1(int v) {
			int idx = vToFind1Idx[v];
			return idx < 0 ? null : find1Blossoms[find1.find(idx)];
		}

		/* Init find1 indexing for all vertices contained in the blossomD */
		private void find1InitIndexing(Blossom<E> b) {
			b.find1SeqBegin = find1IdxNext;
			if (b.child == null) {
				b.isEven = false;
				vToFind1Idx[b.base] = find1IdxNext++;
			} else {
				for (Blossom<E> sub = b.child;;) {
					find1InitIndexing(sub);
					sub = sub.right;
					if (sub == b.child)
						break;
				}
			}
			b.find1SeqEnd = find1IdxNext;
		}

		/* Split a blossom from a bigger blossom in the find1 data structure */
		private void find1Split(Blossom<E> b) {
			int begin = b.find1SeqBegin, end = b.find1SeqEnd;
			Blossom<E> b1 = begin > 0 ? find1Blossoms[find1.find(begin - 1)] : null;
			Blossom<E> b2 = end < find1Blossoms.length ? find1Blossoms[find1.find(end)] : null;

			if (begin > 0) {
				find1.split(begin);
				find1Blossoms[find1.find(begin - 1)] = b1;
			}
			if (end < find1Blossoms.length) {
				find1.split(end);
				find1Blossoms[find1.find(end)] = b2;
			}
			find1Blossoms[find1.find(b.find1SeqBegin)] = b;
		}

		private Blossom<E> topBlossom(int v) {
			return isEven(v) ? find0(v) : find1(v);
		}

		private void forEachBlossom(Consumer<Blossom<E>> f) {
			int n = g.vertices();
			int visitIdx = ++blossomVisitIdx;
			for (int v = 0; v < n; v++) {
				for (Blossom<E> b = blossoms[v]; b.lastVisitIdx != visitIdx; b = b.parent) {
					b.lastVisitIdx = visitIdx;
					f.accept(b);
					if (b.parent == null)
						break;
				}
			}
		}

		private void forEachTopBlossom(Consumer<Blossom<E>> f) {
			int n = g.vertices();
			int visitIdx = ++blossomVisitIdx;
			for (int v = 0; v < n; v++) {
				for (Blossom<E> b = blossoms[v]; b.lastVisitIdx != visitIdx; b = b.parent) {
					b.lastVisitIdx = visitIdx;
					if (b.parent == null) {
						if (b.child != null) {
							// Mark children as visited in case blossom expand
							for (Blossom<E> c = b.child;;) {
								c.lastVisitIdx = visitIdx;
								c = c.left;
								if (c == b.child)
									break;
							}
						}
						f.accept(b);
						break;
					}
				}
			}
		}

		private static <E> void forEachVertexInBlossom(Blossom<E> b, IntConsumer f) {
			if (b.child == null) {
				f.accept(b.base);
				return;
			}
			for (Blossom<E> sub = b.child;;) {
				forEachVertexInBlossom(sub, f);
				sub = sub.right;
				if (sub == b.child)
					break;
			}
		}

		private double dualVal(int v) {
			Blossom<E> b = find1(v);
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

		private double dualVal(Blossom<E> b) {
			assert0(!b.isSingleton());
			double zb = b.z0;
			if (b.parent == null && b.root != -1)
				zb += 2 * (b.isEven ? +(delta - b.delta0) : -(delta - b.delta1));
			return zb;
		}

		private double growEventsKey(EdgeEvent<E> event) {
			int v = event.e.v();
			assert0(!isEven(v));
			return find1(v).deltaOdd + event.slack;
		}

		private void insertGrowEventsFromVertex(int u) {
			double Yu = delta + dualVal(u);
			for (Edge<EdgeVal<E>> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (isEven(v))
					continue;
				double slackBar = Yu + vertexDualValBase[v] - w.weight(e.val().e);
				if (vToGrowEvent[v] == null || slackBar < vToGrowEvent[v].slack) {
					EdgeEvent<E> event = vToGrowEvent[v] = new EdgeEvent<>(e, slackBar);
					if (!find1.decreaseKey(vToFind1Idx[v], event))
						continue;
					assert0(find1.getKey(find1.findMin(vToFind1Idx[v])) == event);

					Blossom<E> V = find1(v);
					if (!isInTree(V)) {
						if (V.growHandle == null)
							V.growHandle = growEvents.insert(event);
						else
							growEvents.decreaseKey(V.growHandle, event);
					}
				}
			}
		}

		private void insertBlossomEventsFromVertex(int u) {
			assert0(isEven(u));
			Blossom<E> U = find0(u);
			double Yu = delta + dualVal(u);
			for (Edge<EdgeVal<E>> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (!isEven(v))
					continue;
				Blossom<E> V = find0(v);
				if (U == V)
					continue;
				double Yv = delta + dualVal(v);
				double slackBar = Yu + Yv - w.weight(e.val().e);

				assert0(slackBar >= 0);
				blossomEvents.insert(new EdgeEvent<>(e, slackBar));
			}
		};

		private static <E> void connectSubBlossoms(Blossom<E> left, Blossom<E> right, Edge<EdgeVal<E>> leftToRightEdge,
				boolean reverse) {
			if (reverse) {
				Blossom<E> temp = left;
				left = right;
				right = temp;
				leftToRightEdge = leftToRightEdge.val().twin;
			}
			left.right = right;
			left.toRightEdge = leftToRightEdge;
			right.left = left;
			right.toLeftEdge = leftToRightEdge.val().twin;
			leftToRightEdge.val().b0 = left;
			leftToRightEdge.val().b1 = right;
			leftToRightEdge.val().twin.val().b0 = right;
			leftToRightEdge.val().twin.val().b1 = left;
		}

		private Blossom<E> lcaInSearchTree(Blossom<E> b1, Blossom<E> b2) {
			int visitIdx = ++blossomVisitIdx;
			for (@SuppressWarnings("unchecked")
			Blossom<E>[] bs = new Blossom[] { b1, b2 };;) {
				if (bs[0] == null && bs[1] == null)
					return null;
				for (int i = 0; i < bs.length; i++) {
					Blossom<E> b = bs[i];
					if (b == null)
						continue;
					if (b.lastVisitIdx == visitIdx)
						return b;
					b.lastVisitIdx = visitIdx;
					bs[i] = b.treeParentEdge == null ? null : topBlossom(b.treeParentEdge.v());
				}
			}
		}

		// TODO check if needed
		@SuppressWarnings("unused")
		private Blossom<E> lcaInBlossomTree(Blossom<E> b1, Blossom<E> b2) {
			int visitIdx = ++blossomVisitIdx;
			for (@SuppressWarnings("unchecked")
			Blossom<E>[] bs = new Blossom[] { b1, b2 };;) {
				if (bs[0] == null && bs[1] == null)
					return null;
				for (int i = 0; i < bs.length; i++) {
					Blossom<E> b = bs[i];
					if (b == null)
						continue;
					if (b.lastVisitIdx == visitIdx)
						return b;
					b.lastVisitIdx = visitIdx;
					bs[i] = b.parent;
				}
			}
		}

		// TODO remove
		private static void assert0(boolean condition) {
			if (!condition)
				throw new InternalError();
		}

	}

}
