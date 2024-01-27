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

import java.nio.charset.Charset;

class GraphFormats {

	private GraphFormats() {}

	// TODO make this method public
	// static IntGraph readGraph(String path) {
	// GraphReader reader = getFormat(path).newReader();
	// return reader.readGraph(new File(path));
	// }

	// TODO make this method public
	// static void writeGraph(IntGraph graph, String path) {
	// GraphWriter writer = getFormat(path).newWriter();
	// writer.writeGraph(graph, new File(path));
	// }

	// private static GraphFormat getFormat(String path) {
	// String extension = getFileExtension(path);
	// if (extension == null)
	// throw new IllegalArgumentException("can't determinate file extension in path: " + path);
	// GraphFormat format = FileExtensionToFormat.get(extension);
	// if (format == null)
	// throw new IllegalArgumentException("file extension does not match any format: '" + extension + "'");
	// return format;
	// }

	// private static String getFileExtension(String path) {
	// int dotIdx = path.lastIndexOf('.');
	// return dotIdx < 0 ? null : path.substring(dotIdx + 1);
	// }

	// static final Map<String, GraphFormat> Formats;

	// static {
	// Object2ObjectMap<String, GraphFormat> formats = new Object2ObjectOpenHashMap<>();
	// formats.put("csv", new GraphFormat("csv") {
	// @Override
	// <V, E> GraphWriter<V, E> newWriter() {
	// throw new UnsupportedOperationException();
	// }

	// @Override
	// <V, E> GraphReader<V, E> newReader() {
	// throw new UnsupportedOperationException();
	// }
	// });
	// formats.put("dimacs", new GraphFormat("col", "gr") {
	// @Override
	// public <V, E> GraphWriter<V, E> newWriter() {
	// return new DimacsGraphWriter();
	// }

	// @Override
	// <V, E> GraphReader<V, E> newReader() {
	// return new DimacsGraphReader();
	// }
	// });
	// formats.put("gexf", new GraphFormat("gexf") {

	// @Override
	// <V, E> GraphWriter<V, E> newWriter() {
	// throw new UnsupportedOperationException();
	// }

	// @Override
	// <V, E> GraphReader<V, E> newReader() {
	// throw new UnsupportedOperationException();
	// }

	// });
	// formats.put("gml", new GraphFormat("gml") {
	// @Override
	// <V, E> GraphWriter<V, E> newWriter() {
	// return new GmlGraphWriter();
	// }

	// @Override
	// <V, E> GraphReader<V, E> newReader() {
	// return new GmlGraphReader();
	// }
	// });
	// formats.put("graph6", new GraphFormat("g6") {
	// @Override
	// <V, E> GraphWriter<V, E> newWriter() {
	// throw new UnsupportedOperationException();
	// }

	// @Override
	// <V, E> GraphReader<V, E> newReader() {
	// throw new UnsupportedOperationException();
	// }
	// });
	// formats.put("graphml", new GraphFormat("graphml") {
	// @Override
	// <V, E> GraphWriter<V, E> newWriter() {
	// throw new UnsupportedOperationException();
	// }

	// @Override
	// <V, E> GraphReader<V, E> newReader() {
	// throw new UnsupportedOperationException();
	// }
	// });
	// formats.put("leda", new GraphFormat("lgr") {
	// @Override
	// <V, E> GraphWriter<V, E> newWriter() {
	// return new LedaGraphWriter();
	// }

	// @Override
	// <V, E> GraphReader<V, E> newReader() {
	// return new LedaGraphReader();
	// }
	// });
	// formats.put("sparse6", new GraphFormat("s6") {
	// @Override
	// <V, E> GraphWriter<V, E> newWriter() {
	// throw new UnsupportedOperationException();
	// }

	// @Override
	// <V, E> GraphReader<V, E> newReader() {
	// throw new UnsupportedOperationException();
	// }
	// });
	// Formats = Object2ObjectMaps.unmodifiable(formats);
	// }

	// private static final Map<String, GraphFormat> FileExtensionToFormat;

	// static {
	// Object2ObjectMap<String, GraphFormat> fileExtensionToFormat = new Object2ObjectOpenHashMap<>();
	// for (GraphFormat format : Formats.values()) {
	// for (String ext : format.getFileExtensions()) {
	// GraphFormat f1 = fileExtensionToFormat.put(ext, format);
	// if (f1 != null)
	// throw new IllegalStateException("two formats with same file extension: '" + ext + "'");
	// }
	// }
	// FileExtensionToFormat = Object2ObjectMaps.unmodifiable(fileExtensionToFormat);
	// }

	static final Charset JGALGO_CHARSET = Charset.forName("UTF-8");

}
