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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.Weights;

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
				Set<Object> w_collection_vertices = graph.getVerticesWeightsKeys();
				Set<Object> w_collection_edges = graph.getEdgesWeightsKeys();
				String info_type_for_vertices = null; // can be void, string, int etc
				String info_type_for_edges = null; // can be void, string, int etc
				Weights<?> w_vertices = null; // weights for vertices
				Weights<?> w_edges = null; // weights for edges
				if (w_collection_vertices.isEmpty())
					info_type_for_vertices = "void";
				else {
					// for now, take the first weights collection
					for (Object obj : w_collection_vertices) {
						w_vertices = graph.getVerticesWeights(obj);
						if (w_vertices instanceof Weights.Int)
							info_type_for_vertices = "int";
						else if (w_vertices instanceof Weights.Short)
							info_type_for_vertices = "short";
						else if (w_vertices instanceof Weights.Long)
							info_type_for_vertices = "long";
						else if (w_vertices instanceof Weights.Float)
							info_type_for_vertices = "float";
						else if (w_vertices instanceof Weights.Double)
							info_type_for_vertices = "double";
						else
							info_type_for_vertices = "string";
						break;
					}
				}

				if (w_collection_edges.isEmpty())
					info_type_for_edges = "void";
				else {
					// for now, take the first weights collection
					for (Object obj : w_collection_edges) {
						w_edges = graph.getEdgesWeights(obj);
						if (w_edges instanceof Weights.Int)
							info_type_for_edges = "int";
						else if (w_edges instanceof Weights.Short)
							info_type_for_edges = "short";
						else if (w_edges instanceof Weights.Long)
							info_type_for_edges = "long";
						else if (w_edges instanceof Weights.Float)
							info_type_for_edges = "float";
						else if (w_edges instanceof Weights.Double)
							info_type_for_edges = "double";
						else
							info_type_for_edges = "string";
						break;
					}
				}

				writer.append("LEDA.GRAPH\n");
				writer.append(info_type_for_vertices + "\n"); // void string int etc
				writer.append(info_type_for_edges + "\n"); // void string int etc
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
				if (info_type_for_vertices.equals("void")) {
					for (int vertix : vertices)
						writer.append("|{}|\n");
				} else if (info_type_for_vertices.equals("int")) {
					for (int vertix : vertices) {
						writer.append("|{" + ((Weights.Int) w_vertices).getInt(vertix) + "}|\n");
					}
				} else if (info_type_for_vertices.equals("short")) {
					for (int vertix : vertices) {
						writer.append("|{" + ((Weights.Short) w_vertices).getShort(vertix) + "}|\n");
					}
				} else if (info_type_for_vertices.equals("long")) {
					for (int vertix : vertices) {
						writer.append("|{" + ((Weights.Long) w_vertices).getLong(vertix) + "}|\n");
					}
				} else if (info_type_for_vertices.equals("float")) {
					for (int vertix : vertices) {
						writer.append("|{" + ((Weights.Float) w_vertices).getFloat(vertix) + "}|\n");
					}
				} else if (info_type_for_vertices.equals("double")) {
					for (int vertix : vertices) {
						writer.append("|{" + ((Weights.Double) w_vertices).getDouble(vertix) + "}|\n");
					}
				} else if (info_type_for_vertices.equals("string")) {
					for (int vertix : vertices) {
						writer.append("|{" + ((Weights<String>) w_vertices).get(vertix) + "}|\n");
					}
				} else
					throw new IllegalArgumentException("Leda file format: unknown weight format");

				final IntSet edges = graph.edges(); // get the keyset
				writer.append("# section edges\n");
				final int num_edges = (edges == null || edges.isEmpty()) //
						? 0 //
						: edges.size();
				writer.append("" + num_edges + "\n");
				// write all edges info
				for (int edge : edges) {
					int source_vertix = graph.edgeSource(edge);
					int target_vertix = graph.edgeTarget(edge);
					String val = "";
					// do this edge has any weight?
					if (w_edges != null) {
						Object obj = w_edges.get(edge);
						if (obj != null)
							val = obj.toString();
					}

					writer.append("" + source_vertix // from
							+ "  " + target_vertix // to
							+ "  0" //
							+ " |{" + val + "}|\n"); // edge label/name
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
			
			// Pattern pattern_edge to regexp the edge line
			// Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+\\{\\|(.*)\\|\\}\\s*$");
			Pattern pattern_edge = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S\\S.*\\S\\S)\\s*$");

			try (BufferedReader br = reader instanceof BufferedReader //
					? (BufferedReader) reader //
					: new BufferedReader(reader)) {
				// these are the oly weights format we support
				HashMap<String, String> hashSupportedTypes = new HashMap<>();
				hashSupportedTypes.put("void", "void");
				hashSupportedTypes.put("int", "int");
				hashSupportedTypes.put("short", "short");
				hashSupportedTypes.put("long", "long");
				hashSupportedTypes.put("float", "float");
				hashSupportedTypes.put("double", "double");
				hashSupportedTypes.put("string", "string");
				String line;
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
						throw new IllegalArgumentException("Leda file format: first non-comment line must equals LEDA.GRAPH");
					}

					if (line_number == 2) {
						info_type_for_vertices = line.trim().toLowerCase();
						if (hashSupportedTypes.get(info_type_for_vertices) == null)
							throw new IllegalArgumentException("Leda file format: unsopported info/weight to vertices/nodes");
						continue; // skip 2nd - label string
					}

					if (line_number == 3) {
						info_type_for_edges = line.trim().toLowerCase();
						if (hashSupportedTypes.get(info_type_for_edges) == null)
							throw new IllegalArgumentException("Leda file format: unsopported info/weight to edges");
						continue; // skip 3nd - label string
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

					// is this the nodes section?
					if (num_secetion == 1) {
						// define node/vertex
						// |{v1}| --> v1 is any label or name or identifier
						// |{}| --> empty label or name or identifier
						line = line.trim();
						if (!line.startsWith("|{") || !line.endsWith("}|"))
							throw new IllegalArgumentException(
									"Leda file format: node/vertex error. must be |{any_name_label}| or |{}|.");
						// get the label
						String label = line.substring(2, line.length() - 2);
						// debug
						list_nodes.add(label);
						final int id = builder.addVertex(); // 1,2,3...

						// record label/weight
						switch (info_type_for_vertices) {
						case "int":
							try {
								int val = Integer.parseInt(label);
								((Weights.Int) w_info_type_for_vertices).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: vertice must have int info.");
							}
							break;
						case "short":
							try {
								short val = Short.parseShort(label);
								((Weights.Short) w_info_type_for_vertices).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: vertice must have short info.");
							}
							break;
						case "long":
							try {
								long val = Long.parseLong(label);
								((Weights.Long) w_info_type_for_vertices).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: vertice must have long info.");
							}
							break;
						case "float":
							try {
								float val = Float.parseFloat(label);
								((Weights.Float) w_info_type_for_vertices).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: vertice must have float info.");
							}
							break;
						case "double":
							try {
								double val = Double.parseDouble(label);
								((Weights.Double) w_info_type_for_vertices).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: vertice must have double info.");
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
					final int id = builder.addEdge(from_vertex, to_vertex);

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
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return builder;
		} // end readIntoBuilder

	} // end class ReaderImpl implements GraphReader

}
