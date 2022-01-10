package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

public class SSSPDial1969 implements SSSP {

	/*
	 * O(m + D) where D is the sum of all edges (can be improved to sum of max n
	 * edges)
	 */

	private SSSPDial1969() {
	}

	private static final SSSPDial1969 INSTANCE = new SSSPDial1969();

	public static SSSPDial1969 getInstace() {
		return INSTANCE;
	}

	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunction<E> w0, int source) {
		if (!(w0 instanceof WeightFunctionInt<?>))
			throw new IllegalArgumentException("only int weights are supported");
		WeightFunctionInt<E> w = (WeightFunctionInt<E>) w0;
		int maxDistance = 0;
		// TODO possible to take only heaviest n-1 edges
		for (Edge<E> e : g.edges()) {
			int weight = w.weightInt(e);
			if (maxDistance + weight < maxDistance)
				throw new IllegalArgumentException("overflow");
			maxDistance += weight;
		}
		return calcDistances(g, w, source, maxDistance);
	}

	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunctionInt<E> w, int source, int maxDistance) {
		int n = g.vertices();
		int[] distances = new int[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] backtrack = new Edge[n];

		if (n == 0)
			return new Result<>(distances, backtrack);

		DialHeap<E> heap = new DialHeap<>(maxDistance);
		@SuppressWarnings("unchecked")
		DialHeap.Node<E>[] verticesPtrs = new DialHeap.Node[n];

		Arrays.fill(distances, Integer.MAX_VALUE);
		distances[source] = 0;

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
					(vPtr = verticesPtrs[v] = heap.insert(v, distance)).backtrack = e;
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
			distances[u = next.v] = next.distance;
			backtrack[u] = next.backtrack;
		}

		return new Result<>(distances, backtrack);
	}

	private static class DialHeap<E> {

		private final Node<E>[] a;
		private int scanIdx;

		@SuppressWarnings("unchecked")
		DialHeap(int maxKey) {
			a = new Node[maxKey];
			scanIdx = 0;
		}

		Node<E> insert(int v, int key) {
			Node<E> n = new Node<E>(v, key);
			insertNode(n);
			return n;
		}

		private void insertNode(Node<E> n) {
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
			final int v;
			Edge<E> backtrack;

			Node<E> next;
			Node<E> prev;

			Node(int v, int key) {
				this.distance = key;
				this.v = v;
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

	}

}
