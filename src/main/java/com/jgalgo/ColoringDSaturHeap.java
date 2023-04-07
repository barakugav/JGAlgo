package com.jgalgo;

import java.util.BitSet;
import java.util.Objects;

public class ColoringDSaturHeap implements Coloring {

	/**
	 * Compute a coloring approximation in O(m + n \log n)
	 */

	private HeapDirectAccessed.Builder heapBuilder = HeapPairing::new;

	public void setHeapBuilder(HeapDirectAccessed.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	@Override
	public Coloring.Result calcColoring(UGraph g) {
		if (Graphs.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();

		HeapDirectAccessed<HeapElm> heap = heapBuilder.build();
		@SuppressWarnings("unchecked")
		HeapDirectAccessed.Handle<HeapElm>[] vPtrs = new HeapDirectAccessed.Handle[n];
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
					HeapDirectAccessed.Handle<HeapElm> vPtr = vPtrs[v];
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
			System.out.println();
			if ((c = -Integer.compare(neighborColorsNum, o.neighborColorsNum)) != 0)
				return c;
			if ((c = Integer.compare(degree, o.degree)) != 0)
				return c;
			return 0;
		}
	}

}
