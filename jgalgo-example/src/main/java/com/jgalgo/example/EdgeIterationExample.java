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

import java.util.Set;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This example demonstrates how to iterate over the edges of a graph.
 *
 * @author Barak Ugav
 */
public class EdgeIterationExample {

	private EdgeIterationExample() {}

	/**
	 * This example demonstrates how to iterate over the edges of a graph.
	 */
	@SuppressWarnings("boxing")
	public static void edgeIterationExample() {
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
		g.addEdge("Miller", "Smith", 5);
		g.addEdge("Williams", "Jones", 6);
		g.addEdge("Brown", "Jones", 7);

		/* Query the edges of some vertices */
		/* The EdgeIter returned by outEdges is an int iterator that yield edges identifiers */
		Set<Integer> v1Edges = g.outEdges("Smith");
		assert v1Edges.equals(IntSet.of(1, 3, 5));

		Set<Integer> v2Edges = g.outEdges("Johnson");
		assert v2Edges.equals(IntSet.of(1, 2));

		Set<Integer> v3Edges = g.outEdges("Williams");
		assert v3Edges.equals(IntSet.of(2, 3, 6));

		/* Print the out-edges of Brown */
		System.out.println("The edges of Brown:");
		for (EdgeIter<String, Integer> eit = g.outEdges("Brown").iterator(); eit.hasNext();) {
			Integer e = eit.next();
			/* EdgeIter.source() and EdgeIter.target() can be used to get the endpoints of the last returned edge */
			String u = eit.source();
			String v = eit.target();

			/* If the iterator was created using g.outEdges(u).iterator(), EdgeIter.source() will always be u */
			/* If the iterator was created using g.inEdges(v).iterator(), EdgeIter.target() will always be v */
			assert u.equals("Brown");
			assert Set.of(4, 7).contains(e);
			System.out.println("\t" + e + "=(" + u + ", " + v + ")");
		}
	}

	public static void main(String[] args) {
		edgeIterationExample();
	}

}
