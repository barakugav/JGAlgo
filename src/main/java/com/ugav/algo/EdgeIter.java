package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Edge iterator. Each int returned by nextInt() is ID of an edge iterated by
 * the iterator.
 */
public interface EdgeIter extends IntIterator {

	/** Get the source vertex of the last returned edge */
	int u();

	/** Get the target vertex of the last returned edge */
	int v();

}