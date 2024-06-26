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
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilder;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;

/**
 * Read a graph in 'GraphML' format.
 *
 * <p>
 * GraphML is an XML-based format for graphs. Both directed and undirected graphs are supported, along with mixed graphs
 * (some edges are directed while others are undirected) although the {@link Graph} does not supported mixed graphs
 * (therefore the reader also doesn't support it). The format support graphs with vertices and edges of any type, as
 * long as they can be written as an XML attribute string. The format also support multiple weights for vertices and
 * edges, of type {@code int}, {@code long}, {@code float}, {@code double}, {@code boolean} and {@code String}, and a
 * default value for each weight type.
 *
 * <p>
 * Identifiers of vertices are mandatory, and must be unique. Identifiers of edges are optional, if not specified the
 * reader will try generate them using a builder provided by the user, or a default builder for certain types (see
 * {@link #setEdgeBuilder(IdBuilder)} and {@link #setEdgeBuilderDefault(Class)}). Vertices identifiers (and edges
 * identifiers if specified) are parsed using a parser provided by the user, or a default parser for certain types (see
 * {@link #setVertexParserDefault(Class)} and {@link #setEdgeParserDefault(Class)}).
 *
 * <p>
 * The GraphML format support both self edges and parallel edges.
 *
 * @see        GraphMlGraphWriter
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public final class GraphMlGraphReader<V, E> extends GraphIoUtils.AbstractGraphReader<V, E> {

	private Class<V> vertexType;
	private Class<E> edgeType;
	private Function<String, V> vertexParser;
	private Function<String, E> edgeParser;
	private IdBuilder<E> edgeBuilder;

	/**
	 * Create a new reader.
	 *
	 * <p>
	 * The user should set the vertex/edge parsers and edge builder manually using {@link #setVertexParser(Function)},
	 * {@link #setEdgeParser(Function)} and {@link #setEdgeBuilder(IdBuilder)}. Setting the vertex parser is mandatory,
	 * while setting the edge parser is only required if edges identifiers are specified. Similarly, setting the edge
	 * builder is only required if edges identifiers are not specified.
	 */
	public GraphMlGraphReader() {}

	/**
	 * Create a new reader with default parsers and builders for the given vertex and edge types.
	 *
	 * <p>
	 * During the reading process, the reader will use the parser to convert the vertex identifiers from string to the
	 * given type, and similarly for edges if edges identifiers are specified. If edges identifiers are not specified,
	 * the reader will use the builder to generate them. Default parsers exists for types {@code byte}, {@code short},
	 * {@code int}, {@code long}, {@code float}, {@code double} and {@code String}. Default edge builder is instantiated
	 * using {@link IdBuilder#defaultBuilder(Class)}, see it documentation for supported types If the given types are
	 * not supported by the default parsers and builder, the reader will throw an exception. In such case, the
	 * constructor {@link #GraphMlGraphReader()} should be used, and the user should set the vertex/edge parsers and
	 * edge builder manually using {@link #setVertexParser(Function)}, {@link #setEdgeParser(Function)} and
	 * {@link #setEdgeBuilder(IdBuilder)}.
	 *
	 * @param  vertexType               the type of the vertices
	 * @param  edgeType                 the type of the edges
	 * @throws IllegalArgumentException if the given types are not supported by the default vertex/edge parsers and edge
	 *                                      builder. The supported types are {@code byte}, {@code short}, {@code int},
	 *                                      {@code long}, {@code float}, {@code double} and {@code String}.
	 * @see                             #setVertexParserDefault(Class)
	 * @see                             #setEdgeParserDefault(Class)
	 * @see                             #setEdgeBuilderDefault(Class)
	 */
	public GraphMlGraphReader(Class<V> vertexType, Class<E> edgeType) {
		setVertexParserDefault(this.vertexType = vertexType);
		setEdgeParserDefault(this.edgeType = edgeType);
		setEdgeBuilderDefault(this.edgeType);
	}

	/**
	 * Set the parser for the vertices identifiers.
	 *
	 * <p>
	 * The parser is used to convert the vertex identifiers from string to the given vertex type. The parser is
	 * mandatory, and must be set before reading a graph. For default parsers for certain types, see
	 * {@link #setVertexParserDefault(Class)}.
	 *
	 * @param vertexParser a parser for the vertices identifiers
	 */
	public void setVertexParser(Function<String, V> vertexParser) {
		this.vertexParser = Objects.requireNonNull(vertexParser);
	}

	/**
	 * Set the parser for the vertices identifiers, using a default parser for the given vertex type.
	 *
	 * <p>
	 * The parser is used to convert the vertex identifiers from string to the given vertex type. The parser is
	 * mandatory, and must be set before reading a graph. The default parser exists for types {@code byte},
	 * {@code short}, {@code int}, {@code long}, {@code float}, {@code double} and {@code String}. If the given type is
	 * not supported by the default parser, the reader will throw an exception. In such case, the method
	 * {@link #setVertexParser(Function)} should be used for custom parsing.
	 *
	 * @param  vertexType               the type of the vertices
	 * @throws IllegalArgumentException if the given type is not supported by the default parser. The supported types
	 *                                      are {@code byte}, {@code short}, {@code int}, {@code long}, {@code float},
	 *                                      {@code double} and {@code String}.
	 */
	public void setVertexParserDefault(Class<V> vertexType) {
		this.vertexParser = GraphIoUtils.defaultParser(vertexType);
		this.vertexType = vertexType;
	}

	/**
	 * Set the parser for the edges identifiers.
	 *
	 * <p>
	 * The parser is used to convert the edges identifiers from string to the given edge type. The parser is mandatory
	 * if edges identifiers are specified. In case edge identifiers are not specified, an edge builder must be set (see
	 * {@link #setEdgeBuilder(IdBuilder)}). For default parsers for certain types, see
	 * {@link #setEdgeParserDefault(Class)}.
	 *
	 * @param edgeParser a parser for the edges identifiers
	 */
	public void setEdgeParser(Function<String, E> edgeParser) {
		this.edgeParser = Objects.requireNonNull(edgeParser);
	}

	/**
	 * Set the parser for the edges identifiers, using a default parser for the given edge type.
	 *
	 * <p>
	 * The parser is used to convert the edges identifiers from string to the given edge type. The parser is mandatory
	 * if edges identifiers are specified. In case edge identifiers are not specified, an edge builder must be set (see
	 * {@link #setEdgeBuilder(IdBuilder)}). The default parser exists for types {@code byte}, {@code short},
	 * {@code int}, {@code long}, {@code float}, {@code double} and {@code String}. If the given type is not supported
	 * by the default parser, the reader will throw an exception. In such case, the method
	 * {@link #setEdgeParser(Function)} should be used for custom parsing.
	 *
	 * @param  edgeType                 the type of the edges
	 * @throws IllegalArgumentException if the given type is not supported by the default parser. The supported types
	 *                                      are {@code byte}, {@code short}, {@code int}, {@code long}, {@code float},
	 *                                      {@code double} and {@code String}.
	 */
	public void setEdgeParserDefault(Class<E> edgeType) {
		this.edgeParser = GraphIoUtils.defaultParser(edgeType);
		this.edgeType = edgeType;
	}

	/**
	 * Set the builder for the edges identifiers.
	 *
	 * <p>
	 * The builder is used to generate edges identifiers if edges identifiers are not specified. The builder is
	 * mandatory if edges identifiers are not specified. In case edge identifiers are specified, an edge parser must be
	 * set (see {@link #setEdgeParser(Function)}). For default builders for certain types, see
	 * {@link #setEdgeBuilderDefault(Class)}.
	 *
	 * <p>
	 * The edge builder accepts a set of existing edges, and should return a new edge identifier that is not in the set.
	 *
	 * @param edgeBuilder a builder for the edges identifiers
	 */
	public void setEdgeBuilder(IdBuilder<E> edgeBuilder) {
		this.edgeBuilder = Objects.requireNonNull(edgeBuilder);
	}

	/**
	 * Set the builder for the edges identifiers, using a default builder for the given edge type.
	 *
	 * <p>
	 * The builder is used to generate edges identifiers if edges identifiers are not specified. The builder is
	 * mandatory if edges identifiers are not specified. In case edge identifiers are specified, an edge parser must be
	 * set (see {@link #setEdgeParser(Function)}). The default builder is instantiated using
	 * {@link IdBuilder#defaultBuilder(Class)}, see it documentation for supported types. If the given type is not
	 * supported by the default builder, the reader will throw an exception. In such case, the method
	 * {@link #setEdgeBuilder(IdBuilder)} should be used for custom builder.
	 *
	 * @param  edgeType                 the type of the edges
	 * @throws IllegalArgumentException if the given type is not supported by the default builder. See
	 *                                      {@link IdBuilder#defaultBuilder(Class)} for supported types
	 */
	public void setEdgeBuilderDefault(Class<E> edgeType) {
		edgeBuilder = IdBuilder.defaultBuilder(edgeType);
		this.edgeType = edgeType;
	}

	@Override
	GraphBuilder<V, E> readIntoBuilderImpl(Reader reader) throws IOException {
		if (vertexParser == null)
			throw new IllegalStateException("Vertex parser was not set");
		try {
			Document document =
					DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(reader));
			Element doc = document.getDocumentElement();
			doc.normalize();

			if (!doc.getTagName().equals("graphml"))
				throw new IllegalArgumentException("root element is not 'graphml'");

			// searchUnexpectedChildren(doc, "graph", "key");

			Element graph = XmlUtils.requiredChild(doc, "graph");
			// searchUnexpectedChildren(graph, "node", "edge");

			boolean directed = XmlUtils.requiredAttribute(graph, "edgedefault").equals("directed");
			GraphFactory<V, E> factory;
			if (vertexType == int.class && edgeType == int.class) {
				@SuppressWarnings("unchecked")
				GraphFactory<V, E> factory0 = (GraphFactory<V, E>) IntGraphFactory.newInstance(directed);
				factory = factory0;
			} else {
				factory = GraphFactory.newInstance(directed);
			}
			GraphBuilder<V, E> g = factory.allowSelfEdges().allowParallelEdges().newBuilder();
			XmlUtils.optionalAttribute(graph, "parse.nodes").map(Integer::parseInt).ifPresent(g::ensureVertexCapacity);
			XmlUtils.optionalAttribute(graph, "parse.edges").map(Integer::parseInt).ifPresent(g::ensureEdgeCapacity);

			Map<String, BiConsumer<V, String>> vWeights = new HashMap<>();
			Map<String, BiConsumer<E, String>> eWeights = new HashMap<>();
			for (Element key : XmlUtils.children(doc, "key")) {
				String weightId = XmlUtils.requiredAttribute(key, "id");
				String domain = XmlUtils.requiredAttribute(key, "for");
				String attrName = XmlUtils.requiredAttribute(key, "attr.name");
				String attrTypeStr = XmlUtils.requiredAttribute(key, "attr.type");
				String defValStr = XmlUtils.optionalChild(key, "default").map(Element::getTextContent).orElse(null);

				Class<?> attrType;
				Object defVal;
				Function<Weights<Object, Object>, BiConsumer<Object, String>> setterFactory;
				switch (attrTypeStr) {
					case "int": {
						attrType = int.class;
						defVal = defValStr == null ? null : Integer.valueOf(defValStr);
						setterFactory = weights -> {
							@SuppressWarnings({ "unchecked", "rawtypes" })
							WeightsInt<Object> weights2 = (WeightsInt) weights;
							return (o, val) -> weights2.set(o, Integer.parseInt(val));
						};
						break;
					}
					case "long": {
						attrType = long.class;
						defVal = defValStr == null ? null : Long.valueOf(defValStr);
						setterFactory = weights -> {
							@SuppressWarnings({ "unchecked", "rawtypes" })
							WeightsLong<Object> weights2 = (WeightsLong) weights;
							return (o, val) -> weights2.set(o, Long.parseLong(val));
						};
						break;
					}
					case "float": {
						attrType = float.class;
						defVal = defValStr == null ? null : Float.valueOf(defValStr);
						setterFactory = weights -> {
							@SuppressWarnings({ "unchecked", "rawtypes" })
							WeightsFloat<Object> weights2 = (WeightsFloat) weights;
							return (o, val) -> weights2.set(o, Float.parseFloat(val));
						};
						break;
					}
					case "double": {
						attrType = double.class;
						defVal = defValStr == null ? null : Double.valueOf(defValStr);
						setterFactory = weights -> {
							@SuppressWarnings({ "unchecked", "rawtypes" })
							WeightsDouble<Object> weights2 = (WeightsDouble) weights;
							return (o, val) -> weights2.set(o, Double.parseDouble(val));
						};
						break;
					}
					case "boolean": {
						attrType = boolean.class;
						defVal = defValStr == null ? null : Boolean.valueOf(defValStr);
						setterFactory = weights -> {
							@SuppressWarnings({ "unchecked", "rawtypes" })
							WeightsBool<Object> weights2 = (WeightsBool) weights;
							return (o, val) -> weights2.set(o, Boolean.parseBoolean(val));
						};
						break;
					}
					case "string": {
						attrType = String.class;
						defVal = defValStr;
						setterFactory = weights -> {
							@SuppressWarnings({ "unchecked", "rawtypes" })
							WeightsObj<Object, String> weights2 = (WeightsObj) weights;
							return (o, val) -> weights2.set(o, val);
						};
						break;
					}
					default:
						throw new IllegalArgumentException("unknown attr.type: '" + attrTypeStr + "'");
				}
				boolean isVertexWeight;
				boolean isEdgeWeight;
				switch (domain) {
					case "node":
						isVertexWeight = true;
						isEdgeWeight = false;
						break;
					case "edge":
						isVertexWeight = false;
						isEdgeWeight = true;
						break;
					case "all":
						isVertexWeight = true;
						isEdgeWeight = true;
						break;
					default:
						throw new IllegalArgumentException("unknown domain: " + domain);
				}
				if (isVertexWeight) {
					@SuppressWarnings("unchecked")
					Class<Object> attrType0 = (Class<Object>) attrType;
					Weights<V, Object> weights = g.addVerticesWeights(attrName, attrType0, defVal);
					@SuppressWarnings("unchecked")
					BiConsumer<V, String> setter =
							(BiConsumer<V, String>) setterFactory.apply((Weights<Object, Object>) weights);
					Object oldVal = vWeights.put(weightId, setter);
					if (oldVal != null)
						throw new IllegalArgumentException("duplicate vertex weight key: " + weightId);
				}
				if (isEdgeWeight) {
					@SuppressWarnings("unchecked")
					Class<Object> attrType0 = (Class<Object>) attrType;
					Weights<E, Object> weights = g.addEdgesWeights(attrName, attrType0, defVal);
					@SuppressWarnings("unchecked")
					BiConsumer<E, String> setter =
							(BiConsumer<E, String>) setterFactory.apply((Weights<Object, Object>) weights);
					Object oldVal = eWeights.put(weightId, setter);
					if (oldVal != null)
						throw new IllegalArgumentException("duplicate edge weight key: " + weightId);
				}
			}

			for (Element vElm : XmlUtils.children(graph, "node")) {
				V v = vertexParser.apply(XmlUtils.requiredAttribute(vElm, "id"));
				g.addVertex(v);
				for (Element dataElm : XmlUtils.children(vElm, "data")) {
					String weightId = XmlUtils.requiredAttribute(dataElm, "key");
					BiConsumer<V, String> vWeight = vWeights.get(weightId);
					if (vWeight == null)
						throw new IllegalArgumentException("unknown weight key: " + weightId);
					vWeight.accept(v, dataElm.getTextContent());
				}
				// searchUnexpectedChildren(vElm, "data");
			}
			for (Element eElm : XmlUtils.children(graph, "edge")) {
				E e;
				Optional<String> id = XmlUtils.optionalAttribute(eElm, "id");
				if (id.isPresent()) {
					if (edgeParser == null)
						throw new IllegalStateException("Edge parser was not set");
					e = edgeParser.apply(id.get());
				} else {
					if (edgeBuilder == null)
						throw new IllegalStateException("Edge builder was not set");
					e = edgeBuilder.build(g.edges());
				}
				V u = vertexParser.apply(XmlUtils.requiredAttribute(eElm, "source"));
				V v = vertexParser.apply(XmlUtils.requiredAttribute(eElm, "target"));
				g.addEdge(u, v, e);

				if (eElm.hasAttribute("directed"))
					throw new IllegalArgumentException("directed attribute per-edge is not supported");

				for (Element dataElm : XmlUtils.children(eElm, "data")) {
					String weightId = XmlUtils.requiredAttribute(dataElm, "key");
					BiConsumer<E, String> eWeight = eWeights.get(weightId);
					if (eWeight == null)
						throw new IllegalArgumentException("unknown weight key: " + weightId);
					eWeight.accept(e, dataElm.getTextContent());
				}
				// searchUnexpectedChildren(eElm, "data");
			}

			return g;

		} catch (ParserConfigurationException | SAXException e) {
			throw new IllegalArgumentException(e);
		}
	}

	// private static void searchUnexpectedChildren(Element element, String... tags) {
	// List<String> tagsList = ObjectImmutableList.of(tags);
	// for (Element child : children(element))
	// if (!tagsList.contains(child.getTagName()))
	// throw new IllegalArgumentException("unexpected element: " + child.getTagName());
	// }

}
