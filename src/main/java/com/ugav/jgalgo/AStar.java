package com.ugav.jgalgo;

import java.util.Objects;
import java.util.function.IntToDoubleFunction;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public class AStar {

	private HeapDirectAccessed.Builder heapBuilder;

	public AStar() {
		heapBuilder = HeapPairing::new;
	}

	public void setHeapBuilder(HeapDirectAccessed.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	public IntList calcPath(Graph g, EdgeWeightFunc w, int source, int target, IntToDoubleFunction vHeuristic) {
		if (source == target)
			return IntLists.emptyList();
		int n = g.vertices().size();
		HeapDirectAccessed<HeapElm> heap = heapBuilder
				.build((a, b) -> Utils.compare(a.distanceAstimate, b.distanceAstimate));
		@SuppressWarnings("unchecked")
		HeapDirectAccessed.Handle<HeapElm>[] verticesPtrs = new HeapDirectAccessed.Handle[n];

		SSSPResultImpl res = new SSSPResultImpl(g);
		res.distances[source] = 0;

		for (int u = source;;) {
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				double ws = w.weight(e);
				if (ws < 0)
					throw new IllegalArgumentException("negative weights are not supported");
				double distance = res.distances[u] + ws;
				if (distance >= res.distances[v])
					continue;
				res.distances[v] = distance;
				double distanceAstimate = distance + vHeuristic.applyAsDouble(v);

				HeapDirectAccessed.Handle<HeapElm> vPtr = verticesPtrs[v];
				if (vPtr == null) {
					verticesPtrs[v] = heap.insert(new HeapElm(distanceAstimate, e, v));
				} else {
					HeapElm ptr = vPtr.get();
					assert distance < ptr.distanceAstimate;
					if (distance < ptr.distanceAstimate) {
						ptr.distanceAstimate = distance;
						ptr.backtrack = e;
						heap.decreaseKey(vPtr, ptr);
					}
				}
			}

			if (heap.isEmpty())
				break;
			HeapElm next = heap.extractMin();
			verticesPtrs[next.v] = null;
			res.backtrack[next.v] = next.backtrack;
			u = next.v;
			if (u == target)
				return res.getPathTo(target);
		}
		return null;
	}

	private static class HeapElm {

		double distanceAstimate;
		int backtrack;
		final int v;

		HeapElm(double distanceAstimate, int backtrack, int v) {
			this.distanceAstimate = distanceAstimate;
			this.backtrack = backtrack;
			this.v = v;
		}

	}

}
