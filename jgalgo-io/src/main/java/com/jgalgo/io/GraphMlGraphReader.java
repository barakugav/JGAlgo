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
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
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
 * edges, of type boolean, int, long, float, double or string, and a default value for each weight type.
 *
 * <p>
 * Identifiers of vertices are mandatory, and must be unique. Identifiers of edges are optional, if not specified the
 * reader will try generate them using a supplier provided by the user, or a default supplier for certain types (see
 * {@link #setEdgeSupplier(Function)} and {@link #setEdgeSupplierDefault(Class)}). Vertices identifiers (and edges
 * identifiers if specified) are parsed using a parser provided by the user, or a default parser for certain types (see
 * {@link #setVertexParserDefault(Class)} and {@link #setEdgeParserDefault(Class)}).
 *
 * <p>
 * The GraphML format support both self edges and parallel edges.
 *
 * @see    GraphMlGraphWriter
 * @author Barak Ugav
 */
public class GraphMlGraphReader<V, E> extends GraphIoUtils.AbstractGraphReader<V, E> {

	private Class<V> vertexType;
	private Class<E> edgeType;
	private Function<String, V> vertexParser;
	private Function<String, E> edgeParser;
	private Function<Set<E>, E> edgeSupplier;

	/**
	 * Create a new reader.
	 *
	 * <p>
	 * The user should set the parsers and supplier manually using {@link #setVertexParser(Function)},
	 * {@link #setEdgeParser(Function)} and {@link #setEdgeSupplier(Function)}. Setting the vertex parser is mandatory,
	 * while setting the edge parser is only required if edges identifiers are specified. Similarly, setting the edge
	 * supplier is only required if edges identifiers are not specified.
	 */
	public GraphMlGraphReader() {}

	/**
	 * Create a new reader with default parsers and suppliers for the given vertex and edge types.
	 *
	 * <p>
	 * During the reading process, the reader will use the parser to convert the vertex identifiers from string to the
	 * given type, and similarly for edges if edges identifiers are specified. If edges identifiers are not specified,
	 * the reader will use the supplier to generate them. A default parsers and edge supplier exists for types byte,
	 * short, int, long, float, double and String, which will be used by the reader for the given types. If the given
	 * types are not supported by the default parsers and supplier, the reader will throw an exception. In such case,
	 * the constructor {@link #GraphMlGraphReader()} should be used, and the user should set the parsers and supplier
	 * manually using {@link #setVertexParser(Function)}, {@link #setEdgeParser(Function)} and
	 * {@link #setEdgeSupplier(Function)}.
	 *
	 *
	 * @param  vertexType               the type of the vertices
	 * @param  edgeType                 the type of the edges
	 * @throws IllegalArgumentException if the given types are not supported by the default parsers and supplier. The
	 *                                      supported types are byte, short, int, long, float, double and String.
	 * @see                             #setVertexParserDefault(Class)
	 * @see                             #setEdgeParserDefault(Class)
	 * @see                             #setEdgeSupplierDefault(Class)
	 */
	public GraphMlGraphReader(Class<V> vertexType, Class<E> edgeType) {
		setVertexParserDefault(this.vertexType = vertexType);
		setEdgeParserDefault(this.edgeType = edgeType);
		setEdgeSupplierDefault(this.edgeType);
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
	 * mandatory, and must be set before reading a graph. The default parser exists for types byte, short, int, long,
	 * float, double and String. If the given type is not supported by the default parser, the reader will throw an
	 * exception. In such case, the method {@link #setVertexParser(Function)} should be used for custom parsing.
	 *
	 * @param  vertexType               the type of the vertices
	 * @throws IllegalArgumentException if the given type is not supported by the default parser. The supported types
	 *                                      are byte, short, int, long, float, double and String.
	 */
	public void setVertexParserDefault(Class<V> vertexType) {
		this.vertexParser = defaultParser(vertexType);
		this.vertexType = vertexType;
	}

	/**
	 * Set the parser for the edges identifiers.
	 *
	 * <p>
	 * The parser is used to convert the edges identifiers from string to the given edge type. The parser is mandatory
	 * if edges identifiers are specified. In case edge identifiers are not specified, an edge supplier must be set (see
	 * {@link #setEdgeSupplier(Function)}). For default parsers for certain types, see
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
	 * if edges identifiers are specified. In case edge identifiers are not specified, an edge supplier must be set (see
	 * {@link #setEdgeSupplier(Function)}). The default parser exists for types byte, short, int, long, float, double
	 * and String. If the given type is not supported by the default parser, the reader will throw an exception. In such
	 * case, the method {@link #setEdgeParser(Function)} should be used for custom parsing.
	 *
	 * @param  edgeType                 the type of the edges
	 * @throws IllegalArgumentException if the given type is not supported by the default parser. The supported types
	 *                                      are byte, short, int, long, float, double and String.
	 */
	public void setEdgeParserDefault(Class<E> edgeType) {
		this.edgeParser = defaultParser(edgeType);
		this.edgeType = edgeType;
	}

	@SuppressWarnings("unchecked")
	private static <K> Function<String, K> defaultParser(Class<K> type) {
		if (type == byte.class || type == Byte.class) {
			Function<String, Byte> parser = Byte::valueOf;
			return (Function<String, K>) parser;

		} else if (type == short.class || type == Short.class) {
			Function<String, Short> parser = Short::valueOf;
			return (Function<String, K>) parser;

		} else if (type == int.class || type == Integer.class) {
			Function<String, Integer> parser = Integer::valueOf;
			return (Function<String, K>) parser;

		} else if (type == long.class || type == Long.class) {
			Function<String, Long> parser = Long::valueOf;
			return (Function<String, K>) parser;

		} else if (type == float.class || type == Float.class) {
			Function<String, Float> parser = Float::valueOf;
			return (Function<String, K>) parser;

		} else if (type == double.class || type == Double.class) {
			Function<String, Double> parser = Double::valueOf;
			return (Function<String, K>) parser;

		} else if (type == String.class) {
			Function<String, String> parser = Function.identity();
			return (Function<String, K>) parser;

		} else {
			throw new IllegalArgumentException("no default parser for type: " + type);
		}
	}

	/**
	 * Set the supplier for the edges identifiers.
	 *
	 * <p>
	 * The supplier is used to generate edges identifiers if edges identifiers are not specified. The supplier is
	 * mandatory if edges identifiers are not specified. In case edge identifiers are specified, an edge parser must be
	 * set (see {@link #setEdgeParser(Function)}). For default suppliers for certain types, see
	 * {@link #setEdgeSupplierDefault(Class)}.
	 *
	 * <p>
	 * The edge supplier accepts a set of existing edges, and should return a new edge identifier that is not in the
	 * set.
	 *
	 * @param edgeSupplier a supplier for the edges identifiers
	 */
	public void setEdgeSupplier(Function<Set<E>, E> edgeSupplier) {
		this.edgeSupplier = Objects.requireNonNull(edgeSupplier);
	}

	/**
	 * Set the supplier for the edges identifiers, using a default supplier for the given edge type.
	 *
	 * <p>
	 * The supplier is used to generate edges identifiers if edges identifiers are not specified. The supplier is
	 * mandatory if edges identifiers are not specified. In case edge identifiers are specified, an edge parser must be
	 * set (see {@link #setEdgeParser(Function)}). The default supplier exists for types byte, short, int, long, float,
	 * double and String. If the given type is not supported by the default supplier, the reader will throw an
	 * exception. In such case, the method {@link #setEdgeSupplier(Function)} should be used for custom supplier.
	 *
	 * @param  edgeType                 the type of the edges
	 * @throws IllegalArgumentException if the given type is not supported by the default supplier. The supported types
	 *                                      are byte, short, int, long, float, double and String.
	 */
	public void setEdgeSupplierDefault(Class<E> edgeType) {
		if (edgeType == byte.class || edgeType == Byte.class) {
			long min = Byte.MIN_VALUE, max = Byte.MAX_VALUE, maxEdgesSize = 1 << Byte.SIZE;
			edgeSupplier = defaultEdgeSupplier(min, max, maxEdgesSize, x -> Byte.valueOf((byte) x));

		} else if (edgeType == short.class || edgeType == Short.class) {
			long min = Short.MIN_VALUE, max = Short.MAX_VALUE, maxEdgesSize = 1 << Short.SIZE;
			edgeSupplier = defaultEdgeSupplier(min, max, maxEdgesSize, x -> Short.valueOf((short) x));

		} else if (edgeType == int.class || edgeType == Integer.class) {
			long min = Integer.MIN_VALUE, max = Integer.MAX_VALUE, maxEdgesSize = 1L << Integer.SIZE;
			edgeSupplier = defaultEdgeSupplier(min, max, maxEdgesSize, x -> Integer.valueOf((int) x));

		} else if (edgeType == long.class || edgeType == Long.class) {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			edgeSupplier = defaultEdgeSupplier(min, max, maxEdgesSize, x -> Long.valueOf(x));

		} else if (edgeType == float.class || edgeType == Float.class) {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			edgeSupplier = defaultEdgeSupplier(min, max, maxEdgesSize, x -> Float.valueOf(x));

		} else if (edgeType == double.class || edgeType == Double.class) {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			edgeSupplier = defaultEdgeSupplier(min, max, maxEdgesSize, x -> Double.valueOf(x));

		} else if (edgeType == String.class) {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			edgeSupplier = defaultEdgeSupplier(min, max, maxEdgesSize, x -> ("e" + x));

		} else {
			throw new IllegalArgumentException("no default edge supplier for type: " + edgeType);
		}
		this.edgeType = edgeType;
	}

	private static <E> Function<Set<E>, E> defaultEdgeSupplier(long minVal, long maxVal, long maxEdgesSize,
			LongFunction<Object> idBuilder) {
		return new Function<>() {
			long nextId;

			@SuppressWarnings("unchecked")
			@Override
			public E apply(Set<E> existingEdges) {
				if (existingEdges.size() >= maxEdgesSize)
					throw new IllegalArgumentException("too many edges");
				for (Object id;;)
					if (!existingEdges.contains(id = idBuilder.apply(getAndInc())))
						return (E) id;
			}

			private long getAndInc() {
				long ret = nextId;
				if (nextId < maxVal) {
					nextId++;
				} else {
					nextId = minVal;
				}
				return ret;
			}
		};
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
				GraphFactory<V, E> factory0 = (GraphFactory<V, E>) (directed ? IntGraphFactory.newDirected()
						: IntGraphFactory.newUndirected());
				factory = factory0;
			} else {
				factory = directed ? GraphFactory.newDirected() : GraphFactory.newUndirected();
			}
			GraphBuilder<V, E> g = factory.allowSelfEdges().allowParallelEdges().newBuilder();
			XmlUtils.optionalAttribute(graph, "parse.nodes").map(Integer::parseInt).ifPresent(g::expectedVerticesNum);
			XmlUtils.optionalAttribute(graph, "parse.edges").map(Integer::parseInt).ifPresent(g::expectedEdgesNum);

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
					vWeights.put(weightId, setter);
				}
				if (isEdgeWeight) {
					@SuppressWarnings("unchecked")
					Class<Object> attrType0 = (Class<Object>) attrType;
					Weights<E, Object> weights = g.addEdgesWeights(attrName, attrType0, defVal);
					@SuppressWarnings("unchecked")
					BiConsumer<E, String> setter =
							(BiConsumer<E, String>) setterFactory.apply((Weights<Object, Object>) weights);
					eWeights.put(weightId, setter);
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
					if (edgeSupplier == null)
						throw new IllegalStateException("Edge supplier was not set");
					e = edgeSupplier.apply(g.edges());
				}
				V u = vertexParser.apply(XmlUtils.requiredAttribute(eElm, "source"));
				V v = vertexParser.apply(XmlUtils.requiredAttribute(eElm, "target"));
				if (XmlUtils.optionalAttribute(eElm, "directed").isPresent())
					throw new IllegalArgumentException("directed attribute per-edge is not supported");
				g.addEdge(u, v, e);
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
