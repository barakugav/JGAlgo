package com.ugav.jgalgo;

import java.util.Arrays;

public class SSSPDijkstra implements SSSP {

	/*
	 * O(m + n log n)
	 */

	private int allocSize;
	private double[] distances;
	private int[] backtrack;
	private final Heap<HeapElm> heap;
	private HeapDirectAccessed.Handle<HeapElm>[] verticesPtrs;

	public SSSPDijkstra() {
		allocSize = 0;
		heap = new HeapFibonacci<>((a, b) -> Utils.compare(a.distance, b.distance));
	}

	@SuppressWarnings("unchecked")
	private void memAlloc(int n) {
		if (allocSize < n) {
			distances = new double[n];
			backtrack = new int[n];
			verticesPtrs = new HeapDirectAccessed.Handle[n];
			allocSize = n;
		}
	}

	private void memClear(int n) {
		Arrays.fill(distances, 0, n, 0);
		Arrays.fill(backtrack, 0, n, -1);
		heap.clear();
		Arrays.fill(verticesPtrs, 0, n, null);
	}

	@Override
	public SSSP.Result calcDistances(Graph g, EdgeWeightFunc w, int source) {
		int n = g.vertices().size();
		if (n <= 0)
			throw new IllegalArgumentException();

		memAlloc(n);
		double[] distances = this.distances;
		int[] backtrack = this.backtrack;
		Arrays.fill(backtrack, 0, n, -1);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HeapDirectAccessed<HeapElm> heap = (HeapDirectAccessed) this.heap;
		HeapDirectAccessed.Handle<HeapElm>[] verticesPtrs = this.verticesPtrs;

		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		distances[source] = 0;

		for (int u = source;;) {
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (distances[v] != Double.POSITIVE_INFINITY)
					continue;
				double ws = w.weight(e);
				if (ws < 0)
					throw new IllegalArgumentException("negative weights are not supported");
				double distance = distances[u] + ws;

				HeapDirectAccessed.Handle<HeapElm> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					vPtr = verticesPtrs[v] = heap.insert(new HeapElm(distance, e, v));
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
			distances[u = next.v] = next.distance;
			backtrack[u] = next.backtrack;
		}

		SSSP.Result res = new SSSPResultsImpl(g, Arrays.copyOf(distances, n), Arrays.copyOf(backtrack, n));
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
