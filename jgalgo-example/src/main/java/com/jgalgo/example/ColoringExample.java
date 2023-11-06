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

import com.jgalgo.alg.ColoringAlgo;
import com.jgalgo.alg.IVertexPartition;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;

/**
 * This example demonstrates how to use the coloring algorithm.
 *
 * @author Barak Ugav
 */
public class ColoringExample {

	/**
	 * This example demonstrates how to use the coloring algorithm.
	 */
	public static void coloringExample() {
		/* Create a graph with few vertices and edges */
		IntGraph g = createGraph();

		/* Compute a color for each vertex, tying to minimize the number of colors used */
		ColoringAlgo coloringAlgo = ColoringAlgo.newInstance();
		IVertexPartition colors = (IVertexPartition) coloringAlgo.computeColoring(g);

		for (int u : g.vertices()) {
			int uColor = colors.vertexBlock(u);
			System.out.println("The color of " + u + " is " + uColor);

			/* For each edge (u,v), the endpoints u and v have different colors */
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				int vColor = colors.vertexBlock(v);
				assert uColor != vColor;
			}
		}
	}

	private static IntGraph createGraph() {
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
		coloringExample();
	}

}
