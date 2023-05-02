package com.jgalgo;

import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A greedy coloring algorithm with random vertices order.
 * <p>
 * The algorithm examine the vertices in random order and assign for each
 * vertex the minimum (integer) color which is not used by its neighbors.
 * <p>
 * The algorithm runs in linear time, assuming the number of colors is constant.
 * <p>
 * Note that the result is an approximate for the minimum number of colors, as
 * coloring is an NP-hard problem.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Greedy_coloring">Wikipedia</a>
 * @author Barak Ugav
 */
public class ColoringGreedyRandom implements Coloring {

	private final Random rand;

	/**
	 * Create a new coloring algorithm object with random seed.
	 */
	public ColoringGreedyRandom() {
		rand = new Random();
	}

	/**
	 * Create a new coloring algorithm object with the provided seed.
	 *
	 * @param seed the seed to use for all random operations
	 */
	public ColoringGreedyRandom(long seed) {
		rand = new Random(seed);
	}

	@Override
	public Coloring.Result computeColoring(UGraph g) {
		if (GraphsUtils.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();
		int[] order = new int[n];
		for (int u = 0; u < n; u++)
			order[u] = u;
		IntArrays.shuffle(order, rand);

		IntSet usedColors = new IntOpenHashSet();
		for (int u : order) {
			usedColors.clear();
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
