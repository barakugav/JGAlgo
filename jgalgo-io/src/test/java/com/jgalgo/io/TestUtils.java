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

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;

class TestUtils {

	// static String bytesStr(byte... bytes) {
	// return new String(bytes, GraphIoUtils.JGALGO_CHARSET);
	// }

	// static String bytesStr(char... bytes) {
	// BytesBuilder b = new BytesBuilder();
	// for (char c : bytes)
	// b.add(c);
	// return b.str();
	// }

	static String bytesStr(String s) {
		BytesBuilder b = new BytesBuilder();
		for (char c : s.toCharArray())
			b.add(c);
		return b.str();
	}

	static class BytesBuilder {

		private final ByteList bytes = new ByteArrayList();

		// void add(byte b) {
		// bytes.add(b);
		// }

		// void add(byte... bytes) {
		// for (byte b : bytes)
		// add(b);
		// }

		void add(char b) {
			assert b >= 0 && b <= 255;
			bytes.add((byte) b);
		}

		void add(int b) {
			assert b >= 0 && b <= 255;
			bytes.add((byte) b);
		}

		void add(int... bytes) {
			for (int b : bytes)
				add(b);
		}

		String str() {
			return new String(bytes.toByteArray(), GraphIoUtils.JGALGO_CHARSET);
		}

		String strAndClear() {
			String s = str();
			clear();
			return s;
		}

		@Override
		public String toString() {
			return bytes.toString();
		}

		void clear() {
			bytes.clear();
		}
	}

}
