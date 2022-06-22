package com.ugav.algo;

import java.util.Iterator;

public interface GraphDirected<E> extends Graph<E> {

	/**
	 * Get the edges whose source is u
	 */
	@Override
	default EdgeIterator<E> edges(int u) {
		return edgesOut(u);
	}

	/**
	 * Get the edges whose source is u
	 *
	 * @param u a source vertex
	 * @return iterator that iterate over the edges whose source is u
	 */
	public EdgeIterator<E> edgesOut(int u);

	/**
	 * Get the edges whose target is v
	 *
	 * @param v a target vertex
	 * @return iterator that iterate over the edges whose target is v
	 */
	public EdgeIterator<E> edgesIn(int v);

	/**
	 * Remove all the edges going out of a given vertex
	 *
	 * @param u a source vertex
	 */
	default void removeEdgesOut(int u) {
		for (Iterator<Edge<E>> it = edgesOut(u); it.hasNext();) {
			it.next();
			it.remove();
		}
	}

	/**
	 * Remove all the edges going into a given vertex
	 *
	 * @param v a target vertex
	 */
	default void removeEdgesIn(int v) {
		for (Iterator<Edge<E>> it = edgesIn(v); it.hasNext();) {
			it.next();
			it.remove();
		}
	}

}
