package com.ugav.algo;

import java.util.Arrays;
import java.util.Iterator;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class Dijkstra {

	private Dijkstra() {
	}

	private static final Dijkstra INSTANCE = new Dijkstra();

	public static Dijkstra getInstace() {
		return INSTANCE;
	}

	public <E> double[] calcDistances(Graph<E> g, WeightFunction<E> w, int s) {
		int n = g.vertices();
		double[] distances = new double[n];

		Heap<HeapElm> heap = new HeapFibonacci<>((a, b) -> Double.compare(a.distance, b.distance));
		@SuppressWarnings("unchecked")
		Heap.Handle<HeapElm>[] verticesPtrs = new Heap.Handle[n];

		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		distances[s] = 0;

		for (int u = s;;) {
			for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();) {
				Edge<E> e = it.next();
				int v = e.v();
				if (distances[v] != Double.POSITIVE_INFINITY)
					continue;
				double distance = distances[u] + w.weight(e);

				Heap.Handle<HeapElm> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					vPtr = verticesPtrs[v] = heap.insert(new HeapElm(v, distance));
				} else {
					HeapElm ptr = vPtr.get();
					if (distance < ptr.distance) {
						ptr.distance = distance;
						heap.decreaseKey(vPtr, ptr);
					}
				}
			}

			if (heap.isEmpty())
				break;
			HeapElm next = heap.extractMin();
			distances[u = next.v] = next.distance;
		}

		return distances;
	}

	private static class HeapElm {

		final int v;
		double distance;

		HeapElm(int v, double distance) {
			this.v = v;
			this.distance = distance;
		}

	}

}
