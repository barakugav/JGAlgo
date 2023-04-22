package com.jgalgo;

import java.util.Arrays;

/**
 * Dijkstra's algorithm for Single Source Shortest Path (SSSP).
 * <p>
 * Compute the shortest paths from a single source to all other vertices in
 * {@code O(m + n log n)} time, using {@link HeapReferenceable} with
 * {@code O(1)} time for
 * {@link HeapReferenceable#decreaseKey(HeapReference, Object)} operations.
 * <p>
 * Only positive edge weights are supported. This implementation should be the
 * first choice for {@link SSSP} with positive weights. For negative weights use
 * {@link SSSPBellmanFord} for floating points or {@link SSSPGoldberg} for
 * integers.
 * <p>
 * Based on 'A note on two problems in connexion with graphs' by E. W. Dijkstra
 * (1959). A 'note'??!! this guy changed the world, and he publish it as a
 * 'note'.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class SSSPDijkstra implements SSSP {

	private int allocSize;
	private HeapReferenceable<HeapElm> heap;
	private HeapReference<HeapElm>[] verticesPtrs;

	/**
	 * Construct a new SSSP algorithm object.
	 */
	public SSSPDijkstra() {
		allocSize = 0;
		heap = new HeapPairing<>();
	}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	public void setHeapBuilder(HeapReferenceable.Builder heapBuilder) {
		heap = heapBuilder.build();
	}

	@SuppressWarnings("unchecked")
	private void memAlloc(int n) {
		if (allocSize < n) {
			verticesPtrs = new HeapReference[n];
			allocSize = n;
		}
	}

	private void memClear(int n) {
		heap.clear();
		Arrays.fill(verticesPtrs, 0, n, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if one of the edge weights is negative
	 */
	@Override
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		int n = g.vertices().size();

		memAlloc(n);
		HeapReferenceable<HeapElm> heap = this.heap;
		HeapReference<HeapElm>[] verticesPtrs = this.verticesPtrs;

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

				HeapReference<HeapElm> vPtr = verticesPtrs[v];
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
