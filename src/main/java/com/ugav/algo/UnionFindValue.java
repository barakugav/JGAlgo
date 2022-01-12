package com.ugav.algo;

public interface UnionFindValue extends UnionFind {

	/**
	 * Create a new element with a given value
	 *
	 * @param value the value of the new element
	 * @return index of the new element in the union find data structure
	 */
	public int make(double value);

	@Override
	default int make() {
		return make(0);
	}

	/**
	 * Get the value of an element
	 *
	 * @param x index of element in the data structure
	 * @return value of the element
	 */
	public double getValue(int x);

	/**
	 * Add value to ALL elements in the set of x
	 * 
	 * @param x     arbitrary element in a set
	 * @param value additional value to add to all elements of the set
	 */
	public void addValue(int x, double value);

}
