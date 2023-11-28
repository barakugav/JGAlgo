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
import com.jgalgo.graph.IWeightsInt;

public class DimacsGraphWriter implements GraphWriter<Integer, Integer> {

	@Override
	public void writeGraph(Graph<Integer, Integer> graph, Writer writer) {
		if (graph.isDirected())
			throw new IllegalArgumentException("the DIMACS format support undirected graphs only");
		final int numVertices = graph.vertices().size();
		final int numEdges = graph.edges().size();
		if (!range(1, numVertices + 1).equals(graph.vertices()))
			throw new IllegalArgumentException("the DIMACS format support graphs with vertices 1..n only");
		if (!range(1, numEdges + 1).equals(graph.edges()))
			throw new IllegalArgumentException("the DIMACS format support graphs with edges 1..m only");

		try {
			writer.append("c DIMACS written graph by JGAlgo").append(System.lineSeparator());
			final IWeightsInt w = graph.getEdgesWeights("weightsEdges");
			final boolean hasWeights = w != null;

			if (hasWeights) {
				writer.append("p sp " + numVertices + " " + numEdges).append(System.lineSeparator());
			} else {
				writer.append("p edge " + numVertices + " " + numEdges).append(System.lineSeparator());
			}

			// writes all edges, optional with weights
			for (int e = 1; e <= numEdges; e++) {
				/* e {source} {target} */
				writer.append("e ").append(Integer.toString(graph.edgeSource(e))).append(' ')
						.append(Integer.toString(graph.edgeTarget(e)));
				/* e {source} {target} {weight} */
				if (hasWeights)
					writer.append(' ').append(Integer.toString(w.get(e)));
				writer.append(System.lineSeparator());
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
