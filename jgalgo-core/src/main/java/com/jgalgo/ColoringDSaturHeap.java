package com.jgalgo;

import java.util.BitSet;
import java.util.Objects;

/**
 * The DSatur coloring algorithm implemented with a heap.
 * <p>
 * The Saturation Degree (DSatur) coloring algorithm is a greedy algorithm,
 * namely it examine the vertices in some order and assign for each vertex the
 * minimum (integer) color which is not used by its neighbors. It differ from
 * other greedy coloring algorithm by the order of the vertices: the next vertex
 * to be colored is the vertex with the highest number of colors in its
 * neighborhood (called saturation degree).
 * <p>
 * This implementation differ from {@link ColoringDSatur} as it uses a heap to
 * maintain the uncolored vertices, and perform {@code decreaseKey} operations.
 * The running time of the algorithm is {@code O(m + log n)} assuming the number
 * of colors is constant.
 * <p>
 * Note that the result is an approximate for the minimum number of colors, as
 * coloring is an NP-hard problem.
 *
 * @see <a href="https://en.wikipedia.org/wiki/DSatur">Wikipedia</a>
 * @see ColoringDSatur
 * @author Barak Ugav
 */
public class ColoringDSaturHeap implements Coloring {

	private HeapReferenceable.Builder heapBuilder = HeapPairing::new;

	/**
	 * Create a new coloring algorithm object.
	 */
	public ColoringDSaturHeap() {
	}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	public void setHeapBuilder(HeapReferenceable.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	@Override
	public Coloring.Result computeColoring(UGraph g) {
		if (Graphs.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();

		HeapReferenceable<HeapElm> heap = heapBuilder.build();
		@SuppressWarnings("unchecked")
		HeapReference<HeapElm>[] vPtrs = new HeapReference[n];
		for (int u = 0; u < n; u++)
			vPtrs[u] = heap.insert(new HeapElm(u, g.degreeOut(u)));

		while (!heap.isEmpty()) {
			HeapElm elm = heap.extractMin();
			int u = elm.v;

			int color = 0;
			while (elm.neighborColors.get(color))
				color++;
			res.colors[u] = color;
			res.colorsNum = Math.max(res.colorsNum, color + 1);

			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (res.colorOf(v) == -1) { /* v is uncolored */
					HeapReference<HeapElm> vPtr = vPtrs[v];
					HeapElm vElm = vPtr.get();
					if (!vElm.neighborColors.get(color)) {
						vElm.neighborColors.set(color);
						vElm.neighborColorsNum++;
						heap.decreaseKey(vPtr, vElm);
					}
				}
			}
		}
		return res;
	}

	private static class HeapElm implements Comparable<HeapElm> {
		final int v;
		final int degree;
		final BitSet neighborColors = new BitSet();
		int neighborColorsNum;

		HeapElm(int v, int degree) {
			this.v = v;
			this.degree = degree;
		}

		@Override
		public int compareTo(HeapElm o) {
			int c;
			if ((c = Integer.compare(neighborColorsNum, o.neighborColorsNum)) != 0)
				// negate compare, more neighbor colors should be extracted from the heap first
				return -c;
			if ((c = Integer.compare(degree, o.degree)) != 0)
				return c;
			return 0;
		}
	}

}
