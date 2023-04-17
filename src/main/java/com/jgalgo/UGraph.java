package com.jgalgo;

/**
 * A discrete undirected graph with vertices and edges.
 * <p>
 * An extension to the {@link Graph} interface, where edges are undirected,
 * namely
 * an edge {@code e(u, v)} will appear in the iteration of {@code edgesOut(u)},
 * {@code edgesIn(v)}, {@code edgesOut(v)} and {@code edgesIn(u)}. Also
 * {@link #edgesOut(int)} and {@link #edgesIn(int)} are equivalent for the same
 * vertex, same for {@link #degreeIn(int)} and {@link #degreeOut(int)}, and
 * similarly {@link #removeEdges(int)}, {@link #removeEdgesIn(int)} and
 * {@link #removeEdgesOut(int)}.
 *
 * @see DiGraph
 * @author Barak Ugav
 */
public interface UGraph extends Graph {

	@Override
	default EdgeIter edgesIn(int v) {
		return edgesOut(v);
	}

	@Override
	default void removeEdges(int u) {
		for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Override
	default void removeEdgesOut(int u) {
		removeEdges(u);
	}

	@Override
	default void removeEdgesIn(int v) {
		removeEdges(v);
	}

	@Override
	default int degreeIn(int v) {
		return degreeOut(v);
	}

}
