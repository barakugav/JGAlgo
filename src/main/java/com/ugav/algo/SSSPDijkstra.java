package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class SSSPDijkstra implements SSSP {

	/*
	 * O(m + nlogn)
	 */

	private int allocSize;
	private double[] distances;
	@SuppressWarnings("rawtypes")
	private Edge[] backtrack;
	@SuppressWarnings("rawtypes")
	private final Heap<HeapElm> heap;
	@SuppressWarnings("rawtypes")
	private HeapDirectAccessed.Handle<HeapElm>[] verticesPtrs;

	public SSSPDijkstra() {
		allocSize = 0;
		heap = new HeapFibonacci<>((a, b) -> Utils.compare(a.distance, b.distance));
	}

	@SuppressWarnings("unchecked")
	private void memAlloc(int n) {
		if (allocSize < n) {
			distances = new double[n];
			backtrack = new Edge[n];
			verticesPtrs = new HeapDirectAccessed.Handle[n];
			allocSize = n;
		}
	}

	private void memClear(int n) {
		Arrays.fill(distances, 0, n, 0);
		Arrays.fill(backtrack, 0, n, null);
		heap.clear();
		Arrays.fill(verticesPtrs, 0, n, null);
	}

	@Override
	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunction<E> w, int source) {
		int n = g.vertices();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n);
		double[] distances = this.distances;
		@SuppressWarnings("unchecked")
		Edge<E>[] backtrack = this.backtrack;
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HeapDirectAccessed<HeapElm<E>> heap = (HeapDirectAccessed) this.heap;
		@SuppressWarnings("unchecked")
		HeapDirectAccessed.Handle<HeapElm<E>>[] verticesPtrs = (HeapDirectAccessed.Handle[]) this.verticesPtrs;

		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		distances[source] = 0;

		for (int u = source;;) {
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (distances[v] != Double.POSITIVE_INFINITY)
					continue;
				double ws = w.weight(e);
				if (ws < 0)
					throw new IllegalArgumentException("negative weights are not supported");
				double distance = distances[u] + ws;

				HeapDirectAccessed.Handle<HeapElm<E>> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					vPtr = verticesPtrs[v] = heap.insert(new HeapElm<>(distance, e));
				} else {
					HeapElm<E> ptr = vPtr.get();
					if (distance < ptr.distance) {
						ptr.distance = distance;
						ptr.backtrack = e;
						heap.decreaseKey(vPtr, ptr);
					}
				}
			}

			if (heap.isEmpty())
				break;
			HeapElm<E> next = heap.extractMin();
			distances[u = next.backtrack.v()] = next.distance;
			backtrack[u] = next.backtrack;
		}

		SSSP.Result<E> res = new SSSPResultsImpl<>(Arrays.copyOf(distances, n), Arrays.copyOf(backtrack, n));
		memClear(n);
		return res;
	}

	private static class HeapElm<E> {

		double distance;
		Edge<E> backtrack;

		HeapElm(double distance, Edge<E> backtrack) {
			this.distance = distance;
			this.backtrack = backtrack;
		}

	}

}
