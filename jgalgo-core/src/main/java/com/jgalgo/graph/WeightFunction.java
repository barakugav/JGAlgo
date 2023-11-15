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
import java.util.Comparator;
import com.jgalgo.alg.MatchingAlgo;
import com.jgalgo.alg.MinimumSpanningTree;
import com.jgalgo.alg.ShortestPathSingleSource;
import com.jgalgo.alg.VertexCover;

/**
 * Weight function that maps graph edges or vertices to weights.
 *
 * <p>
 * This interface is usually used as weight function of edges, for example in algorithms such as
 * {@link ShortestPathSingleSource}, {@link MinimumSpanningTree} and {@link MatchingAlgo}, in which the algorithm try to
 * find a set of edges satisfying some constraint while minimizing/maximizing some objective function based on the
 * weights of the edges. But it can represent weights assigned to vertices, in algorithms such as {@link VertexCover}.
 *
 * <p>
 * An instance of this interface represent weights of edges only or vertices only, and never both. As this function
 * represent weights for either edges or vertex, the documentation refer to these edges/vertices as <i>elements</i>.
 *
 * <pre> {@code
 * // Create an undirected graph with three vertices and edges between them
 * Graph<String, Integer> g = Graph.newUndirected();
 * g.addVertex("Berlin");
 * g.addVertex("Leipzig");
 * g.addVertex("Dresden");
 * g.addEdge("Berlin", "Leipzig", 9);
 * g.addEdge("Berlin", "Dresden", 13);
 * g.addEdge("Dresden", "Leipzig", 14);
 *
 * // Assign some weights to the edges
 * WeightsDouble<Integer> w = g.addEdgesWeights("distance-km", double.class);
 * w.set(9, 191.1);
 * w.set(13, 193.3);
 * w.set(14, 121.3);
 *
 * // Calculate the shortest paths from Berlin to all other cities
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, "Berlin");
 *
 * // Print the shortest path from Berlin to Leipzig
 * System.out.println("Distance from Berlin to Leipzig is: " + ssspRes.distance("Leipzig"));
 * System.out.println("The shortest path from Berlin to Leipzig is:");
 * for (int e : ssspRes.getPath("Leipzig")) {
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @param  <K> the elements (vertices/edges) type
 * @see        WeightFunctionInt
 * @author     Barak Ugav
 */
@FunctionalInterface
public interface WeightFunction<K> extends Comparator<K> {

	/**
	 * Get the weight of an element.
	 *
	 * @param  element               an element identifier
	 * @return                       the weight of the element
	 * @throws NoSuchVertexException if this weight function maps vertices and {@code element} is not a valid vertex
	 *                                   identifier in the graph
	 * @throws NoSuchEdgeException   if this weight function maps edges and {@code element} is not a valid edge
	 *                                   identifier in the graph
	 */
	double weight(K element);

	/**
	 * Compare two elements by their weights.
	 */
	@Override
	default int compare(K e1, K e2) {
		return Double.compare(weight(e1), weight(e2));
	}

	/**
	 * Get the sum of the weights of multiple elements.
	 *
	 * @param  elements a collection of elements
	 * @return          the sum of the weights of the elements
	 */
	default double weightSum(Iterable<K> elements) {
		double sum = 0;
		for (K e : elements)
			sum += weight(e);
		return sum;
	}

	/**
	 * Get the sum of the weights of multiple elements.
	 *
	 * <p>
	 * This method is equivalent to {@link #weightSum(Iterable)}, but it also support {@code null} weight function,
	 * which is treated is cardinality weight function.
	 *
	 * @param  <K>        the elements (vertices/edges) type
	 * @param  weightFunc the weight function to use, or {@code null} to use cardinality weight function
	 * @param  elements   a collection of elements
	 * @return            the sum of the weights of the elements
	 */
	static <K> double weightSum(WeightFunction<K> weightFunc, Iterable<K> elements) {
		if (isCardinality(weightFunc)) {
			if (elements instanceof Collection) {
				return ((Collection<?>) elements).size();
			} else {
				int s = 0;
				for (@SuppressWarnings("unused")
				K elm : elements)
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
	public static WeightFunctionInt<?> CardinalityWeightFunction = e -> 1;

	/**
	 * Get the cardinality weight function.
	 *
	 * <p>
	 * The cardinality weight function assign a weight of {@code 1} to any element. The function always return the same
	 * object, which can be accessed directed via {@link #CardinalityWeightFunction}. This is method is exposed only to
	 * avoid unchecked casts with generics.
	 *
	 * @param  <K> the type of the elements
	 * @return     the cardinality weight function
	 */
	@SuppressWarnings("unchecked")
	public static <K> WeightFunctionInt<K> cardinalityWeightFunction() {
		return (WeightFunctionInt<K>) CardinalityWeightFunction;
	}

	/**
	 * Check if the given weight function is the cardinality weight function.
	 *
	 * <p>
	 * This function does not checks that the weight function maps every element to {@code 1}, but only compares it to
	 * the static weight functions {@link #CardinalityWeightFunction} and
	 * {@link IWeightFunction#CardinalityWeightFunction}, or {@code null}.
	 *
	 * @param  weightFunc the weight function to check
	 * @return            {@code true} if the weight function is the cardinality weight function, {@code false}
	 */
	public static boolean isCardinality(WeightFunction<?> weightFunc) {
		return weightFunc == null || weightFunc == CardinalityWeightFunction
				|| weightFunc == IWeightFunction.CardinalityWeightFunction;
	}

	/**
	 * Check if the given weight function is an integer weight function.
	 *
	 * <p>
	 * This function does not checks that the weight function maps every element to an integer, but only checks that the
	 * weight function is an instance of {@link WeightFunctionInt} or it is {@code null}.
	 *
	 * @param  weightFunc the weight function to check
	 * @return            {@code true} if the weight function is an integer weight function, {@code false}
	 */
	public static boolean isInteger(WeightFunction<?> weightFunc) {
		return weightFunc == null || weightFunc instanceof WeightFunctionInt<?>;
	}

}
