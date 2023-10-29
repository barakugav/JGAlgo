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

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;

class FormatGEXF implements GraphFormat {

	private FormatGEXF() {}

	static final FormatGEXF Instance = new FormatGEXF();

	@Override
	public GraphWriter newWriter() {
		return new WriterImpl();
	}

	@Override
	public GraphReader newReader() {
		return new ReaderImpl();
	}

	private static final List<String> FILE_EXTENSIONS = List.of("gexf");

	@Override
	public List<String> getFileExtensions() {
		return FILE_EXTENSIONS;
	}

	private static class WriterImpl implements GraphWriter {

		@Override
		public void writeGraph(IntGraph graph, Writer writer) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'writeGraph'");
		}

	}

	private static class ReaderImpl implements GraphReader {

		@Override
		public IntGraphBuilder readIntoBuilder(Reader reader) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'readIntoBuilder'");
		}

	}

}
