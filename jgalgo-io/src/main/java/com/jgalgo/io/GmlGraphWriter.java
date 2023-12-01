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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;
import it.unimi.dsi.fastutil.Pair;

/**
 * Write a graph using the GML format.
 *
 * <p>
 * The GML format is a simple text format for describing graphs. It can represent directed and undirected graphs, and
 * supports integers, floats and strings as vertices/edges identifiers and weights. The format is described in
 * <a href="https://en.wikipedia.org/wiki/Graph_Modelling_Language">Wikipedia</a>. The format uses a tree-like
 * structure, similar to JSON or XML. The root of the tree is a node with the key "graph", and its children are the
 * vertices, edges, and additional properties such as whether the graph is directed or not. Each vertex and edge will
 * have a property 'id', which is the identifier in the written graph. The source and target of each edge will be stored
 * as properties of an edge node. Except for the 'id' property for vertices, and 'id','source' and 'target' for edges,
 * the weights of each vertex/edge will be stored as properties of the vertex/edge node. An example of a GML file:
 *
 * <pre>
 * graph [
 * 	# "This is a sample graph"
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
 * The identifiers of a graph will be written using the {@link Object#toString()} method. The vertices and edges should
 * be either integers, floats, or strings so that when reading the graph using {@link GmlGraphReader} the identifiers
 * will be parsed correctly.
 *
 * <p>
 * The writer support writing weights of vertices and edges of type {@link WeightsByte}, {@link WeightsShort} and
 * {@link WeightsInt} as integer weights, {@link WeightsFloat}, {@link WeightsDouble} and {@link WeightsLong} as
 * floating numbers weights, {@link WeightsObj} as string weights using the {@link Object#toString()} method,
 * {@link WeightsChar} as string weights, and lastly {@link WeightsBool} as <i>integer</i> weights where {@code 0} is
 * {@code false} and {@code 1} is {@code true}. Note that this way of writing the weights will not yield the exact same
 * graph when reading it using {@link GmlGraphReader}, since the weights will be parsed into {@link WeightsInt},
 * {@link WeightsDouble} and {@link WeightsObj} only. If only {@link WeightsInt}, {@link WeightsDouble} and
 * {@link WeightsObj} of strings are written, the read graph will be identical. The weights will be written as
 * properties of the vertex/edge node, and the key of the property will be the key of the weights. The weights keys
 * should be valid GML keys. By default, all weights of the vertices/edges will be written. To write only specific
 * weights, use {@link #setVerticesWeightsKeys(Collection)} and {@link #setEdgesWeightsKeys(Collection)}.
 *
 * <p>
 * The format was presented in a paper 'GML: A portable Graph File Format' by Michael Himsolt.
 *
 * @author Barak Ugav
 */
public class GmlGraphWriter<V, E> extends GraphIoUtils.AbstractGraphWriter<V, E> {

	/*
	 * TODO: we can write some additional comments to the GML file that describe the type of weights exactly, and avoid
	 * the loss of types from byte/short/int to int, float,double,long to double, bool to int, ect.
	 */

	private String[] verticesWeightsKeys;
	private String[] edgesWeightsKeys;

	/**
	 * Create a new writer.
	 */
	public GmlGraphWriter() {}

	/**
	 * Set the weights keys of the vertices to write.
	 *
	 * <p>
	 * By default, all weights of the vertices will be written. The writer support writing weights of type
	 * {@link WeightsByte}, {@link WeightsShort} and {@link WeightsInt} as integer weights, {@link WeightsFloat},
	 * {@link WeightsDouble} and {@link WeightsLong} as floating numbers weights, {@link WeightsObj} as string weights
	 * using the {@link Object#toString()} method, {@link WeightsChar} as string weights, and lastly {@link WeightsBool}
	 * as integer weights where {@code 0} is {@code false} and {@code 1} is {@code true}. Note that this way of writing
	 * the weights will not yield the exact same graph when reading it using {@link GmlGraphReader}, since the weights
	 * will be parsed into {@link WeightsInt}, {@link WeightsDouble} and {@link WeightsObj} only. If only
	 * {@link WeightsInt}, {@link WeightsDouble} and {@link WeightsObj} of strings are written, the read graph will be
	 * identical. The weights will be written as properties of the vertex node, and the key of the property will be the
	 * key of the weights. The weights keys should be valid GML keys.
	 *
	 * @param verticesWeightsKeys The weights keys of the vertices to write, or {@code null} to write all weights (which
	 *                                is the default)
	 */
	public void setVerticesWeightsKeys(Collection<String> verticesWeightsKeys) {
		this.verticesWeightsKeys = verticesWeightsKeys == null ? null : verticesWeightsKeys.toArray(String[]::new);
	}

	/**
	 * Set the weights keys of the edges to write.
	 *
	 * <p>
	 * By default, all weights of the edges will be written. The writer support writing weights of type
	 * {@link WeightsByte}, {@link WeightsShort} and {@link WeightsInt} as integer weights, {@link WeightsFloat},
	 * {@link WeightsDouble} and {@link WeightsLong} as floating numbers weights, {@link WeightsObj} as string weights
	 * using the {@link Object#toString()} method, {@link WeightsChar} as string weights, and lastly {@link WeightsBool}
	 * as integer weights where {@code 0} is {@code false} and {@code 1} is {@code true}. Note that this way of writing
	 * the weights will not yield the exact same graph when reading it using {@link GmlGraphReader}, since the weights
	 * will be parsed into {@link WeightsInt}, {@link WeightsDouble} and {@link WeightsObj} only. If only
	 * {@link WeightsInt}, {@link WeightsDouble} and {@link WeightsObj} of strings are written, the read graph will be
	 * identical. The weights will be written as properties of the edge node, and the key of the property will be the
	 * key of the weights. The weights keys should be valid GML keys.
	 *
	 * @param edgesWeightsKeys The weights keys of the edges to write, or {@code null} to write all weights (which is
	 *                             the default)
	 */
	public void setEdgesWeightsKeys(Collection<String> edgesWeightsKeys) {
		this.edgesWeightsKeys = edgesWeightsKeys == null ? null : edgesWeightsKeys.toArray(String[]::new);
	}

	@Override
	public void writeGraphImpl(Graph<V, E> graph, Writer writer) throws IOException {
		WeightsStringifier.Builder weightsStringifierBuilder = new WeightsStringifier.Builder();
		/* longs are written as floats */
		weightsStringifierBuilder.setLongStringifier(l -> Double.toString(l));
		/* chars are written as strings, and both chars and strings are wrapped in "{weight}" */
		weightsStringifierBuilder.setObjectStringifier(o -> {
			String str = String.valueOf(o);
			if (str.contains("\""))
				throw new IllegalArgumentException("String '" + str + "' contains '\"', which is not supported by GML");
			return "\"" + str + "\"";
		});
		weightsStringifierBuilder.setCharStringifier(c -> {
			if (c == '"')
				throw new IllegalArgumentException("Char '\"' is not supported by GML");
			return "\"" + c + "\"";
		});
		/* boolean are written as ones and zeros */
		weightsStringifierBuilder.setBoolStringifier(b -> b ? "1" : "0");

		List<Pair<String, WeightsStringifier<V>>> vWeights = new ArrayList<>();
		List<Pair<String, WeightsStringifier<E>>> eWeights = new ArrayList<>();
		if (verticesWeightsKeys == null) {
			for (String key : graph.getVerticesWeightsKeys()) {
				Weights<V, ?> weights = graph.getVerticesWeights(key);
				vWeights.add(Pair.of(key, weightsStringifierBuilder.build(weights)));
			}
		} else {
			for (String key : verticesWeightsKeys) {
				Weights<V, ?> weights = graph.getVerticesWeights(key);
				if (weights == null)
					throw new IllegalArgumentException("Vertices weights key '" + key + "' does not exist");
				vWeights.add(Pair.of(key, weightsStringifierBuilder.build(weights)));
			}
		}
		if (edgesWeightsKeys == null) {
			for (String key : graph.getEdgesWeightsKeys()) {
				Weights<E, ?> weights = graph.getEdgesWeights(key);
				eWeights.add(Pair.of(key, weightsStringifierBuilder.build(weights)));
			}
		} else {
			for (String key : edgesWeightsKeys) {
				Weights<E, ?> weights = graph.getEdgesWeights(key);
				if (weights == null)
					throw new IllegalArgumentException("Edges weights key '" + key + "' does not exist");
				eWeights.add(Pair.of(key, weightsStringifierBuilder.build(weights)));
			}
		}
		for (Pair<String, WeightsStringifier<V>> w : vWeights)
			Gml.checkValidWeightsKey(w.first());
		for (Pair<String, WeightsStringifier<E>> w : eWeights)
			Gml.checkValidWeightsKey(w.first());

		Writer2 out = new Writer2(writer);
		out.append("graph [").appendNewline();
		out.append("comment \"GML written graph by JGAlgo\"").appendNewline();
		out.append("\tdirected ").append(graph.isDirected() ? "1" : "0").appendNewline();
		for (V v : graph.vertices()) {
			out.append("\tnode [").appendNewline();
			out.append("\t\tid ").append(v).appendNewline();
			for (Pair<String, WeightsStringifier<V>> weights : vWeights) {
				out.append("\t\t");
				String key = weights.first();
				String weightStr = weights.right().weightStr(v);
				out.append(key).append(' ').append(weightStr).appendNewline();
			}
			out.append("\t]").appendNewline();
		}
		for (E e : graph.edges()) {
			out.append("\tedge [").appendNewline();
			out.append("\t\tid ").append(e).appendNewline();
			out.append("\t\tsource ").append(graph.edgeSource(e)).appendNewline();
			out.append("\t\ttarget ").append(graph.edgeTarget(e)).appendNewline();
			for (Pair<String, WeightsStringifier<E>> weights : eWeights) {
				out.append("\t\t");
				String key = weights.first();
				String weightStr = weights.right().weightStr(e);
				out.append(key).append(' ').append(weightStr).appendNewline();
			}
			out.append("\t]").appendNewline();
		}
		out.append(']').appendNewline();
	}

}
