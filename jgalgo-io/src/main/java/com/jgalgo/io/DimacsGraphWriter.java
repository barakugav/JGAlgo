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
import java.util.function.Function;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsShort;

/**
 * Write a graph using the DIMACS format.
 *
 * <p>
 * The DIMACS format is the graph format used by the 'Center of Discrete Mathematics and Theoretical Computer Science'
 * for their graph challenges. There are many sub-formats, but the most common are the 'edge' and 'sp' formats, which
 * are supported by this class. A DIMACS file contains a short header which specify the sub-format along with the number
 * of edges and vertices, followed by the edges themselves. The edges are specified by a pair of vertices, and in the
 * 'sp' format, also by a weight. The vertices are numbered from 1 to n, where n is the number of vertices, and
 * similarly the edges are numbered from 1 to m, where m is the number of edges. Only undirected graphs are supported by
 * this format.
 *
 * <p>
 * The <a href="https://github.com/akinanop/mvl-solver/wiki/DIMACS-Graph-Format">'edge' format</a> is the simplest, and
 * is used for unweighted undirected graphs. An example file is:
 *
 * <pre>
 * c this is a comment
 * c this is the graph with vertices {1,2,3,4,5} and edges {1=(1,2),2=(2,3),3=(2,4),4=(3,4),5=(4,5)}
 * p edge 5 5
 * e 1 2
 * e 2 3
 * e 2 4
 * e 3 4
 * e 4 5
 * </pre>
 *
 * <p>
 * The <a href="http://www.diag.uniroma1.it/challenge9/format.shtml#graph">'sp' format</a> is used for (integer)
 * weighted undirected graphs. An example file is:
 *
 * <pre>
 * c this is a comment
 * c this is the graph with vertices {1,2,3,4,5} and edges {1=(1,2),2=(2,3),3=(2,4),4=(3,4),5=(4,5)}
 * c the weights of the edges are {1=5,2=13,3=2,4=-7,5=0}
 * p sp 5 5
 * e 1 2 3
 * e 2 3 13
 * e 2 4 2
 * e 3 4 -7
 * e 4 5 0
 * </pre>
 *
 * <p>
 * By default, the writer will write the graph in the 'edge' format. To write the graph in the 'sp' format (with edges
 * weights), use the {@link #setEdgeWeights(String)} method to specify the key of the edge weights.
 *
 * @see    <a href="http://www.diag.uniroma1.it/challenge9/format.shtml#graph">DIMACS Graph Format</a>
 * @see    DimacsGraphReader
 * @author Barak Ugav
 */
public class DimacsGraphWriter implements GraphWriter<Integer, Integer> {

	private String weightsKey;

	/**
	 * Create a new writer.
	 */
	public DimacsGraphWriter() {}

	/**
	 * Set the key of the edge weights to write.
	 *
	 * <p>
	 * By default, the writer will write the graph in the 'edge' format, which is unweighted format. To write the graph
	 * in the 'sp' format (with weights), use this method to specify the key of the edge weights. See the class
	 * documentation for more details.
	 *
	 * <p>
	 * The weights must be integer, namely the {@link Weights} must be {@link WeightFunctionInt}. It can be any of the
	 * following: {@link WeightsByte}, {@link WeightsShort} or {@link WeightsInt}.
	 *
	 * @see              Graph#getEdgesWeights(String)
	 * @param weightsKey the key of the edge weights to write, or {@code null} to write the graph in the 'edge' format
	 *                       (unweighted)
	 */
	public void setEdgeWeights(String weightsKey) {
		this.weightsKey = weightsKey;
	}

	@Override
	public void writeGraph(Graph<Integer, Integer> graph, Writer writer) {
		if (graph.isDirected())
			throw new IllegalArgumentException("the DIMACS format support undirected graphs only");
		final int n = graph.vertices().size();
		final int m = graph.edges().size();
		if (!range(1, n + 1).equals(graph.vertices()))
			throw new IllegalArgumentException("the DIMACS format support graphs with vertices 1..n only");
		if (!range(1, m + 1).equals(graph.edges()))
			throw new IllegalArgumentException("the DIMACS format support graphs with edges 1..m only");

		final boolean hasWeights = weightsKey != null;
		Function<Integer, String> w = null;
		if (hasWeights) {
			Weights<Integer, ?> w0 = graph.getEdgesWeights(weightsKey);
			if (w0 == null)
				throw new IllegalArgumentException("the graph does not have weights for key: " + weightsKey);
			if (!(w0 instanceof WeightFunctionInt))
				throw new IllegalArgumentException(
						"Only integer weights are supported. Unsupported: " + w0.getClass().getName());
			@SuppressWarnings("unchecked")
			WeightFunctionInt<Integer> w1 = (WeightFunctionInt<Integer>) w0;
			w = e -> Integer.toString(w1.weightInt(e));
		}

		Writer2 out = new Writer2(writer);
		try {
			out.append("c DIMACS written graph by JGAlgo").appendNewline();

			if (hasWeights) {
				out.append("p sp " + n + " " + m).appendNewline();
			} else {
				out.append("p edge " + n + " " + m).appendNewline();
			}

			// writes all edges, optional with weights
			for (int e0 = 1; e0 <= m; e0++) {
				Integer e = Integer.valueOf(e0);
				/* e {source} {target} */
				out.append("e ").append(graph.edgeSource(e)).append(' ').append(graph.edgeTarget(e));
				/* e {source} {target} {weight} */
				if (hasWeights)
					out.append(' ').append(w.apply(e));
				out.appendNewline();
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
