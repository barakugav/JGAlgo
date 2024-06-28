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

import static com.jgalgo.internal.util.Numbers.log2ceil;
import static com.jgalgo.internal.util.Range.range;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import it.unimi.dsi.fastutil.ints.IntIntPair;

/**
 * Read a graph in 'sparse6' format.
 *
 * <p>
 * 'sparse6' is a format for storing undirected graphs in a compact manner, using only printable ASCII characters. Files
 * in these format have text type and contain one line per graph. It is space-efficient for large sparse graphs. The
 * format support graphs with vertices numbered 0..n-1 only, where n is the number of vertices, and edges numbered
 * 0..m-1 only, where m is the number of edges. A sparse6 file contains the number of vertices, following by a list of
 * edges, encoded in bytes, each in range 63..126 which are the printable ASCII characters. Each byte encode 6 bits.
 *
 * <p>
 * The format does not support specifying the ids of the edges, therefore the reader will number them from {@code 0} to
 * {@code m-1} in the order it reads them.
 *
 * <p>
 * Self edges and parallel edges are supported by the format. The full description of the format can be found
 * <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.html">here</a>, which also define the 'graph6' format, which
 * is used for dense graph. See {@link Graph6GraphReader} for reading graphs in 'graph6' format.
 *
 * <p>
 * File with a graph in 'sparse6' format usually have the extension {@code .s6}.
 *
 * @see    Sparse6GraphWriter
 * @author Barak Ugav
 */
public final class Sparse6GraphReader extends GraphIoUtils.AbstractIntGraphReader {

	/**
	 * Create a new reader.
	 */
	public Sparse6GraphReader() {}

	@Override
	IntGraphBuilder readIntoBuilderImpl(Reader reader) throws IOException {
		BufferedReader br = GraphIoUtils.bufferedReader(reader);
		IntGraphBuilder g = IntGraphFactory.undirected().allowSelfEdges().allowParallelEdges().newBuilder();

		String line = br.readLine();
		if (line == null)
			throw new IllegalArgumentException("empty file");

		if (line.startsWith(":")) {
			line = line.substring(":".length());

		} else if (line.startsWith(">>sparse6<<:")) { /* optional header */
			line = line.substring(">>sparse6<<:".length());
		} else {
			throw new IllegalArgumentException("Invalid header, expected ':' or '>>sparse6<<:'");
		}
		byte[] bytes = line.getBytes(GraphIoUtils.JGALGO_CHARSET);
		int cursor = 0;

		/* Read N(n) */
		IntIntPair nPair = Graph6.readNumberOfVertices(bytes, cursor);
		final int n = nPair.firstInt();
		cursor = nPair.secondInt();
		g.addVertices(range(n)); /* vertices ids are 0,1,2,...,n-1 */

		/* Read all edges */
		final int k = n == 0 ? n : log2ceil(n);
		Graph6.BitsReader bitsReader = new Graph6.BitsReader(bytes, cursor);
		edgesLoop: for (int v = 0;;) {
			if (!bitsReader.hasNext())
				break;
			boolean b = bitsReader.next();
			int x = 0;
			for (int i = 0; i < k; i++) {
				if (!bitsReader.hasNext())
					// throw new IllegalArgumentException("Unexpected end of bits");
					break edgesLoop; /* In decoding, an incomplete (b,x) pair at the end is discarded. */
				x <<= 1; /* bigendian */
				x |= bitsReader.next() ? 1 : 0;
			}

			if (b)
				v++;
			if (x > v) {
				v = x;
			} else if (v < n) {
				g.addEdge(v, x, g.edges().size());
			} else {
				bitsReader.skipToCurrentByteEnd(); /* skip last edge padding */
				if (bitsReader.hasNext())
					throw new IllegalArgumentException("invalid edge endpoint: " + v);
				break;
			}
		}

		if (br.readLine() != null)
			throw new IllegalArgumentException("Expected a single line in file");

		return g;
	}

}
