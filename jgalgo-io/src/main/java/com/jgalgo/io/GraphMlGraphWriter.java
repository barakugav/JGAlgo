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
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

/**
 * Write a graph in 'GraphML' format.
 *
 * <p>
 * GraphML is an XML-based format for graphs. Both directed and undirected graphs are supported, along with mixed graphs
 * (some edges are directed while others are undirected) although the {@link Graph} does not supported mixed graphs
 * (therefore the writer also doesn't support it). The format support graphs with vertices and edges of any type, as
 * long as they can be written as an XML attribute string. The format also support multiple weights for vertices and
 * edges, of type {@code boolean}, {@code int}, {@code long}, {@code float}, {@code double} or {@link String}, and a
 * default value for each weight type.
 *
 * <p>
 * The GraphML format support both self edges and parallel edges.
 *
 * @see    GraphMlGraphReader
 * @author Barak Ugav
 */
public class GraphMlGraphWriter<V, E> extends GraphIoUtils.AbstractGraphWriter<V, E> {

	/**
	 * Create a new writer.
	 */
	public GraphMlGraphWriter() {}

	@Override
	void writeGraphImpl(Graph<V, E> graph, Writer writer) throws IOException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element rootElm = document.createElement("graphml");
			rootElm.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");
			document.appendChild(rootElm);

			int nextWeightsIdx = 0;
			List<BiConsumer<V, Element>> vWeightsWriters = new ArrayList<>();
			List<BiConsumer<E, Element>> eWeightsWriters = new ArrayList<>();
			for (String weightKey : graph.getVerticesWeightsKeys()) {
				Weights<V, ?> weights = graph.getVerticesWeights(weightKey);
				Element weightElm = document.createElement("key");
				String weightId = "d" + nextWeightsIdx++;
				weightElm.setAttribute("id", weightId);
				weightElm.setAttribute("for", "node");
				weightElm.setAttribute("attr.name", weightKey);
				weightElm.setAttribute("attr.type", attrType(weights));
				Object defVal = weights.defaultWeightAsObj();
				if (defVal != null) {
					Element defaultElm = document.createElement("default");
					defaultElm.setTextContent(String.valueOf(defVal));
					weightElm.appendChild(defaultElm);
				}
				rootElm.appendChild(weightElm);

				vWeightsWriters.add((v, vertexElm) -> {
					Object w = weights.getAsObj(v);
					if (Objects.equals(defVal, w))
						return;
					Element dataElm = document.createElement("data");
					dataElm.setAttribute("key", weightId);
					dataElm.setTextContent(String.valueOf(w));
					vertexElm.appendChild(dataElm);
				});
			}
			for (String weightKey : graph.getEdgesWeightsKeys()) {
				Weights<E, ?> weights = graph.getEdgesWeights(weightKey);
				Element weightElm = document.createElement("key");
				String weightId = "d" + nextWeightsIdx++;
				weightElm.setAttribute("id", weightId);
				weightElm.setAttribute("for", "edge");
				weightElm.setAttribute("attr.name", weightKey);
				weightElm.setAttribute("attr.type", attrType(weights));
				Object defVal = weights.defaultWeightAsObj();
				if (defVal != null) {
					Element defaultElm = document.createElement("default");
					defaultElm.setTextContent(String.valueOf(defVal));
					weightElm.appendChild(defaultElm);
				}
				rootElm.appendChild(weightElm);

				eWeightsWriters.add((e, edgeElm) -> {
					Object w = weights.getAsObj(e);
					if (Objects.equals(defVal, w))
						return;
					Element dataElm = document.createElement("data");
					dataElm.setAttribute("key", weightId);
					dataElm.setTextContent(String.valueOf(w));
					edgeElm.appendChild(dataElm);
				});
			}

			Element graphElm = document.createElement("graph");
			graphElm.setAttribute("edgedefault", graph.isDirected() ? "directed" : "undirected");
			graphElm.setAttribute("parse.nodes", String.valueOf(graph.vertices().size()));
			graphElm.setAttribute("parse.edges", String.valueOf(graph.edges().size()));
			rootElm.appendChild(graphElm);

			for (V vertex : graph.vertices()) {
				Element nodeElm = document.createElement("node");
				nodeElm.setAttribute("id", String.valueOf(vertex));
				for (BiConsumer<V, Element> vWeightsWriter : vWeightsWriters)
					vWeightsWriter.accept(vertex, nodeElm);
				graphElm.appendChild(nodeElm);
			}
			for (E edge : graph.edges()) {
				Element edgeElm = document.createElement("edge");
				edgeElm.setAttribute("id", String.valueOf(edge));
				edgeElm.setAttribute("source", String.valueOf(graph.edgeSource(edge)));
				edgeElm.setAttribute("target", String.valueOf(graph.edgeTarget(edge)));
				for (BiConsumer<E, Element> eWeightsWriter : eWeightsWriters)
					eWeightsWriter.accept(edge, edgeElm);
				graphElm.appendChild(edgeElm);
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(writer));

		} catch (TransformerFactoryConfigurationError | ParserConfigurationException | TransformerException e) {
			throw new IOException(e);
		}
	}

	private static String attrType(Weights<?, ?> weights) {
		if (weights instanceof WeightsByte) {
			return "int";
		} else if (weights instanceof WeightsShort) {
			return "int";
		} else if (weights instanceof WeightsInt) {
			return "int";
		} else if (weights instanceof WeightsLong) {
			return "long";
		} else if (weights instanceof WeightsFloat) {
			return "float";
		} else if (weights instanceof WeightsDouble) {
			return "double";
		} else if (weights instanceof WeightsBool) {
			return "boolean";
		} else if (weights instanceof WeightsChar) {
			return "string";
		} else {
			assert weights instanceof WeightsObj;
			return "string";
		}
	}

}
