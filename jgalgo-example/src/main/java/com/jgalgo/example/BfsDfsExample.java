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

import com.jgalgo.alg.Dfs;
import com.jgalgo.alg.Bfs;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntList;

public class BfsDfsExample {

	public static void BFSExample() {
		/* Create a graph and choose an arbitrary source vertex */
		IntGraph g = createGraph();
		int source = g.vertices().iterator().nextInt();

		/* Iterate over the graph vertices in a breadth-first search (BFS) order */
		for (Bfs.IntIter iter = Bfs.newInstance(g, source); iter.hasNext();) {
			/* v is a vertex the iterator didn't visit before */
			int v = iter.nextInt();
			/* e is the edge used to reach v */
			/* In a directed graph, v is the 'target' of e */
			int e = iter.lastEdgeInt();
			/* the layer is the distance of v from the source vertex */
			int layer = iter.layer();
			System.out.println("BFS reached vertex " + v + " at layer " + layer + " using edge " + e);
		}
	}

	public static void DFSExample() {
		/* Create a graph and choose an arbitrary source vertex */
		IntGraph g = createGraph();
		int source = g.vertices().iterator().nextInt();

		/* Iterate over the graph vertices in a depth-first search (DFS) order */
		for (Dfs.IntIter iter = Dfs.newInstance(g, source); iter.hasNext();) {
			/* v is a vertex the iterator didn't visit before */
			int v = iter.nextInt();
			/* edgePath is a list of edges, forming a path from the source to v */
			IntList edgePath = iter.edgePath();
			System.out.println("Reached vertex " + v + " using the edges: " + edgePath);
		}
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
		BFSExample();
		DFSExample();
	}

}
