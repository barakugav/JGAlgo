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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import com.jgalgo.graph.IntGraphBuilder;
import it.unimi.dsi.fastutil.ints.IntIntPair;

/**
 * Read a graph in 'graph6' format.
 *
 * <p>
 * 'graph6' is a format for storing undirected graphs in a compact manner, using only printable ASCII characters. Files
 * in these format have text type and contain one line per graph. It is suitable for small graphs, or large dense
 * graphs. The format support graphs with vertices numbered 0..n-1 only, where n is the number of vertices, and edges
 * numbered 0..m-1 only, where m is the number of edges. A graph6 file contains a bit vector with {@code n (n - 1) / 2}
 * bits representing the edges of the graph. All bytes of a graph6 file are in the range 63..126, which are the
 * printable ASCII characters, therefore a bit vector is represented by a sequence of bytes in which each byte encode
 * only 6 bits.
 *
 * <p>
 * The format does not support specifying the ids of the edges, therefore the reader will number them from {@code 0} to
 * {@code m-1} in the order in the bit vector. The order, and the format details can be found
 * <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.html">here</a>.
 *
 * <p>
 * The 'graph6' format is efficient for dense graph, for dense graphs the {@linkplain Sparse6GraphReader 'sparse6'
 * format} should be used.
 *
 * <p>
 * Self edges and parallel edges are not supported by the format.
 *
 * <p>
 * File with a graph in 'graph6' format usually have the extension {@code .g6}.
 *
 * @see    Graph6GraphWriter
 * @author Barak Ugav
 */
public final class Graph6GraphReader extends GraphIoUtils.AbstractIntGraphReader {

	/**
	 * Create a new reader.
	 */
	public Graph6GraphReader() {}

	@Override
	IntGraphBuilder readIntoBuilderImpl(Reader reader) throws IOException {
		BufferedReader br = GraphIoUtils.bufferedReader(reader);
		IntGraphBuilder g = IntGraphBuilder.undirected();

		String line = br.readLine();
		if (line == null)
			throw new IllegalArgumentException("empty file");

		/* optional header */
		if (line.startsWith(">>graph6<<"))
			line = line.substring(">>graph6<<".length());
		byte[] bytes = line.getBytes(GraphIoUtils.JGALGO_CHARSET);
		int cursor = 0;

		/* Read N(n) */
		IntIntPair nPair = Graph6.readNumberOfVertices(bytes, cursor);
		final int n = nPair.firstInt();
		cursor = nPair.secondInt();
		g.addVertices(range(n)); /* vertices ids are 0,1,2,...,n-1 */

		/* Read all edges R(x) */
		Graph6.BitsReader bitsReader = new Graph6.BitsReader(bytes, cursor);
		for (int u : range(1, Math.max(1, n))) {
			for (int v : range(u)) {
				if (!bitsReader.hasNext())
					throw new IllegalArgumentException("Too few bits for edges bit vector");
				boolean edgeExist = bitsReader.next();
				if (edgeExist)
					g.addEdge(u, v, g.edges().size());
			}
		}
		if (bitsReader.hasNext()) {
			bitsReader.skipToCurrentByteEnd(); /* skip last byte padding */
			if (bitsReader.hasNext())
				throw new IllegalArgumentException("Too many bits for edges bit vector");
		}

		if (br.readLine() != null)
			throw new IllegalArgumentException("Expected a single line in file");

		return g;
	}

}
