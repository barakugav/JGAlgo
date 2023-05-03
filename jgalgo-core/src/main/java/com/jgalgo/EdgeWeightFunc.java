/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntComparator;

/**
 * Weight function that maps a graph edge to a weight.
 * <p>
 * Many algorithms such as {@link SSSP}, {@link MST}, {@link MaximumMatchingWeighted}, and more, try to find a set of
 * edges satisfying some constraint while minimizing/maximizing some objective function based on the weights of the
 * edges. This interface is the API by which the user specify the weights of the edges.
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * Graph g = Graph.newBuilderDirected().build();
 * int v1 = g.addVertex();
 * int v2 = g.addVertex();
 * int v3 = g.addVertex();
 * int e1 = g.addEdge(v1, v2);
 * int e2 = g.addEdge(v2, v3);
 * int e3 = g.addEdge(v1, v3);
 *
 * // Assign some weights to the edges
 * Weights.Double weights = g.addEdgesWeights("weightsKey", double.class);
 * weights.set(e1, 1.2);
 * weights.set(e2, 3.1);
 * weights.set(e3, 15.1);
 * EdgeWeightFunc weightFunc = weights;
 *
 * // Calculate the shortest paths from v1 to all other vertices
 * SSSP ssspAlgo = SSSP.newBuilder().build();
 * SSSP.Result ssspRes = ssspAlgo.computeShortestPaths(g, weightFunc, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPathTo(v3).equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (IntIterator it = ssspRes.getPathTo(v3).iterator(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @author Barak Ugav
 */
@FunctionalInterface
public interface EdgeWeightFunc extends IntComparator {

	/**
	 * Get the weight of an edge.
	 *
	 * @param  e                         an edge identifier
	 * @return                           the weight of the edge
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
	 */
	public double weight(int e);

	/**
	 * Compare two edges by their weights.
	 */
	@Override
	default int compare(int e1, int e2) {
		return Double.compare(weight(e1), weight(e2));
	}

	/**
	 * Weight function that maps a graph edge to an integer weight.
	 * <p>
	 * Some algorithms implementations support only integers weights, or run faster in such a case. This interface is
	 * the API for these algorithms for the edges integer weights.
	 *
	 * <pre> {@code
	 * // Create a directed graph with three vertices and edges between them
	 * Graph g = Graph.newBuilderDirected().build();
	 * int v1 = g.addVertex();
	 * int v2 = g.addVertex();
	 * int v3 = g.addVertex();
	 * int e1 = g.addEdge(v1, v2);
	 * int e2 = g.addEdge(v2, v3);
	 * int e3 = g.addEdge(v1, v3);
	 *
	 * // Assign some weights to the edges
	 * Weights.Int weights = g.addEdgesWeights("weightsKey", int.class);
	 * weights.set(e1, 1);
	 * weights.set(e2, 3);
	 * weights.set(e3, 15);
	 * EdgeWeightFunc.Int weightFunc = weights;
	 *
	 * // Calculate the shortest paths from v1 to all other vertices
	 * SSSP ssspAlgo = new SSSPDial();
	 * SSSP.Result ssspRes = ssspAlgo.computeShortestPaths(g, weightFunc, v1);
	 *
	 * // Print the shortest path from v1 to v3
	 * assert ssspRes.distance(v3) == 4;
	 * assert ssspRes.getPathTo(v3).equals(IntList.of(e1, e2));
	 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
	 * System.out.println("The shortest path from v1 to v3 is:");
	 * for (IntIterator it = ssspRes.getPathTo(v3).iterator(); it.hasNext();) {
	 * 	int e = it.nextInt();
	 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
	 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
	 * }
	 * }</pre>
	 *
	 * @see    SSSPDial
	 * @see    SSSPGoldberg
	 * @author Barak Ugav
	 */
	@FunctionalInterface
	public static interface Int extends EdgeWeightFunc {

		@Deprecated
		@Override
		default double weight(int e) {
			return weightInt(e);
		}

		/**
		 * Get the integer weight of an edge.
		 *
		 * @param  e                         an edge identifier
		 * @return                           the integer weight of the edge
		 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
		 */
		public int weightInt(int e);

		@Override
		default int compare(int e1, int e2) {
			return Integer.compare(weightInt(e1), weightInt(e2));
		}

	}

}
