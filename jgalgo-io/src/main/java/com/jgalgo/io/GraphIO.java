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
import java.util.List;
import java.util.Map;
import com.jgalgo.Graph;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class GraphIO {

	// TODO make this method public
	static Graph readGraph(String path) {
		GraphReader reader = getFormat(path).newReader();
		return reader.readGraph(new File(path));
	}

	// TODO make this method public
	static void writeGraph(Graph graph, String path) {
		GraphWriter writer = getFormat(path).newWriter();
		writer.writeGraph(graph, new File(path));
	}

	private static GraphFormat getFormat(String path) {
		String extension = getFileExtension(path);
		if (extension == null)
			throw new IllegalArgumentException("can't determinate file extension in path: " + path);
		GraphFormat format = FileExtensionToFormat.get(extension);
		if (format == null)
			throw new IllegalArgumentException("file extension does not match any format: '" + extension + "'");
		return format;
	}

	private static String getFileExtension(String path) {
		int dotIdx = path.lastIndexOf('.');
		return dotIdx == -1 ? null : path.substring(dotIdx + 1);
	}

	private static final Map<String, GraphFormat> FileExtensionToFormat;
	static {
		List<GraphFormat> formats = new ObjectArrayList<>();
		formats.add(FormatCSV.Instance);
		formats.add(FormatDIMACS.Instance);
		formats.add(FormatGEXF.Instance);
		formats.add(FormatGML.Instance);
		formats.add(FormatGraphML.Instance);
		formats.add(FormatLEDA.Instance);
		formats.add(FormatGraph6.Instance);
		formats.add(FormatSparse6.Instance);

		Object2ObjectMap<String, GraphFormat> fileExtensionToFormat = new Object2ObjectArrayMap<>();
		for (GraphFormat format : formats)
			fileExtensionToFormat.put(format.getFileExtension(), format);
		FileExtensionToFormat = Object2ObjectMaps.unmodifiable(fileExtensionToFormat);
	}

}
