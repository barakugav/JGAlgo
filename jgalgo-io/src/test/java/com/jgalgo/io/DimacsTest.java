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

import static com.jgalgo.internal.util.Range.range;
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
		TextBuilder text = new TextBuilder();
		text.addLine("c this is the graph with vertices {1,2,3,4,5} and edges {(1,2),(2,3),(2,4),(3,4),(4,5)}");
		text.addLine("p edge 5 5");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 2 4");
		text.addLine("e 3 4");
		text.addLine("e 4 5");
		final IntGraph g = new DimacsGraphReader().readGraph(new StringReader(text.getAndClear()));

		final StringWriter writer = new StringWriter();
		new DimacsGraphWriter().writeGraph(g, writer);
		String data2 = writer.toString();

		text.addLine("c DIMACS written graph by JGAlgo");
		text.addLine("p edge 5 5");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 2 4");
		text.addLine("e 3 4");
		text.addLine("e 4 5");
		String checkData2 = text.getAndClear();

		text.addLine("c DIMACS written graph by JGAlgo");
		text.addLine("p edge 5 5");
		text.addLine("e 1 2");
		text.addLine("e 2 4");
		text.addLine("e 2 3");
		text.addLine("e 3 4");
		text.addLine("e 4 5");
		String checkData3 = text.getAndClear();

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
		TextBuilder text = new TextBuilder();
		text.addLine("c this is the graph");
		text.addLine("p sp 5 5");
		text.addLine("e 1 2 3");
		text.addLine("e 2 3 9");
		text.addLine("e 2 4 15");
		text.addLine("e 3 4 2");
		text.addLine("e 4 5 7");
		final IntGraph g = new DimacsGraphReader().readGraph(new StringReader(text.getAndClear()));

		final StringWriter writer = new StringWriter();
		DimacsGraphWriter graphWriter = new DimacsGraphWriter();
		graphWriter.setEdgeWeights("weight");
		graphWriter.writeGraph(g, writer);
		String data2 = writer.toString();

		text.addLine("c DIMACS written graph by JGAlgo");
		text.addLine("p sp 5 5");
		text.addLine("e 1 2 3");
		text.addLine("e 2 3 9");
		text.addLine("e 2 4 15");
		text.addLine("e 3 4 2");
		text.addLine("e 4 5 7");
		String checkData2 = text.getAndClear();

		text.addLine("c DIMACS written graph by JGAlgo");
		text.addLine("p sp 5 5");
		text.addLine("e 1 2 3");
		text.addLine("e 2 4 15");
		text.addLine("e 2 3 9");
		text.addLine("e 3 4 2");
		text.addLine("e 4 5 7");
		String checkData3 = text.getAndClear();

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
						intGraph ? IntGraphFactory.undirected() : GraphFactory.undirected();
				Graph<Integer, Integer> g = factory.allowSelfEdges().newGraph();

				/* DIMACS format support vertices with labels 1..n only */
				g.addVertices(range(1, n + 1));

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
			IntGraph g = IntGraphFactory.undirected().allowSelfEdges().newGraph();

			/* DIMACS format support vertices with labels 1..n only */
			g.addVertices(range(1, n + 1));

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
		TextBuilder text = new TextBuilder();
		text.addLine("p edge 3 3");
		text.addLine("");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");

		IntGraph g = new DimacsGraphReader().readGraph(new StringReader(text.getAndClear()));

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
		TextBuilder text = new TextBuilder();
		text.addLine("cc not a valid comment");
		text.addLine("p edge 3 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readInvalidProblemLine() {
		TextBuilder text = new TextBuilder();
		text.addLine("pp edge 3 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p nonexistingformat 3 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p 3 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p edge -1 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p edge 3 -1");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p edge notanumber 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p edge 3 notanumber");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p edge 3 3");
		text.addLine("p edge 3 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readInvalidEdgeLine() {
		TextBuilder text = new TextBuilder();
		text.addLine("p edge 3 3");
		text.addLine("e 1");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p sp 3 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3 1");
		text.addLine("e 3 1 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p 3 3");
		text.addLine("e notanumber 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p edge 3 3");
		text.addLine("e 1 notanumber");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));

		text.addLine("p sp 3 3");
		text.addLine("e 1 2 5");
		text.addLine("e 2 3 notanumber");
		text.addLine("e 3 1 6");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readUnknownLine() {
		TextBuilder text = new TextBuilder();
		text.addLine("p edge 3 3");
		text.addLine("e 1 2");
		text.addLine("e 2 3");
		text.addLine("e 3 1");
		text.addLine("u unknown line");
		assertThrows(IllegalArgumentException.class,
				() -> new DimacsGraphReader().readGraph(new StringReader(text.getAndClear())));
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
