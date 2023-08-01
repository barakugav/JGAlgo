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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.Weights;

class FormatDIMACS implements GraphFormat {

	private FormatDIMACS() {
	}

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
		public void writeGraph(Graph graph, Writer writer) {
			try {
				writer.append("c DIMACS written graph by JGAlgo") //
						.append(System.lineSeparator());
				final int num_vertices = graph.vertices().size();
				final int num_edges = graph.edges().size();
				final Weights.Int w = graph.getEdgesWeights("weightsEdges");
				final boolean hash_weights = w != null;
				if (hash_weights)
					writer.append("p sp " + num_vertices + " " + num_edges) //
							.append(System.lineSeparator());
				else
					writer.append("p edge " + num_vertices + " " + num_edges) //
							.append(System.lineSeparator());

				// writes all edges, optional with weights
				for (int e : graph.edges()) {
					writer.append("e " + graph.edgeSource(e) + " " + graph.edgeTarget(e));
					if (hash_weights) {
						int the_weight = w.getInt(e);
						writer.append(" " + the_weight);
					}
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
		p edge <NumVertices> <NumEdges>
		e <VertexName1> <VertexName2>
		Example:
		c this is the graph with vertices {1,2,3,4,5} and edges {(1,2),(2,3),(2,4),(3,4),(4,5)}
		p edge 5 5
		e 1 2
		e 2 3
		e 2 4
		e 3 4
		e 4 5
		 * </pre>
		 * 
		 * 2. Shortest path format (with weights)<br>
		 * Two assumptions:<br>
		 * (1) Undirected graph.<br>
		 * (2) Weights are integers.<br>
		 * <br>
		 * The .gr files:<br>
		 * see http://www.diag.uniroma1.it/challenge9/format.shtml#graph
		 * 
		 * <pre>
			c
			p sp n nm
			a u v w
		 * </pre>
		 */
		@Override
		public GraphBuilder readIntoBuilder(Reader reader) {
			try (BufferedReader br = reader instanceof BufferedReader ? (BufferedReader) reader
					: new BufferedReader(reader)) {
				GraphBuilder gb = null;
				Weights.Int w = null;
				String line;
				String graph_format = null;
				int num_vertices = -1;
				int num_edges = -1;
				while ((line = br.readLine()) != null) {
					// skip empty and comment lines (starts with "c")
					if (line.trim().length() == 0 || line.trim().startsWith("c"))
						continue;
					// replace multiple spaces with just one space
					line = line.trim().replaceAll("\\s+", " ");
					if (line.trim().startsWith("p")) {
						String[] arr = line.split(" ");
						if (arr.length < 4)
							throw new IllegalArgumentException(
									"p lines must have 4 parameters: p edge <NumVertices> <NumEdges> or p sp <NumVertices> <NumEdges>");
						graph_format = arr[1].toLowerCase();
						if (!(graph_format.equals("sp") || graph_format.equals("edge")))
							throw new IllegalArgumentException(
									"support only: p edge <NumVertices> <NumEdges> or p sp <NumVertices> <NumEdges>");
						try {
							num_vertices = Integer.parseInt(arr[2]);
							num_edges = Integer.parseInt(arr[3]);
						} catch (Exception e) {
							throw new IllegalArgumentException(
									"expect numbers: p edge <NumVertices> <NumEdges> or p sp <NumVertices> <NumEdges>");
						}
						if (num_vertices < 1)
							return gb;
						gb = GraphBuilder.newUndirected();
						if (graph_format.equals("sp"))
							w = gb.addEdgesWeights("weightsEdges", int.class);
						for (int i = 0; i < num_vertices; i++)
							gb.addVertex(i + 1); // vertices are labeled as 1,2,3,4...
						if (num_edges < 1)
							return gb;
						continue;
					}

					// here, we expect edge definition
					// e source_vertice destination_vertice [weight]
					if (!line.trim().toLowerCase().startsWith("e"))
						throw new IllegalArgumentException(
								"expect edge definition: e <source_vertice> <destination_vertice>");
					String[] arr = line.split(" ");
					if (graph_format.equals("sp") && arr.length < 4)
						throw new IllegalArgumentException(
								"expect edge definition: e <source_vertice> <destination_vertice> <weight>");
					if (graph_format.equals("edge") && arr.length < 3)
						throw new IllegalArgumentException(
								"expect edge definition: e <source_vertice> <destination_vertice>");
					int vertice_source = -1;
					int vertice_target = -1;
					int edge_weight = -1;
					try {
						vertice_source = Integer.parseInt(arr[1]);
						vertice_target = Integer.parseInt(arr[2]);
						if (graph_format.equals("sp"))
							edge_weight = Integer.parseInt(arr[3]);
					} catch (Exception e) {
						if (graph_format.equals("sp"))
							throw new IllegalArgumentException("edge must have 2 vertices as numbers and a weight");
						throw new IllegalArgumentException("edge must have 2 vertices as numbers");
					}
					if (vertice_source < 1 || vertice_source > num_vertices //
							|| vertice_target < 1 || vertice_target > num_vertices)
						throw new IllegalArgumentException("vertice nmber must be betwen 1 and num_vertices");
					// vertices labels 1..num_vertices are mapped to 0..num_vertices-1
					final int e = gb.addEdge(vertice_source, vertice_target);
					if (graph_format.equals("sp"))
						w.set(e, edge_weight);
					continue;
				}
				return gb;

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

}
