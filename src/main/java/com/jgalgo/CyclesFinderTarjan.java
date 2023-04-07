package com.jgalgo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

public class CyclesFinderTarjan implements CyclesFinder {

	/*
	 * Find all cycles in a directed graph in O((n+m)(c+1)) where c is the number of
	 * simple cycles in the graph
	 */

	@Override
	public List<Path> findAllCycles(Graph g0) {
		if (!(g0 instanceof DiGraph))
			throw new IllegalArgumentException();
		DiGraph g = (DiGraph) g0;
		Worker worker = new Worker(g);
		int n = g.vertices().size();
		for (int s = 0; s < n; s++) {
			worker.findAllCycles(s);
			worker.reset();
		}
		return worker.cycles;
	}

	private static class Worker {
		private final DiGraph g;
		private final IntStack path = new IntArrayList();
		private final IntStack markedStack = new IntArrayList();
		private final BitSet isMarked;
		private final List<Path> cycles = new ArrayList<>();

		Worker(DiGraph g) {
			this.g = g;
			int n = g.vertices().size();
			isMarked = new BitSet(n);
		}

		void reset() {
			((IntArrayList) path).clear();
			((IntArrayList) markedStack).clear();
			isMarked.clear();
		}

		boolean findAllCycles(int startV) {
			boolean cycleFound = false;

			int u = path.isEmpty() ? startV : g.edgeTarget(path.topInt());
			isMarked.set(u);
			markedStack.push(u);

			for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
				int e = it.nextInt();
				int v = it.v();
				if (v < startV)
					continue;
				if (v == startV) {
					path.push(e);
					cycles.add(new Path(g, startV, startV, new IntArrayList((IntArrayList) path)));
					path.popInt();
					cycleFound = true;
				} else if (!isMarked.get(v)) {
					path.push(e);
					if (findAllCycles(startV))
						cycleFound = true;
					path.popInt();
				}
			}
			if (cycleFound) {
				while (markedStack.topInt() != u) {
					int w = markedStack.popInt();
					isMarked.clear(w);
				}
				markedStack.popInt();
				isMarked.clear(u);
			}

			return cycleFound;
		}
	}

}
