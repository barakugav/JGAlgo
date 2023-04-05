package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntIterator;

public class ColoringRecursiveLargestFirst implements Coloring {

	/**
	 * Compute a coloring approximation in O(m n)
	 */

	@Override
	public Result calcColoring(UGraph g) {
		if (Graphs.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();
		int[] degree = new int[n];
		for (int u = 0; u < n; u++)
			degree[u] = g.degree(u);

		BitSet S = new BitSet(n);
		BitSet isAdjacentToS = new BitSet(n);

		for (int color = 0;; color++) {
			S.clear();
			isAdjacentToS.clear();

			int bestDegree = -1, firstU = -1;
			for (int u = 0; u < n; u++) {
				if (res.colorOf(u) != -1)
					continue;
				int d = degree[u];
				if (bestDegree < d) {
					bestDegree = d;
					firstU = u;
				}
			}
			if (firstU == -1) {
				res.colorsNum = color;
				break;
			}

			for (int u = firstU; u != -1;) {
				// add u to S
				S.set(u);

				// update info
				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (res.colorOf(v) != -1)
						continue;
					isAdjacentToS.set(v);
				}

				int nextU = -1, bestNumOfNeighborsAdjacentToS = -1;
				bestDegree = -1;
				for (int v = 0; v < n; v++) {
					if (res.colorOf(v) != -1 || S.get(v) || isAdjacentToS.get(v))
						continue;
					int numOfNeighborsAdjacentToS = 0;
					for (EdgeIter eit = g.edges(v); eit.hasNext();) {
						eit.nextInt();
						int w = eit.v();
						if (isAdjacentToS.get(w))
							numOfNeighborsAdjacentToS++;
					}
					if (bestNumOfNeighborsAdjacentToS < numOfNeighborsAdjacentToS
							|| (bestNumOfNeighborsAdjacentToS == numOfNeighborsAdjacentToS
									&& degree[nextU] > degree[v])) {
						nextU = v;
						bestNumOfNeighborsAdjacentToS = numOfNeighborsAdjacentToS;
					}
				}
				u = nextU;
			}

			for (IntIterator it = Utils.bitSetIterator(S); it.hasNext();) {
				int u = it.nextInt();
				res.colors[u] = color;

				// update degree to include only vertices without color
				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					eit.nextInt();
					degree[eit.v()]--;
				}
			}
		}

		return res;
	}

}
