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

/**
 * Read a graph in Graph6 format.
 *
 * <p>
 * 'graph6' is a format for storing undirected graphs in a compact manner, using only printable ASCII characters. Files
 * in these formats have text type and contain one line per graph. It is suitable for small graphs, or large dense
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
 * Self edges and parallel edges are not supported by the format.
 *
 * @see    Graph6GraphWriter
 * @author Barak Ugav
 */
public class Graph6GraphReader extends GraphIoUtils.AbstractIntGraphReader {

	/**
	 * Create a new reader.
	 */
	public Graph6GraphReader() {}

	@Override
	public IntGraphBuilder readIntoBuilderImpl(Reader reader) throws IOException {
		BufferedReader br = GraphIoUtils.bufferedReader(reader);
		IntGraphBuilder g = IntGraphBuilder.newUndirected();

		String line = br.readLine();

		/* optional header */
		if (line.startsWith(">>graph6<<"))
			line = line.substring(">>graph6<<".length());
		byte[] bytes = line.getBytes();
		int cursor = 0;

		/* Read N(n) */
		final int n;
		if (bytes[cursor] != 126) {
			/* n is a single byte number */
			n = checkByte(bytes[cursor++]) - 63;

		} else if (++cursor + 2 >= bytes.length) {
			throw new IllegalArgumentException("Invalid number of vertices, expected 126 and further bytes");
		} else if (bytes[cursor] != 126) {
			/* n is a 3 byte number */
			int b1 = checkByte(bytes[cursor++]) - 63;
			int b2 = checkByte(bytes[cursor++]) - 63;
			int b3 = checkByte(bytes[cursor++]) - 63;
			int n0 = 0;
			n0 |= b1 << 12;
			n0 |= b2 << 6;
			n0 |= b3 << 0;
			n = n0;

		} else if (++cursor + 5 >= bytes.length) {
			throw new IllegalArgumentException("Invalid number of vertices, expected 126 126 and further bytes");
		} else {
			/* n is a 6 byte number */
			int b1 = checkByte(bytes[cursor++]) - 63;
			int b2 = checkByte(bytes[cursor++]) - 63;
			int b3 = checkByte(bytes[cursor++]) - 63;
			int b4 = checkByte(bytes[cursor++]) - 63;
			int b5 = checkByte(bytes[cursor++]) - 63;
			int b6 = checkByte(bytes[cursor++]) - 63;
			long n0 = 0;
			n0 |= ((long) b1) << 30;
			n0 |= ((long) b2) << 24;
			n0 |= ((long) b3) << 18;
			n0 |= ((long) b4) << 12;
			n0 |= ((long) b5) << 6;
			n0 |= ((long) b6) << 0;
			if (n0 > Integer.MAX_VALUE)
				throw new IllegalArgumentException("n is too big: " + n0);
			n = (int) n0;
		}
		g.expectedVerticesNum(n);
		for (int v = 0; v < n; v++)
			g.addVertex(v); /* vertices ids are 0,1,2,...,n-1 */

		/* Read all edges R(x) */
		final long maxEdgesNum = (n * ((long) n - 1)) / 2;
		final long bytesToRead = (maxEdgesNum + 5) / 6; /* div round up */
		if (cursor + bytesToRead != bytes.length)
			throw new IllegalArgumentException("Unexpected number of bytes for edges bit vector");
		assert bytesToRead <= Integer.MAX_VALUE;
		final int edgesBase = cursor;
		for (int i = 0; i < (int) bytesToRead; i++)
			bytes[edgesBase + i] = (byte) (checkByte(bytes[edgesBase + i]) - 63);
		int bitNum = 0;
		for (int u : range(1, n)) {
			for (int v : range(0, u)) {
				assert u * (u - 1) / 2 + v == bitNum;
				int byteIdx = bitNum / 6;
				int bitIdx = 5 - (bitNum % 6); /* bigendian */
				boolean edgeExist = (bytes[edgesBase + byteIdx] & (1 << bitIdx)) != 0;
				if (edgeExist)
					g.addEdge(u, v, g.edges().size());
				bitNum++;
			}
		}

		if (br.readLine() != null)
			throw new IllegalArgumentException("Expected a single line in file");

		return g;
	}

	private static byte checkByte(byte b) {
		if (!(63 <= b && b <= 126))
			throw new IllegalArgumentException("Invalid byte, not in range [63, 126]: " + b);
		return b;
	}

}
