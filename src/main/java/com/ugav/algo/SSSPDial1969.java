package com.ugav.algo;

import java.util.Arrays;
import java.util.Collections;

import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

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
	private int[] distances;
	private final DialHeap heap;
	private DialHeap.Node[] verticesPtrs;

	private void memAlloc(int n, int m) {
		if (allocSizeN < n) {
			distances = new int[n];
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
		Arrays.fill(distances, 0, n, 0);
		heap.clear();
		Arrays.fill(verticesPtrs, 0, n, null);
	}

	@Override
	public SSSP.Result calcDistances(Graph g, WeightFunction w0, int source) {
		if (!(w0 instanceof WeightFunctionInt))
			throw new IllegalArgumentException("only int weights are supported");
		WeightFunctionInt w = (WeightFunctionInt) w0;

		int n = g.verticesNum(), m = g.edgesNum();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n, m);

		int maxDistance = 0;
		if (m <= n - 1) {
			for (int e = 0; e < m; e++)
				maxDistance += w.weightInt(e);

		} else {
			int[] edges = this.edges;
			for (int e = 0; e < m; e++)
				edges[e] = e;

			Array.Int.getKthElement(edges, 0, g.edgesNum(), n - 1,
					(e1, e2) -> -Integer.compare(w.weightInt(e1), w.weightInt(e2)), true);

			for (int i = 0; i <= n - 1; i++)
				maxDistance += w.weightInt(edges[i]);
		}

		SSSP.Result res = calcDistances(g, w, source, maxDistance);
		memClear(n, m);
		return res;
	}

	public SSSP.Result calcDistances(Graph g, WeightFunctionInt w, int source, int maxDistance) {
		int n = g.verticesNum(), m = g.edgesNum();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n, m);

		int[] distances = this.distances;
		int[] backtrack = this.edges;
		DialHeap heap = this.heap;
		DialHeap.Node[] verticesPtrs = this.verticesPtrs;

		Arrays.fill(distances, 0, n, Integer.MAX_VALUE);
		Arrays.fill(backtrack, 0, n, -1);
		distances[source] = 0;

		heap.init(maxDistance);

		for (int u = source;;) {
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (distances[v] != Integer.MAX_VALUE)
					continue;
				int ws = w.weightInt(e);
				if (ws < 0)
					throw new IllegalArgumentException("negative weights are not supported");
				int distance = distances[u] + ws;

				DialHeap.Node vPtr = verticesPtrs[v];
				if (vPtr == null) {
					vPtr = verticesPtrs[v] = heap.insert(distance, e, v);
				} else {
					if (distance < vPtr.distance) {
						vPtr.backtrack = e;
						heap.decreaseKey(vPtr, distance);
					}
				}
			}

			DialHeap.Node next = heap.extractMin();
			if (next == null)
				break;
			distances[u = next.v] = next.distance;
			backtrack[u] = next.backtrack;
		}

		SSSP.Result res = new Result(g, Arrays.copyOf(distances, n), Arrays.copyOf(backtrack, n));
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

		Node insert(int distance, int backtrack, int v) {
			Node n = new Node(distance, backtrack, v);
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
			int backtrack;
			final int v;

			Node next;
			Node prev;

			Node(int distance, int backtrack, int v) {
				this.distance = distance;
				this.backtrack = backtrack;
				this.v = v;
			}
		}

	}

	private static class Result implements SSSP.Result {

		private final Graph g;
		private final int[] distances;
		private final int[] backtrack;

		Result(Graph g, int[] distances, int[] backtrack) {
			this.g = g;
			this.distances = distances;
			this.backtrack = backtrack;
		}

		@Override
		public double distance(int v) {
			int d = distances[v];
			return d != Integer.MAX_VALUE ? d : Double.POSITIVE_INFINITY;
		}

		@Override
		public IntList getPathTo(int v) {
			if (distances[v] == Integer.MAX_VALUE)
				return null;
			IntList path = new IntArrayList();
			for (;;) {
				int e = backtrack[v];
				if (e == -1)
					break;
				path.add(e);
				v = g.edgeEndpoint(e, v);
			}
			Collections.reverse(path); // TODO
			return path;
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public IntList getNegativeCycle() {
			throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return Arrays.toString(distances);
		}

	}

}
