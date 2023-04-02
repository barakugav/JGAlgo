package com.ugav.jgalgo;

import com.ugav.jgalgo.Utils.StackIntFixSize;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class EulerianTour {

	private EulerianTour() {
	}

	public static IntList calcTour(UGraph g) {
		int n = g.vertices().size();

		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			if (g.degree(u) % 2 == 0)
				continue;
			if (start == -1)
				start = u;
			else if (end == -1)
				end = u;
			else
				throw new IllegalArgumentException(
						"More than two vertices have an odd degree (" + start + ", " + end + ", " + u + ")");
		}
		if (start != -1 && end == -1)
			throw new IllegalArgumentException(
					"Eulerian tour exists only if all vertices have even degree or only two vertices have odd degree");
		if (start == -1)
			start = 0;

		IntArrayList tour = new IntArrayList(g.edges().size());
		IntSet usedEdges = new IntOpenHashSet();
		EdgeIter[] iters = new EdgeIter[n];
		for (int u = 0; u < n; u++)
			iters[u] = g.edges(u);

		StackIntFixSize queue = new StackIntFixSize(g.edges().size());

		for (int u = start;;) {
			findCycle: for (;;) {
				int e, v;
				for (EdgeIter iter = iters[u];;) {
					if (!iter.hasNext())
						break findCycle;
					e = iter.nextInt();
					if (!usedEdges.contains(e)) {
						v = iter.v();
						break;
					}
				}
				usedEdges.add(e);
				queue.push(e);
				u = v;
			}

			if (queue.isEmpty())
				break;

			int e = queue.pop();
			tour.add(e);
			u = g.edgeEndpoint(e, u);
		}

		return tour;
	}

}
