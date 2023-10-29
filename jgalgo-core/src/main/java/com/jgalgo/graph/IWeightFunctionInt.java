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

import it.unimi.dsi.fastutil.ints.IntIterable;

/**
 * Weight function that maps graph edges (or vertices) to integer weights.
 * <p>
 * Some algorithms implementations support only integers weights, or run faster in such a case. This interface is the
 * API for these algorithms for the edges (or vertices) integer weights.
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
 * IWeightsInt weights = g.addEdgesWeights("weightsKey", int.class);
 * weights.set(e1, 1);
 * weights.set(e2, 3);
 * weights.set(e3, 15);
 * EdgeWeightFunc.Int weightFunc = weights;
 *
 * // Calculate the shortest paths from v1 to all other vertices
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newBuilder().setIntWeights(true).build();
 * ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, weightFunc, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4;
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
public interface IWeightFunctionInt extends IWeightFunction {

	@Deprecated
	@Override
	default double weight(int element) {
		return weightInt(element);
	}

	/**
	 * Get the integer weight of an element.
	 *
	 * @param  element                   an element identifier
	 * @return                           the integer weight of the element
	 * @throws IndexOutOfBoundsException if {@code element} is not a valid element identifier
	 */
	public int weightInt(int element);

	@Override
	default int compare(int e1, int e2) {
		return Integer.compare(weightInt(e1), weightInt(e2));
	}

	@Override
	default double weightSum(IntIterable elements) {
		long sum = 0;
		for (int e : elements)
			sum += weightInt(e);
		return sum;
	}

}
