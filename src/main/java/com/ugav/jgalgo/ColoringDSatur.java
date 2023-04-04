package com.ugav.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ColoringDSatur implements Coloring {

	/**
	 * Compute a coloring approximation in O(m n)
	 */

	@Override
	public Coloring.Result calcColoring(UGraph g) {
		if (Graphs.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();

		IntSet uncolored = new IntOpenHashSet(n);
		BitSet[] usedColors = new BitSet[n];
		int[] degree = new int[n];
		for (int u = 0; u < n; u++) {
			uncolored.add(u);
			usedColors[u] = new BitSet();
			degree[u] = g.degree(u);
		}

		while (!uncolored.isEmpty()) {
			int u = uncolored.iterator().nextInt();
			for (IntIterator it = uncolored.iterator(); it.hasNext();) {
				int u1 = it.nextInt();
				if (usedColors[u].size() < usedColors[u1].size())
					continue;
				if (usedColors[u].size() > usedColors[u1].size() || degree[u] > degree[u1])
					u = u1;
			}

			int color = 0;
			while (usedColors[u].get(color))
				color++;
			res.colors[u] = color;
			res.colorsNum = Math.max(res.colorsNum, color + 1);
			uncolored.remove(u);
			usedColors[u].clear();
			usedColors[u] = null;

			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (res.colorOf(v) == -1) /* v is uncolored */
					usedColors[v].set(color);
			}
		}
		return res;
	}

}
