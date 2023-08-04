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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.Weights;

class FormatLEDA implements GraphFormat {

	private FormatLEDA() {}

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
			final int numVertices = graph.vertices().size();
			final int numEdges = graph.edges().size();
			for (int v = 1; v <= numVertices; v++)
				if (!graph.vertices().contains(v))
					throw new IllegalArgumentException("the LEDA format support graphs with vertices 1..n only");
			for (int e = 1; e <= numEdges; e++)
				if (!graph.edges().contains(e))
					throw new IllegalArgumentException("the LEDA format support graphs with edges 1..m only");

			try {
				String info_type_for_vertices; // can be void, string, int etc
				Weights<?> w_vertices; // weights for vertices
				if (graph.getVerticesWeightsKeys().isEmpty()) {
					info_type_for_vertices = "void";
					w_vertices = null;
				} else {
					// for now, take the first weights collection
					Object key = graph.getVerticesWeightsKeys().iterator().next();
					w_vertices = graph.getVerticesWeights(key);
					if (w_vertices instanceof Weights.Int) {
						info_type_for_vertices = "int";
					} else if (w_vertices instanceof Weights.Short) {
						info_type_for_vertices = "short";
					} else if (w_vertices instanceof Weights.Long) {
						info_type_for_vertices = "long";
					} else if (w_vertices instanceof Weights.Float) {
						info_type_for_vertices = "float";
					} else if (w_vertices instanceof Weights.Double) {
						info_type_for_vertices = "double";
					} else {
						info_type_for_vertices = "string";
					}
				}

				String info_type_for_edges; // can be void, string, int etc
				Weights<?> w_edges; // weights for edges
				if (graph.getEdgesWeightsKeys().isEmpty()) {
					info_type_for_edges = "void";
					w_edges = null;
				} else {
					// for now, take the first weights collection
					Object key = graph.getEdgesWeightsKeys().iterator().next();
					w_edges = graph.getEdgesWeights(key);
					if (w_edges instanceof Weights.Int) {
						info_type_for_edges = "int";
					} else if (w_edges instanceof Weights.Short) {
						info_type_for_edges = "short";
					} else if (w_edges instanceof Weights.Long) {
						info_type_for_edges = "long";
					} else if (w_edges instanceof Weights.Float) {
						info_type_for_edges = "float";
					} else if (w_edges instanceof Weights.Double) {
						info_type_for_edges = "double";
					} else {
						info_type_for_edges = "string";
					}
				}

				writer.append("LEDA.GRAPH").append(System.lineSeparator());
				writer.append(info_type_for_vertices).append(System.lineSeparator()); // void/string/int etc
				writer.append(info_type_for_edges).append(System.lineSeparator()); // void/string/int etc
				final boolean is_directed = graph.getCapabilities().directed();
				writer.append(is_directed ? "-1" : "-2").append(System.lineSeparator());

				writer.append("# section nodes/vertices").append(System.lineSeparator());
				writer.append(Integer.toString(numVertices)).append(System.lineSeparator());
				// write all vertices info
				// --> LEDA expects 1..num_vertices
				// for (int ix = 1; ix <= num_vertices; ix++)
				// but just in case, we are consistent with our labels etc
				if (info_type_for_vertices.equals("void")) {
					for (int vertex = 1; vertex <= numVertices; vertex++)
						writer.append("|{}|").append(System.lineSeparator());
				} else if (info_type_for_vertices.equals("int")) {
					for (int vertex = 1; vertex <= numVertices; vertex++) {
						int weight = ((Weights.Int) w_vertices).getInt(vertex);
						writer.append("|{").append(String.valueOf(weight)).append("}|").append(System.lineSeparator());
					}
				} else if (info_type_for_vertices.equals("short")) {
					for (int vertex = 1; vertex <= numVertices; vertex++) {
						short weight = ((Weights.Short) w_vertices).getShort(vertex);
						writer.append("|{").append(String.valueOf(weight)).append("}|").append(System.lineSeparator());
					}
				} else if (info_type_for_vertices.equals("long")) {
					for (int vertex = 1; vertex <= numVertices; vertex++) {
						long weight = ((Weights.Long) w_vertices).getLong(vertex);
						writer.append("|{").append(String.valueOf(weight)).append("}|").append(System.lineSeparator());
					}
				} else if (info_type_for_vertices.equals("float")) {
					for (int vertex = 1; vertex <= numVertices; vertex++) {
						float weight = ((Weights.Float) w_vertices).getFloat(vertex);
						writer.append("|{").append(String.valueOf(weight)).append("}|").append(System.lineSeparator());
					}
				} else if (info_type_for_vertices.equals("double")) {
					for (int vertex = 1; vertex <= numVertices; vertex++) {
						double weight = ((Weights.Double) w_vertices).getDouble(vertex);
						writer.append("|{").append(String.valueOf(weight)).append("}|").append(System.lineSeparator());
					}
				} else if (info_type_for_vertices.equals("string")) {
					for (int vertex = 1; vertex <= numVertices; vertex++) {
						String weight = ((Weights<String>) w_vertices).get(vertex);
						writer.append("|{").append(weight).append("}|").append(System.lineSeparator());
					}
				} else
					throw new IllegalArgumentException("Leda file format: unknown weight format");

				writer.append("# section edges").append(System.lineSeparator());
				writer.append(Integer.toString(numEdges)).append(System.lineSeparator());
				// write all edges info
				for (int edge = 1; edge <= numEdges; edge++) {
					int source_vertex = graph.edgeSource(edge);
					int target_vertex = graph.edgeTarget(edge);
					String edgeWeight = w_edges != null ? String.valueOf(w_edges.get(edge)) : "";

					writer.append(Integer.toString(source_vertex)).append(' ');
					writer.append(Integer.toString(target_vertex)).append(' ');
					writer.append(/* twin edge */ '0').append(' ');
					writer.append("|{").append(edgeWeight).append("}|").append(System.lineSeparator());
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

	private static class ReaderImpl implements GraphReader {

		@Override
		public GraphBuilder readIntoBuilder(Reader reader) {
			GraphBuilder builder = null;

			// list_nodes keep the label/name of each node/vertex
			// we do not need it, but for future use
			ArrayList<String> list_nodes = new ArrayList<>();

			// Pattern pattern_edge to regexp the edge line
			// Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+\\{\\|(.*)\\|\\}\\s*$");
			Pattern pattern_edge = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S\\S.*\\S\\S)\\s*$");

			try (BufferedReader br =
					reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
				// these are the oly weights format we support
				Set<String> hashSupportedTypes = new HashSet<>();
				hashSupportedTypes.add("void");
				hashSupportedTypes.add("int");
				hashSupportedTypes.add("short");
				hashSupportedTypes.add("long");
				hashSupportedTypes.add("float");
				hashSupportedTypes.add("double");
				hashSupportedTypes.add("string");
				int line_number = 0; // sequence of lines is important!
				boolean in_section = false;
				int num_secetion = 0; // 1 is nodes section, 2 is edges section
				int section_num_elements = -1;
				// section_ix_element will count from 1,2,3,4...,section_num_elements
				int section_ix_element = -1;
				String info_type_for_vertices = null; // can be void,string, int etc
				String info_type_for_edges = null; // can be void,string, int etc
				Weights<?> w_info_type_for_vertices = null;
				Weights<?> w_info_type_for_edges = null;

				// read all lines from file
				for (String line; (line = br.readLine()) != null;) {
					line = line.trim();
					if (line.length() == 0)
						continue; // skip empty lines
					// # This is a comment line
					if (line.toLowerCase().startsWith("#"))
						continue; // skip comment lines
					line_number++;

					// LEDA.GRAPH
					if (line_number == 1) {
						if (line.toUpperCase().startsWith("LEDA.GRAPH"))
							continue; // skip LEDA.GRAPH
						throw new IllegalArgumentException(
								"Leda file format: first non-comment line must equals LEDA.GRAPH");
					}

					if (line_number == 2) {
						info_type_for_vertices = line.toLowerCase();
						if (!hashSupportedTypes.contains(info_type_for_vertices))
							throw new IllegalArgumentException(
									"Leda file format: unsupported info/weight to vertices/nodes");
						continue; // skip 2nd - label string
					}

					if (line_number == 3) {
						info_type_for_edges = line.toLowerCase();
						if (!hashSupportedTypes.contains(info_type_for_edges))
							throw new IllegalArgumentException("Leda file format: unsupported info/weight to edges");
						continue; // skip 3nd - label string
					}

					if (line_number == 4) {
						// The fourth line specifies if the graph is
						// either directed (-1)
						// or undirected (-2).
						switch (line) {
							case "-1":
								builder = GraphBuilder.newDirected();
								break;
							case "-2":
								builder = GraphBuilder.newUndirected();
								break;
							default:
								throw new IllegalArgumentException(
										"Leda file format: 4th non-comment line must equals -1 or -2. -1 is Directed graph. -2 is Undirected graph.");
						}
						continue;
					}

					if (line_number > 4 && w_info_type_for_vertices == null) {
						// once, we set w_info_type_for_vertices and w_info_type_for_edges
						switch (info_type_for_vertices) {
							case "int":
								w_info_type_for_vertices = builder.addVerticesWeights("weightsKey", int.class);
								break;
							case "short":
								w_info_type_for_vertices = builder.addVerticesWeights("weightsKey", short.class);
								break;
							case "long":
								w_info_type_for_vertices = builder.addVerticesWeights("weightsKey", long.class);
								break;
							case "float":
								w_info_type_for_vertices = builder.addVerticesWeights("weightsKey", float.class);
								break;
							case "double":
								w_info_type_for_vertices = builder.addVerticesWeights("weightsKey", double.class);
								break;
							case "void":
								break;
							case "string":
							default:
								w_info_type_for_vertices = builder.addVerticesWeights("weightsKey", String.class);
								break;
						}
						switch (info_type_for_edges) {
							case "int":
								w_info_type_for_edges = builder.addEdgesWeights("weightsKey", int.class);
								break;
							case "short":
								w_info_type_for_edges = builder.addEdgesWeights("weightsKey", short.class);
								break;
							case "long":
								w_info_type_for_edges = builder.addEdgesWeights("weightsKey", long.class);
								break;
							case "float":
								w_info_type_for_edges = builder.addEdgesWeights("weightsKey", float.class);
								break;
							case "double":
								w_info_type_for_edges = builder.addEdgesWeights("weightsKey", double.class);
								break;
							case "void":
								break;
							case "string":
							default:
								w_info_type_for_edges = builder.addEdgesWeights("weightsKey", String.class);
								break;
						}
					}

					if (!in_section) {
						// this line must be the number of elements in this section
						in_section = true;
						num_secetion++; // 1=nodes/vertices, 2=edges
						section_ix_element = 0;
						section_num_elements = -1;
						try {
							section_num_elements = Integer.parseInt(line);
						} catch (Exception e) {
							section_num_elements = -1;
						}
						if (section_num_elements < 0)
							throw new IllegalArgumentException(
									"Leda file format: number of elements must be non-negative integers.");
						continue; // next
					}

					// we are inside a section
					section_ix_element++;

					// is this the nodes section?
					if (num_secetion == 1) {
						// define node/vertex
						// |{v1}| --> v1 is any label or name or identifier
						// |{}| --> empty label or name or identifier
						if (!line.startsWith("|{") || !line.endsWith("}|"))
							throw new IllegalArgumentException(
									"Leda file format: node/vertex error. must be |{any_name_label}| or |{}|.");
						// get the label
						String label = line.substring(2, line.length() - 2);
						// debug
						list_nodes.add(label);
						final int id = builder.vertices().size() + 1; // 1,2,3...
						builder.addVertex(id);

						// record label/weight
						switch (info_type_for_vertices) {
							case "int":
								try {
									int val = Integer.parseInt(label);
									((Weights.Int) w_info_type_for_vertices).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException("Leda file format: vertex must have int info.");
								}
								break;
							case "short":
								try {
									short val = Short.parseShort(label);
									((Weights.Short) w_info_type_for_vertices).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException(
											"Leda file format: vertex must have short info.");
								}
								break;
							case "long":
								try {
									long val = Long.parseLong(label);
									((Weights.Long) w_info_type_for_vertices).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException("Leda file format: vertex must have long info.");
								}
								break;
							case "float":
								try {
									float val = Float.parseFloat(label);
									((Weights.Float) w_info_type_for_vertices).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException(
											"Leda file format: vertex must have float info.");
								}
								break;
							case "double":
								try {
									double val = Double.parseDouble(label);
									((Weights.Double) w_info_type_for_vertices).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException(
											"Leda file format: vertex must have double info.");
								}
								break;
							case "void":
								break;
							case "string":
							default:
								((Weights<String>) w_info_type_for_vertices).set(id, label);
								break;
						}

						if (section_ix_element == section_num_elements) {
							// we ended the current section
							in_section = false;
							section_ix_element = -1;
							section_num_elements = -1;
						}
						continue;
					}

					if (num_secetion == 3)
						throw new IllegalArgumentException(
								"Leda file format: too many parameers. 2nd section, the edges, is complete.");

					// if (num_secetion == 2)
					// define edges
					// edge definition consists of four space-separated parts:
					// ==> the number of the source node
					// ==> the number of the target node
					// ==> the number of the reversal edge or 0, if no such edge is set
					// ==> the information associated with the edge (cf. nodes section)

					// We use regexp to parse line
					final Matcher edge_matcher = pattern_edge.matcher(line);
					if (!edge_matcher.find())
						throw new IllegalArgumentException("Leda file format: invalid edge. must have 4 parts.");

					int from_vertex = Integer.parseInt(edge_matcher.group(1));
					int to_vertex = Integer.parseInt(edge_matcher.group(2));
					int reversal_edge = Integer.parseInt(edge_matcher.group(3)); // not used right now
					String label = edge_matcher.group(4);

					if (from_vertex < 1 //
							|| to_vertex < 1 //
							|| from_vertex > list_nodes.size() //
							|| to_vertex > list_nodes.size())
						throw new IllegalArgumentException(
								"Leda file format: invalid edge. must be between 1 and num nodes/vertices.");

					// |{v1}| --> v1 is any label or name or identifier
					// |{}| --> empty label or name or identifier
					if (!label.startsWith("|{") || !label.endsWith("}|"))
						throw new IllegalArgumentException(
								"Leda file format: invalid edge. inof/label must be starts with |{ and ends with }|.");
					// get the label
					label = label.substring(2, label.length() - 2);

					// add the edge
					final int id = builder.edges().size() + 1; // 1,2,3...
					builder.addEdge(from_vertex, to_vertex, id);

					// record label/weight
					switch (info_type_for_edges) {
						case "int":
							try {
								int val = Integer.parseInt(label);
								((Weights.Int) w_info_type_for_edges).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have int info.");
							}
							break;
						case "short":
							try {
								short val = Short.parseShort(label);
								((Weights.Short) w_info_type_for_edges).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have short info.");
							}
							break;
						case "long":
							try {
								long val = Long.parseLong(label);
								((Weights.Long) w_info_type_for_edges).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have long info.");
							}
							break;
						case "float":
							try {
								float val = Float.parseFloat(label);
								((Weights.Float) w_info_type_for_edges).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have float info.");
							}
							break;
						case "double":
							try {
								double val = Double.parseDouble(label);
								((Weights.Double) w_info_type_for_edges).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have double info.");
							}
							break;
						case "void":
							break;
						case "string":
						default:
							((Weights<String>) w_info_type_for_edges).set(id, label);
							break;
					}

					if (section_ix_element == section_num_elements) {
						// we ended the current section
						in_section = false;
						section_ix_element = -1;
						section_num_elements = -1;
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			return builder;
		}

	}

}
