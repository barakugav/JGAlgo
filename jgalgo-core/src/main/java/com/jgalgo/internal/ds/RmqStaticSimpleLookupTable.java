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

package com.jgalgo.internal.ds;

import static com.jgalgo.internal.util.Range.range;
import java.util.Objects;

/**
 * Naive lookup table for RMQ queries.
 *
 * <p>
 * For a sequence of size \(n\), a lookup table of size \(O(n^2)\) will be constructed, such that for each two indices
 * \(i,j\) the index of the minimum element in the range \([i,j]\) can be simply accessed from the table in \(O(1)\)
 * time.
 *
 * <p>
 * This algorithm require \(O(n^2)\) pre processing time and space, and answer queries in \(O(1)\).
 *
 * @author Barak Ugav
 */
class RmqStaticSimpleLookupTable implements RmqStatic {

	/**
	 * Construct a new static RMQ algorithm object.
	 */
	RmqStaticSimpleLookupTable() {}

	@Override
	public RmqStatic.DataStructure preProcessSequence(RmqStaticComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException("Invalid length: " + n);
		Objects.requireNonNull(c);

		if (n <= DSu08.LIMIT)
			return new DSu08(c, (byte) n);
		else if (n <= DSu16.LIMIT)
			return new DSu16(c, (short) n);
		else if (n <= DSu32.LIMIT)
			return new DSu32(c, n);
		else
			throw new IllegalArgumentException("N too big (" + n + ")");
	}

	private static int arrSize(int n) {
		return n * (n - 1) / 2;
	}

	private static int indexOf(int n, int i, int j) {
		return (2 * n - i - 1) * i / 2 + j - i - 1;
	}

	private static class DSu08 implements RmqStatic.DataStructure {

		private final byte n;
		private final byte[] arr;
		private static final int LIMIT = 1 << ((Byte.SIZE - 1) / 2);

		DSu08(RmqStaticComparator c, byte n) {
			arr = new byte[arrSize(n)];
			this.n = n;

			for (byte i = 0; i < n - 1; i++)
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : (byte) (i + 1);
			for (byte i = 0; i < n - 2; i++) {
				for (byte j = (byte) (i + 2); j < n; j++) {
					byte m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			RmqStatics.checkIndices(i, j, n);
			if (i == j)
				return i;
			return arr[indexOf(n, i, j)];
		}

		@Override
		public long sizeInBytes() {
			return 1 + 8 + arr.length;
		}
	}

	private static class DSu16 implements RmqStatic.DataStructure {

		private final short n;
		private final short[] arr;

		private static final int LIMIT = 1 << ((Short.SIZE - 1) / 2);

		DSu16(RmqStaticComparator c, short n) {
			arr = new short[arrSize(n)];
			this.n = n;

			for (short i = 0; i < n - 1; i++)
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : (short) (i + 1);
			for (short i = 0; i < n - 2; i++) {
				for (short j = (short) (i + 2); j < n; j++) {
					short m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			RmqStatics.checkIndices(i, j, n);
			if (i == j)
				return i;
			return arr[indexOf(n, i, j)];
		}

		@Override
		public long sizeInBytes() {
			return 2 + 8 + 2 * arr.length;
		}
	}

	private static class DSu32 implements RmqStatic.DataStructure {

		private final int n;
		private final int[] arr;

		private static final int LIMIT = 1 << ((Integer.SIZE - 1) / 2);

		DSu32(RmqStaticComparator c, int n) {
			arr = new int[arrSize(n)];
			this.n = n;

			for (int i : range(n - 1))
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : i + 1;
			for (int i : range(n - 2)) {
				for (int j : range(i + 2, n)) {
					int m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			RmqStatics.checkIndices(i, j, n);
			if (i == j)
				return i;
			return arr[indexOf(n, i, j)];
		}

		@Override
		public long sizeInBytes() {
			return 4 + 8 + 4 * arr.length;
		}
	}

}
