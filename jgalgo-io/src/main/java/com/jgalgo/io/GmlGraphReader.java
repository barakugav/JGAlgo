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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsObj;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GmlGraphReader<V, E> implements GraphReader<V, E> {

	private Class<V> vType;
	private Class<E> eType;
	private Class<V> vTypeBoxed;
	private Class<E> eTypeBoxed;

	public GmlGraphReader() {}

	public GmlGraphReader(Class<V> vType, Class<E> eType) {
		this.vType = Objects.requireNonNull(vType);
		this.eType = Objects.requireNonNull(eType);
	}

	public void setVertexType(Class<V> vType) {
		this.vType = Objects.requireNonNull(vType);
	}

	public void setEdgeType(Class<E> eType) {
		this.eType = Objects.requireNonNull(eType);
	}

	private V vertex(Object v) {
		try {
			return vTypeBoxed.cast(v);
		} catch (ClassCastException e) {
			throw new ClassCastException("expected vertex type " + vType + " but got " + v.getClass());
		}
	}

	private E edge(Object e) {
		try {
			return eTypeBoxed.cast(e);
		} catch (ClassCastException ex) {
			throw new ClassCastException("expected edge type " + eType + " but got " + e.getClass());
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> boxedType(Class<T> type) {
		if (!type.isPrimitive())
			return type;
		if (type == byte.class)
			return (Class<T>) Byte.class;
		if (type == short.class)
			return (Class<T>) Short.class;
		if (type == int.class)
			return (Class<T>) Integer.class;
		if (type == long.class)
			return (Class<T>) Long.class;
		if (type == float.class)
			return (Class<T>) Float.class;
		if (type == double.class)
			return (Class<T>) Double.class;
		if (type == boolean.class)
			return (Class<T>) Boolean.class;
		if (type == char.class)
			return (Class<T>) Character.class;
		throw new AssertionError();
	}

	@Override
	public GraphBuilder<V, E> readIntoBuilder(Reader reader) {
		if (vType == null)
			throw new IllegalStateException("Type of vertices was not set");
		if (eType == null)
			throw new IllegalStateException("Type of edges was not set");
		vTypeBoxed = boxedType(vType);
		eTypeBoxed = boxedType(eType);

		try (BufferedReader r0 =
				reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {

			List<Node> roots = parseIntoHierarchy(new CharReader(r0));
			return readIntoBuilder(roots);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private GraphBuilder<V, E> readIntoBuilder(List<Node> roots) {
		Node root;
		if (roots.size() != 1 || !(root = roots.get(0)).key.equals("graph"))
			throw new IllegalArgumentException("expected a single root list 'graph'");

		final boolean intGraph = vType == int.class && eType == int.class;
		@SuppressWarnings("unchecked")
		GraphFactory<V, E> factory =
				intGraph ? (GraphFactory<V, E>) IntGraphFactory.newUndirected() : GraphFactory.newUndirected();

		GraphBuilder<V, E> b = factory.allowSelfEdges().newBuilder();
		IntGraphBuilder bInt = intGraph ? (IntGraphBuilder)b : null;
		
		Map<String, List<Pair<V, Object>>> vWeights = new Object2ObjectOpenHashMap<>();
		Map<String, List<Pair<E, Object>>> eWeights = new Object2ObjectOpenHashMap<>();
		for (Node n : root.children()) {
			if ("node".equals(n.key)) {
				V id = null;
				boolean idFound = false;
				for (Node prop : n.children()) {
					if ("id".equals(prop.key)) {
						if (idFound)
							throw new IllegalArgumentException("duplicate 'id' property");
						idFound = true;
						id = vertex(prop.value);
					} else if (prop.value instanceof List<?>) {
						throw new IllegalArgumentException("unexpected nested vertex weight");
					}
				}
				if (!idFound)
					throw new IllegalArgumentException("no 'id' property for node");
				b.addVertex(id);

				for (Node prop : n.children())
					if (!List.of("id").contains(prop.key))
						vWeights.computeIfAbsent(prop.key, k -> new ArrayList<>()).add(Pair.of(id, prop.value));

			} else if ("edge".equals(n.key)) {
				E id = null;
				V source = null, target = null;
				boolean idFound = false;
				boolean sourceFound = false;
				boolean targetFound = false;
				for (Node prop : n.children()) {
					if ("id".equals(prop.key)) {
						if (idFound)
							throw new IllegalArgumentException("duplicate 'id' property");
						idFound = true;
						id = edge(prop.value);
					} else if ("source".equals(prop.key)) {
						if (sourceFound)
							throw new IllegalArgumentException("duplicate 'source' property");
						sourceFound = true;
						source = vertex(prop.value);
					} else if ("target".equals(prop.key)) {
						if (targetFound)
							throw new IllegalArgumentException("duplicate 'target' property");
						targetFound = true;
						target = vertex(prop.value);
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
				} else if (intGraph){
					int source0 = ((Integer)source).intValue();
					int target0 = ((Integer)target).intValue();
					@SuppressWarnings("unchecked")
					E id0 = (E) Integer.valueOf(bInt.addEdge(source0, target0));
					id = id0;
				} else {
					
				}

				for (Node prop : n.children())
					if (!List.of("id", "source", "target").contains(prop.key))
						eWeights.computeIfAbsent(prop.key, k -> new ArrayList<>()).add(Pair.of(id, prop.value));

			} else {
				throw new IllegalArgumentException("unexpected key: '" + n.key + "'");
			}
		}

		for (var entry : vWeights.entrySet()) {
			String key = entry.getKey();
			Class<?> weightType = chooseWeightType(entry.getValue());
			if (weightType == int.class) {
				WeightsInt<V> w = b.addVerticesWeights(key, int.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), ((Integer) pair.second()).intValue());

			} else if (weightType == double.class) {
				WeightsDouble<V> w = b.addVerticesWeights(key, double.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), ((Number) pair.second()).doubleValue());

			} else if (weightType == String.class) {
				WeightsObj<V, String> w = b.addVerticesWeights(key, String.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), (String) pair.second());

			} else {
				WeightsObj<V, Object> w = b.addVerticesWeights(key, Object.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), pair.second());
			}
		}
		for (var entry : eWeights.entrySet()) {
			String key = entry.getKey();
			Class<?> weightType = chooseWeightType(entry.getValue());
			if (weightType == int.class) {
				WeightsInt<E> w = b.addEdgesWeights(key, int.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), ((Integer) pair.second()).intValue());

			} else if (weightType == double.class) {
				WeightsDouble<E> w = b.addEdgesWeights(key, double.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), ((Number) pair.second()).doubleValue());

			} else if (weightType == String.class) {
				WeightsObj<E, String> w = b.addEdgesWeights(key, String.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), (String) pair.second());

			} else {
				WeightsObj<E, Object> w = b.addEdgesWeights(key, Object.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), pair.second());
			}
		}

		return b;
	}

	private static <K> Class<?> chooseWeightType(List<Pair<K, Object>> weights) {
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
			if (!Gml.isKeyCharPrefix(c))
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
				if (!Gml.isKeyChar(c))
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

				if (reader.hasChar() && Gml.isNumSign(c = reader.getChar())) {
					num.append(c);
					reader.advance();
				}
				while (reader.hasChar() && Gml.isDigit(c = reader.getChar())) {
					num.append(c);
					reader.advance();
				}
				boolean isFloat = reader.hasChar() && (c = reader.getChar()) == '.';
				if (!isFloat) {
					node.value = Integer.valueOf(num.toString());
				} else {

					num.append('.');
					reader.advance();

					while (reader.hasChar() && Gml.isDigit(c = reader.getChar())) {
						num.append(c);
						reader.advance();
					}
					if (reader.hasChar() && (c = reader.getChar()) == 'E') {
						num.append(c);
						reader.advance();

						if (!(reader.hasChar() && Gml.isNumSign(c = reader.getChar())))
							throw new IllegalArgumentException("expected a sign in float num mantissa");
						num.append(c);
						reader.advance();
						while (reader.hasChar() && Gml.isDigit(c = reader.getChar())) {
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
