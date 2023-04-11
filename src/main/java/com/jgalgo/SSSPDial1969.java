package com.jgalgo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntIterator;

public class SSSPDial1969 implements SSSP {

	/*
	 * edges) O(m + D) where D is the maximum distance, or the sum of heaviest n-1
	 * edges if the maximum distance is not known
	 */

	public SSSPDial1969() {
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

	@Override
	public SSSP.Result calcDistances(Graph g, EdgeWeightFunc w0, int source) {
		if (!(w0 instanceof EdgeWeightFunc.Int))
			throw new IllegalArgumentException("only int weights are supported");
		EdgeWeightFunc.Int w = (EdgeWeightFunc.Int) w0;

		int n = g.vertices().size(), m = g.edges().size();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n, m);

		int maxDistance = 0;
		if (m <= n - 1) {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxDistance += w.weightInt(e);
			}

		} else {
			int[] edges = g.edges().toArray(this.edges);
			ArraysUtils.getKthElement(edges, 0, g.edges().size(), n - 1,
					(e1, e2) -> -Integer.compare(w.weightInt(e1), w.weightInt(e2)), true);

			for (int i = 0; i <= n - 1; i++)
				maxDistance += w.weightInt(edges[i]);
		}

		SSSP.Result res = calcDistances(g, w, source, maxDistance);
		memClear(n, m);
		return res;
	}

	public SSSP.Result calcDistances(Graph g, EdgeWeightFunc.Int w, int source, int maxDistance) {
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
