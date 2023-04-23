package com.jgalgo.example;

import org.junit.jupiter.api.Test;

import com.jgalgo.DiGraph;
import com.jgalgo.GraphArrayDirected;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPDijkstra;
import com.jgalgo.Weights;
import com.jgalgo.test.TestBase;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class ReadmeExample extends TestBase {

	@Test
	public void testReadmeExample() {
		/* Create a directed graph with three vertices and edges between them */
		DiGraph g = new GraphArrayDirected();
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
		SSSP ssspAlgo = new SSSPDijkstra();
		SSSP.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);

		/* Print the shortest path from v1 to v3 */
		assert ssspRes.distance(v3) == 4.3;
		assert ssspRes.getPath(v3).equals(IntList.of(e1, e2));
		System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
		System.out.println("The shortest path from v1 to v3 is:");
		for (IntIterator it = ssspRes.getPath(v3).iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			System.out.println(" " + e + "(" + u + ", " + v + ")");
		}
	}

}
