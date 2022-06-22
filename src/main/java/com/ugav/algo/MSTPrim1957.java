package com.ugav.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graphs.EdgeWeightComparator;

public class MSTPrim1957 implements MST {

	/*
	 * O(m + nlogn)
	 */

	public MSTPrim1957() {
	}

	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w) {
		if (g instanceof GraphDirected<?>)
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();
		if (n == 0)
			return Collections.emptyList();

		Comparator<Edge<E>> c = new EdgeWeightComparator<>(w);
		HeapDirectAccessed<Edge<E>> heap = new HeapFibonacci<>(c);
		@SuppressWarnings("unchecked")
		HeapDirectAccessed.Handle<Edge<E>>[] verticesPtrs = new HeapDirectAccessed.Handle[n];
		boolean[] visited = new boolean[n];

		Collection<Edge<E>> mst = new ArrayList<>(n - 1);
		for (int r = 0; r < n; r++) {
			if (visited[r])
				continue;

			treeLoop: for (int u = r;;) {
				visited[u] = true;
				verticesPtrs[u] = null;

				/* decrease edges keys if a better one is found */
				for (Edge<E> e : Utils.iterable(g.edges(u))) {
					int v = e.v();
					if (visited[v])
						continue;

					HeapDirectAccessed.Handle<Edge<E>> vPtr = verticesPtrs[v];
					if (vPtr == null)
						vPtr = verticesPtrs[v] = heap.insert(e);
					else if (c.compare(e, vPtr.get()) < 0)
						heap.decreaseKey(vPtr, e);
				}

				/* find next lightest edge */
				Edge<E> e = null;
				while (true) {
					if (heap.isEmpty())
						/* reached all vertices from current root, continue to next tree */
						break treeLoop;
					e = heap.extractMin();
					if (!visited[e.v()])
						break;
				}

				/* add lightest edge to MST */
				mst.add(e);
				u = e.v();
			}
		}

		return mst;
	}

}
