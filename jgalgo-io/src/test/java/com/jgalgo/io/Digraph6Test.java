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
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class Digraph6Test extends TestUtils {

	@Test
	public void readSimpleGraph1() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 0);

		Digraph6GraphReader reader = new Digraph6GraphReader();
		assertEquals(g, reader.readGraph(new StringReader(bytesStr("&AO"))));
		assertEquals(g, reader.readGraph(new StringReader(bytesStr("&AO") + System.lineSeparator())));
		assertEquals(g, reader.readGraph(new StringReader(">>digraph6<<" + bytesStr("&AO"))));
		assertEquals(g, reader.readGraph(new StringReader(">>digraph6<<" + bytesStr("&AO") + System.lineSeparator())));
	}

	@Test
	public void readSimpleGraph2() {
		IntGraph g = IntGraphFactory.newDirected().allowSelfEdges().newGraph();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1, 0);
		g.addEdge(1, 2, 1);
		g.addEdge(2, 2, 2);

		Digraph6GraphReader reader = new Digraph6GraphReader();
		assertEquals(g, reader.readGraph(new StringReader(bytesStr("&BPG"))));
		assertEquals(g, reader.readGraph(new StringReader(bytesStr("&BPG") + System.lineSeparator())));
		assertEquals(g, reader.readGraph(new StringReader(">>digraph6<<" + bytesStr("&BPG"))));
		assertEquals(g, reader.readGraph(new StringReader(">>digraph6<<" + bytesStr("&BPG") + System.lineSeparator())));
	}

	@Test
	public void readWriteRandGraphs() {
		final long seed = 0x6e13abce2729d464L;
		Random rand = new Random(seed);
		for (int nBase : IntList.of(10, 200)) {
			for (int repeat = 0; repeat < 32; repeat++) {
				final int n = nBase + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				IntGraph g = IntGraphFactory.newDirected().allowSelfEdges().newGraph();

				/* digraph6 format support vertices with labels 0..n-1 only */
				for (int v = 0; v < n; v++)
					g.addVertex(v);

				Bitmap edges = new Bitmap(n * n);
				int edgesNum = 0;
				while (edgesNum < m) {
					int source = Graphs.randVertex(g, rand);
					int target = Graphs.randVertex(g, rand);
					int edgeIdx = source * n + target;
					if (edges.get(edgeIdx))
						continue; /* parallel edges are not supported */
					edges.set(edgeIdx);
					edgesNum++;
				}
				for (int u : range(0, n))
					for (int v : range(0, n))
						if (edges.get(u * n + v))
							g.addEdge(u, v, g.edges().size()); /* edges ids are 0,1,2,...,m-1 */

				StringWriter writer = new StringWriter();
				Digraph6GraphWriter graphWriter = new Digraph6GraphWriter();
				graphWriter.keepEdgesIds(true);
				graphWriter.writeGraph(g, writer);
				String data = writer.toString();
				IntGraph g1 = new Digraph6GraphReader().readGraph(new StringReader(data));

				assertEquals(g, g1);
			}
		}
	}

	@Test
	public void readWriteRandGraphsWithoutKeepingEdgesIds() {
		final long seed = 0x7ce94f72bc7edb86L;
		Random rand = new Random(seed);
		for (int nBase : IntList.of(10, 200)) {
			for (int repeat = 0; repeat < 32; repeat++) {
				final int n = nBase + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				IntGraph g = IntGraphFactory.newDirected().allowSelfEdges().newGraph();

				/* digraph6 format support vertices with labels 0..n-1 only */
				for (int v = 0; v < n; v++)
					g.addVertex(v);

				while (g.edges().size() < m) {
					int source = Graphs.randVertex(g, rand);
					int target = Graphs.randVertex(g, rand);
					if (g.getEdge(source, target) != -1)
						continue; /* parallel edges are not supported */
					g.addEdge(source, target, g.edges().size()); /* edges ids are 0,1,2,...,m-1 */
				}

				StringWriter writer = new StringWriter();
				Digraph6GraphWriter graphWriter = new Digraph6GraphWriter();
				graphWriter.keepEdgesIds(false);
				graphWriter.writeGraph(g, writer);
				String data = writer.toString();
				IntGraph g1 = new Digraph6GraphReader().readGraph(new StringReader(data));

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
		Digraph6GraphReader reader = new Digraph6GraphReader();
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader(">>not-digraph-6<<&" + bytesStr("AO"))));
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytesStr("AO"))));
	}

	@Test
	public void readNumberOfVertices() {
		BytesBuilder bytes = new BytesBuilder();
		Digraph6GraphReader reader = new Digraph6GraphReader();

		/* n is a single byte number */
		bytes.add(0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader("&" + bytes.strAndClear())).vertices());
		bytes.add(40);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(62);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(127);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));

		/* n is a 3 bytes number */
		bytes.add(126, 0 + 63, 0 + 63, 0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader("&" + bytes.strAndClear())).vertices());
		bytes.add(126, 0 + 63, 0 + 63, 1 + 63, 0 + 63);
		assertEquals(range(1), reader.readGraph(new StringReader("&" + bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader("&" + bytes.strAndClear())).edges());
		bytes.add(126, 0 + 63, 0 + 63, 2 + 63, 63);
		assertEquals(range(2), reader.readGraph(new StringReader("&" + bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader("&" + bytes.strAndClear())).edges());
		bytes.add(126);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 62, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 127, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 63, 62, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 63, 127, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 63, 63, 62);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 63, 63, 127);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));

		/* n is a 6 bytes number */
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader("&" + bytes.strAndClear())).vertices());
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 1 + 63, 0 + 63);
		assertEquals(range(1), reader.readGraph(new StringReader("&" + bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader("&" + bytes.strAndClear())).edges());
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 2 + 63, 63);
		assertEquals(range(2), reader.readGraph(new StringReader("&" + bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader("&" + bytes.strAndClear())).edges());
		bytes.add(126, 126);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 62, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 127, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 62, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 127, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 62, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 127, 63, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 62, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 127, 63, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 62, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 127, 63);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63, 62);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63, 127);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
		bytes.add(126, 126, 126, 126, 126, 126, 126, 126);
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
	}

	@Test
	public void readInvalidEdgesVector() {
		BytesBuilder bytes = new BytesBuilder();
		Digraph6GraphReader reader = new Digraph6GraphReader();

		bytes.add(2 + 63); /* edges vector should be of one byte, missing */
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));

		bytes.add(2 + 63, 0 + 63, 0 + 63); /* edges vector should be of one byte, too many */
		assertThrows(IllegalArgumentException.class,
				() -> reader.readGraph(new StringReader("&" + bytes.strAndClear())));
	}

	@Test
	public void readMultipleGraphs() {
		BytesBuilder bytes = new BytesBuilder();
		Digraph6GraphReader reader = new Digraph6GraphReader();

		bytes.add(0 + 63);
		String str = "";
		str += bytes.str() + System.lineSeparator();
		str += bytes.str() + System.lineSeparator();
		String str0 = str;
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader("&" + str0)));
	}

	@Test
	public void writeUndirected() {
		IntGraph g = IntGraph.newUndirected();
		Digraph6GraphWriter writer = new Digraph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeInvalidVertices() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);

		Digraph6GraphWriter writer = new Digraph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeTooManyVertices() {
		IntGraph g = IntGraph.newDirected();
		for (int i = 0; i < (1 << 19); i++)
			g.addVertex(i);

		Digraph6GraphWriter writer = new Digraph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeInvalidEdges() {
		Digraph6GraphWriter writer = new Digraph6GraphWriter();

		IntGraph g = IntGraph.newDirected();
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
	public void writeWithParallelEdges() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 0);
		g.addEdge(0, 1, 1);

		Digraph6GraphWriter writer = new Digraph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void readEmptyFile() {
		assertThrows(IllegalArgumentException.class, () -> new Digraph6GraphReader().readGraph(new StringReader("")));
	}

}
