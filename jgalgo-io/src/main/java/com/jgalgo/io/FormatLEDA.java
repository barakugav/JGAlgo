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
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;

import it.unimi.dsi.fastutil.ints.IntSet;

class FormatLEDA implements GraphFormat {

	private FormatLEDA() {
	}

	static final FormatLEDA Instance = new FormatLEDA();

	@Override
	public GraphWriter newWriter() {
		return new WriterImpl();
	}

	@Override
	public GraphReader newReader() {
		return new ReaderImpl();
	}

	private static final List<String> FILE_EXTENSIONS = List.of("lgr");

	@Override
	public List<String> getFileExtensions() {
		return FILE_EXTENSIONS;
	}

	private static class WriterImpl implements GraphWriter {

		@Override
		public void writeGraph(Graph graph, Writer writer) {
			try {
				writer.append("LEDA.GRAPH\n");
				writer.append("string\n");
				writer.append("int\n");
				final boolean is_directed = graph.getCapabilities().directed();
				writer.append(is_directed ? "-1\n" : "-2\n");

				final IntSet vertices = graph.vertices(); // get the keyset
				writer.append("# section nodes/vertices\n");
				final int num_vertices = (vertices == null || vertices.isEmpty()) //
						? 0 //
						: vertices.size();
				writer.append("" + num_vertices + "\n");
				// write all vertices info
				// --> LEDA expects 1..num_vertices
				// for (int ix = 1; ix <= num_vertices; ix++)
				// but just in case, we are consistent with our labels etc
				for (int vertice : vertices)
					writer.append("|{v" + vertice + "}|\n");

				final IntSet edges = graph.edges(); // get the keyset
				writer.append("# section edges\n");
				final int num_edges = (edges == null || edges.isEmpty()) //
						? 0 //
						: edges.size();
				writer.append("" + num_edges + "\n");
				// write all edges info
				for (int edge : edges) {
					int source_vertix = graph.edgeSource(edge);
					int tarhet_vertix = graph.edgeTarget(edge);
					writer.append("" + source_vertix // from
							+ "  " + tarhet_vertix // to
							+ "  0" //
							+ " |{" + edge + "}|\n"); // edge label/name
				} // end for (int edge : edges)
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} // end public void writeGraph(Graph graph, Writer writer)

	} // end private static class WriterImpl implements GraphWriter

	private static class ReaderImpl implements GraphReader {

		@Override
		public GraphBuilder readIntoBuilder(Reader reader) {
			GraphBuilder builder = GraphBuilder.newUndirected();
			// list_nodes keep the label/name of each node/vertex
			// we do not need it, but for future use
			ArrayList<String> list_nodes = new ArrayList<>();
			// Weights.Double w_double = null;
			// Weights.Int w_int = null;

			try (BufferedReader br = new BufferedReader(reader)) {
				String line;
				int line_number = 0;
				boolean in_section = false;
				int num_secetion = 0; // 1 is notes, 2 is edges
				int section_num_elements = -1;
				// section_ix_element will count from 1,2,3,4...,section_num_elements
				int section_ix_element = -1;
				while ((line = br.readLine()) != null) {
					if (line.trim().length() == 0)
						continue; // skip empty lines
					// # This is a comment line
					if (line.trim().toLowerCase().startsWith("#"))
						continue; // skip comment lines
					line_number++;
					// LEDA.GRAPH
					if (line_number == 1) {
						if (line.trim().toUpperCase().startsWith("LEDA.GRAPH"))
							continue; // skip LEDA.GRAPH
						throw new RuntimeException(
								new Exception("Leda file format: first non-comment line must equals LEDA.GRAPH"));
					}
					if (line_number == 2)
						continue; // skip 2nd - label string
					// Weights.Double w = g.addEdgesWeights("weightsKey", double.class);
					if (line_number == 3) {
						if (true)
							continue;// no weights
//						if (line.trim().length() == 0)
//							continue; // skip - no weights
//						if (line.trim().equals("int"))
//							//w_double = g.addEdgesWeights("weightsKey", double.class);
//							w_double = Weights.Double;
//						else if (line.trim().equals("int"))
//							w_int = g.addEdgesWeights("weightsKey", int.class);
//						else throw new RuntimeException(
//								new Exception("Leda file format: weights can only be int, double, or null/empty."));
//						continue;
					}

					if (line_number == 4) {
						// The fourth line specifies if the graph is
						// either directed (-1)
						// or undirected (-2).
						if (line.trim().equals("-1")) {
							builder = GraphBuilder.newDirected();
							continue; // next
						}
						if (line.trim().equals("-2")) {
							// already unDirected - the default
							continue; // next
						}
						throw new RuntimeException(new Exception(
								"Leda file format: 4th non-comment line must equals -1 or -2. -1 is Directed graph. -2 is Undirected graph."));
					}

					if (!in_section) {
						// this line must be the number of elements in this section
						in_section = true;
						num_secetion++; // 1=nodes/vertices, 2=edges
						section_ix_element = 0;
						section_num_elements = -1;
						try {
							section_num_elements = Integer.parseInt(line.trim());
						} catch (Exception e) {
							section_num_elements = -1;
						}
						if (section_num_elements < 0)
							throw new RuntimeException(new Exception(
									"Leda file format: number of elements must be non-negative integers."));
						continue; // next
					}

					// we are inside a section
					section_ix_element++;
					// testing
					System.out.println("line=" + line);

					if (num_secetion == 1) {
						// define node/vertex
						// |{v1}| --> v1 is any label or name or identifier
						// |{}| --> empty label or name or identifier
						line = line.trim();
						if (line.startsWith("|{") && line.endsWith("}|")) {
							// get the label
							String label = line.substring(2, line.length() - 2);
							// debug
							System.out.println("node/vertex label=" + label);
							list_nodes.add(label);
							builder.addVertex(list_nodes.size()); // 1,2,3...
							if (section_ix_element == section_num_elements) {
								// we ended the current section
								in_section = false;
								section_ix_element = -1;
								section_num_elements = -1;
							}
							continue;
						} else
							throw new RuntimeException(new Exception(
									"Leda file format: node/vertex error. must be |{any_name_label}| or |{}|."));
					}

					if (num_secetion == 3)
						throw new RuntimeException(new Exception(
								"Leda file format: too many parameers. 2nd section, the edges, is complete."));

					// if (num_secetion == 2)
					// define edges
					// edge definition consists of four space-separated parts:
					// ==> the number of the source node
					// ==> the number of the target node
					// ==> the number of the reversal edge or 0, if no such edge is set
					// ==> the information associated with the edge (cf. nodes section)

					String[] arr = line.trim().split(" ");

					if (arr.length != 4)
						throw new RuntimeException(
								new Exception("Leda file format: edge must be define by 4 parts, seperated by space."));

					int from_vertex = Integer.parseInt(arr[0]);
					int to_vertex = Integer.parseInt(arr[1]);

					if (from_vertex < 1 //
							|| to_vertex < 1 //
							|| from_vertex > list_nodes.size() //
							|| to_vertex > list_nodes.size())
						throw new RuntimeException(new Exception(
								"Leda file format: invalid edge. must be between 1 and num nodes/vertices."));

					// testing
					System.out.println("new edge: from " + from_vertex + " --> to " + to_vertex);
					builder.addEdge(from_vertex, to_vertex);

					if (section_ix_element == section_num_elements) {
						// we ended the current section
						in_section = false;
						section_ix_element = -1;
						section_num_elements = -1;
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return builder;
		}

	}

}
