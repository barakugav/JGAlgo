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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import com.jgalgo.graph.Graph;

/**
 * A writer that writes Graphs to files/IO-writer.
 *
 * @see    GraphReader
 * @author Barak Ugav
 */
public interface GraphWriter {

	/**
	 * Write a graph to an I/O writer.
	 *
	 * @param graph  a graph
	 * @param writer an I/O writer to which the graph description will be written to
	 */
	void writeGraph(Graph graph, Writer writer);

	/**
	 * Write a graph to a file.
	 *
	 * @param graph a graph
	 * @param file  a file descriptor to which the graph will be written to
	 */
	default void writeGraph(Graph graph, File file) {
		try (Writer writer = new FileWriter(file, GraphIO.JGALGO_CHARSET)) {
			writeGraph(graph, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write a graph to a file, given its path.
	 *
	 * @param graph a graph
	 * @param path  a path to a file to which the graph will be written to
	 */
	default void writeGraph(Graph graph, String path) {
		try (Writer writer = new FileWriter(path, GraphIO.JGALGO_CHARSET)) {
			writeGraph(graph, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get new {@link GraphWriter} instance by a format name.
	 * <p>
	 * Any one of the following formats is supported: ['csv', 'dimacs', 'gexf', 'gml', 'graph6', 'space6', 'graphml',
	 * 'leda']
	 *
	 * @param  format the name of the format
	 * @return        a writer that can write graphs with the given format
	 */
	static GraphWriter newInstance(String format) {
		return GraphFormat.getInstanceByName(format).newWriter();
	}

}
