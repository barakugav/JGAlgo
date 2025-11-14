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
import com.jgalgo.alg.cover.VertexCover;
import com.jgalgo.alg.match.MatchingAlgo;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSource;
import com.jgalgo.alg.span.MinimumSpanningTree;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterables;

/**
 * Weight function that maps graph edges or vertices of {@link IntGraph} to weights.
 *
 * <p>
 * This interface is a specification of {@link WeightFunction} for {@link IntGraph}.
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
 * // Create a directed graph with three vertices and edges between them
 * IntGraph g = IntGraph.newDirected();
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
 * IWeightFunction weightFunc = weights;
 *
 * // Calculate the shortest paths from v1 to all other vertices
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, weightFunc, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPath(v3).edges().equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (int e : ssspRes.getPath(v3).edges()) {
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    IWeightFunctionInt
 * @author Barak Ugav
 */
@FunctionalInterface
public interface IWeightFunction extends WeightFunction<Integer>, IntComparator {

	/**
	 * Get the weight of an element.
	 *
	 * @param  element               an element identifier
	 * @return                       the weight of the element
	 * @throws NoSuchVertexException if this weight container holds vertices weights and {@code element} is not a valid
	 *                                   vertex identifier in the graph
	 * @throws NoSuchEdgeException   if this weight container holds edges weights and {@code element} is not a valid
	 *                                   edge identifier in the graph
	 */
	double weight(int element);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #weight(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default double weight(Integer element) {
		return weight(element.intValue());
	}

	/**
	 * Compare two elements by their weights.
	 */
	@Override
	default int compare(int e1, int e2) {
		return Double.compare(weight(e1), weight(e2));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #compare(int, int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default int compare(Integer e1, Integer e2) {
		return Double.compare(weight(e1), weight(e2));
	}

	@Override
	default double weightSum(Iterable<Integer> elements) {
		double sum = 0;
		if (elements instanceof IntIterable) {
			for (int e : (IntIterable) elements)
				sum += weight(e);
		} else {
			for (Integer e : elements)
				sum += weight(e.intValue());
		}
		return sum;
	}

	/**
	 * Get the sum of the weights of multiple elements.
	 *
	 * <p>
	 * This method is equivalent to {@link #weightSum(Iterable)}, but it also support {@code null} weight function,
	 * which is treated is cardinality weight function.
	 *
	 * @param  weightFunc the weight function to use, or {@code null} to use cardinality weight function
	 * @param  elements   a collection of elements
	 * @return            the sum of the weights of the elements
	 */
	static double weightSum(IWeightFunction weightFunc, IntIterable elements) {
		if (WeightFunction.isCardinality(weightFunc)) {
			return elements instanceof Collection ? ((Collection<?>) elements).size() : IntIterables.size(elements);
		} else {
			return weightFunc.weightSum(elements);
		}
	}

	/**
	 * A weight function that assign a weight of {@code 1} to any element.
	 */
	public static IWeightFunctionInt CardinalityWeightFunction = e -> 1;

	/**
	 * Replace {@code null} weight function with {@link #CardinalityWeightFunction}.
	 *
	 * @param  weightFunc the weight function to replace
	 * @return            {@link #CardinalityWeightFunction} if {@code weightFunc} is {@code null}, otherwise
	 *                    {@code weightFunc}
	 */
	public static IWeightFunction replaceNullWeightFunc(IWeightFunction weightFunc) {
		return weightFunc == null ? CardinalityWeightFunction : weightFunc;
	}

	/**
	 * Replace {@code null} weight function with {@link #CardinalityWeightFunction}.
	 *
	 * @param  weightFunc the weight function to replace
	 * @return            {@link #CardinalityWeightFunction} if {@code weightFunc} is {@code null}, otherwise
	 *                    {@code weightFunc}
	 */
	public static IWeightFunctionInt replaceNullWeightFunc(IWeightFunctionInt weightFunc) {
		return weightFunc == null ? CardinalityWeightFunction : weightFunc;
	}

}
