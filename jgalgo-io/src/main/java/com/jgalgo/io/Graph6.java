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

import static com.jgalgo.internal.util.Range.range;
import java.io.IOException;
import it.unimi.dsi.fastutil.ints.IntIntPair;

class Graph6 {

	private Graph6() {}

	static IntIntPair readNumberOfVertices(byte[] bytes, int cursor) {
		if (bytes[cursor] != 126) {
			/* n is a single byte number */
			int n = checkByte(bytes[cursor++]) - 63;
			return IntIntPair.of(n, cursor);

		} else if (++cursor + 2 >= bytes.length) {
			throw new IllegalArgumentException("Invalid number of vertices, expected 126 and further bytes");
		} else if (bytes[cursor] != 126) {
			/* n is a 3 byte number */
			int b1 = checkByte(bytes[cursor++]) - 63;
			int b2 = checkByte(bytes[cursor++]) - 63;
			int b3 = checkByte(bytes[cursor++]) - 63;
			int n = 0;
			n |= b1 << 12;
			n |= b2 << 6;
			n |= b3 << 0;
			return IntIntPair.of(n, cursor);

		} else if (++cursor + 5 >= bytes.length) {
			throw new IllegalArgumentException("Invalid number of vertices, expected 126 126 and further bytes");
		} else {
			/* n is a 6 byte number */
			int b1 = checkByte(bytes[cursor++]) - 63;
			int b2 = checkByte(bytes[cursor++]) - 63;
			int b3 = checkByte(bytes[cursor++]) - 63;
			int b4 = checkByte(bytes[cursor++]) - 63;
			int b5 = checkByte(bytes[cursor++]) - 63;
			int b6 = checkByte(bytes[cursor++]) - 63;
			long n = 0;
			n |= ((long) b1) << 30;
			n |= ((long) b2) << 24;
			n |= ((long) b3) << 18;
			n |= ((long) b4) << 12;
			n |= ((long) b5) << 6;
			n |= ((long) b6) << 0;
			if (n > Integer.MAX_VALUE)
				throw new IllegalArgumentException("n is too big: " + n);
			return IntIntPair.of((int) n, cursor);
		}
	}

	static void writeNumberOfVertices(Writer2 out, int n) throws IOException {
		assert n >= 0;
		if (n <= 62) {
			/* n is a single byte number */
			out.appendByte((byte) (n + 63));

		} else if (n <= 258047) {
			/* n is a 3 byte number */
			int b1 = (n >> 12) & ((1 << 6) - 1);
			int b2 = (n >> 6) & ((1 << 6) - 1);
			int b3 = (n >> 0) & ((1 << 6) - 1);
			out.appendByte((byte) 126);
			out.appendByte((byte) (b1 + 63));
			out.appendByte((byte) (b2 + 63));
			out.appendByte((byte) (b3 + 63));

		} else {
			/* n is a 6 byte number */
			int b1 = (n >> 30) & ((1 << 6) - 1);
			int b2 = (n >> 24) & ((1 << 6) - 1);
			int b3 = (n >> 18) & ((1 << 6) - 1);
			int b4 = (n >> 12) & ((1 << 6) - 1);
			int b5 = (n >> 6) & ((1 << 6) - 1);
			int b6 = (n >> 0) & ((1 << 6) - 1);
			out.appendByte((byte) 126);
			out.appendByte((byte) 126);
			out.appendByte((byte) (b1 + 63));
			out.appendByte((byte) (b2 + 63));
			out.appendByte((byte) (b3 + 63));
			out.appendByte((byte) (b4 + 63));
			out.appendByte((byte) (b5 + 63));
			out.appendByte((byte) (b6 + 63));
		}
	}

	static class BitsReader {

		private final byte[] bytes;
		private byte currentByte;
		private int cursor;
		private int currentBit;

		BitsReader(byte[] bytes, int cursor) {
			this.bytes = bytes;
			this.cursor = cursor;
			if (cursor < bytes.length) {
				currentByte = (byte) (checkByte(bytes[cursor]) - 63);
				currentBit = 0;
			} else {
				currentBit = 6;
			}
		}

		boolean hasNext() {
			return currentBit < 6;
		}

		boolean next() {
			boolean ret = (currentByte & (1 << (/* bigendian */ 5 - currentBit))) != 0;
			currentBit++;
			if (currentBit == 6) {
				if (++cursor < bytes.length) {
					currentByte = (byte) (checkByte(bytes[cursor]) - 63);
					currentBit = 0;
				}
			}
			return ret;
		}

		void skipToCurrentByteEnd() {
			if (currentBit == 0)
				return;
			if (++cursor < bytes.length) {
				currentByte = (byte) (checkByte(bytes[cursor]) - 63);
				currentBit = 0;
			} else {
				currentBit = 6;
			}
		}
	}

	static class BitsWriter {
		private final Writer2 out;
		private byte currentByte;
		private int currentBitNum;

		BitsWriter(Writer2 out) {
			this.out = out;
		}

		void write(boolean b) throws IOException {
			if (b)
				currentByte |= 1 << (/* bigendian */ 5 - currentBitNum);
			currentBitNum++;

			if (currentBitNum == 6) {
				out.appendByte((byte) (currentByte + 63));
				currentByte = 0;
				currentBitNum = 0;
			}
		}

		void write(int x, int bitsNum) throws IOException {
			assert x < (1 << bitsNum);
			for (int i : range(bitsNum))
				write((x & (1 << (bitsNum - 1 - i))) != 0); /* bigendian */
		}

		boolean paddingRequired() {
			return currentBitNum != 0;
		}
	}

	static byte checkByte(byte b) {
		if (!(63 <= b && b <= 126))
			throw new IllegalArgumentException("Invalid byte, not in range [63, 126]: " + b);
		return b;
	}

}
