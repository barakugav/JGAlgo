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
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.Weights;

public class FormatGMLTest {

	@Test
	public void parseSimpleGraph() {
		String data = "";
		data += "graph\n";
		data += "[\n";
		data += "  node\n";
		data += "  [\n";
		data += "   id 1\n";
		data += "   label \"Node A\"\n";
		data += "  ]\n";
		data += "  node\n";
		data += "  [\n";
		data += "   id 3\n";
		data += "   label \"Node B\"\n";
		data += "  ]\n";
		data += "  node\n";
		data += "  [\n";
		data += "   id 5\n";
		data += "   label \"Node C\"\n";
		data += "  ]\n";
		data += "   edge\n";
		data += "  [\n";
		data += "   source 3\n";
		data += "   target 1\n";
		data += "   label \"Edge B to A\"\n";
		data += "  ]\n";
		data += "  edge\n";
		data += "  [\n";
		data += "   source 5\n";
		data += "   target 1\n";
		data += "   label \"Edge C to A\"\n";
		data += "  ]\n";
		data += "]\n";
		GraphReader.newInstance("gml").readGraph(new StringReader(data));
	}

	@Test
	public void writeAndReadRandomGraphs() {
		final long seed = 0xaf7128f601c98d2cL;
		Random rand = new Random(seed);
		for (int repeat = 0; repeat < 32; repeat++) {
			final int n = 10 + rand.nextInt(20);
			final int m = 15 + rand.nextInt(30);
			Graph g = Graph.newUndirected();

			while (g.vertices().size() < n) {
				int v = rand.nextInt(n * 3);
				if (!g.vertices().contains(v))
					g.addVertex(v);
			}

			for (int[] vs = g.vertices().toIntArray(); g.edges().size() < m;) {
				int source = vs[rand.nextInt(n)];
				int target = vs[rand.nextInt(n)];
				int e = rand.nextInt(m * 3);
				if (!g.edges().contains(e))
					g.addEdge(source, target, e);
			}

			StringWriter writer = new StringWriter();
			GraphWriter.newInstance("gml").writeGraph(g, writer);
			String data = writer.toString();

			GraphBuilder gb = GraphReader.newInstance("gml").readIntoBuilder(new StringReader(data));
			Graph gImmutable = gb.build();
			Graph gMutable = gb.buildMutable();
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
			Graph g = Graph.newUndirected();

			while (g.vertices().size() < n) {
				int v = rand.nextInt(n * 3);
				if (!g.vertices().contains(v))
					g.addVertex(v);
			}

			for (int[] vs = g.vertices().toIntArray(); g.edges().size() < m;) {
				int source = vs[rand.nextInt(n)];
				int target = vs[rand.nextInt(n)];
				int e = rand.nextInt(m * 3);
				if (!g.edges().contains(e))
					g.addEdge(source, target, e);
			}

			Weights.Int wv1 = g.addVerticesWeights("v1", int.class);
			Weights.Double wv2 = g.addVerticesWeights("v2", double.class);
			Weights<String> wv3 = g.addVerticesWeights("v3", String.class);
			for (int v : g.vertices()) {
				wv1.set(v, n + rand.nextInt(n * 3));
				wv2.set(v, n + rand.nextDouble());
				wv3.set(v, Character.toString('a' + rand.nextInt('z' - 'a' + 1)));
			}

			Weights.Int we1 = g.addEdgesWeights("e1", int.class);
			Weights.Double we2 = g.addEdgesWeights("e2", double.class);
			Weights<String> we3 = g.addEdgesWeights("e3", String.class);
			for (int e : g.edges()) {
				we1.set(e, n + rand.nextInt(m * 3));
				we2.set(e, n + rand.nextDouble());
				we3.set(e, Character.toString('a' + rand.nextInt('z' - 'a' + 1)));
			}

			StringWriter writer = new StringWriter();
			GraphWriter.newInstance("gml").writeGraph(g, writer);
			String data = writer.toString();

			GraphBuilder gb = GraphReader.newInstance("gml").readIntoBuilder(new StringReader(data));
			Graph gImmutable = gb.build();
			Graph gMutable = gb.buildMutable();
			assertEquals(g, gImmutable);
			assertEquals(g, gMutable);
		}
	}

	@Test
	public void writeDirectedGraphUnsupported() {
		Graph g = Graph.newDirected();
		g.addVertex(1);
		g.addVertex(6);
		g.addVertex(78);
		g.addEdge(78, 1);
		g.addEdge(1, 6);
		assertThrows(IllegalArgumentException.class,
				() -> GraphWriter.newInstance("gml").writeGraph(g, new StringWriter()));
	}

}
