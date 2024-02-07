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
import java.util.Arrays;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.JGAlgoUtils;

/**
 * Write a graph in 'sparse6' format.
 *
 * <p>
 * 'sparse6' is a format for storing undirected graphs in a compact manner, using only printable ASCII characters. Files
 * in these format have text type and contain one line per graph. It is space-efficient for large sparse graphs. The
 * format support graphs with vertices numbered 0..n-1 only, where n is the number of vertices, and edges numbered
 * 0..m-1 only, where m is the number of edges. A sparse6 file contains the number of vertices, following by a list of
 * edges, encoded in bytes, each in range 63..126 which are the printable ASCII characters. Each byte encode 6 bits.
 *
 * <p>
 * The format does not support specifying the ids of the edges, therefore the writer will not write them. Nevertheless,
 * its possible to write and later read the graph using the {@link Sparse6GraphReader} class while keeping the edges
 * ids: if the edges in the written graph are numbered 0..m-1, and when they are iterated from {@code 0} to {@code m-1}
 * they are also ordered similar to (0,0),(1,0),(1,1),(2,0),(2,1),(2,2), then the reader will assigned the same ids to
 * the edges. . This way of keeping the edges ids is enforced by the writer by default, but can be disabled by calling
 * {@link #keepEdgesIds(boolean)} with {@code false}.
 *
 * <p>
 * Self edges and parallel edges are supported by the format. The full description of the format can be found
 * <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.html">here</a>, which also define the 'graph6' format, which
 * is used for dense graph. See {@link Graph6GraphWriter} for writing graphs in 'graph6' format.
 *
 * <p>
 * File with a graph in 'sparse6' format usually have the extension {@code .s6}.
 *
 * @see    Sparse6GraphReader
 * @author Barak Ugav
 */
public class Sparse6GraphWriter extends GraphIoUtils.AbstractGraphWriter<Integer, Integer> {

	private boolean keepEdgesIds = true;

	/**
	 * Create a new writer.
	 */
	public Sparse6GraphWriter() {}

	/**
	 * Enable or disable keeping the edges ids.
	 *
	 * <p>
	 * The format does not support specifying the ids of the edges, therefore the writer will not write them.
	 * Nevertheless, its possible to write and later read the graph using the {@link Sparse6GraphReader} class while
	 * keeping the edges ids: if the edges in the written graph are numbered 0..m-1, and when they are iterated from
	 * {@code 0} to {@code m-1} they are also ordered similar to (0,0),(1,0),(1,1),(2,0),(2,1),(2,2), then the reader
	 * will assigned the same ids to the edges.
	 *
	 * <p>
	 * If this option is enabled, which is also the default, if a graph is written by the writer and the edges iterated
	 * from {@code 0} to {@code m-1} are not ordered by the above order, then an exception will be thrown. If this
	 * option is disabled, the ids of the edges will be ignored and there is not guarantee that the reader will assign
	 * the same ids to the edges.
	 *
	 * @param enable {@code true} to enable keeping the edges ids, {@code false} to disable it
	 */
	public void keepEdgesIds(boolean enable) {
		this.keepEdgesIds = enable;
	}

	@Override
	void writeGraphImpl(Graph<Integer, Integer> graph, Writer writer) throws IOException {
		if (graph.isDirected())
			throw new IllegalArgumentException("the sparse6 format support undirected graphs only");
		final int n = graph.vertices().size();
		final int m = graph.edges().size();
		if (!range(n).equals(graph.vertices()))
			throw new IllegalArgumentException("the sparse6 format support graphs with vertices 0..n-1 only");
		if (keepEdgesIds && !range(m).equals(graph.edges()))
			throw new IllegalArgumentException("the sparse6 format support graphs with edges 0..m-1 only");

		Writer2 out = new Writer2(writer);
		out.append(">>sparse6<<:");

		/* write number of vertices */
		Graph6.writeNumberOfVertices(out, n);

		/* sort edges similar to (0,0),(1,0),(1,1),(2,0),(2,1),(2,2) */
		int[] edges;
		if (keepEdgesIds) {
			edges = range(m).toIntArray();
		} else {
			edges = graph instanceof IntGraph ? ((IntGraph) graph).edges().toIntArray()
					: graph.edges().stream().mapToInt(Integer::intValue).toArray();
		}
		/* bucket sort by target vertex */
		int[] edgesSorted = new int[m];
		int[] inDegree = new int[n];
		for (int e : edges) {
			Integer eObj = Integer.valueOf(e);
			int u = graph.edgeSource(eObj).intValue();
			int v = graph.edgeTarget(eObj).intValue();
			if (u < v) {
				int temp = u;
				u = v;
				v = temp;
			}
			inDegree[v]++;
		}
		int[] offset = inDegree;
		int s = 0;
		for (int v : range(n)) {
			int temp = offset[v];
			offset[v] = s;
			s += temp;
		}
		for (int e : edges) {
			Integer eObj = Integer.valueOf(e);
			int u = graph.edgeSource(eObj).intValue();
			int v = graph.edgeTarget(eObj).intValue();
			if (u < v) {
				int temp = u;
				u = v;
				v = temp;
			}
			edgesSorted[offset[v]++] = e;
		}
		{
			int[] temp = edges;
			edges = edgesSorted;
			edgesSorted = temp;
		}
		/* bucket sort by source vertex */
		int[] outDegree = inDegree;
		Arrays.fill(outDegree, 0);
		for (int e : edges) {
			Integer eObj = Integer.valueOf(e);
			int u = graph.edgeSource(eObj).intValue();
			int v = graph.edgeTarget(eObj).intValue();
			if (u < v) {
				int temp = u;
				u = v;
				v = temp;
			}
			outDegree[u]++;
		}
		offset = outDegree;
		s = 0;
		for (int v : range(n)) {
			int temp = offset[v];
			offset[v] = s;
			s += temp;
		}
		for (int e : edges) {
			Integer eObj = Integer.valueOf(e);
			int u = graph.edgeSource(eObj).intValue();
			int v = graph.edgeTarget(eObj).intValue();
			if (u < v) {
				int temp = u;
				u = v;
				v = temp;
			}
			edgesSorted[offset[u]++] = e;
		}
		{
			// int[] temp = edges;
			edges = edgesSorted;
			// edgesSorted = temp;
		}
		/* assert sorted */
		if (m > 0) {
			final int[] edges0 = edges;
			assert range(m - 1).allMatch(i -> {
				int e1 = edges0[i], e2 = edges0[i + 1];
				Integer e1Obj = Integer.valueOf(e1);
				Integer e2Obj = Integer.valueOf(e2);
				int u1 = graph.edgeSource(e1Obj).intValue();
				int v1 = graph.edgeTarget(e1Obj).intValue();
				int u2 = graph.edgeSource(e2Obj).intValue();
				int v2 = graph.edgeTarget(e2Obj).intValue();
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
					return u1 < u2;
				if (v1 != v2)
					return v1 < v2;
				return !keepEdgesIds || e1 < e2;
			});
		}

		if (keepEdgesIds)
			for (int i : range(edges.length))
				if (edges[i] != i)
					throw new IllegalArgumentException("edges ids must be 0,1,2,...,m-1 and ordered similar to "
							+ "(0,0),(1,0),(1,1),(2,0),(2,1),(2,2)");

		/* write all edges */
		Graph6.BitsWriter bitsWriter = new Graph6.BitsWriter(out);
		final int k = n == 0 ? 0 : JGAlgoUtils.log2ceil(n);
		int currentVertex = 0;
		for (int e : edges) {
			int u = graph.edgeSource(Integer.valueOf(e)).intValue();
			int v = graph.edgeTarget(Integer.valueOf(e)).intValue();
			if (u < v) {
				int temp = u;
				u = v;
				v = temp;
			}

			if (u == currentVertex) {
				bitsWriter.write(false);

			} else if (u == currentVertex + 1) {
				bitsWriter.write(true);
				currentVertex = u;

			} else {
				assert u > currentVertex + 1;
				bitsWriter.write(false);
				bitsWriter.write(u, k);
				currentVertex = u;
				bitsWriter.write(false);
			}

			bitsWriter.write(v, k);
		}

		/* add padding to a multiple of 6 */
		if (bitsWriter.paddingRequired()) {
			boolean zeroPrefix = true;
			zeroPrefix = zeroPrefix && Fastutil.list(2, 4, 8, 16).contains(n);
			zeroPrefix = zeroPrefix && !graph.outEdges(Integer.valueOf(n - 2)).isEmpty();
			zeroPrefix = zeroPrefix && graph.outEdges(Integer.valueOf(n - 1)).isEmpty();
			if (zeroPrefix)
				bitsWriter.write(false);
			while (bitsWriter.paddingRequired())
				bitsWriter.write(true);
		}

		/* terminate graph object with a new line */
		out.appendNewline();
	}

}
