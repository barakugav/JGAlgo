package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graphs.EdgeWeightComparator;

public class MSTPrim implements MST {

	private MSTPrim() {
	}

	private static final MSTPrim INSTANCE = new MSTPrim();

	public static MSTPrim getInstance() {
		return INSTANCE;
	}

	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w) {
		if (g.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();
		if (n == 0)
			Collections.emptyList();

		Comparator<Edge<E>> c = new EdgeWeightComparator<>(w);
		Heap<Edge<E>> heap = new HeapFibonacci<>(c);
		@SuppressWarnings("unchecked")
		Heap.Handle<Edge<E>>[] verticesPtrs = new Heap.Handle[n];
		boolean[] visited = new boolean[n];
		Arrays.fill(visited, false);

		Collection<Edge<E>> mst = new ArrayList<>(n - 1);
		for (int r = 0; r < n; r++) {
			if (visited[r])
				continue;

			treeLoop: for (int u = r;;) {
				visited[u] = true;
				verticesPtrs[u] = null;

				/* decrease edges keys if a better one is found */
				for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();) {
					Edge<E> e = it.next();
					int v = e.v();
					if (visited[v])
						continue;

					Heap.Handle<Edge<E>> vPtr = verticesPtrs[v];
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
