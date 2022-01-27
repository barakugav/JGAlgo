package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

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
		final Graph<E> g;

		/* the weight function */
		final WeightFunction<E> w;

		/* vertex -> matched edge */
		final Edge<E>[] matched;

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
			Edge<E> toLeftEdge;

			/*
			 * the edge that connected this blossom and it's right brother, null if right is
			 * null
			 */
			Edge<E> toRightEdge;

			/*
			 * index of root vertex in the search tree, -1 if this blossom is out. relevant
			 * only for top blossoms
			 */
			int root;

			/* edge that connect this blossom to the parent blossom in the search tree */
			Edge<E> treeParentEdge;

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

		private static class EdgeEvent<E> {
			final Edge<E> e;
			final double slack;

			EdgeEvent(Edge<E> e, double slack) {
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
			this.g = g;
			this.w = w;

			int n = g.vertices();
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
			for (Edge<E> e : g.edges())
				maxWeight = Math.max(maxWeight, w.weight(e));
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
						Edge<E> e = blossomEvents.extractMin().e;
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
					res.add(matched[u]);
			return res;
		}

		private void growStep() {
			// Grow step
			assert0(delta == growEventsKey(growEvents.findMin()));
			Edge<E> e = growEvents.extractMin().e;

			Blossom<E> U = find0(e.u()), V = find1(e.v());
			assert0(!V.isEven && !isInTree(V));

			// Add odd vertex
			V.root = U.root;
			V.treeParentEdge = e.twin();
			V.isEven = false;
			V.delta1 = delta;
			assert0(V.growHandle.get().e == e);
			V.growHandle = null;
			if (!V.isSingleton()) {
				V.expandDelta = V.z0 / 2 + V.delta1;
				V.expandHandle = expandEvents.insert(V);
			}

			// Immediately add it's matched edge and vertex as even vertex
			Edge<E> matchedEdge = matched[V.base];
			V = topBlossom(matchedEdge.v());
			V.root = U.root;
			V.treeParentEdge = matchedEdge.twin();
			if (V.growHandle != null) {
				growEvents.removeHandle(V.growHandle);
				V.growHandle = null;
			}
			makeEven(V);
		}

		private void blossomStep(Edge<E> e) {
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
			for (Blossom<E> p : bs) {
				boolean prevIsRight = p == U;
				Blossom<E> prev = p == U ? V : U;
				Edge<E> toPrevEdge = p == U ? e : e.twin();

				while (true) {
					// handle even sub blossom
					assert0(p.isEven);
					if (!p.isSingleton())
						p.z0 = dualVal(p);
					p.parent = newb;
					connectSubBlossoms(p, prev, toPrevEdge, !prevIsRight);
					unionQueue.push(p.base);

					if (p == base)
						break;
					prev = p;
					toPrevEdge = matched[p.base].twin();
					assert0(matched[p.base] == p.treeParentEdge);
					p = topBlossom(toPrevEdge.u());

					// handle odd vertex
					assert0(!p.isEven);
					p.deltaOdd += delta - p.delta1;
					if (!p.isSingleton())
						p.z0 = dualVal(p);
					p.parent = newb;
					connectSubBlossoms(p, prev, toPrevEdge, !prevIsRight);
					forEachVertexInBlossom(p, v -> {
						blossoms[v].isEven = true;
						unionQueue.push(v);
						scanQueue.push(v);
					});
					p.delta0 = delta;
					if (!p.isSingleton()) {
						expandEvents.removeHandle(p.expandHandle);
						p.expandHandle = null;
					}

					prev = p;
					toPrevEdge = p.treeParentEdge.twin();
					p = topBlossom(toPrevEdge.u());
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
			// TODO this function can be implemented MUCH faster.

			assert0(delta == expandEvents.findMin().expandDelta);
			Blossom<E> topBlossom = expandEvents.extractMin();

			assert0(topBlossom.root != -1 && !topBlossom.isEven && !topBlossom.isSingleton()
					&& dualVal(topBlossom) <= 0);

			// Remove parent pointer from all children
			for (Blossom<E> p = topBlossom.child;;) {
				p.parent = null;
				p = p.right;
				if (p == topBlossom.child)
					break;
			}
			final Blossom<E> b = subBlossom(topBlossom.treeParentEdge.u(), null);
			final Blossom<E> subBase = subBlossom(topBlossom.base, null);
			topBlossom.deltaOdd += delta - topBlossom.delta1;
			topBlossom.delta0 = delta;

			// Iterate over sub blossom that should stay in the tree
			boolean left = matched[b.toLeftEdge.u()] == b.toLeftEdge;
			for (Blossom<E> p = b;;) {
				// sub blossom odd
				p.root = topBlossom.root;
				p.treeParentEdge = left ? p.toRightEdge : p.toLeftEdge;
				p.isEven = false;
				p.delta1 = delta;
				p.deltaOdd = topBlossom.deltaOdd;
				find1Split(p);
				assert0(p.expandHandle == null);
				if (!p.isSingleton()) {
					p.expandDelta = p.z0 / 2 + p.delta1;
					p.expandHandle = expandEvents.insert(p);
				}
				if (p == subBase)
					break;
				p = left ? p.left : p.right;

				// sub blossom even
				p.root = topBlossom.root;
				p.treeParentEdge = left ? p.toRightEdge : p.toLeftEdge;
				p.deltaOdd = topBlossom.deltaOdd;
				makeEven(p);
				p = left ? p.left : p.right;
			}
			b.treeParentEdge = topBlossom.treeParentEdge;
			topBlossom.root = -1;

			// Iterate over sub blossom that should not stay in the tree
			for (Blossom<E> p = subBase;;) {
				p = left ? p.left : p.right;
				if (p == b)
					break;
				p.root = -1;
				p.treeParentEdge = null;
				p.isEven = false;
				find1Split(p);
				p.deltaOdd = topBlossom.deltaOdd;
				assert0(p.growHandle == null);
				EdgeEvent<E> inEdgeEvent = find1.getKey(find1.findMin(vToFind1Idx[p.base]));
				if (inEdgeEvent != null)
					p.growHandle = growEvents.insert(inEdgeEvent);

				p = left ? p.left : p.right;
				assert0(p != b);
				p.root = -1;
				p.treeParentEdge = null;
				p.isEven = false;
				find1Split(p);
				p.deltaOdd = topBlossom.deltaOdd;
				assert0(p.growHandle == null);
				inEdgeEvent = find1.getKey(find1.findMin(vToFind1Idx[p.base]));
				if (inEdgeEvent != null)
					p.growHandle = growEvents.insert(inEdgeEvent);
			}
			for (Blossom<E> p = b;;) {
				Blossom<E> next = p.left;
				p.right = p.left = null;
				p.toRightEdge = p.toLeftEdge = null;
				if (next == b)
					break;
				p = next;
			}
		}

		private void augmentStep(Edge<E> bridge) {
			// TODO this function can be implemented MUCH faster.

			Blossom<E> U = topBlossom(bridge.u()), V = topBlossom(bridge.v());
			@SuppressWarnings("unchecked")
			Blossom<E>[] bs = new Blossom[] { U, V };
			for (Blossom<E> b : bs) {

				assert0(b.isEven);
				Edge<E> e = null;
				for (int u = b == U ? bridge.u() : bridge.v();;) {
					assert0(b.isEven);
					augmentPath(b, u);
					if (e != null) {
						matched[e.u()] = e;
						matched[e.v()] = e.twin();
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
			matched[bridge.v()] = bridge.twin();
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

		private Blossom<E> subBlossom(int v, Blossom<E> parent) {
			// TODO remove
			assert0(blossoms[v] != null);
			return subBlossom(blossoms[v], parent);
		}

		private static <E> Blossom<E> subBlossom(Blossom<E> b, Blossom<E> parent) {
			while (b.parent != parent)
				b = b.parent;
			return b;
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
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (isEven(v))
					continue;
				double slackBar = Yu + vertexDualValBase[v] - w.weight(e);
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
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (!isEven(v))
					continue;
				Blossom<E> V = find0(v);
				if (U == V)
					continue;
				double Yv = delta + dualVal(v);
				double slackBar = Yu + Yv - w.weight(e);

				assert0(slackBar >= 0);
				blossomEvents.insert(new EdgeEvent<>(e, slackBar));
			}
		};

		private void augmentPath(Blossom<E> b, int u) {
			if (b.base == u)
				return;

			final Blossom<E> b0 = subBlossom(u, b);
			final Blossom<E> bk = subBlossom(b.base, b);
			boolean left = matched[b0.toLeftEdge.u()] == b0.toLeftEdge;

			int newBase = u;
			Edge<E> e = null;
			for (Blossom<E> p = b0;;) {
				augmentPath(p, u);
				if (e != null) {
					matched[e.u()] = e;
					matched[e.v()] = e.twin();
				}
				if (p == bk) {
					b.base = newBase;
					return;
				}
				if (left) {
					p = p.left;
					u = p.toLeftEdge.u();
				} else {
					p = p.right;
					u = p.toRightEdge.u();
				}
				assert0(p != bk);
				augmentPath(p, u);

				if (left) {
					e = p.toLeftEdge;
					u = p.toLeftEdge.v();
					p = p.left;
				} else {
					e = p.toRightEdge;
					u = p.toRightEdge.v();
					p = p.right;
				}
			}
		}

		// TODO check if needed
		@SuppressWarnings("unused")
		private List<Edge<E>> findAugPath(Blossom<E> b, int u) {
			if (b.base == u)
				return new ArrayList<>(0);
			final Blossom<E> b0 = subBlossom(u, b);
			final Blossom<E> bk = subBlossom(b.base, b);
			boolean left = matched[b0.toLeftEdge.u()] == b0.toLeftEdge;
			List<Edge<E>> path = new ArrayList<>();
			for (Blossom<E> p = b0;;) {
				path.addAll(findAugPath(p, u));
				if (p == bk)
					return path;
				if (left) {
					path.add(p.toLeftEdge);
					p = p.left;
					u = p.toLeftEdge.u();
				} else {
					path.add(p.toRightEdge);
					p = p.right;
					u = p.toRightEdge.u();
				}
				assert0(p != bk);
				path.addAll(reverse(findAugPath(p, u)));
				if (left) {
					path.add(p.toLeftEdge);
					u = p.toLeftEdge.v();
					p = p.left;
				} else {
					path.add(p.toRightEdge);
					u = p.toRightEdge.v();
					p = p.right;
				}
			}
		}

		private static <E> List<E> reverse(List<E> l) {
			Collections.reverse(l);
			return l;
		}

		private static <E> void connectSubBlossoms(Blossom<E> left, Blossom<E> right, Edge<E> leftToRightEdge,
				boolean reverse) {
			if (reverse) {
				Blossom<E> temp = left;
				left = right;
				right = temp;
				leftToRightEdge = leftToRightEdge.twin();
			}
			left.right = right;
			left.toRightEdge = leftToRightEdge;
			right.left = left;
			right.toLeftEdge = leftToRightEdge.twin();
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
