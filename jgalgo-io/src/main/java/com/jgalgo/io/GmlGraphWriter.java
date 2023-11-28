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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.Pair;

public class GmlGraphWriter implements GraphWriter {

	@Override
	public void writeGraph(IntGraph graph, Writer writer) {
		if (graph.isDirected())
			throw new IllegalArgumentException("GML format support undirected graphs only");

		try {
			List<Pair<String, WeightsStringifier>> vWeights = new ArrayList<>();
			List<Pair<String, WeightsStringifier>> eWeights = new ArrayList<>();
			for (String key : graph.getVerticesWeightsKeys()) {
				IWeights<?> w = graph.getVerticesIWeights(key);
				Gml.checkValidWeightsKey(key);
				vWeights.add(Pair.of(key, WeightsStringifier.newInstance(w, "\"", "\"")));
			}
			for (String key : graph.getEdgesWeightsKeys()) {
				IWeights<?> w = graph.getEdgesIWeights(key);
				Gml.checkValidWeightsKey(key);
				eWeights.add(Pair.of(key, WeightsStringifier.newInstance(w, "\"", "\"")));
			}

			writer.append("graph [").append(System.lineSeparator());
			for (int v : graph.vertices()) {
				writer.append("\tnode [").append(System.lineSeparator());
				writer.append("\t\tid ").append(Integer.toString(v)).append(System.lineSeparator());
				for (Pair<String, WeightsStringifier> weights : vWeights) {
					writer.append("\t\t");
					String key = weights.first();
					String weightStr = weights.right().getWeightAsString(v);
					writer.append(key).append(' ').append(weightStr);
					writer.append(System.lineSeparator());
				}
				writer.append("\t]").append(System.lineSeparator());
			}
			for (int e : graph.edges()) {
				writer.append("\tedge [").append(System.lineSeparator());
				writer.append("\t\tid ").append(Integer.toString(e)).append(System.lineSeparator());
				writer.append("\t\tsource ").append(Integer.toString(graph.edgeSource(e)))
						.append(System.lineSeparator());
				writer.append("\t\ttarget ").append(Integer.toString(graph.edgeTarget(e)))
						.append(System.lineSeparator());
				for (Pair<String, WeightsStringifier> weights : eWeights) {
					writer.append("\t\t");
					String key = weights.first();
					String weightStr = weights.right().getWeightAsString(e);
					writer.append(key).append(' ').append(weightStr);
					writer.append(System.lineSeparator());
				}
				writer.append("\t]").append(System.lineSeparator());
			}
			writer.append(']').append(System.lineSeparator());

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
