package com.jgalgo;

import java.util.BitSet;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MSTPrim1957 implements MST {

	/*
	 * O(m + nlogn)
	 */

	private HeapReferenceable.Builder heapBuilder = HeapPairing::new;

	public MSTPrim1957() {
	}

	public void setHeapBuilder(HeapReferenceable.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	@Override
	public IntCollection calcMST(Graph g, EdgeWeightFunc w) {
		if (!(g instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		int n = g.vertices().size();
		if (n == 0)
			return IntLists.emptyList();

		HeapReferenceable<Integer> heap = heapBuilder.build(w);
		@SuppressWarnings("unchecked")
		HeapReference<Integer>[] verticesPtrs = new HeapReference[n];
		BitSet visited = new BitSet(n);

		IntCollection mst = new IntArrayList(n - 1);
		for (int r = 0; r < n; r++) {
			if (visited.get(r))
				continue;

			treeLoop: for (int u = r;;) {
				visited.set(u);
				verticesPtrs[u] = null;

				/* decrease edges keys if a better one is found */
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					if (visited.get(v))
						continue;

					HeapReference<Integer> vPtr = verticesPtrs[v];
					if (vPtr == null)
						verticesPtrs[v] = heap.insert(Integer.valueOf(e));
					else if (w.compare(e, vPtr.get().intValue()) < 0)
						heap.decreaseKey(vPtr, Integer.valueOf(e));
				}

				/* find next lightest edge */
				int e, v;
				for (;;) {
					if (heap.isEmpty())
						/* reached all vertices from current root, continue to next tree */
						break treeLoop;
					e = heap.extractMin().intValue();
					if (!visited.get(v = g.edgeSource(e)))
						break;
					if (!visited.get(v = g.edgeTarget(e)))
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
