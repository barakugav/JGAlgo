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
package com.jgalgo.demo;

import com.jgalgo.BFSIter;
import com.jgalgo.DFSIter;
import com.jgalgo.Graph;
import it.unimi.dsi.fastutil.ints.IntList;

public class BfsDfsExample {

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

	public static void BFSExample() {
		Graph g = createGraph();
		int source = g.vertices().iterator().nextInt();

		for (BFSIter iter = new BFSIter(g, source); iter.hasNext();) {
			int v = iter.nextInt();
			int e = iter.inEdge();
			int layer = iter.layer();
			System.out.println("BFS reached vertex " + v + " at layer " + layer + " using edge " + e);
		}
	}

	public static void DFSExample() {
		Graph g = createGraph();
		int source = g.vertices().iterator().nextInt();

		for (DFSIter iter = new DFSIter(g, source); iter.hasNext();) {
			int v = iter.nextInt();
			IntList edgePath = iter.edgePath();
			System.out.println("Reached vertex " + v + " using the edges: " + edgePath);
		}
	}

	public static void main(String[] args) {
		BFSExample();
		DFSExample();
	}

}
