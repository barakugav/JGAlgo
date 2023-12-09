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
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IWeightsByte;
import com.jgalgo.graph.IWeightsChar;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsFloat;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IWeightsObj;
import com.jgalgo.graph.IWeightsShort;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import it.unimi.dsi.fastutil.booleans.BooleanList;

public class LedaTest {

	@Test
	public void ReadWriteLEDAWithWeights() {
		TextBuilder text = new TextBuilder();
		text.addLine("#header section");
		text.addLine("LEDA.GRAPH");
		text.addLine("string");
		text.addLine("int");
		text.addLine("-1");
		text.addLine("#nodes section");
		text.addLine("5");
		text.addLine("|{v1}|");
		text.addLine("|{v2}|");
		text.addLine("|{v3}|");
		text.addLine("|{v4}|");
		text.addLine("|{v5}|");
		text.addLine("");
		text.addLine("#edges section");
		text.addLine("7");
		text.addLine("1 2 0 |{4}|");
		text.addLine("1 3 0 |{3}|");
		text.addLine("2 3 0 |{2}|");
		text.addLine("3 4 0 |{3}|");
		text.addLine("3 5 0 |{7}|");
		text.addLine("4 5 0 |{6}|");
		text.addLine("5 1 0 |{1}|");
		text.addLine("");

		LedaGraphReader graphReader = new LedaGraphReader();
		StringReader sr = new StringReader(text.getAndClear());
		IntGraphBuilder gb = graphReader.readIntoBuilder(sr);
		IntGraph graph1 = gb.build();

		LedaGraphWriter graphWriter = new LedaGraphWriter();
		StringWriter sw = new StringWriter();
		graphWriter.writeGraph(graph1, sw);
	}

	@Test
	public void ReadWriteLEDAWithoutWeights() {
		TextBuilder text = new TextBuilder();
		text.addLine("#header section");
		text.addLine("LEDA.GRAPH");
		text.addLine("string");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("#nodes section");
		text.addLine("5");
		text.addLine("|{vX1}|");
		text.addLine("|{vX2}|");
		text.addLine("|{vX3}|");
		text.addLine("|{vX4}|");
		text.addLine("|{vX5}|");
		text.addLine("");
		text.addLine("#edges section");
		text.addLine("7");
		text.addLine("1 2 0 |{}|");
		text.addLine("1 3 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 4 0 |{}|");
		text.addLine("3 5 0 |{}|");
		text.addLine("4 5 0 |{}|");
		text.addLine("5 1 0 |{}|");
		text.addLine("");

		LedaGraphReader graphReader = new LedaGraphReader();
		StringReader sr = new StringReader(text.getAndClear());
		IntGraphBuilder gb = graphReader.readIntoBuilder(sr);
		IntGraph graph1 = gb.build();

		LedaGraphWriter graphWriter = new LedaGraphWriter();
		StringWriter sw = new StringWriter();
		graphWriter.writeGraph(graph1, sw);
	}

	@Test
	public void writeAndReadRandomGraphs() {
		final long seed = 0x71a78c3b16b1e662L;
		Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (int repeat = 0; repeat < 32; repeat++) {
				final int n = 10 + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				IntGraph g = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();

				/* LEDA format support vertices with labels 1..n only */
				for (int v = 1; v <= n; v++)
					g.addVertex(v);

				while (g.edges().size() < m) {
					int source = Graphs.randVertex(g, rand);
					int target = Graphs.randVertex(g, rand);
					/* LEDA format support edges with labels 1..m only */
					int e = g.edges().size() + 1;
					g.addEdge(source, target, e);
				}

				StringWriter writer = new StringWriter();
				new LedaGraphWriter().writeGraph(g, writer);
				String data = writer.toString();

				IntGraphBuilder gb = new LedaGraphReader().readIntoBuilder(new StringReader(data));
				IntGraph gImmutable = gb.build();
				IntGraph gMutable = gb.buildMutable();
				assertEquals(g, gImmutable);
				assertEquals(g, gMutable);
			}
		}
	}

	@Test
	public void writeAndReadRandomGraphsWithWeights() {
		final long seed = 0x4fc76d07796e9c4cL;
		Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (int repeat = 0; repeat < 32; repeat++) {
				final int n = 10 + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				IntGraph g = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();

				/* LEDA format support vertices with labels 1..n only */
				for (int v = 1; v <= n; v++)
					g.addVertex(v);

				while (g.edges().size() < m) {
					int source = Graphs.randVertex(g, rand);
					int target = Graphs.randVertex(g, rand);
					/* LEDA format support edges with labels 1..m only */
					int e = g.edges().size() + 1;
					g.addEdge(source, target, e);
				}

				IWeightsInt we1 = g.addEdgesWeights("weightsKey", int.class);
				for (int e : g.edges())
					we1.set(e, n + rand.nextInt(m * 3));

				StringWriter writer = new StringWriter();
				LedaGraphWriter graphWriter = new LedaGraphWriter();
				graphWriter.setEdgesWeightsKey("weightsKey");
				graphWriter.writeGraph(g, writer);
				String data = writer.toString();

				LedaGraphReader graphReader = new LedaGraphReader();
				graphReader.setEdgesWeightsKey("weightsKey");
				IntGraphBuilder gb = graphReader.readIntoBuilder(new StringReader(data));
				IntGraph gImmutable = gb.build();
				IntGraph gMutable = gb.buildMutable();
				assertEquals(g, gImmutable);
				assertEquals(g, gMutable);
			}
		}
	}

	@Test
	public void writeInvalidVertices() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		/* missing vertex 3 */
		g.addVertex(4);
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphWriter().writeGraph(g, new StringWriter()));
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
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphWriter().writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeNonExistingWeights() {
		IntGraph g = createGraph();
		LedaGraphWriter graphWriter = new LedaGraphWriter();

		graphWriter.setVerticesWeightsKey("weights-that-do-not-exist");
		assertThrows(IllegalArgumentException.class, () -> graphWriter.writeGraph(g, new StringWriter()));
		graphWriter.setVerticesWeightsKey(null);

		graphWriter.setEdgesWeightsKey("weights-that-do-not-exist");
		assertThrows(IllegalArgumentException.class, () -> graphWriter.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeReadWeightsByte() {
		IntGraph g = createGraph();
		IWeightsByte vw = g.addVerticesWeights("v-weights", byte.class);
		IWeightsByte ew = g.addEdgesWeights("e-weights", byte.class);
		for (int v : g.vertices())
			vw.set(v, (byte) (46 - v));
		for (int e : g.edges())
			ew.set(e, (byte) (55 + e));
		writeReadWeights(g);
	}

	@Test
	public void writeReadWeightsShort() {
		IntGraph g = createGraph();
		IWeightsShort vw = g.addVerticesWeights("v-weights", short.class);
		IWeightsShort ew = g.addEdgesWeights("e-weights", short.class);
		for (int v : g.vertices())
			vw.set(v, (short) (46 - v));
		for (int e : g.edges())
			ew.set(e, (short) (55 + e));
		writeReadWeights(g);
	}

	@Test
	public void writeReadWeightsInt() {
		IntGraph g = createGraph();
		IWeightsInt vw = g.addVerticesWeights("v-weights", int.class);
		IWeightsInt ew = g.addEdgesWeights("e-weights", int.class);
		for (int v : g.vertices())
			vw.set(v, 46 - v);
		for (int e : g.edges())
			ew.set(e, 55 + e);
		writeReadWeights(g);
	}

	@Test
	public void writeReadWeightsLong() {
		IntGraph g = createGraph();
		IWeightsLong vw = g.addVerticesWeights("v-weights", long.class);
		IWeightsLong ew = g.addEdgesWeights("e-weights", long.class);
		for (int v : g.vertices())
			vw.set(v, 46 - v);
		for (int e : g.edges())
			ew.set(e, 55 + e);
		writeReadWeights(g);
	}

	@Test
	public void writeReadWeightsFloat() {
		IntGraph g = createGraph();
		IWeightsFloat vw = g.addVerticesWeights("v-weights", float.class);
		IWeightsFloat ew = g.addEdgesWeights("e-weights", float.class);
		for (int v : g.vertices())
			vw.set(v, 46 - v);
		for (int e : g.edges())
			ew.set(e, 55 + e);
		writeReadWeights(g);
	}

	@Test
	public void writeReadWeightsDouble() {
		IntGraph g = createGraph();
		IWeightsDouble vw = g.addVerticesWeights("v-weights", double.class);
		IWeightsDouble ew = g.addEdgesWeights("e-weights", double.class);
		for (int v : g.vertices())
			vw.set(v, 46 - v);
		for (int e : g.edges())
			ew.set(e, 55 + e);
		writeReadWeights(g);
	}

	@Test
	public void writeReadWeightsBool() {
		IntGraph g = createGraph();
		IWeightsBool vw = g.addVerticesWeights("v-weights", boolean.class);
		IWeightsBool ew = g.addEdgesWeights("e-weights", boolean.class);
		for (int v : g.vertices())
			vw.set(v, Double.hashCode(46 - v) % 2 == 0);
		for (int e : g.edges())
			ew.set(e, Double.hashCode(55 + e) % 2 == 0);
		writeReadWeights(g);
	}

	@Test
	public void writeReadWeightsChar() {
		IntGraph g = createGraph();
		IWeightsChar vw = g.addVerticesWeights("v-weights", char.class);
		IWeightsChar ew = g.addEdgesWeights("e-weights", char.class);
		for (int v : g.vertices())
			vw.set(v, (char) (46 - v));
		for (int e : g.edges())
			ew.set(e, (char) (55 + e));
		writeReadWeights(g);

		TextBuilder text = new TextBuilder();
		text.addLine("LEDA.GRAPH");
		text.addLine("char");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{alongword}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void writeReadWeightsString() {
		IntGraph g = createGraph();
		IWeightsObj<String> vw = g.addVerticesWeights("v-weights", String.class);
		IWeightsObj<String> ew = g.addEdgesWeights("e-weights", String.class);
		for (int v : g.vertices())
			vw.set(v, "s" + (46 - v));
		for (int e : g.edges())
			ew.set(e, "s" + (55 + e));
		writeReadWeights(g);
	}

	@Test
	public void readWeightsVoid() {
		TextBuilder text = new TextBuilder();
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{notempty}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));
	}

	private static void writeReadWeights(IntGraph g) {
		LedaGraphWriter graphWriter = new LedaGraphWriter();
		graphWriter.setVerticesWeightsKey("v-weights");
		graphWriter.setEdgesWeightsKey("e-weights");

		StringWriter sw = new StringWriter();
		graphWriter.writeGraph(g, sw);
		String data = sw.toString();

		LedaGraphReader graphReader = new LedaGraphReader();
		graphReader.setVerticesWeightsKey("v-weights");
		graphReader.setEdgesWeightsKey("e-weights");
		IntGraph gb = graphReader.readGraph(new StringReader(data));

		assertEquals(g, gb);
	}

	private static IntGraph createGraph() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(1, 2, 1);
		g.addEdge(2, 3, 2);
		g.addEdge(3, 1, 3);
		return g;
	}

	@Test
	public void readInvalidHeader() {
		TextBuilder text = new TextBuilder();
		/* empty file */
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid 'LEDA.GRAPH' header */
		text.addLine("LEDA.GRAPH.invalid-first-line");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* missing vertices weights type */
		text.addLine("LEDA.GRAPH");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* missing edges weights type */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid vertices weights type */
		text.addLine("LEDA.GRAPH");
		text.addLine("not-a-weights-type");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid 'LEDA.GRAPH' header */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("not-a-weights-type");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* missing directed/undirected */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid directed/undirected */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("0");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid directed/undirected */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-3");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readInvalidNodeSection() {
		TextBuilder text = new TextBuilder();

		/* missing number of vertices */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid number of vertices */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("not-a-number");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* negative number of vertices */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("-9");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* missing vertices */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("5");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* no vertex weight prefix */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("no-prefix}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* no vertex weight suffix */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{no-suffix");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void invalidEdgeSection() {
		TextBuilder text = new TextBuilder();

		/* missing number of edges */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid number of edges */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("not-a-number");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* negative number of edges */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("-6");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* missing edges */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("4");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid edge */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("230|{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid edge */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 30|{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid edge */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0|{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid edge source */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("not-a-number 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid edge target */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 not-a-number 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* invalid edge reverse-twin */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 not-a-number |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* edge reverse-twin */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 1 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* no edge weight prefix */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 no-prefix}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

		/* no edge weight suffix */
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{no-suffix");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readTrailingLines() {
		TextBuilder text = new TextBuilder();
		text.addLine("LEDA.GRAPH");
		text.addLine("void");
		text.addLine("void");
		text.addLine("-1");
		text.addLine("3");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("|{}|");
		text.addLine("3");
		text.addLine("1 2 0 |{}|");
		text.addLine("2 3 0 |{}|");
		text.addLine("3 1 0 |{}|");
		text.addLine("3 1 0 |{}|");
		assertThrows(IllegalArgumentException.class,
				() -> new LedaGraphReader().readGraph(new StringReader(text.getAndClear())));

	}

}
