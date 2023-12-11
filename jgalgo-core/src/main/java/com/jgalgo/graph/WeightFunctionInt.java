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

/**
 * Weight function that maps graph edges or vertices to integer weights.
 *
 * <p>
 * Some algorithms implementations support only integers weights, or run faster in such a case. This interface is the
 * API for these algorithms for the edges (or vertices) integer weights.
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
 * WeightsInt<Integer> w = g.addEdgesWeights("distance-km", int.class);
 * w.set(9, 191);
 * w.set(13, 193);
 * w.set(14, 121);
 *
 * // Calculate the shortest paths from Berlin to all other cities
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result<String, Integer> ssspRes = ssspAlgo.computeShortestPaths(g, w, "Berlin");
 *
 * // Print the shortest path from Berlin to Leipzig
 * System.out.println("Distance from Berlin to Leipzig is: " + ssspRes.distance("Leipzig"));
 * System.out.println("The shortest path from Berlin to Leipzig is:");
 * for (Integer e : ssspRes.getPath("Leipzig").edges()) {
 * 	String u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @param  <K> the elements (vertices/edges) type
 * @author     Barak Ugav
 */
@FunctionalInterface
public interface WeightFunctionInt<K> extends WeightFunction<K> {

	/**
	 * Get the integer weight of an element.
	 *
	 * @param  element               an element identifier
	 * @return                       the weight of the element
	 * @throws NoSuchVertexException if this weight function maps vertices and {@code element} is not a valid vertex
	 *                                   identifier in the graph
	 * @throws NoSuchEdgeException   if this weight function maps edges and {@code element} is not a valid edge
	 *                                   identifier in the graph
	 */
	public int weightInt(K element);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #weightInt(Object)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default double weight(K element) {
		return weightInt(element);
	}

	@Override
	default int compare(K e1, K e2) {
		return Integer.compare(weightInt(e1), weightInt(e2));
	}

	@Override
	default double weightSum(Iterable<K> elements) {
		long sum = 0;
		for (K e : elements)
			sum += weightInt(e);
		return sum;
	}

}
