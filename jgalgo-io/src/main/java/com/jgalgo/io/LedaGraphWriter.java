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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;

/**
 * Write a graph in 'LEDA' format.
 *
 * <p>
 * The <a href=https://www.algorithmic-solutions.info/leda_guide/graphs/leda_native_graph_fileformat.html>LEDA
 * format</a> is a simple format for both directed and undirected graphs, used by the <a
 * href=https://en.wikipedia.org/wiki/Library_of_Efficient_Data_types_and_Algorithms>LEDA</a> library. Vertices are
 * numbered from 1 to n, and edges are numbered from 1 to m. It support a single weight for vertices and a single weight
 * for edges. The weights can be any primitive, or a string.
 *
 * <p>
 * By default, the writer will write the graph without any weights. Its possible to write one of the vertices weights
 * and one of the edges weights in the format. Use the {@link #setVerticesWeightsKey(String)} and
 * {@link #setEdgesWeightsKey(String)} methods to specify the keys of the weights to write.
 *
 * @see    LedaGraphReader
 * @author Barak Ugav
 */
public class LedaGraphWriter extends GraphIoUtils.AbstractGraphWriter<Integer, Integer> {

	private String verticesWeightsKey;
	private String edgesWeightsKey;

	/**
	 * Create a new writer.
	 */
	public LedaGraphWriter() {}

	/**
	 * Set the key of the vertices weights to write.
	 *
	 * <p>
	 * By default, the writer will write the graph without any weights. Use this method to specify the key of the
	 * vertices weights to write.
	 *
	 * @see                      Graph#getVerticesWeights(String)
	 * @param verticesWeightsKey the key of the vertices weights to write, or {@code null} to write no vertices weights
	 */
	public void setVerticesWeightsKey(String verticesWeightsKey) {
		this.verticesWeightsKey = verticesWeightsKey;
	}

	/**
	 * Set the key of the edges weights to write.
	 *
	 * <p>
	 * By default, the writer will write the graph without any weights. Use this method to specify the key of the edges
	 * weights to write.
	 *
	 * @see                   Graph#getEdgesWeights(String)
	 * @param edgesWeightsKey the key of the edges weights to write, or {@code null} to write no edges weights
	 */
	public void setEdgesWeightsKey(String edgesWeightsKey) {
		this.edgesWeightsKey = edgesWeightsKey;
	}

	@Override
	public void writeGraphImpl(Graph<Integer, Integer> graph, Writer writer) throws IOException {
		final int n = graph.vertices().size();
		final int m = graph.edges().size();
		if (!range(1, n + 1).equals(graph.vertices()))
			throw new IllegalArgumentException("the LEDA format support graphs with vertices 1..n only");
		if (!range(1, m + 1).equals(graph.edges()))
			throw new IllegalArgumentException("the LEDA format support graphs with edges 1..m only");

		Weights<Integer, ?> verticesWeights = null;
		if (verticesWeightsKey != null) {
			verticesWeights = graph.getVerticesWeights(verticesWeightsKey);
			if (verticesWeights == null)
				throw new IllegalArgumentException("vertices weights key '" + verticesWeightsKey + "' not found");
		}
		Weights<Integer, ?> edgesWeights = null;
		if (edgesWeightsKey != null) {
			edgesWeights = graph.getEdgesWeights(edgesWeightsKey);
			if (edgesWeights == null)
				throw new IllegalArgumentException("edges weights key '" + edgesWeightsKey + "' not found");
		}

		Writer2 out = new Writer2(writer);
		out.append("# header section").appendNewline();
		out.append("LEDA.GRAPH").appendNewline();
		out.append(weightsToLedaType(verticesWeights)).appendNewline();
		out.append(weightsToLedaType(edgesWeights)).appendNewline();
		out.append(graph.isDirected() ? "-1" : "-2").appendNewline();

		out.append("# section nodes/vertices").appendNewline();
		out.append(n).appendNewline();
		if (verticesWeights == null) {
			for (int vertex = 1; vertex <= n; vertex++)
				out.append("|{}|").appendNewline();
		} else {
			WeightsStringifier<Integer> weightsStringifier = WeightsStringifier.newInstance(verticesWeights);
			for (int v0 = 1; v0 <= n; v0++) {
				Integer v = Integer.valueOf(v0);
				String weightStr = weightsStringifier.weightStr(v);
				out.append("|{").append(weightStr).append("}|").appendNewline();
			}
		}

		out.append("# section edges").appendNewline();
		out.append(m).appendNewline();
		WeightsStringifier<Integer> weightsStringifier =
				edgesWeights == null ? null : WeightsStringifier.newInstance(edgesWeights);
		for (int e0 = 1; e0 <= m; e0++) {
			Integer e = Integer.valueOf(e0);
			out.append(graph.edgeSource(e)).append(' ');
			out.append(graph.edgeTarget(e)).append(' ');
			out.append(/* twin edge */ '0').append(' ');
			if (weightsStringifier == null) {
				out.append("|{}|").appendNewline();
			} else {
				String weightStr = weightsStringifier.weightStr(e);
				out.append("|{").append(weightStr).append("}|").appendNewline();
			}
		}
	}

	private static String weightsToLedaType(Weights<?, ?> weights) {
		if (weights == null)
			return "void";
		if (weights instanceof WeightsByte)
			return "byte";
		if (weights instanceof WeightsShort)
			return "short";
		if (weights instanceof WeightsInt)
			return "int";
		if (weights instanceof WeightsLong)
			return "long";
		if (weights instanceof WeightsFloat)
			return "float";
		if (weights instanceof WeightsDouble)
			return "double";
		if (weights instanceof WeightsBool)
			return "bool";
		if (weights instanceof WeightsChar)
			return "char";
		assert weights instanceof WeightsObj;
		return "string";
	}

}
