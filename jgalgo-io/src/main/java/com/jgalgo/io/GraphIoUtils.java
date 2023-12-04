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

}
