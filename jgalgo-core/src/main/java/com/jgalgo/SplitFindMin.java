package com.jgalgo;

import java.util.Collection;
import java.util.Comparator;

import com.jgalgo.Utils.NullList;

/**
 * An extension to {@link SplitFind} that support value keys and {@code findMin}
 * operations.
 * <p>
 * As the {@link SplitFind}, a data structure implementing this interface
 * maintain a collection disjoint sets and support {@link #find(int)} and
 * {@link #split(int)} operations. In addition, each element have a key, which
 * is comparable to any other key by a provided comparator, and the minimum key
 * in each set can be queried using {@link #findMin(int)}.
 *
 * @author Barak Ugav
 */
public interface SplitFindMin<K> extends SplitFind {

	/**
	 * Init the data structure with a sequence {@code [0, keys.size())} with the
	 * given keys.
	 *
	 * @param keys       collection of keys. The size of the collection determine
	 *                   the
	 *                   number of elements in the data structure
	 * @param comparator a comparator to compare the keys of the elements, if
	 *                   {@code null} the default comparator will be used
	 *                   (Comparable interface)
	 */
	public void init(Collection<K> keys, Comparator<? super K> comparator);

	@Override
	default void init(int size) {
		init(new NullList<>(size), (k1, k2) -> 0);
	}

	/**
	 * Get the key associated with an element.
	 *
	 * @param x an element in the data structure
	 * @return the key associated with the element
	 */
	public K getKey(int x);

	/**
	 * Find the element with the minimum key in the sequence of {@code x}
	 *
	 * @param x an element in the data structure
	 * @return the element with the minimum key in the sequence of {@code x}
	 */
	public int findMin(int x);

	/**
	 * Decrease the key of an element.
	 *
	 * @param x      an element in the data structure
	 * @param newKey new key for the element
	 * @return {@code true} if the decreased key is the minimum key in the element's
	 *         sequence
	 */
	public boolean decreaseKey(int x, K newKey);

}
