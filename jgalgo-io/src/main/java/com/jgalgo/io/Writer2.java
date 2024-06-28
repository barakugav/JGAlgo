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

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * A wrapper for {@link Writer} that provides a fluent API for writing to the underlying writer.
 *
 * <p>
 * {@link Writer} accept only strings and characters, and so this class provides methods for appending other types such
 * as primitives and objects.
 *
 * @author Barak Ugav
 */
class Writer2 {

	private final Writer writer;

	Writer2(Writer writer) {
		this.writer = Objects.requireNonNull(writer);
	}

	Writer2 append(String s) throws IOException {
		writer.append(s);
		return this;
	}

	Writer2 append(char c) throws IOException {
		writer.append(c);
		return this;
	}

	Writer2 append(byte b) throws IOException {
		writer.append(Byte.toString(b));
		return this;
	}

	Writer2 append(short s) throws IOException {
		writer.append(Short.toString(s));
		return this;
	}

	Writer2 append(int i) throws IOException {
		writer.append(Integer.toString(i));
		return this;
	}

	Writer2 append(long l) throws IOException {
		writer.append(Long.toString(l));
		return this;
	}

	Writer2 append(float f) throws IOException {
		writer.append(Float.toString(f));
		return this;
	}

	Writer2 append(double d) throws IOException {
		writer.append(Double.toString(d));
		return this;
	}

	Writer2 append(boolean b) throws IOException {
		writer.append(Boolean.toString(b));
		return this;
	}

	Writer2 append(Object o) throws IOException {
		writer.append(Objects.toString(o));
		return this;
	}

	Writer2 appendNewline() throws IOException {
		writer.append(System.lineSeparator());
		return this;
	}

	Writer2 appendByte(byte b) throws IOException {
		writer.append(new String(new byte[] { b }, GraphIoUtils.JGALGO_CHARSET));
		return this;
	}

	Writer2 appendBytes(byte[] bytes) throws IOException {
		writer.append(new String(bytes, GraphIoUtils.JGALGO_CHARSET));
		return this;
	}

}
