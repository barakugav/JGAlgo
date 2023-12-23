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

public class Graph6Test extends TestUtils {

	@Test
	public void readSimpleGraph1() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 0);

		Graph6GraphReader reader = new Graph6GraphReader();
		assertEquals(g, reader.readGraph(new StringReader(bytesStr("A_"))));
		assertEquals(g, reader.readGraph(new StringReader(bytesStr("A_") + System.lineSeparator())));
		assertEquals(g, reader.readGraph(new StringReader(">>graph6<<" + bytesStr("A_"))));
		assertEquals(g, reader.readGraph(new StringReader(">>graph6<<" + bytesStr("A_") + System.lineSeparator())));
	}

	@Test
	public void readSimpleGraph2() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 2, 0);
		g.addEdge(1, 2, 1);

		Graph6GraphReader reader = new Graph6GraphReader();
		assertEquals(g, reader.readGraph(new StringReader(bytesStr("BW"))));
		assertEquals(g, reader.readGraph(new StringReader(bytesStr("BW") + System.lineSeparator())));
		assertEquals(g, reader.readGraph(new StringReader(">>graph6<<" + bytesStr("BW"))));
		assertEquals(g, reader.readGraph(new StringReader(">>graph6<<" + bytesStr("BW") + System.lineSeparator())));
	}

	@Test
	public void readWriteRandGraphs() {
		final long seed = 0x52fbff243a5a5994L;
		Random rand = new Random(seed);
		for (int nBase : IntList.of(10, 200)) {
			for (int repeat = 0; repeat < 32; repeat++) {
				final int n = nBase + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				IntGraph g = IntGraphFactory.undirected().newGraph();

				/* graph6 format support vertices with labels 0..n-1 only */
				g.addVertices(range(10));

				Bitmap edges = new Bitmap(n * (n - 1) / 2);
				int edgesNum = 0;
				while (edgesNum < m) {
					int source = Graphs.randVertex(g, rand);
					int target = Graphs.randVertex(g, rand);
					int edgeIdx = source * (source - 1) / 2 + target;
					if (source == target || edges.get(edgeIdx))
						continue; /* self and parallel edges are not supported */
					edges.set(edgeIdx);
					edgesNum++;
				}
				for (int u : range(1, n))
					for (int v : range(0, u))
						if (edges.get(u * (u - 1) / 2 + v))
							g.addEdge(u, v, g.edges().size()); /* edges ids are 0,1,2,...,m-1 */

				StringWriter writer = new StringWriter();
				Graph6GraphWriter graphWriter = new Graph6GraphWriter();
				graphWriter.keepEdgesIds(true);
				graphWriter.writeGraph(g, writer);
				String data = writer.toString();
				IntGraph g1 = new Graph6GraphReader().readGraph(new StringReader(data));

				assertEquals(g, g1);
			}
		}
	}

	@Test
	public void readWriteRandGraphsWithoutKeepingEdgesIds() {
		final long seed = 0x2ac236f27917171eL;
		Random rand = new Random(seed);
		for (int nBase : IntList.of(10, 200)) {
			for (int repeat = 0; repeat < 32; repeat++) {
				final int n = nBase + rand.nextInt(20);
				final int m = 15 + rand.nextInt(30);
				IntGraph g = IntGraphFactory.undirected().newGraph();

				/* graph6 format support vertices with labels 0..n-1 only */
				g.addVertices(range(n));

				while (g.edges().size() < m) {
					int source = Graphs.randVertex(g, rand);
					int target = Graphs.randVertex(g, rand);
					if (source == target || g.containsEdge(source, target))
						continue; /* self and parallel edges are not supported */
					g.addEdge(source, target, g.edges().size()); /* edges ids are 0,1,2,...,m-1 */
				}

				StringWriter writer = new StringWriter();
				Graph6GraphWriter graphWriter = new Graph6GraphWriter();
				graphWriter.keepEdgesIds(false);
				graphWriter.writeGraph(g, writer);
				String data = writer.toString();
				IntGraph g1 = new Graph6GraphReader().readGraph(new StringReader(data));

				assertEquals(g.vertices(), g1.vertices());
				assertEquals(g.edges(), g1.edges());

				for (int u : g.vertices()) {
					Set<Integer> outNeighbors = g
							.outEdges(u)
							.intStream()
							.map(e -> g.edgeEndpoint(e, u))
							.boxed()
							.collect(Collectors.toSet());
					Set<Integer> inNeighbors =
							g.inEdges(u).intStream().map(e -> g.edgeEndpoint(e, u)).boxed().collect(Collectors.toSet());
					Set<Integer> outNeighbors1 = g1
							.outEdges(u)
							.intStream()
							.map(e -> g1.edgeEndpoint(e, u))
							.boxed()
							.collect(Collectors.toSet());
					Set<Integer> inNeighbors1 = g1
							.inEdges(u)
							.intStream()
							.map(e -> g1.edgeEndpoint(e, u))
							.boxed()
							.collect(Collectors.toSet());
					assertEquals(outNeighbors, outNeighbors1);
					assertEquals(inNeighbors, inNeighbors1);
				}
			}
		}
	}

	@Test
	public void readNumberOfVertices() {
		BytesBuilder bytes = new BytesBuilder();
		Graph6GraphReader reader = new Graph6GraphReader();

		/* n is a single byte number */
		bytes.add(0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader(bytes.strAndClear())).vertices());
		bytes.add(40);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(62);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(127);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));

		/* n is a 3 bytes number */
		bytes.add(126, 0 + 63, 0 + 63, 0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader(bytes.strAndClear())).vertices());
		bytes.add(126, 0 + 63, 0 + 63, 1 + 63);
		assertEquals(range(1), reader.readGraph(new StringReader(bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader(bytes.strAndClear())).edges());
		bytes.add(126, 0 + 63, 0 + 63, 2 + 63, 63);
		assertEquals(range(2), reader.readGraph(new StringReader(bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader(bytes.strAndClear())).edges());
		bytes.add(126);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 62, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 127, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 63, 62, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 63, 127, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 63, 63, 62);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 63, 63, 127);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));

		/* n is a 6 bytes number */
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63);
		assertEquals(range(0), reader.readGraph(new StringReader(bytes.strAndClear())).vertices());
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 1 + 63);
		assertEquals(range(1), reader.readGraph(new StringReader(bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader(bytes.strAndClear())).edges());
		bytes.add(126, 126, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 0 + 63, 2 + 63, 63);
		assertEquals(range(2), reader.readGraph(new StringReader(bytes.str())).vertices());
		assertEquals(IntSet.of(), reader.readGraph(new StringReader(bytes.strAndClear())).edges());
		bytes.add(126, 126);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 62, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 127, 63, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 62, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 127, 63, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 62, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 127, 63, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 62, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 127, 63, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 62, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 127, 63);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63, 62);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 63, 63, 63, 63, 63, 127);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
		bytes.add(126, 126, 126, 126, 126, 126, 126, 126);
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
	}

	@Test
	public void readInvalidEdgesVector() {
		BytesBuilder bytes = new BytesBuilder();
		Graph6GraphReader reader = new Graph6GraphReader();

		bytes.add(2 + 63); /* edges vector should be of one byte, missing */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));

		bytes.add(2 + 63, 0 + 63, 0 + 63); /* edges vector should be of one byte, too many */
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(bytes.strAndClear())));
	}

	@Test
	public void readMultipleGraphs() {
		BytesBuilder bytes = new BytesBuilder();
		Graph6GraphReader reader = new Graph6GraphReader();

		bytes.add(0 + 63);
		String str = "";
		str += bytes.str() + System.lineSeparator();
		str += bytes.str() + System.lineSeparator();
		String str0 = str;
		assertThrows(IllegalArgumentException.class, () -> reader.readGraph(new StringReader(str0)));
	}

	@Test
	public void writeDirected() {
		IntGraph g = IntGraph.newDirected();
		Graph6GraphWriter writer = new Graph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeInvalidVertices() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);

		Graph6GraphWriter writer = new Graph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeTooManyVertices() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertices(range(1 << 19));

		Graph6GraphWriter writer = new Graph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeInvalidEdges() {
		Graph6GraphWriter writer = new Graph6GraphWriter();

		IntGraph g = IntGraph.newUndirected();
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
	public void writeWithSelfEdges() {
		IntGraph g = IntGraphFactory.undirected().allowSelfEdges().newGraph();
		g.addVertex(0);
		g.addEdge(0, 0, 0);

		Graph6GraphWriter writer = new Graph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void writeWithParallelEdges() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addEdge(0, 1, 0);
		g.addEdge(0, 1, 1);

		Graph6GraphWriter writer = new Graph6GraphWriter();
		assertThrows(IllegalArgumentException.class, () -> writer.writeGraph(g, new StringWriter()));
	}

	@Test
	public void readEmptyFile() {
		assertThrows(IllegalArgumentException.class, () -> new Graph6GraphReader().readGraph(new StringReader("")));
	}

}
