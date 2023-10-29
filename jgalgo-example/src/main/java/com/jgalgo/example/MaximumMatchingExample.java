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
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class MaximumMatchingExample {

	public static void maximumMatchingExample() {
		/* Create a graph with few vertices and edges */
		IntGraph g = createGraph();

		/* Compute a maximum (cardinality) matching */
		MatchingAlgo matchingAlgo = MatchingAlgo.newInstance();
		Matching matching = matchingAlgo.computeMaximumCardinalityMatching(g);

		/* Validate the matching is valid */
		for (int u : g.vertices()) {

			/* Find the matched edges adjacent to u */
			IntSet uEdges = new IntOpenHashSet(g.outEdges(u));
			uEdges.removeIf(e -> !matching.containsEdge(e));

			/* No vertex is allowed to have more than one matched edge */
			assert uEdges.size() <= 1;
		}

		System.out.println("The maximum matching in the graph has a size of " + matching.edges().size());
		System.out.println("The maximum matching is: " + matching.edges());
	}

	public static IntGraph createGraph() {
		IntGraph g = IntGraph.newUndirected();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		int v4 = g.addVertex();
		int v5 = g.addVertex();
		int v6 = g.addVertex();
		int v7 = g.addVertex();

		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v1, v3);
		g.addEdge(v7, v5);
		g.addEdge(v6, v1);
		g.addEdge(v3, v4);

		return g;
	}

	public static void main(String[] args) {
		maximumMatchingExample();
	}

}
