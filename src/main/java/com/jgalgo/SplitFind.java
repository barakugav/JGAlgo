package com.jgalgo;

public interface SplitFind {

	/**
	 * Init the data structure with a sequence [0, size)
	 *
	 * @param size the size of the sequence
	 */
	public void init(int size);

	/**
	 * Find the set x belongs to
	 *
	 * @param x index of an element in the data structure
	 * @return index of some element in the set x belongs to. If x1,x2 belongs to
	 *         the same set, find(x1)==find(x2)
	 */
	public int find(int x);

	/**
	 * Split an element's sequence into two separate sequences relative to the given
	 * element
	 *
	 * For example, if a data structure was initialized with size 5, it will contain
	 * single sequence [0,1,2,3,4]. After split(2), it will contains two sequences
	 * {[0,1], [2,3,4]}
	 *
	 * @param x index of an element in the data structure, will be included in the
	 *          upper sequence
	 */
	public void split(int x);

}
