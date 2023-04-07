package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ColoringGreedy implements Coloring {

	/**
	 * Compute a coloring approximation in O(m n)
	 */

	@Override
	public Coloring.Result calcColoring(UGraph g) {
		if (Graphs.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();
		for (int u = 0; u < n; u++) {
			IntSet usedColors = new IntOpenHashSet();
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				int c = res.colorOf(v);
				if (c != -1)
					usedColors.add(c);
			}
			int color = 0;
			while (usedColors.contains(color))
				color++;
			res.colors[u] = color;
			res.colorsNum = Math.max(res.colorsNum, color + 1);
		}
		return res;
	}

}
