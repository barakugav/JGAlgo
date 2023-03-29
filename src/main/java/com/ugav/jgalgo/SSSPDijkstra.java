package com.ugav.jgalgo;

import java.util.Arrays;

public class SSSPDijkstra implements SSSP {

	/*
	 * O(m + n log n)
	 */

	private int allocSize;
	private final HeapDirectAccessed<HeapElm> heap;
	private HeapDirectAccessed.Handle<HeapElm>[] verticesPtrs;

	public SSSPDijkstra() {
		this(HeapPairing::new);
	}

	public SSSPDijkstra(HeapDirectAccessed.Builder heapBuilder) {
		allocSize = 0;
		heap = heapBuilder.build((a, b) -> Utils.compare(a.distance, b.distance));
	}

	@SuppressWarnings("unchecked")
	private void memAlloc(int n) {
		if (allocSize < n) {
			verticesPtrs = new HeapDirectAccessed.Handle[n];
			allocSize = n;
		}
	}

	private void memClear(int n) {
		heap.clear();
		Arrays.fill(verticesPtrs, 0, n, null);
	}

	@Override
	public SSSP.Result calcDistances(Graph g, EdgeWeightFunc w, int source) {
		int n = g.vertices().size();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n);
		HeapDirectAccessed<HeapElm> heap = this.heap;
		HeapDirectAccessed.Handle<HeapElm>[] verticesPtrs = this.verticesPtrs;

		SSSPResultImpl res = new SSSPResultImpl(g);
		res.distances[source] = 0;

		for (int u = source;;) {
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (res.distances[v] != Double.POSITIVE_INFINITY)
					continue;
				double ws = w.weight(e);
				if (ws < 0)
					throw new IllegalArgumentException("negative weights are not supported");
				double distance = res.distances[u] + ws;

				HeapDirectAccessed.Handle<HeapElm> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					verticesPtrs[v] = heap.insert(new HeapElm(distance, e, v));
				} else {
					HeapElm ptr = vPtr.get();
					if (distance < ptr.distance) {
						ptr.distance = distance;
						ptr.backtrack = e;
						heap.decreaseKey(vPtr, ptr);
					}
				}
			}

			if (heap.isEmpty())
				break;
			HeapElm next = heap.extractMin();
			res.distances[u = next.v] = next.distance;
			res.backtrack[u] = next.backtrack;
		}

		memClear(n);
		return res;
	}

	private static class HeapElm {

		double distance;
		int backtrack;
		final int v;

		HeapElm(double distance, int backtrack, int v) {
			this.distance = distance;
			this.backtrack = backtrack;
			this.v = v;
		}

	}

}
