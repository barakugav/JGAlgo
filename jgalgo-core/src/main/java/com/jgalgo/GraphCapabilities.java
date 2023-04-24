package com.jgalgo;

/**
 * Object specifying the capabilities of a graph implementation.
 * <p>
 * Each implementation of the {@link Graph} interface may are may not support
 * some operations. Each graph provide its capabilities using
 * {@link Graph#getCapabilities()}.
 *
 * @see GraphBuilder
 * @see GraphArrayUndirected
 * @see GraphLinkedUndirected
 * @see GraphTableUndirected
 * @author Barak Ugav
 */
public interface GraphCapabilities {

	/**
	 * Checks whether vertex additions are supported.
	 *
	 * @return true if the graph support vertex additions, else false.
	 */
	boolean vertexAdd();

	/**
	 * Checks whether vertex removals are supported.
	 *
	 * @return true if the graph support vertex removals, else false.
	 */
	boolean vertexRemove();

	/**
	 * Checks whether edge additions are supported.
	 *
	 * @return true if the graph support edge additions, else false.
	 */
	boolean edgeAdd();

	/**
	 * Checks whether edge removals are supported.
	 *
	 * @return true if the graph support edge removals, else false.
	 */
	boolean edgeRemove();

	/**
	 * Checks whether parallel edges are supported.
	 * <p>
	 * Parallel edges are multiple edges with identical source and target.
	 *
	 * @return true if the graph support parallel edges, else false.
	 */
	boolean parallelEdges();

	/**
	 * Checks whether self edges are supported.
	 * <p>
	 * Self edges are edges with the same source and target, namely a vertex with an
	 * edge to itself.
	 *
	 * @return true if the graph support self edges, else false.
	 */
	boolean selfEdges();

	/**
	 * Checks whether the graph is directed.
	 *
	 * @return true if the graph is directed, else false.
	 */
	boolean directed();

}
