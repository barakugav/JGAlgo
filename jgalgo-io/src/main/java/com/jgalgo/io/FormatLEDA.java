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
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsFloat;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IWeightsObj;
import com.jgalgo.graph.IWeightsShort;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;

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
		public void writeGraph(IntGraph graph, Writer writer) {
			final int numVertices = graph.vertices().size();
			final int numEdges = graph.edges().size();
			if (!range(1, numVertices + 1).equals(graph.vertices()))
				throw new IllegalArgumentException("the LEDA format support graphs with vertices 1..n only");
			if (!range(1, numEdges + 1).equals(graph.edges()))
				throw new IllegalArgumentException("the LEDA format support graphs with edges 1..m only");

			try {
				String verticesWeightsType; // can be void, string, int etc
				IWeights<?> verticesWeights; // weights for vertices
				if (graph.getVerticesWeightsKeys().isEmpty()) {
					verticesWeightsType = "void";
					verticesWeights = null;
				} else {
					// for now, take the first weights collection
					String key = graph.getVerticesWeightsKeys().iterator().next();
					verticesWeights = graph.getVerticesIWeights(key);
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
				IWeights<?> edgesWeights; // weights for edges
				if (graph.getEdgesWeightsKeys().isEmpty()) {
					edgesWeightsType = "void";
					edgesWeights = null;
				} else {
					// for now, take the first weights collection
					String key = graph.getEdgesWeightsKeys().iterator().next();
					edgesWeights = graph.getEdgesIWeights(key);
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
					WeightsStringifier weightsStringer = WeightsStringifier.newInstance(verticesWeights);
					for (int vertex = 1; vertex <= numVertices; vertex++) {
						String weightStr = weightsStringer.getWeightAsString(vertex);
						writer.append("|{").append(weightStr).append("}|").append(System.lineSeparator());
					}
				}

				writer.append("# section edges").append(System.lineSeparator());
				writer.append(Integer.toString(numEdges)).append(System.lineSeparator());
				// write all edges info
				WeightsStringifier weightsStringer =
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

	private static class ReaderImpl implements GraphReader {

		@Override
		public IntGraphBuilder readIntoBuilder(Reader reader) {
			IntGraphBuilder builder = null;

			// nodes keep the label/name of each node/vertex
			// we do not need it, but for future use
			ArrayList<String> nodes = new ArrayList<>();

			// Pattern edgePattern to regexp the edge line
			// Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+\\{\\|(.*)\\|\\}\\s*$");
			Pattern edgePattern = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S\\S.*\\S\\S)\\s*$");

			try (BufferedReader br =
					reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
				// these are the oly weights format we support
				Set<String> hashSupportedTypes = Set.of("void", "int", "short", "long", "float", "double", "string");
				int lineNumber = 0; // sequence of lines is important!
				boolean inSection = false;
				int numberOfSections = 0; // 1 is nodes section, 2 is edges section
				int sectionElementsNum = -1;
				// elementIdxInSection will count from 1,2,3,4...,sectionElementsNum
				int elementIdxInSection = -1;
				String verticesWeightsType = null; // can be void,string, int etc
				String edgesWeightsType = null; // can be void,string, int etc
				IWeights<?> verticesWeights = null;
				IWeights<?> edgesWeights = null;

				// read all lines from file
				for (String line; (line = br.readLine()) != null;) {
					line = line.trim();
					if (line.length() == 0)
						continue; // skip empty lines
					// # This is a comment line
					if (line.toLowerCase().startsWith("#"))
						continue; // skip comment lines
					lineNumber++;

					// LEDA.GRAPH
					if (lineNumber == 1) {
						if (line.toUpperCase().startsWith("LEDA.GRAPH"))
							continue; // skip LEDA.GRAPH
						throw new IllegalArgumentException(
								"Leda file format: first non-comment line must equals LEDA.GRAPH");
					}

					if (lineNumber == 2) {
						verticesWeightsType = line.toLowerCase();
						if (!hashSupportedTypes.contains(verticesWeightsType))
							throw new IllegalArgumentException(
									"Leda file format: unsupported info/weight to vertices/nodes");
						continue; // skip 2nd - label string
					}

					if (lineNumber == 3) {
						edgesWeightsType = line.toLowerCase();
						if (!hashSupportedTypes.contains(edgesWeightsType))
							throw new IllegalArgumentException("Leda file format: unsupported info/weight to edges");
						continue; // skip 3nd - label string
					}

					if (lineNumber == 4) {
						// The fourth line specifies if the graph is
						// either directed (-1)
						// or undirected (-2).
						switch (line) {
							case "-1":
								builder = IntGraphBuilder.newDirected();
								break;
							case "-2":
								builder = IntGraphBuilder.newUndirected();
								break;
							default:
								throw new IllegalArgumentException(
										"Leda file format: 4th non-comment line must equals -1 or -2. "
												+ "-1 is Directed graph. -2 is Undirected graph.");
						}

						switch (verticesWeightsType) {
							case "int":
								verticesWeights = (IWeights<?>) builder.addVerticesWeights("weightsKey", int.class);
								break;
							case "short":
								verticesWeights = (IWeights<?>) builder.addVerticesWeights("weightsKey", short.class);
								break;
							case "long":
								verticesWeights = (IWeights<?>) builder.addVerticesWeights("weightsKey", long.class);
								break;
							case "float":
								verticesWeights = (IWeights<?>) builder.addVerticesWeights("weightsKey", float.class);
								break;
							case "double":
								verticesWeights = (IWeights<?>) builder.addVerticesWeights("weightsKey", double.class);
								break;
							case "void":
								break;
							case "string":
							default:
								verticesWeights = (IWeights<?>) builder.addVerticesWeights("weightsKey", String.class);
								break;
						}

						switch (edgesWeightsType) {
							case "int":
								edgesWeights = (IWeights<?>) builder.addEdgesWeights("weightsKey", int.class);
								break;
							case "short":
								edgesWeights = (IWeights<?>) builder.addEdgesWeights("weightsKey", short.class);
								break;
							case "long":
								edgesWeights = (IWeights<?>) builder.addEdgesWeights("weightsKey", long.class);
								break;
							case "float":
								edgesWeights = (IWeights<?>) builder.addEdgesWeights("weightsKey", float.class);
								break;
							case "double":
								edgesWeights = (IWeights<?>) builder.addEdgesWeights("weightsKey", double.class);
								break;
							case "void":
								break;
							case "string":
							default:
								edgesWeights = (IWeights<?>) builder.addEdgesWeights("weightsKey", String.class);
								break;
						}

						continue;
					}

					if (!inSection) {
						// this line must be the number of elements in this section
						inSection = true;
						numberOfSections++; // 1=nodes/vertices, 2=edges
						elementIdxInSection = 0;
						sectionElementsNum = -1;
						try {
							sectionElementsNum = Integer.parseInt(line);
						} catch (Exception e) {
							sectionElementsNum = -1;
						}
						if (sectionElementsNum < 0)
							throw new IllegalArgumentException(
									"Leda file format: number of elements must be non-negative integers.");
						continue; // next
					}

					// we are inside a section
					elementIdxInSection++;

					// is this the nodes section?
					if (numberOfSections == 1) {
						// define node/vertex
						// |{v1}| --> v1 is any label or name or identifier
						// |{}| --> empty label or name or identifier
						if (!line.startsWith("|{") || !line.endsWith("}|"))
							throw new IllegalArgumentException(
									"Leda file format: node/vertex error. must be |{any_name_label}| or |{}|.");
						// get the label
						String label = line.substring(2, line.length() - 2);
						// debug
						nodes.add(label);
						final int id = builder.vertices().size() + 1; // 1,2,3...
						builder.addVertex(id);

						// record label/weight
						switch (verticesWeightsType) {
							case "int":
								try {
									int val = Integer.parseInt(label);
									((IWeightsInt) verticesWeights).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException("Leda file format: vertex must have int info.");
								}
								break;
							case "short":
								try {
									short val = Short.parseShort(label);
									((IWeightsShort) verticesWeights).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException(
											"Leda file format: vertex must have short info.");
								}
								break;
							case "long":
								try {
									long val = Long.parseLong(label);
									((IWeightsLong) verticesWeights).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException("Leda file format: vertex must have long info.");
								}
								break;
							case "float":
								try {
									float val = Float.parseFloat(label);
									((IWeightsFloat) verticesWeights).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException(
											"Leda file format: vertex must have float info.");
								}
								break;
							case "double":
								try {
									double val = Double.parseDouble(label);
									((IWeightsDouble) verticesWeights).set(id, val);
								} catch (Exception e) {
									throw new IllegalArgumentException(
											"Leda file format: vertex must have double info.");
								}
								break;
							case "void":
								break;
							case "string":
							default:
								@SuppressWarnings("unchecked")
								IWeightsObj<String> temp = (IWeightsObj<String>) verticesWeights;
								temp.set(id, label);
								break;
						}

						if (elementIdxInSection == sectionElementsNum) {
							// we ended the current section
							inSection = false;
							elementIdxInSection = -1;
							sectionElementsNum = -1;
						}
						continue;
					}

					if (numberOfSections == 3)
						throw new IllegalArgumentException(
								"Leda file format: too many parameers. 2nd section, the edges, is complete.");

					// if (numberOfSections == 2)
					// define edges
					// edge definition consists of four space-separated parts:
					// ==> the number of the source node
					// ==> the number of the target node
					// ==> the number of the reversal edge or 0, if no such edge is set
					// ==> the information associated with the edge (cf. nodes section)

					// We use regexp to parse line
					final Matcher edgeMatcher = edgePattern.matcher(line);
					if (!edgeMatcher.find())
						throw new IllegalArgumentException("Leda file format: invalid edge. must have 4 parts.");

					int fromVertex = Integer.parseInt(edgeMatcher.group(1));
					int toVertex = Integer.parseInt(edgeMatcher.group(2));
					// int reverseEdge = Integer.parseInt(edgeMatcher.group(3)); // not used right now
					String label = edgeMatcher.group(4);

					if (fromVertex < 1 //
							|| toVertex < 1 //
							|| fromVertex > nodes.size() //
							|| toVertex > nodes.size())
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
					builder.addEdge(fromVertex, toVertex, id);

					// record label/weight
					switch (edgesWeightsType) {
						case "int":
							try {
								int val = Integer.parseInt(label);
								((IWeightsInt) edgesWeights).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have int info.");
							}
							break;
						case "short":
							try {
								short val = Short.parseShort(label);
								((IWeightsShort) edgesWeights).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have short info.");
							}
							break;
						case "long":
							try {
								long val = Long.parseLong(label);
								((IWeightsLong) edgesWeights).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have long info.");
							}
							break;
						case "float":
							try {
								float val = Float.parseFloat(label);
								((IWeightsFloat) edgesWeights).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have float info.");
							}
							break;
						case "double":
							try {
								double val = Double.parseDouble(label);
								((IWeightsDouble) edgesWeights).set(id, val);
							} catch (Exception e) {
								throw new IllegalArgumentException("Leda file format: edge must have double info.");
							}
							break;
						case "void":
							break;
						case "string":
						default:
							@SuppressWarnings("unchecked")
							IWeightsObj<String> temp = (IWeightsObj<String>) edgesWeights;
							temp.set(id, label);
							break;
					}

					if (elementIdxInSection == sectionElementsNum) {
						// we ended the current section
						inSection = false;
						elementIdxInSection = -1;
						sectionElementsNum = -1;
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			return builder;
		}

	}

}
