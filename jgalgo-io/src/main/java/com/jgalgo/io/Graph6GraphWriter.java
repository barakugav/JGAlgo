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
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import com.jgalgo.graph.Graph;

/**
 * Write a graph in 'graph6' format.
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
 * The format does not support specifying the ids of the edges, therefore the writer will not write them. Nevertheless,
 * its possible to write and later read the graph using the {@link Graph6GraphReader} class while keeping the edges ids:
 * if the edges in the written graph are numbered 0..m-1, and when they are iterated from {@code 0} to {@code m-1} they
 * are also ordered by the order of the bit vector, then the reader will assigned the same ids to the edges. The order
 * of the bit vector and the format details can be found
 * <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.html">here</a>. This way of keeping the edges ids is
 * enforced by the writer by default, but can be disabled by calling {@link #keepEdgesIds(boolean)} with {@code false}.
 *
 * <p>
 * The 'graph6' format is efficient for dense graph, for dense graphs the {@linkplain Sparse6GraphWriter 'sparse6'
 * format} should be used.
 *
 * <p>
 * Self edges and parallel edges are not supported by the format.
 *
 * <p>
 * File with a graph in 'graph6' format usually have the extension {@code .g6}.
 *
 * @see    Graph6GraphReader
 * @author Barak Ugav
 */
public class Graph6GraphWriter extends GraphIoUtils.AbstractGraphWriter<Integer, Integer> {

	private boolean keepEdgesIds = true;

	/**
	 * Create a new writer.
	 */
	public Graph6GraphWriter() {}

	/**
	 * Enable or disable keeping the edges ids.
	 *
	 * <p>
	 * The format does not support specifying the ids of the edges, therefore the writer will not write them.
	 * Nevertheless, its possible to write and later read the graph using the {@link Graph6GraphReader} class while
	 * keeping the edges ids: if the edges in the written graph are numbered 0..m-1, and when they are iterated from
	 * {@code 0} to {@code m-1} they are also ordered by the order of the bit vector, then the reader will assigned the
	 * same ids to the edges. The order of the bit vector and the format details can be found
	 * <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.html">here</a>.
	 *
	 * <p>
	 * If this option is enabled, which is also the default, if a graph is written by the writer and the edges iterated
	 * from {@code 0} to {@code m-1} are not ordered by the order of the bit vector, then an exception will be thrown.
	 * If this option is disabled, the ids of the edges will be ignored and there is not guarantee that the reader will
	 * assign the same ids to the edges.
	 *
	 * @param enable {@code true} to enable keeping the edges ids, {@code false} to disable it
	 */
	public void keepEdgesIds(boolean enable) {
		this.keepEdgesIds = enable;
	}

	@Override
	void writeGraphImpl(Graph<Integer, Integer> graph, Writer writer) throws IOException {
		if (graph.isDirected())
			throw new IllegalArgumentException("the graph6 format support undirected graphs only");
		final int n = graph.vertices().size();
		final int m = graph.edges().size();
		if (!range(n).equals(graph.vertices()))
			throw new IllegalArgumentException("the graph6 format support graphs with vertices 0..n-1 only");
		if (keepEdgesIds && !range(m).equals(graph.edges()))
			throw new IllegalArgumentException("the graph6 format support graphs with edges 0..m-1 only");

		Writer2 out = new Writer2(writer);
		out.append(">>graph6<<");

		/* write number of vertices */
		assert n >= 0;
		if (n <= 62) {
			/* n is a single byte number */
			out.appendByte((byte) (n + 63));

		} else if (n <= 258047) {
			/* n is a 3 byte number */
			int b1 = (n >> 12) & ((1 << 6) - 1);
			int b2 = (n >> 6) & ((1 << 6) - 1);
			int b3 = (n >> 0) & ((1 << 6) - 1);
			out.appendByte((byte) 126);
			out.appendByte((byte) (b1 + 63));
			out.appendByte((byte) (b2 + 63));
			out.appendByte((byte) (b3 + 63));

		} else {
			/* n is a 6 byte number */
			int b1 = (n >> 30) & ((1 << 6) - 1);
			int b2 = (n >> 24) & ((1 << 6) - 1);
			int b3 = (n >> 18) & ((1 << 6) - 1);
			int b4 = (n >> 12) & ((1 << 6) - 1);
			int b5 = (n >> 6) & ((1 << 6) - 1);
			int b6 = (n >> 0) & ((1 << 6) - 1);
			out.appendByte((byte) 126);
			out.appendByte((byte) 126);
			out.appendByte((byte) (b1 + 63));
			out.appendByte((byte) (b2 + 63));
			out.appendByte((byte) (b3 + 63));
			out.appendByte((byte) (b4 + 63));
			out.appendByte((byte) (b5 + 63));
			out.appendByte((byte) (b6 + 63));
		}

		/* write all edges as bytes, 6 bits each */
		final long maxNumberOfEdges = n * ((long) n - 1) / 2;
		final long bytesToWrite = (maxNumberOfEdges + 5) / 6; /* div round up */

		if (bytesToWrite > Integer.MAX_VALUE) {
			/*
			 * TODO: we should be able to support this. Instead of allocating an array of all the bytes, we can write
			 * directly to the output.
			 */
			throw new IllegalArgumentException("too many vertices, the graph6 format support up to 2^36 vertices, "
					+ "but this implementation does not support more than 2^17.5");
		}

		byte[] bytes = new byte[(int) bytesToWrite];
		int maxEdgeBit = -1;
		Set<Integer> edges = keepEdgesIds ? range(m) : graph.edges();
		for (Integer e : edges) {
			int u = graph.edgeSource(e).intValue();
			int v = graph.edgeTarget(e).intValue();
			if (u == v)
				throw new IllegalArgumentException("self edges are not supported");
			if (u < v) {
				int tmp = u;
				u = v;
				v = tmp;
			}
			int edgeBit = u * (u - 1) / 2 + v;
			int byteIdx = edgeBit / 6;
			int bitIdx = 5 - (edgeBit % 6); /* bigendian */
			if ((bytes[byteIdx] & (1 << bitIdx)) != 0)
				throw new IllegalArgumentException("parallel edges are not supported");
			if (keepEdgesIds) {
				if (edgeBit < maxEdgeBit)
					throw new IllegalArgumentException("edges ids must be 0,1,2,...,m-1 and ordered similar to "
							+ "(0,1),(0,2),(1,2),(0,3),(1,3),(2,3),...,(n-2,n-1)");
				maxEdgeBit = edgeBit;
			}
			bytes[byteIdx] |= 1 << bitIdx;
		}
		for (int i = 0; i < bytes.length; i++)
			bytes[i] += 63;
		out.appendBytes(bytes);

		/* terminate graph object with a new line */
		out.appendNewline();
	}

}
