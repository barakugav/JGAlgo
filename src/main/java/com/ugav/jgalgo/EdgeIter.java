package com.ugav.jgalgo;

import java.util.NoSuchElementException;

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

	public static final EdgeIter Empty = new EdgeIter() {

		@Override
		public int nextInt() {
			throw new NoSuchElementException();
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public int u() {
			throw new NoSuchElementException();
		}

		@Override
		public int v() {
			throw new NoSuchElementException();
		}
	};

}