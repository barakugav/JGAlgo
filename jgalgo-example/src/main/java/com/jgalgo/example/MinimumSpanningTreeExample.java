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

import com.jgalgo.alg.MinimumSpanningTree;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsDouble;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This example demonstrates how to use the minimum spanning tree algorithm.
 *
 * @author Barak Ugav
 */
public class MinimumSpanningTreeExample {

	private MinimumSpanningTreeExample() {}

	/**
	 * This example demonstrates how to use the minimum spanning tree algorithm.
	 */
	@SuppressWarnings("boxing")
	public static void mstExample() {
		/* Create a graph with 7 vertices */
		Graph<String, Integer> g = Graph.newUndirected();
		g.addVertex("Smith");
		g.addVertex("Johnson");
		g.addVertex("Williams");
		g.addVertex("Jones");
		g.addVertex("Brown");
		g.addVertex("Davis");
		g.addVertex("Miller");

		/* Add a few edges between the vertices */
		g.addEdge("Smith", "Johnson", 1);
		g.addEdge("Johnson", "Williams", 2);
		g.addEdge("Smith", "Williams", 3);
		g.addEdge("Miller", "Brown", 4);
		g.addEdge("Davis", "Smith", 5);
		g.addEdge("Williams", "Jones", 6);
		g.addEdge("Williams", "Davis", 7);
		g.addEdge("Smith", "Johnson", 8);
		g.addEdge("Brown", "Davis", 9);
		g.addEdge("Williams", "Brown", 10);
		g.addEdge("Miller", "Jones", 11);
		g.addEdge("Williams", "Davis", 12);

		/* Assign a weight to each edge */
		WeightsDouble<Integer> weights = g.addEdgesWeights("weightsKey", double.class);
		weights.set(1, 4.6);
		weights.set(2, 5.2);
		weights.set(3, 6.1);
		weights.set(4, 1.5);
		weights.set(5, 1.8);
		weights.set(6, 4.1);
		weights.set(7, 7.2);
		weights.set(8, 5.4);
		weights.set(9, 6.3);
		weights.set(10, 5.7);
		weights.set(11, 1.9);
		weights.set(12, 5.5);

		/* Compute the minimum spanning tree of the graph */
		WeightFunction<Integer> w = weights;
		MinimumSpanningTree mstAlgo = MinimumSpanningTree.newInstance();
		MinimumSpanningTree.Result<String, Integer> mst = mstAlgo.computeMinimumSpanningTree(g, w);

		assert IntSet.of(1, 2, 4, 5, 6, 11).equals(mst.edges());
		System.out.println("The minimum spanning tree of the graph has the following edges: " + mst.edges());
		System.out.println("The minimum spanning tree weight is " + w.weightSum(mst.edges()));
	}

	public static void main(String[] args) {
		mstExample();
	}

}
