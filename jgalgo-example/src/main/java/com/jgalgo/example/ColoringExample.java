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

import com.jgalgo.Coloring;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;

public class ColoringExample {

	public static void coloringExample() {
		/* Create a graph with few vertices and edges */
		Graph g = createGraph();

		/* Compute a color for each vertex, tying to minimize the number of colors used */
		Coloring coloringAlgo = Coloring.newBuilder().build();
		Coloring.Result colors = coloringAlgo.computeColoring(g);

		for (int u : g.vertices()) {
			int uColor = colors.colorOf(u);
			System.out.println("The color of " + u + " is " + uColor);

			/* For each edge (u,v), the endpoints u and v have different colors */
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				int vColor = colors.colorOf(v);
				assert uColor != vColor;
			}
		}
	}

	public static Graph createGraph() {
		Graph g = Graph.newBuilderUndirected().build();
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
