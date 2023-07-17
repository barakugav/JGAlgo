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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * The GML format.
 * <p>
 * According to 'GML: A portable Graph File Format'
 *
 * @author Barak Ugav
 */
class FormatGML implements GraphFormat {

	private FormatGML() {}

	static final FormatGML Instance = new FormatGML();

	@Override
	public GraphWriter newWriter() {
		return new WriterImpl();
	}

	@Override
	public GraphReader newReader() {
		return new ReaderImpl();
	}

	@Override
	public String getFileExtension() {
		return "gml";
	}

	private static class WriterImpl implements GraphWriter {

		@Override
		public void writeGraph(Graph graph, Writer writer) {
			if (graph.getCapabilities().directed())
				throw new IllegalArgumentException("GML format support undirected graphs only");

			try {
				List<AppendOp> appendVWeights = new ArrayList<>();
				List<AppendOp> appendEWeights = new ArrayList<>();
				for (Object key : graph.getVerticesWeightsKeys()) {
					Weights<?> w = graph.getVerticesWeights(key);
					appendVWeights.add(AppendOp.fromWeights(Objects.toString(key), w, writer));
				}
				for (Object key : graph.getEdgesWeightsKeys()) {
					Weights<?> w = graph.getEdgesWeights(key);
					appendEWeights.add(AppendOp.fromWeights(Objects.toString(key), w, writer));
				}

				writer.append("graph [").append(System.lineSeparator());
				for (int v : graph.vertices()) {
					writer.append("\tnode [").append(System.lineSeparator());
					writer.append("\t\tid ").append(Integer.toString(v)).append(System.lineSeparator());
					for (AppendOp appendOp : appendVWeights) {
						writer.append("\t\t");
						appendOp.append(v);
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
					for (AppendOp appendOp : appendEWeights) {
						writer.append("\t\t");
						appendOp.append(e);
						writer.append(System.lineSeparator());
					}
					writer.append("\t]").append(System.lineSeparator());
				}
				writer.append(']').append(System.lineSeparator());

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@FunctionalInterface
		private static interface AppendOp {
			default void append(int id) {
				try {
					append0(id);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			void append0(int elm) throws IOException;

			static AppendOp fromWeights(String key, Weights<?> w0, Writer writer) {
				if (key == null || key.isEmpty())
					throw new IllegalArgumentException("invalid key: '" + key + "'");
				if (!isKeyCharPrefix(key.charAt(0)))
					throw new IllegalArgumentException("invalid key: '" + key + "'");
				for (int i = 1; i < key.length(); i++)
					if (!isKeyChar(key.charAt(i)))
						throw new IllegalArgumentException("invalid key: '" + key + "'");

				if (w0 instanceof Weights.Byte) {
					Weights.Byte w = (Weights.Byte) w0;
					return v -> writer.append(key).append(' ').append(Byte.toString(w.getByte(v)));
				} else if (w0 instanceof Weights.Short) {
					Weights.Short w = (Weights.Short) w0;
					return v -> writer.append(key).append(' ').append(Short.toString(w.getShort(v)));
				} else if (w0 instanceof Weights.Int) {
					Weights.Int w = (Weights.Int) w0;
					return v -> writer.append(key).append(' ').append(Integer.toString(w.getInt(v)));
				} else if (w0 instanceof Weights.Long) {
					Weights.Long w = (Weights.Long) w0;
					return v -> writer.append(key).append(' ').append(Long.toString(w.getLong(v)));
				} else if (w0 instanceof Weights.Float) {
					Weights.Float w = (Weights.Float) w0;
					return v -> writer.append(key).append(' ').append(Float.toString(w.getFloat(v)));
				} else if (w0 instanceof Weights.Double) {
					Weights.Double w = (Weights.Double) w0;
					return v -> writer.append(key).append(' ').append(Double.toString(w.getDouble(v)));
				} else if (w0 instanceof Weights.Bool) {
					Weights.Bool w = (Weights.Bool) w0;
					return v -> writer.append(key).append(' ').append('"').append(Boolean.toString(w.getBool(v)))
							.append('"');
				} else if (w0 instanceof Weights.Char) {
					Weights.Char w = (Weights.Char) w0;
					return v -> writer.append(key).append(' ').append('"').append(w.getChar(v)).append('"');
				} else {
					return v -> writer.append(key).append(' ').append('"').append(Objects.toString(w0.get(v)))
							.append('"');
				}
			}
		}

	}

	private static class ReaderImpl implements GraphReader {

		@Override
		public GraphBuilder readIntoBuilder(Reader reader) {
			try (BufferedReader r0 =
					reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {

				List<Node> roots = parseIntoHierarchy(new CharReader(r0));
				return readIntoBuilder(roots);

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private static GraphBuilder readIntoBuilder(List<Node> roots) {
			Node root;
			if (roots.size() != 1 || !(root = roots.get(0)).key.equals("graph"))
				throw new IllegalArgumentException("expected a single root list 'graph'");

			GraphBuilder b = GraphBuilder.newUndirected();
			Map<String, List<IntObjectPair<Object>>> vWeights = new HashMap<>();
			Map<String, List<IntObjectPair<Object>>> eWeights = new HashMap<>();
			for (Node n : root.children()) {
				if ("node".equals(n.key)) {
					int id = -1;
					boolean idFound = false;
					for (Node prop : n.children()) {
						if ("id".equals(prop.key)) {
							if (idFound)
								throw new IllegalArgumentException("duplicate 'id' property");
							idFound = true;
							id = ((Integer) prop.value).intValue();
						} else if (prop.value instanceof List<?>) {
							throw new IllegalArgumentException("unexpected nested vertex weight");
						}
					}
					if (!idFound)
						throw new IllegalArgumentException("no 'id' property for node");
					b.addVertex(id);

					for (Node prop : n.children())
						if (!List.of("id").contains(prop.key))
							vWeights.computeIfAbsent(prop.key, k -> new ArrayList<>())
									.add(IntObjectPair.of(id, prop.value));

				} else if ("edge".equals(n.key)) {
					int id = -1;
					int source = -1, target = -1;
					boolean idFound = false;
					boolean sourceFound = false;
					boolean targetFound = false;
					for (Node prop : n.children()) {
						if ("id".equals(prop.key)) {
							if (idFound)
								throw new IllegalArgumentException("duplicate 'id' property");
							idFound = true;
							id = ((Integer) prop.value).intValue();
						} else if ("source".equals(prop.key)) {
							if (sourceFound)
								throw new IllegalArgumentException("duplicate 'source' property");
							sourceFound = true;
							source = ((Integer) prop.value).intValue();
						} else if ("target".equals(prop.key)) {
							if (targetFound)
								throw new IllegalArgumentException("duplicate 'target' property");
							targetFound = true;
							target = ((Integer) prop.value).intValue();
						} else if (prop.value instanceof List<?>) {
							throw new IllegalArgumentException("unexpected nested vertex weight");
						}
					}
					if (!sourceFound)
						throw new IllegalArgumentException("no 'source' property for edge");
					if (!targetFound)
						throw new IllegalArgumentException("no 'target' property for edge");
					if (idFound) {
						b.addEdge(source, target, id);
					} else {
						id = b.addEdge(source, target);
					}

					for (Node prop : n.children())
						if (!List.of("id", "source", "target").contains(prop.key))
							eWeights.computeIfAbsent(prop.key, k -> new ArrayList<>())
									.add(IntObjectPair.of(id, prop.value));

				} else {
					throw new IllegalArgumentException("unexpected key: '" + n.key + "'");
				}
			}

			for (var entry : vWeights.entrySet()) {
				String key = entry.getKey();
				Class<?> weightType = chooseWeightType(entry.getValue());
				if (weightType == int.class) {
					Weights.Int w = b.addVerticesWeights(key, int.class);
					for (var pair : entry.getValue())
						w.set(pair.firstInt(), ((Integer) pair.second()).intValue());

				} else if (weightType == double.class) {
					Weights.Double w = b.addVerticesWeights(key, double.class);
					for (var pair : entry.getValue())
						w.set(pair.firstInt(), ((Number) pair.second()).doubleValue());

				} else if (weightType == String.class) {
					Weights<String> w = b.addVerticesWeights(key, String.class);
					for (var pair : entry.getValue())
						w.set(pair.firstInt(), (String) pair.second());

				} else {
					Weights<Object> w = b.addVerticesWeights(key, Object.class);
					for (var pair : entry.getValue())
						w.set(pair.firstInt(), pair.second());
				}
			}
			for (var entry : eWeights.entrySet()) {
				String key = entry.getKey();
				Class<?> weightType = chooseWeightType(entry.getValue());
				if (weightType == int.class) {
					Weights.Int w = b.addEdgesWeights(key, int.class);
					for (var pair : entry.getValue())
						w.set(pair.firstInt(), ((Integer) pair.second()).intValue());

				} else if (weightType == double.class) {
					Weights.Double w = b.addEdgesWeights(key, double.class);
					for (var pair : entry.getValue())
						w.set(pair.firstInt(), ((Number) pair.second()).doubleValue());

				} else if (weightType == String.class) {
					Weights<String> w = b.addEdgesWeights(key, String.class);
					for (var pair : entry.getValue())
						w.set(pair.firstInt(), (String) pair.second());

				} else {
					Weights<Object> w = b.addEdgesWeights(key, Object.class);
					for (var pair : entry.getValue())
						w.set(pair.firstInt(), pair.second());
				}
			}

			return b;
		}

		private static Class<?> chooseWeightType(List<IntObjectPair<Object>> weights) {
			Class<?> weightType = null;
			for (var pair : weights) {
				Object weightVal = pair.second();
				Class<?> weightValType;
				if (weightVal instanceof Integer) {
					weightValType = int.class;
				} else if (weightVal instanceof Double) {
					weightValType = double.class;
				} else if (weightVal instanceof String) {
					weightValType = String.class;
				} else {
					throw new IllegalArgumentException("unexpected value type: " + weightVal.getClass());
				}

				if (weightType == null)
					weightType = weightValType;
				else if (weightType == Object.class || weightValType == Object.class)
					weightType = Object.class;
				else if (weightType == String.class && weightValType == String.class)
					weightType = String.class;
				else if (weightType == String.class || weightValType == String.class)
					weightType = Object.class;
				else if (weightType == double.class || weightValType == double.class)
					weightType = double.class;
				else if (weightType == int.class || weightValType == int.class)
					weightType = int.class;
				else
					throw new IllegalArgumentException("unexpected weight type " + weightValType + " " + weightType);
			}
			assert weightType != null;
			return weightType;
		}

		private static List<Node> parseIntoHierarchy(CharReader reader) throws IOException {
			StringBuilder strBuilder = new StringBuilder();
			List<Node> roots = new ArrayList<>();
			Stack<Node> s = new ObjectArrayList<>();
			mainLoop: for (;;) {
				char c = reader.getChar("");
				if (!isKeyCharPrefix(c))
					throw new IllegalArgumentException("invalid character for key: " + c);

				Node node = new Node();
				if (s.isEmpty()) {
					roots.add(node);
				} else {
					s.top().children().add(node);
				}

				// parse key
				StringBuilder key = strBuilder;
				key.append(c);
				reader.advance();
				for (;;) {
					c = reader.getChar("no value of key: " + key);
					if (!isKeyChar(c))
						break;
					key.append(c);
					reader.advance();
				}
				node.key = key.toString();
				key.setLength(0);

				readWhitespace(reader, true);

				c = reader.getChar("no value for key");
				if (c == '"') { // string
					reader.advance();

					StringBuilder strVal = strBuilder;
					for (;;) {
						c = reader.getCharAndAdvance("string value without terminating character");
						if (c == '"')
							break;
						strVal.append(c);
					}
					node.value = strVal.toString();
					strVal.setLength(0);

				} else if (c == '[') { // list
					reader.advance();
					node.value = new ArrayList<>();
					s.push(node);
					readWhitespace(reader, false);
					continue;

				} else { // number
					StringBuilder num = strBuilder;

					if (reader.hasChar() && isNumSign(c = reader.getChar())) {
						num.append(c);
						reader.advance();
					}
					while (reader.hasChar() && isDigit(c = reader.getChar())) {
						num.append(c);
						reader.advance();
					}
					boolean isFloat = reader.hasChar() && (c = reader.getChar()) == '.';
					if (!isFloat) {
						node.value = Integer.valueOf(num.toString());
					} else {

						num.append('.');
						reader.advance();

						while (reader.hasChar() && isDigit(c = reader.getChar())) {
							num.append(c);
							reader.advance();
						}
						if (reader.hasChar() && (c = reader.getChar()) == 'E') {
							num.append(c);
							reader.advance();

							if (!(reader.hasChar() && isNumSign(c = reader.getChar())))
								throw new IllegalArgumentException("expected a sign in float num mantissa");
							num.append(c);
							reader.advance();
							while (reader.hasChar() && isDigit(c = reader.getChar())) {
								num.append(c);
								reader.advance();
							}
						}

						node.value = Double.valueOf(num.toString());
					}

					num.setLength(0);
				}

				boolean hadWhitespace = readWhitespace(reader, false);
				if (!reader.hasChar())
					break mainLoop;
				if (!hadWhitespace && reader.getChar() != ']')
					throw new IllegalArgumentException("expected ']");
				for (;;) {
					if (!reader.hasChar())
						break mainLoop;
					if (reader.getChar() == ']') {
						reader.advance();
						readWhitespace(reader, false);
						if (s.isEmpty())
							throw new IllegalArgumentException("']' without list in scope");
						s.pop();
					} else {
						break;
					}
				}
			}
			if (!s.isEmpty())
				throw new IllegalArgumentException("lists without termination");
			return roots;
		}

		private static boolean readWhitespace(CharReader reader, boolean required) throws IOException {
			int count = 0;
			for (;;) {
				if (!reader.hasChar())
					break;
				char c = reader.getChar();
				if (!Character.isWhitespace(c))
					break;
				reader.advance();
				count++;
			}
			if (required && count == 0)
				throw new IllegalArgumentException("expected whitespace");
			return count > 0;
		}

		private static class Node {
			String key;
			Object value;

			@SuppressWarnings("unchecked")
			List<Node> children() {
				if (!(value instanceof List<?>))
					throw new IllegalArgumentException("node doesn't have a list as value");
				return (List<Node>) value;
			}
		}

		private static class CharReader {
			private final Reader reader;
			private int nextChar;

			CharReader(Reader reader) throws IOException {
				this.reader = Objects.requireNonNull(reader);
				advance();
			}

			char getChar() {
				return getChar("");
			}

			char getChar(String errMsgOnEOF) {
				if (!hasChar())
					throw new IllegalArgumentException(errMsgOnEOF);
				return (char) nextChar;
			}

			boolean hasChar() {
				return nextChar != -1;
			}

			void advance() throws IOException {
				nextChar = reader.read();
			}

			char getCharAndAdvance(String errMsgOnEOF) throws IOException {
				char c = getChar(errMsgOnEOF);
				advance();
				return c;
			}

			// char getCharAndAdvance() throws IOException {
			// return getCharAndAdvance("");
			// }
		}

	}

	private static boolean isKeyCharPrefix(char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
	}

	private static boolean isKeyChar(char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
	}

	private static boolean isNumSign(char c) {
		return c == '+' || c == '-';
	}

	private static boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

}