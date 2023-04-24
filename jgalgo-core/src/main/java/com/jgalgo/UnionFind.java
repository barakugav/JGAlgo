package com.jgalgo;

/**
 * Data structure of a finite set of elements supporting union and find
 * operations.
 * <p>
 * The Union Find data structure stores a collection of disjoint sets. Each such
 * set has some representative element, which is an arbitrary element from the
 * set. Three basic operations are supported:
 * <ul>
 * <li>{@link #make()} - create a new element in a new set.</li>
 * <li>{@link #find(int)} - find the representative of the set of an element
 * (return the same representative for any element in the set).</li>
 * <li>{@link #union(int, int)} - union the sets of two elements.</li>
 * </ul>
 *
 * <pre> {@code
 * UnionFind uf = ...;
 * int x1 = uf.make();
 * int x2 = uf.make();
 * int x3 = uf.make();
 *
 * assert uf.find(x1) == x1;
 * assert uf.find(x2) == x2;
 * assert uf.find(x3) == x3;
 *
 * uf.union(x1, x2);
 * assert uf.find(x1) == uf.find(x2);
 * assert uf.find(x1) != uf.find(x3);
 * }</pre>
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Disjoint-set_data_structure">Wikipedia</a>
 * @author Barak Ugav
 */
public interface UnionFind {

	/**
	 * Create a new element in a singleton set.
	 *
	 * @return identifier of the new element
	 */
	public int make();

	/**
	 * Find the set of an element and get an arbitrary element from it.
	 * <p>
	 * {@code find(a) == find(b)} if an only if {@code a} and {@code b} are in the
	 * same set.
	 *
	 * @param x element in the data structure
	 * @return arbitrary element from the set of x
	 */
	public int find(int x);

	/**
	 * Union the two sets of {@code a} and {@code b}.
	 *
	 * @param a the first element
	 * @param b the second element
	 * @return arbitrary element from the union of sets of {@code a} and {@code b}.
	 */
	public int union(int a, int b);

	/**
	 * Get the number of elements in all the sets in the union find data structure.
	 *
	 * @return number of elements in the data structure
	 */
	public int size();

	/**
	 * Clear the data structure by removing all elements from all sets.
	 * <p>
	 * This method can be used to reuse allocated memory of the data structure.
	 */
	public void clear();

}
