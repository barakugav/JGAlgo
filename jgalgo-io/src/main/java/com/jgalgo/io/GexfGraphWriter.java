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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.shorts.ShortList;

/**
 * Write a graph in 'GEXF' format.
 *
 * <p>
 * GEXF is an XML-based format for graphs. Both directed and undirected graphs are supported, along with mixed graphs
 * (some edges are directed while others are undirected) although the {@link Graph} does not supported mixed graphs
 * (therefore the writer also doesn't support it). The format support graphs with vertices and edges of any type, as
 * long as they can be written as an XML attribute string. The format also support multiple weights for vertices and
 * edges, of any primitive Java type ({@code int}, {@code long}, {@code double}, {@code boolean}, ect.), {@link String},
 * {@link Date}, {@link URI}, {@link BigInteger}, {@link BigDecimal}, along with 'list' (arrays) of any of the above
 * except {@link Date} and {@link URI}.
 *
 * <p>
 * When the writer writes a graph with weights, it will try to identify the type of weights and write them accordingly.
 * For any of the primitive type weights containers such as {@link WeightsInt}, {@link WeightsLong},
 * {@link WeightsDouble}, {@link WeightsBool}, ect., the writer will easily identify the primitive types. On the other
 * hand, for {@link WeightsObj}, the writer will try to identify the type of the weights by checking the type of the
 * default value and the actual weights of the vertices/edges. If all weights (and default value if exists) are a boxed
 * type of any primitive type, the writer will write the weights as the corresponding primitive type. If all weights are
 * {@link String}, {@link Date}, {@link URI}, {@link BigInteger}, {@link BigDecimal}, the writer will be able to
 * identify the type of the weights and write them accordingly. If all weights are arrays of primitive types (such as
 * {@code int[]}, {@code long[]}, {@code double[]}, {@code boolean[]}, ect.), or fastutil lists of primitive types (such
 * as {@link IntList}, {@link LongList}, {@link DoubleList}, {@link BooleanList}, ect.), or {@link List} of boxed
 * primitive types (such as {@code List<Integer>}, {@code List<Long>}, {@code List<Double>}, {@code List<Boolean>},
 * ect.), the writer will write the weights 'list' (array) type of the primitive type (the {@linkplain GexfGraphReader
 * reader} will read them as arrays of primitives). If all weights are arrays of {@link String}, {@link BigInteger},
 * {@link BigDecimal}, or {@link List} of {@link String}, {@link BigInteger}, {@link BigDecimal}, the writer will write
 * the weights as 'list' (array) of {@link String}, {@link BigInteger}, {@link BigDecimal} respectively (the
 * {@linkplain GexfGraphReader reader} will read them as arrays of objects). Note that {@code null} weights are not
 * supported and will cause an exception to be thrown.
 *
 * <p>
 * The GEXF format support both self edges and parallel edges. The format documentation can be found
 * <a href= "https://gexf.net/">here</a>.
 *
 * @see    GexfGraphReader
 * @author Barak Ugav
 */
public class GexfGraphWriter<V, E> extends GraphIoUtils.AbstractGraphWriter<V, E> {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat(Gexf.DateFormat);

	/**
	 * Create a new writer.
	 */
	public GexfGraphWriter() {}

	@Override
	void writeGraphImpl(Graph<V, E> graph, Writer writer) throws IOException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element rootElm = document.createElement("gexf");
			rootElm.setAttribute("xmlns", "http://gexf.net/1.3");
			document.appendChild(rootElm);

			Element metaElm = document.createElement("meta");
			metaElm
					.setAttribute("lastmodifieddate",
							new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
			Element creatorElm = document.createElement("creator");
			creatorElm.setTextContent("jgalgo.com");
			metaElm.appendChild(creatorElm);
			rootElm.appendChild(metaElm);

			Element graphElm = document.createElement("graph");
			graphElm.setAttribute("defaultedgetype", graph.isDirected() ? "directed" : "undirected");
			rootElm.appendChild(graphElm);

			Element nodeAttributesElm = null;
			if (!graph.getVerticesWeightsKeys().isEmpty()) {
				nodeAttributesElm = document.createElement("attributes");
				nodeAttributesElm.setAttribute("class", "node");
				graphElm.appendChild(nodeAttributesElm);
			}
			List<Function<V, Element>> vWeightsWriters = new ArrayList<>();
			for (String weightKey : graph.getVerticesWeightsKeys()) {
				Weights<V, ?> weights = graph.getVerticesWeights(weightKey);
				Pair<String, Function<Object, String>> attrWriter0 = attrWriter(weights, graph.vertices());
				if (attrWriter0 == null)
					throw new IllegalArgumentException(
							"Vertices weights with key '" + weightKey + "' are not supported");
				String attrType = attrWriter0.first();
				Function<Object, String> attrWriter = attrWriter0.second();

				Element weightElm = document.createElement("attribute");
				String weightId = String.valueOf(vWeightsWriters.size());
				weightElm.setAttribute("id", weightId);
				weightElm.setAttribute("title", weightKey);
				weightElm.setAttribute("type", attrType);
				Object defVal = weights.defaultWeightAsObj();
				if (defVal != null) {
					Element defaultElm = document.createElement("default");
					defaultElm.setTextContent(attrWriter.apply(defVal));
					weightElm.appendChild(defaultElm);
				}
				nodeAttributesElm.appendChild(weightElm);

				vWeightsWriters.add(v -> {
					Object w = weights.getAsObj(v);
					if (Objects.equals(defVal, w))
						return null;
					Element dataElm = document.createElement("attvalue");
					dataElm.setAttribute("for", weightId);
					dataElm.setAttribute("value", attrWriter.apply(w));
					return dataElm;
				});
			}

			Element edgeAttributesElm = null;
			if (!graph.getEdgesWeightsKeys().isEmpty()) {
				edgeAttributesElm = document.createElement("attributes");
				edgeAttributesElm.setAttribute("class", "edge");
				graphElm.appendChild(edgeAttributesElm);
			}
			List<Function<E, Element>> eWeightsWriters = new ArrayList<>();
			for (String weightKey : graph.getEdgesWeightsKeys()) {
				Weights<E, ?> weights = graph.getEdgesWeights(weightKey);
				Pair<String, Function<Object, String>> attrWriter0 = attrWriter(weights, graph.edges());
				if (attrWriter0 == null)
					throw new IllegalArgumentException("Edges weights with key '" + weightKey + "' are not supported");
				String attrType = attrWriter0.first();
				Function<Object, String> attrWriter = attrWriter0.second();

				Element weightElm = document.createElement("attribute");
				String weightId = String.valueOf(eWeightsWriters.size());
				weightElm.setAttribute("id", weightId);
				weightElm.setAttribute("title", weightKey);
				weightElm.setAttribute("type", attrType);
				Object defVal = weights.defaultWeightAsObj();
				if (defVal != null) {
					Element defaultElm = document.createElement("default");
					defaultElm.setTextContent(attrWriter.apply(defVal));
					weightElm.appendChild(defaultElm);
				}
				edgeAttributesElm.appendChild(weightElm);

				eWeightsWriters.add(e -> {
					Object w = weights.getAsObj(e);
					if (Objects.equals(defVal, w))
						return null;
					Element dataElm = document.createElement("attvalue");
					dataElm.setAttribute("for", weightId);
					dataElm.setAttribute("value", attrWriter.apply(w));
					return dataElm;
				});
			}

			Element nodesElm = null;
			if (!graph.vertices().isEmpty()) {
				nodesElm = document.createElement("nodes");
				graphElm.appendChild(nodesElm);
			}
			for (V vertex : graph.vertices()) {
				Element nodeElm = document.createElement("node");
				nodeElm.setAttribute("id", String.valueOf(vertex));

				List<Element> ws = vWeightsWriters
						.stream()
						.map(w -> w.apply(vertex))
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
				if (!ws.isEmpty()) {
					Element attvaluesElm = document.createElement("attvalues");
					ws.forEach(attvaluesElm::appendChild);
					nodeElm.appendChild(attvaluesElm);
				}
				nodesElm.appendChild(nodeElm);
			}
			Element edgesElm = null;
			if (!graph.edges().isEmpty()) {
				edgesElm = document.createElement("edges");
				graphElm.appendChild(edgesElm);
			}
			for (E edge : graph.edges()) {
				Element edgeElm = document.createElement("edge");
				edgeElm.setAttribute("id", String.valueOf(edge));
				edgeElm.setAttribute("source", String.valueOf(graph.edgeSource(edge)));
				edgeElm.setAttribute("target", String.valueOf(graph.edgeTarget(edge)));

				List<Element> ws = eWeightsWriters
						.stream()
						.map(w -> w.apply(edge))
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
				if (!ws.isEmpty()) {
					Element attvaluesElm = document.createElement("attvalues");
					ws.forEach(attvaluesElm::appendChild);
					edgeElm.appendChild(attvaluesElm);
				}

				edgesElm.appendChild(edgeElm);
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(writer));

		} catch (TransformerFactoryConfigurationError | ParserConfigurationException | TransformerException e) {
			throw new IOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <K> Pair<String, Function<Object, String>> attrWriter(Weights<K, ?> weights, Set<K> elements) {
		Supplier<Object[]> objWeights = new Supplier<>() {
			Object[] objWeightsCache;

			@Override
			public Object[] get() {
				if (objWeightsCache == null) {
					WeightsObj<K, ?> ws = (WeightsObj<K, ?>) weights;
					objWeightsCache = elements.stream().map(ws::get).toArray();
				}
				return objWeightsCache;
			}
		};
		Predicate<Class<?>> allInstanceof = cls -> {
			if (!(weights instanceof WeightsObj))
				return false;
			WeightsObj<K, ?> ws = (WeightsObj<K, ?>) weights;
			Object defVal = ws.defaultWeight();
			if (defVal != null && !cls.isInstance(defVal))
				return false;
			return Arrays.stream(objWeights.get()).allMatch(cls::isInstance);
		};

		/* check string first so that if there are no vertices/edges, string will be chosen */
		if (weights instanceof WeightsObj && allInstanceof.test(String.class))
			return Pair.of("string", w -> (String) w);

		if (weights instanceof WeightsByte || allInstanceof.test(Byte.class))
			return Pair.of("byte", w -> String.valueOf(((Byte) w).byteValue()));
		if (weights instanceof WeightsShort || allInstanceof.test(Short.class))
			return Pair.of("short", w -> String.valueOf(((Short) w).shortValue()));
		if (weights instanceof WeightsInt || allInstanceof.test(Integer.class))
			return Pair.of("integer", w -> String.valueOf(((Integer) w).intValue()));
		if (weights instanceof WeightsLong || allInstanceof.test(Long.class))
			return Pair.of("long", w -> String.valueOf(((Long) w).longValue()));
		if (weights instanceof WeightsFloat || allInstanceof.test(Float.class))
			return Pair.of("float", w -> String.valueOf(((Float) w).floatValue()));
		if (weights instanceof WeightsDouble || allInstanceof.test(Double.class))
			return Pair.of("double", w -> String.valueOf(((Double) w).doubleValue()));
		if (weights instanceof WeightsBool || allInstanceof.test(Boolean.class))
			return Pair.of("boolean", w -> String.valueOf(((Boolean) w).booleanValue()));
		if (weights instanceof WeightsChar || allInstanceof.test(Character.class))
			return Pair.of("char", w -> String.valueOf(((Character) w).charValue()));

		WeightsObj<K, ?> ws = (WeightsObj<K, ?>) weights;
		Predicate<Class<?>> allListOf = cls -> {
			Object defVal = ws.defaultWeight();
			Predicate<Object> isListOf = o -> o instanceof List && ((List<?>) o).stream().allMatch(cls::isInstance);
			if (defVal != null && !isListOf.test(defVal))
				return false;
			return Arrays.stream(objWeights.get()).allMatch(isListOf);
		};

		if (allInstanceof.test(Date.class))
			return Pair.of("date", w -> dateFormat.format((Date) w));
		if (allInstanceof.test(URI.class))
			return Pair.of("anyURI", w -> ((URI) w).toString());
		if (allInstanceof.test(BigDecimal.class))
			return Pair.of("bigdecimal", w -> ((BigDecimal) w).toString());
		if (allInstanceof.test(BigInteger.class))
			return Pair.of("biginteger", w -> ((BigInteger) w).toString());

		if (allInstanceof.test(byte[].class))
			return Pair.of("listbyte", w -> Arrays.toString(Objects.requireNonNull((byte[]) w)));
		if (allInstanceof.test(ByteList.class))
			return Pair.of("listbyte", w -> ((ByteList) w).toString());
		if (allListOf.test(Byte.class))
			return Pair.of("listbyte", w -> requireNonNullList((List<Byte>) w).toString());

		if (allInstanceof.test(short[].class))
			return Pair.of("listshort", w -> Arrays.toString(Objects.requireNonNull((short[]) w)));
		if (allInstanceof.test(ShortList.class))
			return Pair.of("listshort", w -> ((ShortList) w).toString());
		if (allListOf.test(Short.class))
			return Pair.of("listshort", w -> requireNonNullList((List<Short>) w).toString());

		if (allInstanceof.test(int[].class))
			return Pair.of("listinteger", w -> Arrays.toString(Objects.requireNonNull((int[]) w)));
		if (allInstanceof.test(IntList.class))
			return Pair.of("listinteger", w -> ((IntList) w).toString());
		if (allListOf.test(Integer.class))
			return Pair.of("listinteger", w -> requireNonNullList((List<Integer>) w).toString());

		if (allInstanceof.test(long[].class))
			return Pair.of("listlong", w -> Arrays.toString(Objects.requireNonNull((long[]) w)));
		if (allInstanceof.test(LongList.class))
			return Pair.of("listlong", w -> ((LongList) w).toString());
		if (allListOf.test(Long.class))
			return Pair.of("listlong", w -> requireNonNullList((List<Long>) w).toString());

		if (allInstanceof.test(float[].class))
			return Pair.of("listfloat", w -> Arrays.toString(Objects.requireNonNull((float[]) w)));
		if (allInstanceof.test(FloatList.class))
			return Pair.of("listfloat", w -> ((FloatList) w).toString());
		if (allListOf.test(Float.class))
			return Pair.of("listfloat", w -> requireNonNullList((List<Float>) w).toString());

		if (allInstanceof.test(double[].class))
			return Pair.of("listdouble", w -> Arrays.toString(Objects.requireNonNull((double[]) w)));
		if (allInstanceof.test(DoubleList.class))
			return Pair.of("listdouble", w -> ((DoubleList) w).toString());
		if (allListOf.test(Double.class))
			return Pair.of("listdouble", w -> requireNonNullList((List<Double>) w).toString());

		if (allInstanceof.test(boolean[].class))
			return Pair.of("listboolean", w -> Arrays.toString(Objects.requireNonNull((boolean[]) w)));
		if (allInstanceof.test(BooleanList.class))
			return Pair.of("listboolean", w -> ((BooleanList) w).toString());
		if (allListOf.test(Boolean.class))
			return Pair.of("listboolean", w -> requireNonNullList((List<Boolean>) w).toString());

		if (allInstanceof.test(char[].class))
			return Pair.of("listchar", w -> Arrays.toString(Objects.requireNonNull((char[]) w)));
		if (allInstanceof.test(CharList.class))
			return Pair.of("listchar", w -> ((CharList) w).toString());
		if (allListOf.test(Character.class))
			return Pair.of("listchar", w -> requireNonNullList((List<Character>) w).toString());

		if (allInstanceof.test(String[].class))
			return Pair.of("liststring", w -> Arrays.toString(requireNonNullArr((String[]) w)));
		if (allListOf.test(String.class))
			return Pair.of("liststring", w -> requireNonNullList((List<String>) w).toString());

		if (allInstanceof.test(BigDecimal[].class))
			return Pair.of("listbigdecimal", w -> Arrays.toString(requireNonNullArr((BigDecimal[]) w)));
		if (allListOf.test(BigDecimal.class))
			return Pair.of("listbigdecimal", w -> requireNonNullList((List<BigDecimal>) w).toString());

		if (allInstanceof.test(BigInteger[].class))
			return Pair.of("listbiginteger", w -> Arrays.toString(requireNonNullArr((BigInteger[]) w)));
		if (allListOf.test(BigInteger.class))
			return Pair.of("listbiginteger", w -> requireNonNullList((List<BigInteger>) w).toString());

		return null;
	}

	private static <T> T[] requireNonNullArr(T[] arr) {
		Objects.requireNonNull(arr);
		for (T t : arr)
			Objects.requireNonNull(t);
		return arr;
	}

	private static <T> List<T> requireNonNullList(List<T> list) {
		Objects.requireNonNull(list);
		for (T t : list)
			Objects.requireNonNull(t);
		return list;
	}

}
