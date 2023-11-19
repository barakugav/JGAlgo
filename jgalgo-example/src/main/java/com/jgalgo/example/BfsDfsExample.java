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

import java.util.List;
import com.jgalgo.alg.Bfs;
import com.jgalgo.alg.Dfs;
import com.jgalgo.graph.Graph;

/**
 * This example demonstrates how to use the BFS and DFS algorithms.
 *
 * @author Barak Ugav
 */
public class BfsDfsExample {

	private BfsDfsExample() {}

	/**
	 * This example demonstrates how to use the BFS algorithm.
	 */
	public static void bfsExample() {
		/* Create a graph and choose an arbitrary source vertex */
		Graph<String, Integer> g = createGraph();
		String source = g.vertices().iterator().next();

		/* Iterate over the graph vertices in a breadth-first search (BFS) order */
		for (Bfs.Iter<String, Integer> iter = Bfs.newInstance(g, source); iter.hasNext();) {
			/* v is a vertex the iterator didn't visit before */
			String v = iter.next();
			/* e is the edge used to reach v */
			/* In a directed graph, v is the 'target' of e */
			Integer e = iter.lastEdge();
			/* the layer is the distance of v from the source vertex */
			int layer = iter.layer();
			System.out.println("BFS reached vertex " + v + " at layer " + layer + " using edge " + e);
		}
	}

	/**
	 * This example demonstrates how to use the DFS algorithm.
	 */
	public static void dfsExample() {
		/* Create a graph and choose an arbitrary source vertex */
		Graph<String, Integer> g = createGraph();
		String source = g.vertices().iterator().next();

		/* Iterate over the graph vertices in a depth-first search (DFS) order */
		for (Dfs.Iter<String, Integer> iter = Dfs.newInstance(g, source); iter.hasNext();) {
			/* v is a vertex the iterator didn't visit before */
			String v = iter.next();
			/* edgePath is a list of edges, forming a path from the source to v */
			List<Integer> edgePath = iter.edgePath();
			System.out.println("Reached vertex " + v + " using the edges: " + edgePath);
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

	public static void main(String[] args) {
		bfsExample();
		dfsExample();
	}

}
