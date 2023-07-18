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

import java.util.List;

interface GraphFormat {

	GraphWriter newWriter();

	GraphReader newReader();

	List<String> getFileExtensions();

	static GraphFormat getInstanceByName(String formatName) {
		switch (formatName.toLowerCase()) {
			case "csv":
				return FormatCSV.Instance;
			case "dimacs":
				return FormatDIMACS.Instance;
			case "gexf":
				return FormatGEXF.Instance;
			case "gml":
				return FormatGML.Instance;
			case "graph6":
				return FormatGraph6.Instance;
			case "graphml":
				return FormatGraphML.Instance;
			case "leda":
				return FormatLEDA.Instance;
			case "space6":
				return FormatSparse6.Instance;
			default:
				throw new IllegalArgumentException("unsupported format: " + formatName);
		}
	}

}
