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
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongFunction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;

class GraphIoUtils {

	private GraphIoUtils() {}

	static Iterable<String> lines(BufferedReader reader, boolean trim) {
		return () -> new Iterator<>() {
			private String nextLine = null;

			@Override
			public boolean hasNext() {
				if (nextLine != null)
					return true;
				try {
					nextLine = reader.readLine();
					if (nextLine == null)
						return false;
					if (trim)
						nextLine = nextLine.trim();
					return true;
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public String next() {
				if (!hasNext())
					throw new NoSuchElementException();
				String line = nextLine;
				nextLine = null;
				return line;
			}
		};
	}

	static BufferedReader bufferedReader(Reader reader) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}

	abstract static class AbstractGraphReader<V, E> implements GraphReader<V, E> {

		@Override
		public GraphBuilder<V, E> readIntoBuilder(Reader reader) {
			try {
				return readIntoBuilderImpl(reader);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		abstract GraphBuilder<V, E> readIntoBuilderImpl(Reader reader) throws IOException;
	}

	abstract static class AbstractIntGraphReader extends AbstractGraphReader<Integer, Integer> {

		@Override
		public IntGraph readGraph(Reader reader) {
			return (IntGraph) super.readGraph(reader);
		}

		@Override
		public IntGraph readGraph(File file) {
			return (IntGraph) super.readGraph(file);
		}

		@Override
		public IntGraph readGraph(String path) {
			return (IntGraph) super.readGraph(path);
		}

		@Override
		public final IntGraphBuilder readIntoBuilder(Reader reader) {
			return (IntGraphBuilder) super.readIntoBuilder(reader);
		}

		@Override
		abstract IntGraphBuilder readIntoBuilderImpl(Reader reader) throws IOException;
	}

	abstract static class AbstractGraphWriter<V, E> implements GraphWriter<V, E> {

		@Override
		public final void writeGraph(Graph<V, E> graph, Writer writer) {
			try {
				writeGraphImpl(graph, writer);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		abstract void writeGraphImpl(Graph<V, E> graph, Writer writer) throws IOException;
	}

	static char parseChar(String s) {
		if (s.length() != 1)
			throw new IllegalArgumentException("expected a string of a single character, got: '" + s + "'");
		return s.charAt(0);
	}

	@SuppressWarnings("unchecked")
	static <K> Function<String, K> defaultParser(Class<K> type) {
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

	static <E> Function<Set<E>, E> defaultEdgeSupplier(Class<E> edgeType) {
		if (edgeType == byte.class || edgeType == Byte.class) {
			long min = Byte.MIN_VALUE, max = Byte.MAX_VALUE, maxEdgesSize = 1 << Byte.SIZE;
			return defaultEdgeSupplier(min, max, maxEdgesSize, x -> Byte.valueOf((byte) x));

		} else if (edgeType == short.class || edgeType == Short.class) {
			long min = Short.MIN_VALUE, max = Short.MAX_VALUE, maxEdgesSize = 1 << Short.SIZE;
			return defaultEdgeSupplier(min, max, maxEdgesSize, x -> Short.valueOf((short) x));

		} else if (edgeType == int.class || edgeType == Integer.class) {
			long min = Integer.MIN_VALUE, max = Integer.MAX_VALUE, maxEdgesSize = 1L << Integer.SIZE;
			return defaultEdgeSupplier(min, max, maxEdgesSize, x -> Integer.valueOf((int) x));

		} else if (edgeType == long.class || edgeType == Long.class) {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			return defaultEdgeSupplier(min, max, maxEdgesSize, x -> Long.valueOf(x));

		} else if (edgeType == float.class || edgeType == Float.class) {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			return defaultEdgeSupplier(min, max, maxEdgesSize, x -> Float.valueOf(x));

		} else if (edgeType == double.class || edgeType == Double.class) {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			return defaultEdgeSupplier(min, max, maxEdgesSize, x -> Double.valueOf(x));

		} else if (edgeType == String.class) {
			long min = Long.MIN_VALUE, max = Long.MAX_VALUE, maxEdgesSize = 1L << 48;
			return defaultEdgeSupplier(min, max, maxEdgesSize, x -> ("e" + x));

		} else {
			throw new IllegalArgumentException("no default edge supplier for type: " + edgeType);
		}
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

}
