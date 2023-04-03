package com.ugav.jgalgo;

import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ColoringGreedyRandom implements Coloring {

	/**
	 * Compute a coloring approximation in O(m n)
	 */

	private final Random rand;

	public ColoringGreedyRandom() {
		rand = new Random();
	}

	public ColoringGreedyRandom(long seed) {
		rand = new Random(seed);
	}

	@Override
	public Coloring.Result calcColoring(UGraph g) {
		if (Graphs.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();
		int[] order = new int[n];
		for (int u = 0; u < n; u++)
			order[u] = u;
		IntArrays.shuffle(order, rand);

		for (int i = 0; i < n; i++) {
			int u = order[i];
			IntSet usedColors = new IntOpenHashSet();
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
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
