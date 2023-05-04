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

import com.jgalgo.Graph;
import com.jgalgo.SSSP;
import com.jgalgo.Weights;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class ShortestPathExample {

	public static void shortestPathExample() {
		/* Create a directed graph with three vertices and edges between them */
		Graph g = Graph.newBuilderDirected().build();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v3);
		int e3 = g.addEdge(v1, v3);

		/* Assign some weights to the edges */
		Weights.Double w = g.addEdgesWeights("weightsKey", double.class);
		w.set(e1, 1.2);
		w.set(e2, 3.1);
		w.set(e3, 15.1);

		/* Calculate the shortest paths from v1 to all other vertices */
		SSSP ssspAlgo = SSSP.newBuilder().build();
		SSSP.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);

		assert ssspRes.distance(v3) == 4.3;
		assert ssspRes.getPath(v3).equals(IntList.of(e1, e2));
		System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));

		/* Print the shortest path from v1 to v3 */
		System.out.println("The shortest path from v1 to v3 is:");
		for (IntIterator it = ssspRes.getPath(v3).iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			System.out.println(" " + e + "(" + u + ", " + v + ")");
		}
	}

	public static void main(String[] args) {
		shortestPathExample();
	}

}
