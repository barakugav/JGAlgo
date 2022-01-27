package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class SSSPDijkstra implements SSSP {

	/*
	 * O(m + nlogn)
	 */

	private SSSPDijkstra() {
	}

	private static final SSSPDijkstra INSTANCE = new SSSPDijkstra();

	public static SSSPDijkstra getInstace() {
		return INSTANCE;
	}

	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunction<E> w, int source) {
		int n = g.vertices();
		double[] distances = new double[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] backtrack = new Edge[n];

		if (n == 0)
			return new SSSPResultsImpl<>(distances, backtrack);

		Heap<HeapElm<E>> heap = new HeapFibonacci<>((a, b) -> Utils.compare(a.distance, b.distance));
		@SuppressWarnings("unchecked")
		Heap.Handle<HeapElm<E>>[] verticesPtrs = new Heap.Handle[n];

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

				Heap.Handle<HeapElm<E>> vPtr = verticesPtrs[v];
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

		return new SSSPResultsImpl<>(distances, backtrack);
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
