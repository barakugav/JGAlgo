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
				final int m = n + rand.nextInt(2 * n);
				IntGraphFactory factory = directed ? IntGraphFactory.newDirected() : IntGraphFactory.newUndirected();
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
		IWeightsShort vWeightsShort1 = g.addVerticesWeights("v-weights-short1", short.class);
		IWeightsShort vWeightsShort2 = g.addVerticesWeights("v-weights-short2", short.class, Short.valueOf((short) 2));
		IWeightsInt vWeightsInt1 = g.addVerticesWeights("v-weights-int1", int.class);
		IWeightsInt vWeightsInt2 = g.addVerticesWeights("v-weights-int2", int.class, Integer.valueOf(3));
		IWeightsLong vWeightsLong1 = g.addVerticesWeights("v-weights-long1", long.class);
		IWeightsLong vWeightsLong2 = g.addVerticesWeights("v-weights-long2", long.class, Long.valueOf(4));
		IWeightsFloat vWeightsFloat1 = g.addVerticesWeights("v-weights-float1", float.class);
		IWeightsFloat vWeightsFloat2 = g.addVerticesWeights("v-weights-float2", float.class, Float.valueOf(5.5f));
		IWeightsDouble vWeightsDouble1 = g.addVerticesWeights("v-weights-double1", double.class);
		IWeightsDouble vWeightsDouble2 = g.addVerticesWeights("v-weights-double2", double.class, Double.valueOf(6.6));
		IWeightsBool vWeightsBool1 = g.addVerticesWeights("v-weights-bool1", boolean.class);
		IWeightsBool vWeightsBool2 = g.addVerticesWeights("v-weights-bool2", boolean.class, Boolean.TRUE);
		IWeightsChar vWeightsChar1 = g.addVerticesWeights("v-weights-char1", char.class, Character.valueOf(' '));
		IWeightsChar vWeightsChar2 = g.addVerticesWeights("v-weights-char2", char.class, Character.valueOf('7'));
		IWeightsObj<String> vWeightsObj1 = g.addVerticesWeights("v-weights-obj1", String.class);
		IWeightsObj<String> vWeightsObj2 = g.addVerticesWeights("v-weights-obj2", String.class, "8");
		vWeightsByte1.set(0, (byte) 31);
		vWeightsByte2.set(0, (byte) 32);
		vWeightsShort1.set(0, (short) 33);
		vWeightsShort2.set(0, (short) 34);
		vWeightsInt1.set(0, 35);
		vWeightsInt2.set(0, 36);
		vWeightsLong1.set(0, 37);
		vWeightsLong2.set(0, 38);
		vWeightsFloat1.set(0, 39.39f);
		vWeightsFloat2.set(0, 40.40f);
		vWeightsDouble1.set(0, 41.41);
		vWeightsDouble2.set(0, 42.42);
		vWeightsBool1.set(0, true);
		vWeightsBool2.set(0, false);
		vWeightsChar1.set(0, '9');
		vWeightsChar2.set(0, 'A');
		vWeightsObj1.set(0, "43");
		vWeightsObj2.set(0, "44");

		IWeightsByte eWeightsByte1 = g.addEdgesWeights("e-weights-byte1", byte.class);
		IWeightsByte eWeightsByte2 = g.addEdgesWeights("e-weights-byte2", byte.class, Byte.valueOf((byte) 11));
		IWeightsShort eWeightsShort1 = g.addEdgesWeights("e-weights-short1", short.class);
		IWeightsShort eWeightsShort2 = g.addEdgesWeights("e-weights-short2", short.class, Short.valueOf((short) 12));
		IWeightsInt eWeightsInt1 = g.addEdgesWeights("e-weights-int1", int.class);
		IWeightsInt eWeightsInt2 = g.addEdgesWeights("e-weights-int2", int.class, Integer.valueOf(13));
		IWeightsLong eWeightsLong1 = g.addEdgesWeights("e-weights-long1", long.class);
		IWeightsLong eWeightsLong2 = g.addEdgesWeights("e-weights-long2", long.class, Long.valueOf(14));
		IWeightsFloat eWeightsFloat1 = g.addEdgesWeights("e-weights-float1", float.class);
		IWeightsFloat eWeightsFloat2 = g.addEdgesWeights("e-weights-float2", float.class, Float.valueOf(15.15f));
		IWeightsDouble eWeightsDouble1 = g.addEdgesWeights("e-weights-double1", double.class);
		IWeightsDouble eWeightsDouble2 = g.addEdgesWeights("e-weights-double2", double.class, Double.valueOf(16.16));
		IWeightsBool eWeightsBool1 = g.addEdgesWeights("e-weights-bool1", boolean.class);
		IWeightsBool eWeightsBool2 = g.addEdgesWeights("e-weights-bool2", boolean.class, Boolean.TRUE);
		IWeightsChar eWeightsChar1 = g.addEdgesWeights("e-weights-char1", char.class, Character.valueOf(' '));
		IWeightsChar eWeightsChar2 = g.addEdgesWeights("e-weights-char2", char.class, Character.valueOf('*'));
		IWeightsObj<String> eWeightsObj1 = g.addEdgesWeights("e-weights-obj1", String.class);
		IWeightsObj<String> eWeightsObj2 = g.addEdgesWeights("e-weights-obj2", String.class, "17");
		eWeightsByte1.set(0, (byte) 51);
		eWeightsByte2.set(0, (byte) 52);
		eWeightsShort1.set(0, (short) 53);
		eWeightsShort2.set(0, (short) 54);
		eWeightsInt1.set(0, 55);
		eWeightsInt2.set(0, 56);
		eWeightsLong1.set(0, 57);
		eWeightsLong2.set(0, 58);
		eWeightsFloat1.set(0, 59.59f);
		eWeightsFloat2.set(0, 60.60f);
		eWeightsDouble1.set(0, 61.61);
		eWeightsDouble2.set(0, 62.62);
		eWeightsBool1.set(0, true);
		eWeightsBool2.set(0, false);
		eWeightsChar1.set(0, 'C');
		eWeightsChar2.set(0, 'D');
		eWeightsObj1.set(0, "63");
		eWeightsObj2.set(0, "64");

		GraphMlGraphWriter<Integer, Integer> writer = new GraphMlGraphWriter<>();
		StringWriter out = new StringWriter();
		writer.writeGraph(g, out);
		String text = out.toString();

		GraphMlGraphReader<Integer, Integer> reader = new GraphMlGraphReader<>(int.class, int.class);
		IntGraph g1 = (IntGraph) reader.readGraph(new StringReader(text));

		assertEquals(g.vertices(), g1.vertices());
		assertEquals(g.edges(), g1.edges());

		IWeightsInt vWeightsByte3 = g1.getVerticesWeights("v-weights-byte1");
		IWeightsInt vWeightsByte4 = g1.getVerticesWeights("v-weights-byte2");
		IWeightsInt vWeightsShort3 = g1.getVerticesWeights("v-weights-short1");
		IWeightsInt vWeightsShort4 = g1.getVerticesWeights("v-weights-short2");
		IWeightsInt vWeightsInt3 = g1.getVerticesWeights("v-weights-int1");
		IWeightsInt vWeightsInt4 = g1.getVerticesWeights("v-weights-int2");
		IWeightsLong vWeightsLong3 = g1.getVerticesWeights("v-weights-long1");
		IWeightsLong vWeightsLong4 = g1.getVerticesWeights("v-weights-long2");
		IWeightsFloat vWeightsFloat3 = g1.getVerticesWeights("v-weights-float1");
		IWeightsFloat vWeightsFloat4 = g1.getVerticesWeights("v-weights-float2");
		IWeightsDouble vWeightsDouble3 = g1.getVerticesWeights("v-weights-double1");
		IWeightsDouble vWeightsDouble4 = g1.getVerticesWeights("v-weights-double2");
		IWeightsBool vWeightsBool3 = g1.getVerticesWeights("v-weights-bool1");
		IWeightsBool vWeightsBool4 = g1.getVerticesWeights("v-weights-bool2");
		IWeightsObj<String> vWeightsChar3 = g1.getVerticesWeights("v-weights-char1");
		IWeightsObj<String> vWeightsChar4 = g1.getVerticesWeights("v-weights-char2");
		IWeightsObj<String> vWeightsObj3 = g1.getVerticesWeights("v-weights-obj1");
		IWeightsObj<String> vWeightsObj4 = g1.getVerticesWeights("v-weights-obj2");
		assertEquals(vWeightsByte1.get(0), vWeightsByte3.get(0));
		assertEquals(vWeightsByte1.get(1), vWeightsByte3.get(1));
		assertEquals(vWeightsByte2.get(0), vWeightsByte4.get(0));
		assertEquals(vWeightsByte2.get(1), vWeightsByte4.get(1));
		assertEquals(vWeightsShort1.get(0), vWeightsShort3.get(0));
		assertEquals(vWeightsShort1.get(1), vWeightsShort3.get(1));
		assertEquals(vWeightsShort2.get(0), vWeightsShort4.get(0));
		assertEquals(vWeightsShort2.get(1), vWeightsShort4.get(1));
		assertEquals(vWeightsInt1.get(0), vWeightsInt3.get(0));
		assertEquals(vWeightsInt1.get(1), vWeightsInt3.get(1));
		assertEquals(vWeightsInt2.get(0), vWeightsInt4.get(0));
		assertEquals(vWeightsInt2.get(1), vWeightsInt4.get(1));
		assertEquals(vWeightsLong1.get(0), vWeightsLong3.get(0));
		assertEquals(vWeightsLong1.get(1), vWeightsLong3.get(1));
		assertEquals(vWeightsLong2.get(0), vWeightsLong4.get(0));
		assertEquals(vWeightsLong2.get(1), vWeightsLong4.get(1));
		assertEquals(vWeightsFloat1.get(0), vWeightsFloat3.get(0));
		assertEquals(vWeightsFloat1.get(1), vWeightsFloat3.get(1));
		assertEquals(vWeightsFloat2.get(0), vWeightsFloat4.get(0));
		assertEquals(vWeightsFloat2.get(1), vWeightsFloat4.get(1));
		assertEquals(vWeightsDouble1.get(0), vWeightsDouble3.get(0));
		assertEquals(vWeightsDouble1.get(1), vWeightsDouble3.get(1));
		assertEquals(vWeightsDouble2.get(0), vWeightsDouble4.get(0));
		assertEquals(vWeightsDouble2.get(1), vWeightsDouble4.get(1));
		assertEquals(vWeightsBool1.get(0), vWeightsBool3.get(0));
		assertEquals(vWeightsBool1.get(1), vWeightsBool3.get(1));
		assertEquals(vWeightsBool2.get(0), vWeightsBool4.get(0));
		assertEquals(vWeightsBool2.get(1), vWeightsBool4.get(1));
		assertEquals("" + vWeightsChar1.get(0), vWeightsChar3.get(0));
		assertEquals("" + vWeightsChar1.get(1), vWeightsChar3.get(1));
		assertEquals("" + vWeightsChar2.get(0), vWeightsChar4.get(0));
		assertEquals("" + vWeightsChar2.get(1), vWeightsChar4.get(1));
		assertEquals(vWeightsObj1.get(0), vWeightsObj3.get(0));
		assertEquals(vWeightsObj1.get(1), vWeightsObj3.get(1));
		assertEquals(vWeightsObj2.get(0), vWeightsObj4.get(0));
		assertEquals(vWeightsObj2.get(1), vWeightsObj4.get(1));

		IWeightsInt eWeightsByte3 = g1.getEdgesWeights("e-weights-byte1");
		IWeightsInt eWeightsByte4 = g1.getEdgesWeights("e-weights-byte2");
		IWeightsInt eWeightsShort3 = g1.getEdgesWeights("e-weights-short1");
		IWeightsInt eWeightsShort4 = g1.getEdgesWeights("e-weights-short2");
		IWeightsInt eWeightsInt3 = g1.getEdgesWeights("e-weights-int1");
		IWeightsInt eWeightsInt4 = g1.getEdgesWeights("e-weights-int2");
		IWeightsLong eWeightsLong3 = g1.getEdgesWeights("e-weights-long1");
		IWeightsLong eWeightsLong4 = g1.getEdgesWeights("e-weights-long2");
		IWeightsFloat eWeightsFloat3 = g1.getEdgesWeights("e-weights-float1");
		IWeightsFloat eWeightsFloat4 = g1.getEdgesWeights("e-weights-float2");
		IWeightsDouble eWeightsDouble3 = g1.getEdgesWeights("e-weights-double1");
		IWeightsDouble eWeightsDouble4 = g1.getEdgesWeights("e-weights-double2");
		IWeightsBool eWeightsBool3 = g1.getEdgesWeights("e-weights-bool1");
		IWeightsBool eWeightsBool4 = g1.getEdgesWeights("e-weights-bool2");
		IWeightsObj<String> eWeightsChar3 = g1.getEdgesWeights("e-weights-char1");
		IWeightsObj<String> eWeightsChar4 = g1.getEdgesWeights("e-weights-char2");
		IWeightsObj<String> eWeightsObj3 = g1.getEdgesWeights("e-weights-obj1");
		IWeightsObj<String> eWeightsObj4 = g1.getEdgesWeights("e-weights-obj2");
		assertEquals(eWeightsByte1.get(0), eWeightsByte3.get(0));
		assertEquals(eWeightsByte1.get(1), eWeightsByte3.get(1));
		assertEquals(eWeightsByte2.get(0), eWeightsByte4.get(0));
		assertEquals(eWeightsByte2.get(1), eWeightsByte4.get(1));
		assertEquals(eWeightsShort1.get(0), eWeightsShort3.get(0));
		assertEquals(eWeightsShort1.get(1), eWeightsShort3.get(1));
		assertEquals(eWeightsShort2.get(0), eWeightsShort4.get(0));
		assertEquals(eWeightsShort2.get(1), eWeightsShort4.get(1));
		assertEquals(eWeightsInt1.get(0), eWeightsInt3.get(0));
		assertEquals(eWeightsInt1.get(1), eWeightsInt3.get(1));
		assertEquals(eWeightsInt2.get(0), eWeightsInt4.get(0));
		assertEquals(eWeightsInt2.get(1), eWeightsInt4.get(1));
		assertEquals(eWeightsLong1.get(0), eWeightsLong3.get(0));
		assertEquals(eWeightsLong1.get(1), eWeightsLong3.get(1));
		assertEquals(eWeightsLong2.get(0), eWeightsLong4.get(0));
		assertEquals(eWeightsLong2.get(1), eWeightsLong4.get(1));
		assertEquals(eWeightsFloat1.get(0), eWeightsFloat3.get(0));
		assertEquals(eWeightsFloat1.get(1), eWeightsFloat3.get(1));
		assertEquals(eWeightsFloat2.get(0), eWeightsFloat4.get(0));
		assertEquals(eWeightsFloat2.get(1), eWeightsFloat4.get(1));
		assertEquals(eWeightsDouble1.get(0), eWeightsDouble3.get(0));
		assertEquals(eWeightsDouble1.get(1), eWeightsDouble3.get(1));
		assertEquals(eWeightsDouble2.get(0), eWeightsDouble4.get(0));
		assertEquals(eWeightsDouble2.get(1), eWeightsDouble4.get(1));
		assertEquals(eWeightsBool1.get(0), eWeightsBool3.get(0));
		assertEquals(eWeightsBool1.get(1), eWeightsBool3.get(1));
		assertEquals(eWeightsBool2.get(0), eWeightsBool4.get(0));
		assertEquals(eWeightsBool2.get(1), eWeightsBool4.get(1));
		assertEquals("" + eWeightsChar1.get(0), eWeightsChar3.get(0));
		assertEquals("" + eWeightsChar1.get(1), eWeightsChar3.get(1));
		assertEquals("" + eWeightsChar2.get(0), eWeightsChar4.get(0));
		assertEquals("" + eWeightsChar2.get(1), eWeightsChar4.get(1));
		assertEquals(eWeightsObj1.get(0), eWeightsObj3.get(0));
		assertEquals(eWeightsObj1.get(1), eWeightsObj3.get(1));
		assertEquals(eWeightsObj2.get(0), eWeightsObj4.get(0));
		assertEquals(eWeightsObj2.get(1), eWeightsObj4.get(1));
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

}
