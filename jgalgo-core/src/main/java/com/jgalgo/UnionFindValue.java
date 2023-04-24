package com.jgalgo;

/**
 * Union Find with {@code double} values for the elements.
 * <p>
 * This interface is an extension to the {@link UnionFind} interface that
 * support, along with regular operation, value of each elements and addition of
 * some value to all elements of a set using the {@link #addValue(int, double)}
 * method.
 *
 * <pre> {@code
 * UnionFindValue uf = ...;
 * int x1 = uf.make(4);
 * int x2 = uf.make(11);
 * int x3 = uf.make(6);
 *
 * assert uf.getValue(x1) == 4;
 * assert uf.getValue(x2) == 11;
 * assert uf.getValue(x3) == 6;
 *
 * uf.union(x1, x2);
 * uf.addValue(x1, 20);
 * assert uf.getValue(x1) == 24;
 * assert uf.getValue(x2) == 31;
 * assert uf.getValue(x3) == 6;
 *
 * uf.addValue(x3, -2);
 * assert uf.getValue(x1) == 24;
 * assert uf.getValue(x2) == 31;
 * assert uf.getValue(x3) == 4;
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface UnionFindValue extends UnionFind {

	/**
	 * Create a new element with a given value.
	 *
	 * @param value the value of the new element
	 * @return identifier of the new element in the union find data structure
	 */
	public int make(double value);

	/**
	 * {@inheritDoc}
	 * <p>
	 * The created element will be assigned a value of {@code 0}.
	 */
	@Override
	default int make() {
		return make(0);
	}

	/**
	 * Get the value of an element.
	 *
	 * @param x an element in the data structure
	 * @return value of the element
	 */
	public double getValue(int x);

	/**
	 * Add value to ALL elements in the set of a given element.
	 *
	 * @param x     an element in the data structure
	 * @param value value to add to all elements of the set of {@code x}
	 */
	public void addValue(int x, double value);

}
