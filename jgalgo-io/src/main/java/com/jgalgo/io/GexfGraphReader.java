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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;
import it.unimi.dsi.fastutil.booleans.BooleanSet;
import it.unimi.dsi.fastutil.booleans.BooleanUnaryOperator;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.bytes.ByteUnaryOperator;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.chars.CharUnaryOperator;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.doubles.DoubleUnaryOperator;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntUnaryOperator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongUnaryOperator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import it.unimi.dsi.fastutil.shorts.ShortUnaryOperator;

/**
 * Read a graph in 'GEXF' format.
 *
 * <p>
 * GEXF is an XML-based format for graphs. Both directed and undirected graphs are supported, along with mixed graphs
 * (some edges are directed while others are undirected) although the {@link Graph} does not supported mixed graphs
 * (therefore the reader also doesn't support it). The format support graphs with vertices and edges of any type, as
 * long as they can be written as an XML attribute string. The format also support multiple weights for vertices and
 * edges, of any primitive Java type ({@code int}, {@code long}, {@code double}, {@code boolean}, ect.), {@link String},
 * {@link Date}, {@link URI}, {@link BigInteger}, {@link BigDecimal}, along with 'list' (arrays) of any of the above
 * except {@link Date} and {@link URI}.
 *
 * <p>
 * Identifiers of vertices are mandatory, and must be unique. Identifiers of edges are optional, if not specified the
 * reader will try generate them using a builder provided by the user, or a default builder for certain types (see
 * {@link #setEdgeBuilder(IdBuilder)} and {@link #setEdgeBuilderDefault(Class)}). Vertices identifiers (and edges
 * identifiers if specified) are parsed using a parser provided by the user, or a default parser for certain types (see
 * {@link #setVertexParserDefault(Class)} and {@link #setEdgeParserDefault(Class)}).
 *
 * <p>
 * When the reader reads a graph with weights, it will create a {@link Weights} object for each type of weights. For any
 * of the primitive types such as {@code int}, {@code long}, {@code double}, {@code boolean}, ect., the reader will
 * create a {@link Weights} object of the corresponding type, such as {@link WeightsInt}, {@link WeightsLong},
 * {@link WeightsDouble}, {@link WeightsBool}, ect. For {@link String}, {@link Date}, {@link URI}, {@link BigInteger},
 * {@link BigDecimal}, the reader will create a {@link Weights} object of type {@link WeightsObj}. For 'list' types,
 * which are supported for any primitive, {@link String}, {@link BigInteger} and {@link BigDecimal}, the reader will
 * create a {@link Weights} object of type {@link WeightsObj} and will populate it with arrays of the corresponding
 * type, such as {@code int[]}, {@code long[]}, {@code String[]}, {@code BigInteger[]}, ect. Default values are
 * supported for all types of weights, and will be available after reading via {@link Weights#defaultWeightAsObj()} or
 * any of the specific types weights such as {@link WeightsInt#defaultWeight()}. Note that the default value of 'list'
 * types is an array, which is shared between all vertices/edges that do not explicitly specify a value for the weight,
 * and should not be modified.
 *
 * <p>
 * The GEXF format support both self edges and parallel edges. The format documentation can be found
 * <a href= "https://gexf.net/">here</a>.
 *
 * @see    GexfGraphWriter
 * @author Barak Ugav
 */
public class GexfGraphReader<V, E> extends GraphIoUtils.AbstractGraphReader<V, E> {

	private Class<V> vertexType;
	private Class<E> edgeType;
	private Function<String, V> vertexParser;
	private Function<String, E> edgeParser;
	private IdBuilder<E> edgeBuilder;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(Gexf.DateFormat);

	/**
	 * Create a new reader.
	 *
	 * <p>
	 * The user should set the vertex/edge parsers and edge builder manually using {@link #setVertexParser(Function)},
	 * {@link #setEdgeParser(Function)} and {@link #setEdgeBuilder(IdBuilder)}. Setting the vertex parser is mandatory,
	 * while setting the edge parser is only required if edges identifiers are specified. Similarly, setting the edge
	 * builder is only required if edges identifiers are not specified.
	 */
	public GexfGraphReader() {}

	/**
	 * Create a new reader with default parsers and builders for the given vertex and edge types.
	 *
	 * <p>
	 * During the reading process, the reader will use the parser to convert the vertex identifiers from string to the
	 * given type, and similarly for edges if edges identifiers are specified. If edges identifiers are not specified,
	 * the reader will use the builder to generate them. Default parsers exist for types {@code byte}, {@code short},
	 * {@code int}, {@code long}, {@code float}, {@code double} and {@code String}. Default edge builder is instantiated
	 * using {@link IdBuilder#defaultBuilder(Class)}, see it documentation for supported types. If the given types are
	 * not supported by the default parsers and builder, the reader will throw an exception. In such case, the
	 * constructor {@link #GexfGraphReader()} should be used, and the user should set the vertex/edge parsers and edge
	 * builder manually using {@link #setVertexParser(Function)}, {@link #setEdgeParser(Function)} and
	 * {@link #setEdgeBuilder(IdBuilder)}.
	 *
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
	public GexfGraphReader(Class<V> vertexType, Class<E> edgeType) {
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
			if (!doc.getTagName().equals("gexf"))
				throw new IllegalArgumentException("root element is not 'gexf'");
			Element graph = XmlUtils.requiredChild(doc, "graph");

			boolean directed = graph.getAttribute("defaultedgetype").equals("directed");
			GraphFactory<V, E> factory;
			if (vertexType == int.class && edgeType == int.class) {
				@SuppressWarnings("unchecked")
				GraphFactory<V, E> factory0 = (GraphFactory<V, E>) IntGraphFactory.newInstance(directed);
				factory = factory0;
			} else {
				factory = GraphFactory.newInstance(directed);
			}
			GraphBuilder<V, E> g = factory.allowSelfEdges().allowParallelEdges().newBuilder();

			if (XmlUtils.optionalAttribute(graph, "mode").orElse("static").equals("dynamic"))
				throw new IllegalArgumentException("dynamic graphs are not supported");

			Map<String, BiConsumer<V, String>> vWeights = new HashMap<>();
			Map<String, BiConsumer<E, String>> eWeights = new HashMap<>();

			for (Element attributesElm : XmlUtils.children(graph, "attributes")) {
				String clazz = XmlUtils.requiredAttribute(attributesElm, "class");
				boolean isVertices;
				if (clazz.equals("node")) {
					isVertices = true;
				} else if (clazz.equals("edge")) {
					isVertices = false;
				} else {
					throw new IllegalArgumentException("unknown attributes class: " + clazz);
				}

				for (Element attributeElm : XmlUtils.children(attributesElm, "attribute")) {
					String weightsId = XmlUtils.requiredAttribute(attributeElm, "id");
					String weightsName = XmlUtils.requiredAttribute(attributeElm, "title");
					String typeStr = XmlUtils.requiredAttribute(attributeElm, "type");
					String defValStr =
							XmlUtils.optionalChild(attributeElm, "default").map(Element::getTextContent).orElse(null);
					String optionsStr =
							XmlUtils.optionalChild(attributeElm, "options").map(Element::getTextContent).orElse(null);

					Class<?> type;
					Object defVal;
					Function<Weights<Object, Object>, BiConsumer<Object, String>> setterFactory;
					switch (typeStr) {
						case "byte": {
							type = byte.class;
							ByteSet options = optionsStr == null ? null : ByteSet.of(parseListByte(optionsStr));
							ByteUnaryOperator checkOptions = options == null ? ByteUnaryOperator.identity() : b -> {
								if (!options.contains(b))
									throw new IllegalArgumentException(
											"invalid value: " + b + " (not in options: " + options + ")");
								return b;
							};
							defVal = defValStr == null ? null
									: Byte.valueOf(checkOptions.apply(Byte.parseByte(defValStr)));
							setterFactory = weights -> {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								WeightsByte<Object> weights2 = (WeightsByte) weights;
								return (o, val) -> weights2.set(o, checkOptions.apply(Byte.parseByte(val)));
							};
							break;
						}
						case "short": {
							type = short.class;
							ShortSet options = optionsStr == null ? null : ShortSet.of(parseListShort(optionsStr));
							ShortUnaryOperator checkOptions = options == null ? ShortUnaryOperator.identity() : s -> {
								if (!options.contains(s))
									throw new IllegalArgumentException(
											"invalid value: " + s + " (not in options: " + options + ")");
								return s;
							};
							defVal = defValStr == null ? null
									: Short.valueOf(checkOptions.apply(Short.parseShort(defValStr)));
							setterFactory = weights -> {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								WeightsShort<Object> weights2 = (WeightsShort) weights;
								return (o, val) -> weights2.set(o, checkOptions.apply(Short.parseShort(val)));
							};
							break;
						}
						case "integer": {
							type = int.class;
							IntSet options = optionsStr == null ? null : IntSet.of(parseListInt(optionsStr));
							IntUnaryOperator checkOptions = options == null ? IntUnaryOperator.identity() : i -> {
								if (!options.contains(i))
									throw new IllegalArgumentException(
											"invalid value: " + i + " (not in options: " + options + ")");
								return i;
							};
							defVal = defValStr == null ? null
									: Integer.valueOf(checkOptions.apply(Integer.parseInt(defValStr)));
							setterFactory = weights -> {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								WeightsInt<Object> weights2 = (WeightsInt) weights;
								return (o, val) -> weights2.set(o, checkOptions.apply(Integer.parseInt(val)));
							};
							break;
						}
						case "long": {
							type = long.class;
							LongSet options = optionsStr == null ? null : LongSet.of(parseListLong(optionsStr));
							LongUnaryOperator checkOptions = options == null ? LongUnaryOperator.identity() : l -> {
								if (!options.contains(l))
									throw new IllegalArgumentException(
											"invalid value: " + l + " (not in options: " + options + ")");
								return l;
							};
							defVal = defValStr == null ? null
									: Long.valueOf(checkOptions.apply(Long.parseLong(defValStr)));
							setterFactory = weights -> {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								WeightsLong<Object> weights2 = (WeightsLong) weights;
								return (o, val) -> weights2.set(o, checkOptions.apply(Long.parseLong(val)));
							};
							break;
						}
						case "float": {
							type = float.class;
							FloatSet options = optionsStr == null ? null : FloatSet.of(parseListFloat(optionsStr));
							FloatUnaryOperator checkOptions = options == null ? FloatUnaryOperator.identity() : f -> {
								if (!options.contains(f))
									throw new IllegalArgumentException(
											"invalid value: " + f + " (not in options: " + options + ")");
								return f;
							};
							defVal = defValStr == null ? null
									: Float.valueOf(checkOptions.apply(Float.parseFloat(defValStr)));
							setterFactory = weights -> {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								WeightsFloat<Object> weights2 = (WeightsFloat) weights;
								return (o, val) -> weights2.set(o, checkOptions.apply(Float.parseFloat(val)));
							};
							break;
						}
						case "double": {
							type = double.class;
							DoubleSet options = optionsStr == null ? null : DoubleSet.of(parseListDouble(optionsStr));
							DoubleUnaryOperator checkOptions = options == null ? DoubleUnaryOperator.identity() : d -> {
								if (!options.contains(d))
									throw new IllegalArgumentException(
											"invalid value: " + d + " (not in options: " + options + ")");
								return d;
							};
							defVal = defValStr == null ? null
									: Double.valueOf(checkOptions.apply(Double.parseDouble(defValStr)));
							setterFactory = weights -> {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								WeightsDouble<Object> weights2 = (WeightsDouble) weights;
								return (o, val) -> weights2.set(o, checkOptions.apply(Double.parseDouble(val)));
							};
							break;
						}
						case "boolean": {
							type = boolean.class;
							BooleanSet options = optionsStr == null ? null : BooleanSet.of(parseListBool(optionsStr));
							BooleanUnaryOperator checkOptions =
									options == null ? BooleanUnaryOperator.identity() : b -> {
										if (!options.contains(b))
											throw new IllegalArgumentException(
													"invalid value: " + b + " (not in options: " + options + ")");
										return b;
									};
							defVal = defValStr == null ? null
									: Boolean.valueOf(checkOptions.apply(Boolean.parseBoolean(defValStr)));
							setterFactory = weights -> {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								WeightsBool<Object> weights2 = (WeightsBool) weights;
								return (o, val) -> weights2.set(o, checkOptions.apply(Boolean.parseBoolean(val)));
							};
							break;
						}
						case "char": {
							type = char.class;
							CharSet options = optionsStr == null ? null : CharSet.of(parseListChar(optionsStr));
							CharUnaryOperator checkOptions = options == null ? CharUnaryOperator.identity() : c -> {
								if (!options.contains(c))
									throw new IllegalArgumentException(
											"invalid value: " + c + " (not in options: " + options + ")");
								return c;
							};
							defVal = defValStr == null ? null
									: Character.valueOf(checkOptions.apply(GraphIoUtils.parseChar(defValStr)));
							setterFactory = weights -> {
								return (o, val) -> {
									@SuppressWarnings({ "unchecked", "rawtypes" })
									WeightsChar<Object> weights2 = (WeightsChar) weights;
									weights2.set(o, checkOptions.apply(GraphIoUtils.parseChar(val)));
								};
							};
							break;
						}

						/* common for all Object weights */
						case "string":
						case "date":
						case "anyURI":
						case "bigdecimal":
						case "biginteger":
						case "listbyte":
						case "listshort":
						case "listinteger":
						case "listlong":
						case "listfloat":
						case "listdouble":
						case "listboolean":
						case "listchar":
						case "liststring":
						case "listbigdecimal":
						case "listbiginteger": {
							Function<String, Object> parser;
							Set<Object> objOptions = null;

							switch (typeStr) {
								case "string":
									type = String.class;
									objOptions =
											optionsStr == null ? null : new ObjectOpenHashSet<>(splitList(optionsStr));
									parser = s -> s;
									break;
								case "date":
									type = Date.class;
									objOptions = optionsStr == null ? null
											: new ObjectOpenHashSet<>(parseListDate(optionsStr));
									parser = this::parseDate;
									break;
								case "anyURI":
									type = URI.class;
									objOptions = optionsStr == null ? null
											: new ObjectOpenHashSet<>(parseListURI(optionsStr));
									parser = GexfGraphReader::parseURI;
									break;
								case "bigdecimal":
									type = BigDecimal.class;
									objOptions = optionsStr == null ? null
											: new ObjectOpenHashSet<>(parseListBigDecimal(optionsStr));
									parser = BigDecimal::new;
									break;
								case "biginteger":
									type = BigInteger.class;
									objOptions = optionsStr == null ? null
											: new ObjectOpenHashSet<>(parseListBigInteger(optionsStr));
									parser = BigInteger::new;
									break;
								case "listbyte":
									type = byte[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListByte;
									} else {
										ByteSet options = ByteSet.of(parseListByte(optionsStr));
										parser = val -> {
											byte[] list = GexfGraphReader.parseListByte(val);
											for (byte i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listshort":
									type = short[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListShort;
									} else {
										ShortSet options = ShortSet.of(parseListShort(optionsStr));
										parser = val -> {
											short[] list = GexfGraphReader.parseListShort(val);
											for (short i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listinteger":
									type = int[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListInt;
									} else {
										IntSet options = IntSet.of(parseListInt(optionsStr));
										parser = val -> {
											int[] list = GexfGraphReader.parseListInt(val);
											for (int i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listlong":
									type = long[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListLong;
									} else {
										LongSet options = LongSet.of(parseListLong(optionsStr));
										parser = val -> {
											long[] list = GexfGraphReader.parseListLong(val);
											for (long i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listfloat":
									type = float[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListFloat;
									} else {
										FloatSet options = FloatSet.of(parseListFloat(optionsStr));
										parser = val -> {
											float[] list = GexfGraphReader.parseListFloat(val);
											for (float i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listdouble":
									type = double[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListDouble;
									} else {
										DoubleSet options = DoubleSet.of(parseListDouble(optionsStr));
										parser = val -> {
											double[] list = GexfGraphReader.parseListDouble(val);
											for (double i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listboolean":
									type = boolean[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListBool;
									} else {
										BooleanSet options = BooleanSet.of(parseListBool(optionsStr));
										parser = val -> {
											boolean[] list = GexfGraphReader.parseListBool(val);
											for (boolean i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listchar":
									type = char[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListChar;
									} else {
										CharSet options = CharSet.of(parseListChar(optionsStr));
										parser = val -> {
											char[] list = GexfGraphReader.parseListChar(val);
											for (char i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "liststring":
									type = String[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::splitList;
									} else {
										Set<String> options = new ObjectOpenHashSet<>(splitList(optionsStr));
										parser = val -> {
											String[] list = splitList(val);
											for (String i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listbigdecimal":
									type = BigDecimal[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListBigDecimal;
									} else {
										Set<BigDecimal> options =
												new ObjectOpenHashSet<>(parseListBigDecimal(optionsStr));
										parser = val -> {
											BigDecimal[] list = parseListBigDecimal(val);
											for (BigDecimal i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								case "listbiginteger":
									type = BigInteger[].class;
									if (optionsStr == null) {
										parser = GexfGraphReader::parseListBigInteger;
									} else {
										Set<BigInteger> options =
												new ObjectOpenHashSet<>(parseListBigInteger(optionsStr));
										parser = val -> {
											BigInteger[] list = parseListBigInteger(val);
											for (BigInteger i : list)
												if (!options.contains(i))
													throw new IllegalArgumentException("invalid value: " + i
															+ " (not in options: " + options + ")");
											return list;
										};
									}
									break;
								default:
									/* can't each here, already matched the first switch to one of the object types */
									throw new AssertionError();
							}

							if (objOptions != null) {
								Function<String, Object> parser0 = parser;
								Set<Object> objOptions0 = objOptions;
								parser = val -> {
									Object weight = parser0.apply(val);
									if (!objOptions0.contains(weight))
										throw new IllegalArgumentException(
												"invalid value: " + weight + " (not in options: " + objOptions0 + ")");
									return weight;
								};
							}
							defVal = defValStr == null ? null : parser.apply(defValStr);
							Function<String, Object> parser0 = parser;
							setterFactory = weights -> {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								WeightsObj<Object, Object> weights2 = (WeightsObj) weights;
								return (o, val) -> weights2.set(o, parser0.apply(val));
							};
							break;
						}
						default:
							throw new IllegalArgumentException("unknown attribute type: " + typeStr);
					}
					if (isVertices) {
						@SuppressWarnings("unchecked")
						Class<Object> attrType0 = (Class<Object>) type;
						Weights<V, Object> weights = g.addVerticesWeights(weightsName, attrType0, defVal);
						@SuppressWarnings("unchecked")
						BiConsumer<V, String> setter =
								(BiConsumer<V, String>) setterFactory.apply((Weights<Object, Object>) weights);
						Object oldVal = vWeights.put(weightsId, setter);
						if (oldVal != null)
							throw new IllegalArgumentException("duplicate node attribute id: " + weightsId);
					} else {
						@SuppressWarnings("unchecked")
						Class<Object> type0 = (Class<Object>) type;
						Weights<E, Object> weights = g.addEdgesWeights(weightsName, type0, defVal);
						@SuppressWarnings("unchecked")
						BiConsumer<E, String> setter =
								(BiConsumer<E, String>) setterFactory.apply((Weights<Object, Object>) weights);
						Object oldVal = eWeights.put(weightsId, setter);
						if (oldVal != null)
							throw new IllegalArgumentException("duplicate edge attribute id: " + weightsId);
					}
				}
			}

			WeightsObj<V, String> vLabels = null;
			WeightsObj<E, String> eLabels = null;
			WeightsDouble<E> eWeight = null;

			Iterable<Element> nodes = XmlUtils.optionalChild(graph, "nodes")
					.map(nodesElm -> XmlUtils.children(nodesElm, "node")).orElse(Collections.emptyList());
			for (Element vElm : nodes) {
				V v = vertexParser.apply(XmlUtils.requiredAttribute(vElm, "id"));
				g.addVertex(v);

				Optional<String> label = XmlUtils.optionalAttribute(vElm, "label");
				if (label.isPresent()) {
					if (vLabels == null)
						vLabels = g.addVerticesWeights("label", String.class);
					vLabels.set(v, label.get());
				}

				Optional<Element> attvaluesElm0 = XmlUtils.optionalChild(vElm, "attvalues");
				if (attvaluesElm0.isPresent()) {
					Element attvaluesElm = attvaluesElm0.get();
					for (Element attvalueElm : XmlUtils.children(attvaluesElm, "attvalue")) {
						String weightsId = XmlUtils.requiredAttribute(attvalueElm, "for");
						String value = XmlUtils.requiredAttribute(attvalueElm, "value");
						BiConsumer<V, String> setter = vWeights.get(weightsId);
						if (setter == null)
							throw new IllegalArgumentException("unknown attribute id: " + weightsId);
						setter.accept(v, value);
					}
				}
			}

			Iterable<Element> edges = XmlUtils.optionalChild(graph, "edges")
					.map(edgesElm -> XmlUtils.children(edgesElm, "edge")).orElse(Collections.emptyList());
			for (Element eElm : edges) {
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

				if (eElm.hasAttribute("type"))
					throw new IllegalArgumentException("directed attribute per-edge is not supported");

				Optional<String> label = XmlUtils.optionalAttribute(eElm, "label");
				if (label.isPresent()) {
					if (eLabels == null)
						eLabels = g.addEdgesWeights("label", String.class);
					eLabels.set(e, label.get());
				}

				Optional<String> weight = XmlUtils.optionalAttribute(eElm, "weight");
				if (weight.isPresent()) {
					if (eWeight == null)
						eWeight = g.addEdgesWeights("weight", double.class, Double.valueOf(1.0));
					eWeight.set(e, Double.parseDouble(weight.get()));
				}

				Optional<Element> attvaluesElm0 = XmlUtils.optionalChild(eElm, "attvalues");
				if (attvaluesElm0.isPresent()) {
					Element attvaluesElm = attvaluesElm0.get();
					for (Element attvalueElm : XmlUtils.children(attvaluesElm, "attvalue")) {
						String weightsId = XmlUtils.requiredAttribute(attvalueElm, "for");
						String value = XmlUtils.requiredAttribute(attvalueElm, "value");
						BiConsumer<E, String> setter = eWeights.get(weightsId);
						if (setter == null)
							throw new IllegalArgumentException("unknown attribute id: " + weightsId);
						setter.accept(e, value);
					}
				}
			}

			return g;
		} catch (SAXException | ParserConfigurationException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Date parseDate(String s) {
		try {
			return dateFormat.parse(s);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static URI parseURI(String s) {
		try {
			return new URI(s);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static byte[] parseListByte(String val) {
		String[] words = splitList(val);
		byte[] list = new byte[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = Byte.parseByte(words[i]);
		return list;
	}

	private static short[] parseListShort(String val) {
		String[] words = splitList(val);
		short[] list = new short[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = Short.parseShort(words[i]);
		return list;
	}

	private static int[] parseListInt(String val) {
		String[] words = splitList(val);
		int[] list = new int[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = Integer.parseInt(words[i]);
		return list;
	}

	private static long[] parseListLong(String val) {
		String[] words = splitList(val);
		long[] list = new long[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = Long.parseLong(words[i]);
		return list;
	}

	private static float[] parseListFloat(String val) {
		String[] words = splitList(val);
		float[] list = new float[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = Float.parseFloat(words[i]);
		return list;
	}

	private static double[] parseListDouble(String val) {
		String[] words = splitList(val);
		double[] list = new double[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = Double.parseDouble(words[i]);
		return list;
	}

	private static boolean[] parseListBool(String val) {
		String[] words = splitList(val);
		boolean[] list = new boolean[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = Boolean.parseBoolean(words[i]);
		return list;
	}

	private static char[] parseListChar(String val) {
		String[] words = splitList(val);
		char[] list = new char[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = GraphIoUtils.parseChar(words[i]);
		return list;
	}

	private static BigDecimal[] parseListBigDecimal(String val) {
		String[] words = splitList(val);
		BigDecimal[] list = new BigDecimal[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = new BigDecimal(words[i]);
		return list;
	}

	private static BigInteger[] parseListBigInteger(String val) {
		String[] words = splitList(val);
		BigInteger[] list = new BigInteger[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = new BigInteger(words[i]);
		return list;
	}

	private static URI[] parseListURI(String val) {
		String[] words = splitList(val);
		URI[] list = new URI[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = parseURI(words[i]);
		return list;
	}

	private Date[] parseListDate(String val) {
		String[] words = splitList(val);
		Date[] list = new Date[words.length];
		for (int i = 0; i < words.length; i++)
			list[i] = parseDate(words[i]);
		return list;
	}

	private static String[] splitList(String s) {
		if (!s.startsWith("[") || !s.endsWith("]"))
			throw new IllegalArgumentException("list attribute must be of the form [val1, val2,...]");
		s = s.substring(1, s.length() - 1);
		String[] words = s.split(",");
		for (int i = 0; i < words.length; i++)
			words[i] = words[i].trim();
		return words;
	}

}
