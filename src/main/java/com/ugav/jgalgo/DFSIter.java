package com.ugav.jgalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class DFSIter implements IntIterator {

	private final Graph g;
	private final boolean[] visited;
	private final List<EdgeIter> edgeIters;
	private final IntList edgePath;
	private boolean isValid;

	public DFSIter(Graph g, int source) {
		int n = g.vertices().size();
		this.g = g;
		visited = new boolean[n];
		edgeIters = new ArrayList<>();
		edgePath = new IntArrayList();

		visited[source] = true;
		edgeIters.add(g.edges(source));
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
				if (visited[v])
					continue;
				visited[v] = true;
				edgeIters.add(g.edges(v));
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