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

import com.jgalgo.alg.color.ColoringAlgo;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;

/**
 * This example demonstrates how to use the coloring algorithm.
 *
 * @author Barak Ugav
 */
public class ColoringExample {

	private ColoringExample() {}

	/**
	 * This example demonstrates how to use the coloring algorithm.
	 */
	public static void coloringExample() {
		/* Create a graph with few vertices and edges */
		Graph<String, Integer> g = createGraph();

		/* Compute a color for each vertex, tying to minimize the number of colors used */
		ColoringAlgo coloringAlgo = ColoringAlgo.newInstance();
		VertexPartition<String, Integer> colors = coloringAlgo.computeColoring(g);

		for (String u : g.vertices()) {
			int uColor = colors.vertexBlock(u);
			System.out.println("The color of " + u + " is " + uColor);

			/* For each edge (u,v), the endpoints u and v have different colors */
			for (EdgeIter<String, Integer> eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.next();
				String v = eit.target();
				int vColor = colors.vertexBlock(v);
				assert uColor != vColor;
			}
		}
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
		coloringExample();
	}

}
