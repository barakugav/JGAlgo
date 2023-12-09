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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
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
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class GexfTest {

	@Test
	public void readSimpleGraph1() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("0");
		g.addVertex("1");
		g.addEdge("0", "1", "8");
		WeightsObj<String, String> labels = g.addVerticesWeights("label", String.class);
		labels.set("0", "Hello");
		labels.set("1", "Word");

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF−8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\"");
		text.addLine("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema−instance\"");
		text.addLine("    xsi:schemaLocation=\"http://gexf.net/1.3");
		text.addLine("    http://gexf.net/1.3/gexf.xsd\"");
		text.addLine("    version=\"1.3\">");
		text.addLine("  <meta lastmodifieddate=\"2009−03−20\">");
		text.addLine("    <creator>Gephi.org</creator>");
		text.addLine("    <description>A hello world! file</description>");
		text.addLine("  </meta>");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" label=\"Hello\"/>");
		text.addLine("      <node id=\"1\" label=\"Word\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"8\" source=\"0\" target=\"1\"/>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");

		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readSimpleGraph2() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("0");
		g.addVertex("1");
		g.addVertex("2");
		g.addVertex("3");
		g.addEdge("0", "1", "e0");
		g.addEdge("0", "2", "e1");
		g.addEdge("1", "0", "e2");
		g.addEdge("2", "1", "e3");
		g.addEdge("0", "3", "e4");

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF−8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\"");
		text.addLine("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema−instance\"");
		text.addLine("    xsi:schemaLocation=\"http://gexf.net/1.3");
		text.addLine("    http://gexf.net/1.3/gexf.xsd\"");
		text.addLine("    version=\"1.3\">");
		text.addLine("  <meta lastmodifieddate=\"2009−03−20\">");
		text.addLine("      <creator>Gephi.org</creator>");
		text.addLine("      <description>A Web network</description>");
		text.addLine("  </meta>");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"0\" title=\"url\" type=\"string\"/>");
		text.addLine("      <attribute id=\"1\" title=\"indegree\" type=\"float\"/>");
		text.addLine("      <attribute id=\"2\" title=\"frog\" type=\"boolean\">");
		text.addLine("        <default>true</default>");
		text.addLine("      </attribute>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" label=\"Gephi\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"0\" value=\"http://gephi.org\"/>");
		text.addLine("          <attvalue for=\"1\" value=\"1\"/>");
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("      <node id=\"1\" label=\"Webatlas\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"0\" value=\"http://webatlas.fr\"/>");
		text.addLine("          <attvalue for=\"1\" value=\"2\"/>");
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("      <node id=\"2\" label=\"RTGI\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"0\" value=\"http://rtgi.fr\"/>");
		text.addLine("          <attvalue for=\"1\" value=\"1\"/>");
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("      <node id=\"3\" label=\"BarabasiLab\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"0\" value=\"http://barabasilab.com\"/>");
		text.addLine("          <attvalue for=\"1\" value=\"1\"/>");
		text.addLine("          <attvalue for=\"2\" value=\"false\"/>");
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge source=\"0\" target=\"1\"/>");
		text.addLine("      <edge source=\"0\" target=\"2\"/>");
		text.addLine("      <edge source=\"1\" target=\"0\"/>");
		text.addLine("      <edge source=\"2\" target=\"1\"/>");
		text.addLine("      <edge source=\"0\" target=\"3\"/>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");

		WeightsObj<String, String> labels = g.addVerticesWeights("label", String.class);
		labels.set("0", "Gephi");
		labels.set("1", "Webatlas");
		labels.set("2", "RTGI");
		labels.set("3", "BarabasiLab");
		WeightsObj<String, String> url = g.addVerticesWeights("url", String.class);
		url.set("0", "http://gephi.org");
		url.set("1", "http://webatlas.fr");
		url.set("2", "http://rtgi.fr");
		url.set("3", "http://barabasilab.com");
		WeightsFloat<String> indegree = g.addVerticesWeights("indegree", float.class);
		indegree.set("0", 1f);
		indegree.set("1", 2f);
		indegree.set("2", 1f);
		indegree.set("3", 1f);
		WeightsBool<String> frog = g.addVerticesWeights("frog", boolean.class, Boolean.TRUE);
		frog.set("3", false);

		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void writeReadRandGraphs() {
		final long seed = 0x97448c2a3c4f525L;
		Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (int n : IntList.of(6, 30, 211)) {
				for (int repeat = 0; repeat < 32; repeat++) {
					final int m = n + rand.nextInt(2 * n);
					IntGraph g = IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges().newGraph();
					while (g.vertices().size() < n) {
						int v = rand.nextInt(2 * n);
						if (!g.vertices().contains(v))
							g.addVertex(v);
					}
					while (g.edges().size() < m) {
						int u = Graphs.randVertex(g, rand);
						int v = Graphs.randVertex(g, rand);
						int e = rand.nextInt(2 * m);
						if (!g.edges().contains(e))
							g.addEdge(u, v, e);
					}

					IWeightsInt vWeights = g.addVerticesWeights("v-weights", int.class);
					for (int v : g.vertices())
						vWeights.set(v, rand.nextInt(2 * n));
					IWeightsInt eWeights = g.addEdgesWeights("e-weights", int.class);
					for (int e : g.edges())
						eWeights.set(e, rand.nextInt(2 * m));

					StringWriter writer = new StringWriter();
					GexfGraphWriter<Integer, Integer> graphWriter = new GexfGraphWriter<>();
					graphWriter.writeGraph(g, writer);
					String data = writer.toString();
					GexfGraphReader<Integer, Integer> graphReader = new GexfGraphReader<>(int.class, int.class);
					IntGraph g1 = (IntGraph) graphReader.readGraph(new StringReader(data));

					assertEquals(g, g1);
				}
			}
		}
	}

	@Test
	public void readVertexParser() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"8\" />");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>();
		reader.setVertexParser(s -> s + "a");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("8a");
		assertEquals(g, reader.readGraph(new StringReader(text.get())));

		assertThrows(IllegalStateException.class,
				() -> new GexfGraphReader<>().readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVertexParserDefault() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"8\" />");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");

		{
			Graph<Byte, String> g = Graph.newDirected();
			g.addVertex(Byte.valueOf((byte) 8));
			GexfGraphReader<Byte, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(byte.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Byte.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Short, String> g = Graph.newDirected();
			g.addVertex(Short.valueOf((short) 8));
			GexfGraphReader<Short, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(short.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Short.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Integer, String> g = Graph.newDirected();
			g.addVertex(Integer.valueOf(8));
			GexfGraphReader<Integer, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(int.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Integer.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Long, String> g = Graph.newDirected();
			g.addVertex(Long.valueOf(8));
			GexfGraphReader<Long, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(long.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Long.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Float, String> g = Graph.newDirected();
			g.addVertex(Float.valueOf(8));
			GexfGraphReader<Float, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(float.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Float.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Double, String> g = Graph.newDirected();
			g.addVertex(Double.valueOf(8));
			GexfGraphReader<Double, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(double.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Double.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, String> g = Graph.newDirected();
			g.addVertex("8");
			GexfGraphReader<String, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			GexfGraphReader<IntList, String> reader = new GexfGraphReader<>();
			assertThrows(IllegalArgumentException.class, () -> reader.setVertexParserDefault(IntList.class));
		}
	}

	@Test
	public void readEdgeParser() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" />");
		text.addLine("      <node id=\"1\" />");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"88\" source=\"0\" target=\"1\" />");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>();
		reader.setVertexParserDefault(String.class);
		reader.setEdgeParser(s -> s + "a");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("0");
		g.addVertex("1");
		g.addEdge("0", "1", "88a");
		assertEquals(g, reader.readGraph(new StringReader(text.get())));

		assertThrows(IllegalStateException.class, () -> {
			GexfGraphReader<String, String> reader2 = new GexfGraphReader<>();
			reader2.setVertexParserDefault(String.class);
			reader2.readGraph(new StringReader(text.get()));
		});
	}

	@Test
	public void readEdgeParserDefault() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" />");
		text.addLine("      <node id=\"1\" />");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"88\" source=\"0\" target=\"1\" />");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");

		{
			Graph<String, Byte> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Byte.valueOf((byte) 88));
			GexfGraphReader<String, Byte> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeParserDefault(byte.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeParserDefault(Byte.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Short> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Short.valueOf((short) 88));
			GexfGraphReader<String, Short> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeParserDefault(short.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeParserDefault(Short.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Integer> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Integer.valueOf(88));
			GexfGraphReader<String, Integer> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeParserDefault(int.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeParserDefault(Integer.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Long> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Long.valueOf(88));
			GexfGraphReader<String, Long> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeParserDefault(long.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeParserDefault(Long.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Float> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Float.valueOf(88));
			GexfGraphReader<String, Float> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeParserDefault(float.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeParserDefault(Float.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Double> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Double.valueOf(88));
			GexfGraphReader<String, Double> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeParserDefault(double.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeParserDefault(Double.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, String> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", "88");
			GexfGraphReader<String, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeParserDefault(String.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			GexfGraphReader<String, IntList> reader = new GexfGraphReader<>();
			assertThrows(IllegalArgumentException.class, () -> reader.setEdgeParserDefault(IntList.class));
		}
	}

	@Test
	public void readEdgeSupplier() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" />");
		text.addLine("      <node id=\"1\" />");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge source=\"0\" target=\"1\" />");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, Integer> reader = new GexfGraphReader<>();
		reader.setVertexParserDefault(String.class);
		reader.setEdgeSupplier(Set::size);

		Graph<String, Integer> g = Graph.newDirected();
		g.addVertex("0");
		g.addVertex("1");
		g.addEdge("0", "1", Integer.valueOf(0));
		assertEquals(g, reader.readGraph(new StringReader(text.get())));

		assertThrows(IllegalStateException.class, () -> {
			GexfGraphReader<String, String> reader2 = new GexfGraphReader<>();
			reader2.setVertexParserDefault(String.class);
			reader2.readGraph(new StringReader(text.get()));
		});
	}

	@Test
	public void readEdgeSupplierDefault() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" />");
		text.addLine("      <node id=\"1\" />");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge source=\"0\" target=\"1\" />");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");

		{
			Graph<String, Byte> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Byte.valueOf((byte) 0));
			GexfGraphReader<String, Byte> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeSupplierDefault(byte.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeSupplierDefault(Byte.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Short> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Short.valueOf((short) 0));
			GexfGraphReader<String, Short> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeSupplierDefault(short.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeSupplierDefault(Short.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Integer> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Integer.valueOf(0));
			GexfGraphReader<String, Integer> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeSupplierDefault(int.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeSupplierDefault(Integer.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Long> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Long.valueOf(0));
			GexfGraphReader<String, Long> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeSupplierDefault(long.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeSupplierDefault(Long.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Float> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Float.valueOf(0));
			GexfGraphReader<String, Float> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeSupplierDefault(float.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeSupplierDefault(Float.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, Double> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Double.valueOf(0));
			GexfGraphReader<String, Double> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeSupplierDefault(double.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setEdgeSupplierDefault(Double.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, String> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", "e0");
			GexfGraphReader<String, String> reader = new GexfGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeSupplierDefault(String.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			GexfGraphReader<String, IntList> reader = new GexfGraphReader<>();
			assertThrows(IllegalArgumentException.class, () -> reader.setEdgeSupplierDefault(IntList.class));
		}
	}

	@Test
	public void readEdgeSupplierTooManyEdges() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" />");
		text.addLine("      <node id=\"1\" />");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		for (int i = 0; i < 257; i++)
			text.addLine("      <edge source=\"0\" target=\"1\" />");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");

		GexfGraphReader<String, Byte> reader = new GexfGraphReader<>();
		reader.setVertexParserDefault(String.class);
		reader.setEdgeSupplierDefault(byte.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgeIdsSomeMissingSomeExists() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" />");
		text.addLine("      <node id=\"1\" />");
		text.addLine("      <node id=\"2\" />");
		text.addLine("      <node id=\"3\" />");
		text.addLine("      <node id=\"4\" />");
		text.addLine("      <node id=\"5\" />");
		text.addLine("      <node id=\"6\" />");
		text.addLine("      <node id=\"7\" />");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge source=\"0\" target=\"1\" id=\"0\" />");
		text.addLine("      <edge source=\"0\" target=\"2\" />");
		text.addLine("      <edge source=\"0\" target=\"3\" />");
		text.addLine("      <edge source=\"0\" target=\"4\" id=\"4\" />");
		text.addLine("      <edge source=\"0\" target=\"5\" id=\"6\" />");
		text.addLine("      <edge source=\"0\" target=\"6\" id=\"3\" />");
		text.addLine("      <edge source=\"0\" target=\"7\" />");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, Integer> reader = new GexfGraphReader<>();
		reader.setVertexParserDefault(String.class);
		reader.setEdgeParserDefault(int.class);
		reader.setEdgeSupplierDefault(int.class);

		Graph<String, Integer> g = Graph.newDirected();
		g.addVertex("0");
		g.addVertex("1");
		g.addVertex("2");
		g.addVertex("3");
		g.addVertex("4");
		g.addVertex("5");
		g.addVertex("6");
		g.addVertex("7");
		g.addEdge("0", "1", Integer.valueOf(0));
		g.addEdge("0", "2", Integer.valueOf(1));
		g.addEdge("0", "3", Integer.valueOf(2));
		g.addEdge("0", "4", Integer.valueOf(4));
		g.addEdge("0", "5", Integer.valueOf(6));
		g.addEdge("0", "6", Integer.valueOf(3));
		g.addEdge("0", "7", Integer.valueOf(5));
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readInvalidRoot() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<notgexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"0\" />");
		text.addLine("      <node id=\"1\" />");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge source=\"0\" target=\"1\" id=\"0\" />");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</notgexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVertexIdEmpty() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">\n");
		text.addLine("  <graph id=\"G\" defaultedgetype=\"directed\">\n");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"\"/>\n");
		text.addLine("    </nodes>");
		text.addLine("  </graph>\n");
		text.addLine("</gexf>\n");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("");
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVertexIdMissing() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node />");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgeMissingSource() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph id=\"G\" defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"e0\" target=\"n1\"/>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgeMissingTarget() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph id=\"G\" defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"e0\" source=\"n0\"/>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readDirectedUndirectedMix() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph id=\"G\" defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"e0\" source=\"n0\" target=\"n1\" type=\"undirected\"/>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readNoGraph() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readMultipleGraphs() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"e0\" source=\"n0\" target=\"n1\"/>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("  <graph id=\"G2\" defaultedgetype=\"directed\">");
		text.addLine("    </=nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"e0\" source=\"n0\" target=\"n1\"/>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readInvalidXml() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"e0\" source=\"n0\" target=\"n1\"/>");
		text.addLine("    </edges>");
		text.addLine("  </notgraph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readNoVertices() {
		Graph<String, String> g = Graph.newDirected();

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
		assertEquals(Graph.newDirected(), reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readNoEdges() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readDynamicGraph() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\" mode=\"dynamic\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"e0\" source=\"n0\" target=\"n1\"/>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsUnknownClass() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"unknown-class\">");
		text.addLine("      <attribute id=\"0\" title=\"url\" type=\"string\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVerticesLabels() {
		Graph<String, String> g = Graph.newUndirected();
		g.addVertex("n0");
		g.addVertex("n1");
		WeightsObj<String, String> labels = g.addVerticesWeights("label", String.class);
		labels.set("n0", "123456");
		labels.set("n1", "78");

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"undirected\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\" label=\"123456\"/>");
		text.addLine("      <node id=\"n1\" label=\"78\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgesLabels() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addEdge("n0", "n1", "0");
		g.addEdge("n1", "n0", "1");
		WeightsObj<String, String> labels = g.addEdgesWeights("label", String.class);
		labels.set("0", "123456");
		labels.set("1", "78");

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">\n");
		text.addLine("  <graph defaultedgetype=\"directed\">\n");
		text.addLine("    <nodes>\n");
		text.addLine("      <node id=\"n0\"/>\n");
		text.addLine("      <node id=\"n1\"/>\n");
		text.addLine("    </nodes>\n");
		text.addLine("    <edges>\n");
		text.addLine("      <edge id=\"0\" source=\"n0\" target=\"n1\" label=\"123456\"/>\n");
		text.addLine("      <edge id=\"1\" source=\"n1\" target=\"n0\" label=\"78\"/>\n");
		text.addLine("    </edges>\n");
		text.addLine("  </graph>\n");
		text.addLine("</gexf>\n");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgesWeightsBuiltIn() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addEdge("n0", "n1", "0");
		g.addEdge("n1", "n0", "1");
		WeightsDouble<String> labels = g.addEdgesWeights("weight", double.class);
		labels.set("0", 123456.78);
		labels.set("1", 910.1112);

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">\n");
		text.addLine("  <graph defaultedgetype=\"directed\">\n");
		text.addLine("    <nodes>\n");
		text.addLine("      <node id=\"n0\"/>\n");
		text.addLine("      <node id=\"n1\"/>\n");
		text.addLine("    </nodes>\n");
		text.addLine("    <edges>\n");
		text.addLine("      <edge id=\"0\" source=\"n0\" target=\"n1\" weight=\"123456.78\"/>\n");
		text.addLine("      <edge id=\"1\" source=\"n1\" target=\"n0\" weight=\"910.1112\"/>\n");
		text.addLine("    </edges>\n");
		text.addLine("  </graph>\n");
		text.addLine("</gexf>\n");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeights() throws ParseException, URISyntaxException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Gexf.DateFormat);

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">\n");
		text.addLine("  <graph defaultedgetype=\"directed\">\n");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"0\" title=\"v-byte-weights1\" type=\"byte\"/>");
		text.addLine("      <attribute id=\"1\" title=\"v-byte-weights2\" type=\"byte\">");
		text.addLine("        <default>1</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"2\" title=\"v-short-weights1\" type=\"short\"/>");
		text.addLine("      <attribute id=\"3\" title=\"v-short-weights2\" type=\"short\">");
		text.addLine("        <default>2</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"4\" title=\"v-int-weights1\" type=\"integer\"/>");
		text.addLine("      <attribute id=\"5\" title=\"v-int-weights2\" type=\"integer\">");
		text.addLine("        <default>3</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"6\" title=\"v-long-weights1\" type=\"long\"/>");
		text.addLine("      <attribute id=\"7\" title=\"v-long-weights2\" type=\"long\">");
		text.addLine("        <default>4</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"8\" title=\"v-float-weights1\" type=\"float\"/>");
		text.addLine("      <attribute id=\"9\" title=\"v-float-weights2\" type=\"float\">");
		text.addLine("        <default>5.5</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"10\" title=\"v-double-weights1\" type=\"double\"/>");
		text.addLine("      <attribute id=\"11\" title=\"v-double-weights2\" type=\"double\">");
		text.addLine("        <default>6.6</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"12\" title=\"v-bool-weights1\" type=\"boolean\"/>");
		text.addLine("      <attribute id=\"13\" title=\"v-bool-weights2\" type=\"boolean\">");
		text.addLine("        <default>true</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"14\" title=\"v-char-weights1\" type=\"char\"/>");
		text.addLine("      <attribute id=\"15\" title=\"v-char-weights2\" type=\"char\">");
		text.addLine("        <default>a</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"16\" title=\"v-string-weights1\" type=\"string\"/>");
		text.addLine("      <attribute id=\"17\" title=\"v-string-weights2\" type=\"string\">");
		text.addLine("        <default>abc</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"18\" title=\"v-date-weights1\" type=\"date\"/>");
		text.addLine("      <attribute id=\"19\" title=\"v-date-weights2\" type=\"date\">");
		text.addLine("        <default>2002-10-10T00:00:00+13</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"20\" title=\"v-uri-weights1\" type=\"anyURI\"/>");
		text.addLine("      <attribute id=\"21\" title=\"v-uri-weights2\" type=\"anyURI\">");
		text.addLine("        <default>http://example.com</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"22\" title=\"v-bigdecimal-weights1\" type=\"bigdecimal\"/>");
		text.addLine("      <attribute id=\"23\" title=\"v-bigdecimal-weights2\" type=\"bigdecimal\">");
		text.addLine("        <default>12345678901234567890123456789012345678.90</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"24\" title=\"v-biginteger-weights1\" type=\"biginteger\"/>");
		text.addLine("      <attribute id=\"25\" title=\"v-biginteger-weights2\" type=\"biginteger\">");
		text.addLine("        <default>1234567890123456745678901234567890</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"26\" title=\"v-listbyte-weights1\" type=\"listbyte\"/>");
		text.addLine("      <attribute id=\"27\" title=\"v-listbyte-weights2\" type=\"listbyte\">");
		text.addLine("        <default>[0, 1, 2]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"28\" title=\"v-listshort-weights1\" type=\"listshort\"/>");
		text.addLine("      <attribute id=\"29\" title=\"v-listshort-weights2\" type=\"listshort\">");
		text.addLine("        <default>[3, 4, 5]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"30\" title=\"v-listinteger-weights1\" type=\"listinteger\"/>");
		text.addLine("      <attribute id=\"31\" title=\"v-listinteger-weights2\" type=\"listinteger\">");
		text.addLine("        <default>[6, 7, 8]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"32\" title=\"v-listlong-weights1\" type=\"listlong\"/>");
		text.addLine("      <attribute id=\"33\" title=\"v-listlong-weights2\" type=\"listlong\">");
		text.addLine("        <default>[9, 10, 11]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"34\" title=\"v-listfloat-weights1\" type=\"listfloat\"/>");
		text.addLine("      <attribute id=\"35\" title=\"v-listfloat-weights2\" type=\"listfloat\">");
		text.addLine("        <default>[12.12, 13.13, 14.14]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"36\" title=\"v-listdouble-weights1\" type=\"listdouble\"/>");
		text.addLine("      <attribute id=\"37\" title=\"v-listdouble-weights2\" type=\"listdouble\">");
		text.addLine("        <default>[15.15, 16.16, 17.17]</default>");
		text.addLine("      </attribute>");
		text.addLine("    </attributes>");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"38\" title=\"v-listboolean-weights1\" type=\"listboolean\"/>");
		text.addLine("      <attribute id=\"39\" title=\"v-listboolean-weights2\" type=\"listboolean\">");
		text.addLine("        <default>[true, false, true]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"40\" title=\"v-listchar-weights1\" type=\"listchar\"/>");
		text.addLine("      <attribute id=\"41\" title=\"v-listchar-weights2\" type=\"listchar\">");
		text.addLine("        <default>[a, b, c]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"42\" title=\"v-liststring-weights1\" type=\"liststring\"/>");
		text.addLine("      <attribute id=\"43\" title=\"v-liststring-weights2\" type=\"liststring\">");
		text.addLine("        <default>[abc, def, ghi]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"44\" title=\"v-listbigdecimal-weights1\" type=\"listbigdecimal\"/>");
		text.addLine("      <attribute id=\"45\" title=\"v-listbigdecimal-weights2\" type=\"listbigdecimal\">");
		text.addLine("        <default>[5497489789789.86, 54874465465498.98, 68728936740730000000010000.01]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"46\" title=\"v-listbiginteger-weights1\" type=\"listbiginteger\"/>");
		text.addLine("      <attribute id=\"47\" title=\"v-listbiginteger-weights2\" type=\"listbiginteger\">");
		text.addLine("        <default>[1234567867894230706490, 123528974567890, 123495941321324586567890]</default>");
		text.addLine("      </attribute>");
		text.addLine("    </attributes>");
		text.addLine("    <attributes class=\"edge\">");
		text.addLine("      <attribute id=\"55\" title=\"e-byte-weights1\" type=\"byte\"/>");
		text.addLine("      <attribute id=\"47\" title=\"e-byte-weights2\" type=\"byte\">");
		text.addLine("        <default>101</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"46\" title=\"e-short-weights1\" type=\"short\"/>");
		text.addLine("      <attribute id=\"45\" title=\"e-short-weights2\" type=\"short\">");
		text.addLine("        <default>102</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"44\" title=\"e-int-weights1\" type=\"integer\"/>");
		text.addLine("      <attribute id=\"43\" title=\"e-int-weights2\" type=\"integer\">");
		text.addLine("        <default>103</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"42\" title=\"e-long-weights1\" type=\"long\"/>");
		text.addLine("      <attribute id=\"41\" title=\"e-long-weights2\" type=\"long\">");
		text.addLine("        <default>104</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"40\" title=\"e-float-weights1\" type=\"float\"/>");
		text.addLine("      <attribute id=\"39\" title=\"e-float-weights2\" type=\"float\">");
		text.addLine("        <default>105.5</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"38\" title=\"e-double-weights1\" type=\"double\"/>");
		text.addLine("      <attribute id=\"37\" title=\"e-double-weights2\" type=\"double\">");
		text.addLine("        <default>106.6</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"36\" title=\"e-bool-weights1\" type=\"boolean\"/>");
		text.addLine("      <attribute id=\"35\" title=\"e-bool-weights2\" type=\"boolean\">");
		text.addLine("        <default>true</default>");
		text.addLine("      </attribute>");
		text.addLine("    </attributes>");
		text.addLine("    <attributes class=\"edge\">");
		text.addLine("      <attribute id=\"34\" title=\"e-char-weights1\" type=\"char\"/>");
		text.addLine("      <attribute id=\"33\" title=\"e-char-weights2\" type=\"char\">");
		text.addLine("        <default>k</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"32\" title=\"e-string-weights1\" type=\"string\"/>");
		text.addLine("      <attribute id=\"31\" title=\"e-string-weights2\" type=\"string\">");
		text.addLine("        <default>lmn</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"30\" title=\"e-date-weights1\" type=\"date\"/>");
		text.addLine("      <attribute id=\"29\" title=\"e-date-weights2\" type=\"date\">");
		text.addLine("        <default>2033-11-10T00:00:00+09</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"28\" title=\"e-uri-weights1\" type=\"anyURI\"/>");
		text.addLine("      <attribute id=\"27\" title=\"e-uri-weights2\" type=\"anyURI\">");
		text.addLine("        <default>http://example2.com</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"26\" title=\"e-bigdecimal-weights1\" type=\"bigdecimal\"/>");
		text.addLine("      <attribute id=\"25\" title=\"e-bigdecimal-weights2\" type=\"bigdecimal\">");
		text.addLine("        <default>274602789672307293670.39</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"24\" title=\"e-biginteger-weights1\" type=\"biginteger\"/>");
		text.addLine("      <attribute id=\"23\" title=\"e-biginteger-weights2\" type=\"biginteger\">");
		text.addLine("        <default>780768763587634583456</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"22\" title=\"e-listbyte-weights1\" type=\"listbyte\"/>");
		text.addLine("      <attribute id=\"21\" title=\"e-listbyte-weights2\" type=\"listbyte\">");
		text.addLine("        <default>[18, 19, 20]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"20\" title=\"e-listshort-weights1\" type=\"listshort\"/>");
		text.addLine("      <attribute id=\"19\" title=\"e-listshort-weights2\" type=\"listshort\">");
		text.addLine("        <default>[21, 22, 23]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"18\" title=\"e-listinteger-weights1\" type=\"listinteger\"/>");
		text.addLine("      <attribute id=\"17\" title=\"e-listinteger-weights2\" type=\"listinteger\">");
		text.addLine("        <default>[24, 25, 26]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"16\" title=\"e-listlong-weights1\" type=\"listlong\"/>");
		text.addLine("      <attribute id=\"15\" title=\"e-listlong-weights2\" type=\"listlong\">");
		text.addLine("        <default>[27, 28, 29]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"14\" title=\"e-listfloat-weights1\" type=\"listfloat\"/>");
		text.addLine("      <attribute id=\"13\" title=\"e-listfloat-weights2\" type=\"listfloat\">");
		text.addLine("        <default>[30.30, 31.31, 32.32]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"12\" title=\"e-listdouble-weights1\" type=\"listdouble\"/>");
		text.addLine("      <attribute id=\"11\" title=\"e-listdouble-weights2\" type=\"listdouble\">");
		text.addLine("        <default>[33.33, 34.34, 35.35]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"10\" title=\"e-listboolean-weights1\" type=\"listboolean\"/>");
		text.addLine("      <attribute id=\"9\" title=\"e-listboolean-weights2\" type=\"listboolean\">");
		text.addLine("        <default>[true, false, true]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"8\" title=\"e-listchar-weights1\" type=\"listchar\"/>");
		text.addLine("      <attribute id=\"7\" title=\"e-listchar-weights2\" type=\"listchar\">");
		text.addLine("        <default>[d, e, f]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"6\" title=\"e-liststring-weights1\" type=\"liststring\"/>");
		text.addLine("      <attribute id=\"5\" title=\"e-liststring-weights2\" type=\"liststring\">");
		text.addLine("        <default>[ghi, jkl, mno]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"4\" title=\"e-listbigdecimal-weights1\" type=\"listbigdecimal\"/>");
		text.addLine("      <attribute id=\"3\" title=\"e-listbigdecimal-weights2\" type=\"listbigdecimal\">");
		text.addLine("        <default>[7015938769324.67, 8909000045689674.02, 1111111111235.46]</default>");
		text.addLine("      </attribute>");
		text.addLine("      <attribute id=\"2\" title=\"e-listbiginteger-weights1\" type=\"listbiginteger\"/>");
		text.addLine("      <attribute id=\"1\" title=\"e-listbiginteger-weights2\" type=\"listbiginteger\">");
		text.addLine("        <default>[582790273698, 16549699999, 88888436549874]</default>");
		text.addLine("      </attribute>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>\n");
		text.addLine("      <node id=\"n0\"/>\n");
		text.addLine("      <node id=\"n1\">\n");
		text.addLine("        <attvalues>\n");
		text.addLine("          <attvalue for=\"0\" value=\"60\"/>\n");
		text.addLine("          <attvalue for=\"1\" value=\"61\"/>\n");
		text.addLine("          <attvalue for=\"2\" value=\"62\"/>\n");
		text.addLine("          <attvalue for=\"3\" value=\"63\"/>\n");
		text.addLine("          <attvalue for=\"4\" value=\"64\"/>\n");
		text.addLine("          <attvalue for=\"5\" value=\"65\"/>\n");
		text.addLine("          <attvalue for=\"6\" value=\"66\"/>\n");
		text.addLine("          <attvalue for=\"7\" value=\"67\"/>\n");
		text.addLine("          <attvalue for=\"8\" value=\"68.68\"/>\n");
		text.addLine("          <attvalue for=\"9\" value=\"69.69\"/>\n");
		text.addLine("          <attvalue for=\"10\" value=\"70.70\"/>\n");
		text.addLine("          <attvalue for=\"11\" value=\"71.71\"/>\n");
		text.addLine("          <attvalue for=\"12\" value=\"true\"/>\n");
		text.addLine("          <attvalue for=\"13\" value=\"false\"/>\n");
		text.addLine("          <attvalue for=\"14\" value=\"d\"/>\n");
		text.addLine("          <attvalue for=\"15\" value=\"e\"/>\n");
		text.addLine("          <attvalue for=\"16\" value=\"zxw\"/>\n");
		text.addLine("          <attvalue for=\"17\" value=\"yvu\"/>\n");
		text.addLine("          <attvalue for=\"18\" value=\"2011-11-11T00:00:00+11\"/>\n");
		text.addLine("          <attvalue for=\"19\" value=\"2012-12-12T00:00:00+12\"/>\n");
		text.addLine("          <attvalue for=\"20\" value=\"http://example3.com\"/>\n");
		text.addLine("          <attvalue for=\"21\" value=\"http://example4.com\"/>\n");
		text.addLine("          <attvalue for=\"22\" value=\"59715917907239064783.3564968432\"/>\n");
		text.addLine("          <attvalue for=\"23\" value=\"2736902746897236940.56456156\"/>\n");
		text.addLine("          <attvalue for=\"24\" value=\"5467854616418964889257409867\"/>\n");
		text.addLine("          <attvalue for=\"25\" value=\"28936702376897367489346345\"/>\n");
		text.addLine("          <attvalue for=\"26\" value=\"[40, 41, 42, 43, 44]\"/>\n");
		text.addLine("          <attvalue for=\"27\" value=\"[45, 46, 47, 48, 49]\"/>\n");
		text.addLine("          <attvalue for=\"28\" value=\"[50, 51, 52, 53, 54]\"/>\n");
		text.addLine("          <attvalue for=\"29\" value=\"[55, 56, 57, 58, 59]\"/>\n");
		text.addLine("          <attvalue for=\"30\" value=\"[60, 61, 62, 63, 64]\"/>\n");
		text.addLine("          <attvalue for=\"31\" value=\"[65, 66, 67, 68, 69]\"/>\n");
		text.addLine("          <attvalue for=\"32\" value=\"[70, 71, 72, 73, 74]\"/>\n");
		text.addLine("          <attvalue for=\"33\" value=\"[75, 76, 77, 78, 79]\"/>\n");
		text.addLine("          <attvalue for=\"34\" value=\"[80.80, 81.81, 82.82, 83.83, 84.84]\"/>\n");
		text.addLine("          <attvalue for=\"35\" value=\"[85.85, 86.86, 87.87, 88.88, 89.89]\"/>\n");
		text.addLine("          <attvalue for=\"36\" value=\"[90.90, 91.91, 92.92, 93.93, 94.94]\"/>\n");
		text.addLine("          <attvalue for=\"37\" value=\"[95.95, 96.96, 97.97, 98.98, 99.99]\"/>\n");
		text.addLine("          <attvalue for=\"38\" value=\"[true, false, true, false, true]\"/>\n");
		text.addLine("          <attvalue for=\"39\" value=\"[false, true, false, true, false]\"/>\n");
		text.addLine("          <attvalue for=\"40\" value=\"[h, i, j, k, l]\"/>\n");
		text.addLine("          <attvalue for=\"41\" value=\"[m, n, o, p, q]\"/>\n");
		text.addLine("          <attvalue for=\"42\" value=\"[rst, uvw, xyz, abc, def]\"/>\n");
		text.addLine("          <attvalue for=\"43\" value=\"[ghi, jkl, mno, pqr, stu]\"/>\n");
		text.addLine(
				"          <attvalue for=\"44\" value=\"[1326452364.67, 456546547.02, 8888886783568.46, 2345456747.39, 4573457]\"/>\n");
		text.addLine(
				"          <attvalue for=\"45\" value=\"[3333, 2345254, 23642547547.2455874457457, 236346262734575367.351564568547, 245785678735746586]\"/>\n");
		text.addLine(
				"          <attvalue for=\"46\" value=\"[541564646546, 6542317544532678547854354674574, 25632896357845,444444444444]\"/>\n");
		text.addLine("          <attvalue for=\"47\" value=\"[45674586342]\"/>\n");
		text.addLine("        </attvalues>\n");
		text.addLine("      </node>\n");
		text.addLine("    </nodes>\n");
		text.addLine("    <edges>\n");
		text.addLine("      <edge id=\"e0\" source=\"n0\" target=\"n1\"/>\n");
		text.addLine("      <edge id=\"e1\" source=\"n1\" target=\"n0\">\n");
		text.addLine("        <attvalues>\n");
		text.addLine("          <attvalue for=\"55\" value=\"111\"/>\n");
		text.addLine("          <attvalue for=\"47\" value=\"112\"/>\n");
		text.addLine("          <attvalue for=\"46\" value=\"113\"/>\n");
		text.addLine("          <attvalue for=\"45\" value=\"114\"/>\n");
		text.addLine("          <attvalue for=\"44\" value=\"115\"/>\n");
		text.addLine("          <attvalue for=\"43\" value=\"116\"/>\n");
		text.addLine("          <attvalue for=\"42\" value=\"117\"/>\n");
		text.addLine("          <attvalue for=\"41\" value=\"118\"/>\n");
		text.addLine("          <attvalue for=\"40\" value=\"119.119\"/>\n");
		text.addLine("          <attvalue for=\"39\" value=\"120.120\"/>\n");
		text.addLine("          <attvalue for=\"38\" value=\"121.121\"/>\n");
		text.addLine("          <attvalue for=\"37\" value=\"122.122\"/>\n");
		text.addLine("          <attvalue for=\"36\" value=\"true\"/>\n");
		text.addLine("          <attvalue for=\"35\" value=\"false\"/>\n");
		text.addLine("          <attvalue for=\"34\" value=\"r\"/>\n");
		text.addLine("          <attvalue for=\"33\" value=\"s\"/>\n");
		text.addLine("          <attvalue for=\"32\" value=\"tuv\"/>\n");
		text.addLine("          <attvalue for=\"31\" value=\"wxy\"/>\n");
		text.addLine("          <attvalue for=\"30\" value=\"2013-11-11T00:00:00+11\"/>\n");
		text.addLine("          <attvalue for=\"29\" value=\"2014-12-12T00:00:00+12\"/>\n");
		text.addLine("          <attvalue for=\"28\" value=\"http://example5.com\"/>\n");
		text.addLine("          <attvalue for=\"27\" value=\"http://example6.com\"/>\n");
		text.addLine("          <attvalue for=\"26\" value=\"12958713097845609318467.3408578346\"/>\n");
		text.addLine("          <attvalue for=\"25\" value=\"109283598367913.3464573457\"/>\n");
		text.addLine("          <attvalue for=\"24\" value=\"674134579835363462475637246275567\"/>\n");
		text.addLine("          <attvalue for=\"23\" value=\"93720967520867289467289357345345\"/>\n");
		text.addLine("          <attvalue for=\"22\" value=\"[46, 47, 48, 49, 50]\"/>\n");
		text.addLine("          <attvalue for=\"21\" value=\"[51, 52, 53, 54, 55]\"/>\n");
		text.addLine("          <attvalue for=\"20\" value=\"[56, 57, 58, 59, 60]\"/>\n");
		text.addLine("          <attvalue for=\"19\" value=\"[61, 62, 63, 64, 65]\"/>\n");
		text.addLine("          <attvalue for=\"18\" value=\"[66, 67, 68, 69, 70]\"/>\n");
		text.addLine("          <attvalue for=\"17\" value=\"[71, 72, 73, 74, 75]\"/>\n");
		text.addLine("          <attvalue for=\"16\" value=\"[76, 77, 78, 79, 80]\"/>\n");
		text.addLine("          <attvalue for=\"15\" value=\"[81, 82, 83, 84, 85]\"/>\n");
		text.addLine("          <attvalue for=\"14\" value=\"[86.86, 87.87, 88.88, 89.89, 90.90]\"/>\n");
		text.addLine("          <attvalue for=\"13\" value=\"[91.91, 92.92, 93.93, 94.94, 95.95]\"/>\n");
		text.addLine("          <attvalue for=\"12\" value=\"[96.96, 97.97, 98.98, 99.99, 100.100]\"/>\n");
		text.addLine("          <attvalue for=\"11\" value=\"[101.101, 102.102, 103.103, 104.104, 105.105]\"/>\n");
		text.addLine("          <attvalue for=\"10\" value=\"[true, false, true, false, false]\"/>\n");
		text.addLine("          <attvalue for=\"9\" value=\"[false, true, false, true, true]\"/>\n");
		text.addLine("          <attvalue for=\"8\" value=\"[u, v, w, x, y]\"/>\n");
		text.addLine("          <attvalue for=\"7\" value=\"[z, a, b, c, d]\"/>\n");
		text.addLine("          <attvalue for=\"6\" value=\"[efg, hij, klm, nop, qrs]\"/>\n");
		text.addLine("          <attvalue for=\"5\" value=\"[tuv, wxy, zab, cde, fgh]\"/>\n");
		text.addLine(
				"		  <attvalue for=\"4\" value=\"[8967236905782930568290678.54654968465, 910263509570831456,13462462456, 56823541352456, 234987234.1]\"/>\n");
		text.addLine(
				"		  <attvalue for=\"3\" value=\"[8888888889182391754123.54, 1.2589739468723094898647, 238423433333333]\"/>\n");
		text.addLine(
				"		  <attvalue for=\"2\" value=\"[67093857982768926749031850345, 5719356758398457938457345, 45354345346245672392211110001]\"/>\n");
		text.addLine(
				"		  <attvalue for=\"1\" value=\"[13467218190275345, 34895672458967134098234, 45545454545454545454454545]\"/>\n");
		text.addLine("        </attvalues>\n");
		text.addLine("      </edge>\n");
		text.addLine("    </edges>\n");
		text.addLine("  </graph>\n");
		text.addLine("</gexf>\n");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addEdge("n0", "n1", "e0");
		g.addEdge("n1", "n0", "e1");

		WeightsByte<String> vByteWeights1 = g.addVerticesWeights("v-byte-weights1", byte.class);
		WeightsByte<String> vByteWeights2 = g.addVerticesWeights("v-byte-weights2", byte.class, Byte.valueOf((byte) 1));
		WeightsShort<String> vShortWeights1 = g.addVerticesWeights("v-short-weights1", short.class);
		WeightsShort<String> vShortWeights2 =
				g.addVerticesWeights("v-short-weights2", short.class, Short.valueOf((short) 2));
		WeightsInt<String> vIntWeights1 = g.addVerticesWeights("v-int-weights1", int.class);
		WeightsInt<String> vIntWeights2 = g.addVerticesWeights("v-int-weights2", int.class, Integer.valueOf(3));
		WeightsLong<String> vLongWeights1 = g.addVerticesWeights("v-long-weights1", long.class);
		WeightsLong<String> vLongWeights2 = g.addVerticesWeights("v-long-weights2", long.class, Long.valueOf(4));
		WeightsFloat<String> vFloatWeights1 = g.addVerticesWeights("v-float-weights1", float.class);
		WeightsFloat<String> vFloatWeights2 =
				g.addVerticesWeights("v-float-weights2", float.class, Float.valueOf(5.5f));
		WeightsDouble<String> vDoubleWeights1 = g.addVerticesWeights("v-double-weights1", double.class);
		WeightsDouble<String> vDoubleWeights2 =
				g.addVerticesWeights("v-double-weights2", double.class, Double.valueOf(6.6));
		WeightsBool<String> vBooleanWeights1 = g.addVerticesWeights("v-bool-weights1", boolean.class);
		WeightsBool<String> vBooleanWeights2 = g.addVerticesWeights("v-bool-weights2", boolean.class, Boolean.TRUE);
		WeightsChar<String> vCharWeights1 = g.addVerticesWeights("v-char-weights1", char.class);
		WeightsChar<String> vCharWeights2 = g.addVerticesWeights("v-char-weights2", char.class, Character.valueOf('a'));
		WeightsObj<String, String> vStringWeights1 = g.addVerticesWeights("v-string-weights1", String.class);
		WeightsObj<String, String> vStringWeights2 = g.addVerticesWeights("v-string-weights2", String.class, "abc");
		WeightsObj<String, Date> vDateWeights1 = g.addVerticesWeights("v-date-weights1", Date.class);
		WeightsObj<String, Date> vDateWeights2 =
				g.addVerticesWeights("v-date-weights2", Date.class, dateFormat.parse("2002-10-10T00:00:00+13"));
		WeightsObj<String, URI> vUriWeights1 = g.addVerticesWeights("v-uri-weights1", URI.class);
		WeightsObj<String, URI> vUriWeights2 =
				g.addVerticesWeights("v-uri-weights2", URI.class, new URI("http://example.com"));
		WeightsObj<String, BigDecimal> vBigDecimalWeights1 =
				g.addVerticesWeights("v-bigdecimal-weights1", BigDecimal.class);
		WeightsObj<String, BigDecimal> vBigDecimalWeights2 = g.addVerticesWeights("v-bigdecimal-weights2",
				BigDecimal.class, new BigDecimal("12345678901234567890123456789012345678.90"));
		WeightsObj<String, BigInteger> vBigIntegerWeights1 =
				g.addVerticesWeights("v-biginteger-weights1", BigInteger.class);
		WeightsObj<String, BigInteger> vBigIntegerWeights2 = g.addVerticesWeights("v-biginteger-weights2",
				BigInteger.class, new BigInteger("1234567890123456745678901234567890"));
		WeightsObj<String, byte[]> vListByteWeights1 = g.addVerticesWeights("v-listbyte-weights1", byte[].class);
		WeightsObj<String, byte[]> vListByteWeights2 =
				g.addVerticesWeights("v-listbyte-weights2", byte[].class, new byte[] { 0, 1, 2 });
		WeightsObj<String, short[]> vListShortWeights1 = g.addVerticesWeights("v-listshort-weights1", short[].class);
		WeightsObj<String, short[]> vListShortWeights2 =
				g.addVerticesWeights("v-listshort-weights2", short[].class, new short[] { 3, 4, 5 });
		WeightsObj<String, int[]> vListIntegerWeights1 = g.addVerticesWeights("v-listinteger-weights1", int[].class);
		WeightsObj<String, int[]> vListIntegerWeights2 =
				g.addVerticesWeights("v-listinteger-weights2", int[].class, new int[] { 6, 7, 8 });
		WeightsObj<String, long[]> vListLongWeights1 = g.addVerticesWeights("v-listlong-weights1", long[].class);
		WeightsObj<String, long[]> vListLongWeights2 =
				g.addVerticesWeights("v-listlong-weights2", long[].class, new long[] { 9, 10, 11 });
		WeightsObj<String, float[]> vListFloatWeights1 = g.addVerticesWeights("v-listfloat-weights1", float[].class);
		WeightsObj<String, float[]> vListFloatWeights2 =
				g.addVerticesWeights("v-listfloat-weights2", float[].class, new float[] { 12.12f, 13.13f, 14.14f });
		WeightsObj<String, double[]> vListDoubleWeights1 =
				g.addVerticesWeights("v-listdouble-weights1", double[].class);
		WeightsObj<String, double[]> vListDoubleWeights2 =
				g.addVerticesWeights("v-listdouble-weights2", double[].class, new double[] { 15.15, 16.16, 17.17 });
		WeightsObj<String, boolean[]> vListBooleanWeights1 =
				g.addVerticesWeights("v-listboolean-weights1", boolean[].class);
		WeightsObj<String, boolean[]> vListBooleanWeights2 =
				g.addVerticesWeights("v-listboolean-weights2", boolean[].class, new boolean[] { true, false, true });
		WeightsObj<String, char[]> vListCharWeights1 = g.addVerticesWeights("v-listchar-weights1", char[].class);
		WeightsObj<String, char[]> vListCharWeights2 =
				g.addVerticesWeights("v-listchar-weights2", char[].class, new char[] { 'a', 'b', 'c' });
		WeightsObj<String, String[]> vListStringWeights1 =
				g.addVerticesWeights("v-liststring-weights1", String[].class);
		WeightsObj<String, String[]> vListStringWeights2 =
				g.addVerticesWeights("v-liststring-weights2", String[].class, new String[] { "abc", "def", "ghi" });
		WeightsObj<String, BigDecimal[]> vListBigDecimalWeights1 =
				g.addVerticesWeights("v-listbigdecimal-weights1", BigDecimal[].class);
		WeightsObj<String, BigDecimal[]> vListBigDecimalWeights2 = g.addVerticesWeights("v-listbigdecimal-weights2",
				BigDecimal[].class, new BigDecimal[] { new BigDecimal("5497489789789.86"),
						new BigDecimal("54874465465498.98"), new BigDecimal("68728936740730000000010000.01") });
		WeightsObj<String, BigInteger[]> vListBigIntegerWeights1 =
				g.addVerticesWeights("v-listbiginteger-weights1", BigInteger[].class);
		WeightsObj<String, BigInteger[]> vListBigIntegerWeights2 = g.addVerticesWeights("v-listbiginteger-weights2",
				BigInteger[].class, new BigInteger[] { new BigInteger("1234567867894230706490"),
						new BigInteger("123528974567890"), new BigInteger("123495941321324586567890") });

		WeightsByte<String> eByteWeights1 = g.addEdgesWeights("e-byte-weights1", byte.class);
		WeightsByte<String> eByteWeights2 = g.addEdgesWeights("e-byte-weights2", byte.class, Byte.valueOf((byte) 101));
		WeightsShort<String> eShortWeights1 = g.addEdgesWeights("e-short-weights1", short.class);
		WeightsShort<String> eShortWeights2 =
				g.addEdgesWeights("e-short-weights2", short.class, Short.valueOf((short) 102));
		WeightsInt<String> eIntWeights1 = g.addEdgesWeights("e-int-weights1", int.class);
		WeightsInt<String> eIntWeights2 = g.addEdgesWeights("e-int-weights2", int.class, Integer.valueOf(103));
		WeightsLong<String> eLongWeights1 = g.addEdgesWeights("e-long-weights1", long.class);
		WeightsLong<String> eLongWeights2 = g.addEdgesWeights("e-long-weights2", long.class, Long.valueOf(104));
		WeightsFloat<String> eFloatWeights1 = g.addEdgesWeights("e-float-weights1", float.class);
		WeightsFloat<String> eFloatWeights2 = g.addEdgesWeights("e-float-weights2", float.class, Float.valueOf(105.5f));
		WeightsDouble<String> eDoubleWeights1 = g.addEdgesWeights("e-double-weights1", double.class);
		WeightsDouble<String> eDoubleWeights2 =
				g.addEdgesWeights("e-double-weights2", double.class, Double.valueOf(106.6));
		WeightsBool<String> eBooleanWeights1 = g.addEdgesWeights("e-bool-weights1", boolean.class);
		WeightsBool<String> eBooleanWeights2 = g.addEdgesWeights("e-bool-weights2", boolean.class, Boolean.TRUE);
		WeightsChar<String> eCharWeights1 = g.addEdgesWeights("e-char-weights1", char.class);
		WeightsChar<String> eCharWeights2 = g.addEdgesWeights("e-char-weights2", char.class, Character.valueOf('k'));
		WeightsObj<String, String> eStringWeights1 = g.addEdgesWeights("e-string-weights1", String.class);
		WeightsObj<String, String> eStringWeights2 = g.addEdgesWeights("e-string-weights2", String.class, "lmn");
		WeightsObj<String, Date> eDateWeights1 = g.addEdgesWeights("e-date-weights1", Date.class);
		WeightsObj<String, Date> eDateWeights2 =
				g.addEdgesWeights("e-date-weights2", Date.class, dateFormat.parse("2033-11-10T00:00:00+09"));
		WeightsObj<String, URI> eUriWeights1 = g.addEdgesWeights("e-uri-weights1", URI.class);
		WeightsObj<String, URI> eUriWeights2 =
				g.addEdgesWeights("e-uri-weights2", URI.class, new URI("http://example2.com"));
		WeightsObj<String, BigDecimal> eBigDecimalWeights1 =
				g.addEdgesWeights("e-bigdecimal-weights1", BigDecimal.class);
		WeightsObj<String, BigDecimal> eBigDecimalWeights2 = g.addEdgesWeights("e-bigdecimal-weights2",
				BigDecimal.class, new BigDecimal("274602789672307293670.39"));
		WeightsObj<String, BigInteger> eBigIntegerWeights1 =
				g.addEdgesWeights("e-biginteger-weights1", BigInteger.class);
		WeightsObj<String, BigInteger> eBigIntegerWeights2 =
				g.addEdgesWeights("e-biginteger-weights2", BigInteger.class, new BigInteger("780768763587634583456"));
		WeightsObj<String, byte[]> eListByteWeights1 = g.addEdgesWeights("e-listbyte-weights1", byte[].class);
		WeightsObj<String, byte[]> eListByteWeights2 =
				g.addEdgesWeights("e-listbyte-weights2", byte[].class, new byte[] { 18, 19, 20 });
		WeightsObj<String, short[]> eListShortWeights1 = g.addEdgesWeights("e-listshort-weights1", short[].class);
		WeightsObj<String, short[]> eListShortWeights2 =
				g.addEdgesWeights("e-listshort-weights2", short[].class, new short[] { 21, 22, 23 });
		WeightsObj<String, int[]> eListIntegerWeights1 = g.addEdgesWeights("e-listinteger-weights1", int[].class);
		WeightsObj<String, int[]> eListIntegerWeights2 =
				g.addEdgesWeights("e-listinteger-weights2", int[].class, new int[] { 24, 25, 26 });
		WeightsObj<String, long[]> eListLongWeights1 = g.addEdgesWeights("e-listlong-weights1", long[].class);
		WeightsObj<String, long[]> eListLongWeights2 =
				g.addEdgesWeights("e-listlong-weights2", long[].class, new long[] { 27, 28, 29 });
		WeightsObj<String, float[]> eListFloatWeights1 = g.addEdgesWeights("e-listfloat-weights1", float[].class);
		WeightsObj<String, float[]> eListFloatWeights2 =
				g.addEdgesWeights("e-listfloat-weights2", float[].class, new float[] { 30.30f, 31.31f, 32.32f });
		WeightsObj<String, double[]> eListDoubleWeights1 = g.addEdgesWeights("e-listdouble-weights1", double[].class);
		WeightsObj<String, double[]> eListDoubleWeights2 =
				g.addEdgesWeights("e-listdouble-weights2", double[].class, new double[] { 33.33, 34.34, 35.35 });
		WeightsObj<String, boolean[]> eListBooleanWeights1 =
				g.addEdgesWeights("e-listboolean-weights1", boolean[].class);
		WeightsObj<String, boolean[]> eListBooleanWeights2 =
				g.addEdgesWeights("e-listboolean-weights2", boolean[].class, new boolean[] { true, false, true });
		WeightsObj<String, char[]> eListCharWeights1 = g.addEdgesWeights("e-listchar-weights1", char[].class);
		WeightsObj<String, char[]> eListCharWeights2 =
				g.addEdgesWeights("e-listchar-weights2", char[].class, new char[] { 'd', 'e', 'f' });
		WeightsObj<String, String[]> eListStringWeights1 = g.addEdgesWeights("e-liststring-weights1", String[].class);
		WeightsObj<String, String[]> eListStringWeights2 =
				g.addEdgesWeights("e-liststring-weights2", String[].class, new String[] { "ghi", "jkl", "mno" });
		WeightsObj<String, BigDecimal[]> eListBigDecimalWeights1 =
				g.addEdgesWeights("e-listbigdecimal-weights1", BigDecimal[].class);
		WeightsObj<String, BigDecimal[]> eListBigDecimalWeights2 = g.addEdgesWeights("e-listbigdecimal-weights2",
				BigDecimal[].class, new BigDecimal[] { new BigDecimal("7015938769324.67"),
						new BigDecimal("8909000045689674.02"), new BigDecimal("1111111111235.46") });
		WeightsObj<String, BigInteger[]> eListBigIntegerWeights1 =
				g.addEdgesWeights("e-listbiginteger-weights1", BigInteger[].class);
		WeightsObj<String, BigInteger[]> eListBigIntegerWeights2 = g.addEdgesWeights("e-listbiginteger-weights2",
				BigInteger[].class, new BigInteger[] { new BigInteger("582790273698"), new BigInteger("16549699999"),
						new BigInteger("88888436549874") });

		vByteWeights1.set("n1", (byte) 60);
		vByteWeights2.set("n1", (byte) 61);
		vShortWeights1.set("n1", (short) 62);
		vShortWeights2.set("n1", (short) 63);
		vIntWeights1.set("n1", 64);
		vIntWeights2.set("n1", 65);
		vLongWeights1.set("n1", 66L);
		vLongWeights2.set("n1", 67L);
		vFloatWeights1.set("n1", 68.68f);
		vFloatWeights2.set("n1", 69.69f);
		vDoubleWeights1.set("n1", 70.70);
		vDoubleWeights2.set("n1", 71.71);
		vBooleanWeights1.set("n1", true);
		vBooleanWeights2.set("n1", false);
		vCharWeights1.set("n1", 'd');
		vCharWeights2.set("n1", 'e');
		vStringWeights1.set("n1", "zxw");
		vStringWeights2.set("n1", "yvu");
		vDateWeights1.set("n1", dateFormat.parse("2011-11-11T00:00:00+11"));
		vDateWeights2.set("n1", dateFormat.parse("2012-12-12T00:00:00+12"));
		vUriWeights1.set("n1", new URI("http://example3.com"));
		vUriWeights2.set("n1", new URI("http://example4.com"));
		vBigDecimalWeights1.set("n1", new BigDecimal("59715917907239064783.3564968432"));
		vBigDecimalWeights2.set("n1", new BigDecimal("2736902746897236940.56456156"));
		vBigIntegerWeights1.set("n1", new BigInteger("5467854616418964889257409867"));
		vBigIntegerWeights2.set("n1", new BigInteger("28936702376897367489346345"));
		vListByteWeights1.set("n1", new byte[] { 40, 41, 42, 43, 44 });
		vListByteWeights2.set("n1", new byte[] { 45, 46, 47, 48, 49 });
		vListShortWeights1.set("n1", new short[] { 50, 51, 52, 53, 54 });
		vListShortWeights2.set("n1", new short[] { 55, 56, 57, 58, 59 });
		vListIntegerWeights1.set("n1", new int[] { 60, 61, 62, 63, 64 });
		vListIntegerWeights2.set("n1", new int[] { 65, 66, 67, 68, 69 });
		vListLongWeights1.set("n1", new long[] { 70, 71, 72, 73, 74 });
		vListLongWeights2.set("n1", new long[] { 75, 76, 77, 78, 79 });
		vListFloatWeights1.set("n1", new float[] { 80.80f, 81.81f, 82.82f, 83.83f, 84.84f });
		vListFloatWeights2.set("n1", new float[] { 85.85f, 86.86f, 87.87f, 88.88f, 89.89f });
		vListDoubleWeights1.set("n1", new double[] { 90.90, 91.91, 92.92, 93.93, 94.94 });
		vListDoubleWeights2.set("n1", new double[] { 95.95, 96.96, 97.97, 98.98, 99.99 });
		vListBooleanWeights1.set("n1", new boolean[] { true, false, true, false, true });
		vListBooleanWeights2.set("n1", new boolean[] { false, true, false, true, false });
		vListCharWeights1.set("n1", new char[] { 'h', 'i', 'j', 'k', 'l' });
		vListCharWeights2.set("n1", new char[] { 'm', 'n', 'o', 'p', 'q' });
		vListStringWeights1.set("n1", new String[] { "rst", "uvw", "xyz", "abc", "def" });
		vListStringWeights2.set("n1", new String[] { "ghi", "jkl", "mno", "pqr", "stu" });
		vListBigDecimalWeights1.set("n1",
				new BigDecimal[] { new BigDecimal("1326452364.67"), new BigDecimal("456546547.02"),
						new BigDecimal("8888886783568.46"), new BigDecimal("2345456747.39"),
						new BigDecimal("4573457") });
		vListBigDecimalWeights2.set("n1",
				new BigDecimal[] { new BigDecimal("3333"), new BigDecimal("2345254"),
						new BigDecimal("23642547547.2455874457457"), new BigDecimal("236346262734575367.351564568547"),
						new BigDecimal("245785678735746586") });
		vListBigIntegerWeights1.set("n1",
				new BigInteger[] { new BigInteger("541564646546"), new BigInteger("6542317544532678547854354674574"),
						new BigInteger("25632896357845"), new BigInteger("444444444444") });
		vListBigIntegerWeights2.set("n1", new BigInteger[] { new BigInteger("45674586342") });

		eByteWeights1.set("e1", (byte) 111);
		eByteWeights2.set("e1", (byte) 112);
		eShortWeights1.set("e1", (short) 113);
		eShortWeights2.set("e1", (short) 114);
		eIntWeights1.set("e1", 115);
		eIntWeights2.set("e1", 116);
		eLongWeights1.set("e1", 117L);
		eLongWeights2.set("e1", 118L);
		eFloatWeights1.set("e1", 119.119f);
		eFloatWeights2.set("e1", 120.120f);
		eDoubleWeights1.set("e1", 121.121);
		eDoubleWeights2.set("e1", 122.122);
		eBooleanWeights1.set("e1", true);
		eBooleanWeights2.set("e1", false);
		eCharWeights1.set("e1", 'r');
		eCharWeights2.set("e1", 's');
		eStringWeights1.set("e1", "tuv");
		eStringWeights2.set("e1", "wxy");
		eDateWeights1.set("e1", dateFormat.parse("2013-11-11T00:00:00+11"));
		eDateWeights2.set("e1", dateFormat.parse("2014-12-12T00:00:00+12"));
		eUriWeights1.set("e1", new URI("http://example5.com"));
		eUriWeights2.set("e1", new URI("http://example6.com"));
		eBigDecimalWeights1.set("e1", new BigDecimal("12958713097845609318467.3408578346"));
		eBigDecimalWeights2.set("e1", new BigDecimal("109283598367913.3464573457"));
		eBigIntegerWeights1.set("e1", new BigInteger("674134579835363462475637246275567"));
		eBigIntegerWeights2.set("e1", new BigInteger("93720967520867289467289357345345"));
		eListByteWeights1.set("e1", new byte[] { 46, 47, 48, 49, 50 });
		eListByteWeights2.set("e1", new byte[] { 51, 52, 53, 54, 55 });
		eListShortWeights1.set("e1", new short[] { 56, 57, 58, 59, 60 });
		eListShortWeights2.set("e1", new short[] { 61, 62, 63, 64, 65 });
		eListIntegerWeights1.set("e1", new int[] { 66, 67, 68, 69, 70 });
		eListIntegerWeights2.set("e1", new int[] { 71, 72, 73, 74, 75 });
		eListLongWeights1.set("e1", new long[] { 76, 77, 78, 79, 80 });
		eListLongWeights2.set("e1", new long[] { 81, 82, 83, 84, 85 });
		eListFloatWeights1.set("e1", new float[] { 86.86f, 87.87f, 88.88f, 89.89f, 90.90f });
		eListFloatWeights2.set("e1", new float[] { 91.91f, 92.92f, 93.93f, 94.94f, 95.95f });
		eListDoubleWeights1.set("e1", new double[] { 96.96, 97.97, 98.98, 99.99, 100.100 });
		eListDoubleWeights2.set("e1", new double[] { 101.101, 102.102, 103.103, 104.104, 105.105 });
		eListBooleanWeights1.set("e1", new boolean[] { true, false, true, false, false });
		eListBooleanWeights2.set("e1", new boolean[] { false, true, false, true, true });
		eListCharWeights1.set("e1", new char[] { 'u', 'v', 'w', 'x', 'y' });
		eListCharWeights2.set("e1", new char[] { 'z', 'a', 'b', 'c', 'd' });
		eListStringWeights1.set("e1", new String[] { "efg", "hij", "klm", "nop", "qrs" });
		eListStringWeights2.set("e1", new String[] { "tuv", "wxy", "zab", "cde", "fgh" });
		eListBigDecimalWeights1.set("e1",
				new BigDecimal[] { new BigDecimal("8967236905782930568290678.54654968465"),
						new BigDecimal("910263509570831456"), new BigDecimal("13462462456"),
						new BigDecimal("56823541352456"), new BigDecimal("234987234.1") });
		eListBigDecimalWeights2.set("e1", new BigDecimal[] { new BigDecimal("8888888889182391754123.54"),
				new BigDecimal("1.2589739468723094898647"), new BigDecimal("238423433333333") });
		eListBigIntegerWeights1.set("e1", new BigInteger[] { new BigInteger("67093857982768926749031850345"),
				new BigInteger("5719356758398457938457345"), new BigInteger("45354345346245672392211110001") });
		eListBigIntegerWeights2.set("e1", new BigInteger[] { new BigInteger("13467218190275345"),
				new BigInteger("34895672458967134098234"), new BigInteger("45545454545454545454454545") });

		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		Graph<String, String> g2 = reader.readGraph(new StringReader(text.toString()));

		assertEquals(g.vertices(), g2.vertices());
		assertEquals(g.edges(), g2.edges());
		assertEquals(g.getVerticesWeightsKeys(), g2.getVerticesWeightsKeys());
		assertEquals(g.getEdgesWeightsKeys(), g2.getEdgesWeightsKeys());

		WeightsByte<String> vByteWeights1_2 = g2.getVerticesWeights("v-byte-weights1");
		WeightsByte<String> vByteWeights2_2 = g2.getVerticesWeights("v-byte-weights2");
		WeightsShort<String> vShortWeights1_2 = g2.getVerticesWeights("v-short-weights1");
		WeightsShort<String> vShortWeights2_2 = g2.getVerticesWeights("v-short-weights2");
		WeightsInt<String> vIntWeights1_2 = g2.getVerticesWeights("v-int-weights1");
		WeightsInt<String> vIntWeights2_2 = g2.getVerticesWeights("v-int-weights2");
		WeightsLong<String> vLongWeights1_2 = g2.getVerticesWeights("v-long-weights1");
		WeightsLong<String> vLongWeights2_2 = g2.getVerticesWeights("v-long-weights2");
		WeightsFloat<String> vFloatWeights1_2 = g2.getVerticesWeights("v-float-weights1");
		WeightsFloat<String> vFloatWeights2_2 = g2.getVerticesWeights("v-float-weights2");
		WeightsDouble<String> vDoubleWeights1_2 = g2.getVerticesWeights("v-double-weights1");
		WeightsDouble<String> vDoubleWeights2_2 = g2.getVerticesWeights("v-double-weights2");
		WeightsBool<String> vBooleanWeights1_2 = g2.getVerticesWeights("v-bool-weights1");
		WeightsBool<String> vBooleanWeights2_2 = g2.getVerticesWeights("v-bool-weights2");
		WeightsChar<String> vCharWeights1_2 = g2.getVerticesWeights("v-char-weights1");
		WeightsChar<String> vCharWeights2_2 = g2.getVerticesWeights("v-char-weights2");
		WeightsObj<String, String> vStringWeights1_2 = g2.getVerticesWeights("v-string-weights1");
		WeightsObj<String, String> vStringWeights2_2 = g2.getVerticesWeights("v-string-weights2");
		WeightsObj<String, Date> vDateWeights1_2 = g2.getVerticesWeights("v-date-weights1");
		WeightsObj<String, Date> vDateWeights2_2 = g2.getVerticesWeights("v-date-weights2");
		WeightsObj<String, URI> vUriWeights1_2 = g2.getVerticesWeights("v-uri-weights1");
		WeightsObj<String, URI> vUriWeights2_2 = g2.getVerticesWeights("v-uri-weights2");
		WeightsObj<String, BigDecimal> vBigDecimalWeights1_2 = g2.getVerticesWeights("v-bigdecimal-weights1");
		WeightsObj<String, BigDecimal> vBigDecimalWeights2_2 = g2.getVerticesWeights("v-bigdecimal-weights2");
		WeightsObj<String, BigInteger> vBigIntegerWeights1_2 = g2.getVerticesWeights("v-biginteger-weights1");
		WeightsObj<String, BigInteger> vBigIntegerWeights2_2 = g2.getVerticesWeights("v-biginteger-weights2");
		WeightsObj<String, byte[]> vListByteWeights1_2 = g2.getVerticesWeights("v-listbyte-weights1");
		WeightsObj<String, byte[]> vListByteWeights2_2 = g2.getVerticesWeights("v-listbyte-weights2");
		WeightsObj<String, short[]> vListShortWeights1_2 = g2.getVerticesWeights("v-listshort-weights1");
		WeightsObj<String, short[]> vListShortWeights2_2 = g2.getVerticesWeights("v-listshort-weights2");
		WeightsObj<String, int[]> vListIntegerWeights1_2 = g2.getVerticesWeights("v-listinteger-weights1");
		WeightsObj<String, int[]> vListIntegerWeights2_2 = g2.getVerticesWeights("v-listinteger-weights2");
		WeightsObj<String, long[]> vListLongWeights1_2 = g2.getVerticesWeights("v-listlong-weights1");
		WeightsObj<String, long[]> vListLongWeights2_2 = g2.getVerticesWeights("v-listlong-weights2");
		WeightsObj<String, float[]> vListFloatWeights1_2 = g2.getVerticesWeights("v-listfloat-weights1");
		WeightsObj<String, float[]> vListFloatWeights2_2 = g2.getVerticesWeights("v-listfloat-weights2");
		WeightsObj<String, double[]> vListDoubleWeights1_2 = g2.getVerticesWeights("v-listdouble-weights1");
		WeightsObj<String, double[]> vListDoubleWeights2_2 = g2.getVerticesWeights("v-listdouble-weights2");
		WeightsObj<String, boolean[]> vListBooleanWeights1_2 = g2.getVerticesWeights("v-listboolean-weights1");
		WeightsObj<String, boolean[]> vListBooleanWeights2_2 = g2.getVerticesWeights("v-listboolean-weights2");
		WeightsObj<String, char[]> vListCharWeights1_2 = g2.getVerticesWeights("v-listchar-weights1");
		WeightsObj<String, char[]> vListCharWeights2_2 = g2.getVerticesWeights("v-listchar-weights2");
		WeightsObj<String, String[]> vListStringWeights1_2 = g2.getVerticesWeights("v-liststring-weights1");
		WeightsObj<String, String[]> vListStringWeights2_2 = g2.getVerticesWeights("v-liststring-weights2");
		WeightsObj<String, BigDecimal[]> vListBigDecimalWeights1_2 = g2.getVerticesWeights("v-listbigdecimal-weights1");
		WeightsObj<String, BigDecimal[]> vListBigDecimalWeights2_2 = g2.getVerticesWeights("v-listbigdecimal-weights2");
		WeightsObj<String, BigInteger[]> vListBigIntegerWeights1_2 = g2.getVerticesWeights("v-listbiginteger-weights1");
		WeightsObj<String, BigInteger[]> vListBigIntegerWeights2_2 = g2.getVerticesWeights("v-listbiginteger-weights2");
		assertArrayEquals(vListByteWeights1.defaultWeight(), vListByteWeights1_2.defaultWeight());
		assertArrayEquals(vListByteWeights2.defaultWeight(), vListByteWeights2_2.defaultWeight());
		assertArrayEquals(vListShortWeights1.defaultWeight(), vListShortWeights1_2.defaultWeight());
		assertArrayEquals(vListShortWeights2.defaultWeight(), vListShortWeights2_2.defaultWeight());
		assertArrayEquals(vListIntegerWeights1.defaultWeight(), vListIntegerWeights1_2.defaultWeight());
		assertArrayEquals(vListIntegerWeights2.defaultWeight(), vListIntegerWeights2_2.defaultWeight());
		assertArrayEquals(vListLongWeights1.defaultWeight(), vListLongWeights1_2.defaultWeight());
		assertArrayEquals(vListLongWeights2.defaultWeight(), vListLongWeights2_2.defaultWeight());
		assertArrayEquals(vListFloatWeights1.defaultWeight(), vListFloatWeights1_2.defaultWeight(), 1e-4f);
		assertArrayEquals(vListFloatWeights2.defaultWeight(), vListFloatWeights2_2.defaultWeight(), 1e-4f);
		assertArrayEquals(vListDoubleWeights1.defaultWeight(), vListDoubleWeights1_2.defaultWeight(), 1e-4);
		assertArrayEquals(vListDoubleWeights2.defaultWeight(), vListDoubleWeights2_2.defaultWeight(), 1e-4);
		assertArrayEquals(vListBooleanWeights1.defaultWeight(), vListBooleanWeights1_2.defaultWeight());
		assertArrayEquals(vListBooleanWeights2.defaultWeight(), vListBooleanWeights2_2.defaultWeight());
		assertArrayEquals(vListCharWeights1.defaultWeight(), vListCharWeights1_2.defaultWeight());
		assertArrayEquals(vListCharWeights2.defaultWeight(), vListCharWeights2_2.defaultWeight());
		assertArrayEquals(vListStringWeights1.defaultWeight(), vListStringWeights1_2.defaultWeight());
		assertArrayEquals(vListStringWeights2.defaultWeight(), vListStringWeights2_2.defaultWeight());
		assertArrayEquals(vListBigDecimalWeights1.defaultWeight(), vListBigDecimalWeights1_2.defaultWeight());
		assertArrayEquals(vListBigDecimalWeights2.defaultWeight(), vListBigDecimalWeights2_2.defaultWeight());
		assertArrayEquals(vListBigIntegerWeights1.defaultWeight(), vListBigIntegerWeights1_2.defaultWeight());
		assertArrayEquals(vListBigIntegerWeights2.defaultWeight(), vListBigIntegerWeights2_2.defaultWeight());
		for (String v : g.vertices()) {
			assertEquals(vByteWeights1.get(v), vByteWeights1_2.get(v));
			assertEquals(vByteWeights2.get(v), vByteWeights2_2.get(v));
			assertEquals(vShortWeights1.get(v), vShortWeights1_2.get(v));
			assertEquals(vShortWeights2.get(v), vShortWeights2_2.get(v));
			assertEquals(vIntWeights1.get(v), vIntWeights1_2.get(v));
			assertEquals(vIntWeights2.get(v), vIntWeights2_2.get(v));
			assertEquals(vLongWeights1.get(v), vLongWeights1_2.get(v));
			assertEquals(vLongWeights2.get(v), vLongWeights2_2.get(v));
			assertEquals(vFloatWeights1.get(v), vFloatWeights1_2.get(v));
			assertEquals(vFloatWeights2.get(v), vFloatWeights2_2.get(v));
			assertEquals(vDoubleWeights1.get(v), vDoubleWeights1_2.get(v));
			assertEquals(vDoubleWeights2.get(v), vDoubleWeights2_2.get(v));
			assertEquals(Boolean.valueOf(vBooleanWeights1.get(v)), Boolean.valueOf(vBooleanWeights1_2.get(v)));
			assertEquals(Boolean.valueOf(vBooleanWeights2.get(v)), Boolean.valueOf(vBooleanWeights2_2.get(v)));
			assertEquals(vCharWeights1.get(v), vCharWeights1_2.get(v));
			assertEquals(vCharWeights2.get(v), vCharWeights2_2.get(v));
			assertEquals(vStringWeights1.get(v), vStringWeights1_2.get(v));
			assertEquals(vStringWeights2.get(v), vStringWeights2_2.get(v));
			assertEquals(vDateWeights1.get(v), vDateWeights1_2.get(v));
			assertEquals(vDateWeights2.get(v), vDateWeights2_2.get(v));
			assertEquals(vUriWeights1.get(v), vUriWeights1_2.get(v));
			assertEquals(vUriWeights2.get(v), vUriWeights2_2.get(v));
			assertEquals(vBigDecimalWeights1.get(v), vBigDecimalWeights1_2.get(v));
			assertEquals(vBigDecimalWeights2.get(v), vBigDecimalWeights2_2.get(v));
			assertEquals(vBigIntegerWeights1.get(v), vBigIntegerWeights1_2.get(v));
			assertEquals(vBigIntegerWeights2.get(v), vBigIntegerWeights2_2.get(v));
			assertArrayEquals(vListByteWeights1.get(v), vListByteWeights1_2.get(v));
			assertArrayEquals(vListByteWeights2.get(v), vListByteWeights2_2.get(v));
			assertArrayEquals(vListShortWeights1.get(v), vListShortWeights1_2.get(v));
			assertArrayEquals(vListShortWeights2.get(v), vListShortWeights2_2.get(v));
			assertArrayEquals(vListIntegerWeights1.get(v), vListIntegerWeights1_2.get(v));
			assertArrayEquals(vListIntegerWeights2.get(v), vListIntegerWeights2_2.get(v));
			assertArrayEquals(vListLongWeights1.get(v), vListLongWeights1_2.get(v));
			assertArrayEquals(vListLongWeights2.get(v), vListLongWeights2_2.get(v));
			assertArrayEquals(vListFloatWeights1.get(v), vListFloatWeights1_2.get(v), 1e-4f);
			assertArrayEquals(vListFloatWeights2.get(v), vListFloatWeights2_2.get(v), 1e-4f);
			assertArrayEquals(vListDoubleWeights1.get(v), vListDoubleWeights1_2.get(v), 1e-4);
			assertArrayEquals(vListDoubleWeights2.get(v), vListDoubleWeights2_2.get(v), 1e-4);
			assertArrayEquals(vListBooleanWeights1.get(v), vListBooleanWeights1_2.get(v));
			assertArrayEquals(vListBooleanWeights2.get(v), vListBooleanWeights2_2.get(v));
			assertArrayEquals(vListCharWeights1.get(v), vListCharWeights1_2.get(v));
			assertArrayEquals(vListCharWeights2.get(v), vListCharWeights2_2.get(v));
			assertArrayEquals(vListStringWeights1.get(v), vListStringWeights1_2.get(v));
			assertArrayEquals(vListStringWeights2.get(v), vListStringWeights2_2.get(v));
			assertArrayEquals(vListBigDecimalWeights1.get(v), vListBigDecimalWeights1_2.get(v));
			assertArrayEquals(vListBigDecimalWeights2.get(v), vListBigDecimalWeights2_2.get(v));
			assertArrayEquals(vListBigIntegerWeights1.get(v), vListBigIntegerWeights1_2.get(v));
			assertArrayEquals(vListBigIntegerWeights2.get(v), vListBigIntegerWeights2_2.get(v));
		}

		WeightsByte<String> eByteWeights1_2 = g2.getEdgesWeights("e-byte-weights1");
		WeightsByte<String> eByteWeights2_2 = g2.getEdgesWeights("e-byte-weights2");
		WeightsShort<String> eShortWeights1_2 = g2.getEdgesWeights("e-short-weights1");
		WeightsShort<String> eShortWeights2_2 = g2.getEdgesWeights("e-short-weights2");
		WeightsInt<String> eIntWeights1_2 = g2.getEdgesWeights("e-int-weights1");
		WeightsInt<String> eIntWeights2_2 = g2.getEdgesWeights("e-int-weights2");
		WeightsLong<String> eLongWeights1_2 = g2.getEdgesWeights("e-long-weights1");
		WeightsLong<String> eLongWeights2_2 = g2.getEdgesWeights("e-long-weights2");
		WeightsFloat<String> eFloatWeights1_2 = g2.getEdgesWeights("e-float-weights1");
		WeightsFloat<String> eFloatWeights2_2 = g2.getEdgesWeights("e-float-weights2");
		WeightsDouble<String> eDoubleWeights1_2 = g2.getEdgesWeights("e-double-weights1");
		WeightsDouble<String> eDoubleWeights2_2 = g2.getEdgesWeights("e-double-weights2");
		WeightsBool<String> eBooleanWeights1_2 = g2.getEdgesWeights("e-bool-weights1");
		WeightsBool<String> eBooleanWeights2_2 = g2.getEdgesWeights("e-bool-weights2");
		WeightsChar<String> eCharWeights1_2 = g2.getEdgesWeights("e-char-weights1");
		WeightsChar<String> eCharWeights2_2 = g2.getEdgesWeights("e-char-weights2");
		WeightsObj<String, String> eStringWeights1_2 = g2.getEdgesWeights("e-string-weights1");
		WeightsObj<String, String> eStringWeights2_2 = g2.getEdgesWeights("e-string-weights2");
		WeightsObj<String, Date> eDateWeights1_2 = g2.getEdgesWeights("e-date-weights1");
		WeightsObj<String, Date> eDateWeights2_2 = g2.getEdgesWeights("e-date-weights2");
		WeightsObj<String, URI> eUriWeights1_2 = g2.getEdgesWeights("e-uri-weights1");
		WeightsObj<String, URI> eUriWeights2_2 = g2.getEdgesWeights("e-uri-weights2");
		WeightsObj<String, BigDecimal> eBigDecimalWeights1_2 = g2.getEdgesWeights("e-bigdecimal-weights1");
		WeightsObj<String, BigDecimal> eBigDecimalWeights2_2 = g2.getEdgesWeights("e-bigdecimal-weights2");
		WeightsObj<String, BigInteger> eBigIntegerWeights1_2 = g2.getEdgesWeights("e-biginteger-weights1");
		WeightsObj<String, BigInteger> eBigIntegerWeights2_2 = g2.getEdgesWeights("e-biginteger-weights2");
		WeightsObj<String, byte[]> eListByteWeights1_2 = g2.getEdgesWeights("e-listbyte-weights1");
		WeightsObj<String, byte[]> eListByteWeights2_2 = g2.getEdgesWeights("e-listbyte-weights2");
		WeightsObj<String, short[]> eListShortWeights1_2 = g2.getEdgesWeights("e-listshort-weights1");
		WeightsObj<String, short[]> eListShortWeights2_2 = g2.getEdgesWeights("e-listshort-weights2");
		WeightsObj<String, int[]> eListIntegerWeights1_2 = g2.getEdgesWeights("e-listinteger-weights1");
		WeightsObj<String, int[]> eListIntegerWeights2_2 = g2.getEdgesWeights("e-listinteger-weights2");
		WeightsObj<String, long[]> eListLongWeights1_2 = g2.getEdgesWeights("e-listlong-weights1");
		WeightsObj<String, long[]> eListLongWeights2_2 = g2.getEdgesWeights("e-listlong-weights2");
		WeightsObj<String, float[]> eListFloatWeights1_2 = g2.getEdgesWeights("e-listfloat-weights1");
		WeightsObj<String, float[]> eListFloatWeights2_2 = g2.getEdgesWeights("e-listfloat-weights2");
		WeightsObj<String, double[]> eListDoubleWeights1_2 = g2.getEdgesWeights("e-listdouble-weights1");
		WeightsObj<String, double[]> eListDoubleWeights2_2 = g2.getEdgesWeights("e-listdouble-weights2");
		WeightsObj<String, boolean[]> eListBooleanWeights1_2 = g2.getEdgesWeights("e-listboolean-weights1");
		WeightsObj<String, boolean[]> eListBooleanWeights2_2 = g2.getEdgesWeights("e-listboolean-weights2");
		WeightsObj<String, char[]> eListCharWeights1_2 = g2.getEdgesWeights("e-listchar-weights1");
		WeightsObj<String, char[]> eListCharWeights2_2 = g2.getEdgesWeights("e-listchar-weights2");
		WeightsObj<String, String[]> eListStringWeights1_2 = g2.getEdgesWeights("e-liststring-weights1");
		WeightsObj<String, String[]> eListStringWeights2_2 = g2.getEdgesWeights("e-liststring-weights2");
		WeightsObj<String, BigDecimal[]> eListBigDecimalWeights1_2 = g2.getEdgesWeights("e-listbigdecimal-weights1");
		WeightsObj<String, BigDecimal[]> eListBigDecimalWeights2_2 = g2.getEdgesWeights("e-listbigdecimal-weights2");
		WeightsObj<String, BigInteger[]> eListBigIntegerWeights1_2 = g2.getEdgesWeights("e-listbiginteger-weights1");
		WeightsObj<String, BigInteger[]> eListBigIntegerWeights2_2 = g2.getEdgesWeights("e-listbiginteger-weights2");
		assertArrayEquals(eListByteWeights1.defaultWeight(), eListByteWeights1_2.defaultWeight());
		assertArrayEquals(eListByteWeights2.defaultWeight(), eListByteWeights2_2.defaultWeight());
		assertArrayEquals(eListShortWeights1.defaultWeight(), eListShortWeights1_2.defaultWeight());
		assertArrayEquals(eListShortWeights2.defaultWeight(), eListShortWeights2_2.defaultWeight());
		assertArrayEquals(eListIntegerWeights1.defaultWeight(), eListIntegerWeights1_2.defaultWeight());
		assertArrayEquals(eListIntegerWeights2.defaultWeight(), eListIntegerWeights2_2.defaultWeight());
		assertArrayEquals(eListLongWeights1.defaultWeight(), eListLongWeights1_2.defaultWeight());
		assertArrayEquals(eListLongWeights2.defaultWeight(), eListLongWeights2_2.defaultWeight());
		assertArrayEquals(eListFloatWeights1.defaultWeight(), eListFloatWeights1_2.defaultWeight(), 1e-4f);
		assertArrayEquals(eListFloatWeights2.defaultWeight(), eListFloatWeights2_2.defaultWeight(), 1e-4f);
		assertArrayEquals(eListDoubleWeights1.defaultWeight(), eListDoubleWeights1_2.defaultWeight(), 1e-4);
		assertArrayEquals(eListDoubleWeights2.defaultWeight(), eListDoubleWeights2_2.defaultWeight(), 1e-4);
		assertArrayEquals(eListBooleanWeights1.defaultWeight(), eListBooleanWeights1_2.defaultWeight());
		assertArrayEquals(eListBooleanWeights2.defaultWeight(), eListBooleanWeights2_2.defaultWeight());
		assertArrayEquals(eListCharWeights1.defaultWeight(), eListCharWeights1_2.defaultWeight());
		assertArrayEquals(eListCharWeights2.defaultWeight(), eListCharWeights2_2.defaultWeight());
		assertArrayEquals(eListStringWeights1.defaultWeight(), eListStringWeights1_2.defaultWeight());
		assertArrayEquals(eListStringWeights2.defaultWeight(), eListStringWeights2_2.defaultWeight());
		assertArrayEquals(eListBigDecimalWeights1.defaultWeight(), eListBigDecimalWeights1_2.defaultWeight());
		assertArrayEquals(eListBigDecimalWeights2.defaultWeight(), eListBigDecimalWeights2_2.defaultWeight());
		assertArrayEquals(eListBigIntegerWeights1.defaultWeight(), eListBigIntegerWeights1_2.defaultWeight());
		assertArrayEquals(eListBigIntegerWeights2.defaultWeight(), eListBigIntegerWeights2_2.defaultWeight());
		for (String e : g.edges()) {
			assertEquals(eByteWeights1.get(e), eByteWeights1_2.get(e));
			assertEquals(eByteWeights2.get(e), eByteWeights2_2.get(e));
			assertEquals(eShortWeights1.get(e), eShortWeights1_2.get(e));
			assertEquals(eShortWeights2.get(e), eShortWeights2_2.get(e));
			assertEquals(eIntWeights1.get(e), eIntWeights1_2.get(e));
			assertEquals(eIntWeights2.get(e), eIntWeights2_2.get(e));
			assertEquals(eLongWeights1.get(e), eLongWeights1_2.get(e));
			assertEquals(eLongWeights2.get(e), eLongWeights2_2.get(e));
			assertEquals(eFloatWeights1.get(e), eFloatWeights1_2.get(e));
			assertEquals(eFloatWeights2.get(e), eFloatWeights2_2.get(e));
			assertEquals(eDoubleWeights1.get(e), eDoubleWeights1_2.get(e));
			assertEquals(eDoubleWeights2.get(e), eDoubleWeights2_2.get(e));
			assertEquals(Boolean.valueOf(eBooleanWeights1.get(e)), Boolean.valueOf(eBooleanWeights1_2.get(e)));
			assertEquals(Boolean.valueOf(eBooleanWeights2.get(e)), Boolean.valueOf(eBooleanWeights2_2.get(e)));
			assertEquals(eCharWeights1.get(e), eCharWeights1_2.get(e));
			assertEquals(eCharWeights2.get(e), eCharWeights2_2.get(e));
			assertEquals(eStringWeights1.get(e), eStringWeights1_2.get(e));
			assertEquals(eStringWeights2.get(e), eStringWeights2_2.get(e));
			assertEquals(eDateWeights1.get(e), eDateWeights1_2.get(e));
			assertEquals(eDateWeights2.get(e), eDateWeights2_2.get(e));
			assertEquals(eUriWeights1.get(e), eUriWeights1_2.get(e));
			assertEquals(eUriWeights2.get(e), eUriWeights2_2.get(e));
			assertEquals(eBigDecimalWeights1.get(e), eBigDecimalWeights1_2.get(e));
			assertEquals(eBigDecimalWeights2.get(e), eBigDecimalWeights2_2.get(e));
			assertEquals(eBigIntegerWeights1.get(e), eBigIntegerWeights1_2.get(e));
			assertEquals(eBigIntegerWeights2.get(e), eBigIntegerWeights2_2.get(e));
			assertArrayEquals(eListByteWeights1.get(e), eListByteWeights1_2.get(e));
			assertArrayEquals(eListByteWeights2.get(e), eListByteWeights2_2.get(e));
			assertArrayEquals(eListShortWeights1.get(e), eListShortWeights1_2.get(e));
			assertArrayEquals(eListShortWeights2.get(e), eListShortWeights2_2.get(e));
			assertArrayEquals(eListIntegerWeights1.get(e), eListIntegerWeights1_2.get(e));
			assertArrayEquals(eListIntegerWeights2.get(e), eListIntegerWeights2_2.get(e));
			assertArrayEquals(eListLongWeights1.get(e), eListLongWeights1_2.get(e));
			assertArrayEquals(eListLongWeights2.get(e), eListLongWeights2_2.get(e));
			assertArrayEquals(eListFloatWeights1.get(e), eListFloatWeights1_2.get(e), 1e-4f);
			assertArrayEquals(eListFloatWeights2.get(e), eListFloatWeights2_2.get(e), 1e-4f);
			assertArrayEquals(eListDoubleWeights1.get(e), eListDoubleWeights1_2.get(e), 1e-4);
			assertArrayEquals(eListDoubleWeights2.get(e), eListDoubleWeights2_2.get(e), 1e-4);
			assertArrayEquals(eListBooleanWeights1.get(e), eListBooleanWeights1_2.get(e));
			assertArrayEquals(eListBooleanWeights2.get(e), eListBooleanWeights2_2.get(e));
			assertArrayEquals(eListCharWeights1.get(e), eListCharWeights1_2.get(e));
			assertArrayEquals(eListCharWeights2.get(e), eListCharWeights2_2.get(e));
			assertArrayEquals(eListStringWeights1.get(e), eListStringWeights1_2.get(e));
			assertArrayEquals(eListStringWeights2.get(e), eListStringWeights2_2.get(e));
			assertArrayEquals(eListBigDecimalWeights1.get(e), eListBigDecimalWeights1_2.get(e));
			assertArrayEquals(eListBigDecimalWeights2.get(e), eListBigDecimalWeights2_2.get(e));
			assertArrayEquals(eListBigIntegerWeights1.get(e), eListBigIntegerWeights1_2.get(e));
			assertArrayEquals(eListBigIntegerWeights2.get(e), eListBigIntegerWeights2_2.get(e));
		}
	}

	@Test
	public void readWeightsWithOptions() {
		String[] lines = new String[18];
		lines[0] = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		lines[1] = "<gexf xmlns=\"http://gexf.net/1.3\">";
		lines[2] = "  <graph defaultedgetype=\"directed\">";
		lines[3] = "    <attributes class=\"node\">";
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"integer\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[7] = "      </attribute>";
		lines[8] = "    </attributes>";
		lines[9] = "    <nodes>";
		lines[10] = "      <node id=\"n0\">";
		lines[11] = "        <attvalues>";
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		lines[13] = "        </attvalues>";
		lines[14] = "      </node>";
		lines[15] = "    </nodes>";
		lines[16] = "  </graph>";
		lines[17] = "</gexf>";
		Supplier<String> text = () -> {
			StringBuilder sb = new StringBuilder();
			for (String line : lines) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		};
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"byte\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"byte\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>3</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"byte\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"3\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"short\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"short\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>3</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"short\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"3\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"integer\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"integer\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>3</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"integer\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"3\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"long\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"long\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>3</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"long\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"3\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"float\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"float\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>3</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"float\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"3\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"double\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"double\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>3</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"double\">";
		lines[5] = "        <options>[0, 1, 2]</options>";
		lines[6] = "        <default>0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"3\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"boolean\">";
		lines[5] = "        <options>[true, false]</options>";
		lines[6] = "        <default>false</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"true\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"boolean\">";
		lines[5] = "        <options>[true]</options>";
		lines[6] = "        <default>false</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"true\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"boolean\">";
		lines[5] = "        <options>[false]</options>";
		lines[6] = "        <default>false</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"true\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"char\">";
		lines[5] = "        <options>[a, b, c]</options>";
		lines[6] = "        <default>a</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"c\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"char\">";
		lines[5] = "        <options>[a, b, c]</options>";
		lines[6] = "        <default>d</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"c\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"char\">";
		lines[5] = "        <options>[a, b, c]</options>";
		lines[6] = "        <default>a</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"d\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"string\">";
		lines[5] = "        <options>[hello, world]</options>";
		lines[6] = "        <default>hello</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"world\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"string\">";
		lines[5] = "        <options>[hello, world]</options>";
		lines[6] = "        <default>goodbye</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"world\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"string\">";
		lines[5] = "        <options>[hello, world]</options>";
		lines[6] = "        <default>hello</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"goodbye\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"date\">";
		lines[5] = "        <options>[2013-11-11T00:00:00+11, 2055-03-11T00:00:00+07]</options>";
		lines[6] = "        <default>2013-11-11T00:00:00+11</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2055-03-11T00:00:00+07\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"date\">";
		lines[5] = "        <options>[2013-11-11T00:00:00+11, 2055-03-11T00:00:00+07]</options>";
		lines[6] = "        <default>1999-11-11T00:00:00+05</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2055-03-11T00:00:00+07\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"date\">";
		lines[5] = "        <options>[2013-11-11T00:00:00+11, 2055-03-11T00:00:00+07]</options>";
		lines[6] = "        <default>2013-11-11T00:00:00+11</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2001-01-23T00:00:00+11\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"anyURI\">";
		lines[5] = "        <options>[http://example.com, http://example.org]</options>";
		lines[6] = "        <default>http://example.com</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"http://example.org\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"anyURI\">";
		lines[5] = "        <options>[http://example.com, http://example.org]</options>";
		lines[6] = "        <default>http://example.net</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"http://example.org\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"anyURI\">";
		lines[5] = "        <options>[http://example.com, http://example.org]</options>";
		lines[6] = "        <default>http://example.com</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"http://example.net\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"bigdecimal\">";
		lines[5] = "        <options>[1.0, 2.0]</options>";
		lines[6] = "        <default>1.0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2.0\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"bigdecimal\">";
		lines[5] = "        <options>[1.0, 2.0]</options>";
		lines[6] = "        <default>3.0</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2.0\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"bigdecimal\">";
		lines[5] = "        <options>[1.0, 2.0]</options>";
		lines[6] = "        <default>1.0</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"3.0\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"biginteger\">";
		lines[5] = "        <options>[1, 2]</options>";
		lines[6] = "        <default>1</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"biginteger\">";
		lines[5] = "        <options>[1, 2]</options>";
		lines[6] = "        <default>3</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"2\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"biginteger\">";
		lines[5] = "        <options>[1, 2]</options>";
		lines[6] = "        <default>1</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"3\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbyte\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbyte\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 5]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbyte\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 5]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listshort\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listshort\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 5]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listshort\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 5]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listinteger\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listinteger\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 5]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listinteger\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 5]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listlong\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listlong\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 5]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listlong\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 5]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listfloat\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 1.5]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 3.5]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listfloat\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 5.5]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 3.5]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listfloat\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 1.5]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 5.5]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listdouble\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 1.5]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 3.5]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listdouble\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 5.5]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 3.5]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listdouble\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 1.5]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 5.5]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listboolean\">";
		lines[5] = "        <options>[true, false]</options>";
		lines[6] = "        <default>[true, true]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[false, false]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listboolean\">";
		lines[5] = "        <options>[true]</options>";
		lines[6] = "        <default>[true, false]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[true, true]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listboolean\">";
		lines[5] = "        <options>[false]</options>";
		lines[6] = "        <default>[false, false]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[false, true]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listchar\">";
		lines[5] = "        <options>[a, b, c, d]</options>";
		lines[6] = "        <default>[c, d]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[b, a]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listchar\">";
		lines[5] = "        <options>[a, b, c, d]</options>";
		lines[6] = "        <default>[c, k]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[b, a]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listchar\">";
		lines[5] = "        <options>[a, b, c, d]</options>";
		lines[6] = "        <default>[c, d]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[b, k]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"liststring\">";
		lines[5] = "        <options>[hello, beautiful, world]</options>";
		lines[6] = "        <default>[hello, world]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[world, beautiful]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"liststring\">";
		lines[5] = "        <options>[hello, beautiful, world]</options>";
		lines[6] = "        <default>[hello, amazing, world]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[world, beautiful]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"liststring\">";
		lines[5] = "        <options>[hello, beautiful, world]</options>";
		lines[6] = "        <default>[hello, world]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[awful, world]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbigdecimal\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 1.5]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 3.5]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbigdecimal\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 5.5]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 3.5]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbigdecimal\">";
		lines[5] = "        <options>[0.5, 1.5, 2.5, 3.5]</options>";
		lines[6] = "        <default>[0.5, 1.5]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2.5, 5.5]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));

		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbiginteger\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertNotNull(reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbiginteger\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 5]</default>"; /* default not in options */
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 3]\"/>";
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
		lines[4] = "      <attribute id=\"0\" title=\"weight\" type=\"listbiginteger\">";
		lines[5] = "        <options>[0, 1, 2, 3]</options>";
		lines[6] = "        <default>[0, 1]</default>";
		lines[12] = "          <attvalue for=\"0\" value=\"[2, 5]\"/>"; /* value not in options */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsOptionsInvalid() {
		TextBuilder text = new TextBuilder();
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"0\" title=\"weight\" type=\"integer\">");
		text.addLine("        <options>0, 1, 2]</options>"); /* missing '[' */
		text.addLine("        <default>0</default>");
		text.addLine("      </attribute>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"0\" value=\"2\"/>");
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"0\" title=\"weight\" type=\"integer\">");
		text.addLine("        <options>[0, 1, 2</options>"); /* missing ']' */
		text.addLine("        <default>0</default>");
		text.addLine("      </attribute>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"0\" value=\"2\"/>");
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readWeightsInvalidDate() {
		TextBuilder text = new TextBuilder();
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"0\" title=\"weight\" type=\"date\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"0\" value=\"invaliddate\"/>");
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readWeightsInvalidUri() {
		TextBuilder text = new TextBuilder();
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"0\" title=\"weight\" type=\"anyURI\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"0\" value=\":invaliduri...\"/>");
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readWeightsUnknownType() {
		TextBuilder text = new TextBuilder();
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"0\" title=\"weight\" type=\"unknowntype\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readWeightsUnknownWeight() {
		TextBuilder text = new TextBuilder();
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"1\" title=\"weight1\" type=\"integer\"/>");
		text.addLine("      <attribute id=\"2\" title=\"weight2\" type=\"integer\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"1\" value=\"1\"/>");
		text.addLine("          <attvalue for=\"2\" value=\"2\"/>");
		text.addLine("          <attvalue for=\"3\" value=\"3\"/>"); /* unknown weight */
		text.addLine("        </attvalues>");
		text.addLine("      </node>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"edge\">");
		text.addLine("      <attribute id=\"1\" title=\"weight1\" type=\"integer\"/>");
		text.addLine("      <attribute id=\"2\" title=\"weight2\" type=\"integer\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("      <node id=\"n1\"/>");
		text.addLine("    </nodes>");
		text.addLine("    <edges>");
		text.addLine("      <edge id=\"8\" source=\"n0\" target=\"n1\">");
		text.addLine("        <attvalues>");
		text.addLine("          <attvalue for=\"1\" value=\"1\"/>");
		text.addLine("          <attvalue for=\"2\" value=\"2\"/>");
		text.addLine("          <attvalue for=\"3\" value=\"3\"/>"); /* unknown weight */
		text.addLine("        </attvalues>");
		text.addLine("      </edge>");
		text.addLine("    </edges>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readVerticesWeightsSameId() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"0\" title=\"weight1\" type=\"integer\"/>");
		text.addLine("      <attribute id=\"0\" title=\"weight2\" type=\"string\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVerticesWeightsSameTitle() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"node\">");
		text.addLine("      <attribute id=\"1\" title=\"weight\" type=\"integer\"/>");
		text.addLine("      <attribute id=\"2\" title=\"weight\" type=\"string\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgesWeightsSameId() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"edge\">");
		text.addLine("      <attribute id=\"0\" title=\"weight1\" type=\"integer\"/>");
		text.addLine("      <attribute id=\"0\" title=\"weight2\" type=\"string\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgesWeightsSameTitle() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <attributes class=\"edge\">");
		text.addLine("      <attribute id=\"1\" title=\"weight\" type=\"integer\"/>");
		text.addLine("      <attribute id=\"2\" title=\"weight\" type=\"string\"/>");
		text.addLine("    </attributes>");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"n0\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		GexfGraphReader<String, String> reader = new GexfGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readIntGraph() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<gexf xmlns=\"http://gexf.net/1.3\">");
		text.addLine("  <graph defaultedgetype=\"directed\">");
		text.addLine("    <nodes>");
		text.addLine("      <node id=\"1\"/>");
		text.addLine("    </nodes>");
		text.addLine("  </graph>");
		text.addLine("</gexf>");
		assertTrue(new GexfGraphReader<>(int.class, int.class)
				.readGraph(new StringReader(text.get())) instanceof IntGraph);
		assertFalse(new GexfGraphReader<>(Integer.class, Integer.class)
				.readGraph(new StringReader(text.get())) instanceof IntGraph);
		assertFalse(new GexfGraphReader<>(int.class, Integer.class)
				.readGraph(new StringReader(text.get())) instanceof IntGraph);
		assertFalse(new GexfGraphReader<>(Integer.class, int.class)
				.readGraph(new StringReader(text.get())) instanceof IntGraph);
	}

	@Test
	public void writeEmptyGraph() {
		IntGraph g = IntGraph.newUndirected();

		GexfGraphWriter<Integer, Integer> writer = new GexfGraphWriter<>();
		StringWriter sw = new StringWriter();
		writer.writeGraph(g, sw);
		String text = sw.toString();

		GexfGraphReader<Integer, Integer> reader = new GexfGraphReader<>(int.class, int.class);
		assertEquals(g, reader.readGraph(new StringReader(text)));
	}

	@Test
	public void writeWeights() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);

		Function<String, Date> date = s -> {
			try {
				return new SimpleDateFormat("yyyy-MM-dd").parse(s);
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		};

		IWeightsByte weightsByte1 = g.addVerticesWeights("vByte1", byte.class);
		IWeightsByte weightsByte2 = g.addVerticesWeights("vByte2", byte.class, Byte.valueOf((byte) 1));
		IWeightsObj<Byte> weightsByte3 = g.addVerticesWeights("vByte3", Byte.class);
		IWeightsObj<Byte> weightsByte4 = g.addVerticesWeights("vByte4", Byte.class, Byte.valueOf((byte) 2));
		IWeightsShort weightsShort1 = g.addVerticesWeights("vShort1", short.class);
		IWeightsShort weightsShort2 = g.addVerticesWeights("vShort2", short.class, Short.valueOf((short) 3));
		IWeightsObj<Short> weightsShort3 = g.addVerticesWeights("vShort3", Short.class);
		IWeightsObj<Short> weightsShort4 = g.addVerticesWeights("vShort4", Short.class, Short.valueOf((short) 4));
		IWeightsInt weightsInt1 = g.addVerticesWeights("vInt1", int.class);
		IWeightsInt weightsInt2 = g.addVerticesWeights("vInt2", int.class, Integer.valueOf(5));
		IWeightsObj<Integer> weightsInt3 = g.addVerticesWeights("vInt3", Integer.class);
		IWeightsObj<Integer> weightsInt4 = g.addVerticesWeights("vInt4", Integer.class, Integer.valueOf(6));
		IWeightsLong weightsLong1 = g.addVerticesWeights("vLong1", long.class);
		IWeightsLong weightsLong2 = g.addVerticesWeights("vLong2", long.class, Long.valueOf(7L));
		IWeightsObj<Long> weightsLong3 = g.addVerticesWeights("vLong3", Long.class);
		IWeightsObj<Long> weightsLong4 = g.addVerticesWeights("vLong4", Long.class, Long.valueOf(8L));
		IWeightsFloat weightsFloat1 = g.addVerticesWeights("vFloat1", float.class);
		IWeightsFloat weightsFloat2 = g.addVerticesWeights("vFloat2", float.class, Float.valueOf(9.9f));
		IWeightsObj<Float> weightsFloat3 = g.addVerticesWeights("vFloat3", Float.class);
		IWeightsObj<Float> weightsFloat4 = g.addVerticesWeights("vFloat4", Float.class, Float.valueOf(10.10f));
		IWeightsDouble weightsDouble1 = g.addVerticesWeights("vDouble1", double.class);
		IWeightsDouble weightsDouble2 = g.addVerticesWeights("vDouble2", double.class, Double.valueOf(11.11));
		IWeightsObj<Double> weightsDouble3 = g.addVerticesWeights("vDouble3", Double.class);
		IWeightsObj<Double> weightsDouble4 = g.addVerticesWeights("vDouble4", Double.class, Double.valueOf(12.12));
		IWeightsBool weightsBoolean1 = g.addVerticesWeights("vBoolean1", boolean.class);
		IWeightsBool weightsBoolean2 = g.addVerticesWeights("vBoolean2", boolean.class, Boolean.TRUE);
		IWeightsObj<Boolean> weightsBoolean3 = g.addVerticesWeights("vBoolean3", Boolean.class);
		IWeightsObj<Boolean> weightsBoolean4 = g.addVerticesWeights("vBoolean4", Boolean.class, Boolean.valueOf(true));
		// IWeightsChar weightsChar1 = g.addVerticesWeights("vChar1", char.class);
		IWeightsChar weightsChar2 = g.addVerticesWeights("vChar2", char.class, Character.valueOf('a'));
		IWeightsObj<Character> weightsChar3 = g.addVerticesWeights("vChar3", Character.class);
		IWeightsObj<Character> weightsChar4 = g.addVerticesWeights("vChar4", Character.class, Character.valueOf('b'));
		IWeightsObj<String> weightsString1 = g.addVerticesWeights("vString1", String.class);
		IWeightsObj<String> weightsString2 = g.addVerticesWeights("vString2", String.class, "abc");
		IWeightsObj<Date> weightsDate1 = g.addVerticesWeights("vDate1", Date.class);
		IWeightsObj<Date> weightsDate2 = g.addVerticesWeights("vDate2", Date.class, date.apply("2019-12-31"));
		IWeightsObj<URI> weightsUri1 = g.addVerticesWeights("vUri1", URI.class);
		IWeightsObj<URI> weightsUri2 = g.addVerticesWeights("vUri2", URI.class, URI.create("http://example.com"));
		IWeightsObj<BigDecimal> weightsBigDecimal1 = g.addVerticesWeights("vBigDecimal1", BigDecimal.class);
		IWeightsObj<BigDecimal> weightsBigDecimal2 =
				g.addVerticesWeights("vBigDecimal2", BigDecimal.class, new BigDecimal("1234567890.1234567890"));
		IWeightsObj<BigInteger> weightsBigInteger1 = g.addVerticesWeights("vBigInteger1", BigInteger.class);
		IWeightsObj<BigInteger> weightsBigInteger2 =
				g.addVerticesWeights("vBigInteger2", BigInteger.class, new BigInteger("12345678901234567890"));
		IWeightsObj<byte[]> weightsByteList1 = g.addVerticesWeights("vByteList1", byte[].class);
		IWeightsObj<byte[]> weightsByteList2 = g.addVerticesWeights("vByteList2", byte[].class, new byte[] { 1, 2 });
		IWeightsObj<ByteList> weightsByteList3 = g.addVerticesWeights("vByteList3", ByteList.class);
		IWeightsObj<ByteList> weightsByteList4 =
				g.addVerticesWeights("vByteList4", ByteList.class, ByteList.of((byte) 3, (byte) 4));
		IWeightsObj<List<Byte>> weightsByteList5 = g.addVerticesWeights("vByteList5", List.class);
		IWeightsObj<List<Byte>> weightsByteList6 =
				g.addVerticesWeights("vByteList6", List.class, List.of(Byte.valueOf((byte) 5), Byte.valueOf((byte) 6)));
		IWeightsObj<short[]> weightsShortList1 = g.addVerticesWeights("vShortList1", short[].class);
		IWeightsObj<short[]> weightsShortList2 =
				g.addVerticesWeights("vShortList2", short[].class, new short[] { 7, 8 });
		IWeightsObj<ShortList> weightsShortList3 = g.addVerticesWeights("vShortList3", ShortList.class);
		IWeightsObj<ShortList> weightsShortList4 =
				g.addVerticesWeights("vShortList4", ShortList.class, ShortList.of((short) 9, (short) 10));
		IWeightsObj<List<Short>> weightsShortList5 = g.addVerticesWeights("vShortList5", List.class);
		IWeightsObj<List<Short>> weightsShortList6 = g.addVerticesWeights("vShortList6", List.class,
				List.of(Short.valueOf((short) 11), Short.valueOf((short) 12)));
		IWeightsObj<int[]> weightsIntList1 = g.addVerticesWeights("vIntList1", int[].class);
		IWeightsObj<int[]> weightsIntList2 = g.addVerticesWeights("vIntList2", int[].class, new int[] { 13, 14 });
		IWeightsObj<IntList> weightsIntList3 = g.addVerticesWeights("vIntList3", IntList.class);
		IWeightsObj<IntList> weightsIntList4 = g.addVerticesWeights("vIntList4", IntList.class, IntList.of(15, 16));
		IWeightsObj<List<Integer>> weightsIntList5 = g.addVerticesWeights("vIntList5", List.class);
		IWeightsObj<List<Integer>> weightsIntList6 =
				g.addVerticesWeights("vIntList6", List.class, List.of(Integer.valueOf(17), Integer.valueOf(18)));
		IWeightsObj<long[]> weightsLongList1 = g.addVerticesWeights("vLongList1", long[].class);
		IWeightsObj<long[]> weightsLongList2 = g.addVerticesWeights("vLongList2", long[].class, new long[] { 19, 20 });
		IWeightsObj<LongList> weightsLongList3 = g.addVerticesWeights("vLongList3", LongList.class);
		IWeightsObj<LongList> weightsLongList4 =
				g.addVerticesWeights("vLongList4", LongList.class, LongList.of(21L, 22L));
		IWeightsObj<List<Long>> weightsLongList5 = g.addVerticesWeights("vLongList5", List.class);
		IWeightsObj<List<Long>> weightsLongList6 =
				g.addVerticesWeights("vLongList6", List.class, List.of(Long.valueOf(23L), Long.valueOf(24L)));
		IWeightsObj<float[]> weightsFloatList1 = g.addVerticesWeights("vFloatList1", float[].class);
		IWeightsObj<float[]> weightsFloatList2 =
				g.addVerticesWeights("vFloatList2", float[].class, new float[] { 25.25f, 26.26f });
		IWeightsObj<FloatList> weightsFloatList3 = g.addVerticesWeights("vFloatList3", FloatList.class);
		IWeightsObj<FloatList> weightsFloatList4 =
				g.addVerticesWeights("vFloatList4", FloatList.class, FloatList.of(27.27f, 28.28f));
		IWeightsObj<List<Float>> weightsFloatList5 = g.addVerticesWeights("vFloatList5", List.class);
		IWeightsObj<List<Float>> weightsFloatList6 =
				g.addVerticesWeights("vFloatList6", List.class, List.of(Float.valueOf(29.29f), Float.valueOf(30.30f)));
		IWeightsObj<double[]> weightsDoubleList1 = g.addVerticesWeights("vDoubleList1", double[].class);
		IWeightsObj<double[]> weightsDoubleList2 =
				g.addVerticesWeights("vDoubleList2", double[].class, new double[] { 31.31, 32.32 });
		IWeightsObj<DoubleList> weightsDoubleList3 = g.addVerticesWeights("vDoubleList3", DoubleList.class);
		IWeightsObj<DoubleList> weightsDoubleList4 =
				g.addVerticesWeights("vDoubleList4", DoubleList.class, DoubleList.of(33.33, 34.34));
		IWeightsObj<List<Double>> weightsDoubleList5 = g.addVerticesWeights("vDoubleList5", List.class);
		IWeightsObj<List<Double>> weightsDoubleList6 =
				g.addVerticesWeights("vDoubleList6", List.class, List.of(Double.valueOf(35.35), Double.valueOf(36.36)));
		IWeightsObj<boolean[]> weightsBooleanList1 = g.addVerticesWeights("vBooleanList1", boolean[].class);
		IWeightsObj<boolean[]> weightsBooleanList2 =
				g.addVerticesWeights("vBooleanList2", boolean[].class, new boolean[] { true, false });
		IWeightsObj<BooleanList> weightsBooleanList3 = g.addVerticesWeights("vBooleanList3", BooleanList.class);
		IWeightsObj<BooleanList> weightsBooleanList4 =
				g.addVerticesWeights("vBooleanList4", BooleanList.class, BooleanList.of(true, false));
		IWeightsObj<List<Boolean>> weightsBooleanList5 = g.addVerticesWeights("vBooleanList5", List.class);
		IWeightsObj<List<Boolean>> weightsBooleanList6 = g.addVerticesWeights("vBooleanList6", List.class,
				List.of(Boolean.valueOf(true), Boolean.valueOf(false)));
		IWeightsObj<char[]> weightsCharList1 = g.addVerticesWeights("vCharList1", char[].class);
		IWeightsObj<char[]> weightsCharList2 =
				g.addVerticesWeights("vCharList2", char[].class, new char[] { 'a', 'b' });
		IWeightsObj<CharList> weightsCharList3 = g.addVerticesWeights("vCharList3", CharList.class);
		IWeightsObj<CharList> weightsCharList4 =
				g.addVerticesWeights("vCharList4", CharList.class, CharList.of('c', 'd'));
		IWeightsObj<List<Character>> weightsCharList5 = g.addVerticesWeights("vCharList5", List.class);
		IWeightsObj<List<Character>> weightsCharList6 =
				g.addVerticesWeights("vCharList6", List.class, List.of(Character.valueOf('e'), Character.valueOf('f')));
		IWeightsObj<String[]> weightsStringList1 = g.addVerticesWeights("vStringList1", String[].class);
		IWeightsObj<String[]> weightsStringList2 =
				g.addVerticesWeights("vStringList2", String[].class, new String[] { "abc", "def" });
		IWeightsObj<List<String>> weightsStringList3 = g.addVerticesWeights("vStringList3", List.class);
		IWeightsObj<List<String>> weightsStringList4 =
				g.addVerticesWeights("vStringList4", List.class, List.of("ghi", "jkl"));
		IWeightsObj<BigDecimal[]> weightsBigDecimalList1 = g.addVerticesWeights("vBigDecimalList1", BigDecimal[].class);
		IWeightsObj<BigDecimal[]> weightsBigDecimalList2 = g.addVerticesWeights("vBigDecimalList2", BigDecimal[].class,
				new BigDecimal[] { new BigDecimal("123.123"), new BigDecimal("456.456") });
		IWeightsObj<List<BigDecimal>> weightsBigDecimalList3 = g.addVerticesWeights("vBigDecimalList3", List.class);
		IWeightsObj<List<BigDecimal>> weightsBigDecimalList4 = g.addVerticesWeights("vBigDecimalList4", List.class,
				List.of(new BigDecimal("789.789"), new BigDecimal("987.987")));
		IWeightsObj<BigInteger[]> weightsBigIntegerList1 = g.addVerticesWeights("vBigIntegerList1", BigInteger[].class);
		IWeightsObj<BigInteger[]> weightsBigIntegerList2 = g.addVerticesWeights("vBigIntegerList2", BigInteger[].class,
				new BigInteger[] { new BigInteger("12345678901234567890"), new BigInteger("98765432109876543210") });
		IWeightsObj<List<BigInteger>> weightsBigIntegerList3 = g.addVerticesWeights("vBigIntegerList3", List.class);
		IWeightsObj<List<BigInteger>> weightsBigIntegerList4 = g.addVerticesWeights("vBigIntegerList4", List.class,
				List.of(new BigInteger("12345678901234567890"), new BigInteger("98765432109876543210")));

		weightsByte3.set(0, Byte.valueOf((byte) 51));
		weightsShort3.set(0, Short.valueOf((short) 52));
		weightsInt3.set(0, Integer.valueOf(53));
		weightsLong3.set(0, Long.valueOf(54L));
		weightsFloat3.set(0, Float.valueOf(55.55f));
		weightsDouble3.set(0, Double.valueOf(56.56));
		weightsBoolean3.set(0, Boolean.valueOf(true));
		weightsChar3.set(0, Character.valueOf('g'));
		weightsString1.set(0, "mno");
		weightsDate1.set(0, date.apply("2005-12-31"));
		weightsUri1.set(0, URI.create("http://example.org"));
		weightsBigDecimal1.set(0, new BigDecimal("1234567890.1234567890"));
		weightsBigInteger1.set(0, new BigInteger("12345678901234567890"));
		weightsByteList1.set(0, new byte[] { 57, 58 });
		weightsByteList3.set(0, ByteList.of((byte) 59, (byte) 60));
		weightsByteList5.set(0, List.of(Byte.valueOf((byte) 61), Byte.valueOf((byte) 62)));
		weightsShortList1.set(0, new short[] { 63, 64 });
		weightsShortList3.set(0, ShortList.of((short) 65, (short) 66));
		weightsShortList5.set(0, List.of(Short.valueOf((short) 67), Short.valueOf((short) 68)));
		weightsIntList1.set(0, new int[] { 69, 70 });
		weightsIntList3.set(0, IntList.of(71, 72));
		weightsIntList5.set(0, List.of(Integer.valueOf(73), Integer.valueOf(74)));
		weightsLongList1.set(0, new long[] { 75, 76 });
		weightsLongList3.set(0, LongList.of(77L, 78L));
		weightsLongList5.set(0, List.of(Long.valueOf(79L), Long.valueOf(80L)));
		weightsFloatList1.set(0, new float[] { 81.81f, 82.82f });
		weightsFloatList3.set(0, FloatList.of(83.83f, 84.84f));
		weightsFloatList5.set(0, List.of(Float.valueOf(85.85f), Float.valueOf(86.86f)));
		weightsDoubleList1.set(0, new double[] { 87.87, 88.88 });
		weightsDoubleList3.set(0, DoubleList.of(89.89, 90.90));
		weightsDoubleList5.set(0, List.of(Double.valueOf(91.91), Double.valueOf(92.92)));
		weightsBooleanList1.set(0, new boolean[] { true, false });
		weightsBooleanList3.set(0, BooleanList.of(true, false));
		weightsBooleanList5.set(0, List.of(Boolean.valueOf(true), Boolean.valueOf(false)));
		weightsCharList1.set(0, new char[] { 'h', 'i' });
		weightsCharList3.set(0, CharList.of('j', 'k'));
		weightsCharList5.set(0, List.of(Character.valueOf('l'), Character.valueOf('m')));
		weightsStringList1.set(0, new String[] { "pqr", "stu" });
		weightsStringList3.set(0, List.of("vwx", "yz"));
		weightsBigDecimalList1.set(0, new BigDecimal[] { new BigDecimal("123.123"), new BigDecimal("456.456") });
		weightsBigDecimalList3.set(0, List.of(new BigDecimal("789.789"), new BigDecimal("987.987")));
		weightsBigIntegerList1.set(0,
				new BigInteger[] { new BigInteger("12345678901234567890"), new BigInteger("98765432109876543210") });
		weightsBigIntegerList3.set(0,
				List.of(new BigInteger("12345678901234567890"), new BigInteger("98765432109876543210")));

		GexfGraphWriter<Integer, Integer> writer = new GexfGraphWriter<>();
		StringWriter sw = new StringWriter();
		writer.writeGraph(g, sw);
		String text = sw.toString();
		GexfGraphReader<Integer, Integer> reader = new GexfGraphReader<>(int.class, int.class);
		IntGraph g2 = (IntGraph) reader.readGraph(new StringReader(text));

		assertEquals(g.vertices(), g2.vertices());
		assertEquals(g.edges(), g2.edges());
		assertEquals(g.getVerticesWeightsKeys(), g2.getVerticesWeightsKeys());
		assertEquals(g.getEdgesWeightsKeys(), g2.getEdgesWeightsKeys());

		IWeightsByte weightsByte1_2 = g2.getVerticesWeights("vByte1");
		IWeightsByte weightsByte2_2 = g2.getVerticesWeights("vByte2");
		IWeightsByte weightsByte3_2 = g2.getVerticesWeights("vByte3");
		IWeightsByte weightsByte4_2 = g2.getVerticesWeights("vByte4");
		IWeightsShort weightsShort1_2 = g2.getVerticesWeights("vShort1");
		IWeightsShort weightsShort2_2 = g2.getVerticesWeights("vShort2");
		IWeightsShort weightsShort3_2 = g2.getVerticesWeights("vShort3");
		IWeightsShort weightsShort4_2 = g2.getVerticesWeights("vShort4");
		IWeightsInt weightsInt1_2 = g2.getVerticesWeights("vInt1");
		IWeightsInt weightsInt2_2 = g2.getVerticesWeights("vInt2");
		IWeightsInt weightsInt3_2 = g2.getVerticesWeights("vInt3");
		IWeightsInt weightsInt4_2 = g2.getVerticesWeights("vInt4");
		IWeightsLong weightsLong1_2 = g2.getVerticesWeights("vLong1");
		IWeightsLong weightsLong2_2 = g2.getVerticesWeights("vLong2");
		IWeightsLong weightsLong3_2 = g2.getVerticesWeights("vLong3");
		IWeightsLong weightsLong4_2 = g2.getVerticesWeights("vLong4");
		IWeightsFloat weightsFloat1_2 = g2.getVerticesWeights("vFloat1");
		IWeightsFloat weightsFloat2_2 = g2.getVerticesWeights("vFloat2");
		IWeightsFloat weightsFloat3_2 = g2.getVerticesWeights("vFloat3");
		IWeightsFloat weightsFloat4_2 = g2.getVerticesWeights("vFloat4");
		IWeightsDouble weightsDouble1_2 = g2.getVerticesWeights("vDouble1");
		IWeightsDouble weightsDouble2_2 = g2.getVerticesWeights("vDouble2");
		IWeightsDouble weightsDouble3_2 = g2.getVerticesWeights("vDouble3");
		IWeightsDouble weightsDouble4_2 = g2.getVerticesWeights("vDouble4");
		IWeightsBool weightsBoolean1_2 = g2.getVerticesWeights("vBoolean1");
		IWeightsBool weightsBoolean2_2 = g2.getVerticesWeights("vBoolean2");
		IWeightsBool weightsBoolean3_2 = g2.getVerticesWeights("vBoolean3");
		IWeightsBool weightsBoolean4_2 = g2.getVerticesWeights("vBoolean4");
		// IWeightsChar weightsChar1_2 = g2.getVerticesWeights("vChar1");
		IWeightsChar weightsChar2_2 = g2.getVerticesWeights("vChar2");
		IWeightsChar weightsChar3_2 = g2.getVerticesWeights("vChar3");
		IWeightsChar weightsChar4_2 = g2.getVerticesWeights("vChar4");
		IWeightsObj<String> weightsString1_2 = g2.getVerticesWeights("vString1");
		IWeightsObj<String> weightsString2_2 = g2.getVerticesWeights("vString2");
		IWeightsObj<Date> weightsDate1_2 = g2.getVerticesWeights("vDate1");
		IWeightsObj<Date> weightsDate2_2 = g2.getVerticesWeights("vDate2");
		IWeightsObj<URI> weightsUri1_2 = g2.getVerticesWeights("vUri1");
		IWeightsObj<URI> weightsUri2_2 = g2.getVerticesWeights("vUri2");
		IWeightsObj<BigDecimal> weightsBigDecimal1_2 = g2.getVerticesWeights("vBigDecimal1");
		IWeightsObj<BigDecimal> weightsBigDecimal2_2 = g2.getVerticesWeights("vBigDecimal2");
		IWeightsObj<BigInteger> weightsBigInteger1_2 = g2.getVerticesWeights("vBigInteger1");
		IWeightsObj<BigInteger> weightsBigInteger2_2 = g2.getVerticesWeights("vBigInteger2");
		IWeightsObj<byte[]> weightsByteList1_2 = g2.getVerticesWeights("vByteList1");
		IWeightsObj<byte[]> weightsByteList2_2 = g2.getVerticesWeights("vByteList2");
		IWeightsObj<byte[]> weightsByteList3_2 = g2.getVerticesWeights("vByteList3");
		IWeightsObj<byte[]> weightsByteList4_2 = g2.getVerticesWeights("vByteList4");
		IWeightsObj<byte[]> weightsByteList5_2 = g2.getVerticesWeights("vByteList5");
		IWeightsObj<byte[]> weightsByteList6_2 = g2.getVerticesWeights("vByteList6");
		IWeightsObj<short[]> weightsShortList1_2 = g2.getVerticesWeights("vShortList1");
		IWeightsObj<short[]> weightsShortList2_2 = g2.getVerticesWeights("vShortList2");
		IWeightsObj<short[]> weightsShortList3_2 = g2.getVerticesWeights("vShortList3");
		IWeightsObj<short[]> weightsShortList4_2 = g2.getVerticesWeights("vShortList4");
		IWeightsObj<short[]> weightsShortList5_2 = g2.getVerticesWeights("vShortList5");
		IWeightsObj<short[]> weightsShortList6_2 = g2.getVerticesWeights("vShortList6");
		IWeightsObj<int[]> weightsIntList1_2 = g2.getVerticesWeights("vIntList1");
		IWeightsObj<int[]> weightsIntList2_2 = g2.getVerticesWeights("vIntList2");
		IWeightsObj<int[]> weightsIntList3_2 = g2.getVerticesWeights("vIntList3");
		IWeightsObj<int[]> weightsIntList4_2 = g2.getVerticesWeights("vIntList4");
		IWeightsObj<int[]> weightsIntList5_2 = g2.getVerticesWeights("vIntList5");
		IWeightsObj<int[]> weightsIntList6_2 = g2.getVerticesWeights("vIntList6");
		IWeightsObj<long[]> weightsLongList1_2 = g2.getVerticesWeights("vLongList1");
		IWeightsObj<long[]> weightsLongList2_2 = g2.getVerticesWeights("vLongList2");
		IWeightsObj<long[]> weightsLongList3_2 = g2.getVerticesWeights("vLongList3");
		IWeightsObj<long[]> weightsLongList4_2 = g2.getVerticesWeights("vLongList4");
		IWeightsObj<long[]> weightsLongList5_2 = g2.getVerticesWeights("vLongList5");
		IWeightsObj<long[]> weightsLongList6_2 = g2.getVerticesWeights("vLongList6");
		IWeightsObj<float[]> weightsFloatList1_2 = g2.getVerticesWeights("vFloatList1");
		IWeightsObj<float[]> weightsFloatList2_2 = g2.getVerticesWeights("vFloatList2");
		IWeightsObj<float[]> weightsFloatList3_2 = g2.getVerticesWeights("vFloatList3");
		IWeightsObj<float[]> weightsFloatList4_2 = g2.getVerticesWeights("vFloatList4");
		IWeightsObj<float[]> weightsFloatList5_2 = g2.getVerticesWeights("vFloatList5");
		IWeightsObj<float[]> weightsFloatList6_2 = g2.getVerticesWeights("vFloatList6");
		IWeightsObj<double[]> weightsDoubleList1_2 = g2.getVerticesWeights("vDoubleList1");
		IWeightsObj<double[]> weightsDoubleList2_2 = g2.getVerticesWeights("vDoubleList2");
		IWeightsObj<double[]> weightsDoubleList3_2 = g2.getVerticesWeights("vDoubleList3");
		IWeightsObj<double[]> weightsDoubleList4_2 = g2.getVerticesWeights("vDoubleList4");
		IWeightsObj<double[]> weightsDoubleList5_2 = g2.getVerticesWeights("vDoubleList5");
		IWeightsObj<double[]> weightsDoubleList6_2 = g2.getVerticesWeights("vDoubleList6");
		IWeightsObj<boolean[]> weightsBooleanList1_2 = g2.getVerticesWeights("vBooleanList1");
		IWeightsObj<boolean[]> weightsBooleanList2_2 = g2.getVerticesWeights("vBooleanList2");
		IWeightsObj<boolean[]> weightsBooleanList3_2 = g2.getVerticesWeights("vBooleanList3");
		IWeightsObj<boolean[]> weightsBooleanList4_2 = g2.getVerticesWeights("vBooleanList4");
		IWeightsObj<boolean[]> weightsBooleanList5_2 = g2.getVerticesWeights("vBooleanList5");
		IWeightsObj<boolean[]> weightsBooleanList6_2 = g2.getVerticesWeights("vBooleanList6");
		IWeightsObj<char[]> weightsCharList1_2 = g2.getVerticesWeights("vCharList1");
		IWeightsObj<char[]> weightsCharList2_2 = g2.getVerticesWeights("vCharList2");
		IWeightsObj<char[]> weightsCharList3_2 = g2.getVerticesWeights("vCharList3");
		IWeightsObj<char[]> weightsCharList4_2 = g2.getVerticesWeights("vCharList4");
		IWeightsObj<char[]> weightsCharList5_2 = g2.getVerticesWeights("vCharList5");
		IWeightsObj<char[]> weightsCharList6_2 = g2.getVerticesWeights("vCharList6");
		IWeightsObj<String[]> weightsStringList1_2 = g2.getVerticesWeights("vStringList1");
		IWeightsObj<String[]> weightsStringList2_2 = g2.getVerticesWeights("vStringList2");
		IWeightsObj<String[]> weightsStringList3_2 = g2.getVerticesWeights("vStringList3");
		IWeightsObj<String[]> weightsStringList4_2 = g2.getVerticesWeights("vStringList4");
		IWeightsObj<BigDecimal[]> weightsBigDecimalList1_2 = g2.getVerticesWeights("vBigDecimalList1");
		IWeightsObj<BigDecimal[]> weightsBigDecimalList2_2 = g2.getVerticesWeights("vBigDecimalList2");
		IWeightsObj<BigDecimal[]> weightsBigDecimalList3_2 = g2.getVerticesWeights("vBigDecimalList3");
		IWeightsObj<BigDecimal[]> weightsBigDecimalList4_2 = g2.getVerticesWeights("vBigDecimalList4");
		IWeightsObj<BigInteger[]> weightsBigIntegerList1_2 = g2.getVerticesWeights("vBigIntegerList1");
		IWeightsObj<BigInteger[]> weightsBigIntegerList2_2 = g2.getVerticesWeights("vBigIntegerList2");
		IWeightsObj<BigInteger[]> weightsBigIntegerList3_2 = g2.getVerticesWeights("vBigIntegerList3");
		IWeightsObj<BigInteger[]> weightsBigIntegerList4_2 = g2.getVerticesWeights("vBigIntegerList4");

		final int v = 0;
		assertEquals(weightsByte1.get(v), weightsByte1_2.get(v));
		assertEquals(weightsByte2.get(v), weightsByte2_2.get(v));
		assertEquals(weightsByte3.get(v), weightsByte3_2.get(v));
		assertEquals(weightsByte4.get(v), weightsByte4_2.get(v));
		assertEquals(weightsShort1.get(v), weightsShort1_2.get(v));
		assertEquals(weightsShort2.get(v), weightsShort2_2.get(v));
		assertEquals(weightsShort3.get(v), weightsShort3_2.get(v));
		assertEquals(weightsShort4.get(v), weightsShort4_2.get(v));
		assertEquals(weightsInt1.get(v), weightsInt1_2.get(v));
		assertEquals(weightsInt2.get(v), weightsInt2_2.get(v));
		assertEquals(weightsInt3.get(v), weightsInt3_2.get(v));
		assertEquals(weightsInt4.get(v), weightsInt4_2.get(v));
		assertEquals(weightsLong1.get(v), weightsLong1_2.get(v));
		assertEquals(weightsLong2.get(v), weightsLong2_2.get(v));
		assertEquals(weightsLong3.get(v), weightsLong3_2.get(v));
		assertEquals(weightsLong4.get(v), weightsLong4_2.get(v));
		assertEquals(weightsFloat1.get(v), weightsFloat1_2.get(v));
		assertEquals(weightsFloat2.get(v), weightsFloat2_2.get(v));
		assertEquals(weightsFloat3.get(v), weightsFloat3_2.get(v));
		assertEquals(weightsFloat4.get(v), weightsFloat4_2.get(v));
		assertEquals(weightsDouble1.get(v), weightsDouble1_2.get(v));
		assertEquals(weightsDouble2.get(v), weightsDouble2_2.get(v));
		assertEquals(weightsDouble3.get(v), weightsDouble3_2.get(v));
		assertEquals(weightsDouble4.get(v), weightsDouble4_2.get(v));
		assertEquals(Boolean.valueOf(weightsBoolean1.get(v)), Boolean.valueOf(weightsBoolean1_2.get(v)));
		assertEquals(Boolean.valueOf(weightsBoolean2.get(v)), Boolean.valueOf(weightsBoolean2_2.get(v)));
		assertEquals(weightsBoolean3.get(v), Boolean.valueOf(weightsBoolean3_2.get(v)));
		assertEquals(weightsBoolean4.get(v), Boolean.valueOf(weightsBoolean4_2.get(v)));
		// assertEquals(weightsChar1.get(v), weightsChar1_2.get(v));
		assertEquals(weightsChar2.get(v), weightsChar2_2.get(v));
		assertEquals(weightsChar3.get(v), weightsChar3_2.get(v));
		assertEquals(weightsChar4.get(v), weightsChar4_2.get(v));
		assertEquals(weightsString1.get(v), weightsString1_2.get(v));
		assertEquals(weightsString2.get(v), weightsString2_2.get(v));
		assertEquals(weightsDate1.get(v), weightsDate1_2.get(v));
		assertEquals(weightsDate2.get(v), weightsDate2_2.get(v));
		assertEquals(weightsUri1.get(v), weightsUri1_2.get(v));
		assertEquals(weightsUri2.get(v), weightsUri2_2.get(v));
		assertEquals(weightsBigDecimal1.get(v), weightsBigDecimal1_2.get(v));
		assertEquals(weightsBigDecimal2.get(v), weightsBigDecimal2_2.get(v));
		assertEquals(weightsBigInteger1.get(v), weightsBigInteger1_2.get(v));
		assertEquals(weightsBigInteger2.get(v), weightsBigInteger2_2.get(v));
		assertArrayEquals(weightsByteList1.get(v), weightsByteList1_2.get(v));
		assertArrayEquals(weightsByteList2.get(v), weightsByteList2_2.get(v));
		assertArrayEquals0(weightsByteList3.get(v), weightsByteList3_2.get(v));
		assertArrayEquals0(weightsByteList4.get(v), weightsByteList4_2.get(v));
		assertArrayEquals0(weightsByteList5.get(v), weightsByteList5_2.get(v));
		assertArrayEquals0(weightsByteList6.get(v), weightsByteList6_2.get(v));
		assertArrayEquals(weightsShortList1.get(v), weightsShortList1_2.get(v));
		assertArrayEquals(weightsShortList2.get(v), weightsShortList2_2.get(v));
		assertArrayEquals0(weightsShortList3.get(v), weightsShortList3_2.get(v));
		assertArrayEquals0(weightsShortList4.get(v), weightsShortList4_2.get(v));
		assertArrayEquals0(weightsShortList5.get(v), weightsShortList5_2.get(v));
		assertArrayEquals0(weightsShortList6.get(v), weightsShortList6_2.get(v));
		assertArrayEquals(weightsIntList1.get(v), weightsIntList1_2.get(v));
		assertArrayEquals(weightsIntList2.get(v), weightsIntList2_2.get(v));
		assertArrayEquals0(weightsIntList3.get(v), weightsIntList3_2.get(v));
		assertArrayEquals0(weightsIntList4.get(v), weightsIntList4_2.get(v));
		assertArrayEquals0(weightsIntList5.get(v), weightsIntList5_2.get(v));
		assertArrayEquals0(weightsIntList6.get(v), weightsIntList6_2.get(v));
		assertArrayEquals(weightsLongList1.get(v), weightsLongList1_2.get(v));
		assertArrayEquals(weightsLongList2.get(v), weightsLongList2_2.get(v));
		assertArrayEquals0(weightsLongList3.get(v), weightsLongList3_2.get(v));
		assertArrayEquals0(weightsLongList4.get(v), weightsLongList4_2.get(v));
		assertArrayEquals0(weightsLongList5.get(v), weightsLongList5_2.get(v));
		assertArrayEquals0(weightsLongList6.get(v), weightsLongList6_2.get(v));
		assertArrayEquals(weightsFloatList1.get(v), weightsFloatList1_2.get(v), 0.0f);
		assertArrayEquals(weightsFloatList2.get(v), weightsFloatList2_2.get(v), 0.0f);
		assertArrayEquals0(weightsFloatList3.get(v), weightsFloatList3_2.get(v), 0.0f);
		assertArrayEquals0(weightsFloatList4.get(v), weightsFloatList4_2.get(v), 0.0f);
		assertArrayEquals0(weightsFloatList5.get(v), weightsFloatList5_2.get(v), 0.0f);
		assertArrayEquals0(weightsFloatList6.get(v), weightsFloatList6_2.get(v), 0.0f);
		assertArrayEquals(weightsDoubleList1.get(v), weightsDoubleList1_2.get(v), 0.0);
		assertArrayEquals(weightsDoubleList2.get(v), weightsDoubleList2_2.get(v), 0.0);
		assertArrayEquals0(weightsDoubleList3.get(v), weightsDoubleList3_2.get(v), 0.0);
		assertArrayEquals0(weightsDoubleList4.get(v), weightsDoubleList4_2.get(v), 0.0);
		assertArrayEquals0(weightsDoubleList5.get(v), weightsDoubleList5_2.get(v), 0.0);
		assertArrayEquals0(weightsDoubleList6.get(v), weightsDoubleList6_2.get(v), 0.0);
		assertArrayEquals(weightsBooleanList1.get(v), weightsBooleanList1_2.get(v));
		assertArrayEquals(weightsBooleanList2.get(v), weightsBooleanList2_2.get(v));
		assertArrayEquals0(weightsBooleanList3.get(v), weightsBooleanList3_2.get(v));
		assertArrayEquals0(weightsBooleanList4.get(v), weightsBooleanList4_2.get(v));
		assertArrayEquals0(weightsBooleanList5.get(v), weightsBooleanList5_2.get(v));
		assertArrayEquals0(weightsBooleanList6.get(v), weightsBooleanList6_2.get(v));
		assertArrayEquals(weightsCharList1.get(v), weightsCharList1_2.get(v));
		assertArrayEquals(weightsCharList2.get(v), weightsCharList2_2.get(v));
		assertArrayEquals0(weightsCharList3.get(v), weightsCharList3_2.get(v));
		assertArrayEquals0(weightsCharList4.get(v), weightsCharList4_2.get(v));
		assertArrayEquals0(weightsCharList5.get(v), weightsCharList5_2.get(v));
		assertArrayEquals0(weightsCharList6.get(v), weightsCharList6_2.get(v));
		assertArrayEquals(weightsStringList1.get(v), weightsStringList1_2.get(v));
		assertArrayEquals(weightsStringList2.get(v), weightsStringList2_2.get(v));
		assertArrayEquals0(weightsStringList3.get(v), weightsStringList3_2.get(v));
		assertArrayEquals0(weightsStringList4.get(v), weightsStringList4_2.get(v));
		assertArrayEquals(weightsBigDecimalList1.get(v), weightsBigDecimalList1_2.get(v));
		assertArrayEquals(weightsBigDecimalList2.get(v), weightsBigDecimalList2_2.get(v));
		assertArrayEquals0(weightsBigDecimalList3.get(v), weightsBigDecimalList3_2.get(v));
		assertArrayEquals0(weightsBigDecimalList4.get(v), weightsBigDecimalList4_2.get(v));
		assertArrayEquals(weightsBigIntegerList1.get(v), weightsBigIntegerList1_2.get(v));
		assertArrayEquals(weightsBigIntegerList2.get(v), weightsBigIntegerList2_2.get(v));
		assertArrayEquals0(weightsBigIntegerList3.get(v), weightsBigIntegerList3_2.get(v));
		assertArrayEquals0(weightsBigIntegerList4.get(v), weightsBigIntegerList4_2.get(v));

		assertEquals(weightsByte1.defaultWeight(), weightsByte1_2.defaultWeight());
		assertEquals(weightsByte2.defaultWeight(), weightsByte2_2.defaultWeight());
		assertEquals(replaceNull(weightsByte3.defaultWeight(), (byte) 0), weightsByte3_2.defaultWeight());
		assertEquals(replaceNull(weightsByte4.defaultWeight(), (byte) 0), weightsByte4_2.defaultWeight());
		assertEquals(weightsShort1.defaultWeight(), weightsShort1_2.defaultWeight());
		assertEquals(weightsShort2.defaultWeight(), weightsShort2_2.defaultWeight());
		assertEquals(replaceNull(weightsShort3.defaultWeight(), (short) 0), weightsShort3_2.defaultWeight());
		assertEquals(replaceNull(weightsShort4.defaultWeight(), (short) 0), weightsShort4_2.defaultWeight());
		assertEquals(weightsInt1.defaultWeight(), weightsInt1_2.defaultWeight());
		assertEquals(weightsInt2.defaultWeight(), weightsInt2_2.defaultWeight());
		assertEquals(replaceNull(weightsInt3.defaultWeight(), 0), weightsInt3_2.defaultWeight());
		assertEquals(replaceNull(weightsInt4.defaultWeight(), 0), weightsInt4_2.defaultWeight());
		assertEquals(weightsLong1.defaultWeight(), weightsLong1_2.defaultWeight());
		assertEquals(weightsLong2.defaultWeight(), weightsLong2_2.defaultWeight());
		assertEquals(replaceNull(weightsLong3.defaultWeight(), 0L), weightsLong3_2.defaultWeight());
		assertEquals(replaceNull(weightsLong4.defaultWeight(), 0L), weightsLong4_2.defaultWeight());
		assertEquals(weightsFloat1.defaultWeight(), weightsFloat1_2.defaultWeight());
		assertEquals(weightsFloat2.defaultWeight(), weightsFloat2_2.defaultWeight());
		assertEquals(replaceNull(weightsFloat3.defaultWeight(), 0.0f), weightsFloat3_2.defaultWeight());
		assertEquals(replaceNull(weightsFloat4.defaultWeight(), 0.0f), weightsFloat4_2.defaultWeight());
		assertEquals(weightsDouble1.defaultWeight(), weightsDouble1_2.defaultWeight());
		assertEquals(weightsDouble2.defaultWeight(), weightsDouble2_2.defaultWeight());
		assertEquals(replaceNull(weightsDouble3.defaultWeight(), 0.0), weightsDouble3_2.defaultWeight());
		assertEquals(replaceNull(weightsDouble4.defaultWeight(), 0.0), weightsDouble4_2.defaultWeight());
		assertEquals(Boolean.valueOf(weightsBoolean1.defaultWeight()),
				Boolean.valueOf(weightsBoolean1_2.defaultWeight()));
		assertEquals(Boolean.valueOf(weightsBoolean2.defaultWeight()),
				Boolean.valueOf(weightsBoolean2_2.defaultWeight()));
		assertEquals(Boolean.valueOf(replaceNull(weightsBoolean3.defaultWeight(), false)),
				Boolean.valueOf(weightsBoolean3_2.defaultWeight()));
		assertEquals(Boolean.valueOf(replaceNull(weightsBoolean4.defaultWeight(), false)),
				Boolean.valueOf(weightsBoolean4_2.defaultWeight()));
		// assertEquals(weightsChar1.defaultWeight(), weightsChar1_2.defaultWeight());
		assertEquals(weightsChar2.defaultWeight(), weightsChar2_2.defaultWeight());
		assertEquals(replaceNull(weightsChar3.defaultWeight(), '\0'), weightsChar3_2.defaultWeight());
		assertEquals(replaceNull(weightsChar4.defaultWeight(), '\0'), weightsChar4_2.defaultWeight());
		assertEquals(weightsString1.defaultWeight(), weightsString1_2.defaultWeight());
		assertEquals(weightsString2.defaultWeight(), weightsString2_2.defaultWeight());
		assertEquals(weightsDate1.defaultWeight(), weightsDate1_2.defaultWeight());
		assertEquals(weightsDate2.defaultWeight(), weightsDate2_2.defaultWeight());
		assertEquals(weightsUri1.defaultWeight(), weightsUri1_2.defaultWeight());
		assertEquals(weightsUri2.defaultWeight(), weightsUri2_2.defaultWeight());
		assertEquals(weightsBigDecimal1.defaultWeight(), weightsBigDecimal1_2.defaultWeight());
		assertEquals(weightsBigDecimal2.defaultWeight(), weightsBigDecimal2_2.defaultWeight());
		assertEquals(weightsBigInteger1.defaultWeight(), weightsBigInteger1_2.defaultWeight());
		assertEquals(weightsBigInteger2.defaultWeight(), weightsBigInteger2_2.defaultWeight());
		assertArrayEquals(weightsByteList1.defaultWeight(), weightsByteList1_2.defaultWeight());
		assertArrayEquals(weightsByteList2.defaultWeight(), weightsByteList2_2.defaultWeight());
		assertArrayEquals0(weightsByteList3.defaultWeight(), weightsByteList3_2.defaultWeight());
		assertArrayEquals0(weightsByteList4.defaultWeight(), weightsByteList4_2.defaultWeight());
		assertArrayEquals0(weightsByteList5.defaultWeight(), weightsByteList5_2.defaultWeight());
		assertArrayEquals0(weightsByteList6.defaultWeight(), weightsByteList6_2.defaultWeight());
		assertArrayEquals(weightsShortList1.defaultWeight(), weightsShortList1_2.defaultWeight());
		assertArrayEquals(weightsShortList2.defaultWeight(), weightsShortList2_2.defaultWeight());
		assertArrayEquals0(weightsShortList3.defaultWeight(), weightsShortList3_2.defaultWeight());
		assertArrayEquals0(weightsShortList4.defaultWeight(), weightsShortList4_2.defaultWeight());
		assertArrayEquals0(weightsShortList5.defaultWeight(), weightsShortList5_2.defaultWeight());
		assertArrayEquals0(weightsShortList6.defaultWeight(), weightsShortList6_2.defaultWeight());
		assertArrayEquals(weightsIntList1.defaultWeight(), weightsIntList1_2.defaultWeight());
		assertArrayEquals(weightsIntList2.defaultWeight(), weightsIntList2_2.defaultWeight());
		assertArrayEquals0(weightsIntList3.defaultWeight(), weightsIntList3_2.defaultWeight());
		assertArrayEquals0(weightsIntList4.defaultWeight(), weightsIntList4_2.defaultWeight());
		assertArrayEquals0(weightsIntList5.defaultWeight(), weightsIntList5_2.defaultWeight());
		assertArrayEquals0(weightsIntList6.defaultWeight(), weightsIntList6_2.defaultWeight());
		assertArrayEquals(weightsLongList1.defaultWeight(), weightsLongList1_2.defaultWeight());
		assertArrayEquals(weightsLongList2.defaultWeight(), weightsLongList2_2.defaultWeight());
		assertArrayEquals0(weightsLongList3.defaultWeight(), weightsLongList3_2.defaultWeight());
		assertArrayEquals0(weightsLongList4.defaultWeight(), weightsLongList4_2.defaultWeight());
		assertArrayEquals0(weightsLongList5.defaultWeight(), weightsLongList5_2.defaultWeight());
		assertArrayEquals0(weightsLongList6.defaultWeight(), weightsLongList6_2.defaultWeight());
		assertArrayEquals(weightsFloatList1.defaultWeight(), weightsFloatList1_2.defaultWeight(), 0.0f);
		assertArrayEquals(weightsFloatList2.defaultWeight(), weightsFloatList2_2.defaultWeight(), 0.0f);
		assertArrayEquals0(weightsFloatList3.defaultWeight(), weightsFloatList3_2.defaultWeight(), 0.0f);
		assertArrayEquals0(weightsFloatList4.defaultWeight(), weightsFloatList4_2.defaultWeight(), 0.0f);
		assertArrayEquals0(weightsFloatList5.defaultWeight(), weightsFloatList5_2.defaultWeight(), 0.0f);
		assertArrayEquals0(weightsFloatList6.defaultWeight(), weightsFloatList6_2.defaultWeight(), 0.0f);
		assertArrayEquals(weightsDoubleList1.defaultWeight(), weightsDoubleList1_2.defaultWeight(), 0.0);
		assertArrayEquals(weightsDoubleList2.defaultWeight(), weightsDoubleList2_2.defaultWeight(), 0.0);
		assertArrayEquals0(weightsDoubleList3.defaultWeight(), weightsDoubleList3_2.defaultWeight(), 0.0);
		assertArrayEquals0(weightsDoubleList4.defaultWeight(), weightsDoubleList4_2.defaultWeight(), 0.0);
		assertArrayEquals0(weightsDoubleList5.defaultWeight(), weightsDoubleList5_2.defaultWeight(), 0.0);
		assertArrayEquals0(weightsDoubleList6.defaultWeight(), weightsDoubleList6_2.defaultWeight(), 0.0);
		assertArrayEquals(weightsBooleanList1.defaultWeight(), weightsBooleanList1_2.defaultWeight());
		assertArrayEquals(weightsBooleanList2.defaultWeight(), weightsBooleanList2_2.defaultWeight());
		assertArrayEquals0(weightsBooleanList3.defaultWeight(), weightsBooleanList3_2.defaultWeight());
		assertArrayEquals0(weightsBooleanList4.defaultWeight(), weightsBooleanList4_2.defaultWeight());
		assertArrayEquals0(weightsBooleanList5.defaultWeight(), weightsBooleanList5_2.defaultWeight());
		assertArrayEquals0(weightsBooleanList6.defaultWeight(), weightsBooleanList6_2.defaultWeight());
		assertArrayEquals(weightsCharList1.defaultWeight(), weightsCharList1_2.defaultWeight());
		assertArrayEquals(weightsCharList2.defaultWeight(), weightsCharList2_2.defaultWeight());
		assertArrayEquals0(weightsCharList3.defaultWeight(), weightsCharList3_2.defaultWeight());
		assertArrayEquals0(weightsCharList4.defaultWeight(), weightsCharList4_2.defaultWeight());
		assertArrayEquals0(weightsCharList5.defaultWeight(), weightsCharList5_2.defaultWeight());
		assertArrayEquals0(weightsCharList6.defaultWeight(), weightsCharList6_2.defaultWeight());
		assertArrayEquals(weightsStringList1.defaultWeight(), weightsStringList1_2.defaultWeight());
		assertArrayEquals(weightsStringList2.defaultWeight(), weightsStringList2_2.defaultWeight());
		assertArrayEquals0(weightsStringList3.defaultWeight(), weightsStringList3_2.defaultWeight());
		assertArrayEquals0(weightsStringList4.defaultWeight(), weightsStringList4_2.defaultWeight());
		assertArrayEquals(weightsBigDecimalList1.defaultWeight(), weightsBigDecimalList1_2.defaultWeight());
		assertArrayEquals(weightsBigDecimalList2.defaultWeight(), weightsBigDecimalList2_2.defaultWeight());
		assertArrayEquals0(weightsBigDecimalList3.defaultWeight(), weightsBigDecimalList3_2.defaultWeight());
		assertArrayEquals0(weightsBigDecimalList4.defaultWeight(), weightsBigDecimalList4_2.defaultWeight());
		assertArrayEquals(weightsBigIntegerList1.defaultWeight(), weightsBigIntegerList1_2.defaultWeight());
		assertArrayEquals(weightsBigIntegerList2.defaultWeight(), weightsBigIntegerList2_2.defaultWeight());
		assertArrayEquals0(weightsBigIntegerList3.defaultWeight(), weightsBigIntegerList3_2.defaultWeight());
		assertArrayEquals0(weightsBigIntegerList4.defaultWeight(), weightsBigIntegerList4_2.defaultWeight());
	}

	private static void assertArrayEquals0(List<Byte> expected, byte[] actual) {
		if (expected == null) {
			assertNull(actual);
		} else {
			ByteList expected0 = expected instanceof ByteList ? (ByteList) expected : new ByteArrayList(expected);
			assertArrayEquals(expected0.toByteArray(), actual);
		}
	}

	private static void assertArrayEquals0(List<Short> expected, short[] actual) {
		if (expected == null) {
			assertNull(actual);
		} else {
			ShortList expected0 = expected instanceof ShortList ? (ShortList) expected : new ShortArrayList(expected);
			assertArrayEquals(expected0.toShortArray(), actual);
		}
	}

	private static void assertArrayEquals0(List<Integer> expected, int[] actual) {
		if (expected == null) {
			assertNull(actual);
		} else {
			IntList expected0 = expected instanceof IntList ? (IntList) expected : new IntArrayList(expected);
			assertArrayEquals(expected0.toIntArray(), actual);
		}
	}

	private static void assertArrayEquals0(List<Long> expected, long[] actual) {
		if (expected == null) {
			assertNull(actual);
		} else {
			LongList expected0 = expected instanceof LongList ? (LongList) expected : new LongArrayList(expected);
			assertArrayEquals(expected0.toLongArray(), actual);
		}
	}

	private static void assertArrayEquals0(List<Float> expected, float[] actual, float delta) {
		if (expected == null) {
			assertNull(actual);
		} else {
			FloatList expected0 = expected instanceof FloatList ? (FloatList) expected : new FloatArrayList(expected);
			assertArrayEquals(expected0.toFloatArray(), actual, delta);
		}
	}

	private static void assertArrayEquals0(List<Double> expected, double[] actual, double delta) {
		if (expected == null) {
			assertNull(actual);
		} else {
			DoubleList expected0 =
					expected instanceof DoubleList ? (DoubleList) expected : new DoubleArrayList(expected);
			assertArrayEquals(expected0.toDoubleArray(), actual, delta);
		}
	}

	private static void assertArrayEquals0(List<Boolean> expected, boolean[] actual) {
		if (expected == null) {
			assertNull(actual);
		} else {
			BooleanList expected0 =
					expected instanceof BooleanList ? (BooleanList) expected : new BooleanArrayList(expected);
			assertArrayEquals(expected0.toBooleanArray(), actual);
		}
	}

	private static void assertArrayEquals0(List<Character> expected, char[] actual) {
		if (expected == null) {
			assertNull(actual);
		} else {
			CharList expected0 = expected instanceof CharList ? (CharList) expected : new CharArrayList(expected);
			assertArrayEquals(expected0.toCharArray(), actual);
		}
	}

	private static <T> void assertArrayEquals0(List<T> expected, T[] actual) {
		if (expected == null) {
			assertNull(actual);
		} else {
			assertArrayEquals(expected.toArray(), actual);
		}
	}

	private static byte replaceNull(Byte b, byte nullVal) {
		return b != null ? b.byteValue() : nullVal;
	}

	private static short replaceNull(Short b, short nullVal) {
		return b != null ? b.shortValue() : nullVal;
	}

	private static int replaceNull(Integer b, int nullVal) {
		return b != null ? b.intValue() : nullVal;
	}

	private static long replaceNull(Long b, long nullVal) {
		return b != null ? b.longValue() : nullVal;
	}

	private static float replaceNull(Float b, float nullVal) {
		return b != null ? b.floatValue() : nullVal;
	}

	private static double replaceNull(Double b, double nullVal) {
		return b != null ? b.doubleValue() : nullVal;
	}

	private static boolean replaceNull(Boolean b, boolean nullVal) {
		return b != null ? b.booleanValue() : nullVal;
	}

	private static char replaceNull(Character b, char nullVal) {
		return b != null ? b.charValue() : nullVal;
	}

	@Test
	public void writeWeightsVerticesUnsupported() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);

		IWeightsObj<IntGraph> weights = g.addVerticesWeights("weight", IntGraph.class);
		weights.set(0, IntGraph.newDirected());

		GexfGraphWriter<Integer, Integer> writer = new GexfGraphWriter<>();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeWeightsEdgesUnsupported() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 88);

		IWeightsObj<IntGraph> weights = g.addEdgesWeights("weight", IntGraph.class);
		weights.set(88, IntGraph.newDirected());

		GexfGraphWriter<Integer, Integer> writer = new GexfGraphWriter<>();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeWeightsVerticesNoDefVal() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);

		IWeightsObj<String> weights = g.addVerticesWeights("weight", String.class);
		weights.set(0, "hello world");

		GexfGraphWriter<Integer, Integer> writer = new GexfGraphWriter<>();
		StringWriter sw = new StringWriter();
		writer.writeGraph(g, sw);
		String text = sw.toString();

		GexfGraphReader<Integer, Integer> reader = new GexfGraphReader<>(int.class, int.class);
		IntGraph g2 = (IntGraph) reader.readGraph(new StringReader(text));
		assertEquals(g, g2);

		IWeightsObj<String> weights2 = g2.getVerticesWeights("weight");
		assertEquals(weights.defaultWeight(), weights2.defaultWeight());
	}

	@Test
	public void writeWeightsEdgesNoDefVal() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 88);

		IWeightsObj<String> weights = g.addEdgesWeights("weight", String.class);
		weights.set(88, "hello world");

		GexfGraphWriter<Integer, Integer> writer = new GexfGraphWriter<>();
		StringWriter sw = new StringWriter();
		writer.writeGraph(g, sw);
		String text = sw.toString();

		GexfGraphReader<Integer, Integer> reader = new GexfGraphReader<>(int.class, int.class);
		IntGraph g2 = (IntGraph) reader.readGraph(new StringReader(text));
		assertEquals(g, g2);

		IWeightsObj<String> weights2 = g2.getEdgesWeights("weight");
		assertEquals(weights.defaultWeight(), weights2.defaultWeight());
	}

}
