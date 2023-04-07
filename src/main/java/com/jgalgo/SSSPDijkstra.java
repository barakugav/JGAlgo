package com.jgalgo;

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
		heap = heapBuilder.build();
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

		memAlloc(n);
		HeapDirectAccessed<HeapElm> heap = this.heap;
		HeapDirectAccessed.Handle<HeapElm>[] verticesPtrs = this.verticesPtrs;

		SSSPResultImpl res = new SSSPResultImpl(g, source);
		res.distances[source] = 0;

		for (int u = source;;) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
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
					verticesPtrs[v] = heap.insert(new HeapElm(distance, v));
					res.backtrack[v] = e;
				} else {
					HeapElm ptr = vPtr.get();
					if (distance < ptr.distance) {
						ptr.distance = distance;
						res.backtrack[v] = e;
						heap.decreaseKey(vPtr, ptr);
					}
				}
			}

			if (heap.isEmpty())
				break;
			HeapElm next = heap.extractMin();
			res.distances[next.v] = next.distance;
			u = next.v;
		}

		memClear(n);
		return res;
	}

	private static class HeapElm implements Comparable<HeapElm> {

		double distance;
		final int v;

		HeapElm(double distance, int v) {
			this.distance = distance;
			this.v = v;
		}

		@Override
		public int compareTo(HeapElm o) {
			return Double.compare(distance, o.distance);
		}

	}

}
