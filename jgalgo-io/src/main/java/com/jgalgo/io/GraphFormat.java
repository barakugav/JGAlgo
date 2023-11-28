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
import it.unimi.dsi.fastutil.objects.ObjectList;

abstract class GraphFormat {

	private final List<String> fileExtensions;

	GraphFormat(String fileExtension, String... fileExtensions) {
		String[] fileExtensions0 = new String[1 + fileExtensions.length];
		fileExtensions0[0] = fileExtension;
		System.arraycopy(fileExtensions, 0, fileExtensions0, 1, fileExtensions.length);
		this.fileExtensions = ObjectList.of(fileExtensions0);
	}

	abstract GraphWriter newWriter();

	abstract GraphReader newReader();

	final List<String> getFileExtensions() {
		return fileExtensions;
	}

	static GraphFormat getInstanceByName(String formatName) {
		GraphFormat format = GraphFormats.Formats.get(formatName.toLowerCase());
		if (format == null)
			throw new IllegalArgumentException("unsupported format: " + formatName);
		return format;
	}

}
