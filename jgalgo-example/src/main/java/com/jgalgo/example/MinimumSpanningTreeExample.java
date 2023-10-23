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
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.ints.IntSet;

public class MinimumSpanningTreeExample {

	public static void MSTExample() {
		/* Create a graph with 7 vertices */
		Graph g = GraphFactory.newUndirected().newGraph();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		int v4 = g.addVertex();
		int v5 = g.addVertex();
		int v6 = g.addVertex();
		int v7 = g.addVertex();

		/* Add a few edges between the vertices */
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v3);
		int e3 = g.addEdge(v1, v3);
		int e4 = g.addEdge(v7, v5);
		int e5 = g.addEdge(v6, v1);
		int e6 = g.addEdge(v3, v4);
		int e7 = g.addEdge(v3, v6);
		int e8 = g.addEdge(v1, v2);
		int e9 = g.addEdge(v5, v6);
		int e10 = g.addEdge(v3, v5);
		int e11 = g.addEdge(v7, v4);
		int e12 = g.addEdge(v3, v6);

		/* Assign a weight to each edge */
		Weights.Double weights = g.addEdgesWeights("weightsKey", double.class);
		weights.set(e1, 4.6);
		weights.set(e2, 5.2);
		weights.set(e3, 6.1);
		weights.set(e4, 1.5);
		weights.set(e5, 1.8);
		weights.set(e6, 4.1);
		weights.set(e7, 7.2);
		weights.set(e8, 5.4);
		weights.set(e9, 6.3);
		weights.set(e10, 5.7);
		weights.set(e11, 1.9);
		weights.set(e12, 5.5);

		/* Compute the minimum spanning tree of the graph */
		WeightFunction w = weights;
		MinimumSpanningTree mstAlgo = MinimumSpanningTree.newBuilder().build();
		MinimumSpanningTree.Result mst = mstAlgo.computeMinimumSpanningTree(g, w);

		assert IntSet.of(e1, e2, e4, e5, e6, e11).equals(IntSet.of(mst.edges().toIntArray()));
		System.out.println("The minimum spanning tree of the graph has the following edges: " + mst.edges());
		System.out.println("The minimum spanning tree weight is " + mst.weight(w));
	}

	public static void main(String[] args) {
		MSTExample();
	}

}
