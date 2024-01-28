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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IWeightsByte;
import com.jgalgo.graph.IWeightsChar;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsFloat;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IWeightsObj;
import com.jgalgo.graph.IWeightsShort;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;

/**
 * Read a graph in 'LEDA' format.
 *
 * <p>
 * The <a href=https://www.algorithmic-solutions.info/leda_guide/graphs/leda_native_graph_fileformat.html>LEDA
 * format</a> is a simple format for both directed and undirected graphs, used by the <a
 * href=https://en.wikipedia.org/wiki/Library_of_Efficient_Data_types_and_Algorithms>LEDA</a> library. Vertices are
 * numbered from 1 to n, and edges are numbered from 1 to m. It support a single weight for vertices and a single weight
 * for edges. The weights can be any primitive, or a string.
 *
 * <p>
 * When the reader reads a graph from a file with weights, it will be added to the graph as a {@link Weights} object
 * with key {@code "weight"}, for both vertices and edges. The weights are later available view
 * {@link Graph#verticesWeights(String)}. To change the weights key, use {@link #setVerticesWeightsKey(String)} and
 * {@link #setEdgesWeightsKey(String)}.
 *
 * @see    LedaGraphWriter
 * @author Barak Ugav
 */
public class LedaGraphReader extends GraphIoUtils.AbstractIntGraphReader {

	private String verticesWeightsKey = "weight";
	private String edgesWeightsKey = "weight";

	/**
	 * Create a new reader.
	 */
	public LedaGraphReader() {}

	/**
	 * Sets the key of the vertices weights that will be read.
	 *
	 * <p>
	 * When the reader reads a graph with vertices weights, {@link Weights} will be added to the built graph. By
	 * default, the weights will be added with key "weight". Use this method to specify a different key.
	 *
	 * @param verticesWeightsKey the key of the vertices weights that will be read
	 */
	public void setVerticesWeightsKey(String verticesWeightsKey) {
		this.verticesWeightsKey = Objects.requireNonNull(verticesWeightsKey);
	}

	/**
	 * Sets the key of the edges weights that will be read.
	 *
	 * <p>
	 * When the reader reads a graph with edges weights, {@link Weights} will be added to the built graph. By default,
	 * the weights will be added with key "weight". Use this method to specify a different key.
	 *
	 * @param edgesWeightsKey the key of the edges weights that will be read
	 */
	public void setEdgesWeightsKey(String edgesWeightsKey) {
		this.edgesWeightsKey = Objects.requireNonNull(edgesWeightsKey);
	}

	@Override
	IntGraphBuilder readIntoBuilderImpl(Reader reader) {
		BufferedReader br = GraphIoUtils.bufferedReader(reader);
		Iterator<String> lineIter = GraphIoUtils.lines(br, true).iterator();
		Supplier<String> next = () -> {
			while (lineIter.hasNext()) {
				String line = lineIter.next();
				if (!line.isEmpty() && !line.startsWith("#"))
					return line;
			}
			return null;
		};

		String line1 = next.get();
		if (line1 == null)
			throw new IllegalArgumentException("Leda file format: empty file");
		if (!line1.equals("LEDA.GRAPH"))
			throw new IllegalArgumentException("Leda file format: first non-comment line must equals LEDA.GRAPH");

		String verticesWeightsStr = next.get();
		if (verticesWeightsStr == null)
			throw new IllegalArgumentException("Leda file format: invalid header");
		Class<?> verticesWeightsType = ledaTypeStrToClass(verticesWeightsStr);

		String edgesWeightsStr = next.get();
		if (edgesWeightsStr == null)
			throw new IllegalArgumentException("Leda file format: invalid header");
		Class<?> edgesWeightsType = ledaTypeStrToClass(edgesWeightsStr);

		String directedOrUndirected = next.get();
		if (directedOrUndirected == null)
			throw new IllegalArgumentException("Leda file format: invalid header");
		if (!List.of("-1", "-2").contains(directedOrUndirected))
			throw new IllegalArgumentException("Leda file format: 4th non-comment line must equals -1 or -2. "
					+ "-1 is Directed graph. -2 is Undirected graph.");
		boolean directed = "-1".equals(directedOrUndirected);
		IntGraphBuilder builder = IntGraphFactory.newInstance(directed).allowSelfEdges().newBuilder();

		IWeights<?> verticesWeights = null;
		IWeights<?> edgesWeights = null;
		if (verticesWeightsType != null)
			verticesWeights = (IWeights<?>) builder.addVerticesWeights(verticesWeightsKey, verticesWeightsType);
		if (edgesWeightsType != null)
			edgesWeights = (IWeights<?>) builder.addEdgesWeights(edgesWeightsKey, edgesWeightsType);
		ObjIntConsumer<String> verticesWeightsReader = weightsReader(verticesWeights);
		ObjIntConsumer<String> edgesWeightsReader = weightsReader(edgesWeights);

		String verticesNumLine = next.get();
		if (verticesNumLine == null)
			throw new IllegalArgumentException("invalid nodes section");
		final int n;
		try {
			n = Integer.parseInt(verticesNumLine);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("invalid nodes section");
		}
		if (n < 0)
			throw new IllegalArgumentException("invalid nodes section. num nodes must be >= 0");

		for (int v = 1; v <= n; v++) {
			String line = next.get();
			if (line == null)
				throw new IllegalArgumentException("Expected more node lines");
			if (!line.startsWith("|{") || !line.endsWith("}|"))
				throw new IllegalArgumentException("node line error. Expected '|{weight}|'");
			String weight = line.substring(2, line.length() - 2);

			builder.addVertex(v);
			verticesWeightsReader.accept(weight, v);
		}

		String edgesNumLine = next.get();
		if (edgesNumLine == null)
			throw new IllegalArgumentException("invalid edges section");
		final int m;
		try {
			m = Integer.parseInt(edgesNumLine);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("invalid edges section");
		}
		if (m < 0)
			throw new IllegalArgumentException("invalid edges section. num edges must be >= 0");

		for (int e = 1; e <= m; e++) {
			String line = next.get();
			if (line == null)
				throw new IllegalArgumentException("Expected more edge lines");

			int sourceEnd = line.indexOf(' ');
			if (sourceEnd < 0)
				throw new IllegalArgumentException("edge line error. Expected 'source target twinEdge weight'");
			int targetEnd = line.indexOf(' ', sourceEnd + 1);
			if (targetEnd < 0)
				throw new IllegalArgumentException("edge line error. Expected 'source target twinEdge weight'");
			int twinEdgeEnd = line.indexOf(' ', targetEnd + 1);
			if (twinEdgeEnd < 0)
				throw new IllegalArgumentException("edge line error. Expected 'source target twinEdge weight'");

			int source, target, twinEdge;
			try {
				source = Integer.parseInt(line.substring(0, sourceEnd));
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("edge line error. Invalid source vertex number");
			}
			try {
				target = Integer.parseInt(line.substring(sourceEnd + 1, targetEnd));
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("edge line error. Invalid target vertex number");
			}
			try {
				twinEdge = Integer.parseInt(line.substring(targetEnd + 1, twinEdgeEnd));
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("edge line error. Invalid twin edge number");
			}
			if (twinEdge != 0)
				throw new IllegalArgumentException("twin edges are not supported");

			if (line.indexOf("|{", twinEdgeEnd + 1) != twinEdgeEnd + 1 || !line.endsWith("}|"))
				throw new IllegalArgumentException("edge line error. Invalid weight, expected format: |{weight}|'");
			String weight = line.substring(twinEdgeEnd + 3, line.length() - 2);

			builder.addEdge(source, target, e);
			edgesWeightsReader.accept(weight, e);
		}

		if (next.get() != null)
			throw new IllegalArgumentException("unexpected lines after edges section");

		return builder;
	}

	private static Class<?> ledaTypeStrToClass(String weightsType) {
		switch (weightsType) {
			case "void":
				return null;
			case "byte":
				return byte.class;
			case "short":
				return short.class;
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "float":
				return float.class;
			case "double":
				return double.class;
			case "bool":
				return boolean.class;
			case "char":
				return char.class;
			case "string":
				return String.class;
			default:
				throw new IllegalArgumentException("unsupported weights type: " + weightsType);
		}
	}

	private static ObjIntConsumer<String> weightsReader(IWeights<?> weights) {
		if (weights == null) {
			return (weight, id) -> {
				if (!weight.isEmpty())
					throw new IllegalArgumentException("void weights must be empty string");
			};
		} else if (weights instanceof IWeightsByte) {
			IWeightsByte weights0 = (IWeightsByte) weights;
			return (weight, id) -> weights0.set(id, Byte.parseByte(weight));

		} else if (weights instanceof IWeightsShort) {
			IWeightsShort weights0 = (IWeightsShort) weights;
			return (weight, id) -> weights0.set(id, Short.parseShort(weight));

		} else if (weights instanceof IWeightsInt) {
			IWeightsInt weights0 = (IWeightsInt) weights;
			return (weight, id) -> weights0.set(id, Integer.parseInt(weight));

		} else if (weights instanceof IWeightsLong) {
			IWeightsLong weights0 = (IWeightsLong) weights;
			return (weight, id) -> weights0.set(id, Long.parseLong(weight));

		} else if (weights instanceof IWeightsFloat) {
			IWeightsFloat weights0 = (IWeightsFloat) weights;
			return (weight, id) -> weights0.set(id, Float.parseFloat(weight));

		} else if (weights instanceof IWeightsDouble) {
			IWeightsDouble weights0 = (IWeightsDouble) weights;
			return (weight, id) -> weights0.set(id, Double.parseDouble(weight));

		} else if (weights instanceof IWeightsBool) {
			IWeightsBool weights0 = (IWeightsBool) weights;
			return (weight, id) -> weights0.set(id, Boolean.parseBoolean(weight));

		} else if (weights instanceof IWeightsChar) {
			IWeightsChar weights0 = (IWeightsChar) weights;
			return (weight, id) -> weights0.set(id, GraphIoUtils.parseChar(weight));
		} else {
			@SuppressWarnings("unchecked")
			IWeightsObj<String> weights0 = (IWeightsObj<String>) weights;
			return (weight, id) -> weights0.set(id, weight);
		}
	}

}
