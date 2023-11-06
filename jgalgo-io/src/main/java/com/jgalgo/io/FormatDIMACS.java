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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.internal.util.Range;

class FormatDIMACS implements GraphFormat {

	private FormatDIMACS() {}

	static final FormatDIMACS Instance = new FormatDIMACS();

	@Override
	public GraphWriter newWriter() {
		return new WriterImpl();
	}

	@Override
	public GraphReader newReader() {
		return new ReaderImpl();
	}

	private static final List<String> FILE_EXTENSIONS = List.of("col", "gr");

	@Override
	public List<String> getFileExtensions() {
		return FILE_EXTENSIONS;
	}

	private static class WriterImpl implements GraphWriter {

		@Override
		public void writeGraph(IntGraph graph, Writer writer) {
			if (graph.isDirected())
				throw new IllegalArgumentException("the DIMACS format support undirected graphs only");
			final int numVertices = graph.vertices().size();
			final int numEdges = graph.edges().size();
			if (!Range.of(1, numVertices + 1).equals(graph.vertices()))
				throw new IllegalArgumentException("the DIMACS format support graphs with vertices 1..n only");
			if (!Range.of(1, numEdges + 1).equals(graph.edges()))
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

	private static class ReaderImpl implements GraphReader {

		/**
		 * Support 2 DIMACS formats:<br>
		 * The "DIMACS edge format" and "DIMACS sp format"<br>
		 * <br>
		 * 1. Basic DIMACS format: <br>
		 * see https://github.com/akinanop/mvl-solver/wiki/DIMACS-Graph-Format
		 *
		 * <pre>
		 * p edge &lt;NumVertices&gt; &lt;NumEdges&gt;
		 * e &lt;VertexName1&gt; &lt;VertexName2&gt;
		 * Example:
		 * c this is the graph with vertices {1,2,3,4,5} and edges {(1,2),(2,3),(2,4),(3,4),(4,5)}
		 * p edge 5 5
		 * e 1 2
		 * e 2 3
		 * e 2 4
		 * e 3 4
		 * e 4 5
		 * </pre>
		 *
		 * <p>
		 * 2. Shortest path format (with weights)<br>
		 * Two assumptions:<br>
		 * (1) Undirected graph.<br>
		 * (2) Weights are integers.<br>
		 * <br>
		 * The .gr files:<br>
		 * see http://www.diag.uniroma1.it/challenge9/format.shtml#graph
		 *
		 * <pre>
		 * c
		 * p sp n nm
		 * a u v w
		 * </pre>
		 */
		@Override
		public IntGraphBuilder readIntoBuilder(Reader reader) {
			try (BufferedReader br =
					reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
				IntGraphBuilder gb = IntGraphBuilder.newUndirected();
				IWeightsInt w = null;
				int verticesNum = -1;
				int edgesNum = -1;
				boolean hasWeights = false;
				boolean problemLineSeen = false;

				for (String line; (line = br.readLine()) != null;) {
					line = line.trim();
					if (line.isEmpty())
						continue;

					// replace multiple spaces with just one space
					line = line.replaceAll("\\s+", " ");

					char firstChar = line.charAt(0);
					switch (firstChar) {
						case 'c': /* comment line */
							continue;

						case 'p': /* problem line */ {
							if (problemLineSeen)
								throw new IllegalArgumentException("more than one problem line ('p' prefix) in file");
							problemLineSeen = true;

							String[] arr = line.split(" ");
							if (arr.length != 4)
								throw new IllegalArgumentException(
										"p lines must have 4 parameters: p edge <NumVertices> <NumEdges>"
												+ " or p sp <NumVertices> <NumEdges>");
							String graphFormat = arr[1].toLowerCase();
							switch (graphFormat) {
								case "edge":
									hasWeights = false;
									break;
								case "sp":
									hasWeights = true;
									break;
								default:
									throw new IllegalArgumentException("support only: p edge <NumVertices> <NumEdges>"
											+ " or p sp <NumVertices> <NumEdges>");
							}

							try {
								verticesNum = Integer.parseInt(arr[2]);
								edgesNum = Integer.parseInt(arr[3]);
							} catch (Exception e) {
								throw new IllegalArgumentException("expect numbers: p edge <NumVertices> <NumEdges>"
										+ " or p sp <NumVertices> <NumEdges>", e);
							}

							if (verticesNum < 0 || edgesNum < 0)
								throw new IllegalArgumentException(
										"negative vertices/edges num: " + verticesNum + " " + edgesNum);
							gb.expectedVerticesNum(verticesNum);
							gb.expectedEdgesNum(edgesNum);

							if (graphFormat.equals("sp"))
								w = gb.addEdgesWeights("weightsEdges", int.class);
							for (int v = 1; v <= verticesNum; v++)
								gb.addVertex(v); // vertices are labeled as 1,2,3,4...
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
							int vertexSource = -1;
							int vertexTarget = -1;
							try {
								vertexSource = Integer.parseInt(arr[1]);
								vertexTarget = Integer.parseInt(arr[2]);
							} catch (Exception e) {
								throw new IllegalArgumentException("edge must have 2 vertices as numbers", e);
							}
							if (vertexSource < 1 || vertexSource > verticesNum || vertexTarget < 1
									|| vertexTarget > verticesNum)
								throw new IllegalArgumentException("vertex number must be between 1 and num_vertices");
							final int e = gb.edges().size() + 1;
							gb.addEdge(vertexSource, vertexTarget, e);

							/* parse edge weight */
							if (hasWeights) {
								int edgeWeight = -1;
								try {
									edgeWeight = Integer.parseInt(arr[3]);
								} catch (Exception ex) {
									throw new IllegalArgumentException(
											"edge must have 2 vertices as numbers and a weight", ex);
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
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

}
