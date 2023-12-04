/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the \"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an \"AS IS\" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jgalgo.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;
import java.util.Set;
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
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntList;

public class GraphMlTest {

	@Test
	public void readSimpleExample1() {
		Graph<String, String> g = Graph.newUndirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addVertex("n2");
		g.addVertex("n3");
		g.addVertex("n4");
		g.addVertex("n5");
		g.addVertex("n6");
		g.addVertex("n7");
		g.addVertex("n8");
		g.addVertex("n9");
		g.addVertex("n10");
		g.addEdge("n0", "n2", "e0");
		g.addEdge("n1", "n2", "e1");
		g.addEdge("n2", "n3", "e2");
		g.addEdge("n3", "n5", "e3");
		g.addEdge("n3", "n4", "e4");
		g.addEdge("n4", "n6", "e5");
		g.addEdge("n6", "n5", "e6");
		g.addEdge("n5", "n7", "e7");
		g.addEdge("n6", "n8", "e8");
		g.addEdge("n8", "n7", "e9");
		g.addEdge("n8", "n9", "e10");
		g.addEdge("n8", "n10", "e11");

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"");
		text.addLine("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		text.addLine("    xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns");
		text.addLine("     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
		text.addLine("  <graph id=\"G\" edgedefault=\"undirected\">");
		text.addLine("    <node id=\"n0\"/>");
		text.addLine("    <node id=\"n1\"/>");
		text.addLine("    <node id=\"n2\"/>");
		text.addLine("    <node id=\"n3\"/>");
		text.addLine("    <node id=\"n4\"/>");
		text.addLine("    <node id=\"n5\"/>");
		text.addLine("    <node id=\"n6\"/>");
		text.addLine("    <node id=\"n7\"/>");
		text.addLine("    <node id=\"n8\"/>");
		text.addLine("    <node id=\"n9\"/>");
		text.addLine("    <node id=\"n10\"/>");
		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n2\"/>");
		text.addLine("    <edge id=\"e1\" source=\"n1\" target=\"n2\"/>");
		text.addLine("    <edge id=\"e2\" source=\"n2\" target=\"n3\"/>");
		text.addLine("    <edge id=\"e3\" source=\"n3\" target=\"n5\"/>");
		text.addLine("    <edge id=\"e4\" source=\"n3\" target=\"n4\"/>");
		text.addLine("    <edge id=\"e5\" source=\"n4\" target=\"n6\"/>");
		text.addLine("    <edge id=\"e6\" source=\"n6\" target=\"n5\"/>");
		text.addLine("    <edge id=\"e7\" source=\"n5\" target=\"n7\"/>");
		text.addLine("    <edge id=\"e8\" source=\"n6\" target=\"n8\"/>");
		text.addLine("    <edge id=\"e9\" source=\"n8\" target=\"n7\"/>");
		text.addLine("    <edge id=\"e10\" source=\"n8\" target=\"n9\"/>");
		text.addLine("    <edge id=\"e11\" source=\"n8\" target=\"n10\"/>");
		text.addLine("  </graph>");
		text.addLine("</graphml>");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readSimpleExample2WithWeights() {
		Graph<String, String> g = Graph.newUndirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addVertex("n2");
		g.addVertex("n3");
		g.addVertex("n4");
		g.addVertex("n5");
		g.addEdge("n0", "n2", "e0");
		g.addEdge("n0", "n1", "e1");
		g.addEdge("n1", "n3", "e2");
		g.addEdge("n3", "n2", "e3");
		g.addEdge("n2", "n4", "e4");
		g.addEdge("n3", "n5", "e5");
		g.addEdge("n5", "n4", "e6");
		WeightsObj<String, String> vWeights = g.addVerticesWeights("color", String.class, "yellow");
		vWeights.set("n0", "green");
		vWeights.set("n2", "blue");
		vWeights.set("n3", "red");
		vWeights.set("n5", "turquoise");
		WeightsDouble<String> eWeights = g.addEdgesWeights("weight", double.class);
		eWeights.set("e0", 1.0);
		eWeights.set("e1", 1.0);
		eWeights.set("e2", 2.0);
		eWeights.set("e6", 1.1);

		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  ");
		text.addLine("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		text.addLine("      xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns ");
		text.addLine("        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
		text.addLine("  <key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">");
		text.addLine("    <default>yellow</default>");
		text.addLine("  </key>");
		text.addLine("  <key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>");
		text.addLine("  <graph id=\"G\" edgedefault=\"undirected\">");
		text.addLine("    <node id=\"n0\">");
		text.addLine("      <data key=\"d0\">green</data>");
		text.addLine("    </node>");
		text.addLine("    <node id=\"n1\"/>");
		text.addLine("    <node id=\"n2\">");
		text.addLine("      <data key=\"d0\">blue</data>");
		text.addLine("    </node>");
		text.addLine("    <node id=\"n3\">");
		text.addLine("      <data key=\"d0\">red</data>");
		text.addLine("    </node>");
		text.addLine("    <node id=\"n4\"/>");
		text.addLine("    <node id=\"n5\">");
		text.addLine("      <data key=\"d0\">turquoise</data>");
		text.addLine("    </node>");
		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n2\">");
		text.addLine("      <data key=\"d1\">1.0</data>");
		text.addLine("    </edge>");
		text.addLine("    <edge id=\"e1\" source=\"n0\" target=\"n1\">");
		text.addLine("      <data key=\"d1\">1.0</data>");
		text.addLine("    </edge>");
		text.addLine("    <edge id=\"e2\" source=\"n1\" target=\"n3\">");
		text.addLine("      <data key=\"d1\">2.0</data>");
		text.addLine("    </edge>");
		text.addLine("    <edge id=\"e3\" source=\"n3\" target=\"n2\"/>");
		text.addLine("    <edge id=\"e4\" source=\"n2\" target=\"n4\"/>");
		text.addLine("    <edge id=\"e5\" source=\"n3\" target=\"n5\"/>");
		text.addLine("    <edge id=\"e6\" source=\"n5\" target=\"n4\">");
		text.addLine("      <data key=\"d1\">1.1</data>");
		text.addLine("    </edge>");
		text.addLine("  </graph>");
		text.addLine("</graphml>");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void writeReadRandGraphs() {
		final long seed = 0x58e933bdecfb13ccL;
		Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (int n : IntList.of(6, 30, 211)) {
				for (int repeat = 0; repeat < 32; repeat++) {
					final int m = n + rand.nextInt(2 * n);
					IntGraphFactory factory =
							directed ? IntGraphFactory.newDirected() : IntGraphFactory.newUndirected();
					IntGraph g = factory.allowSelfEdges().allowParallelEdges().newGraph();
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
					GraphMlGraphWriter<Integer, Integer> graphWriter = new GraphMlGraphWriter<>();
					graphWriter.writeGraph(g, writer);
					String data = writer.toString();
					GraphMlGraphReader<Integer, Integer> graphReader = new GraphMlGraphReader<>(int.class, int.class);
					IntGraph g1 = (IntGraph) graphReader.readGraph(new StringReader(data));

					assertEquals(g, g1);
				}
			}
		}
	}

	@Test
	public void readExpectedVerticesEdgesNum() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  ");
		text.addLine("            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		text.addLine("            xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns ");
		text.addLine("                                http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\" ");
		text.addLine("            parse.nodes=\"11\" parse.edges=\"12\" ");
		text.addLine("            parse.maxindegree=\"2\" parse.maxoutdegree=\"3\"");
		text.addLine("            parse.nodeids=\"canonical\" parse.edgeids=\"free\" ");
		text.addLine("            parse.order=\"nodesfirst\">");
		text.addLine("    <node id=\"n0\" parse.indegree=\"0\" parse.outdegree=\"1\"/>");
		text.addLine("    <node id=\"n1\" parse.indegree=\"0\" parse.outdegree=\"1\"/>");
		text.addLine("    <node id=\"n2\" parse.indegree=\"2\" parse.outdegree=\"1\"/>");
		text.addLine("    <node id=\"n3\" parse.indegree=\"1\" parse.outdegree=\"2\"/>");
		text.addLine("    <node id=\"n4\" parse.indegree=\"1\" parse.outdegree=\"1\"/>");
		text.addLine("    <node id=\"n5\" parse.indegree=\"2\" parse.outdegree=\"1\"/>");
		text.addLine("    <node id=\"n6\" parse.indegree=\"1\" parse.outdegree=\"2\"/>");
		text.addLine("    <node id=\"n7\" parse.indegree=\"2\" parse.outdegree=\"0\"/>");
		text.addLine("    <node id=\"n8\" parse.indegree=\"1\" parse.outdegree=\"3\"/>");
		text.addLine("    <node id=\"n9\" parse.indegree=\"1\" parse.outdegree=\"0\"/>");
		text.addLine("    <node id=\"n10\" parse.indegree=\"1\" parse.outdegree=\"0\"/>");
		text.addLine("    <edge id=\"edge0001\" source=\"n0\" target=\"n2\"/>");
		text.addLine("    <edge id=\"edge0002\" source=\"n1\" target=\"n2\"/>");
		text.addLine("    <edge id=\"edge0003\" source=\"n2\" target=\"n3\"/>");
		text.addLine("    <edge id=\"edge0004\" source=\"n3\" target=\"n5\"/>");
		text.addLine("    <edge id=\"edge0005\" source=\"n3\" target=\"n4\"/>");
		text.addLine("    <edge id=\"edge0006\" source=\"n4\" target=\"n6\"/>");
		text.addLine("    <edge id=\"edge0007\" source=\"n6\" target=\"n5\"/>");
		text.addLine("    <edge id=\"edge0008\" source=\"n5\" target=\"n7\"/>");
		text.addLine("    <edge id=\"edge0009\" source=\"n6\" target=\"n8\"/>");
		text.addLine("    <edge id=\"edge0010\" source=\"n8\" target=\"n7\"/>");
		text.addLine("    <edge id=\"edge0011\" source=\"n8\" target=\"n9\"/>");
		text.addLine("    <edge id=\"edge0012\" source=\"n8\" target=\"n10\"/>");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertNotNull(reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVertexParser() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"8\" />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>();
		reader.setVertexParser(s -> s + "a");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("8a");
		assertEquals(g, reader.readGraph(new StringReader(text.get())));

		assertThrows(IllegalStateException.class,
				() -> new GraphMlGraphReader<>().readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVertexParserDefault() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"8\" />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");

		{
			Graph<Byte, String> g = Graph.newDirected();
			g.addVertex(Byte.valueOf((byte) 8));
			GraphMlGraphReader<Byte, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(byte.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Byte.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Short, String> g = Graph.newDirected();
			g.addVertex(Short.valueOf((short) 8));
			GraphMlGraphReader<Short, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(short.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Short.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Integer, String> g = Graph.newDirected();
			g.addVertex(Integer.valueOf(8));
			GraphMlGraphReader<Integer, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(int.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Integer.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Long, String> g = Graph.newDirected();
			g.addVertex(Long.valueOf(8));
			GraphMlGraphReader<Long, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(long.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Long.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Float, String> g = Graph.newDirected();
			g.addVertex(Float.valueOf(8));
			GraphMlGraphReader<Float, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(float.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Float.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<Double, String> g = Graph.newDirected();
			g.addVertex(Double.valueOf(8));
			GraphMlGraphReader<Double, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(double.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
			reader.setVertexParserDefault(Double.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			Graph<String, String> g = Graph.newDirected();
			g.addVertex("8");
			GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(String.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			GraphMlGraphReader<IntList, String> reader = new GraphMlGraphReader<>();
			assertThrows(IllegalArgumentException.class, () -> reader.setVertexParserDefault(IntList.class));
		}
	}

	@Test
	public void readEdgeParser() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"0\" />");
		text.addLine("    <node id=\"1\" />");
		text.addLine("    <edge id=\"88\" source=\"0\" target=\"1\" />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>();
		reader.setVertexParserDefault(String.class);
		reader.setEdgeParser(s -> s + "a");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("0");
		g.addVertex("1");
		g.addEdge("0", "1", "88a");
		assertEquals(g, reader.readGraph(new StringReader(text.get())));

		assertThrows(IllegalStateException.class, () -> {
			GraphMlGraphReader<String, String> reader2 = new GraphMlGraphReader<>();
			reader2.setVertexParserDefault(String.class);
			reader2.readGraph(new StringReader(text.get()));
		});
	}

	@Test
	public void readEdgeParserDefault() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"0\" />");
		text.addLine("    <node id=\"1\" />");
		text.addLine("    <edge id=\"88\" source=\"0\" target=\"1\" />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");

		{
			Graph<String, Byte> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Byte.valueOf((byte) 88));
			GraphMlGraphReader<String, Byte> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Short> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Integer> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Long> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Float> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Double> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeParserDefault(String.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			GraphMlGraphReader<String, IntList> reader = new GraphMlGraphReader<>();
			assertThrows(IllegalArgumentException.class, () -> reader.setEdgeParserDefault(IntList.class));
		}
	}

	@Test
	public void readEdgeSupplier() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"0\" />");
		text.addLine("    <node id=\"1\" />");
		text.addLine("    <edge source=\"0\" target=\"1\" />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, Integer> reader = new GraphMlGraphReader<>();
		reader.setVertexParserDefault(String.class);
		reader.setEdgeSupplier(Set::size);

		Graph<String, Integer> g = Graph.newDirected();
		g.addVertex("0");
		g.addVertex("1");
		g.addEdge("0", "1", Integer.valueOf(0));
		assertEquals(g, reader.readGraph(new StringReader(text.get())));

		assertThrows(IllegalStateException.class, () -> {
			GraphMlGraphReader<String, String> reader2 = new GraphMlGraphReader<>();
			reader2.setVertexParserDefault(String.class);
			reader2.readGraph(new StringReader(text.get()));
		});
	}

	@Test
	public void readEdgeSupplierDefault() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"0\" />");
		text.addLine("    <node id=\"1\" />");
		text.addLine("    <edge source=\"0\" target=\"1\" />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");

		{
			Graph<String, Byte> g = Graph.newDirected();
			g.addVertex("0");
			g.addVertex("1");
			g.addEdge("0", "1", Byte.valueOf((byte) 0));
			GraphMlGraphReader<String, Byte> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Short> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Integer> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Long> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Float> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, Double> reader = new GraphMlGraphReader<>();
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
			GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>();
			reader.setVertexParserDefault(String.class);
			reader.setEdgeSupplierDefault(String.class);
			assertEquals(g, reader.readGraph(new StringReader(text.get())));
		}
		{
			GraphMlGraphReader<String, IntList> reader = new GraphMlGraphReader<>();
			assertThrows(IllegalArgumentException.class, () -> reader.setEdgeSupplierDefault(IntList.class));
		}
	}

	@Test
	public void readEdgeSupplierTooManyEdges() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"0\" />");
		text.addLine("    <node id=\"1\" />");
		for (int i = 0; i < 257; i++)
			text.addLine("    <edge source=\"0\" target=\"1\" />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");

		GraphMlGraphReader<String, Byte> reader = new GraphMlGraphReader<>();
		reader.setVertexParserDefault(String.class);
		reader.setEdgeSupplierDefault(byte.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgeIdsSomeMissingSomeExists() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"0\" />");
		text.addLine("    <node id=\"1\" />");
		text.addLine("    <node id=\"2\" />");
		text.addLine("    <node id=\"3\" />");
		text.addLine("    <node id=\"4\" />");
		text.addLine("    <node id=\"5\" />");
		text.addLine("    <node id=\"6\" />");
		text.addLine("    <node id=\"7\" />");
		text.addLine("    <edge source=\"0\" target=\"1\" id=\"0\" />");
		text.addLine("    <edge source=\"0\" target=\"2\" />");
		text.addLine("    <edge source=\"0\" target=\"3\" />");
		text.addLine("    <edge source=\"0\" target=\"4\" id=\"4\" />");
		text.addLine("    <edge source=\"0\" target=\"5\" id=\"6\" />");
		text.addLine("    <edge source=\"0\" target=\"6\" id=\"3\" />");
		text.addLine("    <edge source=\"0\" target=\"7\" />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, Integer> reader = new GraphMlGraphReader<>();
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
		text.addLine("<notgraphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node id=\"0\" />");
		text.addLine("    <node id=\"1\" />");
		text.addLine("    <edge source=\"0\" target=\"1\" id=\"0\" />");
		text.addLine("  </graph>");
		text.addLine("</notgraphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVertexIdEmpty() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("");
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVertexIdMissing() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph edgedefault=\"directed\">");
		text.addLine("    <node />");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgeMissingSource() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">");
		text.addLine("    <node id=\"n0\"/>");
		text.addLine("    <node id=\"n1\"/>");
		text.addLine("    <edge id=\"e0\" target=\"n1\"/>");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgeMissingTarget() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">");
		text.addLine("    <node id=\"n0\"/>");
		text.addLine("    <node id=\"n1\"/>");
		text.addLine("    <edge id=\"e0\" source=\"n0\"/>");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readDirectedUndirectedMix() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">");
		text.addLine("    <node id=\"n0\"/>");
		text.addLine("    <node id=\"n1\"/>");
		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\" directed=\"false\"/>");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readNoGraph() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readMultipleGraphs() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph id=\"G1\" edgedefault=\"directed\">");
		text.addLine("    <node id=\"n0\"/>");
		text.addLine("    <node id=\"n1\"/>");
		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>");
		text.addLine("  </graph>");
		text.addLine("  <graph id=\"G2\" edgedefault=\"directed\">");
		text.addLine("    <node id=\"n0\"/>");
		text.addLine("    <node id=\"n1\"/>");
		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>");
		text.addLine("  </graph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readInvalidXml() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		text.addLine("  <graph id=\"G1\" edgedefault=\"directed\">");
		text.addLine("    <node id=\"n0\"/>");
		text.addLine("    <node id=\"n1\"/>");
		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>");
		text.addLine("  </notgraph>");
		text.addLine("</graphml>");
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsInt() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"node-weight\" attr.type=\"int\"/>\n");
		text.addLine("  <key id=\"d2\" for=\"edge\" attr.name=\"edge-weight\" attr.type=\"int\">\n");
		text.addLine("    <default>1</default>\n");
		text.addLine("  </key>\n");
		text.addLine("  <key id=\"d3\" for=\"all\" attr.name=\"all-weight\" attr.type=\"int\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");

		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("    <node id=\"n1\">\n");
		text.addLine("      <data key=\"d1\">1</data>\n");
		text.addLine("    </node>\n");
		text.addLine("    <node id=\"n2\">\n");
		text.addLine("      <data key=\"d3\">2</data>\n");
		text.addLine("    </node>\n");

		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>\n");
		text.addLine("    <edge id=\"e1\" source=\"n1\" target=\"n2\">\n");
		text.addLine("      <data key=\"d2\">6</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("    <edge id=\"e2\" source=\"n2\" target=\"n0\">\n");
		text.addLine("      <data key=\"d3\">17</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addVertex("n2");
		g.addEdge("n0", "n1", "e0");
		g.addEdge("n1", "n2", "e1");
		g.addEdge("n2", "n0", "e2");
		WeightsInt<String> vWeights1 = g.addVerticesWeights("node-weight", int.class);
		WeightsInt<String> vWeights2 = g.addVerticesWeights("all-weight", int.class);
		WeightsInt<String> eWeights1 = g.addEdgesWeights("edge-weight", int.class, Integer.valueOf(1));
		WeightsInt<String> eWeights2 = g.addEdgesWeights("all-weight", int.class);
		vWeights1.set("n1", 1);
		vWeights2.set("n2", 2);
		eWeights1.set("e1", 6);
		eWeights2.set("e2", 17);

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsLong() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"node-weight\" attr.type=\"long\"/>\n");
		text.addLine("  <key id=\"d2\" for=\"edge\" attr.name=\"edge-weight\" attr.type=\"long\">\n");
		text.addLine("    <default>1</default>\n");
		text.addLine("  </key>\n");
		text.addLine("  <key id=\"d3\" for=\"all\" attr.name=\"all-weight\" attr.type=\"long\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");

		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("    <node id=\"n1\">\n");
		text.addLine("      <data key=\"d1\">1</data>\n");
		text.addLine("    </node>\n");
		text.addLine("    <node id=\"n2\">\n");
		text.addLine("      <data key=\"d3\">2</data>\n");
		text.addLine("    </node>\n");

		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>\n");
		text.addLine("    <edge id=\"e1\" source=\"n1\" target=\"n2\">\n");
		text.addLine("      <data key=\"d2\">6</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("    <edge id=\"e2\" source=\"n2\" target=\"n0\">\n");
		text.addLine("      <data key=\"d3\">17</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addVertex("n2");
		g.addEdge("n0", "n1", "e0");
		g.addEdge("n1", "n2", "e1");
		g.addEdge("n2", "n0", "e2");
		WeightsLong<String> vWeights1 = g.addVerticesWeights("node-weight", long.class);
		WeightsLong<String> vWeights2 = g.addVerticesWeights("all-weight", long.class);
		WeightsLong<String> eWeights1 = g.addEdgesWeights("edge-weight", long.class, Long.valueOf(1));
		WeightsLong<String> eWeights2 = g.addEdgesWeights("all-weight", long.class);
		vWeights1.set("n1", 1);
		vWeights2.set("n2", 2);
		eWeights1.set("e1", 6);
		eWeights2.set("e2", 17);

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsFloat() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"node-weight\" attr.type=\"float\"/>\n");
		text.addLine("  <key id=\"d2\" for=\"edge\" attr.name=\"edge-weight\" attr.type=\"float\">\n");
		text.addLine("    <default>1.1</default>\n");
		text.addLine("  </key>\n");
		text.addLine("  <key id=\"d3\" for=\"all\" attr.name=\"all-weight\" attr.type=\"float\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");

		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("    <node id=\"n1\">\n");
		text.addLine("      <data key=\"d1\">1.2</data>\n");
		text.addLine("    </node>\n");
		text.addLine("    <node id=\"n2\">\n");
		text.addLine("      <data key=\"d3\">2.3</data>\n");
		text.addLine("    </node>\n");

		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>\n");
		text.addLine("    <edge id=\"e1\" source=\"n1\" target=\"n2\">\n");
		text.addLine("      <data key=\"d2\">6.7</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("    <edge id=\"e2\" source=\"n2\" target=\"n0\">\n");
		text.addLine("      <data key=\"d3\">17.18</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addVertex("n2");
		g.addEdge("n0", "n1", "e0");
		g.addEdge("n1", "n2", "e1");
		g.addEdge("n2", "n0", "e2");
		WeightsFloat<String> vWeights1 = g.addVerticesWeights("node-weight", float.class);
		WeightsFloat<String> vWeights2 = g.addVerticesWeights("all-weight", float.class);
		WeightsFloat<String> eWeights1 = g.addEdgesWeights("edge-weight", float.class, Float.valueOf(1.1f));
		WeightsFloat<String> eWeights2 = g.addEdgesWeights("all-weight", float.class);
		vWeights1.set("n1", 1.2f);
		vWeights2.set("n2", 2.3f);
		eWeights1.set("e1", 6.7f);
		eWeights2.set("e2", 17.18f);

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsDouble() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"node-weight\" attr.type=\"double\"/>\n");
		text.addLine("  <key id=\"d2\" for=\"edge\" attr.name=\"edge-weight\" attr.type=\"double\">\n");
		text.addLine("    <default>1.1</default>\n");
		text.addLine("  </key>\n");
		text.addLine("  <key id=\"d3\" for=\"all\" attr.name=\"all-weight\" attr.type=\"double\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");

		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("    <node id=\"n1\">\n");
		text.addLine("      <data key=\"d1\">1.2</data>\n");
		text.addLine("    </node>\n");
		text.addLine("    <node id=\"n2\">\n");
		text.addLine("      <data key=\"d3\">2.3</data>\n");
		text.addLine("    </node>\n");

		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>\n");
		text.addLine("    <edge id=\"e1\" source=\"n1\" target=\"n2\">\n");
		text.addLine("      <data key=\"d2\">6.7</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("    <edge id=\"e2\" source=\"n2\" target=\"n0\">\n");
		text.addLine("      <data key=\"d3\">17.18</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addVertex("n2");
		g.addEdge("n0", "n1", "e0");
		g.addEdge("n1", "n2", "e1");
		g.addEdge("n2", "n0", "e2");
		WeightsDouble<String> vWeights1 = g.addVerticesWeights("node-weight", double.class);
		WeightsDouble<String> vWeights2 = g.addVerticesWeights("all-weight", double.class);
		WeightsDouble<String> eWeights1 = g.addEdgesWeights("edge-weight", double.class, Double.valueOf(1.1));
		WeightsDouble<String> eWeights2 = g.addEdgesWeights("all-weight", double.class);
		vWeights1.set("n1", 1.2);
		vWeights2.set("n2", 2.3);
		eWeights1.set("e1", 6.7);
		eWeights2.set("e2", 17.18);

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsString() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"node-weight\" attr.type=\"string\"/>\n");
		text.addLine("  <key id=\"d2\" for=\"edge\" attr.name=\"edge-weight\" attr.type=\"string\">\n");
		text.addLine("    <default>yellow</default>\n");
		text.addLine("  </key>\n");
		text.addLine("  <key id=\"d3\" for=\"all\" attr.name=\"all-weight\" attr.type=\"string\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");

		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("    <node id=\"n1\">\n");
		text.addLine("      <data key=\"d1\">ball</data>\n");
		text.addLine("    </node>\n");
		text.addLine("    <node id=\"n2\">\n");
		text.addLine("      <data key=\"d3\">square</data>\n");
		text.addLine("    </node>\n");

		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>\n");
		text.addLine("    <edge id=\"e1\" source=\"n1\" target=\"n2\">\n");
		text.addLine("      <data key=\"d2\">box</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("    <edge id=\"e2\" source=\"n2\" target=\"n0\">\n");
		text.addLine("      <data key=\"d3\">tower</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addVertex("n2");
		g.addEdge("n0", "n1", "e0");
		g.addEdge("n1", "n2", "e1");
		g.addEdge("n2", "n0", "e2");
		WeightsObj<String, String> vWeights1 = g.addVerticesWeights("node-weight", String.class);
		WeightsObj<String, String> vWeights2 = g.addVerticesWeights("all-weight", String.class);
		WeightsObj<String, String> eWeights1 = g.addEdgesWeights("edge-weight", String.class, "yellow");
		WeightsObj<String, String> eWeights2 = g.addEdgesWeights("all-weight", String.class);
		vWeights1.set("n1", "ball");
		vWeights2.set("n2", "square");
		eWeights1.set("e1", "box");
		eWeights2.set("e2", "tower");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsBoolean() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"node-weight\" attr.type=\"boolean\"/>\n");
		text.addLine("  <key id=\"d2\" for=\"edge\" attr.name=\"edge-weight\" attr.type=\"boolean\">\n");
		text.addLine("    <default>true</default>\n");
		text.addLine("  </key>\n");
		text.addLine("  <key id=\"d3\" for=\"all\" attr.name=\"all-weight\" attr.type=\"boolean\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");

		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("    <node id=\"n1\">\n");
		text.addLine("      <data key=\"d1\">true</data>\n");
		text.addLine("    </node>\n");
		text.addLine("    <node id=\"n2\">\n");
		text.addLine("      <data key=\"d3\">false</data>\n");
		text.addLine("    </node>\n");

		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\"/>\n");
		text.addLine("    <edge id=\"e1\" source=\"n1\" target=\"n2\">\n");
		text.addLine("      <data key=\"d2\">false</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("    <edge id=\"e2\" source=\"n2\" target=\"n0\">\n");
		text.addLine("      <data key=\"d3\">true</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		Graph<String, String> g = Graph.newDirected();
		g.addVertex("n0");
		g.addVertex("n1");
		g.addVertex("n2");
		g.addEdge("n0", "n1", "e0");
		g.addEdge("n1", "n2", "e1");
		g.addEdge("n2", "n0", "e2");
		WeightsBool<String> vWeights1 = g.addVerticesWeights("node-weight", boolean.class);
		WeightsBool<String> vWeights2 = g.addVerticesWeights("all-weight", boolean.class);
		WeightsBool<String> eWeights1 = g.addEdgesWeights("edge-weight", boolean.class, Boolean.TRUE);
		WeightsBool<String> eWeights2 = g.addEdgesWeights("all-weight", boolean.class);
		vWeights1.set("n1", true);
		vWeights2.set("n2", false);
		eWeights1.set("e1", false);
		eWeights2.set("e2", true);

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertEquals(g, reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsUnknownType() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"weight\" attr.type=\"unknown-type\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsUnknownDomain() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"unknown-domain\" attr.name=\"weight\" attr.type=\"int\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVerticesWeightsSameId() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"weight1\" attr.type=\"int\"/>\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"weight2\" attr.type=\"string\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readVerticesWeightsSameName() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"weight\" attr.type=\"int\"/>\n");
		text.addLine("  <key id=\"d2\" for=\"node\" attr.name=\"weight\" attr.type=\"string\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgesWeightsSameId() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"edge\" attr.name=\"weight1\" attr.type=\"int\"/>\n");
		text.addLine("  <key id=\"d1\" for=\"edge\" attr.name=\"weight2\" attr.type=\"string\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readEdgesWeightsSameName() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"int\"/>\n");
		text.addLine("  <key id=\"d2\" for=\"edge\" attr.name=\"weight\" attr.type=\"string\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsMultipleDefaultVal() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"weight\" attr.type=\"int\">\n");
		text.addLine("    <default>1</default>\n");
		text.addLine("    <default>2</default>\n");
		text.addLine("  </key>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readWeightsUndeclaredWeight() {
		TextBuilder text = new TextBuilder();
		GraphMlGraphReader<String, String> reader = new GraphMlGraphReader<>(String.class, String.class);

		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"node\" attr.name=\"weight\" attr.type=\"int\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\">\n");
		text.addLine("      <data key=\"d2\">1</data>\n");
		text.addLine("    </node>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"int\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"n0\"/>\n");
		text.addLine("    <node id=\"n1\"/>\n");
		text.addLine("    <edge id=\"e0\" source=\"n0\" target=\"n1\">\n");
		text.addLine("      <data key=\"d2\">1</data>\n");
		text.addLine("    </edge>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@SuppressWarnings("boxing")
	@Test
	public void writeWeights() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 0);
		g.addEdge(1, 0, 1);

		IWeightsByte vWeightsByte1 = g.addVerticesWeights("v-weights-byte1", byte.class);
		IWeightsByte vWeightsByte2 = g.addVerticesWeights("v-weights-byte2", byte.class, Byte.valueOf((byte) 1));
		IWeightsObj<Byte> vWeightsByte3 = g.addVerticesWeights("v-weights-byte3", Byte.class);
		IWeightsObj<Byte> vWeightsByte4 = g.addVerticesWeights("v-weights-byte4", Byte.class, Byte.valueOf((byte) 2));
		IWeightsShort vWeightsShort1 = g.addVerticesWeights("v-weights-short1", short.class);
		IWeightsShort vWeightsShort2 = g.addVerticesWeights("v-weights-short2", short.class, Short.valueOf((short) 3));
		IWeightsObj<Short> vWeightsShort3 = g.addVerticesWeights("v-weights-short3", Short.class);
		IWeightsObj<Short> vWeightsShort4 =
				g.addVerticesWeights("v-weights-short4", Short.class, Short.valueOf((short) 4));
		IWeightsInt vWeightsInt1 = g.addVerticesWeights("v-weights-int1", int.class);
		IWeightsInt vWeightsInt2 = g.addVerticesWeights("v-weights-int2", int.class, Integer.valueOf(5));
		IWeightsObj<Integer> vWeightsInt3 = g.addVerticesWeights("v-weights-int3", Integer.class);
		IWeightsObj<Integer> vWeightsInt4 = g.addVerticesWeights("v-weights-int4", Integer.class, Integer.valueOf(6));
		IWeightsLong vWeightsLong1 = g.addVerticesWeights("v-weights-long1", long.class);
		IWeightsLong vWeightsLong2 = g.addVerticesWeights("v-weights-long2", long.class, Long.valueOf(7));
		IWeightsObj<Long> vWeightsLong3 = g.addVerticesWeights("v-weights-long3", Long.class);
		IWeightsObj<Long> vWeightsLong4 = g.addVerticesWeights("v-weights-long4", Long.class, Long.valueOf(8));
		IWeightsFloat vWeightsFloat1 = g.addVerticesWeights("v-weights-float1", float.class);
		IWeightsFloat vWeightsFloat2 = g.addVerticesWeights("v-weights-float2", float.class, Float.valueOf(9.9f));
		IWeightsObj<Float> vWeightsFloat3 = g.addVerticesWeights("v-weights-float3", Float.class);
		IWeightsObj<Float> vWeightsFloat4 =
				g.addVerticesWeights("v-weights-float4", Float.class, Float.valueOf(10.10f));
		IWeightsDouble vWeightsDouble1 = g.addVerticesWeights("v-weights-double1", double.class);
		IWeightsDouble vWeightsDouble2 = g.addVerticesWeights("v-weights-double2", double.class, Double.valueOf(11.11));
		IWeightsObj<Double> vWeightsDouble3 = g.addVerticesWeights("v-weights-double3", Double.class);
		IWeightsObj<Double> vWeightsDouble4 =
				g.addVerticesWeights("v-weights-double4", Double.class, Double.valueOf(12.12));
		IWeightsBool vWeightsBool1 = g.addVerticesWeights("v-weights-bool1", boolean.class);
		IWeightsBool vWeightsBool2 = g.addVerticesWeights("v-weights-bool2", boolean.class, Boolean.TRUE);
		IWeightsObj<Boolean> vWeightsBool3 = g.addVerticesWeights("v-weights-bool3", Boolean.class);
		IWeightsObj<Boolean> vWeightsBool4 = g.addVerticesWeights("v-weights-bool4", Boolean.class, Boolean.TRUE);
		IWeightsChar vWeightsChar1 = g.addVerticesWeights("v-weights-char1", char.class, Character.valueOf(' '));
		IWeightsChar vWeightsChar2 = g.addVerticesWeights("v-weights-char2", char.class, Character.valueOf('7'));
		IWeightsObj<Character> vWeightsChar3 = g.addVerticesWeights("v-weights-char3", Character.class);
		IWeightsObj<Character> vWeightsChar4 =
				g.addVerticesWeights("v-weights-char4", Character.class, Character.valueOf('*'));
		IWeightsObj<String> vWeightsObj1 = g.addVerticesWeights("v-weights-obj1", String.class);
		IWeightsObj<String> vWeightsObj2 = g.addVerticesWeights("v-weights-obj2", String.class, "8");
		vWeightsByte1.set(0, (byte) 31);
		vWeightsByte2.set(0, (byte) 32);
		vWeightsByte3.set(0, Byte.valueOf((byte) 33));
		vWeightsByte3.set(1, Byte.valueOf((byte) 34));
		vWeightsByte4.set(0, Byte.valueOf((byte) 35));
		vWeightsShort1.set(0, (short) 36);
		vWeightsShort2.set(0, (short) 37);
		vWeightsShort3.set(0, Short.valueOf((short) 38));
		vWeightsShort3.set(1, Short.valueOf((short) 39));
		vWeightsShort4.set(0, Short.valueOf((short) 40));
		vWeightsInt1.set(0, 41);
		vWeightsInt2.set(0, 42);
		vWeightsInt3.set(0, Integer.valueOf(43));
		vWeightsInt3.set(1, Integer.valueOf(44));
		vWeightsInt4.set(0, Integer.valueOf(45));
		vWeightsLong1.set(0, 46);
		vWeightsLong2.set(0, 47);
		vWeightsLong3.set(0, Long.valueOf(48));
		vWeightsLong3.set(1, Long.valueOf(49));
		vWeightsLong4.set(0, Long.valueOf(50));
		vWeightsFloat1.set(0, 51.51f);
		vWeightsFloat2.set(0, 52.52f);
		vWeightsFloat3.set(0, Float.valueOf(53.53f));
		vWeightsFloat3.set(1, Float.valueOf(54.54f));
		vWeightsFloat4.set(0, Float.valueOf(55.55f));
		vWeightsDouble1.set(0, 56.56);
		vWeightsDouble2.set(0, 57.57);
		vWeightsDouble3.set(0, Double.valueOf(58.58));
		vWeightsDouble3.set(1, Double.valueOf(59.59));
		vWeightsDouble4.set(0, Double.valueOf(60.60));
		vWeightsBool1.set(0, true);
		vWeightsBool2.set(0, false);
		vWeightsBool3.set(0, Boolean.TRUE);
		vWeightsBool3.set(1, Boolean.FALSE);
		vWeightsBool4.set(0, Boolean.FALSE);
		vWeightsChar1.set(0, '8');
		vWeightsChar1.set(1, '9');
		vWeightsChar2.set(0, 'A');
		vWeightsChar3.set(0, Character.valueOf('B'));
		vWeightsChar3.set(1, Character.valueOf('C'));
		vWeightsChar4.set(0, Character.valueOf('D'));
		vWeightsObj1.set(0, "43");
		vWeightsObj1.set(1, "");
		vWeightsObj2.set(0, "44");

		IWeightsByte eWeightsByte1 = g.addEdgesWeights("e-weights-byte1", byte.class);
		IWeightsByte eWeightsByte2 = g.addEdgesWeights("e-weights-byte2", byte.class, Byte.valueOf((byte) 11));
		IWeightsObj<Byte> eWeightsByte3 = g.addEdgesWeights("e-weights-byte3", Byte.class);
		IWeightsObj<Byte> eWeightsByte4 = g.addEdgesWeights("e-weights-byte4", Byte.class, Byte.valueOf((byte) 12));
		IWeightsShort eWeightsShort1 = g.addEdgesWeights("e-weights-short1", short.class);
		IWeightsShort eWeightsShort2 = g.addEdgesWeights("e-weights-short2", short.class, Short.valueOf((short) 13));
		IWeightsObj<Short> eWeightsShort3 = g.addEdgesWeights("e-weights-short3", Short.class);
		IWeightsObj<Short> eWeightsShort4 =
				g.addEdgesWeights("e-weights-short4", Short.class, Short.valueOf((short) 14));
		IWeightsInt eWeightsInt1 = g.addEdgesWeights("e-weights-int1", int.class);
		IWeightsInt eWeightsInt2 = g.addEdgesWeights("e-weights-int2", int.class, Integer.valueOf(15));
		IWeightsObj<Integer> eWeightsInt3 = g.addEdgesWeights("e-weights-int3", Integer.class);
		IWeightsObj<Integer> eWeightsInt4 = g.addEdgesWeights("e-weights-int4", Integer.class, Integer.valueOf(16));
		IWeightsLong eWeightsLong1 = g.addEdgesWeights("e-weights-long1", long.class);
		IWeightsLong eWeightsLong2 = g.addEdgesWeights("e-weights-long2", long.class, Long.valueOf(17));
		IWeightsObj<Long> eWeightsLong3 = g.addEdgesWeights("e-weights-long3", Long.class);
		IWeightsObj<Long> eWeightsLong4 = g.addEdgesWeights("e-weights-long4", Long.class, Long.valueOf(18));
		IWeightsFloat eWeightsFloat1 = g.addEdgesWeights("e-weights-float1", float.class);
		IWeightsFloat eWeightsFloat2 = g.addEdgesWeights("e-weights-float2", float.class, Float.valueOf(19.19f));
		IWeightsObj<Float> eWeightsFloat3 = g.addEdgesWeights("e-weights-float3", Float.class);
		IWeightsObj<Float> eWeightsFloat4 = g.addEdgesWeights("e-weights-float4", Float.class, Float.valueOf(20.20f));
		IWeightsDouble eWeightsDouble1 = g.addEdgesWeights("e-weights-double1", double.class);
		IWeightsDouble eWeightsDouble2 = g.addEdgesWeights("e-weights-double2", double.class, Double.valueOf(21.21));
		IWeightsObj<Double> eWeightsDouble3 = g.addEdgesWeights("e-weights-double3", Double.class);
		IWeightsObj<Double> eWeightsDouble4 =
				g.addEdgesWeights("e-weights-double4", Double.class, Double.valueOf(22.22));
		IWeightsBool eWeightsBool1 = g.addEdgesWeights("e-weights-bool1", boolean.class);
		IWeightsBool eWeightsBool2 = g.addEdgesWeights("e-weights-bool2", boolean.class, Boolean.TRUE);
		IWeightsObj<Boolean> eWeightsBool3 = g.addEdgesWeights("e-weights-bool3", Boolean.class);
		IWeightsObj<Boolean> eWeightsBool4 = g.addEdgesWeights("e-weights-bool4", Boolean.class, Boolean.TRUE);
		IWeightsChar eWeightsChar1 = g.addEdgesWeights("e-weights-char1", char.class, Character.valueOf(' '));
		IWeightsChar eWeightsChar2 = g.addEdgesWeights("e-weights-char2", char.class, Character.valueOf('*'));
		IWeightsObj<Character> eWeightsChar3 = g.addEdgesWeights("e-weights-char3", Character.class);
		IWeightsObj<Character> eWeightsChar4 =
				g.addEdgesWeights("e-weights-char4", Character.class, Character.valueOf('7'));
		IWeightsObj<String> eWeightsObj1 = g.addEdgesWeights("e-weights-obj1", String.class);
		IWeightsObj<String> eWeightsObj2 = g.addEdgesWeights("e-weights-obj2", String.class, "17");
		eWeightsByte1.set(0, (byte) 51);
		eWeightsByte2.set(0, (byte) 52);
		eWeightsByte3.set(0, Byte.valueOf((byte) 53));
		eWeightsByte3.set(1, Byte.valueOf((byte) 54));
		eWeightsByte4.set(0, Byte.valueOf((byte) 55));
		eWeightsShort1.set(0, (short) 56);
		eWeightsShort2.set(0, (short) 57);
		eWeightsShort3.set(0, Short.valueOf((short) 58));
		eWeightsShort3.set(1, Short.valueOf((short) 59));
		eWeightsShort4.set(0, Short.valueOf((short) 60));
		eWeightsInt1.set(0, 61);
		eWeightsInt2.set(0, 62);
		eWeightsInt3.set(0, Integer.valueOf(63));
		eWeightsInt3.set(1, Integer.valueOf(64));
		eWeightsInt4.set(0, Integer.valueOf(65));
		eWeightsLong1.set(0, 66);
		eWeightsLong2.set(0, 67);
		eWeightsLong3.set(0, Long.valueOf(68));
		eWeightsLong3.set(1, Long.valueOf(69));
		eWeightsLong4.set(0, Long.valueOf(70));
		eWeightsFloat1.set(0, 71.71f);
		eWeightsFloat2.set(0, 72.72f);
		eWeightsFloat3.set(0, Float.valueOf(73.73f));
		eWeightsFloat3.set(1, Float.valueOf(74.74f));
		eWeightsFloat4.set(0, Float.valueOf(75.75f));
		eWeightsDouble1.set(0, 76.76);
		eWeightsDouble2.set(0, 77.77);
		eWeightsDouble3.set(0, Double.valueOf(78.78));
		eWeightsDouble3.set(1, Double.valueOf(79.79));
		eWeightsDouble4.set(0, Double.valueOf(80.80));
		eWeightsBool1.set(0, true);
		eWeightsBool2.set(0, false);
		eWeightsBool3.set(0, Boolean.TRUE);
		eWeightsBool3.set(1, Boolean.FALSE);
		eWeightsBool4.set(0, Boolean.FALSE);
		eWeightsChar1.set(0, 'B');
		eWeightsChar1.set(1, 'C');
		eWeightsChar2.set(0, 'D');
		eWeightsChar3.set(0, Character.valueOf('E'));
		eWeightsChar3.set(1, Character.valueOf('F'));
		eWeightsChar4.set(0, Character.valueOf('G'));
		eWeightsObj1.set(0, "63");
		eWeightsObj1.set(1, "");
		eWeightsObj2.set(0, "64");

		GraphMlGraphWriter<Integer, Integer> writer = new GraphMlGraphWriter<>();
		StringWriter out = new StringWriter();
		writer.writeGraph(g, out);
		String text = out.toString();

		GraphMlGraphReader<Integer, Integer> reader = new GraphMlGraphReader<>(int.class, int.class);
		IntGraph g1 = (IntGraph) reader.readGraph(new StringReader(text));

		assertEquals(g.vertices(), g1.vertices());
		assertEquals(g.edges(), g1.edges());

		IWeightsInt vWeightsByte1_2 = g1.getVerticesWeights("v-weights-byte1");
		IWeightsInt vWeightsByte2_2 = g1.getVerticesWeights("v-weights-byte2");
		IWeightsInt vWeightsByte2_3 = g1.getVerticesWeights("v-weights-byte3");
		IWeightsInt vWeightsByte2_4 = g1.getVerticesWeights("v-weights-byte4");
		IWeightsInt vWeightsShort1_2 = g1.getVerticesWeights("v-weights-short1");
		IWeightsInt vWeightsShort2_2 = g1.getVerticesWeights("v-weights-short2");
		IWeightsInt vWeightsShort2_3 = g1.getVerticesWeights("v-weights-short3");
		IWeightsInt vWeightsShort2_4 = g1.getVerticesWeights("v-weights-short4");
		IWeightsInt vWeightsInt1_2 = g1.getVerticesWeights("v-weights-int1");
		IWeightsInt vWeightsInt2_2 = g1.getVerticesWeights("v-weights-int2");
		IWeightsInt vWeightsInt2_3 = g1.getVerticesWeights("v-weights-int3");
		IWeightsInt vWeightsInt2_4 = g1.getVerticesWeights("v-weights-int4");
		IWeightsLong vWeightsLong1_2 = g1.getVerticesWeights("v-weights-long1");
		IWeightsLong vWeightsLong2_2 = g1.getVerticesWeights("v-weights-long2");
		IWeightsLong vWeightsLong2_3 = g1.getVerticesWeights("v-weights-long3");
		IWeightsLong vWeightsLong2_4 = g1.getVerticesWeights("v-weights-long4");
		IWeightsFloat vWeightsFloat1_2 = g1.getVerticesWeights("v-weights-float1");
		IWeightsFloat vWeightsFloat2_2 = g1.getVerticesWeights("v-weights-float2");
		IWeightsFloat vWeightsFloat2_3 = g1.getVerticesWeights("v-weights-float3");
		IWeightsFloat vWeightsFloat2_4 = g1.getVerticesWeights("v-weights-float4");
		IWeightsDouble vWeightsDouble1_2 = g1.getVerticesWeights("v-weights-double1");
		IWeightsDouble vWeightsDouble2_2 = g1.getVerticesWeights("v-weights-double2");
		IWeightsDouble vWeightsDouble2_3 = g1.getVerticesWeights("v-weights-double3");
		IWeightsDouble vWeightsDouble2_4 = g1.getVerticesWeights("v-weights-double4");
		IWeightsBool vWeightsBool1_2 = g1.getVerticesWeights("v-weights-bool1");
		IWeightsBool vWeightsBool2_2 = g1.getVerticesWeights("v-weights-bool2");
		IWeightsBool vWeightsBool2_3 = g1.getVerticesWeights("v-weights-bool3");
		IWeightsBool vWeightsBool2_4 = g1.getVerticesWeights("v-weights-bool4");
		IWeightsObj<String> vWeightsChar1_2 = g1.getVerticesWeights("v-weights-char1");
		IWeightsObj<String> vWeightsChar2_2 = g1.getVerticesWeights("v-weights-char2");
		IWeightsObj<String> vWeightsChar2_3 = g1.getVerticesWeights("v-weights-char3");
		IWeightsObj<String> vWeightsChar2_4 = g1.getVerticesWeights("v-weights-char4");
		IWeightsObj<String> vWeightsObj1_2 = g1.getVerticesWeights("v-weights-obj1");
		IWeightsObj<String> vWeightsObj2_2 = g1.getVerticesWeights("v-weights-obj2");
		for (int v : g.vertices()) {
			assertEquals(vWeightsByte1.get(v), vWeightsByte1_2.get(v));
			assertEquals(vWeightsByte2.get(v), vWeightsByte2_2.get(v));
			assertEquals(vWeightsByte3.get(v).intValue(), vWeightsByte2_3.get(v));
			assertEquals(vWeightsByte4.get(v).intValue(), vWeightsByte2_4.get(v));
			assertEquals(vWeightsShort1.get(v), vWeightsShort1_2.get(v));
			assertEquals(vWeightsShort2.get(v), vWeightsShort2_2.get(v));
			assertEquals(vWeightsShort3.get(v).intValue(), vWeightsShort2_3.get(v));
			assertEquals(vWeightsShort4.get(v).intValue(), vWeightsShort2_4.get(v));
			assertEquals(vWeightsInt1.get(v), vWeightsInt1_2.get(v));
			assertEquals(vWeightsInt2.get(v), vWeightsInt2_2.get(v));
			assertEquals(vWeightsInt3.get(v), Integer.valueOf(vWeightsInt2_3.get(v)));
			assertEquals(vWeightsInt4.get(v), Integer.valueOf(vWeightsInt2_4.get(v)));
			assertEquals(vWeightsLong1.get(v), vWeightsLong1_2.get(v));
			assertEquals(vWeightsLong2.get(v), vWeightsLong2_2.get(v));
			assertEquals(vWeightsLong3.get(v), Long.valueOf(vWeightsLong2_3.get(v)));
			assertEquals(vWeightsLong4.get(v), Long.valueOf(vWeightsLong2_4.get(v)));
			assertEquals(vWeightsFloat1.get(v), vWeightsFloat1_2.get(v));
			assertEquals(vWeightsFloat2.get(v), vWeightsFloat2_2.get(v));
			assertEquals(vWeightsFloat3.get(v), Float.valueOf(vWeightsFloat2_3.get(v)));
			assertEquals(vWeightsFloat4.get(v), Float.valueOf(vWeightsFloat2_4.get(v)));
			assertEquals(vWeightsDouble1.get(v), vWeightsDouble1_2.get(v));
			assertEquals(vWeightsDouble2.get(v), vWeightsDouble2_2.get(v));
			assertEquals(vWeightsDouble3.get(v), Double.valueOf(vWeightsDouble2_3.get(v)));
			assertEquals(vWeightsDouble4.get(v), Double.valueOf(vWeightsDouble2_4.get(v)));
			assertEquals(vWeightsBool1.get(v), vWeightsBool1_2.get(v));
			assertEquals(vWeightsBool2.get(v), vWeightsBool2_2.get(v));
			assertEquals(vWeightsBool3.get(v), Boolean.valueOf(vWeightsBool2_3.get(v)));
			assertEquals(vWeightsBool4.get(v), Boolean.valueOf(vWeightsBool2_4.get(v)));
			assertEquals("" + vWeightsChar1.get(v), vWeightsChar1_2.get(v));
			assertEquals("" + vWeightsChar2.get(v), vWeightsChar2_2.get(v));
			assertEquals("" + vWeightsChar3.get(v), vWeightsChar2_3.get(v));
			assertEquals("" + vWeightsChar4.get(v), vWeightsChar2_4.get(v));
			assertEquals(vWeightsObj1.get(v), vWeightsObj1_2.get(v));
			assertEquals(vWeightsObj2.get(v), vWeightsObj2_2.get(v));
		}

		IWeightsInt eWeightsByte1_2 = g1.getEdgesWeights("e-weights-byte1");
		IWeightsInt eWeightsByte2_2 = g1.getEdgesWeights("e-weights-byte2");
		IWeightsInt eWeightsByte2_3 = g1.getEdgesWeights("e-weights-byte3");
		IWeightsInt eWeightsByte2_4 = g1.getEdgesWeights("e-weights-byte4");
		IWeightsInt eWeightsShort1_2 = g1.getEdgesWeights("e-weights-short1");
		IWeightsInt eWeightsShort2_2 = g1.getEdgesWeights("e-weights-short2");
		IWeightsInt eWeightsShort2_3 = g1.getEdgesWeights("e-weights-short3");
		IWeightsInt eWeightsShort2_4 = g1.getEdgesWeights("e-weights-short4");
		IWeightsInt eWeightsInt1_2 = g1.getEdgesWeights("e-weights-int1");
		IWeightsInt eWeightsInt2_2 = g1.getEdgesWeights("e-weights-int2");
		IWeightsInt eWeightsInt2_3 = g1.getEdgesWeights("e-weights-int3");
		IWeightsInt eWeightsInt2_4 = g1.getEdgesWeights("e-weights-int4");
		IWeightsLong eWeightsLong1_2 = g1.getEdgesWeights("e-weights-long1");
		IWeightsLong eWeightsLong2_2 = g1.getEdgesWeights("e-weights-long2");
		IWeightsLong eWeightsLong2_3 = g1.getEdgesWeights("e-weights-long3");
		IWeightsLong eWeightsLong2_4 = g1.getEdgesWeights("e-weights-long4");
		IWeightsFloat eWeightsFloat1_2 = g1.getEdgesWeights("e-weights-float1");
		IWeightsFloat eWeightsFloat2_2 = g1.getEdgesWeights("e-weights-float2");
		IWeightsFloat eWeightsFloat2_3 = g1.getEdgesWeights("e-weights-float3");
		IWeightsFloat eWeightsFloat2_4 = g1.getEdgesWeights("e-weights-float4");
		IWeightsDouble eWeightsDouble1_2 = g1.getEdgesWeights("e-weights-double1");
		IWeightsDouble eWeightsDouble2_2 = g1.getEdgesWeights("e-weights-double2");
		IWeightsDouble eWeightsDouble2_3 = g1.getEdgesWeights("e-weights-double3");
		IWeightsDouble eWeightsDouble2_4 = g1.getEdgesWeights("e-weights-double4");
		IWeightsBool eWeightsBool1_2 = g1.getEdgesWeights("e-weights-bool1");
		IWeightsBool eWeightsBool2_2 = g1.getEdgesWeights("e-weights-bool2");
		IWeightsBool eWeightsBool2_3 = g1.getEdgesWeights("e-weights-bool3");
		IWeightsBool eWeightsBool2_4 = g1.getEdgesWeights("e-weights-bool4");
		IWeightsObj<String> eWeightsChar1_2 = g1.getEdgesWeights("e-weights-char1");
		IWeightsObj<String> eWeightsChar2_2 = g1.getEdgesWeights("e-weights-char2");
		IWeightsObj<String> eWeightsChar2_3 = g1.getEdgesWeights("e-weights-char3");
		IWeightsObj<String> eWeightsChar2_4 = g1.getEdgesWeights("e-weights-char4");
		IWeightsObj<String> eWeightsObj1_2 = g1.getEdgesWeights("e-weights-obj1");
		IWeightsObj<String> eWeightsObj2_2 = g1.getEdgesWeights("e-weights-obj2");
		for (int e : g.edges()) {
			assertEquals(eWeightsByte1.get(e), eWeightsByte1_2.get(e));
			assertEquals(eWeightsByte2.get(e), eWeightsByte2_2.get(e));
			assertEquals(eWeightsByte3.get(e).intValue(), eWeightsByte2_3.get(e));
			assertEquals(eWeightsByte4.get(e).intValue(), eWeightsByte2_4.get(e));
			assertEquals(eWeightsShort1.get(e), eWeightsShort1_2.get(e));
			assertEquals(eWeightsShort2.get(e), eWeightsShort2_2.get(e));
			assertEquals(eWeightsShort3.get(e).intValue(), eWeightsShort2_3.get(e));
			assertEquals(eWeightsShort4.get(e).intValue(), eWeightsShort2_4.get(e));
			assertEquals(eWeightsInt1.get(e), eWeightsInt1_2.get(e));
			assertEquals(eWeightsInt2.get(e), eWeightsInt2_2.get(e));
			assertEquals(eWeightsInt3.get(e), Integer.valueOf(eWeightsInt2_3.get(e)));
			assertEquals(eWeightsInt4.get(e), Integer.valueOf(eWeightsInt2_4.get(e)));
			assertEquals(eWeightsLong1.get(e), eWeightsLong1_2.get(e));
			assertEquals(eWeightsLong2.get(e), eWeightsLong2_2.get(e));
			assertEquals(eWeightsLong3.get(e), Long.valueOf(eWeightsLong2_3.get(e)));
			assertEquals(eWeightsLong4.get(e), Long.valueOf(eWeightsLong2_4.get(e)));
			assertEquals(eWeightsFloat1.get(e), eWeightsFloat1_2.get(e));
			assertEquals(eWeightsFloat2.get(e), eWeightsFloat2_2.get(e));
			assertEquals(eWeightsFloat3.get(e), Float.valueOf(eWeightsFloat2_3.get(e)));
			assertEquals(eWeightsFloat4.get(e), Float.valueOf(eWeightsFloat2_4.get(e)));
			assertEquals(eWeightsDouble1.get(e), eWeightsDouble1_2.get(e));
			assertEquals(eWeightsDouble2.get(e), eWeightsDouble2_2.get(e));
			assertEquals(eWeightsDouble3.get(e), Double.valueOf(eWeightsDouble2_3.get(e)));
			assertEquals(eWeightsDouble4.get(e), Double.valueOf(eWeightsDouble2_4.get(e)));
			assertEquals(eWeightsBool1.get(e), eWeightsBool1_2.get(e));
			assertEquals(eWeightsBool2.get(e), eWeightsBool2_2.get(e));
			assertEquals(eWeightsBool3.get(e), Boolean.valueOf(eWeightsBool2_3.get(e)));
			assertEquals(eWeightsBool4.get(e), Boolean.valueOf(eWeightsBool2_4.get(e)));
			assertEquals("" + eWeightsChar1.get(e), eWeightsChar1_2.get(e));
			assertEquals("" + eWeightsChar2.get(e), eWeightsChar2_2.get(e));
			assertEquals("" + eWeightsChar3.get(e), eWeightsChar2_3.get(e));
			assertEquals("" + eWeightsChar4.get(e), eWeightsChar2_4.get(e));
			assertEquals(eWeightsObj1.get(e), eWeightsObj1_2.get(e));
			assertEquals(eWeightsObj2.get(e), eWeightsObj2_2.get(e));
		}
	}

	@Test
	public void readIntGraph() {
		TextBuilder text = new TextBuilder();
		text.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		text.addLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
		text.addLine("  <key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"int\"/>\n");
		text.addLine("  <graph id=\"G\" edgedefault=\"directed\">\n");
		text.addLine("    <node id=\"0\"/>\n");
		text.addLine("    <node id=\"1\"/>\n");
		text.addLine("    <edge id=\"0\" source=\"0\" target=\"1\"/>\n");
		text.addLine("  </graph>\n");
		text.addLine("</graphml>\n");

		assertTrue(new GraphMlGraphReader<>(int.class, int.class)
				.readGraph(new StringReader(text.get())) instanceof IntGraph);
		assertFalse(new GraphMlGraphReader<>(Integer.class, Integer.class)
				.readGraph(new StringReader(text.get())) instanceof IntGraph);
		assertFalse(new GraphMlGraphReader<>(Integer.class, int.class)
				.readGraph(new StringReader(text.get())) instanceof IntGraph);
		assertFalse(new GraphMlGraphReader<>(int.class, Integer.class)
				.readGraph(new StringReader(text.get())) instanceof IntGraph);
	}

	@Test
	public void writeWeightsVerticesUnsupported() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);

		IWeightsObj<IntGraph> weights = g.addVerticesWeights("weight", IntGraph.class);
		weights.set(0, IntGraph.newDirected());

		GraphMlGraphWriter<Integer, Integer> writer = new GraphMlGraphWriter<>();
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

		GraphMlGraphWriter<Integer, Integer> writer = new GraphMlGraphWriter<>();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

}
