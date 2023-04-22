package com.jgalgo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Dial's algorithm for Single Source Shortest Path for positive integer
 * weights.
 * <p>
 * The algorithm runs in {@code O(n + m + D)} where {@code D} is the maximum
 * distance, or the sum of heaviest n-1 edges if the maximum distance is not
 * known. It takes advantage of the fact that a heap for integers can be
 * implemented using buckets, one for each weight. Such a heap require {@code D}
 * buckets, and therefore the algorithm running time and space depends on
 * {@code D}.
 * <p>
 * This algorithm should be used in case the maximal distance is known in
 * advance, and its small. For example, its used by {@link SSSPDial} as a
 * subroutine, where the maximum distance is bounded by the number of layers.
 * <p>
 * Based on 'Algorithm 360: Shortest-Path Forest with Topological Ordering' by
 * Dial, Robert B. (1969).
 *
 * @author Barak Ugav
 */
public class SSSPDial implements SSSP {

	/**
	 * Construct a new SSSP algorithm object.
	 */
	public SSSPDial() {
		allocSizeN = allocSizeM = 0;
		heap = new DialHeap();
	}

	private int allocSizeN;
	private int allocSizeM;
	private int[] edges;
	private final DialHeap heap;
	private DialHeap.Node[] verticesPtrs;

	private void memAlloc(int n, int m) {
		if (allocSizeN < n) {
			verticesPtrs = new DialHeap.Node[n];
			allocSizeN = n;
		}
		m = Math.max(n, m);
		if (allocSizeM < m) {
			edges = new int[m];
			allocSizeM = m;
		}
	}

	private void memClear(int n, int m) {
		Arrays.fill(edges, 0, Math.max(n, m), -1);
		heap.clear();
		Arrays.fill(verticesPtrs, 0, n, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if one of the edge weights is negative or
	 *                                  the weight function is not of type
	 *                                  {@link EdgeWeightFunc.Int}
	 */
	@Override
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		if (!(w instanceof EdgeWeightFunc.Int))
			throw new IllegalArgumentException("only int weights are supported");
		EdgeWeightFunc.Int w0 = (EdgeWeightFunc.Int) w;

		int n = g.vertices().size(), m = g.edges().size();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n, m);

		int maxDistance = 0;
		if (m <= n - 1) {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxDistance += w0.weightInt(e);
			}

		} else {
			int[] edges = g.edges().toArray(this.edges);
			ArraysUtils.getKthElement(edges, 0, g.edges().size(), n - 1,
					(e1, e2) -> -Integer.compare(w0.weightInt(e1), w0.weightInt(e2)), true);

			for (int i = 0; i <= n - 1; i++)
				maxDistance += w0.weightInt(edges[i]);
		}

		SSSP.Result res = computeShortestPaths(g, w0, source, maxDistance);
		memClear(n, m);
		return res;
	}

	/**
	 * Compute the shortest paths from a source to any other vertex in a graph,
	 * given a maximal distance bound.
	 *
	 * @param g           a graph
	 * @param w           an integer edge weight function with non negative values
	 * @param source      a source vertex
	 * @param maxDistance a bound on the maximal distance to any vertex in the graph
	 * @return a result object containing the distances and shortest paths from the
	 *         source to any other vertex
	 * @see #computeShortestPaths(Graph, EdgeWeightFunc, int)
	 */
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc.Int w, int source, int maxDistance) {
		int n = g.vertices().size(), m = g.edges().size();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n, m);

		SSSPResultImpl.Int res = new SSSPResultImpl.Int(g, source);
		DialHeap heap = this.heap;
		DialHeap.Node[] verticesPtrs = this.verticesPtrs;

		for (int v = 0; v < n; v++) {
			res.distances[v] = Integer.MAX_VALUE;
			res.backtrack[v] = -1;
		}
		res.distances[source] = 0;

		heap.init(maxDistance);

		for (int u = source;;) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (res.distances[v] != Integer.MAX_VALUE)
					continue;
				int ws = w.weightInt(e);
				if (ws < 0)
					throw new IllegalArgumentException("negative weights are not supported");
				int distance = res.distances[u] + ws;

				DialHeap.Node vPtr = verticesPtrs[v];
				if (vPtr == null) {
					vPtr = verticesPtrs[v] = heap.insert(distance, v);
					res.backtrack[v] = e;
				} else {
					if (distance < vPtr.distance) {
						heap.decreaseKey(vPtr, distance);
						res.backtrack[v] = e;
					}
				}
			}

			DialHeap.Node next = heap.extractMin();
			if (next == null)
				break;
			res.distances[next.v] = next.distance;
			u = next.v;
		}

		memClear(n, m);
		return res;
	}

	private static class DialHeap {

		private Node[] a;
		private int maxKey;
		private int scanIdx;

		DialHeap() {
			maxKey = -1;
		}

		void init(int maxKey) {
			if (a == null || this.maxKey < maxKey) {
				a = new Node[maxKey];
				this.maxKey = maxKey;
			}
			scanIdx = 0;
		}

		void clear() {
			Node[] a = this.a;
			int maxKey = this.maxKey;
			for (int i = 0; i < maxKey; i++) {
				if (a[i] == null)
					continue;
				for (Node p = a[i];;) {
					Node n = p.next;
					if (n == null)
						break;
					p.next = n.prev = null;
					n = p;
				}
				a[i] = null;
			}
		}

		Node insert(int distance, int v) {
			Node n = new Node(distance, v);
			insertNode(n);
			return n;
		}

		private void insertNode(Node n) {
			Node[] a = this.a;
			Node head = a[n.distance];
			if (head != null) {
				n.next = head;
				head.prev = n;
			}
			a[n.distance] = n;
		}

		void decreaseKey(Node n, int k) {
			Node next = n.next, prev = n.prev;
			if (next != null) {
				next.prev = prev;
				n.next = null;
			}
			if (prev != null) {
				prev.next = next;
				n.prev = null;
			} else
				a[n.distance] = next;
			n.distance = k;
			insertNode(n);
		}

		Node extractMin() {
			Node[] a = this.a;
			Node min = null;
			int i;
			for (i = scanIdx; i < a.length; i++) {
				Node n = a[i];
				if (n != null) {
					Node next = n.next;
					a[i] = next;
					if (next != null)
						n.next = next.prev = null;
					min = n;
					break;
				}
			}
			scanIdx = i;
			return min;
		}

		static class Node {

			int distance;
			final int v;

			Node next;
			Node prev;

			Node(int distance, int v) {
				this.distance = distance;
				this.v = v;
			}
		}

	}

}
