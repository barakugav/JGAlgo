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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
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
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsObj;
import it.unimi.dsi.fastutil.Pair;

public class GmlTest {

	@Test
	public void parseSimpleGraph() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    label \"Node A\"");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 3");
		text.addLine("    label \"Node B\"");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 5");
		text.addLine("    label \"Node C\"");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 3");
		text.addLine("    target 1");
		text.addLine("    label \"Edge B to A\"");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 5");
		text.addLine("    target 1");
		text.addLine("    label \"Edge C to A\"");
		text.addLine("  ]");
		text.addLine("]");
		new GmlGraphReader<>(int.class, int.class).readGraph(new StringReader(text.getAndClear()));
	}

	@Test
	public void writeAndReadRandomGraphs() {
		final long seed = 0xaf7128f601c98d2cL;
		Random rand = new Random(seed);
		for (int repeat = 0; repeat < 32; repeat++) {
			final int n = 10 + rand.nextInt(20);
			final int m = 15 + rand.nextInt(30);
			IntGraph g = IntGraphFactory.undirected().allowSelfEdges().newGraph();

			while (g.vertices().size() < n) {
				int v = rand.nextInt(n * 3);
				if (!g.vertices().contains(v))
					g.addVertex(v);
			}

			while (g.edges().size() < m) {
				int source = Graphs.randVertex(g, rand);
				int target = Graphs.randVertex(g, rand);
				int e = rand.nextInt(m * 3);
				if (!g.edges().contains(e))
					g.addEdge(source, target, e);
			}

			StringWriter writer = new StringWriter();
			new GmlGraphWriter<Integer, Integer>().writeGraph(g, writer);
			String data = writer.toString();

			GraphBuilder<Integer, Integer> gb =
					new GmlGraphReader<>(int.class, int.class).readIntoBuilder(new StringReader(data));
			Graph<Integer, Integer> gImmutable = gb.build();
			Graph<Integer, Integer> gMutable = gb.buildMutable();
			assertEquals(g, gImmutable);
			assertEquals(g, gMutable);
		}
	}

	@Test
	public void writeAndReadRandomGraphsWithWeights() {
		final long seed = 0xbe0b2f7e6b3ac164L;
		Random rand = new Random(seed);
		for (int repeat = 0; repeat < 32; repeat++) {
			final int n = 10 + rand.nextInt(20);
			final int m = 15 + rand.nextInt(30);
			IntGraph g = IntGraphFactory.undirected().allowSelfEdges().newGraph();

			while (g.vertices().size() < n) {
				int v = rand.nextInt(n * 3);
				if (!g.vertices().contains(v))
					g.addVertex(v);
			}

			while (g.edges().size() < m) {
				int source = Graphs.randVertex(g, rand);
				int target = Graphs.randVertex(g, rand);
				int e = rand.nextInt(m * 3);
				if (!g.edges().contains(e))
					g.addEdge(source, target, e);
			}

			IWeightsInt wv1 = g.addVerticesWeights("v1", int.class);
			IWeightsDouble wv2 = g.addVerticesWeights("v2", double.class);
			IWeightsObj<String> wv3 = g.addVerticesWeights("v3", String.class);
			for (int v : g.vertices()) {
				wv1.set(v, n + rand.nextInt(n * 3));
				wv2.set(v, n + rand.nextDouble());
				wv3.set(v, Character.toString('a' + rand.nextInt('z' - 'a' + 1)));
			}

			IWeightsInt we1 = g.addEdgesWeights("e1", int.class);
			IWeightsDouble we2 = g.addEdgesWeights("e2", double.class);
			IWeightsObj<String> we3 = g.addEdgesWeights("e3", String.class);
			for (int e : g.edges()) {
				we1.set(e, n + rand.nextInt(m * 3));
				we2.set(e, n + rand.nextDouble());
				we3.set(e, Character.toString('a' + rand.nextInt('z' - 'a' + 1)));
			}

			StringWriter writer = new StringWriter();
			new GmlGraphWriter<Integer, Integer>().writeGraph(g, writer);
			String data = writer.toString();

			GraphBuilder<Integer, Integer> gb =
					new GmlGraphReader<>(int.class, int.class).readIntoBuilder(new StringReader(data));
			Graph<Integer, Integer> gImmutable = gb.build();
			Graph<Integer, Integer> gMutable = gb.buildMutable();
			assertEquals(g, gImmutable);
			assertEquals(g, gMutable);
		}
	}

	@Test
	public void readDirectedUndirected() {
		TextBuilder text = new TextBuilder();
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);

		/* if not specified, graph is undirected */
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		assertFalse(reader.readGraph(new StringReader(text.getAndClear())).isDirected());

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear())).isDirected());

		text.addLine("graph [");
		text.addLine("  directed 0");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		assertFalse(reader.readGraph(new StringReader(text.getAndClear())).isDirected());
	}

	@Test
	public void readWithoutVerticesEdgesTypes() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");

		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>();
		assertThrows(IllegalStateException.class, () -> reader.readGraph(new StringReader(text.get())));

		reader.setVertexType(int.class);
		assertThrows(IllegalStateException.class, () -> reader.readGraph(new StringReader(text.get())));

		reader.setEdgeType(int.class);
		assertNotNull(reader.readGraph(new StringReader(text.get())));
	}

	@Test
	public void readerInvalidVerticesEdgesType() {
		assertThrows(NullPointerException.class, () -> new GmlGraphReader<>(null, int.class));
		assertThrows(NullPointerException.class, () -> new GmlGraphReader<>(int.class, null));
		assertThrows(IllegalArgumentException.class, () -> new GmlGraphReader<>(boolean.class, int.class));
		assertThrows(IllegalArgumentException.class, () -> new GmlGraphReader<>(int.class, boolean.class));

		GmlGraphReader<Boolean, Boolean> reader = new GmlGraphReader<>();
		assertThrows(NullPointerException.class, () -> reader.setVertexType(null));
		assertThrows(NullPointerException.class, () -> reader.setEdgeType(null));
		assertThrows(IllegalArgumentException.class, () -> reader.setVertexType(boolean.class));
		assertThrows(IllegalArgumentException.class, () -> reader.setEdgeType(boolean.class));
	}

	@Test
	public void readMultipleGraphs() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.get() + text.get())));
	}

	@Test
	public void readNonGraphRoot() {
		TextBuilder text = new TextBuilder();
		text.addLine("graphwrapper [");
		text.addLine("  graph [");
		text.addLine("    node [");
		text.addLine("      id 1");
		text.addLine("    ]");
		text.addLine("    node [");
		text.addLine("      id 2");
		text.addLine("    ]");
		text.addLine("    edge [");
		text.addLine("      source 1");
		text.addLine("      target 2");
		text.addLine("    ]");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("notagraph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readVertexWhichIsNotAList() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node 1");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(Integer.class, Integer.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readVertexWithNestedDataList() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    datalist [");
		text.addLine("      data 3");
		text.addLine("    ]");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(Integer.class, Integer.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeWithNestedDataList() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("    datalist [");
		text.addLine("      data 3");
		text.addLine("    ]");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(Integer.class, Integer.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readVertexWithNoId() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(Integer.class, Integer.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readVertexDuplicateId() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(Integer.class, Integer.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readVertexIdDifferentClass() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id \"vertex-id1\"");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(Integer.class, Integer.class);
		assertThrows(ClassCastException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeWithNoId() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(Integer.class, Integer.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeWithNoIdIntGraph() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertNotNull(reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id 1");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    source 2");
		text.addLine("    target 1");
		text.addLine("  ]");
		text.addLine("]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeDuplicateId() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id 1");
		text.addLine("    id 2");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeIdDifferentClass() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id \"edge-id1\"");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(Integer.class, Integer.class);
		assertThrows(ClassCastException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeWithNoSource() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeDuplicateSource() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id 1");
		text.addLine("    source 1");
		text.addLine("    source 2");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeWithNoTarget() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id 1");
		text.addLine("    source 1");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readEdgeDuplicateTarget() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id 1");
		text.addLine("    source 1");
		text.addLine("    target 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readUnknownKey() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  unknownkey 1");
		text.addLine("]");
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readIntGraph() {
		TextBuilder text = new TextBuilder();
		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id 1");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("  ]");
		text.addLine("]");

		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		assertTrue(reader.readGraph(new StringReader(text.get())) instanceof IntGraph);

		reader.setVertexType(Integer.class);
		reader.setEdgeType(Integer.class);
		assertFalse(reader.readGraph(new StringReader(text.get())) instanceof IntGraph);

		reader.setVertexType(int.class);
		reader.setEdgeType(Integer.class);
		assertFalse(reader.readGraph(new StringReader(text.get())) instanceof IntGraph);

		reader.setVertexType(Integer.class);
		reader.setEdgeType(int.class);
		assertFalse(reader.readGraph(new StringReader(text.get())) instanceof IntGraph);
	}

	@Test
	public void readWeightsType() {
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);
		TextBuilder text = new TextBuilder();

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    weight 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    weight 1");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear()))
				.getVerticesWeights("weight") instanceof WeightsInt);

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    weight 1.1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    weight 1");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear()))
				.getVerticesWeights("weight") instanceof WeightsDouble);

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    weight 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    weight 1.1");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear()))
				.getVerticesWeights("weight") instanceof WeightsDouble);

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    weight 1.1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    weight 1.1");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear()))
				.getVerticesWeights("weight") instanceof WeightsDouble);

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    weight 1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    weight \"hello\"");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear()))
				.getVerticesWeights("weight") instanceof WeightsObj);

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    weight 1.1");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    weight \"hello\"");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear()))
				.getVerticesWeights("weight") instanceof WeightsObj);

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    weight \"hello\"");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    weight 1");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear()))
				.getVerticesWeights("weight") instanceof WeightsObj);

		text.addLine("graph [");
		text.addLine("  directed 1");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    weight \"hello\"");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    weight 1.1");
		text.addLine("  ]");
		text.addLine("]");
		assertTrue(reader.readGraph(new StringReader(text.getAndClear()))
				.getVerticesWeights("weight") instanceof WeightsObj);
	}

	@SuppressWarnings("boxing")
	@Test
	public void readDoubleIds() {
		TextBuilder text = new TextBuilder();
		GmlGraphReader<Double, Double> reader = new GmlGraphReader<>(double.class, double.class);

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1.3");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2.5");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id -4.5");
		text.addLine("    source 1.3");
		text.addLine("    target 2.5");
		text.addLine("  ]");
		text.addLine("]");
		Graph<Double, Double> g = reader.readGraph(new StringReader(text.getAndClear()));

		Graph<Double, Double> g1 = Graph.newUndirected();
		g1.addVertex(1.3);
		g1.addVertex(2.5);
		g1.addEdge(1.3, 2.5, -4.5);

		assertEquals(g1, g);
	}

	@Test
	public void readDoubleWeights() {
		TextBuilder text = new TextBuilder();
		GmlGraphReader<Double, Double> reader = new GmlGraphReader<>(double.class, double.class);

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1.3");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals(1.3,
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next().doubleValue());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id +1.3");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals(+1.3,
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next().doubleValue());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id -1.3");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals(-1.3,
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next().doubleValue());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1.3E+15");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals(1.3E+15,
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next().doubleValue());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1.3E-15");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals(1.3E-15,
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next().doubleValue());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1.3E15");
		text.addLine("  ]");
		text.addLine("]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readStringWeights() {
		TextBuilder text = new TextBuilder();
		GmlGraphReader<String, String> reader = new GmlGraphReader<>(String.class, String.class);

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id \"hello\"");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals("hello", reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id \"hello world\"");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals("hello world",
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id \"hello\nworld\"");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals("hello\nworld",
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id \"hello\tworld\"");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals("hello\tworld",
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id \"hello\bworld\"");
		text.addLine("  ]");
		text.addLine("]");
		assertEquals("hello\bworld",
				reader.readGraph(new StringReader(text.getAndClear())).vertices().iterator().next());

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id \"hello\"world\"");
		text.addLine("  ]");
		text.addLine("]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id \"hello world");
		text.addLine("  ]");
		text.addLine("]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readInvalidParentheses() {
		TextBuilder text = new TextBuilder();
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  node ]");
		text.addLine("]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("  ]");
		text.addLine("  edge ]");
		text.addLine("]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  directed 1");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  directed 1directed 0");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  directed 1]]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  directed]");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  directed ");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  directed 1");
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(text.getAndClear())));
	}

	@Test
	public void readIgnoreComments() {
		TextBuilder text = new TextBuilder();
		GmlGraphReader<Integer, Integer> reader = new GmlGraphReader<>(int.class, int.class);

		text.addLine("# comment");
		text.addLine("graph [");
		text.addLine("  # 123456");
		text.addLine("  node [");
		text.addLine("    # 6666666");
		text.addLine("    id 1");
		text.addLine("    # co asghoewh");
		text.addLine("  ]");
		text.addLine("  # comment222");
		text.addLine("  edge [");
		text.addLine("    # comment");
		text.addLine("    id 1");
		text.addLine("    # comment");
		text.addLine("    source 1");
		text.addLine("    # comment whatsup");
		text.addLine("    target 1");
		text.addLine("    # comment helloooo");
		text.addLine("  ]");
		text.addLine("  # comment 2351235 ");
		text.addLine("]");
		assertNotNull(reader.readGraph(new StringReader(text.getAndClear())));

		text.addLine("graph [");
		text.addLine("  node [");
		text.addLine("    id 1");
		text.addLine("    comment \"some comment\"");
		text.addLine("  ]");
		text.addLine("  node [");
		text.addLine("    id 2");
		text.addLine("    comment \"another comment\"");
		text.addLine("  ]");
		text.addLine("  edge [");
		text.addLine("    id 1");
		text.addLine("    source 1");
		text.addLine("    target 2");
		text.addLine("    comment \"edge com\"");
		text.addLine("  ]");
		text.addLine("]");
		Graph<Integer, Integer> g = reader.readGraph(new StringReader(text.getAndClear()));

		assertNull(g.getVerticesWeights("comment"));
		assertNull(g.getEdgesWeights("comment"));
	}

	@Test
	public void writeUnweighted() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertices(range(10));
		for (int i = 0; i < 10; i++)
			g.addEdge(i, (i + 1) % 10, 131 + i);

		StringWriter writer = new StringWriter();
		new GmlGraphWriter<Integer, Integer>().writeGraph(g, writer);

		Graph<Integer, Integer> g1 =
				new GmlGraphReader<>(int.class, int.class).readGraph(new StringReader(writer.toString()));

		assertEquals(g, g1);
	}

	@Test
	public void writeDirected() {
		IntGraph g = IntGraph.newDirected();
		g.addVertices(range(10));
		for (int i = 0; i < 10; i++)
			g.addEdge(i, (i + 1) % 10, 131 + i);

		StringWriter writer = new StringWriter();
		new GmlGraphWriter<Integer, Integer>().writeGraph(g, writer);
		List<String> lines = Arrays.asList(writer.toString().split("\\r?\\n"));

		List<String> directedLines =
				lines.stream().map(String::trim).filter(s -> s.startsWith("directed")).collect(Collectors.toList());
		assertEquals(1, directedLines.size());
		assertEquals("directed 1", directedLines.get(0));
	}

	@Test
	public void writeUndirected() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertices(range(10));
		for (int i = 0; i < 10; i++)
			g.addEdge(i, (i + 1) % 10, 131 + i);

		StringWriter writer = new StringWriter();
		new GmlGraphWriter<Integer, Integer>().writeGraph(g, writer);
		List<String> lines = Arrays.asList(writer.toString().split("\\r?\\n"));

		List<String> directedLines =
				lines.stream().map(String::trim).filter(s -> s.startsWith("directed")).collect(Collectors.toList());
		assertEquals(1, directedLines.size());
		assertEquals("directed 0", directedLines.get(0));
	}

	@SuppressWarnings("boxing")
	@Test
	public void writeWeighted() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertices(range(10));
		for (int i = 0; i < 10; i++)
			g.addEdge(i, (i + 1) % 10, i);

		GmlGraphWriter<Integer, Integer> gmlWriter = new GmlGraphWriter<>();
		GmlGraphReader<Integer, Integer> gmlReader = new GmlGraphReader<>(int.class, int.class);
		StringWriter writer = new StringWriter();

		IWeightsByte vwByte = g.addVerticesWeights("weightsByte", byte.class);
		IWeightsByte ewByte = g.addEdgesWeights("weightsByte", byte.class);
		for (int v : g.vertices())
			vwByte.set(v, (byte) (v + 66));
		for (int e : g.edges())
			ewByte.set(e, (byte) (e - 37));
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		IntGraph g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsInt vwByte1 = g1.getVerticesWeights("weightsByte");
		IWeightsInt ewByte1 = g1.getEdgesWeights("weightsByte");
		assertNotNull(vwByte1);
		assertNotNull(ewByte1);
		for (int v : g.vertices())
			assertEquals(vwByte.get(v), vwByte1.get(v));
		for (int e : g.edges())
			assertEquals(ewByte.get(e), ewByte1.get(e));
		g.removeVerticesWeights("weightsByte");
		g.removeEdgesWeights("weightsByte");

		IWeightsShort vwShort = g.addVerticesWeights("weightsShort", short.class);
		IWeightsShort ewShort = g.addEdgesWeights("weightsShort", short.class);
		for (int v : g.vertices())
			vwShort.set(v, (short) (v + 66));
		for (int e : g.edges())
			ewShort.set(e, (short) (e - 37));
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsInt vwShort1 = g1.getVerticesWeights("weightsShort");
		IWeightsInt ewShort1 = g1.getEdgesWeights("weightsShort");
		assertNotNull(vwShort1);
		assertNotNull(ewShort1);
		for (int v : g.vertices())
			assertEquals(vwShort.get(v), vwShort1.get(v));
		for (int e : g.edges())
			assertEquals(ewShort.get(e), ewShort1.get(e));
		g.removeVerticesWeights("weightsShort");
		g.removeEdgesWeights("weightsShort");

		IWeightsInt vwInt = g.addVerticesWeights("weightsInt", int.class);
		IWeightsInt ewInt = g.addEdgesWeights("weightsInt", int.class);
		for (int v : g.vertices())
			vwInt.set(v, v + 66);
		for (int e : g.edges())
			ewInt.set(e, e - 37);
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsInt vwInt1 = g1.getVerticesWeights("weightsInt");
		IWeightsInt ewInt1 = g1.getEdgesWeights("weightsInt");
		assertNotNull(vwInt1);
		assertNotNull(ewInt1);
		for (int v : g.vertices())
			assertEquals(vwInt.get(v), vwInt1.get(v));
		for (int e : g.edges())
			assertEquals(ewInt.get(e), ewInt1.get(e));
		g.removeVerticesWeights("weightsInt");
		g.removeEdgesWeights("weightsInt");

		IWeightsLong vwLong = g.addVerticesWeights("weightsLong", long.class);
		IWeightsLong ewLong = g.addEdgesWeights("weightsLong", long.class);
		for (int v : g.vertices())
			vwLong.set(v, v + 66);
		for (int e : g.edges())
			ewLong.set(e, e - 37);
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsDouble vwLong1 = g1.getVerticesWeights("weightsLong");
		IWeightsDouble ewLong1 = g1.getEdgesWeights("weightsLong");
		assertNotNull(vwLong1);
		assertNotNull(ewLong1);
		for (int v : g.vertices())
			assertEquals(vwLong.get(v), (long) vwLong1.get(v));
		for (int e : g.edges())
			assertEquals(ewLong.get(e), (long) ewLong1.get(e));
		g.removeVerticesWeights("weightsLong");
		g.removeEdgesWeights("weightsLong");

		IWeightsFloat vwFloat = g.addVerticesWeights("weightsFloat", float.class);
		IWeightsFloat ewFloat = g.addEdgesWeights("weightsFloat", float.class);
		for (int v : g.vertices())
			vwFloat.set(v, v + 66);
		for (int e : g.edges())
			ewFloat.set(e, e - 37);
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsDouble vwFloat1 = g1.getVerticesWeights("weightsFloat");
		IWeightsDouble ewFloat1 = g1.getEdgesWeights("weightsFloat");
		assertNotNull(vwFloat1);
		assertNotNull(ewFloat1);
		for (int v : g.vertices())
			assertEquals(vwFloat.get(v), (float) vwFloat1.get(v), 1e-4);
		for (int e : g.edges())
			assertEquals(ewFloat.get(e), (float) ewFloat1.get(e), 1e-4);
		g.removeVerticesWeights("weightsFloat");
		g.removeEdgesWeights("weightsFloat");

		IWeightsDouble vwDouble = g.addVerticesWeights("weightsDouble", double.class);
		IWeightsDouble ewDouble = g.addEdgesWeights("weightsDouble", double.class);
		for (int v : g.vertices())
			vwDouble.set(v, v + 66);
		for (int e : g.edges())
			ewDouble.set(e, e - 37);
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsDouble vwDouble1 = g1.getVerticesWeights("weightsDouble");
		IWeightsDouble ewDouble1 = g1.getEdgesWeights("weightsDouble");
		assertNotNull(vwDouble1);
		assertNotNull(ewDouble1);
		for (int v : g.vertices())
			assertEquals(vwDouble.get(v), vwDouble1.get(v), 1e-4);
		for (int e : g.edges())
			assertEquals(ewDouble.get(e), ewDouble1.get(e), 1e-4);
		g.removeVerticesWeights("weightsDouble");
		g.removeEdgesWeights("weightsDouble");

		IWeightsBool vwBool = g.addVerticesWeights("weightsBool", boolean.class);
		IWeightsBool ewBool = g.addEdgesWeights("weightsBool", boolean.class);
		for (int v : g.vertices())
			vwBool.set(v, v % 2 == 0);
		for (int e : g.edges())
			ewBool.set(e, e % 2 == 0);
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsInt vwBool1 = g1.getVerticesWeights("weightsBool");
		IWeightsInt ewBool1 = g1.getEdgesWeights("weightsBool");
		assertNotNull(vwBool1);
		assertNotNull(ewBool1);
		for (int v : g.vertices())
			assertEquals(vwBool.get(v), vwBool1.get(v) == 1);
		for (int e : g.edges())
			assertEquals(ewBool.get(e), ewBool1.get(e) == 1);
		g.removeVerticesWeights("weightsBool");
		g.removeEdgesWeights("weightsBool");

		IWeightsChar vwChar = g.addVerticesWeights("weightsChar", char.class);
		IWeightsChar ewChar = g.addEdgesWeights("weightsChar", char.class);
		for (int v : g.vertices())
			vwChar.set(v, (char) (v + 66));
		for (int e : g.edges())
			ewChar.set(e, (char) (e - 37));
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsObj<String> vwChar1 = g1.getVerticesWeights("weightsChar");
		IWeightsObj<String> ewChar1 = g1.getEdgesWeights("weightsChar");
		assertNotNull(vwChar1);
		assertNotNull(ewChar1);
		for (int v : g.vertices())
			assertEquals(String.valueOf(vwChar.get(v)), vwChar1.get(v));
		for (int e : g.edges())
			assertEquals(String.valueOf(ewChar.get(e)), ewChar1.get(e));
		g.removeVerticesWeights("weightsChar");
		g.removeEdgesWeights("weightsChar");

		IWeightsObj<String> vwString = g.addVerticesWeights("weightsString", String.class);
		IWeightsObj<String> ewString = g.addEdgesWeights("weightsString", String.class);
		for (int v : g.vertices())
			vwString.set(v, String.valueOf(v + 66));
		for (int e : g.edges())
			ewString.set(e, String.valueOf(e - 37));
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsObj<String> vwString1 = g1.getVerticesWeights("weightsString");
		IWeightsObj<String> ewString1 = g1.getEdgesWeights("weightsString");
		assertNotNull(vwString1);
		assertNotNull(ewString1);
		for (int v : g.vertices())
			assertEquals(vwString.get(v), vwString1.get(v));
		for (int e : g.edges())
			assertEquals(ewString.get(e), ewString1.get(e));
		g.removeVerticesWeights("weightsString");
		g.removeEdgesWeights("weightsString");

		IWeightsObj<Pair<Integer, Integer>> vwObj = g.addVerticesWeights("weightsObj", Object.class);
		IWeightsObj<Pair<Integer, Integer>> ewObj = g.addEdgesWeights("weightsObj", Object.class);
		for (int v : g.vertices())
			vwObj.set(v, Pair.of(v + 66, v * 66));
		for (int e : g.edges())
			ewObj.set(e, Pair.of(e - 37, e * 37));
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		IWeightsObj<String> vwObj1 = g1.getVerticesWeights("weightsObj");
		IWeightsObj<String> ewObj1 = g1.getEdgesWeights("weightsObj");
		assertNotNull(vwObj1);
		assertNotNull(ewObj1);
		for (int v : g.vertices())
			assertEquals(vwObj.get(v).toString(), vwObj1.get(v));
		for (int e : g.edges())
			assertEquals(ewObj.get(e).toString(), ewObj1.get(e));
		g.removeVerticesWeights("weightsObj");
		g.removeEdgesWeights("weightsObj");
	}

	@Test
	public void writeWeightsCustom() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertices(range(10));
		for (int i = 0; i < 10; i++)
			g.addEdge(i, (i + 1) % 10, i);

		GmlGraphWriter<Integer, Integer> gmlWriter = new GmlGraphWriter<>();
		GmlGraphReader<Integer, Integer> gmlReader = new GmlGraphReader<>(int.class, int.class);
		StringWriter writer = new StringWriter();

		g.addVerticesWeights("weightsByte", byte.class);
		g.addVerticesWeights("weightsShort", short.class);
		g.addVerticesWeights("weightsInt", int.class);
		g.addVerticesWeights("weightsLong", long.class);
		g.addVerticesWeights("weightsFloat", float.class);
		g.addVerticesWeights("weightsDouble", double.class);
		g.addVerticesWeights("weightsBool", boolean.class);
		g.addVerticesWeights("weightsChar", char.class);
		g.addVerticesWeights("weightsObj", Object.class);
		g.addEdgesWeights("weightsByte", byte.class);
		g.addEdgesWeights("weightsShort", short.class);
		g.addEdgesWeights("weightsInt", int.class);
		g.addEdgesWeights("weightsLong", long.class);
		g.addEdgesWeights("weightsFloat", float.class);
		g.addEdgesWeights("weightsDouble", double.class);
		g.addEdgesWeights("weightsBool", boolean.class);
		g.addEdgesWeights("weightsChar", char.class);
		g.addEdgesWeights("weightsObj", Object.class);

		Set<String> verticesWeights = Set.of("weightsByte", "weightsInt", "weightsFloat", "weightsBool", "weightsObj");
		Set<String> edgesWeights = Set.of("weightsShort", "weightsFloat", "weightsDouble", "weightsObj");
		gmlWriter.setVerticesWeightsKeys(verticesWeights);
		gmlWriter.setEdgesWeightsKeys(edgesWeights);
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		IntGraph g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		assertEquals(verticesWeights, g1.getVerticesWeightsKeys());
		assertEquals(edgesWeights, g1.getEdgesWeightsKeys());

		gmlWriter.setVerticesWeightsKeys(null);
		gmlWriter.setEdgesWeightsKeys(null);
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		g1 = (IntGraph) gmlReader.readGraph(new StringReader(writer.toString()));
		assertEquals(g.getVerticesWeightsKeys(), g1.getVerticesWeightsKeys());
		assertEquals(g.getEdgesWeightsKeys(), g1.getEdgesWeightsKeys());

		gmlWriter.setVerticesWeightsKeys(Set.of("nonexistentweights"));
		gmlWriter.setEdgesWeightsKeys(edgesWeights);
		writer.getBuffer().setLength(0);
		assertThrows(IllegalArgumentException.class, () -> gmlWriter.writeGraph(g, writer));

		gmlWriter.setVerticesWeightsKeys(verticesWeights);
		gmlWriter.setEdgesWeightsKeys(Set.of("nonexistentweights"));
		writer.getBuffer().setLength(0);
		assertThrows(IllegalArgumentException.class, () -> gmlWriter.writeGraph(g, writer));
	}

	@Test
	public void writeInvalidString() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		IWeightsObj<String> wStrings = g.addVerticesWeights("weights1", String.class);
		IWeightsChar wChars = g.addVerticesWeights("weights2", char.class);

		GmlGraphWriter<Integer, Integer> gmlWriter = new GmlGraphWriter<>();
		GmlGraphReader<Integer, Integer> gmlReader = new GmlGraphReader<>(int.class, int.class);
		StringWriter writer = new StringWriter();

		wStrings.set(1, "hello world");
		wChars.set(1, 'h');
		writer.getBuffer().setLength(0);
		gmlWriter.writeGraph(g, writer);
		assertNotNull(gmlReader.readGraph(new StringReader(writer.toString())));

		wStrings.set(1, "hello \"world");
		wChars.set(1, 'h');
		writer.getBuffer().setLength(0);
		assertThrows(IllegalArgumentException.class, () -> gmlWriter.writeGraph(g, writer));

		wStrings.set(1, "hello world");
		wChars.set(1, '"');
		writer.getBuffer().setLength(0);
		assertThrows(IllegalArgumentException.class, () -> gmlWriter.writeGraph(g, writer));
	}

}
