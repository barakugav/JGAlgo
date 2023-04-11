package com.jgalgo;

/**
 * An algorithm that assign a color to each vertex in a graph while avoiding
 * identical color for any pair of adjacent vertices.
 * <p>
 * Given a graph {@code G=(V,E)} a valid coloring is a function {@code C:v->c}
 * for any vertex {@code v} in {@code V} where each edge {@code (u, v)} in
 * {@code E} satisfy {@code C(u) != C(v)}. The objective is to minimize the
 * total number of different colors. The problem is NP-hard, but various
 * heuristics exists which give decent results for general graphs and optimal
 * results for special cases.
 * <p>
 * Each color is represented as an integer in range {@code [0, colorsNum)}.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Graph_coloring">Wikipedia</a>
 * @author Barak Ugav
 */
public interface Coloring {

	/**
	 * Assign a color to each vertex of the given graph, resulting in a valid
	 * coloring.
	 *
	 * @param g a graph
	 * @return a valid coloring with (hopefully) small number of different colors
	 */
	Coloring.Result calcColoring(UGraph g);

	/**
	 * A coloring result containing a color for each vertex.
	 *
	 * @author Barak Ugav
	 */
	interface Result {

		/**
		 * The total number of different colors used in the coloring.
		 *
		 * @return number of different colors
		 */
		int colorsNum();

		/**
		 * Get the color assigned to a vertex.
		 *
		 * @param v a vertex identifier in the graph
		 * @return a color of the vertex, represented as integer
		 */
		int colorOf(int v);

	}

}
