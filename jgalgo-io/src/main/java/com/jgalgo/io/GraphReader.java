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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;

/**
 * A reader that reads Graphs from files/IO-reader.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @see        GraphWriter
 * @author     Barak Ugav
 */
public interface GraphReader<V, E> {

	/**
	 * Read a graph from an I/O reader.
	 *
	 * @param  reader               an I/O reader that contain a graph description
	 * @return                      a new graph read from the reader
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	default Graph<V, E> readGraph(Reader reader) {
		return readIntoBuilder(reader).build();
	}

	/**
	 * Read a graph from a file.
	 *
	 * @param  file                 a file that contain a graph description
	 * @return                      a new graph read from the file
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	default Graph<V, E> readGraph(File file) {
		try (Reader reader = new FileReader(file, GraphIoUtils.JGALGO_CHARSET)) {
			return readGraph(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Read a graph from a file, given a path to it.
	 *
	 * @param  path                 a path to a file that contain a graph description
	 * @return                      a new graph read from the file
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	default Graph<V, E> readGraph(String path) {
		try (Reader reader = new FileReader(path, GraphIoUtils.JGALGO_CHARSET)) {
			return readGraph(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Read a graph from an I/O reader into a {@link GraphBuilder}.
	 *
	 * @param  reader               an I/O reader that contain a graph description
	 * @return                      a graph builder containing the vertices and edge read from the reader
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	GraphBuilder<V, E> readIntoBuilder(Reader reader);

}
