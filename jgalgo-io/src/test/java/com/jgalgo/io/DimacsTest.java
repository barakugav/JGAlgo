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

package com.jgalgo.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;

public class DimacsTest {

	@Test
	public void parseDimacsEdgeGraph() {
		String data = "";
		data += "c this is the graph with vertices {1,2,3,4,5} and edges {(1,2),(2,3),(2,4),(3,4),(4,5)}\n";
		data += "p edge 5 5\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 2 4\n";
		data += "e 3 4\n";
		data += "e 4 5\n";
		final IntGraph g = new DimacsGraphReader().readGraph(new StringReader(data));

		final StringWriter writer = new StringWriter();
		new DimacsGraphWriter().writeGraph(g, writer);
		String data2 = writer.toString();

		String check_data2 = "";
		check_data2 += "c DIMACS written graph by JGAlgo\n";
		check_data2 += "p edge 5 5\n";
		check_data2 += "e 1 2\n";
		check_data2 += "e 2 3\n";
		check_data2 += "e 2 4\n";
		check_data2 += "e 3 4\n";
		check_data2 += "e 4 5\n";
		check_data2 = check_data2.replace("\n", System.lineSeparator());
		String check_data3 = "";
		check_data3 += "c DIMACS written graph by JGAlgo\n";
		check_data3 += "p edge 5 5\n";
		check_data3 += "e 1 2\n";
		check_data3 += "e 2 4\n";
		check_data3 += "e 2 3\n";
		check_data3 += "e 3 4\n";
		check_data3 += "e 4 5\n";
		check_data3 = check_data3.replace("\n", System.lineSeparator());

		if (data2.trim().equals(check_data2.trim()))
			assertEquals(data2.trim(), check_data2.trim());
		else if (data2.trim().equals(check_data3.trim()))
			assertEquals(data2.trim(), check_data3.trim());
		else
			assertEquals(data2.trim(), check_data2.trim());
	}

	@Test
	public void parseDimacsSpGraph() {
		String data = "";
		data += "c this is the graph\n";
		data += "p sp 5 5\n";
		data += "e 1 2 3\n";
		data += "e 2 3 9\n";
		data += "e 2 4 15\n";
		data += "e 3 4 2\n";
		data += "e 4 5 7\n";
		final IntGraph g = new DimacsGraphReader().readGraph(new StringReader(data));

		final StringWriter writer = new StringWriter();
		new DimacsGraphWriter().writeGraph(g, writer);
		String data2 = writer.toString();

		String check_data2 = "";
		check_data2 += "c DIMACS written graph by JGAlgo\n";
		check_data2 += "p sp 5 5\n";
		check_data2 += "e 1 2 3\n";
		check_data2 += "e 2 3 9\n";
		check_data2 += "e 2 4 15\n";
		check_data2 += "e 3 4 2\n";
		check_data2 += "e 4 5 7\n";
		check_data2 = check_data2.replace("\n", System.lineSeparator());
		String check_data3 = "";
		check_data3 += "c DIMACS written graph by JGAlgo\n";
		check_data3 += "p sp 5 5\n";
		check_data3 += "e 1 2 3\n";
		check_data3 += "e 2 4 15\n";
		check_data3 += "e 2 3 9\n";
		check_data3 += "e 3 4 2\n";
		check_data3 += "e 4 5 7\n";
		check_data3 = check_data3.replace("\n", System.lineSeparator());

		if (data2.trim().equals(check_data2.trim()))
			assertEquals(data2.trim(), check_data2.trim());
		else if (data2.trim().equals(check_data3.trim()))
			assertEquals(data2.trim(), check_data3.trim());
		else
			assertEquals(data2.trim(), check_data2.trim());
	}

	@Test
	public void writeAndReadRandomGraphs() {
		final long seed = 0x428fcf43adbd26e6L;
		Random rand = new Random(seed);
		for (int repeat = 0; repeat < 32; repeat++) {
			final int n = 10 + rand.nextInt(20);
			final int m = 15 + rand.nextInt(30);
			IntGraph g = IntGraphFactory.newUndirected().allowSelfEdges().newGraph();

			/* DIMACS format support vertices with labels 1..n only */
			for (int v = 1; v <= n; v++)
				g.addVertex(v);

			while (g.edges().size() < m) {
				int source = Graphs.randVertex(g, rand);
				int target = Graphs.randVertex(g, rand);
				/* DIMACS format support edges with labels 1..m only */
				int e = g.edges().size() + 1;
				g.addEdge(source, target, e);
			}

			StringWriter writer = new StringWriter();
			new DimacsGraphWriter().writeGraph(g, writer);
			String data = writer.toString();

			IntGraphBuilder gb = new DimacsGraphReader().readIntoBuilder(new StringReader(data));
			IntGraph gImmutable = gb.build();
			IntGraph gMutable = gb.buildMutable();
			assertEquals(g, gImmutable);
			assertEquals(g, gMutable);
		}
	}

	@Test
	public void writeAndReadRandomGraphsWithWeights() {
		final long seed = 0x203078ae64766b7cL;
		Random rand = new Random(seed);
		for (int repeat = 0; repeat < 32; repeat++) {
			final int n = 10 + rand.nextInt(20);
			final int m = 15 + rand.nextInt(30);
			IntGraph g = IntGraphFactory.newUndirected().allowSelfEdges().newGraph();

			/* DIMACS format support vertices with labels 1..n only */
			for (int v = 1; v <= n; v++)
				g.addVertex(v);

			while (g.edges().size() < m) {
				int source = Graphs.randVertex(g, rand);
				int target = Graphs.randVertex(g, rand);
				/* DIMACS format support edges with labels 1..m only */
				int e = g.edges().size() + 1;
				g.addEdge(source, target, e);
			}

			IWeightsInt we1 = g.addEdgesWeights("weightsEdges", int.class);
			for (int e : g.edges())
				we1.set(e, n + rand.nextInt(m * 3));

			StringWriter writer = new StringWriter();
			new DimacsGraphWriter().writeGraph(g, writer);
			String data = writer.toString();

			IntGraphBuilder gb = new DimacsGraphReader().readIntoBuilder(new StringReader(data));
			IntGraph gImmutable = gb.build();
			IntGraph gMutable = gb.buildMutable();
			assertEquals(g, gImmutable);
			assertEquals(g, gMutable);
		}
	}

	@Test
	public void writeDirectedGraphUnsupported() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(1);
		g.addVertex(6);
		g.addVertex(78);
		g.addEdge(78, 1);
		g.addEdge(1, 6);
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphWriter().writeGraph(g, new StringWriter()));
	}

}
