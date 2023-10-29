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

import java.util.Collection;
import com.jgalgo.alg.MatchingAlgo;
import com.jgalgo.alg.MinimumSpanningTree;
import com.jgalgo.alg.ShortestPathSingleSource;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterable;

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
 * IWeightsDouble weights = g.addEdgesWeights("weightsKey", double.class);
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
 * @see    IWeightFunctionInt
 * @author Barak Ugav
 */
@FunctionalInterface
public interface IWeightFunction extends IntComparator {

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
	 * Get the sum of the weights of multiple elements.
	 *
	 * @param  elements a collection of elements
	 * @return          the sum of the weights of the elements
	 */
	default double weightSum(IntIterable elements) {
		double sum = 0;
		for (int e : elements)
			sum += weight(e);
		return sum;
	}

	/**
	 * Get the sum of the weights of multiple elements.
	 * <p>
	 * This method is equivalent to {@link #weightSum(IntIterable)}, but it also support {@code null} weight function,
	 * which is treated is cardinality weight function.
	 *
	 * @param  weightFunc the weight function to use, or {@code null} to use cardinality weight function
	 * @param  elements   a collection of elements
	 * @return            the sum of the weights of the elements
	 */
	static double weightSum(IWeightFunction weightFunc, IntIterable elements) {
		if (weightFunc == null || weightFunc == CardinalityWeightFunction) {
			if (elements instanceof Collection) {
				return ((Collection<?>) elements).size();
			} else {
				int s = 0;
				for (@SuppressWarnings("unused")
				int elm : elements)
					s++;
				return s;
			}
		} else {
			return weightFunc.weightSum(elements);
		}
	}

	/**
	 * A weight function that assign a weight of {@code 1} to any element.
	 */
	public static IWeightFunctionInt CardinalityWeightFunction = e -> 1;

}
