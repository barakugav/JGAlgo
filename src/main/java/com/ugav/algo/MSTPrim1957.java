package com.ugav.algo;

import com.ugav.algo.Graphs.EdgeWeightComparator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MSTPrim1957 implements MST {

	/*
	 * O(m + nlogn)
	 */

	public MSTPrim1957() {
	}

	@Override
	public IntCollection calcMST(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		UGraph g = (UGraph) g0;
		int n = g.verticesNum();
		if (n == 0)
			return IntLists.emptyList();

		IntComparator c = new EdgeWeightComparator(w);
		HeapDirectAccessed<Integer> heap = new HeapFibonacci<>(c);
		@SuppressWarnings("unchecked")
		HeapDirectAccessed.Handle<Integer>[] verticesPtrs = new HeapDirectAccessed.Handle[n];
		boolean[] visited = new boolean[n];

		IntCollection mst = new IntArrayList(n - 1);
		for (int r = 0; r < n; r++) {
			if (visited[r])
				continue;

			treeLoop: for (int u = r;;) {
				visited[u] = true;
				verticesPtrs[u] = null;

				/* decrease edges keys if a better one is found */
				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					if (visited[v])
						continue;

					HeapDirectAccessed.Handle<Integer> vPtr = verticesPtrs[v];
					if (vPtr == null)
						vPtr = verticesPtrs[v] = heap.insert(Integer.valueOf(e));
					else if (c.compare(e, vPtr.get().intValue()) < 0)
						heap.decreaseKey(vPtr, Integer.valueOf(e));
				}

				/* find next lightest edge */
				int e, v;
				for (;;) {
					if (heap.isEmpty())
						/* reached all vertices from current root, continue to next tree */
						break treeLoop;
					e = heap.extractMin().intValue();
					if (!visited[v = g.edgeSource(e)])
						break;
					if (!visited[v = g.edgeTarget(e)])
						break;
				}

				/* add lightest edge to MST */
				mst.add(e);
				u = v;
			}
		}

		return mst;
	}

}
