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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;

/**
 * Read a graph in 'GML' format.
 *
 * <p>
 * The GML format is a simple text format for describing graphs. It can represent directed and undirected graphs, and
 * supports integers, floats and strings as vertices/edges identifiers and weights. The format is described in
 * <a href="https://en.wikipedia.org/wiki/Graph_Modelling_Language">Wikipedia</a>. The format uses a tree-like
 * structure, similar to JSON or XML. The root of the tree is a node with the key "graph", and its children are the
 * vertices, edges, and additional properties such as whether the graph is directed or not. Each node has a property
 * 'id', which is used as identifier in the built graph. Edges are not required to have an 'id' property by the format,
 * and we try to choose valid ids if they are missing, but its recommended to always provide an 'id' property for edges,
 * which will be used as the edge identifier in the built graph. In addition to 'id', each edge node should have a
 * property 'source' and 'target', specifying the endpoints vertices of the edge. Except for the 'id' property for
 * vertices, and 'id','source' and 'target' for edges, all other properties are added as weights to the graph. An
 * example of a GML file:
 *
 * <pre>
 * graph [
 * 	# This is a comment about this sample graph
 * 	comment "This is another comment about this sample graph"
 * 	directed 1
 * 	node [
 * 		id 1
 * 		label "node 1"
 * 		thisIsASampleAttribute 42
 * 	]
 * 	node [
 * 		id 2
 * 		label "node 2"
 * 		thisIsASampleAttribute 43
 * 	]
 * 	node [
 * 		id 3
 * 		label "node 3"
 * 		thisIsASampleAttribute 44
 * 	]
 * 	edge [
 * 		id 1
 * 		source 1
 * 		target 2
 * 		label "Edge from node 1 to node 2"
 * 		weight 22.7
 * 	]
 * 	edge [
 * 		id 2
 * 		source 2
 * 		target 3
 * 		label "Edge from node 2 to node 3"
 * 		weight 1.5
 * 	]
 * 	edge [
 * 		id 3
 * 		source 3
 * 		target 1
 * 		label "Edge from node 3 to node 1"
 * 		weight -13.54
 * 	]
 * ]
 * </pre>
 *
 * <p>
 * The GML format supports comments in the form of a node with the key 'comment', or lines that starts with '#'. Both
 * are completely ignored by the reader.
 *
 * <p>
 * The GML reader must know the type of vertices and edges during runtime in order to safely read the vertices and edges
 * into a graph. The types can be set using {@link #setVertexType(Class)} and {@link #setEdgeType(Class)}, or passed in
 * the {@linkplain #GmlGraphReader(Class, Class) constructor}. The supported types are integers, doubles and strings. If
 * both the vertices and edges types are {@code int.class}, an {@link IntGraph} will be built.
 *
 * <p>
 * The weights of the vertices will added to the built graph, and will be accessed later by the
 * {@link Graph#verticesWeights(String)} method, and similarly for the edges weights. GML supports integers, doubles and
 * strings as weights, and the weights will be added to the graph using {@link WeightsInt}, {@link WeightsDouble} or
 * {@link WeightsObj}. If the vertices (edges) contains weights with the same key but different type, the weights will
 * be added to the graph using a 'supertype' of the weights type. For example, if the vertices contains weights with key
 * 'weight' of type {@code int.class} and {@code double.class}, the weights will be added to the graph using
 * {@link WeightsDouble}. If the vertices contains weights with key 'weight' of type {@code int.class} and
 * {@code String.class}, the weights will be added to the graph using {@link WeightsObj}. If a vertex do not contain a
 * weight with a certain key but others do, the vertex will be assigned the default value of the weights type. All these
 * rules apply also for the edges weights.
 *
 * <p>
 * The format was presented in a paper 'GML: A portable Graph File Format' by Michael Himsolt.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public final class GmlGraphReader<V, E> extends GraphIoUtils.AbstractGraphReader<V, E> {

	private Class<V> vType;
	private Class<E> eType;
	private Class<V> vTypeBoxed;
	private Class<E> eTypeBoxed;

	/**
	 * Create a new reader.
	 *
	 * <p>
	 * Before any graph can be parsed, the vertices and edges types must be set using {@link #setVertexType(Class)} and
	 * {@link #setEdgeType(Class)}. Alternatively, the types can be passed in the
	 * {@linkplain #GmlGraphReader(Class, Class) constructor}.
	 */
	public GmlGraphReader() {}

	/**
	 * Create a new reader and provide the types of vertices and edges.
	 *
	 * @param vType the type of vertices
	 * @param eType the type of edges
	 * @see         #setVertexType(Class)
	 * @see         #setEdgeType(Class)
	 */
	public GmlGraphReader(Class<V> vType, Class<E> eType) {
		setVertexType(vType);
		setEdgeType(eType);
	}

	/**
	 * Set the type of vertices.
	 *
	 * <p>
	 * The GML reader must know the type of vertices during runtime in order to safely read the vertices into a graph.
	 * This method must be called before any graph can be built, unless the vertices type was passed in the constructor.
	 *
	 * <p>
	 * The supported types are integers, doubles and strings. If both the vertices and edges types are
	 * {@code int.class}, an {@link IntGraph} will be built.
	 *
	 * @param vType the type of vertices
	 */
	public void setVertexType(Class<V> vType) {
		checkVertexEdgeType(vType);
		this.vType = vType;
	}

	/**
	 * Set the type of edges.
	 *
	 * <p>
	 * The GML reader must know the type of edges during runtime in order to safely read the edges into a graph. This
	 * method must be called before any graph can be built, unless the edges type was passed in the constructor.
	 *
	 * <p>
	 * The supported types are integers, doubles and strings. If both the vertices and edges types are
	 * {@code int.class}, an {@link IntGraph} will be built.
	 *
	 * @param eType the type of edges
	 */
	public void setEdgeType(Class<E> eType) {
		checkVertexEdgeType(eType);
		this.eType = eType;
	}

	private static void checkVertexEdgeType(Class<?> type) {
		Objects.requireNonNull(type);
		if (!List.of(int.class, Integer.class, double.class, Double.class, String.class).contains(type))
			throw new IllegalArgumentException(
					"Only integers, doubles and strings are supported. Unsupported: " + type);
	}

	@Override
	GraphBuilder<V, E> readIntoBuilderImpl(Reader reader) throws IOException {
		if (vType == null)
			throw new IllegalStateException("Type of vertices was not set");
		if (eType == null)
			throw new IllegalStateException("Type of edges was not set");
		vTypeBoxed = boxedType(vType);
		eTypeBoxed = boxedType(eType);

		List<Pair<String, Object>> roots = parseHierarchy(new CharReader(GraphIoUtils.bufferedReader(reader)));
		return readIntoBuilder(roots);
	}

	private GraphBuilder<V, E> readIntoBuilder(List<Pair<String, Object>> roots) {
		Pair<String, Object> root;
		if (roots.size() != 1 || !(root = roots.get(0)).first().equals("graph"))
			throw new IllegalArgumentException("expected a single root list 'graph'");

		KeySearcher directedSearcher = new KeySearcher("directed", "graph");
		searchChildren(root, directedSearcher);
		final boolean directed = Integer.valueOf(1).equals(directedSearcher.value);

		final boolean intGraph = vType == int.class && eType == int.class;
		@SuppressWarnings("unchecked")
		GraphFactory<V, E> factory = intGraph ? (GraphFactory<V, E>) IntGraphFactory.newInstance(directed)
				: GraphFactory.newInstance(directed);

		GraphBuilder<V, E> b = factory.allowSelfEdges().newBuilder();
		IntGraphBuilder bInt = intGraph ? (IntGraphBuilder) b : null;

		Map<String, List<Pair<V, Object>>> vWeights = new Object2ObjectOpenHashMap<>();
		Map<String, List<Pair<E, Object>>> eWeights = new Object2ObjectOpenHashMap<>();

		for (Pair<String, Object> n : children(root)) {
			switch (n.first()) {
				case "node": {
					checkNoNestedLists(n);

					/* search for id */
					KeySearcher idSearcher = new KeySearcher("id", "vertex");
					searchChildren(n, idSearcher);
					V id = vertex(idSearcher.get());
					b.addVertex(id);

					/* add all other children as weights */
					for (Pair<String, Object> prop : children(n))
						if (!ObjectList.of("id").contains(prop.first()))
							vWeights
									.computeIfAbsent(prop.first(), k -> new ArrayList<>())
									.add(Pair.of(id, prop.second()));
					break;
				}
				case "edge": {
					checkNoNestedLists(n);

					/* search for id, source and target */
					KeySearcher idSearcher = new KeySearcher("id", "edge");
					KeySearcher sourceSearcher = new KeySearcher("source", "edge");
					KeySearcher targetSearcher = new KeySearcher("target", "edge");
					searchChildren(n, idSearcher, sourceSearcher, targetSearcher);
					V source = vertex(sourceSearcher.get());
					V target = vertex(targetSearcher.get());
					E id;
					if (intGraph && !idSearcher.found) {
						/* no id found, let IntGraphBuilder choose the id itself */
						int source0 = ((Integer) source).intValue();
						int target0 = ((Integer) target).intValue();
						@SuppressWarnings("unchecked")
						E id0 = (E) Integer.valueOf(bInt.addEdge(source0, target0));
						id = id0;

					} else {
						id = edge(idSearcher.get());
						b.addEdge(source, target, id);
					}

					/* add all other children as weights */
					for (Pair<String, Object> prop : children(n))
						if (!ObjectList.of("id", "source", "target").contains(prop.first()))
							eWeights
									.computeIfAbsent(prop.first(), k -> new ArrayList<>())
									.add(Pair.of(id, prop.second()));
					break;
				}
				case "directed":
					break; /* already searched this key */
				default:
					throw new IllegalArgumentException("unexpected key: '" + n.first() + "'");
			}
		}

		addWeights(vWeights, b::addVerticesWeights);
		addWeights(eWeights, b::addEdgesWeights);

		return b;
	}

	static class KeySearcher {
		boolean found;
		private String key;
		private Object value;
		private final String parentNode;

		KeySearcher(String key, String parentNode) {
			this.key = key;
			this.parentNode = parentNode;
		}

		boolean accept(Pair<String, Object> node) {
			if (!key.equals(node.first()))
				return false;
			if (found)
				throw new IllegalArgumentException("duplicate '" + key + "' property");
			value = node.second();
			return found = true;
		}

		Object get() {
			if (!found)
				throw new IllegalArgumentException("no '" + key + "' property for " + parentNode);
			return value;
		}
	}

	@SuppressWarnings("unchecked")
	private static <K> void addWeights(Map<String, List<Pair<K, Object>>> weights,
			BiFunction<String, Class<?>, Weights<K, ?>> weightsAdder) {
		for (var entry : weights.entrySet()) {
			String key = entry.getKey();
			Class<?> weightType = chooseWeightType(entry.getValue());
			if (weightType == int.class) {
				WeightsInt<K> w = (WeightsInt<K>) weightsAdder.apply(key, int.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), ((Integer) pair.second()).intValue());

			} else if (weightType == double.class) {
				WeightsDouble<K> w = (WeightsDouble<K>) weightsAdder.apply(key, double.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), ((Number) pair.second()).doubleValue());

			} else if (weightType == String.class) {
				WeightsObj<K, String> w = (WeightsObj<K, String>) weightsAdder.apply(key, String.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), (String) pair.second());

			} else {
				WeightsObj<K, Object> w = (WeightsObj<K, Object>) weightsAdder.apply(key, Object.class);
				for (var pair : entry.getValue())
					w.set(pair.first(), pair.second());
			}
		}
	}

	private static <K> Class<?> chooseWeightType(List<Pair<K, Object>> weights) {
		Iterator<Class<?>> typeIt = IterTools.map(weights.iterator(), pair -> {
			Object weight = pair.second();
			if (weight instanceof Integer) {
				return int.class;
			} else if (weight instanceof Double) {
				return double.class;
			} else {
				assert weight instanceof String;
				return String.class;
			}
		});
		Class<?> weightType = typeIt.next();

		while (weightType == int.class && typeIt.hasNext()) {
			Class<?> type = typeIt.next();
			if (type != int.class) {
				if (type == double.class) {
					weightType = double.class;
				} else {
					weightType = Object.class;
				}
			}
		}
		while (weightType == double.class && typeIt.hasNext()) {
			Class<?> type = typeIt.next();
			if (type != int.class && type != double.class)
				weightType = Object.class;
		}
		while (weightType == String.class && typeIt.hasNext()) {
			Class<?> type = typeIt.next();
			if (type != String.class)
				weightType = Object.class;
		}
		return weightType;
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
		if (type == int.class)
			return (Class<T>) Integer.class;
		if (type == double.class)
			return (Class<T>) Double.class;
		return type;
	}

	private static List<Pair<String, Object>> parseHierarchy(CharReader reader) throws IOException {
		StringBuilder strBuilder = new StringBuilder();
		List<Pair<String, Object>> roots = new ArrayList<>();
		Stack<Pair<String, Object>> stack = new ObjectArrayList<>();
		mainLoop: for (;;) {
			Pair<String, Object> node = new ObjectObjectMutablePair<>(null, null);
			if (stack.isEmpty()) {
				roots.add(node);
			} else {
				children(stack.top()).add(node);
			}

			/* parse key */
			StringBuilder key = strBuilder;
			if (!reader.appendIf(Gml::isKeyCharPrefix, key))
				throw new IllegalArgumentException("invalid key");
			while (reader.appendIf(Gml::isKeyChar, key));

			node.first(key.toString());
			key.setLength(0);

			readWhitespace(reader, true);

			char c = reader.getChar(() -> "no value of key '" + key + "'");
			if (c == '"') { /* string */
				reader.advance();

				StringBuilder strVal = strBuilder;
				for (;;) {
					c = reader.getCharAndAdvance("string value without terminating character");
					if (c == '"')
						break;
					strVal.append(c);
				}
				node.second(strVal.toString());
				strVal.setLength(0);

			} else if (c == '[') { /* list */
				reader.advance();
				node.second(new ArrayList<>());
				stack.push(node);
				readWhitespace(reader, false);
				continue;

			} else { /* number */
				StringBuilder num = strBuilder;

				/* read all digits */
				reader.appendIf(Gml::isNumSign, num);
				while (reader.appendIf(Gml::isDigit, num));

				/* floating number */
				if (reader.appendIf(ch -> ch == '.', num)) {

					/* read all digits after the period */
					while (reader.appendIf(Gml::isDigit, num));

					/* scientific format */
					if (reader.appendIf(ch -> ch == 'E', num)) {

						/* read all digits of exponent */
						if (!reader.appendIf(Gml::isNumSign, num))
							throw new IllegalArgumentException("expected a sign in float num exponent");
						while (reader.appendIf(Gml::isDigit, num));
					}
					node.second(Double.valueOf(num.toString()));

				} else {
					/* integer */
					node.second(Integer.valueOf(num.toString()));
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
					if (stack.isEmpty())
						throw new IllegalArgumentException("']' without list in scope");
					stack.pop();
				} else {
					break;
				}
			}
		}
		if (!stack.isEmpty())
			throw new IllegalArgumentException("lists without termination");

		/* remove nodes with 'comment' key */
		roots.removeIf(n -> n.first().equals("comment"));
		roots.forEach(stack::push);
		while (!stack.isEmpty()) {
			Pair<String, Object> node = stack.pop();
			if (hasChildren(node)) {
				children(node).removeIf(n -> n.first().equals("comment"));
				children(node).forEach(stack::push);
			}
		}

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

	@SuppressWarnings("unchecked")
	static List<Pair<String, Object>> children(Pair<String, Object> parent) {
		if (!hasChildren(parent))
			throw new IllegalArgumentException("node doesn't have a list as value");
		return (List<Pair<String, Object>>) parent.second();
	}

	static boolean hasChildren(Pair<String, Object> parent) {
		return parent.second() instanceof List<?>;
	}

	static void searchChildren(Pair<String, Object> parent, KeySearcher... searchers) {
		for (Pair<String, Object> child : children(parent))
			for (KeySearcher searcher : searchers)
				if (searcher.accept(child))
					break;
	}

	static void checkNoNestedLists(Pair<String, Object> parent) {
		for (Pair<String, Object> child : children(parent))
			if (hasChildren(child))
				throw new IllegalArgumentException("unexpected nested list");
	}

	private static final class CharReader {
		private final BufferedReader reader;
		private String line;
		private int linePos;
		private int nextChar;

		CharReader(BufferedReader reader) throws IOException {
			this.reader = Objects.requireNonNull(reader);
			nextLine();
		}

		char getChar() {
			return getChar("");
		}

		char getChar(String errMsgOnEOF) {
			return getChar(() -> errMsgOnEOF);
		}

		char getChar(Supplier<String> errMsgOnEOF) {
			if (!hasChar())
				throw new IllegalArgumentException(errMsgOnEOF.get());
			return (char) nextChar;
		}

		boolean hasChar() {
			return nextChar != -1;
		}

		void advance() throws IOException {
			if (linePos <= line.length()) {
				if (linePos < line.length()) {
					nextChar = line.charAt(linePos);
				} else {
					nextChar = '\n';
				}
				linePos++;

			} else {
				nextLine();
			}
		}

		private void nextLine() throws IOException {
			/* ignore lines that start with '#' */
			for (;;) {
				line = reader.readLine();
				if (line == null) {
					nextChar = -1;
					return;
				}
				char firstChar = ' ';
				for (int i = 0; i < line.length() && Character.isWhitespace(firstChar = line.charAt(i)); i++);
				if (firstChar != '#')
					break;
			}

			linePos = 0;
			advance();
		}

		boolean appendIf(CharPredicate pred, StringBuilder sb) throws IOException {
			if (hasChar() && pred.test((char) nextChar)) {
				sb.append(getChar());
				advance();
				return true;
			} else {
				return false;
			}
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
