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
import java.io.Reader;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightsInt;

/**
 * Read a graph in 'DIMACS' format.
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
 * The reader will identify the format automatically by looking at the header of the file. If the format contains edge
 * weights ('sp' format), the built graph will have edges {@linkplain WeightsInt integer weights} keys by "weight", or a
 * key chosen by the user using {@link #setEdgeWeightsKey(String)}. See {@link Graph#getEdgesWeights(String)}.
 *
 * @see    <a href="http://www.diag.uniroma1.it/challenge9/format.shtml#graph">DIMACS Graph Format</a>
 * @see    DimacsGraphWriter
 * @author Barak Ugav
 */
public class DimacsGraphReader extends GraphIoUtils.AbstractIntGraphReader {

	private String weightsKey = "weight";

	/**
	 * Create a new reader.
	 */
	public DimacsGraphReader() {}

	/**
	 * Sets the key of the edge weights that will be read.
	 *
	 * <p>
	 * When the reader reads a graph in the 'sp' format a {@link WeightsInt} weights will be added to the built graph.
	 * By default, the weights will be added with key "weight". Use this method to specify a different key.
	 *
	 * @param weightsKey the key of the edge weights that will be read
	 * @see              Graph#getEdgesWeights(String)
	 */
	public void setEdgeWeightsKey(String weightsKey) {
		this.weightsKey = Objects.requireNonNull(weightsKey);
	}

	@Override
	IntGraphBuilder readIntoBuilderImpl(Reader reader) {
		BufferedReader br = GraphIoUtils.bufferedReader(reader);
		IntGraphBuilder gb = IntGraphFactory.undirected().allowSelfEdges().newBuilder();
		boolean problemLineSeen = false;
		boolean hasWeights = false;
		IWeightsInt w = null;
		int n = -1, m = -1;

		for (String line : GraphIoUtils.lines(br, true)) {
			if (line.isEmpty())
				continue;
			switch (line.charAt(0)) {
				case 'c': /* comment line */
					if (!line.startsWith("c "))
						throw new IllegalArgumentException("comment line must start with 'c '");
					continue;

				case 'p': /* problem line */ {
					if (!line.startsWith("p "))
						throw new IllegalArgumentException("problem line must start with 'p '");
					if (problemLineSeen)
						throw new IllegalArgumentException("more than one problem line ('p' prefix) in file");
					problemLineSeen = true;

					String[] arr = line.split(" ");
					if (arr.length != 4)
						throw new IllegalArgumentException(
								"expected problem line in format: p (edge | sp) <NumVertices> <NumEdges>");
					String graphFormat = arr[1].toLowerCase();
					switch (graphFormat) {
						case "edge":
							hasWeights = false;
							break;
						case "sp":
							hasWeights = true;
							break;
						default:
							throw new IllegalArgumentException(
									"Support formats 'sp' and 'edge'. Unknown format: '" + graphFormat + "'");
					}

					try {
						n = Integer.parseInt(arr[2]);
						m = Integer.parseInt(arr[3]);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Invalid number of vertices or edges", e);
					}

					if (n < 0 || m < 0)
						throw new IllegalArgumentException("negative vertices/edges num: " + n + " " + m);
					gb.ensureEdgeCapacity(m);
					gb.addVertices(range(1, n + 1)); /* vertices are labeled as 1,2,3,4... */

					if (hasWeights)
						w = gb.addEdgesWeights(weightsKey, int.class);
					break;
				}

				case 'e': /* edge line */ {
					if (!problemLineSeen)
						throw new IllegalArgumentException("problem line ('p' prefix) was not seen yet");

					String[] arr = line.split(" ");
					if (!hasWeights) {
						if (arr.length != 3)
							throw new IllegalArgumentException(
									"expect edge definition: e <source_vertex> <destination_vertex>");
					} else {
						if (arr.length != 4)
							throw new IllegalArgumentException(
									"expect edge definition: e <source_vertex> <destination_vertex> <weight>");
					}

					/* parse edge source and target vertices */
					int source, target;
					try {
						source = Integer.parseInt(arr[1]);
						target = Integer.parseInt(arr[2]);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("edge must have 2 vertices as numbers", e);
					}
					final int e = gb.edges().size() + 1;
					gb.addEdge(source, target, e);

					/* parse edge weight */
					if (hasWeights) {
						int edgeWeight = -1;
						try {
							edgeWeight = Integer.parseInt(arr[3]);
						} catch (NumberFormatException ex) {
							throw new IllegalArgumentException("edge must have 2 vertices as numbers and a weight", ex);
						}
						w.set(e, edgeWeight);
					}
					break;
				}
				default:
					throw new IllegalArgumentException("unknown line: " + line);
			}
		}
		return gb;
	}

}
