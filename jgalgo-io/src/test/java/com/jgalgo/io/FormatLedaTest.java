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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.booleans.BooleanList;

public class FormatLedaTest {

	@Test
	public void ReadWriteLEDAWithWeights() {
		String leda_definition = "";
		leda_definition += "#header section\n";
		leda_definition += "LEDA.GRAPH\n";
		leda_definition += "string\n";
		leda_definition += "int\n";
		leda_definition += "-1\n";
		leda_definition += "#nodes section\n";
		leda_definition += "5\n";
		leda_definition += "|{v1}|\n";
		leda_definition += "|{v2}|\n";
		leda_definition += "|{v3}|\n";
		leda_definition += "|{v4}|\n";
		leda_definition += "|{v5}|\n";
		leda_definition += "\n";
		leda_definition += "#edges section\n";
		leda_definition += "7\n";
		leda_definition += "1 2 0 |{4}|\n";
		leda_definition += "1 3 0 |{3}|\n";
		leda_definition += "2 3 0 |{2}|\n";
		leda_definition += "3 4 0 |{3}|\n";
		leda_definition += "3 5 0 |{7}|\n";
		leda_definition += "4 5 0 |{6}|\n";
		leda_definition += "5 1 0 |{1}|\n";
		leda_definition += "\n";

		GraphReader rd_leda1 = GraphReader.newInstance("leda");
		StringReader sr = new StringReader(leda_definition);
		GraphBuilder gb = rd_leda1.readIntoBuilder(sr);
		Graph graph1 = gb.build();

		GraphWriter wr_leda1 = GraphWriter.newInstance("leda");
		StringWriter sw = new StringWriter();
		wr_leda1.writeGraph(graph1, sw);
		System.out.println("The exported LEDA graph is:");
		System.out.println(sw.toString());
	}

	@Test
	public void ReadWriteLEDAWithoutWeights() {
		String leda_definition = "";
		leda_definition += "#header section\n";
		leda_definition += "LEDA.GRAPH\n";
		leda_definition += "string\n";
		leda_definition += "void\n";
		leda_definition += "-1\n";
		leda_definition += "#nodes section\n";
		leda_definition += "5\n";
		leda_definition += "|{vX1}|\n";
		leda_definition += "|{vX2}|\n";
		leda_definition += "|{vX3}|\n";
		leda_definition += "|{vX4}|\n";
		leda_definition += "|{vX5}|\n";
		leda_definition += "\n";
		leda_definition += "#edges section\n";
		leda_definition += "7\n";
		leda_definition += "1 2 0 |{}|\n";
		leda_definition += "1 3 0 |{}|\n";
		leda_definition += "2 3 0 |{}|\n";
		leda_definition += "3 4 0 |{}|\n";
		leda_definition += "3 5 0 |{}|\n";
		leda_definition += "4 5 0 |{}|\n";
		leda_definition += "5 1 0 |{}|\n";
		leda_definition += "\n";

		GraphReader rd_leda1 = GraphReader.newInstance("leda");
		StringReader sr = new StringReader(leda_definition);
		GraphBuilder gb = rd_leda1.readIntoBuilder(sr);
		Graph graph1 = gb.build();

		GraphWriter wr_leda1 = GraphWriter.newInstance("leda");
		StringWriter sw = new StringWriter();
		wr_leda1.writeGraph(graph1, sw);
		System.out.println("The exported LEDA graph is:");
		System.out.println(sw.toString());
	}

	@Test
	public void writeAndReadRandomGraphs() {
		final long seed = 0x71a78c3b16b1e662L;
		Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (int repeat = 0; repeat < 32; repeat++) {
				final int n = 10 + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				Graph g = directed ? GraphFactory.newDirected().newGraph() : GraphFactory.newUndirected().newGraph();

				/* LEDA format support vertices with labels 1..n only */
				for (int v = 1; v <= n; v++)
					g.addVertex(v);

				for (int[] vs = g.vertices().toIntArray(); g.edges().size() < m;) {
					int source = vs[rand.nextInt(n)];
					int target = vs[rand.nextInt(n)];
					/* LEDA format support edges with labels 1..m only */
					int e = g.edges().size() + 1;
					g.addEdge(source, target, e);
				}

				StringWriter writer = new StringWriter();
				GraphWriter.newInstance("leda").writeGraph(g, writer);
				String data = writer.toString();

				GraphBuilder gb = GraphReader.newInstance("leda").readIntoBuilder(new StringReader(data));
				Graph gImmutable = gb.build();
				Graph gMutable = gb.buildMutable();
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
				Graph g = directed ? GraphFactory.newDirected().newGraph() : GraphFactory.newUndirected().newGraph();

				/* LEDA format support vertices with labels 1..n only */
				for (int v = 1; v <= n; v++)
					g.addVertex(v);

				for (int[] vs = g.vertices().toIntArray(); g.edges().size() < m;) {
					int source = vs[rand.nextInt(n)];
					int target = vs[rand.nextInt(n)];
					/* LEDA format support edges with labels 1..m only */
					int e = g.edges().size() + 1;
					g.addEdge(source, target, e);
				}

				Weights.Int we1 = g.addEdgesWeights("weightsKey", int.class);
				for (int e : g.edges())
					we1.set(e, n + rand.nextInt(m * 3));

				StringWriter writer = new StringWriter();
				GraphWriter.newInstance("leda").writeGraph(g, writer);
				String data = writer.toString();

				GraphBuilder gb = GraphReader.newInstance("leda").readIntoBuilder(new StringReader(data));
				Graph gImmutable = gb.build();
				Graph gMutable = gb.buildMutable();
				assertEquals(g, gImmutable);
				assertEquals(g, gMutable);
			}
		}

	}

}
