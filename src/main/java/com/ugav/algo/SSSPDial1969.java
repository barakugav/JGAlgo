package com.ugav.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

public class SSSPDial1969 implements SSSP {

	/*
	 * edges) O(m + D) where D is the maximum distance, or the sum of heaviest n-1
	 * edges if the maximum distance is not known
	 */

	public SSSPDial1969() {
		allocSizeN = allocSizeM = 0;
		heap = new DialHeap<>();
	}

	private int allocSizeN;
	private int allocSizeM;
	@SuppressWarnings("rawtypes")
	private Edge[] edges;
	private int[] distances;
	@SuppressWarnings("rawtypes")
	private final DialHeap heap;
	@SuppressWarnings("rawtypes")
	private DialHeap.Node[] verticesPtrs;

	private void memAlloc(int n, int m) {
		if (allocSizeN < n) {
			distances = new int[n];
			verticesPtrs = new DialHeap.Node[n];
			allocSizeN = n;
		}
		m = Math.max(n, m);
		if (allocSizeM < m) {
			edges = new Edge[m];
			allocSizeM = m;
		}
	}

	private void memClear(int n, int m) {
		java.util.Arrays.fill(edges, 0, Math.max(n, m), null);
		java.util.Arrays.fill(distances, 0, n, 0);
		heap.clear();
		java.util.Arrays.fill(verticesPtrs, 0, n, null);
	}

	@Override
	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunction<E> w0, int source) {
		if (!(w0 instanceof WeightFunctionInt<?>))
			throw new IllegalArgumentException("only int weights are supported");
		WeightFunctionInt<E> w = (WeightFunctionInt<E>) w0;

		int n = g.vertices(), m = g.edges().size();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n, m);

		int maxDistance = 0;
		if (g.edges().size() <= n - 1) {
			for (Edge<E> edge : g.edges())
				maxDistance += w.weightInt(edge);

		} else {
			@SuppressWarnings("unchecked")
			Edge<E>[] edges = g.edges().toArray(this.edges);
			Edge<E> pivot = Arrays.getKthElement(edges, n - 1,
					(e1, e2) -> -Integer.compare(w.weightInt(e1), w.weightInt(e2)));
			int weightThreshold = w.weightInt(pivot);

			for (Edge<E> edge : edges) {
				int ew = w.weightInt(edge);
				if (ew >= weightThreshold)
					maxDistance += ew;
			}
		}

		SSSP.Result<E> res = calcDistances(g, w, source, maxDistance);
		memClear(n, m);
		return res;
	}

	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunctionInt<E> w, int source, int maxDistance) {
		int n = g.vertices(), m = g.edges().size();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n, m);

		int[] distances = this.distances;
		@SuppressWarnings("unchecked")
		Edge<E>[] backtrack = this.edges;
		@SuppressWarnings("unchecked")
		DialHeap<E> heap = this.heap;
		@SuppressWarnings("unchecked")
		DialHeap.Node<E>[] verticesPtrs = this.verticesPtrs;

		java.util.Arrays.fill(distances, 0, n, Integer.MAX_VALUE);
		java.util.Arrays.fill(backtrack, 0, n, null);
		distances[source] = 0;

		heap.init(maxDistance);

		for (int u = source;;) {
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (distances[v] != Integer.MAX_VALUE)
					continue;
				int ws = w.weightInt(e);
				if (ws < 0)
					throw new IllegalArgumentException("negative weights are not supported");
				int distance = distances[u] + ws;

				DialHeap.Node<E> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					vPtr = verticesPtrs[v] = heap.insert(distance, e);
				} else {
					if (distance < vPtr.distance) {
						vPtr.backtrack = e;
						heap.decreaseKey(vPtr, distance);
					}
				}
			}

			DialHeap.Node<E> next = heap.extractMin();
			if (next == null)
				break;
			distances[u = next.backtrack.v()] = next.distance;
			backtrack[u] = next.backtrack;
		}

		SSSP.Result<E> res = new Result<>(java.util.Arrays.copyOf(distances, n), java.util.Arrays.copyOf(backtrack, n));
		memClear(n, m);
		return res;
	}

	private static class DialHeap<E> {

		private Node<E>[] a;
		private int maxKey;
		private int scanIdx;

		DialHeap() {
			maxKey = -1;
		}

		@SuppressWarnings("unchecked")
		void init(int maxKey) {
			if (a == null || this.maxKey < maxKey) {
				a = new Node[maxKey];
				this.maxKey = maxKey;
			}
			scanIdx = 0;
		}

		void clear() {
			Node<E>[] a = this.a;
			int maxKey = this.maxKey;
			for (int i = 0; i < maxKey; i++) {
				if (a[i] == null)
					continue;
				for (Node<E> p = a[i];;) {
					p.backtrack = null;
					Node<E> n = p.next;
					if (n == null)
						break;
					p.next = n.prev = null;
					n = p;
				}
				a[i] = null;
			}
		}

		Node<E> insert(int distance, Edge<E> backtrack) {
			Node<E> n = new Node<>(distance, backtrack);
			insertNode(n);
			return n;
		}

		private void insertNode(Node<E> n) {
			Node<E>[] a = this.a;
			Node<E> head = a[n.distance];
			if (head != null) {
				n.next = head;
				head.prev = n;
			}
			a[n.distance] = n;
		}

		void decreaseKey(Node<E> n, int k) {
			Node<E> next = n.next, prev = n.prev;
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

		Node<E> extractMin() {
			Node<E>[] a = this.a;
			Node<E> min = null;
			int i;
			for (i = scanIdx; i < a.length; i++) {
				Node<E> n = a[i];
				if (n != null) {
					Node<E> next = n.next;
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

		static class Node<E> {

			int distance;
			Edge<E> backtrack;

			Node<E> next;
			Node<E> prev;

			Node(int distance, Edge<E> backtrack) {
				this.distance = distance;
				this.backtrack = backtrack;
			}
		}

	}

	private static class Result<E> implements SSSP.Result<E> {

		private final int[] distances;
		private final Edge<E>[] backtrack;

		Result(int[] distances, Edge<E>[] backtrack) {
			this.distances = distances;
			this.backtrack = backtrack;
		}

		@Override
		public double distance(int v) {
			int d = distances[v];
			return d != Integer.MAX_VALUE ? d : Double.POSITIVE_INFINITY;
		}

		@Override
		public List<Edge<E>> getPathTo(int v) {
			if (distances[v] == Integer.MAX_VALUE)
				return null;
			List<Edge<E>> path = new ArrayList<>();
			for (;;) {
				Edge<E> e = backtrack[v];
				if (e == null)
					break;
				path.add(e);
				v = e.u();
			}
			Collections.reverse(path);
			return path;
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public List<Edge<E>> getNegativeCycle() {
			throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return java.util.Arrays.toString(distances);
		}

	}

}
