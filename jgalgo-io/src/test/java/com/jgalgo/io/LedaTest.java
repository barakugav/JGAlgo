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
		String ledaDefinition = "";
		ledaDefinition += "#header section\n";
		ledaDefinition += "LEDA.GRAPH\n";
		ledaDefinition += "string\n";
		ledaDefinition += "int\n";
		ledaDefinition += "-1\n";
		ledaDefinition += "#nodes section\n";
		ledaDefinition += "5\n";
		ledaDefinition += "|{v1}|\n";
		ledaDefinition += "|{v2}|\n";
		ledaDefinition += "|{v3}|\n";
		ledaDefinition += "|{v4}|\n";
		ledaDefinition += "|{v5}|\n";
		ledaDefinition += "\n";
		ledaDefinition += "#edges section\n";
		ledaDefinition += "7\n";
		ledaDefinition += "1 2 0 |{4}|\n";
		ledaDefinition += "1 3 0 |{3}|\n";
		ledaDefinition += "2 3 0 |{2}|\n";
		ledaDefinition += "3 4 0 |{3}|\n";
		ledaDefinition += "3 5 0 |{7}|\n";
		ledaDefinition += "4 5 0 |{6}|\n";
		ledaDefinition += "5 1 0 |{1}|\n";
		ledaDefinition += "\n";

		LedaGraphReader graphReader = new LedaGraphReader();
		StringReader sr = new StringReader(ledaDefinition);
		IntGraphBuilder gb = graphReader.readIntoBuilder(sr);
		IntGraph graph1 = gb.build();

		LedaGraphWriter graphWriter = new LedaGraphWriter();
		StringWriter sw = new StringWriter();
		graphWriter.writeGraph(graph1, sw);
	}

	@Test
	public void ReadWriteLEDAWithoutWeights() {
		String ledaDefinition = "";
		ledaDefinition += "#header section\n";
		ledaDefinition += "LEDA.GRAPH\n";
		ledaDefinition += "string\n";
		ledaDefinition += "void\n";
		ledaDefinition += "-1\n";
		ledaDefinition += "#nodes section\n";
		ledaDefinition += "5\n";
		ledaDefinition += "|{vX1}|\n";
		ledaDefinition += "|{vX2}|\n";
		ledaDefinition += "|{vX3}|\n";
		ledaDefinition += "|{vX4}|\n";
		ledaDefinition += "|{vX5}|\n";
		ledaDefinition += "\n";
		ledaDefinition += "#edges section\n";
		ledaDefinition += "7\n";
		ledaDefinition += "1 2 0 |{}|\n";
		ledaDefinition += "1 3 0 |{}|\n";
		ledaDefinition += "2 3 0 |{}|\n";
		ledaDefinition += "3 4 0 |{}|\n";
		ledaDefinition += "3 5 0 |{}|\n";
		ledaDefinition += "4 5 0 |{}|\n";
		ledaDefinition += "5 1 0 |{}|\n";
		ledaDefinition += "\n";

		LedaGraphReader graphReader = new LedaGraphReader();
		StringReader sr = new StringReader(ledaDefinition);
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
				IntGraphFactory factory = directed ? IntGraphFactory.newDirected() : IntGraphFactory.newUndirected();
				IntGraph g = factory.allowSelfEdges().newGraph();

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
				IntGraphFactory factory = directed ? IntGraphFactory.newDirected() : IntGraphFactory.newUndirected();
				IntGraph g = factory.allowSelfEdges().newGraph();

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

		String data = "";
		data += "LEDA.GRAPH\n";
		data += "char\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{alongword}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data1)));
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
		String data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{notempty}|\n";
		data += "3 1 0 |{}|\n";
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data1)));
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
		/* empty file */
		String data = "";
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data1)));

		/* invalid 'LEDA.GRAPH' header */
		data = "";
		data += "LEDA.GRAPH.invalid-first-line\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data2 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data2)));

		/* missing vertices weights type */
		data = "";
		data += "LEDA.GRAPH\n";
		String data3 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data3)));

		/* missing edges weights type */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		String data4 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data4)));

		/* invalid vertices weights type */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "not-a-weights-type\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data5 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data5)));

		/* invalid 'LEDA.GRAPH' header */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "not-a-weights-type\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data6 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data6)));

		/* missing directed/undirected */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		String data7 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data7)));

		/* invalid directed/undirected */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "0\n";
		String data8 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data8)));

		/* invalid directed/undirected */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-3\n";
		String data9 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data9)));
	}

	@Test
	public void readInvalidNodeSection() {
		/* missing number of vertices */
		String data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data1)));

		/* invalid number of vertices */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "not-a-number\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data2 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data2)));

		/* negative number of vertices */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "-9\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data3 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data3)));

		/* missing vertices */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "5\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		String data4 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data4)));

		/* no vertex weight prefix */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "no-prefix}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data5 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data5)));

		/* no vertex weight suffix */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{no-suffix\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data6 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data6)));
	}

	@Test
	public void invalidEdgeSection() {
		/* missing number of edges */
		String data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data1)));

		/* invalid number of edges */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "not-a-number\n";
		String data2 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data2)));

		/* negative number of edges */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "-6\n";
		String data3 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data3)));

		/* missing edges */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "4\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data4 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data4)));

		/* invalid edge */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "230|{}|\n";
		data += "3 1 0 |{}|\n";
		String data5 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data5)));

		/* invalid edge */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 30|{}|\n";
		data += "3 1 0 |{}|\n";
		String data6 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data6)));

		/* invalid edge */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0|{}|\n";
		data += "3 1 0 |{}|\n";
		String data7 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data7)));

		/* invalid edge source */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "not-a-number 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data8 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data8)));

		/* invalid edge target */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 not-a-number 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data9 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data9)));

		/* invalid edge reverse-twin */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 not-a-number |{}|\n";
		data += "3 1 0 |{}|\n";
		String data10 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data10)));

		/* edge reverse-twin */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 1 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data11 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data11)));

		/* no edge weight prefix */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 no-prefix}|\n";
		data += "3 1 0 |{}|\n";
		String data12 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data12)));

		/* no edge weight suffix */
		data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{no-suffix\n";
		data += "3 1 0 |{}|\n";
		String data13 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data13)));
	}

	@Test
	public void readTrailingLines() {
		String data = "";
		data += "LEDA.GRAPH\n";
		data += "void\n";
		data += "void\n";
		data += "-1\n";
		data += "3\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "|{}|\n";
		data += "3\n";
		data += "1 2 0 |{}|\n";
		data += "2 3 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		data += "3 1 0 |{}|\n";
		String data1 = data;
		assertThrows(IllegalArgumentException.class, () -> new LedaGraphReader().readGraph(new StringReader(data1)));

	}

}
