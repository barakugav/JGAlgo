package com.jgalgo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Depth first search (DFS) iterator.
 * <p>
 * The DFS iterator is used to iterate over the vertices of a graph is a depth
 * first manner, namely it explore as far as possible along each branch before
 * backtracking. The iterator will visit every vertex {@code v} for which there
 * is a path from the source(s) to {@code v}. Each such vertex will be visited
 * exactly once.
 *
 * <pre> {@code
 * Graph g = ...;
 * int sourceVertex = ...;
 * for (DFSIter iter = new DFSIter(g, sourceVertex); iter.hasNext();) {
 *     int v = iter.nextInt();
 *     IntList edgePath = iter.edgePath();
 *     System.out.println("Reached vertex v using the edges: " + edgePath);
 * }
 * }</pre>
 *
 * @see BFSIter
 * @see <a href="https://en.wikipedia.org/wiki/Depth-first_search">Wikipedia</a>
 * @author Barak Ugav
 */
public class DFSIter implements IntIterator {

	private final Graph g;
	private final BitSet visited;
	private final List<EdgeIter> edgeIters;
	private final IntList edgePath;
	private boolean isValid;

	/**
	 * Create a DFS iterator rooted at some source vertex.
	 *
	 * @param g      a graph
	 * @param source a vertex in the graph from which the search will start from.
	 */
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

	/**
	 * Check whether there is more vertices to iterate over.
	 */
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

	/**
	 * Advance the iterator and return a vertex that was not visited by the iterator
	 * yet.
	 */
	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		isValid = false;
		return edgeIters.get(edgeIters.size() - 1).u();
	}

	/**
	 * Get the path from the source to the last vertex returned by {@link nextInt}.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return list of edges forming a path from the source to the last vertex
	 *         returned by {@link nextInt}. The returned list should not be modified
	 *         by the user.
	 */
	public IntList edgePath() {
		return edgePath;
	}
}