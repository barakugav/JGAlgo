package com.ugav.jgalgo;

public interface UnionFind {

	/**
	 * Create a new element in a singleton set in the union find data structure
	 *
	 * @return index of the new element
	 */
	public int make();

	/**
	 * Find the set of x and get an arbitrary element from it
	 *
	 * find(a) == find(b) iff a and b are in the same set
	 *
	 * @param x element in the data structure
	 * @return arbitrary element from the set of x
	 */
	public int find(int x);

	/**
	 * Union the two sets of a and b
	 *
	 * @param a arbitrary element of some set
	 * @param b arbitrary element of another set
	 * @return arbitrary element from the unioned set
	 */
	public int union(int a, int b);

	/**
	 * Get the number of elements in all the sets in the union find data structure
	 *
	 * @return size of the data structure
	 */
	public int size();

	/**
	 * Remove all elements from the data structure
	 *
	 * This method can be used to reuse allocated memory of the data structure
	 */
	public void clear();

}
