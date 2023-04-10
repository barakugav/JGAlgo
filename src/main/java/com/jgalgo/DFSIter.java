package com.jgalgo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class DFSIter implements IntIterator {

	private final Graph g;
	private final BitSet visited;
	private final List<EdgeIter> edgeIters;
	private final IntList edgePath;
	private boolean isValid;

	public DFSIter(Graph g, int source) {
		int n = g.vertices().size();
		this.g = g;
		visited = new BitSet(n);
		edgeIters = new ArrayList<>();
		edgePath = new IntArrayList();

		visited.set(source);
		edgeIters.add(g.edgesOut(source));
		isValid = true;
	}

	@Override
	public boolean hasNext() {
		if (isValid)
			return true;
		if (edgeIters.isEmpty())
			return false;
		for (;;) {
			for (EdgeIter eit = edgeIters.get(edgeIters.size() - 1); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (visited.get(v))
					continue;
				visited.set(v);
				edgeIters.add(g.edgesOut(v));
				edgePath.add(e);
				return isValid = true;
			}
			edgeIters.remove(edgeIters.size() - 1);
			if (edgeIters.isEmpty()) {
				assert edgePath.isEmpty();
				return false;
			}
			edgePath.removeInt(edgePath.size() - 1);
		}
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		isValid = false;
		return edgeIters.get(edgeIters.size() - 1).u();
	}

	public IntList edgePath() {
		return edgePath;
	}
}