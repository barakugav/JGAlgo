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

package com.jgalgo.graph;

import com.jgalgo.alg.MatchingAlgo;
import com.jgalgo.alg.MinimumSpanningTree;
import com.jgalgo.alg.ShortestPathSingleSource;
import it.unimi.dsi.fastutil.ints.IntComparator;

/**
 * Weight function that maps graph edges (or vertices) to weights.
 * <p>
 * This interface is usually used as weight function of edges, for example in algorithms such as
 * {@link ShortestPathSingleSource}, {@link MinimumSpanningTree} and {@link MatchingAlgo} try to find a set of edges
 * satisfying some constraint while minimizing/maximizing some objective function based on the weights of the edges. But
 * it can represent weights assigned to vertices, in algorithms such as vertex cover.
 * <p>
 * An instance of this interface represent weights of edges only or vertices only, and never both. As this function
 * represent weights for either edges or vertex, the documentation refer to these edges/vertices as <i>elements</i>.
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * Graph g = Graph.newDirected();
 * int v1 = g.addVertex();
 * int v2 = g.addVertex();
 * int v3 = g.addVertex();
 * int e1 = g.addEdge(v1, v2);
 * int e2 = g.addEdge(v2, v3);
 * int e3 = g.addEdge(v1, v3);
 *
 * // Assign some weights to the edges
 * WeightsDouble weights = g.addEdgesWeights("weightsKey", double.class);
 * weights.set(e1, 1.2);
 * weights.set(e2, 3.1);
 * weights.set(e3, 15.1);
 * EdgeWeightFunc weightFunc = weights;
 *
 * // Calculate the shortest paths from v1 to all other vertices
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, weightFunc, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPath(v3).equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (int e : ssspRes.getPath(v3)) {
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @author Barak Ugav
 */
@FunctionalInterface
public interface WeightFunction extends IntComparator {

	/**
	 * Get the weight of an element.
	 *
	 * @param  element                   an element identifier
	 * @return                           the weight of the element
	 * @throws IndexOutOfBoundsException if {@code element} is not a valid element identifier in the graph
	 */
	public double weight(int element);

	/**
	 * Compare two elements by their weights.
	 */
	@Override
	default int compare(int e1, int e2) {
		return Double.compare(weight(e1), weight(e2));
	}

	/**
	 * A weight function that assign a weight of {@code 1} to any element.
	 */
	public static WeightFunctionInt CardinalityWeightFunction = e -> 1;

}
