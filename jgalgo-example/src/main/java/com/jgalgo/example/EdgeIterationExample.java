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

import com.jgalgo.EdgeIter;
import com.jgalgo.Graph;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class EdgeIterationExample {

	public static void edgeIterationExample() {
		Graph g = Graph.newBuilderUndirected().build();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		int v4 = g.addVertex();
		int v5 = g.addVertex();
		int v6 = g.addVertex();
		int v7 = g.addVertex();

		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v3);
		int e3 = g.addEdge(v1, v3);
		int e4 = g.addEdge(v7, v5);
		int e5 = g.addEdge(v6, v1);
		int e6 = g.addEdge(v3, v4);
		int e7 = g.addEdge(v5, v4);

		/* Query the edges of some vertices */
		/* The EdgeIter returned by edgesOut is an int iterator that yield edges identifiers */
		IntSet v1Edges = new IntOpenHashSet(g.edgesOut(v1));
		assert v1Edges.equals(IntSet.of(e1, e3, e5));

		IntSet v2Edges = new IntOpenHashSet(g.edgesOut(v2));
		assert v2Edges.equals(IntSet.of(e1, e2));

		IntSet v3Edges = new IntOpenHashSet(g.edgesOut(v3));
		assert v3Edges.equals(IntSet.of(e2, e3, e6));

		/* Print the out-edges of v5 */
		System.out.println("The edges of v5:");
		for (EdgeIter eit = g.edgesOut(v5); eit.hasNext();) {
			int e = eit.nextInt();
			/* EdgeIter.source() and EdgeIter.target() can be used to get the endpoints of the last returned edge */
			int u = eit.source();
			int v = eit.target();

			/* If the iterator was created using g.edgesOut(u), EdgeIter.source() will always be u */
			/* If the iterator was created using g.edgesIn(v), EdgeIter.target() will always be v */
			assert u == v5;
			assert IntSet.of(e4, e7).contains(e);
			System.out.println("\t" + e + "=(" + u + ", " + v + ")");
		}

	}

	public static void main(String[] args) {
		edgeIterationExample();
	}

}
