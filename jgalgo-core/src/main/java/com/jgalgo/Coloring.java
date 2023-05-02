package com.jgalgo;

/**
 * An algorithm that assign a color to each vertex in a graph while avoiding identical color for any pair of adjacent
 * vertices.
 * <p>
 * Given a graph \(G=(V,E)\) a valid coloring is a function \(C:v \rightarrow c\) for any vertex \(v\) in \(V\) where
 * each edge \((u,v)\) in \(E\) satisfy \(C(u) \neq C(v)\). The objective is to minimize the total number of different
 * colors. The problem is NP-hard, but various heuristics exists which give decent results for general graphs and
 * optimal results for special cases.
 * <p>
 * Each color is represented as an integer in range \([0, \textit{colorsNum})\).
 *
 * <pre> {@code
 * UGraph g = UGraph.newBuilder().build();
 * int v1 = g.addVertex();
 * int v2 = g.addVertex();
 * int v3 = g.addVertex();
 * int v4 = g.addVertex();
 * g.addEdge(v1, v2);
 * g.addEdge(v2, v3);
 * g.addEdge(v3, v1);
 * g.addEdge(v3, v4);
 *
 * Coloring coloringAlg = Coloring.newBuilder().build();
 * Coloring.Result colors = coloringAlg.computeColoring(g);
 * System.out.println("A valid coloring with " + colors.colorsNum() + " colors was found");
 * for (IntIterator uit = g.vertices().iterator(); uit.hasNext();) {
 * 	int u = uit.nextInt();
 * 	System.out.println("The color of vertex " + u + " is " + colors.colorOf(u));
 * 	for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
 * 		eit.nextInt();
 * 		int v = eit.v();
 * 		assert colors.colorOf(u) != colors.colorOf(v);
 * 	}
 * }
 * }</pre>
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Graph_coloring">Wikipedia</a>
 * @author Barak Ugav
 */
public interface Coloring {

	/**
	 * Assign a color to each vertex of the given graph, resulting in a valid coloring.
	 *
	 * @param  g a graph
	 * @return   a valid coloring with (hopefully) small number of different colors
	 */
	Coloring.Result computeColoring(UGraph g);

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
		 * @param  v a vertex identifier in the graph
		 * @return   a color of the vertex, represented as integer
		 */
		int colorOf(int v);

	}

	/**
	 * Create a new coloring algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link Coloring} object.
	 *
	 * @return a new builder that can build {@link Coloring} objects
	 */
	static Coloring.Builder newBuilder() {
		return ColoringDSaturHeap::new;
	}

	/**
	 * A builder for {@link Coloring} objects.
	 *
	 * @see    Coloring#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for coloring computation.
		 *
		 * @return a new coloring algorithm
		 */
		Coloring build();
	}

}
