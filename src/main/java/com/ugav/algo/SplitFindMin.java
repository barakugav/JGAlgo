package com.ugav.algo;

import java.util.Collection;
import java.util.Comparator;

import com.ugav.algo.Utils.NullList;

public interface SplitFindMin<K> extends SplitFind {

	/**
	 * Init the data structure with a sequence [0, keys.size()) with the given keys
	 *
	 * @param keys collection of keys
	 * @param c    comparator, if null the default comparator will be used
	 *             (Comparable interface)
	 */
	public void init(Collection<K> keys, Comparator<? super K> c);

	@Override
	default void init(int size) {
		init(new NullList<>(size), (k1, k2) -> 0);
	}

	/**
	 * Get the key associated with an element
	 *
	 * @param x index of an element in the data structure
	 * @return the key associated with the element
	 */
	public K getKey(int x);

	/**
	 * Find the element with the minimum key in the sequence of x
	 *
	 * @param x index of an element in the data structure
	 * @return index of the element with the minimum key in the sequence of x
	 */
	public int findMin(int x);

	/**
	 * Decrease the key of an element
	 *
	 * @param x      index of an element in the data structure
	 * @param newKey new key for the element
	 */
	public void decreaseKey(int x, K newKey);

}
