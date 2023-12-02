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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class Sparse6Test extends TestUtils {

	@Test
	public void readSimpleGraph1() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		g.addEdge(0, 1, 0);
		g.addEdge(0, 2, 1);
		g.addEdge(1, 2, 2);
		g.addEdge(5, 6, 3);

		Sparse6GraphReader reader = new Sparse6GraphReader();
		assertEquals(g, reader.readGraph(new StringReader(bytesStr(":Fa@x^"))));
		assertEquals(g, reader.readGraph(new StringReader(bytesStr(":Fa@x^") + System.lineSeparator())));
		assertEquals(g, reader.readGraph(new StringReader(">>sparse6<<" + bytesStr(":Fa@x^"))));
		assertEquals(g,
				reader.readGraph(new StringReader(">>sparse6<<" + bytesStr(":Fa@x^") + System.lineSeparator())));
	}

	@Test
	public void readSimpleGraph2() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 0);

		Sparse6GraphReader reader = new Sparse6GraphReader();
		assertEquals(g, reader.readGraph(new StringReader(bytesStr(":An"))));
		assertEquals(g, reader.readGraph(new StringReader(bytesStr(":An") + System.lineSeparator())));
		assertEquals(g, reader.readGraph(new StringReader(">>sparse6<<" + bytesStr(":An"))));
		assertEquals(g, reader.readGraph(new StringReader(">>sparse6<<" + bytesStr(":An") + System.lineSeparator())));
	}

	@Test
	public void readSimpleGraph3() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 0);
		g.addEdge(0, 1, 1);
		g.addEdge(0, 1, 2);

		Sparse6GraphReader reader = new Sparse6GraphReader();
		assertEquals(g, reader.readGraph(new StringReader(bytesStr(":A_"))));
		assertEquals(g, reader.readGraph(new StringReader(bytesStr(":A_") + System.lineSeparator())));
		assertEquals(g, reader.readGraph(new StringReader(">>sparse6<<" + bytesStr(":A_"))));
		assertEquals(g, reader.readGraph(new StringReader(">>sparse6<<" + bytesStr(":A_") + System.lineSeparator())));
	}

	@Test
	public void readWriteRandGraphs() {
		final long seed = 0x71ca01849d86c256L;
		Random rand = new Random(seed);
		for (int nBase : IntList.of(10, 200, 300_000)) {
			final int repeatCount = nBase > 10_000 ? 1 : 32;
			for (int repeat = 0; repeat < repeatCount; repeat++) {
				final int n = nBase + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				IntGraph g = IntGraphFactory.newUndirected().allowSelfEdges().allowParallelEdges().newGraph();

				/* graph6 format support vertices with labels 0..n-1 only */
				for (int v = 0; v < n; v++)
					g.addVertex(v);

				Set<IntSet> edges = new HashSet<>();
				while (edges.size() < m) {
					int source = Graphs.randVertex(g, rand);
					int target = Graphs.randVertex(g, rand);
					edges.add(source == target ? IntSet.of(source) : IntSet.of(source, target));
				}
				edges.stream().sorted((e1, e2) -> {
					int u1, v1, u2, v2;
					if (e1.size() == 1) {
						u1 = e1.iterator().nextInt();
						v1 = u1;
					} else {
						IntIterator it = e1.iterator();
						u1 = it.nextInt();
						v1 = it.nextInt();
					}
					if (e2.size() == 1) {
						u2 = e2.iterator().nextInt();
						v2 = u2;
					} else {
						IntIterator it = e2.iterator();
						u2 = it.nextInt();
						v2 = it.nextInt();
					}
					if (u1 < v1) {
						int temp = u1;
						u1 = v1;
						v1 = temp;
					}
					if (u2 < v2) {
						int temp = u2;
						u2 = v2;
						v2 = temp;
					}
					if (u1 != u2)
						return Integer.compare(u1, u2);
					if (v1 != v2)
						return Integer.compare(v1, v2);
					return 0;

				}).forEach(e -> {
					int u, v;
					if (e.size() == 1) {
						u = v = e.iterator().nextInt();
					} else {
						IntIterator it = e.iterator();
						u = it.nextInt();
						v = it.nextInt();
					}
					g.addEdge(u, v, g.edges().size());
				});

				StringWriter writer = new StringWriter();
				Sparse6GraphWriter graphWriter = new Sparse6GraphWriter();
				graphWriter.keepEdgesIds(true);
				graphWriter.writeGraph(g, writer);
				String data = writer.toString();
				IntGraph g1 = new Sparse6GraphReader().readGraph(new StringReader(data));

				assertEquals(g, g1);
			}
		}
	}

	@Test
	public void readWriteRandGraphsWithoutKeepingEdgesIds() {
		final long seed = 0x918786e1eefcd9ddL;
		Random rand = new Random(seed);
		for (int nBase : IntList.of(10, 200, 300_000)) {
			final int repeatCount = nBase > 10_000 ? 1 : 32;
			for (int repeat = 0; repeat < repeatCount; repeat++) {
				final int n = nBase + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				IntGraph g = IntGraphFactory.newUndirected().allowSelfEdges().allowParallelEdges().newGraph();

				/* graph6 format support vertices with labels 0..n-1 only */
				for (int v = 0; v < n; v++)
					g.addVertex(v);

				while (g.edges().size() < m) {
					int source = Graphs.randVertex(g, rand);
					int target = Graphs.randVertex(g, rand);
					g.addEdge(source, target, g.edges().size()); /* edges ids are 0,1,2,...,m-1 */
				}

				StringWriter writer = new StringWriter();
				Sparse6GraphWriter graphWriter = new Sparse6GraphWriter();
				graphWriter.keepEdgesIds(false);
				graphWriter.writeGraph(g, writer);
				String data = writer.toString();
				IntGraph g1 = new Sparse6GraphReader().readGraph(new StringReader(data));

				assertEquals(g.vertices(), g1.vertices());
				assertEquals(g.edges(), g1.edges());

				for (int u : g.vertices()) {
					Set<Integer> outNeighbors = g.outEdges(u).intStream().map(e -> g.edgeEndpoint(e, u)).boxed()
							.collect(Collectors.toSet());
					Set<Integer> inNeighbors =
							g.inEdges(u).intStream().map(e -> g.edgeEndpoint(e, u)).boxed().collect(Collectors.toSet());
					Set<Integer> outNeighbors1 = g1.outEdges(u).intStream().map(e -> g1.edgeEndpoint(e, u)).boxed()
							.collect(Collectors.toSet());
					Set<Integer> inNeighbors1 = g1.inEdges(u).intStream().map(e -> g1.edgeEndpoint(e, u)).boxed()
							.collect(Collectors.toSet());
					assertEquals(outNeighbors, outNeighbors1);
					assertEquals(inNeighbors, inNeighbors1);
				}
			}
		}
	}

	@Test
	public void readInvalidHeader() {
		Sparse6GraphReader reader = new Sparse6GraphReader();
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(">>not-sparse-6<<:" + bytesStr(":An"))));
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytesStr("An"))));
	}

	@Test
	public void readNumberOfVertices() {
		BytesBuilder bytes = new BytesBuilder();
		Sparse6GraphReader reader = new Sparse6GraphReader();

		/* n is a single byte number */
		bytes.add(0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader(":" + bytes.strAndClear())).vertices());
		bytes.add(40);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(62);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(127);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));

		/* n is a 3 bytes number */
		bytes.add(126, 0 + 63, 0 + 63, 0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader(":" + bytes.strAndClear())).vertices());
		bytes.add(126, 0 + 63, 0 + 63, 1 + 63);
		assertEquals(range(1), reader.readGraph(new StringReader(":" + bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader(":" + bytes.strAndClear())).edges());
		bytes.add(126, 0 + 63, 0 + 63, 2 + 63);
		assertEquals(range(2), reader.readGraph(new StringReader(":" + bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader(":" + bytes.strAndClear())).edges());
		bytes.add(126);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 62, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 127, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 63, 62, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 63, 127, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 63, 63, 62);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 63, 63, 127);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));

		/* n is a 6 bytes number */
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader(":" + bytes.strAndClear())).vertices());
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 1 + 63);
		assertEquals(range(1), reader.readGraph(new StringReader(":" + bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader(":" + bytes.strAndClear())).edges());
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 2 + 63);
		assertEquals(range(2), reader.readGraph(new StringReader(":" + bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader(":" + bytes.strAndClear())).edges());
		bytes.add(126, 126);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 62, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 127, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 62, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 127, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 62, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 127, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 62, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 127, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 62, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 127, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63, 62);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63, 127);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
		bytes.add(126, 126, 126, 126, 126, 126, 126, 126);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
	}

	@Test
	public void invalidEdgeEndpoint() {
		BytesBuilder bytes = new BytesBuilder();
		Sparse6GraphReader reader = new Sparse6GraphReader();
		bytes.add(3 + 63, 0b011011 + 63, 0b011011 + 63, 0b011011 + 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(":" + bytes.strAndClear())));
	}

	@Test
	public void readMultipleGraphs() {
		BytesBuilder bytes = new BytesBuilder();
		Sparse6GraphReader reader = new Sparse6GraphReader();

		bytes.add(0 + 63);
		String str = "";
		str += ":" + bytes.str() + System.lineSeparator();
		str += ":" + bytes.str() + System.lineSeparator();
		String str0 = str;
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(str0)));
	}

	@Test
	public void readSingletonGraphWithSelfEdge() {
		IntGraph g = IntGraphFactory.newUndirected().allowSelfEdges().newGraph();
		g.addVertex(0);
		g.addEdge(0, 0, 0);

		BytesBuilder bytes = new BytesBuilder();
		Sparse6GraphReader reader = new Sparse6GraphReader();

		bytes.add(1 + 63, 0b011111 + 63);
		assertEquals(g, reader.readGraph(new StringReader(":" + bytes.strAndClear())));
	}

	@Test
	public void writeDirected() {
		IntGraph g = IntGraph.newDirected();
		Sparse6GraphWriter writer = new Sparse6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeInvalidVertices() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);

		Sparse6GraphWriter writer = new Sparse6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@SuppressWarnings("boxing")
	@Test
	public void writeInvalidEdges() {
		Sparse6GraphWriter writer = new Sparse6GraphWriter();

		Graph<Integer, Integer> g = Graph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1, 1);
		g.addEdge(1, 2, 2);
		g.addEdge(2, 0, 3);
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));

		g.clearEdges();
		g.addEdge(0, 2, 0);
		g.addEdge(0, 1, 1);
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeCustomPadding() {
		Sparse6GraphWriter writer = new Sparse6GraphWriter();
		IntGraph g = IntGraphFactory.newUndirected().allowSelfEdges().newGraph();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(0, 0, 0);
		g.addEdge(2, 1, 1);

		StringWriter sw = new StringWriter();
		writer.writeGraph(g, sw);

		BytesBuilder bytes = new BytesBuilder();
		bytes.add(4 + 63, 0b000010 + 63, 0b001011 + 63);
		assertEquals(">>sparse6<<:" + bytes.str() + System.lineSeparator(), sw.toString());
	}

	@Test
	public void writeReadEmptyGraph() {
		IntGraph g = IntGraph.newUndirected();
		Sparse6GraphWriter writer = new Sparse6GraphWriter();
		Sparse6GraphReader reader = new Sparse6GraphReader();
		StringWriter sw = new StringWriter();
		writer.writeGraph(g, sw);
		assertEquals(g, reader.readGraph(new StringReader(sw.toString())));
	}

	@Test
	public void readEmptyFile() {
		assertThrows(IllegalArgumentException.class, () -> new Sparse6GraphReader().readGraph(new StringReader("")));
	}

}
