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
import java.io.UncheckedIOException;
import java.io.Writer;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsFloat;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IWeightsShort;
import com.jgalgo.graph.Weights;

public class LedaGraphWriter implements GraphWriter<Integer, Integer> {

	@Override
	public void writeGraph(Graph<Integer, Integer> graph, Writer writer) {
		final int numVertices = graph.vertices().size();
		final int numEdges = graph.edges().size();
		if (!range(1, numVertices + 1).equals(graph.vertices()))
			throw new IllegalArgumentException("the LEDA format support graphs with vertices 1..n only");
		if (!range(1, numEdges + 1).equals(graph.edges()))
			throw new IllegalArgumentException("the LEDA format support graphs with edges 1..m only");

		try {
			String verticesWeightsType; // can be void, string, int etc
			Weights<Integer, ?> verticesWeights; // weights for vertices
			if (graph.getVerticesWeightsKeys().isEmpty()) {
				verticesWeightsType = "void";
				verticesWeights = null;
			} else {
				// for now, take the first weights collection
				String key = graph.getVerticesWeightsKeys().iterator().next();
				verticesWeights = graph.getVerticesWeights(key);
				if (verticesWeights instanceof IWeightsInt) {
					verticesWeightsType = "int";
				} else if (verticesWeights instanceof IWeightsShort) {
					verticesWeightsType = "short";
				} else if (verticesWeights instanceof IWeightsLong) {
					verticesWeightsType = "long";
				} else if (verticesWeights instanceof IWeightsFloat) {
					verticesWeightsType = "float";
				} else if (verticesWeights instanceof IWeightsDouble) {
					verticesWeightsType = "double";
				} else {
					verticesWeightsType = "string";
				}
			}

			String edgesWeightsType; // can be void, string, int etc
			Weights<Integer, ?> edgesWeights; // weights for edges
			if (graph.getEdgesWeightsKeys().isEmpty()) {
				edgesWeightsType = "void";
				edgesWeights = null;
			} else {
				// for now, take the first weights collection
				String key = graph.getEdgesWeightsKeys().iterator().next();
				edgesWeights = graph.getEdgesWeights(key);
				if (edgesWeights instanceof IWeightsInt) {
					edgesWeightsType = "int";
				} else if (edgesWeights instanceof IWeightsShort) {
					edgesWeightsType = "short";
				} else if (edgesWeights instanceof IWeightsLong) {
					edgesWeightsType = "long";
				} else if (edgesWeights instanceof IWeightsFloat) {
					edgesWeightsType = "float";
				} else if (edgesWeights instanceof IWeightsDouble) {
					edgesWeightsType = "double";
				} else {
					edgesWeightsType = "string";
				}
			}

			writer.append("LEDA.GRAPH").append(System.lineSeparator());
			writer.append(verticesWeightsType).append(System.lineSeparator()); // void/string/int etc
			writer.append(edgesWeightsType).append(System.lineSeparator()); // void/string/int etc
			writer.append(graph.isDirected() ? "-1" : "-2").append(System.lineSeparator());

			writer.append("# section nodes/vertices").append(System.lineSeparator());
			writer.append(Integer.toString(numVertices)).append(System.lineSeparator());
			// write all vertices info
			// --> LEDA expects 1..numVertices
			// for (int ix = 1; ix <= numVertices; ix++)
			// but just in case, we are consistent with our labels etc
			if (verticesWeights == null) {
				for (int vertex = 1; vertex <= numVertices; vertex++)
					writer.append("|{}|").append(System.lineSeparator());
			} else {
				WeightsStringifier<Integer> weightsStringer = WeightsStringifier.newInstance(verticesWeights);
				for (int vertex = 1; vertex <= numVertices; vertex++) {
					String weightStr = weightsStringer.getWeightAsString(vertex);
					writer.append("|{").append(weightStr).append("}|").append(System.lineSeparator());
				}
			}

			writer.append("# section edges").append(System.lineSeparator());
			writer.append(Integer.toString(numEdges)).append(System.lineSeparator());
			// write all edges info
			WeightsStringifier<Integer> weightsStringer =
					edgesWeights != null ? WeightsStringifier.newInstance(edgesWeights) : null;
			for (int edge = 1; edge <= numEdges; edge++) {
				writer.append(Integer.toString(graph.edgeSource(edge))).append(' ');
				writer.append(Integer.toString(graph.edgeTarget(edge))).append(' ');
				writer.append(/* twin edge */ '0').append(' ');
				if (weightsStringer == null) {
					writer.append("|{}|").append(System.lineSeparator());
				} else {
					String weightStr = weightsStringer.getWeightAsString(edge);
					writer.append("|{").append(weightStr).append("}|").append(System.lineSeparator());
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
