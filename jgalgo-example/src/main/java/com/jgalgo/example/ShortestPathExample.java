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

package com.jgalgo.example;

import com.jgalgo.alg.ShortestPathSingleSource;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightsDouble;

/**
 * This example demonstrates how to use the single-source shortest path algorithm.
 *
 * @author Barak Ugav
 */
public class ShortestPathExample {

	private ShortestPathExample() {}

	/**
	 * This example demonstrates how to use the single-source shortest path algorithm.
	 */
	@SuppressWarnings("boxing")
	public static void shortestPathExample() {
		/* Create an undirected graph with three vertices and edges between them */
		Graph<String, Integer> g = Graph.newUndirected();
		g.addVertex("Berlin");
		g.addVertex("Leipzig");
		g.addVertex("Dresden");
		g.addEdge("Berlin", "Leipzig", 9);
		g.addEdge("Berlin", "Dresden", 13);
		g.addEdge("Dresden", "Leipzig", 14);

		/* Assign some weights to the edges */
		WeightsDouble<Integer> w = g.addEdgesWeights("distance-km", double.class);
		w.set(9, 191.1);
		w.set(13, 193.3);
		w.set(14, 121.3);

		/* Calculate the shortest paths from Berlin to all other cities */
		ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
		ShortestPathSingleSource.Result<String, Integer> ssspRes = ssspAlgo.computeShortestPaths(g, w, "Berlin");

		/* Print the shortest path from Berlin to Leipzig */
		System.out.println("Distance from Berlin to Leipzig is: " + ssspRes.distance("Leipzig"));
		System.out.println("The shortest path from Berlin to Leipzig is:");
		for (Integer e : ssspRes.getPath("Leipzig").edges()) {
			String u = g.edgeSource(e), v = g.edgeTarget(e);
			System.out.println(" " + e + "(" + u + ", " + v + ")");
		}
	}

	public static void main(String[] args) {
		shortestPathExample();
	}

}
