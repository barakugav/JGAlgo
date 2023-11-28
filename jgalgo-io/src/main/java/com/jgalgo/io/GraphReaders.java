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
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class GraphReaders {

	private GraphReaders() {}

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

}
