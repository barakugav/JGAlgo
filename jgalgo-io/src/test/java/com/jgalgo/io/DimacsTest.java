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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import it.unimi.dsi.fastutil.booleans.BooleanList;

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

		String checkData2 = "";
		checkData2 += "c DIMACS written graph by JGAlgo\n";
		checkData2 += "p edge 5 5\n";
		checkData2 += "e 1 2\n";
		checkData2 += "e 2 3\n";
		checkData2 += "e 2 4\n";
		checkData2 += "e 3 4\n";
		checkData2 += "e 4 5\n";
		checkData2 = checkData2.replace("\n", System.lineSeparator());
		String checkData3 = "";
		checkData3 += "c DIMACS written graph by JGAlgo\n";
		checkData3 += "p edge 5 5\n";
		checkData3 += "e 1 2\n";
		checkData3 += "e 2 4\n";
		checkData3 += "e 2 3\n";
		checkData3 += "e 3 4\n";
		checkData3 += "e 4 5\n";
		checkData3 = checkData3.replace("\n", System.lineSeparator());

		if (data2.trim().equals(checkData2.trim())) {
			assertEquals(data2.trim(), checkData2.trim());
		} else if (data2.trim().equals(checkData3.trim())) {
			assertEquals(data2.trim(), checkData3.trim());
		} else {
			assertEquals(data2.trim(), checkData2.trim());
		}
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
		DimacsGraphWriter graphWriter = new DimacsGraphWriter();
		graphWriter.setEdgeWeights("weight");
		graphWriter.writeGraph(g, writer);
		String data2 = writer.toString();

		String checkData2 = "";
		checkData2 += "c DIMACS written graph by JGAlgo\n";
		checkData2 += "p sp 5 5\n";
		checkData2 += "e 1 2 3\n";
		checkData2 += "e 2 3 9\n";
		checkData2 += "e 2 4 15\n";
		checkData2 += "e 3 4 2\n";
		checkData2 += "e 4 5 7\n";
		checkData2 = checkData2.replace("\n", System.lineSeparator());
		String checkData3 = "";
		checkData3 += "c DIMACS written graph by JGAlgo\n";
		checkData3 += "p sp 5 5\n";
		checkData3 += "e 1 2 3\n";
		checkData3 += "e 2 4 15\n";
		checkData3 += "e 2 3 9\n";
		checkData3 += "e 3 4 2\n";
		checkData3 += "e 4 5 7\n";
		checkData3 = checkData3.replace("\n", System.lineSeparator());

		if (data2.trim().equals(checkData2.trim())) {
			assertEquals(data2.trim(), checkData2.trim());
		} else if (data2.trim().equals(checkData3.trim())) {
			assertEquals(data2.trim(), checkData3.trim());
		} else {
			assertEquals(data2.trim(), checkData2.trim());
		}
	}

	@Test
	public void writeAndReadRandomGraphs() {
		final long seed = 0x428fcf43adbd26e6L;
		Random rand = new Random(seed);
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (int repeat = 0; repeat < 32; repeat++) {
				final int n = 10 + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				GraphFactory<Integer, Integer> factory =
						intGraph ? IntGraphFactory.newUndirected() : GraphFactory.newUndirected();
				Graph<Integer, Integer> g = factory.allowSelfEdges().newGraph();

				/* DIMACS format support vertices with labels 1..n only */
				for (int v = 1; v <= n; v++)
					g.addVertex(Integer.valueOf(v));

				while (g.edges().size() < m) {
					Integer source = Graphs.randVertex(g, rand);
					Integer target = Graphs.randVertex(g, rand);
					/* DIMACS format support edges with labels 1..m only */
					Integer e = Integer.valueOf(g.edges().size() + 1);
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
			DimacsGraphWriter graphWriter = new DimacsGraphWriter();
			graphWriter.setEdgeWeights("weightsEdges");
			graphWriter.writeGraph(g, writer);
			String data = writer.toString();

			DimacsGraphReader reader = new DimacsGraphReader();
			reader.setEdgeWeightsKey("weightsEdges");
			IntGraphBuilder gb = reader.readIntoBuilder(new StringReader(data));
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

	@Test
	public void readIgnoreBlankLines() {
		String data = "";
		data += "p edge 3 3\n";
		data += "\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());

		IntGraph g = new DimacsGraphReader().readGraph(new StringReader(data));

		IntGraph g1 = IntGraph.newUndirected();
		g1.addVertex(1);
		g1.addVertex(2);
		g1.addVertex(3);
		g1.addEdge(1, 2, 1);
		g1.addEdge(2, 3, 2);
		g1.addEdge(3, 1, 3);

		assertEquals(g, g1);
	}

	@Test
	public void readInvalidCommentLine() {
		String data = "";
		data += "cc not a valid comment\n";
		data += "p edge 3 3\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data1)));
	}

	@Test
	public void readInvalidProblemLine() {
		String data = "";
		data += "pp edge 3 3\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data1)));

		data = "";
		data += "p nonexistingformat 3 3\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data2 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data2)));

		data = "";
		data += "p 3 3\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data3 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data3)));

		data = "";
		data += "p edge -1 3\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data4 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data4)));

		data = "";
		data += "p edge 3 -1\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data5 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data5)));

		data = "";
		data += "p edge notanumber 3\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data6 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data6)));

		data = "";
		data += "p edge 3 notanumber\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data7 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data7)));

		data = "";
		data += "p edge 3 3\n";
		data += "p edge 3 3\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data8 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data8)));

		data = "";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data9 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data9)));
	}

	@Test
	public void readInvalidEdgeLine() {
		String data = "";
		data += "p edge 3 3\n";
		data += "e 1\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data1)));

		data = "";
		data += "p sp 3 3\n";
		data += "e 1 2\n";
		data += "e 2 3 1\n";
		data += "e 3 1 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data2 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data2)));

		data = "";
		data += "p 3 3\n";
		data += "e notanumber 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data3 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data3)));

		data = "";
		data += "p edge 3 3\n";
		data += "e 1 notanumber\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data = data.replace("\n", System.lineSeparator());
		String data4 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data4)));

		data = "";
		data += "p sp 3 3\n";
		data += "e 1 2 5\n";
		data += "e 2 3 notanumber\n";
		data += "e 3 1 6\n";
		data = data.replace("\n", System.lineSeparator());
		String data5 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data5)));
	}

	@Test
	public void readUnknownLine() {
		String data = "";
		data += "p edge 3 3\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 3 1\n";
		data += "u unknown line\n";
		data = data.replace("\n", System.lineSeparator());
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphReader().readGraph(new StringReader(data1)));
	}

	@Test
	public void writeInvalidVertices() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		/* missing vertex 3 */
		g.addVertex(4);
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphWriter().writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeInvalidEdges() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(1, 2, 1);
		g.addEdge(2, 3, 2);
		/* missing edge 4 */
		g.addEdge(3, 1, 4);
		assertThrows(IllegalArgumentException.class, () -> new DimacsGraphWriter().writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeNonIntegerWeights() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(1, 2, 1);
		g.addEdge(2, 3, 2);
		g.addEdge(3, 1, 3);
		IWeightsDouble w = g.addEdgesWeights("weights", double.class);
		w.set(1, 1.0);
		w.set(2, -2.5);
		w.set(3, 8.4);

		DimacsGraphWriter writer = new DimacsGraphWriter();
		writer.setEdgeWeights("weights");
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
		writer.setEdgeWeights("non-existing-weights");
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

}
