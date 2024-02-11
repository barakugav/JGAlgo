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

import com.jgalgo.alg.Matching;
import com.jgalgo.alg.MatchingAlgo;
import com.jgalgo.graph.Graph;

/**
 * This example demonstrates how to use the maximum matching algorithm.
 *
 * @author Barak Ugav
 */
public class MaximumMatchingExample {

	private MaximumMatchingExample() {}

	/**
	 * This example demonstrates how to use the maximum matching algorithm.
	 */
	public static void maximumMatchingExample() {
		/* Create a graph with few vertices and edges */
		Graph<String, Integer> g = createGraph();

		/* Compute a maximum (cardinality) matching */
		MatchingAlgo matchingAlgo = MatchingAlgo.newInstance();
		Matching<String, Integer> matching = matchingAlgo.computeMaximumMatching(g, null);

		/* Validate the matching is valid */
		for (String u : g.vertices()) {

			/* No vertex is allowed to have more than one matched edge */
			assert g.outEdges(u).stream().filter(matching::containsEdge).count() <= 1;
		}

		System.out.println("The maximum matching in the graph has a size of " + matching.edges().size());
		System.out.println("The maximum matching is: " + matching.edges());
	}

	@SuppressWarnings("boxing")
	private static Graph<String, Integer> createGraph() {
		Graph<String, Integer> g = Graph.newUndirected();
		g.addVertex("Smith");
		g.addVertex("Johnson");
		g.addVertex("Williams");
		g.addVertex("Jones");
		g.addVertex("Brown");
		g.addVertex("Davis");
		g.addVertex("Miller");

		g.addEdge("Smith", "Johnson", 1);
		g.addEdge("Johnson", "Williams", 2);
		g.addEdge("Smith", "Williams", 3);
		g.addEdge("Miller", "Brown", 4);
		g.addEdge("Davis", "Smith", 5);
		g.addEdge("Williams", "Jones", 6);

		return g;
	}

	/**
	 * Main function that runs the examples.
	 *
	 * @param args main args
	 */
	public static void main(String[] args) {
		maximumMatchingExample();
	}

}
